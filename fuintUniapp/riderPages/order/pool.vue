<template>
  <view class="rider-pool">
    <scroll-view scroll-y class="pool-list" @scrolltolower="loadMore" refresher-enabled @refresherrefresh="onRefresh" :refresher-triggered="refreshing">
      <view class="pool-empty" v-if="poolList.length === 0 && !loading">
        <text>暂无待接订单</text>
      </view>
      <view class="order-card" v-for="item in poolList" :key="item.orderId" @click="goDetail(item.orderId)">
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
          <text class="order-amount">¥{{ item.payAmount || item.amount }}</text>
        </view>
        <view class="accept-row">
          <view class="accept-btn" @click.stop="doAccept(item.orderId)">立即接单</view>
        </view>
      </view>
      <view v-if="loading" class="loading-tip">加载中...</view>
      <view v-if="poolList.length > 0 && noMore" class="loading-tip">没有更多了</view>
    </scroll-view>
  </view>
</template>

<script>
import { getOrderPool, acceptOrder } from '@/api/rider'

export default {
  data() {
    return {
      poolList: [],
      page: 1,
      pageSize: 10,
      loading: false,
      noMore: false,
      refreshing: false
    }
  },
  onShow() {
    this.onRefresh()
  },
  methods: {
    async fetchData() {
      this.loading = true
      try {
        const res = await getOrderPool({
          page: this.page,
          pageSize: this.pageSize,
          searchParams: {}
        })
        if (res.code === 200 && res.data) {
          const list = res.data.content || []
          if (this.page === 1) {
            this.poolList = list
          } else {
            this.poolList = [...this.poolList, ...list]
          }
          if (list.length < this.pageSize) {
            this.noMore = true
          }
        }
      } catch (e) {
        console.error(e)
      }
      this.loading = false
      this.refreshing = false
    },
    async onRefresh() {
      this.page = 1
      this.poolList = []
      this.noMore = false
      await this.fetchData()
    },
    async loadMore() {
      if (this.loading || this.noMore) return
      this.page++
      await this.fetchData()
    },
    async doAccept(orderId) {
      uni.showModal({
        title: '确认接单',
        content: '确定要接此订单吗？',
        success: async (res) => {
          if (res.confirm) {
            try {
              const result = await acceptOrder(orderId)
              if (result.code === 200) {
                uni.showToast({ title: '接单成功', icon: 'success' })
                this.onRefresh()
              } else {
                uni.showToast({ title: result.message || '接单失败', icon: 'none' })
              }
            } catch (e) {
              uni.showToast({ title: '接单失败', icon: 'none' })
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
.rider-pool {
  min-height: 100vh;
  background: #f5f5f5;
}
.pool-list {
  height: 100vh;
  padding: 20rpx;
}
.order-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
}
.order-top {
  display: flex;
  justify-content: space-between;
  margin-bottom: 16rpx;
}
.order-sn { font-size: 28rpx; color: #666; }
.order-fee { font-size: 32rpx; font-weight: bold; color: #FF6B35; }
.order-info { margin-bottom: 16rpx; }
.address-line {
  display: flex;
  align-items: flex-start;
  margin-bottom: 10rpx;
}
.dot {
  width: 12rpx; height: 12rpx;
  border-radius: 50%;
  margin-right: 12rpx;
  margin-top: 6rpx;
  flex-shrink: 0;
  &.pickup { background: #52c41a; }
  &.delivery { background: #1890ff; }
}
.address-text { font-size: 26rpx; color: #333; line-height: 1.4; }
.order-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 16rpx;
  border-bottom: 1rpx solid #f0f0f0;
}
.order-goods { font-size: 24rpx; color: #999; flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; margin-right: 20rpx; }
.order-amount { font-size: 28rpx; color: #333; font-weight: bold; }
.accept-row { margin-top: 16rpx; display: flex; justify-content: flex-end; }
.accept-btn { background: linear-gradient(135deg, #FF6B35, #FF8C42); color: #fff; font-size: 28rpx; padding: 12rpx 50rpx; border-radius: 30rpx; }
.pool-empty { text-align: center; color: #999; padding: 200rpx 0; font-size: 28rpx; }
.loading-tip { text-align: center; color: #999; padding: 20rpx 0; font-size: 24rpx; }
</style>
