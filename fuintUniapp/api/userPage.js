import request from '@/utils/request'

// api地址
const api = {
  userPage: 'clientApi/userPage/info',
}

// 个人中心页面配置
export function info() {
  return request.get(api.userPage)
}
