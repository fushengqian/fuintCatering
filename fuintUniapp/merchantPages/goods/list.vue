<template>
  <mescroll-body ref="mescrollRef" :sticky="true" @init="mescrollInit" :down="{ native: true }" @down="downCallback"
    :up="upOption" @up="upCallback">

    <!-- 页面标题栏 -->
    <view class="page-header">
      <view class="header-title">商品管理</view>
      <view class="header-add" @click="onAdd">
        <text class="iconfont icon-add"></text>
        <text>新增商品</text>
      </view>
    </view>

    <!-- 搜索栏 -->
    <view class="search-wrapper">
      <view class="search-input">
        <view class="search-input-wrapper">
          <view class="left">
            <text class="search-icon iconfont icon-sousuo"></text>
          </view>
          <view class="right">
            <input v-model="keyword" class="input" placeholder="请输入商品名称..." type="text" @confirm="doSearch"></input>
          </view>
        </view>
      </view>
      <view class="search-button">
        <button class="button" @click="doSearch">搜索</button>
      </view>
    </view>

    <!-- 商品列表 -->
    <view class="goods-list clearfix" :class="['column-1']">
      <view class="goods-item" v-for="(item, index) in list" :key="index" @click="onEdit(item.id)">
        <view class="dis-flex">
          <!-- 商品图片 -->
          <view class="goods-item_left">
            <image class="image" :src="getImageUrl(item.logo)" mode="aspectFill"></image>
          </view>
          <view class="goods-item_right">
            <!-- 商品名称 -->
            <view class="goods-name twolist-hidden">
              <text>{{ item.name }}</text>
            </view>
            <view class="goods-item_desc">
              <view class="goods-attr">
                <view class="attr-l">
                  <view class="goods-price">
                    <text class="price_x">¥{{ item.price }}</text>
                    <text v-if="item.linePrice > 0" class="price_y">¥{{ item.linePrice }}</text>
                  </view>
                  <view class="goods-meta">
                    <text class="meta-stock">库存: {{ item.stock || 0 }}</text>
                    <text :class="['meta-status', item.status === 'A' ? 'status-on' : 'status-off']">
                      {{ item.status === 'A' ? '上架中' : '已下架' }}
                    </text>
                  </view>
                </view>
                <view class="attr-r">
                  <view class="btn-edit">编辑</view>
                </view>
              </view>
            </view>
          </view>
        </view>

        <!-- 操作按钮 -->
        <view class="goods-actions">
          <text class="action-btn action-edit" @click.stop="onEdit(item.id)">编辑</text>
          <text v-if="item.status === 'A'" class="action-btn action-off" @click.stop="onToggleStatus(item, 'N')">下架</text>
          <text v-else class="action-btn action-on" @click.stop="onToggleStatus(item, 'A')">上架</text>
          <text class="action-btn action-delete" @click.stop="onDelete(item)">删除</text>
        </view>
      </view>
    </view>

    <!-- 空数据 -->
    <view v-if="!isLoading && list.length === 0" class="empty-wrap">
      <view class="empty-icon iconfont icon-wushuju"></view>
      <view class="empty-text">暂无商品</view>
      <button class="empty-btn" @click="onAdd">新增商品</button>
    </view>

  </mescroll-body>
</template>

<script>
  import MescrollBody from '@/components/mescroll-uni/mescroll-body.vue'
  import MescrollMixin from '@/components/mescroll-uni/mescroll-mixins'
  import * as GoodsApi from '@/api/merchant/goods'

  const pageSize = 15

  export default {
    components: {
      MescrollBody
    },
    mixins: [MescrollMixin],

    data() {
      return {
        keyword: '',
        list: [],
        imagePath: '',
        isLoading: false,
        upOption: {
          auto: true,
          page: { size: pageSize },
          noMoreSize: 4,
        }
      }
    },

    onShow() {
      this.getGoodsList(1)
    },

    methods: {
      // 获取图片完整URL（兼容已有完整URL和相对路径）
      getImageUrl(logo) {
        if (!logo) return ''
        if (logo.startsWith('http://') || logo.startsWith('https://')) {
          return logo
        }
        return (this.imagePath || '') + logo
      },

      // 上拉加载
      upCallback(page) {
        const app = this
        app.getGoodsList(page.num)
          .then(list => {
            const curPageLen = list ? list.length : 0
            const totalSize = app.totalSize || 0
            app.mescroll.endBySize(curPageLen, totalSize)
          })
          .catch(() => app.mescroll.endErr())
      },

      // 搜索
      doSearch() {
        this.mescroll.resetUpScroll()
      },

      // 获取商品列表
      getGoodsList(pageNo = 1) {
        const app = this
        app.isLoading = true
        const param = {
          keyword: app.keyword,
          page: pageNo,
          pageSize: pageSize
        }

        return new Promise((resolve, reject) => {
          GoodsApi.goodsList(param)
            .then(result => {
              app.isLoading = false
              app.imagePath = result.data.imagePath || ''
              const newList = result.data.paginationResponse || {}
              app.totalSize = newList.totalElements || 0

              if (pageNo === 1) {
                app.list = newList.content || []
              } else {
                app.list = app.list.concat(newList.content || [])
              }
              resolve(newList.content)
            })
            .catch(err => {
              app.isLoading = false
              reject(err)
            })
        })
      },

      // 新增商品
      onAdd() {
        this.$navTo('merchantPages/goods/add')
      },

      // 编辑商品
      onEdit(goodsId) {
        this.$navTo(`merchantPages/goods/add?goodsId=${goodsId}`)
      },

      // 上架/下架
      onToggleStatus(item, status) {
        const tip = status === 'A' ? '确认上架该商品？' : '确认下架该商品？'
        uni.showModal({
          title: '提示',
          content: tip,
          success: (res) => {
            if (res.confirm) {
              GoodsApi.updateGoodsStatus({ id: item.id, status: status })
                .then(() => {
                  uni.showToast({ title: '操作成功', icon: 'success' })
                  this.mescroll.resetUpScroll()
                })
                .catch(() => {
                  uni.showToast({ title: '操作失败', icon: 'none' })
                })
            }
          }
        })
      },

      // 删除商品
      onDelete(item) {
        uni.showModal({
          title: '提示',
          content: '确认删除该商品？删除后不可恢复',
          success: (res) => {
            if (res.confirm) {
              GoodsApi.updateGoodsStatus({ id: item.id, status: 'D' })
                .then(() => {
                  uni.showToast({ title: '删除成功', icon: 'success' })
                  this.mescroll.resetUpScroll()
                })
                .catch(() => {
                  uni.showToast({ title: '删除失败', icon: 'none' })
                })
            }
          }
        })
      }
    }
  }
</script>

<style lang="scss" scoped>
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 30rpx 30rpx 20rpx;
    background: #fff;

    .header-title {
      font-size: 32rpx;
      font-weight: bold;
      color: #333;
    }

    .header-add {
      display: flex;
      align-items: center;
      font-size: 26rpx;
      color: $fuint-theme;
      font-weight: bold;

      .iconfont {
        margin-right: 8rpx;
        font-size: 30rpx;
      }
    }
  }

  .search-wrapper {
    display: flex;
    height: 80rpx;
    margin-top: 20rpx;
    padding: 0rpx 20rpx;
  }

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
          color: #666;
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
        }
      }
    }
  }

  .search-button {
    width: 20%;
    box-sizing: border-box;

    .button {
      line-height: 78rpx;
      height: 78rpx;
      font-size: 28rpx;
      border-radius: 0 20px 20px 0;
      background: $fuint-theme;
	  color: #ffffff;
    }
  }

  .goods-list {
    padding: 20rpx;
    box-sizing: border-box;
  }

  .goods-list.column-1 {
    .goods-item {
      width: 100%;
      margin-bottom: 16rpx;
      padding: 20rpx;
      box-sizing: border-box;
      background: #fff;
      border-radius: 16rpx;
      line-height: 1.6;

      &:last-child {
        margin-bottom: 0;
      }
    }

    .goods-item_left {
      display: flex;
      width: 180rpx;
      height: 180rpx;
      background: #f5f5f5;
      border-radius: 12rpx;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;

      .image {
        display: block;
        width: 100%;
        height: 100%;
        border-radius: 12rpx;
      }
    }

    .goods-item_right {
      position: relative;
      flex: 1;
      margin-left: 20rpx;

      .goods-name {
        height: 44rpx;
        line-height: 1.3;
        white-space: normal;
        color: #484848;
        font-size: 30rpx;
      }
    }

    .goods-item_desc {
      margin-top: 10rpx;

      .goods-attr {
        display: flex;
        justify-content: space-between;

        .attr-l {
          flex: 1;
        }

        .attr-r {
          display: flex;
          align-items: flex-end;
        }
      }
    }

    .goods-price {
      font-size: 28rpx;
      margin-bottom: 8rpx;

      .price_x {
        color: #f03c3c;
        font-size: 34rpx;
        font-weight: bold;
        margin-right: 10rpx;
      }

      .price_y {
        text-decoration: line-through;
        color: #999;
        font-size: 24rpx;
      }
    }

    .goods-meta {
      display: flex;
      align-items: center;
      gap: 12rpx;

      .meta-stock {
        font-size: 22rpx;
        color: #999;
      }

      .meta-status {
        font-size: 22rpx;
        padding: 2rpx 12rpx;
        border-radius: 4rpx;

        &.status-on {
          color: #07c160;
          background: #e8f8ef;
        }

        &.status-off {
          color: #999;
          background: #f5f5f5;
        }
      }
    }

    .btn-edit {
      height: 50rpx;
      width: 108rpx;
      padding: 0rpx 10rpx;
      line-height: 50rpx;
      text-align: center;
      border: 1px solid $fuint-theme;
      border-radius: 40rpx;
      color: #fff;
      background: $fuint-theme;
      font-size: 22rpx;
    }
  }

  .goods-actions {
    display: flex;
    justify-content: flex-end;
    margin-top: 16rpx;
    padding-top: 16rpx;
    border-top: 1rpx solid #f0f0f0;
    gap: 16rpx;

    .action-btn {
      font-size: 24rpx;
      padding: 8rpx 24rpx;
      border-radius: 30rpx;
      border: 1rpx solid #eee;

      &.action-edit {
        color: $fuint-theme;
        border-color: $fuint-theme;
      }

      &.action-on {
        color: #07c160;
        border-color: #07c160;
      }

      &.action-off {
        color: #ff976a;
        border-color: #ff976a;
      }

      &.action-delete {
        color: #ee0a24;
        border-color: #ee0a24;
      }
    }
  }

  .empty-wrap {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding-top: 200rpx;

    .empty-icon {
      font-size: 120rpx;
      color: #ddd;
    }

    .empty-text {
      margin-top: 20rpx;
      font-size: 28rpx;
      color: #999;
    }

    .empty-btn {
      margin-top: 30rpx;
      width: 220rpx;
      height: 64rpx;
      line-height: 64rpx;
      text-align: center;
      font-size: 28rpx;
      color: #fff;
      border-radius: 32rpx;
      background: $fuint-theme;
      border: none;
    }
  }
</style>
