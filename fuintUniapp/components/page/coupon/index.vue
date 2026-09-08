<template>
  <!-- 卡券组 -->
  <view class="diy-coupon" :style="couponWrapStyle">
    <view v-if="itemStyle.title" class="coupon-title" :style="{ color: titleColor }">
      <text class="txt">{{ itemStyle.title }}</text>
    </view>
    <view class="coupon-list">
      <view
        class="coupon-card"
        v-for="(dataItem, index) in dataList"
        :key="index"
        :style="couponCardStyle"
        @click="onTargetCoupon(dataItem.id, dataItem.type, dataItem.userCouponId)"
      >
        <view class="coupon-notch coupon-notch-left" :style="{ background: wrapBg }"></view>
        <view class="coupon-notch coupon-notch-right" :style="{ background: wrapBg }"></view>
        <view class="coupon-left">
          <view class="coupon-amount">
            <text v-if="dataItem.type === 'ZK' && dataItem.discount > 0">{{ dataItem.discount }}折</text>
            <text v-else>¥{{ dataItem.amount || 0 }}</text>
          </view>
          <view class="coupon-threshold">
            <text v-if="dataItem.minSendAmount > 0">满{{ dataItem.minSendAmount }}可用</text>
            <text v-else>无门槛</text>
          </view>
          <view class="coupon-scope">{{ scopeText(dataItem) }}</view>
        </view>
        <view class="coupon-right">
          <view class="coupon-btn" :class="{ state: dataItem.isReceive }" :style="couponBtnStyle">
            <text>{{ btnText(dataItem) }}</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
  import { getThemePrimary } from '@/utils/theme'

  export default {
    name: "Coupon",
    props: {
      itemIndex: String,
      itemStyle: Object,
      params: Object,
      dataList: Array
    },

    computed: {
      primary() {
        const p = getThemePrimary()
        // 主题未拉取或后台未配置时，使用项目默认青色兜底，避免纯白卡片
        return p && p !== '#ffffff' ? p : '#1abc9c'
      },
      // 外层背景优先使用后台配置；未配置或配置为白色/旧默认值时使用浅青色，与设计图/后台预览默认一致
      wrapBg() {
        const bg = this.itemStyle.background
        if (!bg || bg === '#ffffff' || bg === '#1890ff') {
          return '#e0f7fa'
        }
        return bg
      },
      // 卡片背景/标题色优先使用后台配置的文字色；未配置或配置为旧默认/白色时使用主题色
      themeColor() {
        const c = this.itemStyle.color
        if (!c || c === '#ffffff' || c === '#1890ff') {
          return this.primary
        }
        return c
      },
      titleColor() {
        // 外层背景为主题色时标题用白色，保证可读
        return this.wrapBg === this.primary ? '#ffffff' : this.themeColor
      },
      borderRadius() {
        const r = this.itemStyle.borderRadius
        return r === undefined || r === null || r === '' ? 16 : Number(r) * 2
      },
      // 注意：小程序端 :style 绑定对象会被序列化成 [object Object] 导致样式失效，
      // 因此统一返回 style 字符串（与 utils/theme.js buildThemeVars 的做法一致）
      couponWrapStyle() {
        return `background: ${this.wrapBg};`
      },
      couponCardStyle() {
        return `background: ${this.themeColor}; color: #ffffff; border-radius: ${this.borderRadius}rpx;`
      },
      couponBtnStyle() {
        return `background: #ffffff; color: ${this.themeColor};`
      }
    },

    methods: {
      scopeText(item) {
        // 兼容 applyGoods/useFor 字段，默认与后台预览保持一致
        if (item.applyGoods === 'allGoods' || item.useFor === 'allGoods') {
          return '全场通用'
        }
        return '指定商品可用'
      },
      btnText(item) {
        if (item.isReceive) {
          if (item.type === 'P') return '已预存'
          if (item.type === 'T') return '已领次卡'
          return '已领取'
        }
        if (item.type === 'P') return '立即预存'
        if (item.type === 'T') return '领取次卡'
        return '立即领取'
      },
      onTargetCoupon(couponId, type, userCouponId) {
        if (type === 'P') {
          this.$navTo(`subPages/prestore/buy`, { couponId })
        } else {
          if (type === 'C') {
            this.$navTo(`subPages/coupon/detail`, { couponId, userCouponId })
          } else if (type === 'T') {
            this.$navTo(`subPages/timer/detail`, { couponId, userCouponId })
          }
        }
      }
    }
  }
</script>

<style lang="scss" scoped>
  .diy-coupon {
    margin: 0 20rpx 20rpx 20rpx;
    padding: 20rpx 16rpx;
    border: 1rpx solid #e6e6e6;
    border-radius: 20rpx;
    box-sizing: border-box;
  }

  .coupon-title {
    font-size: 30rpx;
    font-weight: bold;
    padding: 10rpx 8rpx;
    .txt {
      border-left: solid currentColor 10rpx;
      padding-left: 10rpx;
    }
  }

  .coupon-list {
    display: flex;
    flex-wrap: wrap;
    margin: 0 -6rpx;
  }

  .coupon-card {
    box-sizing: border-box;
    width: calc(50% - 12rpx);
    margin: 6rpx;
    padding: 20rpx;
    display: flex;
    align-items: center;
    justify-content: space-between;
    min-height: 160rpx;
    position: relative;

    .coupon-notch {
      position: absolute;
      top: 50%;
      width: 24rpx;
      height: 24rpx;
      border-radius: 50%;
      transform: translateY(-50%);
      z-index: 1;
    }

    .coupon-notch-left {
      left: -12rpx;
    }

    .coupon-notch-right {
      right: -12rpx;
    }
  }

  .coupon-left {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
    justify-content: center;
  }

  .coupon-amount {
    font-size: 40rpx;
    font-weight: bold;
    line-height: 1.2;
  }

  .coupon-threshold {
    font-size: 24rpx;
    margin-top: 8rpx;
    opacity: 0.9;
  }

  .coupon-scope {
    font-size: 20rpx;
    margin-top: 8rpx;
    opacity: 0.75;
  }

  .coupon-right {
    flex-shrink: 0;
    margin-left: 16rpx;
  }

  .coupon-btn {
    height: 48rpx;
    line-height: 48rpx;
    padding: 0 22rpx;
    font-size: 22rpx;
    border-radius: 26rpx;
    text-align: center;
    white-space: nowrap;
    &.state {
      background: rgba(0, 0, 0, 0.1) !important;
      color: rgba(255, 255, 255, 0.8) !important;
    }
  }
</style>
