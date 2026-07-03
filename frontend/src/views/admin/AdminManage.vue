<template>
  <div class="admin-manage">
    <div class="page-header">
      <div class="header-left">
        <h2>管理员管理</h2>
        <span class="header-desc">管理系统管理员和普通管理员</span>
      </div>
      <el-button type="primary" @click="openAdd">
        <el-icon><Plus /></el-icon> 新增管理员
      </el-button>
    </div>

    <el-card shadow="never">
      <el-table :data="admins" v-loading="loading" border stripe>
        <el-table-column type="index" label="#" width="60" align="center" />
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="nickname" label="昵称" min-width="120" />
        <el-table-column prop="role" label="角色" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="row.role === 0 ? 'danger' : 'primary'" effect="dark" size="small">
              {{ row.role === 0 ? '系统管理员' : '普通管理员' }}
            </el-tag>
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
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="openEdit(row)">编辑</el-button>
            <el-popconfirm
              v-if="row.role !== 0"
              title="确定删除此管理员？"
              @confirm="handleDelete(row.id)"
            >
              <template #reference>
                <el-button text type="danger" size="small">删除</el-button>
              </template>
            </el-popconfirm>
            <el-tag v-else size="small" type="info" effect="plain">不可删除</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑管理员' : '新增管理员'" width="480px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="密码" :prop="isEdit ? '' : 'password'">
          <el-input
            v-model="form.password"
            type="password"
            show-password
            :placeholder="isEdit ? '留空则不修改密码' : '请输入密码'"
          />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch
            v-model="form.status"
            active-text="启用"
            inactive-text="禁用"
            :active-value="1"
            :inactive-value="0"
          />
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
import { getAdminList, addAdmin, updateAdmin, deleteAdmin } from '@/api/admin'

const loading = ref(false)
const admins = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref(null)

const form = reactive({
  id: null,
  username: '',
  password: '',
  nickname: '',
  status: 1
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

onMounted(() => {
  fetchData()
})

async function fetchData() {
  loading.value = true
  try {
    const res = await getAdminList()
    admins.value = res.data || []
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

function openAdd() {
  isEdit.value = false
  form.id = null
  form.username = ''
  form.password = ''
  form.nickname = ''
  form.status = 1
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  form.id = row.id
  form.username = row.username
  form.password = ''
  form.nickname = row.nickname
  form.status = row.status
  // 编辑时密码非必填
  rules.password = []
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (isEdit.value) {
      await updateAdmin({
        id: form.id,
        username: form.username,
        password: form.password || undefined,
        nickname: form.nickname,
        status: form.status
      })
      ElMessage.success('更新成功')
    } else {
      await addAdmin({
        username: form.username,
        password: form.password,
        nickname: form.nickname,
        status: form.status
      })
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    await fetchData()
  } catch {
    // handled by interceptor
  } finally {
    submitting.value = false
    rules.password = [{ required: true, message: '请输入密码', trigger: 'blur' }]
  }
}

async function handleDelete(id) {
  try {
    await deleteAdmin(id)
    ElMessage.success('删除成功')
    await fetchData()
  } catch {
    // handled by interceptor
  }
}
</script>

<style scoped>
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}
.header-left h2 {
  margin: 0 0 4px;
  font-size: 20px;
}
.header-desc {
  font-size: 13px;
  color: var(--text-secondary);
}
</style>
