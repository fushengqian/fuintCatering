import request from '@/utils/request'

// api地址
const api = {
  list: 'clientApi/book/list',
  detail: 'clientApi/book/detail',
  cate: 'clientApi/book/cateList',
  submit: 'clientApi/book/submit',
  bookable: 'clientApi/book/bookable',
  myBookList: 'clientApi/book/myBook',
  myBookDetail: 'clientApi/book/myBookDetail',
  cancel: 'clientApi/book/cancel',
}

// 预约项目列表
export const list = (param) => {
  return request.post(api.list, param)
}

// 预约详情
export const detail = (bookId) => {
  return request.post(api.detail, { bookId })
}

// 预约分类列表
export const cateList = (param) => {
  return request.get(api.cate, param)
}

// 提交预约
export const submit = (data) => {
  return request.post(api.submit, data)
}

// 是否可预约
export const bookable = (data) => {
  return request.post(api.bookable, data)
}

// 我的预约列表
export const myBookList = (param) => {
  return request.get(api.myBookList, param)
}

// 我的预约详情
export const myBookDetail = (bookId) => {
  return request.post(api.myBookDetail, { bookId })
}

// 取消预约
export function cancel(bookId, data) {
  return request.get(api.cancel, { bookId, ...data })
}