<template>
  <view class="container">
    <view class="wechatapp">
      <view class="header">
        <open-data class="avatar" type="userAvatarUrl"></open-data>
      </view>
    </view>
    <view class="auth-title">申请获取以下权限</view>
    <view v-if="isProfile">
        <view class="auth-subtitle">获得您微信绑定的手机号码</view>
        <view class="login-btn">
          <button class="button-mobile btn-primary" open-type="getPhoneNumber" @getphonenumber="getPhoneNumber">授权手机号</button>
        </view>
    </view>
    <view v-if="!isProfile">
        <view class="auth-subtitle">获得你的公开信息（昵称、头像等）</view>
        <view class="login-btn">
          <button class="button btn-normal" @click.stop="getUserProfile">授权登录</button>
        </view>
    </view>
    <view class="no-login-btn">
      <button class="button btn-normal" @click="cancelLogin">暂不登录</button>
    </view>
  </view>
</template>

<script>
  import store from '@/store'
  import * as UserApi from '@/api/user'
  export default {

    data() {
      return {
        // 微信小程序登录凭证 (code)
        // 提交到后端，用于换取openid
        code: '',
        needPhone: false,
        isProfile: false
      }
    },

    created() {
      // 获取code
      this.getCode()
      
      // 获取配置
      this.getUserSetting()
    },

    methods: {

      // 获取code
      // https://developers.weixin.qq.com/miniprogram/dev/api/open-api/login/wx.login.html
      getCode() {
        return new Promise((resolve, reject) => {
          uni.login({
            provider: 'weixin',
            success: res => {
              console.log('getCode res = ', res)
              resolve(res.code)
            },
            fail: reject
          })
        })
      },
      
      getUserSetting() {
        const app = this
        return new Promise((resolve, reject) => {
            UserApi.setting()
            .then(result => {
                app.needPhone = result.data.loginNeedPhone === 'true' ? true : false
            })
            .catch(err => {
              // empty
            })
        })
      },

      // 获取微信用户信息
      getUserProfile() {
        const app = this;
        wx.canIUse('getUserProfile') && wx.getUserProfile({
          lang: 'zh_CN',
          desc: '获取用户相关信息',
          success({ userInfo }) {
            console.log('userInfo == ', userInfo)
            if (app.needPhone) {
                app.isProfile = true;
            }
            // 授权成功事件
            userInfo.type = "profile";
            app.onAuthSuccess(userInfo);
          },
          fail: function(res) {
            console.log('登录授权失败，请设置小程序隐私保护协议，返回信息： ', res);
            app.$error("抱歉，登录授权失败.");
          }
        })
      },
      
      // 获取微信绑定的手机号
      getPhoneNumber(e) {
          if (e.detail.errMsg == "getPhoneNumber:ok") {
             this.onAuthSuccess({"type": "phone", "encryptedData": e.detail.encryptedData, "iv": e.detail.iv, "sessionKey": e.detail.iv})
          }
      },
      
      // 授权成功事件
      // 这里分为两个逻辑:
      // 1.将code和userInfo提交到后端，如果存在该用户 则实现自动登录，无需再填写手机号
      // 2.如果不存在该用户, 则显示注册页面, 需填写手机号
      async onAuthSuccess(userInfo) {
        const app = this
        // 提交到后端
        store.dispatch('MpWxLogin', { code: await app.getCode(), userInfo })
          .then(result => {
            if (!app.needPhone || userInfo.type == "phone") {
                // 显示登录成功
                app.$toast(result.message)
                // 跳转回原页面
                setTimeout(() => {
                  app.onNavigateBack()
                }, 1000)
            } else {
                store.dispatch('Logout')
            }
          })
          .catch(() => {
            // 将oauth提交给父级
            app.onEmitSuccess(userInfo)
          })
      },

      // 将oauth提交给父级
      // 这里要重新获取code, 因为上一次获取的code不能复用(会报错)
      async onEmitSuccess(userInfo) {
        const app = this
        app.$emit('success', {
          oauth: 'MP-WEIXIN', // 第三方登录类型: MP-WEIXIN
          code: await app.getCode(), // 微信登录的code, 用于换取openid
          userInfo // 微信用户信息
        })
      },

      /**
       * 暂不登录
       */
      cancelLogin() {
        // 跳转回原页面
        this.onNavigateBack();
      },

      /**
       * 授权成功 跳转回原页面
       */
      onNavigateBack(delta = 1) {
        uni.navigateBack({
          delta: Number(delta)
        })
      }
    }
  }
</script>

<style lang="scss" scoped>
  .container {
    padding: 0 60rpx;
    font-size: 32rpx;
    background: #fff;
    min-height: 100vh;
  }

  .wechatapp {
    padding: 80rpx 0 48rpx;
    border-bottom: 1rpx solid #e3e3e3;
    margin-bottom: 72rpx;
    text-align: center;

    .header {
      width: 190rpx;
      height: 190rpx;
      border: 4rpx solid #fff;
      margin: 0 auto 0;
      border-radius: 50%;
      overflow: hidden;
      box-shadow: 2rpx 0 10rpx rgba(50, 50, 50, 0.3);
    }
  }

  .auth-title {
    color: #585858;
    font-size: 34rpx;
    margin-bottom: 40rpx;
  }

  .auth-subtitle {
    color: #888;
    margin-bottom: 88rpx;
    font-size: 28rpx;
  }

  .login-btn {
    padding: 0 20rpx;

    .button {
      height: 88rpx;
      line-height: 88rpx;
      background: $fuint-theme;
      color: #fff;
      font-size: 30rpx;
      border-radius: 12rpx;
      text-align: center;
    }
    .button-mobile {
        height: 88rpx;
        line-height: 88rpx;
        background: $fuint-theme;
        color: #fff;
        font-size: 30rpx;
        border-radius: 12rpx;
        text-align: center;
    }
  }


  .no-login-btn {
    margin-top: 24rpx;
    padding: 0 20rpx;

    .button {
      height: 88rpx;
      line-height: 88rpx;
      background: #dfdfdf;
      color: #fff;
      font-size: 30rpx;
      border-radius: 12rpx;
      text-align: center;
    }
  }
</style>
