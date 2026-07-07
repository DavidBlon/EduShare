import request from '@/utils/request'

// 分页查询下载日志
export function getDownloadLogList(params) {
  return request.get('/api/admin/download-log/list', { params })
}

// 获取下载统计（按资源分组）
export function getDownloadStats(limit = 10) {
  return request.get('/api/admin/download-log/stats', { params: { limit } })
}

// 获取每日下载统计（最近 N 天）
export function getDailyDownloadStats(days = 15) {
  return request.get('/api/admin/download-log/daily-stats', { params: { days } })
}

// 获取某日下载量 Top N 资源
export function getDailyTopResources(date, limit = 3) {
  return request.get('/api/admin/download-log/daily-top', { params: { date, limit } })
}
