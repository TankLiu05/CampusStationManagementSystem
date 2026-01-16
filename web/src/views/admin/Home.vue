<template>
  <div class="admin-home">
    <!-- 顶部导航栏 -->
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
          <span class="username">{{ currentUser?.username || 'Admin' }}</span>
          <button class="logout-btn" @click="handleLogout">退出登录</button>
        </div>
      </div>
    </header>

    <!-- 主要内容区域 -->
    <main class="main-content">
      <div class="welcome-section">
        <h1>管理员工作台</h1>
        <p>欢迎，{{ currentUser?.username }}！系统运行正常</p>
      </div>

      <!-- 数据统计卡片 -->
      <div class="stats-cards">
        <div class="stat-card">
          <div class="stat-icon" style="background: #e6f7ff;">👥</div>
          <div class="stat-content">
            <h3>用户总数</h3>
            <div class="stat-number">0</div>
            <p class="stat-desc">注册用户</p>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon" style="background: #fff7e6;">📦</div>
          <div class="stat-content">
            <h3>包裹总数</h3>
            <div class="stat-number">0</div>
            <p class="stat-desc">待取包裹</p>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon" style="background: #f0f5ff;">✅</div>
          <div class="stat-content">
            <h3>今日签收</h3>
            <div class="stat-number">0</div>
            <p class="stat-desc">已完成签收</p>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon" style="background: #e6fffb;">📢</div>
          <div class="stat-content">
            <h3>公告数量</h3>
            <div class="stat-number">0</div>
            <p class="stat-desc">已发布公告</p>
          </div>
        </div>
      </div>

      <!-- 管理功能区 -->
      <div class="admin-sections">
        <div class="section">
          <h2>快捷操作</h2>
          <div class="action-buttons">
            <button class="action-btn primary">
              <span class="btn-icon">➕</span>
              <span>录入包裹</span>
            </button>
            <button class="action-btn">
              <span class="btn-icon">📢</span>
              <span>发布公告</span>
            </button>
            <button class="action-btn">
              <span class="btn-icon">👥</span>
              <span>用户管理</span>
            </button>
            <button class="action-btn">
              <span class="btn-icon">📊</span>
              <span>数据报表</span>
            </button>
          </div>
        </div>

        <div class="section">
          <h2>系统状态</h2>
          <div class="system-status">
            <div class="status-item">
              <span class="status-label">系统运行状态</span>
              <span class="status-value success">正常</span>
            </div>
            <div class="status-item">
              <span class="status-label">数据库连接</span>
              <span class="status-value success">正常</span>
            </div>
            <div class="status-item">
              <span class="status-label">服务器负载</span>
              <span class="status-value">低</span>
            </div>
          </div>
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
    
    // 权限检查：如果不是管理员，跳转到用户首页
    if (currentUser.value.role !== 'ADMIN') {
      router.replace('/user/home')
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

.admin-home {
  min-height: 100vh;
  background: #f0f2f5;
}

/* 顶部导航栏 */
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

/* 统计卡片 */
.stats-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
  margin-bottom: 40px;
}

.stat-card {
  background: white;
  padding: 24px;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  display: flex;
  align-items: center;
  gap: 20px;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12);
}

.stat-icon {
  font-size: 36px;
  width: 70px;
  height: 70px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
}

.stat-content h3 {
  font-size: 14px;
  color: #999;
  font-weight: 500;
  margin-bottom: 8px;
}

.stat-number {
  font-size: 28px;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 4px;
}

.stat-desc {
  font-size: 12px;
  color: #999;
}

/* 管理功能区 */
.admin-sections {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 24px;
}

.section {
  background: white;
  padding: 30px;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.section h2 {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 24px;
}

.action-buttons {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.action-btn {
  padding: 20px;
  border: 1px solid #e0e0e0;
  border-radius: 12px;
  background: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 16px;
  color: #333;
  transition: all 0.2s;
}

.action-btn:hover {
  border-color: #667eea;
  background: #f5f7ff;
  color: #667eea;
}

.action-btn.primary {
  background: #667eea;
  color: white;
  border-color: #667eea;
}

.action-btn.primary:hover {
  background: #5568d3;
}

.btn-icon {
  font-size: 24px;
}

.system-status {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.status-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid #f0f0f0;
}

.status-item:last-child {
  border-bottom: none;
}

.status-label {
  font-size: 15px;
  color: #666;
}

.status-value {
  font-size: 15px;
  color: #333;
  font-weight: 600;
}

.status-value.success {
  color: #10b981;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .stats-cards {
    grid-template-columns: repeat(2, 1fr);
  }

  .admin-sections {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .header-content {
    padding: 0 20px;
  }

  .nav {
    display: none;
  }

  .stats-cards {
    grid-template-columns: 1fr;
  }

  .action-buttons {
    grid-template-columns: 1fr;
  }

  .main-content {
    padding: 20px;
  }
}
</style>
