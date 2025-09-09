import request from '@/utils/request'

// api地址
const api = {
  initiatePayment: 'clientApi/yeepay/initiate-payment',
  queryOrder: 'clientApi/yeepay/query-order'
}

// 发起易宝支付
export function initiatePayment(orderId, payWay, channel) {
  return request.post(api.initiatePayment, { orderId, payWay, channel })
}

// 查询易宝支付订单状态
export function queryOrder(orderId) {
  return request.get(api.queryOrder, { orderId })
}