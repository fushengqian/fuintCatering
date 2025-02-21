import request from '@/utils/request'

// api地址
const api = {
  sendCoupon: 'merchantApi/coupon/sendCoupon',
  search: 'clientApi/coupon/list'
}

// 查询接口
export const search = (param) => {
  return request.post(api.search, param)
}

// 发放卡券
export const sendCoupon = (param) => {
  return request.post(api.sendCoupon, param)
}