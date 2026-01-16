<template>
  <header class="header">
    <div class="header-content">
      <div class="logo">
        <h2>校园驿站管理系统</h2>
      </div>
      <nav class="nav">
        <span class="nav-item active">首页</span>
        <span class="nav-item">我的包裹</span>
        <span class="nav-item">公告信息</span>
        <span class="nav-item">个人中心</span>
      </nav>
      <div class="user-info">
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
  username: '用户'
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
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
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
  color: #10b981;
}

.nav {
  display: flex;
  gap: 40px;
}

.nav-item {
  font-size: 16px;
  color: #666;
  cursor: pointer;
  padding: 8px 16px;
  border-radius: 8px;
  transition: all 0.2s;
}

.nav-item:hover {
  background: #f0f0f0;
  color: #10b981;
}

.nav-item.active {
  color: #10b981;
  font-weight: 600;
  background: #e6f7f1;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 20px;
}

.username {
  font-size: 16px;
  color: #333;
  font-weight: 500;
}

.logout-btn {
  padding: 8px 20px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  background: white;
  color: #666;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.logout-btn:hover {
  border-color: #10b981;
  color: #10b981;
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
