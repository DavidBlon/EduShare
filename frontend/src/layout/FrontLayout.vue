<template>
  <div class="front-layout">
    <!-- Top Header -->
    <header class="site-header">
      <div class="header-inner">
        <router-link to="/" class="logo">
          <img src="/logo.jpg" alt="小初学习资料圈" class="logo-img" />
        </router-link>

        <nav class="main-nav">
          <router-link to="/" class="nav-item" :class="{ active: $route.path === '/' }">
            首页
          </router-link>
          <router-link to="/resources" class="nav-item" :class="{ active: $route.path === '/resources' }">
            资源库
          </router-link>
          <router-link to="/qq-group" class="nav-item" :class="{ active: $route.path === '/qq-group' }">
            资料群
          </router-link>
          <router-link to="/about" class="nav-item" :class="{ active: $route.path === '/about' }">
            关于我们
          </router-link>
        </nav>

        <div class="header-right">
          <!-- Bell notification -->
          <el-popover
            placement="bottom-end"
            :width="320"
            trigger="click"
            v-model:visible="bellVisible"
            @show="onBellShow"
          >
            <template #reference>
              <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99" class="bell-badge">
                <el-button text class="bell-btn">
                  <el-icon :size="20"><Bell /></el-icon>
                </el-button>
              </el-badge>
            </template>

            <div class="popover-content">
              <div class="popover-header">
                <span class="popover-title">公告通知</span>
                <el-button text size="small" @click="markAllRead">标记已读</el-button>
              </div>

              <div v-if="recentList.length === 0" class="popover-empty">暂无公告</div>

              <div v-else class="popover-list">
                <div
                  v-for="item in recentList"
                  :key="item.id"
                  class="popover-item"
                  :class="{ unread: item.id > lastReadId }"
                  @click="goAnnouncement(item.id)"
                >
                  <div class="item-title">
                    <span v-if="item.id > lastReadId" class="unread-dot"></span>
                    <span class="item-text">{{ item.title }}</span>
                  </div>
                  <div class="item-time">{{ item.createdAt }}</div>
                </div>
              </div>

              <div class="popover-footer">
                <router-link to="/announcements" class="view-all" @click="bellVisible = false">
                  查看全部公告 <el-icon><ArrowRight /></el-icon>
                </router-link>
              </div>
            </div>
          </el-popover>

          <el-button text @click="goAdmin">
            <el-icon><Setting /></el-icon>
            <span class="admin-text">管理后台</span>
          </el-button>
        </div>
      </div>
    </header>

    <!-- Main Content -->
    <main class="site-main">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <keep-alive :include="['ResourceList']">
            <component :is="Component" />
          </keep-alive>
        </transition>
      </router-view>
    </main>

    <!-- Footer -->
    <footer class="site-footer">
      <div class="footer-inner">
        <div class="footer-grid">
          <div class="footer-brand">
            <div class="footer-logo">
              <img src="/logo.jpg" alt="小初学习资料圈" class="footer-logo-img" />
              <span class="footer-logo-text">小初学习资料圈</span>
            </div>
            <p class="footer-desc">
              致力于为广大师生提供优质的教育资源共享服务，让知识传递更加便捷高效。
            </p>
          </div>
          <div class="footer-links">
            <h4>快速链接</h4>
            <router-link to="/">首页</router-link>
            <router-link to="/resources">资源库</router-link>
            <router-link to="/qq-group">资料群</router-link>
            <router-link to="/about">关于我们</router-link>
            <router-link to="/announcements">平台公告</router-link>
          </div>
          <div v-if="hasFooterContact" class="footer-contact">
            <h4>联系我们</h4>
            <p v-if="footerContact.showEmail === 1 && footerContact.email">
              <el-icon><Message /></el-icon> {{ footerContact.email }}
            </p>
            <p v-if="footerContact.showPhone === 1 && footerContact.phone">
              <el-icon><Iphone /></el-icon> {{ footerContact.phone }}
            </p>
            <p v-if="footerContact.showAddress === 1 && footerContact.address">
              <el-icon><Location /></el-icon> {{ footerContact.address }}
            </p>
          </div>
        </div>
        <div class="footer-bottom">
          <p>&copy; 2024 小初学习资料圈. All rights reserved.</p>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getPublicContact } from '@/api/contact'
import { getUnreadCount, getRecentAnnouncements } from '@/api/announcement'

const router = useRouter()
const route = useRoute()

const footerContact = ref({
  email: '',
  phone: '',
  address: '',
  showEmail: 1,
  showPhone: 1,
  showAddress: 1
})

const hasFooterContact = computed(() =>
  (footerContact.value.showEmail === 1 && footerContact.value.email) ||
  (footerContact.value.showPhone === 1 && footerContact.value.phone) ||
  (footerContact.value.showAddress === 1 && footerContact.value.address)
)

// ====== 公告通知铃铛 ======
const bellVisible = ref(false)
const unreadCount = ref(0)
const recentList = ref([])
const lastReadId = ref(0)

function loadLastReadId() {
  try {
    const val = localStorage.getItem('frontAnnouncementLastReadId')
    lastReadId.value = val ? parseInt(val, 10) : 0
  } catch {
    lastReadId.value = 0
  }
}

function saveLastReadId(id) {
  lastReadId.value = id
  try {
    localStorage.setItem('frontAnnouncementLastReadId', String(id))
  } catch {
    // ignore
  }
}

async function fetchUnreadCount() {
  try {
    const res = await getUnreadCount(lastReadId.value)
    unreadCount.value = res.data?.count || 0
  } catch {
    // ignore
  }
}

async function fetchRecent() {
  try {
    const res = await getRecentAnnouncements()
    recentList.value = res.data || []
  } catch {
    // ignore
  }
}

function onBellShow() {
  fetchRecent()
}

function markAllRead() {
  const maxId = recentList.value.reduce((max, item) => Math.max(max, item.id), 0)
  if (maxId > 0) {
    saveLastReadId(maxId)
    unreadCount.value = 0
  }
  bellVisible.value = false
}

function goAnnouncement(id) {
  bellVisible.value = false
  router.push('/announcement/' + id)
}

onMounted(async () => {
  loadLastReadId()
  fetchUnreadCount()

  try {
    const res = await getPublicContact()
    if (res.data) {
      footerContact.value = res.data
    }
  } catch {
    // 使用默认值
  }
})

// 路由变化时重新获取未读数
watch(() => route.path, () => {
  loadLastReadId()
  fetchUnreadCount()
})

function goAdmin() {
  const token = localStorage.getItem('adminToken')
  if (token) {
    router.push('/admin/dashboard')
  } else {
    router.push('/admin/login')
  }
}
</script>

<style scoped>
.front-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

/* ========== Header ========== */
.site-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid var(--border-light);
  height: var(--header-height);
}

.header-inner {
  max-width: 1200px;
  height: 100%;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  align-items: center;
  gap: 40px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
  flex-shrink: 0;
}

.logo-icon {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, var(--primary), var(--primary-dark));
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 18px;
  font-weight: 800;
}

.logo-text {
  font-size: 22px;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: 1px;
}

.logo-img {
  height: 50px;
  width: 50px;
  border-radius: 50%;
  object-fit: cover;
}

.main-nav {
  display: flex;
  gap: 8px;
  flex: 1;
}

.nav-item {
  padding: 8px 20px;
  border-radius: 20px;
  font-size: 15px;
  font-weight: 500;
  color: var(--text-regular);
  text-decoration: none;
  transition: var(--transition);
}
.nav-item:hover {
  color: var(--primary);
  background: rgba(64, 158, 255, 0.08);
}
.nav-item.active {
  color: var(--primary);
  background: rgba(64, 158, 255, 0.1);
}

.header-right {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 4px;
}

/* ====== Bell notification ====== */
.bell-btn {
  font-size: 18px;
  padding: 6px;
}
.bell-badge :deep(.el-badge__content) {
  background: #e6a23c;
  border: none;
  font-size: 11px;
  height: 16px;
  line-height: 16px;
  padding: 0 5px;
}

/* ====== Popover content (frontend) ====== */
.popover-content {
  font-size: 14px;
}
.popover-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 10px;
  border-bottom: 1px solid var(--border-light);
  margin-bottom: 4px;
}
.popover-title {
  font-weight: 600;
  font-size: 15px;
  color: var(--text-primary);
}
.popover-empty {
  text-align: center;
  padding: 24px 0;
  color: var(--text-secondary);
  font-size: 13px;
}
.popover-list {
  max-height: 320px;
  overflow-y: auto;
}
.popover-item {
  padding: 10px 4px;
  border-bottom: 1px solid var(--border-light);
  cursor: pointer;
  transition: var(--transition);
  border-radius: 4px;
}
.popover-item:hover {
  background: var(--bg);
}
.popover-item:last-child {
  border-bottom: none;
}
.popover-item.unread {
  background: rgba(64, 158, 255, 0.04);
}
.item-title {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 4px;
}
.unread-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #409eff;
  flex-shrink: 0;
}
.item-text {
  font-size: 14px;
  color: var(--text-primary);
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.item-time {
  font-size: 12px;
  color: var(--text-placeholder);
  padding-left: 14px;
}
.popover-footer {
  border-top: 1px solid var(--border-light);
  padding-top: 10px;
  margin-top: 4px;
  display: flex;
  justify-content: flex-end;
  align-items: center;
}
.view-all {
  font-size: 13px;
  color: var(--primary);
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}
.view-all:hover {
  color: var(--primary-dark);
}

/* ========== Main ========== */
.site-main {
  flex: 1;
}

/* ========== Footer ========== */
.site-footer {
  background: #1a1a2e;
  color: #a0a4b8;
  padding: 60px 0 0;
  margin-top: 60px;
}

.footer-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.footer-grid {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr;
  gap: 40px;
  padding-bottom: 40px;
}

.footer-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  color: white;
  font-size: 20px;
  font-weight: 700;
  margin-bottom: 16px;
}
.footer-logo .logo-icon {
  font-size: 16px;
}

.footer-logo-img {
  height: 60px;
  width: 60px;
  border-radius: 50%;
  object-fit: cover;
}

.footer-logo-text {
  font-size: 26px;
  font-weight: 800;
  line-height: 1;
  color: #fff;
  letter-spacing: 0;
  white-space: nowrap;
}

.footer-desc {
  font-size: 14px;
  line-height: 1.8;
  color: #a0a4b8;
}

.footer-links h4,
.footer-contact h4 {
  color: white;
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
}

.footer-links a {
  display: block;
  color: #a0a4b8;
  font-size: 14px;
  padding: 4px 0;
  transition: var(--transition);
}
.footer-links a:hover {
  color: var(--primary);
}

.footer-contact p {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  margin-bottom: 8px;
}

.footer-bottom {
  border-top: 1px solid rgba(255, 255, 255, 0.08);
  padding: 20px 0;
  text-align: center;
  font-size: 13px;
}

@media (max-width: 768px) {
  .header-inner {
    gap: 4px;
    padding: 0 8px;
    flex-wrap: nowrap;
  }
  .logo-img {
    height: 28px;
    width: 28px;
    flex-shrink: 0;
  }
  .main-nav {
    flex: 1;
    gap: 0;
    overflow: hidden;
  }
  .nav-item {
    padding: 4px 6px;
    font-size: 12px;
    white-space: nowrap;
    flex-shrink: 0;
  }
  .admin-text {
    display: none;
  }
  .header-right :deep(.el-button) {
    padding: 4px 6px;
  }
  /* Footer */
  .footer-grid {
    grid-template-columns: 1fr;
    gap: 30px;
  }
  .site-footer {
    padding: 40px 0 0;
    margin-top: 40px;
  }
  .footer-logo-img {
    height: 40px;
    width: 40px;
  }
  .footer-logo-text {
    font-size: 20px;
  }
}
</style>
