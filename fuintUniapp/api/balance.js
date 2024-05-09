import request from '@/utils/request'

// api地址
const api = {
  list: 'clientApi/balance/list',
  setting: 'clientApi/balance/setting',
  doRecharge: 'clientApi/balance/doRecharge',
}

// 余额设置
export const setting = (param) => {
  return request.get(api.setting, param)
}

// 余额明细
export const list = (param) => {
  return request.post(api.list, param)
}

// 提交充值
export const doRecharge = (rechargeAmount, customAmount) => {
  return request.post(api.doRecharge, { rechargeAmount, customAmount })
}