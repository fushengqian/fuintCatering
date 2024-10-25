<template>
  <view v-if="!isLoading" class="empty-content" :style="customStyle">
    <view class="empty-icon">
      <image class="image" src="/static/empty.png" mode="widthFix"></image>
    </view>
    <view class="tips">{{ tips }}</view>
    <slot name="slot"></slot>
  </view>
</template>

<script>
  export default {

    /**
     * 组件的属性列表
     * 用于组件自定义设置
     */
    props: {
      // 正在加载
      isLoading: {
        type: Boolean,
        default: true
      },
      // 自定义样式
      customStyle: {
        type: Object,
        default () {
          return {}
        }
      },
      // 提示的问题
      tips: {
        type: String,
        default: '亲，暂无相关数据'
      }
    },

    data() {
      return {}
    },

    methods: {

    }
  }
</script>

<style lang="scss" scoped>
  .empty-content {
    box-sizing: border-box;
    width: 100%;
    padding: 20rpx 0rpx 10rpx 0rpx;
    text-align: center;
    margin-top: 50rpx;
    .tips {
      font-size: 26rpx;
      color: gray;
      margin: 10rpx 0rpx 40rpx 0rpx;
    }

    .empty-icon .image {
      width: 160rpx;
    }

  }
</style>
