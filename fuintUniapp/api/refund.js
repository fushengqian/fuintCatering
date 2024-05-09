import request from '@/utils/request'

// api地址
const api = {
  list: 'clientApi/refund/list',
  goods: 'clientApi/refund/goods',
  apply: 'clientApi/refund/submit',
  detail: 'clientApi/refund/detail',
  delivery: 'clientApi/refund/delivery'
}

// 售后单列表
export const list = (param, option) => {
  return request.get(api.list, param, option)
}

// 申请售后
export const apply = (orderId, data) => {
  return request.post(api.apply, { orderId, type: data.type, remark: data.content, images: data.images })
}

// 售后单详情
export const detail = (refundId, param) => {
  return request.get(api.detail, { refundId, ...param })
}

// 用户发货
export const delivery = (refundId, data) => {
  return request.post(api.delivery, { refundId, form: data })
}
