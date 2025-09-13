<template>
  <view class="container">
    <view class="info-list">
      <view class="info-item">
          <view class="contacts avatar-warp">
            <text class="name">头像</text>
            <image class="avatar" @click="chooseImage()" :src="avatar"></image>
          </view>
      </view>
      <view class="info-item">
        <view class="contacts">
          <text class="name">姓名</text>
          <input class="weui-input value" type="text" v-model="realName" placeholder="请输入姓名"/>
        </view>
      </view>
      <view class="info-item">
        <view class="contacts">
          <text class="name">手机</text>
          <input class="weui-input value" type="text" v-model="mobile" placeholder="请输入手机号"/>
        </view>
      </view>
      <view class="info-item">
        <view class="contacts">
          <text class="name">性别</text>
          <view class="value">
             <radio-group @change="genderChange">
                <label class="radio"><radio value="1" color="#00acac" :checked="userInfo.sex == '1' ? true : false"/>男</label>
                <label class="radio second"><radio value="0" color="#00acac" :checked="userInfo.sex == '0' ? true: false"/>女</label>
             </radio-group>
          </view>
        </view>
      </view>
    </view>
    <!-- 底部操作按钮 -->
    <view class="footer-fixed">
      <view class="btn-wrapper">
        <view class="btn-item btn-item-main" @click="save()">保存信息</view>
      </view>
    </view>
  </view>
</template>

<script>
  import * as StaffApi from '@/api/merchant/staff'
  import * as UploadApi from '@/api/upload'
  import store from '@/store'
  export default {
    data() {
      return {
        //当前页面参数
        options: {},
        // 正在加载
        isLoading: true,
        userInfo: { avatar: '', realName: '', mobile: '', sex: '' },
        openCardPara: null,
        code: "",
        realName: "",
        avatar: "/static/default-avatar.png",
        mobile: ""
      }
    },

    /**
     * 生命周期函数--监听页面加载
     */
    onLoad(options) {
      // 当前页面参数
      this.options = options;
    },

    methods: {
      genderChange(e) {
          this.userInfo.sex = e.detail.value;
      },
      // 选择图片
      chooseImage() {
        const app = this
        // 选择图片
        uni.chooseImage({
          count: 1,
          sizeType: ['original', 'compressed'], // 可以指定是原图还是压缩图，默认二者都有
          sourceType: ['album', 'camera'], // 可以指定来源是相册还是相机，默认二者都有
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
       * 保存会员信息
       */
      save() {
          const app = this
          if (!app.realName) {
              app.$error('姓名不能为空！');
              return false;
          }
          if (!app.mobile) {
              app.$error('手机号不能为空！');
              return false;
          }
          
          app.isLoading = true
          StaffApi.save({ "name": app.realName, mobile: app.mobile, "avatar": app.avatar, "sex": app.userInfo.sex })
            .then(result => {
              app.userInfo = result.data
              app.isLoading = false
              app.$success('保存成功！')
         }).catch(err => {
            app.isLoading = false;
         })
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
