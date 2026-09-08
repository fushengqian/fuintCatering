import util from '@/utils/util'

export default {
  data() {
    return {}
  },
  methods: {

    /**
     * link对象点击事件
     * 支持tabBar页面
     * 兼容字符串形式页面路径与对象形式 {type: 'PAGE', param: {path, query}}
     */
    onLink(linkObj) {
      if (!linkObj) return false
      // 字符串形式的页面路径
      if (typeof linkObj === 'string') {
        this.$navTo(linkObj)
        return true
      }
      // 跳转到指定页面
      if (linkObj.type === 'PAGE') {
        this.$navTo(linkObj.param.path, linkObj.param.query)
      }
      return true
    }
  },

}
