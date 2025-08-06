<template>
  <view class="container">
    <!-- 店铺切换 -->
    <Location v-if="storeInfo" :storeInfo="storeInfo" :tableInfo="tableInfo"/>
    
    <!-- 搜索框 -->
    <Search tips="请输入搜索关键字..." @event="$navTo('pages/search/index')" />

    <view class="cate-content dis-flex" v-if="list.length > 0">
      <!-- 左侧分类 -->
      <scroll-view 
        class="cate-left f-28" 
        scroll-y 
        :show-scrollbar="false" 
        :enhanced="true" 
        :style="{ height: `${scrollHeight}px` }"
      >
        <view 
          v-for="(item, index) in list" 
          :key="index"
          :class="{ 'active-category': curIndex === index }"
        >
          <text class="cart-badge" v-if="item.total">{{ item.total }}</text>
          <view 
            class="type-nav" 
            :class="{ selected: curIndex == index }" 
            @click="handleSelectNav(index)"
            role="tab"
            :aria-selected="curIndex === index"
          >
            <image 
              class="logo" 
              lazy-load 
              :lazy-load-margin="0" 
              :src="item.logo || '/static/empty-02.png'"
              @error="handleImageError"
            />
            <view class="name">{{ item.name }}</view>
          </view>
        </view>
      </scroll-view>

      <!-- 右侧商品 -->
      <scroll-view 
        class="cate-right b-f" 
        :scroll-top="scrollTop" 
        scroll-y
        :style="{ height: `${scrollHeight}px` }"
        @scroll="handleScroll"
        scroll-with-animation
        :scroll-into-view="scrollIntoView"
      >
        <view v-if="list.length > 0">
          <view class="cate-right-cont">
            <view class="cate-two-box">
              <view v-if="list[curIndex].goodsList.length" class="cate-cont-box">
                <view 
                  v-for="(category, catIndex) in list" 
                  :key="catIndex" 
                  :id="`category-${catIndex}`"
                >
                  <view class="category-title">{{category.name}}</view>
                  <view 
                    class="flex-five item" 
                    v-for="(item, idx) in category.goodsList" 
                    :key="idx"
                  >
                    <view class="cate-img" @click="onTargetGoods(item.id)">
                      <image 
                        v-if="item.logo" 
                        lazy-load 
                        :lazy-load-margin="0" 
                        :src="item.logo"
                        @error="handleImageError"
                      />
                    </view>
                    <view class="cate-info">
                      <view class="base">
                       <text class="name text">{{ item.name }}</text>
                       <text class="stock text">库存:{{ item.stock || 0 }} 已售:{{ item.initSale || 0 }}</text>
                      </view>
                      <view class="action">
                          <text class="price">￥{{ item.price || 0 }}</text>
                          <view class="cart">
                              <view v-if="item.isSingleSpec === 'Y'" class="singleSpec">
                                  <view 
                                    class="ii do-minus" 
                                    v-if="item.buyNum" 
                                    @click.stop="onSaveCart(item.id, '-')"
                                  ></view>
                                  <view class="ii num" v-if="item.buyNum">{{ item.buyNum || 0 }}</view>
                                  <view 
                                    class="ii do-add" 
                                    v-if="item.stock > 0" 
                                    @click.stop="onSaveCart(item.id, '+')"
                                  ></view>
                              </view>
                              <view v-if="item.isSingleSpec === 'N'" class="multiSpec">
                                  <text class="num-badge" v-if="item.buyNum">{{ item.buyNum }}</text>
                                  <view 
                                    class="select-spec" 
                                    @click.stop="onShowSkuPopup(2, item.id)"
                                  >
                                    选规格
                                  </view>
                              </view>
                          </view>
                      </view>
                    </view>
                  </view>
                </view>
              </view>
              <empty v-else :isLoading="isLoading" tips="暂无商品~"></empty>
            </view>
          </view>
        </view>
      </scroll-view>
    </view>
    
    <!-- 商品SKU弹窗 -->
    <SkuPopup v-show="!isLoading" v-model="showSkuPopup" :skuMode="skuMode" :goods="goods" @addCart="onAddCart"/>
    
    <!-- 底部购物车栏 -->
    <view class="flow-fixed-footer b-f m-top10">
      <view class="dis-flex chackout-box">
        <view class="chackout-left pl-12">
          <view class="col-amount-do">总金额：<text class="amount">￥{{ totalPrice.toFixed(2) }}</text></view>
          <view class="col-amount-view">共计：{{ totalNum }} 件</view>
        </view>
        <view class="chackout-right" @click="doSubmit()">
          <view class="flow-btn f-32" :class="{ disabled: totalPrice <= 0 }">去结算</view>
        </view>
      </view>
    </view>
    
    <empty v-if="!list.length" :isLoading="isLoading" />
  </view>
</template>

<script>
import { debounce } from 'lodash';
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
      orderId: 0,
      goodsCart: [],
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
      storeInfo: null,
      tableInfo: null,
      // 滚动相关
      scrollIntoView: '',
      categoryPositions: [],
      isManualSelect: false
    }
  },

  computed: {
    // 计算总数量
    totalNum() {
      return this.goodsCart.reduce((total, item) => total + item.num, 0)
    },
    // 计算总价格
    totalPrice() {
      return this.goodsCart.reduce((total, item) => {
        return total + (item.goodsInfo.price * item.num)
      }, 0)
    }
  },

  onLoad({ tableId, orderId }) {
    this.orderId = orderId || 0
    if (tableId) {
      uni.setStorageSync('tableId', parseInt(tableId))
    }
    this.setListHeight()
  },

  onShow() {
    this.loadPageData()
    this.loadStoreInfo()
    this.getUserLocation()
  },

  methods: {
    // 获取用户位置
    getUserLocation() {
      uni.getLocation({
        type: 'gcj02',
        success: (res) => {
          uni.setStorageSync('latitude', res.latitude)
          uni.setStorageSync('longitude', res.longitude)
          this.loadStoreInfo()
        },
        fail: () => {
          console.warn('获取位置失败')
        }
      })
    },
    
    // 加载页面数据
    async loadPageData() {
      try {
        this.isLoading = true
        const [categories, cart] = await Promise.all([
          GoodsApi.cateList(),
          CartApi.list()
        ])
        
        this.list = categories.data
        this.goodsCart = cart.data.list
        
        this.$nextTick(() => {
          this.calculateCategoryPositions()
          this.updateCartBadges()
        })
      } catch (error) {
        console.error('加载数据失败:', error)
        this.$toast('加载失败，请重试')
      } finally {
        this.isLoading = false
      }
    },
    
    // 更新购物车角标
    updateCartBadges() {
      setCartTotalNum(this.totalNum)
      setCartTabBadge()
      
      this.list.forEach((item, index) => {
        let total = 0
        item.goodsList.forEach((goods, key) => {
          let totalBuyNum = 0
          this.goodsCart.forEach(cart => {
            if (goods.id === cart.goodsId) {
              total += cart.num
              totalBuyNum += cart.num
            }
          })
          this.$set(this.list[index].goodsList[key], 'buyNum', totalBuyNum)
        })
        this.$set(this.list[index], 'total', total)
      })
    },
    
    // 加载店铺信息
    async loadStoreInfo() {
      try {
        const result = await settingApi.systemConfig()
        this.storeInfo = result.data.storeInfo
        this.tableInfo = result.data.tableInfo
      } catch (error) {
        console.error('加载店铺信息失败:', error)
      }
    },
    
    // 计算分类位置
    calculateCategoryPositions() {
      const query = uni.createSelectorQuery().in(this)
      this.categoryPositions = []
      
      this.list.forEach((item, index) => {
        query.select(`#category-${index}`).boundingClientRect()
      })
      
      query.exec(res => {
        res.forEach((rect, index) => {
          if (rect) {
            this.categoryPositions.push({
              index,
              top: rect.top,
              height: rect.height
            })
          }
        })
      })
    },
    
    // 滚动事件处理
    handleScroll: debounce(function(e) {
      if (this.isManualSelect) return
      
      const scrollTop = e.detail.scrollTop
      const offset = 100 // 提前切换的偏移量
      const adjustedScrollTop = scrollTop + offset
      
      let activeIndex = 0
      for (let i = 0; i < this.categoryPositions.length; i++) {
        const position = this.categoryPositions[i]
        if (adjustedScrollTop >= position.top) {
          activeIndex = position.index
        } else {
          break
        }
      }
      
      if (this.curIndex !== activeIndex) {
        this.curIndex = activeIndex
      }
    }, 100),
    
    // 设置列表高度
    setListHeight() {
      uni.getSystemInfo({
        success: (res) => {
          this.scrollHeight = res.windowHeight - 120
        }
      })
    },
    
    // 选择分类
    handleSelectNav(index) {
      this.isManualSelect = true
      this.curIndex = index
      this.scrollIntoView = `category-${index}`
      setTimeout(() => {
        this.scrollIntoView = ''
      }, 500)
    },
    
    // 图片加载失败处理
    handleImageError(e) {
      const img = e.target
      img.src = '/static/empty-02.png'
    },
    
    // 跳转到商品详情
    onTargetGoods(goodsId) {
      this.$navTo(`pages/goods/detail`, { goodsId })
    },
    
    // 更新购物车
    async onSaveCart(goodsId, action) {
      try {
        await CartApi.save(goodsId, action)
        await this.loadPageData()
      } catch (error) {
        console.error('更新购物车失败:', error)
        this.$toast('操作失败，请重试')
      }
    },
    
    // 添加购物车成功回调
    onAddCart() {
      this.loadPageData()
      this.$toast("添加购物车成功")
    },
    
    // 显示SKU弹窗
    async onShowSkuPopup(skuMode, goodsId) {
      try {
        this.isLoading = true
        const result = await GoodsApi.detail(goodsId)
        const goodsData = result.data
        
        if (goodsData.skuList) {
          goodsData.skuList.forEach(sku => {
            sku.specIds = sku.specIds.split('-')
            sku.skuId = sku.id
          })
        }
        
        this.goods = goodsData
        this.skuMode = skuMode
        this.showSkuPopup = true
      } catch (error) {
        console.error('加载商品详情失败:', error)
        this.$toast('加载商品信息失败')
      } finally {
        this.isLoading = false
      }
    },
    
    // 提交订单
    doSubmit() {
      if (this.totalPrice <= 0) {
        return this.$error("请先选择商品")
      }
      this.$navTo('pages/cart/index')
    }
  },

  onShareAppMessage() {
    return {
      title: this.templet?.shareTitle || '发现好商品',
      path: '/pages/category/index?' + this.$getShareUrlParams()
    }
  },

  onShareTimeline() {
    return {
      title: this.templet?.shareTitle || '发现好商品',
      path: '/pages/category/index?' + this.$getShareUrlParams()
    }
  }
}
</script>

<style lang="scss">
page {
  background: #fff;
}

.cate-content {
  background: #fff;
  margin-top: 118rpx;
  /* #ifdef H5 */
  margin-top: 124rpx;
  /* #endif */
}

.cate-left {
  flex-direction: column;
  display: flex;
  width: 200rpx;
  color: #444;
  height: 100%;
  background: #f8f8f8;
  margin-bottom: 120rpx;
  overflow: hidden;
  &::-webkit-scrollbar {
      display: none !important;
      width: 0 !important;
      height: 0 !important;
  }
  
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
  
  .active-category {
    background: #fff;
  }
}

.cate-right {
  display: flex;
  flex-direction: column;
  width: 100%;
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
  transition: all 0.3s ease;
  
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
    transition: transform 0.2s;
    
    &:active {
      transform: scale(0.98);
    }
  }
  
  .category-title {
    font-size: 32rpx;
    font-weight: bold;
    padding: 20rpx;
    background: #f8f8f8;
    margin-bottom: 10rpx;
    border-left: 4rpx solid #f03c3c;
  }
}

.cate-cont-box .cate-img {
  padding: 13rpx 10rpx 4rpx 10rpx;
  display: block;
  
  image {
    width: 160rpx;
    height: 150rpx;
    float: left;
    border-radius: 5rpx;
    display: block;
    border: #cccccc solid 1rpx;
    margin-top: 5rpx;
  }
}

.cate-cont-box .cate-info {
  text-align: left;
  display: flex;
  flex-direction: column;
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
          right: 90rpx;
        }
        
        .select-spec {
          border: solid 1rpx $fuint-theme;
          padding: 10rpx 20rpx 10rpx 20rpx;
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
    transition: opacity 0.3s;
    
    &.disabled {
      background: #ff9779;
      opacity: 0.7;
    }
  }
}

// 响应式调整
@media (max-width: 600px) {
  .cate-left {
    width: 180rpx;
  }
  
  .type-nav {
    padding: 15rpx 0;
    height: 120rpx;
    
    .logo {
      width: 50rpx;
      height: 50rpx;
    }
  }
}
</style>