<template>
  <view v-if="!isLoading" class="container">
    <view class="goods-list">
      <view class="goods-item" v-for="(item, index) in goodsList" :key="index">
        <!-- 商品详情 -->
        <view class="goods-main">
          <view class="goods-image">
            <image class="image" :src="item.goodsLogo" mode="aspectFill"></image>
          </view>
          <view class="goods-content">
            <view class="goods-title twolist-hidden"><text>{{ item.goodsName }}</text></view>
            <view class="goods-num">
              <text>×{{ item.num }}</text>
            </view>
          </view>
          <!-- 已评价标记 -->
          <view v-if="item.commented" class="commented-tag">已评价</view>
        </view>

        <!-- 待评价表单 -->
        <view v-if="item.canComment">
          <!-- 星级评分 -->
          <view class="score-row">
            <view class="stars">
              <text
                v-for="s in 5"
                :key="s"
                class="star"
                :class="{ active: formData[index].score >= s }"
                @click="setScore(index, s)"
                >★</text>
            </view>
            <text class="score-tip">{{ scoreTips[formData[index].score - 1] }}</text>
          </view>

          <!-- 评价内容 -->
          <view class="form-content">
            <textarea class="textarea" v-model="formData[index].content" maxlength="500" placeholder="说说这款商品的体验吧～（500字以内）"></textarea>
          </view>

          <!-- 图片列表 -->
          <view class="image-list">
            <view class="image-preview" v-for="(image, imageIndex) in formData[index].imageList" :key="imageIndex">
              <text class="image-delete iconfont icon-shanchu" @click="deleteImage(index, imageIndex)"></text>
              <image class="image" mode="aspectFill" :src="image.path"></image>
            </view>
            <view v-if="formData[index].imageList.length < maxImageLength" class="image-picker" @click="chooseImage(index)">
              <text class="choose-icon iconfont icon-tubiao_xiangji"></text>
              <text class="choose-text">上传图片</text>
            </view>
          </view>

          <!-- 匿名开关 -->
          <view class="anon-row">
            <text>匿名评价</text>
            <view class="switch" :class="{ on: formData[index].isAnonymous === 'Y' }" @click="toggleAnonymous(index)">
              <view class="switch-inner"></view>
            </view>
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
  import * as GoodsCommentApi from '@/api/goodsComment'

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
        // 评分文案
        scoreTips: ['非常差', '较差', '一般', '满意', '非常满意'],
        // 按钮禁用
        disabled: false
      }
    },

    onLoad({ orderId }) {
      this.orderId = orderId
      this.getOrderGoodsList()
    },

    methods: {
      // 获取订单内商品评价情况
      getOrderGoodsList() {
        const app = this
        app.isLoading = true
        GoodsCommentApi.orderGoods(app.orderId)
          .then(result => {
            app.goodsList = result.data.list || []
            app.initFormData()
            app.isLoading = false
          })
      },

      // 初始化form数据
      initFormData() {
        const { goodsList } = this
        this.formData = goodsList.map(item => {
          return {
            orderGoodsId: item.orderGoodsId,
            score: 5,
            content: '',
            imageList: [],
            uploaded: [],
            isAnonymous: 'N'
          }
        })
      },

      // 设置评分
      setScore(index, score) {
        this.formData[index].score = score
      },

      // 切换匿名
      toggleAnonymous(index) {
        this.formData[index].isAnonymous = this.formData[index].isAnonymous === 'Y' ? 'N' : 'Y'
      },

      // 选择图片
      chooseImage(index) {
        const app = this
        const oldImageList = app.formData[index].imageList
        uni.chooseImage({
          count: maxImageLength - oldImageList.length,
          sizeType: ['compressed'],
          sourceType: ['album', 'camera'],
          // tempFiles 为对象数组（含 path），与上传框架 UploadApi.image 的入参约定一致
          success({ tempFiles }) {
            app.formData[index].imageList = oldImageList.concat(tempFiles)
          }
        })
      },

      // 删除图片
      deleteImage(index, imageIndex) {
        this.formData[index].imageList.splice(imageIndex, 1)
      },

      // 表单提交
      handleSubmit() {
        const app = this
        if (app.disabled === true) return false
        // 待评价且填写了内容的商品
        const targets = []
        app.formData.forEach((item, index) => {
          const canComment = app.goodsList[index].canComment
          if (canComment && (item.content.trim() || item.imageList.length)) {
            targets.push({ index, data: item })
          }
        })
        if (!targets.length) {
          app.$error('请至少对一个商品填写评价内容或上传图片')
          return
        }
        app.disabled = true
        app.submitTargets(targets)
      },

      // 依次上传图片并提交
      submitTargets(targets) {
        const app = this
        const next = (i) => {
          if (i >= targets.length) {
            app.$toast('评价提交成功')
            setTimeout(() => {
              uni.navigateBack()
            }, 1500)
            return
          }
          const { index, data } = targets[i]
          const uploadPromise = data.imageList.length
            ? UploadApi.image(data.imageList)
            : Promise.resolve([])
          uploadPromise
            .then(paths => {
              // 上传接口返回的 data 为对象（含 fileName/domain 等），取相对路径 fileName 作为评价图片
              const imageUrls = (paths || []).map(item => (typeof item === 'string' ? item : item.fileName))
              return GoodsCommentApi.submit({
                orderId: app.orderId,
                orderGoodsId: data.orderGoodsId,
                score: data.score,
                content: data.content.trim(),
                images: imageUrls,
                isAnonymous: data.isAnonymous
              })
            })
            .then(() => next(i + 1))
            .catch(err => {
              app.disabled = false
            })
        }
        next(0)
      }
    }
  }
</script>

<style lang="scss" scoped>
  .container {
    padding-bottom: 160rpx;
  }

  .goods-list {
    font-size: 28rpx;
    padding-top: 30rpx;
  }

  .goods-item {
    width: 94%;
    background: #fff;
    padding: 24rpx;
    box-shadow: 0 1rpx 5rpx 0px rgba(0, 0, 0, 0.05);
    margin: 0 auto 30rpx auto;
    border-radius: 20rpx;
  }

  .goods-main {
    display: flex;
    align-items: center;
    margin-bottom: 10rpx;

    .goods-image {
      width: 140rpx;
      height: 140rpx;

      .image {
        display: block;
        width: 100%;
        height: 100%;
        border-radius: 8rpx;
      }
    }

    .goods-content {
      flex: 1;
      padding-left: 16rpx;

      .goods-title {
        font-size: 26rpx;
        max-height: 76rpx;
      }

      .goods-num {
        margin-top: 12rpx;
        color: $uni-text-color-grey;
        font-size: 24rpx;
      }
    }

    .commented-tag {
      padding: 6rpx 18rpx;
      font-size: 24rpx;
      color: #999;
      background: #f5f5f5;
      border-radius: 20rpx;
    }
  }

  .score-row {
    display: flex;
    align-items: center;
    padding: 16rpx 0;

    .stars {
      .star {
        font-size: 44rpx;
        color: #e0e0e0;
        margin-right: 10rpx;

        &.active {
          color: #ffb800;
        }
      }
    }

    .score-tip {
      margin-left: 12rpx;
      font-size: 24rpx;
      color: #ff976a;
    }
  }

  .form-content {
    padding: 8rpx 0;

    .textarea {
      width: 100%;
      height: 200rpx;
      padding: 12rpx;
      background: #f7f8fa;
      border-radius: 8rpx;
      box-sizing: border-box;
      font-size: 26rpx;
    }
  }

  .image-list {
    display: flex;
    flex-wrap: wrap;
    padding: 8rpx 0;

    .image {
      display: block;
      width: 100%;
      height: 100%;
    }

    .image-picker,
    .image-preview {
      width: 160rpx;
      height: 160rpx;
      margin-right: 20rpx;
      margin-bottom: 20rpx;
    }

    .image-picker {
      display: flex;
      flex-direction: column;
      justify-content: center;
      align-items: center;
      border: 1rpx dashed #ccc;
      color: #ccc;

      .choose-icon {
        font-size: 44rpx;
        margin-bottom: 6rpx;
      }

      .choose-text {
        font-size: 22rpx;
      }
    }

    .image-preview {
      position: relative;

      .image-delete {
        position: absolute;
        top: -12rpx;
        right: -12rpx;
        height: 42rpx;
        width: 42rpx;
        line-height: 42rpx;
        background: rgba(0, 0, 0, 0.64);
        border-radius: 50%;
        color: #fff;
        font-size: 22rpx;
        z-index: 10;
        text-align: center;
      }
    }
  }

  .anon-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12rpx 0;
    font-size: 26rpx;
    color: #333;

    .switch {
      width: 84rpx;
      height: 44rpx;
      border-radius: 22rpx;
      background: #dcdfe6;
      position: relative;
      transition: background 0.2s;

      .switch-inner {
        position: absolute;
        left: 4rpx;
        top: 4rpx;
        width: 36rpx;
        height: 36rpx;
        border-radius: 50%;
        background: #fff;
        transition: left 0.2s;
      }

      &.on {
        background: #f9211c;

        .switch-inner {
          left: 44rpx;
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
    height: 140rpx;
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
      line-height: 80rpx;
      text-align: center;
      color: #fff;
      border-radius: 40rpx;
    }

    .btn-item-main {
      background: linear-gradient(to right, #f9211c, #ff6335);

      &.disabled {
        background: #ff9779;
      }
    }
  }
</style>

