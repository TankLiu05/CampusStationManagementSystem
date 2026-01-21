<template>
  <UserLayout>
    <div class="announcements">
    <div class="page-header">
      <h1>公告信息</h1>
      <p>查看驿站发布的最新公告</p>
    </div>

    <!-- 重要公告 -->
    <div class="important-section" v-if="importantAnnouncements.length > 0">
      <h2>重要公告</h2>
      <div class="announcement-card important" v-for="item in importantAnnouncements" :key="item.id">
        <div class="card-header">
          <div class="header-left">
            <span class="badge important">重要</span>
            <h3>{{ item.title }}</h3>
          </div>
          <span class="publish-time">{{ item.publishTime }}</span>
        </div>
        <div class="card-content">
          <p>{{ item.content }}</p>
        </div>

      </div>
    </div>

    <!-- 普通公告 -->
    <div class="normal-section">
      <h2><img src="@/assets/icons/4.png" alt="公告" style="width: 22px; height: 22px; vertical-align: middle; margin-right: 8px;" /> 全部公告</h2>
      
      <div v-if="normalAnnouncements.length === 0" class="empty-state">
        <img src="@/assets/icons/4.png" alt="暂无公告" class="empty-icon" />
        <p>暂无公告信息</p>
      </div>

      <div v-else class="announcements-list">
        <div class="announcement-card" v-for="item in normalAnnouncements" :key="item.id">
          <div class="card-header">
            <div class="header-left">
              <span class="badge" :class="item.type">{{ item.typeLabel }}</span>
              <h3>{{ item.title }}</h3>
            </div>
            <span class="publish-time">{{ item.publishTime }}</span>
          </div>
          <div class="card-content">
            <p>{{ item.content }}</p>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div class="pagination" v-if="normalAnnouncements.length > 0">
        <button class="page-btn" :disabled="currentPage === 1" @click="prevPage">上一页</button>
        <span class="page-info">第 {{ currentPage }} / {{ totalPages }} 页</span>
        <button class="page-btn" :disabled="currentPage === totalPages" @click="nextPage">下一页</button>
      </div>
    </div>
    </div>
  </UserLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import UserLayout from '@/layouts/UserLayout.vue'
import { listNotices, type Notice } from '@/api/user/notice'


interface Announcement {
  id: number
  title: string
  content: string
  type: string
  typeLabel: string
  publishTime: string
  isImportant: boolean
}

const announcements = ref<Announcement[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const totalPages = ref(1)
const loading = ref(false)

// 计算属性：重要公告
const importantAnnouncements = computed(() => 
  announcements.value.filter(item => item.isImportant)
)

// 计算属性：普通公告
const normalAnnouncements = computed(() => 
  announcements.value.filter(item => !item.isImportant)
)

// 格式化日期时间
const formatDateTime = (dateStr: string) => {
  const date = new Date(dateStr)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

// 判断是否为重要公告（根据标题关键词）
const isImportantNotice = (title: string) => {
  const keywords = ['重要', '紧急', '必读', '通知']
  return keywords.some(keyword => title.includes(keyword))
}

// 获取公告类型标签
const getTypeLabel = (title: string) => {
  if (title.includes('重要') || title.includes('紧急')) return '重要'
  if (title.includes('系统')) return '系统'
  return '通知'
}

// 获取公告类型
const getType = (title: string) => {
  if (title.includes('重要') || title.includes('紧急')) return 'important'
  if (title.includes('系统')) return 'system'
  return 'notice'
}

// 加载公告列表
const loadAnnouncements = async () => {
  loading.value = true
  try {
    const response = await listNotices(currentPage.value - 1, pageSize.value)
    
    // 转换后端数据为前端格式
    announcements.value = response.content.map((notice: Notice) => ({
      id: notice.id,
      title: notice.title,
      content: notice.content,
      type: getType(notice.title),
      typeLabel: getTypeLabel(notice.title),
      publishTime: formatDateTime(notice.createTime),
      isImportant: isImportantNotice(notice.title)
    }))
    
    totalPages.value = response.totalPages
  } catch (error) {
    console.error('加载公告列表失败:', error)
    announcements.value = []
  } finally {
    loading.value = false
  }
}

// 上一页
const prevPage = () => {
  if (currentPage.value > 1) {
    currentPage.value--
    loadAnnouncements()
  }
}

// 下一页
const nextPage = () => {
  if (currentPage.value < totalPages.value) {
    currentPage.value++
    loadAnnouncements()
  }
}

// 页面加载时获取公告列表
onMounted(() => {
  loadAnnouncements()
})
</script>

<style scoped>
.announcements {
}

.page-header {
  margin-bottom: 30px;
}

.page-header h1 {
  font-size: 28px;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 8px;
}

.page-header p {
  font-size: 16px;
  color: #666;
}

.important-section,
.normal-section {
  margin-bottom: 30px;
}

.important-section h2,
.normal-section h2 {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 20px;
}

.announcements-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.announcement-card {
  background: white;
  border-radius: 10px;
  padding: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.announcement-card.important {
  border-left: 4px solid #f5222d;
  background: linear-gradient(to right, #fff1f0 0%, white 20%);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.badge {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  white-space: nowrap;
}

.badge.important {
  background: #fff1f0;
  color: #f5222d;
}

.badge.notice {
  background: #e6f7ff;
  color: #1890ff;
}

.badge.system {
  background: #f6ffed;
  color: #52c41a;
}

.card-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
}

.publish-time {
  font-size: 14px;
  color: #999;
  white-space: nowrap;
}

.card-content {
  margin-bottom: 16px;
  padding: 16px 0;
  border-bottom: 1px solid #f0f0f0;
}

.card-content p {
  font-size: 15px;
  color: #666;
  line-height: 1.8;
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.empty-state {
  background: white;
  padding: 80px;
  border-radius: 12px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.empty-icon[src] {
  width: 64px;
  height: 64px;
  object-fit: contain;
}

.empty-state p {
  font-size: 16px;
  color: #999;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px;
  margin-top: 10px;
  padding: 10px;
  background: white;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.page-btn {
  padding: 8px 20px;
  background: white;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  color: #333;
  transition: all 0.2s;
}

.page-btn:hover:not(:disabled) {
  border-color: #666;
  color: #666;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  font-size: 14px;
  color: #666;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .page-header h1 {
    font-size: 24px;
  }
  
  .page-header p {
    font-size: 14px;
  }
  
  .important-section h2,
  .normal-section h2 {
    font-size: 18px;
  }
  
  .announcement-card {
    padding: 16px;
  }
  
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .header-left {
    flex-wrap: wrap;
    gap: 8px;
  }
  
  .card-header h3 {
    font-size: 16px;
  }
  
  .publish-time {
    font-size: 13px;
  }
  
  .card-content p {
    font-size: 14px;
  }
  
  .pagination {
    flex-wrap: wrap;
    gap: 8px;
  }
  
  .page-btn {
    padding: 6px 14px;
    font-size: 13px;
  }
  
  .empty-state {
    padding: 40px 20px;
  }
}

@media (max-width: 480px) {
  .page-header h1 {
    font-size: 22px;
  }
  
  .important-section h2,
  .normal-section h2 {
    font-size: 16px;
  }
  
  .badge {
    padding: 3px 8px;
    font-size: 11px;
  }
  
  .card-header h3 {
    font-size: 15px;
  }
  
  .card-content p {
    font-size: 13px;
    -webkit-line-clamp: 2;
  }
  
}
</style>
