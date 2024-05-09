<template>
  <!-- 文章组 -->
  <view class="diy-article">
    <view class="article-item" :class="[`show-type__${item.show_type}`]" v-for="(item, index) in dataList" :key="index"
      @click="onTargetDetail(item.article_id)">
      <!-- 小图模式 -->
      <block v-if="item.show_type == 10">
        <view class="article-item__left flex-box">
          <view class="article-item__title twolist-hidden">
            <text>{{ item.title }}</text>
          </view>
          <view class="article-item__footer m-top10">
            <text class="article-views f-24 col-8">{{ item.show_views }}次浏览</text>
          </view>
        </view>
        <view class="article-item__image">
          <image class="image" mode="widthFix" :src="item.image_url"></image>
        </view>
      </block>
      <!-- 大图模式 -->
      <block v-if="item.show_type == 20">
        <view class="article-item__title twolist-hidden">
          <text>{{ item.title }}</text>
        </view>
        <view class="article-item__image m-top20">
          <image class="image" mode="widthFix" :src="item.image_url"></image>
        </view>
        <view class="article-item__footer m-top10">
          <text class="article-views f-24 col-8">{{ item.show_views }}次浏览</text>
        </view>
      </block>
    </view>
  </view>
</template>

<script>
  export default {
    name: "Article",
    /**
     * 组件的属性列表
     * 用于组件自定义设置
     */
    props: {
      itemIndex: String,
      params: Object,
      dataList: Array
    },

    /**
     * 组件的方法列表
     * 更新属性和数据的方法与更新页面数据的方法类似
     */
    methods: {

      /**
       * 跳转文章详情页
       */
      onTargetDetail(id) {
        uni.navigateTo({
          url: '/pages/article/detail?articleId=' + id
        })
      }

    }

  }
</script>

<style lang="scss" scoped>
  .diy-article {
    background: #f7f7f7;

    .article-item {
      margin-bottom: 20rpx;
      padding: 30rpx;
      background: #fff;

      &:last-child {
        margin-bottom: 0;
      }

      .article-item__title {
        max-height: 80rpx;
        font-size: 30rpx;
        color: #333;
      }

      .article-item__image .image {
        display: block;
      }

    }



  }

  /* 小图模式 */

  .show-type__10 {
    display: flex;

    .article-item__left {
      padding-right: 20rpx;
    }

    .article-item__title {
      // min-height: 72rpx;
    }

    .article-item__image .image {
      width: 240rpx;
    }

  }

  /* 大图模式 */

  .show-type__20 .article-item__image .image {
    width: 100%;
  }
</style>
