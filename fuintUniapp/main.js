import Vue from 'vue'
import App from './App'
import store from './store'
import uView from 'uview-ui'
import bootstrap from './core/bootstrap'
import {
  getPlatform,
  navTo,
  showToast,
  showSuccess,
  showError,
  getShareUrlParams
} from './utils/app'
import './core/ican-H5Api'
import {
  getTheme,
  loadTheme,
  buildThemeVars,
  getThemePrimary,
  isLightColor,
  applyThemeVarsToPage
} from './utils/theme'
import { loadAndApplyTabbar } from './utils/tabbar'

// 底部 tab 页面（页面装修自定义 tabBar 需要在 tab 页显示时同步配置与选中态）
const TAB_PAGES = [
  'pages/index/index',
  'pages/category/index',
  'pages/order/index',
  'pages/user/index'
]

Vue.config.productionTip = false

App.mpType = 'app'

// 当前运行的终端
Vue.prototype.$platform = getPlatform()

// 全局主题 mixin:注入 themeVars(CSS 变量),页面显示时刷新主题配置
Vue.mixin({
  data() {
    return {
      themeVars: buildThemeVars(getTheme()),
      // 供模板内原生控件(radio/u-icon/第三方组件等)绑定主题色
      themeColor: getThemePrimary()
    }
  },
  onLoad() {
    // 页面创建后立即把主题色 CSS 变量 setProperty 到根节点,
    // 让 100+ 处 $fuint-theme / var(--theme-primary) 在首次渲染前就拿到主色,
    // 避免走 fallback #113a28(uni-app 编译到小程序时 inline style 里的 --xxx 会被丢弃,
    // selectorQuery 直接 setProperty 绕开编译层)
    applyThemeVarsToPage(this)
  },
  onShow() {
    const route = (getCurrentPages().slice(-1)[0] || {}).route || ''
    // #ifdef MP-WEIXIN
    // 底部 tab 页面显示时同步装修 tabBar 配置与选中态
    if (TAB_PAGES.indexOf(route) >= 0) {
      loadAndApplyTabbar(this)
      const host = this.$scope || this
      const tb = typeof host.getTabBar === 'function' && host.getTabBar()
      tb && tb.syncSelected && tb.syncSelected()
    }
    // #endif
    loadTheme().then(theme => {
      this.themeVars = buildThemeVars(theme)
      // 微信小程序运行时设置顶部导航栏颜色，覆盖 pages.json 中的静态值
      // #ifdef MP-WEIXIN
      const c = (theme && theme.colors) || {}
      const primary = c.primary || getThemePrimary()
      this.themeColor = primary
      try {
        uni.setNavigationBarColor({
          // 背景为浅色(含白色兜底)时使用黑色文字, 否则白色文字
          frontColor: isLightColor(primary) ? '#000000' : '#ffffff',
          backgroundColor: primary,
          animation: { duration: 0, timingFunc: 'linear' }
        })
      } catch (e) {}
      // #endif
      // 主题更新后再次 setProperty 到页面根节点, 保证换色即时生效
      applyThemeVarsToPage(this, theme)
    })
  }
})

// 载入uView库
Vue.use(uView)

// 挂载全局函数
Vue.prototype.$toast = showToast
Vue.prototype.$success = showSuccess
Vue.prototype.$error = showError
Vue.prototype.$navTo = navTo
Vue.prototype.$getShareUrlParams = getShareUrlParams

// 实例化应用
const app = new Vue({
  ...App,
  store,
  created: bootstrap
})
app.$mount()
