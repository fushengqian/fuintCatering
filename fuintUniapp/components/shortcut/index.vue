<template>
  <view class="shortcut" :style="{ right: right, bottom: bottom }">

    <!-- 首页 -->
    <view class="nav-item" :class="[isShow ? 'show_60' : (transparent ? '' : 'hide_60')]" @click="onTargetPage(0)">
      <text class="iconfont icon-home"></text>
    </view>
    
    <view class="nav-item" :class="[isShow ? 'show_40' : (transparent ? '' : 'hide_40')]" @click="onTargetPage(1)">
      <text class="iconfont icon-cate"></text>
    </view>
    
    <!-- 个人中心 -->
    <view class="nav-item" :class="[isShow ? 'show_20' : (transparent ? '' : 'hide_20')]" @click="onTargetPage(3)">
      <text class="iconfont icon-profile"></text>
    </view>

    <!-- 显示隐藏开关 -->
    <view class="nav-item nav-item__switch" :class="{ shortcut_click_show: isShow }" @click="onToggleShow()">
      <text class='iconfont icon-daohang'></text>
    </view>

  </view>
</template>

<script>
  import { getTabBarLinks } from '@/utils/app'
  export default {

    /**
     * 组件的属性列表
     * 用于组件自定义设置
     */
    props: {
      right: {
        type: String,
        default: '30rpx'
      },
      bottom: {
        type: String,
        default: '200rpx'
      }
    },

    data() {
      return {
        // 弹窗显示控制
        isShow: false,
        transparent: true
      }
    },

    methods: {

      /**
       * 导航菜单切换事件
       */
      onToggleShow() {
        const app = this
        app.isShow = !app.isShow
        app.transparent = false
      },

      /**
       * 导航页面跳转
       */
      onTargetPage(index = 0) {
        const tabLinks = getTabBarLinks()
        this.$navTo(tabLinks[index])
      }

    }
  }
</script>

<style lang="scss" scoped>
  /* 快捷导航 */
  .shortcut {
    position: fixed;
    right: 24rpx;
    bottom: 250rpx;
    width: 76rpx;
    line-height: 1;
    z-index: 5;
    border-radius: 50%;
  }

  /* 导航菜单元素 */
  .nav-item {
    position: absolute;
    bottom: 0;
    padding: 0;
    width: 76rpx;
    height: 76rpx;
    line-height: 76rpx;
    color: #fff;
    background: rgba(0, 0, 0, 0.4);
    border-radius: 50%;
    text-align: center;
    transform: rotate(0deg);
    opacity: 0;
  }

  .nav-item .iconfont {
    font-size: 40rpx;
  }

  /* 导航开关 */
  .nav-item__switch {
    opacity: 1;
  }

  .shortcut_click_show {
    margin-bottom: 0;
    background: #ff5454;
  }

  /* 显示动画 */
  .show_80 {
    bottom: 384rpx;
    animation: show_80 0.3s forwards;
  }

  .show_60 {
    bottom: 288rpx;
    animation: show_60 0.3s forwards;
  }

  .show_40 {
    bottom: 192rpx;
    animation: show_40 0.3s forwards;
  }

  .show_20 {
    bottom: 96rpx;
    animation: show_20 0.3s forwards;
  }

  @keyframes show_20 {
    from {
      bottom: 0;
      transform: rotate(0deg);
      opacity: 0;
    }

    to {
      bottom: 96rpx;
      transform: rotate(360deg);
      opacity: 1;
    }
  }

  @keyframes show_40 {
    from {
      bottom: 0;
      transform: rotate(0deg);
      opacity: 0;
    }

    to {
      bottom: 192rpx;
      transform: rotate(360deg);
      opacity: 1;
    }
  }

  @keyframes show_60 {
    from {
      bottom: 0;
      transform: rotate(0deg);
      opacity: 0;
    }

    to {
      bottom: 288rpx;
      transform: rotate(360deg);
      opacity: 1;
    }
  }

  @keyframes show_80 {
    from {
      bottom: 0;
      transform: rotate(0deg);
      opacity: 0;
    }

    to {
      bottom: 384rpx;
      transform: rotate(360deg);
      opacity: 1;
    }
  }

  /* 隐藏动画 */

  .hide_80 {
    bottom: 0;
    animation: hide_80 0.3s;
    opacity: 0;
  }

  .hide_60 {
    bottom: 0;
    animation: hide_60 0.3s;
    opacity: 0;
  }

  .hide_40 {
    bottom: 0;
    animation: hide_40 0.3s;
    opacity: 0;
  }

  .hide_20 {
    bottom: 0;
    animation: hide_20 0.3s;
    opacity: 0;
  }

  @keyframes hide_20 {
    from {
      bottom: 96rpx;
      transform: rotate(360deg);
      opacity: 1;
    }

    to {
      bottom: 0;
      transform: rotate(0deg);
      opacity: 0;
    }
  }

  @keyframes hide_40 {
    from {
      bottom: 192rpx;
      transform: rotate(360deg);
      opacity: 1;
    }

    to {
      bottom: 0;
      transform: rotate(0deg);
      opacity: 0;
    }
  }

  @keyframes hide_60 {
    from {
      bottom: 288rpx;
      transform: rotate(360deg);
      opacity: 1;
    }

    to {
      bottom: 0;
      transform: rotate(0deg);
      opacity: 0;
    }
  }

  @keyframes hide_80 {
    from {
      bottom: 384rpx;
      transform: rotate(360deg);
      opacity: 1;
    }

    to {
      bottom: 0;
      transform: rotate(0deg);
      opacity: 0;
    }
  }
</style>
