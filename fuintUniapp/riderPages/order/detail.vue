<template>
  <view class="rider-detail">
    <view class="content" v-if="orderInfo.orderId">
      <!-- 状态 -->
      <view class="status-card">
        <text class="status-text">{{ orderInfo.statusText }}</text>
      </view>

      <!-- 地址信息 -->
      <view class="card">
        <view class="card-title">配送信息</view>
        <view class="address-line">
          <text class="dot pickup"></text>
          <view class="address-detail">
            <text class="addr-label">取货地址</text>
            <text class="addr-text">{{ orderInfo.storeAddress || '暂无' }}</text>
            <text class="addr-contact" v-if="orderInfo.storeInfo">
              店铺：{{ orderInfo.storeInfo.name }}
            </text>
          </view>
        </view>
        <view class="address-line">
          <text class="dot delivery"></text>
          <view class="address-detail">
            <text class="addr-label">配送地址</text>
            <text class="addr-text">{{ orderInfo.userAddress || '暂无' }}</text>
            <text class="addr-contact" v-if="orderInfo.userInfo">
              联系人：{{ orderInfo.userInfo.name }} {{ orderInfo.userInfo.mobile }}
            </text>
          </view>
        </view>
      </view>

      <!-- 商品信息 -->
      <view class="card">
        <view class="card-title">商品信息</view>
        <view class="goods-list">
          <view class="goods-item" v-for="item in orderInfo.goods" :key="item.id">
            <image :src="item.logo" mode="aspectFill" class="goods-img"></image>
            <view class="goods-info">
              <text class="goods-name">{{ item.name }}</text>
              <text class="goods-spec" v-if="item.specIds">规格：{{ item.specIds }}</text>
            </view>
            <view class="goods-right">
              <text class="goods-price">¥{{ item.price }}</text>
              <text class="goods-num">x{{ item.num }}</text>
            </view>
          </view>
        </view>
        <view class="order-summary">
          <view class="summary-row">
            <text class="summary-label">商品金额</text>
            <text class="summary-value">¥{{ orderInfo.amount }}</text>
          </view>
          <view class="summary-row">
            <text class="summary-label">配送费</text>
            <text class="summary-value fee">¥{{ orderInfo.deliveryFee || 0 }}</text>
          </view>
          <view class="summary-row total">
            <text class="summary-label">实付金额</text>
            <text class="summary-value">¥{{ orderInfo.payAmount || orderInfo.amount }}</text>
          </view>
        </view>
      </view>

      <!-- 订单信息 -->
      <view class="card">
        <view class="card-title">订单信息</view>
        <view class="info-row">
          <text class="info-label">订单号</text>
          <text class="info-value">{{ orderInfo.orderSn }}</text>
        </view>
        <view class="info-row">
          <text class="info-label">下单时间</text>
          <text class="info-value">{{ orderInfo.createTime }}</text>
        </view>
        <view class="info-row" v-if="orderInfo.remark">
          <text class="info-label">用户备注</text>
          <text class="info-value">{{ orderInfo.remark }}</text>
        </view>
      </view>
    </view>

    <!-- 操作按钮 -->
    <view class="bottom-bar" v-if="orderInfo.orderId">
      <view class="action-btn pickup-btn" v-if="orderInfo.status === 'L'" @click="doPickup">
        确认取货
      </view>
      <view class="action-btn deliver-btn" v-if="orderInfo.status === 'M'" @click="doDeliver">
        确认送达
      </view>
    </view>
  </view>
</template>

<script>
import { getOrderDetail, confirmPickup, confirmDeliver } from '@/api/rider'

export default {
  data() {
    return {
      orderInfo: {}
    }
  },
  onLoad(options) {
    if (options.orderId) {
      this.loadDetail(options.orderId)
    }
  },
  methods: {
    async loadDetail(orderId) {
      try {
        const res = await getOrderDetail(orderId)
        if (res.code === 200 && res.data) {
          this.orderInfo = res.data
        } else {
          uni.showToast({ title: res.message || '订单不存在', icon: 'none' })
        }
      } catch (e) {
        uni.showToast({ title: '加载失败', icon: 'none' })
      }
    },
    async doPickup() {
      uni.showModal({
        title: '确认取货',
        content: '确认已到店取到商品？',
        success: async (res) => {
          if (res.confirm) {
            try {
              const result = await confirmPickup(this.orderInfo.orderId)
              if (result.code === 200) {
                uni.showToast({ title: '取货确认成功', icon: 'success' })
                this.loadDetail(this.orderInfo.orderId)
              } else {
                uni.showToast({ title: result.message, icon: 'none' })
              }
            } catch (e) {
              uni.showToast({ title: '操作失败', icon: 'none' })
            }
          }
        }
      })
    },
    async doDeliver() {
      uni.showModal({
        title: '确认送达',
        content: '确认已将商品送达给客户？',
        success: async (res) => {
          if (res.confirm) {
            try {
              const result = await confirmDeliver(this.orderInfo.orderId)
              if (result.code === 200) {
                uni.showToast({ title: '送达确认成功', icon: 'success' })
                this.loadDetail(this.orderInfo.orderId)
              } else {
                uni.showToast({ title: result.message, icon: 'none' })
              }
            } catch (e) {
              uni.showToast({ title: '操作失败', icon: 'none' })
            }
          }
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.rider-detail {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 120rpx;
}
.status-card {
  background: linear-gradient(135deg, #FF6B35, #FF8C42);
  padding: 40rpx 30rpx;
}
.status-text {
  font-size: 40rpx;
  font-weight: bold;
  color: #fff;
}
.card {
  background: #fff;
  margin: 20rpx;
  border-radius: 16rpx;
  padding: 24rpx;
}
.card-title {
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
}
.address-line {
  display: flex;
  margin-bottom: 20rpx;
}
.dot {
  width: 12rpx; height: 12rpx;
  border-radius: 50%;
  margin-right: 16rpx;
  margin-top: 8rpx;
  flex-shrink: 0;
  &.pickup { background: #52c41a; }
  &.delivery { background: #1890ff; }
}
.address-detail {
  flex: 1;
}
.addr-label { font-size: 22rpx; color: #999; }
.addr-text { font-size: 28rpx; color: #333; display: block; margin: 6rpx 0; }
.addr-contact { font-size: 24rpx; color: #FF6B35; }
.goods-item {
  display: flex;
  align-items: center;
  padding: 16rpx 0;
  border-bottom: 1rpx solid #f5f5f5;
}
.goods-img { width: 80rpx; height: 80rpx; border-radius: 8rpx; margin-right: 16rpx; }
.goods-info { flex: 1; }
.goods-name { font-size: 28rpx; color: #333; }
.goods-spec { font-size: 22rpx; color: #999; display: block; margin-top: 4rpx; }
.goods-right { text-align: right; }
.goods-price { font-size: 28rpx; color: #333; display: block; }
.goods-num { font-size: 24rpx; color: #999; }
.order-summary {
  margin-top: 20rpx;
  padding-top: 20rpx;
  border-top: 1rpx solid #f5f5f5;
}
.summary-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12rpx;
}
.summary-label { font-size: 26rpx; color: #666; }
.summary-value { font-size: 26rpx; color: #333; }
.summary-value.fee { color: #FF6B35; }
.summary-row.total { margin-top: 10rpx; padding-top: 10rpx; border-top: 1rpx solid #f0f0f0; }
.summary-row.total .summary-label { font-weight: bold; }
.summary-row.total .summary-value { font-size: 30rpx; font-weight: bold; color: #FF6B35; }
.info-row {
  display: flex;
  justify-content: space-between;
  padding: 12rpx 0;
  border-bottom: 1rpx solid #f5f5f5;
}
.info-label { font-size: 26rpx; color: #999; }
.info-value { font-size: 26rpx; color: #333; }
.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  padding: 20rpx 30rpx;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  box-shadow: 0 -2rpx 10rpx rgba(0,0,0,0.05);
  display: flex;
  justify-content: center;
}
.action-btn {
  width: 80%;
  text-align: center;
  padding: 24rpx;
  border-radius: 40rpx;
  font-size: 32rpx;
  font-weight: bold;
  color: #fff;
  &.pickup-btn { background: linear-gradient(135deg, #52c41a, #73d13d); }
  &.deliver-btn { background: linear-gradient(135deg, #1890ff, #40a9ff); }
}
</style>
