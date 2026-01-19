<template>
  <div class="user-layout">
    <!-- 侧边导航栏 -->
    <Sidebar :default-active="activeMenu" />

    <!-- 右侧主体区域 -->
    <div class="right-container">
      <!-- 顶部导航栏 -->
      <Navbar 
        :username="currentUser?.username" 
        :current-page-name="currentPageName" 
        @logout="handleLogout" 
      />

      <!-- 主要内容区域 -->
      <main class="main-content">
        <slot></slot>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getCurrentUser, logout } from '@/api/sysUser'
import type { SysUser } from '@/api/sysUser'
import Navbar from '@/components/user/Navbar.vue'
import Sidebar from '@/components/user/Sidebar.vue'

const router = useRouter()
const route = useRoute()
const currentUser = ref<SysUser | null>(null)

// 菜单ID到名称的映射
const menuNameMap: Record<string, string> = {
  '/user/home': '首页',
  '/user/packages': '我的包裹',
  '/user/pickup-code': '取件码',
  '/user/announcements': '公告信息',
  '/user/history': '历史记录',
  '/user/feedback': '意见反馈',
  '/user/profile': '个人中心'
}

// 路由到菜单ID的映射
const routeToMenuId: Record<string, string> = {
  '/user/home': 'home',
  '/user/packages': 'myPackages',
  '/user/pickup-code': 'pickupCode',
  '/user/announcements': 'announcements',
  '/user/history': 'history',
  '/user/feedback': 'feedback',
  '/user/profile': 'profile'
}

const currentPageName = computed(() => {
  return menuNameMap[route.path] || '首页'
})

const activeMenu = computed(() => {
  return routeToMenuId[route.path] || 'home'
})

onMounted(async () => {
  try {
    currentUser.value = await getCurrentUser()
    // 权限检查已由路由守卫处理，这里不再重复检查
  } catch (err) {
    console.error('获取用户信息失败:', err)
    router.replace('/login')
  }
})

const handleLogout = async () => {
  try {
    await logout()
    router.replace('/login')
  } catch (err) {
    console.error('退出登录失败:', err)
  }
}
</script>

<style scoped>
.user-layout {
  min-height: 100vh;
  background: #f5f7fa;
  display: flex;
}

.right-container {
  flex: 1;
  margin-left: 260px;
  display: flex;
  flex-direction: column;
}

.main-content {
  flex: 1;
  padding: 40px;
}

/* 响应式设计 */
@media (max-width: 992px) {
  .right-container {
    margin-left: 220px;
  }
  
  .main-content {
    padding: 30px;
  }
}

@media (max-width: 768px) {
  .user-layout {
    flex-direction: column;
  }
  
  .right-container {
    margin-left: 0;
  }
  
  .main-content {
    padding: 20px;
  }
}

@media (max-width: 480px) {
  .main-content {
    padding: 15px;
  }
}
</style>
