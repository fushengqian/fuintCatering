<template>
  <view class="container">
    <mescroll-body ref="mescrollRef" :sticky="true" @init="mescrollInit" :down="{ use: true }" @down="downCallback" :up="upOption"
      @up="upCallback">

      <!-- tab栏 -->
      <u-tabs :list="tabs" :is-scroll="false" :current="curTab" active-color="#FA2209" :duration="0.2" @change="onChangeTab" />

      <!-- 卡券列表 -->
      <view class="goods-list">
          <view class="goods-item" v-for="(item, index) in list.content" :key="index">
            <!-- 单列卡券 -->
            <view class="dis-flex" @click="onDetail(item.id, item.type)">
                <!-- 卡券图片 -->
                <view class="goods-item_left">
                  <image class="image" :src="item.image"></image>
                </view>
                <view class="goods-item_right">
                  <!-- 卡券名称 -->
                  <view class="goods-name twolist-hidden">
                    <text>{{ item.name }}</text>
                  </view>
                  <view class="goods-item_desc">
                    <!-- 卡券卖点 -->
                    <view class="desc-selling_point dis-flex">
                      <text class="onelist-hidden">{{ item.tips }}</text>
                    </view>
                    <view class="coupon-attr">
                          <view class="attr-l">
                              <view class="desc-goods_sales dis-flex">
                                <text>{{ item.effectiveDate }}</text>
                              </view>
                              <view v-if="item.amount > 0" class="desc_footer">
                                <text class="price_x">¥{{ item.amount }}</text>
                              </view>
                          </view>
                    </view>
                  </view>
                </view>
              </view>
          </view>
      </view>
    </mescroll-body>
  </view>
</template>

<script>
  import MescrollBody from '@/components/mescroll-uni/mescroll-body.vue'
  import MescrollMixin from '@/components/mescroll-uni/mescroll-mixins'
  import { getEmptyPaginateObj, getMoreListData } from '@/utils/app'
  import * as MyCouponApi from '@/api/myCoupon'
  import { CouponTypeEnum } from '@/common/enum/coupon'
  import Empty from '@/components/empty'

  const color = ['red', 'blue', 'violet', 'yellow']
  const pageSize = 15
  const tabs = [{
    name: `未使用`,
    value: 'A'
  }, {
    name: `已使用`,
    value: 'B'
  }, {
    name: `已过期`,
    value: 'C'
  }]

  export default {
    components: {
      MescrollBody,
      Empty
    },
    mixins: [MescrollMixin],
    data() {
      return {
        // 枚举类
        CouponTypeEnum,
        // 颜色组
        color,
        // 标签栏数据
        tabs,
        // 当前标签索引
        curTab: 0,
        // 卡券类型
        type: "",
        // 优惠券列表数据
        list: getEmptyPaginateObj(),
        // 正在加载
        isLoading: false,
        // 上拉加载配置
        upOption: {
          // 首次自动执行
          auto: true,
          // 每页数据的数量; 默认10
          page: { size: pageSize },
          // 数量要大于4条才显示无更多数据
          noMoreSize: 4,
          // 空布局
          empty: {
            tip: '亲，暂无卡券'
          }
        }
      }
    },

    /**
     * 生命周期函数--监听页面加载
     */
    onLoad(options) {
       let type = options.type !== undefined ? options.type : '';
       this.type = type;
       uni.setNavigationBarTitle({
           title: "我的" + CouponTypeEnum[type].name
       })
    },
    
    onShow() {
      // 获取页面数据
      this.getCouponList(1);
    },

    methods: {
        
      upCallback(page) {
        const app = this
        // 设置列表数据
        app.getCouponList(page.num)
          .then(list => {
            const curPageLen = list.content.length
            const totalSize = list.totalElements
            app.mescroll.endBySize(curPageLen, totalSize)
          })
          .catch(() => app.mescroll.endErr())
      },
      
      // 卡券详情
      onDetail(userCouponId, type) {
          if (type === 'C') {
              this.$navTo(`pages/coupon/detail`, { userCouponId });
          } else if(type === 'T') {
              this.$navTo(`pages/timer/detail`, { userCouponId });
          } else if(type === 'P') {
              this.$navTo(`pages/prestore/detail`, { userCouponId });
          }
      },
      
      /**
       * 获取卡券列表
       */
      getCouponList(pageNo = 1) {
        const app = this;
        return new Promise((resolve, reject) => {
          MyCouponApi.list({ type: app.type, status: app.getTabValue(), page: pageNo }, { load: false })
            .then(result => {
              // 合并新数据
              const newList = result.data;
              app.list.content = getMoreListData(newList, app.list, pageNo);
              resolve(newList);
            })
        })
      },

      // 类型
      getTabValue() {
        return this.tabs[this.curTab].value;
      },

      // 切换标签项
      onChangeTab(index) {
        const app = this;
        // 设置当前选中的标签
        app.curTab = index;
        // 刷新优惠券列表
        app.onRefreshList();
      },

      // 刷新优惠券列表
      onRefreshList() {
        this.list = getEmptyPaginateObj();
        setTimeout(() => {
          this.mescroll.resetUpScroll();
        }, 120)
      }
    }
  }
</script>

<style lang="scss" scoped>
.goods-list {
  padding: 4rpx;
  box-sizing: border-box;

  .goods-item {
    box-sizing: border-box;
    padding: 10rpx;
    height: 260rpx;
    
  .goods-item_left {
    display: flex;
    width: 35%;
    align-items: center;
    background: #fff;
    padding: 20rpx;
    height: 244rpx;
    
    .image {
      display: block;
      border-radius: 5rpx;
      width: 200rpx;
      height: 158rpx;
      border: solid 1rpx #cccccc;
    }
  }
  
  .goods-item_right {
    position: relative;
    width: 65%;
    background: #fff;
  
    .goods-name {
      margin-top: 45rpx;
      height: 45rpx;
      white-space: normal;
      color: #484848;
      font-weight: bold;
      font-size: 30rpx;
    }
  }
  
  .goods-item_desc {
    margin-top: 0rpx;
    .coupon-attr {
       .attr-l {
           float: left;
           width: 100%;
       }
       .attr-r {
           margin-top: 5rpx;
           float:left;
       }
    }
  }
  
  .desc-selling_point {
    width: 400rpx;
    font-size: 24rpx;
    color: #e49a3d;
  }  
  .desc-goods_sales {
    color: #999;
    font-size: 24rpx;
  }
  
  .desc_footer {
    font-size: 24rpx;
  
    .price_x {
      margin-right: 16rpx;
      color: #f03c3c;
      font-size: 30rpx;
    }
  
    .price_y {
      text-decoration: line-through;
    }
  }
 }
 // 空数据按钮
 .empty-ipt {
   width: 220rpx;
   margin: 10rpx auto;
   font-size: 28rpx;
   height: 64rpx;
   line-height: 64rpx;
   text-align: center;
   color: #fff;
   border-radius: 5rpx;
   background: linear-gradient(to right, $fuint-theme, $fuint-theme);
 }
}
</style>
