import request from '@/utils/request'

// api地址
const api = {
  doRecharge: 'merchantApi/balance/doRecharge',
}

// 提交充值
export const doRecharge = (rechargeAmount, customAmount, memberId) => {
  return request.post(api.doRecharge, { rechargeAmount, customAmount, memberId })
}