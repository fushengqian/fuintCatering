<template>
  <view class="page-items">
    <block v-for="(item, index) in items" :key="index">
      <!-- 搜索框 -->
      <block v-if="item.type === 'search'">
        <Search :itemStyle="item.style" :params="item.params" />
      </block>
      <!-- 图片组 -->
      <block v-if="item.type === 'image'">
        <Images :itemStyle="item.style" :params="item.params" :dataList="item.data" />
      </block>
      <!-- 轮播图 -->
      <block v-if="item.type === 'banner'">
        <Banner :itemStyle="item.style" :params="item.params" :dataList="item.data" />
      </block>
      <!-- 图片橱窗 -->
      <block v-if="item.type === 'window'">
        <Window :itemStyle="item.style" :params="item.params" :dataList="item.data" />
      </block>
      <!-- 视频 -->
      <block v-if="item.type === 'video'">
        <Videos :itemStyle="item.style" :params="item.params" />
      </block>
      <!-- 文章组 -->
      <block v-if="item.type === 'article'">
        <Article :params="item.params" :dataList="item.data" />
      </block>
      <!-- 店铺公告 -->
      <block v-if="item.type === 'notice'">
        <Notice :itemStyle="item.style" :params="item.params" />
      </block>
      <!-- 导航 -->
      <block v-if="item.type === 'navBar'">
        <NavBar :itemStyle="item.style" :params="item.params" :dataList="item.data" />
      </block>
      <!-- 商品 -->
      <block v-if="item.type === 'goods'">
        <Goods :itemStyle="item.style" :params="item.params" :dataList="item.data" />
      </block>
      <!-- 在线客服 -->
      <block v-if="item.type === 'service'">
        <Service :itemStyle="item.style" :params="item.params" />
      </block>
      <!-- 辅助空白 -->
      <block v-if="item.type === 'blank'">
        <Blank :itemStyle="item.style" />
      </block>
      <!-- 辅助线 -->
      <block v-if="item.type === 'guide'">
        <Guide :itemStyle="item.style" />
      </block>
      <!-- 富文本 -->
      <block v-if="item.type === 'richText'">
        <RichText :itemStyle="item.style" :params="item.params" />
      </block>
    </block>
  </view>
</template>

<script>
  import Search from './search'
  import Images from './image'
  import Banner from './banner'
  import Window from './window'
  import Videos from './video'
  import Article from './article'
  import Notice from './notice'
  import NavBar from './navBar'
  import Goods from './goods'
  import Service from './service'
  import Blank from './blank'
  import Guide from './guide'
  import RichText from './richText'

  export default {
    name: "Page",
    components: {
      Search,
      Images,
      Banner,
      Window,
      Videos,
      Article,
      Notice,
      NavBar,
      Goods,
      Service,
      Blank,
      Guide,
      RichText
    },
    /**
     * 组件的属性列表
     * 用于组件自定义设置
     */
    props: {
      items: {
        type: Array,
        default () {
          return []
        }
      }
    },
  }
</script>
<style lang="scss">
  // 组件样式
</style>
