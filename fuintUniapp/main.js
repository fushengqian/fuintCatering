import Vue from 'vue'
import App from './App'
import store from './store'
import uView from 'uview-ui'
import bootstrap from './core/bootstrap'
import {
  getPlatform,
  navTo,
  showToast,
  showSuccess,
  showError,
  getShareUrlParams
} from './utils/app'
import './core/ican-H5Api'

Vue.config.productionTip = false

App.mpType = 'app'

// 当前运行的终端
Vue.prototype.$platform = getPlatform()

// 载入uView库
Vue.use(uView)

// 挂载全局函数
Vue.prototype.$toast = showToast
Vue.prototype.$success = showSuccess
Vue.prototype.$error = showError
Vue.prototype.$navTo = navTo
Vue.prototype.$getShareUrlParams = getShareUrlParams

// 实例化应用
const app = new Vue({
  ...App,
  store,
  created: bootstrap
})
app.$mount()
