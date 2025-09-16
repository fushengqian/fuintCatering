<template>
  <mescroll-body ref="mescrollRef" :sticky="true" @init="mescrollInit" :down="{ native: true }" @down="downCallback"
    :up="upOption" @up="upCallback">
    
    <view class="search-wrapper">
      <view class="search-input">
        <view class="search-input-wrapper">
          <view class="left">
            <text class="search-icon iconfont icon-sousuo"></text>
          </view>
          <view class="right">
            <input v-model="keyword" class="input" placeholder="请输入员工手机号 / 姓名" type="text"></input>
          </view>
        </view>
      </view>
      <view class="search-button">
        <button class="button" @click="doSearch" type="warn"> 搜索 </button>
      </view>
    </view>

    <!-- 员工列表 -->
    <view class="staff-list" v-if="staffList.content">
      <view class="staff-item" v-for="(item, index) in staffList.content" :key="index" @click="onTargetDetail(item.id)">
        <block>
          <view class="left flex-box">
            <image class="image" src="/static/default-avatar.png"></image>
          </view>
          <view class="right">
            <view class="base">
              <text class="name">{{ item.realName }}</text>
              <text class="mobile">{{ item.mobile ? item.mobile : '' }}</text>
            </view>
            <view class="footer">
              <text class="staff-views f-24 col-8">创建时间：{{ item.createTime | timeFormat('yyyy-mm-dd hh:MM') }}</text>
            </view>
          </view>
        </block>
      </view>
    </view>
    
    <view class="footer-fixed">
      <view class="footer-container">
        <view class="foo-item-btn">
          <view class="btn-wrapper">
            <view class="btn-item btn-item-main" @click="onAddStaff()">
              <text>新增员工</text>
            </view>
          </view>
        </view>
      </view>
    </view>
    
  </mescroll-body>
</template>

<script>
  import MescrollBody from '@/components/mescroll-uni/mescroll-body.vue'
  import MescrollMixin from '@/components/mescroll-uni/mescroll-mixins'
  import * as StaffApi from '@/api/merchant/staff'
  import { getEmptyPaginateObj, getMoreListData } from '@/utils/app'

  const pageSize = 15

  export default {
    components: {
      MescrollBody
    },
    mixins: [MescrollMixin],
    data() {
      return {
        // 员工列表
        staffList: getEmptyPaginateObj(),
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

    methods: {

      /**
       * 上拉加载的回调 (页面初始化时也会执行一次)
       * 其中page.num:当前页 从1开始, page.size:每页数据条数,默认20
       * @param {Object} page
       */
      upCallback(page) {
        const app = this
        // 设置列表数据
        app.getStaffList(page.num)
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
        // 刷新列表数据
        this.mescroll.resetUpScroll();
      },

      /**
       * 获取员工列表
       * @param {Number} pageNo 页码
       */
      getStaffList(pageNo = 1) {
        const app = this;
        return new Promise((resolve, reject) => {
          StaffApi.getStaffList({ keyword: app.keyword, page: pageNo }, { load: false })
            .then(result => {
              // 合并新数据
              const newList = result.data;
              app.staffList.content = getMoreListData(newList, app.staffList, pageNo);
              resolve(newList);
            })
            .catch(result => reject())
        })
      },

      // 切换选择的分类
      onSwitchTab(dataType = 'all') {
        const app = this;
        app.keyword = '';
        // 刷新列表数据
        app.mescroll.resetUpScroll();
      },
      
      // 跳转新增
      onAddStaff() {
        this.$navTo('pages/merchant/staff/add')
      },

      // 跳转编辑
      onTargetDetail(staffId) {
        this.$navTo('pages/merchant/staff/edit', { staffId })
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

  /* 员工列表 */
  .staff-list {
    padding-top: 10rpx;
    line-height: 1;
    background: #f7f7f7;
  }
  
  .staff-item {
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
            margin-top: 10rpx;
            .name {
              font-weight: bold;
              max-height: 80rpx;
              font-size: 30rpx;
              color: #333;
            }
            .mobile {
                margin-left: 20rpx;
                float: right;
            }
        }
        .footer {
            margin-top: 80rpx;
            .staff-views {
                float: right;
            }
        }
    }
  }
    
    /* 底部操作栏 */
    .footer-fixed {
      position: fixed;
      bottom: var(--window-bottom);
      left: 0;
      right: 0;
      display: flex;
      height: 180rpx;
      z-index: 11;
      box-shadow: 0 -4rpx 40rpx 0 rgba(144, 52, 52, 0.1);
      background: #fff;
    }
    
    .footer-container {
      width: 100%;
      display: flex;
      margin-bottom: 40rpx;
    }
    
    // 操作按钮
    .foo-item-btn {
      flex: 1;
      .btn-wrapper {
        height: 100%;
        display: flex;
        align-items: center;
      }
      .btn-item {
        flex: 1;
        font-size: 28rpx;
        height: 80rpx;
        line-height: 80rpx;
        margin-right: 16rpx;
        margin-left: 16rpx;
        text-align: center;
        color: #fff;
        border-radius: 40rpx;
      }
      .btn-item-main {
        background: linear-gradient(to right, #f9211c, #ff6335);
      }
    }

</style>
