<template>
  <div class="login-page">
    <!-- Animated background orbs -->
    <div class="bg-orbs">
      <div class="orb orb-1"></div>
      <div class="orb orb-2"></div>
      <div class="orb orb-3"></div>
    </div>

    <!-- Floating particles -->
    <div class="bg-particles">
      <div
        v-for="i in 6"
        :key="i"
        class="particle"
        :style="{
          width: (20 + i * 10) + 'px',
          height: (20 + i * 10) + 'px',
          left: (5 + i * 16) + '%',
          animationDelay: (i * 0.7) + 's',
          animationDuration: (6 + i) + 's',
        }"
      ></div>
    </div>

    <div class="login-card">
      <div class="login-header">
        <div class="login-logo">
          <div class="login-logo-circle">
            <img src="/logo.jpg" alt="小初学习资料圈" />
          </div>
        </div>
        <h2 class="login-title">小初学习资料圈</h2>
        <p class="login-subtitle">管理后台 · 教育资源共享平台</p>
      </div>

      <n-form
        ref="formRef"
        :model="form"
        :rules="rules"
        :show-label="false"
        class="login-form"
        @keyup.enter="handleLogin"
      >
        <n-form-item path="username">
          <n-input
            v-model:value="form.username"
            placeholder="用户名"
          >
            <template #prefix>
              <n-icon><PersonOutline /></n-icon>
            </template>
          </n-input>
        </n-form-item>

        <n-form-item path="password">
          <n-input
            v-model:value="form.password"
            type="password"
            placeholder="密码"
            show-password-on-click
          >
            <template #prefix>
              <n-icon><LockClosedOutline /></n-icon>
            </template>
          </n-input>
        </n-form-item>

        <n-form-item>
          <n-button
            type="primary"
            :loading="loading"
            class="login-btn"
            @click="handleLogin"
          >
            {{ loading ? '登录中...' : '登 录' }}
          </n-button>
        </n-form-item>
      </n-form>

      <div class="login-footer">
        <router-link to="/" class="back-link">
          <n-icon><ArrowBackOutline /></n-icon> 返回前台
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { PersonOutline, LockClosedOutline, ArrowBackOutline } from '@vicons/ionicons5'
import { login } from '@/api/admin'

const router = useRouter()
const message = useMessage()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  loading.value = true
  try {
    const res = await login({ username: form.username, password: form.password })
    const data = res.data
    localStorage.setItem('adminToken', data.token)
    localStorage.setItem('adminInfo', JSON.stringify({
      id: data.adminId,
      username: data.username,
      nickname: data.nickname,
      avatar: data.avatar,
      role: data.role
    }))
    message.success('登录成功')
    router.push('/admin/dashboard')
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #0f0c29 0%, #1a1a2e 40%, #16213e 70%, #0f3460 100%);
  position: relative;
  overflow: hidden;
}

/* ====== Animated Orbs ====== */
.bg-orbs {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(100px);
  opacity: 0.12;
  animation: orbFloat 15s ease-in-out infinite alternate;
}

.orb-1 {
  width: 700px;
  height: 700px;
  background: radial-gradient(circle, #6366f1, transparent);
  top: -200px;
  right: -200px;
  animation-delay: 0s;
}

.orb-2 {
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, #8b5cf6, transparent);
  bottom: -150px;
  left: -150px;
  animation-delay: -5s;
}

.orb-3 {
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, #a78bfa, transparent);
  top: 50%;
  left: 60%;
  transform: translate(-50%, -50%);
  animation-delay: -10s;
}

@keyframes orbFloat {
  0% { transform: translate(0, 0) scale(1); }
  33% { transform: translate(30px, -20px) scale(1.1); }
  66% { transform: translate(-20px, 30px) scale(0.9); }
  100% { transform: translate(20px, -10px) scale(1.05); }
}

/* ====== Particles ====== */
.bg-particles {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.particle {
  position: absolute;
  bottom: 10%;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.06);
  animation: particleRise 8s ease-in-out infinite alternate;
}

@keyframes particleRise {
  0% { transform: translateY(0) scale(1); opacity: 0.2; }
  100% { transform: translateY(-40px) scale(1.3); opacity: 0.5; }
}

/* ====== Login Card ====== */
.login-card {
  position: relative;
  z-index: 1;
  width: 420px;
  padding: 44px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  border-radius: 20px;
  box-shadow: 0 24px 80px rgba(0, 0, 0, 0.35);
  animation: cardEnter 0.6s cubic-bezier(0.34, 1.56, 0.64, 1);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

@keyframes cardEnter {
  from {
    opacity: 0;
    transform: scale(0.92) translateY(20px);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

.login-header {
  text-align: center;
  margin-bottom: 36px;
}

.login-logo {
  margin-bottom: 20px;
}

.login-logo-circle {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  overflow: hidden;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 24px rgba(99, 102, 241, 0.2);
}
.login-logo-circle img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.login-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 6px;
  letter-spacing: 0.5px;
}

.login-subtitle {
  font-size: 14px;
  color: var(--text-secondary);
  margin: 0;
}

/* ====== Form ====== */
.login-form {
  margin-bottom: 8px;
}

.login-form :deep(.n-form-item) {
  margin-bottom: 22px;
}

.login-btn {
  width: 100%;
  height: 46px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 3px;
  border-radius: 10px;
  margin-top: 4px;
}
.login-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 16px rgba(99, 102, 241, 0.3);
}

/* ====== Footer ====== */
.login-footer {
  text-align: center;
}

.back-link {
  color: var(--text-secondary);
  font-size: 13px;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  transition: var(--transition);
}
.back-link:hover {
  color: var(--primary);
}

@media (max-width: 768px) {
  .login-card {
    width: 92%;
    padding: 32px 24px;
  }
  .login-title {
    font-size: 20px;
  }
  .login-logo-circle {
    width: 60px;
    height: 60px;
  }
}
</style>
