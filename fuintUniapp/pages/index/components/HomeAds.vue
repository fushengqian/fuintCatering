<template>
    <view class="ads">
        <swiper class="bg" circular :indicator-dots="false" :autoplay="true" :interval="5000" :duration="200">
            <swiper-item v-for="(item, index) in ads" :key="index">
                <view class="swiper">
                    <image class="image" :src="item.image" @click.stop="goUrl(item.url)"></image>
                </view>
            </swiper-item>
        </swiper>
    </view>
</template>

<script>
    export default {
        props: {
            ads: {
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
.ads {
    width: 100%;
    height: 280rpx;
    padding: 0 10rpx;
    display: block;
    background: #fff;
    .bg {
        width: 100%;
        height: 240rpx;
    }
}
.bg,
.swiper {
    width: 100%;
    height: 240rpx;
    margin-bottom: 550rpx;
    image {
        width: 100%;
        height: 100%;
        border-radius: 6rpx;
    }
}

</style>
