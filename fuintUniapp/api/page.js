import request from '@/utils/request'

// api地址
const apiUri = {
  home: 'clientApi/page/home'
}

// 页面数据
export function home() {
  return request.get(apiUri.home)
}
