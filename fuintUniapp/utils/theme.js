import * as themeApi from '@/api/theme'

// 无主题缓存/后台主题不可用时的兜底色：
// 不使用品牌青，避免启动瞬间或主题拉取前闪出与后台主题不一致的青色；
// 用白色作为中性兜底，主题接口返回后即被覆盖
const DEFAULT_PRIMARY = '#ffffff'
const DEFAULT_THEME = {
  themeId: '',
  themeName: '默认主题',
  colors: {
    primary: DEFAULT_PRIMARY,
    secondary: '#e0f4f4',
    text: '#333333',
    bg: '#f5f5f5',
    price: '#f03c3c'
  }
}

// 主题缓存有效期:1 小时
// (App 启动时已通过 loadTheme(true) 强制拉取最新主题并写入缓存,
// 因此页面 onShow 期间只需在缓存超时后兜底刷新, 避免每个页面反复请求导致换色闪烁)
const CACHE_DURATION = 60 * 60 * 1000

let loadingPromise = null

/**
 * 读取缓存的主题配置
 */
export function getTheme() {
  const theme = uni.getStorageSync('theme')
  return theme && theme.colors ? theme : DEFAULT_THEME
}

/**
 * 缓存主题配置
 */
export function setTheme(theme) {
  uni.setStorageSync('theme', theme)
  uni.setStorageSync('theme_time', Date.now())
}

/**
 * 生成页面 CSS 变量样式字符串,用于页面根节点 :style 绑定
 *
 * 注意必须返回字符串而非对象：uni-app 编译到微信小程序时,
 * :style="obj" 会被序列化为 style="{{(obj)}}",对象会变成 [object Object],
 * CSS 变量在 page 内彻底失效。字符串形式在 H5 与小程序端都会被作为 inline style 正确解析。
 */
export function buildThemeVars(theme) {
  const t = theme || getTheme()
  const colors = t.colors || {}
  const c = Object.assign({}, DEFAULT_THEME.colors, colors)
  const parts = []
  parts.push(`--theme-primary: ${c.primary}`)
  parts.push(`--theme-secondary: ${c.secondary}`)
  parts.push(`--theme-text: ${c.text}`)
  parts.push(`--theme-bg: ${c.bg}`)
  parts.push(`--theme-price: ${c.price}`)
  // 同时同步 SCSS 编译后对应的 CSS 变量，让 $fuint-theme 的 100+ 处引用也跟随主题
  parts.push(`--fuint-theme: ${c.primary}`)
  return parts.join('; ')
}

/**
 * 把主题色 CSS 变量直接 setProperty 到页面根节点 (.container) 与 H5 的 documentElement。
 *
 * 背景：:style="themeVars" 把 "--theme-primary: #xxx" 写入 inline style 在 H5 端正常，
 * 但编译到微信小程序时，inline style 里的 CSS 自定义属性会被丢弃，导致
 * var(--theme-primary) 走 fallback (#113a28)，页面元素不跟随主题。
 * 通过 selectorQuery 拿到页面根 wx 节点并直接 style.setProperty 写入，
 * 绕开 uni-app 编译层过滤，让 100+ 处 $fuint-theme / var(--theme-primary) 真正拿到主色。
 *
 * - H5：applyH5Theme 已把变量注入 documentElement，所有页面自动继承
 * - 微信小程序：找到 .container 节点逐个 setProperty
 */
export function applyThemeVarsToPage(pageRef, theme) {
  const t = theme || getTheme()
  const c = Object.assign({}, DEFAULT_THEME.colors, (t && t.colors) || {})
  // #ifdef H5
  applyH5Theme(t)
  // #endif
  // #ifdef MP-WEIXIN
  try {
    const query = uni.createSelectorQuery().in(pageRef)
    // 兼容 .container (含 .dining-container / .container p-bottom 等复合 class);
    // 少数根是 div / mescroll-uni / mescroll-body / .content 等的页面已统一加 class="container" 纯标记
    // (项目无 .container 全局样式, 加类无副作用)
    query.selectAll('.container').fields({ node: true, size: false })
    query.exec(res => {
      const list = res && res[0]
      if (Array.isArray(list)) {
        list.forEach(node => {
          if (node && node.style && typeof node.style.setProperty === 'function') {
            node.style.setProperty('--theme-primary', c.primary)
            node.style.setProperty('--theme-secondary', c.secondary)
            node.style.setProperty('--theme-text', c.text)
            node.style.setProperty('--theme-bg', c.bg)
            node.style.setProperty('--theme-price', c.price)
            // 同步给 SCSS 编译出的 $fuint-theme (uni.scss: var(--theme-primary, #113a28))
            // 提供 --fuint-theme 直接命中, 即使 --theme-primary 被某些作用域隔离
            node.style.setProperty('--fuint-theme', c.primary)
          }
        })
      }
    })
  } catch (e) {}
  // #endif
}

/**
 * 读取当前主题的 primary 色（用于组件如 tabbar 选中色等无 CSS 变量场景的兜底）
 */
export function getThemePrimary() {
  const t = getTheme()
  return (t && t.colors && t.colors.primary) || DEFAULT_PRIMARY
}

/**
 * 判断颜色是否为浅色(用于导航栏前景文字黑/白选择)
 * @param {string} color 如 '#ffffff'
 */
export function isLightColor(color) {
  const hex = String(color || '').trim().replace('#', '')
  if (!/^[0-9a-fA-F]{6}$/.test(hex)) return false
  const r = parseInt(hex.substr(0, 2), 16)
  const g = parseInt(hex.substr(2, 2), 16)
  const b = parseInt(hex.substr(4, 2), 16)
  // 感知亮度(0~255), 大于 160 视为浅色, 前景用深色文字
  return 0.299 * r + 0.587 * g + 0.114 * b > 160
}

/**
 * H5 环境下注入全局 CSS 变量(作用于 document.documentElement)
 */
function applyH5Theme(theme) {
  // #ifdef H5
  const t = theme || getTheme()
  const c = Object.assign({}, DEFAULT_THEME.colors, (t && t.colors) || {})
  const style = document.documentElement.style
  style.setProperty('--theme-primary', c.primary)
  style.setProperty('--theme-secondary', c.secondary)
  style.setProperty('--theme-text', c.text)
  style.setProperty('--theme-bg', c.bg)
  style.setProperty('--theme-price', c.price)
  style.setProperty('--fuint-theme', c.primary)
  // #endif
}

/**
 * 加载主题配置(带缓存,force 为 true 时强制刷新)
 */
export function loadTheme(force) {
  if (!force) {
    const time = uni.getStorageSync('theme_time')
    if (time && Date.now() - time < CACHE_DURATION) {
      const cached = getTheme()
      applyH5Theme(cached)
      return Promise.resolve(cached)
    }
  }
  // 防止并发重复请求
  if (!loadingPromise) {
    loadingPromise = themeApi.theme()
      .then(res => {
        const theme = res.data || {}
        if (!theme.colors) {
          theme.colors = DEFAULT_THEME.colors
        }
        setTheme(theme)
        applyH5Theme(theme)
        return theme
      })
      .catch(() => {
        const theme = getTheme()
        applyH5Theme(theme)
        return theme
      })
      .finally(() => {
        loadingPromise = null
      })
  }
  return loadingPromise
}
