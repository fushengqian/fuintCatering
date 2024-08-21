<template>
  <view class="container p-bottom">
    <view class="flow-mode">
        <selectSwitch :switchList="orderModeList" checked_bj_color="#113a28" @change="switchMode"/> 
    </view>
    <!-- 快递配送：配送地址 -->
    <view @click="onSelectAddress" v-if="orderMode == false" class="flow-delivery">
      <view class="flow-delivery__detail dis-flex flex-y-center">
        <view class="detail-location dis-flex">
          <text class="iconfont icon-dingwei"></text>
        </view>
        <view class="detail-content flex-box">
          <block v-if="address">
            <view class="detail-content__title dis-flex">
              <text class="f-30">{{ address.name }}</text>
              <text class="detail-content__title-phone f-28">{{ address.mobile }}</text>
            </view>
            <view class="address-info detail-content__describe">
              <text class="region">{{ address.provinceName }}{{ address.cityName }}{{ address.regionName }}</text>
              <text class="detail">{{ address.detail }}</text>
              <text class="icon"> » </text>
            </view>
          </block>
          <block v-else>
            <view class="detail-content__describe dis-flex select-address">
              <text class="col-6">请选择配送地址 <text class="icon"> > </text></text>
            </view>
          </block>
        </view>
        <view class="detail-arrow dis-flex">
          <text class="iconfont icon-arrow-right"></text>
        </view>
      </view>
    </view>
    
    <!-- 门店自提：自提地址 -->
    <view v-if="orderMode == true" class="flow-delivery">
      <view class="flow-delivery__detail dis-flex flex-y-center">
        <view class="detail-location dis-flex">
           <text class="iconfont icon-dingwei"></text>
        </view>
        <view class="detail-content flex-box">
          <block>
            <view class="store">
                <view class="f-30 store-name">{{ storeInfo ? storeInfo.name : '' }}</view>
                <view class="f-30 store-phone">{{ storeInfo ? storeInfo.phone : '' }}</view>
                <text class="f-30 store-address">{{ storeInfo ? storeInfo.address : '' }}</text>
            </view>
          </block>
        </view>
        <view class="detail-arrow dis-flex">
          <text class="iconfont icon-arrow-right"></text>
        </view>
      </view>
    </view>
    
    <!-- 商品列表 -->
    <view class="m-top20">
      <view v-for="(item, index) in goodsCart" :key="index" class="checkout_list">
        <view class="flow-shopList dis-flex">
          <!-- 商品图片 -->
          <view class="flow-list-left">
            <image mode="scaleToFill" :src="item.goodsInfo.logo"></image>
          </view>
          <view class="flow-list-right flex-box">
            <!-- 商品名称 -->
            <text class="goods-name twolist-hidden">{{ item.goodsInfo.name }}</text>
            
            <!-- 商品规格 -->
            <view class="goods-props clearfix">
              <view class="goods-props-item" v-for="(props, idx) in item.specList" :key="idx">
                <text class="group-name">{{ props.specName }}: </text>
                <text>{{ props.specValue }}；</text>
              </view>
            </view>
            
             <!-- 商品数量和单价 -->
            <view class="flow-list-cont dis-flex flex-x-between flex-y-center">
              <text class="small"> x {{ item.num }} </text>
              <text class="flow-cont">￥{{ item.goodsInfo.price }} </text>
            </view>
          </view>
        </view>
      </view>
      <view class="flow-num-box b-f">
        <text>共{{ totalNum }}件，合计：</text>
        <text class="flow-money col-m">￥{{ totalPrice }}</text>
      </view>
    </view>
    
    <!-- 订单折扣 -->
    <view class="flow-all-money b-f m-top20">
      <!-- 卡券 -->
      <view class="flow-all-list dis-flex">
        <text class="flex-five">使用卡券抵扣：</text>
        <view class="flex-five t-r">
          <view v-if="couponList.length > 0" @click="handleShowPopup()">
            <text class="col-m" v-if="useCouponInfo">-￥{{ useCouponInfo.amount }}</text>
            <text class="col-m" v-else>共有卡券{{ couponList.length }}张</text>
            <text class="right-arrow iconfont icon-arrow-right"></text>
          </view>
          <text v-else class="">无卡券可用</text>
        </view>
      </view>
      <!-- 积分抵扣 -->
      <view class="points flow-all-list dis-flex flex-y-center" v-if="usePoint > 0">
        <view class="block-left flex-five" @click="handleShowPoints()">
          <text class="title">使用{{ usePoint }}积分抵扣：</text>
          <text class="iconfont icon-help"></text>
        </view>
        <view class="flex-five dis-flex flex-x-end flex-y-center">
          <text class="points-money col-m">-￥{{ usePointAmount }}</text>
          <u-switch v-model="isUsePoints" size="48" active-color="#113a28" @change="getCartList()"></u-switch>
        </view>
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
          <view class="col-amount-do">支付金额：
              <text class="pay-amount">￥{{ payPrice ? payPrice.toFixed(2) : '0.00' }}</text>
              <view v-if="deliveryFee > 0 && orderMode == false" class="delivery-fee">（ 配送费：￥{{ deliveryFee.toFixed(2) }} ）</view>
          </view>
        </view>
        <view class="chackout-right" @click="onSubmitOrder()">
          <view class="flow-btn f-32" :class="{ disabled }">提交订单</view>
        </view>
      </view>
    </view>
    
     <!-- 积分说明弹窗 -->
        <u-modal v-model="showPoints" :title="`积分说明`">
          <scroll-view class="points-content" :scroll-y="true">
            <text>积分兑换金额</text>
          </scroll-view>
        </u-modal>
    
        <!-- 卡券弹出框 -->
        <u-popup v-model="showPopup" mode="bottom">
          <view class="popup__coupon">
            <view class="coupon__title f-30">选择卡券</view>
            <!-- 卡券列表 -->
            <view class="coupon-list">
              <scroll-view :scroll-y="true" style="height: 565rpx;">
                <view class="coupon-item" v-for="(item, index) in couponList" :key="index">
                  <view class="item-wrapper"
                    :class="[item.status == 'A' ? 'color-default': 'color-gray']"
                    @click="handleSelectCoupon(index)">
                    <view class="coupon-type">{{ item.type }}</view>
                    <view class="tip dis-flex flex-dir-column flex-x-center">
                      <text class="money">￥{{ item.amount }}</text>
                      <text class="pay-line">{{ item.description }}</text>
                    </view>
                    <view class="split-line"></view>
                    <view class="content dis-flex flex-dir-column flex-x-between">
                      <view class="title">{{ item.name }}</view>
                      <view class="bottom dis-flex flex-y-center">
                        <view class="time flex-box">
                          <block>{{ item.effectiveDate }}</block>
                        </view>
                      </view>
                    </view>
                  </view>
                </view>
              </scroll-view>
            </view>
            <!-- 不使用卡券 -->
            <view class="coupon__do_not dis-flex flex-y-center flex-x-center">
              <view class="control dis-flex flex-y-center flex-x-center" @click="handleNotUseCoupon()">
                <text class="f-26">不使用卡券</text>
              </view>
            </view>
          </view>
        </u-popup>
        
        <!-- 支付方式弹窗 -->
        <u-popup v-model="showPayPopup" mode="bottom" :closeable="true">
          <view class="pay-type-popup">
            <view class="title">请选择支付方式</view>
            <view class="pop-content">
              <!-- 微信支付 -->
              <view class="pay-item dis-flex flex-x-between" @click="doSubmitOrder(PayTypeEnum.WECHAT.value)">
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
              <view class="pay-item dis-flex flex-x-between" @click="doSubmitOrder(PayTypeEnum.BALANCE.value)">
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
  </view>
</template>

<script>
  import * as Verify from '@/utils/verify'
  import * as CartApi from '@/api/cart'
  import * as SettlementApi from '@/api/settlement'
  import DeliveryTypeEnum from '@/common/enum/order/DeliveryType'
  import PayTypeEnum from '@/common/enum/order/PayType'
  import { wxPayment } from '@/utils/app'
  import selectSwitch from "@/components/xuan-switch/xuan-switch.vue";
  import * as AddressApi from '@/api/address'
  import * as settingApi from '@/api/setting'

  export default {
    components: {
      selectSwitch
    },
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
        goodsCart: [],
        // 优惠券列表
        couponList: [],
        totalPrice: 0,
        payPrice: 0,
        totalNum: 0,
        deliveryFee: 0,
        orderModeList: ['堂食自提', '配送到家'],
        orderMode: true,
        address: null,
        useCouponInfo: null,
        selectCouponId: 0,
        myPoint: 0,
        usePoint: 0,
        usePointAmount: 0.00,
        // 是否使用积分抵扣
        isUsePoints: true,
        // 是否显示积分说明
        showPoints: false,
        // 是否显示卡券弹窗
        showPopup: false,
        storeInfo: null,
        // 支付方式弹窗
        showPayPopup: false,
        // 订单ID
        orderId: ""
      }
    },

    /**
     * 生命周期函数--监听页面加载
     */
    onLoad(options) {
      this.options = options;
    },

    /**
     * 生命周期函数--监听页面显示
     */
    onShow() {
        const app = this;
        if (app.orderId) {
            app.navToOrderResult(app.orderId);
        }
        // 获取购物车信息
        app.getCartList();
        // 获取默认收货地址
        app.getDefaultAddress();
        // 获取店铺信息
        app.getStoreInfo();
    },

    methods: {
      // 获取购物车信息
      getCartList() {
        const app = this
        if (!app.isUsePoints) {
            app.usePoint = 0;
            app.usePointAmount = 0;
        }
        return new Promise((resolve, reject) => {
          // 配送或自取
          let orderMode = "oneself";
          if (!app.orderMode) {
              orderMode = "express";
          }
          CartApi.list(app.options.cartIds, app.options.goodsId, app.options.skuId, app.options.buyNum, app.selectCouponId, app.isUsePoints, orderMode)
            .then(result => {
              app.goodsCart = result.data.list;
              app.totalNum = result.data.totalNum;
              app.totalPrice = result.data.totalPrice;
              app.payPrice = result.data.payPrice;
              app.couponList = result.data.couponList;
              app.useCouponInfo = result.data.useCouponInfo;
              app.usePoint = result.data.usePoint;
              app.myPoint = result.data.myPoint;
              app.deliveryFee = result.data.deliveryFee;
              if (app.usePoint < 1) {
                  app.isUsePoints = false;
              }
              app.usePointAmount = result.data.usePointAmount;
              resolve(result);
            })
            .catch(err => reject(err))
        })
      },
      
      // 获取默认收货地址
      getDefaultAddress() {
        const app = this
        AddressApi.detail(0)
          .then(result => {
              app.address = result.data.address ? result.data.address : null;
          })
      },
      
      // 显示积分说明
      handleShowPoints() {
          this.showPoints = true;
      },

      // 显示卡券弹窗
      handleShowPopup() {
          this.showPopup = true;
      },

      // 选择卡券
      handleSelectCoupon(index) {
          const app = this;
          // 当前选择的卡券
          const couponItem = app.couponList[index];
          // 记录选中的卡券id
          if (couponItem.status != 'A') {
              app.$error('该卡券不可用');
              return false;
          }
          app.selectCouponId = couponItem.userCouponId;
          // 重新获取购物车信息
          app.getCartList();
          // 隐藏卡券弹层
          app.showPopup = false;
      },

      // 不使用卡券
      handleNotUseCoupon() {
          const app = this;
          app.selectCouponId = 0;
          // 重新获取购物车信息
          app.getCartList();
          // 隐藏卡券弹层
          app.showPopup = false;
      },
      
      // 选择支付方式
      handleSelectPayType(value) {
        this.curPayType = value
      },
      
      // 快递配送：选择收货地址
      onSelectAddress() {
         this.$navTo('pages/address/index', { from: 'checkout' })
      },
      
      // 切换配送模式
      switchMode(mode) {
         const app = this;
         app.orderMode = mode;
         if (mode && !app.storeInfo) {
             app.getStoreInfo();
         }
         app.getCartList();
      },
      
      // 获取店铺详情
      getStoreInfo() {
         const app = this;
         if (!app.storeInfo) {
             settingApi.storeDetail()
             .then(result => {
                 app.storeInfo = result.data.storeInfo;
             })
         }
      },
      
      // 弹出支付方式
      onSubmitOrder() {
          const app = this
          if (app.disabled) {
              return false
          }
          
          const tableId = uni.getStorageSync("tableId") ? uni.getStorageSync("tableId") : 0;
          if (tableId > 0) {
              return app.doSubmitOrder(PayTypeEnum.WECHAT.value);
          }
          
          if (app.totalPrice < 0 || app.goodsCart.length < 1) {
              app.disabled = true
              return false
          }
          
          // 表单验证
          if (!app.orderMode && app.address == undefined) {
              app.$toast('请先选择配送地址哦')
              return false
          }
          
          // 配送或自取
          let orderMode = "oneself";
          if (!app.orderMode) {
              orderMode = "express";
          }
          
          app.showPayPopup = true;
      },

      // 订单提交
      doSubmitOrder(payType) {
        const app = this;
        if (app.disabled) {
            app.$toast('请勿重复提交订单');
            return false;
        }
        
        if (app.totalPrice < 0 || app.goodsCart.length < 1) {
            app.$toast('提交订单商品有误');
            app.disabled = true;
            return false;
        }
        
        // 表单验证
        if (!app.orderMode && app.address == undefined) {
            app.$toast('请先选择配送地址哦');
            return false;
        }
        
        // 配送或自取
        let orderMode = "oneself";
        if (!app.orderMode) {
            orderMode = "express";
        }
        
        // 按钮禁用
        app.disabled = true;
        
        // 请求api
        SettlementApi.submit(0, "", "goods", app.remark, 0, app.usePoint, app.selectCouponId, app.options.cartIds, app.options.goodsId, app.options.skuId, app.options.buyNum, orderMode, payType)
          .then(result => {
              app.onSubmitCallback(result);
          })
          .catch(err => {
            if (err.result) {
                const errData = err.result.data;
                if (errData.isCreated) {
                    app.navToOrderResult(errData.orderInfo.id);
                    return false;
                }
            }
            app.disabled = false;
          })
      },

      // 订单提交成功后回调
      onSubmitCallback(result) {
        const app = this;
        if (result.code != '200' && !result.data) {
            if (result.message) {
                app.$error(result.message);
            } else {
                app.$error('订单提交失败');
            }
            app.disabled = false;
            return false;
        }
        
        const tableId = uni.getStorageSync("tableId") ? uni.getStorageSync("tableId") : 0;
        if (tableId > 0) {
            app.navToOrderResult(result.data.orderInfo.id, '订单提交成功');
            return false;
        }
        
        // 发起微信支付
        if (result.data.payType == PayTypeEnum.WECHAT.value) {
            // #ifdef H5
            app.orderId = result.data.orderInfo.id;
            // #endif
            wxPayment(result.data.payment).then(() => {
                app.$success('支付成功');
            }).catch(err => {
                app.$error('支付失败');
            }).finally(() => {
                app.disabled = false;
                app.navToOrderResult(result.data.orderInfo.id, '');
            })
        }
        
        // 余额支付
        if (result.data.payType == PayTypeEnum.BALANCE.value) {
            app.disabled = false;
            app.navToOrderResult(result.data.orderInfo.id, result.message);
        }
      },

      // 跳转到订单结果页
      navToOrderResult(orderId, message) {
        if (!message || message == undefined) {
            message = "";
        }
        this.$navTo('pages/order/result?orderId='+orderId+'&message=' + message);
      },
      
      /**
       * 下拉刷新
       */
      onPullDownRefresh() {
          const app = this;
          setTimeout(() => {
            // 获取购物车信息
            app.getCartList();
            // 获取默认收货地址
            app.getDefaultAddress();
            // 获取店铺信息
            app.getStoreInfo();
            uni.stopPullDownRefresh();
          }, 1000)
      }
    }
  }
</script>

<style lang="scss" scoped>
  @import "./style.scss";
</style>
