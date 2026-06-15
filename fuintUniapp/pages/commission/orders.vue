<template>
  <view class="container">
    <mescroll-body
      ref="mescrollRef"
      :sticky="true"
      @init="mescrollInit"
      :down="{ use: false }"
      :up="upOption"
      @up="upCallback"
    >
      <!-- 佣金订单列表 -->
      <view class="order-list">
        <view v-for="(item, index) in list.content" :key="index" class="order-item">
          <view class="order-top">
            <view class="order-type">
              <text class="type-tag">{{ item.typeName || '--' }}</text>
              <text class="level-tag" :class="item.level == 1 ? 'level-one' : 'level-two'">
                {{ item.level == 1 ? '一级' : '二级' }}
              </text>
            </view>
            <view class="order-amount">+{{ item.amount || '0.00' }}元</view>
          </view>

          <view class="order-mid">
            <view class="order-info" v-if="item.orderInfo">
              <text class="label">订单号：</text>
              <text class="value">{{ item.orderInfo.orderSn || '--' }}</text>
            </view>
            <view class="order-info" v-if="item.ruleInfo">
              <text class="label">分佣规则：</text>
              <text class="value">{{ item.ruleInfo.name || '--' }}</text>
            </view>
          </view>

          <view class="order-bottom">
            <view class="cash-status">
              <text v-if="item.isCash === 'Y' && item.cashId" class="status-done">已提现</text>
              <text v-else class="status-wait">未提现</text>
            </view>
            <view class="order-time">{{ item.createTime | timeFormat('yyyy-mm-dd hh:MM') }}</view>
          </view>
        </view>
      </view>
    </mescroll-body>
  </view>
</template>

<script>
  import MescrollBody from '@/components/mescroll-uni/mescroll-body.vue'
  import MescrollMixin from '@/components/mescroll-uni/mescroll-mixins'
  import * as CommissionApi from '@/api/commission'
  import { getEmptyPaginateObj, getMoreListData } from '@/utils/app'

  const pageSize = 15

  export default {
    components: {
      MescrollBody
    },
    mixins: [MescrollMixin],
    data() {
      return {
        // 佣金订单列表
        list: getEmptyPaginateObj(),
        // 上拉加载配置
        upOption: {
          auto: true,
          page: { size: pageSize },
          noMoreSize: 3,
          empty: {
            tip: '暂无佣金订单'
          }
        }
      }
    },
    methods: {
      /**
       * 上拉加载回调
       */
      upCallback(page) {
        const app = this
        app.getOrderList(page.num)
          .then(list => {
            const curPageLen = list.content ? list.content.length : 0
            const totalSize = list.totalElements || 0
            app.mescroll.endBySize(curPageLen, totalSize)
          })
          .catch(() => app.mescroll.endErr())
      },

      /**
       * 获取佣金订单列表
       */
      getOrderList(pageNo = 1) {
        const app = this
        return new Promise((resolve, reject) => {
          CommissionApi.orders({ page: pageNo, pageSize: pageSize })
            .then(result => {
              const newList = result.data
              app.list.content = getMoreListData(newList, app.list, pageNo)
              resolve(newList)
            })
            .catch(() => reject())
        })
      }
    }
  }
</script>

<style lang="scss" scoped>
  .container {
    min-height: 100vh;
    background: #f7f7f7;
  }

  .order-list {
    padding-top: 10rpx;
  }

  .order-item {
    margin: 0 20rpx 16rpx;
    background: #fff;
    border-radius: 12rpx;
    padding: 24rpx 28rpx;
  }

  .order-top {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding-bottom: 18rpx;
    border-bottom: 1rpx solid #f5f5f5;
  }

  .order-type {
    display: flex;
    align-items: center;
  }

  .type-tag {
    font-size: 26rpx;
    color: #333;
    font-weight: bold;
    margin-right: 12rpx;
  }

  .level-tag {
    font-size: 20rpx;
    padding: 2rpx 12rpx;
    border-radius: 16rpx;
  }

  .level-one {
    background: #fff3e0;
    color: #ff9800;
  }

  .level-two {
    background: #e3f2fd;
    color: #2196f3;
  }

  .order-amount {
    font-size: 30rpx;
    font-weight: bold;
    color: #ee0a24;
  }

  .order-mid {
    padding: 16rpx 0;
  }

  .order-info {
    display: flex;
    align-items: center;
    margin-bottom: 8rpx;

    &:last-child {
      margin-bottom: 0;
    }
  }

  .label {
    font-size: 24rpx;
    color: #999;
    flex-shrink: 0;
  }

  .value {
    font-size: 24rpx;
    color: #666;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .order-bottom {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding-top: 16rpx;
    border-top: 1rpx solid #f5f5f5;
  }

  .cash-status {
    .status-done {
      font-size: 22rpx;
      color: #4caf50;
      background: #e8f5e9;
      padding: 4rpx 14rpx;
      border-radius: 16rpx;
    }
    .status-wait {
      font-size: 22rpx;
      color: #ff9800;
      background: #fff3e0;
      padding: 4rpx 14rpx;
      border-radius: 16rpx;
    }
  }

  .order-time {
    font-size: 22rpx;
    color: #ccc;
  }
</style>
