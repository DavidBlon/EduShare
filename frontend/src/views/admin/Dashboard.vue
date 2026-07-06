<template>
  <div class="dashboard-page">
    <div class="page-header">
      <div>
        <h2>控制台</h2>
        <p class="page-desc">欢迎回来，{{ adminInfo?.nickname || adminInfo?.username || '管理员' }}</p>
      </div>
      <el-button type="primary" @click="$router.push('/admin/resource')">
        <el-icon><Plus /></el-icon> 新增资源
      </el-button>
    </div>

    <!-- Stats Cards -->
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="12" :sm="6" v-for="stat in stats" :key="stat.label">
        <div class="stat-card" :style="{ '--card-accent': stat.color }">
          <div class="stat-icon-wrap" :style="{ background: stat.bg }">
            <el-icon :size="24"><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ stat.value }}</span>
            <span class="stat-label">{{ stat.label }}</span>
          </div>
          <div class="stat-accent"></div>
        </div>
      </el-col>
    </el-row>

    <!-- Quick Actions -->
    <el-card shadow="never" class="quick-actions">
      <template #header>
        <span class="card-header-title">
          <el-icon><Lightning /></el-icon> 快捷操作
        </span>
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
        <el-button @click="$router.push('/admin/import')">
          <el-icon><Upload /></el-icon> 批量导入
        </el-button>
        <el-button @click="$router.push('/admin/download-log')">
          <el-icon><List /></el-icon> 查看日志
        </el-button>
      </div>
    </el-card>

    <!-- Info -->
    <el-row :gutter="20">
      <el-col :xs="24" :sm="12">
        <el-card shadow="never" class="info-card">
          <template #header>
            <span class="card-header-title"><el-icon><InfoFilled /></el-icon> 平台信息</span>
          </template>
          <div class="info-list">
            <div class="info-item">
              <span class="info-label">平台名称</span>
              <span class="info-value">小初学习资料圈</span>
            </div>
            <div class="info-item">
              <span class="info-label">当前版本</span>
              <span class="info-value">v1.0.0</span>
            </div>
            <div class="info-item">
              <span class="info-label">前端框架</span>
              <span class="info-value">Vue 3 + Element Plus</span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12">
        <el-card shadow="never" class="info-card">
          <template #header>
            <span class="card-header-title"><el-icon><Link /></el-icon> 快速链接</span>
          </template>
          <div class="link-list">
            <router-link to="/" target="_blank" class="link-item">
              访问前台首页
              <el-icon><ArrowRight /></el-icon>
            </router-link>
            <router-link to="/resources" target="_blank" class="link-item">
              查看资源列表
              <el-icon><ArrowRight /></el-icon>
            </router-link>
            <router-link to="/admin/password" class="link-item">
              修改密码
              <el-icon><ArrowRight /></el-icon>
            </router-link>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const adminInfo = ref({})
try { adminInfo.value = JSON.parse(localStorage.getItem('adminInfo') || '{}') } catch { /* noop */ }

const stats = ref([
  { label: '资源总数', value: '—', icon: 'Document', bg: 'linear-gradient(135deg,#409eff,#337ecc)', color: '#409eff' },
  { label: '分类数量', value: '—', icon: 'Menu', bg: 'linear-gradient(135deg,#67c23a,#529b2e)', color: '#67c23a' },
  { label: '标签数量', value: '—', icon: 'CollectionTag', bg: 'linear-gradient(135deg,#e6a23c,#d48806)', color: '#e6a23c' },
  { label: '下载日志', value: '—', icon: 'List', bg: 'linear-gradient(135deg,#909399,#73767a)', color: '#909399' }
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
  } catch { /* noop */ }
})
</script>

<style scoped>
.page-desc {
  font-size: 14px;
  color: var(--text-secondary);
  margin: 4px 0 0;
}

.stats-row {
  margin-bottom: 24px;
}

.stat-card {
  position: relative;
  background: white;
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 24px;
  box-shadow: var(--shadow);
  transition: transform 0.3s cubic-bezier(0.34, 1.56, 0.64, 1),
              box-shadow 0.3s ease;
  overflow: hidden;
  margin-bottom: 20px;
}
.stat-card:hover {
  transform: translateY(-3px);
  box-shadow: var(--shadow-hover);
}

.stat-accent {
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 100%;
  background: var(--card-accent);
  border-radius: 0 2px 2px 0;
}

.stat-icon-wrap {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  color: white;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 30px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: var(--text-secondary);
  margin-top: 2px;
}

/* Quick actions */
.quick-actions {
  margin-bottom: 24px;
}

.card-header-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  font-size: 15px;
}

.action-buttons {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

/* Info cards */
.info-card {
  margin-bottom: 24px;
}
.info-card .el-card__body { padding: 0; }

.info-list, .link-list {
  padding: 8px 20px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid var(--border-light);
}
.info-item:last-child { border-bottom: none; }

.info-label {
  font-size: 14px;
  color: var(--text-secondary);
}
.info-value {
  font-size: 14px;
  color: var(--text-primary);
  font-weight: 500;
}

.link-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 0;
  border-bottom: 1px solid var(--border-light);
  color: var(--text-regular);
  text-decoration: none;
  font-size: 14px;
  transition: var(--transition);
}
.link-item:last-child { border-bottom: none; }
.link-item:hover {
  color: var(--primary);
  padding-left: 4px;
}

@media (max-width: 768px) {
  .stat-card { padding: 16px; margin-bottom: 12px; }
  .stat-icon-wrap { width: 44px; height: 44px; border-radius: 10px; }
  .stat-value { font-size: 24px; }
  .action-buttons .el-button { width: 100%; }
}
</style>
