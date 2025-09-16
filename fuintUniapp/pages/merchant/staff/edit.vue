<template>
  <view class="container">
    <view class="info-list">
      <view class="info-item">
        <view class="contacts">
          <text class="name">姓名</text>
          <input class="weui-input value" type="text" v-model="staffInfo.realName" placeholder="请输入员工姓名"/>
        </view>
      </view>
      <view class="info-item">
        <view class="contacts">
          <text class="name">手机</text>
          <input class="weui-input value" type="text" v-model="staffInfo.mobile" placeholder="请输入员工手机号"/>
        </view>
      </view>
      <view class="info-item">
        <view class="contacts">
          <text class="name">状态</text>
          <view class="value">
             <radio-group @change="statusChange">
                <label class="radio"><radio value="A" color="#00acac" :checked="staffInfo.auditedStatus == 'A' ? true : false"/>启用</label>
                <label class="radio second"><radio value="N" color="#00acac" :checked="staffInfo.auditedStatus == 'N' ? true: false"/>禁用</label>
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
  import store from '@/store'
  export default {
    data() {
      return {
        staffId: '',
        // 正在加载
        isLoading: true,
        staffInfo: { id: 0, realName: '', mobile: '', auditedStatus: 'A' }
      }
    },

    /**
     * 生命周期函数--监听页面加载
     */
    onLoad(options) {
      // 当前页面参数
      this.staffId = options.staffId;
      this.getStaffInfo();
    },

    methods: {
      // 获取当前员工信息
      getStaffInfo() {
        const app = this;
        app.showPopup = false;
        return new Promise((resolve, reject) => {
            StaffApi.info(app.staffId)
            .then(result => {
              if (result.data.staffInfo) {
                  app.staffInfo = result.data.staffInfo;
              } else {
                  app.staffInfo = { id: 0, name: '', mobile: '', auditedStatus: '' }
              }
              resolve(app.staffInfo);
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
          this.staffInfo.auditedStatus = e.detail.value;
      },
      /**
       * 保存员工信息
       */
      save() {
          const app = this
          if (!app.staffInfo.realName) {
              app.$error('员工姓名不能为空！');
              return false;
          }
          if (!app.staffInfo.mobile) {
              app.$error('手机号不能为空！');
              return false;
          }
          
          app.isLoading = true
          StaffApi.save({ "id": app.staffId, "realName": app.staffInfo.realName, mobile: app.staffInfo.mobile, "auditedStatus": app.staffInfo.auditedStatus })
            .then(result => {
              app.staffInfo = result.data;
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
