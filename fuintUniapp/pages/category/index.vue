<template>
  <view class="container">
    <!--店铺切换-->
    <Location v-if="storeInfo" :storeInfo="storeInfo" :tableInfo="tableInfo"/>
    
    <!-- 搜索框 -->
    <Search tips="请输入搜索关键字..." @event="$navTo('pages/search/index')" />

    <view class="cate-content dis-flex" v-if="list.length > 0">
      <!-- 左侧 分类 -->
      <scroll-view class="cate-left f-28" scroll-y :show-scrollbar="false" :enhanced="true" :style="{ height: `${scrollHeight}px` }">
          <view v-for="(item, index) in list" :key="index">
              <text class="cart-badge" v-if="item.total">{{ item.total }}</text>
              <view class="type-nav" :class="{ selected: curIndex == index }" @click="handleSelectNav(index)">
                  <image class="logo" lazy-load :lazy-load-margin="0" :src="item.logo ? item.logo : '/static/empty-02.png'"></image>
                  <view class="name">{{ item.name }}</view>
              </view>
          </view>
      </scroll-view>

      <!-- 右侧 商品 -->
      <scroll-view 
        class="cate-right b-f" 
        :scroll-top="scrollTop" 
        :scroll-y="true" 
        :style="{ height: `${scrollHeight}px` }"
        @scroll="handleScroll"
        scroll-with-animation
        :scroll-into-view="scrollIntoView"
      >
        <view v-if="list[curIndex]">
          <view class="cate-right-cont">
            <view class="cate-two-box">
              <view v-if="list[curIndex].goodsList.length" class="cate-cont-box">
                <!-- 为每个分类添加锚点 -->
                <view v-for="(category, catIndex) in list" :key="catIndex" :id="`category-${catIndex}`">
                  <view class="category-title">{{category.name}}</view>
                  <view class="flex-five item" v-for="(item, idx) in category.goodsList" :key="idx">
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
              </view>
              <empty v-if="!list[curIndex].goodsList.length" :isLoading="isLoading" tips="暂无商品~"></empty>
            </view>
          </view>
        </view>
      </scroll-view>
    </view>
    
    <!-- 商品SKU弹窗 -->
    <SkuPopup v-if="!isLoading" v-model="showSkuPopup" :skuMode="skuMode" :goods="goods" @addCart="onAddCart"/>
    
    <!-- 就餐人数对话框 start -->
    <u-popup v-model="peopleNumShow" mode="bottom" width="90%">
        <div class="people-container">
            <div class="popup-header">
                <h2>就餐人数确认</h2>
                <p>请选择本次就餐的人数，我们将为您提供更好的服务</p>
            </div>
            <div class="people-content">
                <div class="grid-container">
                    <div v-for="num in peopleArray" :key="num" 
                         :class="['people-item', { selected: selectedPeopleNum === num }]" 
                         @click="checkPeopleNum(num)">
                        <div class="people-icon">✓</div>
                        <div class="people-number">{{ num }}</div>
                        <div class="people-label">{{ getPeopleLabel(num) }}</div>
                    </div>
                </div>
                <div class="custom-input">
                    <label>其他人数:</label>
                    <input type="number" min="1" max="99" placeholder="请输入就餐人数">
                </div>
                <div class="action-buttons">
                    <button class="btn btn-cancel" @click="cancelPeople()">取消</button>
                    <button class="btn btn-confirm" @click="confirmPeople()">确认选择</button>
                </div>
            </div>
      </div>
    </u-popup>
    <!-- 就餐人数对话框 end -->
    
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
        peopleArray: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12],
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
        peopleNumShow: false,
        selectedPeopleNum: 2,
        skuMode: 1,
        goods: {},
        storeInfo: null,
        tableInfo: null,
        // 用于自动滚动定位
        scrollIntoView: '',
        // 存储每个分类的位置信息
        categoryPositions: [],
        // 防抖计时器
        scrollTimer: null,
        // 是否正在手动切换分类
        isManualSelect: false
      }
    },

    onLoad({ tableId, orderId }) {
      const app = this;
      tableId = tableId ? parseInt(tableId) : 0;
      if (tableId > 0) {
          uni.setStorageSync('tableId', tableId);
      }
      app.orderId = orderId;
      app.setListHeight();
    },

    onShow() {
      const app = this;
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
      getPageData() {
        const app = this
        app.isLoading = true
        Promise.all([
            GoodsApi.cateList(),
            CartApi.list()
          ])
          .then(result => {
            app.list = result[0].data;
            app.totalNum = result[1].data.totalNum;
            app.goodsCart = result[1].data.list;
            setCartTotalNum(app.totalNum);
            setCartTabBadge();
            
            // 数据加载完成后，计算分类位置
            this.$nextTick(() => {
              this.calculateCategoryPositions();
            });
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
      getPeopleLabel(num) {
          const labels = {
              1: '一人食',
              2: '双人餐',
              3: '三人行',
              4: '家庭餐',
              5: '朋友小聚',
              6: '多人聚餐',
              7: '团体用餐',
              8: '团体用餐',
              9: '团体用餐',
              10: '团体用餐',
              11: '团体用餐',
              12: '团体用餐'
          };
          return labels[num] || '用餐';
      },
      cancelPeople() {
        this.peopleNumShow = false;
        uni.setStorageSync('peopleNum', 0);
      },
      confirmPeople() {
        this.peopleNumShow = false;
        uni.setStorageSync('peopleNum', this.selectedPeopleNum);
      },
      checkPeopleNum(peopleNum) {
        this.selectedPeopleNum = peopleNum;
      },
      // 计算每个分类的位置信息
      calculateCategoryPositions() {
        const query = uni.createSelectorQuery().in(this);
        this.categoryPositions = [];
        this.list.forEach((item, index) => {
          query.select(`#category-${index}`).boundingClientRect();
        });
        
        query.exec(res => {
          res.forEach((rect, index) => {
            if (rect) {
              this.categoryPositions.push({
                index,
                top: rect.top,
                height: rect.height
              });
            }
          });
        });
      },
      
      // 滚动事件处理
      handleScroll(e) {
        if (this.isManualSelect) {
            this.isManualSelect = false;
            return;
        }
        
        // 防抖处理
        clearTimeout(this.scrollTimer);
        this.scrollTimer = setTimeout(() => {
          const scrollTop = e.detail.scrollTop;
          this.updateActiveCategory(scrollTop);
        }, 50);
      },
      
      // 根据滚动位置更新当前激活的分类
      updateActiveCategory(scrollTop) {
        if (!this.categoryPositions.length) return;
        
        // 增加一个偏移量，提前切换分类
        const offset = 100;
        const adjustedScrollTop = scrollTop + offset;
        
        // 找到当前应该激活的分类
        let activeIndex = 0;
        for (let i = 0; i < this.categoryPositions.length; i++) {
          const position = this.categoryPositions[i];
          if (adjustedScrollTop >= position.top) {
            activeIndex = position.index;
          } else {
            break;
          }
        }
        
        // 更新当前激活的分类
        if (this.curIndex !== activeIndex) {
          this.curIndex = activeIndex;
        }
      },
      
      onGetStoreInfo() {
         const app = this
         settingApi.systemConfig()
           .then(result => {
               app.storeInfo = result.data.storeInfo;
               app.tableInfo = result.data.tableInfo;
               const showPeopleNum = result.data.peopleNum;
               let peopleNum = uni.getStorageSync('peopleNum');
               if (!peopleNum && app.tableId > 0 && showPeopleNum == 'Y') {
                   app.peopleNumShow = true;
               }
           })
       },
      
      onTargetGoods(goodsId) {
        this.$navTo(`pages/goods/detail`, { goodsId })
      },

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
        this.isManualSelect = true;
        this.curIndex = index;
        this.scrollIntoView = `category-${index}`;
        setTimeout(() => {
          this.scrollIntoView = '';
        }, 500);
      },
      
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
      
      onAddCart(total) {
        this.getPageData();
        this.$toast("添加购物车成功");
      },
      
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
              app.isLoading = false
              resolve(result)
            })
            .catch(err => reject(err))
        })
      },
    },
    onShareAppMessage() {
      const app = this
      return {
        title: _this.templet.shareTitle,
        path: '/pages/category/index?' + app.$getShareUrlParams()
      }
    },

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
    overflow: hidden;
  }
  /* 分类内容 */
  .cate-content {
    width: 100%;
    overflow: hidden;
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
    
    .category-title {
      font-size: 32rpx;
      font-weight: bold;
      padding: 20rpx;
      background: #f8f8f8;
      margin-bottom: 10rpx;
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
    margin-top: 5rpx;
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
       // 禁用按钮
       &.disabled {
         background: #ff9779;
       }
    }
  }
    .people-container {
       padding-top: 0rpx;
       padding-bottom: 20rpx;
    }
    .popup-header {
       background: $fuint-theme;
       color: white;
       padding: 20rpx;
       text-align: center;
    }
    .popup-header h2 {
       font-size: 34rpx;
       font-weight: 600;
       margin-bottom: 3rpx;
    }
    .popup-header p {
       font-size: 26rpx;
       opacity: 0.9;
    }
    .people-content {
       padding: 30rpx 20rpx 20rpx 20rpx;
    }
    .grid-container {
       display: grid;
       grid-template-columns: repeat(4, 1fr);
       gap: 10rpx;
       margin-bottom: 10px;
    }
    .people-item {
       position: relative;
       background: #f8f9fa;
       border-radius: 0rpx;
       height: 120rpx;
       display: flex;
       flex-direction: column;
       justify-content: center;
       align-items: center;
       cursor: pointer;
       transition: all 0.3s ease;
       border: 2rpx solid transparent;
    }
    .people-item:hover {
       transform: translateY(-2px);
       box-shadow: 0 5rpx 12rpx rgba(0, 0, 0, 0.08);
       border-color: #ff9f7d;
    }
    .people-item.selected {
       background: linear-gradient(135deg, #ffd9cc 0%, #ffb499 100%);
       border-color: #ff7e5f;
       color: #d64c2f;
    }
    .people-number {
       font-size: 18px;
       font-weight: 700;
       margin-bottom: 3px;
    }
    .people-label {
       font-size: 11px;
       color: #6c757d;
    }
    .selected .people-label {
       color: #d64c2f;
       font-weight: 500;
    }
    .people-icon {
       position: absolute;
       top: 5rpx;
       right: 5rpx;
       width: 26rpx;
       height: 26rpx;
       background: #ff7e5f;
       border-radius: 50%;
       display: flex;
       justify-content: center;
       align-items: center;
       color: white;
       font-size: 26rpx;
       opacity: 0;
       transition: opacity 0.3s ease;
    }
    .selected .people-icon {
       opacity: 1;
    }
    .action-buttons {
       display: flex;
       justify-content: space-between;
       gap: 12rpx;
    }
    .btn {
       flex: 1;
       padding: 10rpx;
       border: none;
       border-radius: 20rpx;
       font-size: 28rpx;
       font-weight: 500;
       cursor: pointer;
       transition: all 0.3s ease;
    }
    .btn-cancel {
       background: #f8f9fa;
       color: #6c757d;
       border: 1px solid #dee2e6;
    }
    .btn-cancel:hover {
       background: #e9ecef;
    }
    .btn-confirm {
       background: linear-gradient(135deg, #ff7e5f 0%, #feb47b 100%);
       color: white;
       box-shadow: 0 3rpx 8rpx rgba(255, 126, 95, 0.3);
    }
    .btn-confirm:hover {
       transform: translateY(-2px);
       box-shadow: 0 5rpx 12px rgba(255, 126, 95, 0.4);
    }
    .custom-input {
       display: flex;
       align-items: center;
       margin-top: 10rpx;
       padding: 16rpx;
       background: #f8f9fa;
       border-radius: 8px;
    }
    .custom-input label {
       margin-right: 24rpx;
       font-weight: 500;
       color: #495057;
       font-size: 28rpx;
    }
    .custom-input input {
       flex: 1;
       padding: 16rpx 20rpx;
       border: 1px solid #dee2e6;
       border-radius: 6rpx;
       font-size: 28rpx;
       outline: none;
       transition: border-color 0.3s;
    }
    .custom-input input:focus {
       border-color: #ff7e5f;
       box-shadow: 0 0 0 2rpx rgba(255, 126, 95, 0.2);
    }
    @media (max-width: 500rpx) {
       .grid-container {
           grid-template-columns: repeat(4, 1fr);
       }
       .people-item {
           height: 120rpx;
       }
       .people-number {
           font-size: 36rpx;
       }
       .popup-header {
           padding: 25rpx;
       }
       .people-content {
           padding: 25rpx;
       }
    }
</style>