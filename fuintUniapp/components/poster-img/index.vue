<template>
  <view class="poster-popup" v-if="visible">
    <view class="poster-mask" @click="closePoster"></view>
    <view class="poster-container">
      <!-- 海报标题 -->
      <view class="poster-header">
        <text class="poster-title">分享海报</text>
        <text class="poster-close" @click="closePoster">×</text>
      </view>

      <!-- 海报预览区 -->
      <view class="poster-preview">
        <canvas
          canvas-id="sharePosterCanvas"
          class="poster-canvas"
          :style="{ width: canvasWidth + 'px', height: canvasHeight + 'px' }"
        ></canvas>

        <!-- 海报图片展示 -->
        <image
          v-if="posterImage"
          :src="posterImage"
          class="poster-image"
          mode="widthFix"
          :show-menu-by-longpress="true"
        ></image>
      </view>

      <!-- 操作按钮 -->
      <view class="poster-actions">
        <view class="poster-btn save-btn" @click="savePoster">
          <text>保存到相册</text>
        </view>
        <view class="poster-btn cancel-btn" @click="closePoster">
          <text>取消</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
  import store from '@/store'
  import config from '@/config'
  import { getShareQrCode } from '@/api/share'

  export default {
    name: 'PosterImg',
    props: {
      imgShow: {
        type: Boolean,
        default: false
      },
      // 分享参数（用于生成带参数的二维码）
      shareQuery: {
        type: String,
        default: ''
      },
      // H5 端基础 URL
      h5Url: {
        type: String,
        default: ''
      }
    },
    data() {
      return {
        visible: false,
        posterImage: '',
        canvasWidth: 300,
        canvasHeight: 620,
        qrCodePath: '',
        // 系统信息
        systemInfo: null,
        // 设备像素比（H5 端用于高清绘制）
        pixelRatio: 1
      }
    },
    watch: {
      imgShow: {
        immediate: true,
        handler(val) {
          this.visible = val
          if (val) {
            this.$nextTick(() => {
              this.generatePoster()
            })
          }
        }
      },
      visible(val) {
        if (!val) {
          this.$emit('update:imgShow', false)
        }
      }
    },
    mounted() {
      this.initSystemInfo()
    },
    methods: {
      /**
       * 获取系统信息（兼容新版 API）
       */
      initSystemInfo() {
        try {
          // #ifdef MP-WEIXIN
          // 新版 API：getWindowInfo（基础库 2.20.1+）
          if (wx.getWindowInfo) {
            this.systemInfo = wx.getWindowInfo()
          } else {
            this.systemInfo = uni.getSystemInfoSync()
          }
          // #endif
          // #ifndef MP-WEIXIN
          this.systemInfo = uni.getSystemInfoSync()
          // #endif
          // #ifdef H5
          this.pixelRatio = window.devicePixelRatio || 2
          // #endif
        } catch (e) {
          console.error('initSystemInfo error:', e)
          this.systemInfo = null
          // #ifdef H5
          this.pixelRatio = window.devicePixelRatio || 2
          // #endif
        }
      },

      /**
       * 关闭海报
       */
      closePoster() {
        this.visible = false
        this.posterImage = ''
      },

      /**
       * 生成海报
       */
      generatePoster() {
        const app = this
        // 确保 systemInfo 已初始化
        if (!this.systemInfo) {
          this.initSystemInfo()
        }
        // 适配不同屏幕，兜底使用 375
        const screenWidth = this.systemInfo ? this.systemInfo.windowWidth : 375
        const scale = screenWidth / 375
        const pr = this.pixelRatio || 1
        // Canvas 实际绘制分辨率 = 显示尺寸 × 设备像素比，确保 H5 端高清
        this.canvasWidth = 300 * scale * pr
        this.canvasHeight = 620 * scale * pr

        // 先获取二维码
        uni.showLoading({ title: '生成海报中...' })
        app.fetchQrCode().then(() => {
          app.$nextTick(() => {
            app.drawPoster(scale, pr)
          })
        }).catch(() => {
          uni.hideLoading()
          // 即使二维码获取失败，也尝试绘制（不显示二维码）
          app.$nextTick(() => {
            app.drawPoster(scale, pr)
          })
        })
      },

      /**
       * 获取小程序码
       */
      fetchQrCode() {
        const app = this
        return new Promise((resolve, reject) => {
          // #ifdef MP-WEIXIN
          getShareQrCode({
            path: 'pages/index/index',
            query: app.shareQuery || '',
            width: 430,
            platform: 'mp'
          }).then(res => {
            let qrCodeData = res && res.data && res.data.qrCode ? res.data.qrCode : ''
            if (!qrCodeData) {
              reject(new Error('未获取到小程序码'))
              return
            }
            // 后端返回 base64 data URL，写入临时文件供 Canvas 使用
            let base64Data = qrCodeData
            if (base64Data.startsWith('data:image/png;base64,')) {
              base64Data = base64Data.replace('data:image/png;base64,', '')
            }
            const fs = wx.getFileSystemManager()
            const tempFilePath = `${wx.env.USER_DATA_PATH}/temp_qrcode_${Date.now()}.png`
            try {
              fs.writeFileSync(tempFilePath, base64Data, 'base64')
              app.qrCodePath = tempFilePath
              console.log('小程序码临时文件:', tempFilePath)
              resolve()
            } catch (e) {
              console.error('写入小程序码临时文件失败:', e)
              reject(e)
            }
          }).catch(err => {
            console.error('获取小程序码失败:', err)
            reject(err)
          })
          // #endif
          // #ifdef H5
          getShareQrCode({
            path: app.h5Url || '',
            query: app.shareQuery || '',
            width: 430,
            platform: 'h5'
          }).then(res => {
            let qrCodeData = res && res.data && res.data.qrCode ? res.data.qrCode : ''
            if (!qrCodeData) {
              reject(new Error('未获取到二维码'))
              return
            }
            // 后端直接返回 base64 data URL，无需 downloadFile，彻底避免跨域问题
            console.log('h5 qrCode data received, length:', qrCodeData.length)
            app.qrCodePath = qrCodeData
            resolve()
          }).catch(err => {
            console.error('获取 H5 二维码失败:', err)
            reject(err)
          })
          // #endif
        })
      },

      /**
       * 绘制海报
       */
      drawPoster(scale, pr) {
        const app = this
        const ctx = uni.createCanvasContext('sharePosterCanvas', this)

        // H5 端按设备像素比缩放绘制上下文，实现高清输出
        pr = pr || 1
        ctx.scale(pr, pr)

        // 使用显示尺寸（未乘以 pixelRatio 的值）进行布局计算
        const cw = app.canvasWidth / pr
        const ch = app.canvasHeight / pr

        // 背景色
        ctx.setFillStyle('#113a28')
        ctx.fillRect(0, 0, cw, ch)

        // 顶部白色装饰圆
        ctx.setFillStyle('rgba(255,255,255,0.15)')
        ctx.beginPath()
        ctx.arc(cw * 0.85, 30 * scale, 60 * scale, 0, 2 * Math.PI)
        ctx.fill()

        // 底部白色装饰圆
        ctx.setFillStyle('rgba(255,255,255,0.08)')
        ctx.beginPath()
        ctx.arc(cw * 0.15, ch - 40 * scale, 80 * scale, 0, 2 * Math.PI)
        ctx.fill()

        // 主内容区 - 白色卡片
        const cardX = 15 * scale
        const cardY = 60 * scale
        const cardW = cw - 30 * scale
        const cardH = ch - 140 * scale

        // 卡片阴影
        ctx.setFillStyle('rgba(0,0,0,0.15)')
        ctx.fillRect(cardX + 3, cardY + 3, cardW, cardH)

        // 卡片背景
        ctx.setFillStyle('#ffffff')
        ctx.fillRect(cardX, cardY, cardW, cardH)

        // 卡片顶部装饰条
        ctx.setFillStyle('#113a28')
        ctx.fillRect(cardX, cardY, cardW, 6 * scale)

        // 应用名称
        ctx.setFillStyle('#333333')
        ctx.setFontSize(20 * scale)
        ctx.setTextAlign('center')
        const appName = config.name || 'fuint会员系统'
        ctx.fillText(appName, cw / 2, cardY + 45 * scale)

        // subtitle
        ctx.setFillStyle('#113a28')
        ctx.setFontSize(16 * scale)
        ctx.fillText('邀请你一起加入', cw / 2, cardY + 75 * scale)

        // 分隔线
        const lineY = cardY + 95 * scale
        ctx.setStrokeStyle('#eeeeee')
        ctx.setLineWidth(1)
        ctx.beginPath()
        ctx.moveTo(cardX + 30 * scale, lineY)
        ctx.lineTo(cardX + cardW - 30 * scale, lineY)
        ctx.stroke()

        // 特性描述
        const features = ['品质服务 · 值得信赖', '会员专享 · 尊贵体验', '注册即享 · 新人大礼包']
        const featureStartY = lineY + 35 * scale
        ctx.setFillStyle('#666666')
        ctx.setFontSize(13 * scale)
        features.forEach((feature, index) => {
          ctx.fillText(feature, cw / 2, featureStartY + index * 28 * scale)
        })

        // 邀请人信息
        const inviteY = featureStartY + features.length * 28 * scale + 30 * scale
        ctx.setFillStyle('#999999')
        ctx.setFontSize(12 * scale)
        const userInfo = store.getters.userInfo || {}
        const inviterName = userInfo.name || '好友'
        ctx.fillText(`— ${inviterName} 邀请 —`, cw / 2, inviteY)

        // 绘制完成后的回调（先画背景文字，二维码通过 drawImage 异步加载后绘制）
        const finalizePoster = () => {
          ctx.draw(false, () => {
            setTimeout(() => {
              uni.canvasToTempFilePath({
                canvasId: 'sharePosterCanvas',
                width: app.canvasWidth,
                height: app.canvasHeight,
                quality: 1,
                success: (res) => {
                  uni.hideLoading()
                  app.posterImage = res.tempFilePath
                },
                fail: (err) => {
                  uni.hideLoading()
                  console.error('生成海报失败:', err)
                  uni.showToast({ title: '生成海报失败', icon: 'none' })
                }
              }, app)
            }, 600)
          })
        }

        // 绘制小程序码
        if (app.qrCodePath) {
          const qrSize = 140 * scale
          const qrX = (cw - qrSize) / 2
          const qrY = inviteY + 25 * scale

          // 先画一个小圆角白色背景
          ctx.setFillStyle('#ffffff')
          const bgSize = qrSize + 16 * scale
          const bgX = qrX - 8 * scale
          const bgY = qrY - 8 * scale
          ctx.fillRect(bgX, bgY, bgSize, bgSize)

          // 绘制二维码图片
          ctx.drawImage(app.qrCodePath, qrX, qrY, qrSize, qrSize)

          // 底部提示（在二维码下方）
          const bottomTextY = qrY + qrSize + 25 * scale
          ctx.setFillStyle('#113a28')
          ctx.setFontSize(14 * scale)
          // #ifdef H5
          ctx.fillText('长按/扫码识别 立即加入', cw / 2, bottomTextY)
          // #endif
          // #ifndef H5
          ctx.fillText('长按识别小程序码 立即加入', cw / 2, bottomTextY)
          // #endif

          // 底部版权信息
          ctx.setFillStyle('#999999')
          ctx.setFontSize(10 * scale)
          ctx.fillText(config.name || '', cw / 2, bottomTextY + 25 * scale)

          finalizePoster()
        } else {
          // 无二维码时，保留原有文字底部
          const bottomY = cardY + cardH - 40 * scale
          ctx.setFillStyle('#113a28')
          ctx.setFontSize(14 * scale)
          // #ifdef H5
          ctx.fillText('长按/扫码识别 立即加入', cw / 2, bottomY)
          // #endif
          // #ifndef H5
          ctx.fillText('长按识别小程序码 立即加入', cw / 2, bottomY)
          // #endif

          ctx.setFillStyle('#999999')
          ctx.setFontSize(10 * scale)
          ctx.fillText(config.name || '', cw / 2, bottomY + 25 * scale)

          finalizePoster()
        }
      },

      /**
       * 保存海报到相册
       */
      savePoster() {
        if (!this.posterImage) {
          uni.showToast({ title: '海报生成中，请稍候', icon: 'none' })
          return
        }
        // #ifdef MP-WEIXIN
        uni.getSetting({
          success: (res) => {
            if (!res.authSetting['scope.writePhotosAlbum']) {
              uni.authorize({
                scope: 'scope.writePhotosAlbum',
                success: () => {
                  this.doSaveToAlbum()
                },
                fail: () => {
                  uni.showModal({
                    title: '提示',
                    content: '需要授权保存到相册',
                    confirmText: '去设置',
                    success: (modalRes) => {
                      if (modalRes.confirm) {
                        uni.openSetting()
                      }
                    }
                  })
                }
              })
            } else {
              this.doSaveToAlbum()
            }
          }
        })
        // #endif
        // #ifndef MP-WEIXIN
        this.doSaveToAlbum()
        // #endif
      },

      /**
       * 执行保存到相册
       */
      doSaveToAlbum() {
        uni.saveImageToPhotosAlbum({
          filePath: this.posterImage,
          success: () => {
            uni.showToast({ title: '已保存到相册', icon: 'success' })
          },
          fail: () => {
            uni.showToast({ title: '保存失败', icon: 'none' })
          }
        })
      }
    }
  }
</script>

<style lang="scss" scoped>
  .poster-popup {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    z-index: 9999;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
  }

  .poster-mask {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.7);
  }

  .poster-container {
    position: relative;
    width: 90vw;
    max-width: 600rpx;
    background: #fff;
    border-radius: 24rpx;
    overflow: hidden;
    padding-bottom: 20rpx;
  }

  .poster-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 24rpx 30rpx;
    border-bottom: 1rpx solid #f0f0f0;
  }

  .poster-title {
    font-size: 32rpx;
    font-weight: bold;
    color: #333;
  }

  .poster-close {
    font-size: 48rpx;
    color: #999;
    line-height: 1;
    padding: 0 10rpx;
  }

  .poster-preview {
    display: flex;
    justify-content: center;
    align-items: center;
    padding: 30rpx 40rpx;
    min-height: 400rpx;
  }

  .poster-canvas {
    position: fixed;
    top: -9999px;
    left: -9999px;
  }

  .poster-image {
    width: 100%;
    border-radius: 12rpx;
    box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.1);
  }

  .poster-actions {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 24rpx;
    padding: 10rpx 30rpx;
  }

  .poster-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 80rpx;
    border-radius: 40rpx;
    font-size: 28rpx;
    padding: 0 40rpx;

    .iconfont {
      font-size: 32rpx;
      margin-right: 10rpx;
    }
  }

  .save-btn {
    flex: 1;
    background: linear-gradient(135deg, #113a28, #2ab075);
    color: #fff;
    max-width: 280rpx;
  }

  .cancel-btn {
    width: 160rpx;
    background: #f5f5f5;
    color: #666;
  }
</style>
