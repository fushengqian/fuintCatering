<template>
  <!-- 定位店铺 -->
  <view class="main-loc">
      <view v-if="storeInfo.name" class="diy-location" :style="{ background: 'linear-gradient(to bottom,' + themeColor + ',' + themeColor + ')' }">
        <view class="inner" @click="onTargetLocation">
          <view class="location-input">
            <!-- 全部使用块级 view：避免 text(uni-text/inline) 内嵌 block 导致高度计算异常 -->
            <view class="store">
              <view class="store-title">
                <text class="name">{{ storeInfo.name }}</text>
                <text class="switch" v-if="storeInfo.single == 'N'">[切换店铺]</text>
              </view>
              <view class="address">
                <text class="location-icon iconfont icon-dingwei"></text>
                <text class="address-text">{{ storeInfo.address }}</text>
              </view>
            </view>
          </view>
        </view>
        <!-- 右侧桌号：挂在 .diy-location 上绝对定位居中，不依赖 .inner 的内部高度计算 -->
        <view v-if="tableInfo && tableInfo.code" class="table-box">
          <text class="table-label">桌号</text>
          <text class="table-code">{{ tableInfo.code }}</text>
        </view>
      </view>
  </view>
</template>

<script>
  export default {

    /**
     * 组件的属性列表
     * 用于组件自定义设置
     */
    props: {
      itemStyle: Object,
      storeInfo: Object,
      // 桌码信息（扫码进入时由 clientApi/system/config 返回），用于展示右侧桌号
      tableInfo: Object
    },

    /**
     * 组件的方法列表
     * 更新属性和数据的方法与更新页面数据的方法类似
     */
    methods: {
      /**
       * 跳转到定位页面页面
       */
      onTargetLocation() {
        this.$navTo('pages/location/index')
      }
    }

  }
</script>

<style lang="scss" scoped>
.main-loc {
  color: #ffffff;
  // 始终吸顶：在页面或父容器滚动时固定在顶部，不会随分类/商品列表一起滚走
  position: sticky;
  top: 0;
  z-index: 100;

  .diy-location {
    // 桌号的定位基准：绿色块自身高度（含上下 padding）一定包含完整两行文字，最可靠
    position: relative;
    background: linear-gradient(to bottom, $fuint-theme, $fuint-theme);
    padding: 3rpx 20rpx 16rpx 20rpx;
  }

  .inner {
    overflow: hidden;
    &.radius {
      border-radius: 10rpx;
    }
    &.round {
      border-radius: 60rpx;
    }
  }

  .location-input {
    color: #484848;
    padding-left: 10rpx;
  }

  // 右侧桌号：绝对定位 top:50% + translateY(-50%) 垂直居中于绿色块。
  // 因 .diy-location 上下 padding 不对称(上 3rpx / 下 16rpx)，几何中心比文字中心偏下约 6rpx，
  // 用 margin-top 抵消（不用 calc，兼容小程序/各端 WXSS）
  .table-box {
    position: absolute;
    right: 20rpx;
    top: 50%;
    transform: translateY(-50%);
    margin-top: -6rpx;
    padding: 6rpx 16rpx;
    border-radius: 24rpx;
    background: rgba(255, 255, 255, 0.18);
    display: flex;
    align-items: center;

    .table-label {
      font-size: 22rpx;
      color: rgba(255, 255, 255, 0.85);
    }

    .table-code {
      margin-left: 6rpx;
      font-size: 26rpx;
      font-weight: bold;
      color: #ffffff;
    }
  }
  
  // 店铺信息：块级结构，.inner 高度 = 店名行 + 地址行，桌号绝对定位才有正确基准
  .store {
      .store-title {
          display: flex;
          align-items: baseline;
      }
      .name {
          font-size: 32rpx;
          font-weight: bold;
          color: #ffffff;
      }
      .switch {
          margin-left: 15rpx;
          font-size: 22rpx;
          color: #ffffff;
      }
      .address {
          display: flex;
          align-items: center;
          margin-top: 2rpx;
          font-size: 23rpx;
          color: #ffffff;
          .location-icon {
            margin-right: 4rpx;
            font-size: 24rpx;
            color: #f03c3c;
            font-weight: bold;
          }
      }
  }
}
  
</style>
