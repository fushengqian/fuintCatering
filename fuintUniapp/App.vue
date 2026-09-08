<script>
  import { loadTheme, getTheme, getThemePrimary, isLightColor } from '@/utils/theme'

  export default {

    /**
     * 全局变量
     */
    globalData: {

    },

    /**
     * 初始化完成时触发
     */
    onLaunch(options) {
      // 小程序主动更新
      this.updateManager()
      // 先用本地缓存主题同步设置导航栏, 保证首帧不是 pages.json 默认色
      this.applyNavigationBarColor(getTheme())
      // 预加载主题配置(force=true: 忽略本地缓存, 启动时直接拉取后台最新主题并写缓存,
      // 避免页面先用默认色渲染、接口返回后再切换造成的闪烁)
      loadTheme(true).then(theme => {
        this.applyNavigationBarColor(theme)
      })
      if (options.query.spm) {
          uni.setStorageSync('shareId', options.query.spm);
      }
    },

    methods: {

      /**
       * 同步设置顶部导航栏颜色
       */
      applyNavigationBarColor(theme) {
        // #ifdef MP-WEIXIN
        const c = (theme && theme.colors) || {}
        const bg = c.primary || getThemePrimary()
        try {
          uni.setNavigationBarColor({
            // 导航栏背景为浅色(含白色兜底)时使用黑色文字, 否则白色文字
            frontColor: isLightColor(bg) ? '#000000' : '#ffffff',
            backgroundColor: bg,
            animation: { duration: 0, timingFunc: 'linear' }
          })
        } catch (e) {}
        // #endif
      },

      /**
       * 小程序主动更新
       */
      updateManager() {
        const updateManager = uni.getUpdateManager();
        updateManager.onCheckForUpdate(res => {
          // 请求完新版本信息的回调
          // console.log(res.hasUpdate)
        })
        updateManager.onUpdateReady(() => {
          uni.showModal({
            title: '更新提示',
            content: '新版本已经准备好，即将重启应用',
            showCancel: false,
            success(res) {
              if (res.confirm) {
                // 新的版本已经下载好，调用 applyUpdate 应用新版本并重启
                updateManager.applyUpdate()
              }
            }
          })
        })
        updateManager.onUpdateFailed(() => {
          // 新的版本下载失败
          uni.showModal({
            title: '更新提示',
            content: '新版本下载失败',
            showCancel: false
          })
        })
      }
    }

  }
</script>

<style lang="scss">
  /* 引入uView库样式 */
  @import "uview-ui/index.scss";
</style>

<style>
  /* 项目基础样式 */
  @import "./app.scss";
</style>
