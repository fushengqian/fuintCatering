<template>
  <!-- 搜索框 -->
  <view class="diy-search" :style="boxStyle">
    <view class="inner" :class="itemStyle.searchStyle" :style="inputBoxStyle" @click="onTargetSearch">
      <view class="search-input" :style="inputTextStyle">
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

    computed: {
      // 组件整体：仅背景色，间距沿用样式表中的默认 padding
      boxStyle() {
        const style = this.itemStyle || {}
        return `background: ${style.background || '#ffffff'};`
      },
      // 后台数值按 px 配置，这里按 750 设计稿换算后交给 uni 转成运行时 px。
      // 说明：动态 style 字符串中直接写 rpx 只在小程序端生效（H5 端编译期不会转换），改用 px 可两端一致
      inputHeight() {
        const height = parseInt(this.itemStyle && this.itemStyle.height, 10)
        if (isNaN(height) || height <= 0) return ''
        return `${uni.upx2px(height * 2)}px`
      },
      inputRadius() {
        const radius = parseInt(this.itemStyle && this.itemStyle.inputRadius, 10)
        if (isNaN(radius) || radius < 0) return ''
        return `${uni.upx2px(radius * 2)}px`
      },
      // 输入框边框：边框色必填，粗细为 0 时表示无边框；未配置时沿用样式表中的默认边框
      inputBorder() {
        const style = this.itemStyle || {}
        const width = parseInt(style.borderWidth, 10)
        if (isNaN(width) || width < 0 || !style.borderColor) return ''
        return `${uni.upx2px(width * 2)}px ${style.borderStyle || 'solid'} ${style.borderColor}`
      },
      // 输入框外框：背景色 + 高度 + 圆角 + 边框，未配置时沿用样式表中的默认值
      inputBoxStyle() {
        const parts = []
        if (this.itemStyle && this.itemStyle.inputBg) parts.push(`background: ${this.itemStyle.inputBg}`)
        if (this.inputHeight) parts.push(`height: ${this.inputHeight}`)
        if (this.inputRadius) parts.push(`border-radius: ${this.inputRadius}`)
        if (this.inputBorder) parts.push(`border: ${this.inputBorder}`)
        return parts.join('; ') + (parts.length ? ';' : '')
      },
      // 输入区域：高度与行高跟随输入框，保证文字垂直居中；未配置高度时仅输出对齐方式
      inputTextStyle() {
        const style = this.itemStyle || {}
        const parts = [`text-align: ${style.textAlign || 'center'}`]
        if (this.inputHeight) {
          parts.push(`height: ${this.inputHeight}`)
          parts.push(`line-height: ${this.inputHeight}`)
        }
        return parts.join('; ') + ';'
      }
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
