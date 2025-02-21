<template>
  <view v-if="!isLoading" class="container">
    <!-- 页面头部 -->
    <view class="main-header">
      <!-- 用户信息 -->
      <view class="user-info">
        <!--头像-->
        <view class="user-avatar" @click="onUserInfo">
          <image class="image" :src="userInfo.avatar ? userInfo.avatar : '/static/default-avatar.png'"></image>
        </view>
        <view class="user-content" @click="onUserInfo">
          <!-- 会员昵称 -->
          <view class="nick-name">{{ userInfo.name ? userInfo.name : '未登录'}}</view>
          <view class="login-tips" v-if="!isLogin">(点击头像登录)</view>
          <!-- 会员等级 -->
          <view v-if="userInfo.gradeId > 0 && gradeInfo" class="user-grade">
            <view class="user-grade_icon">
              <image class="image" src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACAAAAAgCAMAAABEpIrGAAAA0lBMVEUAAAD/tjL/tzH/uDP/uC7/tjH/tzH/tzL/tTH+tTL+tjP/tDD/tTD+tzD/tjL/szD/uDH/tjL/tjL+tjD/tjT/szb/tzL/tTL+uTH+tjL/tjL/tjL/tTT/tjL/tjL+tjH/uTL/vDD/tjL/tjH/tzL9uS//tTL/nBr/sS7/tjH/ujL/szD/uTv+rzf/tzL+tzH+vDP+uzL+tjP+ry7+tDL9ki/7szf/sEX/tTL/tjL+tjL/tTH/tTT/tzH/tzL/tjP/sTX/uTP/wzX+rTn/vDX9vC8m8ckhAAAAOXRSTlMAlnAMB/vjxKWGMh0S6drMiVxPRkEY9PLy0ru0sKagmo5+dGtgVCMgBP716eXWyMGxqJGRe2o5KSmFNjaYAAABP0lEQVQ4y8XS13KDMBAF0AWDDe4t7r3ETu9lVxJgJ/n/X8rKAzHG5TE+Twz3zki7I/g/KXdghIbGJewrU4yzn08Ebgl6TuZzzuOC6W5es3HX6qsSz3NFShRU0MpucytDmOSpu3yULx3CA9RD1HjVedc0jSjqm6ZzhUjDsFDQhSp/OKj5GQvg0+ZCOixsbtDLAeTTOm/yGi8GyIphIVsgH737FEDV44LJa88IRKK/SetrwT9G/GUIr6vXjoy4GXn7+RboVXnghuSjaoGecwQxL2su3CwAKlO+QFoqxI4FMctHQhQd2OhxTu184jWUlI+rMTBTn1/IQcJHQ6GQdZ7pWiDaNdhTt330efISeiqYwQEzQpTlsURJLhzkEmpCPsERfeIUVyXr6MNuIyp5uziW6xURtt7hhGwzmMNJExfO4Bd9X0ZPqAxdNwAAAABJRU5ErkJggg=="></image>
            </view>
            <view class="user-grade_name">
              <text>{{ gradeInfo.name }}</text>
            </view>
          </view>
          <!-- 会员无等级时显示手机号 -->
          <view v-else class="mobile">{{ userInfo.mobile }}</view>
          <view class="active-time" v-if="userInfo.endTime">{{ userInfo.endTime }}</view>
        </view>
        <view class="amount-info" @click="toMemberWallet(userInfo.id ? userInfo.id : 0)">
            <view class="amount-tip">余额（元）</view>
            <view class="amount-num" v-if="isLogin">{{ userInfo.balance.toFixed(2) }}</view>
            <view class="amount-num" v-if="!isLogin">0.00</view>
            <view class="point-amount" @click="onTargetPoints">积分 {{ userInfo.point ? userInfo.point : 0 }}</view>
        </view>
      </view>
      <view class="user-no">
        <view class="no" v-if="userInfo.userNo">会员号：{{ userInfo.userNo ? userInfo.userNo : '-'}}</view>
      </view>
    </view>

    <!-- 会员资产 -->
    <view class="my-asset">
      <view class="asset-left flex-box dis-flex flex-x-center">
        <view class="asset-left-item" @click="onTargetMyCoupon('C')">
          <view class="item-value dis-flex flex-x-center">
            <text>{{ isLogin ? assets.coupon : '0' }}</text>
          </view>
          <view class="item-name dis-flex flex-x-center">
            <text>优惠券</text>
          </view>
        </view>
        <view class="asset-left-item" @click="onTargetMyCoupon('P')">
          <view class="item-value dis-flex flex-x-center">
            <text>{{ isLogin ? assets.prestore : '0' }}</text>
          </view>
          <view class="item-name dis-flex flex-x-center">
            <text>储值卡</text>
          </view>
        </view>
        <view class="asset-left-item" @click="onTargetMyCoupon('T')">
          <view class="item-value dis-flex flex-x-center">
            <text>{{ isLogin ? assets.timer : '0' }}</text>
          </view>
          <view class="item-name dis-flex flex-x-center">
            <text>计次卡</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 会员服务 -->
    <view class="user-service">
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
  import * as MemberApi from '@/api/merchant/member'
  import * as UserApi from '@/api/user'
  import * as OrderApi from '@/api/order'
  import * as MessageApi from '@/api/message'
  import { checkLogin, showMessage } from '@/utils/app'

  /**
   * 会员服务
   * id: 标识; name: 标题名称; icon: 图标; type 类型(link和button); url: 跳转的链接
   */
  const service = [
    { id: 'rechange', name: '会员充值', icon: 'qiandai', type: 'link', url: 'pages/merchant/balance/recharge' },
    { id: 'payment', name: '余额扣减', icon: 'shouhou', type: 'link', url: 'pages/pay/cashier' },
    { id: 'myCoupon', name: '卡券发放', icon: 'youhuiquan', type: 'link', url: 'pages/merchant/coupon/receive' },
    { id: 'points', name: '会员积分', icon: 'jifen', type: 'link', url: 'pages/merchant/points/detail' },
    { id: 'setting', name: '会员信息', icon: 'shezhi1', type: 'link', url: 'pages/merchant/member/setting' },
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
        // 当前用户信息
        userInfo: { id: 0, name: '', avatar: '', gradeId: 0, mobile: '', balance: 0 },
        gradeInfo: {},
        gradeEndTime: '',
        // 账户资产
        assets: { prestore: '0', timer: '0', coupon: '0' },
        // 会员服务
        service,
        // 会员ID
        memberId: 0,
        // 会员编码
        userCode: ''
      }
    },

    /**
     * 生命周期函数--监听页面加载
     */
    onLoad(options) {
      // 会员ID
      this.memberId = options.memberId ? options.memberId : 0;
      // 获取页面数据
      this.getPageData();
    },

    methods: {
      // 获取页面数据
      getPageData(callback) {
        const app = this
        app.isLoading = true
        Promise.all([app.getSetting(), app.getUserInfo(), app.getUserAssets()])
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

      // 获取设置
      getSetting() {
        this.setting = {}
      },

      // 获取当前用户信息
      getUserInfo() {
        const app = this;
        app.showPopup = false;
        return new Promise((resolve, reject) => {
            MemberApi.detail(app.memberId)
            .then(result => {
              if (result.data.userInfo) {
                  app.userInfo = result.data.userInfo
                  app.userCode = app.userInfo.userNo
                  app.isLogin = true
              } else {
                  app.isLogin = false
                  app.userInfo = { id: 0, name: '', avatar: '', gradeId: 0, mobile: '', balance: 0 }
              }
              app.gradeInfo = result.data.gradeInfo;
              resolve(app.userInfo);
              resolve(app.gradeInfo);
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

      // 获取账户资产
      getUserAssets() {
        const app = this
        return new Promise((resolve, reject) => {
            UserApi.assets({ userId: app.memberId })
            .then(result => {
              app.assets = result.data.asset
              resolve(app.assets)
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
      
      // 跳转我的余额
      toMemberWallet(userId) {
          return false;
      },
      
      // 跳转充值
      toRecharge(userId) {
          return false;
      },

      // 跳转到我的积分页面
      onTargetPoints() {
         return false;
      },

      // 跳转到我的卡券列表页
      onTargetMyCoupon(type) {
         this.$navTo('pages/my-coupon/index?type='+type+'&memberId='+this.memberId)
      },
      
      // 跳转会员设置页面
      onUserInfo() {
          return false;
      },

      // 跳转到服务页面
      handleService({ url }) {
          const app = this;
          console.log(app.userCode)
          app.$navTo(url + '?memberId=' + app.memberId + '&code=' + app.userCode);
      }
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
    background: $fuint-theme;
    height: 380rpx;
    background-size: cover;
    overflow: hidden;
    display: block;
    align-items: center;
    margin: 10rpx 25rpx 10rpx 25rpx;
    border-radius: 10rpx;
    
    .user-info {
      display: block;
      height: 200rpx;
      margin: 20rpx;
      margin-left: 20rpx;
      .user-avatar {
          padding-top: 10rpx;
          width: 50rpx;
          margin-top: 70rpx;
          float: left;
          .image {
              display: block;
              width: 100rpx;
              height: 100rpx;
              border-radius: 999rpx;
          }
      }
      
      .user-content {
        display: block;
        justify-content: center;
        margin-top: 80rpx;
        margin-left: 60rpx;
        float: left;
        color: #ffffff;
        max-width: 300rpx;
        .nick-name {
            font-size: 32rpx;
            font-weight: bold;
            max-width: 270rpx;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
        }

        .mobile {
          margin-top: 15rpx;
          font-size: 26rpx;
        }

        .user-grade {
          display: block;
          align-items: center;
          background: #3c3c3c;
          margin-top: 8rpx;
          border-radius: 10rpx;
          padding: 5rpx 12rpx;
          width: 80%;
          min-width: 160rpx;
          height: 40rpx;

          .user-grade_icon .image {
            display: block;
            width: 32rpx;
            height: 32rpx;
            float: left;
          }

          .user-grade_name {
            margin-left: 5rpx;
            font-size: 24rpx;
            color: #EEE0C3;
            float: left;
          }
        }
        .active-time {
            margin-top: 3rpx;
        }

        .login-tips {
          margin-top: 9rpx;
          font-size: 25rpx;
        }
      }
      .amount-info {
          margin-top: 80rpx;
          margin-left: 70rpx;
          color: #fff;
          display: block;
          float: left;
          max-width: 120rpx;
          .amount-tip {
              font-size: 24rpx;
          }
          .amount-num {
              margin-top: 10rpx;
              font-weight: bold;
              font-size: 48rpx;
          }
          .point-amount {
              display: block;
              margin-top: 2px;
              width: 100px;
          }
      }
    }
    .user-no {
        display: block;
        font-size: 28rpx;
        margin: 110rpx 0rpx 0rpx 20rpx;
        color: #ffffff;
        .no {
            float: left;
        }
    }
  }

  // 我的资产
  .my-asset {
    display: flex;
    background: #fff;
    margin: 10rpx 20rpx 10rpx 20rpx;
    padding: 40rpx 0;
    border: 2rpx #f5f5f5 solid;

    .asset-right {
      width: 200rpx;
      border-left: 1rpx solid #eee;
    }
    .asset-left-item {
      text-align: center;
      color: #666;
      padding: 0 72rpx;
      width: 33%;

      .item-value {
        font-size: 35rpx;
        color: #f03c3c;
        font-weight: bold;
      }
      
      .item-name {
        font-size: 25rpx;
        margin-top: 6rpx;
      }
    }

  }

  // 会员服务
  .user-service {
    margin: 0rpx auto 20rpx auto;
    border: 2rpx #f5f5f5 solid;
    background: #FFF;
    padding: 30rpx 0rpx;
    width: 94%;
    box-shadow: 0 1rpx 5rpx 0px rgba(0, 0, 0, 0.05);
    border-radius: 5rpx;
    display: block;

    .service-content {
       margin-top: 20rpx;
      .service-item {
        width: 25%;
        float: left;
        margin-bottom: 25rpx;

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
  
  // 推荐信息
  .my-recommend {
      height: 20rpx;
  }
  
  // 会员升级
  .member-update {
      margin: 22rpx auto 0rpx auto;
      padding: 20rpx 0;
      border-radius: 5rpx;
      box-shadow: 0 1rpx 5rpx 0px rgba(0, 0, 0, 0.05);
      background: #fff;
      width: 94%;
      text-align: center;
      .update-title {
        padding-left: 20rpx;
        margin-bottom: 30rpx;
        font-size: 28rpx;
        text-align: left;
      }
      .recharge {
            position: relative;
            margin-bottom: 35rpx;
            display: flex;
            flex-direction: row;
            align-items: center;
            
            &-tag {
                position: absolute;
                top: -2rpx;
                left: -2rpx;
                width: 170rpx;
                height: 36rpx;
                display: flex;
                flex-direction: row;
                align-items: center;
                justify-content: center;
                background-image: url('~@/static/user/tag.png');
                background-size: 100%;
                &-text {
                    font-size: 20rpx;
                    color: #FFFFFF;
                    text-align: center;
                }
            }
            
            &-item {
                position: relative;
                padding: 40rpx 0;
                margin-left: 15rpx;
                width: 29.33%;
                height: 270rpx;
                flex-shrink: 0;
                display: flex;
                flex-direction: column;
                align-items: center;
                border: solid 1rpx #CBCCCE;
                border-radius: 12rpx;
                
                &-active {
                    border: solid 2rpx #EDD2A9;
                    background-color: #FBF1E5;
                }
                
                &-duration {
                    margin-bottom: 30rpx;
                    font-size: 26rpx;
                    color: #1C1C1C;
                }
                
                &-price {
                    margin-bottom: 20rpx;
                    display: flex;
                    flex-direction: row;
                    align-items: baseline;
                    
                    &-text {
                        font-size: 48rpx;
                        color: #E3BE83;
                    }
                }
                
                &-des {
                    font-size: 22rpx;
                    color: #A5A3A2;
                }
            }
        }
    }
</style>
