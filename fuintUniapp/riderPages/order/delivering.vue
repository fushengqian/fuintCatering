<template>
  <view class="rider-delivering">
    <scroll-view scroll-y class="order-list" @refresherrefresh="onRefresh" refresher-enabled :refresher-triggered="refreshing">
      <view class="empty" v-if="orderList.length === 0 && !loading">
        <text>暂无配送中订单</text>
      </view>
      <view class="order-card" v-for="item in orderList" :key="item.orderId" @click="goDetail(item.orderId)">
        <view class="order-top">
          <text class="order-sn">#{{ item.orderSn }}</text>
          <text class="order-fee">配送费 ¥{{ item.deliveryFee || 0 }}</text>
        </view>
        <view class="order-info">
          <view class="address-line">
            <text class="dot delivery"></text>
            <view class="addr-detail">
              <text class="addr-text">{{ item.userAddress || '用户地址' }}</text>
              <text class="addr-user" v-if="item.userInfo">
                {{ item.userInfo.name }} {{ item.userInfo.mobile }}
              </text>
            </view>
          </view>
        </view>
        <view class="order-bottom">
          <text class="order-goods" v-if="item.goods && item.goods.length > 0">
            {{ item.goods.map(g => g.name).join('、') }}
          </text>
          <view class="action-btn deliver-btn" @click.stop="doDeliver(item.orderId)">确认送达</view>
        </view>
      </view>
      <view v-if="loading" class="loading-tip">加载中...</view>
    </scroll-view>
  </view>
</template>

<script>
import { getMyOrders, confirmDeliver } from '@/api/rider'

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
          searchParams: { status: 'M' }
        })
        if (res.code === 200 && res.data) {
          this.orderList = res.data.content || []
        }
      } catch (e) {}
      this.loading = false
      this.refreshing = false
    },
    async doDeliver(orderId) {
      uni.showModal({
        title: '确认送达',
        content: '确认已将商品送达给客户？',
        success: async (res) => {
          if (res.confirm) {
            try {
              const result = await confirmDeliver(orderId)
              if (result.code === 200) {
                uni.showToast({ title: '送达成功', icon: 'success' })
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
.rider-delivering { min-height: 100vh; background: #f5f5f5; }
.order-list { height: 100vh; padding: 20rpx; }
.order-card { background: #fff; border-radius: 16rpx; padding: 24rpx; margin-bottom: 20rpx; }
.order-top { display: flex; justify-content: space-between; margin-bottom: 16rpx; }
.order-sn { font-size: 28rpx; color: #666; }
.order-fee { font-size: 32rpx; font-weight: bold; color: #FF6B35; }
.order-info { margin-bottom: 16rpx; }
.address-line { display: flex; align-items: flex-start; margin-bottom: 10rpx; }
.dot { width: 12rpx; height: 12rpx; border-radius: 50%; margin-right: 12rpx; margin-top: 6rpx; flex-shrink: 0; background: #1890ff; }
.addr-detail { flex: 1; }
.addr-text { font-size: 26rpx; color: #333; display: block; }
.addr-user { font-size: 24rpx; color: #1890ff; margin-top: 4rpx; }
.order-bottom { display: flex; justify-content: space-between; align-items: center; }
.order-goods { font-size: 24rpx; color: #999; flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.action-btn { padding: 12rpx 40rpx; border-radius: 30rpx; font-size: 26rpx; color: #fff; }
.deliver-btn { background: linear-gradient(135deg, #1890ff, #40a9ff); }
.empty { text-align: center; color: #999; padding: 200rpx 0; font-size: 28rpx; }
.loading-tip { text-align: center; color: #999; padding: 20rpx; font-size: 24rpx; }
</style>
