import request from '@/utils/request'

// api地址
const api = {
  list: 'clientApi/cart/list',
  save: 'clientApi/cart/save',
  clear: 'clientApi/cart/clear',
}

// 购物车列表
export const list = (cartIds, goodsId, skuId, buyNum, couponId, point, orderMode) => {
  return request.post(api.list, { cartIds, goodsId, skuId, buyNum, couponId, point, orderMode })
}

// 更新购物车
export const save = (goodsId, action, skuId, buyNum) => {
  return request.post(api.save, { goodsId, action, skuId, buyNum })
}

// 删除购物车商品
export const clear = (cartId) => {
  return request.post(api.clear, { cartId })
}
