import request from '@/utils/request'

// api地址
const api = {
  staffList: 'merchantApi/staff/staffList',
  saveStaff: 'merchantApi/staff/saveStaff',
}

// 获取员工列表
export const getStaffList = (param) => {
  return request.post(api.staffList, param)
}

// 保存员工信息
export const save = (param, option) => {
  return request.post(api.saveStaff, param)
}