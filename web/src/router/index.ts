import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import UserHome from '../views/user/Home.vue'
import AdminHome from '../views/admin/Home.vue'
import { getCurrentUser } from '@/api/sysUser'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: Login,
      meta: { requiresAuth: false }
    },
    {
      path: '/user/home',
      name: 'UserHome',
      component: UserHome,
      meta: { requiresAuth: true, role: 'USER' }
    },
    {
      path: '/admin/home',
      name: 'AdminHome',
      component: AdminHome,
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
