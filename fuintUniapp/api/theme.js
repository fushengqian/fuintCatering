import request from '@/utils/request'

// api地址
const api = {
  theme: 'clientApi/theme/info',
}

// 主题配置
export function theme() {
  return request.get(api.theme)
}
