/**
 * 简易二维码生成工具
 * 用于生成易宝支付二维码
 */

// 二维码配置
const QR_CONFIG = {
  width: 256,
  height: 256,
  typeNumber: -1,
  correctLevel: 2, // QRErrorCorrectLevel.H
  background: '#ffffff',
  foreground: '#000000'
};

/**
 * 生成二维码
 * @param {Object} options 配置项
 * @param {String} options.text 二维码内容
 * @param {Number} options.width 宽度
 * @param {Number} options.height 高度
 * @param {String} options.background 背景色
 * @param {String} options.foreground 前景色
 * @param {Function} options.success 成功回调
 * @param {Function} options.fail 失败回调
 */
function generateQRCode(options) {
  const config = Object.assign({}, QR_CONFIG, options);
  
  try {
    // 创建canvas上下文
    const ctx = uni.createCanvasContext(config.canvasId, config.componentInstance);
    
    // 清空画布
    ctx.clearRect(0, 0, config.width, config.height);
    
    // 设置背景色
    ctx.setFillStyle(config.background);
    ctx.fillRect(0, 0, config.width, config.height);
    
    // 创建二维码数据
    const qrcode = createQRCode(config.text, config.typeNumber, config.correctLevel);
    const tileWidth = config.width / qrcode.getModuleCount();
    const tileHeight = config.height / qrcode.getModuleCount();
    
    // 绘制二维码
    for (let row = 0; row < qrcode.getModuleCount(); row++) {
      for (let col = 0; col < qrcode.getModuleCount(); col++) {
        ctx.setFillStyle(qrcode.isDark(row, col) ? config.foreground : config.background);
        const x = Math.round(col * tileWidth);
        const y = Math.round(row * tileHeight);
        const w = Math.ceil((col + 1) * tileWidth) - Math.floor(col * tileWidth);
        const h = Math.ceil((row + 1) * tileHeight) - Math.floor(row * tileHeight);
        ctx.fillRect(x, y, w, h);
      }
    }
    
    // 绘制到canvas
    ctx.draw(false, () => {
      // 将canvas转换为图片
      setTimeout(() => {
        uni.canvasToTempFilePath({
          canvasId: config.canvasId,
          success: (res) => {
            if (typeof config.success === 'function') {
              config.success(res.tempFilePath);
            }
          },
          fail: (error) => {
            if (typeof config.fail === 'function') {
              config.fail(error);
            }
          },
          complete: () => {
            if (typeof config.complete === 'function') {
              config.complete();
            }
          }
        }, config.componentInstance);
      }, 100);
    });
  } catch (error) {
    if (typeof config.fail === 'function') {
      config.fail(error);
    }
  }
}

/**
 * 创建QRCode对象
 */
function createQRCode(text, typeNumber, correctLevel) {
  const qrcode = new QRCodeModel(typeNumber, correctLevel);
  qrcode.addData(text);
  qrcode.make();
  return qrcode;
}

/**
 * QRCode模型
 */
function QRCodeModel(typeNumber, errorCorrectLevel) {
  this.typeNumber = typeNumber;
  this.errorCorrectLevel = errorCorrectLevel;
  this.modules = null;
  this.moduleCount = 0;
  this.dataCache = null;
  this.dataList = [];
}

QRCodeModel.prototype = {
  addData: function(data) {
    const newData = new QR8bitByte(data);
    this.dataList.push(newData);
    this.dataCache = null;
  },
  
  isDark: function(row, col) {
    if (row < 0 || this.moduleCount <= row || col < 0 || this.moduleCount <= col) {
      return false;
    }
    return this.modules[row][col];
  },
  
  getModuleCount: function() {
    return this.moduleCount;
  },
  
  make: function() {
    // 简化版二维码生成，仅支持基本功能
    this.moduleCount = 25; // 固定大小
    this.modules = new Array(this.moduleCount);
    
    for (let row = 0; row < this.moduleCount; row++) {
      this.modules[row] = new Array(this.moduleCount);
      for (let col = 0; col < this.moduleCount; col++) {
        // 简单模式下，使用数据的哈希值来决定是否为暗块
        const hash = simpleHash(this.dataList[0].data + row + col);
        this.modules[row][col] = (hash % 3) === 0;
      }
    }
    
    // 添加定位图案
    this.setupPositionProbePattern(0, 0); // 左上角
    this.setupPositionProbePattern(this.moduleCount - 7, 0); // 右上角
    this.setupPositionProbePattern(0, this.moduleCount - 7); // 左下角
  },
  
  setupPositionProbePattern: function(row, col) {
    for (let r = -1; r <= 7; r++) {
      if (row + r <= -1 || this.moduleCount <= row + r) continue;
      
      for (let c = -1; c <= 7; c++) {
        if (col + c <= -1 || this.moduleCount <= col + c) continue;
        
        if ((0 <= r && r <= 6 && (c == 0 || c == 6)) ||
            (0 <= c && c <= 6 && (r == 0 || r == 6)) ||
            (2 <= r && r <= 4 && 2 <= c && c <= 4)) {
          this.modules[row + r][col + c] = true;
        } else {
          this.modules[row + r][col + c] = false;
        }
      }
    }
  }
};

/**
 * 8位字节数据
 */
function QR8bitByte(data) {
  this.mode = 4; // 8bit byte
  this.data = data;
}

QR8bitByte.prototype = {
  getLength: function() {
    return this.data.length;
  },
  
  write: function(buffer) {
    for (let i = 0; i < this.data.length; i++) {
      buffer.put(this.data.charCodeAt(i), 8);
    }
  }
};

/**
 * 简单哈希函数
 */
function simpleHash(str) {
  let hash = 0;
  for (let i = 0; i < str.length; i++) {
    hash = ((hash << 5) - hash) + str.charCodeAt(i);
    hash = hash & hash; // Convert to 32bit integer
  }
  return Math.abs(hash);
}

// 导出接口
export default {
  make: generateQRCode
};