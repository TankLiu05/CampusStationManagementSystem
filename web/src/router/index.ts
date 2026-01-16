import { createRouter, createWebHistory } from 'vue-router'
import AdminLogin from '../views/adminLogin.vue'
import UserLogin from '../views/userLogin.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/adminLogin',
      name: 'AdminLogin',
      component: AdminLogin
    },
    {
      path: '/userLogin',
      name: 'UserLogin',
      component: UserLogin
    },
    {
      path: '/',
      redirect: '/userLogin'
    }
  ],
})

export default router
