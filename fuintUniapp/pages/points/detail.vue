<template>
  <view class="container">
    <view class="my-point">
        <view class="my-tip"><text class="iconfont icon-jifen"></text>我的积分余额</view>
        <view class="my-account">{{ userInfo.point ? userInfo.point : 0 }}</view>
        <view class="my-gift">
            <view class="gift" @click="toGive()">转赠好友</view>
            <view class="gift" @click="toUsePoint()">兑换积分</view>
        </view>
    </view>
    <mescroll-body ref="mescrollRef" :sticky="true" @init="mescrollInit" :down="{ use: false }" :up="upOption"
      @up="upCallback">
      <view class="log-list">
        <view v-for="(item, index) in list.content" :key="index" class="log-item">
          <view class="item-left flex-box">
            <view class="rec-status">
              <text>{{ item.description }}</text>
            </view>
            <view class="rec-time">
              <text>{{ timeStamp(item.createTime) }}</text>
            </view>
          </view>
          <view class="item-right" :class="[item.amount > 0 ? 'col-green' : 'col-6']">
            <text>{{ item.amount > 0 ? '+' : '' }}{{ item.amount }}</text>
          </view>
        </view>
      </view>
    </mescroll-body>
  </view>
</template>

<script>
  import MescrollBody from '@/components/mescroll-uni/mescroll-body.vue'
  import MescrollMixin from '@/components/mescroll-uni/mescroll-mixins'
  import * as LogApi from '@/api/points/log'
  import * as UserApi from '@/api/user'
  import { getEmptyPaginateObj, getMoreListData } from '@/utils/app'
  import Empty from '@/components/empty'

  const pageSize = 10

  export default {
    components: {
      MescrollBody,
      Empty
    },
    mixins: [MescrollMixin],
    data() {
      return {
        userInfo: {},
        // 数据记录
        list: getEmptyPaginateObj(),
        // 正在加载
        isLoading: false,
        // 上拉加载配置
        upOption: {
          // 首次自动执行
          auto: true,
          // 每页数据的数量; 默认10
          page: { size: pageSize },
          // 数量要大于12条才显示无更多数据
          noMoreSize: 12,
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
    onShow(options) {
        this.getUserInfo()
        this.getLogList(1)
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
        app.getLogList(page.num)
          .then(list => {
            const curPageLen = list.content.length
            const totalSize = list.totalElements
            app.mescroll.endBySize(curPageLen, totalSize)
          })
          .catch(() => app.mescroll.endErr())
      },
      toUsePoint() {
         this.$navTo('subPages/coupon/list?type=C&needPoint=1')
      },
      toGive() {
          this.$navTo('pages/points/gift')
      },
      // 获取当前用户信息
      getUserInfo() {
        const app = this
        return new Promise((resolve, reject) => {
            UserApi.info()
            .then(result => {
                  app.userInfo = result.data.userInfo
                  resolve(app.userInfo)
            })
            .catch(err => {
              if (err.result && err.result.status == 1001) {
                  app.isLogin = false
                  resolve(null)
              } else {
                  reject(err)
              }
            })
        })
      },
      // 获取积分明细列表
      getLogList(pageNo = 1) {
        const app = this
        return new Promise((resolve, reject) => {
          LogApi.list({ page: pageNo })
            .then(result => {
              // 合并新数据
              const newList = result.data
              app.list.content = getMoreListData(newList, app.list, pageNo)
              resolve(newList)
            })
        })
      },
      timeStamp: function(value) {
          var date = new Date(value);
          var year = date.getFullYear();
          var month = ("0" + (date.getMonth() + 1)).slice(-2);
          var sdate = ("0" + date.getDate()).slice(-2);
          var hour = ("0" + date.getHours()).slice(-2);
          var minute = ("0" + date.getMinutes()).slice(-2);
          var second = ("0" + date.getSeconds()).slice(-2);
          // 拼接
          var result = year + "." + month + "." + sdate + " " + hour + ":" + minute //+ ":" + second;
          // 返回
          return result;
      }
    }
  }
</script>

<style lang="scss" scoped>
  .container {
     background: #FFFFFF;
  }
  .my-point {
      height: 320rpx;
      color: #FFFFFF;
      background: $fuint-theme;
      padding-top: 80rpx;
      text-align: center;
      .my-tip {
          text-align: center;
      }
      .my-account {
          text-align: center;
          font-size: 45rpx;
          font-weight: bold;
          margin-top: 14rpx;
      }
      .iconfont {
          height: 30rpx;
          width: 30rpx;
          margin-right: 5rpx;
      }
      .my-gift {
          text-align: center;
          display: inline-block;
          .gift {
            height: 60rpx;
            width: 140rpx;
            margin-top: 20rpx;
            line-height: 60rpx;
            text-align: center;
            align-items: center;
            justify-content: center;
            float: left;
            margin-right: 40rpx;
            margin-left: 40rpx;
            border-radius: 6rpx;
            color: #f86d48;
            background: #f8df98;
            font-size: 22rpx;
          }
      }
  }

  .log-list {
    padding: 0 30rpx;
    background: #FFFFFF;
    border-radius: 20rpx;
  }

  .log-item {
    font-size: 32rpx;
    padding: 20rpx 20rpx;
    line-height: 1.8;
    border-bottom: 1rpx solid rgb(238, 238, 238);
    display: flex;
    justify-content: center;
    align-items: center;
  }

  .rec-time {
    color: #888888;
    font-size: 24rpx;
  }
  // 空数据按钮
  .empty-ipt {
    width: 220rpx;
    margin: 10rpx auto;
    font-size: 28rpx;
    height: 64rpx;
    line-height: 64rpx;
    text-align: center;
    color: #fff;
    border-radius: 5rpx;
    background: linear-gradient(to right, $fuint-theme, $fuint-theme);
  }
</style>
