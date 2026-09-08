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
