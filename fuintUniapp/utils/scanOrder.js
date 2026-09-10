import * as settingApi from '@/api/setting'

// 模块级 JSSDK 就绪状态（H5 下扫描桌码依赖微信 JSSDK）
let wxSdkReady = false

/**
 * 预加载微信 JSSDK（H5 专用）
 * 在页面 onLoad 阶段调用一次即可
 */
export function preloadWxSdk() {
  // #ifdef H5
  if (wxSdkReady) return
  if (typeof document === 'undefined') return
  const script = document.createElement('script')
  script.src = 'https://res.wx.qq.com/open/js/jweixin-1.6.0.js'
  script.onload = function() {
    let n = 0
    const timer = setInterval(function() {
      if (window.wx && typeof window.wx.config === 'function') {
        clearInterval(timer)
        wxSdkReady = true
      } else if (++n >= 30) {
        clearInterval(timer)
      }
    }, 200)
  }
  script.onerror = function() {}
  document.head.appendChild(script)
  // #endif
}

/**
 * 扫描桌码并跳转到点餐页
 * @param {Object} app 当前页面/组件实例（需要 $navTo）
 */
export function scanTableCode(app) {
  // #ifdef MP-WEIXIN
  uni.scanCode({
    scanType: ['qrCode'],
    success(res) {
      handleScanResult(app, res.result)
    },
    fail(err) {
      if (err.errMsg !== 'scanCode:fail cancel') {
        uni.showToast({ title: '扫码失败，请重试', icon: 'none' })
      }
    }
  })
  // #endif
  // #ifdef H5
  const ua = navigator.userAgent.toLowerCase()
  if (ua.indexOf('micromessenger') === -1) {
    uni.showToast({ title: '请在微信中扫码', icon: 'none' })
    return
  }
  loadWxJsSdk(() => {
    const url = window.location.href.split('#')[0]
    settingApi.jsSdkConfig(url).then(function(result) {
      const config = result.data
      if (!config || !config.appId) {
        uni.showToast({ title: '公众号AppID未配置', icon: 'none', duration: 3000 })
        return
      }
      if (!window.wx || typeof window.wx.config !== 'function') {
        uni.showToast({ title: '微信SDK未就绪', icon: 'none', duration: 3000 })
        return
      }
      window.wx.config({
        debug: false,
        appId: config.appId,
        timestamp: config.timestamp,
        nonceStr: config.nonceStr,
        signature: config.signature,
        jsApiList: ['scanQRCode']
      })
      window.wx.ready(function() {
        window.wx.scanQRCode({
          needResult: 1,
          scanType: ['qrCode'],
          success: function(res) { handleScanResult(app, res.resultStr) },
          fail: function() { uni.showToast({ title: '扫码失败，请重试', icon: 'none' }) }
        })
      })
      window.wx.error(function() {
        uni.showToast({ title: '微信配置失败，请重试', icon: 'none' })
      })
    }).catch(function() {
      uni.showToast({ title: '获取配置失败', icon: 'none', duration: 2500 })
    })
  })
  // #endif
}

/**
 * 确保微信 JSSDK 已就绪（H5 专用，内部方法）
 */
function loadWxJsSdk(callback) {
  // #ifdef H5
  if (wxSdkReady && window.wx && typeof window.wx.config === 'function') {
    callback()
    return
  }
  let n = 0
  const timer = setInterval(function() {
    if (window.wx && typeof window.wx.config === 'function') {
      clearInterval(timer)
      wxSdkReady = true
      callback()
    } else if (++n >= 15) {
      clearInterval(timer)
      uni.showToast({ title: '请刷新页面后重试', icon: 'none', duration: 3000 })
    }
  }, 200)
  // #endif
}

/**
 * 解析桌码扫描结果并跳转点餐页
 */
function handleScanResult(app, result) {
  let tableId = 0
  if (/^\d+$/.test(result)) {
    tableId = parseInt(result)
  } else {
    const match = result.match(/[?&]tableId=(\d+)/)
    if (match) tableId = parseInt(match[1])
  }
  if (tableId > 0) {
    uni.setStorageSync('tableId', tableId)
    app.$navTo('pages/category/index', { tableId })
  } else {
    uni.showToast({ title: '无效的桌码二维码', icon: 'none' })
  }
}