import request from '@/utils/request'

// 分页查询下载日志
export function getDownloadLogList(params) {
  return request.get('/api/admin/download-log/list', { params })
}
