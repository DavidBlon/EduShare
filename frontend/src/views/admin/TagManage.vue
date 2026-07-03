<template>
  <div class="tag-manage">
    <div class="page-header">
      <div class="header-left">
        <h2>标签管理</h2>
        <span class="header-desc">管理资源标签</span>
      </div>
      <el-button type="primary" @click="openAdd">
        <el-icon><Plus /></el-icon> 新增标签
      </el-button>
    </div>

    <el-card shadow="never">
      <el-table :data="tags" v-loading="loading" border stripe>
        <el-table-column type="index" label="#" width="60" align="center" />
        <el-table-column prop="name" label="标签名称" min-width="160" />
        <el-table-column prop="sort" label="排序" width="80" align="center" />
        <el-table-column prop="resourceCount" label="资源数量" width="100" align="center">
          <template #default="{ row }">
            <el-tag round>{{ row.resourceCount || 0 }}</el-tag>
          </template>
        </el-table-column>
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
            <el-popconfirm title="确定删除此标签？" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button text type="danger" size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑标签' : '新增标签'" width="450px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="标签名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入标签名称" />
        </el-form-item>
        <el-form-item label="排序号">
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAdminTagList, addTag, updateTag, deleteTag } from '@/api/tag'

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
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    const data = { name: form.name, sort: form.sort, status: form.statusStr }
    if (isEdit.value) {
      data.id = form.id
      await updateTag(data)
      ElMessage.success('更新成功')
    } else {
      await addTag(data)
      ElMessage.success('新增成功')
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
    ElMessage.success('删除成功')
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
