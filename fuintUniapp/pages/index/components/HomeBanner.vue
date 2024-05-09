<template>
	<view class="banner">
		<swiper class="bg" circular :indicator-dots="false" :autoplay="true" :interval="5000" :duration="200">
			<swiper-item v-for="(item, index) in banners" :key="index">
				<view class="swiper">
					<image :src="item.image" @click.stop="goUrl(item.url)"></image>
				</view>
			</swiper-item>
		</swiper>
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
		methods: {
			goUrl(url) {
			    this.$navTo(url);
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
</style>
