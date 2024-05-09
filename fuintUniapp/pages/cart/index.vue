<template>
  <view class="container">
    <!-- 页面顶部 -->
    <view v-if="list.length" class="head-info">
      <view class="cart-total">
        <text>共</text>
        <text class="active">{{ total }}</text>
        <text>件商品</text>
      </view>
      <view class="cart-edit" @click="handleToggleMode">
        <view v-if="mode == 'normal'" class="normal">
          <text class="icon iconfont icon-bianji"></text>
          <text>编辑</text>
        </view>
        <view v-if="mode == 'edit'" class="edit">
          <text>完成</text>
        </view>
      </view>
    </view>
    <!-- 购物车商品列表 -->
    <view v-if="list.length" class="cart-list">
      <view class="cart-item" v-for="(item, index) in list" :key="index" @click="onTargetGoods(item.goodsId)">
        <label class="item-radio" @click.stop="handleCheckItem(item.id)">
          <radio class="radio" color="#fa2209" :checked="inArray(item.id, checkedIds)" />
        </label>
        <view class="goods-image">
          <image class="image" :src="item.goodsInfo.logo"></image>
        </view>
        <view class="item-content">
          <view class="goods-title twolist-hidden"><text>{{ item.goodsInfo.name }}</text></view>
          <view class="goods-props clearfix">
            <view class="goods-props-item" v-for="(spec, idx) in item.specList" :key="idx">
              <text>{{ spec.specName }}{{ spec.specValue }}</text>
            </view>
          </view>
          <view class="item-foot">
            <view class="goods-price">
              <text class="unit">￥</text>
              <text class="value">{{ item.goodsInfo.price }}</text>
            </view>
            <view class="stepper">
              <u-number-box :min="1" :value="item.num" @change="onChangeStepper($event, item)" />
            </view>
          </view>
        </view>
      </view>
    </view>
    <!-- 购物车数据为空 -->
    <empty v-if="!list.length" :isLoading="isLoading" :custom-style="{ padding: '180rpx 50rpx' }" tips="您没有待结算商品, 快去逛逛吧">
      <view slot="slot" class="empty-ipt" @click="onTargetIndex">
        <text>去逛逛</text>
      </view>
    </empty>
    <!-- 底部操作栏 -->
    <view v-if="list.length" class="footer-fixed">
      <label class="all-radio" @click="handleCheckAll">
        <radio class="radio" color="#fa2209" :checked="checkedIds.length > 0 && checkedIds.length === list.length" />
        <text>全选</text>
      </label>
      <view class="total-info">
        <text>合计：</text>
        <view class="goods-price">
          <text class="unit">￥</text>
          <text class="value">{{ totalPrice }}</text>
        </view>
      </view>
      <view class="cart-action">
        <view class="btn-wrapper">
          <!-- dev:下面的disabled条件使用checkedIds.join方式判断 -->
          <!-- dev:通常情况下vue项目使用checkedIds.length更合理, 但是length属性在微信小程序中不起作用 -->
          <view v-if="mode == 'normal'" class="btn-item btn-main" :class="{ disabled: checkedIds.join() == '' }"
            @click="handleOrder()">
            <text>确认结算 {{ checkedIds.length > 0 ? `(${checkedIds.length})` : '' }}</text>
          </view>
          <view v-if="mode == 'edit'" class="btn-item btn-main" :class="{ disabled: !checkedIds.length }"
            @click="handleDelete()">
            <text>删除</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
  import { inArray, arrayIntersect, debounce } from '@/utils/util'
  import { checkLogin, setCartTotalNum, setCartTabBadge } from '@/utils/app'
  import * as CartApi from '@/api/cart'
  import Empty from '@/components/empty'
  const CartIdsIndex = 'CartIds'
  export default {
    components: {
      Empty
    },
    data() {
      return {
        inArray,
        // 正在加载
        isLoading: true,
        // 当前模式: normal正常 edit编辑
        mode: 'normal',
        // 购物车列表
        list: [],
        // 购物车商品总数量
        total: 0,
        // 选中的商品ID记录
        checkedIds: [],
        // 选中的商品总金额
        totalPrice: '0.00'
      }
    },
    watch: {
      // 监听选中的商品
      checkedIds: {
        handler(val) {
          // 计算合计金额
          this.onCalcTotalPrice()
          // 记录到缓存中
          uni.setStorageSync(CartIdsIndex, val)
        },
        immediate: false
      },
      // 监听购物车商品总数量
      total(val) {
        // 缓存并设置角标
        setCartTotalNum(val)
        setCartTabBadge()
      }
    },

    /**
     * 生命周期函数--监听页面显示
     */
    onShow(options) {
      // 获取购物车卡券列表
      checkLogin() ? this.getCartList() : this.isLoading = false
      // 获取缓存中的选中记录
      this.checkedIds = uni.getStorageSync(CartIdsIndex)
    },

    methods: {
      // 计算合计金额 (根据选中的商品)
      onCalcTotalPrice() {
        const app = this
        // 选中的商品记录
        const checkedList = app.list.filter(item => inArray(item.id, app.checkedIds))
        // 计算总金额
        let tempPrice = 0;
        checkedList.forEach(item => {
          // 商品单价, 为了方便计算先转换单位为分 (整数)
          const unitPrice = item.goodsInfo.price * 100
          tempPrice += unitPrice * item.num
        })
        app.totalPrice = (tempPrice / 100).toFixed(2)
      },

      // 获取购物车商品列表
      getCartList() {
        const app = this
        app.isLoading = true
        CartApi.list()
          .then(result => {
            app.list = result.data.list
            app.total = result.data.totalNum
            // 清除checkedIds中无效的ID
            app.onClearInvalidId()
          })
          .finally(() => app.isLoading = false)
      }, 

      // 清除checkedIds中无效的ID
      onClearInvalidId() {
        const app = this
        const listIds = app.list.map(item => item.id)
        app.checkedIds = arrayIntersect(listIds, app.checkedIds)
      },

      // 切换当前模式
      handleToggleMode() {
        this.mode = this.mode == 'normal' ? 'edit' : 'normal'
      },

      // 监听步进器更改事件
      onChangeStepper({ value }, item) {
        // 这里是组织首次启动时的执行
        if (item.num == value) return
        // 记录一个节流函数句柄
        if (!item.debounceHandle) {
          item.oldValue = item.num
          item.debounceHandle = debounce(this.onUpdateCartNum, 500)
        }
        // 更新商品数量
        item.num = value
        // 提交更新购物车数量 (节流)
        item.debounceHandle(item, item.oldValue, value)
      },

      // 提交更新购物车数量
      onUpdateCartNum(item, oldValue, newValue) {
        const app = this
        CartApi.save(item.goodsId, "=", item.skuId, newValue)
          .then(result => {
            // 更新商品数量
            app.total = result.data.cartTotal
            // 重新计算合计金额
            app.onCalcTotalPrice()
            // 清除节流函数句柄
            item.debounceHandle = null
          })
          .catch(err => {
            // 还原商品数量
            item.num = oldValue
            setTimeout(() => app.$toast(err.errMsg), 10)
          })
      },

      // 跳转到商品详情页
      onTargetGoods(goodsId) {
        this.$navTo('pages/goods/detail', { goodsId })
      },

      // 点击去逛逛按钮, 跳转到首页
      onTargetIndex() {
        this.$navTo('pages/index/index')
      },

      // 选中商品
      handleCheckItem(cartId) {
        const { checkedIds } = this
        const index = checkedIds.findIndex(id => id === cartId)
        index < 0 ? checkedIds.push(cartId) : checkedIds.splice(index, 1)
      },

      // 全选事件
      handleCheckAll() {
        const { checkedIds, list } = this
        this.checkedIds = checkedIds.length === list.length ? [] : list.map(item => item.id)
      },

      // 结算选中的商品
      handleOrder() {
        const app = this
        if (app.checkedIds.length) {
          const cartIds = app.checkedIds.join()
          app.$navTo('pages/settlement/goods', { cartIds })
        }
      },

      // 删除选中的商品弹窗事件
      handleDelete() {
        const app = this
        if (!app.checkedIds.length) {
          return false
        }
        uni.showModal({
          title: '友情提示',
          content: '您确定要删除商品吗？',
          showCancel: true,
          success({ confirm }) {
            // 确认删除
            confirm && app.onClearCart()
          }
        })
      },

      // 确认删除商品
      onClearCart() {
        const app = this
        CartApi.clear(app.checkedIds)
          .then(result => app.getCartList())
      }

    }
  }
</script>

<style>
  page {
    background: #f5f5f5;
  }
</style>
<style lang="scss" scoped>
  // 页面顶部
  .head-info {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 4rpx 30rpx;
    height: 80rpx;

    .cart-total {
      font-size: 35rpx;
      color: #333;

      .active {
        color: #f03c3c;
        margin: 0 2rpx;
      }
    }

    .cart-edit {
      padding-left: 20rpx;

      .icon {
        margin-right: 12rpx;
      }

      .edit {
        color: #f03c3c;
        font-weight: bold;
      }
    }

  }

  // 购物车列表
  .cart-list {
    padding: 0 16rpx 180rpx 16rpx;
  }

  .cart-item {
    background: #fff;
    border-radius: 12rpx;
    display: flex;
    align-items: center;
    padding: 30rpx 16rpx;
    margin-bottom: 24rpx;


    .item-radio {
      width: 56rpx;
      height: 80rpx;
      line-height: 80rpx;
      margin-right: 10rpx;
      text-align: center;

      .radio {
        transform: scale(0.76)
      }
    }

    .goods-image {
      width: 180rpx;
      .image {
        display: block;
        width: 100%;
        height: 160rpx;
        border-radius: 8rpx;
      }
    }

    .item-content {
      flex: 1;
      padding-left: 24rpx;

      .goods-title {
        font-size: 28rpx;
        max-height: 76rpx;
      }

      .goods-props {
        margin-top: 14rpx;
        height: 40rpx;
        color: #ababab;
        font-size: 24rpx;
        overflow: hidden;

        .goods-props-item {
          display: inline-block;
          margin-right: 14rpx;
          padding: 4rpx 16rpx;
          border-radius: 12rpx;
          background-color: #F5F5F5;
          width: auto;
        }
      }


      .item-foot {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-top: 20rpx;

        .goods-price {
          vertical-align: bottom;
          color: $uni-text-color-active;

          .unit {
            font-size: 24rpx;
          }

          .value {
            font-size: 32rpx;
            color: #f03c3c;
            font-weight: bold;
          }
        }
      }

    }
  }

  // 空数据按钮
  .empty-ipt {
    width: 150rpx;
    margin: 0 auto;
    font-size: 28rpx;
    height: 68rpx;
    line-height: 68rpx;
    text-align: center;
    color: #fff;
    border-radius: 5rpx;
    background: linear-gradient(to right, $fuint-theme, $fuint-theme);
  }

  // 底部操作栏
  .footer-fixed {
    display: flex;
    align-items: center;
    height: 180rpx;
    background: #fff;
    padding: 0rpx 0rpx 40rpx 30rpx;
    position: fixed;
    bottom: var(--window-bottom);
    left: 0;
    right: 0;
    z-index: 11;

    .all-radio {
      width: 140rpx;
      display: flex;
      align-items: center;

      .radio {
        margin-bottom: -4rpx;
        transform: scale(0.76)
      }
    }

    .total-info {
      flex: 1;
      display: flex;
      align-items: center;
      justify-content: flex-end;
      padding-right: 30rpx;

      .goods-price {
        vertical-align: bottom;
        color: #fa2209;

        .unit {
          font-size: 24rpx;
        }

        .value {
          font-size: 36rpx;
          color: #f03c3c;
          font-weight: bold;
        }
      }
    }

    .cart-action {
      width: 200rpx;

      .btn-wrapper {
        height: 100%;
        display: flex;
        align-items: center;
      }

      .btn-item {
        flex: 1;
        font-size: 28rpx;
        height: 92rpx;
        line-height: 92rpx;
        text-align: center;
        color: #fff;
      }

      // 立即购买按钮
      .btn-main {
        background: linear-gradient(to right, $fuint-theme, $fuint-theme);
        color: #fff;
        padding-top: 5rpx;
        border-radius: 5rpx;
        margin-right: 20rpx;
        // 禁用按钮
        &.disabled {
          background: #6cb0b0;
        }
      }

    }

  }
</style>
