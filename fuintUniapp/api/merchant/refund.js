import request from '@/utils/request'

// api地址
const api = {
  list: 'merchantApi/refund/list',
  detail: 'merchantApi/refund/detail',
  update: 'merchantApi/refund/update'
}


// 售后订单列表
export function list(param, option) {
  return request.post(api.list, param, option)
}

// 售后订单详情
export function detail(refundId, param) {
  return request.get(api.detail, { refundId, ...param })
}

// 更新售后订单
export const update = (param, option) => {
  return request.post(api.update, param)
}