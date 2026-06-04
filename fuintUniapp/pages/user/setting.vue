<template>
  <view class="container">
    <view class="info-list">
      <view class="info-item">
          <view class="contacts avatar-warp">
            <text class="name">头像</text>
            <image class="avatar" @click="showAvatarAction()" :src="avatar"></image>
          </view>
      </view>
      <view class="info-item">
        <view class="contacts">
          <text class="name">称呼</text>
          <input class="weui-input value" type="nickname" @blur="getnickname" v-model="nickname" placeholder="请输入称呼"/>
        </view>
      </view>
      <view class="info-item">
        <view class="contacts">
          <text class="name">手机</text>
          <button class="button btn-normal value" open-type="getPhoneNumber" @click="changeMobile" @getphonenumber="getPhoneNumber">
              <text v-if="userInfo.mobile">{{ userInfo.mobile }}</text>
              <text style="color: #f9211c;margin-left: 2px;">更换</text>
          </button>
        </view>
      </view>
      <view class="info-item">
        <view class="contacts">
          <text class="name">密码</text>
          <button class="button btn-normal value" @click="changePassword">
              <text class="password">********</text>
              <text style="color: #f9211c;margin-left: 2px;">修改</text>
          </button>
        </view>
      </view>
      <view class="info-item">
        <view class="contacts">
          <text class="name">性别</text>
          <view class="value">
             <radio-group @change="genderChange">
                <label class="radio"><radio value="1" color="#113a28" :checked="userInfo.sex == '1' ? true : false"/>男</label>
                <label class="radio second"><radio value="0" color="#113a28" :checked="userInfo.sex == '0' ? true: false"/>女</label>
             </radio-group>
          </view>
        </view>
      </view>
      <view class="info-item">
        <view class="contacts">
          <text class="name">生日</text>
          <picker class="value" mode="date" :value="userInfo.birthday" start="1900-01-01" @change="bindDateChange">
              <view class="picker">{{ userInfo.birthday ? userInfo.birthday : '选择生日' }}</view>
           </picker>
        </view>
      </view>
    </view>
    <!-- 底部操作按钮 -->
    <view class="footer-fixed" v-if="userInfo.id">
      <view class="btn-wrapper">
        <view class="btn-item btn-item-main" @click="save()">保存信息</view>
      </view>
      <view class="btn-wrapper">
        <view class="btn-item btn-item-out" @click="logout()">退出登录</view>
      </view>
    </view>

    <!-- 头像选择弹窗 -->
    <view class="avatar-action-mask" v-if="avatarActionVisible" @click="hideAvatarAction"></view>
    <view class="avatar-action-sheet" v-if="avatarActionVisible">
      <!-- #ifdef MP-WEIXIN -->
      <button class="action-item action-wx" open-type="chooseAvatar" @chooseavatar="onChooseAvatar">
        <image class="action-wx-avatar" :src="avatar" mode="aspectFill"></image>
        <text class="action-title">用微信头像</text>
      </button>
      <!-- #endif -->
      <view class="action-item" @click="chooseImage('album')">从相册选择</view>
      <view class="action-item" @click="chooseImage('camera')">拍照</view>
      <view class="action-item action-cancel" @click="hideAvatarAction">取消</view>
    </view>
  </view>
</template>

<script>
  import * as UserApi from '@/api/user'
  import * as UploadApi from '@/api/upload'
  import store from '@/store'
  export default {
    data() {
      return {
        //当前页面参数
        options: {},
        // 正在加载
        isLoading: true,
        userInfo: { avatar: '', name: '', sex: 0, birthday: '', hasPassword: '' },
        openCardPara: null,
        code: "",
        nickname: "",
        avatar: "",
        // 头像选择弹窗显示状态
        avatarActionVisible: false
      }
    },

    /**
     * 生命周期函数--监听页面加载
     */
    onLoad(options) {
      // 当前页面参数
      this.options = options;
      this.getUserInfo();
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
            app.userInfo = result.data.userInfo;
            if (result.data.openCardPara) {
                app.openCardPara = result.data.openCardPara;
            }
            app.nickname = app.userInfo.name;
            app.avatar = app.userInfo.avatar;
            app.isLoading = false;
          })
      },
      bindDateChange (e) {
        let that = this;
        that.userInfo.birthday = e.detail.value;
      },
      getnickname(e) {
          this.nickname = e.detail.value;  
      }, 
      genderChange(e) {
          this.userInfo.sex = e.detail.value
      },
      /**
       * 获取会员手机
       * */
      getPhoneNumber(e) {
          if (e.detail.errMsg == "getPhoneNumber:ok") {
              this.onAuthSuccess(e)
          }
      },
      getCode(e) {
        const app = this
        return new Promise((resolve, reject) => {
          uni.login({
            provider: 'weixin',
            success: res => {
              e.detail.code = res.code
              UserApi.save(e.detail)
                 .then(result => {
                 app.userInfo.mobile = result.data.mobile
              })
              resolve(res.code)
            },
            fail: reject
          })
        })
      },
      onAuthSuccess(e) {
         this.getCode(e)
      },
      // 修改密码
      changePassword() {
         this.$navTo('pages/user/password?hasPassword=' + this.userInfo.hasPassword);
         console.log(this.userInfo.hasPassword);
      },
      // 修改手机号
      changeMobile() {
         // #ifdef H5
         this.$navTo('pages/user/mobile');
         // #endif
      },
      // 显示头像选择弹窗
      showAvatarAction() {
        this.avatarActionVisible = true
      },
      // 隐藏头像选择弹窗
      hideAvatarAction() {
        this.avatarActionVisible = false
      },
      // 微信 open-type="chooseAvatar" 回调
      onChooseAvatar(e) {
        const app = this
        const avatarUrl = e.detail.avatarUrl
        if (!avatarUrl) {
          app.$error('获取微信头像失败')
          return
        }
        app.avatarActionVisible = false
        // 构造成 UploadApi.image 需要的格式上传
        const tempFiles = [{ path: avatarUrl }]
        UploadApi.image(tempFiles)
          .then(files => {
            if (files && files.length > 0) {
              app.userInfo.avatar = files[0].fileName
              app.avatar = files[0].domain + app.userInfo.avatar
            }
          })
          .catch(() => {
            app.$error('头像上传失败')
          })
      },
      // 选择图片
      chooseImage(sourceType) {
        const app = this
        app.avatarActionVisible = false
        let sourceTypeArr = ['album', 'camera']
        if (sourceType === 'album') {
          sourceTypeArr = ['album']
        } else if (sourceType === 'camera') {
          sourceTypeArr = ['camera']
        }
        // 选择图片
        uni.chooseImage({
          count: 1,
          sizeType: ['original', 'compressed'], // 可以指定是原图还是压缩图，默认二者都有
          sourceType: sourceTypeArr, // 可以指定来源是相册还是相机
          success({ tempFiles }) {
            const imageList = tempFiles;
            return new Promise((resolve, reject) => {
              if (imageList.length > 0) {
                  UploadApi.image(imageList)
                      .then(files => {
                          if (files && files.length > 0) {
                              app.userInfo.avatar = files[0].fileName;
                              app.avatar = files[0].domain + app.userInfo.avatar;
                          }
                          resolve(files)
                      })
                      .catch(err => reject(err))
              } else {
                resolve()
              }
            })
          }
        });
      },
      /**
       * 保存个人信息
       */
      save() {
          const app = this
          // 头像校验
          if (!app.avatar) {
              app.$error('请上传头像')
              return
          }
          // 昵称校验
          if (!app.nickname) {
              app.$error('请填写称呼')
              return
          }
          app.isLoading = true
          UserApi.save({"name": app.nickname, "avatar": app.avatar, "sex": app.userInfo.sex, "birthday": app.userInfo.birthday})
            .then(result => {
              app.userInfo = result.data
              app.isLoading = false
              app.$success('保存成功！')
         }).catch(err => {
            app.isLoading = false;
         })
      },
      
      /**
       * 退出登录
       */
      logout() {
         store.dispatch('Logout');
         this.$navTo('pages/user/index');
      }
    }
  }
</script>

<style lang="scss" scoped>
  .info-list {
    padding-bottom: 100rpx;
    margin-top: 25rpx;
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
    .name {
      margin-left:0px;
    }
    .value {
        float:right;
        color:#999999;
        text-align: right;
        .second {
            margin-left: .6rem;
        }
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
  .item-option {
    display: flex;
    justify-content: space-between;
    height: 48rpx;
  }

  // 头像选择弹窗遮罩
  .avatar-action-mask {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.5);
    z-index: 100;
  }

  // 头像选择弹窗
  .avatar-action-sheet {
    position: fixed;
    left: 0;
    right: 0;
    bottom: 0;
    z-index: 101;
    background: #fff;
    border-radius: 24rpx 24rpx 0 0;
    padding-bottom: constant(safe-area-inset-bottom);
    padding-bottom: env(safe-area-inset-bottom);
    animation: slideUp 0.2s ease-out;

    .action-item {
      height: 100rpx;
      line-height: 100rpx;
      text-align: center;
      font-size: 30rpx;
      color: #333;
      border-bottom: 1rpx solid #f0f0f0;
    }

    .action-wx {
      display: flex;
      flex-direction: row;
      align-items: center;
      justify-content: center;
      height: 100rpx;
      line-height: normal;
      background: none;
      border: none;
      border-radius: 0;
      border-bottom: 1rpx solid #f0f0f0;
      font-size: 30rpx;
      color: #333;
      width: 100%;

      // 重置 button 默认样式
      &::after {
        display: none;
      }

      .action-wx-avatar {
        width: 60rpx;
        height: 60rpx;
        border-radius: 50%;
        margin-right: 16rpx;
      }

      .action-title {
        font-size: 30rpx;
        color: #333;
      }
    }

    .action-cancel {
      border-top: 8rpx solid #f5f5f5;
      border-bottom: none;
      color: #999;
      font-weight: 500;
    }
  }

  @keyframes slideUp {
    from {
      transform: translateY(100%);
    }
    to {
      transform: translateY(0);
    }
  }

  // 底部操作栏
  .footer-fixed {
    height: 100rpx;
    z-index: 11;
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
    .btn-item-out {
      margin-top: 20rpx;
      background: #FFFFFF;
      border: 1px solid $fuint-theme;
      color: #666666;
    }

  }
</style>
