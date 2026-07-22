<template>
  <view class="rider-stats">
    <!-- 模式切换 -->
    <view class="tabs">
      <view class="tab" :class="{ active: mode === 'today' }" @click="switchMode('today')">今日</view>
      <view class="tab" :class="{ active: mode === 'week' }" @click="switchMode('week')">近7天</view>
      <view class="tab" :class="{ active: mode === 'month' }" @click="switchMode('month')">近30天</view>
    </view>

    <!-- 汇总卡片 -->
    <view class="summary-cards">
      <view class="card">
        <text class="card-num">{{ statsData.totalOrders || 0 }}</text>
        <text class="card-label">配送单量</text>
      </view>
      <view class="card">
        <text class="card-num">{{ statsData.avgDailyOrders || 0 }}</text>
        <text class="card-label">日均单量</text>
      </view>
      <view class="card">
        <text class="card-num">¥{{ statsData.totalIncome || 0 }}</text>
        <text class="card-label">总收入</text>
      </view>
    </view>

    <!-- 趋势图 -->
    <view class="chart-section">
      <view class="chart-title">每日配送单量趋势</view>
      <view class="bar-chart">
        <view class="bar-item" v-for="(item, idx) in dailyDelivery" :key="idx">
          <view class="bar-val">{{ item.count || 0 }}</view>
          <view class="bar" :style="{ height: calcBarHeight(item.count, dailyDelivery) + 'rpx' }"></view>
          <view class="bar-label">{{ formatDateLabel(item.date) }}</view>
        </view>
        <view v-if="dailyDelivery.length === 0" class="chart-empty">暂无数据</view>
      </view>
    </view>

    <view class="chart-section">
      <view class="chart-title">每日收入趋势</view>
      <view class="bar-chart">
        <view class="bar-item" v-for="(item, idx) in dailyIncome" :key="idx">
          <view class="bar-val">¥{{ item.income || 0 }}</view>
          <view class="bar income-bar" :style="{ height: calcBarHeight(item.income, dailyIncome, true) + 'rpx' }"></view>
          <view class="bar-label">{{ formatDateLabel(item.date) }}</view>
        </view>
        <view v-if="dailyIncome.length === 0" class="chart-empty">暂无数据</view>
      </view>
    </view>
  </view>
</template>

<script>
import { getDeliveryStats } from '@/api/rider'

export default {
  data() {
    return {
      mode: 'week',
      statsData: {},
      dailyDelivery: [],
      dailyIncome: []
    }
  },
  onShow() {
    this.fetchStats()
  },
  methods: {
    async switchMode(mode) {
      this.mode = mode
      await this.fetchStats()
    },
    async fetchStats() {
      try {
        const res = await getDeliveryStats(this.mode)
        if (res.code === 200 && res.data) {
          this.statsData = res.data
          this.dailyDelivery = res.data.dailyDeliveryList || []
          this.dailyIncome = res.data.dailyIncomeList || []
        }
      } catch (e) {}
    },
    calcBarHeight(val, list, isIncome) {
      if (!list || list.length === 0) return 20
      const values = list.map(item => isIncome ? (item.income || 0) : (item.count || 0))
      const max = Math.max(...values)
      if (max === 0) return 20
      return Math.max(20, (val || 0) / max * 200)
    },
    formatDateLabel(dateStr) {
      if (!dateStr) return ''
      const parts = dateStr.split('-')
      if (parts.length >= 3) return parts[1] + '/' + parts[2]
      return dateStr
    }
  }
}
</script>

<style lang="scss" scoped>
.rider-stats { min-height: 100vh; background: #f5f5f5; padding-bottom: 40rpx; }
.tabs { display: flex; background: #fff; padding: 20rpx; }
.tab { flex: 1; text-align: center; padding: 16rpx 0; font-size: 28rpx; color: #666; border-radius: 10rpx; }
.tab.active { background: #FFF3E0; color: #FF6B35; font-weight: bold; }
.summary-cards { display: flex; padding: 20rpx; gap: 16rpx; }
.card { flex: 1; background: #fff; border-radius: 16rpx; padding: 30rpx 16rpx; text-align: center; box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.04); }
.card-num { font-size: 36rpx; font-weight: bold; color: #FF6B35; display: block; }
.card-label { font-size: 24rpx; color: #999; margin-top: 8rpx; }
.chart-section { background: #fff; margin: 0 20rpx 20rpx; border-radius: 16rpx; padding: 24rpx; }
.chart-title { font-size: 28rpx; font-weight: bold; color: #333; margin-bottom: 24rpx; }
.bar-chart { display: flex; align-items: flex-end; justify-content: space-around; height: 320rpx; }
.bar-item { display: flex; flex-direction: column; align-items: center; flex: 1; }
.bar-val { font-size: 20rpx; color: #666; margin-bottom: 6rpx; }
.bar { width: 36rpx; background: linear-gradient(to top, #FF8C42, #FFB347); border-radius: 6rpx 6rpx 0 0; min-height: 6rpx; }
.income-bar { background: linear-gradient(to top, #FF6B6B, #FF8E8E); }
.bar-label { font-size: 20rpx; color: #999; margin-top: 10rpx; white-space: nowrap; }
.chart-empty { text-align: center; color: #ccc; padding: 60rpx 0; font-size: 26rpx; width: 100%; }
</style>
