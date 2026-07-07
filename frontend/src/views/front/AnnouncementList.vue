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
        <n-skeleton text :repeat="5" />
      </div>

      <!-- Empty -->
      <div v-else-if="!list.length" class="empty-wrap">
        <div class="empty-inner">
          <span class="empty-icon"><svg viewBox="0 0 24 24" width="48" height="48" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M11 5L6 9H2v6h4l5 4V5z"/><path d="M15.54 8.46a5 5 0 0 1 0 7.07"/><line x1="19" y1="9" x2="23" y2="13"/><line x1="19" y1="13" x2="23" y2="9"/></svg></span>
          <p class="empty-text">暂无公告</p>
        </div>
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
            <n-tag size="small" type="info">公告</n-tag>
          </div>
          <div class="card-body">
            <h3 class="card-title">{{ item.title }}</h3>
            <p class="card-desc">{{ truncateContent(item.content) }}</p>
            <div class="card-footer">
              <span class="footer-author">
                <n-icon><PersonOutline /></n-icon>
                {{ item.adminName || '管理员' }}
              </span>
              <span class="footer-time">
                <n-icon><TimeOutline /></n-icon>
                {{ item.createdAt }}
              </span>
            </div>
          </div>
          <n-icon class="card-arrow"><ChevronForwardOutline /></n-icon>
        </div>
      </div>

      <!-- Pagination -->
      <div v-if="total > pageSize" class="pagination-wrap">
        <n-pagination
          v-model:page="page"
          v-model:page-size="pageSize"
          :page-count="pageCount"
          @update:page="fetchList"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { PersonOutline, TimeOutline, ChevronForwardOutline } from '@vicons/ionicons5'
import { getPublishedAnnouncementPage } from '@/api/announcement'

const list = ref([])
const total = ref(0)
const loading = ref(true)
const page = ref(1)
const pageSize = ref(10)
const pageCount = computed(() => Math.max(1, Math.ceil(total.value / pageSize.value)))

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
  text-align: center;
}
.empty-icon {
  display: block;
  margin-bottom: 8px;
}
.empty-icon svg {
  display: block;
  margin: 0 auto;
}
.empty-text {
  font-size: 14px;
  color: var(--text-secondary);
  margin: 0;
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
