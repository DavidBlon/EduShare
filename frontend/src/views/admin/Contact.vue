<template>
  <div class="contact-page">
    <div class="page-header">
      <div class="header-left">
        <h2>联系方式设置</h2>
        <span class="header-desc">设置平台对外展示的联系方式</span>
      </div>
    </div>

    <el-card shadow="never" style="max-width:600px;">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px" status-icon>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱地址">
            <template #prefix><el-icon><Message /></el-icon></template>
          </el-input>
        </el-form-item>
        <el-form-item label="显示邮箱">
          <el-switch
            v-model="form.showEmail"
            :active-value="1"
            :inactive-value="0"
            active-text="显示"
            inactive-text="隐藏"
          />
        </el-form-item>

        <el-divider />

        <el-form-item label="电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入联系电话">
            <template #prefix><el-icon><Iphone /></el-icon></template>
          </el-input>
        </el-form-item>
        <el-form-item label="显示电话">
          <el-switch
            v-model="form.showPhone"
            :active-value="1"
            :inactive-value="0"
            active-text="显示"
            inactive-text="隐藏"
          />
        </el-form-item>

        <el-divider />

        <el-form-item label="地址" prop="address">
          <el-input v-model="form.address" placeholder="请输入地址">
            <template #prefix><el-icon><Location /></el-icon></template>
          </el-input>
        </el-form-item>
        <el-form-item label="显示地址">
          <el-switch
            v-model="form.showAddress"
            :active-value="1"
            :inactive-value="0"
            active-text="显示"
            inactive-text="隐藏"
          />
        </el-form-item>

        <el-divider />

        <el-form-item>
          <div class="form-actions">
            <el-button type="primary" :loading="submitting" @click="handleSubmit" size="large">
              保存设置
            </el-button>
            <el-button @click="handleReset" size="large">重置</el-button>
          </div>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 预览卡片 -->
    <el-card shadow="never" style="max-width:600px;margin-top:20px;">
      <template #header><span>前台预览</span></template>
      <div class="preview-list">
        <div v-if="form.showEmail === 1 && form.email" class="preview-item">
          <el-icon><Message /></el-icon>
          <span>邮箱：{{ form.email }}</span>
        </div>
        <div v-if="form.showPhone === 1 && form.phone" class="preview-item">
          <el-icon><Iphone /></el-icon>
          <span>电话：{{ form.phone }}</span>
        </div>
        <div v-if="form.showAddress === 1 && form.address" class="preview-item">
          <el-icon><Location /></el-icon>
          <span>地址：{{ form.address }}</span>
        </div>
        <el-empty v-if="!hasVisible" description="所有联系方式均已隐藏" :image-size="60" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getContact, updateContact } from '@/api/admin'

const formRef = ref(null)
const submitting = ref(false)

const form = reactive({
  email: '',
  phone: '',
  address: '',
  showEmail: 1,
  showPhone: 1,
  showAddress: 1
})

const hasVisible = computed(() =>
  (form.showEmail === 1 && form.email) ||
  (form.showPhone === 1 && form.phone) ||
  (form.showAddress === 1 && form.address)
)

const rules = {
  email: [{ type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }]
}

onMounted(async () => {
  try {
    const res = await getContact()
    const d = res.data
    form.email = d.email || ''
    form.phone = d.phone || ''
    form.address = d.address || ''
    form.showEmail = d.showEmail ?? 1
    form.showPhone = d.showPhone ?? 1
    form.showAddress = d.showAddress ?? 1
  } catch {
    // handled
  }
})

function handleReset() {
  ElMessageBox.confirm('重置将丢失所有未保存的更改，确定吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    location.reload()
  }).catch(() => {})
}

async function handleSubmit() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await updateContact({
      email: form.email || null,
      phone: form.phone || null,
      address: form.address || null,
      showEmail: form.showEmail,
      showPhone: form.showPhone,
      showAddress: form.showAddress
    })
    ElMessage.success('联系方式已更新')
  } catch {
    // handled by interceptor
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.page-header {
  margin-bottom: 20px;
}
.page-header h2 {
  font-size: 20px;
  font-weight: 700;
  margin: 0 0 4px;
}
.header-desc { font-size:13px; color:var(--text-secondary); }

.form-actions {
  display: flex;
  gap: 12px;
}

.preview-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.preview-item {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 15px;
  color: var(--text-regular);
  padding: 12px 16px;
  background: var(--bg);
  border-radius: 8px;
}
</style>
