<template>
  <view class="container">
    <view class="search-wrapper">
      <view class="search-input">
        <view class="search-input-wrapper">
          <view class="left">
            <text class="search-icon iconfont icon-sousuo"></text>
          </view>
          <view class="right">
            <input v-model="searchValue" class="input" focus="true" placeholder="请输入搜索关键字" type="text"></input>
          </view>
        </view>
      </view>
      <view class="search-button">
        <button class="button" @click="onSearch" type="warn"> 搜索 </button>
      </view>
    </view>
    <view class="history" v-if="historySearch.length">
      <view class="his-head">
        <text class="title">最近搜索</text>
        <text class="icon iconfont icon-lajixiang col-7" @click="clearSearch"></text>
      </view>
      <view class="his-list">
        <view class="his-item" v-for="(val, index) in historySearch" :key="index">
          <view class="history-button" @click="handleQuick(val)">{{ val }}</view>
        </view>
      </view>
    </view>
    <!-- </view> -->
  </view>
</template>

<script>
  const HISTORY_SEARCH = 'historySearch'

  export default {
    data() {
      return {
        historySearch: [],
        searchValue: ''
      }
    },

    /**
     * 生命周期函数--监听页面加载
     */
    onLoad(options) {
      // 获取历史搜索
      this.historySearch = this.getHistorySearch()
    },

    methods: {

      /**
       * 获取历史搜索
       */
      getHistorySearch() {
        return uni.getStorageSync(HISTORY_SEARCH) || []
      },

      /**
       * 搜索提交
       */
      onSearch() {
        const { searchValue } = this
        if (searchValue) {
          // 记录历史搜索
          this.setHistory(searchValue)
          // 跳转到商品列表页
          this.$navTo('pages/goods/list', { search: searchValue })
        }
      },

      /**
       * 记录历史搜索
       */
      setHistory(searchValue) {
        const data = this.getHistorySearch()
        const index = data.indexOf(searchValue)
        index > -1 && data.splice(index, 1)
        data.unshift(searchValue)
        this.historySearch = data
        this.onUpdateStorage()
      },

      /**
       * 清空最近搜索记录
       */
      clearSearch() {
        this.historySearch = []
        this.onUpdateStorage()
      },

      /**
       * 更新历史搜索缓存
       * @param {Object} data
       */
      onUpdateStorage(data) {
        uni.setStorageSync(HISTORY_SEARCH, this.historySearch)
      },

      /**
       * 跳转到最近搜索
       */
      handleQuick(search) {
        this.$navTo('pages/goods/list', { search })
      }

    }
  }
</script>

<style lang="scss" scoped>
  .container {
    padding: 20rpx;
    min-height: 100vh;
    background: #f7f7f7;
  }

  .search-wrapper {
    display: flex;
    height: 78rpx;
  }

  // 搜索输入框
  .search-input {
    width: 80%;
    background: #fff;
    border-radius: 50rpx 0 0 50rpx;
    box-sizing: border-box;
    overflow: hidden;
    border: solid 1px #cccccc;
    .search-input-wrapper {
      display: flex;

      .left {
        display: flex;
        width: 60rpx;
        justify-content: center;
        align-items: center;
        .search-icon {
          display: block;
          color: #b4b4b4;
          font-size: 30rpx;
          font-weight: bold;
        }
      }

      .right {
        flex: 1;

        input {
          font-size: 28rpx;
          height: 78rpx;
          line-height: 78rpx;

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


  // 最近搜索
  .history {

    .his-head {
      font-size: 28rpx;
      padding: 50rpx 0 0 0;
      color: #777;

      .icon {
        float: right;
      }

    }

    .his-list {
      padding: 10px 0;
      overflow: hidden;

      .his-item {
        width: 33.3%;
        float: left;
        padding: 10rpx;
        box-sizing: border-box;

        .history-button {
          text-align: center;
          padding: 14rpx;
          line-height: 30rpx;
          border-radius: 100rpx;
          background: #fff;
          font-size: 26rpx;
          border: 1px solid #efefef;
          overflow: hidden;
          white-space: nowrap;
          text-overflow: ellipsis;
        }

      }

    }
  }
</style>
