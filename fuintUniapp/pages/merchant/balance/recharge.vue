<template>
  <view class="container" v-if="userInfo.id">
    <view class="account-panel dis-flex flex-y-center">
      <view class="panel-lable">
        <text>账户余额</text>
      </view>
      <view class="panel-balance flex-box">
        <text>￥{{ userInfo.balance }}</text>
      </view>
    </view>
    <view class="recharge-panel">
      <view class="recharge-label">
        <text>充值金额</text>
      </view>
      <view class="recharge-plan clearfix" v-if="setting.planList.length > 0">
        <block v-for="(item, index) in setting.planList" :key="index">
          <view class="recharge-plan_item" :class="{ active: rechargeAmount == item.rechargeAmount }" @click="onSelectPlan(item.rechargeAmount)">
            <view class="plan_money">
              <text>{{ item.rechargeAmount }}</text>
            </view>
            <view class="plan_gift" v-if="item.giveAmount > 0">
              <text>送{{ item.giveAmount }}</text>
            </view>
          </view>
        </block>
      </view>
      <!-- 手动充值输入框 -->
      <view class="recharge-input" v-if="!setting.planList || setting.planList.length < 1">
        <input type="digit" placeholder="请输入充值金额" v-model="inputValue" @input="onChangeMoney" />
      </view>
      <!-- 确认按钮 -->
      <view class="recharge-submit btn-submit">
        <form @submit="onSubmit">
          <button class="button" formType="submit" :disabled="disabled">立即充值</button>
        </form>
      </view>
    </view>
    <!-- 充值描述 -->
    <view class="recharge-describe" v-if="setting.remark.length > 0">
      <view class="recharge-label">
        <text>充值说明</text>
      </view>
      <view class="content">
        <text space="ensp">{{setting.remark}}</text>
      </view>
    </view>
  </view>
</template>

<script>
  import * as MemberApi from '@/api/merchant/member'
  import * as UserApi from '@/api/user'
  import * as BalanceApi from '@/api/balance'
  import * as RechargeApi from '@/api/merchant/recharge'

  export default {
    data() {
      return {
        // 正在加载
        isLoading: true,
        // 会员信息
        userInfo: {},
        // 充值设置
        setting: { isOpen: false, planList: [], remark: '' },
        // 按钮禁用
        disabled: false,
        // 当前选中的套餐id
        rechargeAmount: 0,
        // 自定义金额
        inputValue: '',
        // 会员ID
        memberId: 0
      }
    },

    /**
     * 生命周期函数--监听页面加载
     */
    onLoad(options) {
      // 会员ID
      this.memberId = options.memberId ? options.memberId : 0;
      // 获取页面数据
      this.getPageData()
    },

    methods: {

      /**
       * 选择充值套餐
       */
      onSelectPlan(rechargeAmount) {
        this.rechargeAmount = rechargeAmount
        this.inputValue = ''
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
          .then(() => app.isLoading = false)
      },

      // 获取会员信息
      getUserInfo() {
        const app = this
        return new Promise((resolve, reject) => {
          MemberApi.detail(app.memberId)
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
              app.setting = result.data
              console.log("app.setting = ", app.setting)
              resolve(app.setting)
            })
        })
      },

      // 立即充值
      onSubmit(e) {
        const app = this
        if (!app.setting.isOpen) {
            app.$error('当前未开启充值！')
            return false
        }
        
        if (parseFloat(app.rechargeAmount) <= 0 && app.inputValue.length < 1) {
            app.$error('请确认充值金额！')
            return false
        }
        
        uni.showModal({
          title: "提示",
          content: "您确定要充值金额"+ (app.rechargeAmount + app.inputValue) +"吗?",
          success({ confirm }) {
             if (!confirm) {
                 return false;
             }  
             app.disabled = true
             RechargeApi.doRecharge(app.rechargeAmount, app.inputValue, app.memberId)
               .then(result => {
                   if (result.data) {
                       app.$success('充值成功');
                       setTimeout(() => {
                           app.getPageData();
                       }, 2000)
                   }
               })
               .catch(err => app.$error('充值失败'))
               .finally(() => app.disabled = false)
          }
        });
      }
    }
  }
</script>

<style lang="scss" scoped>
  page,
  .container {
    background: #fff;
  }

  .container {
    padding-bottom: 70rpx;
  }

  /* 账户面板 */
  .account-panel {
    width: 650rpx;
    height: 180rpx;
    margin: 50rpx auto;
    padding: 0 60rpx;
    box-sizing: border-box;
    border-radius: 8rpx;
    color: #fff;
    background: $fuint-theme;
    box-shadow: 0 5px 22px 0 rgba(0, 0, 0, 0.26);
  }

  .panel-lable {
    font-size: 32rpx;
  }

  .recharge-label {
    color: rgb(51, 51, 51);
    font-size: 32rpx;
    margin-bottom: 25rpx;
  }

  .panel-balance {
    text-align: right;
    font-size: 46rpx;
  }

  .recharge-panel {
    margin-top: 60rpx;
    padding: 0 60rpx;
  }

  /* 充值套餐 */
  .recharge-plan {
    .recharge-plan_item {
      width: 192rpx;
      padding: 15rpx 0;
      float: left;
      text-align: center;
      color: #888;
      border: 1rpx solid rgb(228, 228, 228);
      border-radius: 5rpx;
      margin: 0 20rpx 20rpx 0;

      &:nth-child(3n + 0) {
        margin-right: 0;
      }

      &.active {
        background: #E3BE83;
        color: #FFFFFF;
        border: 1rpx solid #EDD2A9;
        .plan_money {
          color: #FFFFFF;
          font-weight: bold;
        }
      }
    }

  }

  .plan_money {
    font-size: 32rpx;
    color: rgb(82, 82, 82);
  }

  .plan_gift {
    font-size: 25rpx;
  }

  .recharge-input {
    margin-top: 25rpx;

    input {
      border: 1rpx solid rgb(228, 228, 228);
      border-radius: 10rpx;
      padding: 15rpx 16rpx;
      font-size: 26rpx;
    }
  }

  /* 立即充值 */
  .recharge-submit {
    margin-top: 70rpx;
  }

  .btn-submit {
    .button {
      font-size: 30rpx;
      background: linear-gradient(to right, #f9211c, #ff6335);
      border: none;
      color: white;
      border-radius: 40rpx;
      padding: 0 120rpx;
      line-height: 3;
    }

    .button[disabled] {
      background: #ff6335;
      border-color: #ff6335;
      color: white;
    }
  }

  /* 充值说明 */
  .recharge-describe {
    margin-top: 50rpx;
    padding: 0 60rpx;
    .content {
      font-size: 26rpx;
      line-height: 1.6;
      color: #888;
    }
  }
</style>
