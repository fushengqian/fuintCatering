import request from '@/utils/request'

// api地址
const api = {
  doGive: 'clientApi/give/doGive',
  giveLog: 'clientApi/give/giveLog'
}

// 转赠
export const doGive = (data) => {
  return request.post(api.doGive, data)
}

// 转赠记录
export const giveLog = (param, option) => {
  return request.post(api.giveLog, param)
}
