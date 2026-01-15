import { createRouter, createWebHistory } from 'vue-router'
import Admin_Login from '../views/Admin_Login.vue'
import User_Login from '../views/User_Login.vue'


const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    // 在这里添加你的路由配置
   
    {
      path: '/AdminLogin',
      name: 'AdminLogin',
      component: Admin_Login
    },
    {
      path: '/UserLogin',
      name: 'UserLogin',
      component: User_Login
    }
  ],
})

export default router
