<template>
  <view v-show="!isLoading" class="container">
    <!-- 商品图片轮播 -->
    <SlideImage v-if="!isLoading" :images="goods.images" />

    <!-- 商品信息 -->
    <view v-if="!isLoading" class="goods-info m-top20">
      <!-- 价格、销量 -->
      <view class="info-item info-item__top dis-flex flex-x-between flex-y-end">
        <view class="block-left dis-flex flex-y-end">
          <!-- 商品售价 -->
          <text class="floor-price__samll">￥</text>
          <text class="floor-price">{{ goods.price }}</text>
          <!-- 划线价 -->
          <text class="original-price">￥{{ goods.linePrice }}</text>
        </view>
        <view class="block-right dis-flex">
          <!-- 销量 -->
          <view class="goods-sales">
            <text>已销售{{ goods.initSale }}</text>
          </view>
        </view>
      </view>
      <!-- 标题、分享 -->
      <view class="info-item info-item__name dis-flex flex-y-center">
        <view class="goods-name flex-box">
          <text class="twolist-hidden">{{ goods.name }}</text>
        </view>
        <!-- #ifdef MP-WEIXIN -->
        <view class="goods-share__line"></view>
        <view class="goods-share">
          <button class="share-btn dis-flex flex-dir-column" open-type="share">
            <text class="share__icon iconfont icon-fenxiang"></text>
            <text class="f-24">分享</text>
          </button>
        </view>
        <!-- #endif -->
      </view>
      <!-- 商品卖点 -->
      <view v-if="goods.salePoint" class="info-item info-item_selling-point">
        <text>{{ goods.salePoint }}</text>
      </view>
    </view>

    <!-- 选择商品规格 -->
    <view v-if="goods.isSingleSpec == 'N'" class="goods-choice m-top20 b-f" @click="onShowSkuPopup(3)">
      <view class="spec-list">
        <view class="flex-box">
          <text class="col-8">选择：</text>
          <text class="spec-name" v-for="(item, index) in goods.specList" :key="index">{{ item.name }}</text>
        </view>
        <view class="f-26 col-9 t-r">
          <text class="iconfont icon-xiangyoujiantou"></text>
        </view>
      </view>
    </view>

    <!-- 商品SKU弹窗 -->
    <SkuPopup v-if="!isLoading" v-model="showSkuPopup" :skuMode="skuMode" :goods="goods" @addCart="onAddCart"/>

    <!-- 商品描述 -->
    <view v-if="!isLoading" class="goods-content m-top20">
      <view class="item-title b-f">
        <text>商品详情</text>
      </view>
      <block v-if="goods.description != ''">
        <view class="goods-content-detail b-f">
          <jyf-parser :html="goods.description"></jyf-parser>
        </view>
      </block>
      <empty v-else tips="亲，暂无商品描述" />
    </view>

    <!-- 底部选项卡 -->
    <view class="footer-fixed">
      <view class="footer-container">
        <!-- 导航图标 -->
        <view class="foo-item-fast">
          <!-- 首页 -->
          <view class="fast-item fast-item--home" @click="onTargetHome">
            <view class="fast-icon">
              <text class="iconfont icon-shouye"></text>
            </view>
            <view class="fast-text">
              <text>首页</text>
            </view>
          </view>
          <!-- 购物车-->
          <view class="fast-item fast-item--cart" @click="onTargetCart">
            <view v-if="cartTotal > 0" class="fast-badge fast-badge--fixed">{{ cartTotal > 99 ? '99+' : cartTotal }}</view>
            <view class="fast-icon">
              <text class="iconfont icon-gouwuche"></text>
            </view>
            <view class="fast-text">
              <text>购物车</text>
            </view>
          </view>
        </view>
        <!-- 操作按钮 -->
        <view class="foo-item-btn">
          <view class="btn-wrapper">
            <view class="btn-item btn-item-deputy" @click="onShowSkuPopup(2)">
              <text>加入购物车</text>
            </view>
            <view class="btn-item btn-item-main" @click="onShowSkuPopup(3)">
              <text>立即购买</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 快捷导航 -->
    <shortcut bottom="200rpx" />

  </view>
</template>

<script>
  import { setCartTabBadge, setCartTotalNum } from '@/utils/app'
  import * as GoodsApi from '@/api/goods'
  import * as CartApi from '@/api/cart'
  import jyfParser from '@/components/jyf-parser/jyf-parser'
  import Shortcut from '@/components/shortcut'
  import SlideImage from './components/SlideImage'
  import SkuPopup from './components/SkuPopup'

  export default {
    components: {
      jyfParser,
      Shortcut,
      SlideImage,
      SkuPopup,
    },
    data() {
      return {
        // 正在加载
        isLoading: true,
        // 当前商品ID
        goodsId: null,
        // 商品详情
        goods: {},
        // 购物车总数量
        cartTotal: 0,
        // 显示/隐藏SKU弹窗
        showSkuPopup: false,
        // 模式 1:都显示 2:只显示购物车 3:只显示立即购买
        skuMode: 1
      }
    },

    /**
     * 生命周期函数--监听页面加载
     */
    onLoad(options) {
      // 商品ID
      this.goodsId = parseInt(options.goodsId)
      // 加载页面数据
      this.onRefreshPage()
    },

    methods: {

      // 刷新页面数据
      onRefreshPage() {
        const app = this
        app.isLoading = true
        Promise.all([app.getGoodsDetail(), app.getCartTotal()])
          .finally(() => app.isLoading = false)
      },

      // 获取商品信息
      getGoodsDetail() {
        const app = this
        return new Promise((resolve, reject) => {
          GoodsApi.detail(app.goodsId)
            .then(result => {
                const goodsData = result.data;
                if (goodsData.skuList) {
                    goodsData.skuList.forEach(function(sku, index) {
                       goodsData.skuList[index].specIds = sku.specIds.split('-');
                       goodsData.skuList[index].skuId = sku.id;
                    })
                }
                app.goods = goodsData;
                app.skuMode = 3;
                resolve(result);
            })
            .catch(err => reject(err))
        })
      },

      // 获取购物车总数量
      getCartTotal() {
        const app = this
        return new Promise((resolve, reject) => {
          CartApi.list()
            .then(result => {
              app.cartTotal = result.data.totalNum;
              setCartTotalNum(app.cartTotal);
              setCartTabBadge();
              resolve(result);
            })
            .catch(err => reject(err));
        })
      },

      // 更新购物车数量
      onAddCart() {
         this.$toast("添加购物车成功");
         this.getCartTotal();
      },

      /**
       * 显示/隐藏SKU弹窗
       * @param {skuMode} 模式 1:都显示 2:只显示购物车 3:只显示立即购买
       */
      onShowSkuPopup(skuMode = 1) {
        this.skuMode = skuMode;
        this.showSkuPopup = !this.showSkuPopup;
      },

      // 跳转到首页
      onTargetHome(e) {
        this.$navTo('pages/index/index');
      },

      // 跳转到购物车页
      onTargetCart() {
        this.$navTo('pages/cart/index')
      }
    },

    /**
     * 分享当前页面
     */
    onShareAppMessage() {
      const app = this
      // 构建页面参数
      const params = app.$getShareUrlParams({
          goodsId: app.goodsId,
      })
      return {
        title: app.goods.name,
        imageUrl: app.goods.images[0],
        path: `/pages/goods/detail?${params}`
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
        goodsId: app.goodsId,
      })
      return {
        title: app.goods.name,
        imageUrl: app.goods.images[0],
        path: `/pages/goods/detail?${params}`
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
  @import "./detail.scss";
</style>
