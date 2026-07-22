<template>
  <view class="rider-income">
    <!-- 模式切换 -->
    <view class="tabs">
      <view class="tab" :class="{ active: mode === 'today' }" @click="switchMode('today')">今日</view>
      <view class="tab" :class="{ active: mode === 'week' }" @click="switchMode('week')">近7天</view>
      <view class="tab" :class="{ active: mode === 'month' }" @click="switchMode('month')">近30天</view>
    </view>

    <!-- 收入汇总 -->
    <view class="income-summary">
      <text class="total-label">总收入</text>
      <text class="total-amount">¥{{ statsData.totalIncome || 0 }}</text>
      <view class="income-detail">
        <view class="detail-item">
          <text class="detail-num">{{ statsData.totalOrders || 0 }}</text>
          <text class="detail-label">总单量</text>
        </view>
        <view class="detail-item">
          <text class="detail-num">{{ statsData.avgDailyOrders || 0 }}</text>
          <text class="detail-label">日均单量</text>
        </view>
        <view class="detail-item">
          <text class="detail-num">¥{{ avgPerOrder }}</text>
          <text class="detail-label">单均收入</text>
        </view>
      </view>
    </view>

    <!-- 每日收入趋势 -->
    <view class="chart-section">
      <view class="chart-title">每日收入趋势</view>
      <view class="bar-chart">
        <view class="bar-item" v-for="(item, idx) in dailyIncome" :key="idx">
          <view class="bar-val">¥{{ item.income || 0 }}</view>
          <view class="bar" :style="{ height: calcBarHeight(item.income) + 'rpx' }"></view>
          <view class="bar-label">{{ formatLabel(item.date) }}</view>
        </view>
        <view v-if="dailyIncome.length === 0" class="chart-empty">暂无数据</view>
      </view>
    </view>
  </view>
</template>

<script>
import { getIncomeStats } from '@/api/rider'

export default {
  data() {
    return {
      mode: 'week',
      statsData: {},
      dailyIncome: []
    }
  },
  computed: {
    avgPerOrder() {
      if (!this.statsData.totalOrders || this.statsData.totalOrders === 0) return '0.00'
      return ((this.statsData.totalIncome || 0) / this.statsData.totalOrders).toFixed(2)
    }
  },
  onShow() {
    this.fetchData()
  },
  methods: {
    async switchMode(mode) {
      this.mode = mode
      await this.fetchData()
    },
    async fetchData() {
      try {
        const res = await getIncomeStats(this.mode)
        if (res.code === 200 && res.data) {
          this.statsData = res.data
          this.dailyIncome = res.data.dailyIncomeList || []
        }
      } catch (e) {}
    },
    calcBarHeight(val) {
      if (!this.dailyIncome || this.dailyIncome.length === 0) return 20
      const max = Math.max(...this.dailyIncome.map(i => i.income || 0))
      if (max === 0) return 20
      return Math.max(20, (val || 0) / max * 200)
    },
    formatLabel(dateStr) {
      if (!dateStr) return ''
      const parts = dateStr.split('-')
      return parts.length >= 3 ? parts[1] + '/' + parts[2] : dateStr
    }
  }
}
</script>

<style lang="scss" scoped>
.rider-income { min-height: 100vh; background: #f5f5f5; padding-bottom: 40rpx; }
.tabs { display: flex; background: #fff; padding: 20rpx; }
.tab { flex: 1; text-align: center; padding: 16rpx 0; font-size: 28rpx; color: #666; border-radius: 10rpx; }
.tab.active { background: #FFF3E0; color: #FF6B35; font-weight: bold; }
.income-summary { background: linear-gradient(135deg, #FF6B35, #FF8C42); margin: 20rpx; border-radius: 16rpx; padding: 40rpx 30rpx; text-align: center; }
.total-label { font-size: 26rpx; color: rgba(255,255,255,0.8); }
.total-amount { font-size: 60rpx; font-weight: bold; color: #fff; display: block; margin: 16rpx 0; }
.income-detail { display: flex; justify-content: space-around; margin-top: 20rpx; }
.detail-item { display: flex; flex-direction: column; align-items: center; }
.detail-num { font-size: 30rpx; font-weight: bold; color: #fff; }
.detail-label { font-size: 22rpx; color: rgba(255,255,255,0.7); margin-top: 6rpx; }
.chart-section { background: #fff; margin: 0 20rpx; border-radius: 16rpx; padding: 24rpx; }
.chart-title { font-size: 28rpx; font-weight: bold; color: #333; margin-bottom: 24rpx; }
.bar-chart { display: flex; align-items: flex-end; justify-content: space-around; height: 320rpx; }
.bar-item { display: flex; flex-direction: column; align-items: center; flex: 1; }
.bar-val { font-size: 20rpx; color: #666; margin-bottom: 6rpx; }
.bar { width: 36rpx; background: linear-gradient(to top, #FF6B6B, #FF8E8E); border-radius: 6rpx 6rpx 0 0; min-height: 6rpx; }
.bar-label { font-size: 20rpx; color: #999; margin-top: 10rpx; white-space: nowrap; }
.chart-empty { text-align: center; color: #ccc; padding: 60rpx 0; font-size: 26rpx; width: 100%; }
</style>
