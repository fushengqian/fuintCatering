<template>
  <div>
    <MpWeixin v-if="isShowUserInfo" @passwordLogin="passwordLogin" @success="onGetUserInfoSuccess" />
    <Main v-else :isParty="isExistUserInfo" :partyData="partyData" />
  </div>
</template>

<script>
import store from '@/store'
import { checkLogin, isWechat } from '../../utils/app'
import Main from './components/main'
import MpWeixin from './components/mp-weixin'
import * as LoginApi from '@/api/login'

  export default {
    components: {
      Main,
      MpWeixin
    },

    data() {
      return {
        // 是否显示获取用户信息组件
        isShowUserInfo: false,
        // 是否已获取到了用户信息
        isExistUserInfo: false,
        // 第三方用户信息数据
        partyData: {}
      }
    },
    
    /**
     * 生命周期函数--监听页面显示
     */
    onShow() {
      const app = this
      const isLogin = checkLogin();
      if (isLogin) {
          store.dispatch('Logout');
      }
      if (isWechat()) {
          // #ifdef H5
          uni.showLoading({ title: '页面加载中..', mask:true });
          LoginApi.authLoginConfig()
            .then(result => {
                uni.hideLoading();
                if (result.data.appId && result.data.domain) {
                   const appId = result.data.appId;
                   const domain = result.data.domain;
                   const redirect_uri = encodeURIComponent(domain + "#pages/login/auth");
                   const url = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + appId + "&redirect_uri="+ redirect_uri +"&response_type=code&scope=snsapi_userinfo&state=1#wechat_redirect";
                   window.location.href = url;
                   return true; 
                }
          })
          // #endif
      }
    },

    /**
     * 生命周期函数--监听页面加载
     */
    onLoad(options) {
      const app = this
      // 只有微信小程序才显示获取用户信息按钮
      // #ifdef MP-WEIXIN
      app.isShowUserInfo = wx.canIUse('getUserProfile')
      // #endif
      
      const code = options.code;
      if (code) {
          app.getMpLogin(code);
      }
    },

    methods: {

      // 获取openid
      getMpLogin(code) {
        const app = this
        return new Promise((resolve, reject) => {
          LoginApi.mpWxLogin({ code : code}, { isPrompt: false })
            .then(result => {
                console.log("login-info", result);
                resolve(result)
            })
            .catch(err => reject(err))
        })
      },
      
      // 使用账号密码登录
      passwordLogin(show) {
         this.isShowUserInfo = show;
      },

      // 获取到用户信息的回调函数
      onGetUserInfoSuccess({ oauth, code, userInfo }) {
        // 记录第三方用户信息数据
        this.partyData = { oauth, code, userInfo }
        // 显示注册页面
        this.onShowRegister()
      },

      // 显示注册页面
      onShowRegister() {
        // 是否显示获取用户信息组件
        this.isShowUserInfo = false
        // 是否已获取到了用户信息
        this.isExistUserInfo = true
      }
    }
  }
</script>

<style lang="scss" scoped>
  //empty
</style>
