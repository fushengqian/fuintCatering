<template>
  <view class="container">
    <view class="info-list">
      <view class="info-item">
          <view class="contacts avatar-warp">
            <text class="name">头像</text>
            <image class="avatar" @click="chooseImage()" :src="imagePath + merchantInfo.logo"></image>
          </view>
      </view>
      <view class="info-item">
        <view class="contacts">
          <text class="name">名称</text>
          <input class="weui-input value" type="nickname" v-model="merchantInfo.name" placeholder="请输入商户名称"/>
        </view>
      </view>
      <view class="info-item">
        <view class="contacts">
          <text class="name">联系人</text>
          <input class="weui-input value" type="text" v-model="merchantInfo.contact" placeholder="请输入联系人"/>
        </view>
      </view>
      <view class="info-item">
        <view class="contacts">
          <text class="name">手机</text>
          <input class="weui-input value" type="text" v-model="merchantInfo.phone" placeholder="请输入手机号"/>
        </view>
      </view>
      <view class="info-item">
        <view class="contacts">
          <text class="name">状态</text>
          <view class="value">
             <radio-group @change="statusChange">
                <label class="radio"><radio value="false" color="#00acac" :checked="merchantInfo.status == 'false' ? true : false"/>营业中</label>
                <label class="radio second"><radio value="true" color="#00acac" :checked="merchantInfo.status == 'true' ? true: false"/>已打烊</label>
             </radio-group>
          </view>
        </view>
      </view>
    </view>
    <!-- 底部操作按钮 -->
    <view class="footer-fixed">
      <view class="btn-wrapper">
        <view class="btn-item btn-item-main" @click="save()">保存设置</view>
      </view>
    </view>
  </view>
</template>

<script>
  import * as MerchantApi from '@/api/merchant'
  import * as UploadApi from '@/api/upload'
  import store from '@/store'
  export default {
    data() {
      return {
        //当前页面参数
        options: {},
        // 正在加载
        isLoading: true,
        imagePath: '',
        merchantInfo: { logo: '', name: '', status: 'false', phone: '' }
      }
    },

    /**
     * 生命周期函数--监听页面加载
     */
    onLoad(options) {
      this.getMerchantInfo();
    },

    methods: {
      // 获取当前商户信息
      getMerchantInfo() {
        const app = this;
        app.showPopup = false;
        return new Promise((resolve, reject) => {
            MerchantApi.settingInfo()
            .then(result => {
              if (result.data.merchantInfo) {
                  app.merchantInfo = result.data.merchantInfo;
                  app.imagePath = result.data.imagePath;
              } else {
                  app.merchantInfo = { id: 0, name: '', phone: '', logo: '', status: '' }
              }
              resolve(app.merchantInfo);
            })
            .catch(err => {
              if (err.result && err.result.status == 1001) {
                resolve(null)
              } else {
                reject(err)
              }
            })
        })
      },
      statusChange(e) {
          this.merchantInfo.status = e.detail.value;
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
                              app.merchantInfo.logo = files[0].fileName;
                              app.logo = files[0].domain + app.merchantInfo.logo;
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
       * 保存设置信息
       */
      save() {
          const app = this
          app.isLoading = true
          MerchantApi.saveSetting(app.merchantInfo)
            .then(result => {
              app.merchantInfo = result.data
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
