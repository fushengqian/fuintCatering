<template>
  <view v-if="!isLoading" class="container" :style="themeVars">

    <view class="header">
      <!-- 订单状态 -->
      <view class="order-status">
        <view class="status-icon">
          <!-- 进行中的订单 -->
          <block>
              <!-- 待支付 -->
              <block>
                 <image v-if="order.status == OrderStatusEnum.CREATED.value" class="image" src="/static/order/status/wait_pay.png" mode="aspectFit"></image>
              </block>
              <!-- 已支付 -->
              <block>
                <image v-if="order.status == OrderStatusEnum.PAID.value" class="image" src="/static/order/status/received.png" mode="aspectFit"></image>
              </block>
              <!-- 待发货 -->
              <block>
                <image v-if="order.status == OrderStatusEnum.DELIVERY.value" class="image" src="/static/order/status/wait_deliver.png" mode="aspectFit"></image>
              </block>
              <!-- 已发货 -->
              <block>
                <image v-if="order.status == OrderStatusEnum.DELIVERED.value" class="image" src="/static/order/status/wait_receipt.png" mode="aspectFit"></image>
              </block>
              <!-- 已收货-->
              <block>
                <image v-if="order.status == OrderStatusEnum.RECEIVED.value || order.status == OrderStatusEnum.COMPLETE.value" class="image" src="/static/order/status/received.png" mode="aspectFit"></image>
              </block>
              <!-- 已取消-->
              <block>
                <image v-if="order.status == OrderStatusEnum.CANCEL.value || order.status == OrderStatusEnum.REFUND.value" class="image" src="/static/order/status/close.png" mode="aspectFit"></image>
              </block>
            </block>
        </view>
        <view class="status-text">
          <text v-if="order.status == OrderStatusEnum.CREATED.value">{{OrderStatusEnum.CREATED.name}}</text>
          <text v-else-if="order.status == OrderStatusEnum.PAID.value">{{OrderStatusEnum.PAID.name}}</text>
          <text v-else-if="order.status == OrderStatusEnum.DELIVERY.value">{{OrderStatusEnum.DELIVERY.name}}</text>
          <text v-else-if="order.status == OrderStatusEnum.DELIVERED.value">{{OrderStatusEnum.DELIVERED.name}}</text>
          <text v-else-if="order.status == OrderStatusEnum.RECEIVED.value">{{OrderStatusEnum.RECEIVED.name}}</text>
          <text v-else-if="order.status == OrderStatusEnum.CANCEL.value">{{OrderStatusEnum.CANCEL.name}}</text>
          <text v-else-if="order.status == OrderStatusEnum.REFUND.value">{{OrderStatusEnum.REFUND.name}}</text>
          <text v-else-if="order.status == OrderStatusEnum.COMPLETE.value">{{OrderStatusEnum.COMPLETE.name}}</text>
        </view>
        <view class="verify-btn" v-if="order.orderMode == 'oneself' && order.type == 'goods' && order.verifyCode && order.payStatus == 'B' && !order.tableInfo && ( !['C', 'H', 'G'].includes(order.status))" @click="handleShowVerifyPopup">
          <u-icon name="scan" size="28" color="#333"></u-icon>
          <text>提取码</text>
        </view>
      </view>
    </view>
    
    <!--订单类型-->
    <view class="order-type">
      <text class="type">{{ order.typeName }}</text>
      <text class="pickup-no">取单号：{{ order.pickupNo }}</text>
    </view>

    <!-- 快递配送：配送地址 -->
    <view v-if="order.address" class="delivery-address i-card">
      <view class="link-man">
        <text class="name">{{ order.address.name }}</text>
        <text class="phone">{{ order.address.mobile }}</text>
      </view>
      <view class="address">
        <text class="region">{{ order.address.provinceName }}{{ order.address.cityName }}{{ order.address.regionName }}</text>
        <text class="detail">{{ order.address.detail }}</text>
      </view>
    </view>
    
    <!-- 门店自提：自提地址 -->
    <view v-if="order.orderMode == 'oneself'" class="delivery-address i-card">
      <view class="link-man">
        <text class="type">[门店自提]</text>
      </view>
      <view class="link-man">
        <text class="name">{{ order.storeInfo.name }}</text>
        <text class="phone">{{ order.storeInfo.phone }}</text>
      </view>
      <view class="address">
        <text class="region">{{ order.storeInfo.address }}</text>
      </view>
    </view>

    <!-- 商品列表 -->
    <!-- 快递配送：物流信息入口 -->
    <view v-if="showExpressEntry" class="express-entry i-card" @click="handleViewExpress">
      <view class="entry-left">
        <text class="entry-title">物流信息</text>
        <text class="entry-desc" v-if="order.expressInfo && order.expressInfo.expressNo">
          {{ order.expressInfo.expressCompany }} {{ order.expressInfo.expressNo }}
        </text>
        <text class="entry-desc" v-else>查看配送进度</text>
      </view>
      <view class="entry-right">
        <text class="entry-status">{{ expressInfo && expressInfo.stateText ? expressInfo.stateText : '查看物流' }}</text>
        <text class="entry-arrow iconfont icon-xiangyoujiantou"></text>
      </view>
    </view>

    <view class="goods-list i-card" v-if="order.goods.length > 0">
      <view class="goods-item" v-for="(goods, idx) in order.goods" :key="idx">
        <view class="goods-main" v-if="goods.num > 0" @click="handleTargetGoods(goods.goodsId, goods.type)">
          <!-- 商品图片 -->
          <view class="goods-image">
            <image class="image" :src="goods.image" mode="aspectFill"></image>
          </view>
          <!-- 商品信息 -->
          <view class="goods-content">
            <view class="goods-title twolist-hidden"><text>{{goods.name}}</text></view>
            <view class="goods-props clearfix">
              <view class="goods-props-item" v-for="(props, idx) in goods.specList" :key="idx">
                <text>{{ props.specValue }}</text>
              </view>
            </view>
          </view>
          <!-- 交易信息 -->
          <view class="goods-trade">
            <view class="goods-price">
              <text class="unit">￥</text>
              <text class="value">{{ goods.price }}</text>
            </view>
            <view class="goods-num">
              <text>×{{goods.num}}</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 订单信息 -->
    <view class="order-info i-card">
      <view class="info-item">
        <view class="item-lable">订单编号</view>
        <view class="item-content">
          <text>{{order.orderSn}}</text>
          <view class="act-copy" @click="handleCopy(order.orderSn)">
            <text>复制</text>
          </view>
        </view>
      </view>
      <view class="info-item">
        <view class="item-lable">下单时间</view>
        <view class="item-content">
          <text>{{order.createTime}}</text>
        </view>
      </view>
      <view class="info-item">
        <view class="item-lable">支付时间</view>
        <view class="item-content">
          <text>{{order.payTime ? order.payTime : '--'}}</text>
        </view>
      </view>
      <view class="info-item">
        <view class="item-lable">备注信息</view>
        <view class="item-content">
          <text>{{order.remark ? order.remark : '--'}}</text>
        </view>
      </view>
    </view>

    <!-- 结算信息 -->
    <view class="trade-info i-card">
      <view class="info-item">
        <view class="item-lable">订单金额</view>
        <view class="item-content">
          <text>￥{{ order.amount ? Number(order.amount).toFixed(2) : '0.00' }}</text>
        </view>
      </view>
      <view v-if="order.discount > 0" class="info-item">
        <view class="item-lable">优惠金额</view>
        <view class="item-content">
          <text>-￥{{ order.discount.toFixed(2) }}</text>
        </view>
      </view>
      <view v-if="order.deliveryFee > 0" class="info-item">
        <view class="item-lable">配送费用</view>
        <view class="item-content">
          <text>+￥{{ order.deliveryFee.toFixed(2) }}</text>
        </view>
      </view>
      <!-- 积分兑换订单：显示消耗积分 -->
      <view v-if="order.type == 'exchange'" class="info-item">
        <view class="item-lable">兑换消耗积分</view>
        <view class="item-content">
          <text>{{ order.usePoint ? order.usePoint : 0 }} 积分</text>
        </view>
      </view>
      <view v-if="order.pointAmount && Number(order.pointAmount) > 0" class="info-item">
        <view class="item-lable">积分抵扣</view>
        <view class="item-content">
          <text>-￥{{ Number(order.pointAmount).toFixed(2) }}</text>
        </view>
      </view>
      <!-- 使用卡券信息 -->
      <view v-if="order.couponInfoList && order.couponInfoList.length > 0" class="info-item coupon-used">
        <view class="item-lable">使用卡券</view>
        <view class="item-content">
          <view class="coupon-used-list">
            <view class="coupon-used-item" v-for="(coupon, idx) in order.couponInfoList" :key="idx">
              <text class="coupon-name">{{ coupon.name }}</text>
              <text class="coupon-amount">-￥{{ coupon.amount ? coupon.amount.toFixed(2) : '0.00' }}</text>
            </view>
          </view>
        </view>
      </view>
      <view class="info-item" v-if="order.payStatus == 'B'">
        <view class="item-lable">支付方式</view>
        <view class="item-content">
          <text>{{ PayTypeEnum.getNameByValue(order.payType) }}</text>
        </view>
      </view>
      <view class="divider"></view>
      <view class="trade-total">
        <text class="lable">实付款</text>
        <view class="goods-price">
          <text class="unit">￥</text>
          <text class="value">{{order.payAmount ? order.payAmount.toFixed(2) : '0.00'}}</text>
        </view>
      </view>
    </view>

    <!-- 底部操作按钮 -->
    <view class="footer-fixed" v-if="order.status == OrderStatusEnum.CREATED.value">
      <view class="btn-wrapper">
        <block v-if="payFirst == 'Y' || !order.tableInfo">
          <view class="btn-item" @click="onCancel(order.id)">取消订单</view>
        </block>
        <block v-if="order.tableInfo">
          <view class="btn-item" @click="onContinue(order.id, order.tableInfo.id)">继续点单</view>
        </block>
        <block>
          <view class="btn-item active" @click="onPay(order.id)">去支付</view>
        </block>
      </view>
    </view>
    
    <!-- 已支付的订单 -->
    <view class="footer-fixed" v-if="(order.payStatus == OrderStatusEnum.PAID.value) && !order.tableInfo">
      <view class="btn-wrapper">
        <block v-if="!order.refundInfo">
          <view class="btn-item active" @click="handleApplyRefund(order.id)">申请售后</view>
        </block>
        <block v-if="order.refundInfo">
          <view class="btn-item common" @click="handleRefundDetail(order.refundInfo.id)">售后详情</view>
        </block>
      </view>
    </view>
    
    <view class="footer-fixed" v-if="order.status == OrderStatusEnum.DELIVERED.value">
      <view class="btn-wrapper">
        <block>
          <view class="btn-item active" @click="onReceipt(order.id)">确认收货</view>
        </block>
      </view>
    </view>

    <!-- 支付方式弹窗 -->
    <u-popup v-model="showPayPopup" mode="bottom" :closeable="true">
      <view class="pay-popup">
        <view class="title">请选择支付方式</view>
        <view class="pop-content">
          <!-- 微信支付 -->
          <view class="pay-item dis-flex flex-x-between" @click="onSelectPayType(PayTypeEnum.WECHAT.value)">
            <view class="item-left dis-flex flex-y-center">
              <view class="item-left_icon wechat">
                <text class="iconfont icon-weixinzhifu"></text>
              </view>
              <view class="item-left_text">
                <text>{{ PayTypeEnum.WECHAT.name }}</text>
              </view>
            </view>
          </view>
          <!-- 余额支付 -->
          <view v-if="order.type != 'recharge' && order.type != 'prestore'" class="pay-item dis-flex flex-x-between" @click="onSelectPayType(PayTypeEnum.BALANCE.value)">
            <view class="item-left dis-flex flex-y-center">
              <view class="item-left_icon balance">
                <text class="iconfont icon-qiandai"></text>
              </view>
              <view class="item-left_text">
                <text>{{ PayTypeEnum.BALANCE.name }}</text>
              </view>
            </view>
          </view>
        </view>
      </view>
    </u-popup>
    
    <!-- 核销二维码弹窗 -->
    <u-popup v-model="showVerifyPopup" mode="center" border-radius="20" :closeable="true">
      <view class="verify-popup">
        <view class="popup-title">订单提取码</view>
        <view class="popup-tip">请向店员出示二维码核销</view>
        <view class="qr-code-box" v-if="qrCodeImage">
          <image class="qr-image" :src="qrCodeImage" mode="aspectFit"></image>
        </view>
        <view class="code-text" v-if="verifyCode">
          <text class="label">提取码：</text>
          <text class="code">{{ verifyCode }}</text>
        </view>
        <view class="popup-close-btn" @click="showVerifyPopup = false">
          <text>关闭</text>
        </view>
      </view>
    </u-popup>

    <!-- 物流信息弹窗 -->
    <u-popup v-model="showExpressPopup" mode="bottom" border-radius="20" :closeable="true" @close="showExpressPopup = false">
      <view class="express-popup">
        <view class="popup-title">物流详情</view>
        <view class="express-loading" v-if="expressLoading">
          <text>正在查询物流信息...</text>
        </view>
        <block v-else-if="expressInfo">
          <view class="express-brief">
            <view class="brief-row">
              <text class="brief-label">物流公司</text>
              <text class="brief-value">{{ expressInfo.expressCompany }}</text>
            </view>
            <view class="brief-row">
              <text class="brief-label">物流单号</text>
              <text class="brief-value">{{ expressInfo.expressNo }}</text>
              <text class="brief-copy" @click="handleCopy(expressInfo.expressNo)">复制</text>
            </view>
            <view class="brief-row" v-if="expressInfo.expressTime">
              <text class="brief-label">发货时间</text>
              <text class="brief-value">{{ expressInfo.expressTime }}</text>
            </view>
            <view class="brief-row">
              <text class="brief-label">物流状态</text>
              <text class="brief-value brief-status">{{ expressInfo.stateText }}</text>
            </view>
          </view>
          <view class="express-timeline" v-if="expressInfo.traceList && expressInfo.traceList.length > 0">
            <view class="timeline-item" v-for="(item, idx) in expressInfo.traceList" :key="idx"
              :class="{ 'timeline-item--current': idx === 0 }">
              <view class="timeline-dot"></view>
              <view class="timeline-body">
                <text class="timeline-text">{{ item.context }}</text>
                <text class="timeline-time">{{ item.time || item.ftime }}</text>
              </view>
            </view>
          </view>
          <view class="express-empty" v-else>
            <text>暂无物流轨迹，请稍后再试</text>
          </view>
        </block>
      </view>
    </u-popup>

    <!-- 快捷导航 -->
    <shortcut/>
  </view>
</template>

<script>
  import {
    DeliveryStatusEnum,
    DeliveryTypeEnum,
    OrderStatusEnum,
    PayStatusEnum,
    PayTypeEnum,
    ReceiptStatusEnum
  } from '@/common/enum/order'
  import * as OrderApi from '@/api/order'
  import { wxPayment } from '@/utils/app'
  import Shortcut from '@/components/shortcut'

  export default {
    components: {
       Shortcut
    },
    data() {
      return {
        // 枚举类
        DeliveryStatusEnum,
        DeliveryTypeEnum,
        OrderStatusEnum,
        PayStatusEnum,
        PayTypeEnum,
        ReceiptStatusEnum,
        // 当前订单ID
        orderId: null,
        // 桌码ID
        tableId: 0,
        // 加载中
        isLoading: true,
        // 当前订单详情
        order: {},
        // 当前设置
        setting: {},
        // 支付方式弹窗
        showPayPopup: false,
        // 核销二维码弹窗
        showVerifyPopup: false,
        qrCodeImage: '',
        verifyCode: '',
        // 刷新页面
        reflash: false,
        // 物流信息弹窗
        showExpressPopup: false,
        expressLoading: false,
        expressInfo: null,
		payFirst: uni.getStorageSync("payFirst") ? uni.getStorageSync("payFirst") : 'Y'
      }
    },

    computed: {

      // 是否展示物流信息入口：配送订单 + 已发货 + 已填写物流单号
      showExpressEntry() {
        const order = this.order || {}
        if (order.orderMode !== 'express') {
          return false
        }
        if (!order.expressInfo || !order.expressInfo.expressNo) {
          return false
        }
        // 商家自送没有第三方物流轨迹
        if (order.expressInfo.expressCode === 'SELF') {
          return false
        }
        // 待支付、已支付、待发货状态还没有物流轨迹
        const noTraceStatus = [OrderStatusEnum.CREATED.value, OrderStatusEnum.PAID.value, OrderStatusEnum.DELIVERY.value]
        return !noTraceStatus.includes(order.status)
      }
    },

    /**
     * 生命周期函数--监听页面加载
     */
    onLoad({ orderId }) {
      // 当前订单ID
      this.orderId = orderId;
      this.tableId = uni.getStorageSync("tableId") ? uni.getStorageSync("tableId") : 0;
    },

    /**
     * 生命周期函数--监听页面显示
     */
    onShow() {
      // 获取当前订单信息
      this.getOrderDetail();
    },

    methods: {

      // 获取当前订单信息
      getOrderDetail() {
        const app = this
        app.isLoading = true
        OrderApi.detail(app.orderId)
          .then(result => {
            app.order = result.data
            app.setting = result.data
            app.isLoading = false
            // 待核销订单自动弹出核销二维码
            if (app.order.orderMode == 'oneself' && app.order.type == 'goods'
                && app.order.verifyCode && app.order.payStatus == 'B'
                && !app.order.tableInfo && !['C', 'H', 'G'].includes(app.order.status)) {
              app.getVerifyQrCode()
            }
          })
      },

      // 查看物流信息
      handleViewExpress() {
        const app = this
        app.showExpressPopup = true
        app.expressLoading = true
        OrderApi.express(app.orderId)
          .then(result => {
            app.expressInfo = result.data
          })
          .catch(() => {
            app.showExpressPopup = false
          })
          .finally(() => {
            app.expressLoading = false
          })
      },

      // 复制指定内容
      handleCopy(value) {
        const app = this
        uni.setClipboardData({
          data: value,
          success() {
            app.$toast('复制成功')
          }
        })
      },

      // 跳转到商品详情页面
      handleTargetGoods(goodsId, type) {
        if (goodsId && parseInt(goodsId) > 0) {
            this.$navTo('pages/goods/detail', { goodsId })
        }
      },

      // 跳转到申请售后页面
      handleApplyRefund(orderId) {
        this.$navTo('pages/refund/apply', { orderId })
      },
      
      // 售后详情
      handleRefundDetail(refundId) {
        this.$navTo('pages/refund/detail', { refundId })
      },

      // 取消订单
      onCancel() {
        const app = this
        uni.showModal({
          title: '友情提示',
          content: '确认要取消该订单吗？',
          success(o) {
            if (o.confirm) {
              OrderApi.cancel(app.orderId)
                .then(result => {
                  // 显示成功信息
                  app.$success(result.message);
                  // 刷新当前订单数据
                  app.getOrderDetail();
                })
            }
          }
        });
      },
      // 继续点单
      onContinue(orderId, tableId) {
         this.$navTo('pages/category/index');
         uni.setStorageSync('orderId', orderId);
         uni.setStorageSync('tableId', tableId);
      },

      // 点击去支付
      onPay() {
        // 显示支付方式弹窗
        this.showPayPopup = true;
      },
      
      // 确认收货
      onReceipt(orderId) {
          const app = this
          uni.showModal({
            title: '友情提示',
            content: '确认收到商品了吗？',
            success(o) {
              if (o.confirm) {
                OrderApi.receipt(orderId)
                  .then(result => {
                    // 显示成功信息
                    app.$success(result.message)
                    // 刷新当前订单数据
                    app.getOrderDetail()
                  })
              }
            }
          });
       },

      // 选择支付方式
      onSelectPayType(payType) {
        const app = this
        // 隐藏支付方式弹窗
        this.showPayPopup = false
        // 发起支付请求
        OrderApi.pay(app.orderId, payType)
          .then(result => app.onSubmitCallback(result))
          .catch(err => err)
      },
      
      // 显示核销二维码弹窗
      handleShowVerifyPopup() {
        this.getVerifyQrCode();
      },
      
      // 获取核销二维码
      getVerifyQrCode() {
        const app = this;
        OrderApi.verifyQrCode(app.orderId)
          .then(result => {
            if (result.code === 200 && result.data) {
              app.qrCodeImage = result.data.qrCode;
              app.verifyCode = result.data.verifyCode;
              app.showVerifyPopup = true;
            } else {
              app.$error(result.message || '获取提取码失败');
            }
          })
          .catch(() => {
            app.$error('获取提取码失败');
          });
      },

      // 订单提交成功后回调
      onSubmitCallback(result) {
        const app = this;
        if (!result.data) {
            if (result.message) {
                app.$error(result.message);
            } else {
                app.$error('支付失败');
            }
            return false;
        }
        
        // 发起微信支付
        if (result.data.payType == PayTypeEnum.WECHAT.value) {
            wxPayment(result.data.payment)
              .then(() => {
                app.$success('支付成功');
                setTimeout(() => {
                   app.getOrderDetail();
                }, 1500)
              })
              .catch(err => {
                 app.$error('订单未支付');
              })
              .finally(() => {
                 app.disabled = false;
              })
         }
         // 余额支付
         if (result.data.payType == PayTypeEnum.BALANCE.value) {
            if (result.data.orderInfo.payStatus == 'B') {
                app.$success('支付成功');
                app.disabled = false;
                setTimeout(() => {
                    // 刷新当前订单数据
                    app.getOrderDetail();
                }, 1500)
            } else {
                app.$error('支付失败');
            }
         }
      }
    }
  }
</script>

<style>
  page {
    background: #f4f4f4;
  }
</style>
<style lang="scss" scoped>
  .container {
    padding-bottom: 140rpx;
  }

  // 页面顶部
  .header {
    display: flex;
    justify-content: space-between;
    background-color: $fuint-theme;
    height: 280rpx;
    padding: 56rpx 30rpx 0 30rpx;

    .order-status {
      flex: 1;
      display: flex;
      align-items: center;
      height: 128rpx;
      .status-icon {
        width: 128rpx;
        height: 128rpx;
        .image {
          display: block;
          width: 100%;
          height: 100%;
        }
      }
      .status-text {
        padding-left: 20rpx;
        color: #fff;
        font-size: 38rpx;
        font-weight: bold;
      }
      .verify-btn {
        display: flex;
        align-items: center;
        margin-left: auto;
        padding: 8rpx 24rpx;
        font-size: 24rpx;
        color: #333;
        background: #fff;
        border-radius: 28rpx;
        font-weight: bold;
        gap: 6rpx;
      }
    }

    .next-action {
      display: flex;
      align-items: center;
      height: 128rpx;

      .action-btn {
        min-width: 150rpx;
        height: 60rpx;
        padding: 0 30rpx;
        line-height: 60rpx;
        text-align: center;
        border-radius: 30rpx;
        border-color: #ffffff;
        color: #ffffff;
        background: linear-gradient(to right, #f9211c, #ff6335);
        cursor: pointer;
        user-select: none;
      }
    }
  }

  // 通栏卡片
  .i-card {
    background: #fff;
    padding: 24rpx 24rpx;
    width: 94%;
    box-shadow: 0 1rpx 5rpx 0px rgba(0, 0, 0, 0.05);
    margin: 0 auto 20rpx auto;
    border-radius: 20rpx;
  }
  
  // 订单类型
  .order-type {
    font-weight: bold;
    margin: 20rpx 50rpx;
    .pickup-no {
       float: right; 
    }
  }

  // 收货地址
  .delivery-address {
    margin-top: 20rpx;

    .link-man {
      line-height: 46rpx;
      color: #333;
      .type {
        margin-right: 10rpx;
        font-weight: bold;
      }
      .name {
        margin-right: 10rpx;
        color: #999;
      }
      .phone {
        margin-right: 10rpx;
        color: #999;
      }
    }

    .address {
      margin-top: 12rpx;
      color: #999;
      font-size: 24rpx;

      .detail {
        margin-left: 6rpx;
      }
    }

  }

  // 物流公司
  .express {
    display: flex;
    align-items: center;

    .main {
      flex: 1;
    }

    .info-item {
      display: flex;
      margin-bottom: 24rpx;

      &:last-child {
        margin-bottom: 0;
      }

      .item-lable {
        display: flex;
        align-items: center;
        font-size: 24rpx;
        color: #999;
        margin-right: 30rpx;
      }

      .item-content {
        flex: 1;
        display: flex;
        align-items: center;
        font-size: 26rpx;
        color: #333;

        .act-copy {
          margin-left: 20rpx;
          padding: 2rpx 20rpx;
          font-size: 22rpx;
          color: #666;
          border: 1rpx solid #c1c1c1;
          border-radius: 16rpx;
        }
      }
    }
    // 右侧箭头
    .right-arrow {
      margin-left: 16rpx;
      // color: #777;
      font-size: 26rpx;
    }
  }

  // 物流入口
  .express-entry {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-top: 20rpx;

    .entry-left {
      flex: 1;
      overflow: hidden;

      .entry-title {
        font-size: 28rpx;
        font-weight: bold;
        color: #333;
      }

      .entry-desc {
        display: block;
        margin-top: 8rpx;
        font-size: 24rpx;
        color: #999;
      }
    }

    .entry-right {
      display: flex;
      align-items: center;

      .entry-status {
        font-size: 24rpx;
        color: #ff6000;
      }

      .entry-arrow {
        margin-left: 10rpx;
        font-size: 24rpx;
        color: #c1c1c1;
      }
    }
  }

  // 物流详情弹窗
  .express-popup {
    max-height: 900rpx;
    padding: 30rpx 30rpx 40rpx;
    overflow-y: auto;

    .popup-title {
      margin-bottom: 24rpx;
      font-size: 32rpx;
      font-weight: bold;
      color: #333;
      text-align: center;
    }

    .express-loading,
    .express-empty {
      padding: 60rpx 0;
      font-size: 26rpx;
      color: #999;
      text-align: center;
    }

    .express-brief {
      padding: 20rpx 24rpx;
      background: #f8f8f8;
      border-radius: 16rpx;

      .brief-row {
        display: flex;
        align-items: center;
        font-size: 26rpx;
        line-height: 48rpx;

        .brief-label {
          width: 130rpx;
          color: #999;
        }

        .brief-value {
          flex: 1;
          color: #333;
        }

        .brief-status {
          color: #ff6000;
        }

        .brief-copy {
          margin-left: 16rpx;
          padding: 2rpx 16rpx;
          font-size: 22rpx;
          color: #666;
          border: 1rpx solid #c1c1c1;
          border-radius: 16rpx;
        }
      }
    }

    .express-timeline {
      margin-top: 30rpx;
      padding-left: 10rpx;

      .timeline-item {
        position: relative;
        padding: 0 0 32rpx 32rpx;
        border-left: 2rpx solid #e8e8e8;

        &:last-child {
          padding-bottom: 0;
          border-left-color: transparent;
        }

        .timeline-dot {
          position: absolute;
          left: -9rpx;
          top: 8rpx;
          width: 16rpx;
          height: 16rpx;
          border-radius: 50%;
          background: #dcdcdc;
        }

        .timeline-body {
          .timeline-text {
            display: block;
            font-size: 26rpx;
            color: #666;
            line-height: 40rpx;
          }

          .timeline-time {
            display: block;
            margin-top: 6rpx;
            font-size: 22rpx;
            color: #b0b0b0;
          }
        }

        &.timeline-item--current {
          .timeline-dot {
            background: #ff6000;
            box-shadow: 0 0 0 6rpx rgba(255, 96, 0, 0.12);
          }

          .timeline-text {
            color: #333;
          }
        }
      }
    }
  }

  // 商品列表
  .goods-list {
    // 商品项
    .goods-item {
      margin-bottom: 40rpx;
      border-bottom: #f5f5f5 solid 5rpx;
      padding-bottom: 20rpx;
      &:last-child {
        margin-bottom: 0;
      }

      // 商品信息
      .goods-main {
        display: flex;
      }

      // 商品图片
      .goods-image {
        width: 180rpx;
        height: 160rpx;
        position: relative; 
        .image {
          display: block;
          width: 100%;
          height: 100%;
          border-radius: 8rpx;
          object-fit: cover; 
        }
      }

      // 商品内容
      .goods-content {
        flex: 1;
        padding-left: 16rpx;
        padding-top: 16rpx;

        .goods-title {
          font-size: 26rpx;
          max-height: 76rpx;
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

      // 交易信息
      .goods-trade {
        padding-top: 16rpx;
        width: 150rpx;
        text-align: right;
        color: $uni-text-color-grey;
        font-size: 26rpx;

        .goods-price {
          vertical-align: bottom;
          margin-bottom: 16rpx;

          .unit {
            margin-right: -2rpx;
            font-size: 24rpx;
          }
        }
      }

      // 商品售后
      .goods-refund {
        display: flex;
        justify-content: flex-end;

        .stata-text {
          font-size: 24rpx;
          color: #999;
        }

        .action-btn {
          border-radius: 28rpx;
          padding: 8rpx 26rpx;
          font-size: 24rpx;
          color: #383838;
          border: 1rpx solid #a8a8a8;
        }
      }
    }
  }

  // 订单信息
  .order-info {
    .info-item {
      display: flex;
      margin-bottom: 24rpx;

      &:last-child {
        margin-bottom: 0;
      }

      .item-lable {
        display: flex;
        align-items: center;
        font-size: 24rpx;
        color: #999;
        margin-right: 30rpx;
      }

      .item-content {
        flex: 1;
        display: flex;
        align-items: center;
        font-size: 26rpx;
        color: #333;

        .act-copy {
          margin-left: 20rpx;
          padding: 2rpx 20rpx;
          font-size: 22rpx;
          color: #666;
          border: 1rpx solid #c1c1c1;
          border-radius: 16rpx;
        }
      }
    }
  }

  // 交易信息
  .trade-info {
    margin-bottom: 80rpx;
    .info-item {
      display: flex;
      margin-bottom: 24rpx;

      .item-lable {
        font-size: 24rpx;
        color: #999;
        margin-right: 24rpx;
      }

      .item-content {
        flex: 1;
        font-size: 26rpx;
        color: #333;
        text-align: right;
      }
    }

    .coupon-used {
      .item-lable {
        align-self: flex-start;
      }
      .item-content {
        text-align: left;
      }
      .coupon-used-list {
        width: 100%;
        .coupon-used-item {
          display: flex;
          justify-content: space-between;
          padding: 8rpx 0;
          border-bottom: 1rpx dashed #eee;
          &:first-child {
            padding-top: 0;
          }
          &:last-child {
            border-bottom: none;
          }
          .coupon-name {
            color: #666;
            font-size: 24rpx;
          }
          .coupon-amount {
            color: #ff5b57;
            font-size: 24rpx;
            font-weight: bold;
          }
        }
      }
    }

    .divider {
      height: 1rpx;
      background: #f1f1f1;
      margin-bottom: 24rpx;
    }

    .trade-total {
      display: flex;
      justify-content: flex-end;

      .goods-price {
        margin-left: 12rpx;
        vertical-align: bottom;
        color: $uni-text-color-active;

        .unit {
          margin-right: -2rpx;
          font-size: 24rpx;
        }
      }
    }
  }

  /* 底部操作栏 */
  .footer-fixed {
    position: fixed;
    bottom: var(--window-bottom);
    left: 0;
    right: 0;
    height: 180rpx;
    padding-bottom: 30rpx;
    z-index: 11;
    box-shadow: 0 -4rpx 40rpx 0 rgba(97, 97, 97, 0.1);
    background: #fff;

    .btn-wrapper {
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: flex-end;
      padding: 0 30rpx;
    }

    .btn-item {
      min-width: 164rpx;
      border-radius: 8rpx;
      padding: 20rpx 24rpx;
      font-size: 28rpx;
      color: #383838;
      text-align: center;
      border: 1rpx solid #a8a8a8;
      margin-left: 10rpx;
      &.common {
          color: #fff;
          border: none;
          background: linear-gradient(to right, $fuint-theme, $fuint-theme);
      }

      &.active {
        color: #fff;
        border: none;
        background: linear-gradient(to right, #f9211c, #ff6335);
      }
    }
  }

  // 弹出层-支付方式
  .pay-popup {
    padding: 25rpx 25rpx 70rpx 25rpx;
    .title {
      font-size: 30rpx;
      margin-bottom: 50rpx;
      font-weight: bold;
      text-align: center;
    }

    .pop-content {
      min-height: 120rpx;
      padding: 0 20rpx;

      .pay-item {
        padding: 30rpx;
        font-size: 30rpx;
        background: #fff;
        border: 1rpx solid $fuint-theme;
        border-radius: 8rpx;
        color: #888;
        margin-bottom: 12rpx;
        text-align: center;

        .item-left_icon {
          margin-right: 20rpx;
          font-size: 48rpx;

          &.wechat {
            color: #00c800;
          }

          &.balance {
            color: $fuint-theme;
          }
        }
      }
    }
  }

  // 核销二维码弹窗
  .verify-popup {
    width: 500rpx;
    padding: 40rpx 30rpx;
    text-align: center;
    
    .popup-title {
      font-size: 34rpx;
      font-weight: bold;
      color: #333;
      margin-bottom: 16rpx;
    }
    
    .popup-tip {
      font-size: 24rpx;
      color: #999;
      margin-bottom: 30rpx;
    }
    
    .qr-code-box {
      width: 360rpx;
      height: 360rpx;
      margin: 0 auto 30rpx auto;
      padding: 16rpx;
      background: #fff;
      border: 1rpx solid #eee;
      border-radius: 12rpx;
      
      .qr-image {
        width: 100%;
        height: 100%;
      }
    }
    
    .code-text {
      font-size: 28rpx;
      color: #333;
      margin-bottom: 30rpx;
      
      .label {
        color: #666;
      }
      
      .code {
        color: $fuint-theme;
        font-weight: bold;
        font-size: 36rpx;
      }
    }
    
    .popup-close-btn {
      padding: 16rpx 60rpx;
      background: $fuint-theme;
      color: #fff;
      border-radius: 40rpx;
      font-size: 28rpx;
      display: inline-block;
    }
  }
</style>
