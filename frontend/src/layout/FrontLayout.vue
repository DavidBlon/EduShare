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
          <n-badge :value="unreadCount" :max="99" :show="unreadCount > 0">
            <n-button quaternary class="bell-btn" :class="{ 'bell-ring': unreadCount > 0 }" @click="bellVisible = true">
              <template #icon>
                <n-icon :size="20"><NotificationsOutline /></n-icon>
              </template>
            </n-button>
          </n-badge>

          <n-button quaternary class="admin-btn" @click="goAdmin">
            <template #icon>
              <n-icon><SettingsOutline /></n-icon>
            </template>
          </n-button>

          <!-- Mobile hamburger -->
          <n-button quaternary class="hamburger-btn" @click="mobileMenuOpen = !mobileMenuOpen">
            <template #icon>
              <n-icon :size="22"><MenuOutline v-if="!mobileMenuOpen" /><CloseOutline v-else /></n-icon>
            </template>
          </n-button>
        </div>
      </div>
    </header>

    <!-- Bell Modal -->
    <n-modal v-model:show="bellVisible" preset="card" title="公告通知" style="width:380px" :bordered="false" :segmented="{ footer: true }">
      <template #header-extra>
        <n-button text size="tiny" @click="markAllRead">全部已读</n-button>
      </template>
      <div v-if="recentList.length === 0" class="popover-empty">
        <n-empty description="暂无公告" />
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
      <template #footer>
        <div style="display:flex;justify-content:flex-end;align-items:center">
          <router-link to="/announcements" class="view-all" @click="bellVisible = false">查看全部公告 →</router-link>
        </div>
      </template>
    </n-modal>

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
            <n-icon><component :is="iconMap[item.icon]" /></n-icon>
            {{ item.label }}
          </router-link>
        </nav>
        <div class="drawer-footer">
          <n-button quaternary @click="goAdmin" class="drawer-admin-btn">
            <template #icon><n-icon><SettingsOutline /></n-icon></template>
            管理后台
          </n-button>
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
            <router-link to="/about#disclaimer">免责声明及侵权处理</router-link>
          </div>
          <div v-if="hasFooterContact" class="footer-contact">
            <h4>联系我们</h4>
            <p v-if="footerContact.showEmail === 1 && footerContact.email">
              <n-icon><ChatboxEllipsesOutline /></n-icon> {{ footerContact.email }}
            </p>
            <p v-if="footerContact.showPhone === 1 && footerContact.phone">
              <n-icon><PhonePortraitOutline /></n-icon> {{ footerContact.phone }}
            </p>
            <p v-if="footerContact.showAddress === 1 && footerContact.address">
              <n-icon><LocationOutline /></n-icon> {{ footerContact.address }}
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
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  NotificationsOutline, SettingsOutline, MenuOutline, CloseOutline,
  HomeOutline, BookOutline, ChatbubblesOutline, InformationCircleOutline,
  ChatboxEllipsesOutline, PhonePortraitOutline, LocationOutline
} from '@vicons/ionicons5'
import { getPublicContact } from '@/api/contact'
import { getPublicDisclaimer } from '@/api/disclaimer'
import { getUnreadCount, getRecentAnnouncements } from '@/api/announcement'

const router = useRouter()
const route = useRoute()

// ----- Nav items -----
const navItems = [
  { path: '/', label: '首页', icon: 'home' },
  { path: '/resources', label: '资源库', icon: 'resources' },
  { path: '/qq-group', label: '资料群', icon: 'qq' },
  { path: '/about', label: '关于我们', icon: 'about' },
]

const iconMap = {
  home: HomeOutline,
  resources: BookOutline,
  qq: ChatbubblesOutline,
  about: InformationCircleOutline
}

function isActive(path) {
  if (path === '/') return route.path === '/'
  return route.path.startsWith(path)
}

// ----- Scroll behavior -----
const headerHidden = ref(false)
const scrolled = ref(false)
let lastScrollY = 0

function handleScroll() {
  const currentY = window.scrollY
  scrolled.value = currentY > 10
  if (currentY > lastScrollY && currentY > 80) {
    headerHidden.value = true
  } else {
    headerHidden.value = false
  }
  lastScrollY = currentY
}

onMounted(() => window.addEventListener('scroll', handleScroll, { passive: true }))
onUnmounted(() => window.removeEventListener('scroll', handleScroll))

// ----- Mobile drawer -----
const mobileMenuOpen = ref(false)

// ----- Notification bell -----
const bellVisible = ref(false)
const unreadCount = ref(0)
const recentList = ref([])
const lastReadId = ref(0)

function loadLastReadId() {
  try { lastReadId.value = parseInt(localStorage.getItem('frontAnnouncementLastReadId') || '0', 10) }
  catch { lastReadId.value = 0 }
}
function saveLastReadId(id) {
  lastReadId.value = id
  try { localStorage.setItem('frontAnnouncementLastReadId', String(id)) } catch { }
}

async function fetchUnreadCount() {
  try {
    const res = await getUnreadCount(lastReadId.value)
    unreadCount.value = res.data?.count || 0
  } catch { }
}
async function fetchRecent() {
  try {
    const res = await getRecentAnnouncements()
    recentList.value = res.data || []
  } catch { }
}
function markAllRead() {
  const maxId = recentList.value.reduce((max, item) => Math.max(max, item.id), 0)
  if (maxId > 0) saveLastReadId(maxId)
  unreadCount.value = 0
  bellVisible.value = false
}
function goAnnouncement(id) {
  bellVisible.value = false
  router.push('/announcements')
}

// ----- Footer contact & disclaimer -----
const footerContact = ref({ showEmail: 1, email: '', showPhone: 1, phone: '', showAddress: 1, address: '' })
const briefDisclaimer = ref('')

const hasFooterContact = computed(() =>
  (footerContact.value.showEmail === 1 && footerContact.value.email) ||
  (footerContact.value.showPhone === 1 && footerContact.value.phone) ||
  (footerContact.value.showAddress === 1 && footerContact.value.address)
)

// ----- Admin navigation -----
function goAdmin() {
  window.location.href = '/admin'
}

onMounted(async () => {
  loadLastReadId()
  fetchUnreadCount()
  fetchRecent()

  try {
    const res = await getPublicContact()
    if (res.data) footerContact.value = res.data
  } catch { }
  try {
    const res = await getPublicDisclaimer()
    if (res.data) briefDisclaimer.value = res.data.briefContent || ''
  } catch { }
})
</script>

<style scoped>
/* ===== Header ===== */
.site-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: var(--header-height);
  z-index: var(--z-sticky);
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(12px) saturate(180%);
  -webkit-backdrop-filter: blur(12px) saturate(180%);
  border-bottom: 1px solid var(--border-light);
  transition: transform 0.35s cubic-bezier(0.4, 0, 0.2, 1),
              box-shadow 0.35s cubic-bezier(0.4, 0, 0.2, 1);
}
.site-header.header-hidden {
  transform: translateY(-100%);
}
.site-header.header-shadow {
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}

.header-inner {
  max-width: var(--content-max-width);
  margin: 0 auto;
  padding: 0 24px;
  height: 100%;
  display: flex;
  align-items: center;
  gap: 32px;
}

.logo {
  display: flex;
  align-items: center;
  flex-shrink: 0;
  text-decoration: none;
}
.logo-img {
  height: 36px;
  width: 36px;
  border-radius: 50%;
  object-fit: cover;
}

/* Desktop Nav */
.main-nav {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 4px;
}
.nav-item {
  position: relative;
  padding: 8px 16px;
  font-size: 14px;
  font-weight: 500;
  color: var(--text-regular);
  border-radius: 8px;
  transition: all 0.2s;
  text-decoration: none;
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
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 20px;
  height: 3px;
  background: var(--primary-gradient);
  border-radius: 2px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-shrink: 0;
}
.bell-btn,
.admin-btn,
.hamburger-btn {
  padding: 6px;
  border-radius: 8px;
  color: var(--text-secondary);
  transition: all 0.2s;
}
.bell-btn:hover,
.admin-btn:hover,
.hamburger-btn:hover {
  color: var(--primary);
  background: var(--primary-bg);
}
.bell-ring {
  animation: bellShake 0.5s ease-in-out infinite;
}
@keyframes bellShake {
  0%, 100% { transform: rotate(0); }
  25% { transform: rotate(8deg); }
  75% { transform: rotate(-8deg); }
}
.hamburger-btn {
  display: none;
}

/* ===== Bell Popover ===== */
.popover-empty {
  padding: 20px 0;
}
.popover-list {
  max-height: 340px;
  overflow-y: auto;
  margin: 0 -12px;
}
.popover-item {
  padding: 10px 12px;
  cursor: pointer;
  border-radius: 8px;
  transition: all 0.2s;
}
.popover-item:hover {
  background: var(--primary-bg);
}
.popover-item.unread {
  background: rgba(99, 102, 241, 0.04);
}
.item-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 2px;
}
.unread-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--primary);
  flex-shrink: 0;
  box-shadow: 0 0 6px rgba(99, 102, 241, 0.4);
}
.item-text {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.item-time {
  font-size: 12px;
  color: var(--text-placeholder);
  padding-left: 16px;
}
.view-all {
  font-size: 13px;
  color: var(--primary);
  text-decoration: none;
}

/* ===== Mobile Overlay ===== */
.mobile-overlay {
  position: fixed;
  inset: 0;
  z-index: var(--z-overlay);
  background: rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
}

/* ===== Mobile Drawer ===== */
.mobile-drawer {
  position: fixed;
  top: 0;
  left: 0;
  bottom: 0;
  width: 280px;
  z-index: calc(var(--z-overlay) + 1);
  background: var(--bg-white);
  display: flex;
  flex-direction: column;
  box-shadow: var(--shadow-xl);
}
.drawer-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px;
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
  padding: 12px 0;
  overflow-y: auto;
}
.drawer-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 20px;
  font-size: 15px;
  font-weight: 500;
  color: var(--text-regular);
  text-decoration: none;
  transition: all 0.2s;
}
.drawer-item:hover {
  color: var(--primary);
  background: var(--primary-bg);
}
.drawer-item.active {
  color: var(--primary);
  font-weight: 600;
  background: var(--primary-bg);
}
.drawer-footer {
  padding: 16px 20px;
  border-top: 1px solid var(--border-light);
}
.drawer-admin-btn {
  width: 100%;
  justify-content: flex-start;
  color: var(--text-secondary);
}

/* ===== Main Content ===== */
.site-main {
  min-height: calc(100vh - 200px);
  padding-top: var(--header-height);
}

/* ===== Footer ===== */
.site-footer {
  background: #0f172a;
  color: #94a3b8;
  padding: 48px 0 24px;
}
.footer-inner {
  max-width: var(--content-max-width);
  margin: 0 auto;
  padding: 0 24px;
}
.footer-grid {
  display: grid;
  grid-template-columns: 1.6fr 1fr 1.2fr;
  gap: 48px;
  margin-bottom: 32px;
}
.footer-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}
.footer-logo-img {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
}
.footer-logo-text {
	  font-size: 18px;
	  font-weight: 700;
	  color: #ffffff;
	}
.footer-desc {
  font-size: 14px;
  line-height: 1.7;
  color: #64748b;
  margin: 0;
  max-width: 360px;
}
.footer-links h4,
.footer-contact h4 {
  font-size: 14px;
  font-weight: 600;
  color: #e2e8f0;
  margin: 0 0 16px;
}
.footer-links a {
  display: block;
  padding: 4px 0;
  font-size: 14px;
  color: #64748b;
  text-decoration: none;
  transition: color 0.2s;
}
.footer-links a:hover {
  color: var(--primary-light);
}
.footer-contact p {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #64748b;
  margin: 0 0 8px;
}
.footer-bottom {
  border-top: 1px solid rgba(255, 255, 255, 0.06);
  padding-top: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
  gap: 16px;
}
.footer-bottom p {
  margin: 0;
}
.footer-disclaimer {
  color: #475569;
  text-align: right;
  white-space: nowrap;
}

/* ===== Transitions ===== */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.slide-left-enter-active,
.slide-left-leave-active {
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
.slide-left-enter-from {
  transform: translateX(-100%);
}
.slide-left-leave-to {
  transform: translateX(-100%);
}

/* ===== Responsive ===== */
@media (max-width: 768px) {
  .header-inner {
    padding: 0 16px;
    gap: 12px;
  }
  .main-nav {
    display: none;
  }
  .admin-btn {
    display: none;
  }
  .hamburger-btn {
    display: flex;
  }
  .footer-grid {
    grid-template-columns: 1fr;
    gap: 32px;
  }
  .footer-bottom {
    flex-direction: column;
    text-align: center;
  }
  .footer-disclaimer {
    text-align: center;
  }
}

@media (min-width: 769px) {
  .hamburger-btn {
    display: none;
  }
}
</style>
