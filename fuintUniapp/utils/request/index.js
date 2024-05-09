/**
 * request插件地址：https://ext.dcloud.net.cn/plugin?id=822
 */
import store from '@/store'
import request from './request'
import config from '@/config'
import { isWechat } from '../app';

// 后端api地址
const baseURL = config.apiUrl;
const merchantNo = config.merchantNo;

// 可以new多个request来支持多个域名请求
const $http = new request({
  // 接口请求地址
  baseUrl: baseURL,
  // 服务器本地上传文件地址
  fileUrl: baseURL,
  // 服务器上传图片默认url
  defaultUploadUrl: 'clientApi/file/upload',
  // 设置请求头（如果使用报错跨域问题，可能是content-type请求类型和后台那边设置的不一致）
  header: {
    'content-type': 'application/json;charset=utf-8'
  },
  // 请求超时时间, 单位ms（默认300000）
  timeout: 300000,
  // 默认配置（可不写）
  config: {
    // 是否自动提示错误
    isPrompt: true,
    // 是否显示加载动画
    load: true,
    // 是否使用数据工厂
    isFactory: true
  }
})

// 当前接口请求数
let requestNum = 0
// 请求开始拦截器
$http.requestStart = options => {
  if (options.load) {
    if (requestNum <= 0) {
      // 打开加载动画
      uni.showLoading({
        title: '加载中',
        mask: true
      })
    }
    requestNum += 1
  }
  // 图片上传大小限制
  if (options.method == "FILE" && options.maxSize) {
    // 文件最大字节: options.maxSize 可以在调用方法的时候加入参数
    const maxSize = options.maxSize
    for (let item of options.files) {
      if (item.size > maxSize) {
        setTimeout(() => {
          uni.showToast({
            title: "图片过大，请重新上传",
            icon: "none"
          })
        }, 10)
        return false
      }
    }
  }
  // 请求前加入当前终端
  options.header['platform'] = store.getters.platform ? String(store.getters.platform) : ''
  
  // 请求前加入Token
  options.header['Access-Token'] = store.getters.token ? String(store.getters.token) : ''
  // 商户号
  options.header['merchantNo'] = uni.getStorageSync("merchantNo") ? uni.getStorageSync("merchantNo") : merchantNo; 
  // 店铺ID
  options.header['storeId'] = uni.getStorageSync("storeId") ? uni.getStorageSync("storeId") : 0;
  options.header['latitude'] = uni.getStorageSync("latitude") ? uni.getStorageSync("latitude") : '';
  options.header['longitude'] = uni.getStorageSync("longitude") ? uni.getStorageSync("longitude") : '';
  options.header['isWechat'] = isWechat() ? 'Y' : 'N';
  // return false 表示请求拦截，不会继续请求
  return options
}

// 请求结束
$http.requestEnd = options => {
  // 判断当前接口是否需要加载动画
  if (options.load) {
    requestNum = requestNum - 1
    if (requestNum <= 0) {
      uni.hideLoading()
    }
  }
}

// 登录弹窗次数
let loginPopupNum = 0

// 所有接口数据处理（可在接口里设置不调用此方法）
// 此方法需要开发者根据各自的接口返回类型修改，以下只是模板
$http.dataFactory = async res => {
  // console.log("接口请求数据", {
  //   url: res.url,
  //   resolve: res.response,
  //   header: res.header,
  //   data: res.data,
  //   method: res.method,
  // })

  if (!res.response.statusCode || res.response.statusCode != 200) {
    // 返回错误的结果(catch接受数据)
    return Promise.reject({
      statusCode: res.response.statusCode,
      // errMsg: "数据工厂验证不通过"
      errMsg: 'http状态码错误'
    })
  }

  let httpData = res.response.data
  if (typeof httpData == "string") {
    try {
      httpData = JSON.parse(httpData)
    } catch {
      httpData = false
    }
  }
  if (httpData === false || typeof httpData !== 'object') {
    // 返回错误的结果(catch接受数据)
    return Promise.reject({
      statusCode: res.response.statusCode,
      errMsg: "返回数据验证不通过"
    })
  }

  /*********以下只是模板(及共参考)，需要开发者根据各自的接口返回类型修改*********/
  // 判断数据是否请求成功
  // result.code [ 200正常 5000有错误 1001未登录 4003没有权限访问 ]
  // 判断是否需要登录
  if (httpData.code == 1001) {
    // 1001也有可能是后端登录态到期, 所以要清空本地的登录状态
    store.dispatch('Logout')
    // 弹窗告诉用户去登录
    if (loginPopupNum <= 0) {
      loginPopupNum++
      uni.showModal({
        title: '温馨提示',
        content: '此时此刻需要您登录喔~',
        confirmText: "去登录",
        cancelText: "再逛会",
        success: res => {
          loginPopupNum--
          if (res.confirm) {
            uni.navigateTo({
              url: "/pages/login/index"
            })
          }
        }
      })
    }
    // 返回错误的结果(catch接受数据)
    return Promise.reject({
      statusCode: 0,
      errMsg: httpData.message,
      result: httpData
    })
  }
  // 其他错误提示
  else if (httpData.status == 5000) {
    if (res.isPrompt) {
      setTimeout(() => {
        uni.showToast({
          title: httpData.message,
          icon: "none",
          duration: 2500
        }, 10)
      })
    }
    // 返回错误的结果(catch接受数据)
    return Promise.reject({
      statusCode: 0,
      errMsg: httpData.message,
      result: httpData
    })
  } else {
    // 返回正确的结果(then接受数据)
    return Promise.resolve(httpData)
  }

  /*********以上只是模板(及共参考)，需要开发者根据各自的接口返回类型修改*********/
}

// 错误回调
$http.requestError = e => {
  if (e.statusCode === 0) {
    throw e
  } else {
    setTimeout(() => {
      uni.showToast({
        title: `网络请求出错：${e.errMsg}`,
        icon: "none",
        duration: 2500
      })
    })
  }
}
export default $http
