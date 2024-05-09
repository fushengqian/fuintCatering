<template>
  <view v-if="!isLoading" class="container">
    <view class="goods-list">
      <view class="goods-item" v-for="(item, index) in goodsList" :key="index">
        <!-- 商品详情 -->
        <view class="goods-main">
          <!-- 商品图片 -->
          <view class="goods-image">
            <image class="image" :src="item.goods_image" mode="widthFix"></image>
          </view>
          <!-- 商品信息 -->
          <view class="goods-content">
            <view class="goods-title twolist-hidden"><text>{{ item.name }}</text></view>
            <view class="goods-props clearfix">
              <view class="goods-props-item" v-for="(props, idx) in item.goods_props" :key="idx">
                <text>{{ props.value.name }}</text>
              </view>
            </view>
          </view>
          <!-- 交易信息 -->
          <view class="goods-trade">
            <view class="goods-price">
              <text class="unit">￥</text>
              <text class="value">{{ item.goods_price }}</text>
            </view>
            <view class="goods-num">
              <text>×{{ item.total_num }}</text>
            </view>
          </view>
        </view>
        <!-- 选择评价 -->
        <view class="score-row">
          <view class="score-item score-praise" :class="{ active: formData[index].score == 10 }" @click="setScore(index, 10)">
            <view class="score">
              <text class="score-icon iconfont icon-haoping"></text>
              <text class="score-text">好评</text>
            </view>
          </view>
          <view class="score-item score-review" :class="{ active: formData[index].score == 20 }" @click="setScore(index, 20)">
            <view class="score">
              <text class="score-icon iconfont icon-zhongping"></text>
              <text class="score-text">中评</text>
            </view>
          </view>
          <view class="score-item score-negative" :class="{ active: formData[index].score == 30 }" @click="setScore(index, 30)">
            <view class="score">
              <text class="score-icon iconfont icon-chaping"></text>
              <text class="score-text">差评</text>
            </view>
          </view>
        </view>

        <!-- 评价内容 -->
        <view class="form-content">
          <textarea class="textarea" v-model="formData[index].content" maxlength="500" placeholder="请输入评价内容 (留空则不评价)"></textarea>
        </view>

        <!-- 图片列表 -->
        <view class="image-list">
          <view class="image-preview" v-for="(image, imageIndex) in formData[index].imageList" :key="imageIndex">
            <text class="image-delete iconfont icon-shanchu" @click="deleteImage(index, imageIndex)"></text>
            <image class="image" mode="aspectFill" :src="image.path"></image>
          </view>
          <view v-if="!formData[index].imageList || formData[index].imageList.length < maxImageLength" class="image-picker"
            @click="chooseImage(index)">
            <text class="choose-icon iconfont icon-tubiao_xiangji"></text>
            <text class="choose-text">上传图片</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 底部操作按钮 -->
    <view class="footer-fixed">
      <view class="btn-wrapper">
        <view class="btn-item btn-item-main" :class="{ disabled }" @click="handleSubmit()">确认提交</view>
      </view>
    </view>

  </view>
</template>

<script>
  import * as UploadApi from '@/api/upload'
  import * as OrderCommentApi from '@/api/order/comment'

  const maxImageLength = 6

  export default {
    data() {
      return {
        // 正在加载
        isLoading: true,
        // 当前订单ID
        orderId: null,
        // 待评价商品列表
        goodsList: [],
        // 表单数据
        formData: [],
        // 最大图片数量
        maxImageLength,
        // 按钮禁用
        disabled: false
      }
    },

    /**
     * 生命周期函数--监听页面加载
     */
    onLoad({ orderId }) {
      this.orderId = orderId
      // 获取待评价商品列表
      this.getGoodsList()
    },

    methods: {

      // 获取待评价商品列表
      getGoodsList() {
        const app = this
        app.isLoading = true
        OrderCommentApi.list(app.orderId)
          .then(result => {
            app.goodsList = result.data.goodsList
            app.initFormData()
            app.isLoading = false
          })
      },

      // 初始化form数据
      initFormData() {
        const { goodsList } = this
        const formData = goodsList.map(item => {
          return {
            goods_id: item.goods_id,
            order_goods_id: item.order_goods_id,
            score: 10,
            content: '',
            imageList: [],
            uploaded: []
          }
        })
        this.formData = formData
      },

      // 设置评分
      setScore(index, score) {
        this.formData[index].score = score
      },

      // 选择图片
      chooseImage(index) {
        const app = this
        const oldImageList = app.formData[index].imageList
        // 选择图片
        uni.chooseImage({
          count: maxImageLength - oldImageList.length,
          sizeType: ['original', 'compressed'], // 可以指定是原图还是压缩图，默认二者都有
          sourceType: ['album', 'camera'], // 可以指定来源是相册还是相机，默认二者都有
          success({ tempFiles }) {
            // tempFiles = [{path:'xxx', size:100}]
            app.formData[index].imageList = oldImageList.concat(tempFiles)
          }
        });
      },

      // 删除图片
      deleteImage(index, imageIndex) {
        this.formData[index].imageList.splice(imageIndex, 1)
      },

      // 表单提交
      handleSubmit() {
        const app = this
        // 判断是否重复提交
        if (app.disabled === true) return false
        // 按钮禁用
        app.disabled = true
        // 判断是否需要上传图片
        const imagesLength = app.getImagesLength()
        if (imagesLength > 0) {
          app.uploadFile()
            .then(() => {
              console.log('then')
              app.onSubmit()
            })
            .catch(err => {
              console.log('catch')
              app.disabled = false
              if (err.statusCode !== 0) {
                app.$toast(err.errMsg)
              }
              console.log('err', err)
            })
        } else {
          app.onSubmit()
        }
      },

      // 统计图片数量
      getImagesLength() {
        const { formData } = this
        let imagesLength = 0
        formData.forEach(item => {
          if (item.content.trim()) {
            imagesLength += item.imageList.length
          }
        })
        return imagesLength
      },

      // 提交到后端
      onSubmit() {
        const app = this
        OrderCommentApi.submit(app.orderId, app.formData)
          .then(result => {
            app.$toast(result.message)
            setTimeout(() => {
              app.disabled = false
              uni.navigateBack()
            }, 1500)
          })
          .catch(err => app.disabled = false)
      },

      // 上传图片
      uploadFile() {
        const app = this
        const { formData } = app
        // 整理上传文件路径
        const files = []
        formData.forEach((item, index) => {
          if (item.content.trim() && item.imageList.length) {
            const images = item.imageList.map(image => image)
            files.push({ formDataIndex: index, images })
          }
        })
        // 批量上传
        return new Promise((resolve, reject) => {
          Promise.all(files.map((file, index) => {
              return new Promise((resolve, reject) => {
                UploadApi.image(file.images)
                  .then(fileIds => {
                    app.formData[index].uploaded = fileIds
                    resolve(fileIds)
                  })
                  .catch(err => reject(err))
              })
            }))
            .then(resolve, reject)
        })
      }

    }
  }
</script>

<style lang="scss" scoped>
  .container {
    padding-bottom: 100rpx;
  }

  .goods-list {
    font-size: 28rpx;
    padding-top: 30rpx;
  }

  .goods-item {
    width: 94%;
    background: #fff;
    padding: 24rpx 24rpx;
    box-shadow: 0 1rpx 5rpx 0px rgba(0, 0, 0, 0.05);
    margin: 0 auto 30rpx auto;
    border-radius: 20rpx;

    .goods-detail {
      padding: 24rpx 20rpx;

      .left {
        .goods-image {
          display: block;
          width: 150rpx;
          height: 150rpx;
        }
      }

      .right {
        padding-left: 20rpx;
      }
    }

    .score-row {
      display: flex;
      justify-content: space-around;
      padding: 24rpx 20rpx;

      .score-item {
        display: flex;
        justify-content: center;
        align-items: center;

        &.score-praise {
          color: #ff4544;

          .score-icon {
            background: #ff4544;
          }
        }

        &.score-review {
          color: #fcb500;

          .score-icon {
            background: #fcb500;
          }
        }

        &.score-negative {
          color: #9b9b9b;

          .score-icon {
            background: #9b9b9b;
          }
        }

        .score {
          padding: 10rpx 20rpx 10rpx 10rpx;
          border-radius: 30rpx;

          .score-icon {
            margin-right: 10rpx;
            padding: 10rpx;
            border-radius: 50%;
            font-size: 30rpx;
            color: #fff;
          }
        }

        &.active {
          .score {
            color: #fff;
          }

          &.score-praise {
            .score {
              background: #ff4544;
            }
          }

          &.score-review {
            .score {
              background: #fcb500;
            }
          }

          &.score-negative {
            .score {
              background: #9b9b9b;
            }
          }
        }
      }
    }

    // 评价内容
    .form-content {
      padding: 14rpx 10rpx;

      .textarea {
        width: 100%;
        height: 220rpx;
        padding: 12rpx;
        border: 1rpx solid #e8e8e8;
        border-radius: 5rpx;
        box-sizing: border-box;
        font-size: 26rpx;
      }
    }

    .image-list {
      padding: 0 20rpx;
      margin-top: 20rpx;
      margin-bottom: -20rpx;

      &:after {
        clear: both;
        content: " ";
        display: table;
      }

      .image {
        display: block;
        width: 100%;
        height: 100%;
      }

      .image-picker,
      .image-preview {
        width: 184rpx;
        height: 184rpx;
        margin-right: 30rpx;
        margin-bottom: 30rpx;
        float: left;

        &:nth-child(3n+0) {
          margin-right: 0;
        }
      }

      .image-picker {
        display: flex;
        flex-direction: column;
        justify-content: center;
        align-items: center;
        border: 1rpx dashed #ccc;
        color: #ccc;

        .choose-icon {
          font-size: 48rpx;
          margin-bottom: 6rpx;
        }

        .choose-text {
          font-size: 24rpx;
        }
      }

      .image-preview {
        position: relative;

        .image-delete {
          position: absolute;
          top: -15rpx;
          right: -15rpx;
          height: 42rpx;
          width: 42rpx;
          line-height: 42rpx;
          background: rgba(0, 0, 0, 0.64);
          border-radius: 50%;
          color: #fff;
          font-weight: bolder;
          font-size: 22rpx;
          z-index: 10;
          text-align: center;
        }
      }
    }
  }



  // 商品项
  .goods-main {
    display: flex;
    margin-bottom: 20rpx;

    // 商品图片
    .goods-image {
      width: 180rpx;
      height: 180rpx;

      .image {
        display: block;
        width: 100%;
        height: 100%;
        border-radius: 8rpx;
      }
    }

    // 商品内容
    .goods-content {
      flex: 1;
      padding-left: 16rpx;
      padding-top: 16rpx;

      .goods-title {
        font-size: 26rpx;
        max-height: 76rpx;
      }

      .goods-props {
        margin-top: 14rpx;
        height: 40rpx;
        color: #ababab;
        font-size: 24rpx;
        overflow: hidden;

        .goods-props-item {
          display: inline-block;
          margin-right: 14rpx;
          padding: 4rpx 16rpx;
          border-radius: 12rpx;
          background-color: #F5F5F5;
          width: auto;
        }
      }


    }

    // 交易信息
    .goods-trade {
      padding-top: 16rpx;
      width: 150rpx;
      text-align: right;
      color: $uni-text-color-grey;
      font-size: 26rpx;

      .goods-price {
        vertical-align: bottom;
        margin-bottom: 16rpx;

        .unit {
          margin-right: -2rpx;
          font-size: 24rpx;
        }
      }
    }

  }


  // 底部操作栏
  .footer-fixed {
    position: fixed;
    bottom: var(--window-bottom);
    left: 0;
    right: 0;
    height: 180rpx;
    padding-bottom: 30rpx;
    z-index: 11;
    box-shadow: 0 -4rpx 40rpx 0 rgba(144, 52, 52, 0.1);
    background: #fff;

    .btn-wrapper {
      height: 100%;
      display: flex;
      align-items: center;
      padding: 0 20rpx;
    }

    .btn-item {
      flex: 1;
      font-size: 28rpx;
      height: 80rpx;
      line-height: 90rpx;
      text-align: center;
      color: #fff;
      border-radius: 8rpx;
    }

    .btn-item-main {
      background: linear-gradient(to right, #f9211c, #ff6335);

      // 禁用按钮
      &.disabled {
        background: #ff9779;
      }
    }

  }
</style>
