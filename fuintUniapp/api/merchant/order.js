import request from '@/utils/request'

// api地址
const api = {
  list: 'merchantApi/order/list',
  detail: 'merchantApi/order/detail',
  confirm: 'merchantApi/order/confirm'
}


// 订单列表
export function list(param, option) {
  return request.post(api.list, param, option)
}

// 订单详情
export function detail(orderId, param) {
  return request.get(api.detail, { orderId, ...param })
}

// 核销订单
export const confirm = (param, option) => {
  return request.post(api.confirm, param)
}
