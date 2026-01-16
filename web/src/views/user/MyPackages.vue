<template>
  <UserLayout>
    <div class="my-packages">
      <div class="page-header">
        <h1>我的包裹</h1>
        <p>查看和管理您的快递包裹</p>
      </div>

      <!-- 筛选区域 -->
      <div class="filter-section">
        <div class="filter-tabs">
          <button 
            v-for="tab in tabs" 
            :key="tab.value"
            :class="['tab-btn', { active: activeTab === tab.value }]"
            @click="activeTab = tab.value"
          >
            {{ tab.label }}
            <span class="tab-count">{{ tab.count }}</span>
          </button>
        </div>
        <div class="search-box">
          <input type="text" placeholder="搜索快递单号、快递公司..." v-model="searchKeyword">
          <button class="search-btn">搜索</button>
        </div>
      </div>

      <!-- 包裹列表 -->
      <div class="packages-list">
        <div v-if="packages.length === 0" class="empty-state">
          <img src="@/assets/icons/2.png" alt="暂无包裹" class="empty-icon" />
          <p>暂无包裹信息</p>
        </div>
        
        <div v-else class="package-card" v-for="pkg in packages" :key="pkg.id">
          <div class="package-header">
            <span class="package-company">{{ pkg.company }}</span>
            <span :class="['package-status', pkg.statusClass]">{{ pkg.status }}</span>
          </div>
          <div class="package-body">
            <div class="package-info">
              <div class="info-row">
                <span class="label">快递单号：</span>
                <span class="value">{{ pkg.trackingNumber }}</span>
              </div>
              <div class="info-row">
                <span class="label">存放位置：</span>
                <span class="value">{{ pkg.location }}</span>
              </div>
              <div class="info-row">
                <span class="label">到达时间：</span>
                <span class="value">{{ pkg.arrivalTime }}</span>
              </div>
            </div>
            <div class="package-actions">
              <button class="action-btn primary" v-if="pkg.canPickup">查看取件码</button>
              <button class="action-btn">查看详情</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </UserLayout>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import UserLayout from '@/layouts/UserLayout.vue'

interface Tab {
  label: string
  value: string
  count: number
}

interface Package {
  id: string
  company: string
  status: string
  statusClass: string
  trackingNumber: string
  location: string
  arrivalTime: string
  canPickup: boolean
}

const activeTab = ref('all')
const searchKeyword = ref('')

const tabs: Tab[] = [
  { label: '全部', value: 'all', count: 0 },
  { label: '待取件', value: 'pending', count: 0 },
  { label: '已取件', value: 'picked', count: 0 },
  { label: '已超期', value: 'overdue', count: 0 }
]

const packages = ref<Package[]>([])
// TODO: 从后端获取包裹数据
</script>

<style scoped>
.my-packages {
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

.filter-section {
  background: white;
  padding: 20px;
  border-radius: 12px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.filter-tabs {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.tab-btn {
  padding: 10px 20px;
  border: 1px solid #e0e0e0;
  background: white;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  color: #666;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 8px;
}

.tab-btn.active {
  background: #10b981;
  color: white;
  border-color: #10b981;
}

.tab-count {
  background: rgba(0, 0, 0, 0.1);
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
}

.tab-btn.active .tab-count {
  background: rgba(255, 255, 255, 0.3);
}

.search-box {
  display: flex;
  gap: 12px;
}

.search-box input {
  flex: 1;
  padding: 10px 16px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
}

.search-btn {
  padding: 10px 24px;
  background: #10b981;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.search-btn:hover {
  background: #059669;
}

.packages-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
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

.package-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.2s;
}

.package-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.package-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.package-company {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
}

.package-status {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 13px;
  font-weight: 500;
}

.package-status.pending {
  background: #fff7e6;
  color: #fa8c16;
}

.package-status.picked {
  background: #f6ffed;
  color: #52c41a;
}

.package-status.overdue {
  background: #fff1f0;
  color: #f5222d;
}

.package-body {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.package-info {
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
  margin-right: 8px;
}

.value {
  color: #333;
  font-weight: 500;
}

.package-actions {
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

.action-btn.primary {
  background: #10b981;
  color: white;
  border-color: #10b981;
}

.action-btn.primary:hover {
  background: #059669;
}
</style>
