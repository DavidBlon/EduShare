<template>
  <div class="keyword-rule">
    <div class="page-header">
      <div class="header-left">
        <h2>关键词规则</h2>
        <span class="header-desc">管理批量导入时的自动解析规则，新增规则后解析器会自动生效</span>
      </div>
      <el-button type="primary" @click="openAdd">
        <el-icon><Plus /></el-icon> 新增规则
      </el-button>
    </div>

    <el-card shadow="never">
      <el-tabs v-model="activeTab" @tab-change="onTabChange">
        <el-tab-pane label="标签匹配规则" name="TAG">
          <el-alert
            :title="'标题包含关键词时自动添加对应标签。例如关键词“期中” → 标签“期中考试”'"
            type="info"
            :closable="false"
            show-icon
            class="tab-alert"
          />
          <el-table :data="tagRules" v-loading="loading" border stripe>
            <el-table-column type="index" label="#" width="60" align="center" />
            <el-table-column prop="keyword" label="匹配关键词" min-width="140" />
            <el-table-column prop="targetName" label="映射标签名" min-width="140" />
            <el-table-column prop="sort" label="排序" width="70" align="center" />
            <el-table-column prop="status" label="状态" width="80" align="center">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
                  {{ row.status === 1 ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="创建时间" width="170" />
            <el-table-column label="操作" width="160" fixed="right">
              <template #default="{ row }">
                <el-button text type="primary" size="small" @click="openEdit(row)">编辑</el-button>
                <el-popconfirm title="确定删除此规则？" @confirm="handleDelete(row.id)">
                  <template #reference>
                    <el-button text type="danger" size="small">删除</el-button>
                  </template>
                </el-popconfirm>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="教材版本" name="VERSION">
          <el-alert
            :title="'标题包含关键词时自动识别教材版本。例如关键词“人教版” → 版本名“人教版”'"
            type="info"
            :closable="false"
            show-icon
            class="tab-alert"
          />
          <el-table :data="versionRules" v-loading="loading" border stripe>
            <el-table-column type="index" label="#" width="60" align="center" />
            <el-table-column prop="keyword" label="匹配关键词" min-width="160">
              <template #default="{ row }">
                <el-tag>{{ row.keyword }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="targetName" label="映射版本名" min-width="140" />
            <el-table-column prop="sort" label="排序" width="70" align="center" />
            <el-table-column prop="status" label="状态" width="80" align="center">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
                  {{ row.status === 1 ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="创建时间" width="170" />
            <el-table-column label="操作" width="160" fixed="right">
              <template #default="{ row }">
                <el-button text type="primary" size="small" @click="openEdit(row)">编辑</el-button>
                <el-popconfirm title="确定删除此规则？" @confirm="handleDelete(row.id)">
                  <template #reference>
                    <el-button text type="danger" size="small">删除</el-button>
                  </template>
                </el-popconfirm>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑规则' : '新增规则'" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="规则类型" prop="type">
          <el-select v-model="form.type" :disabled="isEdit" style="width: 100%">
            <el-option label="标签匹配（TAG）" value="TAG" />
            <el-option label="教材版本（VERSION）" value="VERSION" />
          </el-select>
        </el-form-item>
        <el-form-item label="匹配关键词" prop="keyword">
          <el-input v-model="form.keyword" placeholder="标题中包含此关键词时触发" />
        </el-form-item>
        <el-form-item label="映射目标" prop="targetName">
          <el-input v-model="form.targetName" :placeholder="form.type === 'TAG' ? '映射的标签名称' : '映射的教材版本名'" />
        </el-form-item>
        <el-form-item label="排序号" prop="sort">
          <el-input-number v-model="form.sort" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.statusStr" active-text="启用" inactive-text="禁用" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { getKeywordRuleList, addKeywordRule, updateKeywordRule, deleteKeywordRule } from '@/api/keywordRule'

const loading = ref(false)
const rawData = ref({})  // { TAG: [...], VERSION: [...] }
const activeTab = ref('TAG')
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref(null)

const tagRules = computed(() => rawData.value['TAG'] || [])
const versionRules = computed(() => rawData.value['VERSION'] || [])

const form = reactive({
  id: null,
  keyword: '',
  type: 'TAG',
  targetName: '',
  sort: 0,
  statusStr: 1
})

const rules = {
  keyword: [{ required: true, message: '请输入匹配关键词', trigger: 'blur' }],
  type: [{ required: true, message: '请选择规则类型', trigger: 'change' }],
  targetName: [{ required: true, message: '请输入映射目标', trigger: 'blur' }]
}

onMounted(loadRules)

async function loadRules() {
  loading.value = true
  try {
    const res = await getKeywordRuleList()
    rawData.value = res.data || {}
  } catch {
    // handled
  } finally {
    loading.value = false
  }
}

function onTabChange() {
  // noop
}

function openAdd() {
  isEdit.value = false
  form.id = null
  form.keyword = ''
  form.type = activeTab.value
  form.targetName = ''
  form.sort = 0
  form.statusStr = 1
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  form.id = row.id
  form.keyword = row.keyword
  form.type = row.type
  form.targetName = row.targetName
  form.sort = row.sort
  form.statusStr = row.status
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    const data = {
      keyword: form.keyword,
      type: form.type,
      targetName: form.targetName,
      sort: form.sort,
      status: form.statusStr
    }
    if (isEdit.value) {
      data.id = form.id
      await updateKeywordRule(data)
      ElMessage.success('更新成功')
    } else {
      await addKeywordRule(data)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    await loadRules()
  } catch {
    // handled by interceptor
  } finally {
    submitting.value = false
  }
}

async function handleDelete(id) {
  try {
    await deleteKeywordRule(id)
    ElMessage.success('删除成功')
    await loadRules()
  } catch {
    // handled
  }
}
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}
.page-header h2 {
  font-size: 20px;
  font-weight: 700;
  margin: 0 0 4px;
}
.header-desc {
  font-size: 13px;
  color: var(--text-secondary);
}
.tab-alert {
  margin-bottom: 16px;
}
</style>
