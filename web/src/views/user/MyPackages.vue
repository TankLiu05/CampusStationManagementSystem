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
        
        <div v-else class="package-card" v-for="pkg in packages" :key="pkg.id" @click="showPackageDetail(pkg)">
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
            <div class="package-actions" @click.stop>
              <button 
                class="action-btn view" 
                @click="showPackageDetail(pkg)"
              >
                查看详情
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- 包裹详情对话框 -->
      <div v-if="showDetailDialog" class="dialog-overlay" @click="closeDetailDialog">
        <div class="dialog-content" @click.stop>
          <div class="dialog-header">
            <h2>包裹详情</h2>
            <button class="close-btn" @click="closeDetailDialog">×</button>
          </div>
          <div class="dialog-body" v-if="selectedPackage">
            <div class="detail-section">
              <h3>基本信息</h3>
              <div class="detail-row">
                <span class="detail-label">快递公司：</span>
                <span class="detail-value">{{ selectedPackage.company }}</span>
              </div>
              <div class="detail-row">
                <span class="detail-label">快递单号：</span>
                <span class="detail-value">{{ selectedPackage.trackingNumber }}</span>
              </div>
              <div class="detail-row">
                <span class="detail-label">当前状态：</span>
                <span :class="['detail-status', selectedPackage.statusClass]">{{ selectedPackage.status }}</span>
              </div>
            </div>

            <div class="detail-section">
              <h3>收件信息</h3>
              <div class="detail-row" v-if="selectedPackage.rawData.receiverName">
                <span class="detail-label">收件人：</span>
                <span class="detail-value">{{ selectedPackage.rawData.receiverName }}</span>
              </div>
              <div class="detail-row" v-if="selectedPackage.rawData.receiverPhone">
                <span class="detail-label">联系电话：</span>
                <span class="detail-value">{{ selectedPackage.rawData.receiverPhone }}</span>
              </div>
              <div class="detail-row">
                <span class="detail-label">存放位置：</span>
                <span class="detail-value">{{ selectedPackage.location }}</span>
              </div>
              <div class="detail-row" v-if="selectedPackage.rawData.pickupCode">
                <span class="detail-label">取件码：</span>
                <span class="detail-value pickup-code">{{ selectedPackage.rawData.pickupCode }}</span>
              </div>
            </div>

            <div class="detail-section">
              <h3>时间信息</h3>
              <div class="detail-row">
                <span class="detail-label">到达时间：</span>
                <span class="detail-value">{{ selectedPackage.arrivalTime }}</span>
              </div>
              <div class="detail-row" v-if="selectedPackage.rawData.updateTime">
                <span class="detail-label">更新时间：</span>
                <span class="detail-value">{{ new Date(selectedPackage.rawData.updateTime).toLocaleString('zh-CN') }}</span>
              </div>
            </div>

            <div class="detail-section" v-if="selectedPackage.rawData.isSigned === 0">
              <h3>取件提示</h3>
              <div class="pickup-notice">
                <p>请携带有效证件到校园驿站取件</p>
                <p>如长时间未取件可能会被退回</p>
              </div>
            </div>
          </div>
          <div class="dialog-footer">
            <button class="dialog-btn cancel" @click="closeDetailDialog">关闭</button>
            <button 
              v-if="selectedPackage?.canPickup" 
              class="dialog-btn confirm" 
              @click="handleSignFromDialog"
              :disabled="signing"
            >
              {{ signing ? '确认中...' : '确认收货' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </UserLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import UserLayout from '@/layouts/UserLayout.vue'
import { 
  listMyParcels, 
  searchMyParcelByTrackingNumber, 
  signParcel, 
  listSignedParcels, 
  listUnsignedParcels,
  type Parcel 
} from '@/api/user/parcel'

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
const showDetailDialog = ref(false)
const selectedPackage = ref<Package | null>(null)
const signing = ref(false)

// 监听activeTab变化，自动加载对应数据
watch(activeTab, () => {
  if (!searchKeyword.value.trim()) {
    loadParcels()
  }
})

// 计算标签计数（使用totalElements而非本地数据）
const tabs = computed<Tab[]>(() => {
  const allCount = activeTab.value === 'all' ? totalElements.value : allParcels.value.length
  const pendingCount = activeTab.value === 'pending' ? totalElements.value : allParcels.value.filter(p => p.isSigned === 0).length
  const pickedCount = activeTab.value === 'picked' ? totalElements.value : allParcels.value.filter(p => p.isSigned === 1).length
  
  return [
    { label: '全部', value: 'all', count: allCount },
    { label: '待取件', value: 'pending', count: pendingCount },
    { label: '已取件', value: 'picked', count: pickedCount },
    { label: '已超期', value: 'overdue', count: 0 } // TODO: 需要根据实际业务逻辑判断超期
  ]
})

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
    location: parcel.location || '校园驿站',
    arrivalTime: new Date(parcel.createTime).toLocaleString('zh-CN'),
    canPickup: parcel.isSigned === 0 && parcel.status === 2 && !!(parcel.location && parcel.pickupCode),
    rawData: parcel
  }
}

// 根据当前激活标签和搜索关键词过滤包裹
const packages = computed<Package[]>(() => {
  let filtered = allParcels.value.map(transformParcel)

  // 如果有搜索关键词，进行本地筛选
  if (searchKeyword.value.trim()) {
    const keyword = searchKeyword.value.trim().toLowerCase()
    filtered = filtered.filter(p => 
      p.trackingNumber.toLowerCase().includes(keyword) ||
      p.company.toLowerCase().includes(keyword)
    )
  }
  // 注意：不需要根据activeTab进行本地筛选，因为后端已经返回了过滤后的数据

  return filtered
})

// 加载包裹列表（根据activeTab调用不同的后端接口）
async function loadParcels() {
  try {
    loading.value = true
    let response
    
    // 根据当前激活标签调用不同的API
    switch (activeTab.value) {
      case 'pending':
        response = await listUnsignedParcels(currentPage.value, pageSize.value)
        break
      case 'picked':
        response = await listSignedParcels(currentPage.value, pageSize.value)
        break
      case 'overdue':
        // TODO: 如果后端有超期快递接口，在此调用
        allParcels.value = []
        totalElements.value = 0
        return
      case 'all':
      default:
        response = await listMyParcels(currentPage.value, pageSize.value)
        break
    }
    
    allParcels.value = response.content
    totalElements.value = response.totalElements
  } catch (error) {
    console.error('加载包裹列表失败:', error)
    allParcels.value = []
    totalElements.value = 0
  } finally {
    loading.value = false
  }
}

// 搜索包裹
async function handleSearch() {
  const keyword = searchKeyword.value.trim()
  if (!keyword) {
    // 如果搜索框为空，重新加载当前标签的数据
    await loadParcels()
    return
  }

  try {
    loading.value = true
    // 使用后端搜索接口
    const parcel = await searchMyParcelByTrackingNumber(keyword)
    allParcels.value = [parcel]
    totalElements.value = 1
  } catch (error) {
    console.error('搜索失败:', error)
    allParcels.value = []
    totalElements.value = 0
  } finally {
    loading.value = false
  }
}

// 显示包裹详情
function showPackageDetail(pkg: Package) {
  selectedPackage.value = pkg
  showDetailDialog.value = true
}

// 关闭详情对话框
function closeDetailDialog() {
  showDetailDialog.value = false
  selectedPackage.value = null
  signing.value = false
}

// 从详情对话框签收
async function handleSignFromDialog() {
  if (!selectedPackage.value) return
  
  try {
    signing.value = true
    await signParcel(selectedPackage.value.id)
    // 签收成功后重新加载列表
    await loadParcels()
    // 关闭对话框
    closeDetailDialog()
    alert('签收成功！')
  } catch (error) {
    console.error('签收失败:', error)
    alert('签收失败，请重试')
  } finally {
    signing.value = false
  }
}

// 组件挂载时加载数据
onMounted(() => {
  loadParcels()
})
</script>

<style scoped>
.my-packages {
  padding: 0;
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
  background: #666;
  color: white;
  border-color: #666;
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
  background: #666;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
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
  cursor: pointer;
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
}

.action-btn.primary {
  background: #666;
  color: white;
  border-color: #666;
}

.action-btn.view {
  background: white;
  color: #666;
  border-color: #666;
}

/* 对话框样式 */
.dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  animation: fadeIn 0.2s;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.dialog-content {
  background: white;
  border-radius: 16px;
  width: 90%;
  max-width: 600px;
  max-height: 80vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  animation: slideUp 0.3s;
}

@keyframes slideUp {
  from {
    transform: translateY(20px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px;
  border-bottom: 1px solid #f0f0f0;
}

.dialog-header h2 {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
}

.close-btn {
  width: 32px;
  height: 32px;
  border: none;
  background: #f5f5f5;
  border-radius: 50%;
  cursor: pointer;
  font-size: 24px;
  color: #666;
  display: flex;
  align-items: center;
  justify-content: center;
}

.dialog-body {
  padding: 24px;
  overflow-y: auto;
  flex: 1;
}

.detail-section {
  margin-bottom: 24px;
}

.detail-section:last-child {
  margin-bottom: 0;
}

.detail-section h3 {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 2px solid #f0f0f0;
}

.detail-row {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  font-size: 14px;
}

.detail-row:last-child {
  margin-bottom: 0;
}

.detail-label {
  color: #999;
  min-width: 100px;
  font-weight: 500;
}

.detail-value {
  color: #333;
  font-weight: 500;
  flex: 1;
}

.pickup-code {
  font-size: 18px;
  font-weight: 700;
  color: #666;
  letter-spacing: 2px;
  font-family: 'Courier New', monospace;
}

.detail-status {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 13px;
  font-weight: 500;
}

.detail-status.pending {
  background: #fff7e6;
  color: #fa8c16;
}

.detail-status.picked {
  background: #f6ffed;
  color: #52c41a;
}

.detail-status.overdue {
  background: #fff1f0;
  color: #f5222d;
}

.pickup-notice {
  background: #f5f5f5;
  border: 1px solid #d9d9d9;
  border-radius: 8px;
  padding: 16px;
}

.pickup-notice p {
  margin: 8px 0;
  font-size: 14px;
  color: #666;
  line-height: 1.6;
}

.pickup-notice p:first-child {
  margin-top: 0;
}

.pickup-notice p:last-child {
  margin-bottom: 0;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 24px;
  border-top: 1px solid #f0f0f0;
}

.dialog-btn {
  padding: 10px 24px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
}

.dialog-btn.cancel {
  background: #f5f5f5;
  color: #666;
}

.dialog-btn.confirm {
  background: #666;
  color: white;
}

.dialog-btn.confirm:disabled {
  background: #ccc;
  cursor: not-allowed;
}
</style>
