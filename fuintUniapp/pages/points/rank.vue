<template>
  <view class="container">
    <!-- 顶部背景 -->
    <view class="rank-header">
      <view class="rank-title">积分排行榜</view>
      <view class="rank-subtitle">看看谁是积分达人</view>
    </view>

    <!-- Tab 切换 -->
    <view class="tab-bar">
      <view
        v-for="(tab, index) in tabs"
        :key="index"
        class="tab-item"
        :class="{ active: currentTab === tab.key }"
        @click="switchTab(tab.key)"
      >
        {{ tab.label }}
      </view>
    </view>

    <!-- 排行榜列表 -->
    <view class="rank-list" v-if="!isLoading">
      <view
        v-for="(item, index) in rankList"
        :key="index"
        class="rank-item"
      >
        <!-- 排名徽章 -->
        <view class="rank-badge">
          <text v-if="index === 0" class="badge-medal medal-1">🥇</text>
          <text v-else-if="index === 1" class="badge-medal medal-2">🥈</text>
          <text v-else-if="index === 2" class="badge-medal medal-3">🥉</text>
          <text v-else class="badge-num">{{ index + 1 }}</text>
        </view>

        <!-- 头像 -->
        <image
          class="rank-avatar"
          :src="item.avatar || '/static/images/default-avatar.png'"
          mode="aspectFill"
        ></image>

        <!-- 用户信息 -->
        <view class="rank-info">
          <text class="rank-name">{{ item.name || '匿名用户' }}</text>
          <text class="rank-id">ID: {{ item.userId }}</text>
        </view>

        <!-- 积分 -->
        <view class="rank-point">
          <text class="point-num">{{ item.point }}</text>
          <text class="point-label">积分</text>
        </view>
      </view>

      <!-- 空状态 -->
      <view v-if="rankList.length === 0" class="empty-state">
        <text class="empty-text">暂无排行数据</text>
      </view>
    </view>

    <!-- 加载中 -->
    <view v-if="isLoading" class="loading-state">
      <text class="loading-text">加载中...</text>
    </view>
  </view>
</template>

<script>
  import * as PointApi from '@/api/points/log'

  export default {
    data() {
      return {
        tabs: [
          { label: '总榜单', key: 'total' },
          { label: '月榜单', key: 'month' },
          { label: '周榜单', key: 'week' }
        ],
        currentTab: 'total',
        rankList: [],
        isLoading: true
      }
    },

    onLoad() {
      this.getRankList()
    },

    methods: {
      switchTab(key) {
        if (this.currentTab === key) return
        this.currentTab = key
        this.getRankList()
      },

      getRankList() {
        const app = this
        app.isLoading = true
        PointApi.rank({ type: app.currentTab })
          .then(result => {
            app.rankList = result.data || []
            app.isLoading = false
          })
          .catch(() => {
            app.rankList = []
            app.isLoading = false
          })
      }
    }
  }
</script>

<style lang="scss" scoped>
  .container {
    min-height: 100vh;
    background: #f5f5f5;
  }

  // 顶部标题区
  .rank-header {
    background: linear-gradient(135deg, #f9211c, #ff6335);
    padding: 60rpx 40rpx 40rpx;
    text-align: center;
    color: #fff;

    .rank-title {
      font-size: 40rpx;
      font-weight: bold;
    }

    .rank-subtitle {
      font-size: 24rpx;
      margin-top: 10rpx;
      opacity: 0.8;
    }
  }

  // Tab 栏
  .tab-bar {
    display: flex;
    background: #fff;
    padding: 0 30rpx;
    border-bottom: 1rpx solid #f0f0f0;

    .tab-item {
      flex: 1;
      height: 88rpx;
      line-height: 88rpx;
      text-align: center;
      font-size: 28rpx;
      color: #666;
      position: relative;

      &.active {
        color: #f9211c;
        font-weight: bold;

        &::after {
          content: '';
          position: absolute;
          bottom: 0;
          left: 50%;
          transform: translateX(-50%);
          width: 48rpx;
          height: 4rpx;
          background: #f9211c;
          border-radius: 2rpx;
        }
      }
    }
  }

  // 排行列表
  .rank-list {
    padding: 20rpx 30rpx;

    .rank-item {
      display: flex;
      align-items: center;
      background: #fff;
      border-radius: 16rpx;
      padding: 24rpx 24rpx;
      margin-bottom: 16rpx;
      box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.04);
    }

    // 排名
    .rank-badge {
      width: 60rpx;
      text-align: center;
      flex-shrink: 0;

      .badge-medal {
        font-size: 36rpx;
      }

      .badge-num {
        font-size: 28rpx;
        color: #999;
        font-weight: bold;
      }
    }

    // 头像
    .rank-avatar {
      width: 80rpx;
      height: 80rpx;
      border-radius: 50%;
      margin: 0 20rpx;
      flex-shrink: 0;
      background: #eee;
    }

    // 用户信息
    .rank-info {
      flex: 1;
      overflow: hidden;

      .rank-name {
        display: block;
        font-size: 28rpx;
        color: #333;
        font-weight: 500;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .rank-id {
        display: block;
        font-size: 22rpx;
        color: #999;
        margin-top: 4rpx;
      }
    }

    // 积分
    .rank-point {
      flex-shrink: 0;
      text-align: right;

      .point-num {
        display: block;
        font-size: 32rpx;
        color: #f9211c;
        font-weight: bold;
      }

      .point-label {
        display: block;
        font-size: 20rpx;
        color: #999;
      }
    }
  }

  // 空状态
  .empty-state {
    padding: 120rpx 0;
    text-align: center;

    .empty-text {
      font-size: 28rpx;
      color: #999;
    }
  }

  // 加载中
  .loading-state {
    padding: 200rpx 0;
    text-align: center;

    .loading-text {
      font-size: 28rpx;
      color: #999;
    }
  }
</style>
