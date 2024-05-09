<template>
  <view class="container">
    <mescroll-body ref="mescrollRef" :sticky="true" @init="mescrollInit" :down="{ use: false }" :up="upOption"
      @up="upCallback">
      <view class="help cont-box b-f" v-for="(item, index) in list.content" :key="index" @click="onTargetDetail(item.id)">
        <view class="title">
          <text>{{ item.title }}</text>
        </view>
        <view class="content">
          <text>{{ item.brief }}</text>
        </view>
      </view>
    </mescroll-body>
  </view>
</template>

<script>
  import MescrollBody from '@/components/mescroll-uni/mescroll-body.vue'
  import MescrollMixin from '@/components/mescroll-uni/mescroll-mixins'
  import * as HelpApi from '@/api/help'
  import { getEmptyPaginateObj, getMoreListData } from '@/utils/app'

  const pageSize = 15;

  export default {
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
          // 数量要大于3条才显示无更多数据
          noMoreSize: 3,
          // 空布局
          empty: {
            tip: '亲，暂无相关数据'
          }
        }
      }
    },

    /**
     * 生命周期函数--监听页面加载
     */
    onLoad(options) {},

    methods: {

      /**
       * 上拉加载的回调 (页面初始化时也会执行一次)
       * 其中page.num:当前页 从1开始, page.size:每页数据条数,默认10
       * @param {Object} page
       */
      upCallback(page) {
        const app = this
        // 设置列表数据
        app.getHelpList(page.num)
          .then(list => {
            const curPageLen = list.content.length;
            const totalSize = list.content.totalElements;
            app.mescroll.endBySize(curPageLen, totalSize);
          })
          .catch(() => app.mescroll.endErr())
      },

      // 获取帮助列表
      getHelpList(pageNo = 1) {
        const app = this
        return new Promise((resolve, reject) => {
          HelpApi.list({ page: pageNo })
            .then(result => {
              // 合并新数据
              const newList = result.data;
              app.list.content = getMoreListData(newList, app.list, pageNo);
              resolve(newList);
            }).catch(() => app.mescroll.endErr());
        })
      },
      
      // 跳转文章详情页
      onTargetDetail(articleId) {
        this.$navTo('pages/article/detail', { articleId });
      }
    }
  }
</script>

<style lang="scss" scoped>
  .help {
    border-bottom: 2rpx solid #f6f6f9;

    .title {
      font-size: 32rpx;
      color: #333;
      font-weight: bold;
      margin-bottom: 10rpx;
    }

    .content {
      font-size: 26rpx;
      color: #666;
    }
  }
</style>
