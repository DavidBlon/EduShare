import request from '@/utils/request'

// 登录
export function login(data) {
  return request.post('/api/admin/login', data)
}

// 获取管理员信息
export function getProfile() {
  return request.get('/api/admin/profile')
}

// 修改密码
export function updatePassword(data) {
  return request.put('/api/admin/password', data)
}

// 获取管理员列表
export function getAdminList() {
  return request.get('/api/admin/admins')
}

// 新增管理员
export function addAdmin(data) {
  return request.post('/api/admin/admin', data)
}

// 编辑管理员
export function updateAdmin(data) {
  return request.put('/api/admin/admin', data)
}

// 删除管理员
export function deleteAdmin(id) {
  return request.delete(`/api/admin/admin/${id}`)
}

// 获取联系方式
export function getContact() {
  return request.get('/api/admin/contact')
}

// 更新联系方式
export function updateContact(data) {
  return request.put('/api/admin/contact', data)
}
