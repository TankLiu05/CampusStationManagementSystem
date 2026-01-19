<template>
  <AdminLayout>
    <div class="warehouse-management">
      <div class="page-header">
        <h1>仓库管理</h1>
        <p>管理驿站货架和存储区域</p>
      </div>

      <!-- 操作栏 -->
      <div class="action-bar">
        <div class="search-section">
          <input 
            type="text" 
            v-model="searchKeyword" 
            placeholder="搜索货架编号或区域..."
            class="search-input"
            @keyup.enter="searchShelves"
          >
          <button class="search-btn" @click="searchShelves">搜索</button>
          <button class="reset-btn" @click="resetFilters">重置</button>
        </div>
        <div class="action-buttons">
          <button class="btn-primary" @click="showAddShelf = true">添加货架</button>
          <button class="btn-secondary" @click="showAddZone = true">添加区域</button>
        </div>
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
            <img src="@/assets/icons/12.png" alt="可用货架" class="icon-img" />
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.availableShelves }}</div>
            <div class="stat-label">可用货架</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon" style="background: #f5f5f5;">
            <img src="@/assets/icons/9.png" alt="已满货架" class="icon-img" />
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.fullShelves }}</div>
            <div class="stat-label">已满货架</div>
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
          {{ zone.name }} ({{ zone.shelfCount }})
        </button>
      </div>

      <!-- 货架列表 -->
      <div class="shelves-table">
        <table>
          <thead>
            <tr>
              <th>货架编号</th>
              <th>所属区域</th>
              <th>层数</th>
              <th>每层格位</th>
              <th>已用/总容量</th>
              <th>使用率</th>
              <th>状态</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="filteredShelves.length === 0">
              <td colspan="8" class="empty-row">暂无货架数据</td>
            </tr>
            <tr v-for="shelf in filteredShelves" :key="shelf.id">
              <td><span class="shelf-code">{{ shelf.code }}</span></td>
              <td>{{ shelf.zoneName }}</td>
              <td>{{ shelf.layers }}</td>
              <td>{{ shelf.slotsPerLayer }}</td>
              <td>{{ shelf.usedSlots }} / {{ shelf.totalSlots }}</td>
              <td>
                <div class="usage-bar">
                  <div 
                    class="usage-fill" 
                    :style="{ 
                      width: shelf.usageRate + '%',
                      background: getUsageColor(shelf.usageRate)
                    }"
                  ></div>
                </div>
                <span class="usage-text">{{ shelf.usageRate }}%</span>
              </td>
              <td>
                <span :class="['status-badge', shelf.status]">
                  {{ getStatusLabel(shelf.status) }}
                </span>
              </td>
              <td>
                <div class="action-btns">
                  <button class="btn-view" @click="viewShelf(shelf)">详情</button>
                  <button class="btn-edit" @click="editShelf(shelf)">编辑</button>
                  <button class="btn-delete" @click="deleteShelf(shelf.id)">删除</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>

        <!-- 分页 -->
        <div class="pagination">
          <button class="page-btn" :disabled="currentPage === 1" @click="currentPage--">上一页</button>
          <span class="page-info">第 {{ currentPage }} / {{ totalPages }} 页</span>
          <button class="page-btn" :disabled="currentPage === totalPages" @click="currentPage++">下一页</button>
        </div>
      </div>

      <!-- 添加货架弹窗 -->
      <div class="modal" v-if="showAddShelf || showEditShelf" @click="closeShelfModal">
        <div class="modal-content" @click.stop>
          <div class="modal-header">
            <h3>{{ showAddShelf ? '添加货架' : '编辑货架' }}</h3>
            <button class="close-btn" @click="closeShelfModal">×</button>
          </div>
          <div class="modal-body">
            <div class="form-group">
              <label>货架编号 *</label>
              <input type="text" v-model="shelfForm.code" placeholder="如：A-01">
            </div>
            <div class="form-group">
              <label>所属区域 *</label>
              <select v-model="shelfForm.zoneId">
                <option value="">请选择区域</option>
                <option v-for="zone in zones" :key="zone.id" :value="zone.id">{{ zone.name }}</option>
              </select>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label>层数 *</label>
                <input type="number" v-model="shelfForm.layers" min="1" max="10">
              </div>
              <div class="form-group">
                <label>每层格位数 *</label>
                <input type="number" v-model="shelfForm.slotsPerLayer" min="1" max="20">
              </div>
            </div>
            <div class="form-group">
              <label>备注</label>
              <textarea v-model="shelfForm.remark" placeholder="可选备注信息"></textarea>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn-cancel" @click="closeShelfModal">取消</button>
            <button class="btn-submit" @click="submitShelf">确定</button>
          </div>
        </div>
      </div>

      <!-- 添加区域弹窗 -->
      <div class="modal" v-if="showAddZone" @click="showAddZone = false">
        <div class="modal-content" @click.stop>
          <div class="modal-header">
            <h3>添加存储区域</h3>
            <button class="close-btn" @click="showAddZone = false">×</button>
          </div>
          <div class="modal-body">
            <div class="form-group">
              <label>区域名称 *</label>
              <input type="text" v-model="zoneForm.name" placeholder="如：A区">
            </div>
            <div class="form-group">
              <label>区域描述</label>
              <textarea v-model="zoneForm.description" placeholder="区域描述信息"></textarea>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn-cancel" @click="showAddZone = false">取消</button>
            <button class="btn-submit" @click="submitZone">确定</button>
          </div>
        </div>
      </div>

      <!-- 货架详情弹窗 -->
      <div class="modal" v-if="showShelfDetail" @click="showShelfDetail = false">
        <div class="modal-content detail-modal" @click.stop>
          <div class="modal-header">
            <h3>货架详情 - {{ currentShelf?.code }}</h3>
            <button class="close-btn" @click="showShelfDetail = false">×</button>
          </div>
          <div class="modal-body">
            <div class="shelf-visual">
              <div class="shelf-info">
                <div class="info-item"><span class="label">所属区域：</span>{{ currentShelf?.zoneName }}</div>
                <div class="info-item"><span class="label">层数：</span>{{ currentShelf?.layers }} 层</div>
                <div class="info-item"><span class="label">每层格位：</span>{{ currentShelf?.slotsPerLayer }} 个</div>
                <div class="info-item"><span class="label">总容量：</span>{{ currentShelf?.totalSlots }} 个</div>
                <div class="info-item"><span class="label">已使用：</span>{{ currentShelf?.usedSlots }} 个</div>
                <div class="info-item"><span class="label">使用率：</span>{{ currentShelf?.usageRate }}%</div>
              </div>
              <div class="slot-grid">
                <div class="layer" v-for="layer in currentShelf?.layers" :key="layer">
                  <span class="layer-label">第{{ layer }}层</span>
                  <div class="slots">
                    <div 
                      v-for="slot in currentShelf?.slotsPerLayer" 
                      :key="slot"
                      :class="['slot', { occupied: isSlotOccupied(layer, slot) }]"
                      :title="getSlotTitle(layer, slot)"
                    >
                      {{ layer }}-{{ slot }}
                    </div>
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

interface Zone {
  id: number
  name: string
  description: string
  shelfCount: number
}

interface Shelf {
  id: number
  code: string
  zoneId: number
  zoneName: string
  layers: number
  slotsPerLayer: number
  totalSlots: number
  usedSlots: number
  usageRate: number
  status: string
  remark?: string
  occupiedSlots?: { layer: number; slot: number; parcelId: number }[]
}

// 模拟数据
const zones = ref<Zone[]>([
  { id: 0, name: '全部区域', description: '', shelfCount: 12 },
  { id: 1, name: 'A区 - 普通件', description: '普通快递存放区', shelfCount: 4 },
  { id: 2, name: 'B区 - 大件', description: '大件包裹存放区', shelfCount: 3 },
  { id: 3, name: 'C区 - 贵重件', description: '贵重物品存放区', shelfCount: 3 },
  { id: 4, name: 'D区 - 临时件', description: '临时存放区', shelfCount: 2 },
])

const shelves = ref<Shelf[]>([
  { id: 1, code: 'A-01', zoneId: 1, zoneName: 'A区 - 普通件', layers: 4, slotsPerLayer: 6, totalSlots: 24, usedSlots: 18, usageRate: 75, status: 'normal', occupiedSlots: [] },
  { id: 2, code: 'A-02', zoneId: 1, zoneName: 'A区 - 普通件', layers: 4, slotsPerLayer: 6, totalSlots: 24, usedSlots: 24, usageRate: 100, status: 'full', occupiedSlots: [] },
  { id: 3, code: 'A-03', zoneId: 1, zoneName: 'A区 - 普通件', layers: 4, slotsPerLayer: 6, totalSlots: 24, usedSlots: 10, usageRate: 42, status: 'normal', occupiedSlots: [] },
  { id: 4, code: 'A-04', zoneId: 1, zoneName: 'A区 - 普通件', layers: 4, slotsPerLayer: 6, totalSlots: 24, usedSlots: 0, usageRate: 0, status: 'empty', occupiedSlots: [] },
  { id: 5, code: 'B-01', zoneId: 2, zoneName: 'B区 - 大件', layers: 3, slotsPerLayer: 4, totalSlots: 12, usedSlots: 8, usageRate: 67, status: 'normal', occupiedSlots: [] },
  { id: 6, code: 'B-02', zoneId: 2, zoneName: 'B区 - 大件', layers: 3, slotsPerLayer: 4, totalSlots: 12, usedSlots: 12, usageRate: 100, status: 'full', occupiedSlots: [] },
  { id: 7, code: 'B-03', zoneId: 2, zoneName: 'B区 - 大件', layers: 3, slotsPerLayer: 4, totalSlots: 12, usedSlots: 5, usageRate: 42, status: 'normal', occupiedSlots: [] },
  { id: 8, code: 'C-01', zoneId: 3, zoneName: 'C区 - 贵重件', layers: 5, slotsPerLayer: 4, totalSlots: 20, usedSlots: 15, usageRate: 75, status: 'normal', occupiedSlots: [] },
  { id: 9, code: 'C-02', zoneId: 3, zoneName: 'C区 - 贵重件', layers: 5, slotsPerLayer: 4, totalSlots: 20, usedSlots: 3, usageRate: 15, status: 'normal', occupiedSlots: [] },
  { id: 10, code: 'C-03', zoneId: 3, zoneName: 'C区 - 贵重件', layers: 5, slotsPerLayer: 4, totalSlots: 20, usedSlots: 0, usageRate: 0, status: 'maintenance', occupiedSlots: [] },
  { id: 11, code: 'D-01', zoneId: 4, zoneName: 'D区 - 临时件', layers: 2, slotsPerLayer: 8, totalSlots: 16, usedSlots: 12, usageRate: 75, status: 'normal', occupiedSlots: [] },
  { id: 12, code: 'D-02', zoneId: 4, zoneName: 'D区 - 临时件', layers: 2, slotsPerLayer: 8, totalSlots: 16, usedSlots: 6, usageRate: 38, status: 'normal', occupiedSlots: [] },
])

const searchKeyword = ref('')
const currentZone = ref(0)
const currentPage = ref(1)
const totalPages = ref(1)

const showAddShelf = ref(false)
const showEditShelf = ref(false)
const showAddZone = ref(false)
const showShelfDetail = ref(false)
const currentShelf = ref<Shelf | null>(null)

const stats = reactive({
  totalZones: 4,
  totalShelves: 12,
  availableShelves: 8,
  fullShelves: 2
})

const shelfForm = reactive({
  code: '',
  zoneId: '',
  layers: 4,
  slotsPerLayer: 6,
  remark: ''
})

const zoneForm = reactive({
  name: '',
  description: ''
})

const filteredShelves = computed(() => {
  let result = shelves.value
  if (currentZone.value !== 0) {
    result = result.filter(s => s.zoneId === currentZone.value)
  }
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(s => 
      s.code.toLowerCase().includes(keyword) || 
      s.zoneName.toLowerCase().includes(keyword)
    )
  }
  return result
})

const getStatusLabel = (status: string) => {
  const labels: Record<string, string> = {
    'empty': '空闲',
    'normal': '正常',
    'full': '已满',
    'maintenance': '维护中'
  }
  return labels[status] || '未知'
}

const searchShelves = () => {
  currentPage.value = 1
}

const resetFilters = () => {
  searchKeyword.value = ''
  currentZone.value = 0
  currentPage.value = 1
}

const viewShelf = (shelf: Shelf) => {
  currentShelf.value = shelf
  showShelfDetail.value = true
}

const editShelf = (shelf: Shelf) => {
  Object.assign(shelfForm, {
    code: shelf.code,
    zoneId: shelf.zoneId,
    layers: shelf.layers,
    slotsPerLayer: shelf.slotsPerLayer,
    remark: shelf.remark || ''
  })
  showEditShelf.value = true
}

const deleteShelf = (id: number) => {
  if (confirm('确定要删除该货架吗？')) {
    alert('删除成功（模拟）')
  }
}

const closeShelfModal = () => {
  showAddShelf.value = false
  showEditShelf.value = false
  Object.assign(shelfForm, { code: '', zoneId: '', layers: 4, slotsPerLayer: 6, remark: '' })
}

const submitShelf = () => {
  if (!shelfForm.code || !shelfForm.zoneId) {
    alert('请填写货架编号和所属区域')
    return
  }
  alert(showAddShelf.value ? '添加成功（模拟）' : '更新成功（模拟）')
  closeShelfModal()
}

const submitZone = () => {
  if (!zoneForm.name) {
    alert('请填写区域名称')
    return
  }
  alert('添加区域成功（模拟）')
  showAddZone.value = false
  Object.assign(zoneForm, { name: '', description: '' })
}

const isSlotOccupied = (layer: number, slot: number) => {
  // 模拟一些占用状态
  const occupied = (layer + slot) % 3 === 0
  return occupied && currentShelf.value && currentShelf.value.usedSlots > 0
}

const getSlotTitle = (layer: number, slot: number) => {
  return isSlotOccupied(layer, slot) ? '已占用' : '空闲'
}

// 根据使用率返回颜色
const getUsageColor = (rate: number) => {
  if (rate === 0) return '#e0e0e0' // 空闲 - 灰色
  if (rate < 50) return '#52c41a' // 低于50% - 绿色
  if (rate < 80) return '#faad14' // 50%-80% - 橙色
  if (rate < 100) return '#fa8c16' // 80%-100% - 深橙色
  return '#f5222d' // 100%已满 - 红色
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
  transition: width 0.3s, background 0.3s;
  border-radius: 4px;
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
  color: #666;
}

.status-badge.normal {
  background: #f6ffed;
  color: #52c41a;
}

.status-badge.full {
  background: #fff7e6;
  color: #fa8c16;
}

.status-badge.maintenance {
  background: #fff1f0;
  color: #f5222d;
}

.action-btns {
  display: flex;
  gap: 8px;
}

.btn-view, .btn-edit, .btn-delete {
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
  width: 500px;
  max-width: 90vw;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-content.detail-modal {
  width: 700px;
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
  display: flex;
  align-items: center;
  justify-content: center;
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
  min-height: 80px;
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

/* Shelf Detail Styles */
.shelf-visual {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.shelf-info {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.info-item {
  font-size: 14px;
  color: #333;
}

.info-item .label {
  color: #999;
}

.slot-grid {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.layer {
  display: flex;
  align-items: center;
  gap: 12px;
}

.layer-label {
  width: 60px;
  font-size: 12px;
  color: #666;
}

.slots {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.slot {
  width: 50px;
  height: 40px;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  color: #999;
  background: #f9f9f9;
}

.slot.occupied {
  background: #808080;
  color: white;
  border-color: #808080;
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
  .shelf-info {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
