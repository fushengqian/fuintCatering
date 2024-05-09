<template>
  <grade-popup :value="value" @input="onChangeValue" @onPaySuccess="onPaySuccess" :memberGrade="memberGrade" border-radius="20" :maskCloseAble="true"
    @open="openPopup" @close="closePopup"/>
</template>

<script>
  import { setCartTotalNum } from '@/utils/app'
  import * as CartApi from '@/api/cart'
  import GradePopup from '@/components/grade-popup'

  let memberGrade;

  export default {
    components: {
      GradePopup
    },
    props: {
      // true 组件显示 false 组件隐藏
      value: {
        Type: Boolean,
        default: false
      },
      // 等级详情
      memberGrade: {
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
        this.$emit('input', val);
      },
      
      // 支付成功
      onPaySuccess() {
        this.$emit('onPaySuccess');
      },

      // sku组件 开始-----------------------------------------------------------
      openPopup() {
         console.log("监听 - 打开弹框组件");
      },

      closePopup() {
         console.log("监听 - 关闭弹框组件")
      }
    }
  }
</script>

<style lang="scss" scoped>

</style>
