<template>
  <div class="contact-page">
    <div class="page-header">
      <div class="header-left">
        <h2>联系方式设置</h2>
        <span class="header-desc">设置平台对外展示的联系方式</span>
      </div>
    </div>

    <n-card :bordered="false" style="max-width:600px;">
      <n-form ref="formRef" :model="form" :rules="rules" label-width="120px" label-placement="left">
        <n-form-item label="邮箱" path="email">
          <n-input v-model:value="form.email" placeholder="请输入邮箱地址">
            <template #prefix>
              <n-icon><ChatboxEllipsesOutline /></n-icon>
            </template>
          </n-input>
        </n-form-item>
        <n-form-item label="显示邮箱">
          <n-switch v-model:value="form.showEmail" :checked-value="1" :unchecked-value="0">
            <template #checked>显示</template>
            <template #unchecked>隐藏</template>
          </n-switch>
        </n-form-item>

        <n-divider />

        <n-form-item label="电话" path="phone">
          <n-input v-model:value="form.phone" placeholder="请输入联系电话">
            <template #prefix>
              <n-icon><PhonePortraitOutline /></n-icon>
            </template>
          </n-input>
        </n-form-item>
        <n-form-item label="显示电话">
          <n-switch v-model:value="form.showPhone" :checked-value="1" :unchecked-value="0">
            <template #checked>显示</template>
            <template #unchecked>隐藏</template>
          </n-switch>
        </n-form-item>

        <n-divider />

        <n-form-item label="地址" path="address">
          <n-input v-model:value="form.address" placeholder="请输入地址">
            <template #prefix>
              <n-icon><LocationOutline /></n-icon>
            </template>
          </n-input>
        </n-form-item>
        <n-form-item label="显示地址">
          <n-switch v-model:value="form.showAddress" :checked-value="1" :unchecked-value="0">
            <template #checked>显示</template>
            <template #unchecked>隐藏</template>
          </n-switch>
        </n-form-item>

        <n-divider />

        <n-form-item>
          <div class="form-actions">
            <n-button type="primary" :loading="submitting" @click="handleSubmit" size="large">
              保存设置
            </n-button>
            <n-button @click="handleReset" size="large">重置</n-button>
          </div>
        </n-form-item>
      </n-form>
    </n-card>

    <n-card :bordered="false" style="max-width:600px;margin-top:20px;">
      <template #header>
        <div class="card-header-title"><n-icon><EyeOutline /></n-icon> 前台预览</div>
      </template>
      <div class="preview-list">
        <div v-if="form.showEmail === 1 && form.email" class="preview-item">
          <div class="preview-icon" style="background:rgba(99,102,241,0.1);color:#6366f1;">
            <n-icon :size="18"><ChatboxEllipsesOutline /></n-icon>
          </div>
          <div class="preview-body">
            <span class="preview-label">邮箱</span>
            <span class="preview-value">{{ form.email }}</span>
          </div>
        </div>
        <div v-if="form.showPhone === 1 && form.phone" class="preview-item">
          <div class="preview-icon" style="background:rgba(16,185,129,0.1);color:#10b981;">
            <n-icon :size="18"><PhonePortraitOutline /></n-icon>
          </div>
          <div class="preview-body">
            <span class="preview-label">电话</span>
            <span class="preview-value">{{ form.phone }}</span>
          </div>
        </div>
        <div v-if="form.showAddress === 1 && form.address" class="preview-item">
          <div class="preview-icon" style="background:rgba(245,158,11,0.1);color:#f59e0b;">
            <n-icon :size="18"><LocationOutline /></n-icon>
          </div>
          <div class="preview-body">
            <span class="preview-label">地址</span>
            <span class="preview-value">{{ form.address }}</span>
          </div>
        </div>
        <n-empty v-if="!hasVisible" description="所有联系方式均已隐藏" />
      </div>
    </n-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useMessage, useDialog } from 'naive-ui'
import { ChatboxEllipsesOutline, PhonePortraitOutline, LocationOutline, EyeOutline } from '@vicons/ionicons5'
import { getContact, updateContact } from '@/api/admin'

const message = useMessage()
const dialog = useDialog()
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
  dialog.warning({
    title: '提示',
    content: '重置将丢失所有未保存的更改，确定吗？',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: () => {
      location.reload()
    }
  })
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
    await updateContact({
      email: form.email || null,
      phone: form.phone || null,
      address: form.address || null,
      showEmail: form.showEmail,
      showPhone: form.showPhone,
      showAddress: form.showAddress
    })
    message.success('联系方式已更新')
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

.card-header-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 600;
  font-size: 15px;
}

.preview-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.preview-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px;
  background: var(--bg);
  border-radius: 12px;
  transition: var(--transition);
}
.preview-item:hover {
  transform: translateX(4px);
}

.preview-icon {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.preview-body {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.preview-label {
  font-size: 12px;
  color: var(--text-secondary);
  font-weight: 500;
}

.preview-value {
  font-size: 15px;
  color: var(--text-primary);
  font-weight: 500;
}
</style>
