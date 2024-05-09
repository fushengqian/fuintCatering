import request from '@/utils/request'

// api地址
const api = {
  list: 'clientApi/coupon/list',
  receive: 'clientApi/coupon/receive',
  detail: 'clientApi/coupon/detail'
}

// 卡券列表
export const list = (param, option) => {
  const options = {
    isPrompt: true, //（默认 true 说明：本接口抛出的错误是否提示）
    load: true, //（默认 true 说明：本接口是否提示加载动画）
    ...option
  }
  return request.post(api.list, param, options)
}

// 领券接口
export const receive = (param) => {
  return request.post(api.receive, param)
}

// 会员卡券详情
export function detail(couponId) {
  return request.post(api.detail, { couponId })
}
