<template>
  <view class="container">
    <!-- 顶部选项卡 -->
    <view class="tab-wrapper">
      <view
        v-for="(tab, index) in tabs"
        :key="index"
        class="tab-item"
        :class="{ 'tab-active': currentTab === tab.value }"
        @click="onTabChange(tab.value)"
      >
        {{ tab.label }}
      </view>
    </view>

    <!-- 邀请列表 -->
    <mescroll-body
      ref="mescrollRef"
      :sticky="true"
      @init="mescrollInit"
      :down="{ use: false }"
      :up="upOption"
      @up="upCallback"
    >
      <view class="invite-list">
        <view v-for="(item, index) in list.content" :key="index" class="invite-item">
          <view class="invite-item-left">
            <image class="avatar" :src="item.subUserInfo.avatar || '/static/default-avatar.png'" mode="aspectFill" />
            <view class="invite-info">
              <view class="name">{{ item.subUserInfo.name || '--' }}</view>
              <view class="no">会员号：{{ item.subUserInfo.userNo || '--' }}</view>
            </view>
          </view>
          <view class="invite-item-right">
            <view class="level-tag" :class="item.level == 1 ? 'level-one' : 'level-two'">
              {{ item.level == 1 ? '一级' : '二级' }}
            </view>
            <view class="time">{{ item.createTime | timeFormat('yyyy-mm-dd hh:MM') }}</view>
          </view>
        </view>
      </view>
    </mescroll-body>
  </view>
</template>

<script>
  import MescrollBody from '@/components/mescroll-uni/mescroll-body.vue'
  import MescrollMixin from '@/components/mescroll-uni/mescroll-mixins'
  import * as CommissionApi from '@/api/commission'
  import { getEmptyPaginateObj, getMoreListData } from '@/utils/app'

  const pageSize = 15

  export default {
    components: {
      MescrollBody
    },
    mixins: [MescrollMixin],
    data() {
      return {
        // 当前选中的tab
        currentTab: '',
        // tab列表
        tabs: [
          { label: '一级邀请', value: 1 },
          { label: '二级邀请', value: 2 }
        ],
        // 邀请列表
        list: getEmptyPaginateObj(),
        // 上拉加载配置
        upOption: {
          auto: true,
          page: { size: pageSize },
          noMoreSize: 3,
          empty: {
            tip: '暂无邀请记录'
          }
        }
      }
    },
    onLoad() {
      // 默认切换到一级tab
      this.currentTab = this.tabs[0].value
    },
    methods: {
      /**
       * 切换tab
       */
      onTabChange(value) {
        if (this.currentTab === value) return
        this.currentTab = value
        this.list = getEmptyPaginateObj()
        this.mescroll.resetUpScroll()
      },

      /**
       * 上拉加载回调
       */
      upCallback(page) {
        const app = this
        app.getInviteList(page.num)
          .then(list => {
            const curPageLen = list.content ? list.content.length : 0
            const totalSize = list.totalElements || 0
            app.mescroll.endBySize(curPageLen, totalSize)
          })
          .catch(() => app.mescroll.endErr())
      },

      /**
       * 获取邀请列表
       */
      getInviteList(pageNo = 1) {
        const app = this
        return new Promise((resolve, reject) => {
          CommissionApi.inviteList({
            page: pageNo,
            level: String(app.currentTab)
          })
            .then(result => {
              const newList = result.data.paginationResponse
              app.list.content = getMoreListData(newList, app.list, pageNo)
              resolve(newList)
            })
            .catch(() => reject())
        })
      }
    }
  }
</script>

<style lang="scss" scoped>
  .container {
    min-height: 100vh;
    background: #f7f7f7;
  }

  /* 顶部选项卡 */
  .tab-wrapper {
    display: flex;
    width: 100%;
    height: 88rpx;
    background: #fff;
    position: sticky;
    top: 0;
    z-index: 100;
  }

  .tab-item {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 28rpx;
    color: #666;
    position: relative;
  }

  .tab-active {
    color: #113a28;
    font-weight: bold;
    &::after {
      content: '';
      position: absolute;
      bottom: 0;
      left: 50%;
      transform: translateX(-50%);
      width: 60rpx;
      height: 6rpx;
      background: #113a28;
      border-radius: 3rpx;
    }
  }

  /* 邀请列表 */
  .invite-list {
    padding-top: 10rpx;
  }

  .invite-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 24rpx 30rpx;
    margin-bottom: 10rpx;
    background: #fff;
  }

  .invite-item-left {
    display: flex;
    align-items: center;
    flex: 1;
    min-width: 0;
  }

  .avatar {
    width: 100rpx;
    height: 100rpx;
    border-radius: 50%;
    border: 2rpx solid #eee;
    flex-shrink: 0;
    background: #f5f5f5;
  }

  .invite-info {
    margin-left: 20rpx;
    min-width: 0;
  }

  .name {
    font-size: 28rpx;
    font-weight: bold;
    color: #333;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    max-width: 280rpx;
  }

  .no {
    margin-top: 10rpx;
    font-size: 22rpx;
    color: #999;
  }

  .invite-item-right {
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    flex-shrink: 0;
  }

  .level-tag {
    font-size: 20rpx;
    padding: 4rpx 14rpx;
    border-radius: 20rpx;
    margin-bottom: 8rpx;
  }

  .level-one {
    background: #fff3e0;
    color: #ff9800;
  }

  .level-two {
    background: #e3f2fd;
    color: #2196f3;
  }

  .time {
    font-size: 22rpx;
    color: #999;
  }
</style>
