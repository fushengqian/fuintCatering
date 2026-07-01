import request from '@/utils/request'

const api = {
  list: 'merchantApi/goods/list',
  detail: 'merchantApi/goods/detail',
  save: 'merchantApi/goods/save',
  updateStatus: 'merchantApi/goods/updateStatus'
}

// 商品列表
export function goodsList(param) {
  return request.post(api.list, param)
}

// 商品详情
export function goodsDetail(param) {
  return request.post(api.detail, param)
}

// 保存商品
export function saveGoods(param) {
  return request.post(api.save, param)
}

// 更新商品状态
export function updateGoodsStatus(param) {
  return request.post(api.updateStatus, param)
}
