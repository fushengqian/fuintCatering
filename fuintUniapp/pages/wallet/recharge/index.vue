<template>
  <view class="container" v-if="userInfo.id" :style="themeVars">
    <!-- 顶部氛围区 -->
    <view class="hero">
      <view class="hero-bubble bubble-1"></view>
      <view class="hero-bubble bubble-2"></view>
      <view class="hero-bubble bubble-3"></view>
      <view class="hero-content">
        <view class="hero-title">储值享好礼</view>
        <view class="hero-desc">充值即赠，余额消费更划算</view>
      </view>
    </view>

    <!-- 余额卡片 -->
    <view class="balance-card">
      <view class="card-head">
        <view class="member-chip">
          <text class="chip-icon iconfont icon-qianbao"></text>
          <text class="chip-name">{{ memberName }}</text>
        </view>
        <view class="record-link" @click="onTargetRechargeOrder">
          <text class="record-text">充值记录</text>
          <text class="record-arrow iconfont icon-xiangyoujiantou"></text>
        </view>
      </view>

      <view class="balance-label">
        <text>总余额（元）</text>
      </view>
      <view class="balance-amount">
        <text class="amount-unit">¥</text>
        <text class="amount-num">{{ balanceText }}</text>
      </view>

      <view class="balance-stats">
        <view class="stat-item">
          <text class="stat-value">{{ pointText }}</text>
          <text class="stat-label">可用积分</text>
        </view>
        <view class="stat-split"></view>
        <view class="stat-item">
          <text class="stat-value">{{ gradeText }}</text>
          <text class="stat-label">会员等级</text>
        </view>
      </view>
    </view>

    <!-- 充值套餐 -->
    <view class="section">
      <view class="section-head">
        <text class="section-title">选择充值金额</text>
        <text class="section-tip" v-if="hasPlan">充值越多，赠送越多</text>
      </view>

      <view class="plan-list" v-if="hasPlan">
        <view class="plan-item"
          v-for="(item, index) in setting.planList"
          :key="index"
          :class="{ 'plan-item--active': isSelected(item), 'plan-item--recommend': isRecommend(item) }"
          @click="onSelectPlan(item)">
          <view class="plan-tag" v-if="isRecommend(item)">超值推荐</view>
          <view class="plan-body">
            <view class="plan-price">
              <text class="plan-cny">¥</text>
              <text class="plan-number">{{ moneyText(item.rechargeAmount) }}</text>
            </view>
            <view class="plan-gift" v-if="giftOf(item) > 0">
              <text class="gift-icon iconfont icon-gift"></text>
              <text>赠 ¥{{ moneyText(item.giveAmount) }}</text>
            </view>
            <view class="plan-check" :class="{ 'plan-check--on': isSelected(item) }">
              <text class="check-icon iconfont icon-duihao"></text>
            </view>
          </view>
          <view class="plan-tip">
            <text>{{ planTip(item) }}</text>
          </view>
        </view>
      </view>

      <!-- 未配置套餐时支持自定义金额 -->
      <view class="custom-input" v-else>
        <text class="custom-unit">¥</text>
        <input class="custom-field" type="digit" placeholder="请输入充值金额" placeholder-class="custom-placeholder"
          v-model="inputValue" @input="onChangeMoney" />
      </view>
    </view>

    <!-- 充值说明 -->
    <view class="notice" v-if="setting.remark && setting.remark.length > 0">
      <text class="notice-icon iconfont icon-bangzhu"></text>
      <view class="notice-body">
        <text space="ensp">{{ setting.remark }}</text>
      </view>
    </view>

    <!-- 底部提交 -->
    <view class="submit-bar">
      <button class="submit-btn" :class="{ 'submit-btn--disabled': !canSubmit }" :disabled="!canSubmit" @click="onSubmit">
        {{ submitText }}
      </button>
    </view>
  </view>
</template>

<script>
  import * as UserApi from '@/api/user'
  import * as BalanceApi from '@/api/balance'
  import { wxPayment } from '@/utils/app'

  export default {
    data() {
      return {
        // 正在加载
        isLoading: true,
        // 会员信息
        userInfo: {},
        // 会员等级信息
        gradeInfo: {},
        // 充值设置
        setting: { isOpen: false, planList: [], remark: '' },
        // 按钮禁用
        disabled: false,
        // 当前选中的套餐金额
        rechargeAmount: 0,
        // 自定义金额
        inputValue: ''
      }
    },

    computed: {

      // 是否有充值套餐
      hasPlan() {
        const list = this.setting.planList
        return !!(list && list.length > 0)
      },

      // 会员称呼
      memberName() {
        return this.userInfo.name ? this.userInfo.name : '尊敬的会员'
      },

      // 余额
      balanceText() {
        return Number(this.userInfo.balance || 0).toFixed(2)
      },

      // 积分
      pointText() {
        return this.userInfo.point ? this.userInfo.point : 0
      },

      // 会员等级
      gradeText() {
        return this.gradeInfo.name ? this.gradeInfo.name : '普通会员'
      },

      // 推荐套餐：赠送比例最高的一个
      recommendAmount() {
        const list = this.setting.planList || []
        let best = ''
        let rate = 0
        for (let i = 0; i < list.length; i++) {
          const money = this.moneyOf(list[i].rechargeAmount)
          const gift = this.giftOf(list[i])
          if (money <= 0 || gift <= 0) {
            continue
          }
          const cur = gift / money
          if (cur > rate) {
            rate = cur
            best = String(list[i].rechargeAmount)
          }
        }
        return best
      },

      // 本次应付金额
      payAmount() {
        if (parseFloat(this.rechargeAmount) > 0) {
          return parseFloat(this.rechargeAmount)
        }
        return parseFloat(this.inputValue) > 0 ? parseFloat(this.inputValue) : 0
      },

      // 是否可提交
      canSubmit() {
        return !!this.setting.isOpen && !this.disabled
      },

      // 提交按钮文案
      submitText() {
        if (!this.setting.isOpen) {
          return '当前未开启充值'
        }
        if (this.payAmount > 0) {
          return '立即充值 ¥' + this.payAmount.toFixed(2)
        }
        return '立即充值'
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

      /**
       * 选择充值套餐
       */
      onSelectPlan(item) {
        const app = this
        app.rechargeAmount = item.rechargeAmount
        app.inputValue = ''
      },

      // 金额输入框
      onChangeMoney(e) {
        this.inputValue = e.target.value
        this.rechargeAmount = 0
      },

      // 获取页面数据
      getPageData() {
        const app = this
        app.isLoading = true
        Promise.all([app.getUserInfo(), app.getSetting()])
          .then(() => {
            app.isLoading = false
          })
      },

      // 获取会员信息
      getUserInfo() {
        const app = this
        return new Promise((resolve, reject) => {
          UserApi.info()
            .then(result => {
              const data = result.data || {}
              app.userInfo = data.userInfo || {}
              app.gradeInfo = data.gradeInfo || {}
              resolve(app.userInfo)
            })
            .catch(() => resolve(app.userInfo))
        })
      },

      // 获取充值设置
      getSetting() {
        const app = this
        return new Promise((resolve, reject) => {
          BalanceApi.setting()
            .then(result => {
              app.setting = result.data || { isOpen: false, planList: [], remark: '' }
              if (!app.setting.isOpen) {
                app.disabled = true
              }
              // 默认选中赠送力度最大的套餐，方便会员一键充值
              if (app.rechargeAmount <= 0 && !app.inputValue && app.hasPlan) {
                app.rechargeAmount = app.recommendAmount ? app.recommendAmount : app.setting.planList[0].rechargeAmount
              }
              resolve(app.setting)
            })
            .catch(err => resolve(app.setting))
        })
      },

      // 套餐是否选中
      isSelected(item) {
        return parseFloat(this.rechargeAmount) > 0 && String(item.rechargeAmount) === String(this.rechargeAmount)
      },

      // 套餐是否推荐
      isRecommend(item) {
        return !!this.recommendAmount && String(item.rechargeAmount) === this.recommendAmount
      },

      // 套餐下面的小字提示
      planTip(item) {
        const money = this.moneyOf(item.rechargeAmount)
        const gift = this.giftOf(item)
        if (gift > 0) {
          return '充值后到账 ¥' + this.moneyText(money + gift)
        }
        return '充值后余额到账 ¥' + this.moneyText(money)
      },

      // 赠送金额
      giftOf(item) {
        return this.moneyOf(item && item.giveAmount)
      },

      // 金额转数字
      moneyOf(value) {
        const num = parseFloat(value)
        return isNaN(num) ? 0 : num
      },

      // 金额展示(整数不带小数)
      moneyText(value) {
        const num = this.moneyOf(value)
        return num % 1 === 0 ? String(num) : num.toFixed(2)
      },

      // 跳转充值记录
      onTargetRechargeOrder() {
        this.$navTo('pages/wallet/recharge/order')
      },

      // 立即充值
      onSubmit(e) {
        const app = this
        if (!app.setting.isOpen) {
          app.$error('当前未开启充值！')
          return false
        }
        if (app.disabled) {
          return false
        }
        if (app.payAmount <= 0) {
          app.$error('请选择或输入充值金额！')
          return false
        }

        // 按钮禁用
        app.disabled = true
        // 提交到后端
        BalanceApi.doRecharge(app.rechargeAmount, app.inputValue)
          .then(result => app.wxPayment(result.data.payment))
          .catch(err => app.$error('提交支付失败'))
          .finally(() => app.disabled = false)
      },

      // 发起微信支付
      wxPayment(option) {
        const app = this
        wxPayment(option)
          .then(() => {
            app.$success('支付成功')
            setTimeout(() => {
              // 获取页面数据
              app.getPageData()
            }, 1500)
          })
          .catch(err => app.$error('订单未支付'))
      }
    }
  }
</script>

<style>
  page {
    background: #f6f7f9;
  }
</style>

<style lang="scss" scoped>
  .container {
    min-height: 100vh;
    /* 预留底部固定按钮的位置 */
    padding-bottom: 220rpx;
    background: #f6f7f9;
  }

  /* 顶部氛围区 */
  .hero {
    position: relative;
    height: 360rpx;
    overflow: hidden;
    background-color: var(--theme-primary);
    background-image: linear-gradient(180deg, rgba(255, 255, 255, 0.16) 0%, rgba(0, 0, 0, 0.08) 100%);
    border-radius: 0 0 40rpx 40rpx;
  }

  .hero-bubble {
    position: absolute;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.14);
  }

  .bubble-1 {
    width: 280rpx;
    height: 280rpx;
    top: -110rpx;
    right: -90rpx;
  }

  .bubble-2 {
    width: 160rpx;
    height: 160rpx;
    top: 100rpx;
    right: 80rpx;
    background: rgba(255, 255, 255, 0.10);
  }

  .bubble-3 {
    width: 90rpx;
    height: 90rpx;
    left: 60rpx;
    bottom: 30rpx;
    background: rgba(255, 255, 255, 0.08);
  }

  .hero-content {
    position: relative;
    z-index: 2;
    padding: 48rpx 44rpx 0;
  }

  .hero-title {
    font-size: 42rpx;
    font-weight: 700;
    color: #fff;
    letter-spacing: 2rpx;
    text-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.12);
  }

  .hero-desc {
    margin-top: 14rpx;
    font-size: 24rpx;
    color: rgba(255, 255, 255, 0.86);
  }

  /* 余额卡片 */
  .balance-card {
    position: relative;
    z-index: 3;
    margin: -190rpx 24rpx 0;
    padding: 32rpx 32rpx 30rpx;
    background: #fff;
    border-radius: 24rpx;
    box-shadow: 0 16rpx 40rpx rgba(0, 0, 0, 0.08);
  }

  .card-head {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  .member-chip {
    display: flex;
    align-items: center;
    height: 48rpx;
    padding: 0 20rpx;
    border-radius: 24rpx;
    background-color: var(--theme-primary);
  }

  .chip-icon {
    margin-right: 8rpx;
    font-size: 22rpx;
    color: #fff;
  }

  .chip-name {
    max-width: 300rpx;
    overflow: hidden;
    font-size: 24rpx;
    color: #fff;
    white-space: nowrap;
    text-overflow: ellipsis;
  }

  .record-link {
    display: flex;
    align-items: center;
    font-size: 24rpx;
    color: #8c8c8c;
  }

  .record-arrow {
    margin-left: 6rpx;
    font-size: 22rpx;
    color: #c0c0c0;
  }

  .balance-label {
    margin-top: 28rpx;
    font-size: 24rpx;
    color: #9b9b9b;
  }

  .balance-amount {
    display: flex;
    align-items: flex-end;
    margin-top: 10rpx;
  }

  .amount-unit {
    padding-bottom: 8rpx;
    margin-right: 6rpx;
    font-size: 30rpx;
    font-weight: 600;
    color: #333;
    line-height: 1;
  }

  .amount-num {
    font-size: 64rpx;
    font-weight: 700;
    color: #222;
    line-height: 1.1;
    letter-spacing: 1rpx;
  }

  .balance-stats {
    display: flex;
    align-items: center;
    margin-top: 28rpx;
    padding-top: 26rpx;
    border-top: 1rpx solid #f4f4f4;
  }

  .stat-item {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
  }

  .stat-value {
    font-size: 30rpx;
    font-weight: 600;
    color: #333;
  }

  .stat-label {
    margin-top: 8rpx;
    font-size: 22rpx;
    color: #9b9b9b;
  }

  .stat-split {
    width: 1rpx;
    height: 52rpx;
    background: #f0f0f0;
  }

  /* 充值套餐 */
  .section {
    margin-top: 36rpx;
    padding: 0 24rpx;
  }

  .section-head {
    display: flex;
    align-items: flex-end;
    justify-content: space-between;
    padding: 0 8rpx 20rpx;
  }

  .section-title {
    font-size: 30rpx;
    font-weight: 700;
    color: #333;
  }

  .section-tip {
    font-size: 22rpx;
    color: #b5b5b5;
  }

  .plan-item {
    position: relative;
    padding: 30rpx 28rpx;
    margin-bottom: 20rpx;
    background: #fff;
    border: 2rpx solid #f0f0f0;
    border-radius: 20rpx;
  }

  .plan-item--recommend {
    background: #fffbf2;
    border-color: #f7d9a6;
  }

  .plan-item--active {
    border-color: var(--theme-primary);
    box-shadow: 0 10rpx 26rpx rgba(0, 0, 0, 0.07);
  }

  .plan-tag {
    position: absolute;
    top: -2rpx;
    left: -2rpx;
    padding: 6rpx 18rpx;
    font-size: 20rpx;
    color: #fff;
    border-radius: 18rpx 0 18rpx 0;
    background: linear-gradient(135deg, #ff8a3d, #ff5b21);
  }

  .plan-body {
    display: flex;
    align-items: center;
  }

  .plan-price {
    display: flex;
    align-items: baseline;
  }

  .plan-cny {
    margin-right: 4rpx;
    font-size: 28rpx;
    font-weight: 600;
    color: #333;
  }

  .plan-number {
    font-size: 44rpx;
    font-weight: 700;
    color: #222;
  }

  .plan-item--active .plan-number {
    color: var(--theme-primary);
  }

  .plan-gift {
    display: flex;
    align-items: center;
    height: 40rpx;
    padding: 0 16rpx;
    margin-left: 20rpx;
    font-size: 22rpx;
    font-weight: 600;
    color: #fff;
    border-radius: 20rpx;
    background: linear-gradient(135deg, #ff8a3d, #ff5b21);
  }

  .gift-icon {
    margin-right: 4rpx;
    font-size: 22rpx;
  }

  .plan-check {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 40rpx;
    height: 40rpx;
    margin-left: auto;
    border: 2rpx solid #dcdcdc;
    border-radius: 50%;
  }

  .plan-check--on {
    border-color: var(--theme-primary);
    background-color: var(--theme-primary);
  }

  .check-icon {
    font-size: 20rpx;
    color: transparent;
  }

  .plan-check--on .check-icon {
    color: #fff;
  }

  .plan-tip {
    margin-top: 16rpx;
    font-size: 22rpx;
    color: #999;
  }

  /* 自定义金额 */
  .custom-input {
    display: flex;
    align-items: center;
    padding: 30rpx 28rpx;
    background: #fff;
    border: 2rpx solid #f0f0f0;
    border-radius: 20rpx;
  }

  .custom-unit {
    margin-right: 12rpx;
    font-size: 36rpx;
    font-weight: 700;
    color: #333;
  }

  .custom-field {
    flex: 1;
    height: 48rpx;
    font-size: 32rpx;
    color: #333;
  }

  .custom-placeholder {
    font-size: 28rpx;
    color: #c8c8c8;
  }

  /* 充值说明 */
  .notice {
    display: flex;
    margin: 28rpx 24rpx 0;
    padding: 24rpx 28rpx;
    background: #fff;
    border-radius: 20rpx;
  }

  .notice-icon {
    flex-shrink: 0;
    margin: 4rpx 12rpx 0 0;
    font-size: 26rpx;
    color: var(--theme-primary);
  }

  .notice-body {
    flex: 1;
    font-size: 24rpx;
    color: #8c8c8c;
    line-height: 1.7;
    word-break: break-all;
  }

  /* 底部提交 */
  .submit-bar {
    position: fixed;
    left: 0;
    right: 0;
    bottom: 0;
    z-index: 20;
    padding: 20rpx 32rpx;
    padding-bottom: calc(20rpx + constant(safe-area-inset-bottom));
    padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
    background: #fff;
    box-shadow: 0 -6rpx 24rpx rgba(0, 0, 0, 0.05);
  }

  .submit-btn {
    width: 100%;
    height: 92rpx;
    padding: 0;
    margin: 0;
    font-size: 32rpx;
    font-weight: 600;
    color: #fff;
    line-height: 92rpx;
    border: none;
    border-radius: 46rpx;
    background-color: var(--theme-primary);
    background-image: linear-gradient(135deg, rgba(255, 255, 255, 0.22) 0%, rgba(255, 255, 255, 0) 60%),
      linear-gradient(135deg, rgba(0, 0, 0, 0.08) 0%, rgba(0, 0, 0, 0) 60%);
  }

  .submit-btn--disabled,
  .submit-btn[disabled] {
    color: #fff;
    background-color: #d8d8d8;
    background-image: none;
    opacity: 1;
  }
</style>
