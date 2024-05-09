import * as SettingApi from '@/api/setting'
import storage from '@/utils/storage'

const CACHE_KEY = 'Setting'

// 写入缓存, 到期时间30分钟
const setStorage = (data) => {
  const expireTime = 30 * 60;
  storage.set(CACHE_KEY, data, expireTime);
}

// 获取缓存中的数据
const getStorage = () => {
  return storage.get(CACHE_KEY);
}

// 获取系统设置
const getApiData = () => {
  return new Promise((resolve, reject) => {
    SettingApi.data()
      .then(result => {
        resolve(result.data.setting);
      })
  })
}

/**
 * 获取商城设置
 * 有缓存的情况下返回缓存, 没有缓存从后端api获取
 * @param {bool} isCache 是否从缓存中获取
 */
const data = (isCache = true) => {
  return new Promise((resolve, reject) => {
    const cacheData = getStorage()
    if (isCache && cacheData) {
      resolve(cacheData)
    } else {
      getApiData().then(data => {
        setStorage(data)
        resolve(data)
      })
    }
  })
}

// 获取指定的系统设置
const item = (key, isCache = true) => {
  return new Promise((resolve, reject) => {
    data(isCache).then(setting => {
      resolve(setting[key])
    })
  })
}

export default {
  data,
  item
}
