<template>
  <view class="pre-store-popup popup" catchtouchmove="true" :class="(value && complete) ? 'show' : 'none'"
    @touchmove.stop.prevent="moveHandle">
    <!-- 页面内容开始 -->
    <view class="mask" @click="close('mask')"></view>
    <!-- 页面开始 -->
    <view class="layer attr-content" :style="'border-radius: 10rpx 10rpx 0 0;'">
      <view class="specification-wrapper">
        <scroll-view class="specification-wrapper-content" scroll-y="true">
          <view class="specification-header">
            <view class="specification-name">{{couponInfo.name}}</view>
          </view>
          <view class="specification-content">
            <view v-for="(item, index) in storeRule" :key="index" class="store-item">
              <view style="flex: 4;">
                <view class="item-rule">预存￥{{ item.store }} 到账 ￥{{ item.upStore }}</view>
              </view>
              <view style="flex: 1;text-align: right;">
                <number-box :min="minBuyNum" :max="maxBuyNum" :step="stepBuyNum" v-model="selectNum[index]"
                  :positive-integer="true">
                </number-box>
              </view>
            </view>
          </view>
        </scroll-view>
        <view class="close" @click="close('close')" v-if="showClose">
          <image class="close-item" :src="closeImage"></image>
        </view>
      </view>

      <view v-if="noStock" class="btn-wrapper">
        <view class="sure" style="color:#ffffff;background-color:#cccccc">库存没有了</view>
      </view>
      <view class="btn-wrapper">
        <view class="sure" @click="buyNow">立即预存</view>
      </view>
      <!-- 页面结束 -->
    </view>
    <!-- 页面内容结束 -->
  </view>
</template>

<script>
  import NumberBox from './number-box'

  var that; // 当前页面对象
  var vk; // 自定义函数集
  export default {
    name: 'CouponPopup',
    components: {
      NumberBox
    },
    props: {
      // true 组件显示 false 组件隐藏
      value: {
        Type: Boolean,
        default: false
      },
      // vk云函数路由模式参数开始-----------------------------------------------------------
      // 卡券信息
      couponInfo: {
        Type: Object,
        default: {}
      },
      // 预存规则
      storeRule: {
          type: Array,
          default: []
      },
      // vk云函数路由模式参数结束-----------------------------------------------------------
      // 点击遮罩是否关闭组件 true 关闭 false 不关闭 默认true
      maskCloseAble: {
        Type: Boolean,
        default: true
      },
      // 最小购买数量
      minBuyNum: {
        Type: Number,
        default: 0
      },
      // 最大购买数量
      maxBuyNum: {
        Type: Number,
        default: 1000
      },
      // 每次点击后的数量
      stepBuyNum: {
        Type: Number,
        default: 1
      },
      // 是否显示右上角关闭按钮
      showClose: {
        Type: Boolean,
        default: true
      },
      // 关闭按钮的图片地址
      closeImage: {
        Type: String,
        default: "https://img.alicdn.com/imgextra/i1/121022687/O1CN01ImN0O11VigqwzpLiK_!!121022687.png"
      }
    },
    data() {
      return {
        complete: false, // 组件是否加载完成
        isShow: false, // true 显示 false 隐藏
        noStock: false,
        selectNum: [], // 选中数量
      };
    },
    mounted() {
      that = this;
    },
    created() {
        const app = this
        this.storeRule.forEach(function(){
            app.selectNum.push(0)
        })
    },
    methods: {
      // 初始化
      init() {
         //empty
      },
      async open() {
        that.complete = true;
        that.$emit("open", true);
        that.$emit("input", true);
      },
      // 监听 - 弹出层收起
      close(s) {
        if (s == "close") {
          that.$emit("input", false);
          that.$emit("close", "close");
        } else if (s == "mask") {
          if (that.maskCloseAble) {
            that.$emit("input", false);
            that.$emit("close", "mask");
          }
        }
      },
      
      moveHandle() {
        //禁止父元素滑动
      },
      
      // 立即预存
      buyNow() {
        let selected = "";
        let allZero = true;
        this.selectNum.forEach(function(num) {
            if (num > 0) {
                allZero = false;
            }
            if (selected.length === 0) {
                selected = num
            } else {
                selected = selected + "," + num;
            }
        })
        
        if (allZero) {
            this.$error("预存数量必须大于1");
            return false
        }
        
        this.$navTo('pages/settlement/index', { couponId: this.couponInfo.id, selectNum: selected })
      },
      // 弹窗
      toast(title, icon) {
        uni.showToast({
          title: title,
          icon: icon
        });
      }
    },
    // 计算属性
    computed: {
       // empty
    },
    watch: {
      value: function(val) {
        if (val) {
          that.open();
        }
      },
    }
  };
</script>

<style lang="scss" scoped>
  .pre-store-popup {
    position: fixed;
    left: 0;
    top: 0;
    right: 0;
    bottom: 0;
    z-index: 21;
    overflow: hidden;

    &.show {
      display: block;

      .mask {
        animation: showPopup 0.2s linear both;
      }

      .layer {
        animation: showLayer 0.2s linear both;
        padding-bottom: 50rpx;
      }
    }

    &.hide {
      .mask {
        animation: hidePopup 0.2s linear both;
      }

      .layer {
        animation: hideLayer 0.2s linear both;
      }
    }

    &.none {
      display: none;
    }

    .mask {
      position: fixed;
      top: 0;
      width: 100%;
      height: 100%;
      z-index: 1;
      background-color: rgba(0, 0, 0, 0.65);
    }

    .layer {
      display: flex;
      width: 100%;
      flex-direction: column;
      position: fixed;
      z-index: 99;
      bottom: 0;
      border-radius: 10rpx 10rpx 0 0;
      background-color: #fff;

      .specification-wrapper {
        width: 100%;
        padding: 30rpx 25rpx;
        box-sizing: border-box;
        background: #ffffff;

        .specification-wrapper-content {
          width: 100%;
          max-height: 900rpx;
          min-height: 300rpx;

          &::-webkit-scrollbar {
            /*隐藏滚轮*/
            display: none;
          }

          .specification-header {
            width: 100%;
            display: flex;
            flex-direction: row;
            position: relative;
            margin-bottom: 40rpx;
            .specification-name{
                font-weight: bold;
            }
          }

          .specification-content {
            font-weight: 500;
            font-size: 26rpx;
            .store-item {
                display: flex;
                height: 100rpx;
                padding-top:30rpx;
                cursor:pointer;
                .item-rule {
                    padding: 10rpx;
                    border: solid 1px #f03c3c;
                    border-radius: 10rpx;
                    width: 400rpx;
                    text-align: center;
                    background: #f9211c;
                    color: #ffffff;
                }
            }
          }
        }

        .close {
          position: absolute;
          top: 30rpx;
          right: 25rpx;
          width: 50rpx;
          height: 50rpx;
          text-align: center;
          line-height: 50rpx;

          .close-item {
            width: 40rpx;
            height: 40rpx;
          }
        }
      }

      .btn-wrapper {
        display: flex;
        width: 100%;
        height: 120rpx;
        flex: 0 0 120rpx;
        align-items: center;
        justify-content: space-between;
        padding: 0 26rpx;
        box-sizing: border-box;

        .layer-btn {
          width: 335rpx;
          height: 76rpx;
          border-radius: 38rpx;
          color: #fff;
          line-height: 76rpx;
          text-align: center;
          font-weight: 500;
          font-size: 28rpx;

          &.add-cart {
            background: #ffbe46;
          }

          &.buy {
            background: #fe560a;
          }
        }

        .sure {
          width: 698rpx;
          height: 80rpx;
          border-radius: 40rpx;
          color: #fff;
          line-height: 80rpx;
          text-align: center;
          font-weight: 500;
          font-size: 28rpx;
          background:linear-gradient(to right, #f9211c, #ff6335)
        }

        .sure.add-cart {
          background: #ff9402;
        }
      }
    }

    @keyframes showPopup {
      0% {
        opacity: 0;
      }

      100% {
        opacity: 1;
      }
    }

    @keyframes hidePopup {
      0% {
        opacity: 1;
      }

      100% {
        opacity: 0;
      }
    }

    @keyframes showLayer {
      0% {
        transform: translateY(120%);
      }

      100% {
        transform: translateY(0%);
      }
    }

    @keyframes hideLayer {
      0% {
        transform: translateY(0);
      }

      100% {
        transform: translateY(120%);
      }
    }
  }
</style>
