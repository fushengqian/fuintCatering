<template>
  <view class="container">
      <view class="success">
        <view v-if="isSuccess" class="result">
           <image class="icon" src='/static/pay/success.png'></image>
           <text class="text">恭喜，支付成功！</text>
        </view>
        <view v-if="!isSuccess" class="result">
           <image class="icon" src='/static/pay/fail.png'></image>
           <text class="text" v-if="message && message != undefined" style="color:#888888;">支付失败：{{ message }}</text>
           <text class="text" v-if="!message || message == undefined" style="color:#888888;">哎呀，支付失败啦~</text>
        </view>
        <view class="options">
            <view class="to-home" @click="toHome()"><text class="iconfont icon-home"></text>返回首页</view>
            <view class="to-order" @click="toOrderInfo()"><text class="iconfont icon-form"></text>查看订单</view>
        </view>
      </view>
      <block>
          <Goods ref="mescrollItem" :itemStyle="goodsStyle" :params="goodsParams"/>
      </block>
  </view>
</template>

<script>
  import Goods from '@/components/page/goods'
  import * as Api from '@/api/page'
  import * as OrderApi from '@/api/order'
  import * as MessageApi from '@/api/message'
  import MescrollCompMixin from "@/components/mescroll-uni/mixins/mescroll-comp.js"
  export default {
    mixins: [MescrollCompMixin],
    components: {
       Goods
    },
    data() {
      return {
        orderId: 0,
        orderInfo: null,
        isLoading: true,
        isSuccess: false,
        message: '',
        goodsStyle: {
                "background": "#F6F6F6",
                "display": "list",
                "column": 1,
                "show": ["goodsName", "goodsPrice", "linePrice", "sellingPoint", "goodsSales"]
        },
        goodsParams: {
                "source": "auto",
                "auto": {
                    "category": 0,
                    "goodsSort": "all",
                    "showNum": 40
                }
        }
      }
    },

    /**
     * 生命周期函数--监听页面加载
     */
    onLoad(options) {
        // 当前页面参数
        this.orderId = options.orderId ? options.orderId : 0
        this.message = options.message ? options.message : ''
        this.getOrderDetail();
    },

    methods: {
      /**
       * 去首页
       * */
      toHome() {
        const app = this
        // #ifdef MP-WEIXIN
        MessageApi.getSubTemplate({keys: "orderCreated,deliverGoods"}).then(result => {
            const templateIds = result.data
            wx.requestSubscribeMessage({tmplIds: templateIds, 
                success(res) {
                    console.log("调用成功！")
                }, fail(res) {
                    console.log("调用失败:", res)
                }, complete() {
                    app.$navTo('pages/index/index')
                }
            })
        })
        // #endif
        // #ifndef MP-WEIXIN
           app.$navTo('pages/index/index')
        // #endif
      },
      
      /**
       * 去订单详情
       * */
      toOrderInfo() {    
        const app = this
        // #ifdef MP-WEIXIN
        MessageApi.getSubTemplate({keys: "orderCreated,deliverGoods"}).then(result => {
            const templateIds = result.data
            wx.requestSubscribeMessage({tmplIds: templateIds, 
                success(res) {
                    console.log("调用成功！")
                }, fail(res) {
                    console.log("调用失败:", res)
                }, complete() {
                    app.$navTo('pages/order/detail?orderId=' + app.orderId)
                }
            })
        })
        // #endif
        // #ifndef MP-WEIXIN
           app.$navTo('pages/order/detail?orderId=' + app.orderId)
        // #endif
      },
      getOrderDetail() {
        const app = this
        app.isLoading = true
        OrderApi.detail(app.orderId)
          .then(result => {
              app.isSuccess = result.data.payStatus === 'B' ? true : false;
              app.isLoading = false;
          })
      },
    }
  }
</script>

<style lang="scss" scoped>
      .success {
        width: 100%;
        text-align: center;
        margin-top: 60rpx;
        margin-bottom: 80rpx;
        .result {
            font-size: 35rpx;
            text-align: center;
            padding: 10rpx;
            height: 70rpx;
            .icon {
                width: 55rpx;
                height: 55rpx;
                display: inline-block;
                box-sizing: border-box;
                vertical-align: middle; 
            }
            .text {
                text-align: center;
                height: 100%;
                display: inline-block;
                box-sizing: border-box;
                vertical-align: middle;
                color: #00B83F;
                margin-left: 10rpx;
                font-weight: bold;
            }
        }
        .options {
            margin-top: 0rpx;
            text-align: center;
            display: flex;
            align-items: center;
            flex-direction:row;
            padding: 50rpx 100rpx 60rpx 100rpx;
            .to-home,.to-order {
              margin: 0 auto;
              font-size: 28rpx;
              height: 72rpx;
              line-height: 72rpx;
              text-align: center;
              color: #888;
              border-radius: 72rpx;
              width: 240rpx;
              background: #fff;
              border: solid 1rpx #888;
              float: left;
            }
            .iconfont {
                font-weight: bold;
                margin-right: 5rpx;
            }
        }
      }
      .attention {
        width: 100%;
        text-align: center;
        margin-top: 14rpx;
      }
</style>
