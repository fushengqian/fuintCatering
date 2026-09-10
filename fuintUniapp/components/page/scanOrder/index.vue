<template>
  <view class="page-scan-order" :style="cardStyle" @click="onTap">
    <view class="scan-icon" :style="{ color: style.iconColor || '#fff' }">
      <text class="iconfont icon-qr-extract"></text>
    </view>
    <view class="scan-text">
      <view class="scan-title" :style="{ color: style.textColor || '#fff' }">{{ params.title || '扫码点餐' }}</view>
      <view class="scan-subtitle">{{ params.subtitle || '扫描桌码二维码，快速点餐' }}</view>
    </view>
    <view class="scan-arrow" :style="{ color: style.textColor || '#fff' }">
      <text class="iconfont icon-xiangyoujiantou"></text>
    </view>
  </view>
</template>

<script>
  import { scanTableCode } from '@/utils/scanOrder'
  import { getThemePrimary } from '@/utils/theme'

  export default {
    name: 'ScanOrder',
    props: {
      itemStyle: {
        type: Object,
        default () {
          return {}
        }
      },
      params: {
        type: Object,
        default () {
          return {}
        }
      }
    },
    computed: {
      style () {
        return this.itemStyle || {}
      },
      // 渐变直接内联输出（不再依赖 CSS 变量，小程序端内联自定义属性不可靠）；
      // 同时返回 style 字符串而非对象（与 utils/theme.js、coupon 组件做法一致）
      cardStyle () {
        const s = this.style
        const height = this.numStyle(s.height, 0)
        const start = s.startColor || getThemePrimary()
        const end = s.endColor || '#c8a060'
        const parts = [
          `background-color: ${start}`,
          `background-image: linear-gradient(135deg, ${start}, ${end})`,
          `border-radius: ${this.numStyle(s.borderRadius, 12)}px`,
          `padding: ${this.numStyle(s.padding, 16)}px`,
          `margin: ${this.resolveMargin(s)}`
        ]
        if (height > 0) {
          parts.push(`height: ${height}px`, 'box-sizing: border-box')
        }
        return parts.join(';')
      }
    },
    methods: {
      // 数值型样式取值：undefined/null/空串/非数字统一回退默认值，避免生成 "nullpx" 之类无效样式
      numStyle (value, defaultValue) {
        if (value === undefined || value === null || value === '') return defaultValue
        const num = Number(value)
        return isNaN(num) ? defaultValue : num
      },
      // 外边距：优先取四方向设置，兼容旧的单值 margin，默认 10px
      resolveMargin (s) {
        const style = s || {}
        const fallback = this.numStyle(style.margin, 10)
        const pick = (key) => (style[key] === undefined || style[key] === null || style[key] === '') ? fallback : style[key]
        return `${pick('marginTop')}px ${pick('marginRight')}px ${pick('marginBottom')}px ${pick('marginLeft')}px`
      },
      onTap () {
        // 优先使用后台配置的跳转链接
        if (this.params && this.params.url) {
          this.$navTo(this.params.url)
          return
        }
        // 默认触发桌码扫码
        scanTableCode(this)
      }
    }
  }
</script>

<style lang="scss" scoped>
  .page-scan-order {
    display: flex;
    align-items: center;
    color: #fff;
    box-sizing: border-box;
    /* 兜底纯色：实际渐变由内联 style 输出，见 cardStyle */
    background-color: $fuint-theme;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }

  .page-scan-order .scan-icon {
    margin-right: 12px;
  }

  .page-scan-order .scan-arrow {
    margin-left: 12px;
  }

  .scan-icon {
    width: 48px;
    height: 48px;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.15);
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }

  .scan-icon .iconfont {
    font-size: 24px;
  }

  .scan-text {
    flex: 1;
    min-width: 0;
  }

  .scan-title {
    font-size: 16px;
    font-weight: bold;
    margin-bottom: 4px;
  }

  .scan-subtitle {
    font-size: 12px;
    color: rgba(255, 255, 255, 0.8);
  }

  .scan-arrow {
    font-size: 18px;
    flex-shrink: 0;
  }

  .scan-arrow .iconfont {
    font-size: 18px;
  }
</style>