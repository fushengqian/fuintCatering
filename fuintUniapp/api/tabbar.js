import request from '@/utils/request'

// api地址
const api = {
  tabbar: 'clientApi/tabbar/info',
}

// 底部导航配置
export function getTabbar() {
  // load: false 避免每次 tab 切换拉取时弹出加载提示
  return request.get(api.tabbar, {}, { load: false })
}

// 兼容旧调用
export const tabbar = getTabbar
