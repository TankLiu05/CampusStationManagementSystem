<template>
  <aside class="sidebar">
    <div class="sidebar-header">
      <div class="logo">
        <h3><img src="@/assets/icons/2.png" alt="校园驿站" style="width: 22px; height: 22px; vertical-align: middle; margin-right: 8px;" /> 校园驿站</h3>
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

    <div class="sidebar-footer">
      <div
        class="nav-item"
        @click="handleProfile"

      >
        <img src="@/assets/icons/5.png" alt="个人中心" class="nav-icon" />
        <span class="nav-label">个人中心</span>
      </div>
    </div>
  </aside>
</template>

<script setup lang="ts">
import { ref } from 'vue'
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
  defaultActive: 'home'
})

const router = useRouter()
const route = useRoute()

const activeItem = ref(props.defaultActive)

const menuItems: MenuItem[] = [
  { id: 'home', label: '首页', icon: 'home', route: '/user/home' },
  { id: 'myPackages', label: '我的包裹', icon: 'myPackages', route: '/user/packages' },
  { id: 'pickupCode', label: '取件码', icon: 'pickupCode', route: '/user/pickup-code' },
  { id: 'announcements', label: '公告信息', icon: 'announcements', route: '/user/announcements' },
  { id: 'history', label: '历史记录', icon: 'history', route: '/user/history' },
]

const handleNavClick = (item: MenuItem) => {
  activeItem.value = item.id
  router.push(item.route)
}

const handleProfile = () => {
  activeItem.value = 'profile'
  router.push('/user/profile')
}

const getIconPath = (icon: string): string | undefined => {
  const iconMap: Record<string, string> = {
    myPackages: '/src/assets/icons/2.png',
    announcements: '/src/assets/icons/4.png',
    pickupCode: '/src/assets/icons/12.png',
    history: '/src/assets/icons/8.png',
    home: '/src/assets/icons/14.png',
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
