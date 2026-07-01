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
      </block>
      <block>
          <HomeService v-if="storeInfo" :data="[]"/>
      </block>
      <block>
          <HomeNav v-if="storeInfo" :navigation="navigation"/>
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
        userInfo: {},
        isReflash: false,
        isLoading: false,
        navigation: []
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
                 app.navigation = result.data.navigation;
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
            const app = this;
            // #ifdef MP-WEIXIN
            uni.scanCode({
                scanType: ['qrCode'],
                success(res) {
                    app.handleScanResult(res.result);
                },
                fail(err) {
                    if (err.errMsg !== 'scanCode:fail cancel') {
                        uni.showToast({
                            title: '扫码失败，请重试',
                            icon: 'none'
                        });
                    }
                }
            });
            // #endif
            // #ifdef H5
            const ua = navigator.userAgent.toLowerCase();
            if (ua.indexOf('micromessenger') === -1) {
                uni.showToast({ title: '请在微信中扫码', icon: 'none' });
                return;
            }
            app.loadWxJsSdk(() => {
                const url = window.location.href.split('#')[0];
                // #ifdef H5
                console.log('[扫码点餐] 请求JSSDK配置, url:', url);
                // #endif
                settingApi.jsSdkConfig(url).then(result => {
                    // #ifdef H5
                    console.log('[扫码点餐] JSSDK配置:', JSON.stringify(result));
                    // #endif
                    const config = result.data;
                    if (!config || !config.appId) {
                        uni.showToast({
                            title: '公众号AppID未配置，请联系管理员',
                            icon: 'none',
                            duration: 3000
                        });
                        return;
                    }
                    wx.config({
                        debug: false,
                        appId: config.appId,
                        timestamp: config.timestamp,
                        nonceStr: config.nonceStr,
                        signature: config.signature,
                        jsApiList: ['scanQRCode']
                    });
                    wx.ready(() => {
                        wx.scanQRCode({
                            needResult: 1,
                            scanType: ['qrCode'],
                            success(res) {
                                app.handleScanResult(res.resultStr);
                            },
                            fail() {
                                uni.showToast({
                                    title: '扫码失败，请重试',
                                    icon: 'none'
                                });
                            }
                        });
                    });
                    wx.error((err) => {
                        // #ifdef H5
                        console.log('[扫码点餐] wx.error:', JSON.stringify(err));
                        // #endif
                        uni.showToast({
                            title: '微信配置失败，请重试',
                            icon: 'none'
                        });
                    });
                }).catch((err) => {
                    // #ifdef H5
                    console.log('[扫码点餐] 请求失败:', JSON.stringify(err));
                    // #endif
                    uni.showToast({
                        title: '获取配置失败',
                        icon: 'none'
                    });
                });
            });
            // #endif
        },

        /**
         * 处理扫码结果（提取tableId）
         */
        handleScanResult(result) {
            const app = this;
            let tableId = 0;
            if (/^\d+$/.test(result)) {
                tableId = parseInt(result);
            } else {
                const match = result.match(/[?&]tableId=(\d+)/);
                if (match) {
                    tableId = parseInt(match[1]);
                }
            }
            if (tableId > 0) {
                uni.setStorageSync('tableId', tableId);
                app.$navTo('pages/category/index', { tableId: tableId });
            } else {
                uni.showToast({
                    title: '无效的桌码二维码',
                    icon: 'none'
                });
            }
        },

        /**
         * 动态加载微信JSSDK
         */
        loadWxJsSdk(callback) {
            if (window.wx) {
                callback();
                return;
            }
            const script = document.createElement('script');
            script.src = 'https://res.wx.qq.com/open/js/jweixin-1.6.0.js';
            script.onload = callback;
            script.onerror = () => {
                uni.showToast({
                    title: '加载微信SDK失败',
                    icon: 'none'
                });
            };
            document.head.appendChild(script);
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
         title: "fuint点餐系统",
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
