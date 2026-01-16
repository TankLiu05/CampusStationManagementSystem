<template>
  <UserLayout>
    <div class="announcements">
    <div class="page-header">
      <h1>公告信息</h1>
      <p>查看驿站发布的最新公告</p>
    </div>

    <!-- 重要公告 -->
    <div class="important-section" v-if="importantAnnouncements.length > 0">
      <h2>📌 重要公告</h2>
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
        <div class="card-footer">
          <button class="detail-btn" @click="viewDetail(item.id)">查看详情</button>
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
          <div class="card-footer">
            <span class="views">👁️ {{ item.views }} 次查看</span>
            <button class="detail-btn" @click="viewDetail(item.id)">查看详情</button>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div class="pagination" v-if="normalAnnouncements.length > 0">
        <button class="page-btn" :disabled="currentPage === 1">上一页</button>
        <span class="page-info">第 {{ currentPage }} / {{ totalPages }} 页</span>
        <button class="page-btn" :disabled="currentPage === totalPages">下一页</button>
      </div>
    </div>
    </div>
  </UserLayout>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import UserLayout from '@/layouts/UserLayout.vue'

interface Announcement {
  id: string
  title: string
  content: string
  type: string
  typeLabel: string
  publishTime: string
  views: number
  isImportant: boolean
}

const importantAnnouncements = ref<Announcement[]>([])
const normalAnnouncements = ref<Announcement[]>([])
const currentPage = ref(1)
const totalPages = ref(1)

// TODO: 从后端获取公告数据

const viewDetail = (id: string) => {
  console.log('查看公告详情:', id)
  // TODO: 实现公告详情查看功能
}
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
  gap: 16px;
}

.announcement-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.3s;
}

.announcement-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
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

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.views {
  font-size: 13px;
  color: #999;
}

.detail-btn {
  padding: 6px 20px;
  background: white;
  color: #10b981;
  border: 1px solid #10b981;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.detail-btn:hover {
  background: #10b981;
  color: white;
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
  gap: 20px;
  margin-top: 30px;
  padding: 20px;
  background: white;
  border-radius: 12px;
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
  border-color: #10b981;
  color: #10b981;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  font-size: 14px;
  color: #666;
}
</style>
