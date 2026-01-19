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
          <button class="btn-secondary" @click="batchUpdateEta">批量更新ETA</button>
        </div>
      </div>

      <!-- 统计卡片 -->
      <div class="stats-cards">
        <div class="stat-card">
          <div class="stat-icon" style="background: #f5f5f5;">
            <img src="@/assets/icons/2.png" alt="在途包裹" class="icon-img" />
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.totalParcels }}</div>
            <div class="stat-label">在途包裹</div>
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
              <th>当前站点</th>
              <th>下一站点</th>
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
              <td>{{ item.currentStation }}</td>
              <td>{{ item.nextStation || '-' }}</td>
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
                  <button class="btn-edit" @click="updateRoute(item)">更新</button>
                  <button class="btn-refresh" @click="refreshLogistics(item)">刷新</button>
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
              <input type="text" v-model="updateForm.currentStation">
            </div>
            <div class="form-group">
              <label>下一站点</label>
              <input type="text" v-model="updateForm.nextStation">
            </div>
            <div class="form-group">
              <label>物流状态</label>
              <select v-model="updateForm.status">
                <option value="collected">已揽收</option>
                <option value="in_transit">运输中</option>
                <option value="arrived">已到站</option>
                <option value="delivering">派送中</option>
                <option value="signed">已签收</option>
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
import { ref, reactive, computed } from 'vue'
import AdminLayout from '@/layouts/AdminLayout.vue'

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
  currentStation: string
  nextStation: string
  etaNextStation: string
  etaDelivered: string
  status: string
  updateTime: string
  isDelayed: boolean
  trackingHistory: TrackingHistory[]
}

const searchKeyword = ref('')
const filterCompany = ref('')
const filterStatus = ref('')
const currentPage = ref(1)
const totalPages = ref(2)
const total = ref(18)

const showAddRoute = ref(false)
const showUpdateRoute = ref(false)
const showTrackingDetail = ref(false)
const currentLogistics = ref<LogisticsItem | null>(null)

const stats = reactive({
  totalParcels: 156,
  inTransit: 89,
  arrived: 45,
  delayed: 12
})

const routeForm = reactive({
  trackingNumber: '',
  currentStation: '',
  nextStation: '',
  etaNextStation: '',
  etaDelivered: ''
})

const updateForm = reactive({
  currentStation: '',
  nextStation: '',
  status: '',
  etaNextStation: '',
  etaDelivered: '',
  remark: ''
})

// 模拟数据
const logisticsList = ref<LogisticsItem[]>([
  {
    id: 1,
    trackingNumber: 'SF1234567890123',
    company: '顺丰速运',
    currentStation: '上海浦东转运中心',
    nextStation: '校园驿站',
    etaNextStation: '2026-01-19 18:00',
    etaDelivered: '2026-01-19 20:00',
    status: 'in_transit',
    updateTime: '2026-01-19 14:30',
    isDelayed: false,
    trackingHistory: [
      { status: '运输中', time: '2026-01-19 14:30', description: '快件已到达上海浦东转运中心', location: '上海市浦东新区' },
      { status: '运输中', time: '2026-01-19 08:00', description: '快件已从杭州转运中心发出', location: '杭州市萧山区' },
      { status: '运输中', time: '2026-01-18 22:15', description: '快件已到达杭州转运中心', location: '杭州市萧山区' },
      { status: '已揽收', time: '2026-01-18 16:30', description: '快递员已揽收', location: '杭州市西湖区' },
    ]
  },
  {
    id: 2,
    trackingNumber: 'YT9876543210987',
    company: '圆通速递',
    currentStation: '校园驿站',
    nextStation: '',
    etaNextStation: '',
    etaDelivered: '2026-01-19 12:00',
    status: 'arrived',
    updateTime: '2026-01-19 10:00',
    isDelayed: false,
    trackingHistory: [
      { status: '已到站', time: '2026-01-19 10:00', description: '快件已到达校园驿站，请及时取件', location: '校园驿站' },
      { status: '派送中', time: '2026-01-19 08:30', description: '快件正在派送中', location: '上海市杨浦区' },
      { status: '运输中', time: '2026-01-18 20:00', description: '快件已到达上海集散中心', location: '上海市青浦区' },
    ]
  },
  {
    id: 3,
    trackingNumber: 'ZT1122334455667',
    company: '中通快递',
    currentStation: '南京转运中心',
    nextStation: '上海转运中心',
    etaNextStation: '2026-01-20 06:00',
    etaDelivered: '2026-01-20 18:00',
    status: 'in_transit',
    updateTime: '2026-01-19 16:00',
    isDelayed: false,
    trackingHistory: [
      { status: '运输中', time: '2026-01-19 16:00', description: '快件已到达南京转运中心', location: '南京市江宁区' },
      { status: '运输中', time: '2026-01-19 10:00', description: '快件已从合肥转运中心发出', location: '合肥市蜀山区' },
      { status: '已揽收', time: '2026-01-18 14:00', description: '快递员已揽收', location: '合肥市包河区' },
    ]
  },
  {
    id: 4,
    trackingNumber: 'ST5566778899001',
    company: '申通快递',
    currentStation: '上海青浦仓',
    nextStation: '杨浦区配送站',
    etaNextStation: '2026-01-19 16:00',
    etaDelivered: '2026-01-19 18:00',
    status: 'delivering',
    updateTime: '2026-01-19 15:30',
    isDelayed: true,
    trackingHistory: [
      { status: '派送中', time: '2026-01-19 15:30', description: '快件正在派送中，预计18:00前送达', location: '上海市杨浦区' },
      { status: '运输中', time: '2026-01-19 08:00', description: '快件已到达上海青浦仓', location: '上海市青浦区' },
    ]
  },
  {
    id: 5,
    trackingNumber: 'JT6677889900112',
    company: '极兔速递',
    currentStation: '校园驿站',
    nextStation: '',
    etaNextStation: '',
    etaDelivered: '2026-01-18 12:00',
    status: 'signed',
    updateTime: '2026-01-18 11:30',
    isDelayed: false,
    trackingHistory: [
      { status: '已签收', time: '2026-01-18 11:30', description: '已签收，签收人：本人', location: '校园驿站' },
      { status: '已到站', time: '2026-01-18 09:00', description: '快件已到达校园驿站', location: '校园驿站' },
      { status: '派送中', time: '2026-01-18 07:30', description: '快件正在派送中', location: '上海市杨浦区' },
    ]
  },
  {
    id: 6,
    trackingNumber: 'YD2233445566778',
    company: '韵达快递',
    currentStation: '郑州转运中心',
    nextStation: '上海转运中心',
    etaNextStation: '2026-01-20 12:00',
    etaDelivered: '2026-01-21 18:00',
    status: 'in_transit',
    updateTime: '2026-01-19 12:00',
    isDelayed: true,
    trackingHistory: [
      { status: '运输中', time: '2026-01-19 12:00', description: '快件已到达郑州转运中心', location: '郑州市新郑市' },
      { status: '运输中', time: '2026-01-18 18:00', description: '快件已从西安转运中心发出', location: '西安市长安区' },
      { status: '已揽收', time: '2026-01-17 16:00', description: '快递员已揽收', location: '西安市雁塔区' },
    ]
  },
  {
    id: 7,
    trackingNumber: 'SF9988776655443',
    company: '顺丰速运',
    currentStation: '广州转运中心',
    nextStation: '上海转运中心',
    etaNextStation: '2026-01-20 08:00',
    etaDelivered: '2026-01-20 20:00',
    status: 'collected',
    updateTime: '2026-01-19 09:00',
    isDelayed: false,
    trackingHistory: [
      { status: '已揽收', time: '2026-01-19 09:00', description: '快递员已揽收', location: '广州市天河区' },
    ]
  },
])

const filteredLogistics = computed(() => {
  let result = logisticsList.value
  if (filterCompany.value) {
    result = result.filter(l => l.company === filterCompany.value)
  }
  if (filterStatus.value) {
    result = result.filter(l => l.status === filterStatus.value)
  }
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(l => l.trackingNumber.toLowerCase().includes(keyword))
  }
  return result
})

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

const searchLogistics = () => {
  currentPage.value = 1
}

const resetFilters = () => {
  searchKeyword.value = ''
  filterCompany.value = ''
  filterStatus.value = ''
  currentPage.value = 1
}

const viewLogistics = (item: LogisticsItem) => {
  currentLogistics.value = item
  showTrackingDetail.value = true
}

const updateRoute = (item: LogisticsItem) => {
  currentLogistics.value = item
  Object.assign(updateForm, {
    currentStation: item.currentStation,
    nextStation: item.nextStation,
    status: item.status,
    etaNextStation: '',
    etaDelivered: '',
    remark: ''
  })
  showUpdateRoute.value = true
}

const refreshLogistics = (item: LogisticsItem) => {
  alert(`刷新物流信息：${item.trackingNumber}（模拟）`)
}

const submitRoute = () => {
  if (!routeForm.trackingNumber || !routeForm.currentStation) {
    alert('请填写快递单号和当前站点')
    return
  }
  alert('添加成功（模拟）')
  showAddRoute.value = false
  Object.assign(routeForm, {
    trackingNumber: '',
    currentStation: '',
    nextStation: '',
    etaNextStation: '',
    etaDelivered: ''
  })
}

const submitUpdate = () => {
  if (!updateForm.currentStation) {
    alert('请填写当前站点')
    return
  }
  alert('更新成功（模拟）')
  showUpdateRoute.value = false
}

const batchUpdateEta = () => {
  alert('批量更新ETA（模拟）')
}
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
}

.status-badge.collected {
  background: #f0f0f0;
  color: #666;
}

.status-badge.in_transit {
  background: #e6f7ff;
  color: #1890ff;
}

.status-badge.arrived {
  background: #f6ffed;
  color: #52c41a;
}

.status-badge.delivering {
  background: #fff7e6;
  color: #fa8c16;
}

.status-badge.signed {
  background: #f0f0f0;
  color: #333;
}

.action-btns {
  display: flex;
  gap: 8px;
}

.btn-view, .btn-edit, .btn-refresh {
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  transition: all 0.2s;
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

.btn-refresh {
  background: white;
  color: #52c41a;
  border: 1px solid #52c41a;
}

.btn-refresh:hover {
  background: #52c41a;
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
  background: white;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  padding: 16px;
}

.timeline-item.active .timeline-content {
  background: #f6ffed;
  border-color: #b7eb8f;
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
