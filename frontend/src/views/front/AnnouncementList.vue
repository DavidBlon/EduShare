<template>
  <div class="announcement-page">
    <div class="page-hero">
      <div class="page-container">
        <h1 class="hero-title">平台公告</h1>
        <p class="hero-desc">了解平台最新动态与通知</p>
      </div>
    </div>

    <div class="page-container">
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
          v-for="(item, idx) in list"
          :key="item.id"
          class="announcement-card"
          :style="{ animationDelay: idx * 0.06 + 's' }"
          @click="$router.push(`/announcement/${item.id}`)"
        >
          <div class="card-badge">
            <el-tag size="small" type="info" effect="dark" class="type-tag">公告</el-tag>
          </div>
          <div class="card-body">
            <h3 class="card-title">{{ item.title }}</h3>
            <p class="card-desc">{{ truncateContent(item.content) }}</p>
            <div class="card-footer">
              <span class="footer-author">
                <el-icon><User /></el-icon>
                {{ item.adminName || '管理员' }}
              </span>
              <span class="footer-time">
                <el-icon><Clock /></el-icon>
                {{ item.createdAt }}
              </span>
            </div>
          </div>
          <el-icon class="card-arrow"><ArrowRight /></el-icon>
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

onMounted(() => fetchList())

async function fetchList() {
  loading.value = true
  try {
    const res = await getPublishedAnnouncementPage({ page: page.value, pageSize: pageSize.value })
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch { /* noop */ }
  finally { loading.value = false }
}

function truncateContent(content) {
  if (!content) return ''
  return content.length > 150 ? content.slice(0, 150) + '…' : content
}
</script>

<style scoped>
.announcement-page {
  min-height: 60vh;
  padding-bottom: 60px;
}

.page-hero {
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  padding: 64px 0 48px;
  text-align: center;
}

.hero-title {
  font-size: 36px;
  font-weight: 800;
  color: white;
  margin: 0 0 8px;
}
.hero-desc {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.6);
  margin: 0;
}

.loading-wrap {
  background: white;
  border-radius: var(--radius-lg);
  padding: 32px;
  box-shadow: var(--shadow);
  margin-top: 32px;
}

.empty-wrap {
  background: white;
  border-radius: var(--radius-lg);
  padding: 60px 20px;
  box-shadow: var(--shadow);
  margin-top: 32px;
}

.announcement-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-top: 32px;
}

.announcement-card {
  position: relative;
  display: flex;
  background: white;
  border-radius: var(--radius-lg);
  padding: 24px;
  box-shadow: var(--shadow);
  cursor: pointer;
  transition: transform 0.25s cubic-bezier(0.34, 1.56, 0.64, 1),
              box-shadow 0.25s ease;
  animation: cardIn 0.4s cubic-bezier(0.4, 0, 0.2, 1) forwards;
  opacity: 0;
  gap: 20px;
  overflow: hidden;
}
.announcement-card:hover {
  box-shadow: var(--shadow-hover);
  transform: translateY(-2px);
}

@keyframes cardIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

.card-badge {
  flex-shrink: 0;
}

.type-tag {
  border-radius: 4px;
}

.card-body {
  flex: 1;
  min-width: 0;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 10px;
  transition: color 0.2s;
}
.announcement-card:hover .card-title {
  color: var(--primary);
}

.card-desc {
  font-size: 14px;
  color: var(--text-regular);
  line-height: 1.6;
  margin: 0 0 14px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-footer {
  display: flex;
  align-items: center;
  gap: 20px;
  font-size: 13px;
  color: var(--text-secondary);
}

.footer-author,
.footer-time {
  display: flex;
  align-items: center;
  gap: 4px;
}

.card-arrow {
  flex-shrink: 0;
  align-self: center;
  color: var(--text-placeholder);
  font-size: 16px;
  transition: var(--transition);
}
.announcement-card:hover .card-arrow {
  color: var(--primary);
  transform: translateX(4px);
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}

@media (max-width: 768px) {
  .hero-title { font-size: 28px; }
  .page-hero { padding: 48px 0 36px; }
  .announcement-list { margin-top: 20px; }
  .announcement-card { padding: 16px; }
  .card-title { font-size: 16px; }
  .card-arrow { display: none; }
}
</style>
