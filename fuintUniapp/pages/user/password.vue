<template>
  <view class="container">    
    <view class="info-item" v-if="hasPassword == 'Y'">
      <view class="contacts">
        <text class="name">旧密码：</text>
        <input class="weui-input value" type="password" disabled="true" @click="inputPassword(1)" v-model="passwordOld" placeholder="请输入旧密码"/>
      </view>
    </view>
    <view class="info-item">
      <view class="contacts">
        <text class="name">新密码：</text>
        <input class="weui-input value" type="password" disabled="true" @click="inputPassword(2)" v-model="password" placeholder="请输入新密码"/>
      </view>
    </view>
    <view class="info-item">
      <view class="contacts">
        <text class="name">新密码确认：</text>
        <input class="weui-input value" type="password" disabled="true" @click="inputPassword(3)" v-model="passwordCopy" placeholder="请输入新密码确认"/>
      </view>
    </view>
    <!-- 底部操作按钮 -->
    <view class="footer-fixed">
      <view class="btn-wrapper">
        <view class="btn-item btn-item-main" @click="doSubmit()">保存</view>
      </view>
    </view>
    
    <!-- 设置密码对话框 -->
    <key-words :mix="true" :title="title" :show_key="showPass" :price="0" @closeFuc="closeFuc" @getPassword="getPassword"></key-words>
  </view>
</template>

<script>
  import * as UserApi from '@/api/user'
  import store from '@/store'
  import keyWords from "@/components/bian-keywords/index.vue"
  export default {
    components: {
      keyWords
    },
    data() {
      return {
        //当前页面参数
        options: {},
        // 正在加载
        isLoading: false,
        userInfo: {},
        qrCode: "",
        title: "",
        showPass: false,
        popType: 1,
        password: "",
        passwordCopy: "",
        passwordOld: "",
        hasPassword: ""
      }
    },

    /**
     * 生命周期函数--监听页面加载
     */
    onLoad(options) {
      this.hasPassword = options.hasPassword;
    },

    methods: {
      // 关闭密码对话框
      closeFuc() {
         this.showPass = false;
      },
      // 输入密码
      inputPassword(type) {
         if (type == 1) {
             this.title = "请输入旧密码";
         } else if (type == 2) {
             this.title = "请输入新密码";
         } else {
             this.title = "请输入新密码确认";
         }
         this.popType = type;
         this.showPass = true;
      },
      // 获取输入的密码
      getPassword(password) {
         if (this.popType == 1) {
             this.passwordOld = password.password;
         } else if (this.popType == 2) {
             this.password = password.password;
         } else {
             this.passwordCopy = password.password;
         }
         this.closeFuc();
      },
      // 提交保存
      doSubmit() {
         const app = this;
         app.isLoading = true;
         if (app.hasPassword == 'Y' && (!app.password || !app.passwordOld)) {
             return false;
         }
         if (app.password != app.passwordCopy) {
             app.$error('新密码输入不一致！');
             return false;
         }
         UserApi.save({"password": app.password, "passwordOld": app.passwordOld})
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
  .info-list {
      padding-bottom: 30rpx;
      margin-top: 30rpx;
  }

  // 项目内容
  .info-item {
    margin: 20rpx auto 20rpx auto;
    padding: 30rpx 40rpx;
    width: 94%;
    box-shadow: 0 1rpx 5rpx 0px rgba(0, 0, 0, 0.05);
    border-radius: 16rpx;
    background: #fff;
    .avatar-warp {
        line-height: 120rpx;
    }
  }

  .contacts {
    font-size: 30rpx;
    height: 40rpx;
    .name {
      margin-left: 0px;
      float: left;
      margin-right: 10rpx;
      line-height: 40rpx;
    }
    .value {
        float:right;
        color:#999999;
        text-align: right;
        .second {
            margin-left: .6rem;
        }
    }
    .vcode {
        float: left;
        line-height: 40rpx;
    }
    .password {
        text-align: right;
        float: left;
        padding-right: 5rpx;
    }
    .avatar {
        width: 120rpx;
        height: 120rpx;
        border-radius: 120rpx;
        border: solid 1px #cccccc;
        float: right;
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
