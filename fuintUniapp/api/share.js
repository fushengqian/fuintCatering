import request from '@/utils/request'

// api地址
const api = {
  list: 'clientApi/share/list',
  getMiniAppLink: 'clientApi/share/getMiniAppLink'
}

// 分享列表
export const list = (param) => {
  return request.post(api.list, param)
}

// 生成小程序链接
export const getMiniAppLink = (param) => {
  return request.post(api.getMiniAppLink, param)
}
