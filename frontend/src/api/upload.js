import request from '@/utils/request'

// 封面上传
export function uploadCover(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/api/admin/upload/cover', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
