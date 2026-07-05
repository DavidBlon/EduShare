import request from '@/utils/request'

// 封面上传
export function uploadCover(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/api/admin/upload/cover', formData)
}

// 微信群二维码上传
export function uploadQrCode(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/api/admin/upload/qrcode', formData)
}
