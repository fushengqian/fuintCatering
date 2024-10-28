<template>
  <view v-if="!isLoading" class="container b-f p-b">
    <view class="base">
        <view class="title"> {{ detail.bookName }} </view>
        <view class="item">
          <view class="label">姓名：</view>
          <view class="value">{{ detail.contact ? detail.contact : '' }}</view>
        </view>
        <view class="item">
          <view class="label">日期：</view>
          <view class="value">{{ detail.serviceDate }}</view>
        </view>
        <view class="item">
          <view class="label">时间：</view>
          <view class="value">{{ detail.serviceTime }}</view>
        </view>
        <view class="item">
          <view class="label">门店：</view>
          <view class="value">{{ detail.storeInfo ? detail.storeInfo.name : '-'}}</view>
        </view>
    </view>
    <view class="book-qr" v-if="detail.code">
      <view>
         <image class="image" :src="detail.qrCode"></image>
      </view>
      <view class="qr-code">
          <p class="code">预约码：{{ detail.code }}</p>
          <p class="tips">请出示以上券码给核销人员</p>
      </view>
    </view>
    <view class="book-content m-top20">
        <view class="title">备注</view>
        <view class="content"><jyf-parser :html="detail.remark ? detail.remark : '暂无...'"></jyf-parser></view>
    </view>
    
    <!-- 底部选项卡 -->
    <view class="footer-fixed" v-if="detail.status == 'A'">
      <view class="footer-container">
        <view class="foo-item-btn">
          <view class="btn-wrapper">
            <view class="btn-item btn-item-main" @click="onCancel(detail.id)">
              <text>取消预约</text>
            </view>
          </view>
        </view>
      </view>
    </view>
    
  </view>
</template>

<script>
  import jyfParser from '@/components/jyf-parser/jyf-parser'
  import * as BookApi from '@/api/book'

  export default {
    data() {
      return {
        // 预约ID
        myBookId: 0,
        // 加载中
        isLoading: true,
        // 当前卡券详情
        detail: null,
        qrCode: '',
      }
    },

    /**
     * 生命周期函数--监听页面加载
     */
    onLoad(options) {
      this.myBookId = options.myBookId ? options.myBookId : 0;
      this.getBookDetail();
    },

    methods: {
      // 获取预约详情
      getBookDetail() {
        const app = this
        BookApi.myBookDetail(app.myBookId)
          .then(result => {
            app.detail = result.data ? result.data.bookInfo : null;
          })
          .finally(() => app.isLoading = false)
      },
      // 取消预约
      onCancel() {
        const app = this
        uni.showModal({
          title: '友情提示',
          content: '确认取消预约吗？',
          success(o) {
            if (o.confirm) {
              BookApi.cancel(app.myBookId)
                .then(result => {
                  // 显示成功信息
                  app.$success(result.message)
                  // 刷新当前订单数据
                  app.getBookDetail()
                })
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
    color:#666666;
  }
  .base {
        border: dashed 5rpx #cccccc;
        padding: 30rpx;
        border-radius: 10rpx;
        margin: 20rpx;
        display: block;
        height: auto;
        min-height: 220rpx;
        .title {
            font-size: 36rpx;
            font-weight: bold;
            margin-bottom: 30rpx;
        }
        .book-main {
            .image {
                width: 200rpx;
                height: 158rpx;
                border-radius: 8rpx;
                border: #cccccc solid 1rpx;
            }
            width: 100%;
        }
        .item {
             margin-bottom: 20rpx;
             font-size: 30rpx;
             color: #666666;
             clear: both;
             .label {
                 float: left;
                 font-weight: bold;
             }
             .value {
                 font-weight: normal;
             }
        }
  }
  .book-qr {
      border: dashed 5rpx #cccccc;
      border-radius: 10rpx;
      margin: 20rpx;
      text-align: center;
      padding-top: 30rpx;
      padding-bottom: 30rpx;
      .image {
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
  .book-content {
    padding: 30rpx;
    border: dashed 5rpx #cccccc;
    border-radius: 5rpx;
    margin: 20rpx;
    min-height: 400rpx;
    .title {
        margin-bottom: 15rpx;
        font-weight: bold;
    }
    .content {
        color: #666666;
        font-size: 24rpx;
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
    z-index: 11;
    box-shadow: 0 -4rpx 40rpx 0 rgba(144, 52, 52, 0.1);
    background: #fff;
  }
  
  .footer-container {
    width: 100%;
    display: flex;
    margin-bottom: 40rpx;
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
      border-radius: 40rpx;
    }
    // 立即领取按钮
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
