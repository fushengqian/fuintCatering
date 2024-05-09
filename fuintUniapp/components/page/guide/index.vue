<template>
  <!-- 辅助线 -->
  <view class="diy-guide" :style="{ padding: `${itemStyle.paddingTop}px 0`, background: itemStyle.background }">
    <view class="line" :style="{ borderTop: `${itemStyle.lineHeight}px ${itemStyle.lineStyle} ${itemStyle.lineColor}` }">
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
      itemStyle: Object
    },

    /**
     * 组件的方法列表
     * 更新属性和数据的方法与更新页面数据的方法类似
     */
    methods: {

    }

  }
</script>

<style lang="scss" scoped>
  .diy-guide .line {
    width: 100%;
  }
</style>
