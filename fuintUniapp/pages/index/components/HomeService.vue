<template>
	<view class="content">
		<view class="entrance">
            <view class="item">
                <image class="icon" src="/static/nav/store.png" @click="goUrl('oneself')"></image>
                <view class="title">门店自取</view>
            </view>
            <view class="item">
                <image class="icon" src="/static/nav/send.png" @click="goUrl('express')"></image>
                <view class="title">外卖到家</view>
            </view>
		</view>
	</view>
</template>

<script>
export default {
	props: {
		data: {
			type: Array,
			default: []
		}
	},
	methods: {
		goUrl(orderMode) {
			this.$navTo('pages/category/index', { orderMode });
		}
	}
}
</script>

<style lang="scss" scoped>
.entrance {
	position: relative;
	margin-top: -20rpx;
	margin-bottom: 30rpx;
	border-radius: 10rpx;
	background-color: #ffffff;
	box-shadow: #666;
	padding: 40rpx 0;
	display: flex;
	align-items: center;
	justify-content: center;
    border: solid 1rpx #ccc;
    margin: 10rpx 10rpx 25rpx 10rpx;
	.item {
		flex: 1;
		display: flex;
		flex-direction: column;
		justify-content: center;
		align-items: center;
		position: relative;

		&:nth-child(1):after {
			content: '';
			position: absolute;
			width: 1rpx;
			background-color: #ccc;
			right: 0;
			height: 100%;
			transform: scaleX(0.5) scaleY(0.8);
		}

		.icon {
			width: 120rpx;
			height: 120rpx;
			margin: 28rpx;
		}

		.title {
			font-size: 36rpx;
			color: #666;
			font-weight: 600;
		}

		.content {
			font-size: 28rpx;
			color: #666;
			font-weight: 400;
		}
	}
}

</style>
