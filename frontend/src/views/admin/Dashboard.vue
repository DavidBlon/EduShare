<template>
  <div class="dashboard-page">
    <h2 class="page-title">控制台</h2>
    <p class="page-desc">欢迎回来，{{ adminInfo?.nickname || adminInfo?.username || '管理员' }}</p>

    <!-- Stats Cards -->
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="12" :sm="6" v-for="stat in stats" :key="stat.label">
        <el-card shadow="never" class="stat-card">
          <div class="stat-icon" :style="{ background: stat.bg }">
            <el-icon :size="24" color="white"><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ stat.value }}</span>
            <span class="stat-label">{{ stat.label }}</span>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Quick Actions -->
    <el-card shadow="never" class="quick-actions">
      <template #header>
        <span><el-icon><Lightning /></el-icon> 快捷操作</span>
      </template>
      <div class="action-buttons">
        <el-button type="primary" @click="$router.push('/admin/resource')">
          <el-icon><Plus /></el-icon> 新增资源
        </el-button>
        <el-button @click="$router.push('/admin/category')">
          <el-icon><Menu /></el-icon> 管理分类
        </el-button>
        <el-button @click="$router.push('/admin/tag')">
          <el-icon><CollectionTag /></el-icon> 管理标签
        </el-button>
        <el-button @click="$router.push('/admin/download-log')">
          <el-icon><List /></el-icon> 查看日志
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

function parseAdminInfo() {
  try {
    return JSON.parse(localStorage.getItem('adminInfo') || '{}')
  } catch {
    return {}
  }
}
const adminInfo = ref(parseAdminInfo())

const stats = ref([
  { label: '资源总数', value: '—', icon: 'Document', bg: 'linear-gradient(135deg,#409eff,#337ecc)' },
  { label: '分类数量', value: '—', icon: 'Menu', bg: 'linear-gradient(135deg,#67c23a,#529b2e)' },
  { label: '标签数量', value: '—', icon: 'CollectionTag', bg: 'linear-gradient(135deg,#e6a23c,#d48806)' },
  { label: '下载日志', value: '—', icon: 'List', bg: 'linear-gradient(135deg,#909399,#73767a)' }
])

onMounted(async () => {
  try {
    const [catRes, tagRes, resourceRes, logRes] = await Promise.all([
      import('@/api/category').then(m => m.getAllCategories()),
      import('@/api/tag').then(m => m.getAdminTagList()),
      import('@/api/resource').then(m => m.getAdminResourcePage({ page: 1, pageSize: 1 })),
      import('@/api/downloadLog').then(m => m.getDownloadLogList({ page: 1, pageSize: 1 }))
    ])
    stats.value[0].value = resourceRes.data?.total || 0
    stats.value[1].value = (catRes.data || []).length
    stats.value[2].value = (tagRes.data || []).length
    stats.value[3].value = logRes.data?.total || 0
  } catch {
    // handled by interceptor
  }
})
</script>

<style scoped>
.page-title {
  font-size: 22px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 4px;
}

.page-desc {
  font-size: 14px;
  color: var(--text-secondary);
  margin: 0 0 24px;
}

.stats-row {
  margin-bottom: 24px;
}

.stat-card {
  margin-bottom: 20px;
}

.stat-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: var(--text-secondary);
  margin-top: 2px;
}

.quick-actions {
  margin-bottom: 24px;
}

.action-buttons {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

@media (max-width: 768px) {
  .stat-card :deep(.el-card__body) {
    padding: 14px !important;
    gap: 12px !important;
  }
  .stat-icon {
    width: 40px;
    height: 40px;
    border-radius: 10px;
  }
  .stat-value {
    font-size: 22px;
  }
  .action-buttons .el-button {
    width: 100%;
  }
}
</style>
