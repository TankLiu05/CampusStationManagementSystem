<template>
  <AdminLayout>
      <div class="welcome-section">
        <h1>管理员工作台</h1>
        <p>欢迎，{{ currentUser?.username }}！系统运行正常</p>
      </div>

      <!-- 数据统计卡片 -->
      <div class="stats-cards">
        <div class="stat-card">
          <div class="stat-icon" style="background: #e6f7ff;">
            <img src="@/assets/icons/1.png" alt="用户管理" />
          </div>
          <div class="stat-content">
            <h3>用户总数</h3>
            <div class="stat-number">{{ userCount }}</div>
            <p class="stat-desc">注册用户</p>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon" style="background: #fff7e6;">
            <img src="@/assets/icons/2.png" alt="包裹管理" />
          </div>
          <div class="stat-content">
            <h3>包裹总数</h3>
            <div class="stat-number">{{ parcelCount }}</div>
            <p class="stat-desc">待取包裹</p>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon" style="background: #f0f5ff;">
            <img src="@/assets/icons/3.png" alt="今日签收" />
          </div>
          <div class="stat-content">
            <h3>今日签收</h3>
            <div class="stat-number">{{ signedCount }}</div>
            <p class="stat-desc">已完成签收</p>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon" style="background: #e6fffb;">
            <img src="@/assets/icons/4.png" alt="公告管理" />
          </div>
          <div class="stat-content">
            <h3>公告数量</h3>
            <div class="stat-number">{{ noticeCount }}</div>
            <p class="stat-desc">已发布公告</p>
          </div>
        </div>
      </div>

      <!-- 管理功能区：快捷操作 + 仓库信息 -->
      <div class="admin-sections">
        <!-- 左侧：快捷操作 -->
        <div class="section">
          <h2>快捷操作</h2>
          <div class="action-buttons">
            <button class="action-btn" @click="handleNavigate('/admin/packages')">
              <span>录入包裹</span>
            </button>
            <button class="action-btn" @click="handleNavigate('/admin/announcements')">
              <span>发布公告</span>
            </button>
            <button class="action-btn" @click="handleNavigate('/admin/users')">
              <span>用户管理</span>
            </button>
            <button class="action-btn" @click="handleNavigate('/admin/logistics')">
              <span>物流管理</span>
            </button>
            <button class="action-btn" @click="handleNavigate('/admin/returns')">
              <span>退货申请</span>
            </button>
            <button class="action-btn" @click="handleNavigate('/admin/warehouse')">
              <span>仓库管理</span>
            </button>
            <button class="action-btn" @click="handleNavigate('/admin/messages')">
              <span>留言管理</span>
            </button>
            <button class="action-btn" @click="handleNavigate('/admin/settings')">
              <span>系统设置</span>
            </button>
          </div>
        </div>

        <!-- 右侧：仓库信息概览 -->
        <div class="section warehouse-section">
          <div class="section-header">
            <h2>仓库信息</h2>
            <button class="link-btn" @click="handleNavigate('/admin/warehouse-info')">查看详情</button>
          </div>
          <div class="warehouse-info">
            <div class="warehouse-basic">
              <div class="warehouse-name">{{ warehouseInfo.name }}</div>
              <div class="warehouse-address">{{ warehouseInfo.address }}</div>
              <div class="warehouse-meta">
                <span>营业时间：{{ warehouseInfo.businessHours }}</span>
                <span>联系电话：{{ warehouseInfo.phone }}</span>
              </div>
            </div>
            <div class="warehouse-capacity">
              <div class="capacity-header">
                <span class="capacity-label">仓库容量</span>
                <span class="capacity-rate">{{ capacityStats.usageRate }}%</span>
              </div>
              <div class="capacity-bar">
                <div class="capacity-fill" :style="{ width: capacityStats.usageRate + '%' }"></div>
              </div>
              <div class="capacity-detail">
                <span>已用 {{ capacityStats.usedCapacity }} / {{ capacityStats.totalCapacity }} 格</span>
                <span>剩余 {{ capacityStats.remainCapacity }} 格</span>
              </div>
            </div>
            <div class="warehouse-stats">
              <div class="warehouse-stat-item">
                <span class="stat-label">今日入库</span>
                <span class="stat-value">{{ capacityStats.todayIn }} 件</span>
              </div>
              <div class="warehouse-stat-item">
                <span class="stat-label">今日出库</span>
                <span class="stat-value">{{ capacityStats.todayOut }} 件</span>
              </div>
              <div class="warehouse-stat-item">
                <span class="stat-label">滞留包裹</span>
                <span class="stat-value warning">{{ capacityStats.overdueParcels }} 件</span>
              </div>
            </div>
          </div>
        </div>
      </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getCurrentUser, logout } from '@/api/sysUser'
import type { SysUser } from '@/api/sysUser'
import AdminLayout from '@/layouts/AdminLayout.vue'
import { parcelApi } from '@/api/admin/parcel'
import { listNotices } from '@/api/admin/notice'
import { getUserList } from '@/api/admin/user'

const router = useRouter()
const currentUser = ref<SysUser | null>(null)
const userCount = ref(0)
const parcelCount = ref(0)
const signedCount = ref(0)
const noticeCount = ref(0)

// 仓库基本信息
const warehouseInfo = reactive({
  name: '东校区快递驿站',
  address: '东校区学生生活区A栋1楼',
  phone: '010-12345678',
  businessHours: '08:00 - 21:00'
})

// 容量统计
const capacityStats = reactive({
  totalCapacity: 500,
  usedCapacity: 356,
  remainCapacity: 144,
  usageRate: 71,
  todayIn: 45,
  todayOut: 32,
  overdueParcels: 15
})

// 导航处理函数
const handleNavigate = (path: string) => {
  router.push(path)
}

onMounted(async () => {
  try {
    currentUser.value = await getCurrentUser()
    // 权限检查已由路由守卫处理，这里不再重复检查
    
    // 加载统计数据
    loadStatistics()
  } catch (err) {
    console.error('获取用户信息失败:', err)
    router.replace('/login')
  }
})

// 加载统计数据
const loadStatistics = async () => {
  try {
    // 加载包裹总数
    const parcelResponse = await parcelApi.list(0, 1)
    parcelCount.value = parcelResponse.totalElements
    
    // 计算今日签收数（从所有包裹中筛选已签收的）
    // 注意：这里暂时使用总签收数，如果需要今日签收需要后端提供专门接口
    const allParcelsResponse = await parcelApi.list(0, 999999)
    signedCount.value = allParcelsResponse.content.filter(p => p.isSigned === 1).length
    
    // 加载公告数量
    const noticeResponse = await listNotices(0, 1)
    noticeCount.value = noticeResponse.totalElements
    
    // 加载用户总数
    const userResponse = await getUserList(0, 1)
    userCount.value = userResponse.totalElements
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}
</script>

<style scoped>
.welcome-section {

  margin-bottom: 40px;
}

.welcome-section h1 {
  font-size: 32px;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 12px;
}

.welcome-section p {
  font-size: 18px;
  color: #666;
}

/* 统计卡片 */
.stats-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
  margin-bottom: 40px;
}

.stat-card {
  background: white;
  padding: 24px;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  display: flex;
  align-items: center;
  gap: 20px;
}

.stat-icon {
  font-size: 36px;
  width: 70px;
  height: 70px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
}

.stat-icon img {
  width: 40px;
  height: 40px;
  object-fit: contain;
}

.stat-content h3 {
  font-size: 14px;
  color: #999;
  font-weight: 500;
  margin-bottom: 8px;
}

.stat-number {
  font-size: 28px;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 4px;
}

.stat-desc {
  font-size: 12px;
  color: #999;
}

/* 管理功能区 */
.admin-sections {
  display: grid;
  grid-template-columns: 1fr 1.5fr;
  gap: 24px;
}

.section {
  background: white;
  padding: 30px;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.section h2 {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 24px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.section-header h2 {
  margin-bottom: 0;
}

.link-btn {
  padding: 6px 16px;
  background: transparent;
  color: #666;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.link-btn:hover {
  background: #f5f5f5;
  border-color: #999;
}

.action-buttons {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.action-btn {
  padding: 20px;
  border: 1px solid #e0e0e0;
  border-radius: 12px;
  background: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  font-size: 16px;
  color: #333;
  transition: all 0.2s;
}

.action-btn:hover {
  background: #f9f9f9;
  border-color: #ccc;
}

.action-btn:active {
  border-color: #999;
}

/* 仓库信息区 */
.warehouse-section {
  display: flex;
  flex-direction: column;
}

.warehouse-info {
  display: flex;
  flex-direction: column;
  gap: 24px;
  flex: 1;
}

.warehouse-basic {
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.warehouse-name {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 8px;
}

.warehouse-address {
  font-size: 14px;
  color: #666;
  margin-bottom: 12px;
}

.warehouse-meta {
  display: flex;
  gap: 24px;
  font-size: 13px;
  color: #999;
}

.warehouse-capacity {
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.capacity-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.capacity-label {
  font-size: 14px;
  color: #666;
}

.capacity-rate {
  font-size: 18px;
  font-weight: 600;
  color: #52c41a;
}

.capacity-bar {
  height: 10px;
  background: #e0e0e0;
  border-radius: 5px;
  overflow: hidden;
  margin-bottom: 12px;
}

.capacity-fill {
  height: 100%;
  background: linear-gradient(90deg, #52c41a, #73d13d);
  border-radius: 5px;
  transition: width 0.3s;
}

.capacity-detail {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: #999;
}

.warehouse-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.warehouse-stat-item {
  background: #f9f9f9;
  padding: 16px;
  border-radius: 10px;
  text-align: center;
}

.warehouse-stat-item .stat-label {
  display: block;
  font-size: 13px;
  color: #999;
  margin-bottom: 8px;
}

.warehouse-stat-item .stat-value {
  display: block;
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.warehouse-stat-item .stat-value.warning {
  color: #fa8c16;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .stats-cards {
    grid-template-columns: repeat(2, 1fr);
  }

  .admin-sections {
    grid-template-columns: 1fr;
  }

  .warehouse-stats {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 992px) {
  .welcome-section h1 {
    font-size: 28px;
  }
  
  .welcome-section p {
    font-size: 16px;
  }
  
  .stat-card {
    padding: 20px;
  }
  
  .stat-number {
    font-size: 24px;
  }
  
  .stat-icon {
    width: 60px;
    height: 60px;
  }
  
  .stat-icon img {
    width: 32px;
    height: 32px;
  }
}

@media (max-width: 768px) {
  .stats-cards {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .welcome-section {
    margin-bottom: 24px;
  }
  
  .welcome-section h1 {
    font-size: 24px;
  }
  
  .welcome-section p {
    font-size: 14px;
  }
  
  .stats-cards {
    margin-bottom: 24px;
  }
  
  .stat-card {
    padding: 16px;
    gap: 16px;
  }
  
  .stat-icon {
    width: 50px;
    height: 50px;
  }
  
  .stat-icon img {
    width: 28px;
    height: 28px;
  }
  
  .stat-number {
    font-size: 22px;
  }
  
  .section {
    padding: 20px;
  }
  
  .section h2 {
    font-size: 18px;
    margin-bottom: 16px;
  }

  .action-buttons {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }
  
  .action-btn {
    padding: 14px;
    font-size: 14px;
  }

  .warehouse-stats {
    grid-template-columns: repeat(3, 1fr);
    gap: 12px;
  }
  
  .warehouse-stat-item {
    padding: 12px;
  }
  
  .warehouse-stat-item .stat-value {
    font-size: 16px;
  }

  .warehouse-meta {
    flex-direction: column;
    gap: 8px;
  }
}

@media (max-width: 480px) {
  .welcome-section h1 {
    font-size: 22px;
  }
  
  .stat-card {
    flex-direction: column;
    text-align: center;
    gap: 12px;
  }
  
  .action-buttons {
    grid-template-columns: 1fr;
  }
  
  .action-btn {
    padding: 12px;
    font-size: 13px;
  }
  
  .warehouse-stats {
    grid-template-columns: 1fr;
  }
  
  .warehouse-name {
    font-size: 16px;
  }
  
  .warehouse-address {
    font-size: 13px;
  }
  
  .warehouse-meta {
    font-size: 12px;
  }
}
</style>
