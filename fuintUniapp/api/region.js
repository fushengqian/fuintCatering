import request from '@/utils/request'

// api地址
const api = {
  all: 'clientApi/region/all',
  tree: 'clientApi/region/tree'
}

// 获取所有地区
export const all = (param) => {
  return request.get(api.all, param)
}

// 获取所有地区(树状)
export const tree = (param) => {
  return request.get(api.tree, param)
}
