<template>
  <view class="container">
    <mescroll-body ref="mescrollRef" :sticky="true" @init="mescrollInit" :down="{ native: true }" @down="downCallback"
      :up="upOption" @up="upCallback">

      <!-- tab栏 -->
      <u-tabs :list="tabs" :is-scroll="false" :current="curTab" active-color="#FA2209" :duration="0.2"
        @change="onChangeTab" />

      <!-- 转赠记录 -->
      <view class="widget-list">
        <view class="widget-detail" v-for="(item, index) in list.content" :key="index">
          <view class="row-block dis-flex flex-y-center">
            <view class="flex-box">{{ item.createTime }}</view>
            <view class="flex-box t-r">
              <text class="mobile">{{ item.mobile.substr(0, 3) + '****' + item.mobile.substr(7) }}</text>
            </view>
          </view>
          <view class="detail-goods row-block dis-flex">
            <view class="goods-right flex-box">
              <view class="goods-name">
                <text class="twolist-hidden">{{ item.couponNames }}</text>
              </view>
              <view class="goods-props clearfix">
                <view class="goods-props-item">
                  <text>￥{{ item.money }}</text>
                </view>
              </view>
              <view class="goods-num t-r">
                <text class="f-26 col-8">×{{ item.num }}</text>
              </view>
            </view>
          </view>
          <view class="detail-order row-block">
            <view class="item dis-flex flex-x-end flex-y-center">
              <text class="">转赠总金额：</text>
              <text class="col-m">￥{{ item.money * item.num }}</text>
            </view>
          </view>
        </view>
      </view>
    </mescroll-body>
  </view>
</template>

<script>
  import MescrollBody from '@/components/mescroll-uni/mescroll-body.vue'
  import MescrollMixin from '@/components/mescroll-uni/mescroll-mixins'
  import Empty from '@/components/empty'
  import { getEmptyPaginateObj, getMoreListData } from '@/utils/app'
  import * as giveApi from '@/api/give'

  // 每页记录数量
  const pageSize = 15

  // tab栏数据
  const tabs = [{
    name: '收到',
    value: 'gived'
  }, {
    name: '赠出',
    value: 'give'
  }]

  export default {
    components: {
      MescrollBody,
      Empty
    },
    mixins: [MescrollMixin],
    data() {
      return {
        // 订单列表数据
        list: getEmptyPaginateObj(),
        // 正在加载
        isLoading: false,
        // tabs栏数据
        tabs,
        // 当前标签索引
        curTab: 0,
        // 上拉加载配置
        upOption: {
          // 首次自动执行
          auto: true,
          // 每页数据的数量; 默认10
          page: { size: pageSize },
          // 数量要大于2条才显示无更多数据
          noMoreSize: 2,
          // 空布局
          empty: {
            tip: '亲，暂无转赠记录'
          }
        },
        // 控制首次触发onShow事件时不刷新列表
        canReset: false,
      }
    },

    /**
     * 生命周期函数--监听页面加载
     */
    onLoad(options) {
       // empty
    },

    /**
     * 生命周期函数--监听页面显示
     */
    onShow() {
      this.canReset && this.onRefreshList()
      this.canReset = true
    },

    methods: {

      /**
       * 上拉加载的回调 (页面初始化时也会执行一次)
       * 其中page.num:当前页 从1开始, page.size:每页数据条数,默认10
       * @param {Object} page
       */
      upCallback(page) {
        const app = this
        // 设置列表数据
        app.getGiveLogList(page.num)
          .then(list => {
            const curPageLen = list.content.length
            const totalSize = list.totalElements
            app.mescroll.endBySize(curPageLen, totalSize)
          })
          .catch(() => app.mescroll.endErr())
      },

      // 获取转赠列表
      getGiveLogList(pageNo = 1) {
        const app = this
        return new Promise((resolve, reject) => {
          giveApi.giveLog({ type: app.getTabValue(), page: pageNo }, { load: false })
            .then(result => {
              // 合并新数据
              const newList = result.data
              app.list.content = getMoreListData(newList, app.list, pageNo)
              resolve(newList)
            })
        })
      },

      // 切换标签项
      onChangeTab(index) {
        const app = this
        // 设置当前选中的标签
        app.curTab = index
        // 刷新列表
        app.onRefreshList()
      },

      // 刷新订单列表
      onRefreshList() {
        this.list = getEmptyPaginateObj()
        setTimeout(() => {
          this.mescroll.resetUpScroll()
        }, 120)
      },

      // 获取当前标签项的值
      getTabValue() {
        return this.tabs[this.curTab].value
      }
    }
  }
</script>

<style lang="scss" scoped>
  .widget-detail {
    box-sizing: border-box;
    background: #fff;
    margin-bottom: 10rpx;
    border: solid 2px #f5f5f5;
    padding: 30rpx;

    .row-block {
      padding: 0 20rpx;
      min-height: 50rpx;
    }

    .detail-goods {
      padding: 20rpx;

      .goods-right {
        padding: 15rpx 0;
      }
      .mobile {
        color: #888888;  
      }

      .goods-name {
        margin-bottom: 10rpx;
        font-size: 34rpx;
      }

      .goods-props {
        margin-top: 14rpx;
        height: 40rpx;
        color: #ababab;
        font-size: 24rpx;
        overflow: hidden;

        .goods-props-item {
          display: inline-block;
          margin-right: 14rpx;
          padding: 4rpx 16rpx;
          border-radius: 12rpx;
          background-color: #F5F5F5;
          width: auto;
        }
      }

    }

    .detail-operate {
      padding-bottom: 20rpx;

      .detail-btn {
        border-radius: 4px;
        border: 1rpx solid #ccc;
        padding: 8rpx 20rpx;
        font-size: 28rpx;
        color: #555;
        margin-left: 10rpx;
      }
    }

    .detail-order {
      padding: 10rpx 20rpx;
      font-size: 26rpx;
      line-height: 50rpx;
      height: 50rpx;

      .item {
        margin-bottom: 10rpx;

        &:last-child {
          margin-bottom: 0;
        }
      }
    }
  }
  // 空数据按钮
  .empty-ipt {
    width: 220rpx;
    margin: 10px auto;
    font-size: 28rpx;
    height: 64rpx;
    line-height: 64rpx;
    text-align: center;
    color: #fff;
    border-radius: 5rpx;
    background: linear-gradient(to right, $fuint-theme, $fuint-theme);
  }
</style>
