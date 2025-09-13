<template>
  <mescroll-body ref="mescrollRef" :sticky="true" @init="mescrollInit" :down="{ native: true }" @down="downCallback"
    :up="upOption" @up="upCallback">
    
    <view class="search-wrapper">
      <view class="search-input">
        <view class="search-input-wrapper">
          <view class="left">
            <text class="search-icon iconfont icon-sousuo"></text>
          </view>
          <view class="right">
            <input v-model="keyword" class="input" placeholder="请输入卡券名称..." type="text"></input>
          </view>
        </view>
      </view>
      <view class="search-button">
        <button class="button" @click="doSearch" type="warn"> 搜索 </button>
      </view>
    </view>
    
    <!-- 卡券列表 -->
    <view class="goods-list clearfix" :class="['column-1']">
      <view class="goods-item" v-for="(item, index) in list.content" :key="index" @click="onEdit(item.id)">
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
                      <view class="receive">
                        <text>编辑</text>
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
  import * as couponApi from '@/api/merchant/coupon'
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
        keyword: '', // 查找关键字
        type: '', // 卡券类型
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
    onLoad() {
      // 设置默认列表显示方式
      this.setShowView()
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
      
      /**
       * 搜索提交
       */
      doSearch() {
        // 刷新列表数据
        this.mescroll.resetUpScroll();
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
        const param = {
          type: app.type,
          keyword: app.keyword,
          page: pageNo
        }
        
        return new Promise((resolve, reject) => {
          couponApi.couponList(param)
            .then(result => {
              const newList = result.data;
              app.list.content = getMoreListData(newList, app.list, pageNo)
              resolve(newList)
            })
            .catch(reject)
        })
      },

      // 跳转编辑页
      onEdit(couponId) {
        this.$navTo(`pages/merchant/coupon/edit`, { couponId })
      }
    }
  }
</script>

<style lang="scss" scoped>
  // 页面头部
  .header {
    display: block;
    align-items: center;
    text-align: center;
    height: 103rpx;
    background: #fff;

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
  
  .search-wrapper {
    display: flex;
    height: 80rpx;
    margin-top: 20rpx;
    padding: 0rpx 10rpx;
  }
  
  // 搜索输入框
  .search-input {
    width: 80%;
    background: #fff;
    border-radius: 50rpx 0 0 50rpx;
    box-sizing: border-box;
    overflow: hidden;
    border: solid 1px #cccccc;
    line-height: 80rpx;
    .search-input-wrapper {
      display: flex;
      .left {
        display: flex;
        width: 60rpx;
        justify-content: center;
        align-items: center;
        .search-icon {
          display: block;
          color: #666666;
          font-size: 30rpx;
          font-weight: bold;
        }
      }
  
      .right {
        flex: 1;
  
        input {
          font-size: 28rpx;
          height: 80rpx;
          line-height: 80rpx;
          .input-placeholder {
            color: #aba9a9;
          }
        }
  
      }
    }
  }
  
  // 搜索按钮
  .search-button {
    width: 20%;
    box-sizing: border-box;
  
    .button {
      line-height: 78rpx;
      height: 78rpx;
      font-size: 28rpx;
      border-radius: 0 20px 20px 0;
      background: $fuint-theme;
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
      height: 50rpx;
      width: 108rpx;
      padding: 0rpx 10rpx 0rpx 10rpx;
      line-height: 50rpx;
      text-align: center;
      border: 1px solid #f86d48;
      border-radius: 40rpx;
      color: #fff;
      background: linear-gradient(to right, #f9211c, #ff6335);
      font-size: 22rpx;
      &.state {
        border: none;
        color: #888888;
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
        font-weight: bold;
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
