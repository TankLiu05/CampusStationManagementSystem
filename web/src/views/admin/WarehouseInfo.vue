<template>
  <AdminLayout>
    <div class="warehouse-info">
      <div class="page-header">
        <h1>仓库信息</h1>
        <p>查看驿站仓库基本信息与容量统计</p>
      </div>

      <!-- 仓库基本信息卡片 -->
      <div class="info-card">
        <div class="card-header">
          <h2><img src="@/assets/icons/2.png" alt="" class="title-icon" /> 基本信息</h2>
          <button class="btn-edit" @click="showEditInfo = true">编辑信息</button>
        </div>
        <div class="info-grid">
          <div class="info-item">
            <span class="label">仓库名称</span>
            <span class="value">{{ warehouseInfo.name }}</span>
          </div>
          <div class="info-item">
            <span class="label">仓库编号</span>
            <span class="value">{{ warehouseInfo.code }}</span>
          </div>
          <div class="info-item">
            <span class="label">所属校区</span>
            <span class="value">{{ warehouseInfo.campus }}</span>
          </div>
          <div class="info-item">
            <span class="label">详细地址</span>
            <span class="value">{{ warehouseInfo.address }}</span>
          </div>
          <div class="info-item">
            <span class="label">联系电话</span>
            <span class="value">{{ warehouseInfo.phone }}</span>
          </div>
          <div class="info-item">
            <span class="label">营业时间</span>
            <span class="value">{{ warehouseInfo.businessHours }}</span>
          </div>
          <div class="info-item">
            <span class="label">负责人</span>
            <span class="value">{{ warehouseInfo.manager }}</span>
          </div>
          <div class="info-item">
            <span class="label">创建时间</span>
            <span class="value">{{ warehouseInfo.createTime }}</span>
          </div>
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

          <div class="capacity-card">
            <div class="capacity-header">
              <span class="capacity-title">今日入库量</span>
              <span class="capacity-badge today">{{ capacityStats.todayIn }} 件</span>
            </div>
            <div class="capacity-trend">
              <span :class="['trend', capacityStats.todayInTrend > 0 ? 'up' : 'down']">
                {{ capacityStats.todayInTrend > 0 ? '↑' : '↓' }} {{ Math.abs(capacityStats.todayInTrend) }}%
              </span>
              <span class="trend-label">较昨日</span>
            </div>
          </div>

          <div class="capacity-card">
            <div class="capacity-header">
              <span class="capacity-title">今日出库量</span>
              <span class="capacity-badge today">{{ capacityStats.todayOut }} 件</span>
            </div>
            <div class="capacity-trend">
              <span :class="['trend', capacityStats.todayOutTrend > 0 ? 'up' : 'down']">
                {{ capacityStats.todayOutTrend > 0 ? '↑' : '↓' }} {{ Math.abs(capacityStats.todayOutTrend) }}%
              </span>
              <span class="trend-label">较昨日</span>
            </div>
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

      <!-- 近期数据趋势 -->
      <div class="trend-section">
        <h2><img src="@/assets/icons/8.png" alt="" class="title-icon" />近7天数据趋势</h2>
        <div class="trend-table">
          <table>
            <thead>
              <tr>
                <th>日期</th>
                <th>入库量</th>
                <th>出库量</th>
                <th>在库量</th>
                <th>使用率</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in trendData" :key="item.date">
                <td>{{ item.date }}</td>
                <td class="num-cell">+{{ item.inCount }}</td>
                <td class="num-cell">-{{ item.outCount }}</td>
                <td class="num-cell">{{ item.stockCount }}</td>
                <td>
                  <div class="mini-bar">
                    <div class="mini-fill" :style="{ width: item.usageRate + '%' }"></div>
                  </div>
                  <span>{{ item.usageRate }}%</span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 编辑信息弹窗 -->
      <div class="modal" v-if="showEditInfo" @click="showEditInfo = false">
        <div class="modal-content" @click.stop>
          <div class="modal-header">
            <h3>编辑仓库信息</h3>
            <button class="close-btn" @click="showEditInfo = false">×</button>
          </div>
          <div class="modal-body">
            <div class="form-group">
              <label>仓库名称</label>
              <input type="text" v-model="editForm.name">
            </div>
            <div class="form-row">
              <div class="form-group">
                <label>仓库编号</label>
                <input type="text" v-model="editForm.code" disabled>
              </div>
              <div class="form-group">
                <label>所属校区</label>
                <input type="text" v-model="editForm.campus">
              </div>
            </div>
            <div class="form-group">
              <label>详细地址</label>
              <input type="text" v-model="editForm.address">
            </div>
            <div class="form-row">
              <div class="form-group">
                <label>联系电话</label>
                <input type="text" v-model="editForm.phone">
              </div>
              <div class="form-group">
                <label>负责人</label>
                <input type="text" v-model="editForm.manager">
              </div>
            </div>
            <div class="form-group">
              <label>营业时间</label>
              <input type="text" v-model="editForm.businessHours" placeholder="如：08:00-21:00">
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn-cancel" @click="showEditInfo = false">取消</button>
            <button class="btn-submit" @click="saveInfo">保存</button>
          </div>
        </div>
      </div>
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import AdminLayout from '@/layouts/AdminLayout.vue'
import { useToast } from '@/composables/useToast'

const { success } = useToast()

interface WarehouseInfo {
  name: string
  code: string
  campus: string
  address: string
  phone: string
  businessHours: string
  manager: string
  createTime: string
}

interface ZoneCapacity {
  id: number
  name: string
  totalSlots: number
  usedSlots: number
  usageRate: number
  shelfCount: number
  status: string
}

interface TrendItem {
  date: string
  inCount: number
  outCount: number
  stockCount: number
  usageRate: number
}

const showEditInfo = ref(false)

// 仓库基本信息
const warehouseInfo = reactive<WarehouseInfo>({
  name: '东校区快递驿站',
  code: 'WH-EAST-001',
  campus: '东校区',
  address: '东校区学生生活区A栋1楼',
  phone: '010-12345678',
  businessHours: '08:00 - 21:00',
  manager: '张管理员',
  createTime: '2024-01-01'
})

// 编辑表单
const editForm = reactive({
  name: warehouseInfo.name,
  code: warehouseInfo.code,
  campus: warehouseInfo.campus,
  address: warehouseInfo.address,
  phone: warehouseInfo.phone,
  businessHours: warehouseInfo.businessHours,
  manager: warehouseInfo.manager
})

// 容量统计
const capacityStats = reactive({
  totalCapacity: 500,
  usedCapacity: 356,
  remainCapacity: 144,
  usageRate: 71,
  todayIn: 45,
  todayOut: 32,
  todayInTrend: 12,
  todayOutTrend: -8,
  overdueParcels: 15
})

// 各区域容量
const zoneCapacities = ref<ZoneCapacity[]>([
  { id: 1, name: 'A区 - 普通件', totalSlots: 200, usedSlots: 156, usageRate: 78, shelfCount: 8, status: 'normal' },
  { id: 2, name: 'B区 - 大件', totalSlots: 100, usedSlots: 98, usageRate: 98, shelfCount: 4, status: 'full' },
  { id: 3, name: 'C区 - 贵重件', totalSlots: 120, usedSlots: 65, usageRate: 54, shelfCount: 6, status: 'normal' },
  { id: 4, name: 'D区 - 临时件', totalSlots: 80, usedSlots: 37, usageRate: 46, shelfCount: 3, status: 'normal' },
])

// 近7天趋势数据
const trendData = ref<TrendItem[]>([
  { date: '01-13', inCount: 42, outCount: 38, stockCount: 352, usageRate: 70 },
  { date: '01-14', inCount: 55, outCount: 41, stockCount: 366, usageRate: 73 },
  { date: '01-15', inCount: 38, outCount: 45, stockCount: 359, usageRate: 72 },
  { date: '01-16', inCount: 48, outCount: 52, stockCount: 355, usageRate: 71 },
  { date: '01-17', inCount: 61, outCount: 44, stockCount: 372, usageRate: 74 },
  { date: '01-18', inCount: 40, outCount: 36, stockCount: 376, usageRate: 75 },
  { date: '01-19', inCount: 45, outCount: 32, stockCount: 356, usageRate: 71 },
])

const getZoneStatusLabel = (status: string) => {
  const labels: Record<string, string> = {
    'normal': '正常',
    'full': '已满',
    'warning': '紧张'
  }
  return labels[status] || '未知'
}

const saveInfo = () => {
  Object.assign(warehouseInfo, editForm)
  showEditInfo.value = false
  success('保存成功（模拟）')
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

.info-card {
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

.btn-edit {
  padding: 8px 16px;
  background: white;
  color: #808080;
  border: 1px solid #808080;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.btn-edit:hover {
  background: #808080;
  color: white;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-item .label {
  font-size: 13px;
  color: #999;
}

.info-item .value {
  font-size: 15px;
  color: #333;
  font-weight: 500;
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

.capacity-trend {
  display: flex;
  align-items: center;
  gap: 8px;
}

.trend {
  font-size: 16px;
  font-weight: 600;
}

.trend.up {
  color: #52c41a;
}

.trend.down {
  color: #f5222d;
}

.trend-label {
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

.trend-section {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.trend-table {
  overflow-x: auto;
}

.trend-table table {
  width: 100%;
  border-collapse: collapse;
}

.trend-table thead {
  background: #f5f7fa;
}

.trend-table th,
.trend-table td {
  padding: 14px 16px;
  text-align: left;
  font-size: 14px;
}

.trend-table th {
  font-weight: 600;
  color: #666;
}

.trend-table td {
  border-top: 1px solid #f0f0f0;
  color: #333;
}

.num-cell {
  font-family: 'Courier New', monospace;
  font-weight: 500;
}

.mini-bar {
  width: 60px;
  height: 6px;
  background: #e0e0e0;
  border-radius: 3px;
  overflow: hidden;
  display: inline-block;
  vertical-align: middle;
  margin-right: 8px;
}

.mini-fill {
  height: 100%;
  background: #52c41a;
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
  width: 600px;
  max-width: 90vw;
  max-height: 90vh;
  overflow-y: auto;
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

.form-group input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
}

.form-group input:disabled {
  background: #f5f5f5;
  color: #999;
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

@media (max-width: 1200px) {
  .info-grid,
  .capacity-cards,
  .zone-cards {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .info-grid,
  .capacity-cards,
  .zone-cards {
    grid-template-columns: 1fr;
  }
  .form-row {
    grid-template-columns: 1fr;
  }
}
</style>
