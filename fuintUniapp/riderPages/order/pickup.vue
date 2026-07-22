<template>
  <view class="rider-pickup">
    <scroll-view scroll-y class="order-list" @refresherrefresh="onRefresh" refresher-enabled :refresher-triggered="refreshing">
      <view class="empty" v-if="orderList.length === 0 && !loading">
        <text>暂无待取货订单</text>
      </view>
      <view class="order-card" v-for="item in orderList" :key="item.orderId" @click="goDetail(item.orderId)">
        <view class="order-top">
          <text class="order-sn">#{{ item.orderSn }}</text>
          <text class="order-fee">配送费 ¥{{ item.deliveryFee || 0 }}</text>
        </view>
        <view class="order-info">
          <view class="address-line">
            <text class="dot pickup"></text>
            <text class="address-text">{{ item.storeAddress || '店铺地址' }}</text>
          </view>
          <view class="address-line">
            <text class="dot delivery"></text>
            <text class="address-text">{{ item.userAddress || '用户地址' }}</text>
          </view>
        </view>
        <view class="order-bottom">
          <text class="order-goods" v-if="item.goods && item.goods.length > 0">
            {{ item.goods.map(g => g.name).join('、') }}
          </text>
          <view class="action-btn pickup-btn" @click.stop="doPickup(item.orderId)">确认取货</view>
        </view>
      </view>
      <view v-if="loading" class="loading-tip">加载中...</view>
    </scroll-view>
  </view>
</template>

<script>
import { getMyOrders, confirmPickup } from '@/api/rider'

export default {
  data() {
    return {
      orderList: [],
      loading: false,
      refreshing: false
    }
  },
  onShow() {
    this.onRefresh()
  },
  methods: {
    async onRefresh() {
      this.refreshing = true
      this.loading = true
      try {
        const res = await getMyOrders({
          page: 1,
          pageSize: 50,
          searchParams: { status: 'L' }
        })
        if (res.code === 200 && res.data) {
          this.orderList = res.data.content || []
        }
      } catch (e) {}
      this.loading = false
      this.refreshing = false
    },
    async doPickup(orderId) {
      uni.showModal({
        title: '确认取货',
        content: '确认已到店取到商品？',
        success: async (res) => {
          if (res.confirm) {
            try {
              const result = await confirmPickup(orderId)
              if (result.code === 200) {
                uni.showToast({ title: '取货成功', icon: 'success' })
                this.onRefresh()
              } else {
                uni.showToast({ title: result.message || '操作失败', icon: 'none' })
              }
            } catch (e) {
              uni.showToast({ title: '操作失败', icon: 'none' })
            }
          }
        }
      })
    },
    goDetail(orderId) {
      uni.navigateTo({ url: '/riderPages/order/detail?orderId=' + orderId })
    }
  }
}
</script>

<style lang="scss" scoped>
.rider-pickup { min-height: 100vh; background: #f5f5f5; }
.order-list { height: 100vh; padding: 20rpx; }
.order-card { background: #fff; border-radius: 16rpx; padding: 24rpx; margin-bottom: 20rpx; }
.order-top { display: flex; justify-content: space-between; margin-bottom: 16rpx; }
.order-sn { font-size: 28rpx; color: #666; }
.order-fee { font-size: 32rpx; font-weight: bold; color: #FF6B35; }
.order-info { margin-bottom: 16rpx; }
.address-line { display: flex; align-items: flex-start; margin-bottom: 10rpx; }
.dot { width: 12rpx; height: 12rpx; border-radius: 50%; margin-right: 12rpx; margin-top: 6rpx; flex-shrink: 0; }
.dot.pickup { background: #52c41a; }
.dot.delivery { background: #1890ff; }
.address-text { font-size: 26rpx; color: #333; }
.order-bottom { display: flex; justify-content: space-between; align-items: center; }
.order-goods { font-size: 24rpx; color: #999; flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.action-btn { padding: 12rpx 40rpx; border-radius: 30rpx; font-size: 26rpx; color: #fff; }
.pickup-btn { background: linear-gradient(135deg, #52c41a, #73d13d); }
.empty { text-align: center; color: #999; padding: 200rpx 0; font-size: 28rpx; }
.loading-tip { text-align: center; color: #999; padding: 20rpx; font-size: 24rpx; }
</style>
