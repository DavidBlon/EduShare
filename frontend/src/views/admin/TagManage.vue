<template>
  <div class="tag-manage">
    <div class="page-header">
      <div class="header-left">
        <h2>标签管理</h2>
        <span class="header-desc">管理资源标签</span>
      </div>
      <n-button type="primary" @click="openAdd">
        <template #icon><n-icon><AddOutline /></n-icon></template>
        新增标签
      </n-button>
    </div>

    <n-card :bordered="false">
      <n-data-table
        :columns="columns"
        :data="tags"
        :loading="loading"
        :bordered="true"
        :single-line="false"
        size="small"
      />
    </n-card>

    <n-modal v-model:show="dialogVisible" :title="isEdit ? '编辑标签' : '新增标签'" preset="card" style="width:450px" :bordered="false">
      <n-form ref="formRef" :model="form" :rules="rules" label-width="100px" label-placement="left">
        <n-form-item label="标签名称" path="name">
          <n-input v-model:value="form.name" placeholder="请输入标签名称" />
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
import { ref, reactive, h, onMounted } from 'vue'
import { useMessage } from 'naive-ui'
import { NTag, NButton, NPopconfirm } from 'naive-ui'
import { AddOutline } from '@vicons/ionicons5'
import { getAdminTagList, addTag, updateTag, deleteTag } from '@/api/tag'

const message = useMessage()
const loading = ref(false)
const tags = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref(null)

const form = reactive({
  id: null,
  name: '',
  sort: 0,
  statusStr: 1
})

const rules = {
  name: [{ required: true, message: '请输入标签名称', trigger: 'blur' }]
}

const columns = [
  { title: '#', key: 'index', width: 60, align: 'center',
    render: (_, index) => index + 1
  },
  { title: '标签名称', key: 'name', minWidth: 160 },
  { title: '排序', key: 'sort', width: 80, align: 'center' },
  { title: '资源数量', key: 'resourceCount', width: 100, align: 'center',
    render: (row) => h(NTag, { round: true }, { default: () => row.resourceCount || 0 })
  },
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
      ' ',
      h(NPopconfirm, {
        onPositiveClick: () => handleDelete(row.id)
      }, {
        trigger: () => h(NButton, {
          text: true, type: 'error', size: 'small'
        }, { default: () => '删除' }),
        default: () => '确定删除此标签？'
      })
    ]
  }
]

onMounted(loadTags)

async function loadTags() {
  loading.value = true
  try {
    const res = await getAdminTagList()
    tags.value = res.data || []
  } catch {
    // handled
  } finally {
    loading.value = false
  }
}

function openAdd() {
  isEdit.value = false
  form.id = null
  form.name = ''
  form.sort = 0
  form.statusStr = 1
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  form.id = row.id
  form.name = row.name
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
    const data = { name: form.name, sort: form.sort, status: form.statusStr }
    if (isEdit.value) {
      data.id = form.id
      await updateTag(data)
      message.success('更新成功')
    } else {
      await addTag(data)
      message.success('新增成功')
    }
    dialogVisible.value = false
    await loadTags()
  } catch {
    // handled by interceptor
  } finally {
    submitting.value = false
  }
}

async function handleDelete(id) {
  try {
    await deleteTag(id)
    message.success('删除成功')
    await loadTags()
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
</style>
