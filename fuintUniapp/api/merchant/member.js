import request from '@/utils/request'

// api地址
const api = {
  info: 'merchantApi/user/info',
  list: 'merchantApi/member/list'
}

// 会员详情
export function detail(userId, param) {
  return request.post(api.info, { userId, ...param })
}


// 会员列表
export function list(param, option) {
  return request.post(api.list, param, option)
}

