<template>
  <!-- 商品列表 -->
  <view class="goods-container">
      <mescroll-body ref="mescrollRef" :sticky="true" @init="mescrollInit" :down="{ native: true }" @down="downCallback" :up="upOption" @up="upCallback">
      <view class="diy-goods" :style="{ background: itemStyle.background }">
        <view class="goods-list" :class="[`display__${itemStyle.display}`, `column__${itemStyle.column}`]">
          <scroll-view :scroll-x="itemStyle.display === 'slide'">
            <view class="goods-item" v-for="(dataItem, index) in list.content" :key="index" @click="onTargetGoods(dataItem.id)">
              <!-- 单列商品 -->
              <block v-if="itemStyle.column === 1">
                <view class="dis-flex">
                  <!-- 商品图片 -->
                  <view class="goods-item_left">
                    <image class="image" lazy-load :lazy-load-margin="0" :src="dataItem.logo"></image>
                  </view>
                  <view class="goods-item_right">
                    <!-- 商品名称 -->
                    <view v-if="itemStyle.show.includes('goodsName')" class="goods-name twoline-hide">
                      <text>{{ dataItem.name }}</text>
                    </view>
                    <view class="goods-item_desc">
                      <!-- 商品卖点 -->
                      <view v-if="itemStyle.show.includes('sellingPoint')" class="desc-selling_point dis-flex">
                        <text class="oneline-hide">{{ dataItem.salePoint ? dataItem.salePoint : '' }}</text>
                      </view>
                      <!-- 商品销量 -->
                      <view v-if="itemStyle.show.includes('goodsSales')" class="desc-goods_sales dis-flex">
                        <text>已售{{ dataItem.initSale ? dataItem.initSale : 0 }}件</text>
                      </view>
                      <!-- 商品价格 -->
                      <view class="desc_footer">
                        <text v-if="itemStyle.show.includes('goodsPrice')" class="price_x">¥{{ dataItem.price ? dataItem.price : '0.00' }}</text>
                        <text class="price_y col-9" v-if="itemStyle.show.includes('linePrice') && dataItem.linePrice > 0">¥{{ dataItem.linePrice }}</text>
                        <view class="buy-now">去购买</view>
                      </view>
                    </view>
                  </view>
                </view>
              </block>
              <!-- 多列商品 -->
              <block v-else>
                <!-- 商品图片 -->
                <view class="goods-image">
                  <image class="image" lazy-load :lazy-load-margin="0" mode="aspectFill" :src="dataItem.logo"></image>
                </view>
                <view class="detail">
                  <!-- 商品标题 -->
                  <view v-if="itemStyle.show.includes('goodsName')" class="goods-name twoline-hide">
                    {{ dataItem.name }}
                  </view>
                  <!-- 商品价格 -->
                  <view class="detail-price oneline-hide">
                    <text v-if="itemStyle.show.includes('goodsPrice')" class="goods-price f-30 col-m">￥{{ dataItem.price }}</text>
                    <text v-if="itemStyle.show.includes('linePrice') && dataItem.linePrice > 0" class="line-price col-9 f-24">￥{{ dataItem.linePrice }}</text>
                  </view>
                </view>
              </block>
            </view>
          </scroll-view>
        </view>
      </view>
      </mescroll-body>
  </view>
</template>

<script>
  import MescrollBody from '@/components/mescroll-uni/mescroll-body.vue'
  import MescrollMixin from '@/components/mescroll-uni/mescroll-mixins'
  import { getEmptyPaginateObj, getMoreListData } from '@/utils/app'
  import * as GoodsApi from '@/api/goods'
  
  const pageSize = 10;
  
  export default {
    name: "Goods",
    
    /**
     * 组件的属性列表
     * 用于组件自定义设置
     */
    props: {
      itemIndex: String,
      itemStyle: Object,
      params: Object,
      isReflash: Boolean
    },
    
    components: {
      MescrollBody
    },
    
    mixins: [MescrollMixin],
    
    data() {
      return {
        list: getEmptyPaginateObj(),
        // 上拉加载配置
        upOption: {
          // 首次自动执行
          auto: true,
          // 每页数据的数量; 默认10
          page: { size: pageSize },
          // 数量要大于1条才显示无更多数据
          noMoreSize: 1,
        }
      }
    },
    watch: {
      isReflash(value) {
         if (value) {
             this.getGoodsList(1);
         }
      }
    },

    /**
     * 组件的方法列表
     * 更新属性和数据的方法与更新页面数据的方法类似
     */
    methods: {

      /**
       * 跳转商品详情页
       */
      onTargetGoods(goodsId) {
        this.$navTo(`pages/goods/detail`, { goodsId })
      },
      
      /**
       * 上拉加载的回调 (页面初始化时也会执行一次)
       * 其中page.num:当前页 从1开始, page.size:每页数据条数,默认10
       * @param {Object} page
       */
      upCallback(page) {
        const app = this
        // 设置列表数据
        app.getGoodsList(page.num)
          .then(list => {
              const curPageLen = list.content.length;
              const totalSize = list.totalElements;
              app.mescroll.endBySize(curPageLen, totalSize);
          })
          .catch(() => {
              app.mescroll.endErr();
          })
      },
      
      /**
       * 获取商品列表
       * @param {number} pageNo 页码
       */
      getGoodsList(pageNo) {
        const app = this
        console.log('pageNo==', pageNo);
        const param = { page: pageNo, pageSize: pageSize }
        return new Promise((resolve, reject) => {
          GoodsApi.search(param)
            .then(result => {
              // 合并新数据
              const newList = result.data;
              app.list.content = getMoreListData(newList, app.list, pageNo)
              resolve(newList)
            })
            .catch(reject)
        })
      }
    }
  }
</script>
<style lang="scss" scoped>
  .goods-container {
      .diy-goods {
        .goods-list {
          padding: 4rpx;
          box-sizing: border-box;
          .goods-item {
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
                line-height: 1.3;
                white-space: normal;
                color: #484848;
                font-size: 26rpx;
                font-weight: bold;
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

          &.display__slide {
            white-space: nowrap;
            font-size: 0;

            .goods-item {
              display: inline-block;
            }
          }

          &.display__list {
            .goods-item {
              float: left;
            }
          }

          &.column__2 {
            .goods-item {
              width: 50%;
            }
          }

          &.column__3 {
            .goods-item {
              width: 33.33333%;
            }
          }

          &.column__1 {
            .goods-item {
              width: 100%;
              height: 250rpx;
              margin-bottom: 10rpx;
              padding: 20rpx;
              box-sizing: border-box;
              background: #fff;
              line-height: 1.6;
              border: 1rpx #F5f5f5 solid;
              &:last-child {
                margin-bottom: 0;
              }
            }

            .goods-item_left {
              display: flex;
              width: 40%;
              background: #fff;
              align-items: center;

              .image {
                display: block;
                width: 220rpx;
                height: 200rpx;
                border-radius: 10rpx;
                border: 1rpx #cccccc solid;
              }
            }

            .goods-item_right {
              position: relative;
              width: 60%;
              .goods-name {
                margin-top: 20rpx;
                max-height: 69rpx;
                line-height: 1.3;
                white-space: normal;
                color: #484848;
                font-size: 30rpx;
                font-weight: bold;
                overflow: hidden;
                display: -webkit-box;
                -webkit-box-orient: vertical;
                -webkit-line-clamp: 2;
              }
            }

            .goods-item_desc {
              margin-top: 0rpx;
            }

            .desc-selling_point {
              width: 400rpx;
              font-size: 24rpx;
              color: #e49a3d;
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
                font-size: 33rpx;
                font-weight: bold;
              }

              .price_y {
                text-decoration: line-through;
              }
              
              .buy-now {
                  color: #FFFFFF;
                  float: right;
                  margin-right: 20rpx;
                  border: solid 1rpx $fuint-theme;
                  background: $fuint-theme;
                  padding: 8rpx 20rpx 8rpx 20rpx;
                  border-radius: 5rpx;
                  display: block;
              }
            }
          }
        }
      }
  }
</style>
