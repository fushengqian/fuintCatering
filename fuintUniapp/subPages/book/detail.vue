<template>
    <view class="content">
        <view class="top-v" v-show="storeInfo"><text class="storeName">预约【{{ storeInfo.name }}】</text><text class="moreStore" @click="toMoreStore">切换门店</text></view>
        
        <view class="info-v">
            <view class="title">
                ｜请选择预约日期
            </view>
                <view v-if="dateArr && dateArr.length > 0" class="list-v">
                    <view @click="dateClick(index)" v-for="(item, index) in dateArr" :key="index" v-if="item.enable" :class="[dateIndex==index?'activeItem':'item-v']">
                        <view>{{ item.week }}</view>
                        <view>{{ item.date }}</view>
                    </view>
                </view>
                <none v-if="!dateArr.length" :isLoading="false" :custom-style="{ padding: '30px 10px' }" tips="暂无可预约日期"></none>
        </view>
        <view class="info-v">
            <view class="title">
                ｜请选择预约时段
            </view>
                <view v-if="timeArr && timeArr.length > 0" class="list-v">
                    <view @click="timeClick(index)" v-for="(item, index) in timeArr" :key="index"  :class="[timeIndex==index?'activeItem' : (bookable.indexOf(item.time) >= 0 ? 'item-v' : 'disable') ]">
                        <view>{{ item.time }}</view>
                    </view>
                </view>
                <none v-if="!timeArr.length" :isLoading="false" :custom-style="{ padding: '30rpx 10rpx' }" tips="暂无可预约时段"></none>
        </view>
        <view class="btn" @click="doSubmit">确定预约</view>
    </view>
</template>

<script>
    import * as BookApi from '@/api/book'
    import * as SettingApi from '@/api/setting'
    import None from '@/components/none'
    export default {
        components: {
          None
        },
        data() {
            return {
                // 预约项目ID
                bookId: null,
                // 当前预约详情
                bookInfo: null,
                dateArr: [ { week: '星期六', date : '8月17号' }, { week: '星期日', date : '8月18号' }], 
                timeArr: [ '09:00-12:00', '14:00-15:00' ],
                dateIndex: 0,
                timeIndex: 100000,
                storeInfo: null,
                bookable: [],
                isCheck: false
            }
        },
        onLoad(options) {
            // 记录预约ID
            this.bookId = options.bookId;
            // 获取预约详情
            this.getBookDetail();
        },
        onShow() {
            uni.removeStorageSync('bookData');
            this.getStoreInfo();
            this.dateIndex = 0;
            this.timeIndex = 100000;
        },
        methods: {
            // 获取预约项目详情
            getBookDetail() {
              const app = this;
              app.isLoading = true;
              BookApi.detail(app.bookId)
                .then(result => {
                    app.bookInfo = result.data.bookInfo;
                    app.dateArr = app.bookInfo.dateList;
                    app.timeArr = app.bookInfo.timeList;
                    app.dateClick(app.dateIndex);
                })
                .finally(() => app.isLoading = false)
            },
            // 切换门店
            toMoreStore() {
               this.$navTo('pages/location/index');
            },
            // 获取店铺详情
            getStoreInfo() {
               const app = this;
               SettingApi.storeDetail()
               .then(result => {
                   app.storeInfo = result.data.storeInfo;
               })
            },
            // 确定预约
            doSubmit() {
                let app = this;
                if (!app.isCheck) {
                    app.$toast("请选择预约时间！");
                    return false;
                }
                
                uni.showModal({
                    title: '提示',
                    content: '确定预约【'+app.storeInfo.name+'】吗?',
                    success: function (res) {
                        if (res.confirm) {
                            let dates = app.bookInfo.serviceDates.split(",");
                            let week = app.dateArr[app.dateIndex].week;
                            let data = { bookId: app.bookId, week: week, date : dates[app.dateIndex], time: app.timeArr[app.timeIndex].time };
                            uni.setStorageSync('bookData', data);
                            app.$navTo('subPages/book/submit');
                        }
                    }
                });
            },
            // 选择时段
            timeClick(index) {
                const app = this;
                if (app.bookable.indexOf(app.timeArr[index].time) < 0) {
                    return false;
                }
                app.timeIndex = index;
                app.isCheck = true;
            },
            // 选择日期
            dateClick(index) {
                const app = this;
                app.dateIndex = index;
                app.timeIndex = 100000;
                let dates = app.bookInfo.serviceDates.split(",");
                let times = app.timeArr;
                BookApi.bookable({ bookId: app.bookId, date: dates[app.dateIndex], time: '' })
                  .then(result => {
                     if (result.data) {
                         app.bookable = result.data;
                     } else {
                         app.bookable = [];
                     }
                  })
            }
        }
    }
</script>

<style lang="scss" scoped>
    .content {
        .top-v {
            margin: 20rpx;
            .storeName {
                font-weight: bold;
                font-size: 32rpx;
            }
            .moreStore {
                float: right;
                color: $fuint-theme;
                border: 1rpx solid $fuint-theme;
                padding: 6rpx;
                border-radius: 20rpx;
                font-size: 24rpx;
            }
        }
        padding-bottom: 50rpx;
    }
    .getInfo-v {
        background-color: #fff;
        padding: 50rpx 30rpx;
        border-radius: 20rpx;
        width: 600rpx;
        .getInfo-btn{
            background-color: $fuint-theme;
            color: #fff;
            padding: 20rpx;
            border-radius: 10rpx;
            margin-top: 30rpx;
            text-align: center;
        }
    }
    .btn {
        margin: 20rpx auto;
        background-color: $fuint-theme;
        padding: 20rpx;
        border-radius: 40rpx;
        text-align: center;
        color: #fff;
        width: 680rpx;
        font-size: 30rpx;
        margin-top: 50rpx;
    }
    
    .info-v {
        padding: 20rpx;
        background-color: #fff;
        margin-bottom: 20rpx;
        .title {
            font-weight: bold;
            color: $fuint-theme;
        }
        .list-v {
            padding: 20rpx;
            display: flex;
            flex-wrap: wrap;
            .item-v {
                border-radius: 12rpx;
                font-size: 30rpx;
                margin-top: 10rpx;
                margin-left: 10rpx;
                font-weight: bold;
                width: 30%;
                border: 1rpx solid #ccc;
                text-align: center;
                padding: 20rpx;
            }
            .activeItem {
                font-size: 30rpx;
                border-radius: 12rpx;
                margin-top: 10rpx;
                margin-left: 10rpx;
                width: 30%;
                font-weight: bold;
                background-color: $fuint-theme;
                border: 1rpx solid #ccc;
                color: #fff;
                text-align: center;
                padding: 20rpx;
            }
            .disable {
                border-radius: 12rpx;
                font-size: 30rpx;
                margin-top: 10rpx;
                margin-left: 10rpx;
                font-weight: bold;
                width: 30%;
                border: 1rpx solid #ccc;
                text-align: center;
                color: white !important;
                background-color: rgb(188, 188, 188) !important;
                padding: 20rpx;
            }
        }
    }
</style>
