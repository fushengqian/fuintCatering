<template>
	<view class="navigation">
		<view class="nav">
            <view class="item">
                <image class="icon" src="/static/nav/pay.png" @click.stop="goUrl('pages/pay/index')"></image>
                <view class="title">买单支付</view>
            </view>
            <view class="item">
                <image class="icon" src="/static/nav/add.png" @click.stop="goUrl('pages/wallet/recharge/index')"></image>
                <view class="title">充值有礼</view>
            </view>
            <view class="item">
                <image class="icon" src="/static/nav/new.png" @click.stop="goUrl('pages/coupon/list?type=T')"></image>
                <view class="title">新手福利</view>
            </view>
            <view class="item">
                <image class="icon" src="/static/nav/coupon.png" @click.stop="goUrl('pages/coupon/list?type=C')"></image>
                <view class="title">领取卡券</view>
            </view>
		</view>
	</view>
</template>

<script>
export default {
	methods: {
		goUrl(url) {
			this.$navTo(url);
		}
	}
}
</script>

<style lang="scss" scoped>
.nav {
	position: relative;
	margin-top: 30rpx;
	margin-bottom: 30rpx;
	border-radius: 10rpx;
	background-color: #ffffff;
	box-shadow: #666;
	padding: 40rpx 0;
    border: solid 1rpx #ccc;
    margin: 0 10rpx 25rpx 10rpx;
	display: flex;
	align-items: center;
	justify-content: center;

	.item {
		flex: 1;
		display: flex;
		flex-direction: column;
		justify-content: center;
		align-items: center;
		position: relative;

		.icon {
			width: 60rpx;
			height: 60rpx;
			margin: 28rpx;
		}

		.title {
			font-size: 24rpx;
			color: #666;
			font-weight: 600;
		}

		.content {
			font-size: 20rpx;
			color: #666;
			font-weight: 400;
		}
	}
}

</style>
