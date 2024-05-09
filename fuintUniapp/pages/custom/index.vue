<template>
  <view class="container">
    <!-- 店铺页面组件 -->
    <Page :items="items" />
  </view>
</template>

<script>
  import * as Api from '@/api/page'
  import Page from '@/components/page'

  const App = getApp()

  export default {
    components: {
      Page
    },
    data() {
      return {
        // 页面参数
        options: {},
        // 页面属性
        page: {},
        // 页面元素
        items: []
      }
    },

    /**
     * 生命周期函数--监听页面加载
     */
    onLoad(options) {
      // 当前页面参数
      this.options = options
      // 加载页面数据
      this.getPageData()
    },
    methods: {

      /**
       * 加载页面数据
       * @param {Object} callback
       */
      getPageData(callback) {
        const app = this
        const pageId = app.options.pageId || 0
        Api.detail(pageId)
          .then(result => {
            // 设置页面数据
            const { data: { pageData } } = result
            app.page = pageData.page
            app.items = pageData.items
            // 设置顶部导航栏栏
            app.setPageBar();
          })
          .finally(() => callback && callback())
      },

      /**
       * 设置顶部导航栏
       */
      setPageBar() {
        const { page } = this
        // 设置页面标题
        uni.setNavigationBarTitle({
          title: page.params.title
        })
        // 设置navbar标题、颜色
        uni.setNavigationBarColor({
          frontColor: page.style.titleTextColor === 'white' ? '#ffffff' : '#000000',
          backgroundColor: page.style.titleBackgroundColor
        })
      }

    },

    /**
     * 下拉刷新
     */
    onPullDownRefresh() {
      // 获取首页数据
      this.getPageData(() => {
        uni.stopPullDownRefresh()
      })
    },

    /**
     * 分享当前页面
     */
    onShareAppMessage() {
      const app = this
      const { page } = app
      return {
        title: page.params.share_title,
        path: "/pages/index/index?" + app.$getShareUrlParams()
      }
    },

    /**
     * 分享到朋友圈
     * 本接口为 Beta 版本，暂只在 Android 平台支持，详见分享到朋友圈 (Beta)
     * https://developers.weixin.qq.com/miniprogram/dev/framework/open-ability/share-timeline.html
     */
    onShareTimeline() {
      const app = this
      const { page } = app
      return {
        title: page.params.share_title,
        path: "/pages/index/index?" + app.$getShareUrlParams()
      }
    }

  }
</script>

<style lang="scss" scoped>
  .container {
    background: #fff;
  }
</style>
