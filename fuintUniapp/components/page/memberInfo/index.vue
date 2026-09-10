<template>
  <view class="page-member-info" :style="cardStyle">
    <view class="member-row">
      <view class="member-avatar" :style="{ background: style.avatarBg || themePrimaryAlpha }">
        <image
          class="avatar-img"
          :src="avatarSrc"
          mode="aspectFill"
        />
      </view>
      <view class="member-main">
        <view class="member-greeting" :style="{ color: style.textColor || '#333' }">{{ greeting }}</view>
        <view v-if="isLogin" class="member-info-row" :style="{ color: style.subTextColor || '#666' }">
          <view class="member-asset-text" @click="goWallet">
            <text class="asset-label">余额：</text>
            <text class="asset-value" :style="{ color: style.textColor || '#333' }">{{ userInfo.balance || 0 }}</text>
          </view>
          <view
            class="member-rank-tag"
            :style="{ background: style.rankBg || themePrimaryAlpha, color: style.primaryColor || themeColor }"
            @click="goRank"
          >
            <text class="iconfont icon-huo"></text>
            <text>排行</text>
          </view>
          <view class="member-asset-text" @click="goPoints">
            <text class="asset-label">积分：</text>
            <text class="asset-value" :style="{ color: style.textColor || '#333' }">{{ userInfo.point || 0 }}</text>
          </view>
        </view>
        <view v-else class="member-info-row member-tip" :style="{ color: style.subTextColor || '#666' }">
          登录后查看会员信息
        </view>
      </view>
      <view
        v-if="isLogin"
        class="member-qrcode"
        :style="{ color: style.primaryColor || themeColor }"
        @click="goMemberCode"
      >
        <text class="iconfont icon-qr-extract"></text>
      </view>
      <view
        v-else
        class="member-login"
        :style="{ background: themeColor, color: '#fff' }"
        @click="goLogin"
      >登录</view>
    </view>
  </view>
</template>

<script>
  import { getThemePrimary } from '@/utils/theme'

  export default {
    name: 'MemberInfo',
    props: {
      itemStyle: {
        type: Object,
        default () {
          return {}
        }
      },
      params: {
        type: Object,
        default () {
          return {}
        }
      },
      userInfo: {
        type: Object,
        default () {
          return {}
        }
      }
    },
    computed: {
      style() {
        return this.itemStyle || {}
      },
      isLogin() {
        return !!(this.userInfo && this.userInfo.id)
      },
      greeting() {
        return (this.params && this.params.greeting) || 'Hi，你好！'
      },
      // 头像地址：未设置头像时回退到 default-avatar.png（与 HomeUser 保持一致）
      avatarSrc() {
        return (this.userInfo && this.userInfo.avatar) ? this.userInfo.avatar : '/static/default-avatar.png'
      },
      // 注意：小程序端 :style 绑定对象会被序列化成 [object Object] 导致样式失效，
      // 因此统一返回 style 字符串（与 utils/theme.js、coupon 组件做法一致）
      cardStyle() {
        const s = this.style
        const borderWidth = this.numStyle(s.borderWidth, 0)
        const height = this.numStyle(s.height, 0)
        const parts = [
          `background: ${s.background || '#ffffff'}`,
          `border-radius: ${this.numStyle(s.borderRadius, 12)}px`,
          `padding: ${this.numStyle(s.padding, 16)}px`,
          `margin: ${this.resolveMargin(s)}`
        ]
        if (borderWidth > 0) {
          parts.push(`border: ${borderWidth}px ${s.borderStyle || 'solid'} ${s.borderColor || '#e5e5e5'}`)
        }
        if (height > 0) {
          parts.push(`height: ${height}px`, 'display: flex', 'align-items: center')
        }
        return parts.join(';')
      },
      // 主题色 0.1 透明度的 rgba（用于等级标签背景），hex -> rgba
      themePrimaryAlpha() {
        // 主题色缺失时回退全局主题主色(utils/theme 兜底色)，避免硬编码品牌深绿 rgba(17,58,40)
        const c = String(this.themeColor || getThemePrimary() || '').replace('#', '')
        if (!/^[0-9a-fA-F]{6}$/.test(c)) return 'transparent'
        const r = parseInt(c.substr(0, 2), 16)
        const g = parseInt(c.substr(2, 2), 16)
        const b = parseInt(c.substr(4, 2), 16)
        return `rgba(${r}, ${g}, ${b}, 0.1)`
      }
    },
    methods: {
      // 数值型样式取值：undefined/null/空串/非数字统一回退默认值，避免生成 "nullpx" 之类无效样式
      numStyle(value, defaultValue) {
        if (value === undefined || value === null || value === '') return defaultValue
        const num = Number(value)
        return isNaN(num) ? defaultValue : num
      },
      // 外边距：优先取四方向设置，兼容旧的单值 margin，默认 10px
      resolveMargin(s) {
        const style = s || {}
        const fallback = this.numStyle(style.margin, 10)
        const pick = (key) => (style[key] === undefined || style[key] === null || style[key] === '') ? fallback : style[key]
        return `${pick('marginTop')}px ${pick('marginRight')}px ${pick('marginBottom')}px ${pick('marginLeft')}px`
      },
      // 去登录
      goLogin() {
        this.$navTo('pages/login/index')
      },
      // 跳转会员码
      goMemberCode() {
        this.$navTo('pages/user/code', { userId: this.userInfo.id })
      },
      // 跳转积分排行榜
      goRank() {
        this.$navTo('pages/points/rank')
      },
      // 跳转钱包
      goWallet() {
        this.$navTo('pages/wallet/index')
      },
      // 跳转积分明细
      goPoints() {
        this.$navTo('pages/points/detail')
      }
    }
  }
</script>

<style lang="scss" scoped>
  .page-member-info {
    box-sizing: border-box;
    width: auto;

    .member-row {
      display: flex;
      align-items: center;
      flex: 1;
      width: 100%;
      min-width: 0;
    }

    .member-avatar {
      width: 96rpx;
      height: 96rpx;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      overflow: hidden;
      flex-shrink: 0;

      .avatar-img {
        width: 100%;
        height: 100%;
        display: block;
      }
    }

    .member-main {
      flex: 1;
      min-width: 0;
      padding-left: 20rpx;
      overflow: hidden;
    }

    .member-greeting {
      font-size: 32rpx;
      font-weight: bold;
      line-height: 1.4;
    }

    .member-info-row {
      display: flex;
      align-items: center;
      margin-top: 12rpx;
      font-size: 24rpx;
      line-height: 1.4;
      flex-wrap: nowrap;
      overflow: hidden;
    }

    .member-asset-text {
      display: flex;
      align-items: baseline;
      flex-shrink: 1;
      min-width: 0;

      .asset-label {
        flex-shrink: 0;
      }

      .asset-value {
        font-weight: bold;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        max-width: 200rpx;
      }
    }

    .member-asset-text + .member-rank-tag {
      margin-left: 16rpx;
    }

    .member-rank-tag {
      display: flex;
      align-items: center;
      flex-shrink: 0;
      font-size: 22rpx;
      padding: 4rpx 14rpx;
      border-radius: 20rpx;
      line-height: 1.4;

      .iconfont {
        font-size: 22rpx;
        margin-right: 4rpx;
      }
    }

    .member-rank-tag + .member-asset-text {
      margin-left: 16rpx;
    }

    .member-tip {
      font-size: 24rpx;
    }

    .member-qrcode {
      flex-shrink: 0;
      font-size: 48rpx;
      font-weight: bold;
      margin-left: 16rpx;
      padding: 8rpx;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .member-login {
      flex-shrink: 0;
      height: 56rpx;
      line-height: 56rpx;
      padding: 0 32rpx;
      border-radius: 60rpx;
      font-size: 26rpx;
      margin-left: 16rpx;
    }
  }
</style>