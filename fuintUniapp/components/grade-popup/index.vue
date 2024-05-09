<template>
  <view class="grade-popup popup" catchtouchmove="true" :class="(value && complete) ? 'show' : 'none'"
    @touchmove.stop.prevent="moveHandle">
    
    <view class="mask" @click="close('mask')"></view>
    <!-- 要购买的等级信息确认 start -->
    <view v-if="!isShowPay" class="layer attr-content" :style="'border-radius: 10rpx 10rpx 0 0;'">
      <view class="specification-wrapper">
        <scroll-view class="specification-wrapper-content" scroll-y="true">
          <view class="specification-header">
            <view class="specification-name"><text class="price" v-if="memberGrade.catchValue > 0">￥{{ memberGrade.catchValue }}</text>购买{{ memberGrade.name }}</view>
          </view>
          <view class="specification-content">
            <view class="grade-item" v-if="memberGrade.discount > 0">
                <view class="item-rule"><view class="title">买单折扣：</view>{{ memberGrade.discount }}折</view>
            </view>
            <view class="grade-item" v-if="memberGrade.speedPoint > 0">
                <view class="item-rule"><view class="title">积分加速：</view>{{ memberGrade.speedPoint }}倍</view>
            </view>
            <view class="grade-item">
                <view class="item-rule">
                    <view class="title">有效期限：</view>
                    <text v-if="memberGrade.validDay > 0">{{ memberGrade.validDay }}天</text>
                    <text v-else>永久</text>
                </view>
            </view>
            <view class="grade-description">
                <view class="item-rule">
                    <view class="title">权益说明：</view>
                    <view>{{ memberGrade.userPrivilege ? memberGrade.userPrivilege : '暂无'}}</view>
                </view>
            </view>
          </view>
        </scroll-view>
        <view class="close" @click="close('close')" v-if="showClose">
          <image class="close-item" :src="closeImage"></image>
        </view>
      </view>
      <view class="btn-wrapper">
        <view class="sure" @click="buyNow">立即购买</view>
      </view>
      <!-- 页面结束 -->
    </view>
    <!-- 要购买的等级信息确认 end -->
    
    <!-- 支付信息确认 start -->
    <view class="confirm" v-if="isShowPay">
        <view class="layer attr-content" :style="'border-radius: 10rpx 10rpx 0 0;'">
          <view class="specification-wrapper">
            <scroll-view class="specification-wrapper-content" scroll-y="true">
              <view class="specification-header">
                <view class="specification-name">支付确认</view>
              </view>
              <view class="specification-content">
                <view v-if="couponInfo && couponInfo.amount" class="pay-item">
                    <view class="item-point">
                        <view class="title" @click="doUseCoupon">
                           <text v-if="useCoupon" class="iconfont is-use icon-success"></text>
                           <text v-if="!useCoupon" class="iconfont icon-success"></text>
                           <text class="point-amount">使用卡券抵扣</text>
                           <text class="amount">￥{{ couponInfo.amount }}</text>
                        </view>
                    </view>
                </view>
                <view class="pay-item">
                    <view class="item-amount">
                        <view class="title">
                            实付金额：<text class="amount">￥{{ ((parseFloat(memberGrade.catchValue) - parseFloat(useCouponInfo.amount)) >= 0 ? (parseFloat(memberGrade.catchValue) - parseFloat(useCouponInfo.amount)) : 0.0.toFixed(2)) }}</text>
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
            <view class="sure" @click="doBuy">确认支付</view>
          </view>
          <!-- 页面结束 -->
        </view>
    </view>
    <!-- 支付信息确认 end -->
    
  </view>
</template>

<script>
  import * as SettlementApi from '@/api/settlement'
  import PayTypeEnum from '@/common/enum/order/PayType'
  import { wxPayment } from '@/utils/app'
  
  var that; // 当前页面对象
  var vk; // 自定义函数集
  export default {
    name: 'GradePopup',
    props: {
      // true 组件显示 false 组件隐藏
      value: {
        Type: Boolean,
        default: false
      },
      // vk云函数路由模式参数开始-----------------------------------------------------------
      // 等级信息
      memberGrade: {
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
        isShowPay: false, // true 显示 false 隐藏
        useCoupon: true,  // 是否使用卡券
        useCouponInfo: { amount: 0, id: '' }, // 使用的卡券
        couponInfo: null // 可用卡券
      };
    },
    mounted() {
      that = this;
    },
    methods: {
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
            that.isShowPay = false;
            that.useCoupon = true;
        } else if (s == "mask") {
          if (that.maskCloseAble) {
              that.$emit("input", false);
              that.$emit("close", "mask");
          }
        }
      },
      moveHandle() {
        //禁止父元素滑动
      },
      // 立即购买
      buyNow() {
         this.prePay();
      },
      // 是否使用卡券
      doUseCoupon() {
        if (this.useCoupon) {
            this.useCoupon = false;
            this.useCouponInfo = { amount: 0, id: '' };
        } else {
            this.useCoupon = true;
            this.useCouponInfo = this.couponInfo;
        }
      },
      // 确认购买
      doBuy() {
        const app = this
        let couponId = "";
        if (app.useCoupon) {
            couponId = app.couponInfo ? app.couponInfo.userCouponId : 0;
        }
        // 请求api
        SettlementApi.submit(app.memberGrade.id, "", "member", "", "", 0, couponId, "", 0, 0, 0, "", "JSAPI")
          .then(result => app.onSubmitCallback(result))
          .catch(err => {
            if (err.result) {
              const errData = err.result.data
              if (errData) {
                  return false
              }
            }
        })
      },
      // 支付前查询
      prePay() {
          const app = this
          // 请求api
          SettlementApi.prePay({ type: 'memberGrade' })
            .then(result => {
                if (result.data) {
                  if (result.data.canUseCouponInfo) {
                      app.couponInfo = result.data.canUseCouponInfo;
                      if (app.useCoupon) {
                          app.useCouponInfo = app.couponInfo;
                      }
                  }
                  app.isShowPay = true;
                }
            })
            .catch(err => {
              if (err.result) {
                  const errData = err.result.data;
                  if (errData) {
                      return false;
                  }
              }
          })  
      },
      // 订单提交成功后回调
      onSubmitCallback(result) {
        const app = this
        // 微信支付
        if (result.data.payType == PayTypeEnum.WECHAT.value) {
           wxPayment(result.data.payment)
            .then(() => {
                      uni.showModal({
                        title: '支付结果',
                        content: '支付成功',
                        showCancel: false,
                        success(o) {
                          if (o.confirm) {
                              app.$router.go(0);
                              app.$emit('onPaySuccess');
                          }
                        }
                      })
                  })
            .catch(err => app.$error('支付失败'))
            .finally(() => {
              //empty
            })
        }
          
        // 余额支付
        if (result.data.payType == PayTypeEnum.BALANCE.value) {
            app.$error('支付成功');
            app.$emit('onPaySuccess');
        }
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
  .grade-popup {
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
                .price {
                    color: #f03c3c;
                }
                font-weight: bold;
                width: 100%;
                font-size: 30rpx;
                padding: 10rpx;
            }
          }

          .specification-content {
            font-weight: 500;
            font-size: 26rpx;
            .grade-item {
                .title {
                   font-weight: bold;
                   display: flex;
                   float: left;
                }
                display: flex;
                height: 100rpx;
                padding-top:30rpx;
                cursor:pointer;
                .item-rule {
                    padding: 20rpx;
                    border-bottom: solid 1px #cccccc;
                    width: 100%;
                    text-align: left;
                }
            }
            .grade-description {
                margin-top: 20rpx;
                padding: 20rpx;
                min-height: 100rpx;
                .title {
                   font-weight: bold;
                }
            }
            .pay-item {
              padding: 30rpx;
              font-size: 30rpx;
              background: #fff;
              border: 1rpx solid $fuint-theme;
              border-radius: 8rpx;
              color: #888;
              margin-bottom: 12rpx;
              text-align: center;
              .amount {
                  color: #f03c3c;
                  font-weight: bold;
              }
              .iconfont {
                  margin-right: 10rpx;
              }
              .is-use {
                color: $fuint-theme
              }               
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
        margin-bottom: 120rpx;
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
