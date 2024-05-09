import store from '@/store'
import storage from '@/utils/storage'
import { getPlatform } from '@/utils/app'
import { ACCESS_TOKEN, USER_ID } from '@/store/mutation-types'

export default function Initializer() {
  // 当前运行的终端
  store.commit('SET_PLATFORM', getPlatform())
  // 用户认证token
  store.commit('SET_TOKEN', storage.get(ACCESS_TOKEN))
  // 当前用户ID
  store.commit('SET_USER_ID', storage.get(USER_ID))
}
