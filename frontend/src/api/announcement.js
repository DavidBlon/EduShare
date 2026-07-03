import request from '@/utils/request'

// 管理端
export function getAdminAnnouncementPage(params) {
  return request.get('/api/admin/announcement/page', { params })
}

export function addAnnouncement(data) {
  return request.post('/api/admin/announcement', data)
}

export function updateAnnouncement(data) {
  return request.put('/api/admin/announcement', data)
}

export function togglePublish(id, isPublished) {
  return request.put('/api/admin/announcement/publish', { id, isPublished })
}

export function deleteAnnouncement(id) {
  return request.delete(`/api/admin/announcement/${id}`)
}

export function getAnnouncementDetail(id) {
  return request.get(`/api/admin/announcement/${id}`)
}

// 前台
export function getPublishedAnnouncementPage(params) {
  return request.get('/api/front/announcement/page', { params })
}

export function getPublishedAnnouncementDetail(id) {
  return request.get(`/api/front/announcement/${id}`)
}

/** 获取未读公告数量 */
export function getUnreadCount(lastReadId) {
  return request.get('/api/front/announcement/unread-count', { params: { lastReadId } })
}

/** 获取最近的公告列表（弹窗展示） */
export function getRecentAnnouncements() {
  return request.get('/api/front/announcement/recent')
}
