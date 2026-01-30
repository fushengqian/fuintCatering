## 开发

### 环境要求
- Node.js >= 16.0.0 (推荐使用 LTS 版本，如 18.x 或 20.x)
- npm >= 6.0.0
- 最低版本：Node.js 16.0.0
- 推荐版本：Node.js 18.x 或 20.x (LTS 版本)
- 最高支持：Node.js 20.x

### 快速开始

```bash

# 进入项目目录
# 推荐使用 nvm 管理 Node.js 版本
# 使用命令 `nvm use` 切换到项目指定的 Node.js 版本

cd fuintAdmin

# 安装依赖
npm install

# 建议不要直接使用 cnpm 安装依赖，会有各种诡异的 bug。可以通过如下操作解决 npm 下载速度慢的问题
npm install --registry=https://registry.npmmirror.com

# 启动服务
npm run dev
```

浏览器访问 http://localhost:8081

## 发布

```bash
# 构建测试环境
npm run build:stage

# 构建生产环境
npm run build:prod

# 清除缓存
npm cache clean --force
```