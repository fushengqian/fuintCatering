<template>
  <view class="pay-popup popup" catchtouchmove="true" :class="(value && complete) ? 'show' : 'none'"
    @touchmove.stop.prevent="moveHandle">
    <!-- 页面内容开始 -->
    <view class="mask" @click="close('mask')"></view>
    <!-- 页面开始 -->
    <view class="confirm" v-if="!showPayPopup">
        <view class="layer attr-content" :style="'border-radius: 10rpx 10rpx 0 0;'">
          <view class="specification-wrapper">
            <scroll-view class="specification-wrapper-content" scroll-y="true">
              <view class="specification-header">
                <view class="specification-name">支付确认</view>
              </view>
              <view class="specification-content">
                <view v-if="payInfo.isLogin && ((parseFloat(payInfo.totalAmount) - (parseFloat(payInfo.totalAmount) * parseFloat(payInfo.payDiscount))) > 0)" class="pay-item">
                   <view class="item-point">
                      <view class="title">
                         <text class="iconfont icon-success"></text>
                         <text v-if="payInfo.payDiscount < 1" class="point-amount">会员{{parseFloat(payInfo.payDiscount * 10).toFixed(2)}}折优惠<text class="amount">￥{{(parseFloat(payInfo.totalAmount) - ((parseFloat(payInfo.totalAmount) * parseFloat(payInfo.payDiscount)))).toFixed(2)}}</text></text>
                      </view>
                   </view>
                </view>
                <view v-if="payInfo.isLogin && payInfo.pointAmount >= 0.01 && payInfo.canUsedAsMoney == 'true'" class="pay-item">
                    <view class="item-point">
                        <view class="title">
                           <text class="iconfont icon-success"></text>
                           <text v-if="payInfo.usePoint > 0" class="point-amount">使用{{ payInfo.usePoint.toFixed(0) }}积分抵扣</text>
                           <text v-if="payInfo.usePoint > 0" class="amount">￥{{ parseFloat(payInfo.pointAmount).toFixed(2) }}</text>
                           <text v-if="payInfo.usePoint < 1" class="point-amount">不使用积分抵扣</text>
                           <text v-if="payInfo.maxPoint > 0" class="modify" @click="modifyPoint">修改>></text>
                        </view>
                    </view>
                </view>
                <view v-if="!payInfo.isLogin && payInfo.maxPoint < 1" class="pay-item">
                    <view class="item-point">
                        <view class="title">
                           <text class="iconfont icon-success"></text>
                           <text>会员可使用积分进行抵扣哦~</text>
                           <text class="modify" @click="onGetLogin">去登录>></text>
                        </view>
                    </view>
                </view>
                <view v-if="payInfo.isLogin && payInfo.couponInfo !== null" class="pay-item">
                    <view class="item-point">
                        <view class="title">
                           <text class="iconfont icon-success"></text>
                           <text v-if="payInfo.couponAmount > 0" class="point-amount">使用卡券抵扣</text>
                           <text v-if="payInfo.couponAmount > 0" class="amount">￥{{ parseFloat(payInfo.couponAmount).toFixed(2) }}</text>
                           <text v-if="payInfo.couponAmount <= 0 && payInfo.couponInfo.amount" class="point-amount">不使用卡券抵扣？</text>
                           <text v-if="payInfo.couponInfo.amount" class="modify" @click="modifyCoupon">修改>></text>
                        </view>
                    </view>
                </view>
                <view class="pay-item">
                    <view class="item-amount">
                        <view class="title">
                            <text class="iconfont icon-success"></text>
                            实付金额：<text class="amount">￥{{ (parseFloat(payInfo.payAmount)).toFixed(2) }}</text>
                        </view>
                    </view>
                </view>
              </view>
            </scroll-view>
            <view class="close" @click="close('close')" v-if="showClose">
              <image class="close-item" :src="closeImage"></image>
            </view>
          </view>
          <view class="btn-wrapper">
            <view class="sure" @click="toPay">确认支付</view>
          </view>
          <!-- 页面结束 -->
        </view>
        <view class="point-popup">
           <uni-popup ref="pointPopup" type="dialog">
              <uni-popup-dialog mode="input" focus="false" v-model="payInfo.usePoint.toFixed(0)" title="修改积分数量"  type="info" placeholder="请输入积分数量" :before-close="true" @close="closeDialog" @confirm="doUsePoint"></uni-popup-dialog>
           </uni-popup>
        </view>
        <view class="coupon-popup">
           <uni-popup ref="couponPopup" type="dialog">
              <uni-popup-dialog focus="false" v-if="payInfo.couponAmount > 0" v-model="payInfo.usePoint.toFixed(0)" title="确认信息" content="不使用卡券进行抵扣？" type="info" :before-close="true" @close="closeDialog" @confirm="doUseCoupon"></uni-popup-dialog>
              <uni-popup-dialog focus="false" v-if="payInfo.couponAmount <= 0 && payInfo.couponInfo !== null" title="确认信息" :content="'使用卡券最多可抵扣￥'+ payInfo.couponInfo.amount" type="info" :before-close="true" @close="closeDialog" @confirm="doUseCoupon"></uni-popup-dialog>
           </uni-popup>
        </view>
    </view>
    
    <!-- 支付方式弹窗 -->
    <u-popup v-model="showPayPopup" mode="bottom" :closeable="true">
      <view class="pay-type-popup">
        <view class="title">请选择支付方式</view>
        <view class="pop-content">
          <!-- 微信支付 -->
          <view class="pay-item dis-flex flex-x-between" @click="payNow(PayTypeEnum.WECHAT.value)">
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
          <view class="pay-item dis-flex flex-x-between" @click="payNow(PayTypeEnum.BALANCE.value)">
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
    <!-- 页面内容结束 -->
  </view>
</template>

<script>
  import * as SettlementApi from '@/api/settlement'
  import PayTypeEnum from '@/common/enum/order/PayType'
  import { wxPayment } from '@/utils/app'
  
  var that; // 当前页面对象
  var vk; // 自定义函数集
  export default {
    name: 'PayPopup',
    props: {
      // true 组件显示 false 组件隐藏
      value: {
        Type: Boolean,
        default: false
      },
      // vk云函数路由模式参数开始-----------------------------------------------------------
      // 支付信息
      payInfo: {
        Type: Object,
        default: {}
      },
      // vk云函数路由模式参数结束-----------------------------------------------------------
      // 点击遮罩是否关闭组件 true 关闭 false 不关闭 默认true
      maskCloseAble: {
        Type: Boolean,
        default: true
      },
      // 是否显示右上角关闭按钮
      showClose: {
        Type: Boolean,
        default: true
      },
      // 关闭按钮的图片地址
      closeImage: {
        Type: String,
        default: "https://img.alicdn.com/imgextra/i1/121022687/O1CN01ImN0O11VigqwzpLiK_!!121022687.png"
      }
    },
    data() {
      return {
        complete: false, // 组件是否加载完成
        usePoint: '',
        showPayPopup: false,
        PayTypeEnum
      };
    },
    mounted() {
      that = this;
    },
    methods: {
      // 初始化
      init() {
         //empty
      },
      async open() {
        that.complete = true;
        that.$emit("open", true);
        that.$emit("input", true);
      },
      // 监听 - 弹出层收起
      close(s) {
        if (s == "close") {
            that.$emit("input", false);
            that.$emit("close", "close");
        } else if (s == "mask") {
          if (that.maskCloseAble) {
              that.$emit("input", false);
              that.$emit("close", "mask");
          }
        }
      },
      
      moveHandle() {
        // 禁止父元素滑动
      },
      toPay() {
         that.showPayPopup = true
      },
      // 立即支付
      payNow(payType) {
        const app = this
        // 请求api
        let couponId = 0
        if (app.payInfo.couponAmount > 0) {
            couponId = app.payInfo.couponInfo.userCouponId;
        }
        SettlementApi.submit(0, "", "payment", app.payInfo.remark, parseFloat(app.payInfo.totalAmount).toFixed(2), parseInt(app.payInfo.usePoint), couponId, "", 0, 0, 0, "", payType)
          .then(result => app.onSubmitCallback(result))
          .catch(err => {
            if (err.result) {
                const errData = err.result.data;
                if (errData) {
                    return false;
                }
            }
        })
      },
      // 去登录
      onGetLogin() {
        uni.navigateTo({
          url: "/pages/login/index"
        })
      },
      // 订单提交成功后回调
      onSubmitCallback(result) {
        const app = this
        if (result.code != '200') {
            if (result.message) {
                app.$error(result.message);
            } else {
                app.$error('支付失败');
            }
            return false
        }
        
        // 微信支付
        if (result.data.payType == PayTypeEnum.WECHAT.value) {
            wxPayment(result.data.payment)
           .then(() => {
                  app.$navTo(`pages/pay/result`, { amount: parseFloat(result.data.orderInfo.amount).toFixed(2), point: parseInt(result.data.orderInfo.usePoint)})
              })
            .catch(err => 
                app.$error('订单未支付'))
            .finally(() => {
               //empty
            })
        }
          
          // 余额支付
        if (result.data.payType == PayTypeEnum.BALANCE.value) {
            if (result.data.orderInfo.payStatus == 'B') {
                app.$navTo(`pages/pay/result`, { amount: parseFloat(result.data.orderInfo.amount).toFixed(2), point: parseInt(result.data.orderInfo.usePoint)})
            } else {
                if (result.message) {
                    app.$error(result.message);
                } else {
                    app.$error('支付失败');
                }
            }
        }
      },
      modifyPoint() {
          this.$refs.pointPopup.open('top')
      },
      modifyCoupon() {
          this.$refs.couponPopup.open('top')
      },
      doUsePoint(usePoint) {
        if (usePoint.length < 1) {
            usePoint = 0
        }
        if (!(/(^[0-9]\d*$)/.test(usePoint))) {
            this.$error('请输入正整数')
        　　return false
        }
        
        if (usePoint > this.payInfo.maxPoint) {
            if (this.payInfo.maxPoint > 0) {
                this.$error('最多使用' + this.payInfo.maxPoint + '积分')
            } else {
                this.$error('您暂无可用积分')
            }
            return false
        }
        
        this.payInfo.usePoint = usePoint
        
        this.$emit('modifyChoice', this.payInfo)
          this.$refs.pointPopup.close()
      },
      doUseCoupon() {
          if (this.payInfo.couponAmount > 0) {
              this.payInfo.couponAmount = 0
          } else {
              this.payInfo.couponAmount = this.payInfo.couponInfo.amount
          }
          this.$emit('modifyChoice', this.payInfo)
          this.$refs.couponPopup.close()
      },
      closeDialog() {
            this.$refs.pointPopup.close()
          this.$refs.couponPopup.close()
      },
      // 弹窗
      toast(title, icon) {
        uni.showToast({
          title: title,
          icon: icon
        });
      }
    },
    watch: {
      value: function(val) {
        if (val) {
          that.open();
        }
      },
    }
  };
</script>

<style lang="scss" scoped>
   // 弹出层-支付方式
   .pay-type-popup {
     padding: 25rpx 25rpx 70rpx 25rpx;
     .title {
       font-size: 30rpx;
       margin-bottom: 50rpx;
       font-weight: bold;
       text-align: center;
     }
   
     .pop-content {
       min-height: 140rpx;
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
  
  .pay-popup {
    position: fixed;
    left: 0;
    top: 0;
    right: 0;
    bottom: 0;
    z-index: 21;
    overflow: hidden;

    &.show {
      display: block;

      .mask {
        animation: showPopup 0.2s linear both;
      }

      .layer {
        animation: showLayer 0.2s linear both;
      }
    }

    &.hide {
      .mask {
        animation: hidePopup 0.2s linear both;
      }

      .layer {
        animation: hideLayer 0.2s linear both;
      }
    }

    &.none {
      display: none;
    }

    .mask {
      position: fixed;
      top: 0;
      width: 100%;
      height: 100%;
      z-index: 1;
      background-color: rgba(0, 0, 0, 0.65);
    }

    .layer {
      display: flex;
      width: 100%;
      flex-direction: column;
      position: fixed;
      z-index: 99;
      bottom: 0;
      border-radius: 10rpx 10rpx 0 0;
      background-color: #fff;

      .specification-wrapper {
        width: 100%;
        padding: 30rpx 25rpx 10rpx 25rpx;
        box-sizing: border-box;
        background: #ffffff;

        .specification-wrapper-content {
          width: 100%;
          max-height: 900rpx;
          min-height: 300rpx;

          &::-webkit-scrollbar {
            /*隐藏滚轮*/
            display: none;
          }

          .specification-header {
            width: 100%;
            display: flex;
            flex-direction: row;
            position: relative;
            margin-bottom: 40rpx;
            text-align: center;
            .specification-name {
                font-weight: bold;
                width: 100%;
                font-size: 30rpx;
                padding: 10rpx;
            }
          }

          .specification-content {
            text-align: left;
            .pay-item {
                padding: 35rpx 30rpx 30rpx 100rpx;
                cursor: pointer;
                margin-bottom: 8rpx;
                border: solid 3rpx #cccccc;
                border-radius: 10rpx;
                .iconfont {
                    margin-right: 10rpx;
                    color: $fuint-theme
                }
                .item-point {
                    .amount {
                        color: #f9211c;
                    }
                    .modify {
                        margin-left: 30rpx;
                        color: $fuint-theme;
                    }
                }
                .item-amount {
                    font-size: 30rpx;
                    .amount {
                        color: #f9211c;
                        font-size: 35rpx;
                        font-weight: bold;
                    }
                }
            }
          }
        }

        .close {
          position: absolute;
          top: 30rpx;
          right: 25rpx;
          width: 50rpx;
          height: 50rpx;
          text-align: center;
          line-height: 50rpx;

          .close-item {
            width: 40rpx;
            height: 40rpx;
          }
        }
      }

      .btn-wrapper {
        display: flex;
        width: 100%;
        height: 120rpx;
        flex: 0 0 120rpx;
        align-items: center;
        justify-content: space-between;
        padding: 0 26rpx;
        box-sizing: border-box;
        margin-bottom: 60rpx;
        .layer-btn {
          width: 335rpx;
          height: 76rpx;
          border-radius: 38rpx;
          color: #fff;
          line-height: 76rpx;
          text-align: center;
          font-weight: 500;
          font-size: 28rpx;

          &.add-cart {
            background: #ffbe46;
          }

          &.buy {
            background: #fe560a;
          }
        }

        .sure {
          width: 698rpx;
          height: 80rpx;
          border-radius: 40rpx;
          color: #fff;
          line-height: 80rpx;
          text-align: center;
          font-weight: 500;
          font-size: 28rpx;
          background:linear-gradient(to right, #f9211c, #ff6335)
        }

        .sure.add-cart {
          background: #ff9402;
        }
      }
    }

    @keyframes showPopup {
      0% {
        opacity: 0;
      }

      100% {
        opacity: 1;
      }
    }

    @keyframes hidePopup {
      0% {
        opacity: 1;
      }

      100% {
        opacity: 0;
      }
    }

    @keyframes showLayer {
      0% {
        transform: translateY(120%);
      }

      100% {
        transform: translateY(0%);
      }
    }

    @keyframes hideLayer {
      0% {
        transform: translateY(0);
      }

      100% {
        transform: translateY(120%);
      }
    }
  }
</style>
