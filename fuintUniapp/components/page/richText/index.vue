<template>
  <!-- 富文本 -->
  <view class="diy-richText" :style="{ padding: `${itemStyle.paddingTop}px ${itemStyle.paddingLeft}px`, background: itemStyle.background }">
    <jyf-parser :html="params.content"></jyf-parser>
  </view>
</template>

<script>
  import jyfParser from "@/components/jyf-parser/jyf-parser"

  export default {

    /**
     * 组件的属性列表
     * 用于组件自定义设置
     */
    props: {
      itemStyle: Object,
      params: Object
    },

    components: {
      jyfParser
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
  .diy-richText {
    font-size: 28rpx;
  }
</style>
