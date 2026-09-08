<template>
  <view class="page-items">
    <!-- 吸顶头部：门店信息(location)始终吸顶固定，search 仍保持原逻辑（只在装修顺序开头连续时吸顶）。
         sticky 自身占据文档流位置（不脱离文档流），下方焦点图等内容天然不会被遮挡。
         这里把 location 从 body 中提取出来，确保无论装修顺序怎么排，门店定位都一直吸顶 -->
    <view v-if="stickyLocations.length > 0 || headItems.length > 0" class="page-head-sticky">
      <!-- 门店定位：始终吸顶 -->
      <block v-for="(item, index) in stickyLocations" :key="index">
        <Location :itemStyle="item.style" :storeInfo="storeInfo" />
      </block>
      <!-- 开头连续的搜索框 -->
      <block v-for="(item, index) in headItems" :key="index">
        <Search :itemStyle="item.style" :params="item.params" />
      </block>
    </view>
    <!-- 其余组件正常滚动 -->
    <block v-for="(item, index) in bodyItems" :key="index">
      <!-- 搜索框 -->
      <block v-if="item.type === 'search'">
        <Search :itemStyle="item.style" :params="item.params" />
      </block>
      <!-- 图片组 -->
      <block v-if="item.type === 'image'">
        <Images :itemStyle="item.style" :params="item.params" :dataList="item.dataList" />
      </block>
      <!-- 轮播图 -->
      <block v-if="item.type === 'banner'">
        <Banner :itemStyle="item.style" :params="item.params" :dataList="item.dataList" />
      </block>
      <!-- 视频 -->
      <block v-if="item.type === 'video'">
        <Videos :itemStyle="item.style" :params="item.params" />
      </block>
      <!-- 文章组 -->
      <block v-if="item.type === 'article'">
        <Article :params="item.params" :dataList="item.dataList" />
      </block>
      <!-- 店铺公告 -->
      <block v-if="item.type === 'notice'">
        <Notice :itemStyle="item.style" :params="item.params" />
      </block>
      <!-- 导航 -->
      <block v-if="item.type === 'navBar'">
        <NavBar :itemStyle="item.style" :params="item.params" :dataList="item.dataList" />
      </block>
      <!-- 商品 -->
      <block v-if="item.type === 'goods'">
        <Goods :itemStyle="item.style" :params="item.params" :dataList="item.dataList" />
      </block>
      <!-- 辅助空白 -->
      <block v-if="item.type === 'blank'">
        <Blank :itemStyle="item.style" />
      </block>
      <!-- 富文本 -->
      <block v-if="item.type === 'richText'">
        <RichText :itemStyle="item.style" :params="item.params" />
      </block>
      <!-- 卡券组 -->
      <block v-if="item.type === 'coupon'">
        <Coupon :itemStyle="item.style" :params="item.params" :dataList="item.dataList" />
      </block>
    </block>
  </view>
</template>

<script>
  import Search from './search'
  import Images from './image'
  import Banner from './banner'
  import Videos from './video'
  import Article from './article'
  import Notice from './notice'
  import NavBar from './navBar'
  import Goods from './goods'
  import Blank from './blank'
  import RichText from './richText'
  import Coupon from './coupon'
  import Location from './location'

  export default {
    name: "Page",
    components: {
      Search,
      Images,
      Banner,
      Videos,
      Article,
      Notice,
      NavBar,
      Goods,
      Blank,
      RichText,
      Coupon,
      Location
    },
    /**
     * 组件的属性列表
     * 用于组件自定义设置
     */
    data() {
      return {}
    },

    props: {
      items: {
        type: Array,
        default () {
          return []
        }
      },
      storeInfo: {
        type: Object,
        default: null
      },
      // 上传图片根路径（后端 home 接口返回），用于补全装修组件数据中的相对图片路径
      imagePath: {
        type: String,
        default: ''
      }
    },

    computed: {
      // 将后台装修组件数据结构归一化为各 DIY 渲染组件期望的 props 结构
      renderItems() {
        const app = this
        return app.items.map(item => app.normalizeItem(item)).filter(item => item !== null)
      },
      // 门店定位(location)始终需要吸顶，这里单独提取出来
      stickyLocations() {
        return this.renderItems.filter(item => item.type === 'location')
      },
      // 吸顶头部：组件列表开头连续的搜索(search)整体吸顶固定
      // 注意 location 已由 stickyLocations 单独处理，这里排除掉，避免重复
      headItems() {
        const items = this.renderItems.filter(item => item.type !== 'location')
        if (!items.length) return []
        const head = []
        for (const item of items) {
          if (item.type === 'search') {
            head.push(item)
          } else {
            break
          }
        }
        return head
      },
      // 其余组件正常随页面滚动（location 已全部抽到吸顶头部，body 中不再渲染）
      bodyItems() {
        const items = this.renderItems.filter(item => item.type !== 'location')
        return items.slice(this.headItems.length)
      }
    },

    methods: {

      /**
       * 归一化单个装修组件
       * 后台装修编辑器数据结构：item = { type, style, params, data }
       * data 为对象（banner/navBar/notice/article 等存在 data.list 数组；image/window/video 等为单对象）
       */
      normalizeItem(item) {
        if (!item || !item.type) return null
        const type = item.type
        const style = item.style && typeof item.style === 'object' ? JSON.parse(JSON.stringify(item.style)) : {}
        const params = item.params && typeof item.params === 'object' ? Object.assign({}, item.params) : {}
        const data = item.data && typeof item.data === 'object' ? item.data : {}
        const toList = (value) => (Array.isArray(value) ? value : [])
        let dataList = []

        switch (type) {
          case 'search':
            // 搜索：后台 params.placeholder 与客户端一致
            break
          case 'blank':
            // 空白：后台 style.height/background 与客户端一致
            break
          case 'location':
            // 门店信息：客户端直接使用当前 storeInfo 渲染
            break
          case 'banner':
            // 后台 data.list = [{image, url}]，客户端轮播组件期望数组条目 {image, url}
            dataList = toList(data.list).map(it => ({
              image: this.normalizeImage(it.image),
              url: it.url || ''
            }))
            // 后台轮播间隔单位为 ms（默认 3000），客户端模板按秒 *1000 计算
            if (style.interval && style.interval > 1000) {
              style.interval = style.interval / 1000
            }
            break
          case 'navBar':
            // 后台 data.list = [{iconUrl, name, subtitle, url}]
            dataList = toList(data.list).map(it => ({
              iconUrl: this.normalizeImage(it.iconUrl),
              name: it.name || '',
              tips: it.subtitle || it.tips || '',
              subtitle: it.subtitle || it.tips || '',
              url: it.url || ''
            }))
            // 后台 rowsNum 每行显示数量，未设置时按 4 列兜底
            if (!style.rowsNum || Number(style.rowsNum) <= 0) {
              style.rowsNum = 4
            }
            break
          case 'image':
            // 后台单图 data = {image, url}，客户端单图组件期望数组 [{imgUrl, link}]
            dataList = data.image ? [{ imgUrl: this.normalizeImage(data.image), link: data.url || '' }] : []
            break
          case 'video':
            // 后台 data = {videoUrl, poster}，客户端视频组件从 params 读取
            params.videoUrl = data.videoUrl || ''
            params.poster = this.normalizeImage(data.poster)
            if (params.autoplay === undefined) {
              params.autoplay = 0
            }
            if (!style.height) {
              style.height = 360
            }
            break
          case 'notice':
            // 后台 data.list = [{content, url}]，客户端公告组件读取 params.text/link
            {
              const n = toList(data.list)[0] || {}
              params.text = n.content || ''
              params.link = n.url || ''
              if (params.showIcon === undefined) {
                params.showIcon = true
              }
              if (params.scrollable === undefined) {
                params.scrollable = true
              }
            }
            break
          case 'richText':
            // 后台 data.content，客户端富文本组件读取 params.content
            params.content = data.content || ''
            break
          case 'article':
            // 后台 data.list = [{articleId, title, coverUrl}]，客户端文章组件字段为 article_id/image_url/show_type
            dataList = toList(data.list).map(it => ({
              article_id: it.articleId || it.article_id || 0,
              title: it.title || '',
              image_url: this.normalizeImage(it.coverUrl || it.image_url),
              show_views: it.showViews || it.show_views || 0,
              show_type: it.showType || it.show_type || 20
            }))
            break
          case 'goods':
            // 后台手动选品的商品快照 data.goodsList；无快照时组件自行拉取推荐列表
            dataList = toList(data.goodsList).map(g => {
              const obj = Object.assign({}, g)
              obj.logo = this.normalizeImage(g.logo)
              obj.linePrice = g.linePrice || g.originalPrice || 0
              obj.price = g.price || 0
              obj.initSale = g.initSale || g.saleNum || g.saleNumber || 0
              obj.salePoint = g.salePoint || g.sellingPoint || ''
              return obj
            })
            // 后台装修商品组件的样式字段与客户端 DIY 组件不完全一致，做兜底映射
            if (!style.show || !Array.isArray(style.show) || style.show.length === 0) {
              style.show = ['goodsName', 'goodsPrice', 'linePrice', 'goodsSales']
            }
            // showType: 1=单列, 2=双列；客户端使用 display + column
            if (!style.column) {
              if (String(style.showType) === '1') {
                style.column = 1
              } else if (String(style.showType) === '2') {
                style.column = 2
              } else {
                style.column = 2
              }
            }
            if (!style.display) {
              style.display = 'list'
            }
            if (!style.title) {
              style.title = '热门商品'
            }
            // 未手动选品时不自动加载推荐，避免 mescroll 占满屏幕导致下方组件被推到很远
            params.auto = dataList.length > 0 ? true : false
            break
          case 'coupon':
            // 后台手动选券的快照 data.couponList
            dataList = toList(data.couponList).map(c => {
              const obj = Object.assign({}, c)
              obj.id = c.id || 0
              obj.name = c.name || ''
              obj.type = c.type || 'C'
              obj.amount = c.amount || 0
              obj.discount = c.discount || 0
              obj.minSendAmount = c.minSendAmount || 0
              obj.gotNum = c.gotNum || 0
              obj.leftNum = c.leftNum || 0
              obj.isReceive = c.isReceive === true || c.isReceive === 'Y' || false
              obj.userCouponId = c.userCouponId || 0
              obj.sellingPoint = c.sellingPoint || c.description || ''
              obj.image = this.normalizeImage(c.image || c.logo)
              obj.applyGoods = c.applyGoods || ''
              obj.useFor = c.useFor || ''
              return obj
            })
            if (!style.column) {
              style.column = 1
            }
            if (!style.display) {
              style.display = 'list'
            }
            if (!style.title) {
              style.title = '领券中心'
            }
            break
          case 'window':
          case 'guide':
          case 'service':
            // 这三类后台组件（启动弹窗/新手引导/客服宫格）语义与客户端现有 DIY 组件不一致，
            // 暂不渲染，避免错误展示；后续按装修需求单独实现
            return null
        }

        return { type, style, params, dataList }
      },

      /**
       * 补全图片相对路径
       * 后台装修保存的图片为相对路径（fileName），需拼接上传根路径才能访问
       */
      normalizeImage(path) {
        if (!path) return ''
        if (/^(https?:)?\/\//.test(path) || /^data:image/.test(path)) {
          return path
        }
        const prefix = this.imagePath || ''
        return prefix + path
      }
    }
  }
</script>
<style lang="scss">
  // 吸顶头部：门店信息+搜索框整体吸顶（与默认布局 .index-sticky-header 一致）。
  // sticky 仍占据文档流位置，不会脱离文档流遮挡下方焦点图等内容，无需额外占位
  .page-head-sticky {
    position: sticky;
    top: 0;
    z-index: 100;
  }
</style>
