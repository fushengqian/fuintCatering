<template>
	<view class="banner">
		<swiper class="bg" circular :indicator-dots="false" :autoplay="true" :interval="5000" :duration="200" @change="_bindChange">
			<swiper-item v-for="(item, index) in banners" :key="index">
				<view class="swiper">
					<image :src="item.image" @click.stop="goUrl(item.url)"></image>
				</view>
			</swiper-item>
        </swiper>
        <!-- 指示点 -->
        <view class="indicator-dots round">
          <view class="dots-item" :class="{ active: current == index }" v-for="(dataItem, index) in banners" :key="index"></view>
        </view>
	</view>
</template>

<script>
	export default {
		props: {
			banners: {
				type: Array,
				default: []
			}
		},
        data() {
          return {
            current: 1 // 轮播图指针
          }
        },
		methods: {
			goUrl(url) {
			  this.$navTo(url);
			},
            
            /**
             * 记录当前指针
             */
            _bindChange(e) {
              this.current = e.detail.current;
            }
		},
	}
</script>

<style lang="scss" scoped>
.banner {
	position: relative;
	width: 100%;
	height: 600rpx;
	.bg {
		width: 100%;
		height: 600rpx;
	}
}
.banner,
.bg,
.swiper {
	width: 100%;
	height: 600rpx;
	image {
		width: 100%;
		height: 100%;
	}
}
/* 指示点 */
.indicator-dots {
  width: 100%;
  height: 28rpx;
  padding: 10rpx;
  position: absolute;
  left: 0;
  right: 0;
  bottom: 70rpx;
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
      background-color: #000 !important;
    }
  }

  // 圆形
  &.round .dots-item {
    width: 18rpx;
    height: 18rpx;
    border-radius: 30rpx;
  }
}
</style>
