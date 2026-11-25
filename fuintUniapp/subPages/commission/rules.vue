<template>
  <view class="container" :style="themeVars">
    <!-- 顶部 tab -->
    <view class="tab-bar">
      <view
        v-for="tab in tabs"
        :key="tab.key"
        class="tab-item"
        :class="{ 'tab-item-active': activeTab === tab.key }"
        @click="switchTab(tab.key)"
      >
        <text class="tab-text">{{ tab.name }}</text>
        <view v-if="activeTab === tab.key" class="tab-line"></view>
      </view>
    </view>

    <!-- 说明内容 -->
    <view v-if="loading" class="empty-tip">加载中...</view>
    <template v-else>
      <view v-if="sections.length > 0" class="section-list">
        <view v-for="(section, index) in sections" :key="index" class="section-card">
          <view class="section-header">
            <view class="section-title">{{ section.title }}</view>
            <view class="section-period">{{ section.period || '长期有效' }}</view>
          </view>
          <view v-if="section.description" class="section-desc">{{ section.description }}</view>
          <view class="rule-lines">
            <view v-for="(line, lineIndex) in section.lines" :key="lineIndex" class="rule-line">
              <text class="rule-dot"></text>
              <text class="rule-text">{{ line }}</text>
            </view>
          </view>
        </view>
      </view>
      <view v-else class="empty-card">
        <text class="iconfont icon-bangzhu empty-icon"></text>
        <view class="empty-text">暂无{{ activeTabName }}规则</view>
        <view class="empty-sub">商家配置奖励方案后，将在此自动生成说明</view>
      </view>

      <!-- 通用说明 -->
      <view v-if="notes.length > 0" class="notes-card">
        <view class="notes-title">温馨提示</view>
        <view v-for="(note, index) in notes" :key="index" class="note-item">
          <text class="note-index">{{ index + 1 }}</text>
          <text class="note-text">{{ note }}</text>
        </view>
      </view>
    </template>
  </view>
</template>

<script>
  import * as CommissionApi from '@/api/commission'

  export default {
    data() {
      return {
        tabs: [
          { key: 'commission', name: '分佣提成' },
          { key: 'recommend', name: '推荐奖励' },
          { key: 'withdraw', name: '提现规则' }
        ],
        activeTab: 'commission',
        ruleData: {
          commission: { sections: [], notes: [] },
          recommend: { sections: [], notes: [] },
          withdraw: { sections: [], notes: [] }
        },
        loading: true
      }
    },
    computed: {
      activeTabName() {
        const tab = this.tabs.find(item => item.key === this.activeTab)
        return tab ? tab.name : ''
      },
      currentData() {
        const data = this.ruleData[this.activeTab] || {}
        return {
          sections: data.sections || [],
          notes: data.notes || []
        }
      },
      sections() {
        return this.currentData.sections
      },
      notes() {
        return this.currentData.notes
      }
    },
    onLoad(options) {
      // 支持从统计页指定默认tab（如推荐奖励卡片进入时直接展示推荐奖励）
      if (options && options.tab && this.tabs.some(item => item.key === options.tab)) {
        this.activeTab = options.tab
      }
      this.getRuleDesc()
    },
    methods: {
      /**
       * 切换 tab
       */
      switchTab(key) {
        this.activeTab = key
      },

      /**
       * 获取奖励规则说明
       */
      getRuleDesc() {
        this.loading = true
        CommissionApi.ruleDesc()
          .then(result => {
            const data = result.data || {}
            this.ruleData = {
              commission: data.commission || { sections: [], notes: [] },
              recommend: data.recommend || { sections: [], notes: [] },
              withdraw: data.withdraw || { sections: [], notes: [] }
            }
          })
          .catch(() => {})
          .finally(() => {
            this.loading = false
          })
      }
    }
  }
</script>

<style lang="scss" scoped>
  .container {
    min-height: 100vh;
    background: #f7f7f7;
    padding-bottom: 40rpx;
  }

  /* 顶部 tab */
  .tab-bar {
    display: flex;
    align-items: center;
    background: #fff;
    border-bottom: 1rpx solid #f0f0f0;
  }

  .tab-item {
    flex: 1;
    height: 90rpx;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    position: relative;
  }

  .tab-text {
    font-size: 28rpx;
    color: #666;
  }

  .tab-item-active .tab-text {
    color: var(--theme-primary);
    font-weight: bold;
  }

  .tab-line {
    position: absolute;
    bottom: 0;
    width: 56rpx;
    height: 6rpx;
    border-radius: 6rpx;
    background: var(--theme-primary);
  }

  /* 方案卡片 */
  .section-list {
    padding: 20rpx 20rpx 0;
  }

  .section-card {
    background: #fff;
    border-radius: 16rpx;
    padding: 30rpx;
    margin-bottom: 20rpx;
  }

  .section-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  .section-title {
    font-size: 30rpx;
    font-weight: bold;
    color: #333;
    flex: 1;
    min-width: 0;
  }

  .section-period {
    font-size: 22rpx;
    color: var(--theme-primary);
    background: rgba(0, 0, 0, 0.04);
    border-radius: 20rpx;
    padding: 6rpx 16rpx;
    margin-left: 16rpx;
    flex-shrink: 0;
  }

  .section-desc {
    font-size: 24rpx;
    color: #999;
    margin-top: 12rpx;
    line-height: 36rpx;
  }

  .rule-lines {
    margin-top: 20rpx;
    padding-top: 20rpx;
    border-top: 1rpx solid #f5f5f5;
  }

  .rule-line {
    display: flex;
    align-items: flex-start;
    margin-bottom: 14rpx;

    &:last-child {
      margin-bottom: 0;
    }
  }

  .rule-dot {
    width: 10rpx;
    height: 10rpx;
    border-radius: 50%;
    background: var(--theme-primary);
    margin: 14rpx 14rpx 0 0;
    flex-shrink: 0;
  }

  .rule-text {
    font-size: 26rpx;
    color: #555;
    line-height: 40rpx;
  }

  /* 空状态 */
  .empty-card {
    margin: 20rpx;
    background: #fff;
    border-radius: 16rpx;
    padding: 80rpx 30rpx;
    text-align: center;
  }

  .empty-icon {
    font-size: 80rpx;
    color: #ddd;
  }

  .empty-text {
    font-size: 28rpx;
    color: #666;
    margin-top: 20rpx;
  }

  .empty-sub {
    font-size: 22rpx;
    color: #bbb;
    margin-top: 10rpx;
  }

  .empty-tip {
    text-align: center;
    font-size: 24rpx;
    color: #bbb;
    padding: 80rpx 0;
  }

  /* 通用说明 */
  .notes-card {
    margin: 20rpx;
    background: #fff;
    border-radius: 16rpx;
    padding: 30rpx;
  }

  .notes-title {
    font-size: 28rpx;
    font-weight: bold;
    color: #333;
    margin-bottom: 20rpx;
  }

  .note-item {
    display: flex;
    align-items: flex-start;
    margin-bottom: 14rpx;

    &:last-child {
      margin-bottom: 0;
    }
  }

  .note-index {
    width: 32rpx;
    height: 32rpx;
    line-height: 32rpx;
    text-align: center;
    border-radius: 50%;
    background: rgba(0, 0, 0, 0.04);
    font-size: 20rpx;
    color: #999;
    margin-right: 14rpx;
    flex-shrink: 0;
  }

  .note-text {
    font-size: 24rpx;
    color: #999;
    line-height: 36rpx;
  }
</style>
