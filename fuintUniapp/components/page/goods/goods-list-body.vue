<template>
  <view class="goods-list" :class="[displayClass, columnClass]">
    <scroll-view :scroll-x="itemStyle.display === 'slide'">
      <view class="goods-item" v-for="(dataItem, index) in list" :key="index" @click="onClick(dataItem.id)">
        <!-- 单列商品 -->
        <block v-if="column === 1">
          <view class="dis-flex">
            <!-- 商品图片 -->
            <view class="goods-item_left">
              <image class="image" lazy-load :lazy-load-margin="0" :src="dataItem.logo"></image>
              <view class="member-tag" v-if="dataItem.gradeIds" style="top:0;left:auto;right:0;padding:4rpx 12rpx;font-size:20rpx;color:#fff;background:linear-gradient(135deg,#d4a843,#b8860b);border-radius:0 0 0 12rpx;z-index:5;">会员专属</view>
            </view>
            <view class="goods-item_right">
              <!-- 商品名称 -->
              <view v-if="showFields.includes('goodsName')" class="goods-name twoline-hide">
                <text>{{ dataItem.name }}</text>
              </view>
              <view class="goods-item_desc">
                <!-- 商品卖点 -->
                <view v-if="showFields.includes('sellingPoint')" class="desc-selling_point dis-flex">
                  <text class="oneline-hide">{{ dataItem.salePoint ? dataItem.salePoint : '' }}</text>
                </view>
                <!-- 商品销量 -->
                <view v-if="showFields.includes('goodsSales')" class="desc-goods_sales dis-flex">
                  <text>已售{{ dataItem.initSale ? dataItem.initSale : 0 }}件</text>
                </view>
                <!-- 商品价格 -->
                <view class="desc_footer">
                  <text v-if="showFields.includes('goodsPrice')" class="price_x">¥{{ dataItem.price ? dataItem.price : '0.00' }}</text>
                  <text class="price_y col-9" v-if="showFields.includes('linePrice') && dataItem.linePrice > 0">¥{{ dataItem.linePrice }}</text>
                  <view class="buy-now">去购买</view>
                </view>
              </view>
            </view>
          </view>
        </block>
        <!-- 多列商品 -->
        <block v-else>
          <view class="goods-info">
            <!-- 商品图片 -->
            <view class="goods-image">
              <image class="image" lazy-load :lazy-load-margin="0" mode="aspectFill" :src="dataItem.logo"></image>
              <view class="member-tag" v-if="dataItem.gradeIds" style="top:0;left:auto;right:0;padding:4rpx 12rpx;font-size:20rpx;color:#fff;background:linear-gradient(135deg,#d4a843,#b8860b);border-radius:0 0 0 12rpx;z-index:5;">会员专属</view>
            </view>
            <view class="detail">
              <!-- 商品标题 -->
              <view v-if="showFields.includes('goodsName')" class="goods-name twoline-hide">
                {{ dataItem.name }}
              </view>
              <!-- 商品卖点 -->
              <view v-if="showFields.includes('sellingPoint')" class="desc-selling_point dis-flex">
                <text class="oneline-hide">{{ dataItem.salePoint ? dataItem.salePoint : '' }}</text>
              </view>
              <!-- 商品价格 -->
              <view class="detail-price oneline-hide">
                <text v-if="showFields.includes('goodsPrice')" class="goods-price f-30 col-m">￥{{ dataItem.price }}</text>
                <text v-if="showFields.includes('linePrice') && dataItem.linePrice > 0" class="line-price col-9 f-24">￥{{ dataItem.linePrice }}</text>
                <text v-if="showFields.includes('goodsSales')" class="sales">已售{{ dataItem.initSale ? dataItem.initSale : 0 }}件</text>
              </view>
            </view>
          </view>
        </block>
      </view>
    </scroll-view>
  </view>
</template>

<script>
  export default {
    name: 'GoodsListBody',
    props: {
      itemStyle: {
        type: Object,
        default: () => ({})
      },
      list: {
        type: Array,
        default: () => []
      }
    },
    computed: {
      displayClass() {
        return `display__${this.itemStyle.display || 'list'}`
      },
      column() {
        return parseInt(this.itemStyle.column, 10) || 2
      },
      columnClass() {
        return `column__${this.column}`
      },
      showFields() {
        return (this.itemStyle && this.itemStyle.show) || ['goodsName', 'goodsPrice', 'linePrice', 'goodsSales']
      }
    },
    methods: {
      onClick(goodsId) {
        this.$emit('click', goodsId)
      }
    }
  }
</script>

<style lang="scss" scoped>
  .goods-list {
    padding: 0rpx 12rpx 12rpx 12rpx;
    box-sizing: border-box;
    .goods-item {
      box-sizing: border-box;
      padding: 0rpx 12rpx 12rpx 12rpx;
      background: #f5f5f5;
      .goods-info {
        background: #ffffff;
        border-radius: 16rpx;
        padding: 2px;
        overflow: hidden;
        .goods-image {
          position: relative;
          width: 100%;
          height: 0;
          padding-bottom: 100%;
          overflow: hidden;
          text-align: center;
          &:after {
            content: '';
            display: block;
            margin-top: 100%;
          }
          .image {
            position: absolute;
            box-sizing: border-box;
            padding: 10rpx;
            width: 100%;
            height: 100%;
            top: 0;
            left: 0;
            -o-object-fit: cover;
            object-fit: cover;
            border-radius: 40rpx;
          }
          .member-tag {
            position: absolute;
            top: 0;
            right: 0;
            padding: 4rpx 12rpx;
            font-size: 20rpx;
            color: #fff;
            background: linear-gradient(135deg, #d4a843, #b8860b);
            border-radius: 0 0 0 12rpx;
            z-index: 5;
          }
        }
      }

      .detail {
        padding: 8rpx;
        background: #ffffff;
        border-bottom-left-radius: 16rpx;
        border-bottom-right-radius: 16rpx;
        overflow: hidden;
        height: 180rpx;
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
            font-size: 34rpx;
            font-weight: bold;
            color: var(--theme-price) !important;
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
      .desc-selling_point {
        min-height: 40rpx;
        line-height: 40rpx;
        max-width: 400rpx;
        font-size: 24rpx;
        color: #e49a3d;
        overflow: hidden;
      }
      
      .sales {
        color: #999;
        font-size: 24rpx;
        margin-top: 10rpx;
        float: right;
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
        &:last-child {
          margin-bottom: 0;
        }
        &:first-child {
          margin-top: 8rpx;
        }
      }

      .goods-item_left {
        position: relative;
        display: flex;
        width: 40%;
        background: #fff;
        align-items: center;

        .image {
          display: block;
          width: 220rpx;
          height: 200rpx;
          border-radius: 10rpx;
        }
        .member-tag {
          position: absolute;
          top: 0;
          right: 0;
          padding: 4rpx 12rpx;
          font-size: 20rpx;
          color: #fff;
          background: linear-gradient(135deg, #d4a843, #b8860b);
          border-radius: 0 0 0 12rpx;
          z-index: 5;
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
          color: var(--theme-price);
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
          border: solid 1rpx var(--theme-primary);
          background: var(--theme-primary);
          padding: 8rpx 20rpx 8rpx 20rpx;
          border-radius: 5rpx;
          display: block;
        }
      }
    }
  }
</style>
