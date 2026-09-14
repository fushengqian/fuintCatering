import * as TabbarApi from '@/api/tabbar'
import config from '@/config'
import { getThemePrimary } from '@/utils/theme'

// 统一图片地址为完整 URL
// baseUrl 优先使用后端接口返回的 imagePath（图片上传根路径，可能与接口域名不一致，如 OSS/独立文件服务器），无则回退 apiUrl
function normalizeImageUrl(url, baseUrl) {
  if (!url) return ''
  if (/^https?:\/\//.test(url)) return url
  const base = baseUrl || config.apiUrl || ''
  // 去掉开头的斜杠，避免 base 以 / 结尾时产生双斜杠
  const path = url.startsWith('/') ? url.substring(1) : url
  if (!base) return '/' + path
  return base.endsWith('/') ? base + path : base + '/' + path
}

// 统一页面路径：去除前导斜杠、修正 page/ 前缀为 pages/、兜底修复历史损坏路径
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
  // 将 page/xxx 修正为 pages/xxx
  p = p.replace(/^page\//, 'pages/')
  // 兜底：pages 后直接跟非斜杠字符（如历史错误的 pagescategory/index）时补上斜杠
  p = p.replace(/^pages(?=[^\/])/, 'pages/')
  // 去除尾部多余的斜杠
  p = p.replace(/\/+$/, '')
  return p
}

// 归一化后台返回的 tabBar 配置；无配置/无导航项时返回空结构（不填充任何兜底数据）
export function normalizeConfig(config, imagePath) {
  if (!config || typeof config !== 'object') {
    return null
  }

  const style = config.style || {}
  const items = (config.items || []).map(item => {
    return {
      name: item.name || '',
      url: normalizePagePath(item.url),
      iconUrl: normalizeImageUrl(item.iconUrl, imagePath),
      iconSelectedUrl: normalizeImageUrl(item.iconSelectedUrl || item.iconUrl, imagePath)
    }
  }).filter(item => item.url && item.name)

  // 只要后台返回了导航项即视为启用；无导航项时保持 enabled 原值（显式关闭标志）
  const enabled = items.length > 0 ? true : (config.enabled !== false)

  return {
    enabled,
    type: config.type || 'iconText',
    style: {
      bgColor: style.bgColor || '#ffffff',
      textColor: style.textColor || '#999999',
      selectedColor: style.selectedColor || getThemePrimary(),
      height: Number(style.height) || 50
    },
    items
  }
}

// 缓存有效期（毫秒），防止后台修改配置后客户端长期读取旧缓存
const CACHE_TTL = 5 * 60 * 1000

// 缓存按"商户+门店"分片:不同商户/门店可配置不同的底部导航,
// 共用一个缓存会导致切换门店后沿用上一家的导航
function tabbarScope() {
  const merchantNo = uni.getStorageSync('merchantNo') || ''
  const storeId = uni.getStorageSync('storeId') || 0
  return merchantNo + '_' + storeId
}

function tabbarCacheKey() {
  return 'tabbar_' + tabbarScope()
}

// 读取缓存;兼容升级前直接存归一化结果的全局 key。ignoreTTL 用于请求失败时兜底
function readTabbarCache(ignoreTTL) {
  const cached = uni.getStorageSync(tabbarCacheKey()) || uni.getStorageSync('tabbar')
  if (!cached) return null
  // 新结构为 { raw, imagePath, _ts }，旧结构直接是归一化后的配置
  const raw = cached.raw || cached
  if (!(raw && raw.items && raw.items.length)) return null
  if (!ignoreTTL && Date.now() - (cached._ts || 0) >= CACHE_TTL) return null
  return { raw, imagePath: cached.imagePath || '' }
}

// 加载 tabBar 配置并应用到当前页面（自定义 tabBar 实例可能尚未就绪，自动重试）
// force=true 时忽略本地缓存直接拉取，用于切换门店后刷新门店相关的 tabBar 配置
export function loadAndApplyTabbar(page, force = false) {
  // #ifndef MP-WEIXIN
  // H5 等平台没有微信自定义 tabBar（getTabBar）机制，由页面内自定义组件渲染
  return loadTabbar(force).then(() => {})
  // #endif
  // #ifdef MP-WEIXIN
  console.log('[tabbar] loadAndApplyTabbar start')
  return loadTabbar(force).then(config => {
    console.log('[tabbar] loadAndApplyTabbar config:', config)
    const tryApply = (times) => {
      // 微信注入的 getTabBar 挂在原生页面实例上，uni-app 需经 $scope 访问
      const host = page.$scope || page
      if (typeof host.getTabBar === 'function') {
        const tabBar = host.getTabBar()
        console.log('[tabbar] getTabBar try #' + times, tabBar)
        if (tabBar && typeof tabBar.applyConfig === 'function') {
          // 有装修配置直接应用;确认后台无配置(或请求失败)时回退默认 tab,
          // 否则未装修商家的底部导航会一直空着
          if (config && config.items && config.items.length) {
            console.log('[tabbar] calling applyConfig')
            tabBar.applyConfig(config)
          } else {
            console.log('[tabbar] no config, fallback default')
            tabBar.applyDefault()
          }
          return
        }
      }
      // onShow 时自定义 tabBar 实例可能尚未创建，最多重试 2 秒
      if (times < 20) {
        setTimeout(() => tryApply(times + 1), 100)
      } else {
        console.warn('[tabbar] getTabBar not ready after 20 retries')
      }
    }
    tryApply(0)
  })
  // #endif
}

// 加载 tabBar 配置并缓存
export function loadTabbar(force = false) {
  return new Promise((resolve) => {
    if (!force) {
      const cache = readTabbarCache()
      console.log('[tabbar] loadTabbar cache check:', cache)
      if (cache) {
        resolve(normalizeConfig(cache.raw, cache.imagePath))
        return
      }
    }

    console.log('[tabbar] loadTabbar fetching from API...')
    TabbarApi.getTabbar()
      .then(res => {
        const data = res.data || {}
        const tabbar = data.tabbar || data
        // 后端返回的图片上传根路径（如 OSS 域名/独立文件服务器），与接口域名可能不一致
        const imagePath = data.imagePath || ''
        console.log('[tabbar] API response tabbar:', tabbar, 'imagePath:', imagePath)
        // 仅当后台确实返回了导航项时才写入缓存；
        // 否则不缓存默认配置，保证下次能重新请求到最新配置
        const hasItems = !!(tabbar && tabbar.items && tabbar.items.length)
        const config = normalizeConfig(tabbar, imagePath)
        if (hasItems) {
          // 分片缓存存原始配置:归一化结果会把 selectedColor 固化成写入时的主题色,
          // 存原始数据可让每次读取都按当前主题重新计算
          uni.setStorageSync(tabbarCacheKey(), { raw: tabbar, imagePath, _ts: Date.now() })
          // 原生自定义 tabBar 组件不参与编译、无法引用分片 key,
          // 同步一份归一化后的快照供其 attached 时读取
          uni.setStorageSync('tabbar', { ...config, _ts: Date.now() })
          console.log('[tabbar] cache written')
        } else {
          uni.removeStorageSync(tabbarCacheKey())
          // 记录"后台确认无装修配置":下次组件 attached 时可直接回退默认 tab,
          // 否则未装修商家每次冷启动都要等接口返回才有底部导航
          uni.setStorageSync('tabbar', { _empty: true, _ts: Date.now() })
          console.log('[tabbar] cache removed (no items)')
        }
        resolve(config)
      })
      .catch(err => {
        console.error('loadTabbar error:', err)
        // 请求失败时回退缓存（忽略有效期），无缓存则不渲染（不填充兜底数据）
        const cache = readTabbarCache(true)
        resolve(cache ? normalizeConfig(cache.raw, cache.imagePath) : null)
      })
  })
}
