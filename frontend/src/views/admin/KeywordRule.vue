<template>
  <div class="keyword-rule">
    <div class="page-header">
      <div class="header-left">
        <h2>关键词规则</h2>
        <span class="header-desc">管理批量导入时的自动解析规则，新增规则后解析器会自动生效</span>
      </div>
      <n-button type="primary" @click="openAdd">
        <template #icon><n-icon><AddOutline /></n-icon></template>
        新增规则
      </n-button>
    </div>

    <n-card :bordered="false">
      <n-tabs v-model:value="activeTab" @update:value="onTabChange">
        <n-tab-pane name="TAG" tab="标签匹配规则">
          <n-alert title="标题包含关键词时自动添加对应标签。例如关键词「期中」→ 标签「期中考试」" type="info" :bordered="false" class="tab-alert" />
          <n-data-table
            :columns="ruleColumns"
            :data="tagRules"
            :loading="loading"
            :bordered="true"
            :single-line="false"
            size="small"
          />
        </n-tab-pane>

        <n-tab-pane name="VERSION" tab="教材版本">
          <n-alert title="标题包含关键词时自动识别教材版本。例如关键词「人教版」→ 版本名「人教版」" type="info" :bordered="false" class="tab-alert" />
          <n-data-table
            :columns="ruleColumns"
            :data="versionRules"
            :loading="loading"
            :bordered="true"
            :single-line="false"
            size="small"
          />
        </n-tab-pane>
      </n-tabs>
    </n-card>

    <n-modal v-model:show="dialogVisible" :title="isEdit ? '编辑规则' : '新增规则'" preset="card" style="width:500px" :bordered="false">
      <n-form ref="formRef" :model="form" :rules="rules" label-width="110px" label-placement="left">
        <n-form-item label="规则类型" path="type">
          <n-select v-model:value="form.type" :disabled="isEdit"
            :options="[
              { label: '标签匹配（TAG）', value: 'TAG' },
              { label: '教材版本（VERSION）', value: 'VERSION' }
            ]"
          />
        </n-form-item>
        <n-form-item label="匹配关键词" path="keyword">
          <n-input v-model:value="form.keyword" placeholder="标题中包含此关键词时触发" />
        </n-form-item>
        <n-form-item label="映射目标" path="targetName">
          <n-input v-model:value="form.targetName" :placeholder="form.type === 'TAG' ? '映射的标签名称' : '映射的教材版本名'" />
        </n-form-item>
        <n-form-item label="排序号">
          <n-input-number v-model:value="form.sort" :min="0" :max="999" />
        </n-form-item>
        <n-form-item label="状态">
          <n-switch v-model:value="form.statusStr" :checked-value="1" :unchecked-value="0">
            <template #checked>启用</template>
            <template #unchecked>禁用</template>
          </n-switch>
        </n-form-item>
      </n-form>
      <template #footer>
        <div style="display:flex;justify-content:flex-end;gap:8px">
          <n-button @click="dialogVisible = false">取消</n-button>
          <n-button type="primary" :loading="submitting" @click="handleSubmit">确定</n-button>
        </div>
      </template>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, reactive, h, computed, onMounted } from 'vue'
import { useMessage } from 'naive-ui'
import { NTag, NButton, NPopconfirm } from 'naive-ui'
import { AddOutline } from '@vicons/ionicons5'
import { getKeywordRuleList, addKeywordRule, updateKeywordRule, deleteKeywordRule } from '@/api/keywordRule'

const message = useMessage()
const loading = ref(false)
const rawData = ref({})
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

const ruleColumns = [
  { title: '#', key: 'index', width: 60, align: 'center', render: (_, index) => index + 1 },
  { title: '匹配关键词', key: 'keyword', minWidth: 140,
    render: (row) => h(NTag, {}, { default: () => row.keyword })
  },
  { title: '映射目标', key: 'targetName', minWidth: 140 },
  { title: '排序', key: 'sort', width: 70, align: 'center' },
  { title: '状态', key: 'status', width: 80, align: 'center',
    render: (row) => h(NTag,
      { type: row.status === 1 ? 'success' : 'error', size: 'small' },
      { default: () => row.status === 1 ? '启用' : '禁用' }
    )
  },
  { title: '创建时间', key: 'createdAt', width: 170 },
  { title: '操作', key: 'action', width: 160, fixed: 'right',
    render: (row) => [
      h(NButton, {
        text: true, type: 'primary', size: 'small',
        onClick: () => openEdit(row)
      }, { default: () => '编辑' }),
      h(NPopconfirm, {
        onPositiveClick: () => handleDelete(row.id)
      }, {
        trigger: () => h(NButton, {
          text: true, type: 'error', size: 'small',
          style: 'margin-left:8px'
        }, { default: () => '删除' }),
        default: () => '确定删除此规则？'
      })
    ]
  }
]

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

function onTabChange() { /* noop */ }

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
  try {
    await formRef.value.validate()
  } catch {
    return
  }
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
      message.success('更新成功')
    } else {
      await addKeywordRule(data)
      message.success('新增成功')
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
    message.success('删除成功')
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
