import request from '@/utils/request'

// 前台：获取免责声明
export function getPublicDisclaimer() {
  return request.get('/api/front/disclaimer')
}
