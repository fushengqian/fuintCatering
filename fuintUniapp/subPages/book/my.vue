<template>
  <mescroll-body ref="mescrollRef" :sticky="true" @init="mescrollInit" :down="{ use: false }" :up="upOption" @up="upCallback">

    <!-- 分类列表tab -->
    <view class="tabs-wrapper">
      <scroll-view class="scroll-view" scroll-x>
        <view class="tab-item" :class="{ active: curId == '' }" @click="onSwitchTab('')">
          <view class="value"><text>全部</text></view>
        </view>
        <!-- tab列表 -->
        <view class="tab-item" :class="{ active: curId ==  item.key }" @click="onSwitchTab(item.key)"
          v-for="(item, index) in statusList" :key="index">
          <view class="value"><text>{{ item.name }}</text></view>
        </view>
      </scroll-view>
    </view>

    <!-- 预约列表 -->
    <view class="book-list">
      <view class="book-item" v-for="(item, index) in list.content" :key="index">
        <block>
          <view class="flex-box">
            <view class="book-item-title">
              <text>{{ item.bookName }}</text>
            </view>
            <view class="book-content">
                <view class="contacts">姓名：{{ item.contact }}</view>
                <view class="time">时间：{{ item.serviceDate }} {{ item.serviceTime }}</view>
            </view>
            <view class="book-item-footer m-top10">
              <text class="book-views f-24 col-8">{{ item.createTime | timeFormat('yyyy-mm-dd hh:MM') }}</text>
              <view class="btn btn-operate" v-if="item.status == 'A'" @click="onCancel(item.id)">取消</view>
              <view class="btn btn-view" @click="onView(item.id)">详情</view>
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
        // 状态列表
        statusList: [],
        // 预约列表
        list: getEmptyPaginateObj(),
        // 当前选中的分类id (0则代表首页)
        curId: '',
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
      if (options.status) {
          app.curId = options.status;
      }
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
        app.getMyBookList(page.num)
          .then(list => {
            const curPageLen = list.content.length;
            const totalSize = list.content.totalElements;
            app.mescroll.endBySize(curPageLen, totalSize);
          })
          .catch(() => app.mescroll.endErr());
      },

      /**
       * 获取预约列表
       * @param {Number} pageNo 页码
       */
      getMyBookList(pageNo = 1) {
        const app = this;
        return new Promise((resolve, reject) => {
          BookApi.myBookList({ status: app.curId, page: pageNo }, { load: false })
            .then(result => {
              // 合并新数据
              const newList = result.data;
              app.list.content = getMoreListData(newList, app.list, pageNo);
              app.statusList = result.data.statusList;
              resolve(newList);
            })
            .catch(result => reject());
        })
      },

      // 切换选择的分类
      onSwitchTab(status) {
        const app = this;
        // 切换当前的状态
        app.curId = status;
        // 刷新列表数据
        app.list = getEmptyPaginateObj();
        app.mescroll.resetUpScroll();
      },
      
      // 取消预约
      onCancel(myBookId) {
         const app = this;
         uni.showModal({
           title: "提示",
           content: "您确定要取消该预约吗?",
           success({ confirm }) {
             confirm && app.doCancel(myBookId)
           }
         });
      },
      
      // 确认取消预约
      doCancel(myBookId) {
        const app = this;
        BookApi.cancel(myBookId)
          .then(result => {
            app.$success("取消成功！")
            setTimeout(() => {
                app.getMyBookList(1);
            }, 1500)
          })
      },

      // 跳转详情页
      onView(myBookId) {
        this.$navTo('pages/book/bookDetail', { myBookId });
      }
    }

  }
</script>

<style lang="scss" scoped>
  /* 顶部选项卡 */
  .container {
    min-height: 100vh;
    background: #333;
  }

  .tabs-wrapper {
    position: sticky;
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
    padding: 0 15rpx;
    text-align: center;
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
    padding-top: 20rpx;
    line-height: 1;
    background: #f7f7f7;
  }

  .book-item {
    margin: 0rpx 10rpx 20rpx 10rpx;
    padding: 30rpx;
    background: #fff;
    border-radius: 20rpx;
    min-height: 280rpx;
    border: solid 1rpx #f5f5f5;
    &:last-child {
      margin-bottom: 0;
    }

    .book-item-title {
      font-size: 32rpx;
      color: #333;
      font-weight: bold;
      line-height: 40rpx;
    }
    .book-content {
        margin: 30rpx 0rpx 30rpx 0rpx;
        .contacts {
            margin-bottom: 20rpx;
        }
    }

    .book-item-footer {
      .btn {
          width: 120rpx;
          border-radius: 10rpx;
          padding: 18rpx;
          font-size: 28rpx;
          color: #fff;
          text-align: center;
          align-items: center;
          border: 1rpx solid #fff;
          float: right;
          border: solid 1rpx #fff;
          margin-left: 10rpx;
      }
      .btn-operate {
          background: linear-gradient(to right, #f9211c, #ff6335);
      }
      .btn-view {
          background: $fuint-theme;
      }
    }
  }
</style>
