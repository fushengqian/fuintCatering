<template>
  <view class="container">
    <view class="comment-list" v-if="list.length">
      <view class="comment-item" v-for="(item, index) in list" :key="index">
        <!-- 商品信息 -->
        <view class="goods-main">
          <view class="goods-image">
            <image class="image" :src="item.goodsLogo" mode="aspectFill"></image>
          </view>
          <view class="goods-content">
            <view class="goods-title onelist-hidden"><text>{{ item.goodsName }}</text></view>
            <view class="stars">
              <text
                v-for="s in 5"
                :key="s"
                class="star"
                :class="{ active: item.score >= s }"
                >★</text>
            </view>
          </view>
          <view class="goods-action">
            <text class="del-btn" @click="handleDelete(item)">删除</text>
          </view>
        </view>

        <!-- 评价内容 -->
        <view class="comment-content" v-if="item.content">
          <text>{{ item.content }}</text>
        </view>

        <!-- 评价图片 -->
        <view class="comment-images" v-if="item.images && item.images.length">
          <image
            class="c-image"
            v-for="(img, imgIndex) in item.images"
            :key="imgIndex"
            :src="img"
            mode="aspectFill"
            @click="previewImage(item.images, imgIndex)"
          ></image>
        </view>

        <!-- 商家回复 -->
        <view class="reply-box" v-if="item.replyContent">
          <text class="reply-label">商家回复：</text>
          <text class="reply-content">{{ item.replyContent }}</text>
        </view>

        <view class="comment-time">
          <text>{{ item.createTime }}</text>
        </view>
      </view>
    </view>

    <!-- 空提示 -->
    <view class="empty-box" v-else>
      <text class="empty-text">暂无评价，去订单里评价商品吧～</text>
    </view>
  </view>
</template>

<script>
  import * as GoodsCommentApi from '@/api/goodsComment'

  export default {
    data() {
      return {
        // 正在加载
        isLoading: false,
        // 评价列表
        list: [],
        // 分页
        page: 1,
        pageSize: 10,
        // 是否还有更多
        hasMore: true
      }
    },

    onLoad() {
      this.getList(true)
    },

    // 下拉刷新
    onPullDownRefresh() {
      this.getList(true).finally(() => uni.stopPullDownRefresh())
    },

    // 上拉加载
    onReachBottom() {
      if (this.hasMore && !this.isLoading) {
        this.getList(false)
      }
    },

    methods: {
      // 获取我的评价列表
      getList(reset) {
        const app = this
        if (reset) {
          app.page = 1
          app.hasMore = true
        }
        app.isLoading = true
        return GoodsCommentApi.myList({ page: app.page, pageSize: app.pageSize })
          .then(result => {
            const records = result.data.content || []
            app.list = reset ? records : app.list.concat(records)
            app.hasMore = app.page < (result.data.totalPages || 1)
            app.page++
          })
          .finally(() => {
            app.isLoading = false
          })
      },

      // 预览图片
      previewImage(images, index) {
        uni.previewImage({ urls: images, current: index })
      },

      // 删除评价
      handleDelete(item) {
        const app = this
        uni.showModal({
          title: '提示',
          content: '确认删除该评价吗？',
          success({ confirm }) {
            if (confirm) {
              GoodsCommentApi.del(item.id).then(() => {
                app.$toast('删除成功')
                app.getList(true)
              })
            }
          }
        })
      }
    }
  }
</script>

<style lang="scss" scoped>
  .container {
    padding: 30rpx 0;
    min-height: 100vh;
    background: #f5f5f5;
  }

  .comment-item {
    width: 94%;
    background: #fff;
    padding: 24rpx;
    box-shadow: 0 1rpx 5rpx 0px rgba(0, 0, 0, 0.05);
    margin: 0 auto 24rpx auto;
    border-radius: 16rpx;
  }

  .goods-main {
    display: flex;
    align-items: center;

    .goods-image {
      width: 100rpx;
      height: 100rpx;

      .image {
        display: block;
        width: 100%;
        height: 100%;
        border-radius: 8rpx;
      }
    }

    .goods-content {
      flex: 1;
      padding-left: 16rpx;

      .goods-title {
        font-size: 26rpx;
        max-height: 40rpx;
      }

      .stars {
        margin-top: 8rpx;

        .star {
          font-size: 26rpx;
          color: #e0e0e0;

          &.active {
            color: #ffb800;
          }
        }
      }
    }

    .goods-action {
      .del-btn {
        font-size: 24rpx;
        color: #999;
      }
    }
  }

  .comment-content {
    padding: 16rpx 0 0 0;
    font-size: 26rpx;
    color: #444;
    line-height: 1.6;
  }

  .comment-images {
    display: flex;
    flex-wrap: wrap;
    margin-top: 16rpx;

    .c-image {
      width: 150rpx;
      height: 150rpx;
      border-radius: 8rpx;
      margin-right: 12rpx;
      margin-bottom: 12rpx;
    }
  }

  .reply-box {
    background: #f7f8fa;
    border-radius: 8rpx;
    padding: 12rpx 16rpx;
    margin-top: 16rpx;
    font-size: 24rpx;

    .reply-label {
      color: #409eff;
    }

    .reply-content {
      color: #666;
    }
  }

  .comment-time {
    margin-top: 12rpx;
    font-size: 22rpx;
    color: #bbb;
    text-align: right;
  }

  .empty-box {
    padding-top: 200rpx;
    text-align: center;

    .empty-text {
      font-size: 26rpx;
      color: #999;
    }
  }
</style>
