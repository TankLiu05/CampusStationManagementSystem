<template>
  <AdminLayout>
    <div class="packages-management">
    <div class="page-header">
      <h1>包裹管理</h1>
      <p>管理驿站包裹信息</p>
    </div>

    <!-- 操作栏 -->
    <div class="action-bar">
      <div class="search-section">
        <input 
          type="text" 
          v-model="searchKeyword" 
          placeholder="搜索快递单号、收件人..."
          class="search-input"
        >
        <button class="search-btn" @click="searchPackages">搜索</button>
      </div>
      <div class="action-buttons">
        <button class="btn-primary" @click="showAddPackage = true">录入包裹</button>
        <button class="btn-secondary" @click="batchImport">批量导入</button>
        <button class="btn-secondary" @click="exportPackages">导出数据</button>
      </div>
    </div>

    <!-- 筛选区域 -->
    <div class="filter-section">
      <select v-model="statusFilter" class="filter-select">
        <option value="">全部状态</option>
        <option value="PENDING">待取件</option>
        <option value="PICKED">已取件</option>
        <option value="EXPIRED">已过期</option>
      </select>
      <select v-model="companyFilter" class="filter-select">
        <option value="">全部快递公司</option>
        <option value="顺丰速运">顺丰速运</option>
        <option value="中通快递">中通快递</option>
        <option value="圆通速递">圆通速递</option>
        <option value="申通快递">申通快递</option>
        <option value="韵达快递">韵达快递</option>
        <option value="极兔速递">极兔速递</option>
      </select>
      <input type="date" v-model="startDate" class="date-input" placeholder="开始日期">
      <input type="date" v-model="endDate" class="date-input" placeholder="结束日期">
      <button class="reset-btn" @click="resetFilters">重置筛选</button>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <div class="stat-card">
        <img src="@/assets/icons/2.png" alt="待取包裹" class="stat-icon" />
        <div class="stat-content">
          <div class="stat-value">{{ stats.pending }}</div>
          <div class="stat-label">待取包裹</div>
        </div>
      </div>
      <div class="stat-card">
        <img src="@/assets/icons/3.png" alt="今日取件" class="stat-icon" />
        <div class="stat-content">
          <div class="stat-value">{{ stats.todayPicked }}</div>
          <div class="stat-label">今日取件</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">
            <img src="@/assets/icons/9.png" alt="即将过期" class="stat-icon" />
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.expiringSoon }}</div>
          <div class="stat-label">即将过期</div>
        </div>
      </div>
      <div class="stat-card">
        <img src="@/assets/icons/6.png" alt="统计" class="stat-icon" />
        <div class="stat-content">
          <div class="stat-value">{{ total }}</div>
          <div class="stat-label">累计包裹</div>
        </div>
      </div>
    </div>

    <!-- 包裹列表 -->
    <div class="packages-table">
      <table>
        <thead>
          <tr>
            <th>包裹ID</th>
            <th>快递单号</th>
            <th>快递公司</th>
            <th>收件人</th>
            <th>手机号</th>
            <th>存放位置</th>
            <th>取件码</th>
            <th>状态</th>
            <th>到达时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="packageList.length === 0">
            <td colspan="10" class="empty-row">暂无包裹数据</td>
          </tr>
          <tr v-for="pkg in packageList" :key="pkg.id">
            <td>{{ pkg.id }}</td>
            <td>{{ pkg.trackingNumber }}</td>
            <td>{{ pkg.company }}</td>
            <td>{{ pkg.receiverName }}</td>
            <td>{{ pkg.receiverPhone }}</td>
            <td>{{ pkg.location }}</td>
            <td>
              <span class="pickup-code">{{ pkg.pickupCode }}</span>
            </td>
            <td>
              <span :class="['status-badge', pkg.status]">
                {{ getStatusLabel(pkg.status) }}
              </span>
            </td>
            <td>{{ pkg.arrivalTime }}</td>
            <td>
              <div class="action-btns">
                <button class="btn-view" @click="viewPackage(pkg)">详情</button>
                <button class="btn-edit" @click="editPackage(pkg)">编辑</button>
                <button class="btn-delete" @click="deletePackage(pkg.id)">删除</button>
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

    <!-- 录入包裹弹窗 -->
    <div class="modal" v-if="showAddPackage || showEditPackage" @click="closeModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>{{ showAddPackage ? '录入包裹' : '编辑包裹' }}</h3>
          <button class="close-btn" @click="closeModal">×</button>
        </div>
        <div class="modal-body">
          <div class="form-row">
            <div class="form-group">
              <label>快递单号 *</label>
              <input type="text" v-model="packageForm.trackingNumber" placeholder="请输入快递单号">
            </div>
            <div class="form-group">
              <label>快递公司 *</label>
              <select v-model="packageForm.company">
                <option value="">请选择快递公司</option>
                <option value="顺丰速运">顺丰速运</option>
                <option value="中通快递">中通快递</option>
                <option value="圆通速递">圆通速递</option>
                <option value="申通快递">申通快递</option>
                <option value="韵达快递">韵达快递</option>
                <option value="极兔速递">极兔速递</option>
              </select>
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>收件人 *</label>
              <input type="text" v-model="packageForm.receiverName" placeholder="请输入收件人姓名">
            </div>
            <div class="form-group">
              <label>手机号 *</label>
              <input type="tel" v-model="packageForm.receiverPhone" placeholder="请输入手机号">
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>存放位置 *</label>
              <input type="text" v-model="packageForm.location" placeholder="例如：A区-01货架">
            </div>
            <div class="form-group">
              <label>取件码</label>
              <input type="text" v-model="packageForm.pickupCode" placeholder="自动生成" disabled>
            </div>
          </div>
          <div class="form-group">
            <label>备注</label>
            <textarea v-model="packageForm.remark" placeholder="选填" rows="3"></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn-cancel" @click="closeModal">取消</button>
          <button class="btn-submit" @click="submitPackage">确定</button>
        </div>
      </div>
    </div>

    <!-- 包裹详情弹窗 -->
    <div class="modal" v-if="showPackageDetail" @click="showPackageDetail = false">
      <div class="modal-content detail-modal" @click.stop>
        <div class="modal-header">
          <h3>包裹详情</h3>
          <button class="close-btn" @click="showPackageDetail = false">×</button>
        </div>
        <div class="modal-body">
          <div class="detail-section">
            <h4>基本信息</h4>
            <div class="detail-grid">
              <div class="detail-item">
                <span class="label">包裹ID：</span>
                <span class="value">{{ currentPackage?.id }}</span>
              </div>
              <div class="detail-item">
                <span class="label">快递单号：</span>
                <span class="value">{{ currentPackage?.trackingNumber }}</span>
              </div>
              <div class="detail-item">
                <span class="label">快递公司：</span>
                <span class="value">{{ currentPackage?.company }}</span>
              </div>
              <div class="detail-item">
                <span class="label">取件码：</span>
                <span class="value code">{{ currentPackage?.pickupCode }}</span>
              </div>
            </div>
          </div>
          <div class="detail-section">
            <h4>收件人信息</h4>
            <div class="detail-grid">
              <div class="detail-item">
                <span class="label">收件人：</span>
                <span class="value">{{ currentPackage?.receiverName }}</span>
              </div>
              <div class="detail-item">
                <span class="label">手机号：</span>
                <span class="value">{{ currentPackage?.receiverPhone }}</span>
              </div>
            </div>
          </div>
          <div class="detail-section">
            <h4>存储信息</h4>
            <div class="detail-grid">
              <div class="detail-item">
                <span class="label">存放位置：</span>
                <span class="value">{{ currentPackage?.location }}</span>
              </div>
              <div class="detail-item">
                <span class="label">到达时间：</span>
                <span class="value">{{ currentPackage?.arrivalTime }}</span>
              </div>
              <div class="detail-item">
                <span class="label">状态：</span>
                <span :class="['status-badge', currentPackage?.status]">
                  {{ getStatusLabel(currentPackage?.status) }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import AdminLayout from '@/layouts/AdminLayout.vue'
import { parcelApi, type Parcel, type ParcelRequest } from '@/api/admin/parcel'

interface Package {
  id: number
  trackingNumber: string
  company: string
  receiverName: string
  receiverPhone: string
  location: string
  pickupCode: string
  status: string
  arrivalTime: string
  remark?: string
}

interface PackageForm {
  trackingNumber: string
  company: string
  receiverName: string
  receiverPhone: string
  location: string
  pickupCode: string
  remark: string
}

const searchKeyword = ref('')
const statusFilter = ref('')
const companyFilter = ref('')
const startDate = ref('')
const endDate = ref('')
const currentPage = ref(1)
const totalPages = ref(1)
const total = ref(0)
const pageSize = ref(10)

const packageList = ref<Package[]>([])
const showAddPackage = ref(false)
const showEditPackage = ref(false)
const showPackageDetail = ref(false)
const currentPackage = ref<Package | null>(null)
const editingPackageId = ref<number | null>(null)

// 统计数据
const stats = reactive({
  pending: 0,
  todayPicked: 0,
  expiringSoon: 0,
  total: 0
})

const packageForm = reactive<PackageForm>({
  trackingNumber: '',
  company: '',
  receiverName: '',
  receiverPhone: '',
  location: '',
  pickupCode: '',
  remark: ''
})

// 后端状态映射到前端状态
const mapBackendStatusToUI = (status: number, isSigned: number): string => {
  if (isSigned === 1) return 'PICKED' // 已签收
  if (status === 3) return 'EXPIRED' // 退回/异常
  if (status === 2) return 'PENDING' // 已入库待取件
  return 'PENDING'
}

// 前端状态映射到后端状态
const mapUIStatusToBackend = (uiStatus: string): number => {
  switch (uiStatus) {
    case 'PICKED': return 2 // 已入库
    case 'EXPIRED': return 3 // 退回/异常
    case 'PENDING': return 2 // 已入库
    default: return 0
  }
}

// 后端数据转换为前端格式
const convertBackendToUI = (parcel: Parcel): Package => {
  return {
    id: parcel.id,
    trackingNumber: parcel.trackingNumber,
    company: parcel.company,
    receiverName: parcel.receiverName,
    receiverPhone: parcel.receiverPhone,
    location: '', // 后端暂无此字段
    pickupCode: '', // 后端暂无此字段
    status: mapBackendStatusToUI(parcel.status, parcel.isSigned),
    arrivalTime: new Date(parcel.createTime).toLocaleString('zh-CN'),
    remark: ''
  }
}

// 加载包裹列表
const loadPackages = async () => {
  try {
    const response = await parcelApi.list(currentPage.value - 1, pageSize.value)
    packageList.value = response.content.map(convertBackendToUI)
    total.value = response.totalElements
    totalPages.value = response.totalPages
    
    // 更新统计数据
    updateStats(response.content)
  } catch (error) {
    console.error('加载包裹列表失败:', error)
    alert('加载包裹列表失败，请重试')
  }
}

// 更新统计数据
const updateStats = (parcels: Parcel[]) => {
  stats.total = parcels.length
  stats.pending = parcels.filter(p => p.isSigned === 0 && p.status === 2).length
  stats.todayPicked = parcels.filter(p => {
    const today = new Date().toDateString()
    return p.isSigned === 1 && new Date(p.updateTime).toDateString() === today
  }).length
  // 即将过期：假设超过3天未取为即将过期
  stats.expiringSoon = parcels.filter(p => {
    if (p.isSigned === 1) return false
    const daysDiff = (Date.now() - new Date(p.createTime).getTime()) / (1000 * 60 * 60 * 24)
    return daysDiff >= 3
  }).length
}

const getStatusLabel = (status?: string) => {
  const labels: Record<string, string> = {
    'PENDING': '待取件',
    'PICKED': '已取件',
    'EXPIRED': '已过期'
  }
  return labels[status || ''] || '未知'
}

const searchPackages = () => {
  console.log('搜索包裹:', searchKeyword.value)
  // TODO: 后端暂不支持搜索，待扩展
  alert('搜索功能待后端接口扩展')
}

const batchImport = () => {
  console.log('批量导入包裹')
  alert('批量导入功能待开发')
}

const exportPackages = () => {
  console.log('导出包裹数据')
  alert('导出功能待开发')
}

const resetFilters = () => {
  statusFilter.value = ''
  companyFilter.value = ''
  startDate.value = ''
  endDate.value = ''
  searchKeyword.value = ''
  currentPage.value = 1
  loadPackages()
}

const viewPackage = async (pkg: Package) => {
  try {
    // 从后端获取最新详情
    const parcel = await parcelApi.getById(pkg.id)
    currentPackage.value = convertBackendToUI(parcel)
    showPackageDetail.value = true
  } catch (error) {
    console.error('获取包裹详情失败:', error)
    alert('获取包裹详情失败，请重试')
  }
}

const editPackage = (pkg: Package) => {
  editingPackageId.value = pkg.id
  Object.assign(packageForm, {
    trackingNumber: pkg.trackingNumber,
    company: pkg.company,
    receiverName: pkg.receiverName,
    receiverPhone: pkg.receiverPhone,
    location: pkg.location,
    pickupCode: pkg.pickupCode,
    remark: pkg.remark || ''
  })
  showEditPackage.value = true
}

const deletePackage = async (id: number) => {
  if (!confirm('确定要删除该包裹吗？')) {
    return
  }
  
  try {
    await parcelApi.delete(id)
    alert('删除成功')
    loadPackages()
  } catch (error) {
    console.error('删除包裹失败:', error)
    alert('删除包裹失败，请重试')
  }
}

const submitPackage = async () => {
  // 表单验证
  if (!packageForm.trackingNumber || !packageForm.company || 
      !packageForm.receiverName || !packageForm.receiverPhone) {
    alert('请填写所有必填项')
    return
  }
  
  try {
    const data: ParcelRequest = {
      trackingNumber: packageForm.trackingNumber,
      company: packageForm.company,
      receiverName: packageForm.receiverName,
      receiverPhone: packageForm.receiverPhone
    }
    
    if (showEditPackage.value && editingPackageId.value) {
      // 更新包裹
      await parcelApi.update(editingPackageId.value, data)
      alert('更新成功')
    } else {
      // 创建包裹
      await parcelApi.create(data)
      alert('创建成功')
    }
    
    closeModal()
    loadPackages()
  } catch (error) {
    console.error('提交包裹失败:', error)
    alert('提交包裹失败，请重试')
  }
}

const closeModal = () => {
  showAddPackage.value = false
  showEditPackage.value = false
  editingPackageId.value = null
  Object.assign(packageForm, {
    trackingNumber: '',
    company: '',
    receiverName: '',
    receiverPhone: '',
    location: '',
    pickupCode: '',
    remark: ''
  })
}

// 监听分页变化
watch(currentPage, () => {
  loadPackages()
})

// 组件挂载时加载数据
onMounted(() => {
  loadPackages()
})
</script>

<style scoped>
.packages-management {
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
  max-width: 500px;
}

.search-input {
  flex: 1;
  padding: 10px 16px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
}

.search-btn {
  padding: 10px 24px;
  background: #808080;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.search-btn:hover {
  background: #666666;
}

.action-buttons {
  display: flex;
  gap: 12px;
}

.btn-primary,
.btn-secondary {
  padding: 10px 20px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
  white-space: nowrap;
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

.btn-secondary:hover {
  border-color: #808080;
  color: #808080;
}

.filter-section {
  background: white;
  padding: 16px 20px;
  border-radius: 12px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.filter-select,
.date-input {
  padding: 8px 16px;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
}

.reset-btn {
  padding: 8px 20px;
  background: white;
  color: #333;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
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

.packages-table {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
  min-width: 1200px;
}

thead {
  background: #f5f7fa;
}

th {
  padding: 16px;
  text-align: left;
  font-size: 14px;
  font-weight: 600;
  color: #666;
}

td {
  padding: 16px;
  border-top: 1px solid #f0f0f0;
  font-size: 14px;
  color: #333;
}

.empty-row {
  text-align: center;
  color: #999;
  padding: 60px;
}

.pickup-code {
  font-family: 'Courier New', monospace;
  font-weight: 600;
  color: #808080;
  font-size: 16px;
}

.status-badge {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.status-badge.PENDING {
  background: #fff7e6;
  color: #fa8c16;
}

.status-badge.PICKED {
  background: #f6ffed;
  color: #52c41a;
}

.status-badge.EXPIRED {
  background: #fff1f0;
  color: #f5222d;
}

.action-btns {
  display: flex;
  gap: 8px;
}

.btn-view,
.btn-edit,
.btn-delete {
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

.btn-edit {
  background: white;
  color: #808080;
  border: 1px solid #808080;
}

.btn-edit:hover {
  background: #808080;
  color: white;
}

.btn-delete {
  background: white;
  color: #f5222d;
  border: 1px solid #f5222d;
}

.btn-delete:hover {
  background: #f5222d;
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
  font-size: 14px;
  transition: all 0.2s;
}

.page-btn:hover:not(:disabled) {
  border-color: #808080;
  color: #808080;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  font-size: 14px;
  color: #666;
}

/* 弹窗样式 */
.modal {
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
}

.modal-content {
  background: white;
  border-radius: 12px;
  width: 90%;
  max-width: 700px;
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
  width: 30px;
  height: 30px;
  border: none;
  background: none;
  font-size: 24px;
  color: #999;
  cursor: pointer;
}

.modal-body {
  padding: 24px;
  max-height: 60vh;
  overflow-y: auto;
}

.form-row {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 8px;
}

.form-group input,
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 10px 16px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
}

.form-group textarea {
  resize: vertical;
  font-family: inherit;
}

.modal-footer {
  padding: 16px 24px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.btn-cancel,
.btn-submit {
  padding: 8px 24px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
}

.btn-cancel {
  background: white;
  border: 1px solid #e0e0e0;
  color: #333;
}

.btn-submit {
  background: #808080;
  color: white;
  border: none;
}

.btn-submit:hover {
  background: #666666;
}

/* 详情弹窗 */
.detail-modal {
  max-width: 600px;
}

.detail-section {
  margin-bottom: 24px;
}

.detail-section:last-child {
  margin-bottom: 0;
}

.detail-section h4 {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.detail-item {
  display: flex;
  align-items: center;
}

.detail-item .label {
  color: #999;
  font-size: 14px;
  min-width: 80px;
}

.detail-item .value {
  color: #333;
  font-size: 14px;
  font-weight: 500;
}

.detail-item .value.code {
  font-family: 'Courier New', monospace;
  color: #808080;
  font-size: 16px;
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
