<template>
  <view v-show="!isLoading" class="container" :style="themeVars">
    <!-- 商品图片轮播 -->
    <SlideImage v-if="!isLoading" :images="goods.images" />

    <!-- 商品信息 -->
    <view v-if="!isLoading" class="goods-info m-top20">
      <!-- 价格、销量 -->
      <view class="info-item info-item__top dis-flex flex-x-between flex-y-end">
        <!-- 积分兑换商品：展示兑换所需积分 -->
        <block v-if="isPointGoods">
          <view class="block-left dis-flex flex-y-end">
            <text class="floor-price">{{ goods.pointPrice }}</text>
            <text class="floor-price__samll" style="margin-left:8rpx;">积分</text>
          </view>
          <view class="block-right dis-flex">
            <view class="goods-sales">
              <text>已兑换{{ goods.initSale }}</text>
            </view>
          </view>
        </block>
        <block v-else>
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
        </block>
      </view>
      <!-- 积分兑换商品：限购提示 -->
      <view class="info-item" v-if="isPointGoods && goods.exchangeLimit > 0" style="font-size:24rpx;color:#fa2209;">
        <text>该商品每人限兑{{ goods.exchangeLimit }}件</text>
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
            <text class="share__icon iconfont icon-fenxiang-post"></text>
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

    <!-- 详情/评价 锚点Tab（滚动后吸顶） -->
    <view id="anchor-tabs" class="anchor-tabs">
      <view class="anchor-tab-item" :class="{ active: currentTab === 0 }" @click="onAnchorTab(0)">
        <text>商品详情</text>
      </view>
      <view v-if="commentData.total > 0" class="anchor-tab-item" :class="{ active: currentTab === 1 }" @click="onAnchorTab(1)">
        <text>用户评价（{{ commentData.total }}）</text>
      </view>
    </view>

    <!-- 用户评价 -->
    <view id="goods-comment" v-if="!isLoading && commentData.total > 0" class="goods-comment m-top20 b-f">
      <view class="comment-header">
        <text class="comment-title">用户评价（{{ commentData.total }}）</text>
        <text class="comment-rate">{{ commentData.avgScore }}分 好评率{{ commentData.goodRate }}%</text>
      </view>
      <view class="comment-item" v-for="(item, index) in commentData.list" :key="index">
        <view class="comment-user">
          <text class="user-name">{{ item.userName || '匿名会员' }}</text>
          <view class="stars">
            <text v-for="s in 5" :key="s" class="star" :class="{ active: item.score >= s }">★</text>
          </view>
          <text class="comment-date">{{ item.createTime }}</text>
        </view>
        <view class="comment-text" v-if="item.content">
          <text>{{ item.content }}</text>
        </view>
        <view class="comment-images" v-if="item.images && item.images.length">
          <image
            class="c-image"
            v-for="(img, imgIndex) in item.images"
            :key="imgIndex"
            :src="img"
            mode="aspectFill"
            @click="onPreviewCommentImage(item.images, imgIndex)"
          ></image>
        </view>
        <view class="comment-reply" v-if="item.replyContent">
          <text class="reply-label">商家回复：</text>
          <text>{{ item.replyContent }}</text>
        </view>
      </view>
    </view>

    <!-- 商品描述 -->
    <view id="goods-content" v-if="!isLoading" class="goods-content m-top20">
      <view class="item-title b-f">
        <text>商品详情</text>
      </view>
      <block v-if="goods.description != ''">
        <view class="goods-content-detail b-f">
          <jyf-parser :html="goods.description"></jyf-parser>
        </view>
      </block>
      <empty v-else :isLoading="false" tips="亲，暂无商品描述" />
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
            <block v-if="isPointGoods">
              <view class="btn-item btn-item-main" @click="onShowSkuPopup(3)">
                <text>立即兑换</text>
              </view>
            </block>
            <block v-else>
              <view class="btn-item btn-item-deputy" @click="onShowSkuPopup(2)">
                <text>加入购物车</text>
              </view>
              <view class="btn-item btn-item-main" @click="onShowSkuPopup(3)">
                <text>立即下单</text>
              </view>
            </block>
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
  import * as GoodsCommentApi from '@/api/goodsComment'
  import * as CartApi from '@/api/cart'
  import jyfParser from '@/components/jyf-parser/jyf-parser'
  import Shortcut from '@/components/shortcut'
  import Empty from '@/components/empty'
  import SlideImage from './components/SlideImage'
  import SkuPopup from './components/SkuPopup'

  export default {
    components: {
      jyfParser,
      Shortcut,
      Empty,
      SlideImage,
      SkuPopup,
    },

    computed: {
      // 是否积分兑换商品：是则展示积分数、隐藏现金购买入口
      isPointGoods() {
        return this.goods && this.goods.isPointGoods === 'Y'
      }
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
        // 模式 1:都显示 2:只显示购物车 3:只显示立即下单
        skuMode: 1,
        // 商品评价数据
        commentData: { total: 0, avgScore: 5.0, goodRate: 100, list: [] },
        // 锚点Tab：0=商品详情 1=用户评价
        currentTab: 0,
        // 锚点Tab高度
        tabHeight: 44,
        // 商品详情/评价区块的文档位置
        detailTop: 0,
        commentTop: 0
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
      // 加载商品评价
      this.getGoodsComment()
    },

    // 监听页面滚动：同步锚点Tab高亮
    onPageScroll({ scrollTop }) {
      const tabBottom = scrollTop + (this.tabHeight || 44)
      if (this.commentTop > 0 && tabBottom >= this.commentTop) {
        this.currentTab = 1
      } else {
        this.currentTab = 0
      }
    },

    methods: {

      // 测量锚点区块位置
      measureAnchor() {
        const app = this
        app.$nextTick(() => {
          const query = uni.createSelectorQuery()
          query.select('#anchor-tabs').boundingClientRect()
          query.select('#goods-content').boundingClientRect()
          query.select('#goods-comment').boundingClientRect()
          query.selectViewport().scrollOffset()
          query.exec(res => {
            if (!res || !res[0]) return
            const [tabRect, detailRect, commentRect, scroll] = res
            const scrollTop = scroll ? scroll.scrollTop : 0
            app.tabHeight = tabRect ? tabRect.height : 44
            app.detailTop = detailRect ? detailRect.top + scrollTop : 0
            app.commentTop = commentRect ? commentRect.top + scrollTop : 0
          })
        })
      },

      // 锚点Tab切换
      onAnchorTab(tab) {
        const app = this
        app.currentTab = tab
        const target = tab === 1 ? app.commentTop : app.detailTop
        if (target <= 0) return
        uni.pageScrollTo({
          scrollTop: target - (app.tabHeight || 44) + 1,
          duration: 300
        })
      },

      // 获取商品评价（统计+前3条）
      getGoodsComment() {
        const app = this
        GoodsCommentApi.list({ goodsId: app.goodsId, page: 1, pageSize: 3 })
          .then(result => {
            const { statistics, paginationResponse } = result.data
            app.commentData = {
              total: statistics.total || 0,
              avgScore: statistics.avgScore || 0,
              goodRate: statistics.goodRate || 0,
              list: paginationResponse.content || []
            }
            // 评价区块渲染后重新测量锚点位置
            app.measureAnchor()
          })
      },

      // 预览评价图片
      onPreviewCommentImage(images, index) {
        uni.previewImage({ urls: images, current: index })
      },

      // 刷新页面数据
      onRefreshPage() {
        const app = this
        app.isLoading = true
        Promise.all([app.getGoodsDetail(), app.getCartTotal()])
          .finally(() => {
            app.isLoading = false
            // 页面渲染后测量锚点位置
            app.measureAnchor()
          })
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
       * @param {skuMode} 模式 1:都显示 2:只显示购物车 3:只显示立即下单
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
