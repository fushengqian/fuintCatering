<template>
  <view v-if="!isLoading" class="container">
    <!-- 物流信息 -->
    <view class="express i-card">
      <view class="info-item">
        <view class="item-lable">物流公司</view>
        <view class="item-content">
          <text>{{ express.express_name }}</text>
        </view>
      </view>
      <view class="info-item">
        <view class="item-lable">物流单号</view>
        <view class="item-content">
          <text>{{ express.express_no }}</text>
          <view class="act-copy" @click.stop="handleCopy(express.express_no)">
            <text>复制</text>
          </view>
        </view>
      </view>
    </view>
    <!-- 物流动态 -->
    <view class="logis-detail">
      <view class="logis-item" :class="{ first: index === 0 }" v-for="(item, index) in express.list" :key="index">
        <view class="logis-item-content">
          <view class="logis-item-content__describe">
            <text class="f-26">{{ item.context }}</text>
          </view>
          <view class="logis-item-content__time">
            <text class="f-22">{{ item.ftime }}</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
  import * as OrderApi from '@/api/order'

  export default {
    data() {
      return {
        // 正在加载
        isLoading: true,
        // 当前订单ID
        orderId: null,
        // 物流信息
        express: {}
      }
    },

    /**
     * 生命周期函数--监听页面加载
     */
    onLoad({ orderId }) {
      this.orderId = orderId
      // 获取当前订单的物流信息
      this.getExpress()
    },

    methods: {

      // 获取当前订单的物流信息
      getExpress() {
        const app = this
        app.isLoading = true
        OrderApi.express(app.orderId)
          .then(result => {
            app.express = result.data.express
            app.isLoading = false
          })
      },

      // 复制指定内容
      handleCopy(value) {
        const app = this
        uni.setClipboardData({
          data: value,
          success() {
            app.$toast('复制成功')
          }
        })
      }

    }
  }
</script>

<style lang="scss" scoped>
  // 通栏卡片
  .i-card {
    background: #fff;
    padding: 24rpx 24rpx;
    // width: 94%;
    box-shadow: 0 1rpx 5rpx 0px rgba(0, 0, 0, 0.05);
    // margin: 0 auto 20rpx auto;
    // border-radius: 20rpx;
  }


  // 物流公司
  .express {

    // margin-top: 24rpx;

    .info-item {
      display: flex;
      margin-bottom: 24rpx;

      &:last-child {
        margin-bottom: 0;
      }

      .item-lable {
        display: flex;
        align-items: center;
        font-size: 24rpx;
        color: #999;
        margin-right: 30rpx;
      }

      .item-content {
        flex: 1;
        display: flex;
        align-items: center;
        font-size: 26rpx;
        color: #333;

        .act-copy {
          margin-left: 20rpx;
          padding: 2rpx 20rpx;
          font-size: 22rpx;
          color: #666;
          border: 1rpx solid #c1c1c1;
          border-radius: 16rpx;
        }
      }
    }

  }


  .logis-detail {
    padding: 30rpx;
    background-color: #fff;

    .logis-item {
      position: relative;
      padding: 10px 0 10px 25px;
      box-sizing: border-box;
      border-left: 2px solid #ccc;

      &.first {
        border-left: 2px solid #f40;

        &:after {
          background: #f40;
        }

        .logis-item-content {
          background: #ff6e39;
          color: #fff;

          &:after {
            border-bottom-color: #ff6e39;
          }
        }
      }

      &:after {
        content: ' ';
        display: inline-block;
        position: absolute;
        left: -6px;
        top: 30px;
        width: 6px;
        height: 6px;
        border-radius: 10px;
        background: #bdbdbd;
        border: 2px solid #fff;
      }

      .logis-item-content {
        position: relative;
        background: #f9f9f9;
        padding: 10rpx 20rpx;
        box-sizing: border-box;
        color: #666;

        &:after {
          content: '';
          display: inline-block;
          position: absolute;
          left: -10px;
          top: 18px;
          border-left: 10px solid #fff;
          border-bottom: 10px solid #f3f3f3;
        }
      }
    }
  }
</style>
