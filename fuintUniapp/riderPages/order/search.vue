<template>
  <view class="rider-search">
    <!-- 搜索栏 -->
    <view class="search-bar">
      <input class="search-input" v-model="orderSn" placeholder="输入订单号搜索" />
      <view class="search-btn" @click="doSearch">搜索</view>
    </view>
    <!-- 日期筛选 -->
    <view class="date-filter">
      <picker mode="date" :value="beginDate" @change="onBeginChange">
        <view class="date-picker">{{ beginDate || '开始日期' }}</view>
      </picker>
      <text class="date-sep">至</text>
      <picker mode="date" :value="endDate" @change="onEndChange">
        <view class="date-picker">{{ endDate || '结束日期' }}</view>
      </picker>
    </view>
    <!-- 结果列表 -->
    <scroll-view scroll-y class="order-list" @scrolltolower="loadMore">
      <view class="empty" v-if="orderList.length === 0 && !loading">
        <text>暂无配送记录</text>
      </view>
      <view class="order-card" v-for="item in orderList" :key="item.orderId" @click="goDetail(item.orderId)">
        <view class="order-top">
          <text class="order-sn">#{{ item.orderSn }}</text>
          <text class="order-status completed">{{ item.statusText }}</text>
        </view>
        <view class="order-info">
          <text class="info-text">配送费：¥{{ item.deliveryFee || 0 }}</text>
          <text class="info-text">金额：¥{{ item.payAmount || item.amount }}</text>
        </view>
        <view class="order-time">接单时间：{{ item.acceptTime || '--' }}</view>
      </view>
      <view v-if="loading" class="loading-tip">加载中...</view>
      <view v-if="orderList.length > 0 && noMore" class="loading-tip">没有更多了</view>
    </scroll-view>
  </view>
</template>

<script>
import { searchOrders } from '@/api/rider'

export default {
  data() {
    return {
      orderSn: '',
      beginDate: '',
      endDate: '',
      orderList: [],
      page: 1,
      pageSize: 10,
      loading: false,
      noMore: false
    }
  },
  methods: {
    onBeginChange(e) {
      this.beginDate = e.detail.value
    },
    onEndChange(e) {
      this.endDate = e.detail.value
    },
    async doSearch() {
      this.page = 1
      this.orderList = []
      this.noMore = false
      await this.fetchData()
    },
    async loadMore() {
      if (this.loading || this.noMore) return
      this.page++
      await this.fetchData()
    },
    async fetchData() {
      this.loading = true
      const searchParams = {}
      if (this.orderSn) searchParams.orderSn = this.orderSn
      if (this.beginDate) searchParams.beginTime = this.beginDate + ' 00:00:00'
      if (this.endDate) searchParams.endTime = this.endDate + ' 23:59:59'
      try {
        const res = await searchOrders({
          page: this.page,
          pageSize: this.pageSize,
          searchParams
        })
        if (res.code === 200 && res.data) {
          const list = res.data.content || []
          if (this.page === 1) {
            this.orderList = list
          } else {
            this.orderList = [...this.orderList, ...list]
          }
          if (list.length < this.pageSize) this.noMore = true
        }
      } catch (e) {}
      this.loading = false
    },
    goDetail(orderId) {
      uni.navigateTo({ url: '/riderPages/order/detail?orderId=' + orderId })
    }
  }
}
</script>

<style lang="scss" scoped>
.rider-search { min-height: 100vh; background: #f5f5f5; }
.search-bar { display: flex; padding: 20rpx; background: #fff; }
.search-input { flex: 1; height: 70rpx; background: #f5f5f5; border-radius: 35rpx; padding: 0 30rpx; font-size: 28rpx; }
.search-btn { width: 120rpx; height: 70rpx; line-height: 70rpx; text-align: center; background: #FF6B35; color: #fff; border-radius: 35rpx; margin-left: 20rpx; font-size: 28rpx; }
.date-filter { display: flex; align-items: center; padding: 16rpx 20rpx; background: #fff; border-top: 1rpx solid #f0f0f0; }
.date-picker { background: #f5f5f5; padding: 10rpx 24rpx; border-radius: 10rpx; font-size: 26rpx; color: #666; }
.date-sep { margin: 0 16rpx; font-size: 26rpx; color: #999; }
.order-list { height: calc(100vh - 180rpx); padding: 20rpx; }
.order-card { background: #fff; border-radius: 16rpx; padding: 24rpx; margin-bottom: 20rpx; }
.order-top { display: flex; justify-content: space-between; margin-bottom: 12rpx; }
.order-sn { font-size: 28rpx; color: #666; }
.order-status { font-size: 26rpx; padding: 4rpx 16rpx; border-radius: 8rpx; }
.order-status.completed { background: #f6ffed; color: #52c41a; }
.order-info { display: flex; gap: 40rpx; margin-bottom: 12rpx; }
.info-text { font-size: 26rpx; color: #666; }
.order-time { font-size: 24rpx; color: #999; }
.empty { text-align: center; color: #999; padding: 200rpx 0; font-size: 28rpx; }
.loading-tip { text-align: center; color: #999; padding: 20rpx; font-size: 24rpx; }
</style>
