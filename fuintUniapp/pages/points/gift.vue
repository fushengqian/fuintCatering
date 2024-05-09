<template>
  <view class="container">
    <!-- 标题 -->
    <view class="page-title">把积分转赠给好友</view>
    <!-- 表单组件 -->
    <view class="form-wrapper">
      <u-form :model="form" ref="uForm" label-width="160rpx">
        <u-form-item label="手机号码" prop="mobile">
          <u-input v-model="form.mobile" placeholder="请输入好友手机号" />
        </u-form-item>
        <u-form-item label="转赠数量" prop="amount">
          <u-input v-model="form.amount" placeholder="请输入转赠数量" />
        </u-form-item>
        <view class="my-amount">可用积分：
             <text class="amount">{{ userInfo.point ? userInfo.point : 0 }}</text>
             <text class="all" @click="useAllPoint()">全部</text>
        </view>
        <u-form-item label="转赠留言" prop="remark" :border-bottom="false">
          <u-input v-model="form.remark" placeholder="请输入转赠留言" />
        </u-form-item>
      </u-form>
    </view>
    <!-- 操作按钮 -->
    <view class="footer">
      <view class="btn-wrapper">
        <view class="btn-item btn-item-main" :class="{ disabled }" @click="handleSubmit()">确定转赠</view>
      </view>
    </view>
  </view>
</template>

<script>
  import { isMobile } from '@/utils/verify'
  import * as PointApi from '@/api/points/log'
  import * as UserApi from '@/api/user'

  // 表单验证规则
  const rules = {
    amount: [{
      required: true,
      message: '请输入转赠数量',
      trigger: ['blur', 'change']
    }],
    mobile: [{
      required: true,
      message: '请输入好友手机号',
      trigger: ['blur', 'change']
    }, {
      // 自定义验证函数
      validator: (rule, value, callback) => {
        // 返回true表示校验通过，返回false表示不通过
        return isMobile(value)
      },
      message: '好友手机号码不正确',
      // 触发器可以同时用blur和change
      trigger: ['blur'],
    }]
  }

  export default {
    components: {},
    data() {
      return {
        userInfo: { point: 0 },
        form: { mobile: "", amount: "", remark: "" },
        rules,
        // 按钮禁用
        disabled: false
      }
    },

    /**
     * 生命周期函数--监听页面加载
     */
    onShow(options) {
        this.getUserInfo()
    },

    // 必须要在onReady生命周期，因为onLoad生命周期组件可能尚未创建完毕
    onReady() {
      this.$refs.uForm.setRules(this.rules)
    },

    methods: {

      // 表单提交
      handleSubmit() {
        const app = this
        if (app.disabled) {
          return false
        }
        app.$refs.uForm.validate(valid => {
          if (valid) {
            app.disabled = true
            PointApi.gift({ amount: this.form.amount, mobile: this.form.mobile, remark: this.form.remark })
              .then(result => {
                  if (result.data) {
                     app.form.mobile = ""
                     app.form.amount = ""
                     app.form.remark = ""
                     uni.navigateBack() 
                  } else {
                      app.$toast(result.message)
                  }
              })
              .finally(() => app.disabled = false)
          }
        })
      },
      // 获取当前用户信息
      getUserInfo() {
        const app = this
        return new Promise((resolve, reject) => {
            UserApi.info()
            .then(result => {
                  app.userInfo = result.data.userInfo
                  resolve(app.userInfo)
            })
            .catch(err => {
              if (err.result && err.result.status == 1001) {
                  app.isLogin = false
                  resolve(null)
              } else {
                  reject(err)
              }
            })
        })
      },
      useAllPoint() {
          const amount = this.userInfo.point ? this.userInfo.point : 0
          this.form.amount = amount+""
      }
    }
  }
</script>

<style>
  page {
    background: #f7f8fa;
  }
</style>
<style lang="scss" scoped>
  .page-title {
    width: 94%;
    margin: 0 auto;
    padding-top: 40rpx;
    font-size: 28rpx;
    color: rgba(69, 90, 100, 0.6);
  }

  .form-wrapper {
    margin: 20rpx auto 20rpx auto;
    padding: 0 40rpx;
    width: 94%;
    box-shadow: 0 1rpx 5rpx 0px rgba(0, 0, 0, 0.05);
    border-radius: 16rpx;
    background: #fff;
    .my-amount {
        height: 60rpx;
        margin-left: 160rpx;
        margin-top: 10rpx;
        color: #888888;
        .amount{
            color: #f9211c;
        }
        .all {
            margin-left: 30rpx;
            color: #888888;
            background: #f5f5f5;
            padding: 5rpx 10rpx 5rpx 10rpx;
            border-radius: 10rpx;
        }
    }
  }

  /* 底部操作栏 */

  .footer {
    margin-top: 60rpx;

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
