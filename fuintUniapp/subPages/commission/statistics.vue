<template>
  <view class="container">
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
  </view>
</template>

<script>
  import * as CommissionApi from '@/api/commission'

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
        }
      }
    },
    onShow() {
      this.getOverview()
    },
    methods: {
      /**
       * 获取佣金概览
       */
      getOverview() {
        CommissionApi.overview()
          .then(result => {
            if (result.data) {
              this.overview = result.data
            }
          })
          .catch(() => {})
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
    background: linear-gradient(135deg, #113a28, #2ab075);
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

  /* 快捷入口 */
  .entry-section {
    margin: 0 20rpx;
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
</style>
