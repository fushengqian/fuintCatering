import request from '@/utils/request'

// api地址
const api = {
  overview: 'clientApi/commission/overview',
  orders: 'clientApi/commission/orders',
  withdraw: 'clientApi/commission/withdraw',
  inviteList: 'clientApi/share/list',
  rewards: 'clientApi/commission/rewards',
  rewardLogs: 'clientApi/commission/rewardLogs'
}

// 佣金概览
export const overview = (param) => {
  return request.get(api.overview, param)
}

// 佣金订单列表
export const orders = (param) => {
  return request.get(api.orders, param)
}

// 申请提现
export const withdraw = (param) => {
  return request.post(api.withdraw, param)
}

// 邀请明细列表（支持level筛选）
export const inviteList = (param) => {
  return request.post(api.inviteList, param)
}

// 推荐奖励首页数据（活动规则+统计）
export const rewards = (param) => {
  return request.get(api.rewards, param)
}

// 推荐奖励记录
export const rewardLogs = (param) => {
  return request.get(api.rewardLogs, param)
}
