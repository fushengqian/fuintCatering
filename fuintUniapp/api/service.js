import request from '@/utils/request'

const api = {
  list: 'clientApi/service/list',
}

export const list = () => request.post(api.list)
