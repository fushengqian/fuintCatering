<template>
  <view class="rider-home">
    <!-- 顶部概览 -->
    <view class="header-section">
      <view class="header-bg">
        <view class="today-overview">
          <text class="welcome-text">今日配送</text>
          <view class="stats-row">
            <view class="stat-item" v-for="(item, idx) in statsCards" :key="idx">
              <text class="stat-num">{{ item.num }}</text>
              <text class="stat-label">{{ item.label }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 功能入口 -->
    <view class="menu-section">
      <view class="menu-row">
        <view class="menu-item" @click="goOrderPool">
          <view class="menu-icon-wrap">
            <text class="iconfont icon-qpdingdan menu-icon"></text>
          </view>
          <text class="menu-text">接单大厅</text>
        </view>
        <view class="menu-item" @click="goPickup">
          <view class="menu-icon-wrap">
            <text class="iconfont icon-daifahuo menu-icon"></text>
          </view>
          <text class="menu-text">待取货</text>
          <view class="badge" v-if="pickupCount > 0">{{ pickupCount }}</view>
        </view>
        <view class="menu-item" @click="goDelivering">
          <view class="menu-icon-wrap">
            <text class="iconfont icon-daishouhuo menu-icon"></text>
          </view>
          <text class="menu-text">配送中</text>
          <view class="badge" v-if="deliveringCount > 0">{{ deliveringCount }}</view>
        </view>
        <view class="menu-item" @click="goStats">
          <view class="menu-icon-wrap">
            <text class="iconfont icon-dianji menu-icon"></text>
          </view>
          <text class="menu-text">数据统计</text>
        </view>
      </view>
      <view class="menu-row">
        <view class="menu-item" @click="goSearch">
          <view class="menu-icon-wrap">
            <text class="iconfont icon-sousuo menu-icon"></text>
          </view>
          <text class="menu-text">搜索订单</text>
        </view>
        <view class="menu-item" @click="goProfile">
          <view class="menu-icon-wrap">
            <text class="iconfont icon-profile menu-icon"></text>
          </view>
          <text class="menu-text">个人信息</text>
        </view>
      </view>
    </view>

    <!-- 待接单列表 -->
    <view class="pool-section">
      <view class="section-header">
        <text class="section-title">待接订单</text>
        <text class="section-action" @click="refreshPool">刷新</text>
      </view>
      <scroll-view scroll-y class="pool-list" @scrolltolower="loadMore">
        <view class="pool-empty" v-if="poolList.length === 0 && !loading">
          <text>暂无待接订单，休息一下~</text>
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
            <view class="accept-btn" @click.stop="doAccept(item.orderId)">
              接单
            </view>
          </view>
        </view>
        <view v-if="loading" class="loading-tip">加载中...</view>
        <view v-if="poolList.length > 0 && noMore" class="loading-tip">没有更多了</view>
      </scroll-view>
    </view>
  </view>
</template>

<script>
import { getOrderPool, acceptOrder, getOverview } from '@/api/rider'

export default {
  data() {
    return {
      todayOrders: 0,
      deliveringCount: 0,
      completedOrders: 0,
      pickupCount: 0,
      poolList: [],
      page: 1,
      pageSize: 10,
      loading: false,
      noMore: false
    }
  },
  computed: {
    statsCards() {
      return [
        { num: this.todayOrders, label: '今日接单' },
        { num: this.deliveringCount, label: '配送中' },
        { num: this.completedOrders + this.pickupCount, label: '进行中' }
      ]
    }
  },
  onShow() {
    this.loadOverview()
    this.refreshPool()
  },
  methods: {
    async loadOverview() {
      try {
        const res = await getOverview()
        if (res.code === 200 && res.data) {
          this.todayOrders = res.data.todayOrders || 0
          this.deliveringCount = res.data.deliveringOrders || 0
          this.completedOrders = res.data.completedOrders || 0
        }
      } catch (e) {
        console.error('获取概览失败', e)
      }
    },
    async refreshPool() {
      this.page = 1
      this.poolList = []
      this.noMore = false
      await this.fetchPool()
    },
    async loadMore() {
      if (this.loading || this.noMore) return
      this.page++
      await this.fetchPool()
    },
    async fetchPool() {
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
        console.error('获取订单池失败', e)
      }
      this.loading = false
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
                this.refreshPool()
                this.loadOverview()
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
    goOrderPool() {
      uni.navigateTo({ url: '/riderPages/order/pool' })
    },
    goPickup() {
      uni.navigateTo({ url: '/riderPages/order/pickup' })
    },
    goDelivering() {
      uni.navigateTo({ url: '/riderPages/order/delivering' })
    },
    goStats() {
      uni.navigateTo({ url: '/riderPages/stats/overview' })
    },
    goSearch() {
      uni.navigateTo({ url: '/riderPages/order/search' })
    },
    goProfile() {
      uni.navigateTo({ url: '/riderPages/profile/index' })
    },
    goDetail(orderId) {
      uni.navigateTo({ url: '/riderPages/order/detail?orderId=' + orderId })
    }
  }
}
</script>

<style lang="scss" scoped>
.rider-home {
  min-height: 100vh;
  background: #f5f5f5;
}
.header-section {
  background: linear-gradient(135deg, #FF6B35, #FF8C42);
  padding: 40rpx 30rpx 60rpx;
}
.welcome-text {
  color: rgba(255,255,255,0.8);
  font-size: 26rpx;
}
.stats-row {
  display: flex;
  justify-content: space-around;
  margin-top: 30rpx;
}
.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}
.stat-num {
  font-size: 48rpx;
  font-weight: bold;
  color: #fff;
}
.stat-label {
  font-size: 24rpx;
  color: rgba(255,255,255,0.8);
  margin-top: 8rpx;
}
.menu-section {
  background: #fff;
  margin: -20rpx 20rpx 0;
  border-radius: 16rpx;
  padding: 30rpx 0;
  position: relative;
  z-index: 1;
  box-shadow: 0 2rpx 10rpx rgba(0,0,0,0.05);
}
.menu-row {
  display: flex;
  justify-content: space-around;
  padding: 0 20rpx;
  & + .menu-row {
    margin-top: 20rpx;
    justify-content: flex-start;
    padding-left: 40rpx;
  }
}
.menu-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  width: 140rpx;
}
.menu-icon-wrap {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  background: #FFF3E0;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 10rpx;
}
.menu-icon {
  font-size: 40rpx;
  color: #FF6B35;
}
.menu-text {
  font-size: 24rpx;
  color: #333;
}
.badge {
  position: absolute;
  top: -5rpx;
  right: 20rpx;
  background: #FF4D4F;
  color: #fff;
  font-size: 20rpx;
  min-width: 32rpx;
  height: 32rpx;
  line-height: 32rpx;
  text-align: center;
  border-radius: 16rpx;
  padding: 0 8rpx;
}
.pool-section {
  margin: 20rpx;
}
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 0;
}
.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}
.section-action {
  font-size: 26rpx;
  color: #FF6B35;
}
.pool-list {
  height: calc(100vh - 600rpx);
}
.order-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.04);
}
.order-top {
  display: flex;
  justify-content: space-between;
  margin-bottom: 16rpx;
}
.order-sn {
  font-size: 28rpx;
  color: #666;
}
.order-fee {
  font-size: 30rpx;
  font-weight: bold;
  color: #FF6B35;
}
.order-info {
  margin-bottom: 16rpx;
}
.address-line {
  display: flex;
  align-items: flex-start;
  margin-bottom: 10rpx;
}
.dot {
  width: 12rpx;
  height: 12rpx;
  border-radius: 50%;
  margin-right: 12rpx;
  margin-top: 6rpx;
  flex-shrink: 0;
  &.pickup {
    background: #52c41a;
  }
  &.delivery {
    background: #1890ff;
  }
}
.address-text {
  font-size: 26rpx;
  color: #333;
  line-height: 1.4;
}
.order-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.order-goods {
  font-size: 24rpx;
  color: #999;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-right: 20rpx;
}
.accept-btn {
  background: #FF6B35;
  color: #fff;
  font-size: 26rpx;
  padding: 10rpx 40rpx;
  border-radius: 30rpx;
}
.pool-empty {
  text-align: center;
  color: #999;
  padding: 100rpx 0;
  font-size: 28rpx;
}
.loading-tip {
  text-align: center;
  color: #999;
  padding: 20rpx 0;
  font-size: 24rpx;
}
</style>
