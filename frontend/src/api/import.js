import request from '@/utils/request'

/**
 * 解析导入文本
 * @param {string} text
 * @returns {Promise<Array>} 解析后的资源列表
 */
export function parseImportText(text) {
  return request.post('/api/admin/resource/import/parse', { text })
}

/**
 * 批量导入资源
 * @param {Array} resources 解析后的资源列表
 * @returns {Promise}
 */
export function batchImportResources(resources) {
  return request.post('/api/admin/resource/import/batch', { resources })
}
