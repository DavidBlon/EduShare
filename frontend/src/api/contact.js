import request from '@/utils/request'

// 前台：获取公开联系方式
export function getPublicContact() {
  return request.get('/api/front/contact')
}
