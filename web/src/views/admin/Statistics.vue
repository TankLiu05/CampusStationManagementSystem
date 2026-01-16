<template>
  <AdminLayout>
    <div class="statistics">
    <div class="page-header">
      <h1>数据统计</h1>
      <p>查看驿站运营数据统计</p>
    </div>

    <!-- 时间范围选择 -->
    <div class="time-range-selector">
      <button 
        v-for="range in timeRanges" 
        :key="range.value"
        :class="['range-btn', { active: selectedRange === range.value }]"
        @click="selectedRange = range.value"
      >
        {{ range.label }}
      </button>
      <div class="custom-date">
        <input type="date" v-model="startDate">
        <span>至</span>
        <input type="date" v-model="endDate">
        <button class="query-btn">查询</button>
      </div>
    </div>

    <!-- 核心指标 -->
    <div class="core-stats">
      <div class="stat-card">
        <div class="stat-header">
          <span class="stat-title">包裹总量</span>
          <span class="stat-trend up">↑ 12.5%</span>
        </div>
        <div class="stat-value">1,234</div>
        <div class="stat-desc">较上周增长 154 件</div>
      </div>

      <div class="stat-card">
        <div class="stat-header">
          <span class="stat-title">取件率</span>
          <span class="stat-trend up">↑ 3.2%</span>
        </div>
        <div class="stat-value">92.5%</div>
        <div class="stat-desc">1,142 / 1,234 件已取</div>
      </div>

      <div class="stat-card">
        <div class="stat-header">
          <span class="stat-title">平均取件时长</span>
          <span class="stat-trend down">↓ 0.5h</span>
        </div>
        <div class="stat-value">6.8h</div>
        <div class="stat-desc">较上周减少 0.5 小时</div>
      </div>

      <div class="stat-card">
        <div class="stat-header">
          <span class="stat-title">活跃用户</span>
          <span class="stat-trend up">↑ 8.3%</span>
        </div>
        <div class="stat-value">856</div>
        <div class="stat-desc">本周活跃用户数</div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-section">
      <!-- 包裹趋势图 -->
      <div class="chart-card">
        <div class="chart-header">
          <h3><img src="@/assets/icons/6.png" alt="包裹趋势" style="width: 22px; height: 22px; vertical-align: middle; margin-right: 8px;" /> 包裹趋势</h3>
          <div class="chart-legend">
            <span class="legend-item"><span class="dot" style="background: #808080;"></span>到达包裹</span>
            <span class="legend-item"><span class="dot" style="background: #52c41a;"></span>已取包裹</span>
          </div>
        </div>
        <div class="chart-body">
          <div class="chart-placeholder">
            <img src="@/assets/icons/7.png" alt="图表" style="width: 32px; height: 32px; vertical-align: middle; margin-right: 8px;" /> 图表区域 - 此处将集成图表库显示数据趋势
          </div>
        </div>
      </div>

      <!-- 快递公司分布 -->
      <div class="chart-card">
        <div class="chart-header">
          <h3><img src="@/assets/icons/2.png" alt="快递公司分布" style="width: 22px; height: 22px; vertical-align: middle; margin-right: 8px;" /> 快递公司分布</h3>
        </div>
        <div class="chart-body">
          <div class="company-stats">
            <div class="company-item" v-for="company in companyStats" :key="company.name">
              <div class="company-info">
                <span class="company-name">{{ company.name }}</span>
                <span class="company-count">{{ company.count }} 件</span>
              </div>
              <div class="progress-bar">
                <div class="progress-fill" :style="{ width: company.percentage + '%' }"></div>
              </div>
              <span class="percentage">{{ company.percentage }}%</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 详细数据表格 -->
    <div class="data-tables">
      <!-- 热门时段统计 -->
      <div class="table-card">
        <h3>🕐 热门取件时段</h3>
        <table>
          <thead>
            <tr>
              <th>时间段</th>
              <th>取件数量</th>
              <th>占比</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="slot in timeSlots" :key="slot.time">
              <td>{{ slot.time }}</td>
              <td>{{ slot.count }}</td>
              <td>
                <span class="percentage-badge">{{ slot.percentage }}%</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 用户排行 -->
      <div class="table-card">
        <h3><img src="@/assets/icons/1.png" alt="活跃用户排行" style="width: 22px; height: 22px; vertical-align: middle; margin-right: 8px;" /> 活跃用户排行</h3>
        <table>
          <thead>
            <tr>
              <th>排名</th>
              <th>用户名</th>
              <th>取件次数</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(user, index) in topUsers" :key="user.username">
              <td>
                <span :class="['rank-badge', { top: index < 3 }]">
                  {{ index + 1 }}
                </span>
              </td>
              <td>{{ user.username }}</td>
              <td>{{ user.count }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import AdminLayout from '@/layouts/AdminLayout.vue'

interface TimeRange {
  label: string
  value: string
}

interface CompanyStat {
  name: string
  count: number
  percentage: number
}

interface TimeSlot {
  time: string
  count: number
  percentage: number
}

interface TopUser {
  username: string
  count: number
}

const selectedRange = ref('week')
const startDate = ref('')
const endDate = ref('')

const timeRanges: TimeRange[] = [
  { label: '今日', value: 'today' },
  { label: '本周', value: 'week' },
  { label: '本月', value: 'month' },
  { label: '本季度', value: 'quarter' }
]

const companyStats: CompanyStat[] = [
  { name: '顺丰速运', count: 456, percentage: 37 },
  { name: '中通快递', count: 345, percentage: 28 },
  { name: '圆通速递', count: 234, percentage: 19 },
  { name: '申通快递', count: 123, percentage: 10 },
  { name: '其他', count: 76, percentage: 6 }
]

const timeSlots: TimeSlot[] = [
  { time: '08:00-10:00', count: 156, percentage: 15 },
  { time: '10:00-12:00', count: 234, percentage: 23 },
  { time: '12:00-14:00', count: 312, percentage: 31 },
  { time: '14:00-16:00', count: 189, percentage: 19 },
  { time: '16:00-18:00', count: 123, percentage: 12 }
]

const topUsers: TopUser[] = [
  { username: 'user001', count: 45 },
  { username: 'user002', count: 38 },
  { username: 'user003', count: 32 },
  { username: 'user004', count: 28 },
  { username: 'user005', count: 24 }
]
</script>

<style scoped>
.statistics {
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

.time-range-selector {
  background: white;
  padding: 20px;
  border-radius: 12px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.range-btn {
  padding: 8px 20px;
  border: 1px solid #e0e0e0;
  background: white;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  color: #333;
  transition: all 0.2s;
}

.range-btn.active {
  background: #808080;
  color: white;
  border-color: #808080;
}

.custom-date {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: auto;
}

.custom-date input {
  padding: 8px 12px;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  font-size: 14px;
}

.custom-date span {
  color: #999;
}

.query-btn {
  padding: 8px 20px;
  background: #808080;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
}

.core-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  background: white;
  padding: 24px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.stat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.stat-title {
  font-size: 14px;
  color: #999;
}

.stat-trend {
  font-size: 13px;
  font-weight: 600;
}

.stat-trend.up {
  color: #52c41a;
}

.stat-trend.down {
  color: #f5222d;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 8px;
}

.stat-desc {
  font-size: 13px;
  color: #999;
}

.charts-section {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 24px;
  margin-bottom: 24px;
}

.chart-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  overflow: hidden;
}

.chart-header {
  padding: 20px 24px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
}

.chart-legend {
  display: flex;
  gap: 20px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #666;
}

.dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.chart-body {
  padding: 24px;
}

.chart-placeholder {
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  border-radius: 8px;
  color: #999;
  font-size: 14px;
}

.company-stats {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.company-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.company-info {
  min-width: 120px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.company-name {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.company-count {
  font-size: 12px;
  color: #999;
}

.progress-bar {
  flex: 1;
  height: 8px;
  background: #f0f0f0;
  border-radius: 4px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #808080 0%, #666666 100%);
  border-radius: 4px;
  transition: width 0.3s;
}

.percentage {
  min-width: 50px;
  text-align: right;
  font-size: 13px;
  color: #666;
  font-weight: 600;
}

.data-tables {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
}

.table-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  overflow: hidden;
}

.table-card h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
  padding: 20px 24px;
  margin: 0;
  border-bottom: 1px solid #f0f0f0;
}

table {
  width: 100%;
  border-collapse: collapse;
}

thead {
  background: #f5f7fa;
}

th {
  padding: 12px 24px;
  text-align: left;
  font-size: 13px;
  font-weight: 600;
  color: #666;
}

td {
  padding: 16px 24px;
  border-top: 1px solid #f0f0f0;
  font-size: 14px;
  color: #333;
}

.percentage-badge {
  padding: 4px 10px;
  background: #f0f2f5;
  border-radius: 10px;
  font-size: 12px;
  font-weight: 600;
  color: #808080;
}

.rank-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #f0f0f0;
  color: #666;
  font-weight: 600;
  font-size: 13px;
}

.rank-badge.top {
  background: linear-gradient(135deg, #808080 0%, #666666 100%);
  color: white;
}

@media (max-width: 1200px) {
  .core-stats {
    grid-template-columns: repeat(2, 1fr);
  }

  .charts-section {
    grid-template-columns: 1fr;
  }

  .data-tables {
    grid-template-columns: 1fr;
  }
}
</style>
