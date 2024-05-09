<template>
  <view class="container">
    <mescroll-body ref="mescrollRef" :sticky="true" @init="mescrollInit" :down="{ native: true }" @down="downCallback"
      :up="upOption" @up="upCallback">

      <!-- tab栏 -->
      <u-tabs :list="tabs" :is-scroll="false" :current="curTab" active-color="#FA2209" :duration="0.2"
        @change="onChangeTab" />

      <!-- 退款/售后单 -->
      <view class="widget-list">
        <view class="widget-detail" v-for="(item, index) in list.content" :key="index">
          <view class="row-block dis-flex flex-y-center">
            <view class="flex-box">{{ item.createTime }}</view>
            <view class="flex-box t-r">
              <text class="col-m">{{ item.statusText }}</text>
            </view>
          </view>
          <view class="detail-goods row-block dis-flex" v-for="(goodsInfo, key) in item.orderInfo.goods" :key="key" @click.stop="handleTargetDetail(item.id)">
            <view class="goods-image">
              <image class="image" :src="goodsInfo.image" mode="aspectFit"></image>
            </view>
            <view class="goods-right flex-box">
              <view class="goods-name">
                <text class="twolist-hidden">{{ goodsInfo.name }}</text>
              </view>
              <view class="goods-props clearfix">
                <view class="goods-props-item" v-for="(props, idx) in goodsInfo.specList" :key="idx">
                  <text>{{ props.specName }}</text>
                </view>
              </view>
              <view class="goods-num t-r">
                <text class="f-26 col-8">×{{ goodsInfo.num }}</text>
              </view>
            </view>
          </view>
          <view class="detail-order row-block">
            <view class="item dis-flex flex-x-end flex-y-center">
              <text class="">售后金额：</text>
              <text class="col-m">￥{{ item.amount }}</text>
            </view>
          </view>
          <view class="detail-operate row-block dis-flex flex-x-end flex-y-center">
            <view class="detail-btn btn-detail" @click.stop="handleTargetDetail(item.id)">查看详情</view>
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
  import * as RefundApi from '@/api/refund'

  // 每页记录数量
  const pageSize = 15

  // tab栏数据
  const tabs = [{
    name: '全部',
    value: 0
  }, {
    name: '处理中',
    value: 1
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
            tip: '亲，暂无售后记录'
          }
        }
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
      this.onRefreshList();
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
        app.getRefundList(page.num)
          .then(list => {
            const curPageLen = list.content.length;
            const totalSize = list.totalElements;
            app.mescroll.endBySize(curPageLen, totalSize);
          })
          .catch(() => app.mescroll.endErr())
      },

      // 获取退款/售后单列表
      getRefundList(pageNo = 1) {
        const app = this
        return new Promise((resolve, reject) => {
          RefundApi.list({ status: app.getTabValue(), page: pageNo }, { load: false })
            .then(result => {
              // 合并新数据
              const newList = result.data;
              app.list.content = getMoreListData(newList, app.list, pageNo);
              resolve(newList)
            })
        })
      },

      // 切换标签项
      onChangeTab(index) {
        const app = this;
        // 设置当前选中的标签
        app.curTab = index;
        // 刷新售后单列表
        app.onRefreshList();
      },

      // 刷新订单列表
      onRefreshList() {
        this.list = getEmptyPaginateObj();
        setTimeout(() => {
          this.mescroll.resetUpScroll();
        }, 120)
      },

      // 获取当前标签项的值
      getTabValue() {
        return this.tabs[this.curTab].value;
      },

      // 跳转到售后单详情页
      handleTargetDetail(refundId) {
        this.$navTo('pages/refund/detail', { refundId })
      },
      
      // 点击跳转到首页
      onTargetIndex() {
        this.$navTo('pages/user/index');
      }

    }
  }
</script>

<style lang="scss" scoped>
  .widget-detail {
    box-sizing: border-box;
    background: #fff;
    margin-bottom: 20rpx;

    .row-block {
      padding: 0 20rpx;
      min-height: 70rpx;
    }

    .detail-goods {
      padding: 20rpx;
      background: #f9f9f9;

      .goods-image {
        margin-right: 20rpx;

        .image {
          display: block;
          width: 150rpx;
          height: 150rpx;
          border-radius: 6rpx;
        }
      }

      .goods-right {
        padding: 15rpx 0;
      }

      .goods-name {
        margin-bottom: 10rpx;
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
