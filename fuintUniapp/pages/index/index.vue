<template>
  <view class="container" :style="themeVars">
      <empty v-if="!storeInfo" :isLoading="isLoading" tips="数据加载中..."></empty>
      <!-- 页面装修模式：后台配置默认装修页(components>0)时，整体替换为装修渲染 -->
      <Page v-if="storeInfo && pageItems.length > 0" :items="pageItems" :imagePath="imagePath" :storeInfo="storeInfo" :userInfo="userInfo"/>
      <!-- 默认点餐布局（未装修时保留原有首页，兼容现网） -->
      <block v-else>
          <HomeBanner v-if="storeInfo" :banners="banner"/>
          <HomeUser v-if="storeInfo" :userInfo="userInfo"/>
          <view class="scan-entry" v-if="storeInfo" @click="onScanCode">
              <view class="scan-icon">
                  <text class="iconfont icon-qr-extract"></text>
              </view>
              <view class="scan-text">
                  <view class="scan-title">扫码点餐</view>
                  <view class="scan-desc">扫描桌码二维码，快速点餐</view>
              </view>
              <view class="scan-arrow">
                  <text class="iconfont icon-xiangyoujiantou"></text>
              </view>
          </view>
          <HomeService v-if="storeInfo" :data="[]"/>
          <HomeNav v-if="storeInfo" :navigation="navigation"/>
          <HomeAds v-if="storeInfo" :ads="ads"/>
      </block>
      <!-- 自定义 tabBar 占位 -->
      <view class="tabbar-safe-area"></view>
      <!-- #ifdef H5 -->
      <h5-tabbar ref="h5Tabbar"></h5-tabbar>
      <!-- #endif -->
  </view>
</template>

<script>
  import { setCartTabBadge, showMessage } from '@/utils/app'
  import Empty from '@/components/empty'
  import HomeBanner from "./components/HomeBanner.vue"
  import HomeService from "./components/HomeService.vue"
  import HomeUser from "./components/HomeUser.vue"
  import HomeNav from "./components/HomeNav.vue"
  import HomeAds from "./components/HomeAds.vue"
  import Page from '@/components/page'
  import * as settingApi from '@/api/setting'
  import * as Api from '@/api/page'
  import * as UserApi from '@/api/user'
  import MescrollCompMixin from "@/components/mescroll-uni/mixins/mescroll-comp.js";
  import config from '@/config'
  import { loadAndApplyTabbar } from '@/utils/tabbar'
  import { loadTheme } from '@/utils/theme'
  import { preloadWxSdk, scanTableCode } from '@/utils/scanOrder'
  // #ifdef H5
  import H5Tabbar from '@/components/tabbar/index.vue'
  // #endif

  const App = getApp()
  
  export default {
    mixins: [MescrollCompMixin],
    components: {
       Empty,
       HomeBanner,
       HomeService,
       HomeUser,
       HomeNav,
       HomeAds,
       Page,
       // #ifdef H5
       H5Tabbar
       // #endif
    },
    data() {
      return {
        banner: [],
        ads: [],
        storeInfo: null,
        userInfo: {},
        isReflash: false,
        isLoading: false,
        navigation: [],
        // 页面装修组件列表（后台装修页面配置，有值时首页整体切换为装修渲染）
        pageItems: [],
        // 上传图片根路径（后端 home 接口返回），用于补全装修组件数据中的相对图片路径
        imagePath: '',
        // 页面装修数据是否已请求过（首次进入、或切换店铺后重新拉取）
        pageLoaded: false,
        // 页面装修数据请求进行中标识（防止重复请求）
        pageLoading: false,
        // 门店信息请求进行中标识（防止重复请求）
        storeFetching: false,
        // 链接显式指定的门店ID（扫码切店场景），用于判断指定门店是否可用
        requestedStoreId: 0,
        // 切换门店后需强制刷新 tabBar 配置（绕过本地 5 分钟缓存）
        tabbarForce: false
      }
    },

    /**
     * 生命周期函数--监听页面加载
     */
    onLoad({ storeId, tableId }) {
      storeId = storeId ? parseInt(storeId) : 0;
      tableId = tableId ? parseInt(tableId) : 0;
      if (storeId > 0) {
          const prevStoreId = parseInt(uni.getStorageSync('storeId') || 0);
          // 记录链接显式指定的门店，后端在其不可用时回退到其他门店，前端据此提示
          this.requestedStoreId = storeId;
          uni.setStorageSync('storeId', storeId);
          uni.setStorageSync("reflashHomeData", true);
          if (prevStoreId !== storeId) {
              // 切换门店：清除上一家门店残留的桌码，避免下单挂到别的门店的桌上
              // （tableId 写入后无清理时机，会长期留存；后端也已改为 storeId 优先）
              if (tableId <= 0) {
                  uni.removeStorageSync('tableId');
              }
              // 门店相关的主题/tabBar 缓存按门店分片，换店后强制刷新
              this.tabbarForce = true;
              // 立即拉取新门店主题（此时 header 已带新 storeId），后续 onShow 会复用该请求
              loadTheme(true);
          }
      } else {
          this.getPageData();
      }
      // #ifdef H5
      preloadWxSdk();
      // #endif
    },

    /**
     * 生命周期函数--监听页面显示
     */
    onShow() {
      const app = this;
      // 拉取 tabBar 装修配置（缓存优先），自定义 tabBar 实例可能尚未就绪会自动重试
      // 切换门店后强制刷新，避免沿用上一家门店的缓存配置
      loadAndApplyTabbar(this, this.tabbarForce)
      this.tabbarForce = false
      // #ifdef H5
      this.$refs.h5Tabbar && this.$refs.h5Tabbar.refresh()
      // #endif
      // #ifdef MP-WEIXIN
      // 微信注入的 getTabBar 挂在原生页面实例上，uni-app 需经 $scope 访问
      const host = this.$scope || this
      const tb = typeof host.getTabBar === 'function' && host.getTabBar()
      tb && tb.syncSelected && tb.syncSelected()
      // #endif
      showMessage();
      setCartTabBadge();
      app.onGetStoreInfo();
      app.getUserInfo();
      uni.getLocation({
          type: 'gcj02',
          success(res){
              uni.setStorageSync('latitude', res.latitude);
              uni.setStorageSync('longitude', res.longitude);
              app.onGetStoreInfo();
          },
          fail(e) {
             // empty
          }
      })
    },

    methods: {
        
        /**
         * 加载页面数据
         * @param {Object} callback
         */
        getPageData(callback) {
          const app = this;
          if (app.pageLoading) {
              return;
          }
          app.pageLoading = true;
          Api.home()
            .then(result => {
                 app.imagePath = result.data.imagePath || app.imagePath || '';
                 // 优先使用后台装修配置
                 if (result.data.page && result.data.page.components && result.data.page.components.length > 0) {
                     app.pageItems = result.data.page.components;
                 } else {
                     // 未装修时回退默认点餐数据（兼容现网首页）
                     app.pageItems = [];
                     app.banner = result.data.banner;
                     app.ads = result.data.ads;
                     app.navigation = result.data.navigation;
                 }
                 uni.removeStorageSync("reflashHomeData");
                 app.isReflash = false;
            })
            .finally(() => {
                 // 无论成功失败都标记为已加载，避免后续 onShow 重复请求
                 app.pageLoading = false;
                 app.pageLoaded = true;
                 callback && callback()
            })
        },
        
        /**
         * 获取用户信息
         * */
        getUserInfo() {
          const app = this;
          UserApi.info()
            .then(result => {
              app.userInfo = result.data.userInfo ? result.data.userInfo : {};
            })
        },
        
        /**
         * 下拉刷新
         */
        onPullDownRefresh() {
          // 获取数据
          this.getUserInfo();
          this.getPageData(() => {
             uni.stopPullDownRefresh()
          })
        },
        
        /**
         * 扫码点餐
         */
        onScanCode() {
            scanTableCode(this)
        },

        /**
         * 获取默认店铺
         * */
         onGetStoreInfo() {
            const app = this;
            if (app.storeFetching) {
                return;
            }
            app.storeFetching = true;
            const prevMerchantNo = uni.getStorageSync('merchantNo') || '';
            const prevStoreId = parseInt(uni.getStorageSync('storeId') || 0);
            settingApi.systemConfig()
             .then(result => {
                 app.storeInfo = result.data.storeInfo;
                 // 扫码指定的门店不存在或已停用时，后端已回退到其他门店
                 // （跨商户切店属于正常场景，不在此列）
                 if (result.data.storeUnavailable && app.requestedStoreId > 0) {
                     // 置空避免 onShow 重复触发时反复提示
                     app.requestedStoreId = 0;
                     uni.showToast({
                         title: '该门店暂不可用，已为您切换到其他门店',
                         icon: 'none',
                         duration: 3000
                     });
                 }
                 if (app.storeInfo) {
                     const merchantNo = app.storeInfo.merchantNo || '';
                     // 首次进入时无历史商户/门店，不算切换，无需重复拉取
                     const isFirst = !prevMerchantNo && prevStoreId <= 0;
                     const switched = !isFirst && (merchantNo !== prevMerchantNo || app.storeInfo.id !== prevStoreId);
                     uni.setStorageSync("storeId", app.storeInfo.id);
                     uni.setStorageSync("merchantNo", merchantNo);
                     if (switched) {
                         // 商户或门店已变更（跨商户切店、或指定门店不可用被回退）：
                         // 主题与 tabBar 均由后端按 merchantId+storeId 下发，
                         // 必须在 merchantNo 生效后重新拉取，否则仍是上一家的配色
                         // 主题先行刷新：tabBar 的选中色依赖当前主题色，
                         // 否则会用上一家门店的配色渲染导航
                         app.refreshTheme(true).then(() => {
                             loadAndApplyTabbar(app, true);
                             // #ifdef H5
                             app.$refs.h5Tabbar && app.$refs.h5Tabbar.refresh(true);
                             // #endif
                         });
                     }
                     // 判断是否需要更新页面
                     let isReflash = uni.getStorageSync("reflashHomeData");
                     app.isReflash = isReflash;
                     if (isReflash === true || !app.pageLoaded) {
                         app.getPageData();
                     }
                 }
             })
             .finally(() => {
                 app.storeFetching = false;
             })
         }
    },

    /**
     * 分享当前页面
     */
    onShareAppMessage() {
      const app = this
      return {
         title: config.name,
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
        title: config.name,
        path: "/pages/index/index?" + app.$getShareUrlParams()
      }
    }

  }
</script>
<style lang="scss" scoped>
    .scan-entry {
        display: flex;
        align-items: center;
        margin: 0 10rpx 25rpx 10rpx;
        padding: 30rpx;
        background: linear-gradient(135deg, $fuint-theme, #ff9f7d);
        border-radius: 16rpx;
        box-shadow: 0 4rpx 16rpx rgba(255, 100, 50, 0.25);
        
        .scan-icon {
            width: 80rpx;
            height: 80rpx;
            background: rgba(255, 255, 255, 0.25);
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            margin-right: 24rpx;
            flex-shrink: 0;
            
            .iconfont {
                font-size: 44rpx;
                color: #fff;
            }
        }
        
        .scan-text {
            flex: 1;
            
            .scan-title {
                font-size: 32rpx;
                font-weight: bold;
                color: #fff;
            }
            
            .scan-desc {
                font-size: 24rpx;
                color: rgba(255, 255, 255, 0.8);
                margin-top: 4rpx;
            }
        }
        
        .scan-arrow {
            flex-shrink: 0;
            margin-left: 16rpx;
            
            .iconfont {
                font-size: 32rpx;
                color: rgba(255, 255, 255, 0.6);
            }
        }
    }
</style>
