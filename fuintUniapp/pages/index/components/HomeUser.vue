<template>
    <view class="com-user">
        <view class="user-main">
            <image class="avatar" :src="userInfo && userInfo.avatar ? userInfo.avatar : '/static/default-avatar.png'"></image>
            <view class="uc">
                <view class="name">Hi，你好！</view>
                <view class="tip" v-if="!userInfo || !userInfo.id">为了向您提供更好的服务，请登录！</view>
                <view class="tip" v-if="userInfo && userInfo.id">
                    <view>余额：<text>{{ userInfo.balance }}</text></view>
                    <view class="point-row">
                        <text>积分：<text>{{ userInfo.point }}</text></text>
                        <view class="rank-entry" @click="goRank">
                            <text class="iconfont icon-huo"></text>排行
                        </view>
                    </view>
                </view>
            </view>
            <view class="ur" v-if="!userInfo || !userInfo.id" @click="goLogin">登录</view>
            <view class="qr iconfont icon-qr-extract" v-if="userInfo && userInfo.id" @click="goMemberCode"></view>
        </view>
    </view>
</template>

<script>
    export default {
        props: {
            userInfo: {
                type: Object,
                default: { id: '', avatar: '', name: '', balance: '', point: '' }
            }
        },
        methods: {
            // 去登录
            goLogin() {
                this.$navTo('pages/login/index')
            },
            // 跳转会员码
            goMemberCode(userId) {
                this.$navTo('pages/user/code', { userId: this.userInfo.id })
            },
            // 跳转积分排行榜
            goRank() {
                this.$navTo('pages/points/rank')
            },
        }
    }
</script>

<style lang="scss" scoped>
.com-user {
    width: 100%;
    height: auto;
    padding: 0 20rpx 20rpx;
    margin-top: -60rpx;
    position: relative;
    z-index: 2;
    .user-main{
        width: 100%;
        padding: 20rpx;
        background: #f5f5f5;
        border-radius: 20rpx;
        border: #cccccc solid 1rpx;
        display: flex;
        align-items: center;
        .avatar{
            width: 130rpx;
            height: 130rpx;
            border-radius: 50%;
        }
        .uc{
            flex: 1;
            padding-left: 10rpx;
            .name{
                font-size: 32rpx;
                font-weight: 500;
                color: #000;
                
            }
            .tip{
                font-size: 24rpx;
                color: #666;
                margin-top: 10rpx;
                .point-row{
                    display: flex;
                    align-items: center;
                    .rank-entry{
                        margin-left: 16rpx;
                        padding: 4rpx 16rpx;
                        font-size: 20rpx;
                        color: #fff;
                        background-color: $fuint-theme;
                        border-radius: 20rpx;
                        display: flex;
                        align-items: center;
                        .iconfont{
                            font-size: 22rpx;
                            margin-right: 4rpx;
                        }
                    }
                }
            }
        }
        .ur{
            width: 140rpx;
            height: 60rpx;
            display: flex;
            align-items: center;
            border-radius: 60rpx;
            justify-content: center;
            color: #fff;
            font-size: 26rpx;
            background-color: $fuint-theme;
        }
        .qr{
            width: 40rpx;
            height: 40rpx;
            display: flex;
            align-items: center;
            border-radius: 6rpx;
            justify-content: center;
            text-align: center;
            color: $fuint-theme;
            font-size: 48rpx;
            font-weight: bold;
            background-color: #fff;
            margin-top: 15rpx;
            margin-right: 6rpx;
            padding: 2rpx;
        }
    }
}
</style>
