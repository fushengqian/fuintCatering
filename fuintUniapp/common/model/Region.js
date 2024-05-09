import * as Api from '@/api/region'
import storage from '@/utils/storage'

const REGION_TREE = 'region_tree'

/**
 * 商品分类 model类
 * RegionModel
 */
export default {

  // 从服务端获取全部地区数据(树状)
  getTreeDataFromApi () {
    return new Promise((resolve, reject) => {
      Api.tree().then(result => resolve(result.data.data))
    })
  },

  // 获取所有地区(树状)
  getTreeData () {
    return new Promise((resolve, reject) => {
      // 判断缓存中是否存在
      const data = storage.get(REGION_TREE)
      // 从服务端获取全部地区数据
      if (data) {
        resolve(data)
      } else {
        this.getTreeDataFromApi().then(list => {
          // 缓存24小时
          storage.set(REGION_TREE, list, 1 * 24 * 60 * 60 * 1000)
          resolve(list)
        })
      }
    })
  },

  // 获取所有地区的总数
  getCitysCount () {
    return new Promise((resolve, reject) => {
      // 获取所有地区(树状)
      this.getTreeData().then(data => {
        const cityIds = []
        // 遍历省份
        for (const pidx in data) {
          const province = data[pidx]
          // 遍历城市
          for (const cidx in province.city) {
            const cityItem = province.city[cidx]
            cityIds.push(cityItem.id)
          }
        }
        resolve(cityIds.length)
      })
    })
  }

}
