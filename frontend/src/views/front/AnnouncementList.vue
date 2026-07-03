<template>
  <div class="announcement-page">
    <div class="page-container" style="padding-top: 40px;">
      <!-- Breadcrumb -->
      <div class="breadcrumb">
        <router-link to="/">首页</router-link>
        <el-icon><ArrowRight /></el-icon>
        <span>平台公告</span>
      </div>

      <h1 class="page-title">平台公告</h1>

      <!-- Loading -->
      <div v-if="loading" class="loading-wrap">
        <el-skeleton :rows="5" animated />
      </div>

      <!-- Empty -->
      <div v-else-if="!list.length" class="empty-wrap">
        <el-empty description="暂无公告" />
      </div>

      <!-- Announcement List -->
      <div v-else class="announcement-list">
        <div
          v-for="item in list"
          :key="item.id"
          class="announcement-card"
          @click="$router.push(`/announcement/${item.id}`)"
        >
          <div class="card-header">
            <h3 class="card-title">{{ item.title }}</h3>
            <el-tag size="small" type="info" effect="plain">{{ item.adminName }}</el-tag>
          </div>
          <p class="card-desc">{{ truncateContent(item.content) }}</p>
          <div class="card-footer">
            <el-icon><Clock /></el-icon>
            <span>{{ item.createdAt }}</span>
          </div>
        </div>
      </div>

      <!-- Pagination -->
      <div v-if="total > pageSize" class="pagination-wrap">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          :total="total"
          layout="prev, pager, next"
          @current-change="fetchList"
          background
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getPublishedAnnouncementPage } from '@/api/announcement'

const list = ref([])
const total = ref(0)
const loading = ref(true)
const page = ref(1)
const pageSize = ref(10)

onMounted(() => {
  fetchList()
})

async function fetchList() {
  loading.value = true
  try {
    const res = await getPublishedAnnouncementPage({ page: page.value, pageSize: pageSize.value })
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

function truncateContent(content) {
  if (!content) return ''
  return content.length > 150 ? content.slice(0, 150) + '...' : content
}
</script>

<style scoped>
.announcement-page {
  min-height: 60vh;
}

.breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 20px;
}
.breadcrumb a {
  color: var(--text-secondary);
  text-decoration: none;
}
.breadcrumb a:hover {
  color: var(--primary);
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 32px;
}

.loading-wrap {
  background: white;
  border-radius: var(--radius-lg);
  padding: 32px;
  box-shadow: var(--shadow);
}

.empty-wrap {
  background: white;
  border-radius: var(--radius-lg);
  padding: 60px 20px;
  box-shadow: var(--shadow);
}

.announcement-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.announcement-card {
  background: white;
  border-radius: var(--radius-lg);
  padding: 24px;
  box-shadow: var(--shadow);
  cursor: pointer;
  transition: var(--transition);
}
.announcement-card:hover {
  box-shadow: var(--shadow-hover);
  transform: translateY(-2px);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
  flex: 1;
}

.card-desc {
  font-size: 14px;
  color: var(--text-regular);
  line-height: 1.6;
  margin: 0 0 12px;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-footer {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--text-secondary);
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 32px;
  padding-bottom: 40px;
}

@media (max-width: 768px) {
  .page-title {
    font-size: 22px;
    margin-bottom: 20px;
  }
  .announcement-card {
    padding: 16px;
  }
  .card-title {
    font-size: 16px;
  }
}
</style>
