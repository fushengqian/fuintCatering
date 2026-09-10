<template>
    <view class="content">
        <view class="entrance" :style="entranceStyle">
            <view class="item">
                <image class="icon" :style="iconStyle" :src="oneselfIcon" @click="goUrl('oneself')"></image>
                <view class="title">{{ oneselfName }}</view>
            </view>
            <view class="item">
                <image class="icon" :style="iconStyle" :src="expressIcon" @click="goUrl('express')"></image>
                <view class="title">{{ expressName }}</view>
            </view>
        </view>
    </view>
</template>

<script>
// 后台样式数值单位为 px，小程序端按 750rpx 设计稿换算成 rpx（与 components/page/navBar 一致）
const rpxRatio = 2;
// 两入口的内置默认图标与名称：装修未配置或未装修时保持原有表现
const DEFAULT_ONESELF_ICON = '/static/nav/store.png';
const DEFAULT_ONESELF_NAME = '堂食自提';
const DEFAULT_EXPRESS_ICON = '/static/nav/send.png';
const DEFAULT_EXPRESS_NAME = '配送到家';

export default {
    name: 'HomeService',
    // 装修协议 props：itemStyle 控制容器样式（背景/边框/圆角/边距）与图标大小，
    // params 控制两入口的图标与名称；未装修时两者为空，全部回退到组件内置样式与默认值
    inheritAttrs: false,
    props: {
        itemStyle: {
            type: Object,
            default () {
                return {}
            }
        },
        // 装修参数：后台可配置 oneselfIcon/oneselfName/expressIcon/expressName
        params: {
            type: Object,
            default () {
                return {}
            }
        },
        data: {
            type: Array,
            default: []
        }
    },
    computed: {
        // 容器样式：装修配置优先。未装修（itemStyle 为空对象）时返回空字符串，
        // 完全沿用 scss 内置样式，保证默认首页视觉与改造前一致。
        // 注意：小程序端 :style 绑定对象会被序列化成 [object Object] 导致样式失效，
        // 因此统一返回 style 字符串（与 components/page/navBar 等组件保持一致）
        entranceStyle() {
            const s = this.itemStyle || {};
            if (!Object.keys(s).length) {
                return '';
            }
            const parts = [];
            // 背景色
            if (s.background) {
                parts.push(`background: ${s.background}`);
            }
            // 边框：选"无边框"或宽度为 0 时不渲染
            const borderStyle = s.borderStyle || 'solid';
            const borderWidth = this.numStyle(s.borderWidth, 1);
            if (borderStyle === 'none' || borderWidth <= 0) {
                parts.push('border: none');
            } else {
                parts.push(`border: ${borderWidth}px ${borderStyle} ${s.borderColor || '#cccccc'}`);
            }
            // 圆角：未配置时不设置，沿用 scss 内置值
            const radius = this.numStyle(s.borderRadius, null);
            if (radius !== null) {
                parts.push(`border-radius: ${radius}px`);
            }
            // 外边距：未配置的方向回退 10px，与后台预览 resolveMargin 的默认值保持一致
            const marginBase = this.numStyle(s.margin, 10);
            const pickMargin = (key) => this.numStyle(s[key], marginBase);
            parts.push(`margin: ${pickMargin('marginTop')}px ${pickMargin('marginRight')}px ${pickMargin('marginBottom')}px ${pickMargin('marginLeft')}px`);
            return parts.join('; ') + ';';
        },
        // 图标尺寸：装修配置优先；未配置（或值非法/非正）时返回空字符串，沿用 scss 内置的 120rpx。
        // 后台配置单位是 px，这里按 750rpx 设计稿换算为 rpx（60px -> 120rpx），
        // 必须返回 style 字符串，避免小程序端对象绑定被序列化成 [object Object] 而失效
        iconStyle() {
            const s = this.itemStyle || {};
            const size = this.numStyle(s.iconSize, null);
            if (size === null || size <= 0) {
                return '';
            }
            const rpxSize = size * rpxRatio;
            return `width: ${rpxSize}rpx; height: ${rpxSize}rpx;`;
        },
        // 堂食入口图标：装修配置优先，未配置回退内置默认值
        oneselfIcon() {
            return this.params.oneselfIcon || DEFAULT_ONESELF_ICON;
        },
        oneselfName() {
            return this.params.oneselfName || DEFAULT_ONESELF_NAME;
        },
        expressIcon() {
            return this.params.expressIcon || DEFAULT_EXPRESS_ICON;
        },
        expressName() {
            return this.params.expressName || DEFAULT_EXPRESS_NAME;
        }
    },
    methods: {
        // 数值型样式安全取值：空值/非数字一律回退默认值，避免生成 "NaNpx" 之类无效样式
        numStyle(value, defaultValue) {
            if (value === undefined || value === null || value === '') return defaultValue;
            const num = Number(value);
            return isNaN(num) ? defaultValue : num;
        },
        goUrl(orderMode) {
            uni.setStorageSync('orderMode', orderMode);
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
            color: #333;
            font-weight: 600;
        }

        .content {
            font-size: 28rpx;
            color: #333;
            font-weight: 400;
        }
    }
}

</style>
