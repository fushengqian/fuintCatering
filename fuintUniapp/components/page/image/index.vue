<template>
  <!-- 单图组 -->
  <view class="diy-imageSingle" :style="{ paddingBottom: `${itemStyle.paddingTop}px`, background: itemStyle.background }">
    <view class="item-image" v-for="(dataItem, index) in dataList" :key="index" :style="{ padding: `${itemStyle.paddingTop}px ${itemStyle.paddingLeft}px 0` }">
      <view class="nav-to" @click="onLink(dataItem.link)">
        <image class="image" :src="dataItem.imgUrl" mode="widthFix"></image>
      </view>
    </view>
  </view>
</template>

<script>
  import mixin from '../mixin'

  export default {
    name: "Images",

    /**
     * 组件的属性列表
     * 用于组件自定义设置
     */
    props: {
      itemIndex: String,
      itemStyle: Object,
      params: Object,
      dataList: Array
    },

    mixins: [mixin],

    /**
     * 组件的方法列表
     * 更新属性和数据的方法与更新页面数据的方法类似
     */
    methods: {

    }

  }
</script>

<style lang="scss" scoped>
  .diy-imageSingle .item-image .image {
    display: block;
    width: 100%;
  }
</style>
