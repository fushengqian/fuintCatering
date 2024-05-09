<template>
  <view v-if="!isLoading" class="container">
    <!-- 页面头部 -->
    <view class="main-header" :style="{ paddingTop: $platform == 'H5' ? '0' : '50rpx' }">
      <!-- 用户信息 -->
      <view v-if="isLogin" class="user-info">
        <view class="user-content">
          <!-- 会员昵称 -->
          <view class="nick-name">{{ dataInfo.confirmInfo.realName }}</view>
          <!-- 手机号 -->
          <view  class="mobile">{{ dataInfo.confirmInfo.mobile }}</view>
        </view>
        <view class="amount-info">
            <view class="amount-tip">交易金额（元）</view>
            <view v-if="dataInfo.payMoney" class="amount-num">{{ dataInfo.payMoney.toFixed(2) }}</view>
            <view v-if="!dataInfo.payMoney" class="amount-num">0.00</view>
        </view>
      </view>
      <!-- 未登录 -->
      <view v-if="!isLogin" class="user-info" @click="handleLogin">
        <view class="user-content">
          <view class="nick-name">未登录</view>
          <view class="login-tips">点击登录账号</view>
        </view>
      </view>
    </view>
    <view v-if="isLogin" class="user-app">
      <view class="item">
        <view class="tool" @click="scanCodeConfirm">
             <view class="icon">
               <image class="image" src="/static/icon/saoyisao.png" mode="scaleToFill"></image>
           </view>
             <view class="text">核销卡券</view>
        </view>
      </view>
        <view class="item">
            <view class="tool" @click="scanCodeCashier">
                <view class="icon">
                    <image class="image" src="/static/icon/saoma.png" mode="scaleToFill"></image>
                </view>
                <view class="text">扫码收款</view>
            </view>
        </view>
    </view>

    <!-- 概述 -->
    <view class="my-asset">
      <view class="asset-left flex-box dis-flex flex-x-center">
        <view class="asset-left-item" @click="onTargetMember('all')">
          <view class="item-value dis-flex flex-x-center">
            <text>{{ dataInfo.userCount }}</text>
          </view>
          <view class="item-name dis-flex flex-x-center">
            <text>总会员</text>
          </view>
        </view>
        <view class="asset-left-item" @click="onTargetMember('todayActive')">
          <view class="item-value dis-flex flex-x-center">
            <text>{{ dataInfo.todayUser ? dataInfo.todayUser : 0}}</text>
          </view>
          <view class="item-name dis-flex flex-x-center">
            <text>今日活跃</text>
          </view>
        </view>
        <view class="asset-left-item" @click="onTargetOrder('today')">
          <view class="item-value dis-flex flex-x-center">
            <text>{{ dataInfo.orderCount }}</text>
          </view>
          <view class="item-name dis-flex flex-x-center">
            <text>今日订单</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 待办操作 -->
    <view class="order-navbar">
      <view class="order-navbar-item" v-for="(item, index) in orderNavbar" :key="index" @click="onTargetTodo(item)">
        <view class="item-icon">
          <text class="iconfont" :class="[`icon-${item.icon}`]"></text>
        </view>
        <view class="item-name">{{ item.name }}</view>
        <text class="order-badge" v-if="item.count && item.count > 0">{{ item.count }}</text>
      </view>
    </view>

    <!-- 我的服务 -->
    <view class="my-service">
      <view class="service-title">我的管理</view>
      <view class="service-content clearfix">
        <block v-for="(item, index) in service" :key="index">
          <view v-if="item.type == 'link'" class="service-item" @click="handleService(item)">
            <view class="item-icon">
              <text class="iconfont" :class="[`icon-${item.icon}`]"></text>
            </view>
            <view class="item-name">{{ item.name }}</view>
          </view>
          <view v-if="item.type == 'button' && $platform == 'MP-WEIXIN'" class="service-item">
            <button class="btn-normal" :open-type="item.openType">
              <view class="item-icon">
                <text class="iconfont" :class="[`icon-${item.icon}`]"></text>
              </view>
              <view class="item-name">{{ item.name }}</view>
            </button>
          </view>
        </block>
      </view>
    </view>

  </view>
</template>

<script>
  import SettingKeyEnum from '@/common/enum/setting/Key'
  import SettingModel from '@/common/model/Setting'
  import * as MerchantApi from '@/api/merchant'
  import * as OrderApi from '@/api/order'
  import { checkLogin } from '@/utils/app'

  // 订单操作
  const orderNavbar = [
    { id: 'all', name: '全部待办', icon: 'qpdingdan' },
    { id: 'refund', name: '售后处理', icon: 'daifukuan', count: 1 },
    { id: 'confirm', name: '待核销', icon: 'shouhou', count: 1 },
    { id: 'book', name: '预约确认', icon: 'daishouhuo', count: 3 },
  ]

  /**
   * 我的服务
   * id: 标识; name: 标题名称; icon: 图标; type 类型(link和button); url: 跳转的链接
   */
  const service = [
    { id: 'myCoupon', name: '卡券管理', icon: 'youhuiquan', type: 'link', url: 'pages/my-coupon/index' },
    { id: 'coupon', name: '营销活动', icon: 'lingquan', type: 'link', url: 'pages/merchant/activity/index' },
    { id: 'points', name: '订单管理', icon: 'dingdan', type: 'link', url: 'pages/merchant/order/index' },
    { id: 'help', name: '会员管理', icon: 'tuandui', type: 'link', url: 'pages/merchant/member/index' },
    { id: 'address', name: '报表数据', icon: 'zhibozhong', type: 'link', url: 'pages/merchant/data/index' },
    { id: 'refund', name: '售后服务', icon: 'shouhou', type: 'link', url: 'pages/refund/index' },
    { id: 'setting', name: '商家设置', icon: 'shezhi1', type: 'link', url: 'pages/user/setting' },
    { id: 'staff', name: '店铺员工', icon: 'sy-yh', type: 'link', url: 'pages/staff/index' },
  ]

  export default {
    data() {
      return {
        // 枚举类
        SettingKeyEnum,
        // 当前运行的终端 (此处并不冗余,因为微信小程序端view层无法直接读取$platform)
        $platform: this.$platform,
        // 正在加载
        isLoading: true,
        // 是否已登录
        isLogin: false,
        // 系统设置
        setting: {},
        // 当前商户数据
        dataInfo: {},
        // 账户资产
        assets: { prestore: '--', timer: '--', coupon: '--' },
        // 我的服务
        service,
        // 订单操作
        orderNavbar,
        // 当前用户待处理数量
        todoCounts: { refund: 0, book: 0, confirm: 0 }
      }
    },

    /**
     * 生命周期函数--监听页面显示
     */
    onShow(options) {
      // 判断是否已登录
      this.isLogin = checkLogin()
      // 获取页面数据
      this.getPageData()
    },

    methods: {
      // 获取页面数据
      getPageData(callback) {
        const app = this
        app.isLoading = true
        Promise.all([app.getSetting(), app.getUserInfo()])
          .then(result => {
            app.isLoading = false
            // 初始化我的服务数据
            app.initService()
            // 初始化订单操作数据
            app.initOrderTabbar()
            // 执行回调函数
            callback && callback()
          })
          .catch(err => {
            console.log('catch', err)
          })
      },

      // 初始化我的服务数据
      initService() {
        const app = this
        const newService = []
        service.forEach(item => {
          newService.push(item)
        })
        app.service = newService
      },

      // 初始化订单操作数据
      initOrderTabbar() {
        const app = this
        const newOrderNavbar = []
        orderNavbar.forEach(item => {
          if (item.hasOwnProperty('count')) {
              item.count = app.todoCounts[item.id]
          }
          newOrderNavbar.push(item)
        })
        app.orderNavbar = newOrderNavbar
      },

      // 获取设置
      getSetting() {
        const app = this
        app.setting = {}
      },

      // 获取当前用户信息
      getUserInfo() {
        const app = this
        return new Promise((resolve, reject) => {
          !app.isLogin ? resolve(null) : MerchantApi.info()
            .then(result => {
              app.dataInfo = result.data
              resolve(app.dataInfo)
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
      
      // 扫码核销
      scanCodeConfirm() {
        const app = this
        uni.scanCode({
            success:function(res){
                app.$navTo('pages/confirm/doConfirm?code=' + res.result + '&id=0')
            }
        });
      },
      
      // 扫码收款
      scanCodeCashier() {
          const app = this
          uni.scanCode({
              success:function(res){
                  app.$navTo('pages/pay/cashier?code=' + res.result)
              }
          });
      },

      // 跳转到登录页
      handleLogin() {
        !this.isLogin && this.$navTo('pages/login/index')
      },

      // 跳转到订单页
      onTargetOrder(item) {
        this.$navTo('pages/merchant/order/index', { dataType: item.id })
      },

      // 跳转到我的积分页面
      onTargetPoints() {
        this.$navTo('pages/merchant/points/log')
      },
      
      // 跳转会员列表
      onTargetMember(dataType) {
          this.$navTo('pages/merchant/member/index', { dataType: dataType });
      },

      // 跳转到服务页面
      handleService({ url }) {
        this.$navTo(url)
      },
    },

    /**
     * 下拉刷新
     */
    onPullDownRefresh() {
      // 获取首页数据
      this.getPageData(() => {
        uni.stopPullDownRefresh()
      })
    }
  }
</script>

<style lang="scss" scoped>
  // 页面头部
  .main-header {
    position: relative;
    height: 240rpx;
    background-size: 100% 100%;
    overflow: hidden;
    display: flex;
    align-items: center;
    padding-left: 30rpx;
    background: $fuint-theme;
    margin: 0 20rpx;
    border-top-left-radius: 8rpx;
    border-top-right-radius: 8rpx;

    .user-info {
      display: flex;
      height: 100rpx;
      margin-top: 20rpx;
      z-index: 1;
      text-align: center;
      padding-left: 70rpx;
      width: 100%;
      .user-content {
        display: block;
        flex-direction: column;
        justify-content: center;
        margin-left: 30rpx;
        text-align: left;
        color: #ffffff;
        
        .nick-name {
          font-size: 28rpx;
          font-weight: bold;
        }

        .mobile {
          margin-top: 15rpx;
          font-size: 26rpx;
        }

        .user-grade {
          display: flex;
          align-items: center;
          background: #3c3c3c;
          margin-top: 12rpx;
          border-radius: 10rpx;
          padding: 4rpx 12rpx;

          .user-grade_icon .image {
            display: block;
            width: 32rpx;
            height: 32rpx;
          }

          .user-grade_name {
            margin-left: 5rpx;
            font-size: 24rpx;
            color: #EEE0C3;
          }
        }

        .login-tips {
          margin-top: 12rpx;
          font-size: 28rpx;
        }
      }
      .amount-info {
          margin-top: 1rpx;
          margin-left: 220rpx;
          color: #fff;
          display: block;
          text-align: left;
          .amount-tip {
              font-size: 28rpx;
          }
          .amount-num {
              margin-top: 10rpx;
              font-weight: bold;
              font-size: 35rpx;
          }
      }
    }
  }
  .user-app {
    display: flex;
    height: 260rpx;
    text-align: center;
    color: #fff;
    background: $fuint-theme;
    margin: 0 20rpx;
    border-bottom-right-radius: 8rpx;
    border-bottom-left-radius: 8rpx;
    padding-bottom: 80rpx;
    .item {
        width: 50%;
        height: 100%;
        margin: 20rpx;
        padding: 20rpx;
        text-align: right;
        .tool {
            width: 160rpx;
            clear: both;
            padding: 10rpx;
            border-bottom: solid 3px #ffffff;
            border-radius: 10rpx;
            text-align: center;
            margin:0 auto;
            .icon {
                .image {
                    height: 60rpx;
                    width: 60rpx;
                }
            }
            .text {
                margin-top: 10rpx;
                font-size: 30rpx;
                text-align: center;
            }
        }
    }
  }

  // 我的资产
  .my-asset {
    display: flex;
    padding: 40rpx 0;
    box-shadow: 0 1rpx 5rpx 0px rgba(0, 0, 0, 0.05);
    border-radius: 5rpx;
    margin: 25rpx 20rpx 5rpx 20rpx;
    background: #ffffff;
    .asset-right {
      width: 200rpx;
      border-left: 1rpx solid #eee;
    }

    .asset-right-item {
      text-align: center;
      color: #545454;

      .item-icon {
        font-size: 60rpx;
      }

      .item-name {
        margin-top: 10rpx;
      }

      .item-name text {
        font-size: 20rpx;
      }

    }

    .asset-left-item {
      text-align: center;
      color: #666;
      padding: 0 72rpx;

      .item-value {
        font-size: 36rpx;
        color: #f03c3c;
        font-weight: bold;
      }

      .item-name {
        margin-top: 6rpx;
      }

      .item-name {
        font-size: 24rpx;
      }
    }

  }

  // 订单操作
  .order-navbar {
    display: flex;
    margin: 20rpx auto 20rpx auto;
    padding: 20rpx 0;
    width: 94%;
    box-shadow: 0 1rpx 5rpx 0px rgba(0, 0, 0, 0.05);
    font-size: 30rpx;
    border-radius: 5rpx;
    background: #fff;

    &-item {
      position: relative;
      width: 25%;

      .item-icon {
        text-align: center;
        margin: 0 auto;
        padding: 10rpx 0;
        color: #545454;
        font-size: 40rpx;
      }

      .item-name {
        font-size: 24rpx;
        color: #545454;
        text-align: center;
        margin-right: 10rpx;
      }

      .order-badge {
        position: absolute;
        top: 0;
        right: 55rpx;
        font-size: 22rpx;
        background: #fa2209;
        text-align: center;
        line-height: 28rpx;
        color: #fff;
        border-radius: 100%;
        min-height: 30rpx;
        min-width: 30rpx;
        padding: 1rpx;
      }
    }
  }

  // 我的服务
  .my-service {
    margin: 22rpx auto 22rpx auto;
    padding: 20rpx 0;
    width: 94%;
    box-shadow: 0 1rpx 5rpx 0px rgba(0, 0, 0, 0.05);
    border-radius: 5rpx;
    background: #fff;

    .service-title {
      padding-left: 20rpx;
      margin-bottom: 30rpx;
      font-size: 28rpx;
    }

    .service-content {

      // margin-bottom: -30rpx;
      .service-item {
        position: relative;
        width: 25%;
        float: left;
        margin-bottom: 30rpx;

        .item-icon {
          text-align: center;
          margin: 0 auto;
          padding: 10rpx 0;
          color: #ff3800;
          font-size: 40rpx;
        }

        .item-name {
          font-size: 24rpx;
          color: #545454;
          text-align: center;
          margin-right: 10rpx;
        }

      }
    }
  }
</style>
