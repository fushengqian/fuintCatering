<template>
  <!-- 在线客服 -->
  <view v-if="!(params.type === 'chat' && !isMpWeiXin)" class="diy-service" :style="{ right: `${itemStyle.right}%`, bottom: `${itemStyle.bottom}%` }">
    <!-- 拨打电话 -->
    <block v-if="params.type === 'phone'">
      <view class="service-icon" @click="onMakePhoneCall">
        <image class="image" :src="params.image"></image>
      </view>
    </block>
    <!-- 在线聊天 -->
    <block v-else-if="params.type == 'chat'">
      <button open-type="contact" class="btn-normal">
        <view class="service-icon">
          <image class="image" :src="params.image"></image>
        </view>
      </button>
    </block>
  </view>
</template>

<script>
  export default {

    /**
     * 组件的属性列表
     * 用于组件自定义设置
     */
    props: {
      itemStyle: Object,
      params: Object
    },

    /**
     * 私有数据,组件的初始数据
     * 可用于模版渲染
     */
    data() {
      return {
        isMpWeiXin: false,
        isShow: true
      }
    },

    created() {
      // #ifdef MP-WEIXIN
      this.isMpWeiXin = true
      // #endif
    },

    /**
     * 组件的方法列表
     * 更新属性和数据的方法与更新页面数据的方法类似
     */
    methods: {

      /**
       * 点击拨打电话
       */
      onMakePhoneCall(e) {
        uni.makePhoneCall({
          phoneNumber: this.params.tel
        })
      }

    }

  }
</script>

<style lang="scss" scoped>
  .diy-service {
    position: fixed;
    z-index: 999;

    .service-icon {
      padding: 10rpx;

      .image {
        display: block;
        width: 90rpx;
        height: 90rpx;
      }
    }

  }
</style>
