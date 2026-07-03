import request from '@/utils/request'

// 前台：分页搜索资源
export function searchResources(params) {
  return request.get('/api/front/resource/search', { params })
}

// 前台：资源详情
export function getResourceDetail(id) {
  return request.get(`/api/front/resource/detail/${id}`)
}

// 前台：获取网盘信息
export function getNetdiskInfo(id) {
  return request.get(`/api/front/resource/netdisk/${id}`)
}

// 前台：推荐资源
export function getRecommendResources(limit = 8) {
  return request.get('/api/front/resource/recommend', { params: { limit } })
}

// 前台：热门资源
export function getHotResources(limit = 8) {
  return request.get('/api/front/resource/hot', { params: { limit } })
}

// 前台：最新资源
export function getLatestResources(limit = 12) {
  return request.get('/api/front/resource/latest', { params: { limit } })
}

// 管理端：分页查询
export function getAdminResourcePage(params) {
  return request.get('/api/admin/resource/page', { params })
}

// 管理端：资源详情
export function getAdminResourceDetail(id) {
  return request.get(`/api/admin/resource/${id}`)
}

// 管理端：新增
export function addResource(data) {
  return request.post('/api/admin/resource', data)
}

// 管理端：编辑
export function updateResource(data) {
  return request.put('/api/admin/resource', data)
}

// 管理端：删除
export function deleteResource(id) {
  return request.delete(`/api/admin/resource/${id}`)
}

// 管理端：切换推荐状态
export function toggleRecommend(id) {
  return request.patch(`/api/admin/resource/${id}/recommend`)
}
