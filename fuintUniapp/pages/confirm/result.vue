<template>
  <view class="container">
      <view class="success">
        <i></i>
        <p><view>使用成功!</view></p>
        <view class="mark">核销1张端午节优惠券</view>
        <view class="price"><b>￥</b>100</view>
        <view class="mark">流水号：29283723200</view>
      </view>

      <view v-if="false" class="attention">
        <i></i>
        <p><view>使用失败!</view></p>
        <view class="mark">不对</view>
      </view>
  </view>
</template>

<script>
  import * as UserApi from '@/api/user'
  import store from '@/store'

  export default {
    data() {
      return {
        //当前页面参数
        options: {},
        // 正在加载
        isLoading: true,
        userInfo: {},
      }
    },

    /**
     * 生命周期函数--监听页面加载
     */
    onLoad(options) {
      // 当前页面参数
      this.options = options
      this.getUserInfo()
    },

    methods: {
      /**
       * 用户信息
       * */
      getUserInfo() {
        const app = this
        app.isLoading = true
        UserApi.info()
          .then(result => {
            app.userInfo = result.data.userInfo
            app.isLoading = false
          })
      },
      
      /**
       * 退出
       */
      logout() {
        store.dispatch('Logout')
        this.$navTo('pages/user/index')
      }
    }
  }
</script>

<style lang="scss" scoped>
      .success{
        width: 100%;
        text-align: center;
        margin-top: 200rpx;
        i{
          display: block;
          width: 174rpx;
          height: 174rpx;
          margin: 0 auto;
          background-size: contain;
        }
      }
      .attention{
        width: 100%;
        text-align: center;
        margin-top: 14rpx;
        i{
          display: block;
          width: 174rpx;
          height: 174rpx;
          margin: 0 auto;
          background-size: contain;
        }
      }


  // 底部操作栏
  .footer-fixed {
    height: 180rpx;
    z-index: 11;
    box-shadow: 0 -4rpx 40rpx 0 rgba(144, 52, 52, 0.1);
    padding-bottom: 40rpx;
    .btn-wrapper {
      height: 100%;
      display: flex;
      align-items: center;
      padding: 0 20rpx;
    }

    .btn-item {
      flex: 1;
      font-size: 28rpx;
      height: 80rpx;
      line-height: 80rpx;
      text-align: center;
      color: #fff;
      border-radius: 40rpx;
    }

    .btn-item-main {
      background: linear-gradient(to right, #f9211c, #ff6335);
    }

  }
</style>
