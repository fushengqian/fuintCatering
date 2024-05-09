import request from '@/utils/request'

// api地址
const api = {
  list: 'clientApi/article/list',
  detail: 'clientApi/article/detail',
  cate: 'clientApi/article/cateList',
}

// 文章列表
export const list = (param) => {
  return request.post(api.list, param)
}

// 文章详情
export const detail = (articleId) => {
  return request.post(api.detail, { articleId })
}

// 文章分类列表
export const cateList = (param) => {
  return request.post(api.cate, param)
}