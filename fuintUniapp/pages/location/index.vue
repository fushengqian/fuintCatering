<template>
  <view class="container">
    <view class="search-wrapper">
      <view class="search-input">
        <view class="search-input-wrapper">
          <view class="left">
            <text class="search-icon iconfont icon-sousuo"></text>
          </view>
          <view class="right">
            <input v-model="searchValue" class="input" placeholder="请输入店铺关键字" type="text"></input>
          </view>
        </view>
      </view>
      <view class="search-button">
        <button class="button" @click="doSearch" type="warn"> 搜索 </button>
      </view>
    </view>
    <view class="store-list">
      <view class="store-info" v-for="(item, index) in storeList" :key="index" @click="handleQuick(item.id)">
          <view class="base-info">
              <view class="name">{{ item.name }}</view>
              <view class="hours">营业时间：{{ item.hours }}</view>
              <view class="address"><text class="location-icon iconfont icon-dingwei"></text>{{ item.address }}</view>
              <view class="tel">联系电话：{{ item.phone }}</view>
          </view>
          <view class="loc-info">
                <text class="dis"><text class="distance">{{ parseFloat(item.distance).toFixed(1) }}</text>公里</text>
          </view>
      </view>
    </view>
    <empty v-if="!storeList.length" :isLoading="isLoading" :custom-style="{ padding: '180rpx 50rpx' }" tips="暂无店铺~">
    </empty>
  </view>
</template>

<script>
  import * as settingApi from '@/api/setting'
  import * as userApi from '@/api/user'
  import Empty from '@/components/empty'
  export default {
    components: {
      Empty
    },
    data() {
      return {
        storeId: 0,
        searchValue: '',
        storeList: []
      }
    },

    /**
     * 生命周期函数--监听页面加载
     */
    onLoad(options) {
        this.storeId = this.getStoreId()
        this.getStoreList()
    },

    methods: {
        
      /**
       * 获取店铺列表
       * */
       getStoreList() {
           const app = this
           settingApi.storeList(app.searchValue)
             .then(result => {
                app.storeList = result.data.data
           })
       },
       
      /**
       * 获取历史店铺
       */
      getStoreId() {
        return uni.getStorageSync("storeId")
      },

      /**
       * 搜索提交
       */
      doSearch() {
        this.getStoreList()
      },

      /**
       * 跳转回去
       */
      handleQuick(storeId) {
        const app = this
        userApi.defaultStore(storeId)
          .then(result => {
            uni.setStorageSync("storeId", storeId);
            // 刷新相关页面数据
            uni.setStorageSync("reflashHomeData", true);
            uni.navigateBack();
        })
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
          color: #666666;
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

  // 店铺列表
  .store-list {
    .store-info {
      padding: 10px 0;
      overflow: hidden;
      border: 2rpx solid #cccccc;
      min-height: 240rpx;
      border-radius: 5rpx;
      margin-top: 10rpx;
      margin-bottom: 10rpx;
      padding: 30rpx;
      background: #FFFFFF;
      .base-info {
          float: left;
          width: 70%;
          .name {
              font-size: 34rpx;
              font-weight: bold;
              margin-top: 15rpx;
              margin-bottom: 12rpx;
              color: #666;
          }
          .location-icon {
              color: #f03c3c;
              font-weight: bold;
          }
      }
      .loc-info {
        color: #666666;
        dispaly:flex;
        line-height: 240rpx;
        float: left;
        overflow: hidden;
        width: 30%;
        text-align: right;
        .distance {
            font-weight: bold;
            color: #f03c3c;
        }
      }
    }
  }
</style>
