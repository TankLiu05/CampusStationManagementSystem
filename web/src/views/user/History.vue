<template>
  <UserLayout>
    <div class="history">
    <div class="page-header">
      <h1>历史记录</h1>
      <p>查看您的取件历史记录</p>
    </div>

    <!-- 统计概览 -->
    <div class="stats-section">
      <div class="stat-card">
        <img src="@/assets/icons/2.png" alt="累计包裹" class="stat-icon" />
        <div class="stat-content">
          <div class="stat-number">0</div>
          <div class="stat-label">累计包裹</div>
        </div>
      </div>
      <div class="stat-card">
        <img src="@/assets/icons/3.png" alt="已取包裹" class="stat-icon" />
        <div class="stat-content">
          <div class="stat-number">0</div>
          <div class="stat-label">已取包裹</div>
        </div>
      </div>
      <div class="stat-card">
        <img src="@/assets/icons/15.png" alt="平均时长" class="stat-icon" />
        <div class="stat-content">
          <div class="stat-number">0</div>
          <div class="stat-label">平均时长</div>
        </div>
      </div>
      <div class="stat-card">
        <img src="@/assets/icons/6.png" alt="统计" class="stat-icon" />
        <div class="stat-content">
          <div class="stat-number">0%</div>
          <div class="stat-label">按时率</div>
        </div>
      </div>
    </div>

    <!-- 筛选区域 -->
    <div class="filter-section">
      <div class="filter-left">
        <select class="filter-select" v-model="timeRange">
          <option value="all">全部时间</option>
          <option value="week">近一周</option>
          <option value="month">近一月</option>
          <option value="quarter">近三月</option>
        </select>
        <select class="filter-select" v-model="statusFilter">
          <option value="all">全部状态</option>
          <option value="picked">已取件</option>
          <option value="expired">已过期</option>
        </select>
      </div>
      <div class="search-box">
        <input type="text" placeholder="搜索快递单号..." v-model="searchKeyword">
        <button class="search-btn">搜索</button>
      </div>
    </div>

    <!-- 历史记录列表 -->
    <div class="history-list">
      <div v-if="historyRecords.length === 0" class="empty-state">
        <p>暂无历史记录</p>
      </div>

      <div v-else class="timeline">
        <div class="timeline-item" v-for="record in historyRecords" :key="record.id">
          <div class="timeline-dot"></div>
          <div class="timeline-content">
            <div class="record-card">
              <div class="record-header">
                <div class="header-left">
                  <span class="record-company">{{ record.company }}</span>
                  <span :class="['record-status', record.statusClass]">{{ record.status }}</span>
                </div>
                <span class="record-date">{{ record.pickupDate }}</span>
              </div>
              <div class="record-body">
                <div class="record-info">
                  <div class="info-row">
                    <span class="label">快递单号：</span>
                    <span class="value">{{ record.trackingNumber }}</span>
                  </div>
                  <div class="info-row">
                    <span class="label">到达时间：</span>
                    <span class="value">{{ record.arrivalTime }}</span>
                  </div>
                  <div class="info-row">
                    <span class="label">取件时间：</span>
                    <span class="value">{{ record.pickupTime }}</span>
                  </div>
                  <div class="info-row">
                    <span class="label">存放时长：</span>
                    <span class="value">{{ record.storageDuration }}</span>
                  </div>
                </div>
                <div class="record-actions">
                  <button class="action-btn">查看详情</button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 加载更多 -->
      <div class="load-more" v-if="historyRecords.length > 0 && hasMore">
        <button class="load-btn">加载更多</button>
      </div>
    </div>
    </div>
  </UserLayout>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import UserLayout from '@/layouts/UserLayout.vue'

interface HistoryRecord {
  id: string
  company: string
  status: string
  statusClass: string
  trackingNumber: string
  arrivalTime: string
  pickupTime: string
  pickupDate: string
  storageDuration: string
}

const timeRange = ref('all')
const statusFilter = ref('all')
const searchKeyword = ref('')
const historyRecords = ref<HistoryRecord[]>([])
const hasMore = ref(false)

// TODO: 从后端获取历史记录数据
</script>

<style scoped>
.history {
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

.stats-section {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  background: white;
  padding: 24px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  font-size: 36px;
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f2f5;
  border-radius: 10px;
}

.stat-icon[src] {
  object-fit: contain;
  padding: 12px;
}

.stat-number {
  font-size: 24px;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 13px;
  color: #999;
}

.filter-section {
  background: white;
  padding: 20px;
  border-radius: 12px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filter-left {
  display: flex;
  gap: 12px;
}

.filter-select {
  padding: 8px 16px;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  font-size: 14px;
  color: #333;
  background: white;
  cursor: pointer;
}

.search-box {
  display: flex;
  gap: 8px;
}

.search-box input {
  padding: 8px 16px;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  font-size: 14px;
  width: 250px;
}

.search-btn {
  padding: 8px 16px;
  background: #10b981;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.history-list {
  background: white;
  padding: 30px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.empty-state {
  text-align: center;
  padding: 80px;
}

.empty-state p {
  font-size: 16px;
  color: #999;
}

.timeline {
  position: relative;
  padding-left: 30px;
}

.timeline::before {
  content: '';
  position: absolute;
  left: 8px;
  top: 10px;
  bottom: 10px;
  width: 2px;
  background: #e0e0e0;
}

.timeline-item {
  position: relative;
  margin-bottom: 24px;
}

.timeline-item:last-child {
  margin-bottom: 0;
}

.timeline-dot {
  position: absolute;
  left: -26px;
  top: 8px;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: #10b981;
  border: 3px solid white;
  box-shadow: 0 0 0 2px #10b981;
}

.timeline-content {
  margin-left: 10px;
}

.record-card {
  border: 1px solid #e0e0e0;
  border-radius: 10px;
  padding: 20px;
  transition: all 0.2s;
}

.record-card:hover {
  border-color: #10b981;
  box-shadow: 0 2px 12px rgba(16, 185, 129, 0.15);
}

.record-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.record-company {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
}

.record-status {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 13px;
  font-weight: 500;
}

.record-status.picked {
  background: #f6ffed;
  color: #52c41a;
}

.record-status.expired {
  background: #fff1f0;
  color: #f5222d;
}

.record-date {
  font-size: 14px;
  color: #999;
}

.record-body {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.record-info {
  flex: 1;
}

.info-row {
  margin-bottom: 8px;
  font-size: 14px;
}

.info-row:last-child {
  margin-bottom: 0;
}

.label {
  color: #999;
}

.value {
  color: #333;
  font-weight: 500;
  margin-left: 8px;
}

.record-actions {
  display: flex;
  gap: 12px;
}

.action-btn {
  padding: 8px 20px;
  border: 1px solid #e0e0e0;
  background: white;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  color: #333;
  transition: all 0.2s;
}

.action-btn:hover {
  border-color: #10b981;
  color: #10b981;
}

.load-more {
  text-align: center;
  margin-top: 30px;
}

.load-btn {
  padding: 10px 40px;
  background: white;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  color: #333;
  transition: all 0.2s;
}

.load-btn:hover {
  border-color: #10b981;
  color: #10b981;
}

@media (max-width: 768px) {
  .stats-section {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
