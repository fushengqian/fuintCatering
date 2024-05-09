<template>
  <view v-show="!isLoading" class="container">
    <!-- 卡券信息 -->
    <view v-if="!isLoading" class="coupon-info m-top20">
      <!-- 标题、分享 -->
      <view class="info-item info-item__name dis-flex flex-y-center">
        <view class="coupon-name flex-box">
          <text class="twolist-hidden">{{ couponInfo.name }}</text>
        </view>
        <!-- #ifdef MP-WEIXIN -->
        <view class="coupon-share__line"></view>
        <view class="coupon-share">
          <button class="share-btn dis-flex flex-dir-column" open-type="share">
            <text class="share__icon iconfont icon-fenxiang"></text>
            <text class="f-24">分享</text>
          </button>
        </view>
        <!-- #endif -->
      </view>
      <!-- 卡券预存规则 -->
      <view class="store-rule">
          <view class="title">预存规则：</view>
          <view v-for="(item, index) in storeRule" :key="index" class="item">
            <text>预存￥{{ item.store }} 到账 ￥{{ item.upStore }}</text>
          </view>
      </view>
      <view class="info-item">
          <text>已有<text class="number">{{ couponInfo.gotNum }}</text>人预存，剩余<text class="number">{{ couponInfo.limitNum }}</text>名额</text>
      </view>
      <view class="info-item">
          <text>有效期：{{ couponInfo.effectiveDate }}</text>
      </view>
    </view>

    <!-- 选择卡券规格 -->
    <view class="coupon-choice m-top20 b-f" @click="onShowPopup()">
      <view class="spec-list">
        <view class="flex-box">
          <text class="col-8">选择：</text>
          <text class="spec-name" key="index">预存金额、数量</text>
        </view>
        <view class="f-26 col-9 t-r">
          <text class="iconfont icon-xiangyoujiantou"></text>
        </view>
      </view>
    </view>

    <!-- 预存选项弹窗 -->
    <Popup v-if="!isLoading" v-model="showPopup" :couponInfo="couponInfo" :storeRule="storeRule"/>

    <!-- 卡券描述 -->
    <view v-if="!isLoading" class="coupon-content m-top20">
      <view class="item-title b-f">
        <text>卡券描述</text>
      </view>
      <block v-if="couponInfo.description != ''">
        <view class="coupon-content-detail b-f">
          <jyf-parser :html="couponInfo.description"></jyf-parser>
        </view>
      </block>
      <empty v-else tips="亲，暂无卡券描述" />
    </view>

    <!-- 底部选项卡 -->
    <view class="footer-fixed">
      <view class="footer-container">
        <!-- 导航图标 -->
        <view class="foo-item-fast">
          <!-- 客服 (仅微信小程序端显示) -->
          <!-- #ifdef MP-WEIXIN -->
          <button class="btn-normal" open-type="contact">
            <view class="fast-item">
              <view class="fast-icon">
                <text class="iconfont icon-kefu1"></text>
              </view>
              <view class="fast-text">
                <text>客服</text>
              </view>
            </view>
          </button>
          <!-- #endif -->
        </view>
        <!-- 操作按钮 -->
        <view class="foo-item-btn">
          <view class="btn-wrapper">
            <view class="btn-item btn-item-main" @click="onShowPopup()">
              <text>立即预存</text>
            </view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
  import * as CouponApi from '@/api/coupon'
  import jyfParser from '@/components/jyf-parser/jyf-parser'
  import Shortcut from '@/components/shortcut'
  import Popup from './components/Popup'

  export default {
    components: {
      jyfParser,
      Shortcut,
      Popup
    },
    data() {
      return {
        // 正在加载
        isLoading: true,
        // 当前卡券ID
        couponId: null,
        // 卡券详情
        couponInfo: null,
        // 预存规则
        storeRule: [],
        // 显示/隐藏弹窗
        showPopup: false
      }
    },

    /**
     * 生命周期函数--监听页面加载
     */
    onLoad(options) {
      // 卡券ID
      this.couponId = parseInt(options.couponId)
      // 加载页面数据
      this.onRefreshPage()
    },

    methods: {

      // 刷新页面数据
      onRefreshPage() {
        const app = this
        app.isLoading = true
        Promise.all([app.getCouponDetail()])
          .finally(() => app.isLoading = false)
      },

      // 获取卡券信息
      getCouponDetail() {
        const app = this
        return new Promise((resolve, reject) => {
          CouponApi.detail(app.couponId)
            .then(result => {
              app.couponInfo = result.data
              let ruleItem = app.couponInfo.inRule.split(",");
              ruleItem.forEach(function(item) {
                  let rule = item.split("_")
                  app.storeRule.push({"store": rule[0], "upStore": rule[1]})
              })
              resolve(result)
            })
            .catch(err => reject(err))
        })
      },

      /**
       * 显示/隐藏预存弹窗
       */
      onShowPopup() {
        this.showPopup = !this.showPopup
      },

      // 跳转到首页
      onTargetHome(e) {
        this.$navTo('pages/index/index')
      }
      
    },

    /**
     * 分享当前页面
     */
    onShareAppMessage() {
      const app = this
      // 构建页面参数
      const params = app.$getShareUrlParams({
        couponId: app.couponId
      })
      return {
        title: app.couponInfo.name,
        path: `/pages/prestore/buy?${params}`
      }
    },

    /**
     * 分享到朋友圈
     * 本接口为 Beta 版本，暂只在 Android 平台支持，详见分享到朋友圈 (Beta)
     * https://developers.weixin.qq.com/miniprogram/dev/framework/open-ability/share-timeline.html
     */
    onShareTimeline() {
      const app = this
      // 构建页面参数
      const params = app.$getShareUrlParams({
        couponId: app.couponId,
      })
      return {
        title: app.couponInfo.name,
        path: `/pages/prestore/buy?${params}`
      }
    }
  }
</script>

<style>
  page {
    background: #fafafa;
  }
</style>
<style lang="scss" scoped>
  @import "./buy.scss";
</style>
