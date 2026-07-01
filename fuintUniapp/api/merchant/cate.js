import request from '@/utils/request'

const api = {
  list: 'merchantApi/cate/list',
  save: 'merchantApi/cate/save',
  updateStatus: 'merchantApi/cate/updateStatus',
  info: 'merchantApi/cate/info'
}

// 分类列表
export function cateList() {
  return request.get(api.list)
}

// 保存分类（新增/编辑）
export function saveCate(param) {
  return request.post(api.save, param)
}

// 更新分类状态
export function updateCateStatus(param) {
  return request.post(api.updateStatus, param)
}

// 分类详情
export function cateInfo(id) {
  return request.get(api.info + '/' + id)
}
