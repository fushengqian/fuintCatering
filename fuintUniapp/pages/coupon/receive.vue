<template>
  <view class="container">
    <view class="search-wrapper">
      <view class="search-input">
        <view class="search-input-wrapper">
          <view class="right">
            <input v-model="code" class="input" placeholder="请输入卡券核销码" type="text"></input>
          </view>
          <view class="scan u-icon-wrap">
             <view class="icon" @click="doScan">
               <u-icon name="scan"></u-icon>
             </view>
          </view>
        </view>
      </view>
    </view>
    <view class="main-form">
      <view class="footer">
        <view class="btn-wrapper">
          <view class="btn-item btn-item-main" :class="{ disabled }" @click="doSubmit()">确定兑换</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
  import * as couponApi from '@/api/coupon'
  export default {
    data() {
      return {
        code: '',
        // 按钮禁用
        disabled: false
      }
    },

    /**
     * 生命周期函数--监听页面加载
     */
    onLoad(options) {
       this.code = options.code ? options.code : '';
    },

    methods: {
      
      /**
       * 扫码
       */
      doScan() {
          const app = this;
          uni.scanCode({
              success: function(res) {
                app.code = res.result;
              }
          });
      },
      
      /**
       * 提交兑换
       */
      doSubmit() {
        const app = this;
        app.disabled = true;
        couponApi.receive({ 'couponId': 0, 'receiveCode': app.code })
          .then(result => {
                app.code = '';
                app.disabled = false;
                // 显示提示
                if (parseInt(result.code) === 200) {
                    app.$success("兑换成功！");
                } else {
                    app.$error(result.message);
                }
          })
      }
    }
  }
</script>

<style lang="scss" scoped>
  .container {
    padding: 20rpx;
    min-height: 100vh;
    background: #f7f7f7;
  }

  .search-wrapper {
    display: flex;
    height: 100rpx;
    margin-top: 80rpx;
    padding: 0 5rpx;
  }

  // 搜索输入框
  .search-input {
    width: 100%;
    background: #fff;
    border-radius: 10rpx 0 0 10rpx;
    box-sizing: border-box;
    overflow: hidden;
    .search-input-wrapper {
      display: flex;
      .right {
        flex: 1;
        input {
          font-size: 30rpx;
          height: 100rpx;
          line-height: 100rpx;
          padding-left: 30rpx;
          .input-placeholder {
            color: #aba9a9;
          }
        }
      }
      
      .scan {
        display: flex;
        width: 60rpx;
        justify-content: center;
        align-items: center;
        .icon {
          display: block;
          color: #b4b4b4;
          font-size: 48rpx;
        }
      
      }
    }
  }
  /* 底部操作栏 */
  .footer {
    margin-top: 100rpx;
    .btn-wrapper {
      height: 100%;
      display: flex;
      align-items: center;
      padding: 0 5rpx;
    }
    .btn-item {
      flex: 1;
      font-size: 28rpx;
      height: 80rpx;
      line-height: 80rpx;
      text-align: center;
      color: #fff;
      border-radius: 40rpx;
    }
  
    .btn-item-main {
      background: linear-gradient(to right, #f9211c, #ff6335);
      // 禁用按钮
      &.disabled {
        background: #ff9779;
      }
    }
  }
</style>
