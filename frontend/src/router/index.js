import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  // ========== 前台页面 ==========
  {
    path: '/',
    component: () => import('@/layout/FrontLayout.vue'),
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/front/Home.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'resources',
        name: 'ResourceList',
        component: () => import('@/views/front/ResourceList.vue'),
        meta: { title: '资源列表' }
      },
      {
        path: 'resource/:id',
        name: 'ResourceDetail',
        component: () => import('@/views/front/ResourceDetail.vue'),
        meta: { title: '资源详情' }
      },
      {
        path: 'qq-group',
        name: 'QqGroup',
        component: () => import('@/views/front/QqGroup.vue'),
        meta: { title: '资料群' }
      },
      {
        path: 'about',
        name: 'About',
        component: () => import('@/views/front/About.vue'),
        meta: { title: '关于我们' }
      },
      {
        path: 'announcements',
        name: 'AnnouncementList',
        component: () => import('@/views/front/AnnouncementList.vue'),
        meta: { title: '平台公告' }
      },
      {
        path: 'announcement/:id',
        name: 'AnnouncementDetail',
        component: () => import('@/views/front/AnnouncementDetail.vue'),
        meta: { title: '公告详情' }
      }
    ]
  },

  // ========== 管理后台 ==========
  {
    path: '/admin/login',
    name: 'AdminLogin',
    component: () => import('@/views/admin/Login.vue'),
    meta: { title: '管理员登录' }
  },
  {
    path: '/admin',
    component: () => import('@/layout/AdminLayout.vue'),
    redirect: '/admin/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/admin/Dashboard.vue'),
        meta: { title: '控制台', icon: 'Odometer' }
      },
      {
        path: 'category',
        name: 'CategoryManage',
        component: () => import('@/views/admin/CategoryManage.vue'),
        meta: { title: '分类管理', icon: 'Menu' }
      },
      {
        path: 'tag',
        name: 'TagManage',
        component: () => import('@/views/admin/TagManage.vue'),
        meta: { title: '标签管理', icon: 'CollectionTag' }
      },
      {
        path: 'resource',
        name: 'ResourceManage',
        component: () => import('@/views/admin/ResourceManage.vue'),
        meta: { title: '资源管理', icon: 'Document' }
      },
      {
        path: 'download-log',
        name: 'DownloadLog',
        component: () => import('@/views/admin/DownloadLog.vue'),
        meta: { title: '下载日志', icon: 'List' }
      },
      {
        path: 'admin-manage',
        name: 'AdminManage',
        component: () => import('@/views/admin/AdminManage.vue'),
        meta: { title: '管理员管理', icon: 'User', roles: [0] }
      },
      {
        path: 'import',
        name: 'ResourceImport',
        component: () => import('@/views/admin/ResourceImport.vue'),
        meta: { title: '批量导入', icon: 'Upload' }
      },
      {
        path: 'keyword-rule',
        name: 'KeywordRule',
        component: () => import('@/views/admin/KeywordRule.vue'),
        meta: { title: '关键词规则', icon: 'Tools' }
      },
      {
        path: 'announcement',
        name: 'AnnouncementManage',
        component: () => import('@/views/admin/AnnouncementManage.vue'),
        meta: { title: '公告管理', icon: 'Bell' }
      },
      {
        path: 'password',
        name: 'Password',
        component: () => import('@/views/admin/Password.vue'),
        meta: { title: '修改密码', icon: 'Lock' }
      },
      {
        path: 'qrcode',
        name: 'QrCodeManage',
        component: () => import('@/views/admin/QrCodeManage.vue'),
        meta: { title: '资料群二维码', icon: 'Connection', roles: [0] }
      },
      {
        path: 'contact',
        name: 'Contact',
        component: () => import('@/views/admin/Contact.vue'),
        meta: { title: '联系方式', icon: 'Iphone', roles: [0] }
      },
      {
        path: 'disclaimer',
        name: 'DisclaimerManage',
        component: () => import('@/views/admin/DisclaimerManage.vue'),
        meta: { title: '免责声明', icon: 'WarningFilled', roles: [0] }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    // keep-alive 缓存的页面（如 ResourceList）由组件内部的 onActivated 恢复滚动
    if (savedPosition) {
      return savedPosition
    }
    if (to.hash) {
      return {
        el: to.hash,
        behavior: 'smooth'
      }
    }
    return { top: 0 }
  }
})

// 路由守卫 - 管理后台需要登录 + 角色校验
router.beforeEach((to, from, next) => {
  document.title = (to.meta.title ? to.meta.title + ' - ' : '') + '小初学习资料圈'

  if (to.path.startsWith('/admin') && to.path !== '/admin/login') {
    const token = localStorage.getItem('adminToken')
    if (!token) {
      next('/admin/login')
      return
    }

    // 角色校验：如果路由指定了允许的角色
    if (to.meta.roles) {
      try {
        const adminInfo = JSON.parse(localStorage.getItem('adminInfo') || '{}')
        const role = adminInfo.role
        if (!to.meta.roles.includes(role)) {
          next('/admin/dashboard')
          return
        }
      } catch {
        next('/admin/dashboard')
        return
      }
    }
  }

  next()
})

export default router
