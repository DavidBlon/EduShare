<template>
  <div class="password-page">
    <div class="page-header">
      <div class="header-left">
        <h2>修改密码</h2>
        <span class="header-desc">定期修改密码可以提高账户安全性</span>
      </div>
    </div>

    <n-card :bordered="false" style="max-width:500px;">
      <n-form ref="formRef" :model="form" :rules="rules" label-width="100px" label-placement="left">
        <n-form-item label="原密码" path="oldPassword">
          <n-input v-model:value="form.oldPassword" type="password" show-password-on-click placeholder="请输入原密码" />
        </n-form-item>
        <n-form-item label="新密码" path="newPassword">
          <n-input v-model:value="form.newPassword" type="password" show-password-on-click placeholder="请输入新密码" />
        </n-form-item>
        <n-form-item label="确认密码" path="confirmPassword">
          <n-input v-model:value="form.confirmPassword" type="password" show-password-on-click placeholder="请再次输入新密码" />
        </n-form-item>
        <n-form-item>
          <n-button type="primary" :loading="submitting" @click="handleSubmit">确认修改</n-button>
        </n-form-item>
      </n-form>
    </n-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useMessage } from 'naive-ui'
import { updatePassword } from '@/api/admin'

const message = useMessage()
const formRef = ref(null)
const submitting = ref(false)

const form = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirm = (rule, value) => {
  if (value !== form.newPassword) {
    return new Error('两次输入的密码不一致')
  }
  return true
}

const rules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ]
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
    await updatePassword({
      oldPassword: form.oldPassword,
      newPassword: form.newPassword,
      confirmPassword: form.confirmPassword
    })
    message.success('密码修改成功')
    form.oldPassword = ''
    form.newPassword = ''
    form.confirmPassword = ''
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
</style>
