import request from '@/utils/request'

// 获取所有标签（含资源数）
export function getTagList() {
  return request.get('/api/front/tag/list')
}

// 获取所有标签（管理端，与前端一致）
export function getAdminTagList() {
  return request.get('/api/admin/tag/list')
}

// 新增标签
export function addTag(data) {
  return request.post('/api/admin/tag', data)
}

// 编辑标签
export function updateTag(data) {
  return request.put('/api/admin/tag', data)
}

// 删除标签
export function deleteTag(id) {
  return request.delete(`/api/admin/tag/${id}`)
}
