<template>
  <view class="container">
    <view class="info-list">
      <view class="info-item">
        <view class="contacts">
            <text class="name"><text class="iconfont icon-bangzhu"></text> 向商家展示会员码</text>
        </view>
      </view>
    </view>
    
    <view class="info-code">
        <view class="code-text">会员号：{{ userInfo.userNo ? userInfo.userNo : '--'}}</view>
        <image class="qrcode" :src="qrCode"></image>
    </view>
    
    <!-- 底部操作按钮 -->
    <view class="footer-fixed">
      <view class="btn-wrapper">
        <view class="btn-item btn-item-main" @click="goBack()">返回主页</view>
      </view>
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
        qrCode: ""
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
        UserApi.qrCode()
          .then(result => {
            app.userInfo = result.data.userInfo;
            app.qrCode = result.data.qrCode;
            app.isLoading = false;
          })
      },
      goBack() {
          this.$navTo('pages/user/index');
      }
    }
  }
</script>

<style lang="scss" scoped>
  .info-list {
      padding-bottom: 30rpx;
      margin-top: 30rpx;
  }

  // 项目内容
  .info-item {
    margin: 20rpx auto 20rpx auto;
    padding: 30rpx;
    width: 94%;
    box-shadow: 0 1rpx 5rpx 0px rgba(0, 0, 0, 0.05);
    border-radius: 10rpx;
    background: #fff;
  }

  .contacts {
    font-size: 35rpx;
    text-align: center;
    .name {
      margin-left:0px;
      text-align: center;
    }
  }

  .info-code {
      text-align: center;
      padding: 30rpx;
      margin-bottom: 30rpx;
      .code-text{
          margin-bottom: 50rpx;
      }
      .qrcode {
         width: 360rpx;
         height: 360rpx;
         margin: 0 auto;
         border: solid 1px #ccc;
      }
  }

  // 底部操作栏
  .footer-fixed {
    z-index: 11;
    box-shadow: 0 -4rpx 40rpx 0 rgba(144, 52, 52, 0.1);
    .btn-wrapper {
      height: 100%;
      display: flex;
      text-align: center;
      align-items: center;
      padding: 0 30rpx;
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
