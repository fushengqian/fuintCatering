import request from '@/utils/request'

// api地址
const api = {
  list: 'clientApi/myCoupon/list',
  mydetail: 'clientApi/userCouponApi/detail',
  detail: 'clientApi/coupon/detail'
}

// 会员卡券列表
export const list = (param, option) => {
  const options = {
    isPrompt: true, //（默认 true 说明：本接口抛出的错误是否提示）
    load: true, //（默认 true 说明：本接口是否提示加载动画）
    ...option
  }
  return request.get(api.list, param, options)
}

// 卡券详情
export function detail(couponId, userCouponId, userCouponCode) {
    if (parseInt(userCouponId) > 0 || userCouponCode.length > 0) {
        if (userCouponId == undefined) {
            userCouponId = 0
        }
        if (userCouponCode == undefined) {
            userCouponCode = ""
        }
        return request.get(api.mydetail, { userCouponId, userCouponCode })    
    } else {
        return request.post(api.detail, { couponId })    
    }
}
