<template>
  <UserLayout>
      <div class="welcome-section">
        <h1>欢迎回来，{{ currentUser?.username }}！</h1>
        <p>您可以在这里查看和管理您的快递包裹</p>
      </div>

      <!-- 快捷功能卡片 -->
      <div class="function-cards">
        <div class="card">
          <img src="@/assets/icons/2.png" alt="我的包裹" class="card-icon" />
          <h3>我的包裹</h3>
          <p>查看待取包裹</p>
          <div class="card-count">0 件</div>
        </div>

        <div class="card">
          <img src="@/assets/icons/3.png" alt="已签收" class="card-icon" />
          <h3>已签收</h3>
          <p>历史签收记录</p>
          <div class="card-count">0 件</div>
        </div>

        <div class="card">
          <img src="@/assets/icons/4.png" alt="公告通知" class="card-icon" />
          <h3>公告通知</h3>
          <p>最新公告信息</p>
          <div class="card-count">0 条</div>
        </div>

        <div class="card">
          <img src="@/assets/icons/1.png" alt="个人信息" class="card-icon" />
          <h3>个人信息</h3>
          <p>查看和编辑资料</p>
          <div class="card-count">0 条</div>
        </div>
      </div>

      <!-- 最近包裹 -->
      <div class="recent-section">
        <h2>最近包裹</h2>
        <div class="empty-state">
          <p>暂无包裹信息</p>
        </div>
      </div>
  </UserLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getCurrentUser, logout } from '@/api/sysUser'
import type { SysUser } from '@/api/sysUser'
import UserLayout from '@/layouts/UserLayout.vue'

const router = useRouter()
const currentUser = ref<SysUser | null>(null)

onMounted(async () => {
  try {
    currentUser.value = await getCurrentUser()
    
    // 权限检查:如果是管理员,跳转到管理员首页
    if (currentUser.value.role === 'ADMIN') {
      router.replace('/admin/home')
      return
    }
  } catch (err) {
    console.error('获取用户信息失败:', err)
    router.replace('/login')
  }
})
</script>

<style scoped>
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
  text-align: center;
}

.card-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.card-icon[src] {
  width: 48px;
  height: 48px;
  object-fit: contain;
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
