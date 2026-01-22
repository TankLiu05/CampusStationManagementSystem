<template>
  <AdminLayout>
    <div class="warehouse-management">
      <div class="page-header">
        <h1>仓库管理</h1>
        <p>查看驿站各区域货架使用情况</p>
      </div>

      <!-- 统计卡片 -->
      <div class="stats-cards">
        <div class="stat-card">
          <div class="stat-icon" style="background: #f5f5f5;">
            <img src="@/assets/icons/19.png" alt="存储区域" class="icon-img" />
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.totalZones }}</div>
            <div class="stat-label">存储区域</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon" style="background: #f5f5f5;">
            <img src="@/assets/icons/10.png" alt="货架总数" class="icon-img" />
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.totalShelves }}</div>
            <div class="stat-label">货架总数</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon" style="background: #f5f5f5;">
            <img src="@/assets/icons/12.png" alt="在库快递" class="icon-img" />
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.inStockCount }}</div>
            <div class="stat-label">在库快递</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon" style="background: #f5f5f5;">
            <img src="@/assets/icons/9.png" alt="已取件" class="icon-img" />
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.pickedCount }}</div>
            <div class="stat-label">已取件</div>
          </div>
        </div>
      </div>

      <!-- 区域选择 -->
      <div class="zone-tabs">
        <button 
          v-for="zone in zones" 
          :key="zone.id"
          :class="['zone-tab', { active: currentZone === zone.id }]"
          @click="currentZone = zone.id"
        >
          {{ zone.name }} ({{ zone.count }})
        </button>
      </div>

      <!-- 货架列表 -->
      <div class="shelves-table">
        <table>
          <thead>
            <tr>
              <th>货架编号</th>
              <th>所属区域</th>
              <th>在库数量</th>
              <th>已取数量</th>
              <th>总计</th>
              <th>快递公司</th>
              <th>在库率</th>
              <th>状态</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="storageLoading">
              <td colspan="8" class="empty-row">加载中...</td>
            </tr>
            <tr v-else-if="filteredShelves.length === 0">
              <td colspan="8" class="empty-row">暂无货架数据</td>
            </tr>
            <tr v-for="shelf in filteredShelves" :key="`${shelf.area}-${shelf.shelf}`">
              <td><span class="shelf-code">{{ shelf.area }}-{{ shelf.shelf }}</span></td>
              <td>{{ shelf.area }}区</td>
              <td>{{ shelf.inStock }}</td>
              <td>{{ shelf.picked }}</td>
              <td>{{ shelf.total }}</td>
              <td>
                <div class="company-tags">
                  <span 
                    v-for="company in getCompanies(shelf)" 
                    :key="company.name" 
                    class="company-tag"
                    :title="`${company.name}: ${company.count}件`"
                  >
                    {{ company.name }}({{ company.count }})
                  </span>
                  <span v-if="getCompanies(shelf).length === 0">-</span>
                </div>
              </td>
              <td>
                <div class="usage-bar">
                  <div 
                    class="usage-fill" 
                    :style="{ width: getUsageRate(shelf) + '%', background: getUsageColor(getUsageRate(shelf)) }"
                  ></div>
                </div>
                <span class="usage-text">{{ getUsageRate(shelf) }}%</span>
              </td>
              <td>
                <span :class="['status-badge', getShelfStatus(shelf)]">
                  {{ getStatusLabel(shelf) }}
                </span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import AdminLayout from '@/layouts/AdminLayout.vue'
import { useToast } from '@/composables/useToast'
import { searchStorage, type StationStorage } from '@/api/admin/stationStorage'

const { error } = useToast()

const storageList = ref<StationStorage[]>([])
const storageLoading = ref(false)
const currentZone = ref('')
let refreshTimer: number | null = null

// 货架配置常量
const AREAS = ['A', 'B', 'C', 'D'] // 固定4个区域
const SHELVES_PER_AREA = 10 // 每个区域10个货架
const MAX_CAPACITY_PER_SHELF = 50 // 每个货架最大容量50件

// 加载数据
const loadData = async () => {
  storageLoading.value = true
  try {
    // 调用仓库接口获取所有库存数据
    storageList.value = await searchStorage()
  } catch (e) {
    error('加载数据失败')
  } finally {
    storageLoading.value = false
  }
}

onMounted(() => {
  loadData()
  // 每30秒自动刷新一次数据
  refreshTimer = window.setInterval(() => {
    loadData()
  }, 30000)
})

onUnmounted(() => {
  // 清除定时器
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
})

// 扩展货架统计数据
interface ShelfStat {
  area: string
  shelf: string
  inStock: number
  picked: number
  total: number
  maxCapacity: number
  storages: StationStorage[]
}

const shelfStats = computed<ShelfStat[]>(() => {
  // 初始化所有固定的货架（4个区域 × 10个货架 = 40个货架）
  const shelfMap = new Map<string, ShelfStat>()
  
  AREAS.forEach(area => {
    for (let shelfNum = 1; shelfNum <= SHELVES_PER_AREA; shelfNum++) {
      const key = `${area}-${shelfNum}`
      shelfMap.set(key, {
        area,
        shelf: String(shelfNum),
        inStock: 0,
        picked: 0,
        total: 0,
        maxCapacity: MAX_CAPACITY_PER_SHELF,
        storages: []
      })
    }
  })
  
  // 统计实际数据
  storageList.value.forEach(storage => {
    if (!storage.area || !storage.shelf) return // 没有位置信息的跳过
    
    const key = `${storage.area}-${storage.shelf}`
    const stat = shelfMap.get(key)
    if (stat) {
      stat.total++
      if (storage.isSigned === 1) {
        stat.picked++
      } else {
        stat.inStock++
      }
      stat.storages.push(storage)
    }
  })
  
  return Array.from(shelfMap.values()).sort((a, b) => {
    if (a.area !== b.area) return a.area.localeCompare(b.area)
    return parseInt(a.shelf) - parseInt(b.shelf)
  })
})

// 区域列表（固定显示所有区域）
const zones = computed(() => {
  return [
    { id: '', name: '全部区域', count: AREAS.length * SHELVES_PER_AREA },
    ...AREAS.map(area => ({
      id: area,
      name: `${area}区`,
      count: SHELVES_PER_AREA
    }))
  ]
})

// 筛选后的货架
const filteredShelves = computed(() => {
  if (!currentZone.value) return shelfStats.value
  return shelfStats.value.filter(s => s.area === currentZone.value)
})

// 统计数据（固定区域和货架数量）
const stats = computed(() => {
  const inStock = storageList.value.filter(s => s.isSigned !== 1).length
  const picked = storageList.value.filter(s => s.isSigned === 1).length
  return {
    totalZones: AREAS.length, // 固定4个区域
    totalShelves: AREAS.length * SHELVES_PER_AREA, // 固定40个货架
    inStockCount: inStock,
    pickedCount: picked
  }
})

// 获取快递公司统计（仓库表没有company字段，暂不显示）
const getCompanies = (shelf: ShelfStat): Array<{ name: string; count: number }> => {
  // StationStorage 表中没有快递公司字段
  // 如果需要显示，需要关联查询 parcel 表或修改后端接口
  return []
}

// 计算在库率（基于货架最大容量）
const getUsageRate = (shelf: ShelfStat) => {
  if (shelf.maxCapacity === 0) return 0
  return Math.round((shelf.inStock / shelf.maxCapacity) * 100)
}

// 根据在库率返回颜色
const getUsageColor = (rate: number) => {
  if (rate === 0) return '#e0e0e0'
  if (rate < 50) return '#52c41a'
  if (rate < 80) return '#faad14'
  return '#f5222d'
}

// 获取货架状态
const getShelfStatus = (shelf: ShelfStat) => {
  if (shelf.inStock === 0) return 'empty'
  if (getUsageRate(shelf) >= 80) return 'busy'
  return 'normal'
}

// 获取状态标签
const getStatusLabel = (shelf: ShelfStat) => {
  if (shelf.inStock === 0) return '空闲'
  if (getUsageRate(shelf) >= 80) return '繁忙'
  return '正常'
}
</script>

<style scoped>
.warehouse-management {
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

.zone-tabs {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.zone-tab {
  padding: 10px 20px;
  border: 1px solid #e0e0e0;
  border-radius: 20px;
  background: white;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.zone-tab.active {
  background: #808080;
  color: white;
  border-color: #808080;
}

.shelves-table {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
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

.shelf-code {
  font-family: 'Courier New', monospace;
  font-weight: 600;
  color: #808080;
}

.usage-bar {
  width: 80px;
  height: 8px;
  background: #f0f0f0;
  border-radius: 4px;
  overflow: hidden;
  display: inline-block;
  vertical-align: middle;
  margin-right: 8px;
}

.usage-fill {
  height: 100%;
  border-radius: 4px;
  transition: width 0.3s;
}

.usage-text {
  font-size: 12px;
  color: #666;
}

.status-badge {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.status-badge.empty {
  background: #f0f0f0;
  color: #999;
}

.status-badge.normal {
  background: #f6ffed;
  color: #52c41a;
}

.status-badge.busy {
  background: #fff7e6;
  color: #fa8c16;
}

.company-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  align-items: center;
}

.company-tag {
  padding: 2px 8px;
  background: #f0f0f0;
  border-radius: 4px;
  font-size: 12px;
  color: #666;
  white-space: nowrap;
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
}
</style>
