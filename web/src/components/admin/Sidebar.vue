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
import { ref, watch, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getCurrentAdminDetail, type AdminRole } from '@/api/admin/management'
import { useToast } from '@/composables/useToast'

const { warning } = useToast()

// 角色权限等级（数字越小权限越高）
const roleLevel: Record<AdminRole, number> = {
  SUPERADMIN: 1,
  MANAGER: 2,
  CITY_ADMIN: 3,
  STREET_ADMIN: 4
}

// 角色显示名称
const roleDisplayName: Record<AdminRole, string> = {
  SUPERADMIN: '超级管理员',
  MANAGER: '省级管理员',
  CITY_ADMIN: '市级管理员',
  STREET_ADMIN: '站点管理员'
}

interface MenuItem {
  id: string
  label: string
  icon: string
  route: string
  requiredRole?: AdminRole // 所需的最低权限角色
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
const currentRole = ref<AdminRole | null>(null)

// 菜单项及其权限要求
const menuItems: MenuItem[] = [
  { id: 'dashboard', label: '工作台', icon: 'dashboard', route: '/admin/home' },
  { id: 'users', label: '用户管理', icon: 'users', route: '/admin/users', requiredRole: 'CITY_ADMIN' },
  { id: 'admins', label: '管理员管理', icon: 'admins', route: '/admin/admins', requiredRole: 'CITY_ADMIN' },
  { id: 'packages', label: '包裹管理', icon: 'packages', route: '/admin/packages' },
  { id: 'warehouse', label: '仓库管理', icon: 'warehouse', route: '/admin/warehouse' },
  { id: 'warehouse-info', label: '仓库信息', icon: 'warehouse-info', route: '/admin/warehouse-info' },
  { id: 'returns', label: '退货申请', icon: 'returns', route: '/admin/returns' },
  { id: 'logistics', label: '物流管理', icon: 'logistics', route: '/admin/logistics', requiredRole: 'CITY_ADMIN' },
  { id: 'messages', label: '留言管理', icon: 'messages', route: '/admin/messages' },
  { id: 'announcements', label: '公告管理', icon: 'announcements', route: '/admin/announcements', requiredRole: 'MANAGER' },
  { id: 'settings', label: '个人设置', icon: 'settings', route: '/admin/settings' },
]

// 检查当前用户是否有权限访问指定菜单
const hasPermission = (item: MenuItem): boolean => {
  // 没有权限要求的菜单，所有人可访问
  if (!item.requiredRole) return true
  // 未获取到当前角色时，暂时放行（等待加载）
  if (!currentRole.value) return true
  
  const currentLevel = roleLevel[currentRole.value]
  const requiredLevel = roleLevel[item.requiredRole]
  
  // 当前角色等级 <= 要求等级时有权限（数字越小权限越高）
  return currentLevel <= requiredLevel
}

// 获取当前管理员角色
const loadCurrentAdminRole = async () => {
  try {
    const detail = await getCurrentAdminDetail()
    currentRole.value = detail.role
  } catch (error) {
    console.error('获取管理员角色失败:', error)
  }
}

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
  // 检查权限
  if (!hasPermission(item)) {
    const requiredRoleName = item.requiredRole ? roleDisplayName[item.requiredRole] : ''
    const currentRoleName = currentRole.value ? roleDisplayName[currentRole.value] : ''
    warning(`权限不足：「${item.label}」需要${requiredRoleName}及以上权限，您当前是${currentRoleName}`)
    return
  }
  
  activeItem.value = item.id
  router.push(item.route)
}

// 组件挂载时加载角色
onMounted(() => {
  loadCurrentAdminRole()
})

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

/* 响应式设计 */
@media (max-width: 992px) {
  .sidebar {
    width: 220px;
  }
  
  .nav-item {
    padding: 12px 16px;
    margin: 4px 8px;
    font-size: 14px;
  }
  
  .nav-icon {
    font-size: 20px;
    min-width: 26px;
  }
  
  .nav-icon[src] {
    width: 20px;
    height: 20px;
  }
  
  .nav-label {
    margin-left: 10px;
  }
  
  .logo h3 {
    font-size: 18px;
  }
  
  .logo-icon {
    width: 24px;
    height: 24px;
  }
}

@media (max-width: 768px) {
  .sidebar {
    width: 100%;
    height: auto;
    position: relative;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  }
  
  .sidebar-header {
    height: 60px;
    min-height: 60px;
    padding: 0 20px;
  }
  
  .sidebar-nav {
    display: flex;
    flex-wrap: wrap;
    padding: 10px;
    gap: 8px;
  }
  
  .nav-item {
    padding: 10px 14px;
    margin: 0;
    flex: 0 0 auto;
    border-radius: 8px;
    font-size: 13px;
    background: #f5f5f5;
  }
  
  .nav-item.active {
    background: #e0e0e0;
  }
  
  .nav-icon {
    font-size: 16px;
    min-width: 20px;
  }
  
  .nav-icon[src] {
    width: 16px;
    height: 16px;
  }
  
  .nav-label {
    margin-left: 6px;
    font-size: 13px;
  }
}

@media (max-width: 480px) {
  .sidebar-nav {
    padding: 8px;
    gap: 6px;
  }
  
  .nav-item {
    padding: 8px 10px;
    font-size: 12px;
  }
  
  .nav-icon[src] {
    width: 14px;
    height: 14px;
  }
  
  .nav-label {
    margin-left: 4px;
    font-size: 12px;
  }
  
  .logo h3 {
    font-size: 16px;
  }
}
</style>
