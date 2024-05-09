import request from '@/utils/request'

// api地址
const api = {
  uploadUrl: 'clientApi/file/upload'
}

// 图片上传
export const image = (files) => {
  // 文件上传大小, 2M
  const maxSize = 1024 * 1024 * 2;
  // 执行上传
  return new Promise((resolve, reject) => {
    request.urlFileUpload({ files, maxSize })
      .then(result => {
        const fileIds = result.map(item => {
            return item.data;
        })
        resolve(fileIds, result)
      })
      .catch(err => reject(err))
  })
}
