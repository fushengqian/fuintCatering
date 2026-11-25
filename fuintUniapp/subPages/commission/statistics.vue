<template>
  <view class="container" :style="themeVars">
    <!-- 概览统计卡片 -->
    <view class="overview-card">
      <view class="card-header">
        <view class="total-title">累计佣金（元）</view>
        <view class="total-amount">{{ overview.totalAmount || '0.00' }}</view>
      </view>
      <view class="card-body">
        <view class="stat-item">
          <view class="stat-value">{{ overview.amount || '0.00' }}</view>
          <view class="stat-label">待提现</view>
        </view>
        <view class="stat-divider"></view>
        <view class="stat-item">
          <view class="stat-value">{{ overview.withdrawAmount || '0.00' }}</view>
          <view class="stat-label">已提现</view>
        </view>
        <view class="stat-divider"></view>
        <view class="stat-item">
          <view class="stat-value">{{ overview.userCount || 0 }}</view>
          <view class="stat-label">邀请会员</view>
        </view>
        <view class="stat-divider"></view>
        <view class="stat-item">
          <view class="stat-value">{{ overview.orderCount || 0 }}</view>
          <view class="stat-label">分佣订单</view>
        </view>
      </view>
      <view class="withdraw-btn" v-if="canWithdraw" @click="openWithdraw">申请提现</view>
    </view>

    <!-- 推荐奖励区块 -->
    <view class="reward-section" v-if="showReward">
      <view class="reward-header">
        <view class="reward-head-left">
          <view class="reward-title">
            <text class="reward-title-text">推荐奖励</text>
            <text class="reward-title-tag">新</text>
          </view>
          <view class="reward-sub">邀请好友，得券和积分</view>
        </view>
        <view class="reward-rule-link" @click="goRuleDesc()">
          <text>奖励说明</text>
          <text class="iconfont icon-xiangyoujiantou"></text>
        </view>
      </view>
      <view class="reward-stats">
        <view class="reward-stat">
          <view class="reward-stat-value">{{ reward.inviteCount || 0 }}</view>
          <view class="reward-stat-label">已邀请好友</view>
        </view>
        <view class="reward-stat">
          <view class="reward-stat-value">{{ reward.couponCount || 0 }}</view>
          <view class="reward-stat-label">已获券（张）</view>
        </view>
        <view class="reward-stat">
          <view class="reward-stat-value">{{ reward.pointCount || 0 }}</view>
          <view class="reward-stat-label">已获积分</view>
        </view>
      </view>
      <view class="reward-tip" v-if="showRules">
        若好友退款且奖励已被使用，退款将被拒绝
      </view>
      <view class="reward-btn" v-if="showRules" @click="goSharePage">
        立即邀请好友
      </view>
    </view>

    <!-- 快捷入口 -->
    <view class="entry-section">
      <view class="entry-item" @click="goInviteList">
        <view class="entry-icon icon-invite">
          <text class="iconfont icon-tuandui"></text>
        </view>
        <view class="entry-info">
          <view class="entry-title">邀请明细</view>
          <view class="entry-desc">查看邀请的会员列表</view>
        </view>
        <text class="iconfont icon-xiangyoujiantou"></text>
      </view>
      <view class="entry-item" @click="goOrderList">
        <view class="entry-icon icon-order">
          <text class="iconfont icon-qpdingdan"></text>
        </view>
        <view class="entry-info">
          <view class="entry-title">佣金订单</view>
          <view class="entry-desc">查看佣金来源订单</view>
        </view>
        <text class="iconfont icon-xiangyoujiantou"></text>
      </view>
      <view class="entry-item" @click="goSharePage">
        <view class="entry-icon icon-share">
          <text class="iconfont icon-fenxiang"></text>
        </view>
        <view class="entry-info">
          <view class="entry-title">邀请好友</view>
          <view class="entry-desc">立即邀请好友赚佣金</view>
        </view>
        <text class="iconfont icon-xiangyoujiantou"></text>
      </view>
    </view>

    <!-- 奖励记录 -->
    <view class="log-section" v-if="logList.length > 0">
      <view class="section-title">奖励记录</view>
      <view class="log-item" v-for="item in logList" :key="item.id">
        <view class="log-icon" :class="item.rewardType == 'point' ? 'log-icon-point' : 'log-icon-coupon'">
          <text class="iconfont" :class="item.rewardType == 'point' ? 'icon-jifen' : 'icon-youhuiquan'"></text>
        </view>
        <view class="log-main">
          <view class="log-content">{{ rewardContent(item) }}</view>
          <view class="log-source">{{ rewardSource(item) }}</view>
        </view>
        <view class="log-right">
          <view class="log-status" :class="'status-' + item.status">{{ statusName(item.status) }}</view>
          <view class="log-time">{{ formatTime(item.createTime) }}</view>
        </view>
      </view>
    <!-- 奖励记录 -->
    <view class="log-section" v-if="logList.length > 0">
      <view class="section-title">奖励记录</view>
      <view class="log-item" v-for="item in logList" :key="item.id">
        <view class="log-icon" :class="item.rewardType == 'point' ? 'log-icon-point' : 'log-icon-coupon'">
          <text class="iconfont" :class="item.rewardType == 'point' ? 'icon-jifen' : 'icon-youhuiquan'"></text>
        </view>
        <view class="log-main">
          <view class="log-content">{{ rewardContent(item) }}</view>
          <view class="log-source">{{ rewardSource(item) }}</view>
        </view>
        <view class="log-right">
          <view class="log-status" :class="'status-' + item.status">{{ statusName(item.status) }}</view>
          <view class="log-time">{{ formatTime(item.createTime) }}</view>
        </view>
      </view>
      <view class="load-status" v-if="logLoading">加载中...</view>
      <view class="load-status" v-else-if="!hasMore">没有更多了</view>
    </view>

    <!-- 申请提现弹窗 -->
    <view class="withdraw-mask" v-if="showWithdraw" @click="closeWithdraw">
      <view class="withdraw-dialog" @click.stop>
        <view class="withdraw-title">申请提现</view>
        <view class="withdraw-amount-row">
          <text class="withdraw-symbol">￥</text>
          <input class="withdraw-input" type="digit" v-model="withdrawAmount" disabled placeholder="提现金额为全部可提现金额" />
        </view>
        <view class="withdraw-tip">提现金额为全部可提现金额 ￥{{ overview.amount || '0.00' }}，提交后由管理员审核打款</view>
        <view class="reward-rule-link" @click="goRuleDesc('withdraw')">
          <text>查看提现规则说明</text>
          <text class="iconfont icon-xiangyoujiantou"></text>
        </view>
        <view class="withdraw-actions">
          <view class="withdraw-cancel" @click="closeWithdraw">取消</view>
          <view class="withdraw-confirm" :class="{ 'withdraw-confirm-disabled': submitting }" @click="submitWithdraw">提交申请</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
  import * as CommissionApi from '@/api/commission'

  const pageSize = 10

  export default {
    data() {
      return {
        // 概览数据
        overview: {
          totalAmount: '0.00',
          amount: '0.00',
          withdrawAmount: '0.00',
          userCount: 0,
          orderCount: 0
        },
        // 推荐奖励数据
        reward: {
          rules: [],
          inviteCount: 0,
          couponCount: 0,
          pointCount: 0
        },
        // 奖励记录
        logList: [],
        page: 1,
        hasMore: true,
        logLoading: false,
        // 申请提现
        showWithdraw: false,
        withdrawAmount: 
''
,
        submitting: false
      }
    },
    computed: {
      // 有可提现金额时显示申请提现按钮
      canWithdraw() {
        return parseFloat(this.overview.amount || 
'0'
) > 0
      },
      // 有启用中的推荐奖励方案时才展示活动规则与邀请入口
      showRules() {
        const reward = this.reward || {}
        return !!(reward.rules && reward.rules.length > 0)
      },
      // 无活动且无历史奖励时隐藏整个推荐奖励区块
      showReward() {
        const reward = this.reward || {}
        const hasData = (reward.inviteCount || 0) > 0 || (reward.couponCount || 0) > 0 || (reward.pointCount || 0) > 0
        return this.showRules || hasData
      }
    },
    onShow() {
      this.getOverview()
      this.getRewardSummary()
      this.refreshLogs()
    },
    onReachBottom() {
      this.loadMoreLogs()
    },
    methods: {
      /**
       * 获取佣金概览
       */
      getOverview() {
        return CommissionApi.overview()
          .then(result => {
            if (result.data) {
              this.overview = result.data
            }
          })
          .catch(() => {})
      },

      /**
       * 获取推荐奖励数据（活动规则+统计）
       */
      getRewardSummary() {
        CommissionApi.rewards()
          .then(result => {
            if (result.data) {
              this.reward = result.data
            }
          })
          .catch(() => {})
      },

      /**
       * 刷新奖励记录
       */
      refreshLogs() {
        this.page = 1
        this.hasMore = true
        this.logList = []
        this.getRewardLogs()
      },

      /**
       * 获取奖励记录
       */
      getRewardLogs() {
        if (this.logLoading) {
          return
        }
        this.logLoading = true
        CommissionApi.rewardLogs({ page: this.page, pageSize: pageSize })
          .then(result => {
            const data = result.data || {}
            const list = data.content || []
            this.logList = this.page > 1 ? this.logList.concat(list) : list
            this.hasMore = this.page < (data.totalPages || 1)
            this.logLoading = false
          })
          .catch(() => {
            this.logLoading = false
          })
      },

      /**
       * 上拉加载更多
       */
      loadMoreLogs() {
        if (!this.hasMore || this.logLoading) {
          return
        }
        this.page++
        this.getRewardLogs()
      },

      /**
       * 奖励内容文案
       */
      rewardContent(item) {
        if (item.rewardType == 'coupon') {
          return `「${item.couponName || '卡券'}」×${item.couponNum || 1}`
        }
        if (item.rewardType == 'point') {
          return `积分 +${item.point || 0}`
        }
        return item.description || '奖励'
      },

      /**
       * 奖励来源文案
       */
      rewardSource(item) {
        const name = item.inviteeName || '好友'
        if (item.scene == 'register') {
          return `来自好友「${name}」注册`
        }
        return `来自好友「${name}」的订单${item.orderSn ? ' ' + item.orderSn : ''}`
      },

      /**
       * 奖励状态
       */
      statusName(status) {
        const names = { A: '待发放', B: '已发放', C: '发放失败', D: '已追回' }
        return names[status] || '--'
      },

      /**
       * 时间格式化
       */
      formatTime(time) {
        if (!time) {
          return ''
        }
        return String(time).substring(0, 16)
      },

      /**
       * 跳转邀请明细
       */
      goInviteList() {
        this.$navTo('subPages/commission/invite')
      },

      /**
       * 跳转佣金订单
       */
      goOrderList() {
        this.$navTo('subPages/commission/orders')
      },

      /**
       * 跳转邀请好友
       */
      goSharePage() {
        this.$navTo('pages/share/index')
      },

      /**
       * 跳转奖励说明
       * @param {string} tab 默认选中的tab（commission/recommend）
       */
      /**
       * 跳转奖励说明
       * @param {string} tab 默认选中的tab（commission/recommend）
       */
      goRuleDesc(tab) {
        this.$navTo('subPages/commission/rules', tab ? { tab } : {})
      },

      /**
       * 打开申请提现弹窗
       */
      openWithdraw() {
        this.withdrawAmount = this.overview.amount || ''
        this.showWithdraw = true
      },

      /**
       * 关闭申请提现弹窗
       */
      closeWithdraw() {
        this.showWithdraw = false
      },

      /**
       * 提交提现申请（管理员审核后打款）
       */
      submitWithdraw() {
        // 提现金额固定为全部可提现金额，不支持修改
        const amount = parseFloat(this.overview.amount || '0')
        if (!amount || amount <= 0) {
          uni.showToast({ title: '暂无可提现金额', icon: 'none' })
          return
        }
        if (this.submitting) {
          return
        }
        this.submitting = true
        CommissionApi.withdraw({ amount: amount.toFixed(2) })
          .then((res) => {
            this.showWithdraw = false
            const repeated = res && res.data === false
            const message = repeated ? '提现请求已经在审核中，请耐心等待' : '申请已提交，等待管理员审核'
            // 先刷新数据再弹提示，避免加载动画覆盖toast
            this.getOverview().finally(() => {
              uni.showToast({ title: message, icon: 'none' })
            })
          })
          .catch(() => {})
          .finally(() => {
            this.submitting = false
          })
      }

    }
  }
</script>

<style lang="scss" scoped>
  .container {
    min-height: 100vh;
    background: #f7f7f7;
    padding-bottom: 40rpx;
  }

  /* 概览统计卡片 */
  .overview-card {
    margin: 20rpx;
    background: linear-gradient(135deg, $fuint-theme, #2ab075);
    border-radius: 16rpx;
    padding: 30rpx;
    color: #fff;
  }

  .card-header {
    text-align: center;
    padding-bottom: 30rpx;
    border-bottom: 1rpx solid rgba(255, 255, 255, 0.3);
  }

  .total-title {
    font-size: 24rpx;
    opacity: 0.9;
    margin-bottom: 10rpx;
  }

  .total-amount {
    font-size: 56rpx;
    font-weight: bold;
    letter-spacing: 2rpx;
  }

  .card-body {
    display: flex;
    align-items: center;
    justify-content: space-around;
    padding-top: 30rpx;
  }

  .stat-item {
    text-align: center;
    flex: 1;
  }

  .stat-value {
    font-size: 32rpx;
    font-weight: bold;
    margin-bottom: 8rpx;
  }

  .stat-label {
    font-size: 22rpx;
    opacity: 0.85;
  }

  .stat-divider {
    width: 1rpx;
    height: 40rpx;
    background: rgba(255, 255, 255, 0.3);
  }

  /* 申请提现按钮 */
  .withdraw-btn {
    margin-top: 30rpx;
    height: 72rpx;
    line-height: 72rpx;
    text-align: center;
    background: #fff;
    color: var(--theme-primary);
    font-size: 28rpx;
    font-weight: bold;
    border-radius: 36rpx;
  }

  /* 申请提现弹窗 */
  .withdraw-mask {
    position: fixed;
    left: 0;
    top: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.5);
    z-index: 999;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .withdraw-dialog {
    width: 560rpx;
    background: #fff;
    border-radius: 20rpx;
    padding: 40rpx 36rpx 32rpx;
  }

  .withdraw-title {
    font-size: 32rpx;
    font-weight: bold;
    color: #333;
    text-align: center;
    margin-bottom: 32rpx;
  }

  .withdraw-amount-row {
    display: flex;
    align-items: center;
    border-bottom: 1rpx solid #eee;
    padding-bottom: 12rpx;
    margin-bottom: 16rpx;
  }

  .withdraw-symbol {
    font-size: 36rpx;
    font-weight: bold;
    color: #333;
    margin-right: 8rpx;
  }

  .withdraw-input {
    flex: 1;
    font-size: 40rpx;
    font-weight: bold;
    color: #333;
    height: 60rpx;
    line-height: 60rpx;
  }

  .withdraw-tip {
    font-size: 22rpx;
    color: #999;
    margin-bottom: 36rpx;
  }

  .withdraw-actions {
    display: flex;
    align-items: center;
  }

  .withdraw-cancel {
    flex: 1;
    height: 72rpx;
    line-height: 72rpx;
    text-align: center;
    font-size: 28rpx;
    color: #666;
    background: #f5f5f5;
    border-radius: 36rpx;
    margin-right: 20rpx;
  }

  .withdraw-confirm {
    flex: 1;
    height: 72rpx;
    line-height: 72rpx;
    text-align: center;
    font-size: 28rpx;
    color: #fff;
    background: var(--theme-primary);
    border-radius: 36rpx;
  }

  .withdraw-confirm-disabled {
    opacity: 0.6;
  }

  /* 推荐奖励区块 */
  .reward-section {
    margin: 0 20rpx 20rpx;
    background: linear-gradient(135deg, #fff7ec, #fffdfd);
    border: 1rpx solid #ffe3bd;
    border-radius: 16rpx;
    padding: 30rpx;
  }

  .reward-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 24rpx;
  }

  .reward-rule-link {
    display: flex;
    align-items: center;
    font-size: 22rpx;
    color: #b98c58;
    flex-shrink: 0;
    padding: 8rpx 0 8rpx 16rpx;

    .iconfont {
      font-size: 20rpx;
      margin-left: 4rpx;
    }
  }

  .reward-title {
    display: flex;
    align-items: center;
  }

  .reward-title-text {
    font-size: 32rpx;
    font-weight: bold;
    color: #7a4a12;
  }

  .reward-title-tag {
    font-size: 20rpx;
    color: #fff;
    background: linear-gradient(135deg, #ff8a65, #ff6e40);
    border-radius: 14rpx;
    padding: 2rpx 12rpx;
    margin-left: 12rpx;
  }

  .reward-sub {
    font-size: 22rpx;
    color: #b98c58;
    margin-top: 6rpx;
  }

  .reward-stats {
    display: flex;
    align-items: center;
  }

  .reward-stat {
    flex: 1;
    text-align: center;
  }

  .reward-stat-value {
    font-size: 36rpx;
    font-weight: bold;
    color: #ff6e40;
    margin-bottom: 6rpx;
  }

  .reward-stat-label {
    font-size: 22rpx;
    color: #b98c58;
  }

  .reward-tip {
    font-size: 22rpx;
    color: #b98c58;
    margin-top: 24rpx;
  }

  .reward-btn {
    margin-top: 20rpx;
    height: 76rpx;
    line-height: 76rpx;
    text-align: center;
    border-radius: 40rpx;
    background: linear-gradient(135deg, #ff8a65, #ff6e40);
    color: #fff;
    font-size: 28rpx;
    font-weight: bold;
  }

  /* 快捷入口 */
  .entry-section {
    margin: 0 20rpx 20rpx;
    background: #fff;
    border-radius: 16rpx;
    overflow: hidden;
  }

  .entry-item {
    display: flex;
    align-items: center;
    padding: 28rpx 30rpx;
    border-bottom: 1rpx solid #f5f5f5;

    &:last-child {
      border-bottom: none;
    }

    .icon-arrow-right {
      color: #ccc;
      font-size: 28rpx;
    }
  }

  .entry-icon {
    width: 80rpx;
    height: 80rpx;
    border-radius: 16rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 20rpx;
    flex-shrink: 0;

    .iconfont {
      font-size: 36rpx;
      color: #fff;
    }
  }

  .icon-invite {
    background: linear-gradient(135deg, #ff8a65, #ff6e40);
  }

  .icon-order {
    background: linear-gradient(135deg, #5b86e5, #36d1dc);
  }

  .icon-share {
    background: linear-gradient(135deg, #11998e, #38ef7d);
  }

  .entry-info {
    flex: 1;
    min-width: 0;
  }

  .entry-title {
    font-size: 28rpx;
    font-weight: bold;
    color: #333;
    margin-bottom: 6rpx;
  }

  .entry-desc {
    font-size: 22rpx;
    color: #999;
  }

  /* 奖励记录 */
  .log-section {
    margin: 0 20rpx;
    background: #fff;
    border-radius: 16rpx;
    padding: 10rpx 30rpx 20rpx;
  }

  .section-title {
    font-size: 30rpx;
    font-weight: bold;
    color: #333;
    padding: 20rpx 0;
    border-bottom: 1rpx solid #f5f5f5;
  }

  .log-item {
    display: flex;
    align-items: center;
    padding: 24rpx 0;
    border-bottom: 1rpx solid #f5f5f5;

    &:last-of-type {
      border-bottom: none;
    }
  }

  .log-icon {
    width: 70rpx;
    height: 70rpx;
    border-radius: 14rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 20rpx;
    flex-shrink: 0;

    .iconfont {
      font-size: 34rpx;
      color: #fff;
    }
  }

  .log-icon-coupon {
    background: linear-gradient(135deg, #ff8a65, #ff6e40);
  }

  .log-icon-point {
    background: linear-gradient(135deg, #ffb75e, #ed8f03);
  }

  .log-main {
    flex: 1;
    min-width: 0;
  }

  .log-content {
    font-size: 27rpx;
    color: #333;
    font-weight: bold;
    margin-bottom: 8rpx;
  }

  .log-source {
    font-size: 22rpx;
    color: #999;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .log-right {
    text-align: right;
    flex-shrink: 0;
    margin-left: 16rpx;
  }

  .log-status {
    display: inline-block;
    font-size: 22rpx;
    padding: 4rpx 14rpx;
    border-radius: 16rpx;
    margin-bottom: 8rpx;
  }

  .status-A {
    color: #ff9800;
    background: #fff3e0;
  }

  .status-B {
    color: #4caf50;
    background: #e8f5e9;
  }

  .status-C {
    color: #f44336;
    background: #ffebee;
  }

  .status-D {
    color: #909399;
    background: #f4f4f5;
  }

  .log-time {
    font-size: 22rpx;
    color: #ccc;
  }

  .load-status {
    text-align: center;
    font-size: 24rpx;
    color: #bbb;
    padding: 20rpx 0 6rpx;
  }
</style>
