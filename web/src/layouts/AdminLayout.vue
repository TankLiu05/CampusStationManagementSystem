<template>
  <div class="admin-layout">
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
import Navbar from '@/components/admin/Navbar.vue'
import Sidebar from '@/components/admin/Sidebar.vue'

const router = useRouter()
const route = useRoute()
const currentUser = ref<SysUser | null>(null)

// 菜单ID到名称的映射
const menuNameMap: Record<string, string> = {
  '/admin/home': '工作台',
  '/admin/users': '用户管理',
  '/admin/packages': '包裹管理',
  '/admin/announcements': '公告管理',
  '/admin/statistics': '数据统计',
  '/admin/reports': '报表中心'
}

// 路由到菜单ID的映射
const routeToMenuId: Record<string, string> = {
  '/admin/home': 'dashboard',
  '/admin/users': 'users',
  '/admin/packages': 'packages',
  '/admin/announcements': 'announcements',
  '/admin/statistics': 'statistics',
  '/admin/reports': 'reports'
}

const currentPageName = computed(() => {
  return menuNameMap[route.path] || '工作台'
})

const activeMenu = computed(() => {
  return routeToMenuId[route.path] || 'dashboard'
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
.admin-layout {
  min-height: 100vh;
  background: #f0f2f5;
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
</style>
