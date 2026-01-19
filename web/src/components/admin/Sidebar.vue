<template>
  <aside class="sidebar">
    <div class="sidebar-header">
      <div class="logo">
        <img src="@/assets/icons/11.png" alt="管理后台" class="logo-icon" />
        <h3>管理后台</h3>
      </div>
    </div>

    <nav class="sidebar-nav">
      <div
        v-for="item in menuItems"
        :key="item.id"
        class="nav-item"
        :class="{ active: activeItem === item.id }"
        @click="handleNavClick(item)"
      >
        <img v-if="getIconPath(item.icon)" :src="getIconPath(item.icon)" :alt="item.label" class="nav-icon" />
        <span v-else class="nav-icon">{{ item.icon }}</span>
        <span class="nav-label">{{ item.label }}</span>
      </div>
    </nav>
  </aside>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'

interface MenuItem {
  id: string
  label: string
  icon: string
  route: string
}

interface Props {
  defaultActive?: string
}

const props = withDefaults(defineProps<Props>(), {
  defaultActive: 'dashboard'
})

const router = useRouter()
const route = useRoute()

const activeItem = ref(props.defaultActive)

const menuItems: MenuItem[] = [
  { id: 'dashboard', label: '工作台', icon: 'dashboard', route: '/admin/home' },
  { id: 'users', label: '用户管理', icon: 'users', route: '/admin/users' },
  { id: 'admins', label: '管理员管理', icon: 'admins', route: '/admin/admins' },
  { id: 'packages', label: '包裹管理', icon: 'packages', route: '/admin/packages' },
  { id: 'warehouse', label: '仓库管理', icon: 'warehouse', route: '/admin/warehouse' },
  { id: 'warehouse-info', label: '仓库信息', icon: 'warehouse-info', route: '/admin/warehouse-info' },
  { id: 'returns', label: '退货申请', icon: 'returns', route: '/admin/returns' },
  { id: 'logistics', label: '物流管理', icon: 'logistics', route: '/admin/logistics' },
  { id: 'messages', label: '留言管理', icon: 'messages', route: '/admin/messages' },
  { id: 'announcements', label: '公告管理', icon: 'announcements', route: '/admin/announcements' },
  { id: 'settings', label: '个人设置', icon: 'settings', route: '/admin/settings' },
]

// 监听路由变化，自动更新激活状态
watch(
  () => route.path,
  (newPath) => {
    const item = menuItems.find(item => item.route === newPath)
    if (item) {
      activeItem.value = item.id
    }
  },
  { immediate: true }
)

const handleNavClick = (item: MenuItem) => {
  activeItem.value = item.id
  router.push(item.route)
}

const getIconPath = (icon: string): string | undefined => {
  const iconMap: Record<string, string> = {
    dashboard: '/src/assets/icons/6.png',
    users: '/src/assets/icons/1.png',
    admins: '/src/assets/icons/23.png',
    packages: '/src/assets/icons/2.png',
    warehouse: '/src/assets/icons/7.png',
    'warehouse-info': '/src/assets/icons/3.png',
    returns: '/src/assets/icons/9.png',
    logistics: '/src/assets/icons/8.png',
    messages: '/src/assets/icons/10.png',
    announcements: '/src/assets/icons/4.png',
    settings: '/src/assets/icons/5.png',
  }
  return iconMap[icon]
}
</script>

<style scoped>
.sidebar {
  width: 260px;
  height: 100vh;
  background: white;
  box-shadow: 2px 0 12px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: fixed;
  left: 0;
  top: 0;
  z-index: 99;
}

.sidebar-header {
  padding: 0 15px;
  border-bottom: 1px solid #e0e0e0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 70px;
  min-height: 70px;
}

.logo h3 {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin: 0;
  white-space: nowrap;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
}

.logo-icon {
  width: 28px;
  height: 28px;
  object-fit: contain;
}

.sidebar-nav {
  flex: 1;
  padding: 20px 0;
  overflow-y: auto;
}

.sidebar-nav::-webkit-scrollbar {
  width: 6px;
}

.sidebar-nav::-webkit-scrollbar-thumb {
  background: #d0d0d0;
  border-radius: 3px;
}

.nav-item {
  display: flex;
  align-items: center;
  padding: 14px 20px;
  margin: 5px 10px;
  cursor: pointer;
  border-radius: 10px;
  transition: all 0.2s;
  color: #666;
  font-size: 15px;
}

.nav-item.active {
  background: #f0f0f0;
  color: #333;
  font-weight: 600;
}

.nav-icon {
  font-size: 22px;
  min-width: 30px;
  display: inline-flex;
  justify-content: center;
  align-items: center;
}

.nav-icon[src] {
  width: 22px;
  height: 22px;
  object-fit: contain;
}

.nav-label {
  margin-left: 12px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.sidebar-footer {
  padding: 15px 0;
  border-top: 1px solid #e0e0e0;
}
</style>
