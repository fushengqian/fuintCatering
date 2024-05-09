<template>
  <pay-popup :value="value" @input="onChangeValue" :payInfo="payInfo" @modifyChoice="modifyChoice" border-radius="20" :maskCloseAble="true"
    @open="openPopup" @close="closePopup"/>
</template>

<script>
  import PayPopup from '@/components/pay-popup'

  var that; // 当前页面对象
  let payInfo;

  export default {
    components: {
      PayPopup
    },
    props: {
      // true 组件显示 false 组件隐藏
      value: {
        Type: Boolean,
        default: false
      },
      // 支付信息
      payInfo: {
        type: Object,
        default: {}
      }
    },

    data() {
      return {}
    },

    methods: {
      // 监听组件显示隐藏
      onChangeValue(val) {
        this.$emit('input', val)
      },

      // sku组件 开始-----------------------------------------------------------
      openPopup() {
         console.log("监听 - 打开弹框组件")
      },

      closePopup() {
         console.log("监听 - 关闭弹框组件")
      },
      // 改变支付信息
      modifyChoice(payInfo) {
        this.$emit('modifyChoice', payInfo)
      },
    }
  }
</script>

<style lang="scss" scoped>

</style>
