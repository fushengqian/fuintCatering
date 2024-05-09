<template>
  <view class="container p-bottom">
    <!-- 清单列表 -->
    <view class="m-top20">
      <view v-for="(item, index) in storeRule" :key="index" class="checkout_list">
        <view class="flow-shopList dis-flex">
          <view class="flow-list-right flex-box">
            <text class="goods-name twolist-hidden">预存￥{{ item.store }} 到账 ￥{{ item.upStore }}</text>
            <view class="flow-list-cont dis-flex flex-x-between flex-y-center">
              <text class="small">X{{ item.num }} </text>
              <text class="flow-cont">￥{{ item.amount.toFixed(2) }}</text>
            </view>
          </view>
        </view>
      </view>
      <view class="flow-num-box b-f">
        <text>共{{ totalNum }}张，合计：</text>
        <text class="flow-money col-m">￥{{ totalAmount.toFixed(2) }}</text>
      </view>
    </view>

    <!-- 支付方式 -->
    <view class="pay-method flow-all-money b-f m-top20">
      <view class="flow-all-list dis-flex">
        <text class="flex-five">支付方式</text>
      </view>
      <!-- 微信支付 -->
      <view class="pay-item dis-flex flex-x-between" @click="handleSelectPayType(PayTypeEnum.WECHAT.value)">
        <view class="item-left dis-flex flex-y-center">
          <view class="item-left_icon wechat">
            <text class="iconfont icon-weixinzhifu"></text>
          </view>
          <view class="item-left_text">
            <text>{{ PayTypeEnum.WECHAT.name }}</text>
          </view>
        </view>
        <view class="item-right col-m" v-if="curPayType == PayTypeEnum.WECHAT.value">
          <text class="iconfont icon-duihao"></text>
        </view>
      </view>
    </view>

    <!-- 买家留言 -->
    <view class="flow-all-money b-f m-top20">
      <view class="ipt-wrapper">
        <textarea v-model="remark" rows="3" maxlength=100 placeholder="买家留言 (选填,100字以内)" type="text"></textarea>
      </view>
    </view>

    <!-- 提交订单 -->
    <view class="flow-fixed-footer b-f m-top10">
      <view class="dis-flex chackout-box">
        <view class="chackout-left pl-12">
          <view class="col-amount-do">应付金额：￥{{ totalAmount.toFixed(2) }}</view>
          <view class="col-amount-view">实得金额：￥{{ getTotalAmount.toFixed(2) }}</view>
        </view>
        <view class="chackout-right" @click="onSubmitOrder()">
          <view class="flow-btn f-32" :class="{ disabled }">提交订单</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
  import * as Verify from '@/utils/verify'
  import * as CouponApi from '@/api/coupon'
  import * as SettlementApi from '@/api/settlement'
  import PayTypeEnum from '@/common/enum/order/PayType'
  import { wxPayment } from '@/utils/app'

  export default {
    data() {
      return {
        // 枚举类
        PayTypeEnum,
        // 当前页面参数
        options: {},
        // 当前选中的支付方式
        curPayType: PayTypeEnum.WECHAT.value,
        // 买家留言
        remark: '',
        // 禁用submit按钮
        disabled: false,
        // 按钮禁用
        disabled: false,
        couponId: 0,
        selectNum: "",
        storeRule: [],
        totalAmount: 0,
        getTotalAmount: 0,
        totalNum: 0
      }
    },

    /**
     * 生命周期函数--监听页面加载
     */
    onLoad(options) {
      this.options = options
      this.couponId = options.couponId
      this.selectNum = options.selectNum
    },

    /**
     * 生命周期函数--监听页面显示
     */
    onShow() {
      // 获取当前订单信息
      this.getCouponDetail()
    },

    methods: {

      // 获取卡券信息
      getCouponDetail() {
        const app = this
        return new Promise((resolve, reject) => {
          CouponApi.detail(app.couponId)
            .then(result => {
              app.initData()
              app.couponInfo = result.data
                let ruleItem = app.couponInfo.inRule.split(",")
              let selected = app.selectNum.split(",")
                ruleItem.forEach(function(item, index) {
                    let rule = item.split("_")
                    let num = selected[index]
                    let amount = parseFloat(rule[0]) * num
                    app.totalAmount += parseFloat(amount)
                    app.getTotalAmount += parseFloat(rule[1] * num)
                    app.totalNum += parseInt(num) 
                    if (num > 0) {
                       app.storeRule.push({"store": parseFloat(rule[0]), "num": parseInt(num), "amount": parseFloat(amount), "upStore": parseFloat(rule[1])})
                    }
                })
              resolve(result)
            })
            .catch(err => reject(err))
        })
      },
      
      initData() {
        this.storeRule = [],
        this.totalAmount = 0,
        this.getTotalAmount = 0,
        this.totalNum = 0
      },
      
      // 选择支付方式
      handleSelectPayType(value) {
        this.curPayType = value
      },

      // 订单提交
      onSubmitOrder() {
        const app = this
        if (app.disabled) {
            app.$toast('请勿重复提交订单哦');
            return false
        }
        // 表单验证
        if (!app.onVerifyFrom()) {
            return false
        }
        // 按钮禁用
        app.disabled = true
        // 请求api
        SettlementApi.submit(app.couponId, app.selectNum, "prestore", app.remark, 0, 0, 0, "", 0, 0, 0, "", "JSAPI")
          .then(result => app.onSubmitCallback(result))
          .catch(err => {
            if (err.result) {
              const errData = err.result.data
              if (errData.isCreated) {
                  app.navToMyOrder(errData.orderInfo.id)
                  return false
              }
            }
            app.disabled = false
          })
      },

      // 订单提交成功后回调
      onSubmitCallback(result) {
        const app = this
        // 发起微信支付
        if (result.data.payType == PayTypeEnum.WECHAT.value) {
          wxPayment(result.data.payment)
            .then(() => app.$success('支付成功'))
            .catch(err => app.$error('支付失败'))
            .finally(() => {
              app.disabled = false
              app.navToMyOrder(result.data.orderInfo.id)
            })
        }
        
        // 余额支付
        if (result.data.payType == PayTypeEnum.BALANCE.value) {
          app.$success('支付成功')
          app.disabled = false
          app.navToMyOrder(result.data.orderInfo.id)
        }
      },

      // 跳转到我的订单(等待1秒)
      navToMyOrder(orderId) {
        setTimeout(() => {
          this.$navTo('pages/order/detail?orderId='+orderId)
        }, 1000)
      },

      // 表单验证
      onVerifyFrom() {
        const app = this
        if (app.hasError) {
          app.$toast(app.errorMsg)
          return false
        }
        return true
      },

    }
  }
</script>

<style lang="scss" scoped>
  @import "./style.scss";
</style>
