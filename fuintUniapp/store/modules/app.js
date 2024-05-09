import {
  PLATFORM
} from '@/store/mutation-types'
import storage from '@/utils/storage'


const app = {
  state: {
    // 当前终端平台
    platform: ''
  },

  mutations: {
    SET_PLATFORM: (state, value) => {
      state.platform = value
    }
  },

  actions: {

  }
}

export default app
