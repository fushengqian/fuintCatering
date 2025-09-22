<template>
  <mescroll-body ref="mescrollRef" :sticky="true" @init="mescrollInit" :down="{ native: true }" @down="downCallback"
    :up="upOption" @up="upCallback">

    <!-- 分类列表tab -->
    <view class="tabs-wrapper">
      <scroll-view class="scroll-view" scroll-x>
        <view class="tab-item" :class="{ active: curId ==  0 }" @click="onSwitchTab(0)">
          <view class="value"><text>全部</text></view>
        </view>
        <!-- 分类列表 -->
        <view class="tab-item" :class="{ active: curId ==  item.id }" @click="onSwitchTab(item.id)"
          v-for="(item, index) in categoryList" :key="index">
          <view class="value"><text>{{ item.name }}</text></view>
        </view>
      </scroll-view>
    </view>
    
    <view class="search-wrapper">
      <view class="search-input">
        <view class="search-input-wrapper">
          <view class="left">
            <text class="search-icon iconfont icon-sousuo"></text>
          </view>
          <view class="right">
            <input v-model="keyword" class="input" placeholder="请输入会员手机号 / 会员号" type="text"></input>
          </view>
        </view>
      </view>
      <view class="search-button">
        <button class="button" @click="doSearch" type="warn"> 搜索 </button>
      </view>
    </view>

    <!-- 会员列表 -->
    <view class="member-list" v-if="memberList.content">
      <view class="member-item" v-for="(item, index) in memberList.content" :key="index" @click="onTargetDetail(item.id)">
        <block>
          <view class="left flex-box">
            <image class="image" :src="item.avatar"></image>
          </view>
          <view class="right">
            <view class="base">
              <text class="name">{{ item.name }}</text>
              <text class="grade">{{ item.gradeName ? item.gradeName : '' }}</text>
            </view>
            <view class="amount">
              <view class="balance">余额：￥{{ item.balance.toFixed(2) }}</view>
              <view class="point">积分：{{ item.point }}</view>
            </view>
            <view class="footer">
              <text class="member-views f-24 col-8">{{ item.lastLoginTime }}活跃</text>
            </view>
          </view>
        </block>
      </view>
    </view>
  </mescroll-body>
</template>

<script>
  import MescrollBody from '@/components/mescroll-uni/mescroll-body.vue'
  import MescrollMixin from '@/components/mescroll-uni/mescroll-mixins'
  import * as MemberApi from '@/api/merchant/member'
  import { getEmptyPaginateObj, getMoreListData } from '@/utils/app'

  const pageSize = 15

  export default {
    components: {
      MescrollBody
    },
    mixins: [MescrollMixin],
    data() {
      return {
        // 分类列表
        categoryList: [{ name : '今日活跃', id: 'todayActive' }, { name : '今日注册', id: 'todayRegister' }],
        // 会员列表
        memberList: getEmptyPaginateObj(),
        // 当前选中的分类id (all则代表全部)
        curId: 'all',
        // 搜索关键字
        keyword: '',
        // 上拉加载配置
        upOption: {
          // 首次自动执行
          auto: true,
          // 每页数据的数量; 默认20
          page: { size: pageSize },
          // 数量要大于12条才显示无更多数据
          noMoreSize: 12,
          empty: {
            tip: '亲，暂无数据'
          }
        }
      }
    },

    /**
     * 生命周期函数--监听页面加载
     */
    onLoad(options) {
      const app = this
      if (options.dataType) {
          app.curId = options.dataType
      }
    },

    methods: {

      /**
       * 上拉加载的回调 (页面初始化时也会执行一次)
       * 其中page.num:当前页 从1开始, page.size:每页数据条数,默认20
       * @param {Object} page
       */
      upCallback(page) {
        const app = this
        // 设置列表数据
        app.getMemberList(page.num)
          .then(list => {
            const curPageLen = list.content.length;
            const totalSize = list.totalElements;
            app.mescroll.endBySize(curPageLen, totalSize);
          })
          .catch(() => app.mescroll.endErr())
      },
      
      /**
       * 搜索提交
       */
      doSearch() {
        this.curId = 'all';
        // 刷新列表数据
        this.mescroll.resetUpScroll();
      },

      /**
       * 获取会员列表
       * @param {Number} pageNo 页码
       */
      getMemberList(pageNo = 1) {
        const app = this;
        return new Promise((resolve, reject) => {
          MemberApi.list({ dataType: app.curId, keyword: app.keyword, page: pageNo }, { load: false })
            .then(result => {
              // 合并新数据
              const newList = result.data.paginationResponse;
              app.memberList.content = getMoreListData(newList, app.memberList, pageNo);
              resolve(newList);
            })
            .catch(result => reject())
        })
      },

      // 切换选择的分类
      onSwitchTab(dataType = 'all') {
        const app = this;
        // 切换当前的分类ID
        app.curId = dataType;
        app.keyword = '';
        // 刷新列表数据
        app.mescroll.resetUpScroll();
      },

      // 跳转会员详情页
      onTargetDetail(memberId) {
        this.$navTo('merchantPages/member/detail', { memberId })
      },
      
      // 刷新列表
      onRefreshList() {
        this.list = getEmptyPaginateObj()
        setTimeout(() => {
          this.mescroll.resetUpScroll()
        }, 120)
      },
    }

  }
</script>

<style lang="scss" scoped>
  /* 顶部选项卡 */
  .container {
    min-height: 100vh;
  }
  .tabs-wrapper {
    position: sticky;
    display: flex;
    width: 100%;
    height: 88rpx;
    color: #333;
    font-size: 28rpx;
    background: #fff;
    border-bottom: 1rpx solid #e4e4e4;
    z-index: 100;
    overflow: hidden;
    white-space: nowrap;
  }

  .tab-item {
    display: inline-block;
    padding: 0 15rpx;
    text-align: center;
    min-width: 20%;
    height: 87rpx;
    line-height: 88rpx;
    box-sizing: border-box;
    .value {
      height: 100%;
    }
    &.active .value {
      color: #fd4a5f;
      border-bottom: 4rpx solid #fd4a5f;
    }
  }
  
  .search-wrapper {
    display: flex;
    height: 80rpx;
    margin-top: 10rpx;
    padding: 0rpx 10rpx;
  }
  
  // 搜索输入框
  .search-input {
    width: 80%;
    background: #fff;
    border-radius: 50rpx 0 0 50rpx;
    box-sizing: border-box;
    overflow: hidden;
    border: solid 1px #cccccc;
    line-height: 80rpx;
    .search-input-wrapper {
      display: flex;
      .left {
        display: flex;
        width: 60rpx;
        justify-content: center;
        align-items: center;
        .search-icon {
          display: block;
          color: #666666;
          font-size: 30rpx;
          font-weight: bold;
        }
      }
  
      .right {
        flex: 1;
  
        input {
          font-size: 28rpx;
          height: 80rpx;
          line-height: 80rpx;
          .input-placeholder {
            color: #aba9a9;
          }
        }
  
      }
    }
  }
  
  // 搜索按钮
  .search-button {
    width: 20%;
    box-sizing: border-box;
  
    .button {
      line-height: 78rpx;
      height: 78rpx;
      font-size: 28rpx;
      border-radius: 0 20px 20px 0;
      background: $fuint-theme;
    }
  }
  

  /* 会员列表 */
  .member-list {
    padding-top: 10rpx;
    line-height: 1;
    background: #f7f7f7;
  }
  
  .member-item {
    margin-bottom: 10rpx;
    padding: 30rpx;
    background: #fff;
    border: #f5f5f5 solid 1rpx;
    height: 188rpx;

    &:last-child {
      margin-bottom: 0;
    }
    .left {
        width: 320rxp;
        float: left;
        .image {
          display: block;
          width: 120rpx;
          height: 120rpx;
          border-radius: 120rpx;
          border: solid 1rpx #cccccc;
        }
    }
    .right {
        margin-left: 140rpx;
        height: 180rpx;
        .base {
            .name {
              font-weight: bold;
              max-height: 80rpx;
              font-size: 30rpx;
              color: #333;
            }
            .grade {
                margin-left: 20rpx;
                float: right;
            }
        }
        .amount {
            margin-top: 10rpx;
            .balance {
               margin-top: 15rpx;
            }
            .point {
               margin-top: 10rpx;
            }
        }
        .footer {
            margin-top: 20rpx;
            .member-views {
                float: right;
            }
        }
    }
  }

</style>
