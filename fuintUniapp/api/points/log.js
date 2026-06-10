import request from '@/utils/request'

// api地址
const api = {
  list: 'clientApi/points/list',
  gift: 'clientApi/points/doGive',
  rank: 'clientApi/points/rank'
}

// 积分明细列表
export const list = (param) => {
  return request.get(api.list, param)
}

// 积分转赠
export const gift = (param) => {
  return request.post(api.gift, param)
}

// 积分排行榜
export const rank = (param) => {
  return request.get(api.rank, param)
}
