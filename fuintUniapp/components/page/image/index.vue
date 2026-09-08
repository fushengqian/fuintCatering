<template>
  <!-- 单图组 -->
  <view class="diy-imageSingle" :style="{ background: itemStyle.background }">
    <view class="item-image" v-for="(dataItem, index) in dataList" :key="index">
      <view class="nav-to" :style="imageBoxStyle" @click="onLink(dataItem.link)">
        <image class="image" :src="dataItem.imgUrl" mode="aspectFill"></image>
      </view>
    </view>
  </view>
</template>

<script>
  import mixin from '../mixin'

  export default {
    name: "Images",

    props: {
      itemIndex: String,
      itemStyle: Object,
      params: Object,
      dataList: Array
    },

    mixins: [mixin],

    computed: {
      // 小程序端 :style 绑定对象会变成 [object Object]，统一返回 style 字符串
      imageBoxStyle() {
        const style = this.itemStyle || {}
        const parts = []
        if (style.height) parts.push(`height: ${parseInt(style.height, 10) * 2}rpx`)
        if (style.borderRadius !== undefined && style.borderRadius !== '') {
          parts.push(`border-radius: ${parseInt(style.borderRadius, 10) * 2}rpx`)
        }
        return parts.join('; ') + (parts.length ? ';' : '')
      }
    }
  }
</script>

<style lang="scss" scoped>
  .diy-imageSingle {
    margin: 0 20rpx 20rpx 20rpx;
    padding: 20rpx;
    border: 1rpx solid #e6e6e6;
    border-radius: 20rpx;
    box-sizing: border-box;
  }

  .item-image {
    margin-bottom: 16rpx;
    &:last-child {
      margin-bottom: 0;
    }
  }

  .nav-to {
    width: 100%;
    height: 400rpx;
    border-radius: 16rpx;
    overflow: hidden;
    background: #f5f5f5;
  }

  .image {
    width: 100%;
    height: 100%;
  }
</style>
