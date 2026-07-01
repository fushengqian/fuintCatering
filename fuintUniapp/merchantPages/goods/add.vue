<template>
  <view class="container">
    <!-- 页面标题 -->
    <view class="page-header">
      <view class="header-back" @click="onBack">
        <text class="iconfont icon-fanhui"></text>
      </view>
      <view class="header-title">{{ isEdit ? '编辑商品' : '新增商品' }}</view>
      <view class="header-placeholder"></view>
    </view>

    <view class="form-wrap">
      <!-- 商品主图 -->
      <view class="form-section">
        <view class="section-title">商品主图</view>
        <view class="upload-area" @click="onUploadLogo">
          <image v-if="previewLogo" class="upload-preview" :src="previewLogo" mode="aspectFill"></image>
          <view v-else class="upload-placeholder">
            <text class="iconfont icon-tubiao_xiangji"></text>
            <text class="upload-tip">点击上传主图</text>
          </view>
        </view>
      </view>

      <!-- 商品图片（多图） -->
      <view class="form-section">
        <view class="section-title">商品图片（可选多张）</view>
        <view class="image-list">
          <view class="image-item" v-for="(img, index) in imageList" :key="index">
            <image class="img-preview" :src="img" mode="aspectFill"></image>
            <view class="img-del" @click="onRemoveImage(index)">
              <text class="iconfont icon-delete"></text>
            </view>
          </view>
          <view v-if="imageList.length < 5" class="image-item image-add" @click="onUploadImages">
            <text class="iconfont icon-add"></text>
            <text class="add-text">添加</text>
          </view>
        </view>
      </view>

      <!-- 基本信息 -->
      <view class="form-section">
        <view class="section-title">基本信息</view>

        <view class="form-item">
          <view class="form-label">商品名称 <text class="required">*</text></view>
          <input class="form-input" v-model="formData.name" placeholder="请输入商品名称" placeholder-style="color:#ccc" />
        </view>

        <view class="form-item">
          <view class="form-label">所属分类</view>
          <picker :range="cateList" range-key="name" @change="onCateChange">
            <view class="form-picker">
              <text :class="{ 'picker-placeholder': !selectedCateName }">
                {{ selectedCateName || '请选择分类' }}
              </text>
              <text class="iconfont icon-xiangyoujiantou"></text>
            </view>
          </picker>
        </view>

        <view class="form-row">
          <view class="form-item form-half">
            <view class="form-label">销售价格 <text class="required">*</text></view>
            <view class="input-with-prefix">
              <text class="prefix">¥</text>
              <input class="form-input-half" v-model="formData.price" type="digit" placeholder="0.00" placeholder-style="color:#ccc" />
            </view>
          </view>
          <view class="form-item form-half">
            <view class="form-label">划线价格</view>
            <view class="input-with-prefix">
              <text class="prefix">¥</text>
              <input class="form-input-half" v-model="formData.linePrice" type="digit" placeholder="0.00" placeholder-style="color:#ccc" />
            </view>
          </view>
        </view>

        <view class="form-row">
          <view class="form-item form-half">
            <view class="form-label">成本价格</view>
            <view class="input-with-prefix">
              <text class="prefix">¥</text>
              <input class="form-input-half" v-model="formData.costPrice" type="digit" placeholder="0.00" placeholder-style="color:#ccc" />
            </view>
          </view>
          <view class="form-item form-half">
            <view class="form-label">库存数量</view>
            <input class="form-input-half" v-model="formData.stock" type="digit" placeholder="0" placeholder-style="color:#ccc" />
          </view>
        </view>

        <view class="form-item">
          <view class="form-label">商品卖点</view>
          <input class="form-input" v-model="formData.salePoint" placeholder="请输入商品卖点" placeholder-style="color:#ccc" />
        </view>

        <view class="form-item">
          <view class="form-label">排序</view>
          <input class="form-input" v-model="formData.sort" type="number" placeholder="数字越小越靠前" placeholder-style="color:#ccc" />
        </view>

        <view class="form-item">
          <view class="form-label">商品描述</view>
          <textarea class="form-textarea" v-model="formData.description" placeholder="请输入商品描述" placeholder-style="color:#ccc" :maxlength="500" :auto-height="true" />
        </view>

        <view class="form-item">
          <view class="form-label">商品状态</view>
          <view class="status-switch">
            <view
              :class="['switch-item', formData.status === 'A' ? 'active' : '']"
              @click="formData.status = 'A'"
            >上架</view>
            <view
              :class="['switch-item', formData.status === 'D' ? 'active' : '']"
              @click="formData.status = 'D'"
            >下架</view>
          </view>
        </view>
      </view>
    </view>

    <!-- 底部按钮 -->
    <view class="submit-bar">
      <button class="btn-submit" @click="onSubmit">保存商品</button>
    </view>
  </view>
</template>

<script>
  import * as GoodsApi from '@/api/merchant/goods'
  import * as UploadApi from '@/api/upload'

  export default {
    data() {
      return {
        isEdit: false,
        goodsId: '',
        cateList: [],
        imagePath: '',
        selectedCateName: '',
        imageList: [],
        imageFileNames: [],
        previewLogo: '',
        formData: {
          name: '',
          cateId: '',
          logo: '',
          price: '',
          linePrice: '',
          costPrice: '',
          stock: '',
          salePoint: '',
          sort: '',
          description: '',
          status: 'A'
        }
      }
    },

    onLoad(options) {
      if (options.goodsId) {
        this.goodsId = options.goodsId
        this.isEdit = true
        uni.setNavigationBarTitle({ title: '编辑商品' })
        this.loadGoodsDetail()
      } else {
        this.loadCateList()
      }
    },

    methods: {
      // 返回
      onBack() {
        uni.navigateBack()
      },

      // 加载分类列表
      loadCateList() {
        const CateApi = require('@/api/merchant/cate')
        CateApi.cateList()
          .then(result => {
            if (result.data && result.data.cateList) {
              this.cateList = result.data.cateList || []
            }
          })
      },

      // 加载商品详情
      loadGoodsDetail() {
        GoodsApi.goodsDetail({ goodsId: this.goodsId })
          .then(result => {
            const goods = result.data.goods
            const imageList = result.data.imageList || []
            const cateList = result.data.cateList || []
            const imagePath = result.data.imagePath || ''

            this.cateList = cateList
            this.imagePath = imagePath
            this.imageList = imageList.map(img => imagePath + img)
            this.imageFileNames = [...imageList]

            // 查找分类名称
            const cate = cateList.find(c => c.id === goods.cateId)
            this.selectedCateName = cate ? cate.name : ''

            this.previewLogo = goods.logo ? goods.logo : ''
            this.formData = {
              name: goods.name || '',
              cateId: goods.cateId || '',
              logo: goods.logo || '',
              price: goods.price != null ? String(goods.price) : '',
              linePrice: goods.linePrice != null ? String(goods.linePrice) : '',
              costPrice: goods.costPrice != null ? String(goods.costPrice) : '',
              stock: goods.stock != null ? String(goods.stock) : '',
              salePoint: goods.salePoint || '',
              sort: goods.sort != null ? String(goods.sort) : '',
              description: goods.description || '',
              status: goods.status || 'A'
            }
          })
      },

      // 分类选择
      onCateChange(e) {
        const index = e.detail.value
        const cate = this.cateList[index]
        if (cate) {
          this.formData.cateId = cate.id
          this.selectedCateName = cate.name
        }
      },

      // 上传主图
      onUploadLogo() {
        const app = this
        uni.chooseImage({
          count: 1,
          sizeType: ['compressed'],
          sourceType: ['album', 'camera'],
          success: function(res) {
            uni.showLoading({ title: '上传中...' })
            app.uploadFile(res.tempFilePaths[0])
              .then(result => {
                uni.hideLoading()
                app.formData.logo = result.fileName
                app.previewLogo = result.url
              })
              .catch(() => {
                uni.hideLoading()
                uni.showToast({ title: '上传失败', icon: 'none' })
              })
          }
        })
      },

      // 上传多图
      onUploadImages() {
        const app = this
        const remain = 5 - app.imageList.length
        uni.chooseImage({
          count: remain,
          sizeType: ['compressed'],
          sourceType: ['album', 'camera'],
          success: function(res) {
            uni.showLoading({ title: '上传中...' })
            const files = res.tempFilePaths
            UploadApi.image(files)
              .then(uploadResults => {
                uni.hideLoading()
                if (uploadResults && uploadResults.length > 0) {
                  uploadResults.forEach(item => {
                    app.imageList.push(item.domain + item.fileName)
                    app.imageFileNames.push(item.fileName)
                  })
                }
              })
              .catch(() => {
                uni.hideLoading()
                uni.showToast({ title: '上传失败', icon: 'none' })
              })
          }
        })
      },

      // 单文件上传
      uploadFile(filePath) {
        return UploadApi.image([filePath])
          .then(results => {
            if (results && results.length > 0) {
              const data = results[0]
              return { fileName: data.fileName, url: data.domain + data.fileName }
            }
            return { fileName: '', url: '' }
          })
      },

      // 删除图片
      onRemoveImage(index) {
        this.imageList.splice(index, 1)
        this.imageFileNames.splice(index, 1)
      },

      // 提交保存
      onSubmit() {
        if (!this.formData.name || this.formData.name.trim() === '') {
          uni.showToast({ title: '请输入商品名称', icon: 'none' })
          return
        }
        if (!this.formData.price || parseFloat(this.formData.price) <= 0) {
          uni.showToast({ title: '请输入销售价格', icon: 'none' })
          return
        }

        // 构建图片数组（主图第一张，其余跟随）
        const images = []
        if (this.formData.logo) {
          images.push(this.formData.logo)
        }
        this.imageFileNames.forEach(fileName => {
          if (!images.includes(fileName)) {
            images.push(fileName)
          }
        })

        const param = {
          goodsId: this.goodsId || '0',
          name: this.formData.name,
          cateId: this.formData.cateId || 0,
          logo: this.formData.logo,
          images: images,
          price: this.formData.price || '0',
          linePrice: this.formData.linePrice || '0',
          costPrice: this.formData.costPrice || '0',
          stock: this.formData.stock || '0',
          salePoint: this.formData.salePoint || '',
          sort: this.formData.sort || '0',
          description: this.formData.description || '',
          status: this.formData.status
        }

        uni.showLoading({ title: '保存中...' })
        GoodsApi.saveGoods(param)
          .then(() => {
            uni.hideLoading()
            uni.showToast({ title: '保存成功', icon: 'success' })
            setTimeout(() => {
              uni.navigateBack()
            }, 1500)
          })
          .catch(err => {
            uni.hideLoading()
            uni.showToast({ title: '保存失败', icon: 'none' })
          })
      }
    }
  }
</script>

<style lang="scss" scoped>
  .container {
    min-height: 100vh;
    background: #f7f7f7;
    padding-bottom: 120rpx;
  }

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20rpx 30rpx;
    background: #fff;
    position: sticky;
    top: 0;
    z-index: 10;

    .header-back {
      width: 60rpx;

      .iconfont {
        font-size: 36rpx;
        color: #333;
      }
    }

    .header-title {
      font-size: 32rpx;
      font-weight: bold;
      color: #333;
    }

    .header-placeholder {
      width: 60rpx;
    }
  }

  .form-wrap {
    padding: 20rpx;
  }

  .form-section {
    background: #fff;
    border-radius: 16rpx;
    padding: 30rpx;
    margin-bottom: 20rpx;

    .section-title {
      font-size: 28rpx;
      font-weight: bold;
      color: #333;
      margin-bottom: 20rpx;
    }
  }

  // 图片上传
  .upload-area {
    width: 180rpx;
    height: 180rpx;
    border-radius: 12rpx;
    overflow: hidden;

    .upload-preview {
      width: 100%;
      height: 100%;
    }

    .upload-placeholder {
      width: 100%;
      height: 100%;
      background: #f7f7f7;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      border: 2rpx dashed #ddd;
      border-radius: 12rpx;

      .iconfont {
        font-size: 48rpx;
        color: #ccc;
      }

      .upload-tip {
        font-size: 22rpx;
        color: #aaa;
        margin-top: 8rpx;
      }
    }
  }

  .image-list {
    display: flex;
    flex-wrap: wrap;
    gap: 16rpx;

    .image-item {
      width: 150rpx;
      height: 150rpx;
      border-radius: 12rpx;
      position: relative;
      overflow: hidden;

      .img-preview {
        width: 100%;
        height: 100%;
      }

      .img-del {
        position: absolute;
        top: 4rpx;
        right: 4rpx;
        width: 36rpx;
        height: 36rpx;
        background: rgba(0, 0, 0, 0.5);
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;

        .iconfont {
          font-size: 24rpx;
          color: #fff;
        }
      }

      &.image-add {
        background: #f7f7f7;
        border: 2rpx dashed #ddd;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;

        .iconfont {
          font-size: 40rpx;
          color: #ccc;
        }

        .add-text {
          font-size: 22rpx;
          color: #aaa;
          margin-top: 4rpx;
        }
      }
    }
  }

  // 表单项
  .form-row {
    display: flex;
    gap: 20rpx;
  }

  .form-item {
    margin-bottom: 24rpx;

    &.form-half {
      flex: 1;
      margin-bottom: 24rpx;
    }

    .form-label {
      font-size: 26rpx;
      color: #666;
      margin-bottom: 12rpx;

      .required {
        color: #ee0a24;
      }
    }

    .form-input {
      width: 100%;
      height: 72rpx;
      background: #f7f7f7;
      border-radius: 10rpx;
      padding: 0 20rpx;
      font-size: 28rpx;
      box-sizing: border-box;
    }

    .form-input-half {
      flex: 1;
      height: 72rpx;
      font-size: 28rpx;
    }

    .input-with-prefix {
      display: flex;
      align-items: center;
      background: #f7f7f7;
      border-radius: 10rpx;
      padding: 0 20rpx;
      height: 72rpx;

      .prefix {
        font-size: 28rpx;
        color: #666;
        margin-right: 4rpx;
      }
    }

    .form-picker {
      display: flex;
      justify-content: space-between;
      align-items: center;
      width: 100%;
      height: 72rpx;
      background: #f7f7f7;
      border-radius: 10rpx;
      padding: 0 20rpx;
      font-size: 28rpx;
      box-sizing: border-box;

      .picker-placeholder {
        color: #ccc;
      }

      .iconfont {
        font-size: 28rpx;
        color: #999;
      }
    }

    .form-textarea {
      width: 100%;
      min-height: 160rpx;
      background: #f7f7f7;
      border-radius: 10rpx;
      padding: 20rpx;
      font-size: 28rpx;
      box-sizing: border-box;
    }

    .status-switch {
      display: flex;
      background: #f7f7f7;
      border-radius: 10rpx;
      overflow: hidden;

      .switch-item {
        flex: 1;
        height: 64rpx;
        line-height: 64rpx;
        text-align: center;
        font-size: 26rpx;
        color: #999;

        &.active {
          background: $fuint-theme;
          color: #fff;
        }
      }
    }
  }

  // 提交按钮
  .submit-bar {
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    padding: 20rpx 30rpx;
    padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
    background: #fff;
    box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.05);

    .btn-submit {
      width: 100%;
      height: 88rpx;
      line-height: 88rpx;
      background: $fuint-theme;
      color: #fff;
      font-size: 32rpx;
      font-weight: bold;
      border-radius: 44rpx;
      border: none;
      text-align: center;
    }
  }
</style>
