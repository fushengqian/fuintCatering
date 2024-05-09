import request from '@/utils/request'

// api地址
const api = {
  userInfo: 'clientApi/user/info',
  qrCode: 'clientApi/user/qrCode',
  assets: 'clientApi/user/asset',
  setting: 'clientApi/user/setting',
  defaultStore: 'clientApi/user/defaultStore',
  save: 'clientApi/user/saveInfo'
}

// 当前登录的用户信息
export const info = (param, option) => {
  const options = {
    isPrompt: true, //（默认 true 说明：本接口抛出的错误是否提示）
    load: true, //（默认 true 说明：本接口是否提示加载动画）
    ...option
  }
  return request.get(api.userInfo, param, options)
}

// 当前登录的会员码信息
export const qrCode = (param, option) => {
  const options = {
    isPrompt: true,
    load: true,
    ...option
  }
  return request.get(api.qrCode, param, options)
}

// 账户资产
export const assets = (param, option) => {
  return request.get(api.assets, param)
}

// 获取会员设置
export const setting = (param, option) => {
  return request.get(api.setting, param)
}

// 设置会员的默认店铺
export const defaultStore = (storeId) => {
  return request.get(api.defaultStore, { storeId })
}

// 保存会员信息
export const save = (param, option) => {
  return request.post(api.save, param)
}