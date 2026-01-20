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
          placeholder="搜索快递单号..."
          class="search-input"
          @keyup.enter="searchPackages"
        >
        <button class="search-btn" @click="searchPackages">搜索</button>
        <button class="reset-btn" @click="resetFilters">重置筛选</button>
      </div>
      <div class="action-buttons">
        <button class="btn-primary" @click="showAddPackage = true">录入包裹</button>
        <button class="btn-secondary" @click="batchDelete">批量删除</button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <div class="stat-card">
        <img src="@/assets/icons/2.png" alt="待发货" class="stat-icon" />
        <div class="stat-content">
          <div class="stat-value">{{ stats.pendingShip }}</div>
          <div class="stat-label">待发货</div>
        </div>
      </div>
      <div class="stat-card">
        <img src="@/assets/icons/3.png" alt="运输中" class="stat-icon" />
        <div class="stat-content">
          <div class="stat-value">{{ stats.shipping }}</div>
          <div class="stat-label">运输中</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">
            <img src="@/assets/icons/9.png" alt="已入库" class="stat-icon" />
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.stored }}</div>
          <div class="stat-label">已入库</div>
        </div>
      </div>
      <div class="stat-card">
        <img src="@/assets/icons/6.png" alt="异常" class="stat-icon" />
        <div class="stat-content">
          <div class="stat-value">{{ stats.abnormal }}</div>
          <div class="stat-label">异常包裹</div>
        </div>
      </div>
    </div>

    <!-- 包裹列表 -->
    <div class="packages-table">
      <table>
        <thead>
          <tr>
            <th v-if="showCheckboxes">
              <input
                type="checkbox"
                :checked="packageList.length > 0 && selectedIds.length === packageList.length"
                @change="toggleSelectAll(($event.target as HTMLInputElement).checked)"
              >
            </th>
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
            <td :colspan="showCheckboxes ? 11 : 10" class="empty-row">暂无包裹数据</td>
          </tr>
          <tr v-for="pkg in packageList" :key="pkg.id">
            <td v-if="showCheckboxes">
              <input type="checkbox" v-model="selectedIds" :value="pkg.id">
            </td>
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
                <!-- 根据状态显示下一步操作 -->
                <button 
                  v-if="pkg.status === 'PENDING_SHIP'" 
                  class="btn-ship" 
                  @click="shipPackage(pkg.id)">发货</button>
                <button 
                  v-if="pkg.status === 'SHIPPED'" 
                  class="btn-store" 
                  @click="storePackage(pkg.id)">入库</button>
                <button 
                  v-if="pkg.status === 'STORED' && !pkg.hasStorageInfo" 
                  class="btn-storage" 
                  @click="setStorageInfo(pkg)">生成取件码</button>
                <button 
                  v-if="pkg.status === 'STORED' && pkg.hasStorageInfo && pkg.isSigned === 0" 
                  class="btn-sign" 
                  @click="signPackage(pkg.id)">签收</button>
                <button 
                  v-if="pkg.status === 'RETURNED'" 
                  class="btn-reprocess" 
                  @click="reprocessPackage(pkg.id)">重新处理</button>
                <button 
                  v-if="pkg.status !== 'RETURNED'" 
                  class="btn-abnormal" 
                  @click="markAbnormal(pkg.id)">标记异常</button>
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
              <label>收件人</label>
              <input type="text" v-model="packageForm.receiverName" placeholder="可选，不填则使用用户名">
            </div>
            <div class="form-group">
              <label>手机号 *</label>
              <input type="tel" v-model="packageForm.receiverPhone" placeholder="通过手机号查找收件人">
            </div>
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
import { parcelApi, type Parcel, type ParcelCreateRequest, type ParcelUpdateRequest } from '@/api/admin/parcel'
import { useToast } from '@/composables/useToast'
import { useConfirm } from '@/composables/useConfirm'

const { success, error: showError, warning, info } = useToast()
const { confirm } = useConfirm()

interface Package {
  id: number
  trackingNumber: string
  company: string
  receiverName: string
  receiverPhone: string
  location: string
  pickupCode: string
  status: string // 前端状态：PENDING_SHIP, SHIPPED, STORED, RETURNED
  backendStatus: number // 后端状态：0-待发货, 1-已发货, 2-已入库, 3-退回/异常
  isSigned: number // 0-未签收, 1-已签收
  arrivalTime: string
  remark?: string
  hasStorageInfo?: boolean // 是否已设置存储信息
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
const currentPage = ref(1)
const totalPages = ref(1)
const total = ref(0)
const pageSize = ref(10)

const packageList = ref<Package[]>([])
const selectedIds = ref<number[]>([])
const showAddPackage = ref(false)
const showEditPackage = ref(false)
const showPackageDetail = ref(false)
const currentPackage = ref<Package | null>(null)
const editingPackageId = ref<number | null>(null)
const showCheckboxes = ref(false) // 控制勾选框显示状态

// 统计数据
const stats = reactive({
  pendingShip: 0, // 待发货
  shipping: 0, // 运输中
  stored: 0, // 已入库
  abnormal: 0 // 异常包裹
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

// 后端状态映射到前端状态（物流场景）
const mapBackendStatusToUI = (status: number): string => {
  switch (status) {
    case 0: return 'PENDING_SHIP' // 待发货
    case 1: return 'SHIPPED' // 已发货/运输中
    case 2: return 'STORED' // 已入库
    case 3: return 'RETURNED' // 退回/异常
    default: return 'PENDING_SHIP'
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
    location: parcel.location || '-', // 使用后端字段
    pickupCode: parcel.pickupCode || '-', // 使用后端字段
    status: mapBackendStatusToUI(parcel.status),
    backendStatus: parcel.status,
    isSigned: parcel.isSigned,
    arrivalTime: new Date(parcel.createTime).toLocaleString('zh-CN'),
    remark: '',
    hasStorageInfo: !!(parcel.location && parcel.pickupCode) // 根据字段判断是否已设置
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
    showError('加载包裹列表失败，请重试')
  }
}

// 更新统计数据（物流场景）
const updateStats = (parcels: Parcel[]) => {
  stats.pendingShip = parcels.filter(p => p.status === 0).length // 待发货
  stats.shipping = parcels.filter(p => p.status === 1).length // 运输中
  stats.stored = parcels.filter(p => p.status === 2 && p.isSigned === 0).length // 已入库未签收
  stats.abnormal = parcels.filter(p => p.status === 3).length // 异常包裹
}

const getStatusLabel = (status?: string) => {
  const labels: Record<string, string> = {
    'PENDING_SHIP': '待发货',
    'SHIPPED': '运输中',
    'STORED': '已入库',
    'RETURNED': '退回/异常'
  }
  return labels[status || ''] || '未知'
}

const searchPackages = async () => {
  const keyword = searchKeyword.value.trim()
  if (!keyword) {
    warning('请输入搜索关键词')
    return
  }
  
  try {
    // 调用后端搜索接口
    const parcel = await parcelApi.searchByTrackingNumber(keyword)
    // 将搜索结果转换为前端格式并显示
    packageList.value = [convertBackendToUI(parcel)]
    total.value = 1
    totalPages.value = 1
    currentPage.value = 1
    
    // 更新统计数据
    updateStats([parcel])
  } catch (error) {
    console.error('搜索包裹失败:', error)
    warning('未找到该快递单号对应的包裹')
    // 搜索失败时清空列表
    packageList.value = []
    total.value = 0
    totalPages.value = 1
    stats.pendingShip = 0
    stats.shipping = 0
    stats.stored = 0
    stats.abnormal = 0
  }
}

const toggleSelectAll = (checked: boolean) => {
  if (checked) {
    selectedIds.value = packageList.value.map(pkg => pkg.id)
  } else {
    selectedIds.value = []
  }
}

const batchDelete = async () => {
  // 如果勾选框未显示，则显示勾选框并提示用户选择
  if (!showCheckboxes.value) {
    showCheckboxes.value = true
    info('请勾选要删除的包裹，然后再次点击批量删除按钮')
    return
  }
  
  // 如果已显示勾选框但未选择任何包裹
  if (selectedIds.value.length === 0) {
    warning('请先勾选要删除的包裹')
    return
  }

  if (!(await confirm({
    title: '批量删除',
    message: `确定要删除选中的 ${selectedIds.value.length} 个包裹吗？`,
    type: 'danger'
  }))) {
    return
  }

  try {
    await parcelApi.deleteBatch(selectedIds.value)
    success('批量删除成功')
    selectedIds.value = []
    showCheckboxes.value = false // 删除成功后隐藏勾选框
    loadPackages()
  } catch (error) {
    console.error('批量删除包裹失败:', error)
    showError('批量删除失败，请重试')
  }
}

const resetFilters = () => {
  searchKeyword.value = '' // 清空搜索关键词
  currentPage.value = 1
  showCheckboxes.value = false // 重置时隐藏勾选框
  selectedIds.value = [] // 清空已选择的项
  loadPackages() // 重新加载完整列表
}

const viewPackage = async (pkg: Package) => {
  try {
    // 从后端获取最新详情
    const parcel = await parcelApi.getById(pkg.id)
    currentPackage.value = convertBackendToUI(parcel)
    showPackageDetail.value = true
  } catch (error) {
    console.error('获取包裹详情失败:', error)
    showError('获取包裹详情失败，请重试')
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
  if (!(await confirm({
    title: '删除包裹',
    message: '确定要删除该包裹吗？',
    type: 'danger'
  }))) {
    return
  }
  
  try {
    await parcelApi.delete(id)
    success('删除成功')
    loadPackages()
  } catch (error) {
    console.error('删除包裹失败:', error)
    showError('删除包裹失败，请重试')
  }
}

const submitPackage = async () => {
  // 表单验证
  if (!packageForm.trackingNumber || !packageForm.company || 
      !packageForm.receiverPhone) {
    warning('请填写快递单号、快递公司和收件人手机号')
    return
  }
  
  try {
    if (showEditPackage.value && editingPackageId.value) {
      // 更新包裹
      const updateData: ParcelUpdateRequest = {
        trackingNumber: packageForm.trackingNumber,
        company: packageForm.company,
        receiverName: packageForm.receiverName || undefined,
        receiverPhone: packageForm.receiverPhone || undefined
      }
      await parcelApi.update(editingPackageId.value, updateData)
      success('更新成功')
    } else {
      // 创建包裹 - 使用手机号查找用户
      const createData: ParcelCreateRequest = {
        trackingNumber: packageForm.trackingNumber,
        company: packageForm.company,
        receiverPhone: packageForm.receiverPhone, // 通过手机号查找用户
        receiverName: packageForm.receiverName || undefined, // 可选，如果不填则使用用户的username
        status: 0, // 默认状态：待发货
        isSigned: 0 // 默认未签收
      }
      await parcelApi.create(createData)
      success('创建成功')
    }
    
    closeModal()
    loadPackages()
  } catch (error) {
    console.error('提交包裹失败:', error)
    showError('提交包裹失败，请检查收件人信息是否正确')
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

// 发货功能：将包裹状态从“待发货”改为“运输中”
const shipPackage = async (id: number) => {
  if (!(await confirm({
    title: '确认发货',
    message: '确定要将该包裹标记为已发货吗？',
    type: 'info'
  }))) {
    return
  }
  
  try {
    await parcelApi.changeStatus(id, 1) // 状态改为1：已发货/运输中
    success('发货成功')
    loadPackages()
  } catch (error) {
    console.error('发货失败:', error)
    showError('发货失败，请重试')
  }
}

// 入库功能：将包裹状态从“运输中”改为“已入库”
const storePackage = async (id: number) => {
  if (!(await confirm({
    title: '确认入库',
    message: '确定该包裹已到达驿站并入库吗？',
    type: 'info'
  }))) {
    return
  }
  
  try {
    await parcelApi.changeStatus(id, 2) // 状态改为2：已入库
    success('入库成功')
    loadPackages()
  } catch (error) {
    console.error('入库失败:', error)
    showError('入库失败，请重试')
  }
}

// 设置存储信息（自动生成取件码和存放位置）
const setStorageInfo = async (pkg: Package) => {
  if (!(await confirm({
    title: '生成存储信息',
    message: '确定为该包裹自动生成存放位置和取件码吗？',
    type: 'info'
  }))) {
    return
  }
  
  try {
    const updatedParcel = await parcelApi.createPickupInfo(pkg.id)
    success(`存储信息生成成功！存放位置：${updatedParcel.location}，取件码：${updatedParcel.pickupCode}`)
    loadPackages()
  } catch (error) {
    console.error('生成存储信息失败:', error)
    showError('生成存储信息失败，请确认包裹状态为“已入库”且未签收')
  }
}

// 签收功能：标记包裹已签收
const signPackage = async (id: number) => {
  if (!(await confirm({
    title: '确认签收',
    message: '确定该包裹已被签收吗？',
    type: 'info'
  }))) {
    return
  }
  
  try {
    await parcelApi.update(id, { isSigned: 1 })
    success('签收成功')
    loadPackages()
  } catch (error) {
    console.error('签收失败:', error)
    showError('签收失败，请重试')
  }
}

// 标记异常：将包裹状态改为退回/异常
const markAbnormal = async (id: number) => {
  if (!(await confirm({
    title: '标记异常',
    message: '确定要将该包裹标记为异常吗？',
    type: 'warning'
  }))) {
    return
  }
  
  try {
    await parcelApi.changeStatus(id, 3) // 状态改为3：退回/异常
    success('已标记为异常')
    loadPackages()
  } catch (error) {
    console.error('标记异常失败:', error)
    showError('标记异常失败，请重试')
  }
}

// 重新处理：将异常包裹重新置为待发货
const reprocessPackage = async (id: number) => {
  if (!(await confirm({
    title: '重新处理',
    message: '确定要重新处理该包裹吗？状态将重置为待发货',
    type: 'warning'
  }))) {
    return
  }
  
  try {
    await parcelApi.changeStatus(id, 0) // 状态改为0：待发货
    success('已重新处理，状态已重置为待发货')
    loadPackages()
  } catch (error) {
    console.error('重新处理失败:', error)
    showError('重新处理失败，请重试')
  }
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

.status-badge.PENDING_SHIP {
  background: #fff7e6;
  color: #fa8c16;
}

.status-badge.SHIPPED {
  background: #e6f7ff;
  color: #1890ff;
}

.status-badge.STORED {
  background: #f6ffed;
  color: #52c41a;
}

.status-badge.RETURNED {
  background: #fff1f0;
  color: #f5222d;
}

.action-btns {
  display: flex;
  gap: 8px;
}

.btn-view,
.btn-edit,
.btn-ship,
.btn-store,
.btn-storage,
.btn-status,
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

.btn-ship {
  background: white;
  color: #52c41a;
  border: 1px solid #52c41a;
}

.btn-ship:hover {
  background: #52c41a;
  color: white;
}

.btn-store {
  background: white;
  color: #13c2c2;
  border: 1px solid #13c2c2;
}

.btn-store:hover {
  background: #13c2c2;
  color: white;
}

.btn-storage {
  background: white;
  color: #fa8c16;
  border: 1px solid #fa8c16;
}

.btn-storage:hover {
  background: #fa8c16;
  color: white;
}

.btn-sign {
  background: white;
  color: #52c41a;
  border: 1px solid #52c41a;
}

.btn-sign:hover {
  background: #52c41a;
  color: white;
}

.btn-abnormal {
  background: white;
  color: #faad14;
  border: 1px solid #faad14;
}

.btn-abnormal:hover {
  background: #faad14;
  color: white;
}

.btn-reprocess {
  background: white;
  color: #722ed1;
  border: 1px solid #722ed1;
}

.btn-reprocess:hover {
  background: #722ed1;
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
  font-size: 10px;
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
