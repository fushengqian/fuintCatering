<template>
  <view class="container">
    <mescroll-body ref="mescrollRef" :sticky="true" @init="mescrollInit" :down="{ use: false }" :up="upOption"
      @up="upCallback">
      <view class="log-list">
        <view v-for="(item, index) in list.content" :key="index" class="log-item">
          <view class="item-left flex-box">
            <view class="rec-status">
              <text>{{ item.description ? item.description : '' }}</text>
              <text v-if="!item.description">{{ item.amount > 0 ? '增加金额' : '减少金额' }}</text>
            </view>
            <view class="rec-time">
              <text>{{ item.createTime | timeFormat('yyyy-mm-dd hh:MM') }}</text>
            </view>
          </view>
          <view class="item-right">
            <text>{{ item.amount > 0 ? '+' : '' }}{{ item.amount }}元</text>
          </view>
        </view>
      </view>
    </mescroll-body>
  </view>
</template>

<script>
  import MescrollBody from '@/components/mescroll-uni/mescroll-body.vue'
  import MescrollMixin from '@/components/mescroll-uni/mescroll-mixins'
  import * as LogApi from '@/api/balance'
  import { getEmptyPaginateObj, getMoreListData } from '@/utils/app'

  const pageSize = 20

  export default {
    components: {
      MescrollBody
    },
    mixins: [MescrollMixin],
    data() {
      return {
        // 余额账单明细列表
        list: getEmptyPaginateObj(),
        // 上拉加载配置
        upOption: {
          // 首次自动执行
          auto: true,
          // 每页数据的数量; 默认20
          page: { size: pageSize },
          // 数量要大于12条才显示无更多数据
          noMoreSize: 20,
          // 空布局
          empty: {
            tip: '亲，暂无账单明细.'
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
        app.getLogList(page.num)
          .then(list => {
            const curPageLen = list.content.length
            const totalSize = list.totalElements
            app.mescroll.endBySize(curPageLen, totalSize)
          })
          .catch(() => app.mescroll.endErr())
      },

      // 获取余额账单明细列表
      getLogList(pageNo = 1) {
        const app = this
        return new Promise((resolve, reject) => {
          LogApi.list({ page: pageNo })
            .then(result => {
              // 合并新数据
              const newList = result.data.data
              app.list.content = getMoreListData(newList, app.list, pageNo)
              resolve(newList)
            })
        })
      }
    }
  }
</script>

<style lang="scss" scoped>
  page,
  .container {
    background: #fff;
  }

  .log-list {
    padding: 0 30rpx;
  }

  .log-item {
    font-size: 28rpx;
    padding: 20rpx 20rpx;
    line-height: 1.8;
    border-bottom: 1rpx solid rgb(238, 238, 238);
    display: flex;
    justify-content: center;
    align-items: center;
  }
  .item-right {
      color: #000;
      font-weight: bold;
  }
  .rec-status {
    color: #333;
    .rec-time {
      color: rgb(160, 160, 160);
      font-size: 26rpx;
    }
  }
</style>
