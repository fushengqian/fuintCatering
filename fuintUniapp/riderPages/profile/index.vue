<template>
  <view class="rider-profile">
    <view class="profile-header">
      <image :src="profile.avatar || '/static/default-avatar.png'" mode="aspectFill" class="avatar"></image>
      <text class="name">{{ profile.name || '--' }}</text>
      <text class="mobile">{{ profile.mobile || '--' }}</text>
    </view>

    <view class="card">
      <view class="info-row">
        <text class="label">骑手编号</text>
        <text class="value">{{ profile.riderNo || '--' }}</text>
      </view>
      <view class="info-row">
        <text class="label">所属商户</text>
        <text class="value">商户ID: {{ profile.merchantId || '--' }}</text>
      </view>
      <view class="info-row">
        <text class="label">账号状态</text>
        <text class="value status-ok" v-if="profile.status === 'A'">正常</text>
        <text class="value status-off" v-else>禁用</text>
      </view>
      <view class="info-row">
        <text class="label">配送总单量</text>
        <text class="value">{{ profile.totalOrders || 0 }} 单</text>
      </view>
      <view class="info-row">
        <text class="label">配送总收入</text>
        <text class="value income">¥{{ profile.totalIncome || 0 }}</text>
      </view>
    </view>

    <view class="logout-btn" @click="doLogout">
      <text>退出骑手配送</text>
    </view>
  </view>
</template>

<script>
import { getProfileInfo } from '@/api/rider'

export default {
  data() {
    return {
      profile: {}
    }
  },
  onShow() {
    this.loadProfile()
  },
  methods: {
    async loadProfile() {
      try {
        const res = await getProfileInfo()
        if (res.code === 200 && res.data) {
          this.profile = res.data
        }
      } catch (e) {}
    },
    doLogout() {
      uni.showModal({
        title: '退出骑手配送',
        content: '确定要返回会员主页吗？',
        success: (res) => {
          if (res.confirm) {
            uni.switchTab({ url: '/pages/user/index' })
          }
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.rider-profile { min-height: 100vh; background: #f5f5f5; }
.profile-header { background: linear-gradient(135deg, #FF6B35, #FF8C42); padding: 60rpx 30rpx 40rpx; display: flex; flex-direction: column; align-items: center; }
.avatar { width: 120rpx; height: 120rpx; border-radius: 60rpx; border: 4rpx solid rgba(255,255,255,0.5); margin-bottom: 20rpx; }
.name { font-size: 36rpx; font-weight: bold; color: #fff; }
.mobile { font-size: 26rpx; color: rgba(255,255,255,0.8); margin-top: 8rpx; }
.card { background: #fff; margin: 20rpx; border-radius: 16rpx; padding: 30rpx; }
.info-row { display: flex; justify-content: space-between; align-items: center; padding: 20rpx 0; border-bottom: 1rpx solid #f5f5f5; }
.info-row:last-child { border-bottom: none; }
.label { font-size: 28rpx; color: #666; }
.value { font-size: 28rpx; color: #333; }
.value.status-ok { color: #52c41a; }
.value.status-off { color: #FF4D4F; }
.value.income { color: #FF6B35; font-weight: bold; }
.logout-btn { margin: 60rpx 20rpx; background: #fff; border-radius: 40rpx; padding: 24rpx; text-align: center; color: #FF4D4F; font-size: 30rpx; }
</style>
