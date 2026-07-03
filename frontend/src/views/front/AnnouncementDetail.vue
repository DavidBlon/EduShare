<template>
  <div class="detail-page">
    <div class="page-container" style="padding-top: 40px;">
      <!-- Breadcrumb -->
      <div class="breadcrumb">
        <router-link to="/">首页</router-link>
        <el-icon><ArrowRight /></el-icon>
        <router-link to="/announcements">平台公告</router-link>
        <el-icon><ArrowRight /></el-icon>
        <span>{{ detail.title }}</span>
      </div>

      <!-- Loading -->
      <div v-if="loading" class="content-card">
        <el-skeleton :rows="8" animated />
      </div>

      <!-- Detail -->
      <div v-else-if="detail.id" class="content-card">
        <h1 class="detail-title">{{ detail.title }}</h1>
        <div class="detail-meta">
          <span><el-icon><User /></el-icon> {{ detail.adminName }}</span>
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
  } catch {
    detail.value = {}
  } finally {
    loading.value = false
  }
})

function formatContent(content) {
  if (!content) return ''
  // Safe: preserve line breaks as <br> for readability
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
}

.breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 20px;
  flex-wrap: wrap;
}
.breadcrumb a {
  color: var(--text-secondary);
  text-decoration: none;
}
.breadcrumb a:hover {
  color: var(--primary);
}

.content-card {
  background: white;
  border-radius: var(--radius-lg);
  padding: 40px;
  box-shadow: var(--shadow);
  margin-bottom: 40px;
}

.content-card.empty {
  display: flex;
  justify-content: center;
  padding: 60px 20px;
}

.detail-title {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 16px;
  line-height: 1.4;
}

.detail-meta {
  display: flex;
  gap: 24px;
  font-size: 14px;
  color: var(--text-secondary);
  padding-bottom: 20px;
  border-bottom: 1px solid var(--border-light);
  margin-bottom: 24px;
}
.detail-meta span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.detail-content {
  font-size: 16px;
  color: var(--text-regular);
  line-height: 1.8;
}
.detail-content:deep(br) {
  margin-bottom: 8px;
}

@media (max-width: 768px) {
  .content-card {
    padding: 20px;
  }
  .detail-title {
    font-size: 22px;
  }
  .detail-meta {
    flex-direction: column;
    gap: 8px;
  }
}
</style>
