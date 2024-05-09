<template>
  <mescroll-body ref="mescrollRef" :sticky="true" @init="mescrollInit" :down="{ native: true }" @down="downCallback"
    :up="upOption" @up="upCallback">
    <!-- 页面头部 -->
    <view class="header">
      <search class="search" :tips="options.search ? options.search : '搜索卡券'" @event="handleSearch" />
    </view>

    <!-- 排序标签 -->
    <view class="store-sort">
      <view class="sort-item" :class="{ active: sortType === 'all' }" @click="handleSortType('all')">
        <text>综合</text>
      </view>
      <view class="sort-item" :class="{ active: sortType === 'sales' }" @click="handleSortType('sales')">
        <text>领取数</text>
      </view>
      <view class="sort-item sort-item-price" :class="{ active: sortType === 'price' }" @click="handleSortType('price')">
        <text>面额</text>
        <view class="price-arrow">
          <view class="icon up" :class="{ active: sortType === 'price' && !sortPrice }">
            <text class="iconfont icon-arrow-up"></text>
          </view>
          <view class="icon down" :class="{ active: sortType === 'price' && sortPrice }">
            <text class="iconfont icon-arrow-down"></text> </view>
        </view>
      </view>
    </view>

    <!-- 卡券列表 -->
    <view class="goods-list clearfix" :class="['column-1']">
      <view class="goods-item" v-for="(item, index) in list.content" :key="index" @click="onTargetDetail(item.id, item.type, item.userCouponId)">
        <view class="dis-flex">
          <!-- 卡券图片 -->
          <view class="goods-item_left">
            <image class="image" :src="item.image"></image>
          </view>
          <view class="goods-item_right">
            <!-- 卡券名称 -->
            <view class="goods-name twolist-hidden">
              <text>{{ item.name }}</text>
            </view>
            <view class="goods-item_desc">
              <!-- 卡券卖点 -->
              <view class="desc-selling_point dis-flex">
                <text class="onelist-hidden">{{ item.sellingPoint }}</text>
              </view>
              <view class="coupon-attr">
                  <view class="attr-l">
                      <!-- 卡券销量 -->
                      <view class="desc-goods_sales dis-flex">
                        <text v-if="item.type === 'C'">已领取{{ item.gotNum }}，剩余{{ item.leftNum }}</text>
                        <text v-if="item.type === 'P'">已有{{ item.gotNum }}人预存</text>
                        <text v-if="item.type === 'T'">已领取{{ item.gotNum }}，剩余{{ item.leftNum }}</text>
                      </view>
                      <!-- 面额 -->
                      <view v-if="item.amount > 0 && item.type === 'C'" class="desc_footer">
                        <text class="price_x">¥{{ item.amount }}</text>
                      </view>
                  </view>
                  <view class="attr-r">
                      <!--领券按钮-->
                      <view class="receive" v-if="item.type === 'C' && item.isReceive === false">
                          <text v-if="!item.point || item.point < 1">立即领取</text>
                        <text v-if="item.point && item.point > 0">立即兑换</text>
                      </view>
                      <view class="receive state" v-if="item.type === 'C' && item.isReceive === true">
                          <text>已领取</text>
                      </view>
                      <view class="receive" v-if="item.type === 'P' && item.isReceive === false">
                        <text>立即预存</text>
                      </view>
                      <view v-if="item.type === 'T' && item.isReceive === false" class="receive">
                          <text>领取次卡</text>
                      </view>
                      <view v-if="item.type === 'T' && item.isReceive === true" class="receive state">
                          <text>已领取</text>
                      </view>
                  </view>
              </view>
            </view>
          </view>
        </view>
      </view>
    </view>
  </mescroll-body>
</template>

<script>
  import MescrollBody from '@/components/mescroll-uni/mescroll-body.vue'
  import MescrollMixin from '@/components/mescroll-uni/mescroll-mixins'
  import * as couponApi from '@/api/coupon'
  import { getEmptyPaginateObj, getMoreListData } from '@/utils/app'
  import Search from '@/components/search'
  import { CouponTypeEnum } from '@/common/enum/coupon'

  const pageSize = 15
  const showViewKey = 'CouponList-ShowView';

  export default {
    components: {
      MescrollBody,
      Search
    },

    mixins: [MescrollMixin],

    data() {
      return {
        // 枚举类
        CouponTypeEnum,
        sortType: 'all', // 排序类型
        sortPrice: false, // 价格排序 (true高到低 false低到高)
        options: {}, // 当前页面参数
        list: getEmptyPaginateObj(), // 卡券列表数据
        // 正在加载
        isLoading: false,
        // 上拉加载配置
        upOption: {
          // 首次自动执行
          auto: true,
          // 每页数据的数量; 默认10
          page: { size: pageSize },
          // 数量要大于4条才显示无更多数据
          noMoreSize: 4,
        }
      }
    },
    onShow() {
       this.getCouponList(1)
    },
    /**
     * 生命周期函数--监听页面加载
     */
    onLoad(options) {
      // 记录options
      this.options = options
      // 设置默认列表显示方式
      this.setShowView()
      // 设置标题
      let type = options.type
      uni.setNavigationBarTitle({
        title: CouponTypeEnum[type].name + "中心"
      })
    },

    methods: {
      /**
       * 上拉加载的回调 (页面初始化时也会执行一次)
       * 其中page.num:当前页 从1开始, page.size:每页数据条数,默认10
       * @param {Object} page
       */
      upCallback(page) {
        const app = this
        // 设置列表数据
        app.getCouponList(page.num)
          .then(list => {
            const curPageLen = list.content.length
            const totalSize = list.totalElements
            app.mescroll.endBySize(curPageLen, totalSize)
          })
          .catch(() => app.mescroll.endErr())
      },

      // 设置默认列表显示方式
      setShowView() {
        this.showView = uni.getStorageSync(showViewKey) || true
      },
      
      // 点击跳转到首页
      onTargetIndex() {
        this.$navTo('pages/index/index')
      },

      /**
       * 获取卡券列表
       * @param {number} pageNo 页码
       */
      getCouponList(pageNo = 1) {
        const app = this
        console.log(app.options)
        const param = {
          sortType: app.sortType,
          sortPrice: Number(app.sortPrice),
          type: app.options.type || "C",
          needPoint: app.options.needPoint || '0',
          name: app.options.search || '',
          pageNumber: pageNo
        }
        
        return new Promise((resolve, reject) => {
          couponApi.list(param)
            .then(result => {
              const newList = result.data.coupon
              app.list.content = getMoreListData(newList, app.list, pageNo)
              resolve(newList)
            })
            .catch(reject)
        })
      },

      // 切换排序方式
      handleSortType(newSortType) {
        const app = this
        const newSortPrice = newSortType === 'price' ? !app.sortPrice : true
        app.sortType = newSortType
        app.sortPrice = newSortPrice
        // 刷新列表数据
        app.list = getEmptyPaginateObj()
        app.mescroll.resetUpScroll()
      },

      // 切换列表显示方式
      handleShowView() {
        const app = this
        app.showView = !app.showView
        uni.setStorageSync(showViewKey, app.showView)
      },

      // 跳转详情页
      onTargetDetail(couponId, type, userCouponId) {
        if (type === 'P') {
            this.$navTo(`pages/prestore/buy`, { couponId })
        } else {
            if (type === 'C') {
                this.$navTo(`pages/coupon/detail`, { couponId: couponId, userCouponId: userCouponId })
            } else if(type === 'T'){
                this.$navTo(`pages/timer/detail`, { couponId: couponId, userCouponId: userCouponId })
            }
        }
      },

      //卡券搜索
      handleSearch() {
        const searchPageUrl = 'pages/search/index'
        // 判断来源页面
        let pages = getCurrentPages()
        if (pages.length > 1 &&
          pages[pages.length - 2].route === searchPageUrl) {
          uni.navigateBack()
          return
        }
        // 跳转到卡券搜索页
        this.$navTo(searchPageUrl)
      }
    },

    /**
     * 设置分享内容
     */
    onShareAppMessage() {
      // 构建分享参数
      return {
        title: "全部卡券",
        path: "/pages/coupon/list?" + this.$getShareUrlParams()
      }
    },

    /**
     * 分享到朋友圈
     * 本接口为 Beta 版本，暂只在 Android 平台支持，详见分享到朋友圈 (Beta)
     * https://developers.weixin.qq.com/miniprogram/dev/framework/open-ability/share-timeline.html
     */
    onShareTimeline() {
      // 构建分享参数
      return {
        title: "全部卡券",
        path: "/pages/coupon/list?" + this.$getShareUrlParams()
      }
    }

  }
</script>

<style lang="scss" scoped>
  // 页面头部
  .header {
    display: flex;
    align-items: center;
    background-color: #fff;

    // 搜索框
    .search {
      flex: 1;
    }

    // 切换显示方式
    .show-view {
      width: 60rpx;
      height: 60rpx;
      line-height: 60rpx;
      font-size: 36rpx;
      color: #505050;
    }
  }

  // 排序组件
  .store-sort {
    position: sticky;
    top: var(--window-top);
    display: flex;
    padding: 20rpx 0;
    font-size: 28rpx;
    background: #fff;
    color: #000;
    z-index: 99;

    .sort-item {
      flex-basis: 33.3333%;
      display: flex;
      justify-content: center;
      align-items: center;
      height: 50rpx;

      &.active {
        color: #e49a3d;
      }
    }

    .sort-item-price .price-arrow {
      margin-left: 20rpx;
      font-size: 24rpx;
      color: #000;

      .icon {
        &.active {
          color: #e49a3d;
        }

        &.up {
          margin-bottom: -16rpx;
        }

        &.down {
          margin-top: -16rpx;
        }
      }

    }

  }

  // 卡券列表
  .goods-list {
    padding: 4rpx;
    box-sizing: border-box;
  }
  
  // 空数据按钮
  .empty-ipt {
    width: 220rpx;
    margin: 10rpx auto;
    font-size: 28rpx;
    height: 64rpx;
    line-height: 64rpx;
    text-align: center;
    color: #fff;
    border-radius: 5rpx;
    background: linear-gradient(to right, $fuint-theme, $fuint-theme);
  }

  // 单列显示
  .goods-list.column-1 {
    .goods-item {
      width: 100%;
      height: 260rpx;
      margin-bottom: 12rpx;
      padding: 20rpx;
      box-sizing: border-box;
      background: #fff;
      line-height: 1.6;

      &:last-child {
        margin-bottom: 0;
      }
    }

    .goods-item_left {
      display: flex;
      width: 35%;
      background: #fff;
      align-items: center;

      .image {
        display: block;
        width: 200rpx;
        height: 157rpx;
        margin-top: 20rpx;
        border-radius: 6rpx;
        border: solid 1rpx #cccccc;
      }
    }

    .goods-item_right {
      position: relative;
      flex: 1;

      .goods-name {
        margin-top: 30rpx;
        height: 44rpx;
        line-height: 1.3;
        white-space: normal;
        color: #484848;
        font-size: 30rpx;
      }
    }

    .goods-item_desc {
      margin-top: 0rpx;
      .coupon-attr {
         .attr-l {
             float: left;
             width: 70%;
         }
         .attr-r {
             margin-top: 0rpx;
             float: left;
         }
      }
    }
    .desc-selling_point {
      width: 400rpx;
      font-size: 24rpx;
      color: #e49a3d;
    }
    .receive {
      height: 46rpx;
      width: 128rpx;
      line-height: 46rpx;
      text-align: center;
      border: 1px solid #f8df00;
      border-radius: 5rpx;
      color: #f86d48;
      background: #f8df98;
      font-size: 22rpx;
      &.state {
        border: none;
        color: #cccccc;
        background: #F5F5F5;
      }
    }

    .desc-goods_sales {
      color: #999;
      font-size: 24rpx;
    }

    .desc_footer {
      font-size: 24rpx;

      .price_x {
        margin-right: 16rpx;
        color: #f03c3c;
        font-size: 30rpx;
      }

      .price_y {
        text-decoration: line-through;
      }
    }
  }

  .goods-item {
    float: left;
    box-sizing: border-box;
    padding: 6rpx;

    .goods-image {
      position: relative;
      width: 100%;
      height: 0;
      padding-bottom: 100%;
      overflow: hidden;
      background: #fff;

      &:after {
        content: '';
        display: block;
        margin-top: 100%;
      }

      .image {
        position: absolute;
        width: 100%;
        height: 100%;
        top: 0;
        left: 0;
        -o-object-fit: cover;
        object-fit: cover;
      }
    }

    .detail {
      padding: 8rpx;
      background: #fff;

      .goods-name {
        height: 64rpx;
        line-height: 32rpx;
        white-space: normal;
        color: #484848;
        font-size: 26rpx;
      }

      .detail-price {
        .goods-price {
          margin-right: 8rpx;
        }

        .line-price {
          text-decoration: line-through;
        }
      }
    }
  }
</style>
