import request from '@/utils/request'

// api地址
const api = {
  submit: 'clientApi/settlement/submit',
  prePay: 'clientApi/pay/prePay',
}

// 结算台订单提交
export const submit = (targetId, selectNum, type, remark, payAmount, usePoint, couponId, cartIds, goodsId, skuId, buyNum, orderMode, payType) => {
  return request.post(api.submit, { targetId, selectNum, type, remark, payAmount, usePoint, couponId, cartIds, goodsId, skuId, buyNum, orderMode, payType})
}

// 支付前查询
export const prePay = (param) => {
  return request.get(api.prePay, param)
}

// 发起收款
export const doCashier = (param) => {
  return request.post(api.submit, param)
}
