<template>
  <view class="container">
    <view class="qrcode-container">
      <view class="header">
        <view class="title">请扫码支付</view>
        <view class="amount">￥{{ amount }}</view>
        <view class="point" v-if="point > 0">使用{{ point }}积分</view>
      </view>
      
      <view class="qrcode-wrapper">
        <view class="loading" v-if="isLoading">
          <u-loading size="60" mode="circle"></u-loading>
          <text class="loading-text">正在生成支付二维码...</text>
        </view>
        <view class="qrcode" v-else>
          <image v-if="qrCodeImage" :src="qrCodeImage" class="qrcode-image"></image>
          <view v-else class="error-message">
            <text>二维码生成失败，请点击重试</text>
            <view class="retry-btn" @click="generateQRCode">重新生成</view>
          </view>
        </view>
      </view>
      
      <view class="payment-info">
        <view class="channel-info">
          <image class="channel-icon" :src="channelIcon"></image>
          <text class="channel-name">{{ channelName }}</text>
        </view>
        <view class="timer" v-if="countdown > 0">
          <text>支付倒计时：</text>
          <text class="time">{{ formatTime(countdown) }}</text>
        </view>
        <view class="expired" v-else>
          <text>二维码已过期，请点击重新生成</text>
          <view class="retry-btn" @click="generateQRCode">重新生成</view>
        </view>
      </view>
      
      <view class="actions">
        <view class="cancel-btn" @click="cancelPayment">取消支付</view>
        <view class="check-btn" @click="checkPaymentStatus">检查支付状态</view>
      </view>
    </view>
  </view>
</template>

<script>
import * as YeepayApi from '@/api/yeepay'
import QRCode from '@/utils/qrcode.js'

export default {
  data() {
    return {
      orderId: '',
      amount: '0.00',
      point: 0,
      channel: 'WECHAT',
      isLoading: true,
      qrCodeImage: '',
      prePayTn: '',
      uniqueOrderNo: '',
      countdown: 300, // 5分钟倒计时
      timer: null,
      statusTimer: null,
      payStatus: 'PENDING'
    }
  },
  
  computed: {
    channelName() {
      return this.channel === 'WECHAT' ? '微信支付' : '支付宝支付'
    },
    channelIcon() {
      return this.channel === 'WECHAT' ? '/static/channel/wechat.png' : '/static/channel/alipay.png'
    }
  },
  
  onLoad(options) {
    this.orderId = options.orderId || ''
    this.amount = options.amount || '0.00'
    this.point = parseInt(options.point || 0)
    this.channel = options.channel || 'WECHAT'
    
    // 生成二维码
    this.generateQRCode()
    
    // 开始倒计时
    this.startCountdown()
    
    // 开始定时查询支付状态
    this.startStatusCheck()
  },
  
  onUnload() {
    // 页面卸载时清除定时器
    this.clearTimers()
  },
  
  methods: {
    // 生成二维码
    generateQRCode() {
      const app = this
      app.isLoading = true
      app.qrCodeImage = ''
      
      // 重置倒计时
      app.countdown = 300
      
      // 调用易宝支付接口
      YeepayApi.initiatePayment(app.orderId, 'USER_SCAN', app.channel)
        .then(result => {
          if (result.code === '200' && result.data) {
            app.prePayTn = result.data.prePayTn
            app.uniqueOrderNo = result.data.uniqueOrderNo
            
            // 使用QRCode生成二维码
            QRCode.make({
              canvasId: 'qrcode',
              componentInstance: this,
              text: app.prePayTn,
              width: 200,
              height: 200,
              background: '#ffffff',
              foreground: '#000000',
              success: res => {
                app.qrCodeImage = res
                app.isLoading = false
              },
              fail: err => {
                console.error('二维码生成失败:', err)
                app.isLoading = false
                app.$error('二维码生成失败，请重试')
              }
            })
          } else {
            app.isLoading = false
            app.$error(result.message || '支付请求失败')
          }
        })
        .catch(err => {
          console.error('支付请求异常:', err)
          app.isLoading = false
          app.$error('支付请求异常，请重试')
        })
    },
    
    // 开始倒计时
    startCountdown() {
      this.clearTimers()
      this.timer = setInterval(() => {
        if (this.countdown > 0) {
          this.countdown--
        } else {
          clearInterval(this.timer)
        }
      }, 1000)
    },
    
    // 开始定时查询支付状态
    startStatusCheck() {
      this.statusTimer = setInterval(() => {
        this.checkPaymentStatus(false)
      }, 5000) // 每5秒查询一次
    },
    
    // 清除定时器
    clearTimers() {
      if (this.timer) {
        clearInterval(this.timer)
        this.timer = null
      }
      if (this.statusTimer) {
        clearInterval(this.statusTimer)
        this.statusTimer = null
      }
    },
    
    // 检查支付状态
    checkPaymentStatus(showLoading = true) {
      const app = this
      
      if (showLoading) {
        uni.showLoading({
          title: '查询支付状态...'
        })
      }
      
      YeepayApi.queryOrder(app.orderId)
        .then(result => {
          if (showLoading) {
            uni.hideLoading()
          }
          
          if (result.code === '200' && result.data) {
            app.payStatus = result.data.status
            
            // 如果支付成功，跳转到支付结果页
            if (app.payStatus === 'SUCCESS') {
              app.clearTimers()
              app.$navTo('pages/pay/result', { 
                amount: app.amount,
                point: app.point
              })
            } else if (app.payStatus === 'FAILED') {
              app.clearTimers()
              app.$error('支付失败，请重新支付')
            }
          }
        })
        .catch(err => {
          if (showLoading) {
            uni.hideLoading()
          }
          console.error('查询支付状态异常:', err)
          if (showLoading) {
            app.$error('查询支付状态异常')
          }
        })
    },
    
    // 取消支付
    cancelPayment() {
      this.clearTimers()
      uni.navigateBack()
    },
    
    // 格式化时间
    formatTime(seconds) {
      const minutes = Math.floor(seconds / 60)
      const remainingSeconds = seconds % 60
      return `${minutes}:${remainingSeconds < 10 ? '0' : ''}${remainingSeconds}`
    }
  }
}
</script>

<style lang="scss" scoped>
.container {
  background-color: #f5f5f5;
  min-height: 100vh;
  padding: 30rpx;
}

.qrcode-container {
  background-color: #ffffff;
  border-radius: 20rpx;
  padding: 40rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.1);
}

.header {
  text-align: center;
  margin-bottom: 40rpx;
  
  .title {
    font-size: 32rpx;
    color: #333;
    margin-bottom: 20rpx;
  }
  
  .amount {
    font-size: 60rpx;
    font-weight: bold;
    color: #000;
    margin-bottom: 10rpx;
  }
  
  .point {
    font-size: 26rpx;
    color: #666;
  }
}

.qrcode-wrapper {
  width: 300rpx;
  height: 300rpx;
  margin: 0 auto 40rpx;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f9f9f9;
  border-radius: 10rpx;
  
  .loading {
    display: flex;
    flex-direction: column;
    align-items: center;
    
    .loading-text {
      font-size: 24rpx;
      color: #999;
      margin-top: 20rpx;
    }
  }
  
  .qrcode {
    width: 100%;
    height: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
    
    .qrcode-image {
      width: 260rpx;
      height: 260rpx;
    }
    
    .error-message {
      text-align: center;
      font-size: 24rpx;
      color: #ff6335;
    }
  }
}

.payment-info {
  margin-bottom: 60rpx;
  
  .channel-info {
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 30rpx;
    
    .channel-icon {
      width: 40rpx;
      height: 40rpx;
      margin-right: 10rpx;
    }
    
    .channel-name {
      font-size: 28rpx;
      color: #333;
    }
  }
  
  .timer {
    text-align: center;
    font-size: 28rpx;
    color: #666;
    
    .time {
      color: $fuint-theme;
      font-weight: bold;
    }
  }
  
  .expired {
    text-align: center;
    font-size: 28rpx;
    color: #ff6335;
  }
}

.retry-btn {
  margin-top: 20rpx;
  padding: 10rpx 30rpx;
  background-color: $fuint-theme;
  color: #fff;
  font-size: 24rpx;
  border-radius: 30rpx;
  display: inline-block;
}

.actions {
  display: flex;
  justify-content: space-between;
  
  .cancel-btn, .check-btn {
    flex: 1;
    height: 80rpx;
    line-height: 80rpx;
    text-align: center;
    border-radius: 40rpx;
    font-size: 28rpx;
  }
  
  .cancel-btn {
    background-color: #f5f5f5;
    color: #666;
    margin-right: 20rpx;
  }
  
  .check-btn {
    background-color: $fuint-theme;
    color: #fff;
  }
}
</style>