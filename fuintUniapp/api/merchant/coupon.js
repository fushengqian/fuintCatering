import request from '@/utils/request'

// api地址
const api = {
  sendCoupon: 'merchantApi/coupon/sendCoupon',
  couponList: 'merchantApi/coupon/couponList',
  saveCoupon: 'merchantApi/coupon/saveCoupon',
  search: 'clientApi/coupon/list',
  info: 'clientApi/coupon/detail'
}

// 查询接口
export const search = (param) => {
  return request.post(api.search, param)
}

// 列表接口
export const couponList = (param) => {
  return request.post(api.couponList, param)
}

// 保存接口
export const saveCoupon = (param) => {
  return request.post(api.saveCoupon, param)
}

// 发放卡券
export const sendCoupon = (param) => {
  return request.post(api.sendCoupon, param)
}

// 卡券详情
export function info(couponId) {
  return request.post(api.info, { couponId })
}