import request from '@/utils/request'

// api地址
const api = {
  recharge: 'clientApi/system/recharge',
  system: 'clientApi/system/config',
  storeList: 'clientApi/store/list',
  storeDetail: 'clientApi/store/detail',
}

// 充值配置
export function recharge() {
  return request.get(api.recharge)
}

// 系统配置
export function systemConfig() {
  return request.get(api.system)
}

// 店铺列表
export const storeList = (keyword) => {
  return request.post(api.storeList, { keyword })
}

// 店铺详情
export function storeDetail() {
  return request.get(api.storeDetail)
}