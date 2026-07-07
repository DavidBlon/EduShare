<template>
  <div class="admin-layout">
    <!-- Sidebar -->
    <div class="admin-sidebar-wrapper" :class="{ collapsed: isCollapse }">
      <div class="sidebar-inner">
        <div class="sidebar-header">
          <div class="sidebar-logo">
            <img src="/logo.jpg" alt="小初学习资料圈" class="sidebar-logo-img" :class="{ collapsed: isCollapse }" />
            <transition name="fade">
              <span v-if="!isCollapse" class="sidebar-brand">小初学习资料圈管理后台</span>
            </transition>
          </div>
        </div>

        <div class="sidebar-bell" :class="{ collapsed: isCollapse }" @click="bellVisible = true">
          <n-badge :value="unreadCount" :max="99" :show="unreadCount > 0">
            <n-icon :size="isCollapse ? 20 : 18"><NotificationsOutline /></n-icon>
          </n-badge>
          <transition name="fade">
            <span v-if="!isCollapse" class="bell-text">公告通知</span>
          </transition>
        </div>

        <div class="sidebar-menu">
          <div v-for="section in menuSections" :key="section.label" class="menu-section">
            <div v-if="!isCollapse && section.label" class="menu-label">{{ section.label }}</div>
            <div
              v-for="item in section.items"
              :key="item.key"
              class="menu-item"
              :class="{ active: activeMenu === item.key }"
              @click="handleMenuClick(item)"
            >
              <span class="menu-icon" v-html="item.icon"></span>
              <transition name="fade">
                <span v-if="!isCollapse" class="menu-text">{{ item.label }}</span>
              </transition>
              <div v-if="activeMenu === item.key" class="menu-active-indicator"></div>
            </div>
          </div>
        </div>
      </div>

      <div class="sidebar-footer" v-if="!isCollapse">
        <div class="sidebar-footer-user">
          <n-avatar round :size="28" color="#6366f1" />
          <span class="footer-username">{{ adminInfo?.nickname || adminInfo?.username || '管理员' }}</span>
        </div>
      </div>
    </div>

    <!-- Mobile overlay -->
    <div class="mobile-overlay" v-if="!isCollapse && mobileSidebar" @click="isCollapse = true"></div>

    <!-- Bell Modal -->
    <n-modal v-model:show="bellVisible" preset="card" title="公告通知" style="width:380px" :bordered="false" :segmented="{ footer: true }">
      <template #header-extra>
        <n-button text size="tiny" @click="markAllRead">全部已读</n-button>
      </template>
      <div v-if="recentList.length === 0" class="bell-empty">
        <n-empty description="暂无公告" />
      </div>
      <div v-else class="bell-list">
        <div
          v-for="item in recentList"
          :key="item.id"
          class="bell-item"
          :class="{ unread: item.id > lastReadId }"
          @click="goAnnouncement(item.id)"
        >
          <div class="bell-item-dot" v-if="item.id > lastReadId"></div>
          <div class="bell-item-content">
            <div class="bell-item-title">{{ item.title }}</div>
            <div class="bell-item-time">{{ item.createdAt }}</div>
          </div>
        </div>
      </div>
      <template #footer>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <n-button text size="tiny" @click="openCreateDialog">发布新公告</n-button>
          <router-link to="/admin/announcement" class="bell-view-all" @click="bellVisible = false">查看全部</router-link>
        </div>
      </template>
    </n-modal>

    <!-- Main Area -->
    <div class="admin-main" :class="{ collapsed: isCollapse }">
      <header class="admin-topbar">
        <div class="topbar-left">
          <n-button quaternary @click="isCollapse = !isCollapse" class="collapse-btn">
            <template #icon>
              <n-icon :size="18"><MenuOutline v-if="!isCollapse" /><GridOutline v-else /></n-icon>
            </template>
          </n-button>
          <n-breadcrumb>
            <n-breadcrumb-item v-if="$route.meta.title">{{ $route.meta.title }}</n-breadcrumb-item>
          </n-breadcrumb>
        </div>
        <div class="topbar-right">
          <n-dropdown trigger="click" :options="dropdownOptions" @select="handleDropdown">
            <div class="user-info">
              <n-avatar round :size="32" color="#6366f1" />
              <span class="username">{{ adminInfo?.nickname || adminInfo?.username || '管理员' }}</span>
              <n-icon :size="14" class="arrow-down"><ChevronDownOutline /></n-icon>
            </div>
          </n-dropdown>
        </div>
      </header>

      <main class="admin-content">
        <router-view v-slot="{ Component }">
          <transition name="slide-fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>

      <!-- Admin Footer -->
      <footer class="admin-footer">
        <span>小初学习资料圈</span>
        <span class="admin-footer-divider">|</span>
        <span>&copy; 2024-2026 小初学习资料圈 Admin</span>
      </footer>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useDialog } from 'naive-ui'
import { NotificationsOutline, MenuOutline, GridOutline, ChevronDownOutline } from '@vicons/ionicons5'
import { getProfile } from '@/api/admin'
import { getUnreadCount, getRecentAnnouncements } from '@/api/announcement'

const router = useRouter()
const route = useRoute()
const dialog = useDialog()

const isCollapse = ref(false)
const adminInfo = ref({})
const mobileSidebar = ref(false)

onMounted(() => {
  const mql = window.matchMedia('(max-width: 768px)')
  isCollapse.value = mql.matches
  mobileSidebar.value = mql.matches
  mql.addEventListener('change', e => {
    isCollapse.value = e.matches
    mobileSidebar.value = e.matches
  })
})

const activeMenu = computed(() => route.path)

const isSuperAdmin = computed(() => adminInfo.value?.role === 0)

const svg = (path) => `<svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">${path}</svg>`

const menuSections = computed(() => [
  {
    label: '',
    items: [
      { key: '/admin/dashboard', label: '控制台', icon: svg('<rect x="3" y="3" width="7" height="7" rx="1"/><rect x="14" y="3" width="7" height="7" rx="1"/><rect x="3" y="14" width="7" height="7" rx="1"/><rect x="14" y="14" width="7" height="7" rx="1"/>') }
    ]
  },
  {
    label: '内容管理',
    items: [
      { key: '/admin/category', label: '分类管理', icon: svg('<path d="M4 6h16M4 12h16M4 18h16"/>') },
      { key: '/admin/tag', label: '标签管理', icon: svg('<path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"/><line x1="7" y1="7" x2="7.01" y2="7"/>') },
      { key: '/admin/resource', label: '资源管理', icon: svg('<path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/>') },
      { key: '/admin/download-log', label: '下载日志', icon: svg('<polyline points="8 17 12 21 16 17"/><line x1="12" y1="12" x2="12" y2="21"/><path d="M20.88 18.09A5 5 0 0 0 18 9h-1.26A8 8 0 1 0 3 16.29"/>') },
      { key: '/admin/import', label: '批量导入', icon: svg('<path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="17 8 12 3 7 8"/><line x1="12" y1="3" x2="12" y2="15"/>') },
      { key: '/admin/keyword-rule', label: '关键词规则', icon: svg('<circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/><line x1="8" y1="11" x2="14" y2="11"/>') },
      { key: '/admin/announcement', label: '公告管理', icon: svg('<path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/>') }
    ]
  },
  {
    label: '系统设置',
    items: [
      { key: '/admin/password', label: '修改密码', icon: svg('<rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/>') },
      ...(isSuperAdmin.value ? [
        { key: '/admin/admin-manage', label: '管理员管理', icon: svg('<path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/>') },
        { key: '/admin/qrcode', label: '资料群二维码', icon: svg('<rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/>') },
        { key: '/admin/contact', label: '联系方式', icon: svg('<path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/><polyline points="22,6 12,13 2,6"/>') },
        { key: '/admin/disclaimer', label: '免责声明', icon: svg('<circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/>') }
      ] : [])
    ]
  },
  {
    label: '',
    items: [
      { key: '/front-home', label: '返回前台', icon: svg('<path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/>') }
    ]
  }
])

function handleMenuClick(item) {
  if (item.key === '/front-home') {
    window.location.href = '/'
    return
  }
  router.push(item.key)
}

// Notification bell
const bellVisible = ref(false)
const unreadCount = ref(0)
const recentList = ref([])
const lastReadId = ref(0)

function loadLastReadId() {
  try { lastReadId.value = parseInt(localStorage.getItem('adminAnnouncementLastReadId') || '0', 10) }
  catch { lastReadId.value = 0 }
}
function saveLastReadId(id) {
  lastReadId.value = id
  try { localStorage.setItem('adminAnnouncementLastReadId', String(id)) } catch { }
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
  router.push('/admin/announcement')
}
function openCreateDialog() {
  bellVisible.value = false
  router.push('/admin/announcement')
}

onMounted(async () => {
  loadLastReadId()
  fetchUnreadCount()
  try {
    const res = await getProfile()
    adminInfo.value = res.data
    localStorage.setItem('adminInfo', JSON.stringify(res.data))
  } catch { }
})

watch(() => route.path, () => { loadLastReadId(); fetchUnreadCount() })

const dropdownOptions = [
  { label: '修改密码', key: 'password' },
  { label: '返回前台', key: 'home' },
  { type: 'divider' },
  { label: '退出登录', key: 'logout' }
]

function handleDropdown(key) {
  if (key === 'logout') {
    dialog.warning({
      title: '提示',
      content: '确定要退出登录吗？',
      positiveText: '确定',
      negativeText: '取消',
      onPositiveClick: () => {
        localStorage.removeItem('adminToken')
        localStorage.removeItem('adminInfo')
        router.push('/admin/login')
      }
    })
  } else if (key === 'password') {
    router.push('/admin/password')
  } else if (key === 'home') {
    window.location.href = '/'
  }
}
</script>

<style scoped>
.admin-layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

.admin-sidebar-wrapper {
  width: 240px;
  flex-shrink: 0;
  background: linear-gradient(180deg, #0a0a1a 0%, #0d0d24 100%);
  display: flex;
  flex-direction: column;
  border-right: 1px solid rgba(255, 255, 255, 0.04);
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  z-index: 100;
}
.admin-sidebar-wrapper.collapsed { width: 64px; }

.sidebar-inner {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
  overflow-x: hidden;
}
.sidebar-inner::-webkit-scrollbar { width: 3px; }
.sidebar-inner::-webkit-scrollbar-thumb { background: rgba(255,255,255,0.1); border-radius: 2px; }

.sidebar-header {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  flex-shrink: 0;
}

.sidebar-logo { display: flex; align-items: center; gap: 10px; }
.sidebar-logo-img {
  height: 36px; width: 36px; border-radius: 50%; object-fit: cover;
  transition: all 0.3s; flex-shrink: 0;
}
.sidebar-logo-img.collapsed { height: 32px; width: 32px; }
.sidebar-brand {
  font-size: 16px; font-weight: 700;
  color: #ffffff;
  white-space: nowrap;
}

.sidebar-bell {
  display: flex; align-items: center; gap: 12px;
  padding: 12px 20px; cursor: pointer; color: #8b8fa3;
  transition: all 0.2s; border-bottom: 1px solid rgba(255,255,255,0.04);
  flex-shrink: 0;
}
.sidebar-bell:hover { color: #e0e0e0; background: rgba(255,255,255,0.04); }
.sidebar-bell.collapsed { justify-content: center; padding: 12px 0; }
.bell-text { font-size: 13px; font-weight: 500; }

.sidebar-menu { flex: 1; padding: 8px 0; }
.menu-section { margin-bottom: 4px; }
.menu-label {
  padding: 16px 20px 6px; font-size: 11px; font-weight: 600;
  color: rgba(255,255,255,0.3); text-transform: uppercase; letter-spacing: 1px;
}
.menu-item {
  position: relative; display: flex; align-items: center; gap: 12px;
  height: 42px; padding: 0 20px; cursor: pointer; color: #8b8fa3;
  font-size: 14px; font-weight: 500; transition: all 0.2s;
  margin: 1px 8px; border-radius: 8px;
}
.menu-item:hover { color: #e0e0e0; background: rgba(255,255,255,0.06); }
.menu-item.active { color: #fff; background: rgba(99,102,241,0.15); }
.menu-icon { display: flex; align-items: center; width: 18px; height: 18px; flex-shrink: 0; }
.menu-text { white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.menu-active-indicator {
  position: absolute; left: 0; top: 50%; transform: translateY(-50%);
  width: 3px; height: 18px;
  background: linear-gradient(180deg, #6366f1, #8b5cf6);
  border-radius: 0 3px 3px 0;
}

.sidebar-footer {
  padding: 12px 16px; border-top: 1px solid rgba(255,255,255,0.05);
  flex-shrink: 0;
}
.sidebar-footer-user { display: flex; align-items: center; gap: 10px; }
.footer-username { font-size: 13px; color: #8b8fa3; font-weight: 500; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

.mobile-overlay { display: none; }
@media (max-width: 768px) {
  .mobile-overlay {
    display: block; position: fixed; inset: 0; z-index: 99;
    background: rgba(0,0,0,0.4); backdrop-filter: blur(4px);
  }
}

.bell-empty { padding: 20px 0; }
.bell-list { max-height: 340px; overflow-y: auto; margin: 0 -12px; }
.bell-item {
  display: flex; gap: 8px; padding: 10px 12px; cursor: pointer;
  border-radius: 8px; transition: all 0.2s;
}
.bell-item:hover { background: rgba(99,102,241,0.04); }
.bell-item.unread { background: rgba(99,102,241,0.04); }
.bell-item-dot {
  width: 8px; height: 8px; border-radius: 50%; background: #6366f1;
  flex-shrink: 0; margin-top: 6px; box-shadow: 0 0 6px rgba(99,102,241,0.4);
}
.bell-item-content { flex: 1; min-width: 0; }
.bell-item-title { font-size: 14px; font-weight: 500; color: var(--text-primary); margin-bottom: 2px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.bell-item-time { font-size: 12px; color: var(--text-placeholder); }
.bell-view-all { font-size: 13px; color: var(--primary); text-decoration: none; }

.admin-main { flex: 1; display: flex; flex-direction: column; overflow: hidden; min-width: 0; }

.admin-topbar {
  height: 64px;
  background: rgba(255,255,255,0.88);
  backdrop-filter: blur(12px) saturate(180%);
  -webkit-backdrop-filter: blur(12px) saturate(180%);
  border-bottom: 1px solid var(--border-light);
  display: flex; align-items: center; justify-content: space-between;
  padding: 0 24px; flex-shrink: 0;
}
.topbar-left { display: flex; align-items: center; gap: 16px; }
.collapse-btn { padding: 8px; border-radius: 8px; color: var(--text-secondary); }
.collapse-btn:hover { background: var(--bg); }
.topbar-right { display: flex; align-items: center; }
.user-info { display: flex; align-items: center; gap: 8px; cursor: pointer; padding: 6px 12px; border-radius: 8px; transition: all 0.2s; }
.user-info:hover { background: var(--bg); }
.username { font-size: 14px; font-weight: 500; color: var(--text-primary); }
.arrow-down { color: var(--text-secondary); }

.admin-content { flex: 1; padding: 24px; overflow-y: auto; background: var(--bg); }

.admin-footer {
  height: 48px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 13px;
  color: #475569;
  background: #f8fafc;
  border-top: 1px solid var(--border-light);
}
.admin-footer-divider {
  color: #cbd5e1;
}

@media (max-width: 768px) {
  .admin-content { padding: 16px; }
  .admin-topbar { padding: 0 12px; }
  .username { display: none; }
}
</style>
