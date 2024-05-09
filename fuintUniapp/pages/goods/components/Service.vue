<template>
  <view v-if="list.length" class="service-wrapper">
    <!-- 服务简述 -->
    <view class="service-simple" @click="handlePopup">
      <view class="s-list">
        <view class="s-item" v-for="(item, index) in list" :key="index">
          <text class="item-icon iconfont icon-fuwu"></text>
          <text class="item-val">{{ item.name }}</text>
        </view>
      </view>
      <!-- 扩展箭头 -->
      <view class="s-arrow f-26 col-9 t-r">
        <text class="iconfont icon-xiangyoujiantou"></text>
      </view>
    </view>
    <!-- 详情内容弹窗 -->
    <u-popup v-model="showPopup" mode="bottom" :closeable="true" :border-radius="26">
      <view class="service-content">
        <view class="title">服务</view>
        <scroll-view class="content-scroll" :scroll-y="true">
          <view class="s-list clearfix">
            <view class="s-item" v-for="(item, index) in list" :key="index">
              <text class="item-icon iconfont icon-fuwu"></text>
              <view class="item-val">{{ item.name }}</view>
              <view class="item-summary">{{ item.summary }}</view>
            </view>
          </view>
        </scroll-view>
      </view>
    </u-popup>
  </view>
</template>

<script>
  import * as ServiceApi from '@/api/goods/service'

  export default {
    props: {
      // 商品ID
      goodsId: {
        type: Number,
        default: null
      }
    },
    data() {
      return {
        // 正在加载
        isLoading: true,
        // 显示详情内容弹窗
        showPopup: false,
        // 服务列表数据
        list: []
      }
    },

    created() {
      // 获取商品服务列表
      this.getServiceList()
    },

    methods: {

      // 获取商品服务列表
      getServiceList() {
        const app = this
        app.isLoading = true
        ServiceApi.list(app.goodsId)
          .then(result => app.list = result.data.list)
          .finally(() => app.isLoading = false)
      },

      // 显示弹窗
      handlePopup() {
        this.showPopup = !this.showPopup
      }

    }
  }
</script>

<style lang="scss" scoped>
  .service-wrapper {
    min-height: 24rpx;
    margin-bottom: -24rpx;
  }

  // 服务简述
  .service-simple {
    padding: 24rpx 30rpx;
    display: flex;
    align-items: center;

    .s-list {
      flex: 1;
      margin-left: -15rpx;
    }

    .s-item {
      float: left;
      font-size: 26rpx;
      margin: 8rpx 15rpx;

      .item-icon {
        color: #FA2209;
      }

      .item-val {
        margin-left: 12rpx;
      }
    }


  }

  // 服务详细内容
  .service-content {
    padding: 24rpx;

    .title {
      font-size: 30rpx;
      margin-bottom: 50rpx;
      font-weight: bold;
      text-align: center;
    }

    .content-scroll {
      min-height: 400rpx;
      max-height: 750rpx;
    }

    .s-list {
      padding: 0 30rpx 0 80rpx;
    }

    .s-item {
      position: relative;
      margin-bottom: 60rpx;

      .item-icon {
        position: absolute;
        top: 6rpx;
        left: -50rpx;
        color: #FA2209;
      }

      .item-val {
        font-size: 28rpx;
      }

      .item-summary {
        font-size: 26rpx;
        margin-top: 20rpx;
        color: #6d6d6d;
      }
    }

  }
</style>
