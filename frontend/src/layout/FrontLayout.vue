<template>
  <div class="front-layout">
    <!-- Top Header -->
    <header class="site-header" :class="{ 'header-hidden': headerHidden, 'header-shadow': scrolled }">
      <div class="header-inner">
        <router-link to="/" class="logo">
          <img src="/logo.jpg" alt="小初学习资料圈" class="logo-img" />
        </router-link>

        <!-- Desktop Nav -->
        <nav class="main-nav">
          <router-link
            v-for="item in navItems"
            :key="item.path"
            :to="item.path"
            class="nav-item"
            :class="{ active: isActive(item.path) }"
          >
            {{ item.label }}
          </router-link>
        </nav>

        <div class="header-right">
          <!-- Notification bell -->
          <el-popover
            placement="bottom-end"
            :width="340"
            trigger="click"
            v-model:visible="bellVisible"
            @show="onBellShow"
            transition="scale-in"
          >
            <template #reference>
              <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99" class="bell-badge">
                <el-button text class="bell-btn" :class="{ 'bell-ring': unreadCount > 0 }">
                  <el-icon :size="20"><Bell /></el-icon>
                </el-button>
              </el-badge>
            </template>

            <div class="popover-content">
              <div class="popover-header">
                <span class="popover-title">公告通知</span>
                <el-button text size="small" @click="markAllRead">全部已读</el-button>
              </div>

              <div v-if="recentList.length === 0" class="popover-empty">
                <el-empty description="暂无公告" :image-size="60" />
              </div>

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

          <el-button text class="admin-btn" @click="goAdmin">
            <el-icon><Setting /></el-icon>
          </el-button>

          <!-- Mobile hamburger -->
          <el-button text class="hamburger-btn" @click="mobileMenuOpen = !mobileMenuOpen">
            <el-icon :size="22"><Menu v-if="!mobileMenuOpen" /><Close v-else /></el-icon>
          </el-button>
        </div>
      </div>
    </header>

    <!-- Mobile drawer overlay -->
    <transition name="fade">
      <div v-if="mobileMenuOpen" class="mobile-overlay" @click="mobileMenuOpen = false"></div>
    </transition>

    <!-- Mobile drawer -->
    <transition name="slide-left">
      <div v-if="mobileMenuOpen" class="mobile-drawer">
        <div class="drawer-header">
          <img src="/logo.jpg" alt="小初学习资料圈" class="drawer-logo" />
          <span class="drawer-title">小初学习资料圈</span>
        </div>
        <nav class="drawer-nav">
          <router-link
            v-for="item in navItems"
            :key="item.path"
            :to="item.path"
            class="drawer-item"
            :class="{ active: isActive(item.path) }"
            @click="mobileMenuOpen = false"
          >
            <el-icon><component :is="item.icon" /></el-icon>
            {{ item.label }}
          </router-link>
        </nav>
        <div class="drawer-footer">
          <el-button text @click="goAdmin" class="drawer-admin-btn">
            <el-icon><Setting /></el-icon> 管理后台
          </el-button>
        </div>
      </div>
    </transition>

    <!-- Main Content -->
    <main class="site-main">
      <router-view v-slot="{ Component }">
        <transition name="slide-fade" mode="out-in">
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
            <router-link to="/">首 页</router-link>
            <router-link to="/resources">资源库</router-link>
            <router-link to="/qq-group">资料群</router-link>
            <router-link to="/about">关于我们</router-link>
            <router-link to="/announcements">平台公告</router-link>
            <router-link to="/about#disclaimer">免责声明及侵权处理</router-link>
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
          <p>&copy; 2024-2025 小初学习资料圈. All rights reserved.</p>
          <p class="footer-disclaimer">{{ briefDisclaimer || '本站资源仅供个人学习交流，请于下载后24小时内删除。如有侵权，请联系我们处理。' }}</p>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, onBeforeUnmount } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getPublicContact } from '@/api/contact'
import { getPublicDisclaimer } from '@/api/disclaimer'
import { getUnreadCount, getRecentAnnouncements } from '@/api/announcement'

const router = useRouter()
const route = useRoute()

const navItems = [
  { path: '/', label: '首页', icon: 'HomeFilled' },
  { path: '/resources', label: '资源库', icon: 'Reading' },
  { path: '/qq-group', label: '资料群', icon: 'ChatLineSquare' },
  { path: '/about', label: '关于我们', icon: 'InfoFilled' },
]

function isActive(path) {
  if (path === '/') return route.path === '/'
  return route.path.startsWith(path)
}

// ====== Header scroll behavior ======
const scrolled = ref(false)
const headerHidden = ref(false)
let lastScrollY = 0

function onScroll() {
  const currentY = window.scrollY
  scrolled.value = currentY > 10

  // Hide on scroll down, show on scroll up (only past header height)
  if (currentY > 80) {
    headerHidden.value = currentY > lastScrollY
  } else {
    headerHidden.value = false
  }
  lastScrollY = currentY
}

onMounted(() => {
  window.addEventListener('scroll', onScroll, { passive: true })
})
onBeforeUnmount(() => {
  window.removeEventListener('scroll', onScroll)
})

// ====== Mobile drawer ======
const mobileMenuOpen = ref(false)

// ====== Footer contact ======
const footerContact = ref({
  email: '', phone: '', address: '',
  showEmail: 1, showPhone: 1, showAddress: 1
})

const hasFooterContact = computed(() =>
  (footerContact.value.showEmail === 1 && footerContact.value.email) ||
  (footerContact.value.showPhone === 1 && footerContact.value.phone) ||
  (footerContact.value.showAddress === 1 && footerContact.value.address)
)

// ====== Notification bell ======
const bellVisible = ref(false)
const unreadCount = ref(0)
const recentList = ref([])
const lastReadId = ref(0)

// ====== Footer disclaimer ======
const briefDisclaimer = ref('')

function loadLastReadId() {
  try {
    const val = localStorage.getItem('frontAnnouncementLastReadId')
    lastReadId.value = val ? parseInt(val, 10) : 0
  } catch { lastReadId.value = 0 }
}

function saveLastReadId(id) {
  lastReadId.value = id
  try { localStorage.setItem('frontAnnouncementLastReadId', String(id)) } catch { /* noop */ }
}

async function fetchUnreadCount() {
  try {
    const res = await getUnreadCount(lastReadId.value)
    unreadCount.value = res.data?.count || 0
  } catch { /* noop */ }
}

async function fetchRecent() {
  try {
    const res = await getRecentAnnouncements()
    recentList.value = res.data || []
  } catch { /* noop */ }
}

function onBellShow() { fetchRecent() }

function markAllRead() {
  const maxId = recentList.value.reduce((max, item) => Math.max(max, item.id), 0)
  if (maxId > 0) saveLastReadId(maxId)
  unreadCount.value = 0
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
    if (res.data) footerContact.value = res.data
  } catch { /* use defaults */ }

  try {
    const res = await getPublicDisclaimer()
    if (res.data) briefDisclaimer.value = res.data.briefDisclaimer || ''
  } catch { /* use defaults */ }
})

watch(() => route.path, () => {
  loadLastReadId()
  fetchUnreadCount()
  mobileMenuOpen.value = false
})

function goAdmin() {
  const token = localStorage.getItem('adminToken')
  router.push(token ? '/admin/dashboard' : '/admin/login')
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
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: var(--z-sticky);
  height: var(--header-height);
  background: rgba(255, 255, 255, 0.88);
  backdrop-filter: blur(16px) saturate(180%);
  -webkit-backdrop-filter: blur(16px) saturate(180%);
  border-bottom: 1px solid transparent;
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1),
              box-shadow 0.3s ease,
              background 0.3s ease;
}

.site-header.header-shadow {
  box-shadow: 0 1px 8px rgba(0, 0, 0, 0.06);
  border-bottom-color: var(--border-light);
}

.site-header.header-hidden {
  transform: translateY(-100%);
}

.header-inner {
  max-width: var(--content-max-width);
  height: 100%;
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  align-items: center;
  gap: 32px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
  flex-shrink: 0;
}

.logo-img {
  height: 40px;
  width: 40px;
  border-radius: 50%;
  object-fit: cover;
  transition: var(--transition);
}
.logo-img:hover {
  transform: scale(1.05);
}

/* ====== Desktop Nav ====== */
.main-nav {
  display: flex;
  gap: 4px;
  flex: 1;
}

.nav-item {
  position: relative;
  padding: 8px 20px;
  border-radius: 8px;
  font-size: 15px;
  font-weight: 500;
  color: var(--text-regular);
  text-decoration: none;
  transition: var(--transition);
}
.nav-item:hover {
  color: var(--primary);
  background: var(--primary-bg);
}
.nav-item.active {
  color: var(--primary);
  font-weight: 600;
}
.nav-item.active::after {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 50%;
  transform: translateX(-50%);
  width: 20px;
  height: 3px;
  background: var(--primary-gradient);
  border-radius: 2px;
}

/* ====== Header Right ====== */
.header-right {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 4px;
}

.bell-btn {
  font-size: 18px;
  padding: 8px;
  border-radius: 8px;
  transition: var(--transition);
}
.bell-btn:hover {
  background: var(--primary-bg);
}
.bell-ring :deep(.el-icon) {
  animation: pulse 2s ease-in-out infinite;
}

.bell-badge :deep(.el-badge__content) {
  background: linear-gradient(135deg, #e6a23c, #d48806);
  border: 2px solid white;
  font-size: 11px;
  height: 18px;
  line-height: 14px;
  padding: 0 5px;
}

.admin-btn {
  font-size: 14px;
  padding: 8px 12px;
  border-radius: 8px;
  margin-left: 4px;
  transition: var(--transition);
}
.admin-btn:hover {
  background: var(--primary-bg);
}

.hamburger-btn {
  display: none !important;
  font-size: 18px;
  padding: 8px;
  border-radius: 8px;
}

/* ====== Popover ====== */
.popover-content {
  font-size: 14px;
}
.popover-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--border-light);
}
.popover-title {
  font-weight: 600;
  font-size: 15px;
  color: var(--text-primary);
}
.popover-empty {
  padding: 8px 0;
}
.popover-list {
  max-height: 340px;
  overflow-y: auto;
  margin: 0 -4px;
}
.popover-item {
  padding: 10px 8px;
  border-bottom: 1px solid var(--border-light);
  cursor: pointer;
  transition: var(--transition);
  border-radius: 6px;
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
  background: var(--primary);
  flex-shrink: 0;
  box-shadow: 0 0 6px rgba(64, 158, 255, 0.4);
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
  padding-top: 12px;
  margin-top: 4px;
  display: flex;
  justify-content: flex-end;
}
.view-all {
  font-size: 13px;
  color: var(--primary);
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
  padding-top: var(--header-height);
}

/* ========== Mobile Drawer ========== */
.mobile-overlay {
  position: fixed;
  inset: 0;
  z-index: var(--z-overlay);
  background: rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(4px);
}

.mobile-drawer {
  position: fixed;
  top: 0;
  left: 0;
  bottom: 0;
  width: 280px;
  z-index: calc(var(--z-overlay) + 1);
  background: white;
  display: flex;
  flex-direction: column;
  box-shadow: var(--shadow-xl);
}

.drawer-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 24px 20px 20px;
  border-bottom: 1px solid var(--border-light);
}
.drawer-logo {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}
.drawer-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
}

.drawer-nav {
  flex: 1;
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.drawer-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-radius: 8px;
  font-size: 15px;
  font-weight: 500;
  color: var(--text-regular);
  text-decoration: none;
  transition: var(--transition);
}
.drawer-item:hover {
  background: var(--primary-bg);
  color: var(--primary);
}
.drawer-item.active {
  background: var(--primary-bg);
  color: var(--primary);
  font-weight: 600;
}

.drawer-footer {
  padding: 12px;
  border-top: 1px solid var(--border-light);
}
.drawer-admin-btn {
  width: 100%;
  justify-content: flex-start;
  font-size: 14px;
  padding: 10px 16px;
  border-radius: 8px;
}

/* Slide-left transition for drawer */
.slide-left-enter-active,
.slide-left-leave-active {
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
.slide-left-enter-from,
.slide-left-leave-to {
  transform: translateX(-100%);
}

/* ========== Footer ========== */
.site-footer {
  background: linear-gradient(180deg, #1a1a2e 0%, #15152a 100%);
  color: #a0a4b8;
  padding: 60px 0 0;
  margin-top: 80px;
  position: relative;
}
.site-footer::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(64, 158, 255, 0.2), transparent);
}

.footer-inner {
  max-width: var(--content-max-width);
  margin: 0 auto;
  padding: 0 24px;
}

.footer-grid {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr;
  gap: 48px;
  padding-bottom: 40px;
}

.footer-logo {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.footer-logo-img {
  height: 48px;
  width: 48px;
  border-radius: 50%;
  object-fit: cover;
}

.footer-logo-text {
  font-size: 22px;
  font-weight: 800;
  color: #fff;
  letter-spacing: 1px;
}

.footer-desc {
  font-size: 14px;
  line-height: 1.8;
  color: #8a8fa8;
  max-width: 360px;
}

.footer-links h4,
.footer-contact h4 {
  color: #e0e0e0;
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 16px;
  position: relative;
  padding-bottom: 10px;
}
.footer-links h4::after,
.footer-contact h4::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 24px;
  height: 2px;
  background: var(--primary);
  border-radius: 1px;
}

.footer-links a {
  display: block;
  color: #8a8fa8;
  font-size: 14px;
  padding: 5px 0;
  transition: var(--transition);
}
.footer-links a:hover {
  color: var(--primary);
  padding-left: 4px;
}

.footer-contact p {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  margin-bottom: 10px;
  color: #8a8fa8;
}

.footer-bottom {
  border-top: 1px solid rgba(255, 255, 255, 0.06);
  padding: 20px 0;
  text-align: center;
  font-size: 13px;
  color: #6a6f88;
}
.footer-bottom p {
  margin: 0;
}
.footer-bottom p + p {
  margin-top: 6px;
}
.footer-disclaimer {
  font-size: 12px;
  color: #555a75;
}

/* ========== Responsive ========== */
@media (max-width: 768px) {
  .header-inner {
    padding: 0 16px;
    gap: 8px;
  }
  .logo-img {
    height: 34px;
    width: 34px;
  }
  .main-nav {
    display: none;
  }
  .hamburger-btn {
    display: flex !important;
  }

  .footer-grid {
    grid-template-columns: 1fr;
    gap: 32px;
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
    font-size: 18px;
  }
}

@media (min-width: 769px) {
  .hamburger-btn {
    display: none !important;
  }
  .mobile-overlay,
  .mobile-drawer {
    display: none !important;
  }
}
</style>
