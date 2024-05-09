<template>
  <!-- 图片橱窗 -->
  <view class="diy-window" :style="{ background: itemStyle.background, padding: `${itemStyle.paddingTop}px ${itemStyle.paddingLeft}px` }">
    <!-- matrix -->
    <view v-if="itemStyle.layout > -1" class="data-list" :class="[`avg-sm-${itemStyle.layout}`]">
      <view v-for="(dataItem, index) in dataList" :key="index" class="data-item" :style="{ padding: `${itemStyle.paddingTop}px ${itemStyle.paddingLeft}px` }">
        <view class="item-image" @click="onLink(dataItem.link)">
          <image class="image" mode="widthFix" :src="dataItem.imgUrl"></image>
        </view>
      </view>
    </view>
    <!-- display -->
    <view v-else class="display">
      <view class="display-left" :style="{ padding: `${itemStyle.paddingTop}px ${itemStyle.paddingLeft}px` }">
        <image class="image" @click="onLink(dataItem.link)" :src="dataList[0].imgUrl"></image>
      </view>
      <view class="display-right">
        <view v-if="dataList.length >= 2 " class="display-right1" :style="{ padding: `${itemStyle.paddingTop}px ${itemStyle.paddingLeft}px` }">
          <image class="image" @click="onLink(dataItem.link)" :src="dataList[1].imgUrl"></image>
        </view>
        <view class="display-right2">
          <view v-if="dataList.length >= 3 " class="left" :style="{ padding: `${itemStyle.paddingTop}px ${itemStyle.paddingLeft}px` }">
            <image class="image" @click="onLink(dataItem.link)" :src="dataList[2].imgUrl"></image>
          </view>
          <view v-if="dataList.length >= 4 " class="right" :style="{ padding: `${itemStyle.paddingTop}px ${itemStyle.paddingLeft}px` }">
            <image class="image" @click="onLink(dataItem.link)" :src="dataList[3].imgUrl"></image>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
  import mixin from '../mixin'
  export default {
    name: "Window",

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
  .diy-window .data-list::after {
    clear: both;
    content: " ";
    display: table;
  }

  .diy-window .data-list .data-item {
    float: left;
    box-sizing: border-box;
  }

  .diy-window .data-list .image {
    display: block;
    width: 100%;
  }

  /* 分列布局 */

  .diy-window .avg-sm-2>.data-item {
    width: 50%;
  }

  .diy-window .avg-sm-3>.data-item {
    width: 33.33333333%;
  }

  .diy-window .avg-sm-4>.data-item {
    width: 25%;
  }

  .diy-window .avg-sm-5>.data-item {
    width: 20%;
  }

  /* 橱窗样式 */

  .diy-window {
    box-sizing: border-box;
  }

  .diy-window .display {
    height: 0;
    width: 100%;
    margin: 0;
    padding-bottom: 50%;
    position: relative;
    box-sizing: border-box;
  }

  .diy-window .display .image {
    width: 100%;
    height: 100%;
  }

  .diy-window .display .display-left {
    width: 50%;
    height: 100%;
    position: absolute;
    top: 0;
    left: 0;
    box-sizing: border-box;
  }

  .diy-window .display .display-right {
    width: 50%;
    height: 100%;
    position: absolute;
    top: 0;
    left: 50%;
    box-sizing: border-box;
  }

  .diy-window .display .display-right1 {
    width: 100%;
    height: 50%;
    position: absolute;
    top: 0;
    box-sizing: border-box;
    left: 0;
  }

  .diy-window .display .display-right2 {
    width: 100%;
    height: 50%;
    position: absolute;
    top: 50%;
    left: 0;
    box-sizing: border-box;
  }

  .diy-window .display .display-right2 .left {
    width: 50%;
    height: 100%;
    position: absolute;
    top: 0;
    left: 0;
    box-sizing: border-box;
  }

  .diy-window .display .display-right2 .right {
    width: 50%;
    height: 100%;
    position: absolute;
    top: 0;
    left: 50%;
    box-sizing: border-box;
  }
</style>
