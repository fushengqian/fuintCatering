import request from '@/utils/request'

// api地址
const api = {
  submit: 'clientApi/goodsApi/comment/submit',
  list: 'clientApi/goodsApi/comment/list',
  myList: 'clientApi/goodsApi/comment/myList',
  orderGoods: 'clientApi/goodsApi/comment/orderGoods',
  delete: 'clientApi/goodsApi/comment/delete'
}

// 提交商品评价
export const submit = param => {
  return request.post(api.submit, param)
}

// 商品评价列表（含评分统计）
export const list = param => {
  return request.get(api.list, param)
}

// 我的评价列表
export const myList = param => {
  return request.post(api.myList, param)
}

// 订单内商品评价情况
export const orderGoods = orderId => {
  return request.get(api.orderGoods, { orderId })
}

// 删除自己的评价
export const del = commentId => {
  return request.post(api.delete, { commentId })
}
