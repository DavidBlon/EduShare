<template>
  <div class="admin-layout">
    <!-- Sidebar -->
    <el-menu
      :default-active="activeMenu"
      :collapse="isCollapse"
      class="admin-sidebar"
      background-color="#1a1a2e"
      text-color="#a0a4b8"
      active-text-color="#409eff"
      router
    >
      <div class="sidebar-header">
        <div class="sidebar-logo">
          <img src="/logo.jpg" alt="小初学习资料圈" class="sidebar-logo-img" :class="{ collapsed: isCollapse }" />
        </div>
      </div>

      <!-- Bell notification (always visible) -->
      <div class="sidebar-bell-wrap">
        <el-popover
          placement="right-start"
          :width="320"
          trigger="click"
          v-model:visible="bellVisible"
          @show="onBellShow"
          popper-class="admin-bell-popover"
        >
          <template #reference>
            <div class="bell-menu-item" :class="{ collapsed: isCollapse }">
              <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99" class="bell-badge">
                <el-icon :size="isCollapse ? 20 : 18"><Bell /></el-icon>
              </el-badge>
              <span v-if="!isCollapse" class="bell-menu-text">公告通知</span>
            </div>
          </template>

          <div class="popover-content">
            <div class="popover-header">
              <span class="popover-title">公告通知</span>
              <el-button text size="small" @click="markAllRead">标记已读</el-button>
            </div>

            <div v-if="recentList.length === 0" class="popover-empty">
              暂无公告
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
              <el-button text size="small" @click="openCreateDialog">
                <el-icon><Plus /></el-icon> 发布新公告
              </el-button>
              <router-link to="/admin/announcement" class="view-all" @click="bellVisible = false">
                查看全部 <el-icon><ArrowRight /></el-icon>
              </router-link>
            </div>
          </div>
        </el-popover>
      </div>

      <el-menu-item index="/admin/dashboard">
        <el-icon><Odometer /></el-icon>
        <template #title>控制台</template>
      </el-menu-item>
      <el-menu-item index="/admin/category">
        <el-icon><Menu /></el-icon>
        <template #title>分类管理</template>
      </el-menu-item>
      <el-menu-item index="/admin/tag">
        <el-icon><CollectionTag /></el-icon>
        <template #title>标签管理</template>
      </el-menu-item>
      <el-menu-item index="/admin/resource">
        <el-icon><Document /></el-icon>
        <template #title>资源管理</template>
      </el-menu-item>
      <el-menu-item index="/admin/download-log">
        <el-icon><List /></el-icon>
        <template #title>下载日志</template>
      </el-menu-item>
      <el-menu-item index="/admin/import">
        <el-icon><Upload /></el-icon>
        <template #title>批量导入</template>
      </el-menu-item>
      <el-menu-item index="/admin/keyword-rule">
        <el-icon><Tools /></el-icon>
        <template #title>关键词规则</template>
      </el-menu-item>

      <el-menu-item v-if="isSuperAdmin" index="/admin/admin-manage">
        <el-icon><User /></el-icon>
        <template #title>管理员管理</template>
      </el-menu-item>

      <div class="sidebar-divider"></div>

      <el-menu-item index="/admin/password">
        <el-icon><Lock /></el-icon>
        <template #title>修改密码</template>
      </el-menu-item>
      <el-menu-item v-if="isSuperAdmin" index="/admin/contact">
        <el-icon><Iphone /></el-icon>
        <template #title>联系方式</template>
      </el-menu-item>
      <el-menu-item index="/" target="_blank">
        <el-icon><View /></el-icon>
        <template #title>返回前台</template>
      </el-menu-item>
    </el-menu>

    <!-- Mobile sidebar overlay -->
    <div class="mobile-sidebar-overlay" v-if="!isCollapse && mobileSidebar" @click="isCollapse = true"></div>

    <!-- Main area -->
    <div class="admin-main" :class="{ collapsed: isCollapse }">
      <!-- Top bar -->
      <header class="admin-topbar">
        <div class="topbar-left">
          <el-button text @click="isCollapse = !isCollapse" class="collapse-btn">
            <el-icon><Fold v-if="!isCollapse" /><Expand v-else /></el-icon>
          </el-button>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/admin/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="$route.meta.title">{{ $route.meta.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="topbar-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="32" icon="UserFilled" />
              <span class="username">{{ adminInfo?.nickname || adminInfo?.username || '管理员' }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="password">
                  <el-icon><Lock /></el-icon>修改密码
                </el-dropdown-item>
                <el-dropdown-item v-if="isSuperAdmin" command="contact">
                  <el-icon><Iphone /></el-icon>联系方式
                </el-dropdown-item>
                <el-dropdown-item command="home">
                  <el-icon><View /></el-icon>返回前台
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <!-- Content -->
      <main class="admin-content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { getProfile } from '@/api/admin'
import { getUnreadCount, getRecentAnnouncements } from '@/api/announcement'

const router = useRouter()
const route = useRoute()

// ====== 管理员信息 ======
function initAdminInfo() {
  try {
    return JSON.parse(localStorage.getItem('adminInfo') || '{}')
  } catch {
    return {}
  }
}
const isCollapse = ref(false)
const adminInfo = ref(initAdminInfo())
const mobileSidebar = ref(false)

// 检测移动端宽度自动收起侧边栏
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

// ====== 公告通知铃铛 ======
const bellVisible = ref(false)
const unreadCount = ref(0)
const recentList = ref([])
const lastReadId = ref(0)

function loadLastReadId() {
  try {
    const val = localStorage.getItem('adminAnnouncementLastReadId')
    lastReadId.value = val ? parseInt(val, 10) : 0
  } catch {
    lastReadId.value = 0
  }
}

function saveLastReadId(id) {
  lastReadId.value = id
  try {
    localStorage.setItem('adminAnnouncementLastReadId', String(id))
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
  router.push('/admin/announcement')
}

function openCreateDialog() {
  bellVisible.value = false
  router.push('/admin/announcement')
}

// ====== 初始化 ======
onMounted(async () => {
  loadLastReadId()
  fetchUnreadCount()

  try {
    const res = await getProfile()
    adminInfo.value = res.data
    localStorage.setItem('adminInfo', JSON.stringify(res.data))
  } catch {
    // Token invalid, redirect handled by interceptor
  }
})

// 路由变化时重新获取未读数
onMounted(() => {
  // Use the route's path watcher indirectly
})
watch(() => route.path, () => {
  loadLastReadId()
  fetchUnreadCount()
})

// ====== 顶部栏操作 ======
function handleCommand(command) {
  if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      localStorage.removeItem('adminToken')
      localStorage.removeItem('adminInfo')
      router.push('/admin/login')
    }).catch(() => {})
  } else if (command === 'password') {
    router.push('/admin/password')
  } else if (command === 'contact') {
    router.push('/admin/contact')
  } else if (command === 'home') {
    window.open('/', '_blank')
  }
}
</script>

<style scoped>
.admin-layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

/* ========== Sidebar ========== */
.admin-sidebar {
  width: 220px;
  height: 100vh;
  border-right: none;
  overflow-y: auto;
  overflow-x: hidden;
  flex-shrink: 0;
  transition: width 0.3s;
}
.admin-sidebar:not(.el-menu--collapse) {
  width: 220px;
}
.admin-sidebar.el-menu--collapse {
  width: 64px;
}

.sidebar-header {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

.sidebar-logo-img {
  height: 48px;
  width: 48px;
  border-radius: 50%;
  object-fit: cover;
  transition: all 0.3s;
}
.sidebar-logo-img.collapsed {
  height: 40px;
  width: 40px;
}

/* ====== Bell notification menu item ====== */
.sidebar-bell-wrap {
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

.bell-menu-item {
  display: flex;
  align-items: center;
  height: 56px;
  padding: 0 20px;
  cursor: pointer;
  color: #a0a4b8;
  transition: var(--transition);
  gap: 14px;
}
.bell-menu-item:hover {
  color: #409eff;
  background: rgba(64, 158, 255, 0.08);
}
.bell-menu-item.collapsed {
  justify-content: center;
  padding: 0;
  gap: 0;
}
.bell-menu-text {
  font-size: 14px;
  white-space: nowrap;
  overflow: hidden;
}
.bell-badge :deep(.el-badge__content) {
  background: #e6a23c;
  border: none;
  font-size: 11px;
  height: 16px;
  line-height: 16px;
  padding: 0 5px;
}

/* ====== Popover content ====== */
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
  justify-content: space-between;
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

.sidebar-divider {
  height: 1px;
  background: rgba(255, 255, 255, 0.06);
  margin: 8px 12px;
}

/* ========== Main ========== */
.admin-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  transition: margin-left 0.3s;
}

/* ========== Topbar ========== */
.admin-topbar {
  height: 64px;
  background: white;
  border-bottom: 1px solid var(--border-light);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  flex-shrink: 0;
}

.topbar-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.collapse-btn {
  font-size: 18px;
}

.topbar-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 12px;
  border-radius: 8px;
  transition: var(--transition);
}
.user-info:hover {
  background: var(--bg);
}

/* Mobile overlay for sidebar */
.mobile-sidebar-overlay {
  display: none;
}

@media (max-width: 768px) {
  .admin-content {
    padding: 16px;
  }
  .admin-topbar {
    padding: 0 12px;
  }
  .mobile-sidebar-overlay {
    display: block;
    position: fixed;
    inset: 0;
    z-index: 99;
    background: rgba(0,0,0,0.4);
  }
}

.username {
  font-size: 14px;
  color: var(--text-primary);
}

/* ========== Content ========== */
.admin-content {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
  background: var(--bg);
}
</style>
