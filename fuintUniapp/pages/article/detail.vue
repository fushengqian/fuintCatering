<template>
  <view v-if="!isLoading" class="container b-f p-b">
    <view class="article-title">
      <text>{{ detail.title }}</text>
    </view>
    <view class="article-little dis-flex flex-x-between m-top10">
      <view class="article-little-left">
        <text class="article-views f-24 col-8">{{ detail.click }}次浏览</text>
      </view>
      <view class="article-little-right">
        <text class="article-views f-24 col-8">{{ detail.createTime | timeFormat('yyyy-mm-dd hh:MM') }}</text>
      </view>
    </view>
    <view class="article-content m-top20">
      <jyf-parser :html="detail.description"></jyf-parser>
    </view>
    <!-- 快捷导航 -->
    <shortcut />
  </view>
</template>

<script>
  import jyfParser from '@/components/jyf-parser/jyf-parser'
  import Shortcut from '@/components/shortcut'
  import * as ArticleApi from '@/api/article'

  export default {
    components: {
      Shortcut
    },
    data() {
      return {
        // 当前文章ID
        articleId: null,
        // 加载中
        isLoading: true,
        // 当前文章详情
        detail: null
      }
    },

    /**
     * 生命周期函数--监听页面加载
     */
    onLoad(options) {
      // 记录文章ID
      this.articleId = options.articleId;
      // 获取文章详情
      this.getArticleDetail();
    },

    methods: {

      // 获取文章详情
      getArticleDetail() {
        const app = this
        app.isLoading = true
        ArticleApi.detail(app.articleId)
          .then(result => {
              app.detail = result.data.articleInfo;
          })
          .finally(() => app.isLoading = false)
      }
    }
  }
</script>

<style lang="scss" scoped>
  .container {
    min-height: 100vh;
    padding: 30rpx;
    background: #fff;
  }
  .article-title {
     margin-bottom: 20rpx;
     font-size: 35rpx;
     display: block;
  }
  .article-content {
     font-size: 28rpx;
  }
</style>
