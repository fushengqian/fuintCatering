<template>
  <view class="diy-banner" :class="{ 'has-custom-height': hasCustomHeight }" :style="bannerStyle">
    <!-- 图片轮播 -->
    <swiper :autoplay="autoplay" class="swiper-box" :duration="duration" :circular="true" :interval="itemStyle.interval * 1000"
      @change="_bindChange">
      <swiper-item v-for="(dataItem, index) in dataList" :key="index">
        <image lazy-load :lazy-load-margin="0" class="slide-image" :src="dataItem.image" @click="onLink(dataItem.url)"
          @load="_imagesHeight" />
      </swiper-item>
    </swiper>
    <!-- 指示点 -->
    <view class="indicator-dots" :class="itemStyle.btnShape">
      <view class="dots-item" :class="{ active: imgCurrent == index }" :style="{ backgroundColor: itemStyle.btnColor }"
        v-for="(dataItem, index) in dataList" :key="index"></view>
    </view>
  </view>
</template>

<script>
  import mixin from '../mixin'

  export default {
    name: "Banner",

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
     * 私有数据,组件的初始数据
     * 可用于模版渲染
     */
    data() {
      return {
        width: 730,
        indicatorDots: false, // 是否显示面板指示点
        autoplay: true, // 是否自动切换
        duration: 800, // 滑动动画时长
        imgHeights: [], // 图片的高度
        imgCurrent: 0, // 当前banne所在滑块指针
      }
    },

    computed: {
      /**
       * 后台装修是否配置了固定高度
       * 后台 style.height 单位为 px（与 blank/video 组件一致）
       */
      hasCustomHeight() {
        const h = this.itemStyle && this.itemStyle.height
        return h !== undefined && h !== '' && Number(h) > 0
      },

      /**
       * 轮播容器高度（返回 style 字符串，避免小程序端 :style 绑对象变 [object Object]）
       * - 后台配置了高度：按配置值固定渲染
       * - 未配置：回退为按图片真实宽高比自适应（原有逻辑）
       */
      bannerStyle() {
        if (this.hasCustomHeight) {
          return `height: ${parseInt(this.itemStyle.height, 10)}px;`
        }
        return `height: ${this.imgHeights[this.imgCurrent] || 0}rpx;`
      }
    },

    /**
     * 组件的方法列表
     * 更新属性和数据的方法与页面数据的方法类似
     */
    methods: {
        onLink(linkObj) {
            this.$navTo(linkObj)
        },
      /**
       * 计算图片高度
       */
      _imagesHeight(e) {
        // 获取图片真实宽度
        const imgwidth = e.detail.width,
          imgheight = e.detail.height,
          // 宽高比
          ratio = imgwidth / imgheight;
        // 计算的高度值
        const viewHeight = this.width / ratio;
        const imgHeights = this.imgHeights;
        // 把每一张图片的高度记录到数组里
        imgHeights.push(viewHeight);
        this.imgHeights = imgHeights;
      },

      /**
       * 记录当前指针
       */
      _bindChange(e) {
        this.imgCurrent = e.detail.current
      }

    }

  }
</script>

<style lang="scss" scoped>
  .diy-banner {
    position: relative;
    max-height: 400rpx;
    // 后台配置了固定高度时，解除默认高度上限，避免设置的高度被裁剪
    &.has-custom-height {
      max-height: none;
    }
    // swiper组件
    .swiper-box {
      height: 100%;
      padding: 0;
      max-width: 750rpx;
      max-height: 450rpx;
      .slide-image {
        width: 100%;
        height: 100%;
        margin: 0 auto;
        display: block;
      }
    }
    // 配置固定高度时同步解除 swiper 的高度上限
    &.has-custom-height .swiper-box {
      max-height: none;
    }
    
    /* 指示点 */
    .indicator-dots {
      width: 100%;
      height: 28rpx;
      padding: 0;
      position: absolute;
      left: 0;
      right: 0;
      bottom: 20rpx;
      opacity: 0.8;
      display: flex;
      justify-content: center;

      .dots-item {
        width: 16rpx;
        height: 16rpx;
        margin-right: 8rpx;
        background-color: #fff;

        &:last-child {
          margin-right: 0;
        }

        &.active {
          background-color: var(--theme-primary) !important;
        }
      }

      // 圆形（round 为标准类名，circle 兼容后台历史数据）
      &.round .dots-item,
      &.circle .dots-item {
        width: 16rpx;
        height: 16rpx;
        border-radius: 20rpx;
      }

      // 正方形
      &.square .dots-item {
        width: 16rpx;
        height: 16rpx;
      }

      // 长方形
      &.rectangle .dots-item {
        width: 22rpx;
        height: 14rpx;
      }

    }

  }
</style>
