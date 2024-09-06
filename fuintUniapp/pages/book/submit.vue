<template>
  <view v-if="!isLoading" class="container">

    <!-- 预约时间 -->
    <view class="row-service b-f m-top20">
      <view class="row-title">预约时间</view>
      <view class="service-switch dis-flex">
        <view class="switch-item active">{{ bookData.date }} {{ bookData.week }} {{ bookData.time }}
        </view>
      </view>
    </view>
    
    <!-- 称呼 -->
    <view class="row-input b-f m-top20 dis-flex">
      <view class="row-title">联系人名：</view>
      <view class="mo ney col-m">
          <input class="weui-input value" type="text" v-model="form.contact" placeholder="请输入联系人姓名"/>
      </view>
    </view>
    
    <!-- 电话 -->
    <view class="row-input b-f m-top20 dis-flex">
      <view class="row-title">联系电话：</view>
      <view class="money col-m">
          <input class="weui-input value" type="text" v-model="form.mobile" placeholder="请输入联系人电话"/>
      </view>
    </view>

    <!-- 备注信息 -->
    <view class="row-textarea b-f m-top20">
      <view class="row-title">备注信息：</view>
      <view class="content">
        <textarea class="textarea" v-model="form.remark" maxlength="2000" placeholder="请填写备注信息"
          placeholderStyle="color:#ccc"></textarea>
      </view>
    </view>

    <!-- 底部操作按钮 -->
    <view class="footer-fixed">
      <view class="btn-wrapper">
        <view class="btn-item btn-item-main" :class="{ disabled }" @click="handleSubmit()">确认提交</view>
      </view>
    </view>

  </view>
</template>

<script>
  import * as BookApi from '@/api/book'
  export default {
    data() {
      return {
        // 正在加载
        isLoading: true,
        // 预约项目id
        bookId: null,
        // 预约详情
        bookInfo: {},
        // 按钮禁用
        disabled: false,
        // 表单数据
        form: { contact: '', mobile: '', remark: '' },
        bookData: null
      }
    },

    /**
     * 生命周期函数--监听页面加载
     */
    onLoad({ bookId }) {
      this.bookId = bookId
      // 获取预约项目详情
      this.getBookDetail()
    },
    onShow() {
       this.bookData = uni.getStorageSync('bookData');
    },

    methods: {

      // 获取预约详情
      getBookDetail() {
        const app = this
        app.isLoading = true
        BookApi.detail(app.bookId)
          .then(result => {
            app.bookInfo = result.bookInfo;
            app.isLoading = false;
          })
      },

      // 提交预约信息
      handleSubmit() {
        const app = this;
        if (app.form.mobile.length < 6 || app.form.contact.length < 1) {
            app.$toast("请先提交联系人和联系电话！");
            return false;
        }
        if (app.disabled === true) return false;
        const param = { bookId: app.bookData.bookId,
                        remark: app.form.remark,
                        mobile: app.form.mobile,
                        contact: app.form.contact,
                        date: app.bookData.date,
                        time: app.bookData.time };
        BookApi.submit(param)
           .then(result => {
                if (result.code == '200') {
                    app.$toast('提交预约成功，请等待确认！')
                    setTimeout(() => {
                      app.disabled = false;
                      uni.navigateBack();
                    }, 3000)
                } else {
                    if (result.message) {
                        app.$error(result.message);
                    } else {
                        app.$error('预约提交失败');
                    }
                    app.disabled = false;
                    return false;
                }
            }).catch(err => app.disabled = false)
      }
    }
  }
</script>

<style lang="scss" scoped>
  .container {
    padding: 20rpx 20rpx 100rpx 20rpx;
  }

  .row-title {
    color: #333;
    font-weight: bold;
    line-height: 60rpx;
  }

  /* 服务类型 */
  .row-service {
    padding: 24rpx 20rpx;
    border-radius: 20rpx;
  }

  .service-switch {
    .switch-item {
      padding: 6rpx 30rpx;
      margin-right: 25rpx;
      border-radius: 10rpx;
      color: #f9211c;
      border: 1px solid #f9211c;
    }
  }

  /* 备注信息 */
  .row-textarea {
    margin: 20rpx auto;
    border-radius: 20rpx;
    border: solid 1rpx #f5f5f5;
    height: 100%;
    display: block;
    padding: 10rpx 10rpx 80rpx 10rpx;
    .textarea {
      min-height: 260rpx;
      width: 100%;
      padding: 12rpx;
      border: 1rpx solid #ccc;
      border-radius: 12rpx;
      box-sizing: border-box;
      font-size: 26rpx;
      margin: 0 auto;
    }
  }

  /* 表单项 */
  .row-input {
    padding: 24rpx 20rpx;
    border-radius: 20rpx;
    border: solid 1rpx #f5f5f5;
    .row-title {
      margin-bottom: 0;
      margin-right: 20rpx;
    }
    .value {
        color: #333;
        padding-top: 10rpx;
    }
  }

  // 底部操作栏
  .footer-fixed {
    position: fixed;
    bottom: var(--window-bottom);
    left: 0;
    right: 0;
    height: 180rpx;
    padding-bottom: 30rpx;
    z-index: 11;
    box-shadow: 0 -4rpx 40rpx 0 rgba(144, 52, 52, 0.1);
    background: #fff;

    .btn-wrapper {
      height: 100%;
      display: flex;
      align-items: center;
      padding: 0 20rpx;
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