<template>
  <header class="header">
    <div class="header-content">
      <div class="page-title">
        <h2>{{ currentPageName }}</h2>
      </div>
      <div class="datetime">
        <span class="time">{{ currentTime }}</span>
        <span class="date">{{ currentDate }}</span>
      </div>
      <div class="user-info">
        <span class="username">{{ username }}</span>
        <button class="logout-btn" @click="handleLogout">退出登录</button>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'

interface Props {
  username?: string
  currentPageName?: string
}

const props = withDefaults(defineProps<Props>(), {
  username: '用户',
  currentPageName: '首页'
})

const emit = defineEmits<{
  logout: []
}>()

const currentDate = ref('')
const currentTime = ref('')
let timer: number | null = null

const updateDateTime = () => {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  const weekDays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
  const weekDay = weekDays[now.getDay()]
  
  currentDate.value = `${year}年${month}月${day}日 ${weekDay}`
  
  const hours = String(now.getHours()).padStart(2, '0')
  const minutes = String(now.getMinutes()).padStart(2, '0')
  const seconds = String(now.getSeconds()).padStart(2, '0')
  currentTime.value = `${hours}:${minutes}:${seconds}`
}

onMounted(() => {
  updateDateTime()
  timer = window.setInterval(updateDateTime, 1000)
})

onUnmounted(() => {
  if (timer !== null) {
    clearInterval(timer)
  }
})

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
  margin: 0 auto;
  padding: 0 40px;
  height: 70px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.page-title {
  flex-shrink: 1;
  min-width: 0;
  overflow: hidden;
}

.page-title h2 {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.datetime {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
}

.time {
  font-size: 18px;
  color: #333;
  font-weight: 600;
  font-variant-numeric: tabular-nums;
  line-height: 1;
}

.date {
  font-size: 13px;
  color: #666;
  line-height: 1;
  white-space: nowrap;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 20px;
  flex-shrink: 0;
}

.username {
  font-size: 16px;
  color: #333;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 150px;
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

  .time {
    font-size: 16px;
  }
  
  .user-info {
    gap: 12px;
  }
  
  .username {
    font-size: 14px;
    max-width: 120px;
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
