<template>
  <AdminLayout>
    <div class="logistics-management">
      <div class="page-header">
        <h1>物流管理</h1>
        <p>管理物流路线信息与包裹追踪</p>
      </div>

      <!-- 操作栏 -->
      <div class="action-bar">
        <div class="search-section">
          <input 
            type="text" 
            v-model="searchKeyword" 
            placeholder="搜索快递单号..."
            class="search-input"
            @keyup.enter="searchLogistics"
          >
          <select v-model="filterCompany" class="filter-select">
            <option value="">全部快递公司</option>
            <option value="顺丰速运">顺丰速运</option>
            <option value="中通快递">中通快递</option>
            <option value="圆通速递">圆通速递</option>
            <option value="申通快递">申通快递</option>
            <option value="韵达快递">韵达快递</option>
            <option value="极兔速递">极兔速递</option>
          </select>
          <select v-model="filterStatus" class="filter-select">
            <option value="">全部状态</option>
            <option value="collected">已揽收</option>
            <option value="in_transit">运输中</option>
            <option value="arrived">已到站</option>
            <option value="delivering">派送中</option>
            <option value="signed">已签收</option>
          </select>
          <button class="search-btn" @click="searchLogistics">搜索</button>
          <button class="reset-btn" @click="resetFilters">重置</button>
        </div>
        <div class="action-buttons">
          <button class="btn-primary" @click="showAddRoute = true">添加物流信息</button>
        </div>
      </div>

      <!-- 统计卡片 -->
      <div class="stats-cards">
        <div class="stat-card">
          <div class="stat-icon" style="background: #f5f5f5;">
            <img src="@/assets/icons/2.png" alt="包裹总数" class="icon-img" />
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.totalParcels }}</div>
            <div class="stat-label">包裹总数</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon" style="background: #f5f5f5;">
            <img src="@/assets/icons/18.png" alt="运输中" class="icon-img" />
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.inTransit }}</div>
            <div class="stat-label">运输中</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon" style="background: #f5f5f5;">
            <img src="@/assets/icons/17.png" alt="已到站" class="icon-img" />
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.arrived }}</div>
            <div class="stat-label">已到站</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon" style="background: #f5f5f5;">
            <img src="@/assets/icons/9.png" alt="超时预警" class="icon-img" />
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.delayed }}</div>
            <div class="stat-label">超时预警</div>
          </div>
        </div>
      </div>

      <!-- 物流列表 -->
      <div class="logistics-table">
        <table>
          <thead>
            <tr>
              <th>快递单号</th>
              <th>快递公司</th>
              <th>起始站</th>
              <th>当前站点</th>
              <th>下一站点</th>
              <th>终点站</th>
              <th>预计到达</th>
              <th>物流状态</th>
              <th>更新时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="filteredLogistics.length === 0">
              <td colspan="8" class="empty-row">暂无物流数据</td>
            </tr>
            <tr v-for="item in filteredLogistics" :key="item.id">
              <td><span class="tracking-number">{{ item.trackingNumber }}</span></td>
              <td>
                <span class="company-badge">{{ item.company }}</span>
              </td>
              <td>{{ item.origin || '-' }}</td>
              <td>{{ item.currentStation }}</td>
              <td>{{ item.nextStation || '-' }}</td>
              <td>{{ item.destination || '-' }}</td>
              <td>
                <span :class="['eta-text', { delayed: item.isDelayed }]">
                  {{ item.etaNextStation || '-' }}
                </span>
              </td>
              <td>
                <span :class="['status-badge', item.status]">
                  {{ getStatusLabel(item.status) }}
                </span>
              </td>
              <td>{{ item.updateTime }}</td>
              <td>
                <div class="action-btns">
                  <button class="btn-view" @click="viewLogistics(item)">轨迹</button>
                  <button
                    class="btn-edit"
                    :disabled="!!item.pickupCode || !canUpdateRoute(item)"
                    :class="{ disabled: !!item.pickupCode || !canUpdateRoute(item) }"
                    @click="canUpdateRoute(item) && updateRoute(item)"
                  >更新</button>
                  <button
                    class="btn-delivery"
                    :disabled="!!item.pickupCode || !canUpdateRoute(item)"
                    :class="{ disabled: !!item.pickupCode || !canUpdateRoute(item) }"
                    @click="canUpdateRoute(item) && markAsDelivered(item)"
                  >送达</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>

        <!-- 分页 -->
        <div class="pagination">
          <button class="page-btn" :disabled="currentPage === 1 || loading" @click="handlePageChange(-1)">上一页</button>
          <span class="page-info">第 {{ currentPage }} / {{ totalPages }} 页，共 {{ total }} 条</span>
          <button class="page-btn" :disabled="currentPage >= totalPages || loading" @click="handlePageChange(1)">下一页</button>
        </div>
      </div>

      <!-- 添加物流信息弹窗 -->
      <div class="modal" v-if="showAddRoute" @click="showAddRoute = false">
        <div class="modal-content" @click.stop>
          <div class="modal-header">
            <h3>添加物流信息</h3>
            <button class="close-btn" @click="showAddRoute = false">×</button>
          </div>
          <div class="modal-body">
            <div class="form-group">
              <label>快递单号 *</label>
              <input type="text" v-model="routeForm.trackingNumber" placeholder="请输入快递单号">
            </div>
            <div class="form-group">
              <label>快递公司 *</label>
              <select v-model="routeForm.company">
                <option value="顺丰速运">顺丰速运</option>
                <option value="中通快递">中通快递</option>
                <option value="圆通速递">圆通速递</option>
                <option value="申通快递">申通快递</option>
                <option value="韵达快递">韵达快递</option>
                <option value="极兔速递">极兔速递</option>
              </select>
            </div>
            <div class="form-group">
              <label>起始站 *</label>
              <input type="text" v-model="routeForm.origin" placeholder="如：北京转运中心">
            </div>
            <div class="form-group">
              <label>终点站 *</label>
              <input type="text" v-model="routeForm.destination" placeholder="如：广州转运中心">
            </div>
            <div class="form-group">
              <label>当前站点 *</label>
              <input type="text" v-model="routeForm.currentStation" placeholder="如：北京转运中心">
            </div>
            <div class="form-group">
              <label>下一站点</label>
              <input type="text" v-model="routeForm.nextStation" placeholder="如：上海转运中心">
            </div>
            <div class="form-row">
              <div class="form-group">
                <label>预计到达下一站</label>
                <input type="datetime-local" v-model="routeForm.etaNextStation">
              </div>
              <div class="form-group">
                <label>预计送达时间</label>
                <input type="datetime-local" v-model="routeForm.etaDelivered">
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn-cancel" @click="showAddRoute = false">取消</button>
            <button class="btn-submit" @click="submitRoute">提交</button>
          </div>
        </div>
      </div>

      <!-- 更新物流弹窗 -->
      <div class="modal" v-if="showUpdateRoute" @click="showUpdateRoute = false">
        <div class="modal-content" @click.stop>
          <div class="modal-header">
            <h3>更新物流信息</h3>
            <button class="close-btn" @click="showUpdateRoute = false">×</button>
          </div>
          <div class="modal-body">
            <div class="info-row">
              <span class="label">快递单号：</span>
              <span class="value">{{ currentLogistics?.trackingNumber }}</span>
            </div>
            <div class="form-group">
              <label>当前站点 *</label>
              <select v-model="updateForm.currentStation" class="form-select" disabled>
                <option value="">请选择当前站点</option>
                <option v-for="station in stations" :key="station.id" :value="station.station">
                  {{ station.station }} ({{ station.city }})
                </option>
              </select>
            </div>
            <div class="form-group">
              <label>下一站点</label>
              <select v-model="updateForm.nextStation" class="form-select">
                <option value="">请选择下一站点</option>
                <option v-for="station in stations" :key="station.id" :value="station.station">
                  {{ station.station }} ({{ station.city }})
                </option>
              </select>
            </div>

            <div class="form-row">
              <div class="form-group">
                <label>预计到达下一站</label>
                <input type="datetime-local" v-model="updateForm.etaNextStation">
              </div>
              <div class="form-group">
              <label>预计送达时间</label>
              <input type="datetime-local" v-model="updateForm.etaDelivered">
            </div>
          </div>
          <div class="form-group checkbox-group">
             <label class="checkbox-label">
               <input type="checkbox" v-model="updateForm.isDelivered">
               标记为已送达（物流信息将不可再更改）
             </label>
          </div>
          <div class="form-group">
            <label>备注信息</label>
              <textarea v-model="updateForm.remark" placeholder="可选备注" rows="3"></textarea>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn-cancel" @click="showUpdateRoute = false">取消</button>
            <button class="btn-submit" @click="submitUpdate">更新</button>
          </div>
        </div>
      </div>

      <!-- 物流轨迹弹窗 -->
      <div class="modal" v-if="showTrackingDetail" @click="showTrackingDetail = false">
        <div class="modal-content detail-modal" @click.stop>
          <div class="modal-header">
            <h3>物流轨迹</h3>
            <button class="close-btn" @click="showTrackingDetail = false">×</button>
          </div>
          <div class="modal-body">
            <!-- 包裹基本信息 -->
            <div class="parcel-info">
              <div class="info-grid">
                <div class="info-item">
                  <span class="label">快递单号</span>
                  <span class="value tracking">{{ currentLogistics?.trackingNumber }}</span>
                </div>
                <div class="info-item">
                  <span class="label">快递公司</span>
                  <span class="value">{{ currentLogistics?.company }}</span>
                </div>
                <div class="info-item">
                  <span class="label">当前状态</span>
                  <span :class="['status-badge', currentLogistics?.status]">
                    {{ getStatusLabel(currentLogistics?.status) }}
                  </span>
                </div>
                <div class="info-item">
                  <span class="label">预计送达</span>
                  <span class="value">{{ currentLogistics?.etaDelivered || '待确认' }}</span>
                </div>
              </div>
            </div>

            <!-- 物流轨迹 -->
            <div class="tracking-timeline">
              <h4>物流轨迹</h4>
              <div class="timeline">
                <div 
                  class="timeline-item" 
                  v-for="(track, index) in currentLogistics?.trackingHistory" 
                  :key="index"
                  :class="{ active: index === 0 }"
                >
                  <div class="timeline-dot"></div>
                  <div class="timeline-content">
                    <div class="timeline-header">
                      <span class="timeline-status">{{ track.status }}</span>
                      <span class="timeline-time">{{ track.time }}</span>
                    </div>
                    <p class="timeline-desc">{{ track.description }}</p>
                    <span class="timeline-location">{{ track.location }}</span>
                  </div>
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
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import AdminLayout from '@/layouts/AdminLayout.vue'
import { parcelRouteApi, type ParcelRoute, type ParcelRouteCreateRequest } from '@/api/admin/parcelRoute'
import { parcelApi, type Parcel, type PageResponse } from '@/api/admin/parcel'
import { getCurrentAdminDetail, getAllStations, type AdminRole, type AdminStation, type AdminDetail } from '@/api/admin/management'
import { useToast } from '@/composables/useToast'
import { useConfirm } from '@/composables/useConfirm'

const router = useRouter()
const { success, error: showError, warning, info } = useToast()
const { confirm } = useConfirm()

interface TrackingHistory {
  status: string
  time: string
  description: string
  location: string
}

interface LogisticsItem {
  id: number
  trackingNumber: string
  company: string
  origin?: string
  destination?: string
  currentStation: string
  nextStation: string
  etaNextStation: string
  etaDelivered: string
  status: string
  updateTime: string
  isDelayed: boolean
  isDelivered?: number
  trackingHistory: TrackingHistory[]
  pickupCode?: string // 取件码
}

const searchKeyword = ref('')
const filterCompany = ref('')
const filterStatus = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const totalPages = ref(1)
const total = ref(0)
const loading = ref(false)

const showAddRoute = ref(false)
const showUpdateRoute = ref(false)
const showTrackingDetail = ref(false)
const currentLogistics = ref<LogisticsItem | null>(null)
const currentAdmin = ref<AdminDetail | null>(null)

const stats = reactive({
  totalParcels: 0,
  inTransit: 0,
  arrived: 0,
  delayed: 0
})

const routeForm = reactive({
  trackingNumber: '',
  company: '顺丰速运',
  origin: '',
  destination: '',
  currentStation: '',
  nextStation: '',
  etaNextStation: '',
  etaDelivered: ''
})

const updateForm = reactive({
  currentStation: '',
  nextStation: '',
  etaNextStation: '',
  etaDelivered: '',
  isDelivered: false,
  remark: ''
})

const canUpdateRoute = (item: LogisticsItem) => {
  // 已有取件码的快件前面已经做了禁用，这里主要限制“终点站站点管理员”再继续更新物流
  // 如果已标记为送达，则不可再更新
  if (item.isDelivered === 1) {
    return false
  }

  if (!currentAdmin.value) {
    // 未获取到管理员信息时，保守起见允许按钮显示，后端仍有权限校验
    return true
  }

  // 只有站点管理员才有权限限制，超级管理员(ADMIN)可以任意操作
  if (currentAdmin.value.role === 'STREET_ADMIN') {
    const adminStation = currentAdmin.value.station
    const nextStation = item.nextStation
    const currentStation = item.currentStation

    if (!adminStation) return false

    // 1. 如果状态是已揽收（待发货），此时通常没有下一站（或为默认值），允许当前站点（起始站）管理员操作
    if (item.status === 'collected') {
      if (currentStation && currentStation.trim() !== '') {
        return adminStation === currentStation || 
               adminStation.includes(currentStation) || 
               currentStation.includes(adminStation)
      }
      return false
    }

    // 2. 对于运输中或其他状态，如果有下一站信息，只有下一站的管理员可以更新（到达下一站的操作）
    // 注意：'校园驿站' 可能是默认占位符，如果下一站确实是校园驿站，则只有校园驿站管理员可操作
    if (nextStation && nextStation.trim() !== '') {
      return adminStation === nextStation || 
             adminStation.includes(nextStation) || 
             nextStation.includes(adminStation)
    }

    // 3. 如果没有下一站信息（兜底），允许当前站点管理员操作
    if (currentStation && currentStation.trim() !== '') {
      return adminStation === currentStation || 
             adminStation.includes(currentStation) || 
             currentStation.includes(adminStation)
    }
    
    return false
  }

  return true
}

// 包裹列表数据
const logisticsList = ref<LogisticsItem[]>([])

// 将后端包裹数据转换为前端展示格式
const convertParcelToLogistics = (parcel: Parcel): LogisticsItem => {
  // 根据后端状态映射前端物流状态
  const statusMap: Record<number, string> = {
    0: 'collected',    // 待发货 -> 已揽收
    1: 'in_transit',   // 已发货 -> 运输中
    2: 'arrived',      // 已入库 -> 已到站
    3: 'delivering'    // 退回/异常 -> 派送中
  }
  const frontStatus = parcel.isSigned === 1 ? 'signed' : (statusMap[parcel.status] || 'in_transit')
  
  return {
    id: parcel.id,
    trackingNumber: parcel.trackingNumber,
    company: parcel.company,
    origin: parcel.origin,
    destination: parcel.destination,
    currentStation: parcel.currentStation || '待分配',
    nextStation: parcel.nextStation || (parcel.status === 2 ? '' : '校园驿站'),
    etaNextStation: parcel.etaNextStation?.replace('T', ' ').substring(0, 16) || '',
    etaDelivered: parcel.etaDelivered?.replace('T', ' ').substring(0, 16) || '',
    status: frontStatus,
    updateTime: parcel.updateTime?.replace('T', ' ').substring(0, 16) || '',
    isDelayed: false,
    isDelivered: parcel.isDelivered,
    trackingHistory: [],
    pickupCode: parcel.pickupCode
  }
}

const filteredLogistics = computed(() => {
  let result = logisticsList.value
  if (filterCompany.value) {
    result = result.filter(l => l.company === filterCompany.value)
  }
  if (filterStatus.value) {
    result = result.filter(l => l.status === filterStatus.value)
  }
  return result
})

// 加载包裹列表
const loadParcelList = async () => {
  loading.value = true
  try {
    const res = await parcelApi.list(currentPage.value - 1, pageSize.value)
    logisticsList.value = res.content.map(convertParcelToLogistics)
    total.value = res.totalElements
    totalPages.value = res.totalPages || 1
    
    // 更新统计数据
    updateStats(res.content)
  } catch (error: any) {
    console.error('加载包裹列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 更新统计数据
const updateStats = (parcels: Parcel[]) => {
  stats.totalParcels = total.value
  stats.inTransit = parcels.filter(p => p.status === 1).length
  stats.arrived = parcels.filter(p => p.status === 2 && p.isSigned === 0).length
  stats.delayed = 0 // 需要后端提供超时数据
}

const getStatusLabel = (status?: string) => {
  const labels: Record<string, string> = {
    'collected': '已揽收',
    'in_transit': '运输中',
    'arrived': '已到站',
    'delivering': '派送中',
    'signed': '已签收'
  }
  return labels[status || ''] || '未知'
}

const searchLogistics = async () => {
  if (searchKeyword.value.trim()) {
    loading.value = true
    try {
      const parcel = await parcelApi.searchByTrackingNumber(searchKeyword.value.trim())
      if (parcel) {
        logisticsList.value = [convertParcelToLogistics(parcel)]
        total.value = 1
        totalPages.value = 1
      } else {
        logisticsList.value = []
        total.value = 0
      }
    } catch (error: any) {
      logisticsList.value = []
      total.value = 0
    } finally {
      loading.value = false
    }
  } else {
    loadParcelList()
  }
  currentPage.value = 1
}

const resetFilters = () => {
  searchKeyword.value = ''
  filterCompany.value = ''
  filterStatus.value = ''
  currentPage.value = 1
  loadParcelList()
}

const viewLogistics = async (item: LogisticsItem) => {
  currentLogistics.value = item
  
  // 加载真实的物流轨迹数据
  try {
    const routes = await parcelRouteApi.getByTrackingNumber(item.trackingNumber)
    if (routes && routes.length > 0) {
      // 将后端数据转换为前端展示格式
      currentLogistics.value.trackingHistory = routes.map(route => ({
        status: route.currentStation,
        time: route.createTime?.replace('T', ' ').substring(0, 16) || '',
        description: `从 ${route.currentStation} 发往 ${route.nextStation || '目的地'}`,
        location: route.currentStation
      }))
    } else {
      // 没有物流轨迹数据时显示默认信息
      currentLogistics.value.trackingHistory = [{
        status: '暂无物流信息',
        time: item.updateTime,
        description: '暂无详细物流轨迹',
        location: item.currentStation
      }]
    }
  } catch (error: any) {
    console.error('加载物流轨迹失败:', error)
    // 接口调用失败时显示默认信息
    currentLogistics.value.trackingHistory = [{
      status: '暂无物流信息',
      time: item.updateTime,
      description: '暂无详细物流轨迹',
      location: item.currentStation
    }]
  }
  
  showTrackingDetail.value = true
}

const updateRoute = async (item: LogisticsItem) => {
  // 已生成取件码的快件不能修改
  if (item.pickupCode) {
    warning('该快件已生成取件码，无法修改站点信息')
    return
  }
  currentLogistics.value = item
  
  // 自动设置当前站点：查询最新的流转记录，将上一站的下一站（nextStation）作为本次的当前站点
  let newCurrentStation = item.currentStation
  
  try {
    const routes = await parcelRouteApi.getByTrackingNumber(item.trackingNumber)
    if (routes && routes.length > 0) {
      // 获取最后一条记录
      const lastRoute = routes[routes.length - 1]
      
      // 检查是否已送达
      if (lastRoute.isDelivered === 1) {
        warning('该快件已标记为送达，无法继续更新物流信息')
        return
      }

      // 如果上一条记录有下一站，则当前站点应该是上一条记录的下一站
      if (lastRoute.nextStation) {
        newCurrentStation = lastRoute.nextStation
      }
    }
  } catch (error) {
    console.error('获取流转记录失败，使用默认当前站点', error)
  }

  // 权限检查：如果是站点管理员，只能操作本站点的包裹
  if (currentAdmin.value && currentAdmin.value.role === 'STREET_ADMIN') {
    const adminStation = currentAdmin.value.station
    // 宽松匹配逻辑，与后端保持一致
    const match = adminStation && newCurrentStation && 
                 (adminStation === newCurrentStation || 
                  adminStation.includes(newCurrentStation) || 
                  newCurrentStation.includes(adminStation))
    
    if (!match) {
      warning(`您无权操作位于 ${newCurrentStation} 的包裹（非本站包裹）`)
      return
    }
  }
  
  Object.assign(updateForm, {
    currentStation: newCurrentStation,
    nextStation: '', // 下一站需要管理员重新选择
    etaNextStation: '',
    // 将显示用的空格替换回 T，以便 datetime-local 组件识别
    etaDelivered: item.etaDelivered ? item.etaDelivered.replace(' ', 'T') : '',
    isDelivered: false,
    remark: ''
  })
  showUpdateRoute.value = true
}

const submitRoute = async () => {
  if (!routeForm.trackingNumber || !routeForm.currentStation || !routeForm.company) {
    warning('请填写快递单号、快递公司和当前站点')
    return
  }
  if (!routeForm.origin || !routeForm.destination) {
    warning('请填写起始站和终点站')
    return
  }
  if (!routeForm.nextStation) {
    warning('请填写下一站点')
    return
  }
  if (!routeForm.etaNextStation || !routeForm.etaDelivered) {
    warning('请填写预计到达时间')
    return
  }

  try {
    const requestData: any = {
      trackingNumber: routeForm.trackingNumber,
      company: routeForm.company,
      origin: routeForm.origin,
      destination: routeForm.destination,
      currentStation: routeForm.currentStation,
      nextStation: routeForm.nextStation,
      // datetime-local 返回的格式已经是 "2026-01-30T17:20"，直接添加秒即可
      etaNextStation: routeForm.etaNextStation ? routeForm.etaNextStation + ':00' : '',
      etaDelivered: routeForm.etaDelivered ? routeForm.etaDelivered + ':00' : ''
    }
    
    await parcelRouteApi.create(requestData)
    success('添加物流信息成功')
    showAddRoute.value = false
    searchLogistics()
    
    // 重置表单
    Object.assign(routeForm, {
      trackingNumber: '',
      company: '顺丰速运',
      origin: '',
      destination: '',
      currentStation: '',
      nextStation: '',
      etaNextStation: '',
      etaDelivered: ''
    })
  } catch (err: any) {
    showError(err.message || '添加物流信息失败')
  }
}

const submitUpdate = async () => {
  if (!currentLogistics.value) return

  if (!updateForm.currentStation) {
    warning('当前站点信息缺失')
    return
  }
  if (!updateForm.nextStation) {
    warning('请选择下一站点')
    return
  }
  
  try {
    // 创建新的物流路线记录
    const data: ParcelRouteCreateRequest = {
      trackingNumber: currentLogistics.value.trackingNumber,
      currentStation: updateForm.currentStation,
      nextStation: updateForm.nextStation,
      // datetime-local 返回的格式已经是 "2026-01-30T17:20"，直接添加秒即可
      etaNextStation: updateForm.etaNextStation ? updateForm.etaNextStation + ':00' : '',
      etaDelivered: updateForm.etaDelivered ? updateForm.etaDelivered + ':00' : '',
      isDelivered: updateForm.isDelivered ? 1 : 0
    }
    
    await parcelRouteApi.create(data)
    success('更新成功')
    showUpdateRoute.value = false
    loadParcelList()
  } catch (error: any) {
    showError(error.message || '更新失败')
  }
}

const markAsDelivered = async (item: LogisticsItem) => {
  if (!confirm('确定将该快件标记为已送达吗？标记后将无法再修改物流信息。')) {
    return
  }

  try {
    await parcelRouteApi.markDelivered(item.trackingNumber)
    success('标记送达成功')
    loadParcelList()
  } catch (error: any) {
    showError(error.message || '操作失败')
  }
}

// 监听分页变化
const handlePageChange = (delta: number) => {
  currentPage.value += delta
  loadParcelList()
}

const stations = ref<AdminStation[]>([])

// 加载站点列表
const loadStations = async () => {
  try {
    const res = await getAllStations()
    
    // 去重：当 station 编号相同时，只保留 id 最大的记录（最新的）
    const stationMap = new Map<string, AdminStation>()
    res.forEach(station => {
      const key = station.station
      const existing = stationMap.get(key)
      if (!existing || station.id > existing.id) {
        stationMap.set(key, station)
      }
    })
    
    // 转换为数组并按省份和城市排序
    stations.value = Array.from(stationMap.values()).sort((a, b) => {
      const provinceCompare = (a.province || '').localeCompare(b.province || '')
      if (provinceCompare !== 0) return provinceCompare
      
      const cityCompare = (a.city || '').localeCompare(b.city || '')
      if (cityCompare !== 0) return cityCompare
      
      return (a.station || '').localeCompare(b.station || '')
    })
  } catch (error) {
    console.error('加载站点列表失败:', error)
  }
}

// 页面加载时初始化
onMounted(async () => {
  try {
    const adminInfo = await getCurrentAdminDetail()
    currentAdmin.value = adminInfo
  } catch (error) {
    console.error('获取管理员信息失败:', error)
  }
  loadParcelList()
  loadStations()
})
</script>

<style scoped>
.logistics-management {
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
  min-width: 130px;
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

.logistics-table {
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

.tracking-number {
  font-family: 'Courier New', monospace;
  font-weight: 600;
  color: #808080;
  font-size: 13px;
}

.company-badge {
  padding: 4px 10px;
  background: #f0f0f0;
  border-radius: 4px;
  font-size: 13px;
}

.eta-text {
  font-size: 13px;
}

.eta-text.delayed {
  color: #f5222d;
}

.status-badge {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  display: inline-block;
}

/* 已揽收 - 浅灰 */
.status-badge.collected {
  background: #f5f5f5;
  color: #8c8c8c;
}

/* 运输中 - 淡蓝 */
.status-badge.in_transit {
  background: #f0f5ff;
  color: #597ef7;
}

/* 已到站 - 淡绿 */
.status-badge.arrived {
  background: #f6ffed;
  color: #73d13d;
}

/* 派送中 - 淡橙 */
.status-badge.delivering {
  background: #fff7e6;
  color: #ffa940;
}

/* 已签收 - 中灰 */
.status-badge.signed {
  background: #f0f0f0;
  color: #595959;
}

.btn-view, .btn-edit, .btn-delivery {
  padding: 6px 12px;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  border: 1px solid;
  transition: all 0.2s;
}

.btn-view {
  color: #808080;
  border-color: #808080;
  background: white;
}

.btn-view:hover {
  background: #f5f5f5;
}

.btn-edit {
  color: #1a73e8;
  border-color: #1a73e8;
  background: white;
}

.btn-edit:hover:not(:disabled) {
  background: #e8f0fe;
}

.btn-edit:disabled,
.btn-edit.disabled {
  color: #ccc;
  border-color: #eee;
  background: #f5f5f5;
  cursor: not-allowed;
}

.btn-delivery {
  color: #28a745;
  border-color: #28a745;
  background: white;
}

.btn-delivery:hover {
  background: #e6f9e6;
}

.btn-delivery:disabled,
.btn-delivery.disabled {
  color: #ccc;
  border-color: #eee;
  background: #f5f5f5;
  cursor: not-allowed;
}

.action-btns {
  display: flex;
  gap: 8px;
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

.info-row {
  margin-bottom: 20px;
  padding: 12px 16px;
  background: #f9f9f9;
  border-radius: 8px;
}

.info-row .label {
  color: #999;
  margin-right: 8px;
}

.info-row .value {
  font-weight: 600;
  color: #333;
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

/* Parcel Info Styles */
.parcel-info {
  background: #f9f9f9;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 24px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.info-item .label {
  font-size: 12px;
  color: #999;
}

.info-item .value {
  font-size: 14px;
  color: #333;
}

.info-item .value.tracking {
  font-family: 'Courier New', monospace;
  font-weight: 600;
  color: #808080;
}

/* Tracking Timeline */
.tracking-timeline h4 {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 20px;
}

.timeline {
  position: relative;
  padding-left: 28px;
}

.timeline-item {
  position: relative;
  padding-bottom: 24px;
}

.timeline-item:last-child {
  padding-bottom: 0;
}

.timeline-dot {
  position: absolute;
  left: -28px;
  top: 4px;
  width: 14px;
  height: 14px;
  background: #e0e0e0;
  border-radius: 50%;
  border: 3px solid white;
  box-shadow: 0 0 0 2px #e0e0e0;
}

.timeline-item.active .timeline-dot {
  background: #52c41a;
  box-shadow: 0 0 0 2px #52c41a;
}

.timeline-item::before {
  content: '';
  position: absolute;
  left: -22px;
  top: 20px;
  bottom: 0;
  width: 2px;
  background: #e0e0e0;
}

.timeline-item:last-child::before {
  display: none;
}

.timeline-content {
  background: #f5f5f5;
  border-radius: 8px;
  padding: 16px;
}

.timeline-item.active .timeline-content {
  background: #f6ffed;
}

.timeline-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.timeline-status {
  font-weight: 600;
  color: #333;
  font-size: 14px;
}

.timeline-time {
  font-size: 12px;
  color: #999;
}

.timeline-desc {
  font-size: 14px;
  color: #333;
  margin-bottom: 8px;
  line-height: 1.5;
}

.timeline-location {
  font-size: 12px;
  color: #999;
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
  .info-grid {
    grid-template-columns: 1fr;
  }
}
</style>
