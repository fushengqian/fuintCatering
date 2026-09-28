<template>
  <view class="container" :style="themeVars">
    <view class="point-header">
      <view class="point-tip"><text class="iconfont icon-jifen"></text>我的积分余额</view>
      <view class="point-value">{{ userInfo.point ? userInfo.point : 0 }}</view>
    </view>
    <view class="goods-list">
      <view class="goods-item" v-for="(item, index) in goodsList" :key="index" @click="onTargetDetail(item.id)">
        <view class="goods-image">
          <image class="image" mode="aspectFill" :src="item.logo"></image>
        </view>
        <view class="goods-content">
          <view class="goods-name">{{ item.name }}</view>
          <view class="goods-point">
            <text class="point-num">{{ item.pointPrice ? item.pointPrice : 0 }}</text>
            <text class="point-unit">积分</text>
          </view>
          <view class="goods-extra">
            <text class="goods-stock">库存 {{ item.stock ? item.stock : 0 }}</text>
            <text class="goods-limit" v-if="item.exchangeLimit > 0">每人限兑{{ item.exchangeLimit }}件</text>
          </view>
        </view>
        <view class="goods-btn">兑换</view>
      </view>
    </view>
    <empty v-if="!goodsList.length" :isLoading="isLoading" :custom-style="{ padding: '180rpx 50rpx' }" tips="暂无可兑换的商品~" />
  </view>
</template>

<script>
  import * as GoodsApi from '@/api/goods'
  import * as UserApi from '@/api/user'
  import Empty from '@/components/empty'
  import { getEmptyPaginateObj, getMoreListData } from '@/utils/app'

  export default {
    components: {
      Empty
    },
    data() {
      return {
        userInfo: {},
        // 兑换商品列表
        list: getEmptyPaginateObj(),
        // 正在加载
        isLoading: true,
        // 当前页码
        page: 1
      }
    },

    computed: {
      // 兑换商品列表，v-for 不直接使用带括号的表达式，避免小程序事件参数解析异常
      goodsList() {
        return (this.list && this.list.content) ? this.list.content : []
      }
    },

    /**
     * 生命周期函数--监听页面显示
     */
    onShow() {
      this.getUserInfo()
      this.getGoodsList(1)
    },

    /**
     * 页面上拉触底事件
     */
    onReachBottom() {
      if (this.page < this.list.totalPages) {
        this.getGoodsList(this.page + 1)
      }
    },

    methods: {

      /**
       * 获取积分兑换商品列表
       */
      getGoodsList(pageNo = 1) {
        const app = this
        app.isLoading = true
        GoodsApi.pointList({ page: pageNo })
          .then(result => {
            // getMoreListData 返回的是数组，需要赋值给 list.content
            const newList = result.data || {}
            app.list.content = getMoreListData({ content: newList.content || [] }, app.list, pageNo)
            app.list.totalPages = newList.totalPages || 0
            app.page = pageNo
          })
          .catch(() => {
            if (pageNo == 1) {
              app.list.content = []
            }
          })
          .finally(() => {
            app.isLoading = false
          })
      },

      // 获取当前用户信息（积分余额）
      getUserInfo() {
        const app = this
        return new Promise((resolve) => {
          UserApi.info()
            .then(result => {
              app.userInfo = result.data.userInfo || {}
              resolve(app.userInfo)
            })
            .catch(() => resolve(null))
        })
      },

      // 跳转商品详情
      onTargetDetail(goodsId) {
        this.$navTo('pages/goods/detail?goodsId=' + goodsId)
      }

    }
  }
</script>

<style lang="scss" scoped>
  .container {
    min-height: 100vh;
    background: #f7f7f7;
    padding-bottom: 30rpx;
  }

  .point-header {
    height: 200rpx;
    color: #FFFFFF;
    background: $fuint-theme;
    padding-top: 50rpx;
    text-align: center;
    .point-tip {
      font-size: 26rpx;
      .iconfont {
        margin-right: 6rpx;
      }
    }
    .point-value {
      font-size: 56rpx;
      font-weight: bold;
      margin-top: 10rpx;
    }
  }

  .goods-list {
    margin: 20rpx 25rpx 0 25rpx;
  }

  .goods-item {
    background: #FFFFFF;
    border-radius: 12rpx;
    padding: 20rpx;
    margin-bottom: 20rpx;
    display: flex;
    align-items: center;
    overflow: hidden;
  }

  .goods-image {
    width: 160rpx;
    height: 160rpx;
    flex-shrink: 0;
    .image {
      width: 100%;
      height: 100%;
      border-radius: 8rpx;
      display: block;
    }
  }

  .goods-content {
    flex: 1;
    min-width: 0;
    padding: 0 20rpx;
    .goods-name {
      font-size: 30rpx;
      color: #333;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
    .goods-point {
      margin-top: 24rpx;
      .point-num {
        font-size: 40rpx;
        font-weight: bold;
        color: #fa2209;
      }
      .point-unit {
        font-size: 24rpx;
        color: #fa2209;
        margin-left: 6rpx;
      }
    }
    .goods-extra {
      margin-top: 10rpx;
      font-size: 22rpx;
      color: #999;
      .goods-limit {
        margin-left: 16rpx;
      }
    }
  }

  .goods-btn {
    flex-shrink: 0;
    height: 60rpx;
    line-height: 60rpx;
    padding: 0 30rpx;
    border-radius: 30rpx;
    color: #FFFFFF;
    font-size: 26rpx;
    background: $fuint-theme;
  }
</style>
