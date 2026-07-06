<template>
  <div class="detail-page">
    <div class="page-hero page-hero-sm">
      <div class="page-container">
        <div class="breadcrumb">
          <router-link to="/">首页</router-link>
          <el-icon><ArrowRight /></el-icon>
          <router-link to="/announcements">平台公告</router-link>
          <el-icon><ArrowRight /></el-icon>
          <span v-if="detail.title">{{ detail.title }}</span>
        </div>
      </div>
    </div>

    <div class="page-container">
      <!-- Loading -->
      <div v-if="loading" class="content-card">
        <el-skeleton :rows="10" animated />
      </div>

      <!-- Detail -->
      <div v-else-if="detail.id" class="content-card">
        <h1 class="detail-title">{{ detail.title }}</h1>
        <div class="detail-meta">
          <span><el-icon><User /></el-icon> {{ detail.adminName || '管理员' }}</span>
          <span><el-icon><Clock /></el-icon> {{ detail.createdAt }}</span>
        </div>
        <div class="detail-content" v-html="formatContent(detail.content)"></div>
      </div>

      <!-- Not found -->
      <div v-else class="content-card empty">
        <el-empty description="公告不存在" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getPublishedAnnouncementDetail } from '@/api/announcement'

const route = useRoute()
const detail = ref({})
const loading = ref(true)

onMounted(async () => {
  try {
    const res = await getPublishedAnnouncementDetail(route.params.id)
    detail.value = res.data || {}
  } catch { detail.value = {} }
  finally { loading.value = false }
})

function formatContent(content) {
  if (!content) return ''
  return content
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/\n/g, '<br>')
}
</script>

<style scoped>
.detail-page {
  min-height: 60vh;
  padding-bottom: 60px;
}

.page-hero-sm {
  background: linear-gradient(135deg, #1a1a2e, #16213e);
  padding: 32px 0 24px;
}

.breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.6);
  flex-wrap: wrap;
}
.breadcrumb a {
  color: rgba(255, 255, 255, 0.6);
  text-decoration: none;
  transition: color 0.2s;
}
.breadcrumb a:hover {
  color: white;
}
.breadcrumb span {
  color: rgba(255, 255, 255, 0.85);
}

.content-card {
  background: white;
  border-radius: var(--radius-xl);
  padding: 48px;
  box-shadow: var(--shadow);
  margin-top: 32px;
}

.content-card.empty {
  display: flex;
  justify-content: center;
  padding: 60px 20px;
}

.detail-title {
  font-size: 30px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 18px;
  line-height: 1.35;
}

.detail-meta {
  display: flex;
  gap: 28px;
  font-size: 14px;
  color: var(--text-secondary);
  padding-bottom: 24px;
  border-bottom: 1px solid var(--border-light);
  margin-bottom: 28px;
}
.detail-meta span {
  display: flex;
  align-items: center;
  gap: 6px;
}

.detail-content {
  font-size: 16px;
  color: var(--text-regular);
  line-height: 1.9;
}
.detail-content:deep(br) {
  margin-bottom: 10px;
}

@media (max-width: 768px) {
  .content-card {
    padding: 24px;
    margin-top: 20px;
  }
  .detail-title {
    font-size: 22px;
  }
  .detail-meta {
    flex-direction: column;
    gap: 8px;
    padding-bottom: 16px;
    margin-bottom: 20px;
  }
  .page-hero-sm {
    padding: 24px 0 16px;
  }
}
</style>
