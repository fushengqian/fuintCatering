<template>
  <view v-if="!isLoading" class="container b-f p-b">
    <view class="base">
        <view class="coupon-main">
            <view class="left">
               <image class="image" :src="detail.image"></image>
            </view>
            <view class="right">
                <view class="item">
                  <view class="name">{{ detail.name ? detail.name : '' }}</view>
                </view>
                <view v-if="detail.type == 'P'" class="item">
                  <view class="amount">￥<text class="num">{{ detail.balance }}</text></view>
                </view>
            </view>
        </view>
        <view v-if="detail.amount > 0" class="item">
          <view class="label">卡券面额：</view>
          <view class="amount">￥{{ detail.amount }}</view>
        </view>
        <view class="item">
           <view class="label">有效期至：</view>
           <view>{{ detail.effectiveDate }}</view>
        </view>
        <view v-if="detail.code && detail.status == 'A' && detail.isGive" class="gift" @click="give()"><text>转赠好友</text></view>
    </view>
    <view class="coupon-qr">
      <view>
         <image class="image" :src="detail.qrCode"></image>
      </view>
      <view class="qr-code">
          <p class="code">卡号：{{ detail.code }}</p>
          <p class="tips">请出示以上卡号给核销人员</p>
      </view>
    </view>
    <view class="coupon-content m-top20">
        <view class="title">使用须知</view>
        <view class="content"><jyf-parser :html="detail.description ? detail.description : '暂无...'"></jyf-parser></view>
    </view>
    
    <!-- 底部选项卡 -->
    <view class="footer-fixed">
      <view class="footer-container">
        <view class="foo-item-btn">
          <view class="btn-wrapper">
            <view v-if="detail.status != 'D'" class="btn-item btn-item-main" @click="remove(userCouponId)">
              <text>删除卡券</text>
            </view>
            <view v-if="detail.status == 'D'" class="btn-item btn-item-main state">
              <text>已删除</text>
            </view>
          </view>
        </view>
      </view>
    </view>
    
    <!-- 快捷导航 -->
    <shortcut />
    <view class="give-popup">
       <uni-popup ref="givePopup" type="dialog">
          <uni-popup-dialog mode="input" title="转赠给好友" type="info" placeholder="输入好友手机号码" :before-close="true" @close="cancelGive" @confirm="doGive"></uni-popup-dialog>
       </uni-popup>
    </view>
  </view>
</template>

<script>
  import jyfParser from '@/components/jyf-parser/jyf-parser'
  import Shortcut from '@/components/shortcut'
  import * as myCouponApi from '@/api/myCoupon'
  import * as giveApi from '@/api/give'

  export default {
    components: {
      Shortcut
    },
    data() {
      return {
        // 卡券ID
        couponId: null,
        // 加载中
        isLoading: true,
        // 卡券详情
        detail: null,
        qrCode: ""
      }
    },

    /**
     * 生命周期函数--监听页面加载
     */
    onLoad(options) {
      // 记录ID
      this.userCouponId = options.userCouponId
      // 获取卡券详情
      this.getCouponDetail()
    },

    methods: {
      // 获取卡券详情
      getCouponDetail() {
        const app = this
        myCouponApi.detail(0, app.userCouponId, "")
          .then(result => {
            app.detail = result.data
          })
          .finally(() => app.isLoading = false)
      },
      // 转赠
      give() {
         this.$refs.givePopup.open('top')
      },
      // 取消转赠
      cancelGive() {
         this.$refs.givePopup.close()
      },
      // 确定转赠
      doGive(friendMobile) {
              const app = this
              if (friendMobile.length < 11) {
                 app.$error("请先输入好友手机号码！")
                 return false
              } else {
                  app.$refs.givePopup.close()
                  const param = {'mobile': friendMobile,
                                 'couponId': this.userCouponId,
                                 'message': '转赠一张优惠券给你'}
                  giveApi.doGive(param)
                    .then(result => {
                        if (result.code == '200') {
                            uni.showModal({
                              title: '提示信息',
                              content: '恭喜，转增成功！',
                              showCancel: false,
                              success(o) {
                                if (o.confirm) {
                                   uni.navigateBack()
                                }
                              }
                            })
                        } else {
                          app.$error(result.message)
                        }
                  })
              }
      },
      // 删除卡券
      remove() {
         const app = this;
         if (app.isLoading == true) {
             return false;
         }
         uni.showModal({
           title: "提示",
           content: "您确定要删除吗?",
           success({ confirm }) {
             if (confirm) {
                 app.isLoading = true;
                 myCouponApi.remove(app.userCouponId)
                   .then(result => {
                      app.getCouponDetail();
                      app.isLoading = false;
                   })
                   .finally(() => app.isLoading = false)
             }
           }
         });
      }
    }
  }
</script>

<style lang="scss" scoped>
  .container {
    min-height: 100vh;
    padding: 20rpx;
    background: #fff;
  }
  .base {
        border: dashed 5rpx #cccccc;
        padding: 15rpx 0rpx 15rpx 15rpx;
        border-radius: 10rpx;
        margin: 20rpx;
        height: auto;
        min-height: 420rpx;
        .coupon-main {
            clear: both;
            min-height: 164rpx;
            border: #ccc dashed 2rpx;
            border-radius: 5rpx;
            margin-bottom: 20rpx;
            .left {
                width: 215rpx;
                float: left;
                .image {
                    width: 210rpx;
                    height: 160rpx;
                    border-radius: 8rpx;
                    border-right: #cccccc dashed 2rpx;
                }
            }
            .right {
                width: 380rpx;
                float: left;
                overflow: hidden;
                .name {
                    font-size: 38rpx;
                    margin-left: 6rpx;
                }
                .num {
                    font-size: 58rpx;
                    color: red;
                }
            }
        }
        .item {
             clear: both;
             margin-bottom: 10rpx;
             font-size: 30rpx;
             color: #666666;
             .label {
                 float: left;
             }
             .amount {
                 font-weight: bold;
             }
             .name {
                 font-weight: bold;
             }
        }
  }
  .coupon-qr {
      border: dashed 5rpx #cccccc;
      border-radius: 10rpx;
      margin: 20rpx;
      text-align: center;
      padding: 30rpx 15rpx 30rpx 15rpx;
      .image{
          width: 360rpx;
          height: 360rpx;
          margin: 0 auto;
      }
      .qr-code{
          .code{
              font-weight: bold;
              font-size: 30rpx;
              line-height: 50rpx;
          }
          .tips{
              font-size: 25rpx;
              color:#C0C4CC;
          }
      }
  }

  .coupon-content {
    font-size: 28rpx;
    padding: 30rpx;
    border: dashed 5rpx #cccccc;
    border-radius: 5rpx;
    margin: 20rpx 20rpx 200rpx 20rpx;
    min-height: 400rpx;
    .title {
        margin-bottom: 15rpx;
    }
  }
  .gift {
    height: 46rpx;
    width: 120rpx;
    margin-top: 20rpx;
    line-height: 46rpx;
    text-align: center;
    border: 1px solid #f8df00;
    border-radius: 6rpx;
    color: #f86d48;
    background: #f8df98;
    font-size: 22rpx;
    float: right;
    &.state {
      border: none;
        color: #cccccc;
        background: #F5F5F5;
    }
  }
  
  /* 底部操作栏 */
  .footer-fixed {
    position: fixed;
    bottom: var(--window-bottom);
    left: 0;
    right: 0;
    display: flex;
    height: 180rpx;
    padding-bottom: 30rpx;
    z-index: 11;
    box-shadow: 0 -4rpx 40rpx 0 rgba(144, 52, 52, 0.1);
    background: #fff;
  }
  
  .footer-container {
    width: 100%;
    display: flex;
  }
  
  // 操作按钮
  .foo-item-btn {
    flex: 1;
    .btn-wrapper {
      height: 100%;
      display: flex;
      align-items: center;
    }
    .btn-item {
      flex: 1;
      font-size: 28rpx;
      height: 80rpx;
      line-height: 80rpx;
      margin-right: 16rpx;
      margin-left: 16rpx;
      text-align: center;
      color: #fff;
      border-radius: 80rpx;
    }
    // 领取按钮
    .btn-item-main {
      background: linear-gradient(to right, #f9211c, #ff6335);
      &.state {
        border: none;
          color: #cccccc;
          background: #F5F5F5;
      }
    }
  }
</style>
