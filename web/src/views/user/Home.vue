<template>
  <div class="user-home">
    <!-- 顶部导航栏 -->
    <Navbar :username="currentUser?.username" @logout="handleLogout" />

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
import Navbar from '@/components/user/Navbar.vue'

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
.user-home {
  min-height: 100vh;
  background: #f5f7fa;
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
@media (max-width: 768px) {
  .function-cards {
    grid-template-columns: 1fr;
  }

  .main-content {
    padding: 20px;
  }
}
</style>
