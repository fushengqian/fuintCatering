<template>
    <view class="navigation">
        <view class="nav">
            <view class="item" v-for="item in navigation">
                <image class="icon" :src="item.iconUrl" @click.stop="goUrl(item.url)"></image>
                <view class="title">{{ item.name }}</view>
            </view>
        </view>
    </view>
</template>

<script>
export default {
    props: {
        navigation: {
            type: Array,
            default: []
        }
    },
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
            width: 50rpx;
            height: 50rpx;
            margin: 28rpx;
            padding: 20rpx;
            border-radius: 50rpx;
            border: #ccc solid 2rpx;
            background: #f5f5f5;
        }

        .title {
            font-size: 28rpx;
            color: #333;
            font-weight: 700;
        }

        .content {
            font-size: 20rpx;
            color: #666;
            font-weight: 400;
        }
    }
}

</style>
