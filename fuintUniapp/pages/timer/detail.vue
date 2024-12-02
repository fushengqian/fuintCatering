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
                 <view v-if="detail.useRule > 0" class="item">
                   <view class="amount"><text class="num">{{ detail.useRule }}</text>次卡</view>
                 </view>
             </view>
        </view>
        <view class="item">
           <view class="label">有效期至：</view>
           <view>{{ detail.effectiveDate }}</view>
        </view>
        <view class="item">
          <view class="label">适用门店：</view>
          <view>{{ detail.storeNames ? detail.storeNames : '全部'}}</view>
        </view>
    </view>
    <view v-if="detail.qrCode" class="coupon-qr">
      <view>
         <image class="image" :src="detail.qrCode"></image>
      </view>
      <view class="qr-code">
          <p class="code">核销码：{{ detail.code }}</p>
          <p class="tips">请出示以上券码给核销人员</p>
      </view>
    </view>
    <view v-if="dataList.length > 0" class="coupon-timer">
      <view class="tips">核销记录({{detail.confirmCount}}/{{detail.useRule}})</view>
      <uni-row class="time-row" v-for="row in dataList" :key="row.id">
          <uni-col :span="rowCount" v-for="item in row.data" :key="item.id" class="time-item">
              <view v-if="item.isActive == true">
                  <view class="time active"></view>
                  <view class="show-time">{{ item.showTime ? timeStamp(item.showTime) : '' }}</view>
              </view>
              <view v-else class="time"></view>
          </uni-col>
      </uni-row>
    </view>
    <view class="coupon-content m-top20">
        <view class="title">使用须知</view>
        <view class="content"><jyf-parser :html="detail.description ? detail.description : '暂无...'"></jyf-parser></view>
    </view>
    <!-- 快捷导航 -->
    <shortcut />
    
    <!--领取码 start-->
    <view class="receive-pop">
       <uni-popup ref="receiveCodePopup" type="dialog">
          <uni-popup-dialog mode="input" v-model="receiveCode" focus="false" title="领取码" type="info" placeholder="请输入领取码" :before-close="true" @close="cancelReceive" @confirm="doReceive"></uni-popup-dialog>
       </uni-popup>
    </view>
    <!--领取码 end-->
    
    <!-- 底部选项卡 -->
    <view v-if="!detail.code && !detail.isReceive" class="footer-fixed">
      <view class="footer-container">
        <!-- 操作按钮 -->
        <view class="foo-item-btn">
          <view class="btn-wrapper">
            <view class="btn-item btn-item-main" @click="receive(detail.id)">
              <text>领取次卡</text>
            </view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
  import jyfParser from '@/components/jyf-parser/jyf-parser'
  import Shortcut from '@/components/shortcut'
  import * as myCouponApi from '@/api/myCoupon'
  import * as couponApi from '@/api/coupon'

  export default {
    components: {
      Shortcut
    },
    data() {
      return {
        // 当前会员卡券ID
        userCouponId: null,
        // 领取码
        receiveCode: '',
        // 加载中
        isLoading: true,
        // 当前卡券详情
        detail: null,
        rowCount: 0,
        dataList: []
      }
    },

    /**
     * 生命周期函数--监听页面加载
     */
    onLoad(options) {
      this.userCouponId = options.userCouponId ? options.userCouponId : 0;
      this.couponId = options.couponId ?options.couponId : 0;
      this.getCouponDetail();
    },

    methods: {
      // 获取卡券详情
      getCouponDetail() {
        const app = this
        app.isLoading = false
        myCouponApi.detail(app.couponId, app.userCouponId, "")
          .then(result => {
            app.detail = result.data;
            app.detail.confirmCount = app.detail.confirmLogs ? app.detail.confirmLogs.length : 0;
            app.getRowCount(app.detail.useRule);
            app.getDataList(app.detail.useRule, app.detail.confirmLogs);
          })
          .finally(() => app.isLoading = false)
      },
      // 组织数据
      getDataList(num, confirmLogs) {
          const app = this;
          const use = confirmLogs.length;
          // 只有一行的情况
          if (num <= 4 && num > 0) {
              app.dataList = [ { "data": [] } ]
              for (let i = 0; i < num; i++) {
                   app.dataList[0].data.push({ "isActive": (i < use ? true : false), showTime: confirmLogs[i] ? confirmLogs[i].createTime : '' })
              }
          } else {
              let rowCount = Math.ceil(num / 4);
              let c = 1;
              for (let i = 0; i < rowCount; i++) {
                   app.dataList[i] = {"data": []}
                   for (let j = 1; j <= 4; j++) {
                       if (c <= num) {
                           let showTime = '';
                           if (c <= use && confirmLogs[c-1]) {
                               showTime = confirmLogs[c-1].createTime; 
                           }
                           app.dataList[i].data.push({ "isActive": (c <= use ? true : false), showTime: showTime });
                           c++;
                       }
                   }
              }
          }
      },
      // 计算行数
      getRowCount(num) {
         if (num < 4 && num > 0) {
             this.rowCount = 24 / num;
         } else if (num >= 4) {
             this.rowCount = 6;
         }
      },
      // 取消领取码
      cancelReceive() {
          this.receiveCode = '';
          this.$refs.receiveCodePopup.close();
      },
      // 确认领取码
      doReceive(vCode) {
          if (!vCode || vCode.length < 1) {
              this.$error("请先输入领取码！");
              return false;
          }
          this.receiveCode = vCode;
          this.$refs.receiveCodePopup.close();
          this.receive(this.couponId);
      },
      receive(couponId) {
        const app = this;
        if (app.detail.needReceiveCode && !app.receiveCode) {
            app.$refs.receiveCodePopup.open('top');
            return false;
        }
        couponApi.receive({ 'couponId': couponId, 'receiveCode': app.receiveCode })
          .then(result => {
            // 显示提示
            app.receiveCode = '';
            if (parseInt(result.code) === 200) {
                app.detail.isReceive = true;
                app.$success("领取成功！");
            } else {
                app.$error(result.message);
            }
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
          var result = year + "." + month + "." + sdate + " " + hour + ":" + minute;
          // 返回
          return result;
      }
    }
  }
</script>

<style lang="scss" scoped>
  .container {
    min-height: 100vh;
    padding: 20rpx;
    background: #fff;
    color:#666666;
  }
  .base {
        border: dashed 5rpx #cccccc;
        padding: 30rpx;
        border-radius: 10rpx;
        margin: 20rpx;
        height: auto;
        min-height: 365rpx;
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
  .coupon-timer {
      border: dashed 5rpx #cccccc;
      margin-top:20rpx;
      padding: 15px 30rpx 5rpx 30rpx;
      border-radius: 10rpx;
      margin: 20rpx;
      overflow: hidden;
      .tips{
          margin-bottom: 20rpx;
      }
      .time-row {
            margin-bottom: 10rpx;
            height: 100rpx;
            display: flex;
      }
      .time-item {
          padding-top: 10rpx;
          text-align: center;
          align-items: center;
          justify-content: center;
      }
      .time {
          height: 80rpx;
          margin-top: 20rpx;
          margin-bottom: 20rpx;
          text-align: center;
          padding-top: 20rpx;
          border-radius: 40rpx;
          color: #ffffff;
          font-weight: bold;
          background: url('~@/static/confirm/undo.png') no-repeat center center;
          background-size: contain;
      }
      .active {
        background: url('~@/static/confirm/do.png') no-repeat center center;
        background-size: contain;
        border: solid 1px #ffffff;
      }
      .show-time {
          font-size: 22rpx;
          text-align: center;
          margin-bottom: 30rpx;
      }
      min-height: 160rpx;
  }
  .coupon-qr{
      border: dashed 5rpx #cccccc;
      border-radius: 10rpx;
      margin: 20rpx;
      text-align: center;
      padding-top: 30rpx;
      padding-bottom: 30rpx;
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
    margin: 20rpx;
    min-height: 450rpx;
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
    }
  }
</style>
