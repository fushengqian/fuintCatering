<template>
  <view class="container">
    <view class="info-list">
      <view class="info-item">
          <view class="contacts">
            <text class="name">卡券名称：</text>
            <input class="value" type="text" v-model="couponInfo.name" placeholder="请输入卡券名称"/>
          </view>
      </view>
      <view class="info-item">
        <view class="contacts">
          <text class="name">发行数量：</text>
          <input class="weui-input value" type="text" v-model="couponInfo.total" placeholder="请输入发行数量"/>
        </view>
      </view>
      <view class="info-item">
        <view class="contacts">
          <text class="name">启用状态：</text>
          <view class="value">
             <radio-group @change="statusChange">
                <label class="radio"><radio value="A" color="#00acac" :checked="couponInfo.status == 'A' ? true : false"/>启用</label>
                <label class="radio"><radio value="C" color="#00acac" :checked="couponInfo.status == 'C' ? true : false"/>过期</label>
                <label class="radio second"><radio value="N" color="#00acac" :checked="couponInfo.status == 'N' ? true: false"/>禁用</label>
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
  import * as CouponApi from '@/api/merchant/coupon'
  export default {
    data() {
      return {
        couponId: '',
        // 正在加载
        isLoading: true,
        couponInfo: {  name: '', total: '', status: '' },
      }
    },

    /**
     * 生命周期函数--监听页面加载
     */
    onLoad(options) {
      // 当前页面参数
      this.couponId = options.couponId;
      this.getCouponInfo();
    },

    methods: {
      /**
       * 获取卡券信息
       * */
      getCouponInfo() {
        const app = this
        app.isLoading = true
        CouponApi.info(app.couponId)
          .then(result => {
            app.couponInfo = result.data;
            app.isLoading = false;
          })
      },
      statusChange(e) {
        this.couponInfo.status = e.detail.value;
      },
      /**
       * 保存卡券信息
       */
      save() {
          const app = this
          app.isLoading = true
          CouponApi.saveCoupon({"name": app.couponInfo.name,
                                "id": app.couponId,
                                "total": app.couponInfo.total,
                                "status": app.couponInfo.status })
            .then(result => {
              app.couponInfo = result.data
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
    height: 50rpx;
    .name {
      margin-left:0px;
    }
    .value {
        float:right;
        color:#999999;
        text-align: right;
        .second {
          margin-left: 5rpx;
        }
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
