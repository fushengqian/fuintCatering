import request from '@/utils/request'

// api地址
const api = {
  todoCounts: 'clientApi/order/todoCounts',
  list: 'clientApi/order/list',
  detail: 'clientApi/order/detail',
  cancel: 'clientApi/order/cancel',
  pay: 'clientApi/pay/doPay',
  receipt: 'clientApi/order/receipt',
}

// 当前用户待处理的订单数量
export function todoCounts(param) {
  return request.get(api.todoCounts, param)
}

// 我的订单列表
export function list(param, option) {
  return request.post(api.list, param, option)
}

// 订单详情
export function detail(orderId, param) {
  return request.get(api.detail, { orderId, ...param })
}

// 取消订单
export function cancel(orderId, data) {
  return request.get(api.cancel, { orderId, ...data })
}

// 立即支付
export function pay(orderId, payType, param) {
  return request.get(api.pay, { orderId, payType, ...param })
}

// 确认收货
export function receipt(orderId, data) {
  return request.get(api.receipt, { orderId, ...data })
}
