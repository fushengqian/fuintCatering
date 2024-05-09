import util from '@/utils/util'

export default {
  data() {
    return {}
  },
  methods: {

    /**
     * link对象点击事件
     * 支持tabBar页面
     */
    onLink(linkObj) {
      if (!linkObj) return false
      // 跳转到指定页面
      if (linkObj.type === 'PAGE') {
        this.$navTo(linkObj.param.path, linkObj.param.query)
      }
      return true
    }
  },

}
