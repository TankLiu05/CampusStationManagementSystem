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
          <select v-model="filterStatus" class="filter-select">
            <option value="">全部状态</option>
            <option value="pending">待处理</option>
            <option value="processing">处理中</option>
            <option value="approved">已批准</option>
            <option value="rejected">已拒绝</option>
            <option value="completed">已完成</option>
          </select>
          <button class="search-btn" @click="searchReturns">搜索</button>
          <button class="reset-btn" @click="resetFilters">重置</button>
        </div>
        <div class="action-buttons">
          <button class="btn-primary" @click="showCreateReturn = true">新建退货申请</button>
          <button class="btn-secondary" @click="exportData">导出数据</button>
        </div>
      </div>

      <!-- 统计卡片 -->
      <div class="stats-cards">
        <div class="stat-card" @click="filterStatus = 'pending'">
          <div class="stat-icon pending">
            <img src="@/assets/icons/22.png" alt="待处理" class="icon-img" />
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.pending }}</div>
            <div class="stat-label">待处理</div>
          </div>
        </div>
        <div class="stat-card" @click="filterStatus = 'processing'">
          <div class="stat-icon processing">
            <img src="@/assets/icons/21.png" alt="处理中" class="icon-img" />
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.processing }}</div>
            <div class="stat-label">处理中</div>
          </div>
        </div>
        <div class="stat-card" @click="filterStatus = 'approved'">
          <div class="stat-icon approved">
            <img src="@/assets/icons/20.png" alt="已批准" class="icon-img" />
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.approved }}</div>
            <div class="stat-label">已批准</div>
          </div>
        </div>
        <div class="stat-card" @click="filterStatus = 'completed'">
          <div class="stat-icon completed">
            <img src="@/assets/icons/17.png" alt="已完成" class="icon-img" />
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.completed }}</div>
            <div class="stat-label">已完成</div>
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
            <tr v-if="filteredReturns.length === 0">
              <td colspan="8" class="empty-row">暂无退货申请</td>
            </tr>
            <tr v-for="item in filteredReturns" :key="item.id">
              <td><span class="return-code">{{ item.returnCode }}</span></td>
              <td>{{ item.trackingNumber }}</td>
              <td>{{ item.applicantName }}</td>
              <td>{{ item.applicantPhone }}</td>
              <td>
                <span class="reason-tag" :class="item.reasonType">
                  {{ getReasonLabel(item.reasonType) }}
                </span>
              </td>
              <td>{{ item.applyTime }}</td>
              <td>
                <span :class="['status-badge', item.status]">
                  {{ getStatusLabel(item.status) }}
                </span>
              </td>
              <td>
                <div class="action-btns">
                  <button class="btn-view" @click="viewReturn(item)">详情</button>
                  <button 
                    v-if="item.status === 'pending'" 
                    class="btn-approve" 
                    @click="approveReturn(item)"
                  >批准</button>
                  <button 
                    v-if="item.status === 'pending'" 
                    class="btn-reject" 
                    @click="rejectReturn(item)"
                  >拒绝</button>
                  <button 
                    v-if="item.status === 'approved'" 
                    class="btn-process" 
                    @click="processReturn(item)"
                  >处理</button>
                  <button 
                    v-if="item.status === 'processing'" 
                    class="btn-complete" 
                    @click="completeReturn(item)"
                  >完成</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>

        <!-- 分页 -->
        <div class="pagination">
          <button class="page-btn" :disabled="currentPage === 1" @click="currentPage--">上一页</button>
          <span class="page-info">第 {{ currentPage }} / {{ totalPages }} 页，共 {{ total }} 条</span>
          <button class="page-btn" :disabled="currentPage === totalPages" @click="currentPage++">下一页</button>
        </div>
      </div>

      <!-- 新建退货申请弹窗 -->
      <div class="modal" v-if="showCreateReturn" @click="showCreateReturn = false">
        <div class="modal-content" @click.stop>
          <div class="modal-header">
            <h3>新建退货申请</h3>
            <button class="close-btn" @click="showCreateReturn = false">×</button>
          </div>
          <div class="modal-body">
            <div class="form-group">
              <label>快递单号 *</label>
              <input type="text" v-model="returnForm.trackingNumber" placeholder="请输入快递单号">
            </div>
            <div class="form-row">
              <div class="form-group">
                <label>申请人姓名 *</label>
                <input type="text" v-model="returnForm.applicantName" placeholder="请输入申请人姓名">
              </div>
              <div class="form-group">
                <label>联系电话 *</label>
                <input type="text" v-model="returnForm.applicantPhone" placeholder="请输入联系电话">
              </div>
            </div>
            <div class="form-group">
              <label>退货原因 *</label>
              <select v-model="returnForm.reasonType">
                <option value="">请选择退货原因</option>
                <option value="wrong_item">商品错发</option>
                <option value="damaged">包裹破损</option>
                <option value="quality">质量问题</option>
                <option value="not_needed">不想要了</option>
                <option value="other">其他原因</option>
              </select>
            </div>
            <div class="form-group">
              <label>详细说明</label>
              <textarea v-model="returnForm.description" placeholder="请描述退货详细原因" rows="4"></textarea>
            </div>
            <div class="form-group">
              <label>退货方式</label>
              <div class="radio-group">
                <label class="radio-item">
                  <input type="radio" v-model="returnForm.returnMethod" value="pickup">
                  <span>上门取件</span>
                </label>
                <label class="radio-item">
                  <input type="radio" v-model="returnForm.returnMethod" value="dropoff">
                  <span>自行寄回</span>
                </label>
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn-cancel" @click="showCreateReturn = false">取消</button>
            <button class="btn-submit" @click="submitReturn">提交申请</button>
          </div>
        </div>
      </div>

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
                  <span class="value">{{ currentReturn?.returnCode }}</span>
                </div>
                <div class="detail-item">
                  <span class="label">申请时间：</span>
                  <span class="value">{{ currentReturn?.applyTime }}</span>
                </div>
                <div class="detail-item">
                  <span class="label">当前状态：</span>
                  <span :class="['status-badge', currentReturn?.status]">
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
                <div class="detail-item">
                  <span class="label">快递公司：</span>
                  <span class="value">{{ currentReturn?.company }}</span>
                </div>
              </div>
            </div>
            <div class="detail-section">
              <h4>申请人信息</h4>
              <div class="detail-grid">
                <div class="detail-item">
                  <span class="label">申请人：</span>
                  <span class="value">{{ currentReturn?.applicantName }}</span>
                </div>
                <div class="detail-item">
                  <span class="label">联系电话：</span>
                  <span class="value">{{ currentReturn?.applicantPhone }}</span>
                </div>
              </div>
            </div>
            <div class="detail-section">
              <h4>退货原因</h4>
              <div class="detail-grid">
                <div class="detail-item">
                  <span class="label">原因类型：</span>
                  <span class="reason-tag" :class="currentReturn?.reasonType">
                    {{ getReasonLabel(currentReturn?.reasonType) }}
                  </span>
                </div>
                <div class="detail-item">
                  <span class="label">退货方式：</span>
                  <span class="value">{{ currentReturn?.returnMethod === 'pickup' ? '上门取件' : '自行寄回' }}</span>
                </div>
              </div>
              <div class="detail-item full-width">
                <span class="label">详细说明：</span>
                <p class="description">{{ currentReturn?.description || '无' }}</p>
              </div>
            </div>
            <div class="detail-section" v-if="currentReturn?.processNote">
              <h4>处理备注</h4>
              <p class="description">{{ currentReturn.processNote }}</p>
            </div>
            <div class="timeline-section" v-if="currentReturn?.timeline">
              <h4>处理进度</h4>
              <div class="timeline">
                <div class="timeline-item" v-for="(log, index) in currentReturn.timeline" :key="index">
                  <div class="timeline-dot" :class="{ active: index === 0 }"></div>
                  <div class="timeline-content">
                    <span class="timeline-time">{{ log.time }}</span>
                    <span class="timeline-text">{{ log.text }}</span>
                  </div>
                </div>
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
import { ref, reactive, computed } from 'vue'
import AdminLayout from '@/layouts/AdminLayout.vue'
import { useToast } from '@/composables/useToast'

const { success, warning, info } = useToast()

interface ReturnRequest {
  id: number
  returnCode: string
  trackingNumber: string
  company: string
  applicantName: string
  applicantPhone: string
  reasonType: string
  description: string
  returnMethod: string
  status: string
  applyTime: string
  processNote?: string
  timeline?: { time: string; text: string }[]
}

const searchKeyword = ref('')
const filterStatus = ref('')
const currentPage = ref(1)
const totalPages = ref(2)
const total = ref(15)

const showCreateReturn = ref(false)
const showReturnDetail = ref(false)
const showProcessModal = ref(false)
const processModalTitle = ref('')
const processNote = ref('')
const processAction = ref('')
const currentReturn = ref<ReturnRequest | null>(null)

const stats = reactive({
  pending: 8,
  processing: 3,
  approved: 12,
  completed: 45
})

const returnForm = reactive({
  trackingNumber: '',
  applicantName: '',
  applicantPhone: '',
  reasonType: '',
  description: '',
  returnMethod: 'pickup'
})

// 模拟数据
const returnList = ref<ReturnRequest[]>([
  {
    id: 1,
    returnCode: 'RT202401190001',
    trackingNumber: 'SF1234567890',
    company: '顺丰速运',
    applicantName: '张三',
    applicantPhone: '13800138001',
    reasonType: 'damaged',
    description: '包裹外包装严重破损，内部物品受损',
    returnMethod: 'pickup',
    status: 'pending',
    applyTime: '2026-01-19 10:30',
    timeline: [
      { time: '2026-01-19 10:30', text: '用户提交退货申请' }
    ]
  },
  {
    id: 2,
    returnCode: 'RT202401190002',
    trackingNumber: 'YT9876543210',
    company: '圆通速递',
    applicantName: '李四',
    applicantPhone: '13800138002',
    reasonType: 'wrong_item',
    description: '收到的商品与订购的不符',
    returnMethod: 'dropoff',
    status: 'processing',
    applyTime: '2026-01-18 15:20',
    processNote: '已联系商家确认，等待退货地址',
    timeline: [
      { time: '2026-01-19 09:00', text: '管理员开始处理' },
      { time: '2026-01-18 16:00', text: '管理员批准退货申请' },
      { time: '2026-01-18 15:20', text: '用户提交退货申请' }
    ]
  },
  {
    id: 3,
    returnCode: 'RT202401180001',
    trackingNumber: 'ZT1122334455',
    company: '中通快递',
    applicantName: '王五',
    applicantPhone: '13800138003',
    reasonType: 'quality',
    description: '商品存在质量问题，无法正常使用',
    returnMethod: 'pickup',
    status: 'approved',
    applyTime: '2026-01-18 09:15',
    timeline: [
      { time: '2026-01-18 14:00', text: '管理员批准退货申请' },
      { time: '2026-01-18 09:15', text: '用户提交退货申请' }
    ]
  },
  {
    id: 4,
    returnCode: 'RT202401170001',
    trackingNumber: 'ST5566778899',
    company: '申通快递',
    applicantName: '赵六',
    applicantPhone: '13800138004',
    reasonType: 'not_needed',
    description: '购买后发现不需要了',
    returnMethod: 'dropoff',
    status: 'rejected',
    applyTime: '2026-01-17 11:45',
    processNote: '包裹已签收超过7天，不符合退货条件',
    timeline: [
      { time: '2026-01-17 15:00', text: '管理员拒绝退货申请' },
      { time: '2026-01-17 11:45', text: '用户提交退货申请' }
    ]
  },
  {
    id: 5,
    returnCode: 'RT202401160001',
    trackingNumber: 'JT6677889900',
    company: '极兔速递',
    applicantName: '钱七',
    applicantPhone: '13800138005',
    reasonType: 'damaged',
    description: '包裹在运输过程中损坏',
    returnMethod: 'pickup',
    status: 'completed',
    applyTime: '2026-01-16 08:30',
    timeline: [
      { time: '2026-01-18 10:00', text: '退货完成' },
      { time: '2026-01-17 14:00', text: '快递员上门取件' },
      { time: '2026-01-16 11:00', text: '管理员批准退货申请' },
      { time: '2026-01-16 08:30', text: '用户提交退货申请' }
    ]
  },
  {
    id: 6,
    returnCode: 'RT202401190003',
    trackingNumber: 'YD2233445566',
    company: '韵达快递',
    applicantName: '孙八',
    applicantPhone: '13800138006',
    reasonType: 'other',
    description: '地址填写错误，需要退回重新发货',
    returnMethod: 'pickup',
    status: 'pending',
    applyTime: '2026-01-19 14:00',
    timeline: [
      { time: '2026-01-19 14:00', text: '用户提交退货申请' }
    ]
  },
])

const filteredReturns = computed(() => {
  let result = returnList.value
  if (filterStatus.value) {
    result = result.filter(r => r.status === filterStatus.value)
  }
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(r => 
      r.trackingNumber.toLowerCase().includes(keyword) ||
      r.applicantName.toLowerCase().includes(keyword) ||
      r.returnCode.toLowerCase().includes(keyword)
    )
  }
  return result
})

const getStatusLabel = (status?: string) => {
  const labels: Record<string, string> = {
    'pending': '待处理',
    'processing': '处理中',
    'approved': '已批准',
    'rejected': '已拒绝',
    'completed': '已完成'
  }
  return labels[status || ''] || '未知'
}

const getReasonLabel = (reason?: string) => {
  const labels: Record<string, string> = {
    'wrong_item': '商品错发',
    'damaged': '包裹破损',
    'quality': '质量问题',
    'not_needed': '不想要了',
    'other': '其他原因'
  }
  return labels[reason || ''] || '未知'
}

const searchReturns = () => {
  currentPage.value = 1
}

const resetFilters = () => {
  searchKeyword.value = ''
  filterStatus.value = ''
  currentPage.value = 1
}

const viewReturn = (item: ReturnRequest) => {
  currentReturn.value = item
  showReturnDetail.value = true
}

const approveReturn = (item: ReturnRequest) => {
  currentReturn.value = item
  processModalTitle.value = '批准退货申请'
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

const processReturn = (item: ReturnRequest) => {
  currentReturn.value = item
  processModalTitle.value = '开始处理'
  processAction.value = 'process'
  processNote.value = ''
  showProcessModal.value = true
}

const completeReturn = (item: ReturnRequest) => {
  currentReturn.value = item
  processModalTitle.value = '完成退货'
  processAction.value = 'complete'
  processNote.value = ''
  showProcessModal.value = true
}

const confirmProcess = () => {
  if (!currentReturn.value) return
  
  const actionLabels: Record<string, string> = {
    'approve': '批准',
    'reject': '拒绝',
    'process': '处理',
    'complete': '完成'
  }
  
  success(`${actionLabels[processAction.value]}成功（模拟）`)
  showProcessModal.value = false
}

const submitReturn = () => {
  if (!returnForm.trackingNumber || !returnForm.applicantName || 
      !returnForm.applicantPhone || !returnForm.reasonType) {
    warning('请填写必填项')
    return
  }
  success('提交成功（模拟）')
  showCreateReturn.value = false
  Object.assign(returnForm, {
    trackingNumber: '',
    applicantName: '',
    applicantPhone: '',
    reasonType: '',
    description: '',
    returnMethod: 'pickup'
  })
}

const exportData = () => {
  info('导出数据（模拟）')
}
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

.action-buttons {
  display: flex;
  gap: 12px;
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

.btn-secondary {
  background: white;
  color: #333;
  border: 1px solid #e0e0e0;
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

.stat-icon.pending {
  background: #f5f5f5;
}

.stat-icon.processing {
  background: #f5f5f5;
}

.stat-icon.approved {
  background: #f5f5f5;
}

.stat-icon.completed {
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

.reason-tag {
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.reason-tag.wrong_item {
  background: #e6f7ff;
  color: #1890ff;
}

.reason-tag.damaged {
  background: #fff1f0;
  color: #f5222d;
}

.reason-tag.quality {
  background: #fff7e6;
  color: #fa8c16;
}

.reason-tag.not_needed {
  background: #f0f0f0;
  color: #666;
}

.reason-tag.other {
  background: #f6ffed;
  color: #52c41a;
}

.status-badge {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.status-badge.pending {
  background: #fff7e6;
  color: #fa8c16;
}

.status-badge.processing {
  background: #e6f7ff;
  color: #1890ff;
}

.status-badge.approved {
  background: #f6ffed;
  color: #52c41a;
}

.status-badge.rejected {
  background: #fff1f0;
  color: #f5222d;
}

.status-badge.completed {
  background: #f0f0f0;
  color: #666;
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
