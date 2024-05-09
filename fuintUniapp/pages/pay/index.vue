<template>
  <view class="container b-f p-b">
    <view class="base">
        <view class="merchant-name">
          <view class="name">{{ storeInfo ? storeInfo.name : systemName }}</view>
        </view>
    </view>
    <view class="pay-form">
        <u-form :model="form" label-width="100rpx">
          <u-form-item class="input" prop="payAmount" :border-bottom="false" label="金额" rules="[{ required: true, message: '请输入支付金额', trigger: 'blur' }]">
              <view class="amount-icon">￥</view>
              <view class="amount">{{ form.payAmount }}</view>
          </u-form-item>
          <u-form-item class="input" v-if="form.remark" :border-bottom="false" label="备注">
              <u-input v-model="form.remark" type="text" placeholder="请输入备注信息"/>
          </u-form-item>
          <u-form-item :border-bottom="false">
              <view class="remark" @click="showRemarkPop()"><text class="iconfont icon-edit"></text>添加备注</view>
          </u-form-item>
        </u-form>
    </view>
    
    <neoceansoft-keyboard keyboardType="payment" behaviorBgColor="#113a28" @result="changeAmount" @paymentClick="doPay"></neoceansoft-keyboard>

    <view class="remark-popup">
       <uni-popup ref="remarkPopup" type="dialog">
          <uni-popup-dialog mode="input" focus="false" v-model="form.remark" title="备注信息" type="info" placeholder="请输入备注信息" :before-close="true" @close="cancelRemark" @confirm="doRemark"></uni-popup-dialog>
       </uni-popup>
    </view>
    
    <Popup v-if="!isLoading" v-model="showPopup" :payInfo="payInfo" @modifyChoice="modifyChoice"/>
  </view>
</template>

<script>
  import jyfParser from '@/components/jyf-parser/jyf-parser'
  import * as SettlementApi from '@/api/settlement'
  import * as settingApi from '@/api/setting'
  import PayTypeEnum from '@/common/enum/order/PayType'
  import { checkLogin, needLogin } from '@/utils/app'
  import Popup from './components/Popup'
  import config from '@/config'

  export default {
    components: {
      Popup
    },
    data() {
      return {
        systemName: config.name,
        // 加载中
        isLoading: false,
        showPopup: false,
        form: {'payAmount': '', 'remark' : ''},
        payInfo: {'usePoint': 0, 'pointAmount': 0, 'payAmount': 0, 'totalAmount': 0, 'payDiscount': 1, 'maxPoint': 0, 'couponInfo': null, 'couponAmount': 0, "isLogin": false, "canUsedAsMoney": "false"},
        canUsedAsMoney: false,
        exchangeNeedPoint: 0,
        canUsePointAmount: 0,
        storeInfo: null
      }
    },

    /**
     * 生命周期函数--监听页面显示
     */
    onShow(options) {
      this.prePay();
      this.systemConfig();
      // 判断是否已登录
      this.payInfo.isLogin = checkLogin();
    },

    methods: {
      showRemarkPop() {
        this.$refs.remarkPopup.open('top');
      },
      doRemark(remark) {
        this.form.remark = remark
        this.$refs.remarkPopup.close();
      },
      cancelRemark() {
        this.$refs.remarkPopup.close();
      },
      // 支付金额改变
      changeAmount(e) {
        this.form.payAmount = e;
      },
      systemConfig() {
        const app = this;
        settingApi.systemConfig()
          .then(result => {
              app.storeInfo = result.data.storeInfo;
          })
      },
      // 支付前查询
      prePay() {
        const app = this
        // 请求api
        SettlementApi.prePay()
          .then(result => {
              if (result.data) {
                  app.canUsedAsMoney = result.data.canUsedAsMoney;
                  app.exchangeNeedPoint = result.data.exchangeNeedPoint;
                  app.canUsePointAmount = result.data.canUsePointAmount;
                  app.payInfo.maxPoint = result.data.canUsePointAmount;
                  app.payInfo.couponInfo = result.data.canUseCouponInfo;
                  app.payInfo.couponAmount = (result.data.canUseCouponInfo === null) ? 0 : result.data.canUseCouponInfo.amount;
                  app.payInfo.usePoint = result.data.canUsePointAmount;
                  app.payInfo.totalAmount = app.form.payAmount;
                  app.payInfo.payDiscount = result.data.payDiscount;
                  app.payInfo.canUsedAsMoney = result.data.canUsedAsMoney;
                  app.payInfo.isLogin = checkLogin();
                  app.payInfo.exchangeNeedPoint = app.exchangeNeedPoint;
                  app.modifyChoice(app.payInfo);
              }
          })
          .catch(err => {
            if (err.result) {
              const errData = err.result.data;
              if (errData) {
                  return false;
              }
            }
        })  
      },
      // 修改支付选择
      modifyChoice(payInfo) {
        const app = this;
        app.payInfo = payInfo;
        if (app.canUsedAsMoney == 'true') {
            app.payInfo.usePoint = parseInt(app.payInfo.usePoint);
        }
        
        if (app.canUsedAsMoney == 'true' && parseFloat(app.form.payAmount) > 0.1) {
            app.payInfo.pointAmount = parseInt(app.payInfo.usePoint) / parseInt(app.exchangeNeedPoint);
        }
        
        app.payInfo.payAmount = parseFloat(app.form.payAmount);
        
        // 会员等级折扣金额
        if (app.payInfo.payDiscount !== null && app.payInfo.payDiscount > 0) {
            let memberDiscount = parseFloat(app.payInfo.payAmount) - (parseFloat(app.payInfo.payAmount) * parseFloat(app.payInfo.payDiscount));
            app.payInfo.payAmount = app.payInfo.payAmount - parseFloat(memberDiscount);
        }
        
        // 积分金额不能大于支付金额
        if (parseFloat(app.payInfo.pointAmount) > parseFloat(app.payInfo.payAmount)) {
            app.payInfo.pointAmount = parseFloat(app.payInfo.payAmount);
            app.payInfo.usePoint = parseInt(app.exchangeNeedPoint) * parseFloat(app.payInfo.payAmount);
        }
        
        app.payInfo.payAmount = parseFloat(app.payInfo.payAmount) - parseFloat(app.payInfo.pointAmount);
        
        let useCoupon = true;
        if (app.payInfo.payAmount <= 0) {
            app.payInfo.couponAmount = 0;
            useCoupon = false;
        }
        
        // 使用了卡券进行抵扣
        if (app.payInfo.couponInfo !== null && app.payInfo.payAmount > 0 && useCoupon) {
            if (parseFloat(app.payInfo.couponAmount) > app.payInfo.payAmount) {
                app.payInfo.couponAmount = app.payInfo.payAmount;
            }
            app.payInfo.payAmount = app.payInfo.payAmount - parseFloat(app.payInfo.couponAmount);
        }
        
        // 支付金额不能小于0
        if (app.payInfo.payAmount < 0) {
            app.payInfo.payAmount = 0;
        }
      },
      // 提交支付
      doPay() {
        const app = this;
        if (!app.payInfo.isLogin) {
            needLogin();
            return false;
        }
        app.prePay();
        if (app.form.payAmount.length < 1) {
            app.$error('支付金额不能为空');
            return false;
        }
        
        if (parseFloat(app.form.payAmount) < 0.01) {
            app.$error('支付金额不能小于0.01元');
            return false;
        }
        
        if (app.canUsedAsMoney == 'true' && parseInt(app.canUsePointAmount) > 0 && parseFloat(app.form.payAmount) > 0.1) {
            app.payInfo.usePoint = parseInt(app.canUsePointAmount);
        }
        
        if (app.canUsedAsMoney == 'true' && parseInt(app.canUsePointAmount) > 0 && parseFloat(app.form.payAmount) > 0.1) {
            app.payInfo.pointAmount = parseInt(app.canUsePointAmount) / parseInt(app.exchangeNeedPoint);
        }
        
        app.payInfo.payAmount = parseFloat(app.form.payAmount);
        // 会员等级折扣金额
        if (app.payInfo.payDiscount !== null && app.payInfo.payDiscount > 0 && parseFloat(app.form.payAmount) > 0.1) {
            let memberDiscount = parseFloat(app.payInfo.payAmount) - (parseFloat(app.payInfo.payAmount) * parseFloat(app.payInfo.payDiscount));
            app.payInfo.payAmount = app.payInfo.payAmount - parseFloat(memberDiscount);
        }
        
        // 积分金额不能大于支付金额
        if (parseFloat(app.payInfo.pointAmount) > parseFloat(app.payInfo.payAmount) && parseFloat(app.form.payAmount) > 0.01) {
            app.payInfo.pointAmount = parseFloat(app.payInfo.payAmount);
            app.payInfo.usePoint = parseInt(app.exchangeNeedPoint) * parseFloat(app.payInfo.payAmount);
        }
        
        // 减去积分金额
        app.payInfo.payAmount = parseFloat(app.payInfo.payAmount) - parseFloat(app.payInfo.pointAmount)
        if (app.payInfo.payAmount <= 0) {
            app.payInfo.couponAmount = 0;
        }
        
        // 使用卡券进行抵扣
        if (app.payInfo.couponAmount > 0 && app.payInfo.payAmount > 0 && app.payInfo.couponInfo !== null) {
            if (parseFloat(app.payInfo.couponAmount) > app.payInfo.payAmount) {
                app.payInfo.couponAmount = app.payInfo.payAmount;
            }
            app.payInfo.payAmount = app.payInfo.payAmount - parseFloat(app.payInfo.couponInfo.amount);
        }
        
        if (app.payInfo.payAmount < 0) {
            app.payInfo.payAmount = 0;
        }
        
        app.payInfo = { "remark": app.form.remark, "pointAmount": parseFloat(app.payInfo.pointAmount).toFixed(2), 
                        "usePoint": app.payInfo.usePoint, "payAmount": parseFloat(app.payInfo.payAmount).toFixed(2),
                        "maxPoint": app.payInfo.maxPoint, "couponInfo": app.payInfo.couponInfo, couponAmount: app.payInfo.couponAmount,
                        "totalAmount": app.form.payAmount, "payDiscount": app.payInfo.payDiscount, "canUsedAsMoney": app.canUsedAsMoney, "isLogin": checkLogin(),
                        "exchangeNeedPoint": app.exchangeNeedPoint };
        app.showPopup = !app.showPopup;
        return true;
      }
    }
  }
</script>

<style lang="scss" scoped>
  .container {
    min-height: 100vh;
    padding: 20rpx;
    background: #fff;
    color:#666666;
  }
  .base {
      background: $fuint-theme;
        padding: 30rpx;
        border-radius: 10rpx;
      color: #ffffff;
        margin: 20rpx;
        height: 100rpx;
        .merchant-name {
            margin-left: 30rpx;
            overflow: hidden;
          text-align: center;
          font-weight: bold;
          font-size: 30rpx;
        }
  }
  .pay-form {
      border: solid 3rpx $fuint-theme;
      padding: 30rpx;
      border-radius: 10rpx;
      margin: 60rpx 20rpx 20rpx 20rpx;
    .remark-popup {
        border: #cccccc solid 1px;
        background: red;
    }
      .input {
          padding-left: 20rpx;
        padding-right: 20rpx;
        margin-top: 30rpx;
          margin-bottom: 20rpx;
          width: 94%;
          display: inline-flex;
      }
    .amount {
        font-weight: bold;
        font-size: 70rpx;
        float: left;
        min-width: 330rpx;
        display: block;
        height: 88rpx;
        border-bottom: solid 1rpx #CCCCCC;
    }
    .amount-icon {
        font-size: 38rpx;
        font-weight: bold;
        float: left;
    }
    .remark {
        width: 100%;
        text-align: right;
    }
  }
  
  /* 底部操作栏 */
  .footer-fixed {
    position: fixed;
    bottom: var(--window-bottom);
    left: 0;
    right: 0;
    display: flex;
    height: 180rpx;
    padding-bottom: 30rpx;
    z-index: 11;
    box-shadow: 0 -4rpx 40rpx 0 rgba(144, 52, 52, 0.1);
    background: #fff;
  }
  
  .footer-container {
    width: 100%;
    display: flex;
  }
  
  // 操作按钮
  .foo-item-btn {
    flex: 1;
    .btn-wrapper {
      height: 100%;
      display: flex;
      align-items: center;
    }
  
    .btn-item {
      flex: 1;
      font-size: 28rpx;
      height: 80rpx;
      line-height: 80rpx;
      margin-right: 16rpx;
        margin-left: 16rpx;
      text-align: center;
      color: #fff;
      border-radius: 8rpx;
    }
    // 立即领取按钮
    .btn-item-main {
      background: linear-gradient(to right, #f9211c, #ff6335);
    }
  }
</style>
