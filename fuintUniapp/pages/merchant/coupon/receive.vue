<template>
  <view class="container">
    <view class="search-wrapper">
      <view class="search-input">
        <view class="search-input-wrapper">
          <view class="right">
            <input v-model="keyword" class="input" placeholder="请输入卡券名称或ID..." @change="doSearch" type="text"></input>
          </view>
          <view class="search u-icon-wrap">
             <view class="icon" @click="doSearch">
               <u-icon name="search"></u-icon>
             </view>
          </view>
        </view>
      </view>
    </view>
    <view class="number-wrapper">
        <view class="title">发放数量</view>
        <u-number-box :min="1" :value="num"/>
    </view>
    <view class="main-form">
      <view class="footer">
        <view class="btn-wrapper">
          <view class="btn-item btn-item-main" :class="{ disabled }" @click="doSubmit()">确定发放</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
  import * as couponApi from '@/api/merchant/coupon'
  export default {
    data() {
      return {
        keyword: '',
        num: 1,
        memberId: '',
        couponInfo: null,
        // 按钮禁用
        disabled: false
      }
    },
    
    /**
     * 生命周期函数--监听页面加载
     */
    onLoad(options) {
      // 会员ID
      this.memberId = options.memberId ? options.memberId : 0;
    },

    methods: {
      
      /**
       * 搜索卡券
       */
      doSearch() {
          const app = this
          if (!this.keyword) {
              app.$error("请输入卡券名称或ID");
              return false;
          }
          couponApi.search({ 'keyword': this.keyword })
            .then(result => {
                app.disabled = false;
                if (result.data.coupon.content && result.data.coupon.content.length == 1) {
                    app.couponInfo = result.data.coupon.content[0];
                    app.keyword = app.couponInfo.name;
                } else {
                    app.$error("未查询到卡券信息");
                }
            })
      },
      
      /**
       * 提交发放
       */
      doSubmit() {
        const app = this;
        if (!app.couponInfo || !app.couponInfo.id) {
            app.$error("请先查询卡券");
            return false;
        }
        let isConfirm = false;
        uni.showModal({
          title: "提示",
          content: "您确定要发放卡券吗?",
          success({ confirm }) {
             if (confirm) {
                 app.disabled = true;
                 couponApi.sendCoupon({ 'couponId': app.couponInfo.id, 'num': app.num, 'userId': app.memberId })
                   .then(result => {
                         app.code = '';
                         app.disabled = false;
                         // 显示提示
                         if (parseInt(result.code) === 200) {
                             app.$success("发放成功！");
                         } else {
                             app.$error(result.message);
                         }
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
    padding: 20rpx;
    min-height: 100vh;
    background: #f7f7f7;
  }

  .search-wrapper {
    display: flex;
    height: 100rpx;
    margin-top: 40rpx;
    padding: 0 5rpx;
  }
  
  .number-wrapper {
      margin-top: 60rpx;
      text-align: right;
      background: #fff;
      padding: 30rpx;
      border-radius: 10rpx;
      border: solid 1px #ccc;
      .title {
          margin-bottom: 20rpx;
      }
  }

  // 搜索输入框
  .search-input {
    width: 100%;
    background: #fff;
    border-radius: 60rpx;
    box-sizing: border-box;
    overflow: hidden;
    border: solid 1px #ccc;
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
      
      .search {
        display: flex;
        width: 80rpx;
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
