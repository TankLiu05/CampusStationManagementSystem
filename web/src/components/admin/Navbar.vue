<template>
  <header class="header">
    <div class="header-content">
      <div class="page-title">
        <h2>{{ currentPageName }}</h2>
      </div>
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
  currentPageName?: string
}

const props = withDefaults(defineProps<Props>(), {
  username: 'Admin',
  currentPageName: '工作台'
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

.page-title h2 {
  font-size: 20px;
  font-weight: 600;
  color: #333;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.admin-badge {
  font-size: 12px;
  color: #666;
  background: #f5f5f5;
  padding: 4px 12px;
  border-radius: 12px;
  font-weight: 500;
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
  background: #f5f5f5;
  border-color: #999;
  color: #333;
}

/* 响应式设计 */
@media (max-width: 992px) {
  .header-content {
    padding: 0 30px;
  }
}

@media (max-width: 768px) {
  .header-content {
    padding: 0 20px;
    height: 60px;
  }

  .page-title h2 {
    font-size: 16px;
  }

  .admin-badge {
    display: none;
  }
  
  .user-info {
    gap: 10px;
  }
  
  .username {
    font-size: 14px;
  }
  
  .logout-btn {
    padding: 6px 14px;
    font-size: 13px;
  }
}

@media (max-width: 480px) {
  .header-content {
    padding: 0 15px;
    height: 56px;
  }
  
  .page-title h2 {
    font-size: 15px;
  }
  
  .username {
    display: none;
  }
  
  .logout-btn {
    padding: 6px 12px;
    font-size: 12px;
  }
}
</style>
