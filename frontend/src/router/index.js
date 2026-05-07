import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/views/Layout.vue'),
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '仪表盘', icon: 'Odometer' }
      },
      {
        path: 'vendor',
        name: 'Vendor',
        component: () => import('@/views/Vendor.vue'),
        meta: { title: '厂商管理', icon: 'OfficeBuilding' }
      },
      {
        path: 'sync',
        name: 'Sync',
        component: () => import('@/views/Sync.vue'),
        meta: { title: '数据同步', icon: 'Refresh' }
      },
      {
        path: 'record',
        name: 'Record',
        component: () => import('@/views/Record.vue'),
        meta: { title: '操作记录', icon: 'Document' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/dashboard'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 数据源管理系统` : '数据源管理系统'

  const token = localStorage.getItem('token')

  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    next('/dashboard')
  } else {
    next()
  }
})

export default router
