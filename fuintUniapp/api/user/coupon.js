import request from '@/utils/request'

// api地址
const api = {
  receive: 'clientApi/user/coupon/receive'
}

// 优惠券列表
export const receive = (data) => {
  return request.post(api.receive, data)
}
