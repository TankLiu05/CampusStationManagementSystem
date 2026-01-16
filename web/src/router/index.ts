import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import { getCurrentUser } from '@/api/sysUser'

// 用户端页面
import UserHome from '../views/user/Home.vue'
import MyPackages from '../views/user/MyPackages.vue'
import PickupCode from '../views/user/PickupCode.vue'
import UserAnnouncements from '../views/user/Announcements.vue'
import History from '../views/user/History.vue'
import Profile from '../views/user/Profile.vue'

// 管理员端页面
import AdminHome from '../views/admin/Home.vue'
import Users from '../views/admin/Users.vue'
import Packages from '../views/admin/Packages.vue'
import AdminAnnouncements from '../views/admin/Announcements.vue'
import Statistics from '../views/admin/Statistics.vue'

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
      path: '/user/pickup-code',
      name: 'PickupCode',
      component: PickupCode,
      meta: { requiresAuth: true, role: 'USER' }
    },
    {
      path: '/user/announcements',
      name: 'UserAnnouncements',
      component: UserAnnouncements,
      meta: { requiresAuth: true, role: 'USER' }
    },
    {
      path: '/user/history',
      name: 'History',
      component: History,
      meta: { requiresAuth: true, role: 'USER' }
    },
    {
      path: '/user/profile',
      name: 'Profile',
      component: Profile,
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
      path: '/admin/statistics',
      name: 'Statistics',
      component: Statistics,
      meta: { requiresAuth: true, role: 'ADMIN' }
    },
    {
      path: '/',
      redirect: '/login'
    }
  ],
})

// 路由守卫：权限控制
router.beforeEach(async (to, from, next) => {
  // 不需要认证的页面直接放行
  if (!to.meta.requiresAuth) {
    next()
    return
  }

  try {
    // 获取当前用户信息
    const user = await getCurrentUser()
    
    // 检查角色权限
    if (to.meta.role && user.role !== to.meta.role) {
      // 角色不匹配，根据角色重定向到对应首页
      if (user.role === 'ADMIN') {
        next('/admin/home')
      } else {
        next('/user/home')
      }
      return
    }
    
    next()
  } catch (err) {
    // 未登录或认证失败，跳转到登录页
    console.error('认证失败:', err)
    next('/login')
  }
})

export default router
