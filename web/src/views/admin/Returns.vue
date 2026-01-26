<template>
  <AdminLayout>
    <div class="returns-management">
      <div class="page-header">
        <h1>退货申请</h1>
        <p>处理包裹退货和异常申请</p>
      </div>

      <!-- 操作栏 -->
      <div class="action-bar">
        <div class="search-section">
          <input 
            type="text" 
            v-model="searchKeyword" 
            placeholder="搜索快递单号或申请人..."
            class="search-input"
            @keyup.enter="searchReturns"
          >
          <select v-model="filterStatus" class="filter-select" @change="loadReturnRequests">
            <option value="">全部状态</option>
            <option :value="0">待审核</option>
            <option :value="1">已同意</option>
            <option :value="2">已拒绝</option>
          </select>
          <button class="search-btn" @click="searchReturns">搜索</button>
          <button class="reset-btn" @click="resetFilters">重置</button>
        </div>
      </div>

      <!-- 统计卡片 -->
      <div class="stats-cards">
        <div class="stat-card" @click="filterStatus = 0; loadReturnRequests()">
          <div class="stat-icon pending">
            <img src="@/assets/icons/22.png" alt="待审核" class="icon-img" />
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.pending }}</div>
            <div class="stat-label">待审核</div>
          </div>
        </div>
        <div class="stat-card" @click="filterStatus = 1; loadReturnRequests()">
          <div class="stat-icon approved">
            <img src="@/assets/icons/20.png" alt="已同意" class="icon-img" />
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.approved }}</div>
            <div class="stat-label">已同意</div>
          </div>
        </div>
        <div class="stat-card" @click="filterStatus = 2; loadReturnRequests()">
          <div class="stat-icon rejected">
            <img src="@/assets/icons/21.png" alt="已拒绝" class="icon-img" />
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.rejected }}</div>
            <div class="stat-label">已拒绝</div>
          </div>
        </div>
        <div class="stat-card" @click="filterStatus = ''; loadReturnRequests()">
          <div class="stat-icon completed">
            <img src="@/assets/icons/17.png" alt="全部" class="icon-img" />
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.total }}</div>
            <div class="stat-label">全部</div>
          </div>
        </div>
      </div>

      <!-- 退货申请列表 -->
      <div class="returns-table">
        <table>
          <thead>
            <tr>
              <th>申请编号</th>
              <th>快递单号</th>
              <th>申请人</th>
              <th>联系电话</th>
              <th>退货原因</th>
              <th>申请时间</th>
              <th>状态</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading">
              <td colspan="8" class="empty-row">加载中...</td>
            </tr>
            <tr v-else-if="filteredReturns.length === 0">
              <td colspan="8" class="empty-row">暂无退货申请</td>
            </tr>
            <tr v-for="item in filteredReturns" :key="item.id">
              <td><span class="return-code">#{{ item.id }}</span></td>
              <td>{{ item.trackingNumber }}</td>
              <td>{{ item.username }}</td>
              <td>{{ item.phone }}</td>
              <td>
                <span class="reason-text">
                  {{ item.reason }}
                </span>
              </td>
              <td>{{ item.createTime }}</td>
              <td>
                <span :class="['status-badge', 'status-' + item.status]">
                  {{ getStatusLabel(item.status) }}
                </span>
              </td>
              <td>
                <div class="action-btns">
                  <button class="btn-view" @click="viewReturn(item)">详情</button>
                  <button 
                    v-if="item.status === 0" 
                    class="btn-approve" 
                    @click="approveReturn(item)"
                  >同意</button>
                  <button 
                    v-if="item.status === 0" 
                    class="btn-reject" 
                    @click="rejectReturn(item)"
                  >拒绝</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>

        <!-- 分页 -->
        <div class="pagination">
          <button class="page-btn" :disabled="currentPage === 0" @click="currentPage--; loadReturnRequests()">上一页</button>
          <span class="page-info">第 {{ currentPage + 1 }} / {{ totalPages || 1 }} 页，共 {{ totalElements }} 条</span>
          <button class="page-btn" :disabled="currentPage >= totalPages - 1" @click="currentPage++; loadReturnRequests()">下一页</button>
        </div>
      </div>

      <!-- 新建退货申请弹窗（管理员端不需要此功能，移除） -->

      <!-- 退货详情弹窗 -->
      <div class="modal" v-if="showReturnDetail" @click="showReturnDetail = false">
        <div class="modal-content detail-modal" @click.stop>
          <div class="modal-header">
            <h3>退货申请详情</h3>
            <button class="close-btn" @click="showReturnDetail = false">×</button>
          </div>
          <div class="modal-body">
            <div class="detail-section">
              <h4>申请信息</h4>
              <div class="detail-grid">
                <div class="detail-item">
                  <span class="label">申请编号：</span>
                  <span class="value">#{{ currentReturn?.id }}</span>
                </div>
                <div class="detail-item">
                  <span class="label">申请时间：</span>
                  <span class="value">{{ currentReturn?.createTime }}</span>
                </div>
                <div class="detail-item">
                  <span class="label">当前状态：</span>
                  <span :class="['status-badge', 'status-' + currentReturn?.status]">
                    {{ getStatusLabel(currentReturn?.status) }}
                  </span>
                </div>
              </div>
            </div>
            <div class="detail-section">
              <h4>包裹信息</h4>
              <div class="detail-grid">
                <div class="detail-item">
                  <span class="label">快递单号：</span>
                  <span class="value">{{ currentReturn?.trackingNumber }}</span>
                </div>
              </div>
            </div>
            <div class="detail-section">
              <h4>申请人信息</h4>
              <div class="detail-grid">
                <div class="detail-item">
                  <span class="label">申请人：</span>
                  <span class="value">{{ currentReturn?.username }}</span>
                </div>
                <div class="detail-item">
                  <span class="label">联系电话：</span>
                  <span class="value">{{ currentReturn?.phone }}</span>
                </div>
              </div>
            </div>
            <div class="detail-section">
              <h4>退货原因</h4>
              <div class="detail-item full-width">
                <p class="description">{{ currentReturn?.reason || '无' }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 处理弹窗 -->
      <div class="modal" v-if="showProcessModal" @click="showProcessModal = false">
        <div class="modal-content" @click.stop>
          <div class="modal-header">
            <h3>{{ processModalTitle }}</h3>
            <button class="close-btn" @click="showProcessModal = false">×</button>
          </div>
          <div class="modal-body">
            <div class="form-group">
              <label>处理备注</label>
              <textarea v-model="processNote" placeholder="请输入处理备注" rows="4"></textarea>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn-cancel" @click="showProcessModal = false">取消</button>
            <button class="btn-submit" @click="confirmProcess">确认</button>
          </div>
        </div>
      </div>
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import AdminLayout from '@/layouts/AdminLayout.vue'
import { useToast } from '@/composables/useToast'
import { useConfirm } from '@/composables/useConfirm'
import { useAutoRefresh } from '@/composables/useAutoRefresh'
import { 
  getReturnRequestList, 
  updateReturnRequestStatus,
  type ReturnRequest as ApiReturnRequest,
  type PageResponse
} from '@/api/admin/returnRequest'

const { success, warning, info, error } = useToast()
const { confirm } = useConfirm()

// 前端展示使用的接口
interface ReturnRequest {
  id: number
  username: string
  phone: string
  trackingNumber: string
  reason: string
  status: number // 0: 待审核, 1: 已同意, 2: 已拒绝
  createTime: string
  updateTime: string
}

const searchKeyword = ref('')
const filterStatus = ref<number | ''>('') // 0=待审核, 1=已同意, 2=已拒绝
const currentPage = ref(0) // 后端从0开始
const pageSize = ref(10)
const totalPages = ref(0)
const totalElements = ref(0)
const loading = ref(false)

const showReturnDetail = ref(false)
const showProcessModal = ref(false)
const processModalTitle = ref('')
const processNote = ref('')
const processAction = ref('')
const currentReturn = ref<ReturnRequest | null>(null)

const stats = reactive({
  pending: 0,
  approved: 0,
  rejected: 0,
  total: 0  // 新增总数统计
})

// 退货申请列表
const returnList = ref<ReturnRequest[]>([])

// 前端搜索过滤（在已加载的数据中搜索）
const filteredReturns = computed(() => {
  let result = returnList.value
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(r => 
      r.trackingNumber.toLowerCase().includes(keyword) ||
      r.username.toLowerCase().includes(keyword)
    )
  }
  return result
})

const getStatusLabel = (status?: number) => {
  const labels: Record<number, string> = {
    0: '待审核',
    1: '已同意',
    2: '已拒绝'
  }
  return labels[status ?? -1] || '未知'
}

// 加载退货申请列表
const loadReturnRequests = async () => {
  loading.value = true
  try {
    const statusFilter = filterStatus.value === '' ? undefined : filterStatus.value
    const response = await getReturnRequestList(currentPage.value, pageSize.value, statusFilter)
    
    returnList.value = response.content
    totalPages.value = response.totalPages
    totalElements.value = response.totalElements
    
    // 更新统计数据（独立获取，不受筛选影响）
    await updateStats()
  } catch (err: any) {
    // 处理特殊错误：数据库唯一性约束冲突
    if (err.message && err.message.includes('Query did not return a unique result')) {
      // 使用 confirm 弹窗显示错误
      await confirm({
        title: '数据库异常',
        message: '检测到数据库中存在重复记录（可能是用户名、快递单号等字段重复）。这是数据完整性问题，需要技术人员检查并清理 sys_user 表或 parcel 表中的重复数据。',
        confirmText: '我知道了',
        cancelText: '重试加载',
        type: 'warning'
      }).then((confirmed) => {
        if (!confirmed) {
          // 点击"重试加载"
          loadReturnRequests()
        }
      })
      console.error('数据库唯一性冲突:', err)
      // 清空列表，避免显示错误数据
      returnList.value = []
      totalPages.value = 0
      totalElements.value = 0
    } else {
      error(err.message || '加载退货申请列表失败')
      console.error('加载退货申请列表失败:', err)
    }
  } finally {
    loading.value = false
  }
}

// 更新统计数据（独立获取所有状态的数量，不受当前筛选影响）
const updateStats = async () => {
  try {
    // 分别获取各状态的数量
    const [pendingRes, approvedRes, rejectedRes, allRes] = await Promise.all([
      getReturnRequestList(0, 1, 0),  // 待审核
      getReturnRequestList(0, 1, 1),  // 已同意
      getReturnRequestList(0, 1, 2),  // 已拒绝
      getReturnRequestList(0, 1)      // 全部
    ])
    
    stats.pending = pendingRes.totalElements
    stats.approved = approvedRes.totalElements
    stats.rejected = rejectedRes.totalElements
    stats.total = allRes.totalElements
  } catch (err) {
    console.error('更新统计数据失败:', err)
  }
}

const searchReturns = () => {
  currentPage.value = 0
  loadReturnRequests()
}

const resetFilters = () => {
  searchKeyword.value = ''
  filterStatus.value = ''
  currentPage.value = 0
  loadReturnRequests()
}

const viewReturn = (item: ReturnRequest) => {
  currentReturn.value = item
  showReturnDetail.value = true
}

const approveReturn = (item: ReturnRequest) => {
  currentReturn.value = item
  processModalTitle.value = '同意退货申请'
  processAction.value = 'approve'
  processNote.value = ''
  showProcessModal.value = true
}

const rejectReturn = (item: ReturnRequest) => {
  currentReturn.value = item
  processModalTitle.value = '拒绝退货申请'
  processAction.value = 'reject'
  processNote.value = ''
  showProcessModal.value = true
}

const confirmProcess = async () => {
  if (!currentReturn.value) return
  
  loading.value = true
  try {
    const status = processAction.value === 'approve' ? 1 : 2
    await updateReturnRequestStatus(currentReturn.value.id, status)
    
    const actionLabels: Record<string, string> = {
      'approve': '同意',
      'reject': '拒绝'
    }
    
    success(`${actionLabels[processAction.value]}退货申请成功`)
    showProcessModal.value = false
    
    // 刷新列表
    await loadReturnRequests()
  } catch (err: any) {
    // 处理特殊错误
    if (err.message && err.message.includes('Query did not return a unique result')) {
      await confirm({
        title: '操作失败',
        message: '数据库中存在重复记录，无法完成审核操作。请联系技术人员检查并修复数据完整性问题（检查 sys_user 表、parcel 表等）。',
        confirmText: '我知道了',
        type: 'danger'
      })
      console.error('数据库唯一性冲突:', err)
    } else {
      error(err.message || '操作失败')
      console.error('审核退货申请失败:', err)
    }
  } finally {
    loading.value = false
  }
}

// 使用自动刷新功能
useAutoRefresh(loadReturnRequests)

// 页面加载时获取数据已由useAutoRefresh处理
</script>

<style scoped>
.returns-management {
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

.action-bar {
  background: white;
  padding: 20px;
  border-radius: 12px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
}

.search-section {
  display: flex;
  gap: 12px;
  flex: 1;
  flex-wrap: wrap;
}

.search-input {
  flex: 1;
  min-width: 200px;
  padding: 10px 16px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
}

.filter-select {
  padding: 10px 16px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
  background: white;
  min-width: 120px;
}

.search-btn, .reset-btn {
  padding: 10px 20px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.search-btn {
  background: #808080;
  color: white;
  border: none;
}

.search-btn:hover {
  background: #666666;
}

.reset-btn {
  background: white;
  color: #333;
  border: 1px solid #e0e0e0;
}

.btn-primary, .btn-secondary {
  padding: 10px 20px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.btn-primary {
  background: #808080;
  color: white;
  border: none;
}

.btn-primary:hover {
  background: #666666;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

.stat-card {
  background: white;
  padding: 24px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  display: flex;
  align-items: center;
  gap: 16px;
  cursor: pointer;
  transition: all 0.2s;
}

.stat-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  font-size: 28px;
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
}

.stat-icon .icon-img {
  width: 32px;
  height: 32px;
  object-fit: contain;
}

.stat-icon.pending,
.stat-icon.processing,
.stat-icon.approved,
.stat-icon.completed,
.stat-icon.rejected {
  background: #f5f5f5;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 13px;
  color: #999;
}

.returns-table {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
  min-width: 1000px;
}

thead {
  background: #f5f7fa;
}

th, td {
  padding: 16px;
  text-align: left;
  font-size: 14px;
}

th {
  font-weight: 600;
  color: #666;
}

td {
  border-top: 1px solid #f0f0f0;
  color: #333;
}

.empty-row {
  text-align: center;
  color: #999;
  padding: 60px;
}

.return-code {
  font-family: 'Courier New', monospace;
  font-weight: 600;
  color: #808080;
  font-size: 13px;
}

.reason-text {
  display: block;
  max-width: 200px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  color: #666;
  font-size: 13px;
}

.status-badge {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.status-badge.status-0 {
  background: #fff7e6;
  color: #fa8c16;
}

.status-badge.status-1 {
  background: #f6ffed;
  color: #52c41a;
}

.status-badge.status-2 {
  background: #fff1f0;
  color: #f5222d;
}

.action-btns {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.btn-view, .btn-approve, .btn-reject, .btn-process, .btn-complete {
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  transition: all 0.2s;
  white-space: nowrap;
}

.btn-view {
  background: white;
  color: #1890ff;
  border: 1px solid #1890ff;
}

.btn-view:hover {
  background: #1890ff;
  color: white;
}

.btn-approve {
  background: white;
  color: #52c41a;
  border: 1px solid #52c41a;
}

.btn-approve:hover {
  background: #52c41a;
  color: white;
}

.btn-reject {
  background: white;
  color: #f5222d;
  border: 1px solid #f5222d;
}

.btn-reject:hover {
  background: #f5222d;
  color: white;
}

.btn-process {
  background: white;
  color: #1890ff;
  border: 1px solid #1890ff;
}

.btn-process:hover {
  background: #1890ff;
  color: white;
}

.btn-complete {
  background: white;
  color: #808080;
  border: 1px solid #808080;
}

.btn-complete:hover {
  background: #808080;
  color: white;
}

.pagination {
  padding: 20px;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  border-top: 1px solid #f0f0f0;
}

.page-btn {
  padding: 8px 20px;
  background: white;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  cursor: pointer;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  color: #666;
  font-size: 14px;
}

/* Modal Styles */
.modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 16px;
  width: 550px;
  max-width: 90vw;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-content.detail-modal {
  width: 650px;
}

.modal-header {
  padding: 20px 24px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
}

.close-btn {
  width: 32px;
  height: 32px;
  border: none;
  background: #f5f5f5;
  border-radius: 50%;
  font-size: 20px;
  cursor: pointer;
}

.modal-body {
  padding: 24px;
}

.modal-footer {
  padding: 16px 24px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  font-size: 14px;
  color: #333;
  margin-bottom: 8px;
  font-weight: 500;
}

.form-group input,
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
}

.form-group textarea {
  resize: vertical;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.radio-group {
  display: flex;
  gap: 24px;
}

.radio-item {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-size: 14px;
}

.radio-item input {
  width: auto;
}

.btn-cancel, .btn-submit {
  padding: 10px 24px;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-cancel {
  background: white;
  color: #666;
  border: 1px solid #e0e0e0;
}

.btn-submit {
  background: #808080;
  color: white;
  border: none;
}

.btn-submit:hover {
  background: #666666;
}

/* Detail Styles */
.detail-section {
  margin-bottom: 24px;
}

.detail-section h4 {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.detail-item {
  font-size: 14px;
}

.detail-item .label {
  color: #999;
  margin-right: 8px;
}

.detail-item .value {
  color: #333;
}

.detail-item.full-width {
  grid-column: 1 / -1;
}

.description {
  margin-top: 8px;
  padding: 12px;
  background: #f9f9f9;
  border-radius: 8px;
  font-size: 14px;
  color: #333;
  line-height: 1.6;
}

/* Timeline Styles */
.timeline-section h4 {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 16px;
}

.timeline {
  position: relative;
  padding-left: 24px;
}

.timeline-item {
  position: relative;
  padding-bottom: 20px;
}

.timeline-item:last-child {
  padding-bottom: 0;
}

.timeline-dot {
  position: absolute;
  left: -24px;
  top: 4px;
  width: 12px;
  height: 12px;
  background: #e0e0e0;
  border-radius: 50%;
}

.timeline-dot.active {
  background: #808080;
}

.timeline-item::before {
  content: '';
  position: absolute;
  left: -19px;
  top: 16px;
  bottom: 0;
  width: 2px;
  background: #e0e0e0;
}

.timeline-item:last-child::before {
  display: none;
}

.timeline-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.timeline-time {
  font-size: 12px;
  color: #999;
}

.timeline-text {
  font-size: 14px;
  color: #333;
}

@media (max-width: 1200px) {
  .stats-cards {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .stats-cards {
    grid-template-columns: 1fr;
  }
  .form-row {
    grid-template-columns: 1fr;
  }
  .detail-grid {
    grid-template-columns: 1fr;
  }
}
</style>
