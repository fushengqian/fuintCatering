<template>
  <coupon-popup :value="value" @input="onChangeValue" :couponInfo="couponInfo" :storeRule="storeRule" border-radius="20" :maskCloseAble="true"
    @open="openPopup" @close="closePopup"/>
</template>

<script>
  import { setCartTotalNum } from '@/utils/app'
  import * as CartApi from '@/api/cart'
  import CouponPopup from '@/components/prestore-popup'

  export default {
    components: {
      CouponPopup
    },
    props: {
      // true 组件显示 false 组件隐藏
      value: {
        Type: Boolean,
        default: false
      },
      // 卡券详情信息
      couponInfo: {
        type: Object,
        default: {}
      },
      // 预存规则
      storeRule: {
        type: Array,
        default: []
      }
    },

    data() {
      return {}
    },

    created() {
      // empty
    },

    methods: {

      // 监听组件显示隐藏
      onChangeValue(val) {
        this.$emit('input', val)
      },

      /**
       * 获取卡券信息
       */
      findCouponInfo() {
        return new Promise((resolve, reject) => {
          resolve(couponInfo)
        })
      },

      // sku组件 开始-----------------------------------------------------------
      openPopup() {
        // console.log("监听 - 打开sku组件")
      },

      closePopup() {
        // console.log("监听 - 关闭sku组件")
      }
    }
  }
</script>

<style lang="scss" scoped>

</style>
