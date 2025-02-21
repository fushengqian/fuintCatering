import request from '@/utils/request'

// api地址
const api = {
  info: 'merchantApi/member/info',
  list: 'merchantApi/member/list',
  save: 'merchantApi/member/save',
}

// 会员详情
export function detail(memberId, param) {
  return request.post(api.info, { memberId, ...param })
}

// 会员列表
export function list(param, option) {
  return request.post(api.list, param, option)
}

// 保存会员信息
export const save = (param, option) => {
  return request.post(api.save, param)
}