<template>
  <!-- 商品图片 -->
  <view class="images-swiper">
    <swiper class="swiper-box" :autoplay="autoplay" :duration="duration" :indicator-dots="indicatorDots"
      :interval="interval" :circular="true" @change="setCurrent">
      <swiper-item v-for="(item, index) in images" :key="index" @click="onPreviewImages(index)">
        <image class="slide-image" mode="aspectFill" :src="item"></image>
      </swiper-item>
    </swiper>
    <view class="banner-num">
      <text>{{ currentIndex }}</text>
      <text>/</text>
      <text>{{ images.length }}</text>
    </view>
  </view>
</template>

<script>
  export default {
    props: {
      images: {
        type: Array,
        default: []
      }
    },
    data() {
      return {
        indicatorDots: true, // 是否显示面板指示点
        autoplay: true, // 是否自动切换
        interval: 3000, // 自动切换时间间隔
        duration: 800, // 滑动动画时长
        currentIndex: 1 // 轮播图指针
      }
    },

    methods: {

      // 设置轮播图当前指针 数字
      setCurrent(e) {
        const app = this
        app.currentIndex = e.detail.current + 1
      },

      // 浏览商品图片
      onPreviewImages(index) {
        const app = this
        const imageUrls = []
        app.images.forEach(item => {
          imageUrls.push(item);
        });
        uni.previewImage({
          current: imageUrls[index],
          urls: imageUrls
        })
      }

    }
  }
</script>

<style lang="scss" scoped>
  // swiper组件
  .swiper-box {
    width: 100%;
    height: 100vw;

    .slide-image {
      width: 100%;
      height: 100%;
      margin: 0rpx;
      padding: 0rpx;
      display: block;
      border-radius: 1rpx;
    }
  }

  /* banner计数 */
  .banner-num {
    position: absolute;
    right: 30rpx;
    margin-top: -70rpx;
    padding: 2rpx 18rpx;
    background: rgba(0, 0, 0, 0.363);
    border-radius: 50rpx;
    color: #fff;
    font-size: 26rpx;
  }
</style>
