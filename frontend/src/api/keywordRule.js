import request from '@/utils/request'

// 获取所有规则（按类型分组）
export function getKeywordRuleList() {
  return request.get('/api/admin/keyword-rule/list')
}

// 新增规则
export function addKeywordRule(data) {
  return request.post('/api/admin/keyword-rule', data)
}

// 编辑规则
export function updateKeywordRule(data) {
  return request.put('/api/admin/keyword-rule', data)
}

// 删除规则
export function deleteKeywordRule(id) {
  return request.delete(`/api/admin/keyword-rule/${id}`)
}
