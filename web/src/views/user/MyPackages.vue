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

      <!-- 分页 -->
      <div v-if="!loading && packages.length > 0" class="pagination">
        <button 
          class="pagination-btn" 
          :disabled="currentPage === 0"
          @click="handlePageChange(currentPage - 1)"
        >
          上一页
        </button>
        <div class="pagination-info">
          <span>第 {{ currentPage + 1 }} 页 / 共 {{ totalPages }} 页</span>
          <span class="total-count">（共 {{ totalElements }} 条）</span>
        </div>
        <button 
          class="pagination-btn" 
          :disabled="currentPage >= totalPages - 1"
          @click="handlePageChange(currentPage + 1)"
        >
          下一页
        </button>
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
              <div v-if="returnRequestedTrackingNumbers.has(selectedPackage.trackingNumber)" class="detail-row">
                <span class="detail-label">退货状态：</span>
                <span class="detail-value return-pending-badge">待审核</span>
              </div>
              <div v-else-if="returnRejectedTrackingNumbers.has(selectedPackage.trackingNumber)" class="detail-row">
                <span class="detail-label">退货状态：</span>
                <span class="detail-value return-rejected-badge">已拒绝</span>
              </div>
              <div v-else-if="selectedPackage.rawData.isReturned === 1" class="detail-row">
                <span class="detail-label">退货状态：</span>
                <span class="detail-value returned-badge">已同意退货</span>
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

            <div class="detail-section">
              <h3>物流轨迹</h3>
              <div v-if="loadingRoutes" class="routes-loading">
                <p>加载中...</p>
              </div>
              <div v-else-if="parcelRoutes.length === 0" class="routes-empty">
                <p>暂无物流轨迹信息</p>
              </div>
              <div v-else class="routes-timeline">
                <!-- 已送达节点：当最后一个物流节点标记为已送达时显示 -->
                <div 
                  v-if="isParcelDelivered"
                  class="timeline-item active"
                >
                  <div class="timeline-dot"></div>
                  <div class="timeline-content">
                    <div class="timeline-header">
                      <span class="timeline-station">{{ lastRouteStation }}</span>
                      <span class="timeline-time">{{ formatTime(lastRouteTime) }}</span>
                    </div>
                    <p class="timeline-desc">已送达</p>
                  </div>
                </div>
                
                <!-- 常规物流节点 -->
                <div 
                  v-for="(route, index) in parcelRoutes" 
                  :key="route.id"
                  class="timeline-item"
                  :class="{ active: index === 0 && !isParcelDelivered }"
                >
                  <div class="timeline-dot"></div>
                  <div class="timeline-content">
                    <div class="timeline-header">
                      <span class="timeline-station">{{ route.currentStation }}</span>
                      <span class="timeline-time">{{ formatTime(route.createTime) }}</span>
                    </div>
                    <p class="timeline-desc">
                      {{ route.nextStation ? `发往 ${route.nextStation}` : '到达目的地' }}
                    </p>
                  </div>
                </div>
              </div>
            </div>

            <div class="detail-section" v-if="selectedPackage.rawData.isSigned === 0">
              <h3>取件提示</h3>
              <div class="pickup-notice">
                <p>如长时间未取件可能会被退回</p>
              </div>
            </div>
          </div>
          <div class="dialog-footer">
            <button class="dialog-btn cancel" @click="closeDetailDialog">关闭</button>
            <button 
              v-if="selectedPackage?.canPickup && 
                    !returnRequestedTrackingNumbers.has(selectedPackage.trackingNumber)"
              class="dialog-btn confirm" 
              @click="handleSignFromDialog"
              :disabled="signing"
            >
              {{ signing ? '确认中...' : '确认收货' }}
            </button>
            <button 
              v-if="selectedPackage?.rawData.isSigned === 0 && 
                    selectedPackage?.rawData.isReturned !== 1 && 
                    !returnRequestedTrackingNumbers.has(selectedPackage.trackingNumber)"
              class="dialog-btn return-request" 
              @click="showReturnRequestDialog"
              :disabled="signing"
            >
              申请退货
            </button>
          </div>
        </div>
      </div>

      <!-- 退货申请弹窗 -->
      <div v-if="showReturnDialog" class="dialog-overlay" @click="closeReturnDialog">
        <div class="dialog-content" @click.stop>
          <div class="dialog-header">
            <h2>申请退货</h2>
            <button class="close-btn" @click="closeReturnDialog">×</button>
          </div>
          <div class="dialog-body">
            <div class="return-form">
              <div class="form-group">
                <label>快递单号</label>
                <input 
                  type="text" 
                  v-model="returnForm.trackingNumber" 
                  disabled
                  class="form-input"
                />
              </div>
              <div class="form-group">
                <label>退货原因 <span class="required">*</span></label>
                <textarea 
                  v-model="returnForm.reason" 
                  placeholder="请详细说明退货原因（如：商品损坏、质量问题、错发货物等）"
                  rows="5"
                  class="form-textarea"
                  maxlength="500"
                ></textarea>
                <div class="char-count">{{ returnForm.reason.length }}/500</div>
              </div>
              <div class="form-group">
                <label>联系人（可选）</label>
                <input 
                  type="text" 
                  v-model="returnForm.username" 
                  placeholder="默认使用当前用户名"
                  class="form-input"
                />
              </div>
              <div class="form-group">
                <label>联系电话（可选）</label>
                <input 
                  type="text" 
                  v-model="returnForm.phone" 
                  placeholder="默认使用当前用户电话"
                  class="form-input"
                />
              </div>
              <div class="form-notice">
                <p>温馨提示：</p>
                <ul>
                  <li>请确保包裹完好无损</li>
                  <li>退货申请提交后，管理员将在 1-2 个工作日内审核</li>
                  <li>审核通过后，请按照指示办理退货手续</li>
                </ul>
              </div>
            </div>
          </div>
          <div class="dialog-footer">
            <button class="dialog-btn cancel" @click="closeReturnDialog" :disabled="submittingReturn">取消</button>
            <button 
              class="dialog-btn confirm" 
              @click="submitReturnRequest"
              :disabled="submittingReturn || !returnForm.reason.trim()"
            >
              {{ submittingReturn ? '提交中...' : '提交申请' }}
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
import { useAutoRefresh } from '@/composables/useAutoRefresh'
import { 
  listMyParcels, 
  searchMyParcelByTrackingNumber, 
  signParcel, 
  listSignedParcels, 
  listUnsignedParcels,
  getParcelRoutes,
  type Parcel,
  type ParcelRoute
} from '@/api/user/parcel'
import { 
  createReturnRequest,
  getMyReturnRequests,
  type CreateReturnRequestRequest,
  type ReturnRequest
} from '@/api/user/returnRequest'
import { useToast } from '@/composables/useToast'

const { success, error: showError } = useToast()

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
const loadingRoutes = ref(false)
const parcelRoutes = ref<ParcelRoute[]>([])
const showReturnDialog = ref(false)
const submittingReturn = ref(false)
const returnForm = ref<CreateReturnRequestRequest>({
  trackingNumber: '',
  reason: '',
  username: '',
  phone: ''
})

// 存储已申请退货的快递单号集合
const returnRequestedTrackingNumbers = ref<Set<string>>(new Set())
// 存储被拒绝退货的快递单号集合
const returnRejectedTrackingNumbers = ref<Set<string>>(new Set())

// 存储各个标签的真实计数
const tabCounts = ref({
  all: 0,
  pending: 0,
  picked: 0
})

// 监听activeTab变化，自动加载对应数据
watch(activeTab, () => {
  // 切换标签时清空搜索关键词、重置页码并重新加载数据
  searchKeyword.value = ''
  currentPage.value = 0
  loadParcels()
})

// 计算标签计数（使用存储的真实计数）
const tabs = computed<Tab[]>(() => {
  return [
    { label: '全部', value: 'all', count: tabCounts.value.all },
    { label: '待取件', value: 'pending', count: tabCounts.value.pending },
    { label: '已取件', value: 'picked', count: tabCounts.value.picked }
  ]
})

// 将后端 Parcel 转换为前端 Package
function transformParcel(parcel: Parcel): Package {
  // 状态映射
  let status = '未知'
  let statusClass = ''
  
  // 优先检查是否有待审核的退货申请
  if (returnRequestedTrackingNumbers.value.has(parcel.trackingNumber)) {
    status = '待审核'
    statusClass = 'pending-review'
  }
  // 检查是否被拒绝退货（拒绝后恢复到原始状态）
  else if (returnRejectedTrackingNumbers.value.has(parcel.trackingNumber)) {
    status = '退货失败'
    statusClass = 'return-rejected'
  }
  // 检查是否已退货（只有 isReturned=1 且没有被拒绝的才算已退货）
  else if (parcel.isReturned === 1) {
    status = '已退货'
    statusClass = 'returned'
  }
  // 根据签收状态判断
  else if (parcel.isSigned === 1) {
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
  // 直接返回转换后的包裹列表，不进行本地筛选
  // 搜索功能由后端接口处理，点击搜索按钮时调用 handleSearch
  return allParcels.value.map(transformParcel)
})

// 计算总页数
const totalPages = computed(() => {
  return Math.ceil(totalElements.value / pageSize.value)
})

// 加载包裹列表（根据activeTab调用不同的后端接口）
async function loadParcels() {
  try {
    loading.value = true
    let response
    
    // 根据当前激活标签调用不同的API
    switch (activeTab.value) {
      case 'pending':
        // 待取件：未签收的包裹
        response = await listUnsignedParcels(currentPage.value, pageSize.value)
        break
      case 'picked':
        // 已取件：已签收的包裹
        response = await listSignedParcels(currentPage.value, pageSize.value)
        break
      case 'all':
      default:
        response = await listMyParcels(currentPage.value, pageSize.value)
        break
    }
    
    allParcels.value = response.content
    totalElements.value = response.totalElements
    
    // 更新当前标签的计数
    tabCounts.value[activeTab.value as keyof typeof tabCounts.value] = response.totalElements
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
    // 如果搜索框为空，重置页码并重新加载当前标签的数据
    currentPage.value = 0
    await loadParcels()
    return
  }

  try {
    loading.value = true
    // 使用后端搜索接口
    const parcel = await searchMyParcelByTrackingNumber(keyword)
    allParcels.value = [parcel]
    totalElements.value = 1
    currentPage.value = 0
  } catch (error) {
    console.error('搜索失败:', error)
    allParcels.value = []
    totalElements.value = 0
  } finally {
    loading.value = false
  }
}

// 处理页码变化
async function handlePageChange(page: number) {
  if (page < 0 || page >= totalPages.value) return
  currentPage.value = page
  await loadParcels()
  // 滚动到顶部
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// 显示包裹详情
async function showPackageDetail(pkg: Package) {
  selectedPackage.value = pkg
  showDetailDialog.value = true
  
  // 加载物流轨迹
  loadingRoutes.value = true
  parcelRoutes.value = []
  try {
    const routes = await getParcelRoutes(pkg.trackingNumber)
    // 按createTime倒序排序（最新时间在前）
    if (routes && routes.length > 0) {
      parcelRoutes.value = [...routes].sort((a, b) => {
        return new Date(b.createTime).getTime() - new Date(a.createTime).getTime()
      })
    } else {
      parcelRoutes.value = []
    }
  } catch (err: any) {
    console.error('加载物流轨迹失败:', err)
    parcelRoutes.value = []
  } finally {
    loadingRoutes.value = false
  }
}

// 关闭详情对话框
function closeDetailDialog() {
  showDetailDialog.value = false
  selectedPackage.value = null
  signing.value = false
  parcelRoutes.value = []
  loadingRoutes.value = false
}

// 格式化时间
function formatTime(time: string): string {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 判断快递是否已送达（最后一个物流节点的 isDelivered === 1）
const isParcelDelivered = computed(() => {
  if (parcelRoutes.value.length === 0) return false
  // 因为已经按时间倒序排序，第一个就是最新的
  const latestRoute = parcelRoutes.value[0]
  return latestRoute?.isDelivered === 1
})

// 获取最后一个物流节点的站点名称
const lastRouteStation = computed(() => {
  if (parcelRoutes.value.length === 0) return ''
  const latestRoute = parcelRoutes.value[0]
  return latestRoute?.currentStation || ''
})

// 获取最后一个物流节点的时间
const lastRouteTime = computed(() => {
  if (parcelRoutes.value.length === 0) return ''
  const latestRoute = parcelRoutes.value[0]
  return latestRoute?.createTime || ''
})

// 显示退货申请对话框
function showReturnRequestDialog() {
  if (!selectedPackage.value) return
  
  // 初始化表单
  returnForm.value = {
    trackingNumber: selectedPackage.value.trackingNumber,
    reason: '',
    username: '',
    phone: ''
  }
  
  showReturnDialog.value = true
}

// 关闭退货申请对话框
function closeReturnDialog() {
  showReturnDialog.value = false
  returnForm.value = {
    trackingNumber: '',
    reason: '',
    username: '',
    phone: ''
  }
}

// 提交退货申请
async function submitReturnRequest() {
  if (!returnForm.value.reason.trim()) {
    showError('请填写退货原因')
    return
  }
  
  try {
    submittingReturn.value = true
    await createReturnRequest(returnForm.value)
    success('退货申请提交成功，请等待管理员审核')
    
    // 关闭退货对话框和详情对话框
    closeReturnDialog()
    closeDetailDialog()
    
    // 重新加载数据
    await loadAllTabCounts()
    await loadParcels()
  } catch (err: any) {
    console.error('提交退货申请失败:', err)
    showError(err.message || '提交失败，请重试')
  } finally {
    submittingReturn.value = false
  }
}
async function handleSignFromDialog() {
  if (!selectedPackage.value) return
  
  try {
    signing.value = true
    await signParcel(selectedPackage.value.id)
    // 签收成功后重新加载标签计数和列表
    await loadAllTabCounts()
    await loadParcels()
    // 关闭对话框
    closeDetailDialog()
    success('签收成功！')
  } catch (error) {
    console.error('签收失败:', error)
    showError('签收失败，请重试')
  } finally {
    signing.value = false
  }
}

// 加载所有标签的计数
async function loadAllTabCounts() {
  try {
    // 并行请求所有标签的数据以获取计数
    const [allResponse, unsignedResponse, signedResponse] = await Promise.all([
      listMyParcels(0, 1),
      listUnsignedParcels(0, 1),
      listSignedParcels(0, 1)
    ])
    
    tabCounts.value.all = allResponse.totalElements
    tabCounts.value.pending = unsignedResponse.totalElements
    tabCounts.value.picked = signedResponse.totalElements
    
    // 获取退货申请数据以更新快递单号集合
    const returnRequests = await getMyReturnRequests(0, 9999)
    
    // 更新已申请退货的快递单号集合
    returnRequestedTrackingNumbers.value = new Set(
      returnRequests.content
        .filter(req => req.status === 0) // 待审核
        .map(req => req.trackingNumber)
    )
    
    // 更新被拒绝退货的快递单号集合
    returnRejectedTrackingNumbers.value = new Set(
      returnRequests.content
        .filter(req => req.status === 2) // 已拒绝
        .map(req => req.trackingNumber)
    )
  } catch (error) {
    console.error('加载标签计数失败:', error)
  }
}

// 使用自动刷新功能
useAutoRefresh(async () => {
  await loadAllTabCounts()
  await loadParcels()
})

// 组件挂载时加载数据已由useAutoRefresh处理
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

.package-status.returned {
  background: #e6f7ff;
  color: #1890ff;
}

.package-status.pending-review {
  background: #f0f5ff;
  color: #597ef7;
}

.package-status.return-rejected {
  background: #fff1f0;
  color: #cf1322;
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
  margin-bottom: 20px;
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

.detail-status.returned {
  background: #e6f7ff;
  color: #1890ff;
}

.returned-badge {
  display: inline-block;
  color: #1890ff;
  font-size: 14px;
  font-weight: 600;
}

.return-pending-badge {
  display: inline-block;
  color: #597ef7;
  font-size: 14px;
  font-weight: 600;
}

.return-rejected-badge {
  display: inline-block;
  color: #cf1322;
  font-size: 14px;
  font-weight: 600;
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

.dialog-btn.return-request {
  background: #fa8c16;
  color: white;
}

.dialog-btn.return-request:disabled {
  background: #ccc;
  cursor: not-allowed;
}

/* 退货表单样式 */
.return-form {
  width: 100%;
}

.form-group {
  margin-bottom: 20px;
}

.form-group:last-child {
  margin-bottom: 0;
}

.form-group label {
  display: block;
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.form-group label .required {
  color: #f5222d;
  margin-left: 2px;
}

.form-input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 8px;
  font-size: 14px;
  transition: all 0.2s;
}

.form-input:focus {
  outline: none;
  border-color: #666;
  box-shadow: 0 0 0 2px rgba(102, 102, 102, 0.1);
}

.form-input:disabled {
  background: #f5f5f5;
  color: #999;
  cursor: not-allowed;
}

.form-textarea {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 8px;
  font-size: 14px;
  font-family: inherit;
  resize: vertical;
  transition: all 0.2s;
}

.form-textarea:focus {
  outline: none;
  border-color: #666;
  box-shadow: 0 0 0 2px rgba(102, 102, 102, 0.1);
}

.char-count {
  text-align: right;
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.form-notice {
  background: #E8EBE4;
  border: 1px solid #B8C5B0;
  border-radius: 8px;
  padding: 16px;
  margin-top: 20px;
}

.form-notice p {
  margin: 0 0 8px 0;
  font-size: 14px;
  font-weight: 600;
  color: #5C6B5E;
}

.form-notice ul {
  margin: 0;
  padding-left: 20px;
}

.form-notice li {
  font-size: 13px;
  color: #6B7869;
  line-height: 1.8;
  margin-bottom: 4px;
}

.form-notice li:last-child {
  margin-bottom: 0;
}

/* 分页样式 */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  margin-top: 24px;
  padding: 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.pagination-btn {
  padding: 10px 24px;
  background: #666;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s;
}

.pagination-btn:hover:not(:disabled) {
  background: #555;
}

.pagination-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
  opacity: 0.6;
}

/* 物流轨迹样式 */
.routes-timeline {
  position: relative;
  padding-left: 20px;
  margin-top: 16px;
  padding-bottom: 0;
}

.routes-timeline::before {
  content: '';
  position: absolute;
  left: 8px;
  top: 0;
  bottom: 0;
  width: 2px;
  background: #e0e0e0;
}

.timeline-item {
  position: relative;
  margin-bottom: 20px;
  padding-bottom: 0;
}

.timeline-item:last-child {
  margin-bottom: 0;
  padding-bottom: 0;
}

.timeline-item:last-child::after {
  display: none;
}

.timeline-dot {
  position: absolute;
  left: -27px;
  top: 4px;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #666;
  z-index: 1;
}

.timeline-item.active .timeline-dot {
  background: #1890ff;
  box-shadow: 0 0 0 3px rgba(24, 144, 255, 0.2);
}

.timeline-content {
  background: #f8f9fa;
  padding: 12px;
  border-radius: 8px;
}

.timeline-item.active .timeline-content {
  background: #e6f7ff;
}

.timeline-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.timeline-station {
  font-weight: 600;
  color: #333;
}

.timeline-time {
  font-size: 12px;
  color: #999;
}

.timeline-desc {
  margin: 0;
  font-size: 13px;
  color: #666;
}

.routes-loading,
.routes-empty {
  padding: 16px;
  text-align: center;
  color: #999;
  font-size: 14px;
}

.pagination-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: #666;
}

.total-count {
  font-size: 12px;
  color: #999;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .page-header h1 {
    font-size: 24px;
  }
  
  .page-header p {
    font-size: 14px;
  }
  
  .filter-section {
    padding: 16px;
  }
  
  .filter-tabs {
    flex-wrap: wrap;
    gap: 8px;
  }
  
  .tab-btn {
    padding: 8px 14px;
    font-size: 13px;
  }
  
  .search-box {
    flex-direction: column;
  }
  
  .search-box input {
    width: 100%;
  }
  
  .search-btn {
    width: 100%;
  }
  
  .package-card {
    padding: 16px;
  }
  
  .package-body {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .package-actions {
    width: 100%;
  }
  
  .action-btn {
    flex: 1;
    text-align: center;
  }
  
  .dialog-content {
    width: 95%;
    max-height: 90vh;
  }
  
  .dialog-header {
    padding: 16px;
  }
  
  .dialog-body {
    padding: 16px;
  }
  
  .dialog-footer {
    padding: 12px 16px;
  }
  
  .detail-label {
    min-width: 80px;
  }
  
  .form-group {
    margin-bottom: 16px;
  }
  
  .dialog-footer {
    flex-wrap: wrap;
  }
  
  .dialog-btn {
    flex: 1;
    min-width: 100px;
  }
}

@media (max-width: 480px) {
  .page-header h1 {
    font-size: 22px;
  }
  
  .filter-tabs {
    gap: 6px;
  }
  
  .tab-btn {
    padding: 6px 10px;
    font-size: 12px;
  }
  
  .tab-count {
    padding: 2px 6px;
    font-size: 11px;
  }
  
  .package-company {
    font-size: 15px;
  }
  
  .info-row {
    font-size: 13px;
  }
  
  .empty-state,
  .loading-state {
    padding: 40px 20px;
  }
  
  .pagination {
    flex-direction: column;
    gap: 12px;
    padding: 16px;
  }
  
  .pagination-btn {
    width: 100%;
    padding: 12px;
  }
}
</style>
