<template>
  <view class="container">
      <view class="success">
        <view class="result">
           <image class="icon" src="/static/pay/success.png"></image>
           <text class="text">支付成功！</text>
        </view>
        <view class="amount">￥{{ amount }}</view>
        <view class="point" v-if="point > 0 && amount > 0.1">使用{{ point }}积分</view>
      </view>
      <view v-if="false" class="attention">
        <view class="result"><image class="icon" src="/static/pay/success.png"></image>使用失败</view>
      </view>
      <view class="confirm" @click="doConfirm()">确定</view>
  </view>
</template>

<script>
  import * as MessageApi from '@/api/message'
  export default {
    data() {
      return {
        amount: 0,
        point: 0,
      }
    },

    /**
     * 生命周期函数--监听页面加载
     */
    onLoad(options) {
      // 当前页面参数
      this.amount = options.amount ? options.amount : 0
      this.point = options.point ? options.point : 0
    },

    methods: {
      
      /**
       * 确定
       * */
      doConfirm() {
          const app = this
          // #ifdef MP-WEIXIN
          MessageApi.getSubTemplate({keys: "balanceChange,pointChange"}).then(result => {
              const templateIds = result.data
              wx.requestSubscribeMessage({tmplIds: templateIds, 
              success(res) {
                  console.log("调用成功！")
              }, fail(res) {
                  console.log("调用失败:", res)
              }, complete() {
                  app.$navTo('pages/index/index')
              }})
          })
          // #endif
          // #ifndef MP-WEIXIN
             app.$navTo('pages/index/index')
          // #endif
      }
    }
  }
</script>

<style lang="scss" scoped>
      .success {
        width: 100%;
        text-align: center;
        margin-top: 200rpx;
        .result {
            font-size: 35rpx;
            text-align: center;
            padding: 10rpx 10rpx 10rpx 50rpx;
            height: 70rpx;
            .icon {
                width: 45rpx;
                height: 45rpx;
                display: inline-block;
                box-sizing: border-box;
                vertical-align: middle; 
            }
            .text {
                text-align: center;
                height: 100%;
                display: inline-block;
                box-sizing: border-box;
                vertical-align: middle;
                color: #00B83F;
                font-weight: bold;
            }
        }
        .amount {
            font-weight: bold;
            font-size: 65rpx;
            margin-top: 50rpx;
            margin-bottom: 50rpx;
            color: #000000;
        }
        .point {
            font-size: 30rpx;
        }
      }
      .attention {
        width: 100%;
        text-align: center;
        margin-top: 14rpx;
      }
      .confirm {
          flex: 1;
          font-size: 28rpx;
          height: 80rpx;
          line-height: 80rpx;
          text-align: center;
          color: #fff;
          border-radius: 40rpx;
          width: 300rpx;
          margin: 50rpx auto;
          background: $fuint-theme;
      }
</style>
