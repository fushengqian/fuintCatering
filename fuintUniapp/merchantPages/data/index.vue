<template>
  <view class="container">
    <!-- 时间筛选 -->
    <view class="time-filter">
      <picker
        mode="selector"
        :range="storeList"
        range-key="name"
        @change="onStoreChange"
      >
        <view class="store-select">
          <text>{{ currentStore.name }}</text>
          <text class="iconfont icon-arrow-down"></text>
        </view>
      </picker>
    </view>
    <view class="time-tabs">
      <view
        v-for="(tab, index) in timeTabs"
        :key="index"
        :class="['tab-item', { active: currentTab === tab.value }]"
        @click="onTabChange(tab.value)"
      >
        {{ tab.name }}
      </view>
    </view>

    <!-- 数据概况 -->
    <view class="section">
      <view class="section-title">
        <view class="title-icon"></view>
        <text class="title-text">数据概况</text>
      </view>
      <view class="overview-grid">
        <view class="overview-item">
          <view class="item-icon blue">
            <text class="iconfont icon-dingdan"></text>
          </view>
          <view class="item-content">
            <view class="item-value red">{{ statisticsData.orderCount }}</view>
            <view class="item-label">订单数（笔）</view>
          </view>
        </view>
        <view class="overview-item">
          <view class="item-icon orange">
            <text class="iconfont icon-weixinzhifu"></text>
          </view>
          <view class="item-content">
            <view class="item-value red">{{ statisticsData.tradeAmount }}</view>
            <view class="item-label">交易金额（元）</view>
          </view>
        </view>
        <view class="overview-item">
          <view class="item-icon green">
            <text class="iconfont icon-tuandui"></text>
          </view>
          <view class="item-content">
            <view class="item-value red">{{ statisticsData.newMember }}</view>
            <view class="item-label">新增会员数</view>
          </view>
        </view>
        <view class="overview-item">
          <view class="item-icon purple">
            <text class="iconfont icon-tuandui"></text>
          </view>
          <view class="item-content">
            <view class="item-value red">{{ statisticsData.activeMember }}</view>
            <view class="item-label">活跃会员数</view>
          </view>
        </view>
        <view class="overview-item">
          <view class="item-icon cyan">
            <text class="iconfont icon-tuandui"></text>
          </view>
          <view class="item-content">
            <view class="item-value red">{{ statisticsData.totalMember }}</view>
            <view class="item-label">总会员数</view>
          </view>
        </view>
        <view class="overview-item">
          <view class="item-icon pink">
            <text class="iconfont icon-weixinzhifu"></text>
          </view>
          <view class="item-content">
            <view class="item-value red">{{ statisticsData.totalPayAmount }}</view>
            <view class="item-label">总支付金额（元）</view>
          </view>
        </view>
        <view class="overview-item">
          <view class="item-icon yellow">
            <text class="iconfont icon-dingdan"></text>
          </view>
          <view class="item-content">
            <view class="item-value red">{{ statisticsData.totalOrderCount }}</view>
            <view class="item-label">总订单数（笔）</view>
          </view>
        </view>
        <view class="overview-item">
          <view class="item-icon teal">
            <text class="iconfont icon-tuandui"></text>
          </view>
          <view class="item-content">
            <view class="item-value red">{{ statisticsData.totalPayPeople }}</view>
            <view class="item-label">总支付人数</view>
          </view>
        </view>
      </view>
    </view>

    <!-- 运营走势 -->
    <view class="section">
      <view class="section-title">
        <view class="title-icon"></view>
        <text class="title-text">运营走势</text>
      </view>
      <view class="chart-container">
        <!-- 近七日订单数量 -->
        <view class="chart-box">
          <view class="chart-title">近七日订单数量</view>
          <view class="bar-chart">
            <view class="chart-y-axis">
              <text v-for="n in 5" :key="n" class="y-label">{{ 10 - (n-1)*2 }}</text>
            </view>
            <view class="chart-content">
              <view class="grid-lines">
                <view v-for="n in 5" :key="n" class="grid-line"></view>
              </view>
              <view class="bars-wrapper">
                <view
                  v-for="(item, index) in orderChartData"
                  :key="index"
                  class="bar-column"
                >
                  <view class="bar-wrapper">
                    <text class="bar-value">{{ item.value }}</text>
                    <view
                      class="bar"
                      :style="{ height: (item.value / 10 * 100) + '%' }"
                    ></view>
                  </view>
                  <text class="bar-label">{{ item.label }}</text>
                </view>
              </view>
            </view>
          </view>
          <view class="chart-legend">
            <view class="legend-dot red"></view>
            <text class="legend-text">订单统计</text>
          </view>
        </view>

        <!-- 近七日会员活跃数 -->
        <view class="chart-box">
          <view class="chart-title">近七日会员活跃数</view>
          <view class="line-chart">
            <view class="chart-y-axis">
              <text v-for="n in 7" :key="n" class="y-label">{{ 12 - (n-1)*2 }}</text>
            </view>
            <view class="chart-content">
              <view class="grid-lines">
                <view v-for="n in 7" :key="n" class="grid-line"></view>
              </view>
              <view class="line-wrapper">
                <svg viewBox="0 0 100 100" preserveAspectRatio="none" class="line-svg">
                  <polyline
                    :points="memberChartPoints"
                    fill="none"
                    stroke="#52c41a"
                    stroke-width="2"
                  />
                  <circle
                    v-for="(point, index) in memberChartPointsArray"
                    :key="index"
                    :cx="point.x"
                    :cy="point.y"
                    r="2"
                    fill="#52c41a"
                  />
                </svg>
                <view class="line-points">
                  <view
                    v-for="(item, index) in memberChartData"
                    :key="index"
                    class="point-wrapper"
                    :style="{ left: (index / (memberChartData.length - 1) * 100) + '%', bottom: (item.value / 12 * 100) + '%' }"
                  >
                    <text class="point-value">{{ item.value }}</text>
                  </view>
                </view>
              </view>
              <view class="x-labels">
                <text
                  v-for="(item, index) in memberChartData"
                  :key="index"
                  class="x-label"
                >{{ item.label }}</text>
              </view>
            </view>
          </view>
          <view class="chart-legend">
            <view class="legend-dot green"></view>
            <text class="legend-text">会员统计</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 数据排行 -->
    <view class="section">
      <view class="section-title">
        <view class="title-icon"></view>
        <text class="title-text">数据排行</text>
      </view>
      <view class="rank-container">
        <!-- 商品销售排行 -->
        <view class="rank-box">
          <view class="rank-title">商品销售排行</view>
          <view class="rank-table">
            <view class="table-header">
              <text class="th th-id">ID</text>
              <text class="th th-name">商品名称</text>
              <text class="th th-num">销售量</text>
              <text class="th th-amount">金额</text>
            </view>
            <view v-if="goodsRankData.length === 0" class="empty-tip">
              <text>暂无数据</text>
            </view>
            <view
              v-for="(item, index) in goodsRankData"
              :key="index"
              class="table-row"
            >
              <text class="td td-id">{{ item.id }}</text>
              <text class="td td-name">{{ item.name }}</text>
              <text class="td td-num">{{ item.num }}</text>
              <text class="td td-amount">{{ item.amount }}</text>
            </view>
          </view>
        </view>

        <!-- 用户消费排行 -->
        <view class="rank-box">
          <view class="rank-title">用户消费排行</view>
          <view class="rank-table">
            <view class="table-header">
              <text class="th th-id">ID</text>
              <text class="th th-name">用户名</text>
              <text class="th th-num">会员号</text>
              <text class="th th-amount">金额</text>
            </view>
            <view v-if="userRankData.length === 0" class="empty-tip">
              <text>暂无数据</text>
            </view>
            <view
              v-for="(item, index) in userRankData"
              :key="index"
              class="table-row"
            >
              <text class="td td-id">{{ item.id }}</text>
              <text class="td td-name">{{ item.name }}</text>
              <text class="td td-num">{{ item.memberNo }}</text>
              <text class="td td-amount">{{ item.amount }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>

  </view>
</template>

<script>
import * as ReportApi from '@/api/merchant/report'

export default {
  data() {
    return {
      // 当前日期
      currentDate: '',
      // 时间筛选tabs
      timeTabs: [
        { name: '今天', value: 'today' },
        { name: '昨天', value: 'yesterday' },
        { name: '近3天', value: '3days' },
        { name: '近7天', value: '7days' },
        { name: '近15天', value: '15days' },
        { name: '近30天', value: '30days' }
      ],
      currentTab: 'today',
      // 店铺列表
      storeList: [
        { id: 0, name: '全部店铺' }
      ],
      currentStore: { id: 0, name: '全部店铺' },
      // 数据概况
      statisticsData: {
        orderCount: 0,
        tradeAmount: '0.00',
        newMember: 0,
        activeMember: 0,
        totalMember: 0,
        totalPayAmount: '0.00',
        totalOrderCount: 0,
        totalPayPeople: 0
      },
      // 近七日订单数据
      orderChartData: [],
      // 近七日会员活跃数据
      memberChartData: [],
      // 商品销售排行
      goodsRankData: [],
      // 用户消费排行
      userRankData: []
    }
  },

  computed: {
    // 计算折线图的点坐标
    memberChartPoints() {
      const points = []
      const len = this.memberChartData.length
      if (len === 0) return ''
      const maxValue = Math.max(...this.memberChartData.map(item => item.value), 1)
      this.memberChartData.forEach((item, index) => {
        const x = (index / (len - 1)) * 100
        const y = 100 - (item.value / maxValue * 100)
        points.push(`${x},${y}`)
      })
      return points.join(' ')
    },
    memberChartPointsArray() {
      const points = []
      const len = this.memberChartData.length
      if (len === 0) return []
      const maxValue = Math.max(...this.memberChartData.map(item => item.value), 1)
      this.memberChartData.forEach((item, index) => {
        const x = (index / (len - 1)) * 100
        const y = 100 - (item.value / maxValue * 100)
        points.push({ x, y })
      })
      return points
    }
  },

  onLoad() {
    this.initDate()
    this.loadAllData()
  },

  methods: {
    // 初始化日期
    initDate() {
      const now = new Date()
      const year = now.getFullYear()
      const month = String(now.getMonth() + 1).padStart(2, '0')
      const day = String(now.getDate()).padStart(2, '0')
      this.currentDate = `${year}-${month}-${day}`
    },

    // 加载所有数据
    loadAllData() {
      this.getStoreList()
      this.getOverviewData()
      this.getChartData()
      this.getTopData()
    },

    // 获取店铺列表
    getStoreList() {
      ReportApi.getOverview({}, { load: false }).then(res => {
        if (res.data && res.data.storeList) {
          const storeList = [{ id: 0, name: '全部店铺' }]
          res.data.storeList.forEach(store => {
            storeList.push({ id: store.id, name: store.name })
          })
          this.storeList = storeList
          // 确保当前选择的店铺在列表中
          const currentStore = storeList.find(s => s.id === this.currentStore.id)
          if (currentStore) {
            this.currentStore = currentStore
          } else {
            this.currentStore = storeList[0]
          }
        }
      }).catch(err => {
        console.log('获取店铺列表失败', err)
      })
    },

    // 获取日期范围
    getDateRange() {
      const now = new Date()
      let startTime = ''
      let endTime = ''

      switch (this.currentTab) {
        case 'today':
          startTime = this.formatDate(now)
          endTime = this.formatDate(now)
          break
        case 'yesterday':
          const yesterday = new Date(now)
          yesterday.setDate(yesterday.getDate() - 1)
          startTime = this.formatDate(yesterday)
          endTime = this.formatDate(yesterday)
          break
        case '3days':
          const threeDaysAgo = new Date(now)
          threeDaysAgo.setDate(threeDaysAgo.getDate() - 2)
          startTime = this.formatDate(threeDaysAgo)
          endTime = this.formatDate(now)
          break
        case '7days':
          const sevenDaysAgo = new Date(now)
          sevenDaysAgo.setDate(sevenDaysAgo.getDate() - 6)
          startTime = this.formatDate(sevenDaysAgo)
          endTime = this.formatDate(now)
          break
        case '15days':
          const fifteenDaysAgo = new Date(now)
          fifteenDaysAgo.setDate(fifteenDaysAgo.getDate() - 14)
          startTime = this.formatDate(fifteenDaysAgo)
          endTime = this.formatDate(now)
          break
        case '30days':
          const thirtyDaysAgo = new Date(now)
          thirtyDaysAgo.setDate(thirtyDaysAgo.getDate() - 29)
          startTime = this.formatDate(thirtyDaysAgo)
          endTime = this.formatDate(now)
          break
      }

      return { startTime, endTime }
    },

    // 格式化日期
    formatDate(date) {
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      return `${year}-${month}-${day}`
    },

    // 获取数据概况
    getOverviewData() {
      const { startTime, endTime } = this.getDateRange()
      const params = {
        startTime,
        endTime,
        storeId: this.currentStore.id || 0
      }

      ReportApi.getOverview(params, { load: false }).then(res => {
        if (res.data) {
          this.statisticsData = {
            orderCount: res.data.orderCount || 0,
            tradeAmount: this.formatAmount(res.data.payAmount),
            newMember: res.data.userCount || 0,
            activeMember: res.data.activeUserCount || 0,
            totalMember: res.data.totalUserCount || 0,
            totalPayAmount: this.formatAmount(res.data.totalPayAmount),
            totalOrderCount: res.data.totalOrderCount || 0,
            totalPayPeople: res.data.totalPayUserCount || 0
          }
        }
      }).catch(err => {
        console.log('获取数据概况失败', err)
      })
    },

    // 获取运营走势数据
    getChartData() {
      const params = {
        tag: 'order,user_active',
        storeId: this.currentStore.id || 0
      }

      ReportApi.getChart(params, { load: false }).then(res => {
        if (res.data && res.data.data && res.data.labels) {
          const chartData = res.data.data
          const labels = res.data.labels

          // 转换订单数据
          if (chartData.length > 0 && chartData[0]) {
            this.orderChartData = labels.map((label, index) => ({
              label,
              value: chartData[0][index] ? Number(chartData[0][index]) : 0
            }))
          }

          // 转换会员活跃数据
          if (chartData.length > 1 && chartData[1]) {
            this.memberChartData = labels.map((label, index) => ({
              label,
              value: chartData[1][index] ? Number(chartData[1][index]) : 0
            }))
          }
        }
      }).catch(err => {
        console.log('获取运营走势失败', err)
      })
    },

    // 获取数据排行
    getTopData() {
      const { startTime, endTime } = this.getDateRange()
      const params = {
        startTime,
        endTime,
        storeId: this.currentStore.id || 0
      }

      ReportApi.getTop(params, { load: false }).then(res => {
        if (res.data) {
          // 商品销售排行
          if (res.data.goodsList) {
            this.goodsRankData = res.data.goodsList.map(item => ({
              id: item.id,
              name: item.name,
              num: item.num || 0,
              amount: this.formatAmount(item.amount)
            }))
          }

          // 用户消费排行
          if (res.data.memberList) {
            this.userRankData = res.data.memberList.map(item => ({
              id: item.id,
              name: item.name,
              memberNo: item.userNo || '',
              amount: this.formatAmount(item.amount)
            }))
          }
        }
      }).catch(err => {
        console.log('获取数据排行失败', err)
      })
    },

    // 格式化金额
    formatAmount(amount) {
      if (!amount) return '0.00'
      if (typeof amount === 'string') return amount
      return parseFloat(amount).toFixed(2)
    },

    // Tab切换
    onTabChange(value) {
      this.currentTab = value
      this.getOverviewData()
      this.getTopData()
    },

    // 店铺选择
    onStoreChange(e) {
      const index = e.detail.value
      this.currentStore = this.storeList[index]
      this.getOverviewData()
      this.getChartData()
      this.getTopData()
    }
  }
}
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background: #f5f7fa;
  padding-bottom: 30rpx;
}

// 时间筛选
.time-filter {
  display: flex;
  align-items: center;
  background: #fff;
  padding: 20rpx 24rpx;
  border-bottom: 1rpx solid #eee;

  .store-select {
    display: flex;
    align-items: center;
    font-size: 26rpx;
    color: #333;
    padding: 10rpx 24rpx 10rpx 20rpx;
    background: #f5f5f5;
    border-radius: 8rpx;

    .iconfont {
      margin-left: 10rpx;
      font-size: 22rpx;
      color: #999;
    }
  }
}

.time-tabs {
  display: flex;
  background: #fff;
  padding: 20rpx 24rpx;
  border-bottom: 1rpx solid #eee;

  .tab-item {
    padding: 10rpx 20rpx;
    font-size: 24rpx;
    color: #666;
    margin-right: 16rpx;
    border-radius: 6rpx;

    &.active {
      background: $fuint-theme;
      color: #fff;
    }
  }
}

// 通用区块
.section {
  margin: 20rpx;
  background: #fff;
  border-radius: 12rpx;
  padding: 24rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.05);
}

.section-title {
  display: flex;
  align-items: center;
  margin-bottom: 24rpx;

  .title-icon {
    width: 8rpx;
    height: 32rpx;
    background: $fuint-theme;
    border-radius: 4rpx;
    margin-right: 16rpx;
  }

  .title-text {
    font-size: 30rpx;
    font-weight: bold;
    color: #333;
  }
}

// 数据概况 - 2列4行布局
.overview-grid {
  display: flex;
  flex-wrap: wrap;
  margin: 0 -10rpx;

  .overview-item {
    width: 50%;
    display: flex;
    align-items: center;
    padding: 20rpx 10rpx;
    box-sizing: border-box;
    border-bottom: 1rpx solid #f0f0f0;

    &:nth-last-child(1),
    &:nth-last-child(2) {
      border-bottom: none;
    }

    .item-icon {
      width: 72rpx;
      height: 72rpx;
      border-radius: 16rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 16rpx;

      .iconfont {
        font-size: 32rpx;
        color: #fff;
      }

      &.blue {
        background: linear-gradient(135deg, #1890ff 0%, #36cfc9 100%);
      }

      &.orange {
        background: linear-gradient(135deg, #fa8c16 0%, #ffc53d 100%);
      }

      &.green {
        background: linear-gradient(135deg, #52c41a 0%, #95de64 100%);
      }

      &.purple {
        background: linear-gradient(135deg, #722ed1 0%, #b37feb 100%);
      }

      &.cyan {
        background: linear-gradient(135deg, #13c2c2 0%, #5cdbd3 100%);
      }

      &.pink {
        background: linear-gradient(135deg, #eb2f96 0%, #ffadd2 100%);
      }

      &.yellow {
        background: linear-gradient(135deg, #fadb14 0%, #fffb8f 100%);
      }

      &.teal {
        background: linear-gradient(135deg, #009688 0%, #4db6ac 100%);
      }
    }

    .item-content {
      flex: 1;

      .item-value {
        font-size: 32rpx;
        font-weight: bold;
        margin-bottom: 4rpx;

        &.red {
          color: #f5222d;
        }
      }

      .item-label {
        font-size: 22rpx;
        color: #666;
      }
    }
  }
}

// 运营走势 - 图表上下排列
.chart-container {
  .chart-box {
    margin-bottom: 30rpx;

    &:last-child {
      margin-bottom: 0;
    }

    .chart-title {
      font-size: 28rpx;
      font-weight: bold;
      color: #333;
      text-align: center;
      margin-bottom: 20rpx;
    }

    .chart-legend {
      display: flex;
      align-items: center;
      justify-content: center;
      margin-top: 20rpx;

      .legend-dot {
        width: 20rpx;
        height: 20rpx;
        border-radius: 4rpx;
        margin-right: 8rpx;

        &.red {
          background: #ff4d4f;
        }

        &.green {
          background: #52c41a;
        }
      }

      .legend-text {
        font-size: 22rpx;
        color: #666;
      }
    }
  }
}

// 柱状图
.bar-chart {
  display: flex;
  height: 280rpx;

  .chart-y-axis {
    width: 50rpx;
    display: flex;
    flex-direction: column-reverse;
    justify-content: space-between;
    padding-right: 8rpx;

    .y-label {
      font-size: 18rpx;
      color: #999;
      text-align: right;
    }
  }

  .chart-content {
    flex: 1;
    position: relative;
    display: flex;
    flex-direction: column;

    .grid-lines {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 36rpx;
      display: flex;
      flex-direction: column-reverse;
      justify-content: space-between;

      .grid-line {
        height: 1rpx;
        background: #f0f0f0;
      }
    }

    .bars-wrapper {
      flex: 1;
      display: flex;
      align-items: flex-end;
      justify-content: space-around;
      padding-bottom: 36rpx;
      position: relative;
      z-index: 1;

      .bar-column {
        flex: 1;
        display: flex;
        flex-direction: column;
        align-items: center;

        .bar-wrapper {
          width: 100%;
          display: flex;
          flex-direction: column;
          align-items: center;
          justify-content: flex-end;
          height: 180rpx;

          .bar-value {
            font-size: 18rpx;
            color: #333;
            margin-bottom: 4rpx;
          }

          .bar {
            width: 28rpx;
            background: #ff4d4f;
            border-radius: 4rpx 4rpx 0 0;
            min-height: 4rpx;
          }
        }

        .bar-label {
          font-size: 16rpx;
          color: #999;
          margin-top: 8rpx;
        }
      }
    }
  }
}

// 折线图
.line-chart {
  display: flex;
  height: 280rpx;

  .chart-y-axis {
    width: 50rpx;
    display: flex;
    flex-direction: column-reverse;
    justify-content: space-between;
    padding-right: 8rpx;

    .y-label {
      font-size: 18rpx;
      color: #999;
      text-align: right;
    }
  }

  .chart-content {
    flex: 1;
    position: relative;
    display: flex;
    flex-direction: column;

    .grid-lines {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 36rpx;
      display: flex;
      flex-direction: column-reverse;
      justify-content: space-between;

      .grid-line {
        height: 1rpx;
        background: #f0f0f0;
      }
    }

    .line-wrapper {
      flex: 1;
      position: relative;
      margin-bottom: 36rpx;

      .line-svg {
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
      }

      .line-points {
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;

        .point-wrapper {
          position: absolute;
          transform: translateX(-50%);

          .point-value {
            font-size: 18rpx;
            color: #52c41a;
            margin-bottom: 4rpx;
          }
        }
      }
    }

    .x-labels {
      display: flex;
      justify-content: space-between;
      padding: 0 5rpx;

      .x-label {
        font-size: 16rpx;
        color: #999;
      }
    }
  }
}

// 数据排行 - 表格上下排列
.rank-container {
  .rank-box {
    margin-bottom: 24rpx;

    &:last-child {
      margin-bottom: 0;
    }

    .rank-title {
      font-size: 26rpx;
      font-weight: bold;
      color: #333;
      text-align: left;
      margin-bottom: 16rpx;
      padding: 12rpx;
      background: #f5f7fa;
      border-radius: 8rpx;
    }

    .rank-table {
      border: 1rpx solid #e8e8e8;
      border-radius: 8rpx;
      overflow: hidden;

      .table-header {
        display: flex;
        background: #fafafa;
        padding: 16rpx 12rpx;
        border-bottom: 1rpx solid #e8e8e8;

        .th {
          font-size: 22rpx;
          font-weight: bold;
          color: #333;
          text-align: center;

          &.th-id {
            width: 90rpx;
          }

          &.th-name {
            flex: 1;
            overflow: hidden;
            text-overflow: ellipsis;
            text-align: left;
            white-space: nowrap;
          }

          &.th-num {
            width: 120rpx;
          }

          &.th-amount {
            width: 100rpx;
          }
        }
      }

      .table-row {
        display: flex;
        padding: 14rpx 12rpx;
        border-bottom: 1rpx solid #f0f0f0;

        &:last-child {
          border-bottom: none;
        }

        .td {
          font-size: 20rpx;
          color: #666;
          text-align: center;

          &.td-id {
            width: 60rpx;
          }

          &.td-name {
            flex: 1;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
            text-align: left;
          }

          &.td-num {
            width: 180rpx;
          }

          &.td-amount {
            width: 100rpx;
          }
        }
      }

      .empty-tip {
        display: flex;
        justify-content: center;
        align-items: center;
        padding: 60rpx 0;
        color: #999;
        font-size: 24rpx;
      }
    }
  }
}
</style>
