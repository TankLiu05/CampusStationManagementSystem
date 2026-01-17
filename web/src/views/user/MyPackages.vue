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
          <input 
            type="text" 
            placeholder="搜索快递单号、快递公司..." 
            v-model="searchKeyword"
            @keyup.enter="handleSearch"
          >
          <button class="search-btn" @click="handleSearch" :disabled="loading">搜索</button>
        </div>
      </div>

      <!-- 包裹列表 -->
      <div class="packages-list">
        <div v-if="loading" class="loading-state">
          <p>加载中...</p>
        </div>
        <div v-else-if="packages.length === 0" class="empty-state">
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
              <button 
                class="action-btn primary" 
                v-if="pkg.canPickup"
                @click="handleSign(pkg)"
              >
                确认签收
              </button>
              <button class="action-btn">查看详情</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </UserLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import UserLayout from '@/layouts/UserLayout.vue'
import { listMyParcels, searchMyParcelByTrackingNumber, signParcel, type Parcel } from '@/api/user/parcel'

interface Tab {
  label: string
  value: string
  count: number
}

interface Package {
  id: number
  company: string
  status: string
  statusClass: string
  trackingNumber: string
  location: string
  arrivalTime: string
  canPickup: boolean
  rawData: Parcel
}

const activeTab = ref('all')
const searchKeyword = ref('')
const allParcels = ref<Parcel[]>([])
const loading = ref(false)
const currentPage = ref(0)
const pageSize = ref(10)
const totalElements = ref(0)

// 计算标签计数
const tabs = computed<Tab[]>(() => [
  { label: '全部', value: 'all', count: allParcels.value.length },
  { 
    label: '待取件', 
    value: 'pending', 
    count: allParcels.value.filter(p => p.isSigned === 0).length 
  },
  { 
    label: '已取件', 
    value: 'picked', 
    count: allParcels.value.filter(p => p.isSigned === 1).length 
  },
  { 
    label: '已超期', 
    value: 'overdue', 
    count: 0 // TODO: 需要根据实际业务逻辑判断超期
  }
])

// 将后端 Parcel 转换为前端 Package
function transformParcel(parcel: Parcel): Package {
  // 状态映射
  let status = '未知'
  let statusClass = ''
  
  if (parcel.isSigned === 1) {
    status = '已取件'
    statusClass = 'picked'
  } else if (parcel.status === 2) {
    status = '待取件'
    statusClass = 'pending'
  } else if (parcel.status === 1) {
    status = '已发货'
    statusClass = 'pending'
  } else if (parcel.status === 0) {
    status = '待发货'
    statusClass = 'pending'
  } else if (parcel.status === 3) {
    status = '退回/异常'
    statusClass = 'overdue'
  }

  return {
    id: parcel.id,
    company: parcel.company || '未知快递',
    status,
    statusClass,
    trackingNumber: parcel.trackingNumber,
    location: '校园驿站', // TODO: 如果后端有存放位置字段，需要映射
    arrivalTime: new Date(parcel.createTime).toLocaleString('zh-CN'),
    canPickup: parcel.isSigned === 0 && parcel.status === 2,
    rawData: parcel
  }
}

// 根据当前激活标签和搜索关键词过滤包裹
const packages = computed<Package[]>(() => {
  let filtered = allParcels.value.map(transformParcel)

  // 根据标签筛选
  if (activeTab.value === 'pending') {
    filtered = filtered.filter(p => p.rawData.isSigned === 0)
  } else if (activeTab.value === 'picked') {
    filtered = filtered.filter(p => p.rawData.isSigned === 1)
  } else if (activeTab.value === 'overdue') {
    // TODO: 根据实际业务逻辑过滤超期快递
    filtered = []
  }

  // 根据搜索关键词筛选
  if (searchKeyword.value.trim()) {
    const keyword = searchKeyword.value.trim().toLowerCase()
    filtered = filtered.filter(p => 
      p.trackingNumber.toLowerCase().includes(keyword) ||
      p.company.toLowerCase().includes(keyword)
    )
  }

  return filtered
})

// 加载包裹列表
async function loadParcels() {
  try {
    loading.value = true
    const response = await listMyParcels(currentPage.value, pageSize.value)
    allParcels.value = response.content
    totalElements.value = response.totalElements
  } catch (error) {
    console.error('加载包裹列表失败:', error)
    // TODO: 显示错误提示
  } finally {
    loading.value = false
  }
}

// 搜索包裹
async function handleSearch() {
  const keyword = searchKeyword.value.trim()
  if (!keyword) {
    // 如果搜索框为空，重新加载全部数据
    await loadParcels()
    return
  }

  try {
    loading.value = true
    // 使用后端搜索接口
    const parcel = await searchMyParcelByTrackingNumber(keyword)
    allParcels.value = [parcel]
  } catch (error) {
    console.error('搜索失败:', error)
    // 搜索失败时使用本地筛选
  } finally {
    loading.value = false
  }
}

// 签收快递
async function handleSign(pkg: Package) {
  try {
    await signParcel(pkg.id)
    // 签收成功后重新加载列表
    await loadParcels()
    // TODO: 显示成功提示
  } catch (error) {
    console.error('签收失败:', error)
    // TODO: 显示错误提示
  }
}

// 组件挂载时加载数据
onMounted(() => {
  loadParcels()
})
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

.loading-state {
  background: white;
  padding: 80px;
  border-radius: 12px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.loading-state p {
  font-size: 16px;
  color: #666;
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
