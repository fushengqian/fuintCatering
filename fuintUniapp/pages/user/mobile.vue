<template>
  <view class="container">
    <view class="form">
      <!-- 手机号 -->
      <view class="form-item">
          <text class="iconfont icon-shoujihao"></text>
          <text class="pre-mobile">+86</text>
          <input class="form-item--input" style="padding-left: 12rpx;" type="number" v-model="mobile" maxlength="11" placeholder="请输入手机号码" />
      </view>
      <!-- 图形验证码 -->
      <view class="form-item">
        <text class="iconfont icon-tuxingyanzhengma"></text>
        <input class="form-item--input" type="text" v-model="captchaCode" maxlength="5" placeholder="请输入图形验证码" />
        <view class="form-item--parts">
          <view class="captcha" @click="getCaptcha()">
            <image class="image" :src="captcha"></image>
          </view>
        </view>
      </view>
      <!-- 短信验证码 -->
      <view class="form-item">
        <text class="iconfont icon-yanzhengma"></text>
        <input class="form-item--input" type="number" v-model="smsCode" maxlength="6" placeholder="请输入短信验证码" />
        <view class="form-item--parts">
          <view class="captcha-sms" @click="handelSmsCaptcha()">
            <text v-if="!smsState" class="activate">获取验证码</text>
            <text v-else class="un-activate">重新发送({{ times }})秒</text>
          </view>
        </view>
      </view>
    </view>
    
    <!-- 底部操作按钮 -->
    <view class="footer-fixed">
      <view class="btn-wrapper">
        <view class="btn-item btn-item-main" @click="doSubmit()">保存</view>
      </view>
    </view>
  </view>
</template>

<script>
  import * as UserApi from '@/api/user'
  import * as LoginApi from '@/api/login'
  import * as Verify from '@/utils/verify'
  import store from '@/store'
  
  // 倒计时时长(秒)
  const times = 60
  
  export default {
    data() {
      return {
        // 正在加载
        isLoading: false,
        // 手机号
        mobile: '',
        // 图形验证码信息
        captcha: "",
        // 图形验证码uuid
        captchaUuid: "",
        // 图形验证码
        captchaCode: '',
        // 短信验证码发送状态
        smsState: false,
        // 短信验证码
        smsCode: '',
        // 倒计时
        times,
      }
    },
    
    /**
     * 生命周期函数--监听页面加载
     */
    created() {
      // 获取图形验证码
      this.getCaptcha();
    },

    methods: {
      // 获取图形验证码
      getCaptcha() {
        const app = this
        LoginApi.captcha()
          .then(result => {
            app.captcha = result.data.captcha;
            app.captchaUuid = result.data.uuid;
            app.captchaCode = "";
        })
      },
      // 点击发送短信验证码
      handelSmsCaptcha() {
        const app = this
        if (Verify.isEmpty(app.mobile)) {
            app.$toast("请填写手机号");
            return false;
        }
        if (Verify.isEmpty(app.captchaCode)) {
            app.$toast("请填写图形验证码");
            return false;
        }
        if (!app.isLoading && !app.smsState) {
            app.sendSmsCaptcha();
        }
      },
      // 请求发送短信验证码接口
      sendSmsCaptcha() {
        const app = this
        app.isLoading = true
        LoginApi.sendSmsCaptcha({
              captchaKey: app.captcha.key,
              captchaCode: app.captchaCode,
              mobile: app.mobile,
              uuid: app.captchaUuid
          })
          .then(result => {
            // 显示发送成功
            if (result.data) {
               app.$toast(result.message)
               // 执行定时器
               app.timer()
            } else {
                app.$error(result.message)
            }
          })
          .finally(() => app.isLoading = false)
      },
      // 执行定时器
      timer() {
        const app = this
        app.smsState = true
        const inter = setInterval(() => {
          app.times = app.times - 1
          if (app.times <= 0) {
              app.smsState = false;
              app.times = times;
              clearInterval(inter);
          }
        }, 1000)
      },
      // 提交保存
      doSubmit() {
         const app = this;
         if (Verify.isEmpty(app.mobile)) {
             this.$toast('请输入您的手机号')
             return false
         }
         if (Verify.isEmpty(app.smsCode)) {
             this.$toast('请输入短信验证码')
             return false
         }
         app.isLoading = true;
         UserApi.save({"mobile": app.mobile, "verifyCode": app.smsCode})
             .then(result => {
               app.isLoading = false;
               if (result.code == 200) {
                   app.$success('保存成功！');
               } else {
                   app.$error(result.message ? result.message : '保存失败！');
               }
         }).catch(err => {
             app.isLoading = false;
         })
      }
    }
  }
</script>

<style lang="scss" scoped>
  .form {
      margin-top: 50rpx;
  }
  // 输入框元素
  .form-item {
    display: flex;
    padding: 18rpx;
    border-bottom: 2rpx solid #cccccc;
    margin-left: 20rpx;
    margin-right: 20rpx;
    margin-bottom: 25rpx;
    height: 110rpx;
    align-items: center;
    justify-content: center;
    .pre-mobile {
        line-height: 75rpx;
        color: #888888;
    }
  
    &--input {
      font-size: 26rpx;
      letter-spacing: 1rpx;
      flex: 1;
      height: 100%;
      padding-left: 5rpx;
    }
  
    &--parts {
      min-width: 100rpx;
      height: 100%;
    }
  
    // 图形验证码
    .captcha {
      height: 100%;
  
      .image {
        display: block;
        width: 192rpx;
        height: 80rpx;
      }
    }
  
    // 短信验证码
    .captcha-sms {
      font-size: 22rpx;
      line-height: 50rpx;
      padding-right: 20rpx;
  
      .activate {
        color: #cea26a;
        border: #ccc solid 1px;
        padding: 18rpx;
        border-radius: 8rpx;
      }
  
      .un-activate {
        color: #9e9e9e;
      }
    }
  }

  // 底部操作栏
  .footer-fixed {
    z-index: 11;
    box-shadow: 0 -4rpx 40rpx 0 rgba(144, 52, 52, 0.1);
    margin-top: 80rpx;
    .btn-wrapper {
      height: 100%;
      display: flex;
      text-align: center;
      align-items: center;
      padding: 0 30rpx;
      margin-bottom: 10rpx;
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
    
    .btn-item-back {
      margin-top: 20rpx;
      background: #FFFFFF;
      border: 1px solid $fuint-theme;
      color: #666666;
    }

  }
</style>
