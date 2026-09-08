<template>
  <!-- 搜索框 -->
  <view class="diy-search" :style="{ background: itemStyle.background || '#ffffff' }">
    <view class="inner" :class="itemStyle.searchStyle" @click="onTargetSearch">
      <view class="search-input" :style="{ textAlign: itemStyle.textAlign || 'center' }">
        <text class="search-icon iconfont icon-sousuo"></text>
        <text> {{ params.placeholder }}</text>
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
      itemIndex: String,
      itemStyle: Object,
      params: Object
    },

    /**
     * 组件的方法列表
     * 更新属性和数据的方法与更新页面数据的方法类似
     */
    methods: {

      /**
       * 跳转到搜索页面
       */
      onTargetSearch() {
        this.$navTo('pages/search/index')
      }

    }

  }
</script>

<style lang="scss" scoped>
  // 与微店页 @/components/search 保持一致的尺寸和背景色
  .diy-search {
    padding: 0rpx 10rpx 10rpx 10rpx;
    font-size: 28rpx;
  }

  .inner {
    height: 82rpx;
    background: #f5f5f5;
    border: solid 2rpx #ffffff;
    border-radius: 100rpx;
    overflow: hidden;

    &.radius {
      border-radius: 10rpx;
    }

    &.square {
      border-radius: 8rpx;
    }
  }

  .search-input {
    height: 82rpx;
    line-height: 82rpx;
    color: #6d6d6d;
    padding: 0 24rpx;
    background: transparent;
    .search-icon {
      float: left;
      font-size: 30rpx;
      font-weight: bold;
      margin-right: 8rpx;
    }
  }
</style>
