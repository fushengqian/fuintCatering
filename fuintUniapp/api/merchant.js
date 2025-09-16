import request from '@/utils/request'

// api地址
const api = {
  merchantInfo: 'merchantApi/merchant/info',
  settingInfo: 'merchantApi/merchantSetting/settingInfo',
  saveSetting: 'merchantApi/merchantSetting/saveSetting',
}

// 当前商户信息
export const info = (param, option) => {
  const options = {
    isPrompt: true, //（默认 true 说明：本接口抛出的错误是否提示）
    load: true, //（默认 true 说明：本接口是否提示加载动画）
    ...option
  }
  return request.get(api.merchantInfo, param, options)
}

// 商户设置详情
export function settingInfo() {
  return request.get(api.settingInfo)
}

// 保存商户设置信息
export const saveSetting = (param, option) => {
  return request.post(api.saveSetting, param)
}