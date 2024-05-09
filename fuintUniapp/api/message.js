import request from '@/utils/request'

// api地址
const api = {
  getOne: 'clientApi/message/getOne',
  readed: 'clientApi/message/readed',
  getSubTemplate: 'clientApi/message/getSubTemplate'
}

// 读取最新的一条消息
export const getOne = (param, option) => {
  return request.get(api.getOne, param)
}

// 将消息置为已读
export const readed = (param) => {
  return request.get(api.readed, param)
}

// 获取订阅消息模板
export const getSubTemplate = (param, option) => {
  return request.get(api.getSubTemplate, param)
}

