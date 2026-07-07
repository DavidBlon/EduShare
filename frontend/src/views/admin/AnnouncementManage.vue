<template>
  <div class="announcement-page">
    <div class="page-header">
      <div class="header-left">
        <h2>公告管理</h2>
        <span class="header-desc">发布和管理平台公告</span>
      </div>
      <n-button type="primary" @click="openAddDialog">
        <template #icon><n-icon><AddOutline /></n-icon></template>
        发布公告
      </n-button>
    </div>

    <n-card :bordered="false">
      <n-data-table
        :columns="columns"
        :data="list"
        :loading="loading"
        :bordered="true"
        :single-line="false"
        size="small"
      />
      <div class="pagination-wrap">
        <n-pagination
          v-model:page="query.page"
          v-model:page-size="query.pageSize"
          :page-count="pageCount"
          :page-sizes="[10, 20, 50]"
          show-size-picker
          @update:page="fetchList"
          @update:page-size="fetchList"
        />
      </div>
    </n-card>

    <n-modal v-model:show="dialogVisible" :title="isEdit ? '编辑公告' : '发布公告'" preset="card" style="width:700px" :bordered="false">
      <n-form ref="formRef" :model="form" :rules="rules" label-placement="top">
        <n-form-item label="公告标题" path="title">
          <n-input v-model:value="form.title" placeholder="请输入公告标题" :maxlength="100" show-count />
        </n-form-item>
        <n-form-item label="公告内容" path="content">
          <n-input
            v-model:value="form.content"
            type="textarea"
            :rows="12"
            placeholder="请输入公告内容"
            :maxlength="5000"
            show-count
          />
        </n-form-item>
      </n-form>
      <template #footer>
        <div style="display:flex;justify-content:flex-end;gap:8px">
          <n-button @click="dialogVisible = false">取消</n-button>
          <n-button type="primary" :loading="submitting" @click="submitForm">保存</n-button>
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
import {
  getAdminAnnouncementPage,
  addAnnouncement,
  updateAnnouncement,
  togglePublish,
  deleteAnnouncement
} from '@/api/announcement'

const message = useMessage()
const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ page: 1, pageSize: 10 })
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const form = reactive({ id: null, title: '', content: '' })

const pageCount = computed(() => Math.max(1, Math.ceil(total.value / query.pageSize)))

const rules = {
  title: [{ required: true, message: '请输入公告标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入公告内容', trigger: 'blur' }]
}

const columns = [
  { title: '#', key: 'index', width: 60, align: 'center', render: (_, index) => index + 1 },
  { title: '公告标题', key: 'title', minWidth: 200,
    render: (row) => h('span', { class: 'title-text' }, row.title)
  },
  { title: '发布人', key: 'adminName', width: 120 },
  { title: '状态', key: 'isPublished', width: 100,
    render: (row) => h(NTag,
      { type: row.isPublished === 1 ? 'success' : 'info', size: 'small' },
      { default: () => row.isPublished === 1 ? '已发布' : '草稿' }
    )
  },
  { title: '创建时间', key: 'createdAt', width: 180 },
  { title: '操作', key: 'action', width: 260, fixed: 'right',
    render: (row) => [
      h(NButton, {
        text: true, type: 'primary', size: 'small',
        onClick: () => openEditDialog(row)
      }, { default: () => '编辑' }),
      h(NButton, {
        text: true,
        type: row.isPublished === 1 ? 'warning' : 'success',
        size: 'small',
        style: 'margin-left:8px',
        onClick: () => handlePublish(row)
      }, { default: () => row.isPublished === 1 ? '撤回' : '发布' }),
      h(NPopconfirm, {
        onPositiveClick: () => handleDelete(row.id)
      }, {
        trigger: () => h(NButton, {
          text: true, type: 'error', size: 'small',
          style: 'margin-left:8px'
        }, { default: () => '删除' }),
        default: () => '确定删除此公告吗？'
      })
    ]
  }
]

onMounted(fetchList)

async function fetchList() {
  loading.value = true
  try {
    const res = await getAdminAnnouncementPage({ page: query.page, pageSize: query.pageSize })
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

function openAddDialog() {
  isEdit.value = false
  form.id = null
  form.title = ''
  form.content = ''
  dialogVisible.value = true
}

function openEditDialog(row) {
  isEdit.value = true
  form.id = row.id
  form.title = row.title
  form.content = row.content
  dialogVisible.value = true
}

async function submitForm() {
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  submitting.value = true
  try {
    if (isEdit.value) {
      await updateAnnouncement({ id: form.id, title: form.title, content: form.content })
      message.success('更新成功')
    } else {
      await addAnnouncement({ title: form.title, content: form.content })
      message.success('新增成功')
    }
    dialogVisible.value = false
    fetchList()
  } catch {
    // handled by interceptor
  } finally {
    submitting.value = false
  }
}

async function handlePublish(row) {
  try {
    await togglePublish(row.id, row.isPublished === 1 ? 0 : 1)
    message.success(row.isPublished === 1 ? '已撤回' : '已发布')
    fetchList()
  } catch {
    // handled by interceptor
  }
}

async function handleDelete(id) {
  try {
    await deleteAnnouncement(id)
    message.success('删除成功')
    fetchList()
  } catch {
    // handled by interceptor
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
.header-desc { font-size:13px; color:var(--text-secondary); }

.title-text {
  font-weight: 500;
  color: var(--text-primary);
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
}
</style>
