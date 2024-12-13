<template>
  <view class="container" v-if="userInfo.id">
    <view class="account-panel dis-flex flex-y-center">
      <view class="panel-lable">
        <text>会员账户余额</text>
      </view>
      <view class="panel-balance flex-box">
        <text>￥{{ userInfo.balance }}</text>
      </view>
    </view>
    <view class="recharge-panel">
      <!-- 收款金额输入框 -->
      <view class="recharge-input">
          <view class="label">收款金额</view>
          <text class="uni">￥</text>
          <input type="number" class="uni-input" placeholder="请输入收款金额（单位：元）" placeholder-class="placeholder" v-model="inputValue" @input="onChangeMoney" />
      </view>
      <view class="remark-input">
        <view class="label">备注信息</view>
        <input type="text" v-model="remark" placeholder-class="placeholder" placeholder="请输入收款备注"/>
      </view>
      <!-- 确认按钮 -->
      <view class="recharge-submit btn-submit">
        <form @submit="onSubmit">
          <button class="button" formType="submit" :disabled="disabled">立即收款</button>
        </form>
      </view>
    </view>
  </view>
</template>

<script>
  import * as UserApi from '@/api/user'
  import * as BalanceApi from '@/api/balance'
  import * as SettlementApi from '@/api/settlement'
  import { wxPayment } from '@/utils/app'

  export default {
    data() {
      return {
        // 正在加载
        isLoading: true,
        // 会员二维码
        userCode: "",
        // 会员信息
        userInfo: {},
        // 按钮禁用
        disabled: false,
        // 收款备注
        remark: '',
        // 自定义金额
        inputValue: '',
      }
    },

    /**
     * 生命周期函数--监听页面加载
     */
    onLoad(options) {
      this.userCode = options.code
      // 获取页面数据
      this.getPageData()
    },

    methods: {
      // 收款金额输入框
      onChangeMoney(e) {
        this.inputValue = e.target.value;
        this.rechargeAmount = 0;
      },

      // 获取页面数据
      getPageData() {
        const app = this
        app.isLoading = true
        Promise.all([app.getUserInfo()])
          .then(() => app.isLoading = false)
      },

      // 获取会员信息
      getUserInfo() {
        const app = this
        return new Promise((resolve, reject) => {
          UserApi.info({ code: app.userCode })
            .then(result => {
              app.userInfo = result.data.userInfo;
              resolve(app.userInfo);
            })
        })
      },

      // 立即收款
      onSubmit(e) {
        const app = this
        if (app.inputValue.length < 1) {
            app.$error('请输入收款金额！');
            return false;
        }
        
        // 按钮禁用
        app.disabled = true
        
        // 提交到后端
        SettlementApi.doCashier({ type: 'payment', cashierPayAmount: app.inputValue, cashierDiscountAmount: 0, payType: 'BALANCE', userId: app.userInfo.id, remark: app.remark })
          .then(result => app.onSubmitCallback(result))
          .catch(err => {
            if (err.result) {
                const errData = err.result.data;
                if (errData) {
                    return false;
                }
            }
        })
      },
      // 收款回调
      onSubmitCallback(result) {
          const app = this
          if (result.code != '200') {
              app.$error(result.message ? result.message : '收款失败');
              app.disabled = false;
              return false;
          }
          
          uni.showModal({
            title: '收款结果',
            content: '收款成功！',
              showCancel: false,
            success(o) {
              if (o.confirm) {
                  app.inputValue = '';
                  app.remark = '';
                  app.disabled = false;
                  app.getPageData();
              }
            }
          })
      }
    }
  }
</script>

<style>
    .placeholder {
        color: #ccc;
        font-size: 28rpx;
        font-weight: normal;
    }
</style>

<style lang="scss" scoped>
  page,
  .container {
    background: #fff;
  }

  .container {
    padding-bottom: 70rpx;
  }

  /* 账户面板 */
  .account-panel {
    width: 650rpx;
    height: 180rpx;
    margin: 50rpx auto;
    padding: 0 60rpx;
    box-sizing: border-box;
    border-radius: 8rpx;
    color: #fff;
    background: $fuint-theme;
    box-shadow: 0 5px 22px 0 rgba(0, 0, 0, 0.26);
  }

  .panel-lable {
    font-size: 32rpx;
  }

  .recharge-label {
    color: rgb(51, 51, 51);
    font-size: 32rpx;
    margin-bottom: 25rpx;
  }

  .panel-balance {
    text-align: right;
    font-size: 46rpx;
  }

  .recharge-panel {
    margin-top: 60rpx;
    padding: 0 60rpx;
  }
  .recharge-input {
    margin-top: 25rpx;
    .label {
        margin-bottom: 10rpx;
    }
    .uni {
        font-weight: bold;
        font-size: 32rpx;
    }
    input {
      border: 1rpx solid rgb(228, 228, 228);
      border-radius: 6rpx;
      padding: 20rpx;
      font-weight: bold;
      font-size: 58rpx;
    }
  }
  .remark-input {
      .label {
          margin-bottom: 10rpx;
      }
      margin-top: 50rpx;
      input {
        border: 1rpx solid rgb(228, 228, 228);
        border-radius: 6rpx;
        padding: 20rpx;
        font-size: 26rpx;
      }
  }

  /* 立即充值 */
  .recharge-submit {
    margin-top: 70rpx;
  }

  .btn-submit {
    .button {
      font-size: 30rpx;
      background: linear-gradient(to right, #f9211c, #ff6335);
      border: none;
      color: white;
      border-radius: 40rpx;
      padding: 0 120rpx;
      line-height: 3;
    }

    .button[disabled] {
      background: #ff6335;
      border-color: #ff6335;
      color: white;
    }
  }
</style>
