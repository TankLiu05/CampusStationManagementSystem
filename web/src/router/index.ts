import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import { getCurrentUser } from '@/api/sysUser'
import { request } from '@/utls/request'

// 用户端页面
import UserHome from '../views/user/Home.vue'
import MyPackages from '../views/user/MyPackages.vue'
import UserAnnouncements from '../views/user/Announcements.vue'
import Profile from '../views/user/Profile.vue'
import UserMessages from '../views/user/Messages.vue'

// 管理员端页面
import AdminHome from '../views/admin/Home.vue'
import Users from '../views/admin/Users.vue'
import Admins from '../views/admin/Admins.vue'
import Packages from '../views/admin/Packages.vue'
import AdminAnnouncements from '../views/admin/Announcements.vue'
import Settings from '../views/admin/Settings.vue'
import Warehouse from '../views/admin/Warehouse.vue'
import WarehouseInfo from '../views/admin/WarehouseInfo.vue'
import Returns from '../views/admin/Returns.vue'
import Logistics from '../views/admin/Logistics.vue'
import Messages from '../views/admin/Messages.vue'

// 缓存用户角色，避免重复请求
let cachedRole: 'ADMIN' | 'USER' | null = null

// 检查用户是否是管理员
async function checkIsAdmin(): Promise<boolean> {
  try {
    const res = await request('/api/admin/profile', { method: 'GET', skipErrorHandler: true })
    // 如果返回了用户信息（有 id 字段），说明是管理员
    if (res && typeof res === 'object' && 'id' in res) {
      return true
    }
    // 如果是 ApiResponse 格式，检查 success
    if (res && typeof res === 'object' && 'success' in res) {
      return (res as any).success === true
    }
    return false
  } catch {
    return false
  }
}

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: Login,
      meta: { requiresAuth: false }
    },
    // 用户端路由
    {
      path: '/user/home',
      name: 'UserHome',
      component: UserHome,
      meta: { requiresAuth: true, role: 'USER' }
    },
    {
      path: '/user/packages',
      name: 'MyPackages',
      component: MyPackages,
      meta: { requiresAuth: true, role: 'USER' }
    },
    {
      path: '/user/announcements',
      name: 'UserAnnouncements',
      component: UserAnnouncements,
      meta: { requiresAuth: true, role: 'USER' }
    },
    {
      path: '/user/profile',
      name: 'Profile',
      component: Profile,
      meta: { requiresAuth: true, role: 'USER' }
    },
    {
      path: '/user/messages',
      name: 'UserMessages',
      component: UserMessages,
      meta: { requiresAuth: true, role: 'USER' }
    },
    // 管理员端路由
    {
      path: '/admin/home',
      name: 'AdminHome',
      component: AdminHome,
      meta: { requiresAuth: true, role: 'ADMIN' }
    },
    {
      path: '/admin/users',
      name: 'Users',
      component: Users,
      meta: { requiresAuth: true, role: 'ADMIN' }
    },
    {
      path: '/admin/admins',
      name: 'Admins',
      component: Admins,
      meta: { requiresAuth: true, role: 'ADMIN' }
    },
    {
      path: '/admin/packages',
      name: 'Packages',
      component: Packages,
      meta: { requiresAuth: true, role: 'ADMIN' }
    },
    {
      path: '/admin/announcements',
      name: 'AdminAnnouncements',
      component: AdminAnnouncements,
      meta: { requiresAuth: true, role: 'ADMIN' }
    },
    {
      path: '/admin/settings',
      name: 'Settings',
      component: Settings,
      meta: { requiresAuth: true, role: 'ADMIN' }
    },
    {
      path: '/admin/warehouse',
      name: 'Warehouse',
      component: Warehouse,
      meta: { requiresAuth: true, role: 'ADMIN' }
    },
    {
      path: '/admin/warehouse-info',
      name: 'WarehouseInfo',
      component: WarehouseInfo,
      meta: { requiresAuth: true, role: 'ADMIN' }
    },
    {
      path: '/admin/returns',
      name: 'Returns',
      component: Returns,
      meta: { requiresAuth: true, role: 'ADMIN' }
    },
    {
      path: '/admin/logistics',
      name: 'Logistics',
      component: Logistics,
      meta: { requiresAuth: true, role: 'ADMIN' }
    },
    {
      path: '/admin/messages',
      name: 'Messages',
      component: Messages,
      meta: { requiresAuth: true, role: 'ADMIN' }
    },
    {
      path: '/',
      redirect: '/login'
    }
  ],
})

router.beforeEach(async (to, from, next) => {
  if (!to.meta.requiresAuth) {
    next()
    return
  }

  try {
    if (to.meta.role === 'ADMIN') {
      const isAdmin = await checkIsAdmin()
      if (!isAdmin) {
        cachedRole = null
        next('/login')
        return
      }
      cachedRole = 'ADMIN'
      next()
      return
    }

    await getCurrentUser()
    cachedRole = 'USER'
    next()
  } catch (err) {
    cachedRole = null
    console.error('认证失败:', err)
    next('/login')
  }
})

// 导出清除角色缓存的方法，登出时使用
export function clearRoleCache() {
  cachedRole = null
}

export default router
