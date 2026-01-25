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
          {{ zone.name }}
        </button>
      </div>

      <!-- 包裹列表 -->
      <div class="shelves-table">
        <table>
          <thead>
            <tr>
              <th>快递单号</th>
              <th>收件人</th>
              <th>手机号</th>
              <th>取件码</th>
              <th>存放位置</th>
              <th>入库时间</th>
              <th>状态</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="storageLoading">
              <td colspan="7" class="empty-row">加载中...</td>
            </tr>
            <tr v-else-if="filteredStorages.length === 0">
              <td colspan="7" class="empty-row">暂无包裹数据</td>
            </tr>
            <tr v-for="item in filteredStorages" :key="item.id">
              <td>{{ item.trackingNumber }}</td>
              <td>{{ item.receiverName || '-' }}</td>
              <td>{{ item.receiverPhone || '-' }}</td>
              <td><span class="pickup-code">{{ item.pickupCode || '-' }}</span></td>
              <td>
                <span class="location-tag">
                  {{ formatLocation(item) }}
                </span>
              </td>
              <td>{{ formatDate(item.createTime) }}</td>
              <td>
                <span :class="['status-badge', item.isSigned === 1 ? 'signed' : 'instock']">
                  {{ item.isSigned === 1 ? '已取件' : '在库' }}
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

const AREAS = ['A', 'B', 'C', 'D']

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

// 区域列表
const zones = computed(() => {
  return [
    { id: '', name: '全部区域' },
    ...AREAS.map(area => ({
      id: area,
      name: `${area}区`
    }))
  ]
})

// 筛选后的包裹列表
const filteredStorages = computed(() => {
  if (!currentZone.value) return storageList.value
  return storageList.value.filter(s => s.area === currentZone.value)
})

// 统计数据
const stats = computed(() => {
  const inStock = storageList.value.filter(s => s.isSigned !== 1).length
  const picked = storageList.value.filter(s => s.isSigned === 1).length
  // 计算实际使用的区域数
  const usedZones = new Set(storageList.value.map(s => s.area).filter(Boolean)).size
  // 计算实际使用的货架数
  const usedShelves = new Set(storageList.value.map(s => `${s.area}-${s.shelf}`).filter(s => !s.startsWith('undefined-'))).size
  
  return {
    totalZones: usedZones || 0,
    totalShelves: usedShelves || 0,
    inStockCount: inStock,
    pickedCount: picked
  }
})

// 格式化位置
const formatLocation = (item: StationStorage) => {
  if (!item.area || !item.shelf || !item.position) return '待分配'
  return `${item.area}区-${item.shelf}号货架-${item.position}`
}

// 格式化日期
const formatDate = (dateStr?: string) => {
  if (!dateStr) return '-'
  return dateStr.replace('T', ' ').substring(0, 16)
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

.pickup-code {
  font-family: 'Courier New', monospace;
  font-weight: 700;
  color: #1890ff;
  font-size: 16px;
}

.location-tag {
  background: #f0f5ff;
  color: #2f54eb;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.status-badge {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.status-badge.instock {
  background: #f6ffed;
  color: #52c41a;
}

.status-badge.signed {
  background: #f5f5f5;
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
}
</style>
