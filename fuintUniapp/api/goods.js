import request from '@/utils/request'

// api地址
const api = {
  cateList: 'clientApi/goodsApi/cateList',
  list: 'clientApi/goodsApi/list',
  search: 'clientApi/goodsApi/search',
  detail: 'clientApi/goodsApi/detail'
}

// 商品分类列表
export const cateList = param => {
  return request.get(api.cateList, param)
}

// 商品列表
export const list = param => {
  return request.get(api.list, param)
}

// 商品搜索
export const search = param => {
  return request.post(api.search,  param)
}

// 商品详情
export const detail = goodsId => {
  return request.post(api.detail, { goodsId })
}
