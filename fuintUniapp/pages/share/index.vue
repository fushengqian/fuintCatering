<template>
  <mescroll-body ref="mescrollRef" :sticky="true" @init="mescrollInit" :down="{ use: false }" :up="upOption" @up="upCallback">
    <!-- 分享头部区域 -->
    <view class="share-header">
      <view class="header-bg">
        <view class="header-content">
          <text class="header-icon iconfont icon-gift"></text>
          <view class="header-text">
            <text class="header-title">邀请好友 赚取佣金</text>
            <text class="header-desc">每邀请一位好友注册消费，即可获得分佣提成</text>
          </view>
        </view>
      </view>

      <!-- 三个操作按钮 -->
      <view class="action-buttons">
        <view class="action-item" @click="generatePoster">
          <view class="action-icon poster-icon">
            <view class="icon-symbol">图</view>
          </view>
          <text class="action-label">生成海报</text>
        </view>
        <view class="action-item" @click="generateMiniLink">
          <view class="action-icon link-icon">
            <view class="icon-symbol">链</view>
          </view>
          <text class="action-label">复制链接</text>
        </view>

      </view>
    </view>

    <!-- 分享对话框 -->
    <u-popup v-model="shareShow" mode="bottom" border-radius="14" height="453">
      <view class="share-popup">
        <text class="share-title">邀请确认</text>
        <view class="row">
          <view class="col-6" @click="doShare('share')">
            <button class="mt-1" open-type="share">
              <view class="iconfont icon-fenxiang-post"></view>
              <text class="txt">立即邀请</text>
            </button>
          </view>
          <view class="col-6" @click="doShare('copy')">
            <button class="mt-1">
              <view class="iconfont icon-copy"></view>
              <text class="mt-1">复制链接</text>
            </button>
          </view>
        </view>
        <view class="share-btn" @click="shareShow = false">
          <view class="btn-wrapper">
            <view class="btn-item">取消邀请</view>
          </view>
        </view>
      </view>
    </u-popup>

    <!-- 生成海报对话框 -->
    <poster-img :img-show.sync="showPoster" :share-query="shareQuery" :h5-url="url" v-if="showPoster"></poster-img>

    <!-- 邀请记录标题 -->
    <view class="title-wrapper">
      <view class="title">邀请记录</view>
    </view>

    <!-- 邀请列表 -->
    <view class="share-list">
      <view class="share-item show-type" v-for="(item, index) in list.content" :key="index" @click="onTargetDetail(item.id)">
        <block>
          <view class="share-item-left flex-box">
            <view class="share-item-image">
              <image class="image" :src="item.subUserInfo.avatar"></image>
            </view>
            <view class="share-item-title twolist-hidden">
              <view class="name">{{ item.subUserInfo.name }}</view>
              <view class="no">会员号：{{ item.subUserInfo.userNo }}</view>
            </view>
            <view class="share-item-footer m-top10">
              <text class="share-views f-24 col-8">邀请时间：{{ item.createTime | timeFormat('yyyy-mm-dd hh:MM') }}</text>
            </view>
          </view>
        </block>
      </view>
    </view>
  </mescroll-body>
</template>

<script>
  import MescrollBody from '@/components/mescroll-uni/mescroll-body.vue'
  import MescrollMixin from '@/components/mescroll-uni/mescroll-mixins'
  import PosterImg from '@/components/poster-img/index.vue'
  import * as ShareApi from '@/api/share'
  import { getEmptyPaginateObj, getMoreListData } from '@/utils/app'
  import config from '@/config'

  const pageSize = 15

  export default {
    components: {
      MescrollBody,
      PosterImg
    },
    mixins: [MescrollMixin],
    data() {
      return {
        // h5
        url: '',
        // 小程序链接
        miniLink: '',
        // 立即分享
        shareShow: false,
        // 生成海报
        showPoster: false,
        // 邀请列表
        list: getEmptyPaginateObj(),
        // 上拉加载配置
        upOption: {
          auto: true,
          page: { size: pageSize },
          noMoreSize: 3,
        }
      }
    },

    computed: {
      shareQuery() {
        return this.$getShareUrlParams ? this.$getShareUrlParams() : ''
      }
    },

    methods: {

      /**
       * 上拉加载的回调
       */
      upCallback(page) {
        const app = this;
        app.getShareList(page.num)
          .then(list => {
            const curPageLen = list.content.length;
            const totalSize = list.content.totalElements;
            app.mescroll.endBySize(curPageLen, totalSize);
          })
          .catch(() => app.mescroll.endErr());
      },

      /**
       * 获取邀请列表
       */
      getShareList(pageNo = 1) {
        const app = this
        return new Promise((resolve, reject) => {
          ShareApi.list({ page: pageNo }, { load: false })
            .then(result => {
              const newList = result.data.paginationResponse;
              app.url = result.data.url;
              app.list.content = getMoreListData(newList, app.list, pageNo);
              resolve(newList);
            })
            .catch(result => reject());
        })
      },

      /**
       * 生成海报
       */
      generatePoster() {
        this.showPoster = true
      },

      /**
       * 生成小程序链接
       */
      generateMiniLink() {
        const app = this
        // 如果已经生成过，通过弹窗确认复制（保证在用户手势上下文中调用 setClipboardData）
        if (app.miniLink) {
          app.showCopyModal(app.miniLink)
          return
        }
        uni.showLoading({ title: '生成中...' })
        // #ifdef MP-WEIXIN
        ShareApi.getMiniAppLink({
          path: 'pages/index/index',
          query: app.$getShareUrlParams()
        }).then(res => {
          uni.hideLoading()
          const link = res && res.data && res.data.link ? res.data.link : ''
          if (link) {
            app.miniLink = link
            // 在异步回调中不能直接调 setClipboardData，用弹窗让用户点击复制
            app.showCopyModal(app.miniLink)
          } else {
            uni.showToast({ title: '生成链接失败', icon: 'none' })
          }
        }).catch(() => {
          uni.hideLoading()
          uni.showToast({ title: '生成链接失败', icon: 'none' })
        })
        // #endif
        // #ifndef MP-WEIXIN
        uni.hideLoading()
        // H5 端使用网页链接
        const h5Link = app.url + '#?' + app.$getShareUrlParams()
        app.copyMiniLink(h5Link)
        // #endif
      },

      /**
       * 显示复制确认弹窗（解决异步回调中 setClipboardData 手势上下文丢失问题）
       */
      showCopyModal(link) {
        const app = this
        uni.showModal({
          title: '小程序链接',
          content: '点击复制可将链接发送给好友，好友在微信中点击即可打开小程序',
          confirmText: '复制链接',
          cancelText: '关闭',
          success(res) {
            if (res.confirm) {
              // 用户在弹窗中点击按钮，处于新的手势上下文，setClipboardData 可正常调用
              app.copyMiniLink(link)
            }
          }
        })
      },

      /**
       * 复制小程序链接
       */
      copyMiniLink(link) {
        uni.setClipboardData({
          data: link,
          success: function() {
            uni.showToast({
              title: '链接已复制，可发给好友',
              icon: 'none'
            })
          },
          fail: (err) => {
            console.error('setClipboardData fail:', err)
            // #ifdef MP-WEIXIN
            if (wx && wx.setClipboardData) {
              wx.setClipboardData({
                data: link,
                success() {
                  uni.showToast({ title: '链接已复制', icon: 'none' })
                },
                fail() {
                  uni.showToast({ title: '复制失败，请重试', icon: 'none' })
                }
              })
              return
            }
            // #endif
            uni.showToast({ title: '复制失败，请重试', icon: 'none' })
          }
        })
      },

      /**
       * 立即邀请
       */
      shareNow() {
        this.shareShow = true;
      },

      /**
       * 确认分享
       */
      doShare(action) {
        const app = this;
        app.shareShow = false;
        let copyContent = app.url + '#?' + app.$getShareUrlParams();
        if (action == 'copy') {
          // #ifdef MP-WEIXIN
          ShareApi.getMiniAppLink({
            path: 'pages/index/index',
            query: app.$getShareUrlParams()
          }).then(res => {
            const miniLink = res && res.data && res.data.link ? res.data.link : ''
            let content = miniLink || copyContent
            // 异步回调中不能用 setClipboardData，改用弹窗让用户点击复制
            app.showCopyModal(content)
          }).catch(() => {
            // 获取链接失败，用 H5 链接作为兜底
            app.showCopyModal(copyContent)
          })
          // #endif
          // #ifndef MP-WEIXIN
          app.copyMiniLink(copyContent)
          // #endif
        }
      },

      /**
       * 分享当前页面
       */
      onShareAppMessage() {
        const app = this
        return {
          title: config.name,
          path: '/pages/index/index?' + app.$getShareUrlParams()
        }
      },

      /**
       * 分享到朋友圈
       */
      onShareTimeline() {
        const app = this
        return {
          title: config.name,
          path: '/pages/index/index?' + app.$getShareUrlParams()
        }
      }
    }
  }
</script>

<style lang="scss" scoped>
  /* 分享头部 */
  .share-header {
    background: #fff;
  }

  .header-bg {
    background: linear-gradient(135deg, #113a28, #2ab075);
    padding: 40rpx 30rpx;
    position: relative;
    overflow: hidden;

    &::after {
      content: '';
      position: absolute;
      top: -60rpx;
      right: -40rpx;
      width: 200rpx;
      height: 200rpx;
      background: rgba(255, 255, 255, 0.1);
      border-radius: 50%;
    }
  }

  .header-content {
    display: flex;
    align-items: center;
    position: relative;
    z-index: 1;
  }

  .header-icon {
    font-size: 80rpx;
    color: rgba(255, 255, 255, 0.9);
    margin-right: 24rpx;
  }

  .header-text {
    display: flex;
    flex-direction: column;
  }

  .header-title {
    font-size: 36rpx;
    font-weight: bold;
    color: #fff;
    margin-bottom: 10rpx;
  }

  .header-desc {
    font-size: 24rpx;
    color: rgba(255, 255, 255, 0.8);
    line-height: 1.5;
  }

  /* 两个操作按钮 */
  .action-buttons {
    display: flex;
    align-items: center;
    padding: 60rpx 20rpx;
    background: #fff;
    box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
  }

  .action-item {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
  }

  .action-icon {
    width: 90rpx;
    height: 90rpx;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 16rpx;

    .iconfont {
      font-size: 42rpx;
      color: #fff;
    }

    .icon-symbol {
      font-size: 36rpx;
      color: #fff;
      font-weight: bold;
    }
  }

  .poster-icon {
    background: linear-gradient(135deg, #ff8a65, #ff6e40);
  }

  .link-icon {
    background: linear-gradient(135deg, #5b86e5, #36d1dc);
  }

  .share-icon {
    background: linear-gradient(135deg, #11998e, #38ef7d);
  }

  .action-label {
    font-size: 26rpx;
    color: #333;
    font-weight: 500;
  }

  /* 分享对话框 */
  .share-popup {
    padding: 20rpx;
    font-size: 30rpx;
    text-align: center;
    .share-title {
      margin-top: 40rpx;
      font-size: 35rpx;
      font-weight: bold;
    }
    .row {
      margin-top: 30rpx;
      padding-top: 80rpx;
      clear: both;
      height: 240rpx;
      .col-6 {
        width: 50%;
        float: left;
        .mt-1 {
          line-height: 60rpx;
          font-size: 24rpx;
          background: none;
        }
        .iconfont {
          color: #113a28;
          font-size: 56rpx;
        }
      }
    }
    .share-btn {
      text-align: center;
      .btn-wrapper {
        height: 100%;
        display: block;
        align-items: center;
        width: 80%;
        margin: 0 auto;
        border: solid 2rpx #ccc;
        border-radius: 80rpx;
        .btn-item {
          font-size: 28rpx;
          height: 80rpx;
          line-height: 80rpx;
          text-align: center;
          color: #333;
        }
      }
    }
  }

  /* 邀请记录标题 */
  .title-wrapper {
    display: flex;
    width: 100%;
    height: 88rpx;
    line-height: 88rpx;
    color: #333;
    padding-left: 20rpx;
    font-size: 30rpx;
    font-weight: bold;
    background: #fff;
    margin-top: 20rpx;
    border-bottom: 1rpx solid #e4e4e4;
    z-index: 100;
    overflow: hidden;
    white-space: nowrap;
  }

  /* 分享列表 */
  .share-list {
    padding-top: 10rpx;
    line-height: 1;
    background: #f7f7f7;
  }

  .share-item {
    margin-bottom: 10rpx;
    padding: 20rpx;
    background: #fff;

    &:last-child {
      margin-bottom: 0;
    }

    .share-item-title {
      max-height: 80rpx;
      font-size: 24rpx;
      color: #333;
      .name {
        font-weight: bold;
      }
      .no {
        margin-top: 20rpx;
        font-size: 20rpx;
        color: #888888;
      }
    }

    .share-item-image .image {
      display: block;
      border-radius: 100rpx;
      height: 100rpx;
      width: 100rpx;
      border: 2rpx solid #cccccc;
      float: left;
      background: #ccc;
    }
    .share-item-footer {
      margin-top: 0rpx;
      clear: both;
      text-align: right;
    }
  }

  .show-type {
    display: flex;
    .share-item-left {
      padding-right: 20rpx;
      width: 10rpx;
    }
    .share-item-title {
      min-height: 100rpx;
      float: left;
      margin-left: 10rpx;
    }
  }
</style>
