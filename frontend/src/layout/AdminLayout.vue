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
          <span v-if="!isCollapse" class="sidebar-brand">小初学习资料圈</span>
        </div>
      </div>

      <!-- Badge notification -->
      <div class="sidebar-bell-wrap">
        <el-popover
          placement="right-start"
          :width="340"
          trigger="click"
          v-model:visible="bellVisible"
          @show="onBellShow"
          popper-class="admin-bell-popover"
          transition="scale-in"
        >
          <template #reference>
            <div class="bell-menu-item" :class="{ collapsed: isCollapse }">
              <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99" class="bell-badge">
                <el-icon :size="isCollapse ? 20 : 18"><Bell /></el-icon>
              </el-badge>
              <span v-if="!isCollapse" class="bell-menu-text">公告通知</span>
              <span v-if="unreadCount > 0 && !isCollapse" class="bell-count">{{ unreadCount > 99 ? '99+' : unreadCount }}</span>
            </div>
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

      <!-- Menu sections -->
      <div class="menu-section-label" v-if="!isCollapse">内容管理</div>
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
      <el-menu-item index="/admin/announcement">
        <el-icon><Bell /></el-icon>
        <template #title>公告管理</template>
      </el-menu-item>

      <div class="sidebar-divider" v-if="!isCollapse"></div>
      <div class="menu-section-label" v-if="!isCollapse && (isSuperAdmin || true)">系统设置</div>

      <el-menu-item index="/admin/password">
        <el-icon><Lock /></el-icon>
        <template #title>修改密码</template>
      </el-menu-item>
      <el-menu-item v-if="isSuperAdmin" index="/admin/admin-manage">
        <el-icon><User /></el-icon>
        <template #title>管理员管理</template>
      </el-menu-item>
      <el-menu-item v-if="isSuperAdmin" index="/admin/qrcode">
        <el-icon><Connection /></el-icon>
        <template #title>资料群二维码</template>
      </el-menu-item>
      <el-menu-item v-if="isSuperAdmin" index="/admin/contact">
        <el-icon><Iphone /></el-icon>
        <template #title>联系方式</template>
      </el-menu-item>
      <el-menu-item v-if="isSuperAdmin" index="/admin/disclaimer">
        <el-icon><WarningFilled /></el-icon>
        <template #title>免责声明</template>
      </el-menu-item>
      <el-menu-item index="/" onclick="window.open('/','_blank')">
        <el-icon><View /></el-icon>
        <template #title>返回前台</template>
      </el-menu-item>
    </el-menu>

    <!-- Mobile overlay -->
    <div class="mobile-sidebar-overlay" v-if="!isCollapse && mobileSidebar" @click="isCollapse = true"></div>

    <!-- Main Area -->
    <div class="admin-main" :class="{ collapsed: isCollapse }">
      <!-- Topbar -->
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
          <el-dropdown @command="handleCommand" trigger="click">
            <span class="user-info">
              <el-avatar :size="34" icon="UserFilled" class="user-avatar" />
              <span class="username">{{ adminInfo?.nickname || adminInfo?.username || '管理员' }}</span>
              <el-icon class="dropdown-arrow"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="password">
                  <el-icon><Lock /></el-icon> 修改密码
                </el-dropdown-item>
                <el-dropdown-item v-if="isSuperAdmin" command="contact">
                  <el-icon><Iphone /></el-icon> 联系方式
                </el-dropdown-item>
                <el-dropdown-item v-if="isSuperAdmin" command="disclaimer">
                  <el-icon><WarningFilled /></el-icon> 免责声明
                </el-dropdown-item>
                <el-dropdown-item command="home">
                  <el-icon><View /></el-icon> 返回前台
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon> 退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <!-- Content -->
      <main class="admin-content">
        <router-view v-slot="{ Component }">
          <transition name="slide-fade" mode="out-in">
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

function initAdminInfo() {
  try { return JSON.parse(localStorage.getItem('adminInfo') || '{}') }
  catch { return {} }
}

const isCollapse = ref(false)
const adminInfo = ref(initAdminInfo())
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

// ====== Notification bell ======
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
  try { localStorage.setItem('adminAnnouncementLastReadId', String(id)) } catch { /* noop */ }
}

async function fetchUnreadCount() {
  try { const res = await getUnreadCount(lastReadId.value); unreadCount.value = res.data?.count || 0 }
  catch { /* noop */ }
}
async function fetchRecent() {
  try { const res = await getRecentAnnouncements(); recentList.value = res.data || [] }
  catch { /* noop */ }
}
function onBellShow() { fetchRecent() }
function markAllRead() {
  const maxId = recentList.value.reduce((max, item) => Math.max(max, item.id), 0)
  if (maxId > 0) saveLastReadId(maxId)
  unreadCount.value = 0; bellVisible.value = false
}
function goAnnouncement(id) { bellVisible.value = false; router.push('/admin/announcement') }
function openCreateDialog() { bellVisible.value = false; router.push('/admin/announcement') }

onMounted(async () => {
  loadLastReadId(); fetchUnreadCount()
  try {
    const res = await getProfile()
    adminInfo.value = res.data
    localStorage.setItem('adminInfo', JSON.stringify(res.data))
  } catch { /* Token invalid — interceptor handles */ }
})

watch(() => route.path, () => { loadLastReadId(); fetchUnreadCount() })

function handleCommand(command) {
  if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
    }).then(() => {
      localStorage.removeItem('adminToken'); localStorage.removeItem('adminInfo')
      router.push('/admin/login')
    }).catch(() => {})
  } else if (command === 'password') router.push('/admin/password')
  else if (command === 'contact') router.push('/admin/contact')
  else if (command === 'disclaimer') router.push('/admin/disclaimer')
  else if (command === 'home') window.open('/', '_blank')
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
  height: 100vh;
  border-right: none;
  overflow-y: auto;
  overflow-x: hidden;
  flex-shrink: 0;
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: 100;
  display: flex;
  flex-direction: column;
}
.admin-sidebar:not(.el-menu--collapse) { width: var(--sidebar-width); }
.admin-sidebar.el-menu--collapse { width: var(--sidebar-collapsed); }

.sidebar-header {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
  flex-shrink: 0;
}

.sidebar-logo {
  display: flex;
  align-items: center;
  gap: 10px;
}

.sidebar-logo-img {
  height: 40px;
  width: 40px;
  border-radius: 50%;
  object-fit: cover;
  transition: all 0.3s;
  flex-shrink: 0;
}
.sidebar-logo-img.collapsed {
  height: 36px;
  width: 36px;
}

.sidebar-brand {
  font-size: 15px;
  font-weight: 700;
  color: white;
  white-space: nowrap;
  overflow: hidden;
}

/* Menu section labels */
.menu-section-label {
  padding: 12px 20px 4px;
  font-size: 11px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.3);
  text-transform: uppercase;
  letter-spacing: 1px;
}

/* ====== Bell ====== */
.sidebar-bell-wrap {
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
  flex-shrink: 0;
}

.bell-menu-item {
  display: flex;
  align-items: center;
  height: 50px;
  padding: 0 20px;
  cursor: pointer;
  color: #a0a4b8;
  transition: var(--transition);
  gap: 14px;
  position: relative;
}
.bell-menu-item:hover {
  color: #409eff;
  background: rgba(64, 158, 255, 0.08);
}
.bell-menu-item.collapsed {
  justify-content: center;
  padding: 0;
}
.bell-menu-text {
  font-size: 14px;
  white-space: nowrap;
  overflow: hidden;
}
.bell-count {
  margin-left: auto;
  font-size: 11px;
  background: rgba(230, 162, 60, 0.2);
  color: #e6a23c;
  padding: 0 6px;
  border-radius: 8px;
  line-height: 16px;
  min-width: 18px;
  text-align: center;
}
.bell-badge :deep(.el-badge__content) {
  background: linear-gradient(135deg, #e6a23c, #d48806);
  border: 2px solid #1a1a2e;
  font-size: 11px;
  height: 18px;
  line-height: 14px;
  padding: 0 5px;
}

/* ====== Popover ====== */
.popover-content { font-size: 14px; }
.popover-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--border-light);
}
.popover-title { font-weight: 600; font-size: 15px; color: var(--text-primary); }
.popover-empty { padding: 8px 0; }
.popover-list { max-height: 340px; overflow-y: auto; margin: 0 -4px; }
.popover-item {
  padding: 10px 8px;
  border-bottom: 1px solid var(--border-light);
  cursor: pointer;
  transition: var(--transition);
  border-radius: 6px;
}
.popover-item:hover { background: var(--bg); }
.popover-item:last-child { border-bottom: none; }
.popover-item.unread { background: rgba(64, 158, 255, 0.04); }
.item-title { display: flex; align-items: center; gap: 6px; margin-bottom: 4px; }
.unread-dot { width: 8px; height: 8px; border-radius: 50%; background: var(--primary); flex-shrink: 0; }
.item-text { font-size: 14px; color: var(--text-primary); font-weight: 500; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.item-time { font-size: 12px; color: var(--text-placeholder); padding-left: 14px; }
.popover-footer {
  border-top: 1px solid var(--border-light);
  padding-top: 12px;
  margin-top: 4px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.view-all { font-size: 13px; color: var(--primary); display: inline-flex; align-items: center; gap: 4px; text-decoration: none; }

.sidebar-divider {
  height: 1px;
  background: rgba(255, 255, 255, 0.06);
  margin: 8px 20px;
}

/* ========== Main ========== */
.admin-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-width: 0;
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
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.02);
}

.topbar-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.collapse-btn {
  font-size: 18px;
  padding: 8px;
  border-radius: 8px;
  transition: var(--transition);
}
.collapse-btn:hover {
  background: var(--bg);
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
  padding: 6px 14px;
  border-radius: 10px;
  transition: var(--transition);
}
.user-info:hover {
  background: var(--bg);
}

.user-avatar {
  flex-shrink: 0;
}

.username {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

.dropdown-arrow {
  font-size: 12px;
  color: var(--text-secondary);
}

/* ========== Content ========== */
.admin-content {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
  background: var(--bg);
}

/* Mobile overlay */
.mobile-sidebar-overlay {
  display: none;
}

@media (max-width: 768px) {
  .admin-content { padding: 16px; }
  .admin-topbar { padding: 0 12px; }
  .mobile-sidebar-overlay {
    display: block;
    position: fixed;
    inset: 0;
    z-index: 99;
    background: rgba(0, 0, 0, 0.4);
    backdrop-filter: blur(4px);
  }
  .username { display: none; }
}
</style>
