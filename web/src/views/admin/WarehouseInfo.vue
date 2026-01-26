<template>
  <AdminLayout>
    <div class="warehouse-info">
      <div class="page-header">
        <h1>仓库信息</h1>
        <p>查看驿站仓库容量统计与快递搜索</p>
      </div>

      <!-- 搜索功能 -->
      <div class="search-card">
        <div class="card-header">
          <h2><img src="@/assets/icons/2.png" alt="" class="title-icon" /> 快递搜索</h2>
          <button class="btn-reset" @click="resetSearch">重置</button>
        </div>
        <div class="search-form">
          <div class="form-row">
            <div class="form-group">
              <label>区域</label>
              <select v-model="searchForm.area">
                <option value="">全部</option>
                <option value="A">A区</option>
                <option value="B">B区</option>
                <option value="C">C区</option>
                <option value="D">D区</option>
              </select>
            </div>
            <div class="form-group">
              <label>货架号</label>
              <input type="text" v-model="searchForm.shelf" placeholder="如：01、02、03">
            </div>
            <div class="form-group">
              <label>位置码</label>
              <input type="text" v-model="searchForm.position" placeholder="四位数位置码">
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>收件人姓名</label>
              <input type="text" v-model="searchForm.receiverName" placeholder="输入收件人姓名">
            </div>
            <div class="form-group">
              <label>收件人电话</label>
              <input type="text" v-model="searchForm.receiverPhone" placeholder="输入收件人电话">
            </div>
            <div class="form-group">
              <button class="btn-search" @click="handleSearch">搜索</button>
            </div>
          </div>
        </div>
        
        <!-- 搜索结果 -->
        <div class="search-results" v-if="searchResults.length > 0">
          <h3>搜索结果（{{ searchResults.length }} 条）</h3>
          <div class="results-table">
            <table>
              <thead>
                <tr>
                  <th>快递单号</th>
                  <th>收件人</th>
                  <th>电话</th>
                  <th>取件码</th>
                  <th>位置</th>
                  <th>状态</th>
                  <th>入库时间</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="item in searchResults" :key="item.id">
                  <td>{{ item.trackingNumber }}</td>
                  <td>{{ item.receiverName || '-' }}</td>
                  <td>{{ item.receiverPhone || '-' }}</td>
                  <td><span class="pickup-code">{{ item.pickupCode || '-' }}</span></td>
                  <td>{{ item.area }}区-{{ item.shelf }}号货架-{{ item.position }}</td>
                  <td>
                    <span :class="['status-badge', item.isSigned === 1 ? 'signed' : 'waiting']">
                      {{ item.isSigned === 1 ? '已取件' : '待取件' }}
                    </span>
                  </td>
                  <td>{{ formatTime(item.createTime) }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
        <div class="no-results" v-else-if="hasSearched">
          <p>未找到符合条件的快递</p>
        </div>
      </div>

      <!-- 容量统计卡片 -->
      <div class="stats-section">
        <h2><img src="@/assets/icons/7.png" alt="" class="title-icon" />容量统计</h2>
        <div class="capacity-cards">
          <div class="capacity-card">
            <div class="capacity-header">
              <span class="capacity-title">总存储容量</span>
              <span class="capacity-badge total">{{ capacityStats.totalCapacity }} 格</span>
            </div>
            <div class="capacity-bar">
              <div 
                class="capacity-fill" 
                :style="{ width: capacityStats.usageRate + '%' }"
              ></div>
            </div>
            <div class="capacity-detail">
              <span>已使用 {{ capacityStats.usedCapacity }} 格</span>
              <span>剩余 {{ capacityStats.remainCapacity }} 格</span>
            </div>
          </div>

          <div class="capacity-card today-card">
            <div class="capacity-title">今日入库量</div>
            <div class="capacity-number today-in">{{ capacityStats.todayIn }}</div>
            <div class="capacity-unit">件</div>
          </div>

          <div class="capacity-card today-card">
            <div class="capacity-title">今日出库量</div>
            <div class="capacity-number today-out">{{ capacityStats.todayOut }}</div>
            <div class="capacity-unit">件</div>
          </div>

          <div class="capacity-card">
            <div class="capacity-header">
              <span class="capacity-title">滞留包裹</span>
              <span class="capacity-badge warning">{{ capacityStats.overdueParcels }} 件</span>
            </div>
            <div class="capacity-desc">超过7天未取</div>
          </div>
        </div>
      </div>
      
      <!-- 各区域容量 -->
      <div class="zone-capacity-section">
        <h2><img src="@/assets/icons/13.png" alt="" class="title-icon" />各区域容量</h2>
        <div class="zone-cards">
          <div class="zone-card" v-for="zone in zoneCapacities" :key="zone.id">
            <div class="zone-header">
              <span class="zone-name">{{ zone.name }}</span>
              <span :class="['zone-status', zone.status]">{{ getZoneStatusLabel(zone.status) }}</span>
            </div>
            <div class="zone-progress">
              <div class="zone-bar">
                <div 
                  class="zone-fill" 
                  :style="{ width: zone.usageRate + '%' }"
                  :class="{ warning: zone.usageRate > 80, danger: zone.usageRate > 95 }"
                ></div>
              </div>
              <span class="zone-rate">{{ zone.usageRate }}%</span>
            </div>
            <div class="zone-detail">
              <span>{{ zone.usedSlots }} / {{ zone.totalSlots }} 格</span>
              <span>{{ zone.shelfCount }} 个货架</span>
            </div>
          </div>
        </div>
      </div>


    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import AdminLayout from '@/layouts/AdminLayout.vue'
import { useToast } from '@/composables/useToast'
import { searchStorage, type StationStorage } from '@/api/admin/stationStorage'

const { error } = useToast()

interface ZoneCapacity {
  id: number
  name: string
  totalSlots: number
  usedSlots: number
  usageRate: number
  shelfCount: number
  status: string
}

const storageList = ref<StationStorage[]>([])
const loading = ref(false)
const searchResults = ref<StationStorage[]>([])
const hasSearched = ref(false)

// 搜索表单
const searchForm = reactive({
  area: '',
  shelf: '',
  position: '',
  receiverName: '',
  receiverPhone: ''
})

// 货架配置常量
const AREAS = ['A', 'B', 'C', 'D'] // 固定4个区域
const SHELVES_PER_AREA = 10 // 每个区域10个货架
const MAX_CAPACITY_PER_SHELF = 50 // 每个货架最大容量50件
const TOTAL_CAPACITY = AREAS.length * SHELVES_PER_AREA * MAX_CAPACITY_PER_SHELF // 总容量 = 2000件

// 加载仓库数据
const loadWarehouseData = async () => {
  loading.value = true
  try {
    storageList.value = await searchStorage()
  } catch (e) {
    error('加载仓库数据失败')
  } finally {
    loading.value = false
  }
}

// 容量统计（根据实际数据计算）
const capacityStats = computed(() => {
  const inStockList = storageList.value.filter(s => s.isSigned !== 1)
  const usedCapacity = inStockList.length
  const remainCapacity = TOTAL_CAPACITY - usedCapacity
  const usageRate = Math.round((usedCapacity / TOTAL_CAPACITY) * 100)
  
  // 获取今天的日期（用于筛选今日数据）
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  
  const todayIn = storageList.value.filter(s => {
    if (!s.createTime) return false
    const createDate = new Date(s.createTime)
    createDate.setHours(0, 0, 0, 0)
    return createDate.getTime() === today.getTime()
  }).length
  
  const todayOut = storageList.value.filter(s => {
    if (!s.updateTime || s.isSigned !== 1) return false
    const updateDate = new Date(s.updateTime)
    updateDate.setHours(0, 0, 0, 0)
    return updateDate.getTime() === today.getTime()
  }).length
  
  // 计算滞留包裹（超过7天未取）
  const sevenDaysAgo = new Date()
  sevenDaysAgo.setDate(sevenDaysAgo.getDate() - 7)
  const overdueParcels = inStockList.filter(s => {
    if (!s.createTime) return false
    return new Date(s.createTime) < sevenDaysAgo
  }).length
  
  return {
    totalCapacity: TOTAL_CAPACITY,
    usedCapacity,
    remainCapacity,
    usageRate,
    todayIn,
    todayOut,
    overdueParcels
  }
})

// 各区域容量（根据实际数据计算）
const zoneCapacities = computed<ZoneCapacity[]>(() => {
  return AREAS.map((area, index) => {
    const areaStorages = storageList.value.filter(s => s.area === area && s.isSigned !== 1)
    const totalSlots = SHELVES_PER_AREA * MAX_CAPACITY_PER_SHELF
    const usedSlots = areaStorages.length
    const usageRate = Math.round((usedSlots / totalSlots) * 100)
    
    let status = 'normal'
    if (usageRate >= 95) status = 'full'
    else if (usageRate >= 80) status = 'warning'
    
    return {
      id: index + 1,
      name: `${area}区`,
      totalSlots,
      usedSlots,
      usageRate,
      shelfCount: SHELVES_PER_AREA,
      status
    }
  })
})

// 搜索功能
const handleSearch = async () => {
  loading.value = true
  hasSearched.value = true
  try {
    const params: any = {}
    if (searchForm.area) params.area = searchForm.area
    if (searchForm.shelf) params.shelf = searchForm.shelf
    if (searchForm.position) params.position = searchForm.position
    if (searchForm.receiverName) params.receiverName = searchForm.receiverName
    if (searchForm.receiverPhone) params.receiverPhone = searchForm.receiverPhone
    
    searchResults.value = await searchStorage(params)
  } catch (e) {
    error('搜索失败')
    searchResults.value = []
  } finally {
    loading.value = false
  }
}

// 重置搜索
const resetSearch = () => {
  searchForm.area = ''
  searchForm.shelf = ''
  searchForm.position = ''
  searchForm.receiverName = ''
  searchForm.receiverPhone = ''
  searchResults.value = []
  hasSearched.value = false
}

// 格式化时间
const formatTime = (time?: string) => {
  if (!time) return '-'
  const date = new Date(time)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

onMounted(() => {
  loadWarehouseData()
})

const getZoneStatusLabel = (status: string) => {
  const labels: Record<string, string> = {
    'normal': '正常',
    'full': '已满',
    'warning': '紧张'
  }
  return labels[status] || '未知'
}
</script>

<style scoped>
.warehouse-info {
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

h2 {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.title-icon {
  width: 24px;
  height: 24px;
  object-fit: contain;
  vertical-align: middle;
}

.search-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.card-header h2 {
  margin-bottom: 0;
}

.btn-reset {
  padding: 8px 16px;
  background: white;
  color: #666;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.btn-reset:hover {
  background: #f5f5f5;
}

.search-form {
  margin-bottom: 24px;
}

.search-form .form-group {
  flex: 1;
}

.search-form label {
  display: block;
  font-size: 14px;
  color: #333;
  margin-bottom: 8px;
  font-weight: 500;
}

.search-form input,
.search-form select {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
}

.btn-search {
  width: 100%;
  margin-top: 28px;
  padding: 10px 24px;
  background: #808080;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-search:hover {
  background: #666666;
}

.search-results {
  margin-top: 24px;
}

.search-results h3 {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 16px;
}

.results-table {
  overflow-x: auto;
}

.results-table table {
  width: 100%;
  border-collapse: collapse;
}

.results-table thead {
  background: #f5f7fa;
}

.results-table th,
.results-table td {
  padding: 12px 16px;
  text-align: left;
  font-size: 14px;
}

.results-table th {
  font-weight: 600;
  color: #666;
}

.results-table td {
  border-top: 1px solid #f0f0f0;
  color: #333;
}

.pickup-code {
  font-family: 'Courier New', monospace;
  font-weight: 600;
  color: #808080;
  background: #f5f5f5;
  padding: 2px 8px;
  border-radius: 4px;
}

.status-badge {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.status-badge.waiting {
  background: #fff7e6;
  color: #fa8c16;
}

.status-badge.signed {
  background: #f6ffed;
  color: #52c41a;
}

.no-results {
  text-align: center;
  padding: 40px;
  color: #999;
}

.stats-section {
  background: white;
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.capacity-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.capacity-card {
  background: #f9f9f9;
  border-radius: 12px;
  padding: 20px;
}

.capacity-card.today-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
}

.capacity-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.capacity-title {
  font-size: 14px;
  color: #666;
}

.today-card .capacity-title {
  margin-bottom: 12px;
}

.capacity-number {
  font-size: 36px;
  font-weight: 700;
  line-height: 1;
  margin-bottom: 6px;
}

.capacity-number.today-in {
  color: #52c41a;
}

.capacity-number.today-out {
  color: #1890ff;
}

.capacity-unit {
  font-size: 14px;
  color: #999;
}

.capacity-badge {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 600;
}

.capacity-badge.total {
  background: #e6f7ff;
  color: #1890ff;
}

.capacity-badge.today {
  background: #f6ffed;
  color: #52c41a;
}

.capacity-badge.warning {
  background: #fff7e6;
  color: #fa8c16;
}

.capacity-bar {
  height: 8px;
  background: #e0e0e0;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 12px;
}

.capacity-fill {
  height: 100%;
  background: linear-gradient(90deg, #52c41a, #73d13d);
  border-radius: 4px;
  transition: width 0.3s;
}

.capacity-detail {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #999;
}

.capacity-desc {
  font-size: 12px;
  color: #999;
}

.zone-capacity-section {
  background: white;
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.zone-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.zone-card {
  background: #f9f9f9;
  border-radius: 12px;
  padding: 20px;
}

.zone-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.zone-name {
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

.zone-status {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.zone-status.normal {
  background: #f6ffed;
  color: #52c41a;
}

.zone-status.full {
  background: #fff1f0;
  color: #f5222d;
}

.zone-status.warning {
  background: #fff7e6;
  color: #fa8c16;
}

.zone-progress {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.zone-bar {
  flex: 1;
  height: 8px;
  background: #e0e0e0;
  border-radius: 4px;
  overflow: hidden;
}

.zone-fill {
  height: 100%;
  background: #52c41a;
  border-radius: 4px;
  transition: width 0.3s;
}

.zone-fill.warning {
  background: #faad14;
}

.zone-fill.danger {
  background: #f5222d;
}

.zone-rate {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  min-width: 40px;
}

.zone-detail {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #999;
}

.form-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}

@media (max-width: 1200px) {
  .capacity-cards,
  .zone-cards {
    grid-template-columns: repeat(2, 1fr);
  }
  .form-row {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .capacity-cards,
  .zone-cards,
  .form-row {
    grid-template-columns: 1fr;
  }
}
</style>
