import request from '@/utils/request'

// api地址
const api = {
  doConfirm: 'clientApi/confirm/doConfirm',
}

// 确定核销
export function doConfirm(code, amount, remark) {
  return request.post(api.doConfirm,  { code, amount, remark })
}