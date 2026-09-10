<template>
  <view class="container dining-container" :style="[diningStyle, themeVars]">
    <!-- 顶部吸顶区：把 Location 和 Search 包到一起整体吸顶，避免之前两个组件
         各自 sticky/fixed(top:0) 在视口同一位置重叠 → 后渲染的 fixed 搜索盖住门店 -->
    <view class="dining-header">
      <!--店铺切换-->
      <Location v-if="storeInfo" :storeInfo="storeInfo" :tableInfo="tableInfo"/>

      <!-- 搜索框 -->
      <Search tips="请输入搜索关键字..." @event="$navTo('pages/search/index')" />
    </view>

    <view class="cate-content dis-flex" v-if="list.length > 0">
      <!-- 左侧 分类：固定列，不参与任何滚动 -->
      <view class="cate-left f-28">
          <view v-for="(item, index) in list" :key="index">
              <view class="type-nav" :class="{ selected: curIndex == index }" @click="handleSelectNav(index)">
                  <!-- 已加购数量徽章：移入 .type-nav 内，相对分类 cell(position:relative) 右上角定位，
                       之前放在 .type-nav 外会一路向上找到 page，导致徽章飘到视口右上角 -->
                  <text class="cart-badge" v-if="item.total">{{ item.total }}</text>
                  <image class="logo" lazy-load :lazy-load-margin="0" :src="item.logo ? item.logo : '/static/empty-02.png'"></image>
                  <view class="name">{{ item.name }}</view>
              </view>
          </view>
      </view>

      <!-- 右侧 商品 -->
      <scroll-view 
        class="cate-right b-f" 
        :scroll-top="scrollTop" 
        :scroll-y="true" 
        :style="{ height: '100%' }"
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
                      <view class="img-wrap">
                        <image v-if="item.logo" lazy-load :lazy-load-margin="0" :src="item.logo" @click="onTargetGoods(item.id)"></image>
                        <view class="member-tag" v-if="item.gradeIds">会员专属</view>
                      </view>
                    </view>
                    <view class="cate-info">
                      <view class="base">
                        <text class="name text">{{ item.name }}</text>
                        <text class="salepoint text" v-if="item.salePoint">{{ item.salePoint }}</text>
                        <text class="stock text">库存:{{ item.stock ? item.stock : 0 }} 已售:{{ item.initSale ? item.initSale : 0 }}</text>
                      </view>
                      <view class="action">
                          <text class="price">￥{{ item.price ? item.price : 0 }}</text>
                          <view class="cart">
                              <view v-if="item.isSingleSpec === 'Y'" class="singleSpec">
                                  <view class="ii do-minus" v-if="item.buyNum" :style="{ background: themeColor, borderColor: themeColor }" @click="onSaveCart(item.id, '-')">
                                    <view class="minus-bar"></view>
                                  </view>
                                  <view class="ii num" v-if="item.buyNum">{{ (item.buyNum != undefined) ? item.buyNum : 0 }}</view>
                                  <view class="ii do-add" v-if="item.stock > 0" :style="{ background: themeColor, borderColor: themeColor }" @click="onSaveCart(item.id, '+')">
                                    <view class="add-bar-h"></view>
                                    <view class="add-bar-v"></view>
                                  </view>
                              </view>
                              <view v-if="item.isSingleSpec === 'N'" class="multiSpec">
                                  <text class="num-badge" v-if="item.buyNum">{{ item.buyNum }}</text>
                                  <view class="select-spec" :style="{ background: themeColor, borderColor: themeColor }" @click="onShowSkuPopup(2, item.id)">选规格</view>
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
    
    <view class="flow-fixed-footer b-f m-top10" :style="{ paddingBottom: 'calc(' + tabbarHeight + 'px + env(safe-area-inset-bottom))' }">
      <view class="dis-flex chackout-box">
        <view class="chackout-left pl-12">
          <view class="col-amount-do">总金额：<text class="amount">￥{{ totalPrice.toFixed(2) }}</text></view>
          <view class="col-amount-view">共计：{{ totalNum }} 件</view>
        </view>
        <view class="chackout-right" @click="doSubmit()">
          <view class="flow-btn f-32" :style="{ background: 'linear-gradient(to right,' + themeColor + ',' + themeColor + ')' }">去结算</view>
        </view>
      </view>
    </view>
    
    <empty v-if="!list.length" :isLoading="isLoading" />
    <!-- 自定义 tabBar 占位：本页 .dining-container 已用 :style 精确让位 footer(footer 内部已含 tabBar 预留 padding-bottom)，
         此处高度置 0 避免重复占位挤压 .cate-content 产生底部红框 -->
    <view class="tabbar-safe-area" style="height: 0;"></view>
    <!-- #ifdef H5 -->
    <h5-tabbar ref="h5Tabbar"></h5-tabbar>
    <!-- #endif -->
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
  import { loadAndApplyTabbar } from '@/utils/tabbar'
  // #ifdef H5
  import H5Tabbar from '@/components/tabbar/index.vue'
  // #endif

  const App = getApp()

  export default {
    components: {
      Search,
      SkuPopup,
      Empty,
      Location,
      // #ifdef H5
      H5Tabbar
      // #endif
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
        isManualSelect: false,
        // 自定义 tabBar 高度（装修后台可配，默认 50），结算栏需紧贴其顶部
        tabbarHeight: 50
      }
    },

    computed: {
      diningStyle() {
        // 动态计算 .dining-container 高度 = viewport - footer 真实总高
        // footer 真实总高 ≈ 内容(padding-top 8rpx + .chackout-left 98rpx ≈ 55.5px)
        //                  + padding-bottom(tabbarHeight 动态 40~80px + env(safe))
        // 用 :style 而非 CSS calc：响应 tabbarHeight 异步装修配置变化(默认50, 实际40~80)
        // 使 .dining-container 底部 = footer 内容顶部，从根上避免 .cta-content 与 fixed footer 重叠
        return {
          height: `calc(100vh - 55px - ${this.tabbarHeight}px - env(safe-area-inset-bottom))`,
          minHeight: `calc(100vh - 55px - ${this.tabbarHeight}px - env(safe-area-inset-bottom))`
        }
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
      // 拉取 tabBar 装修配置（缓存优先），自定义 tabBar 实例可能尚未就绪会自动重试
      loadAndApplyTabbar(this)
      // 结算栏需紧贴 tabBar 顶部，同步装修配置里的 tabBar 高度
      app.syncTabbarHeight();
      // #ifdef H5
      this.$refs.h5Tabbar && this.$refs.h5Tabbar.refresh()
      // #endif
      // #ifdef MP-WEIXIN
      // 微信注入的 getTabBar 挂在原生页面实例上，uni-app 需经 $scope 访问
      const host = this.$scope || this
      const tb = typeof host.getTabBar === 'function' && host.getTabBar()
      tb && tb.syncSelected && tb.syncSelected()
      // #endif
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
               if (!peopleNum && app.tableInfo != null && showPeopleNum == 'Y') {
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

      // 结算栏要贴在自定义 tabBar 顶部，而 tabBar 高度由装修配置决定(默认50px)，
      // 配置为异步拉取，这里读缓存并轮询重试，避免两者高度不一致产生空隙
      syncTabbarHeight() {
        const app = this
        let times = 0
        const apply = () => {
          const cfg = uni.getStorageSync('tabbar')
          const height = cfg && cfg.style && Number(cfg.style.height)
          if (!height) return false
          // 与自定义 tabBar 组件保持一致的高度区间
          app.tabbarHeight = Math.max(40, Math.min(height, 80))
          return true
        }
        if (apply()) return
        const timer = setInterval(() => {
          times++
          if (apply() || times >= 10) clearInterval(timer)
        }, 300)
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
                // 错误提示已由全局拦截器处理
                reject(err);
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
    /* 禁止页面层滚动，避免滚动事件把左侧分类列顶出可见区 */
    height: 100%;
    overflow: hidden;
  }
</style>
<style lang="scss" scoped>
  /* 堂食点单页根容器：启用 flex 列布局让 .cate-content 占满中部剩余空间，
     scroll-view 通过 height:100% 自适应高度，避免最后一个商品被底部结算栏遮挡 */
  .dining-container {
    /* 高度由 :style 动态绑定(响应 tabbarHeight 装修配置异步下发 40~80px)，
       = calc(100vh - 55px - tabbarHeight - env(safe))，
       使 .dining-container 底部 = footer 内容顶部，从根上避免 .cta-content 与 fixed footer 重叠 */
    display: flex;
    flex-direction: column;
  }
  /* 顶部吸顶容器：统一收纳门店定位 + 搜索框。
     - 子组件自身的 .main-loc(sticky) 与 .search-wrapper(fixed) 都设在 top:0，
       各自吸顶会在视口同一位置重叠，后渲染的 fixed 搜索(z-index:999999) 会盖住门店。
       改为由本容器统一 sticky，子组件内部定位降级为 static，避免嵌套定位冲突 */
  .dining-header {
    position: sticky;
    top: 0;
    z-index: 1000;
    background: #fff;
    /* 子组件降级：避免自身 sticky/fixed 与父 sticky 冲突。
       注意两点：
       1) 这里已嵌套在 .dining-header 内部，不能再重复写 .dining-header，
          否则编译成 `.dining-header .dining-header .xxx` 永远匹配不到，
          导致 Search 的 .search-wrapper 仍是 position:fixed 脱离文档流，
          浮在顶部盖住第一个分类标题和第一个商品；
       2) Location / Search 是子组件，父组件 scoped 样式需用 ::v-deep 穿透，
          才能作用到子组件内部的 class */
    ::v-deep .main-loc {
      position: static;
    }
    ::v-deep .search-wrapper {
      position: static;
      width: auto;
      z-index: auto;
      background: transparent;
    }
  }
  .cate-content {
    background: #fff;
    /* 顶部空间由 .dining-header 吸顶区自然撑出，不再需要 margin-top 给 fixed 搜索留位 */
    /* footer(.flow-fixed-footer) 是 position:fixed 脱离文档流，flex 不会为它预留空间；
       用 margin-bottom 压缩 .cta-content 高度，让其底部落在 footer 顶部上方 4rpx 处，
       配合 .cate-wrapper 的 flex + 最后组 margin-top:auto，使最后一个商品（含 +号按钮）
       紧贴 footer 顶部且不会被 footer 的 1px border-top 切到。
       用 margin-bottom 而非 padding-bottom：margin 区在 .cta-content box 之外（page 白背景），
       不会像 padding 那样把 .cate-content 白色背景延伸成"挡板"；
       且 4rpx 间隙与上方/下方同为白背景，无视觉边界。 */
    margin-bottom: 4rpx; /* .dining-container 已用 calc 让位 footer，这里只留 4rpx 保险防 footer border 切到 +号 */
    flex: 1;
    min-height: 0;
    overflow: hidden;
  }
  .cate-wrapper {
    padding: 0 20rpx 20rpx 20rpx;
    box-sizing: border-box;
    overflow: hidden;
    /* flex 列布局 + 最小高度 = scroll-view 高度：
       - 内容足够时，最后一组自然在底部，滚到底时最后商品贴结算栏顶部
       - 内容不足时，.cate-wrapper 撑满 scroll-view 高度，
         最后一组通过 margin-top:auto 吸收剩余空间推到底，
         使最后商品紧贴 scroll-view 底部（即结算栏顶部），消除"白色挡板" */
    display: flex;
    flex-direction: column;
    min-height: 100%;
  }
  /* 最后一组 margin-top:auto：吸收剩余空间将该组推到底部，
     使该组末尾的最后一个商品始终紧贴 scroll-view 底部（= 结算栏顶部），
     消除内容不足时 footer 上方出现"白色挡板"的视觉 */
  .cate-wrapper > .cate-section:last-child {
    margin-top: auto;
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
    /* 解耦：滚动不传导到父级/右侧，仅响应竖向手势 */
    overscroll-behavior: contain;
    touch-action: pan-y;
    -webkit-overflow-scrolling: touch;
    overflow: hidden;
    &::-webkit-scrollbar {
        display: none !important;
        width: 0 !important;
        height: 0 !important;
    }
    /* 分类 cell"已加购数量"徽章：已移入 .type-nav 内，随分类 cell 右上角定位，不再飘到 page 视口右上角 */
    .cart-badge {
      position: absolute;
      top: 6rpx;
      right: 6rpx;
      font-size: 18rpx;
      background: #fa5151;
      z-index: 11;
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
    flex: 1;
    min-height: 0;
    width: 100%;
    height: 100%;
    overflow: hidden;
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
  
  .cate-cont-box .cate-img .img-wrap {
    position: relative;
    .member-tag {
      position: absolute;
      top: 0;
      right: 0;
      padding: 4rpx 12rpx;
      font-size: 20rpx;
      color: #fff;
      background: linear-gradient(135deg, #d4a843, #b8860b);
      border-radius: 0 0 0 12rpx;
      z-index: 2;
    }
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
        .salepoint {
            font-size: 22rpx;
            color: #e49a3d;
            margin-top: 6rpx;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
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
            .do-add,
            .do-minus {
                width: 45rpx;
                height: 45rpx;
                border-radius: 50%;
                background: $fuint-theme;
                border: 1rpx solid $fuint-theme;
                position: relative;
                box-sizing: border-box;
            }
            .do-add .add-bar-h,
            .do-add .add-bar-v,
            .do-minus .minus-bar {
                position: absolute;
                left: 50%;
                top: 50%;
                background: #ffffff;
                transform: translate(-50%, -50%);
            }
            .do-add .add-bar-h,
            .do-minus .minus-bar {
                width: 18rpx;
                height: 2rpx;
            }
            .do-add .add-bar-v {
                width: 2rpx;
                height: 18rpx;
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
    // 小程序端 var(--window-bottom) 不生效(被编译为 0)，结算栏会落在底部的自定义
    // tabBar(z-index:999) 下方被盖住；这里固定贴底，再用 padding-bottom 把内容顶到
    // tabBar 之上(tabBar 高度由装修配置决定，模板里动态下发)，底边与 tabBar 顶边
    // 无缝贴合，不会出现间隙
    bottom: 0;
    padding-bottom: calc(50px + constant(safe-area-inset-bottom));
    padding-bottom: calc(50px + env(safe-area-inset-bottom));
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