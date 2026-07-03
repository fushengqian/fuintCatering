<template>
  <view class="container">
    <!-- 页面标题 -->
    <view class="page-header">
      <view class="header-title">商品分类</view>
      <view class="header-add" @click="onAdd">
        <text class="iconfont icon-add"></text>
        <text>新增分类</text>
      </view>
    </view>

    <!-- 分类列表 -->
    <view class="cate-list" v-if="cateList.length > 0">
      <view class="cate-item" v-for="(item, index) in cateList" :key="index">
        <view class="cate-left" @click="onEdit(item)">
          <image v-if="item.logo" class="cate-logo" :src="getImageUrl(item.logo)" mode="aspectFill"></image>
          <view v-else class="cate-logo-placeholder">
            <text class="iconfont icon-fenlei"></text>
          </view>
          <view class="cate-info">
            <view class="cate-name">{{ item.name }}</view>
            <view class="cate-desc" v-if="item.description">{{ item.description }}</view>
            <view class="cate-meta">
              <text class="cate-sort">排序: {{ item.sort || 0 }}</text>
              <text :class="['cate-status', item.status === 'A' ? 'status-on' : 'status-off']">
                {{ item.status === 'A' ? '启用' : '禁用' }}
              </text>
            </view>
          </view>
        </view>
        <view class="cate-actions">
          <text class="action-btn action-edit" @click="onEdit(item)">编辑</text>
          <text v-if="item.status === 'A'" class="action-btn action-disable" @click="onToggleStatus(item, 'N')">禁用</text>
          <text v-else class="action-btn action-enable" @click="onToggleStatus(item, 'A')">启用</text>
          <text class="action-btn action-delete" @click="onDelete(item)">删除</text>
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <view class="empty-wrap" v-else>
      <view class="empty-icon iconfont icon-wushuju"></view>
      <view class="empty-text">暂无分类，点击新增吧</view>
    </view>

    <!-- 新增/编辑弹窗 -->
    <u-popup v-model="showPopup" mode="center" border-radius="14" :closeable="true" width="620rpx">
      <view class="popup-content">
        <view class="popup-title">{{ isEdit ? '编辑分类' : '新增分类' }}</view>

        <!-- 分类图片 -->
        <view class="form-item">
          <view class="form-label">分类图片</view>
          <view class="upload-area" @click="onUploadLogo">
            <image v-if="previewLogo" class="upload-preview" :src="previewLogo" mode="aspectFill"></image>
            <view v-else class="upload-placeholder">
              <text class="iconfont icon-tubiao_xiangji"></text>
              <text class="upload-tip">点击上传</text>
            </view>
          </view>
        </view>

        <!-- 分类名称 -->
        <view class="form-item">
          <view class="form-label">分类名称</view>
          <input class="form-input" v-model="formData.name" placeholder="请输入分类名称" placeholder-style="color:#ccc" />
        </view>

        <!-- 分类描述 -->
        <view class="form-item">
          <view class="form-label">分类描述</view>
          <input class="form-input" v-model="formData.description" placeholder="请输入分类描述" placeholder-style="color:#ccc" />
        </view>

        <!-- 排序 -->
        <view class="form-item">
          <view class="form-label">排序</view>
          <input class="form-input" v-model="formData.sort" type="number" placeholder="数字越小越靠前" placeholder-style="color:#ccc" />
        </view>

        <!-- 按钮 -->
        <view class="popup-btns">
          <button class="btn-cancel" @click="showPopup = false">取消</button>
          <button class="btn-confirm" @click="onSave">保存</button>
        </view>
      </view>
    </u-popup>
  </view>
</template>

<script>
  import * as CateApi from '@/api/merchant/cate'
  import * as UploadApi from '@/api/upload'

  export default {
    data() {
      return {
        imagePath: '',
        cateList: [],
        showPopup: false,
        isEdit: false,
        previewLogo: '',
        formData: {
          id: '',
          name: '',
          logo: '',
          description: '',
          sort: ''
        }
      }
    },
    onShow() {
      this.getCateList()
    },
    methods: {
      // 获取图片完整URL（兼容已有完整URL和相对路径）
      getImageUrl(logo) {
        if (!logo) return ''
        if (logo.startsWith('http://') || logo.startsWith('https://')) {
          return logo
        }
        return (this.imagePath || '') + logo
      },

      // 获取分类列表
      getCateList() {
        CateApi.cateList()
          .then(result => {
            if (result.data) {
              this.imagePath = result.data.imagePath || ''
              this.cateList = result.data.allCateList || []
            }
          })
          .catch(err => {
            console.log('获取分类列表失败', err)
          })
      },

      // 新增分类
      onAdd() {
        this.isEdit = false
        this.previewLogo = ''
        this.formData = {
          id: '',
          name: '',
          logo: '',
          description: '',
          sort: ''
        }
        this.showPopup = true
      },

      // 编辑分类
      onEdit(item) {
        this.isEdit = true
        this.formData = {
          id: item.id,
          name: item.name,
          logo: item.logo || '',
          description: item.description || '',
          sort: item.sort != null ? String(item.sort) : ''
        }
        this.previewLogo = this.getImageUrl(item.logo)
        this.showPopup = true
      },

      // 上传分类图片
      onUploadLogo() {
        const app = this
        uni.chooseImage({
          count: 1,
          sizeType: ['compressed'],
          sourceType: ['album', 'camera'],
          success: function(res) {
            const tempFilePath = res.tempFilePaths[0]
            uni.showLoading({ title: '上传中...' })
            UploadApi.image([tempFilePath])
              .then(fileIds => {
                uni.hideLoading()
                if (fileIds && fileIds.length > 0) {
                  const uploadData = fileIds[0]
                  app.formData.logo = uploadData.fileName
                  app.previewLogo = uploadData.domain + uploadData.fileName
                }
              })
              .catch(err => {
                uni.hideLoading()
                uni.showToast({ title: '上传失败', icon: 'none' })
              })
          }
        })
      },

      // 保存分类
      onSave() {
        if (!this.formData.name || this.formData.name.trim() === '') {
          uni.showToast({ title: '请输入分类名称', icon: 'none' })
          return
        }

        const param = {
          id: this.formData.id || '',
          name: this.formData.name,
          logo: this.formData.logo,
          description: this.formData.description,
          sort: this.formData.sort || '0'
        }

        uni.showLoading({ title: '保存中...' })
        CateApi.saveCate(param)
          .then(() => {
            uni.hideLoading()
            uni.showToast({ title: '保存成功', icon: 'success' })
            this.showPopup = false
            this.getCateList()
          })
          .catch(err => {
            uni.hideLoading()
            uni.showToast({ title: '保存失败', icon: 'none' })
          })
      },

      // 启用/禁用
      onToggleStatus(item, status) {
        const tip = status === 'A' ? '确认启用该分类？' : '确认禁用该分类？'
        uni.showModal({
          title: '提示',
          content: tip,
          success: (res) => {
            if (res.confirm) {
              CateApi.updateCateStatus({ id: item.id, status: status })
                .then(() => {
                  uni.showToast({ title: '操作成功', icon: 'success' })
                  this.getCateList()
                })
                .catch(() => {
                  uni.showToast({ title: '操作失败', icon: 'none' })
                })
            }
          }
        })
      },

      // 删除分类
      onDelete(item) {
        uni.showModal({
          title: '提示',
          content: '确认删除该分类？删除后不可恢复',
          success: (res) => {
            if (res.confirm) {
              CateApi.updateCateStatus({ id: item.id, status: 'D' })
                .then(() => {
                  uni.showToast({ title: '删除成功', icon: 'success' })
                  this.getCateList()
                })
                .catch(() => {
                  uni.showToast({ title: '删除失败', icon: 'none' })
                })
            }
          }
        })
      }
    }
  }
</script>

<style lang="scss" scoped>
  .container {
    min-height: 100vh;
    background: #f7f7f7;
    padding-bottom: 30rpx;
  }

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 30rpx 30rpx 20rpx;
    background: #fff;
    margin-bottom: 20rpx;

    .header-title {
      font-size: 32rpx;
      font-weight: bold;
      color: #333;
    }

    .header-add {
      display: flex;
      align-items: center;
      font-size: 26rpx;
      color: $fuint-theme;
      font-weight: bold;

      .iconfont {
        margin-right: 8rpx;
        font-size: 30rpx;
      }
    }
  }

  .cate-list {
    padding: 0 20rpx;
  }

  .cate-item {
    background: #fff;
    border-radius: 16rpx;
    padding: 24rpx;
    margin-bottom: 16rpx;

    .cate-left {
      display: flex;
      align-items: center;
    }

    .cate-logo {
      width: 100rpx;
      height: 100rpx;
      border-radius: 12rpx;
      background: #f5f5f5;
      flex-shrink: 0;
    }

    .cate-logo-placeholder {
      width: 100rpx;
      height: 100rpx;
      border-radius: 12rpx;
      background: #f5f5f5;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;

      .iconfont {
        font-size: 44rpx;
        color: #ccc;
      }
    }

    .cate-info {
      flex: 1;
      margin-left: 20rpx;

      .cate-name {
        font-size: 30rpx;
        font-weight: bold;
        color: #333;
        margin-bottom: 6rpx;
      }

      .cate-desc {
        font-size: 24rpx;
        color: #999;
        margin-bottom: 6rpx;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .cate-meta {
        display: flex;
        align-items: center;
        gap: 16rpx;

        .cate-sort {
          font-size: 22rpx;
          color: #aaa;
        }

        .cate-status {
          font-size: 22rpx;
          padding: 2rpx 12rpx;
          border-radius: 4rpx;

          &.status-on {
            color: #07c160;
            background: #e8f8ef;
          }

          &.status-off {
            color: #999;
            background: #f5f5f5;
          }
        }
      }
    }

    .cate-actions {
      display: flex;
      justify-content: flex-end;
      margin-top: 20rpx;
      padding-top: 20rpx;
      border-top: 1rpx solid #f0f0f0;
      gap: 20rpx;

      .action-btn {
        font-size: 24rpx;
        padding: 8rpx 24rpx;
        border-radius: 30rpx;
        border: 1rpx solid #eee;

        &.action-edit {
          color: $fuint-theme;
          border-color: $fuint-theme;
        }

        &.action-enable {
          color: #07c160;
          border-color: #07c160;
        }

        &.action-disable {
          color: #ff976a;
          border-color: #ff976a;
        }

        &.action-delete {
          color: #ee0a24;
          border-color: #ee0a24;
        }
      }
    }
  }

  .empty-wrap {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding-top: 200rpx;

    .empty-icon {
      font-size: 120rpx;
      color: #ddd;
    }

    .empty-text {
      margin-top: 20rpx;
      font-size: 28rpx;
      color: #999;
    }
  }

  // 弹窗样式
  .popup-content {
    padding: 40rpx 30rpx 30rpx;

    .popup-title {
      text-align: center;
      font-size: 32rpx;
      font-weight: bold;
      color: #333;
      margin-bottom: 30rpx;
    }

    .form-item {
      margin-bottom: 24rpx;

      .form-label {
        font-size: 26rpx;
        color: #666;
        margin-bottom: 12rpx;
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

      .upload-area {
        width: 120rpx;
        height: 120rpx;
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
            font-size: 36rpx;
            color: #ccc;
          }

          .upload-tip {
            font-size: 20rpx;
            color: #ccc;
            margin-top: 4rpx;
          }
        }
      }
    }

    .popup-btns {
      display: flex;
      justify-content: space-between;
      margin-top: 30rpx;
      gap: 20rpx;

      .btn-cancel, .btn-confirm {
        flex: 1;
        height: 72rpx;
        line-height: 72rpx;
        text-align: center;
        border-radius: 36rpx;
        font-size: 28rpx;
        border: none;
        padding: 0;
      }

      .btn-cancel {
        background: #f5f5f5;
        color: #666;
      }

      .btn-confirm {
        background: $fuint-theme;
        color: #fff;
      }
    }
  }
</style>
