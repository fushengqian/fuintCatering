<template>
  <view v-if="!isLoading" class="container b-f p-b">
    <view class="base">
        <view class="coupon-image">
            <image class="image" :src="detail.image"></image>
        </view>
        <view class="coupon-title">
          <view class="name">{{ detail.name }}</view>
          <view v-if="detail.amount > 0" class="price"><span class="label">面额：</span>￥{{ detail.amount }}</view>
          <view v-if="detail.type == 'P'" class="balance"><span class="label">余额：</span>￥{{ detail.balance }}</view>
          <view v-if="detail.tips" class="tips">{{ detail.tips }}</view>
          <view class="time">有效期：{{ detail.effectiveDate }}</view>
        </view>
        <view v-if="detail.status=='B'" class="icon-can"></view>
        <view v-else-if="detail.status=='C'" class="icon-cannot"></view>
    </view>
    <view class="confirm-form">
        <u-form :model="form" label-width="140rpx">
          <u-form-item class="input" v-if="detail.type === 'P'" label="金额:">
            <u-input v-model="form.amount" placeholder="核销金额" />
          </u-form-item>
          <view v-if="detail.type === 'T'" class="coupon-timer">
            <view class="tips">完成情况({{detail.confirmCount}}/{{detail.useRule}})</view>
            <uni-row class="time-row" v-for="row in dataList" :key="row.id">
                <uni-col :span="rowCount" v-for="item in row.data" :key="item.isActive" class="time-item">
                    <view v-if="item.isActive == true" class="time active"></view>
                    <view v-else class="time"></view>
                </uni-col>
            </uni-row>
          </view>
          <u-form-item class="input" label="备注:" :border-bottom="false">
            <u-input v-model="form.remark" placeholder="核销备注" />
          </u-form-item>
        </u-form>
    </view>
    <view class="coupon-content m-top20">
        <view class="title">使用须知</view>
        <view class="content"><jyf-parser :html="detail.description"></jyf-parser></view>
    </view>
    <view class="footer-fixed">
      <view v-if="detail.status == 'A'" class="btn-wrapper">
        <view class="btn-item btn-item-main" @click="doConfirm()">确定核销</view>
      </view>
      <view v-else-if="detail.status == 'B'" class="btn-wrapper">
        <view class="btn-item cannot">已经核销</view>
      </view>
      <view v-else class="btn-wrapper">
        <view class="btn-item cannot">不可核销</view>
      </view>
    </view>
    <!-- 快捷导航 -->
    <shortcut/>
  </view>
</template>

<script>
  import * as myCouponApi from '@/api/myCoupon'
  import * as confirmApi from '@/api/confirm'
  import Shortcut from '@/components/merchant-shortcut'
  
  export default {
    components: {
        Shortcut
    },
    data() {
      return {
        // 会员会员卡券编码
        code: null,
        userCouponId: null,
        userCouponCode: null,
        // 加载中
        isLoading: true,
        // 当前卡券详情
        detail: null,
        rowCount: 0,
        dataList: [],
        // 核销结果
        result: false,
        form: {'code': '', 'amount': '', 'remark': ''}
      }
    },

    /**
     * 生命周期函数--监听页面加载
     */
    onLoad(options) {
      // 记录ID
      this.userCouponId = options.id
      // 核销二维码
      this.userCouponCode = options.code
      // 获取卡券详情
      this.getCouponDetail()
    },

    methods: {
      // 获取卡券详情
      getCouponDetail() {
        const app = this
        myCouponApi.detail(0, app.userCouponId, app.userCouponCode)
          .then(result => {
            app.detail = result.data
            app.getRowCount(app.detail.useRule)
            app.getDataList(app.detail.useRule, app.detail.confirmCount)
          })
          .finally(() => app.isLoading = false)
      },
       doConfirm() {
          const app = this
          // 储值卡核销金额
          if (app.detail.type == 'P') {
              if (app.form.amount <= 0) {
                app.$error("核销金额不能小于0，请输入");
                return false
              } else if (app.form.amount > app.detail.amount) {
                app.$error("核销金额不能大于" + app.detail.amount + "，请重新输入");
                return false
              }
          }
          if (app.isLoading) {
              return false;
          }
          
          app.isLoading = true;
          confirmApi.doConfirm(app.detail.code, app.form.amount, app.form.remark)
            .then(result => {
                if (result.data) {
                    app.getCouponDetail()
                    this.$success("核销成功")
                    app.detail.status = result.data.status
                    app.detail.balance = result.data.balance
                    app.form.amount = ""
                    app.form.remark = ""
                    app.getCouponDetail()
                } else {
                    this.$error(result.message)
                }
                app.isLoading = false
                resolve(result)
            });
      },
      // 组织数据
      getDataList(num, use) {
                const app = this
                if (num <= 4 && num > 0) {
                    app.dataList = [{"data": []}]
                    for (let i = 1; i <= num; i++) {
                        app.dataList[0].data.push({"isActive": (i <= use ? true : false)})
                    }
                } else {
                    let rowCount = Math.ceil(num / 4)
                    let c = 1;
                    for (let i = 0; i < rowCount; i++) {
                        app.dataList[i] = {"data": []}
                        for (let j = 1; j <= 4; j++) {
                            if (c <= num) {
                                 app.dataList[i].data.push({"isActive": (c <= use ? true : false)})
                               c++
                          }
                        }
                    }
                }
      },
      // 计算行数
      getRowCount(num) {
         if (num < 4 && num > 0) {
             this.rowCount = 24 / num
         } else if (num >= 4) {
             this.rowCount = 6
         }
      }
    }
  }
</script>

<style lang="scss" scoped>
  .container {
    min-height: 100vh;
    background: #fff;
    color:#666666;
  }
  
  .base {
      border: dashed 5rpx #cccccc;
      padding: 30rpx;
      border-radius: 10rpx;
      margin: 20rpx;
      height: 270rpx;
      .coupon-image {
          float: left;
          margin-top: 10rpx;
          .image {
              width: 200rpx;
              height: 158rpx;
              border-radius: 8rpx;
          }
          width: 30%;
      }
      .coupon-title {
          float: left;
          margin-left: 30rpx;
          overflow: hidden;
          width: 65%;
          .name {
             font-weight: bold;
          }
          .price {
            margin-top:20rpx;
            color:#666;
            font-size: 20rpx;
            .label {
                font-size:20rpx;
            }
          }
          .balance {
              margin-top:20rpx;
              color:#f9211c;
              font-size:20rpx;
          }
          .tips {
             margin-top:20rpx;
             font-size: 20rpx;
          }
          .time {
             margin-top: 10rpx;
             font-size: 20rpx;
             color: #666666;
          }
      }
  }
  .confirm-form {
    border: dashed 5rpx #cccccc;
    padding: 30rpx;
    border-radius: 10rpx;
    margin: 20rpx;
    .input {
        border: solid 1px #ccc;
        padding-left: 20rpx;
        margin-bottom: 10rpx;
        border-radius: 10rpx;
        width: 98%;
        display: inline-flex;
    }
    .coupon-timer {
          border-radius: 10rpx;
          clear: both;
          overflow: hidden;
          margin-bottom: 10rpx;
          height: 100%;
          .tips {
              margin-bottom: 60rpx;
          }
          .time-row {
              margin-bottom: 10rpx;
              height: 100rpx;
              display: flex;
          }
          .time-item {
              padding-top: 10rpx;
              text-align: center;
              align-items: center;
              justify-content: center;
          }
          .time {
              height: 80rpx;
              margin-bottom: 30rpx;
              text-align: center;
              padding-top: 20rpx;
              border-radius: 40rpx;
              color: #ffffff;
              font-weight: bold;
              background: url('~@/static/confirm/undo.png') no-repeat center center;
              background-size: contain;
          }
          .active {
              background: url('~@/static/confirm/do.png') no-repeat center center;
              background-size: contain;
              border: solid 1px #ffffff;
          }
          min-height: 160rpx;
    }
  }
  .coupon-content {
    font-size: 28rpx;
    padding: 15rpx;
    border: dashed 5rpx #cccccc;
    border-radius: 5rpx;
    margin: 20rpx;
    min-height: 400rpx;
    .title {
        margin-bottom: 15rpx;
    }
  }
  
  .footer-fixed {
      position: fixed;
      width: 100%;
      bottom: var(--window-bottom);
      height: 180rpx;
      padding-bottom: 30rpx;
      z-index: 11;
      margin-top: 20rpx;
      .btn-wrapper {
        height: 100%;
        display: flex;
        align-items: center;
        padding: 0 20rpx;
      .cannot {
        border-radius: 38rpx;
        color: #fff;
        line-height: 80rpx;
        text-align: center;
        font-weight: 500;
        font-size: 28rpx;
        background:linear-gradient(to right, #cccccc, #cccccc)
      }
      }
  
      .btn-item {
        flex: 1;
        font-size: 28rpx;
        height: 80rpx;
        line-height: 80rpx;
        text-align: center;
        color: #ffffff;
        border-radius: 40rpx;
      }
  
      .btn-item-main {
        background: linear-gradient(to right, #f9211c, #ff6335);
      }
  }
</style>
