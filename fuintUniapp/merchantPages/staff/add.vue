<template>
  <view class="container">
    <view class="info-list">
      <view class="info-item">
        <view class="contacts">
          <text class="name">所属店铺</text>
          <picker class="weui-input value" mode="selector" :range="storeList" range-key="name" @change="storeChange">
            <input type="text" v-model="storeName" disabled="disabled" placeholder="请点击选择店铺"/>
          </picker>
        </view>
      </view>
      <view class="info-item">
        <view class="contacts">
          <text class="name">员工姓名</text>
          <input class="weui-input value" type="text" v-model="staffInfo.realName" placeholder="请输入员工姓名"/>
        </view>
      </view>
      <view class="info-item">
        <view class="contacts">
          <text class="name">员工手机</text>
          <input class="weui-input value" type="text" v-model="staffInfo.mobile" placeholder="请输入员工手机号"/>
        </view>
      </view>
      <view class="info-item">
        <view class="contacts">
          <text class="name">启用状态</text>
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
        <view class="btn-item btn-item-main" @click="save()">新增员工</view>
      </view>
    </view>
  </view>
</template>

<script>
  import * as settingApi from '@/api/setting'
  import * as StaffApi from '@/api/merchant/staff'
  import * as UploadApi from '@/api/upload'
  import store from '@/store'
  export default {
    data() {
      return {
        // 店铺列表
        storeList: [],
        // 正在加载
        isLoading: true,
        staffInfo: { storeId: 0, realName: '', mobile: '', auditedStatus: '' },
        storeName: '',
        storeId: 0
      }
    },

    /**
     * 生命周期函数--监听页面加载
     */
    onLoad() {
      this.getStoreList();
    },

    methods: {
     storeChange(e) {
       const app = this;
       for (let i = 0; i < app.storeList.length; i++) {
            if (i == e.detail.value) {
                app.storeName = app.storeList[i].name;
                app.storeId = app.storeList[i].id;
                break;
            }
       }
     },
     /**
      * 获取店铺列表
      * */
      getStoreList() {
        const app = this
        settingApi.storeList()
          .then(result => {
             app.storeList = result.data.data;
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
              app.$error('姓名不能为空！');
              return false;
          }
          if (!app.staffInfo.mobile) {
              app.$error('手机号不能为空！');
              return false;
          }
          
          app.isLoading = true
          StaffApi.save({ "storeId": app.storeId, "realName": app.staffInfo.realName, mobile: app.staffInfo.mobile, "auditedStatus": app.staffInfo.auditedStatus })
            .then(result => {
              app.staffInfo = result.data;
              app.isLoading = false
              app.$success('新增成功！')
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
    .storeName {
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
