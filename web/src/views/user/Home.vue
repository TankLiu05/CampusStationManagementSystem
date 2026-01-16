<template>
  <div class="user-home">
    <!-- 顶部导航栏 -->
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
          <span class="username">{{ currentUser?.username || '用户' }}</span>
          <button class="logout-btn" @click="handleLogout">退出登录</button>
        </div>
      </div>
    </header>

    <!-- 主要内容区域 -->
    <main class="main-content">
      <div class="welcome-section">
        <h1>欢迎回来，{{ currentUser?.username }}！</h1>
        <p>您可以在这里查看和管理您的快递包裹</p>
      </div>

      <!-- 快捷功能卡片 -->
      <div class="function-cards">
        <div class="card">
          <div class="card-icon">📦</div>
          <h3>我的包裹</h3>
          <p>查看待取包裹</p>
          <div class="card-count">0 件</div>
        </div>

        <div class="card">
          <div class="card-icon">✅</div>
          <h3>已签收</h3>
          <p>历史签收记录</p>
          <div class="card-count">0 件</div>
        </div>

        <div class="card">
          <div class="card-icon">📢</div>
          <h3>公告通知</h3>
          <p>最新公告信息</p>
          <div class="card-count">0 条</div>
        </div>

        <div class="card">
          <div class="card-icon">👤</div>
          <h3>个人信息</h3>
          <p>查看和编辑资料</p>
        </div>
      </div>

      <!-- 最近包裹 -->
      <div class="recent-section">
        <h2>最近包裹</h2>
        <div class="empty-state">
          <p>暂无包裹信息</p>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getCurrentUser, logout } from '@/api/sysUser'
import type { SysUser } from '@/api/sysUser'

const router = useRouter()
const currentUser = ref<SysUser | null>(null)

onMounted(async () => {
  try {
    currentUser.value = await getCurrentUser()
    
    // 权限检查：如果是管理员，跳转到管理员首页
    if (currentUser.value.role === 'ADMIN') {
      router.replace('/admin/home')
      return
    }
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
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.user-home {
  min-height: 100vh;
  background: #f5f7fa;
}

/* 顶部导航栏 */
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

/* 主要内容区域 */
.main-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 40px;
}

.welcome-section {
  margin-bottom: 40px;
}

.welcome-section h1 {
  font-size: 32px;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 12px;
}

.welcome-section p {
  font-size: 18px;
  color: #666;
}

/* 功能卡片 */
.function-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
  margin-bottom: 40px;
}

.card {
  background: white;
  padding: 30px;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  cursor: pointer;
  transition: all 0.3s;
  text-align: center;
}

.card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12);
}

.card-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.card h3 {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 8px;
}

.card p {
  font-size: 14px;
  color: #999;
  margin-bottom: 16px;
}

.card-count {
  font-size: 24px;
  font-weight: 700;
  color: #10b981;
}

/* 最近包裹 */
.recent-section {
  background: white;
  padding: 30px;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.recent-section h2 {
  font-size: 24px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 20px;
}

.empty-state {
  text-align: center;
  padding: 60px 0;
}

.empty-state p {
  font-size: 16px;
  color: #999;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .function-cards {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .header-content {
    padding: 0 20px;
  }

  .nav {
    display: none;
  }

  .function-cards {
    grid-template-columns: 1fr;
  }

  .main-content {
    padding: 20px;
  }
}
</style>
