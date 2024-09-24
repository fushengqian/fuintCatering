<template>
  <mescroll-body ref="mescrollRef" :sticky="true" @init="mescrollInit" :down="{ use: false }" :up="upOption" @up="upCallback">

    <!-- 分类列表tab -->
    <view class="tabs-wrapper">
      <scroll-view class="scroll-view" scroll-x>
        <view class="tab-item" :class="{ active: curId ==  0 }" @click="onSwitchTab(0)">
          <view class="value"><text>全部</text></view>
        </view>
        <!-- 分类列表 -->
        <view class="tab-item" :class="{ active: curId ==  item.id }" @click="onSwitchTab(item.id)"
          v-for="(item, index) in categoryList" :key="index">
          <view class="value"><text>{{ item.name }}</text></view>
        </view>
      </scroll-view>
    </view>

    <!-- 预约列表 -->
    <view class="book-list">
      <view class="book-item show-type" v-for="(item, index) in list.content" :key="index" @click="onTargetDetail(item.id)">
        <block>
          <view class="book-item-image">
            <image class="image" :src="item.logo"></image>
          </view>
          <view class="book-item-left flex-box">
            <view class="book-item-title twolist-hidden">
              <text>{{ item.name }}</text>
            </view>
            <view class="book-item-footer m-top10">
              <text class="book-views">{{ item.description }}</text>
            </view>
          </view>
        </block>
      </view>
    </view>
  </mescroll-body>
</template>

<script>
  import MescrollBody from '@/components/mescroll-uni/mescroll-body.vue'
  import MescrollMixin from '@/components/mescroll-uni/mescroll-mixins'
  import * as BookApi from '@/api/book'
  import { getEmptyPaginateObj, getMoreListData } from '@/utils/app'

  const pageSize = 15

  export default {
    components: {
      MescrollBody
    },
    mixins: [MescrollMixin],
    data() {
      return {
        // 分类列表
        categoryList: [],
        // 预约列表
        list: getEmptyPaginateObj(),
        // 当前选中的分类id (0则代表首页)
        curId: 0,
        // 上拉加载配置
        upOption: {
          // 首次自动执行
          auto: true,
          // 每页数据的数量; 默认10
          page: { size: pageSize },
          // 数量要大于3条才显示无更多数据
          noMoreSize: 3,
        }
      }
    },

    /**
     * 生命周期函数--监听页面加载
     */
    onLoad(options) {
      const app = this;
      if (options.categoryId) {
          app.curId = options.categoryId;
      }
      // 获取分类数据
      app.getCategoryList();
    },

    methods: {

      /**
       * 上拉加载的回调 (页面初始化时也会执行一次)
       * 其中page.num:当前页 从1开始, page.size:每页数据条数,默认10
       * @param {Object} page
       */
      upCallback(page) {
        const app = this;
        // 设置列表数据
        app.getBookList(page.num)
          .then(list => {
            const curPageLen = list.content.length;
            const totalSize = list.content.totalElements;
            app.mescroll.endBySize(curPageLen, totalSize);
          })
          .catch(() => app.mescroll.endErr());
      },

      // 获取预约分类数据
      getCategoryList() {
        const app = this;
        BookApi.cateList()
          .then(result => {
              app.categoryList = result.data.cateList;
          })
      },

      /**
       * 获取预约项目列表
       * @param {Number} pageNo 页码
       */
      getBookList(pageNo = 1) {
        const app = this
        return new Promise((resolve, reject) => {
          BookApi.list({ cateId: app.curId, page: pageNo }, { load: false })
            .then(result => {
              // 合并新数据
              const newList = result.data;
              app.list.content = getMoreListData(newList, app.list, pageNo);
              resolve(newList);
            })
            .catch(result => reject());
        })
      },

      // 切换选择的分类
      onSwitchTab(categoryId = 0) {
        const app = this;
        // 切换当前的分类ID
        app.curId = categoryId;
        // 刷新列表数据
        app.list = getEmptyPaginateObj();
        app.mescroll.resetUpScroll();
      },

      // 跳转预约详情页
      onTargetDetail(bookId) {
        this.$navTo('pages/book/detail', { bookId });
      }
    },

    /**
     * 分享当前页面
     */
    onShareAppMessage() {
      return {
        title: '预约项目',
        path: "/pages/book/index?" + this.$getShareUrlParams()
      }
    },

    /**
     * 分享到朋友圈
     * 本接口为 Beta 版本，暂只在 Android 平台支持，详见分享到朋友圈 (Beta)
     * https://developers.weixin.qq.com/miniprogram/dev/framework/open-ability/share-timeline.html
     */
    onShareTimeline() {
      return {
        title: '预约项目',
        path: "/pages/book/index?" + this.$getShareUrlParams()
      }
    }

  }
</script>

<style lang="scss" scoped>
  /* 顶部选项卡 */

  .container {
    min-height: 100vh;
  }

  .tabs-wrapper {
    position: sticky;
    top: 1rpx;
    display: flex;
    width: 100%;
    height: 88rpx;
    color: #333;
    font-size: 28rpx;
    background: #fff;
    border-bottom: 1rpx solid #e4e4e4;
    z-index: 100;
    overflow: hidden;
    white-space: nowrap;
  }

  .tab-item {
    display: inline-block;
    padding: 0 10rpx;
    text-align: center;
    min-width: 10%;
    height: 87rpx;
    line-height: 88rpx;
    box-sizing: border-box;

    .value {
      height: 100%;
    }

    &.active .value {
      color: #fd4a5f;
      border-bottom: 4rpx solid #fd4a5f;
      font-weight: bold;
    }
  }

  /* 预约列表 */
  .book-list {
    padding-top: 0rpx;
    line-height: 1;
    background: #f7f7f7;
  }

  .book-item {
    padding: 40rpx;
    background: #fff;
    margin: 20rpx;
    border-radius: 15rpx;

    &:last-child {
      margin-bottom: 0;
    }

    .book-item-title {
      max-height: 80rpx;
      font-size: 32rpx;
      font-weight: bold;
      color: #333;
    }

    .book-item-image .image {
      display: block;
      border-radius: 16rpx;
      height: 160rpx;
      width: 200rpx;
      border: 2rpx solid #cccccc;
    }
  }

  .show-type {
    display: flex;
    .book-item-left {
      padding-left: 20rpx;
    }
    .book-item-title {
      font-size: 32rpx;
    }
    .book-item-footer {
        line-height: 40rpx;
        max-height: 120rpx;
        text-overflow: ellipsis;
        overflow: hidden;
    }
  }
</style>
