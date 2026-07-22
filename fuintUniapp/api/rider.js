import request from '@/utils/request'

// api地址
const api = {
  // 订单
  pool: 'riderApi/order/pool',
  accept: 'riderApi/order/accept',
  myOrders: 'riderApi/order/myOrders',
  detail: 'riderApi/order/detail',
  pickup: 'riderApi/order/pickup',
  deliver: 'riderApi/order/deliver',
  search: 'riderApi/order/search',
  // 统计
  overview: 'riderApi/stats/overview',
  delivery: 'riderApi/stats/delivery',
  income: 'riderApi/stats/income',
  // 个人信息
  profileInfo: 'riderApi/profile/info',
}

// 获取待接单订单池
export function getOrderPool(param) {
  return request.post(api.pool, param)
}

// 骑手接单
export function acceptOrder(orderId) {
  return request.post(api.accept, null, { params: { orderId } })
}

// 获取骑手配送订单列表
export function getMyOrders(param) {
  return request.post(api.myOrders, param)
}

// 获取订单详情
export function getOrderDetail(orderId) {
  return request.get(api.detail, { orderId })
}

// 确认取货
export function confirmPickup(orderId) {
  return request.post(api.pickup, null, { params: { orderId } })
}

// 确认送达
export function confirmDeliver(orderId) {
  return request.post(api.deliver, null, { params: { orderId } })
}

// 搜索已完成配送订单
export function searchOrders(param) {
  return request.post(api.search, param)
}

// 获取今日概览
export function getOverview() {
  return request.get(api.overview)
}

// 获取配送统计
export function getDeliveryStats(mode) {
  return request.get(api.delivery, { mode })
}

// 获取收入统计
export function getIncomeStats(mode) {
  return request.get(api.income, { mode })
}

// 获取骑手个人信息
export function getProfileInfo() {
  return request.get(api.profileInfo)
}
