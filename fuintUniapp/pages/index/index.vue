<template>
  <view class="container">
      <empty v-if="!storeInfo" :isLoading="isLoading" tips="数据加载中..."></empty>
      <block>
          <HomeBanner v-if="storeInfo" :banners="banner"/>
      </block>
      <block>
          <HomeUser v-if="storeInfo" :userInfo="userInfo"/>
      </block>
      <block>
          <HomeService v-if="storeInfo" :data="[]"/>
      </block>
      <block>
          <HomeNav v-if="storeInfo" :ads="[]"/>
      </block>
      <block>
          <HomeAds v-if="storeInfo" :ads="ads"/>
      </block>
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
  import * as settingApi from '@/api/setting'
  import * as Api from '@/api/page'
  import * as UserApi from '@/api/user'
  import MescrollCompMixin from "@/components/mescroll-uni/mixins/mescroll-comp.js";

  const App = getApp()
  
  export default {
    mixins: [MescrollCompMixin],
    components: {
       Empty,
       HomeBanner,
       HomeService,
       HomeUser,
       HomeNav,
       HomeAds
    },
    data() {
      return {
        banner: [],
        ads: [],
        storeInfo: null,
        userInfo: null,
        isReflash: false,
        isLoading: false
      }
    },

    /**
     * 生命周期函数--监听页面加载
     */
    onLoad({ storeId }) {
      storeId = storeId ? parseInt(storeId) : 0;
      if (storeId > 0) {
          uni.setStorageSync('storeId', storeId);
          uni.setStorageSync("reflashHomeData", true);
      } else {
          this.getPageData();
      }
    },

    /**
     * 生命周期函数--监听页面显示
     */
    onShow() {
      const app = this;
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
          Api.home()
            .then(result => {
                 app.banner = result.data.banner;
                 app.ads = result.data.ads;
                 uni.removeStorageSync("reflashHomeData");
                 app.isReflash = false;
            })
            .finally(() => callback && callback())
        },
        
        /**
         * 获取用户信息
         * */
        getUserInfo() {
          const app = this;
          UserApi.info()
            .then(result => {
              app.userInfo = result.data.userInfo;
            })
        },
        
        /**
         * 下拉刷新
         */
        onPullDownRefresh() {
          // 获取数据
          this.getPageData(() => {
             uni.stopPullDownRefresh()
          })
        },
        
        /**
         * 获取默认店铺
         * */
         onGetStoreInfo() {
            const app = this;
            settingApi.systemConfig()
             .then(result => {
                 app.storeInfo = result.data.storeInfo;
                 if (app.storeInfo) {
                     uni.setStorageSync("storeId", app.storeInfo.id);
                     uni.setStorageSync("merchantNo", app.storeInfo.merchantNo);
                     // 判断是否需要更新页面
                     let isReflash = uni.getStorageSync("reflashHomeData");
                     app.isReflash = isReflash;
                     if (isReflash === true) {
                         app.getPageData();
                     }
                 }
             })
         }
    },

    /**
     * 分享当前页面
     */
    onShareAppMessage() {
      const app = this
      return {
         title: "fuint会员系统",
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
