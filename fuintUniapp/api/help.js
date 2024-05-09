import request from '@/utils/request'

// api地址
const api = {
  list: 'clientApi/article/list'
}

// 帮助中心列表
export const list = (param) => {
  return request.post(api.list, param)
}
