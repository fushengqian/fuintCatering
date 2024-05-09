<template>
  <!-- 视频组 -->
  <view class="diy-video" :style="{ padding: `${itemStyle.paddingTop}px 0` }">
    <video class="video" :style="{ height: `${itemStyle.height}px` }" :src="params.videoUrl" :poster="params.poster"
      :autoplay="params.autoplay == 1" controls></video>
  </view>
</template>

<script>
  export default {
    name: 'Videos',
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

    }

  }
</script>

<style lang="scss" scoped>
  .diy-video .video {
    width: 100%;
    display: block;
  }
</style>
