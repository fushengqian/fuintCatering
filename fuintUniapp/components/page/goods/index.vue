iq<template>
  <!-- 商品列表 -->
  <view class="goods-container">
      <view class="recommend" v-if="showTitle">
         <text class="txt">{{ itemStyle.title || '为您推荐' }}</text>
      </view>
      <!-- 装修快照模式：直接渲染静态列表，不占满屏幕高度 -->
      <view v-if="staticMode" class="diy-goods" :style="{ background: itemStyle.background }">
        <goods-list-body :itemStyle="itemStyle" :list="list.content" @click="onTargetGoods" />
      </view>
      <!-- 普通推荐模式：使用 mescroll 分页加载 -->
      <mescroll-body v-else-if="showMescroll" ref="mescrollRef" :sticky="true" @init="mescrollInit" :down="{ native: true }" @down="downCallback" :up="upOption" @up="upCallback">
        <view class="diy-goods" :style="{ background: itemStyle.background }">
          <goods-list-body :itemStyle="itemStyle" :list="list.content" @click="onTargetGoods" />
        </view>
      </mescroll-body>
  </view>
</template>

<script>
  import MescrollBody from '@/components/mescroll-uni/mescroll-body.vue'
  import MescrollMixin from '@/components/mescroll-uni/mescroll-mixins'
  import { getEmptyPaginateObj, getMoreListData } from '@/utils/app'
  import * as GoodsApi from '@/api/goods'
  import GoodsListBody from './goods-list-body.vue'

  const pageSize = 10;

  export default {
    name: "Goods",

    /**
     * 组件的属性列表
     * 用于组件自定义设置
     */
    props: {
      itemIndex: String,
      itemStyle: Object,
      params: Object,
      isReflash: Boolean,
      // 装修模式下后台手动选品的商品快照列表；为空时组件自行分页拉取推荐商品
      dataList: {
        type: Array,
        default () {
          return []
        }
      }
    },

    components: {
      MescrollBody,
      GoodsListBody
    },

    mixins: [MescrollMixin],

    computed: {
      showTitle() {
        return (this.list.content && this.list.content.length > 0) || (this.dataList && this.dataList.length > 0)
      },
      showMescroll() {
        return !this.staticMode && (this.params && this.params.auto !== false)
      }
    },

    data() {
      return {
        list: getEmptyPaginateObj(),
        // 是否处于装修快照模式（直接渲染后台选中的商品，不走分页接口）
        staticMode: false,
        // 上拉加载配置
        upOption: {
          // 首次自动执行
          auto: true,
          // 每页数据的数量; 默认10
          page: { size: pageSize },
          // 数量要大于1条才显示无更多数据
          noMoreSize: 1,
        }
      }
    },

    created() {
      this.applyStaticList()
    },

    watch: {
      dataList: {
        deep: true,
        handler() {
          this.applyStaticList()
        }
      },
      isReflash(value) {
         if (value) {
             this.getGoodsList(1);
         }
      }
    },

    /**
     * 组件的方法列表
     * 更新属性和数据的方法与更新页面数据的方法类似
     */
    methods: {

      /**
       * 应用装修快照列表：有后台选品快照时直接渲染，否则保持分页拉取模式
       */
      applyStaticList() {
        const app = this
        const staticList = Array.isArray(app.dataList) ? app.dataList : []
        app.staticMode = staticList.length > 0
        app.list.content = app.staticMode ? staticList : []
        // 默认首页：staticMode=false 且 params.auto 不为 false 时自动加载推荐商品
        // 装修页面：未选品时 params.auto=false，避免 mescroll 占满屏幕
        const autoLoad = !app.staticMode && (app.params && app.params.auto !== false)
        app.upOption.auto = autoLoad
        if (app.staticMode) {
          // 快照模式下无需分页加载
          if (app.mescroll) {
            app.mescroll.endBySize(staticList.length, staticList.length)
          }
        }
      },

      /**
       * 跳转商品详情页
       */
      onTargetGoods(goodsId) {
        this.$navTo(`pages/goods/detail`, { goodsId })
      },

      /**
       * 上拉加载的回调 (页面初始化时也会执行一次)
       * 其中page.num:当前页 从1开始, page.size:每页数据条数,默认10
       * @param {Object} page
       */
      upCallback(page) {
        const app = this
        // 装修快照模式下不请求分页接口
        if (app.staticMode) {
          const len = app.list.content.length
          if (app.mescroll) {
            app.mescroll.endBySize(len, len)
          }
          return
        }
        // 设置列表数据
        app.getGoodsList(page.num)
          .then(list => {
              const curPageLen = list.content.length;
              const totalSize = list.totalElements;
              app.mescroll.endBySize(curPageLen, totalSize);
          })
          .catch(() => {
              app.mescroll.endErr();
          })
      },

      /**
       * 获取商品列表
       * @param {number} pageNo 页码
       */
      getGoodsList(pageNo) {
        const app = this
        console.log('pageNo=====', pageNo);
        const param = { page: pageNo, pageSize: pageSize }
        return new Promise((resolve, reject) => {
          GoodsApi.search(param)
            .then(result => {
              // 合并新数据
              const newList = result.data;
              app.list.content = getMoreListData(newList, app.list, pageNo)
              resolve(newList)
            })
            .catch(reject)
        })
      }
    }
  }
</script>
<style lang="scss" scoped>
  .goods-container {
      .recommend {
        font-size: 30rpx;
        font-weight: bold;
        margin-left: 20rpx;
        margin-right: 20rpx;
        padding: 20rpx 8rpx 20rpx 8rpx;
        background: #f5f5f5;
        .txt {
          border-left: solid $fuint-theme 10rpx;
          padding-left: 10rpx;
        }
      }
  }
</style>
