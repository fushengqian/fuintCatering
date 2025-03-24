<template>
  <view class="container" v-if="!isLoading">
    <view class="space-upper">
      <view class="wallet-account">
        <view class="wallet-account_balance">
          <text>￥{{ userInfo.balance }}</text>
        </view>
        <view class="wallet-account_lable">
          <text>余额（元）</text>
        </view>
      </view>
    </view>
    <view class="space-lower">
      <view class="space-lower_item btn-recharge">
        <view class="btn-submit" @click="onTargetRecharge()">充值</view>
      </view>
      <view class="space-lower_item item-lable dis-flex flex-x-around">
        <view class="lable-text" @click="onTargetRechargeOrder()">
          <text>充值订单</text>
        </view>
        <view class="lable-text" @click="onTargetBalanceLog()">
          <text>余额明细</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
  import * as UserApi from '@/api/user'
  import * as BalanceApi from '@/api/balance'

  export default {
    data() {
      return {
        // 正在加载
        isLoading: true,
        // 会员信息
        userInfo: {},
        // 充值设置
        setting: {},
      }
    },

    /**
     * 生命周期函数--监听页面加载
     */
    onLoad(options) {
      // 获取页面数据
      this.getPageData()
    },

    methods: {

      // 获取页面数据
      getPageData() {
        const app = this
        app.isLoading = true
        Promise.all([app.getUserInfo(), app.getSetting()])
          .then(() => app.isLoading = false)
      },

      // 获取会员信息
      getUserInfo() {
        const app = this
        return new Promise((resolve, reject) => {
          UserApi.info()
            .then(result => {
              app.userInfo = result.data.userInfo
              resolve(app.userInfo)
            })
        })
      },

      // 获取充值设置
      getSetting() {
        const app = this
        return new Promise((resolve, reject) => {
          BalanceApi.setting()
            .then(result => {
              app.setting = result.data.data
              resolve(app.setting)
            })
        })
      },

      // 跳转充值页面
      onTargetRecharge() {
        this.$navTo('pages/wallet/recharge/index')
      },

      // 跳转充值记录页面
      onTargetRechargeOrder() {
        this.$navTo('pages/wallet/recharge/order')
      },

      // 跳转账单详情页面
      onTargetBalanceLog() {
        this.$navTo('pages/wallet/balance/log')
      }
    }
  }
</script>
<style>
  page {
    background: #fff;
  }
</style>
<style lang="scss" scoped>
  .container {
    background: #fff;
  }

  .space-upper {
    padding: 100rpx 0;
    text-align: center;
    background: $fuint-theme;
    margin: 50rpx 30rpx 10rpx 30rpx;
    border-radius: 8rpx;
  }

  .wallet-account {
    margin-top: 20rpx;
  }

  .wallet-account_balance {
    font-size: 52rpx;
    color: #FFFFFF;
  }

  .wallet-account_lable {
    margin-top: 10rpx;
    color: #FFFFFF;
    font-size: 24rpx;
  }

  .space-lower {
    margin-top: 30rpx;
    padding: 0rpx 30rpx 0rpx 30rpx;
  }

  .btn-recharge .btn-submit {
    width: 100%;
    height: 84rpx;
    line-height: 84rpx;
    margin: 100rpx auto;
    text-align: center;
    border-radius: 40rpx;
    background: linear-gradient(to right, #f9211c, #ff6335);
    color: white;
    font-size: 30rpx;
  }

  .item-lable {
    margin-top: 80rpx;
    font-size: 26rpx;
    color: rgb(94, 94, 94);
    padding: 0 100rpx;
  }
</style>
