<template>
  <view class="container">
    <!--店铺切换-->
    <Location v-if="storeInfo" :storeInfo="storeInfo"/>
    
    <!-- 搜索框 -->
    <Search tips="请输入搜索关键字..." @event="$navTo('pages/search/index')" />

    <view class="cate-content dis-flex" v-if="list.length > 0">
      <!-- 左侧 分类 -->
      <scroll-view class="cate-left f-28" :scroll-y="true" :style="{ height: `${scrollHeight}px` }">
          <view v-for="(item, index) in list" :key="index">
              <text class="cart-badge" v-if="item.total">{{ item.total }}</text>
              <view class="type-nav" :class="{ selected: curIndex == index }" @click="handleSelectNav(index)">
                  <image class="logo" lazy-load :lazy-load-margin="0" v-if="item.logo" :src="item.logo"></image>
                  <view class="name">{{ item.name }}</view>
              </view>
          </view>
      </scroll-view>

      <!-- 右侧 商品 -->
      <scroll-view class="cate-right b-f" :scroll-top="scrollTop" :scroll-y="true" :style="{ height: `${scrollHeight}px` }">
        <view v-if="list[curIndex]">
          <view class="cate-right-cont">
            <view class="cate-two-box">
              <view v-if="list[curIndex].goodsList.length" class="cate-cont-box">
                <view class="flex-five item" v-for="(item, idx) in list[curIndex].goodsList" :key="idx">
                  <view class="cate-img">
                    <image v-if="item.logo" lazy-load :lazy-load-margin="0" :src="item.logo" @click="onTargetGoods(item.id)"></image>
                  </view>
                  <view class="cate-info">
                    <view class="base">
                       <text class="name text">{{ item.name }}</text>
                       <text class="stock text">库存:{{ item.stock ? item.stock : 0 }} 已售:{{ item.initSale ? item.initSale : 0 }}</text>
                    </view>
                    <view class="action">
                        <text class="price">￥{{ item.price ? item.price : 0 }}</text>
                        <view class="cart">
                            <view v-if="item.isSingleSpec === 'Y'" class="singleSpec">
                                <view class="ii do-minus" v-if="item.buyNum" @click="onSaveCart(item.id, '-')"></view>
                                <view class="ii num" v-if="item.buyNum">{{ (item.buyNum != undefined) ? item.buyNum : 0 }}</view>
                                <view class="ii do-add" v-if="item.stock > 0" @click="onSaveCart(item.id, '+')"></view>
                            </view>
                            <view v-if="item.isSingleSpec === 'N'" class="multiSpec">
                                <text class="num-badge" v-if="item.buyNum">{{ item.buyNum }}</text>
                                <view class="select-spec" @click="onShowSkuPopup(2, item.id)">选规格</view>
                            </view>
                        </view>
                    </view>
                  </view>
                </view>
              </view>
              <empty v-if="!list[curIndex].goodsList.length" :isLoading="isLoading" tips="暂无商品~"></empty>
            </view>
          </view>
        </view>
      </scroll-view>
    
    </view>
    
    <!-- 商品SKU弹窗 -->
    <SkuPopup v-if="!isLoading" v-model="showSkuPopup" :skuMode="skuMode" :goods="goods" @addCart="onAddCart"/>
    
    <view class="flow-fixed-footer b-f m-top10">
      <view class="dis-flex chackout-box">
        <view class="chackout-left pl-12">
          <view class="col-amount-do">总金额：<text class="amount">￥{{ totalPrice.toFixed(2) }}</text></view>
          <view class="col-amount-view">共计：{{ totalNum }} 件</view>
        </view>
        <view class="chackout-right" @click="doSubmit()">
          <view class="flow-btn f-32">去结算</view>
        </view>
      </view>
    </view>
    
    <empty v-if="!list.length" :isLoading="isLoading" />
  </view>
</template>

<script>
  import { setCartTabBadge, setCartTotalNum } from '@/utils/app'
  import * as CartApi from '@/api/cart'
  import * as GoodsApi from '@/api/goods'
  import * as settingApi from '@/api/setting'
  import Search from '@/components/search'
  import Empty from '@/components/empty'
  import SkuPopup from './components/SkuPopup'
  import Location from '@/components/page/location'

  const App = getApp()

  export default {
    components: {
      Search,
      SkuPopup,
      Empty,
      Location
    },
    data() {
      return {
        goodsCart: [],
        totalNum: 0,
        totalPrice: 0.00,
        // 列表高度
        scrollHeight: 500,
        // 一级分类：指针
        curIndex: 0,
        // 内容区竖向滚动条位置
        scrollTop: 0,
        // 分类列表
        list: [],
        // 正在加载中
        isLoading: true,
        showSkuPopup: false,
        skuMode: 1,
        goods: {},
        storeInfo: null
      }
    },

    /**
     * 生命周期函数--监听页面加载
     */
    onLoad() {
      const app = this
      // 设置分类列表高度
      app.setListHeight()
    },

    /**
     * 生命周期函数--监听页面显示
     */
    onShow() {
      const app = this;
      // 获取页面数据
      app.getPageData();
      app.onGetStoreInfo();
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
       * 获取页面数据
       */
      getPageData() {
        const app = this
        app.isLoading = true
        Promise.all([
            // 获取分类列表
            GoodsApi.cateList(),
            // 获取购物车列表
            CartApi.list()
          ])
          .then(result => {
            // 初始化分类列表数据
            app.list = result[0].data;
            app.totalNum = result[1].data.totalNum;
            app.goodsCart = result[1].data.list;
            setCartTotalNum(app.totalNum);
            setCartTabBadge();
          })
          .finally(() => {
              app.isLoading = false
              app.totalPrice = 0
              app.list.forEach(function(item, index) {
                  let total = 0
                  item.goodsList.forEach(function(goods, key) {
                      let totalBuyNum = 0
                      app.goodsCart.forEach(function(cart){
                         if (goods.id == cart.goodsId) {
                             total = total + cart.num
                             totalBuyNum = totalBuyNum + cart.num
                             app.totalPrice = app.totalPrice + (cart.goodsInfo.price * cart.num)
                         } 
                      })
                      app.$set(app.list[index].goodsList[key], 'buyNum', totalBuyNum)
                  })
                  app.$set(app.list[index], 'total', total)
              })
          })
      },
      
      /**
       * 获取默认店铺
       * */
       onGetStoreInfo() {
         const app = this
         settingApi.systemConfig()
           .then(result => {
               app.storeInfo = result.data.storeInfo
           })
       },
      
      /**
       * 跳转商品详情
       */
      onTargetGoods(goodsId) {
        this.$navTo(`pages/goods/detail`, { goodsId })
      },

      /**
       * 设置分类列表高度
       */
      setListHeight() {
        const app = this
        uni.getSystemInfo({
          success(res) {
            app.scrollHeight = res.windowHeight - 120
          }
        })
      },

      // 一级分类：选中分类
      handleSelectNav(index) {
        const app = this;
        app.curIndex = index;
        app.scrollTop = 0;
      },
      
      // 更新购物车
      onSaveCart(goodsId, action) {
        const app = this
        return new Promise((resolve, reject) => {
          CartApi.save(goodsId, action)
            .then(result => {
                app.getPageData();
                resolve(result);
            })
            .catch(err => {
                console.log(err);
            })
        })
      },
      // 更新购物车数量
      onAddCart(total) {
        this.getPageData();
        this.$toast("添加购物车成功");
      },
      // 结算
      doSubmit() {
        if (this.totalPrice > 0) {
            this.$navTo('pages/cart/index')
        } else {
            this.$error("请先选择商品")
        }
      },
      onShowSkuPopup(skuMode, goodsId) {
        const app = this
        app.isLoading = true
        return new Promise((resolve, reject) => {
          GoodsApi.detail(goodsId)
            .then(result => {
              const goodsData = result.data
              
              if (goodsData.skuList) {
                  goodsData.skuList.forEach(function(sku, index) {
                    goodsData.skuList[index].specIds = sku.specIds.split('-')
                    goodsData.skuList[index].skuId = sku.id
                  })
              }
              
              app.goods = goodsData
              app.skuMode = skuMode
              app.showSkuPopup = !app.showSkuPopup
              
              console.log(app.skuMode)
              
              app.isLoading = false
              
              resolve(result)
            })
            .catch(err => reject(err))
        })
      },
    },

    /**
     * 设置分享内容
     */
    onShareAppMessage() {
      const app = this
      return {
        title: _this.templet.shareTitle,
        path: '/pages/category/index?' + app.$getShareUrlParams()
      }
    },

    /**
     * 分享到朋友圈
     * 本接口为 Beta 版本，暂只在 Android 平台支持，详见分享到朋友圈 (Beta)
     * https://developers.weixin.qq.com/miniprogram/dev/framework/open-ability/share-timeline.html
     */
    onShareTimeline() {
      const app = this
      return {
        title: _this.templet.shareTitle,
        path: '/pages/category/index?' + app.$getShareUrlParams()
      }
    }

  }
</script>

<style>
  page {
    background: #fff;
  }
</style>
<style lang="scss" scoped>
  .cate-content {
    background: #fff;
    margin-top: 118rpx;
    /* #ifdef H5 */
    margin-top: 124rpx;
    /* #endif */
  }

  .cate-wrapper {
    padding: 0 20rpx 20rpx 20rpx;
    box-sizing: border-box;
  }

  /* 分类内容 */
  .cate-content {
    width: 100%;
  }

  .cate-left {
    flex-direction: column;
    display: flex;
    width: 200rpx;
    color: #444;
    height: 100%;
    background: #f8f8f8;
    margin-bottom: 120rpx;
    .cart-badge {
      position: absolute;
      right: 1rpx;
      margin-top: 10rpx;
      margin-right: 5rpx;
      font-size: 18rpx;
      background: #fa5151;
      z-index: 999;
      text-align: center;
      line-height: 28rpx;
      color: #ffffff;
      border-radius: 50%;
      min-width: 32rpx;
      padding: 5rpx 13rpx 5rpx 13rpx;
    }
  }

  .cate-right {
    display: flex;
    flex-direction: column;
    width: 550rpx;
    height: 100%;
    overflow: hidden;
    margin-bottom: 80rpx;
  }

  .cate-right-cont {
    width: 100%;
    display: flex;
    flex-flow: row wrap;
    align-content: flex-start;
    padding-top: 10rpx;
  }

  .type-nav {
    position: relative;
    height: 140rpx;
    text-align: center;
    z-index: 10;
    display: block;
    font-size: 26rpx;
    padding: 20rpx 0rpx 126rpx 0rpx;
    .logo {
        width: 60rpx;
        height: 60rpx;
        border-radius: 60rpx;
        margin: 0rpx;
        padding: 0rpx;
    }
    .name {
        margin-top: 2rpx;
        width: 100%;
        overflow-x: hidden;
        height: 40rpx;
        line-height: 40rpx;
        text-align: center;
    }
  }

  .type-nav.selected {
    color: #666666;
    background: #ffffff;
    border-right: none;
    border-left: solid 10rpx #f03c3c;
    font-weight: bold;
    font-size: 28rpx;
  }

  .cate-cont-box {
    margin-bottom: 10rpx;
    padding-bottom: 10rpx;
    overflow: hidden;
    height: auto;
    display: block;
    .item {
        height: 220rpx;
        display: block;
        padding-top: 5rpx;
        border-radius: 3rpx;
        margin-bottom: 5rpx;
    }
  }

  .cate-cont-box .cate-img {
    padding: 13rpx 10rpx 4rpx 10rpx;
    display: block;
  }

  .cate-cont-box .cate-img image {
    width: 160rpx;
    height: 150rpx;
    float: left;
    border-radius: 5rpx;
    display: block;
    border: #cccccc solid 1rpx;
    margin-top: 5rpx;
  }

  .cate-cont-box .cate-info {
    text-align: left;
    display: block;
    font-size: 26rpx;
    margin-left: 168rpx;
    padding-bottom: 14rpx;
    color: #444;
    padding: 0 15rpx 30rpx 15rpx;
    .base {
        height: 100%;
        display: block;
        .text {
            display: block;
            float: left;
            width: 100%;
        }
        .name {
            font-weight: bold;
            width: 100%;
            font-size: 26rpx;
            overflow: hidden;
            display: -webkit-box;
            -webkit-box-orient: vertical;
            -webkit-line-clamp: 2;
        }
        .stock {
            margin-top: 10rpx;
            color: #999;
        }
    }
    .action {
        display: block;
        height: 50rpx;
        .price {
            margin-top: 20rpx;
            color: #f03c3c;
            float: left;
            font-size: 32rpx;
            font-weight: bold;
        }
        .cart {
            margin-top: 20rpx;
            float: right;
            font-size: 30rpx;
            height: 60rpx;
            .ii {
                float: left;
                text-align: center;
                width: 60rpx;
                cursor: pointer;
            }
            .do-add {
                background: url('~@/static/icon/add.png') no-repeat;
                background-size: 100% 100%;
                width: 45rpx;
                height: 45rpx;
            }
            .do-minus {
                background-image: url('~@/static/icon/minus.png');
                background-size: 100% 100%;
                width: 45rpx;
                height: 45rpx;
            }
            .multiSpec {
                .num-badge {
                    position: absolute;
                    margin-top: 10rpx;
                    margin-right: 25rpx;
                    font-size: 18rpx;
                    background: #f03c3c;
                    text-align: center;
                    line-height: 36rpx;
                    color: #ffffff;
                    border-radius: 50%;
                    min-width: 36rpx;
                    padding: 2rpx;
                }
                .select-spec {
                    border: solid 1rpx $fuint-theme;
                    padding: 10rpx 20rpx 10rpx 36rpx;
                    font-size: 25rpx;
                    border-radius: 5rpx;
                    color: #ffffff;
                    background: $fuint-theme;
                }
            }
        }
    }
  }
  .cate-two-box {
    width: 100%;
    padding: 0 2px;
  }
  
  // 底部操作栏
  .flow-fixed-footer {
    position: fixed;
    bottom: var(--window-bottom);
    width: 100%;
    background: #fff;
    border-top: 1px solid #eee;
    z-index: 11;
    padding-top: 8rpx;
    .chackout-left {
      font-size: 28rpx;
      height: 98rpx;
      color: #777;
      flex: 4;
      padding-left: 12px;
        text-align: right;
        padding-right: 40rpx;
        .col-amount-do {
          font-size: 35rpx;
          margin-top: 5rpx;
          margin-bottom:5rpx;
          .amount {
              color: #f03c3c;
              font-weight: bold;
          }
        }
    }
    
    .chackout-right {
      font-size: 34rpx;
      flex: 2;
    }
    
    // 提交按钮
    .flow-btn {
      background: linear-gradient(to right, $fuint-theme, $fuint-theme);
      color: #fff;
      text-align: center;
      line-height: 92rpx;
      display: block;
      font-size: 28rpx;
      border-radius: 5rpx;
      margin-right: 20rpx;
       // 禁用按钮
       &.disabled {
         background: #ff9779;
       }
    }
  }
</style>
