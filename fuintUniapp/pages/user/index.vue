<template>
  <view v-if="!isLoading" class="container" :style="themeVars">
    <!-- 页面头部 -->
    <view v-if="compVisible('userInfoCard')" class="main-header" :style="[userInfoBgStyle, { order: compOrder('userInfoCard') }]">
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
          <view class="active-time" v-if="gradeEndTime">{{ gradeEndTime }}</view>
        </view>
        <view class="pay-qr" @click="toMemberCode(userInfo.id ? userInfo.id : 0)">
            <view class="qrcode iconfont icon-qr-extract"></view>
        </view>
      </view>
      <view class="user-no">
        <view class="no" v-if="userInfo.userNo">会员号：{{ userInfo.userNo ? userInfo.userNo : '-'}}</view>
        <view class="recharge" @click="toRecharge(userInfo.id ? userInfo.id : 0)">储值有礼</view>
      </view>
    </view>

    <!-- 我的资产 -->
    <view class="asset-card" v-if="compVisible('userAssets')" :style="{ order: compOrder('userAssets') }">
      <block v-for="(item, index) in userAssetsItems" :key="index">
        <view class="asset-card-item" @click="onUserAsset(item)">
          <view class="asset-card-icon">
            <text class="iconfont" :class="iconClass(item.icon)" :style="{ color: 'var(--theme-primary)' }"></text>
          </view>
          <view class="asset-card-value">{{ assetValue(item) }}</view>
          <view class="asset-card-label">{{ item.name }}</view>
        </view>
        <view v-if="index < userAssetsItems.length - 1" class="asset-card-divider"></view>
      </block>
    </view>

    <!--会员升级 start-->
    <view class="member-update" v-if="compVisible('vipUpgrade') && vipItems.length > 0" :style="{ order: compOrder('vipUpgrade') }">
        <view class="update-title">
            <text>{{ vipTitle }}</text>
        </view>
        <scroll-view scroll-x>
            <view class="recharge">
                <view class="recharge-item" :class="current == index ? 'recharge-item-active': ''" v-for="(item, index) in vipItems" :key="index" :style="{marginLeft: !index ? '30rpx': ''}" @click="onShowPopup(index)">
                    <view class="recharge-tag">
                        <text class="recharge-tag-text" v-if="parseInt(item.days) > 0">{{ item.days }}天有效期</text>
                        <text class="recharge-tag-text" v-else>永久有效期</text>
                    </view>
                    <text class="recharge-item-duration">{{ item.name }}</text>
                    <view class="recharge-item-price">
                        <text class="rmb">￥</text>
                        <text class="recharge-item-price-text">{{ item.price }}</text>
                    </view>
                    <text class="recharge-item-des" v-for="(line, li) in descLines(item)" :key="li">{{ line }}</text>
                </view>
            </view>
        </scroll-view>
    </view>
    <!-- 弹窗 -->
    <Popup v-if="!isLoading" v-model="showPopup" @onPaySuccess="getPageData" :memberGrade="curGrade"/>
    <!--会员升级 end-->

    <!-- 订单操作 -->
    <view class="order-navbar" v-if="compVisible('orderEntry')" :style="{ order: compOrder('orderEntry') }">
      <view class="order-navbar-item" v-for="(item, index) in orderItems" :key="index" @click="onTargetOrder(item)">
        <view class="item-icon">
          <text class="iconfont" :class="iconClass(item.icon)"></text>
        </view>
        <view class="item-name">{{ item.name }}</view>
        <text class="order-badge" v-if="item.count > 0">{{ item.count }}</text>
      </view>
    </view>

    <!-- 卡券统计 -->
    <view class="my-asset" v-if="compVisible('couponStats')" :style="{ order: compOrder('couponStats') }">
      <view class="asset-left flex-box dis-flex flex-x-center">
        <view class="asset-left-item" v-for="(item, index) in couponStatsItems" :key="index" @click="onCouponStats(item)">
          <view class="item-value dis-flex flex-x-center">
            <text :style="{ color: item.color || '#f03c3c' }">{{ couponStatsValue(item) }}</text>
          </view>
          <view class="item-name dis-flex flex-x-center">
            <text>{{ item.name }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 我的服务 -->
    <view class="my-service" v-if="compVisible('serviceGrid')" :style="{ order: compOrder('serviceGrid') }">
      <view class="service-title">{{ serviceTitle }}</view>
      <view class="service-content clearfix" :class="'service-col-' + serviceColumns">
        <block v-for="(item, index) in service" :key="index">
          <view v-if="item.type == 'link'" class="service-item" @click="handleService(item)">
            <view class="item-icon">
              <text class="iconfont" :class="iconClass(item.icon)"></text>
            </view>
            <view class="item-name">{{ item.name }}</view>
          </view>
          <view v-if="item.type == 'button' && $platform == 'MP-WEIXIN'" class="service-item">
            <button class="btn-normal" :open-type="item.openType">
              <view class="item-icon">
                <text class="iconfont" :class="iconClass(item.icon)"></text>
              </view>
              <view class="item-name">{{ item.name }}</view>
            </button>
          </view>
        </block>
        <block>
          <view v-if="isMerchant == true" class="service-item" @click="handleService({'url': 'merchantPages/index'})">
            <view class="item-icon">
              <text class="iconfont icon-dianpu"></text>
            </view>
            <view class="item-name">商户管理</view>
          </view>
          <view v-else class="service-item" @click="handleBeMerchant()">
              <view class="item-icon">
                <text class="iconfont icon-dianpu"></text>
              </view>
              <view class="item-name">商户管理</view>
          </view>
        </block>
      </view>
    </view>

    <view class="my-recommend" style="order: 100;"></view>

    <!-- 自定义 tabBar 占位 -->
    <view class="tabbar-safe-area" style="order: 101;"></view>
    <!-- #ifdef H5 -->
    <h5-tabbar ref="h5Tabbar"></h5-tabbar>
    <!-- #endif -->
  </view>
</template>

<script>
  import SettingKeyEnum from '@/common/enum/setting/Key'
  import SettingModel from '@/common/model/Setting'
  import * as UserApi from '@/api/user'
  import * as OrderApi from '@/api/order'
  import * as MessageApi from '@/api/message'
  import { checkLogin, showMessage } from '@/utils/app'
  import Popup from './components/Popup'
  import * as SettingApi from '@/api/setting'
  import * as ServiceApi from '@/api/service'
  import * as UserPageApi from '@/api/userPage'
  import { isMobile } from '@/utils/verify'
  import { loadAndApplyTabbar } from '@/utils/tabbar'
  // #ifdef H5
  import H5Tabbar from '@/components/tabbar/index.vue'
  // #endif

  // 订单操作
  const orderNavbar = [
    { id: 'all', name: '全部订单', icon: 'qpdingdan' },
    { id: 'toPay', name: '待支付', icon: 'daifukuan', count: 0 },
    { id: 'paid', name: '已支付', icon: 'daishouhuo', count: 0 }
  ]

  /**
   * 我的服务（本地默认，后台配置后将覆盖）
   * id: 标识; name: 标题名称; icon: 图标; type 类型(link和button); url: 跳转的链接
   */
  const defaultService = [
    { id: 'myCoupon', name: '卡券兑换', icon: 'youhuiquan', type: 'link', url: 'subPages/coupon/receive' },
    { id: 'coupon', name: '转赠记录', icon: 'lingquan', type: 'link', url: 'pages/give/index' },
    { id: 'points', name: '我的积分', icon: 'jifen', type: 'link', url: 'pages/points/detail' },
    { id: 'book', name: '我的预约', icon: 'tuxingyanzhengma', type: 'link', url: 'subPages/book/my' },
    { id: 'help', name: '我的帮助', icon: 'bangzhu', type: 'link', url: 'pages/help/index' },
    { id: 'contact', name: '在线客服', icon: 'kefu', type: 'button', openType: 'contact' },
    { id: 'address', name: '收货地址', icon: 'shouhuodizhi', type: 'link', url: 'pages/address/index' },
    { id: 'refund', name: '售后服务', icon: 'shouhou', type: 'link', url: 'pages/refund/index' },
    { id: 'setting', name: '个人信息', icon: 'shezhi1', type: 'link', url: 'pages/user/setting' },
    { id: 'book2', name: '立即预约', icon: 'naozhong', type: 'link', url: 'subPages/book/index' },
    { id: 'commission', name: '分佣提成', icon: 'zijinmingxi', type: 'link', url: 'subPages/commission/statistics' },
  ]

  /**
   * 后台装修配置图标(el-icon-*) 到 会员端 iconfont 的映射
   */
  const iconMap = {
    'el-icon-wallet': 'icon-qianbao',
    'el-icon-coin': 'icon-jifen',
    'el-icon-present': 'icon-youhuiquan',
    'el-icon-document-copy': 'icon-lingquan',
    'el-icon-date': 'icon-tuxingyanzhengma',
    'el-icon-question': 'icon-bangzhu',
    'el-icon-chat-line-round': 'icon-kefu',
    'el-icon-location-outline': 'icon-shouhuodizhi',
    'el-icon-s-check': 'icon-shouhou',
    'el-icon-s-order': 'icon-qpdingdan',
    'el-icon-money': 'icon-daifukuan',
    'el-icon-check': 'icon-daishouhuo',
    'el-icon-s-grid': 'icon-fuwu',
    'el-icon-set-up': 'icon-shezhi1',
    'el-icon-user': 'icon-profile',
    'el-icon-phone': 'icon-dianhua',
    'el-icon-time': 'icon-shijian',
    'el-icon-star': 'icon-xihuan',
    'el-icon-bell': 'icon-xiaoxi',
    'el-icon-search': 'icon-sousuo',
    'el-icon-menu': 'icon-fenlei',
    'el-icon-shopping-cart-2': 'icon-gouwuche',
    'el-icon-s-goods': 'icon-shangcheng',
    'el-icon-map-location': 'icon-dizhi',
    'el-icon-service': 'icon-fuwu',
    'el-icon-tickets': 'icon-youhuiquan',
    'el-icon-credit-card': 'icon-qiandai'
  }

  /**
   * 后台装修组件 type 别名 到 会员端组件 key 的映射（兼容不同命名）
   */
  const COMP_TYPE_MAP = {
    'memberInfoCard': 'userInfoCard',
    'userInfo': 'userInfoCard',
    'userInfoCard': 'userInfoCard',
    'myAssets': 'userAssets',
    'assets': 'userAssets',
    'userAssets': 'userAssets',
    'order': 'orderEntry',
    'myOrder': 'orderEntry',
    'orderEntry': 'orderEntry',
    'coupon': 'couponStats',
    'myCoupon': 'couponStats',
    'couponStats': 'couponStats',
    'vip': 'vipUpgrade',
    'memberUpgrade': 'vipUpgrade',
    'vipUpgrade': 'vipUpgrade',
    'service': 'serviceGrid',
    'myService': 'serviceGrid',
    'serviceGrid': 'serviceGrid'
  }

  export default {
    components: {
      Popup,
      // #ifdef H5
      H5Tabbar
      // #endif
    },
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
        isMerchant: false,
        gradeEndTime: '',
        // 账户资产
        assets: { prestore: '0', timer: '0', coupon: '0' },
        // 我的服务
        service: [],
        // 订单操作
        orderNavbar,
        // 当前用户待处理的订单数量
        todoCounts: { payment: 0 },
        current: 0,
        // 显示、隐藏弹窗
        showPopup: false,
        memberGrade: [],
        curGrade: {},
        storeList: [],
        // 个人中心页面装修配置
        userPage: { pageName: '会员中心', components: [] }
      }
    },

    /**
     * 生命周期函数--监听页面显示
     */
    onShow(options) {
      // 拉取 tabBar 配置（缓存优先），自定义 tabBar 实例可能尚未就绪会自动重试
      loadAndApplyTabbar(this)
      // #ifdef H5
      this.$refs.h5Tabbar && this.$refs.h5Tabbar.refresh()
      // #endif
      // #ifdef MP-WEIXIN
      // 微信注入的 getTabBar 挂在原生页面实例上，uni-app 需经 $scope 访问
      const host = this.$scope || this
      const tb = typeof host.getTabBar === 'function' && host.getTabBar()
      tb && tb.syncSelected && tb.syncSelected()
      // #endif

      // 获取页面数据
      this.getPageData()

      // 判断是否已登录
      this.isLogin = checkLogin()

      // 消息显示
      showMessage();
    },

    methods: {
      /**
       * 会员卡权益说明行
       * 后台 desc 常为空格分隔的多条权益（如 "买单9折 积分翻2倍"），这里按空格拆开，让每条权益单独换行显示；
       * 同时把独立的折扣/积分字段并入，避免与 desc 文案重复
       */
      descLines(item) {
        const lines = []
        if (item.desc) {
          String(item.desc).split(/\s+/).forEach(t => {
            if (t && lines.indexOf(t) === -1) lines.push(t)
          })
        }
        if (item.discount > 0) {
          const text = '买单' + item.discount + '折'
          if (lines.indexOf(text) === -1) lines.push(text)
        }
        if (item.speedPoint > 0) {
          const text = '积分翻' + item.speedPoint + '倍'
          if (lines.indexOf(text) === -1) lines.push(text)
        }
        return lines
      },

      // 获取页面数据
      getPageData(callback) {
        const app = this
        app.isLoading = true
        Promise.all([app.getSetting(), app.getUserInfo(), app.getUserAssets(), app.getTodoCounts(), app.getUserPage()])
          .then(result => {
            app.isLoading = false
            // 初始化我的服务数据
            app.initService()
            // 应用后台设置的页面名称
            if (app.userPage && app.userPage.pageName) {
              uni.setNavigationBarTitle({ title: app.userPage.pageName })
            }
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
        // 优先使用后台装修配置的服务列表
        const gridComp = app.getComp('serviceGrid')
        if (gridComp && gridComp.data && gridComp.data.items && gridComp.data.items.length > 0) {
          const configItems = gridComp.data.items
          app.service = configItems.map(item => {
            const target = defaultService.find(s => s.name === item.name)
            const isContact = (item.name && item.name.indexOf('客服') > -1) || (item.openType && item.openType === 'contact')
            return {
              id: item.name,
              name: item.name,
              icon: (app.iconClass(item.icon) || 'icon-fuwu').replace('icon-', ''),
              type: isContact ? 'button' : 'link',
              openType: 'contact',
              url: item.url || (target ? target.url : '')
            }
          })
          return
        }
        // 后台无配置时从服务接口获取
        ServiceApi.list()
          .then(result => {
            if (result.data && result.data.serviceList && result.data.serviceList.length > 0) {
              const newService = []
              result.data.serviceList.forEach(item => {
                newService.push({
                  id: item.id || '',
                  name: item.name,
                  icon: item.icon,
                  type: item.type,
                  url: item.url || '',
                  openType: item.openType || '',
                })
              })
              app.service = newService
            } else {
              // 后台无配置时使用本地默认数据
              app.service = [...defaultService]
            }
          })
          .catch(() => {
            // 接口异常时使用本地默认数据
            app.service = [...defaultService]
          })
      },

      // 获取设置
      getSetting() {
        const app = this
        app.setting = {}
      },

      // 获取当前用户信息
      getUserInfo() {
        const app = this
        app.showPopup = false;
        return new Promise((resolve, reject) => {
            UserApi.info()
            .then(result => {
              if (result.data.userInfo) {
                  app.userInfo = result.data.userInfo
                  app.isLogin = true
              } else {
                  app.isLogin = false
                  app.userInfo = { id: 0, name: '', avatar: '', gradeId: 0, mobile: '', balance: 0 }
              }

              // 强制领取会员卡
              if (result.data.openWxCard && app.userInfo) {
                  this.$navTo('pages/user/card?userId='+app.userInfo.id);
                  return false;
              }

             // 强制更新头像或昵称
             if (result.data.needUpdateAvatar || result.data.needUpdateNickname) {
                 let tips = [];
                 if (result.data.needUpdateAvatar) tips.push('头像');
                 if (result.data.needUpdateNickname) tips.push('昵称');
                 uni.showModal({
                    title: '提示',
                    content: '请先完善您的' + tips.join('和'),
                    showCancel: false,
                    confirmText: '去完善',
                    success: () => {
                       app.$navTo('pages/user/setting')
                    }
                 });
             }

              app.gradeInfo = result.data.gradeInfo;
              app.memberGrade = result.data.memberGrade;
              app.gradeEndTime = result.data.gradeEndTime;
              app.isMerchant = result.data.isMerchant;
              resolve(app.userInfo);
              resolve(app.gradeInfo);
              resolve(isMerchant);
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
            UserApi.assets()
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

      // 获取当前用户待处理的事项数量
      getTodoCounts() {
        const app = this
        return new Promise((resolve, reject) => {
          !app.isLogin ? resolve(null) : OrderApi.todoCounts()
            .then(result => {
              app.todoCounts = result.data
              resolve(app.todoCounts)
            })
        })
      },

      // 获取个人中心页面装修配置
      getUserPage() {
        const app = this
        return new Promise((resolve) => {
          UserPageApi.info()
            .then(result => {
              if (result.data) {
                app.userPage = result.data
              }
              resolve(app.userPage)
            })
            .catch(() => {
              resolve(null)
            })
        })
      },

      // 根据组件类型获取组件配置（兼容后台 type 别名）
      getComp(type) {
        const app = this
        const mappedType = COMP_TYPE_MAP[type] || type
        const list = app.userPage && app.userPage.components ? app.userPage.components : []
        for (let i = 0; i < list.length; i++) {
          const itemType = list[i].type && (COMP_TYPE_MAP[list[i].type] || list[i].type)
          if (itemType === mappedType) {
            return list[i]
          }
        }
        return null
      },

      // 组件是否可见
      compVisible(type) {
        const comp = this.getComp(type)
        if (!comp || !comp.data) return true
        return comp.data.visible !== false
      },

      // 组件在后台配置中的排序（index 越大越靠后）
      compOrder(type) {
        const app = this
        const mappedType = COMP_TYPE_MAP[type] || type
        const list = app.userPage && app.userPage.components ? app.userPage.components : []
        for (let i = 0; i < list.length; i++) {
          const itemType = list[i].type && (COMP_TYPE_MAP[list[i].type] || list[i].type)
          if (itemType === mappedType) return i
        }
        return 99
      },

      // 后台装修图标转换为会员端 iconfont 类名
      iconClass(icon) {
        if (!icon) return 'icon-fuwu'
        if (icon.indexOf('icon-') === 0) return icon
        if (icon.indexOf('el-icon-') === 0) return iconMap[icon] || 'icon-fuwu'
        return 'icon-' + icon
      },

      // 资产数值（按名称或跳转路径识别余额/积分，防止后台改了文案数值对不上）
      assetValue(item) {
        const app = this
        const name = item.name || ''
        const url = item.url || ''
        const isBalance = name.indexOf('余额') > -1 || url.indexOf('wallet') > -1
        const isPoint = name.indexOf('积分') > -1 || url.indexOf('points') > -1
        if (isBalance) return app.isLogin ? Number(app.userInfo.balance || 0).toFixed(2) : '0.00'
        if (isPoint) return app.userInfo.point ? app.userInfo.point : 0
        return item.value || 0
      },

      // 资产跳转
      onUserAsset(item) {
        const app = this
        if (!app.isLogin) {
          app.$navTo('pages/login/index')
          return
        }
        // 后台可能使用 url 或 link 字段保存跳转链接
        let url = item.url || item.link
        // 未配置链接时，按名称兜底到默认页面
        if (!url) {
          const name = item.name || ''
          if (name.indexOf('余额') > -1) url = 'pages/wallet/index'
          else if (name.indexOf('积分') > -1) url = 'pages/points/detail'
        }
        if (!url) return
        const userId = app.userInfo.id || 0
        const query = url.indexOf('?') > -1 ? '&' : '?'
        app.$navTo(url + query + 'userId=' + userId)
      },

      // 卡券统计数值
      couponStatsValue(item) {
        const app = this
        const key = item.key
        const assets = app.assets || {}
        if (!app.isLogin) return '0'
        if (key === 'coupon') return assets.coupon || 0
        if (key === 'stored') return assets.prestore || 0
        if (key === 'count') return assets.timer || 0
        return item.value || 0
      },

      // 卡券统计跳转
      onCouponStats(item) {
        const app = this
        if (item.url) {
          app.$navTo(item.url)
          return
        }
        const typeMap = { coupon: 'C', stored: 'P', count: 'T' }
        app.onTargetMyCoupon(typeMap[item.key] || 'C')
      },

      // 成为商家
      handleBeMerchant() {
        if (!this.isLogin) {
          this.$navTo('pages/login/index')
          return
        }
        this.$error('请先联系商家，添加您的员工信息！');
      },

      // 获取店铺列表
      getStoreList() {
        const app = this
        SettingApi.storeList()
          .then(result => {
            const list = result.data.data || []
            app.storeList = [{ id: 0, name: '全部店铺' }, ...list]
          })
          .catch(err => {
            console.log('获取店铺列表失败', err)
          })
      },

      // 会员等级
      onShowPopup(index) {
        this.showPopup = !this.showPopup
        this.current = index
        // 优先使用后端实时套餐数据(支付需要套餐id)，其次使用后台装修配置
        this.curGrade = this.memberGrade[index] || this.vipItems[index]
      },

      // 跳转到会员码
      toMemberCode(userId) {
          !this.isLogin && this.$navTo('pages/login/index')
          this.$navTo('pages/user/code', { userId: userId})
      },

      // 跳转我的余额
      toMemberWallet(userId) {
          !this.isLogin && this.$navTo('pages/login/index')
          this.$navTo('pages/wallet/index', { userId: userId})
      },

      // 跳转充值
      toRecharge(userId) {
          !this.isLogin && this.$navTo('pages/login/index')
          this.$navTo('pages/wallet/recharge/index', { userId: userId})
      },

      // 跳转到订单页
      onTargetOrder(item) {
          if (!this.isLogin) {
              this.$navTo('pages/login/index')
              return
          }
          // 订单页是 tabBar 页面, switchTab 无法携带 query, 通过缓存传递目标 Tab
          uni.setStorageSync('userOrderInitTab', item.id)
          this.$navTo('pages/order/index', { dataType: item.id })
      },

      // 跳转到我的积分页面
      onTargetPoints() {
         !this.isLogin && this.$navTo('pages/login/index')
         this.$navTo('pages/points/detail')
      },

      // 跳转到我的卡券列表页
      onTargetMyCoupon(type) {
          const app = this
          if (app.isLogin) {
              // #ifdef MP-WEIXIN
              MessageApi.getSubTemplate({keys: "couponExpire,couponArrival"}).then(result => {
                  const templateIds = result.data
                  wx.requestSubscribeMessage({tmplIds: templateIds,
                  success(res) {
                      console.log("调用成功！")
                  }, fail(res) {
                      console.log("调用失败:", res)
                  }, complete() {
                      app.$navTo('pages/my-coupon/index?type='+type)
                  }})
              })
              // #endif
              // #ifndef MP-WEIXIN
                 app.$navTo('pages/my-coupon/index?type='+type)
              // #endif
          } else {
              app.$navTo('pages/login/index')
          }
      },

      // 跳转会员设置页面
      onUserInfo() {
          if (!this.isLogin) {
              this.$navTo('pages/login/index')
          } else {
              this.$navTo('pages/user/setting')
          }
      },

      // 跳转到服务页面
      handleService({ url, link }) {
          this.$navTo(url || link)
      }
    },

    computed: {
      // 用户信息卡背景色：统一跟随主题色，后台不再单独配置背景色
      userInfoBgStyle() {
        return { background: 'var(--theme-primary)' }
      },

      // 我的资产列表
      userAssetsItems() {
        const comp = this.getComp('userAssets')
        const defaultItems = [
          { name: '余额', unit: '元', icon: 'qianbao', url: 'pages/wallet/index' },
          { name: '积分', unit: '分', icon: 'jifen', url: 'pages/points/detail' }
        ]
        const items = (comp && comp.data && comp.data.items) ? comp.data.items : defaultItems
        // 后台配置项若未设置链接，按名称兜底补全默认跳转地址
        return items.map(item => {
          const name = item.name || ''
          const target = defaultItems.find(d => d.name === name)
          return {
            ...item,
            url: item.url || item.link || (target ? target.url : '')
          }
        })
      },

      // 订单入口列表
      orderItems() {
        const app = this
        const comp = app.getComp('orderEntry')
        // 订单页支持的 Tab 值: all / toPay / paid / cancel
        const statusMap = { all: 'all', waitPay: 'toPay', toPay: 'toPay', paid: 'paid', cancel: 'cancel' }
        const items = (comp && comp.data && comp.data.items) ? comp.data.items : orderNavbar
        return items.map(item => {
          // 兼容后台配置(status)与本地默认(id)两种取值, 映射失败则原样保留
          const raw = item.status || item.id
          const key = statusMap[raw] || raw || ''
          return {
            id: key,
            name: item.name,
            icon: item.icon,
            count: app.isLogin ? (app.todoCounts[key] || 0) : 0
          }
        })
      },

      // 卡券统计列表
      couponStatsItems() {
        const comp = this.getComp('couponStats')
        const items = (comp && comp.data && comp.data.items) ? comp.data.items : [
          { name: '优惠券', key: 'coupon', color: '#f03c3c' },
          { name: '储值卡', key: 'stored', color: '#f03c3c' },
          { name: '计次卡', key: 'count', color: '#f03c3c' }
        ]
        return items
      },

      // 会员升级标题
      vipTitle() {
        const comp = this.getComp('vipUpgrade')
        return (comp && comp.data && comp.data.title) ? comp.data.title : '会员升级'
      },

      // 会员升级套餐列表
      vipItems() {
        const app = this
        const comp = app.getComp('vipUpgrade')
        const configItems = (comp && comp.data && comp.data.items && comp.data.items.length > 0) ? comp.data.items : []
        if (configItems.length > 0) {
          return configItems.map(item => ({
            name: item.name,
            days: item.days || 0,
            price: item.price || 0,
            desc: item.desc || '',
            discount: 0,
            speedPoint: 0
          }))
        }
        return (app.memberGrade || []).map(item => ({
          name: item.name,
          days: item.validDay || 0,
          price: item.catchValue,
          desc: '',
          discount: item.discount || 0,
          speedPoint: item.speedPoint || 0
        }))
      },

      // 我的服务标题
      serviceTitle() {
        const comp = this.getComp('serviceGrid')
        return (comp && comp.data && comp.data.title) ? comp.data.title : '我的服务'
      },

      // 我的服务列数
      serviceColumns() {
        const comp = this.getComp('serviceGrid')
        return (comp && comp.data && comp.data.columns) ? comp.data.columns : 4
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
  // 页面容器：使用 flex 列布局，通过 order 控制后台装修组件的显示顺序
  .container {
    display: flex;
    flex-direction: column;
    min-height: 100vh;
  }

  // 页面头部
  .main-header {
    position: relative;
    background: var(--theme-primary);
    height: 280rpx;
    background-size: cover;
    overflow: hidden;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    padding: 30rpx 20rpx 20rpx 20rpx;
    margin: 20rpx 25rpx 20rpx 25rpx;
    border-radius: 10rpx;

    .user-info {
      display: flex;
      flex-direction: row;
      align-items: center;
      justify-content: flex-start;
      width: 100%;
      margin: 0;
      .user-avatar {
          padding-top: 0;
          width: 100rpx;
          margin: 0 12rpx 0 0;
          float: none;
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
        margin: 0;
        float: none;
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
          display: inline-flex;
          align-items: center;
          align-self: flex-start;
          background: #3c3c3c;
          margin-top: 8rpx;
          border-radius: 10rpx;
          padding: 5rpx 12rpx;
          height: 40rpx;

          .user-grade_icon .image {
            display: block;
            width: 32rpx;
            height: 32rpx;
            float: none;
          }

          .user-grade_name {
            margin-left: 5rpx;
            font-size: 24rpx;
            color: #EEE0C3;
            white-space: nowrap;
            float: none;
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
      .pay-qr {
          position: absolute;
          top: 20rpx;
          right: 20rpx;
          color:#ffffff;
          margin: 0;
          text-align: center;
          width: 50rpx;
          float: none;
          .qrcode {
              display: block;
              font-size: 40rpx;
          }
      }
    }
    .user-no {
        position: absolute;
        left: 20rpx;
        right: 20rpx;
        bottom: 20rpx;
        display: flex;
        justify-content: space-between;
        align-items: center;
        width: auto;
        font-size: 25rpx;
        margin: 0;
        color: #ffffff;
        .no {
            float: none;
        }
        .recharge {
            float: none;
            margin-left: auto;
            margin-right: 0;
        }
    }
  }

  // 余额和积分卡片（与后台“我的资产”配置保持一致：图标在上、数值居中、名称在下）
  .asset-card {
    display: flex;
    align-items: center;
    margin: 10rpx 25rpx 10rpx 25rpx;
    background: linear-gradient(180deg, #ffffff 0%, #fafbfc 100%);
    border-radius: 16rpx;
    border: 1rpx solid rgba(0, 0, 0, 0.04);
    box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04), 0 0 0 1rpx rgba(0, 172, 172, 0.06);
    padding: 36rpx 0;
    position: relative;
    z-index: 2;

    .asset-card-item {
      flex: 1;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 0 20rpx;

      .asset-card-icon {
        width: 64rpx;
        height: 64rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        line-height: 1;

        .iconfont {
          font-size: 50rpx;
          line-height: 1;
        }
      }

      .asset-card-value {
        font-size: 42rpx;
        font-weight: bold;
        color: #333;
        line-height: 1.2;
        margin-bottom: 10rpx;
      }

      .asset-card-label {
        font-size: 22rpx;
        color: #999;
        line-height: 1;
      }
    }

    .asset-card-divider {
      width: 2rpx;
      height: 60rpx;
      background: #f0f0f0;
    }
  }

  // 我的资产
  .my-asset {
    display: flex;
    background: #fff;
    margin: 10rpx 20rpx 10rpx 20rpx;
    padding: 40rpx 0;
    border: 2rpx #f5f5f5 solid;
    border-radius: 10rpx;
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
        font-size: 42rpx;
        color: #f03c3c;
        font-weight: bold;
      }

      .item-name {
        font-size: 24rpx;
        color: #666;
        margin-top: 10rpx;
      }
    }

  }

  // 订单操作
  .order-navbar {
    display: flex;
    margin: 10rpx 25rpx 10rpx 25rpx;
    padding: 20rpx 0;
    width: auto;
    box-shadow: 0 1rpx 5rpx 0px rgba(0, 0, 0, 0.05);
    font-size: 30rpx;
    border-radius: 10rpx;
    background: #fff;
    border: 2rpx #f5f5f5 solid;
    &-item {
      position: relative;
      width: 33%;
      .item-icon {
        text-align: center;
        margin: 0 auto;
        padding: 10rpx 0;
        color: #545454;
        font-size: 48rpx;
        font-weight: bold;
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
        right: 58rpx;
        font-size: 20rpx;
        background: #fa5151;
        text-align: center;
        line-height: 30rpx;
        color: #fff;
        border-radius: 50%;
        min-width: 36rpx;
        padding: 6rpx 13rpx 6rpx 13rpx;
      }
    }
  }

  // 我的服务
  .my-service {
    margin: 10rpx 25rpx 10rpx 25rpx;
    border: 2rpx #f5f5f5 solid;
    background: #FFF;
    padding: 10rpx 0rpx;
    width: auto;
    box-shadow: 0 1rpx 5rpx 0px rgba(0, 0, 0, 0.05);
    border-radius: 10rpx;
    display: block;

    .service-title {
      padding-left: 20rpx;
      margin-bottom: 30rpx;
      font-size: 28rpx;
    }

    .service-content {
      &.service-col-3 {
        .service-item {
          width: 33.33%;
        }
      }

      &.service-col-5 {
        .service-item {
          width: 20%;
        }
      }

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
      margin: 10rpx 25rpx 10rpx 25rpx;
      padding: 20rpx 0;
      border-radius: 10rpx;
      box-shadow: 0 1rpx 5rpx 0px rgba(0, 0, 0, 0.05);
      background: #fff;
      width: auto;
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
                height: 300rpx;
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
                    display: block;
                    width: 100%;
                    text-align: center;
                    font-size: 22rpx;
                    color: #A5A3A2;
                    white-space: nowrap;
                }
            }
        }
    }
</style>
