<template>
  <header class="header">
    <div class="header-content">
      <div class="logo">
        <h2>🛡️ 管理员后台</h2>
      </div>
      <nav class="nav">
        <span class="nav-item active">工作台</span>
        <span class="nav-item">用户管理</span>
        <span class="nav-item">包裹管理</span>
        <span class="nav-item">公告管理</span>
        <span class="nav-item">系统设置</span>
      </nav>
      <div class="user-info">
        <span class="admin-badge">管理员</span>
        <span class="username">{{ username }}</span>
        <button class="logout-btn" @click="handleLogout">退出登录</button>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
interface Props {
  username?: string
}

const props = withDefaults(defineProps<Props>(), {
  username: 'Admin'
})

const emit = defineEmits<{
  logout: []
}>()

const handleLogout = () => {
  emit('logout')
}
</script>

<style scoped>
.header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 40px;
  height: 70px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.logo h2 {
  font-size: 22px;
  font-weight: 600;
  color: white;
}

.nav {
  display: flex;
  gap: 30px;
}

.nav-item {
  font-size: 15px;
  color: rgba(255, 255, 255, 0.9);
  cursor: pointer;
  padding: 8px 16px;
  border-radius: 8px;
  transition: all 0.2s;
}

.nav-item:hover {
  background: rgba(255, 255, 255, 0.2);
  color: white;
}

.nav-item.active {
  color: white;
  font-weight: 600;
  background: rgba(255, 255, 255, 0.25);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.admin-badge {
  font-size: 12px;
  color: white;
  background: rgba(255, 255, 255, 0.3);
  padding: 4px 12px;
  border-radius: 12px;
  font-weight: 500;
}

.username {
  font-size: 16px;
  color: white;
  font-weight: 500;
}

.logout-btn {
  padding: 8px 20px;
  border: 1px solid rgba(255, 255, 255, 0.5);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.1);
  color: white;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.logout-btn:hover {
  background: rgba(255, 255, 255, 0.2);
  border-color: white;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .header-content {
    padding: 0 20px;
  }

  .nav {
    display: none;
  }
}
</style>
