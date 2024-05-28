<template>
  <!-- 搜索框 -->
  <view class="search-wrapper">
    <view class="index-search" @click="onClick">
      <view class="index-cont-search t-c">
        <text class="search-icon iconfont icon-sousuo"></text>
        <text class="search-text">{{ tips }}</text>
      </view>
    </view>
  </view>
</template>

<script>
  export default {
    props: {
      tips: {
        type: String,
        default: '搜索关键字...'
      },
      itemStyle: Object,
    },
    data() {
      return {}
    },

    methods: {
      onClick() {
        this.$emit('event')
      }
    }
  }
</script>

<style lang="scss" scoped>
  .search-wrapper {
    padding: 0rpx 10rpx 10rpx 10rpx;
    display: block;
    position: fixed;
    z-index: 999999;
    width: 100%;
    background: #ffffff;
  }

  .index-search {
    border-bottom: 0;
    background: #f5f5f5;
    border-radius: 100rpx;
    overflow: hidden;
    font-size: 28rpx;
    color: #6d6d6d;
    box-sizing: border-box;
    height: 82rpx;
    line-height: 82rpx;
    border: solid 2rpx #ffffff;
    text-align: left;
    display: block;
    margin-top: 10rpx;
    .index-cont-search {
      width: 100%;
      font-size: 28rpx;
      background: #f5f5f5;
      text-align: center;
      padding-left: 30rpx;
    }

    .index-cont-search .search-icon {
      font-size: 30rpx;
      font-weight: bold;
      float: left;
    }

    .index-cont-search .search-text {
      margin-left: 5rpx;
      text-align: center;
    }

  }
</style>
