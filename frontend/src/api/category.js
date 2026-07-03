import request from '@/utils/request'

// 获取分类树
export function getCategoryTree() {
  return request.get('/api/front/category/tree')
}

// 按层级获取分类
export function getCategoriesByLevel(level) {
  return request.get(`/api/front/category/level/${level}`)
}

// 获取所有分类（管理端）
export function getAllCategories() {
  return request.get('/api/admin/category/list')
}

// 新增分类
export function addCategory(data) {
  return request.post('/api/admin/category', data)
}

// 编辑分类
export function updateCategory(data) {
  return request.put('/api/admin/category', data)
}

// 删除分类
export function deleteCategory(id) {
  return request.delete(`/api/admin/category/${id}`)
}
