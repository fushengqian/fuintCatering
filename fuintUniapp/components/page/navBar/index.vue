daohang<template>
  <!-- 导航宫格 -->
  <view class="diy-navBar" :style="navBarStyle">
    <view class="data-list" :class="listClass">
      <view class="item-nav" v-for="(dataItem, index) in renderList" :key="index" :style="itemWidth">
        <view class="nav-to" :class="itemClass" :style="itemBoxStyle" @click="onLink(dataItem.url)">
          <view class="item-image">
            <image class="image" mode="aspectFill" :src="dataItem.iconUrl"></image>
          </view>
          <view class="item-text">
            <view class="text" :style="textStyle">{{ dataItem.name }}</view>
            <view v-if="showTip(dataItem)" class="tip" :style="tipStyle">{{ dataItem.subtitle }}</view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
  import mixin from '../mixin'

  const rpxRatio = 2; // 后台样式数值按 px，小程序按 750rpx 设计稿换算

  export default {
    name: "NavBar",
    props: {
      itemIndex: String,
      itemStyle: Object,
      params: Object,
      dataList: Array
    },

    mixins: [mixin],

    computed: {
      // 注意：小程序端 :style 绑定对象会被序列化成 [object Object] 导致样式失效，统一返回 style 字符串
      navBarStyle() {
        const style = this.itemStyle || {}
        return `background: ${style.background || '#ffffff'}; color: ${style.textColor || '#333333'};`
      },
      layout() {
        return (this.itemStyle && this.itemStyle.layout) || 'grid'
      },
      iconPosition() {
        return (this.itemStyle && this.itemStyle.iconPosition) || (this.layout === 'card' ? 'left' : 'top')
      },
      rowsNum() {
        const num = parseInt(this.itemStyle && this.itemStyle.rowsNum, 10)
        return num > 0 ? num : 4
      },
      lineNum() {
        const num = parseInt(this.itemStyle && this.itemStyle.lineNum, 10)
        return num > 0 ? num : 2
      },
      listClass() {
        return [
          `layout-${this.layout}`,
          `icon-${this.iconPosition}`
        ]
      },
      itemClass() {
        return {
          'nav-card': this.layout === 'card',
          'nav-left': this.iconPosition === 'left'
        }
      },
      itemWidth() {
        return `width: ${(100 / this.rowsNum).toFixed(4)}%;`
      },
      itemBoxStyle() {
        const style = this.itemStyle || {}
        const parts = []
        if (style.itemBg) parts.push(`background: ${style.itemBg}`)
        if (style.itemBorder) parts.push(`border: 1rpx solid ${style.itemBorder}`)
        if (style.itemRadius !== undefined && style.itemRadius !== '') {
          parts.push(`border-radius: ${parseInt(style.itemRadius, 10) * rpxRatio}rpx`)
        }
        // 后台 itemHeight(px) = 数据项高度，覆盖默认 min-height
        if (style.itemHeight !== undefined && style.itemHeight !== '') {
          parts.push(`min-height: ${parseInt(style.itemHeight, 10) * rpxRatio}rpx`)
        }
        return parts.join('; ') + (parts.length ? ';' : '')
      },
      textStyle() {
        const style = this.itemStyle || {}
        const parts = []
        if (style.fontSize) parts.push(`font-size: ${parseInt(style.fontSize, 10) * rpxRatio}rpx`)
        if (style.fontWeight) parts.push(`font-weight: ${style.fontWeight}`)
        return parts.join('; ') + (parts.length ? ';' : '')
      },
      tipStyle() {
        const style = this.itemStyle || {}
        const parts = []
        if (style.subColor) parts.push(`color: ${style.subColor}`)
        return parts.join('; ') + (parts.length ? ';' : '')
      },
      renderList() {
        const max = this.rowsNum * this.lineNum
        return (this.dataList || []).slice(0, max)
      }
    },

    methods: {
      showTip(item) {
        const t = item && item.subtitle
        if (!t) return false
        // 过滤纯数字角标，只显示文字副标题
        const s = String(t).trim()
        return s !== '' && isNaN(Number(s))
      },
      onLink(linkObj) {
        this.$navTo(linkObj)
      }
    }
  }
</script>

<style lang="scss" scoped>
  .diy-navBar {
    margin: 20rpx 20rpx;
    padding: 6rpx;
    background: #ffffff;
    border: 1rpx solid #e6e6e6;
    border-radius: 20rpx;
    box-sizing: border-box;
  }

  .data-list {
    display: flex;
    flex-wrap: wrap;
  }

  .item-nav {
    box-sizing: border-box;
    padding: 10rpx;
  }

  .nav-to {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    min-height: 140rpx;
    box-sizing: border-box;
    padding: 16rpx 8rpx;
    border-radius: 12rpx;
    overflow: hidden;
  }

  .item-image {
    width: 80rpx;
    height: 80rpx;
    margin-bottom: 10rpx;
    .image {
      width: 100%;
      height: 100%;
    }
  }

  .item-text {
    text-align: center;
    overflow: hidden;
    .text {
      font-size: 24rpx;
      line-height: 1.4;
      color: inherit;
      white-space: nowrap;
      text-overflow: ellipsis;
      overflow: hidden;
    }
    .tip {
      font-size: 20rpx;
      line-height: 1.3;
      margin-top: 4rpx;
      opacity: 0.8;
      white-space: nowrap;
      text-overflow: ellipsis;
      overflow: hidden;
    }
  }

  /* 图标在左（或卡片布局） */
  .icon-left .nav-to,
  .layout-card .nav-to {
    flex-direction: row;
    justify-content: flex-start;
    padding: 20rpx;
    min-height: 120rpx;
  }

  .layout-card .nav-to {
    background: linear-gradient(to bottom, #ffffff, #f5f5f5);
    border: 1rpx solid #e5e5e5;
  }

  .icon-left .item-image,
  .layout-card .item-image {
    width: 72rpx;
    height: 72rpx;
    margin-bottom: 0;
    margin-right: 16rpx;
  }

  .icon-left .item-text,
  .layout-card .item-text {
    text-align: left;
    flex: 1;
    min-width: 0;
  }

  /* 分列宽度（不再依赖 avg-sm-N 类） */
  .diy-navBar .data-list::after {
    display: none;
  }
</style>
