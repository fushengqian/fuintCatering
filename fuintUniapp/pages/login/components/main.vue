<template>
  <view class="container">      
    <!-- 页面头部 -->
    <view class="header">
      <view class="title">
           <view class="item" @click="switchLoginType('account')"><text :class="loginType === 'account' ? 'active' : ''">{{ accountTitle }}</text></view>
           <view class="item" @click="switchLoginType('sms')"><text :class="loginType === 'sms' ? 'active' : ''">短信登录</text></view>
      </view>
    </view>
    
    <!-- 账号登录表单 start-->
    <view class="login-form" v-if="loginType === 'account'">
      <!-- 手机号 -->
      <view class="form-item">
        <text class="iconfont icon-sy-yh"></text>
        <input class="form-item--input uni-input" type="text" v-model="account" maxlength="30" clearable="true" placeholder="请输入您的用户名" />
      </view>
      <!-- 密码 -->
      <view class="form-item">
        <text class="iconfont icon-suo"></text>
        <input class="form-item--input" type="password" autocomplete="off" v-model="password" maxlength="30" minlength="1" value="" placeholder="请输入您的密码" />
      </view>
      <!-- 确认密码 -->
      <view class="form-item" v-if="isRegister">
        <text class="iconfont icon-suo"></text>
        <input class="form-item--input" type="password" autocomplete="off" v-model="password1" maxlength="30" value="" placeholder="请再次输入密码" />
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
      <!-- 按钮 -->
      <view class="login-button" v-if="!isRegister" @click="handleSubmit"><text>立即登录</text></view>
      <view class="login-button" v-if="isRegister" @click="handleSubmit"><text>立即注册</text></view>
      <view class="cancel-button" @click="handleCancel"><text>取消</text></view>
      <view class="register" v-if="!isRegister" @click="toRegister()">还没有账号？去注册</view>
      <view class="register" v-if="isRegister" @click="toRegister()">已有账号？立即登录</view>
    </view>
    <!-- 账号登录表单 end-->
    
    <!-- 短信登录表单 start-->
    <view class="login-form" v-if="loginType === 'sms'">
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
      <!-- 登录按钮 -->
      <view class="login-button" @click="handleSubmit"><text>验证码登录</text></view>
    </view>
    <!-- 短信登录表单 end-->
  </view>
</template>

<script>
  import store from '@/store'
  import * as LoginApi from '@/api/login'
  import { throttle, debounce } from '@/utils/util'
  import * as Verify from '@/utils/verify'
  import { checkLogin, isWechat } from '@/utils/app'

  // 倒计时时长(秒)
  const times = 60

  // 表单验证场景
  const GET_CAPTCHA = 10
  const SUBMIT_LOGIN = 20
  const SUBMIT_LOGIN_ACCOUNT = 30
  const SUBMIT_REGISTER = 40

  export default {
    props: {
      // 是否存在第三方用户信息
      isParty: {
        type: Boolean,
        default: () => false
      },
      // 第三方用户信息数据
      partyData: {
        type: Object
      }
    },

    data() {
      return {
        // 账号标题
        accountTitle : '账号登录',
        // 是否注册新账号
        isRegister: false,
        // 登录方式
        loginType: 'account',
        // 正在加载
        isLoading: false,
        // 图形验证码信息
        captcha: "",
        // 图形验证码uuid
        captchaUuid: "",
        // 账号图形验证码信息
        captchaForAccount: "",
        // 短信验证码发送状态
        smsState: false,
        // 倒计时
        times,
        // 手机号
        mobile: '',
        // 账号
        account: '',
        // 密码
        password: '',
        // 确认密码
        password1: '',
        // 图形验证码
        captchaCode: '',
        // 短信验证码
        smsCode: ''
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
      // 切换登录方式
      switchLoginType(loginType) {
        this.loginType = loginType;
        this.mobile = "";
        this.account = "";
        this.password = "";
        this.password1 = "";
        this.smsCode = "";
        this.getCaptcha();
        if (loginType === 'sms') {
            this.isRegister = false;
            this.accountTitle = '账号登录';
        }
      },
      // 注册新用户
      toRegister() {
         if (!this.isRegister) {
             this.accountTitle = '注册新账号';
             this.isRegister = true;
         } else {
             this.accountTitle = '账号登录';
             this.isRegister = false;
         }
      },
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
        if (!app.isLoading && !app.smsState && app.formValidation(GET_CAPTCHA)) {
            app.sendSmsCaptcha();
        }
      },

      // 表单验证
      formValidation(scene) {
        const app = this
        // 验证获取短信验证码
        if (scene === GET_CAPTCHA) {
            if (!app.validteMobile(app.mobile) || !app.validteCaptchaCode(app.captchaCode)) {
                return false
            }
        }
        // 验证提交登录
        if (scene === SUBMIT_LOGIN) {
            if (!app.validteMobile(app.mobile) || !app.validteSmsCode(app.smsCode)) {
                return false
            }
        }
        // 验证账号登录
        if (scene === SUBMIT_LOGIN_ACCOUNT) {
            if (!app.validteAccount(app.account) || !app.validtePassword(app.password) || !app.validteCaptchaCode(app.captchaCode)) {
                return false
            }
        }
        // 验证提交注册
        if (scene === SUBMIT_REGISTER) {
            if (!app.validteAccount(app.account) || !app.validtePassword(app.password) || !app.validtePassword1(app.password1) || !app.validteCaptchaCode(app.captchaCode)) {
                return false
            }
            if (app.password !== app.password1) {
                this.$toast('两次输入的密码不一致');
                return false;
            }
        }
        return true;
      },

      // 验证手机号
      validteMobile(str) {
        if (Verify.isEmpty(str)) {
          this.$toast('请先输入手机号')
          return false
        }
        if (!Verify.isMobile(str)) {
          this.$toast('请输入正确格式的手机号')
          return false
        }
        return true
      },
      
      // 验证账号
      validteAccount(str) {
        if (Verify.isEmpty(str)) {
            this.$toast('请先输入您的用户名')
            return false
        }
        if (str.length < 5) {
            this.$toast('用户名不能少于5位')
            return false
        }
        return true
      },
      // 验证密码
      validtePassword(str) {
        if (Verify.isEmpty(str)) {
            this.$toast('请先输入您的密码')
            return false
        }
        if (str.length < 6) {
            this.$toast('密码不能少于6位')
            return false
        }
        return true
      },
      // 验证密码
      validtePassword1(str) {
        if (Verify.isEmpty(str)) {
            this.$toast('请再次输入您的密码')
            return false
        }
        return true
      },

      // 验证图形验证码
      validteCaptchaCode(str) {
        if (Verify.isEmpty(str)) {
            this.$toast('请先输入图形验证码')
            return false
        }
        return true
      },

      // 验证短信验证码
      validteSmsCode(str) {
        if (Verify.isEmpty(str)) {
          this.$toast('请先输入短信验证码')
          return false
        }
        return true
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

      // 点击提交
      handleSubmit() {
        const app = this
        // 短信验证码登录
        if (!app.isLoading && !app.isRegister && app.loginType === 'sms') {
            if (app.formValidation(SUBMIT_LOGIN)) {
                app.submitLogin();
            }
        }
        
        // 账号登录
        if (!app.isLoading && !app.isRegister && app.loginType === 'account') {
            if (app.formValidation(SUBMIT_LOGIN_ACCOUNT)) {
                app.submitLogin();
            }
        }
        
        // 注册新账号
        if (!app.isLoading && app.isRegister) {
            if (app.formValidation(SUBMIT_REGISTER)) {
                app.submitRegister();
            }
        }
        
        return true
      },
      // 取消返回
      handleCancel() {
          this.$navTo('pages/user/index');
      },
      // 确认注册
      submitRegister() {
        const app = this
        app.isLoading = true
        store.dispatch('Register', {
            account: app.account,
            password: app.password,
            password1: app.password1,
            captchaKey: app.captcha.key,
            captchaCode: app.captchaCode,
            uuid: app.captchaUuid
          })
          .then(result => {
              // 显示登录信息
              app.$toast(result.message)
              if (result.code === 200) {
                  // 注册成功，去认证
                  app.isNeedAuth(result.data);
              } else {
                  app.$error(result.message);
                  app.getCaptcha();
              }
          })
          .finally(() => app.isLoading = false)
      },

      // 确认登录
      submitLogin() {
        const app = this
        app.isLoading = true
        store.dispatch('Login', {
            verifyCode: app.smsCode,
            mobile: app.mobile,
            account: app.account,
            password: app.password,
            isParty: app.isParty,
            partyData: app.partyData,
            captchaCode: app.captchaCode,
            uuid: app.captchaUuid
          })
          .then(result => {
            // 显示登录信息
            app.$toast(result.message)
            if (result.code === 200) {
                // 登录成功，去认证
                app.isNeedAuth(result.data);
            } else {
                app.$error(result.message);
                app.getCaptcha();
            }
          })
          .finally(() => app.isLoading = false)
      },
      
      /**
       * 去授权认证
       * */
      isNeedAuth(loginInfo) {
         console.log("loginInfo == ", loginInfo);
         if (isWechat() && loginInfo && loginInfo.appId && loginInfo.domain && !loginInfo.openId) {
             console.log('to auth...')
             // #ifdef H5
             const appId = loginInfo.appId;
             const domain = loginInfo.domain;
             const redirect_uri = encodeURIComponent(domain + "#pages/login/auth");
             const url = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + appId + "&redirect_uri="+ redirect_uri +"&response_type=code&scope=snsapi_userinfo&state=1#wechat_redirect";
             window.location.href = url;
             return true;
             // #endif
         }
         this.onNavigateBack(1);
      },

      /**
       * 登录成功-跳转回原页面
       */
      onNavigateBack(delta) {
        uni.navigateBack({
          delta: Number(delta || 1)
        })
      }
    }
  }
</script>

<style lang="scss" scoped>
  .container {
    padding: 160rpx 60rpx 100rpx 60rpx;
    min-height: 100vh;
    background-color: #fff;
    .fast-icon {
        margin-bottom: 80rpx;
        font-size: 50rpx;
        cursor: pointer;
    }
  }

  // 页面头部
  .header {
    margin-bottom: 50rpx;
    .title {
       color: #191919;
       font-size: 33rpx;
       height: 88rpx;
       padding: 10rpx;
       cursor: pointer;
       .item {
           width: 50%;
           height: 88rpx;
           float: left;
           text-align: center;
           font-weight: bold;
       }
       .active {
           border-bottom: #ff3800 10rpx solid;
           padding-bottom: 10rpx;
           text-align: center;
       }
    }
  }

  // 输入框元素
  .form-item {
    display: flex;
    padding: 18rpx;
    border-bottom: 2rpx solid #cccccc;
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


  // 登录按钮
  .login-button {
    width: 96%;
    height: 86rpx;
    margin: 0 auto;
    margin-top: 60rpx;
    background: $fuint-theme;
    text-align: center;
    line-height: 86rpx;
    color: #fff;
    border-radius: 80rpx;
    box-shadow: 0px 10px 20px 0px rgba(0, 0, 0, 0.1);
    letter-spacing: 5rpx;
    cursor: pointer;
  }
  
  // 取消按钮
  .cancel-button {
    width: 96%;
    height: 86rpx;
    margin: 0 auto;
    margin-top: 20rpx;
    background: #dfdfdf;
    color: #fff;
    text-align: center;
    line-height: 86rpx;
    border-radius: 80rpx;
    box-shadow: 0px 10px 20px 0px rgba(0, 0, 0, 0.1);
    letter-spacing: 5rpx;
    cursor: pointer;
  }
  
  // 去注册
  .register {
      margin-top: 40rpx;
      text-align: right;
  }

  // 微信授权登录
  .wechat-auth {
    display: flex;
    justify-content: center;
    margin-top: 40rpx;

    .icon {
      width: 38rpx;
      height: 38rpx;
      margin-right: 15rpx;
    }

    .title {
      font-size: 28rpx;
      color: #666666;
    }
  }
</style>
