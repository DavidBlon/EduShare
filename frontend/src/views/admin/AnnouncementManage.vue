<template>
  <div class="announcement-page">
    <div class="page-header">
      <div class="header-left">
        <h2>公告管理</h2>
        <span class="header-desc">发布和管理平台公告</span>
      </div>
      <el-button type="primary" @click="openAddDialog">
        <el-icon><Plus /></el-icon> 发布公告
      </el-button>
    </div>

    <!-- 公告列表 -->
    <el-card shadow="never">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column type="index" label="#" width="60" />
        <el-table-column prop="title" label="公告标题" min-width="200">
          <template #default="{ row }">
            <span class="title-text">{{ row.title }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="adminName" label="发布人" width="120" />
        <el-table-column prop="isPublished" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.isPublished === 1 ? 'success' : 'info'" size="small">
              {{ row.isPublished === 1 ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openEditDialog(row)">
              编辑
            </el-button>
            <el-button
              link
              :type="row.isPublished === 1 ? 'warning' : 'success'"
              size="small"
              @click="handlePublish(row)"
            >
              {{ row.isPublished === 1 ? '撤回' : '发布' }}
            </el-button>
            <el-popconfirm title="确定删除此公告吗？" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button link type="danger" size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.pageSize"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="fetchList"
        />
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑公告' : '发布公告'"
      width="700px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="公告标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入公告标题" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="公告内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="12"
            placeholder="请输入公告内容"
            maxlength="5000"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getAdminAnnouncementPage,
  addAnnouncement,
  updateAnnouncement,
  togglePublish,
  deleteAnnouncement
} from '@/api/announcement'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ page: 1, pageSize: 10 })

const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const form = reactive({ id: null, title: '', content: '' })

const rules = {
  title: [{ required: true, message: '请输入公告标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入公告内容', trigger: 'blur' }]
}

onMounted(() => {
  fetchList()
})

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
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (isEdit.value) {
      await updateAnnouncement({ id: form.id, title: form.title, content: form.content })
      ElMessage.success('更新成功')
    } else {
      await addAnnouncement({ title: form.title, content: form.content })
      ElMessage.success('新增成功')
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
    ElMessage.success(row.isPublished === 1 ? '已撤回' : '已发布')
    fetchList()
  } catch {
    // handled by interceptor
  }
}

async function handleDelete(id) {
  try {
    await deleteAnnouncement(id)
    ElMessage.success('删除成功')
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
