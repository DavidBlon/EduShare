<template>
  <div class="admin-manage">
    <div class="page-header">
      <div class="header-left">
        <h2>管理员管理</h2>
        <span class="header-desc">管理系统管理员和普通管理员</span>
      </div>
      <n-button type="primary" @click="openAdd">
        <template #icon><n-icon><AddOutline /></n-icon></template>
        新增管理员
      </n-button>
    </div>

    <n-card :bordered="false">
      <n-data-table
        :columns="columns"
        :data="admins"
        :loading="loading"
        :bordered="true"
        :single-line="false"
        size="small"
      />
    </n-card>

    <n-modal v-model:show="dialogVisible" :title="isEdit ? '编辑管理员' : '新增管理员'" preset="card" style="width:480px" :bordered="false">
      <n-form ref="formRef" :model="form" :rules="rules" label-width="100px" label-placement="left">
        <n-form-item label="用户名" path="username">
          <n-input v-model:value="form.username" placeholder="请输入用户名" :disabled="isEdit" />
        </n-form-item>
        <n-form-item label="密码" :path="isEdit ? undefined : 'password'">
          <n-input
            v-model:value="form.password"
            type="password"
            show-password-on-click
            :placeholder="isEdit ? '留空则不修改密码' : '请输入密码'"
          />
        </n-form-item>
        <n-form-item label="昵称" path="nickname">
          <n-input v-model:value="form.nickname" placeholder="请输入昵称" />
        </n-form-item>
        <n-form-item label="状态">
          <n-switch v-model:value="form.status" :checked-value="1" :unchecked-value="0">
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
import { getAdminList, addAdmin, updateAdmin, deleteAdmin } from '@/api/admin'

const message = useMessage()
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

const columns = [
  { title: '#', key: 'index', width: 60, align: 'center', render: (_, index) => index + 1 },
  { title: '用户名', key: 'username', minWidth: 120 },
  { title: '昵称', key: 'nickname', minWidth: 120 },
  { title: '角色', key: 'role', width: 120, align: 'center',
    render: (row) => h(NTag,
      { type: row.role === 0 ? 'error' : 'primary', size: 'small' },
      { default: () => row.role === 0 ? '系统管理员' : '普通管理员' }
    )
  },
  { title: '状态', key: 'status', width: 80, align: 'center',
    render: (row) => h(NTag,
      { type: row.status === 1 ? 'success' : 'error', size: 'small' },
      { default: () => row.status === 1 ? '启用' : '禁用' }
    )
  },
  { title: '创建时间', key: 'createdAt', width: 170 },
  { title: '操作', key: 'action', width: 200, fixed: 'right',
    render: (row) => {
      const btns = [
        h(NButton, {
          text: true, type: 'primary', size: 'small',
          onClick: () => openEdit(row)
        }, { default: () => '编辑' })
      ]
      if (row.role !== 0) {
        btns.push(h(NPopconfirm, {
          onPositiveClick: () => handleDelete(row.id)
        }, {
          trigger: () => h(NButton, {
            text: true, type: 'error', size: 'small',
            style: 'margin-left:8px'
          }, { default: () => '删除' }),
          default: () => '确定删除此管理员？'
        }))
      } else {
        btns.push(h(NTag, { size: 'small', type: 'info', style: 'margin-left:8px' }, { default: () => '不可删除' }))
      }
      return btns
    }
  }
]

onMounted(fetchData)

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
  // Remove password requirement in edit mode
  rules.password = []
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
    if (isEdit.value) {
      await updateAdmin({
        id: form.id,
        username: form.username,
        password: form.password || undefined,
        nickname: form.nickname,
        status: form.status
      })
      message.success('更新成功')
    } else {
      await addAdmin({
        username: form.username,
        password: form.password,
        nickname: form.nickname,
        status: form.status
      })
      message.success('新增成功')
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
    message.success('删除成功')
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
