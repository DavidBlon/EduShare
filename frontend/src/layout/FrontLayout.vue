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
          <p>&copy; 2024-{{ currentYear }} 小初学习资料圈. All rights reserved.</p>
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
const currentYear = new Date().getFullYear()

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
.site-header{position:fixed;top:0;right:0;left:0;z-index:var(--z-sticky);height:var(--header-height);background:rgba(255,253,248,.92);border-bottom:1px solid rgba(202,194,178,.72);backdrop-filter:blur(14px);transition:transform .3s ease,box-shadow .3s ease}.site-header.header-hidden{transform:translateY(-100%)}.site-header.header-shadow{box-shadow:0 4px 18px rgba(44,52,45,.06)}.header-inner{display:flex;align-items:center;gap:40px;max-width:var(--content-max-width);height:100%;padding:0 28px;margin:0 auto}.logo{display:inline-flex;align-items:center;gap:10px;flex-shrink:0;color:inherit;text-decoration:none}.logo-img{width:38px;height:38px;object-fit:cover;border-radius:50%}.logo-wordmark{display:flex;flex-direction:column;gap:2px;line-height:1}.logo-wordmark b{color:var(--ink);font-size:15px;letter-spacing:.08em}.logo-wordmark small{color:#79908a;font:600 8px/1 'PingFang SC','Microsoft YaHei',sans-serif;letter-spacing:.18em}.main-nav{display:flex;flex:1;align-items:center;gap:25px}.nav-item{position:relative;padding:7px 0;color:#4c605f;font:14px/1.2 'PingFang SC','Microsoft YaHei',sans-serif;text-decoration:none}.nav-item::after{position:absolute;right:0;bottom:-1px;left:0;height:2px;content:'';background:var(--accent);transform:scaleX(0);transform-origin:left;transition:transform var(--transition-fast)}.nav-item:hover,.nav-item.active{color:var(--primary-dark)}.nav-item.active{font-weight:600}.nav-item.active::after{transform:scaleX(1)}.header-right{display:flex;align-items:center;gap:3px}.bell-btn,.admin-btn,.hamburger-btn{color:#61736f;border-radius:50%;transition:background var(--transition-fast),color var(--transition-fast)}.bell-btn:hover,.admin-btn:hover,.hamburger-btn:hover{color:var(--primary-dark);background:#e7f0eb}.bell-ring{animation:bellShake .8s ease-in-out 2}@keyframes bellShake{25%{transform:rotate(9deg)}75%{transform:rotate(-9deg)}}.hamburger-btn{display:none}
.popover-empty{padding:20px 0}.popover-list{max-height:340px;margin:0 -12px;overflow-y:auto}.popover-item{padding:11px 12px;border-radius:5px;cursor:pointer}.popover-item:hover,.popover-item.unread{background:#eef4ef}.item-title{display:flex;gap:8px;align-items:center}.unread-dot{width:6px;height:6px;flex-shrink:0;border-radius:50%;background:var(--accent)}.item-text{overflow:hidden;color:var(--ink);font:13px 'PingFang SC','Microsoft YaHei',sans-serif;text-overflow:ellipsis;white-space:nowrap}.item-time{padding-left:14px;color:var(--text-placeholder);font:11px 'PingFang SC','Microsoft YaHei',sans-serif}.view-all{font:12px 'PingFang SC','Microsoft YaHei',sans-serif}
.mobile-overlay{position:fixed;inset:0;z-index:var(--z-overlay);background:rgba(29,47,45,.38);backdrop-filter:blur(3px)}.mobile-drawer{position:fixed;top:0;bottom:0;left:0;z-index:calc(var(--z-overlay) + 1);display:flex;flex-direction:column;width:min(82vw,310px);background:var(--paper);box-shadow:var(--shadow-xl)}.drawer-header{display:flex;align-items:center;gap:12px;padding:23px 20px;border-bottom:1px solid var(--border-light)}.drawer-logo{width:40px;height:40px;border-radius:50%}.drawer-title{color:var(--ink);font-size:18px;font-weight:700}.drawer-nav{flex:1;padding:12px 0}.drawer-item{display:flex;align-items:center;gap:12px;padding:14px 20px;color:var(--text-regular);font:14px 'PingFang SC','Microsoft YaHei',sans-serif;text-decoration:none}.drawer-item.active{color:var(--primary-dark);background:#e8f1ec}.drawer-footer{padding:16px 20px;border-top:1px solid var(--border-light)}.drawer-admin-btn{width:100%;justify-content:flex-start;color:var(--text-secondary)}.site-main{min-height:calc(100vh - 200px);padding-top:var(--header-height)}
.site-footer{position:relative;overflow:hidden;padding:58px 0 22px;color:#aabbb4;background:#183c3c}.site-footer::after{position:absolute;right:-90px;bottom:-170px;width:380px;height:380px;content:'';border:1px solid rgba(226,196,145,.2);border-radius:50%}.footer-inner{position:relative;z-index:1;max-width:var(--content-max-width);padding:0 28px;margin:0 auto}.footer-grid{display:grid;grid-template-columns:1.55fr .85fr 1.15fr;gap:52px;padding-bottom:36px}.footer-logo{display:flex;align-items:center;gap:11px;margin-bottom:14px}.footer-logo-img{width:38px;height:38px;border-radius:50%}.footer-logo-text{color:#fffcf5;font-size:18px;font-weight:700;letter-spacing:.06em}.footer-desc{max-width:350px;margin:0;color:#9eaea8;font:13px/1.9 'PingFang SC','Microsoft YaHei',sans-serif}.footer-links h4,.footer-contact h4{margin:0 0 15px;color:#f1dfbd;font:600 11px/1.2 'PingFang SC','Microsoft YaHei',sans-serif;letter-spacing:.12em}.footer-links a{display:block;padding:4px 0;color:#abbab4;font:13px/1.5 'PingFang SC','Microsoft YaHei',sans-serif;text-decoration:none}.footer-links a:hover{color:#fff8e9}.footer-contact p{display:flex;gap:8px;align-items:center;margin:0 0 9px;color:#abbab4;font:13px 'PingFang SC','Microsoft YaHei',sans-serif}.footer-bottom{display:flex;justify-content:space-between;gap:20px;padding-top:19px;color:#819690;border-top:1px solid rgba(255,255,255,.13);font:11px/1.6 'PingFang SC','Microsoft YaHei',sans-serif}.footer-bottom p{margin:0}.footer-disclaimer{max-width:600px;text-align:right}
.fade-enter-active,.fade-leave-active{transition:opacity .25s ease}.fade-enter-from,.fade-leave-to{opacity:0}.slide-left-enter-active,.slide-left-leave-active{transition:transform .26s ease}.slide-left-enter-from,.slide-left-leave-to{transform:translateX(-100%)}@media(max-width:768px){.header-inner{gap:12px;padding:0 18px}.logo-wordmark{display:none}.main-nav,.admin-btn{display:none}.hamburger-btn{display:flex}.footer-inner{padding:0 18px}.footer-grid{grid-template-columns:1fr;gap:30px}.footer-bottom{flex-direction:column;text-align:center}.footer-disclaimer{text-align:center}}
</style>
