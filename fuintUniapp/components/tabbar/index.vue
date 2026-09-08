<template>
  <view v-if="visible" class="h5-tabbar" :style="barStyle">
    <view
      v-for="(item, idx) in items"
      :key="idx"
      class="h5-tabbar__item"
      :class="{ 'h5-tabbar__item--active': idx === selected }"
      @click="onTap(item, idx)"
    >
      <image
        v-if="item.icon"
        class="h5-tabbar__icon"
        mode="aspectFit"
        :src="idx === selected && item.selectedIcon ? item.selectedIcon : item.icon"
      />
      <text class="h5-tabbar__text" :style="{ color: idx === selected ? selectedColor : textColor }">{{ item.name }}</text>
    </view>
  </view>
</template>

<script>
  import { loadTabbar } from '@/utils/tabbar'
  import { getThemePrimary } from '@/utils/theme'

  // 修正页面路径：去除前导斜杠与 query/hash、兼容完整链接、修正 page/ 前缀为 pages/
  function normalizePagePath(path) {
    if (!path) return ''
    let p = String(path).trim()
    // 剥离 query 与 hash，避免带参数的链接导致路径匹配失败
    p = p.split('?')[0].split('#')[0]
    // 兼容粘贴的完整链接（如 https://xxx/pages/order/index 或 https://xxx/#/pages/order/index），提取页面路径
    const match = p.match(/(?:^|\/)(pages\/[\w./-]+)(?:\/?)$/)
    if (match) {
      p = match[1]
    }
    p = p.replace(/^\/+/, '')
    p = p.replace(/^page\//, 'pages/')
    // 去除尾部多余的斜杠
    p = p.replace(/\/+$/, '')
    return p
  }

  export default {
    name: 'H5Tabbar',
    data() {
      return {
        visible: false,
        items: [],
        selected: 0,
        bgColor: '#ffffff',
        textColor: '#999999',
        selectedColor: getThemePrimary(),
        barHeight: 50
      }
    },
    computed: {
      barStyle() {
        return {
          height: this.barHeight + 'px',
          backgroundColor: this.bgColor
        }
      }
    },
    created() {
      this.load()
    },
    methods: {
      async load() {
        let config = null
        try {
          config = await loadTabbar()
        } catch (e) {
          config = null
        }
        this.apply(config)
      },
      apply(config) {
        // 后端无配置/无有效导航项时不渲染（不填充任何兜底数据）
        const hasItems = !!(config && config.items && config.items.length)
        if (!hasItems || config.enabled === false) {
          this.items = []
          this.visible = false
          return
        }
        const style = config.style || {}
        this.items = config.items.map(item => ({
          name: item.name || '',
          path: normalizePagePath(item.url),
          icon: item.iconUrl || '',
          selectedIcon: item.iconSelectedUrl || item.iconUrl || ''
        })).filter(item => item.path && item.name)
        if (!this.items.length) {
          this.visible = false
          return
        }
        this.visible = true
        this.bgColor = style.bgColor || '#ffffff'
        this.textColor = style.textColor || '#999999'
        this.selectedColor = style.selectedColor || getThemePrimary()
        this.barHeight = Math.max(40, Math.min(Number(style.height) || 50, 80))
        this.setSelected()
      },
      // 页面 onShow 时调用：tab 切换后刷新选中态；首次未渲染时补拉取
      refresh() {
        if (!this.visible && !this.items.length) {
          this.load()
        } else {
          this.syncSelected()
        }
      },
      // 同步选中态并延迟重试：页面切换时路由可能尚未就绪，延迟保证最终校正到位
      syncSelected() {
        let times = 0
        const doSync = () => {
          this.setSelected()
          if (times < 3) {
            times++
            setTimeout(doSync, 300)
          }
        }
        doSync()
      },
      setSelected() {
        const pages = getCurrentPages()
        const current = pages[pages.length - 1]
        if (!current) return
        // 去掉前导斜杠和 query string（如 pages/category/index?storeId=1）
        const route = String(current.route || '').replace(/^\/+/, '').split('?')[0]
        const index = this.items.findIndex(item => item.path === route)
        if (index >= 0 && index !== this.selected) {
          this.selected = index
        } else if (index < 0 && this.visible && this.selected >= 0) {
          // 当前页面不在底部导航列表中时取消高亮，避免停留在上一个页面的选中态造成“选中与页面不匹配”
          this.selected = -1
        }
      },
      onTap(item, idx) {
        if (!item.path || idx === this.selected) return
        // 乐观更新选中态，保证点击后立即有反馈
        this.selected = idx
        uni.switchTab({
          url: '/' + item.path,
          fail: () => {
            this.setSelected()
          }
        })
      }
    }
  }
</script>

<style lang="scss" scoped>
  .h5-tabbar {
    position: fixed;
    left: 0;
    right: 0;
    bottom: 0;
    z-index: 999;
    display: flex;
    box-sizing: content-box;
    border-top: 1px solid #e5e5e5;
    padding-bottom: constant(safe-area-inset-bottom);
    padding-bottom: env(safe-area-inset-bottom);

    &__item {
      flex: 1;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 6px 0 0;
      cursor: pointer;
    }

    &__icon {
      width: 40rpx;
      height: 40rpx;
      margin-bottom: 2rpx;
    }

    &__text {
      font-size: 20rpx;
      line-height: 1.6;
      white-space: nowrap;
    }
  }
</style>
