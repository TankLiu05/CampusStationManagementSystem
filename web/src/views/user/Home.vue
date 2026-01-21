<template>
  <UserLayout>
      <div class="welcome-section">
        <h1>欢迎回来，{{ currentUser?.username }}！</h1>
        <p>您可以在这里查看和管理您的快递包裹</p>
      </div>

      <!-- 公告走马灯 -->
      <div class="notice-marquee" v-if="recentNotices.length > 0">
        <img src="@/assets/icons/4.png" alt="公告" class="marquee-icon" />
        <div class="marquee-content">
          <div class="marquee-track" :style="{ animationDuration: recentNotices.length * 5 + 's' }">
            <span v-for="(notice, index) in recentNotices" :key="notice.id" class="marquee-item">
              [{{ notice.title }}]{{ truncateContent(notice.content, 50) }}
              <span v-if="index < recentNotices.length - 1" class="marquee-separator">|</span>
            </span>
          </div>
        </div>
      </div>

      <!-- 快捷功能卡片 -->
      <div class="function-cards">
        <div class="card" @click="router.push('/user/packages')">
          <img src="@/assets/icons/2.png" alt="在途包裹" class="card-icon" />
          <div class="card-text">
            <h3>在途包裹</h3>
            <p>查看运输中的包裹</p>
          </div>
          <div class="card-count">{{ inTransitParcelCount }} 件</div>
        </div>

        <div class="card" @click="router.push('/user/packages')">
          <img src="@/assets/icons/3.png" alt="已签收" class="card-icon" />
          <div class="card-text">
            <h3>已签收</h3>
            <p>历史签收记录</p>
          </div>
          <div class="card-count">{{ signedParcelCount }} 件</div>
        </div>

        <div class="card" @click="router.push('/user/announcements')">
          <img src="@/assets/icons/4.png" alt="公告通知" class="card-icon" />
          <div class="card-text">
            <h3>公告通知</h3>
            <p>最新公告信息</p>
          </div>
          <div class="card-count">{{ noticeCount }} 条</div>
        </div>
      </div>

      <!-- 详细信息展示区域 -->
      <div class="detail-sections">
        <!-- 在途包裹列表 -->
        <div class="detail-section">
          <div class="section-header">
            <h2>在途包裹</h2>
            <button class="view-more-btn" @click="router.push('/user/packages')">
              查看全部
            </button>
          </div>
          <div class="section-content">
            <div v-if="recentInTransitParcels.length === 0" class="empty-state">
              <span>暂无在途包裹</span>
            </div>
            <div v-else class="parcel-list">
              <div v-for="parcel in recentInTransitParcels" :key="parcel.id" class="parcel-item">
                <div class="parcel-info">
                  <div class="parcel-company">{{ parcel.company }}</div>
                  <div class="parcel-tracking">单号：{{ parcel.trackingNumber }}</div>
                  <div class="parcel-location">当前位置：{{ parcel.location || '运输途中' }}</div>
                </div>
                <div class="parcel-transit-badge">
                  <span class="transit-text">运输中</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 已签收包裹列表 -->
        <div class="detail-section">
          <div class="section-header">
            <h2>已签收</h2>
            <button class="view-more-btn" @click="router.push('/user/packages')">
              查看全部
            </button>
          </div>
          <div class="section-content">
            <div v-if="recentSignedParcels.length === 0" class="empty-state">
              <span>暂无签收记录</span>
            </div>
            <div v-else class="parcel-list">
              <div v-for="parcel in recentSignedParcels" :key="parcel.id" class="parcel-item signed">
                <div class="parcel-info">
                  <div class="parcel-company">{{ parcel.company }}</div>
                  <div class="parcel-tracking">单号：{{ parcel.trackingNumber }}</div>
                  <div class="parcel-time">签收时间：{{ formatTime(parcel.updateTime) }}</div>
                </div>
                <div class="parcel-status signed-badge">
                  已签收
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 最新公告列表 -->
        <div class="detail-section">
          <div class="section-header">
            <h2>最新公告</h2>
            <button class="view-more-btn" @click="router.push('/user/announcements')">
              查看全部
            </button>
          </div>
          <div class="section-content">
            <div v-if="recentNotices.length === 0" class="empty-state">
              <span>暂无公告</span>
            </div>
            <div v-else class="notice-list">
              <div v-for="notice in recentNotices" :key="notice.id" class="notice-item">
                <div class="notice-title">{{ notice.title }}</div>
                <div class="notice-content">{{ truncateContent(notice.content) }}</div>
                <div class="notice-time">{{ formatTime(notice.createTime) }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
  </UserLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getCurrentUser, logout } from '@/api/sysUser'
import type { SysUser } from '@/api/sysUser'
import UserLayout from '@/layouts/UserLayout.vue'
import { listNotices } from '@/api/user/notice'
import type { Notice } from '@/api/user/notice'
import { listMyParcels } from '@/api/user/parcel'
import type { Parcel } from '@/api/user/parcel'

const router = useRouter()
const currentUser = ref<SysUser | null>(null)
const noticeCount = ref(0)
const inTransitParcelCount = ref(0) // 在途包裹数量（运输中）
const signedParcelCount = ref(0) // 已签收包裹数量

// 最近数据列表
const recentInTransitParcels = ref<Parcel[]>([]) // 最近在途包裹
const recentSignedParcels = ref<Parcel[]>([]) // 最近已签收包裹
const recentNotices = ref<Notice[]>([]) // 最新公告

onMounted(async () => {
  try {
    currentUser.value = await getCurrentUser()
    
    // 权限检查:如果是管理员,跳转到管理员首页
    if (currentUser.value.role === 'ADMIN') {
      router.replace('/admin/home')
      return
    }
    
    // 加载公告数量
    loadNoticeCount()
    // 加载包裹数量
    loadParcelCounts()
  } catch (err) {
    console.error('获取用户信息失败:', err)
    router.replace('/login')
  }
})

// 加载公告数量和最新公告
const loadNoticeCount = async () => {
  try {
    const response = await listNotices(0, 5) // 获取最新5条公告
    noticeCount.value = response.totalElements
    recentNotices.value = response.content
  } catch (error) {
    console.error('加载公告数量失败:', error)
    noticeCount.value = 0
    recentNotices.value = []
  }
}

// 加载包裹数量和最近包裹
const loadParcelCounts = async () => {
  try {
    // 加载所有包裹以统计数量（使用较大的size值获取所有数据）
    const response = await listMyParcels(0, 1000)
    const parcels = response.content
    
    // 筛选在途包裹：已发货/运输中（status=1 且 isSigned=0）
    const inTransitParcels = parcels.filter(
      (p) => p.status === 1 && p.isSigned === 0
    )
    inTransitParcelCount.value = inTransitParcels.length
    // 按更新时间排序后取最近5条在途包裹
    recentInTransitParcels.value = inTransitParcels
      .sort((a, b) => new Date(b.updateTime).getTime() - new Date(a.updateTime).getTime())
      .slice(0, 5)
    
    // 筛选已签收包裹：isSigned=1
    const signedParcels = parcels.filter((p) => p.isSigned === 1)
    signedParcelCount.value = signedParcels.length
    // 按更新时间排序后取最近5条
    recentSignedParcels.value = signedParcels
      .sort((a, b) => new Date(b.updateTime).getTime() - new Date(a.updateTime).getTime())
      .slice(0, 5)
  } catch (error) {
    console.error('加载包裹数量失败:', error)
    inTransitParcelCount.value = 0
    signedParcelCount.value = 0
    recentInTransitParcels.value = []
    recentSignedParcels.value = []
  }
}

// 格式化时间
const formatTime = (timeStr: string) => {
  if (!timeStr) return '-'
  const date = new Date(timeStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 截断公告内容
const truncateContent = (content: string, maxLen = 80) => {
  if (!content) return ''
  return content.length > maxLen ? content.slice(0, maxLen) + '...' : content
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

/* 功能卡片 */
.function-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
  margin-bottom: 40px;
}

/* 公告走马灯 */
.notice-marquee {
  display: flex;
  align-items: center;
  background: #f5f5f5;
  border: 1px solid #e0e0e0;
  border-radius: 10px;
  padding: 10px 16px;
  margin-bottom: 20px;
  overflow: hidden;
}

.marquee-icon {
  width: 20px;
  height: 20px;
  object-fit: contain;
  margin-right: 12px;
  flex-shrink: 0;
}

.marquee-content {
  flex: 1;
  overflow: hidden;
  position: relative;
}

.marquee-track {
  display: inline-block;
  white-space: nowrap;
  animation: marquee-scroll linear infinite;
}

@keyframes marquee-scroll {
  0% {
    transform: translateX(0);
  }
  100% {
    transform: translateX(-50%);
  }
}

.marquee-item {
  font-size: 14px;
  color: #666;
}

.marquee-separator {
  margin: 0 24px;
  color: #999;
}

.card {
  background: white;
  padding: 16px 20px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  cursor: pointer;
  text-align: center;
  display: flex;
  align-items: center;
  gap: 16px;
}

.card-icon {
  font-size: 36px;
  flex-shrink: 0;
}

.card-icon[src] {
  width: 36px;
  height: 36px;
  object-fit: contain;
}

.card-text {
  flex: 1;
  text-align: left;
}

.card h3 {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 4px;
}

.card p {
  font-size: 13px;
  color: #999;
  margin: 0;
}

.card-count {
  font-size: 22px;
  font-weight: 700;
  color: #1890ff;
  flex-shrink: 0;
}

/* 详细信息展示区域 */
.detail-sections {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
  margin-top: 20px;
}

.detail-section {
  background: white;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #f0f0f0;
}

.section-header h2 {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
}

.view-more-btn {
  background: none;
  border: none;
  color: #1890ff;
  font-size: 14px;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: 6px;
  transition: all 0.2s;
}

.view-more-btn:hover {
  background: #e6f7ff;
}

.section-content {
  padding: 16px 24px;
  min-height: 200px;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 150px;
  color: #999;
  font-size: 14px;
}

/* 包裹列表样式 */
.parcel-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.parcel-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
  background: #fafafa;
  border-radius: 10px;
  transition: all 0.2s;
}

.parcel-info {
  flex: 1;
}

.parcel-company {
  font-size: 15px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 4px;
}

.parcel-tracking {
  font-size: 13px;
  color: #666;
  margin-bottom: 2px;
}

.parcel-location,
.parcel-time,
.parcel-status-text {
  font-size: 12px;
  color: #999;
}

.parcel-transit-badge {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 8px 14px;
  background: #e6f7ff;
  border-radius: 8px;
}

.transit-text {
  font-size: 12px;
  font-weight: 500;
  color: #1890ff;
}

.parcel-code {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 8px 14px;
  background: #fff7e6;
  border-radius: 8px;
}

.code-label {
  font-size: 11px;
  color: #fa8c16;
  margin-bottom: 2px;
}

.code-value {
  font-size: 16px;
  font-weight: 700;
  color: #fa541c;
}

.parcel-status {
  padding: 6px 12px;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 500;
}

.signed-badge {
  background: #f6ffed;
  color: #52c41a;
}

/* 公告列表样式 */
.notice-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.notice-item {
  padding: 14px 16px;
  background: #fafafa;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
}

.notice-title {
  font-size: 15px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 6px;
}

.notice-content {
  font-size: 13px;
  color: #666;
  line-height: 1.5;
  margin-bottom: 8px;
}

.notice-time {
  font-size: 12px;
  color: #999;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .detail-sections {
    grid-template-columns: 1fr;
  }
  
  .function-cards {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 992px) {
  .function-cards {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .welcome-section h1 {
    font-size: 28px;
  }
  
  .welcome-section p {
    font-size: 16px;
  }
}

@media (max-width: 768px) {
  .function-cards {
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
  
  .card {
    padding: 14px 16px;
  }
  
  .card-icon[src] {
    width: 32px;
    height: 32px;
  }
  
  .card h3 {
    font-size: 15px;
  }
  
  .card-count {
    font-size: 20px;
  }
  
  .detail-section {
    border-radius: 12px;
  }
  
  .section-header {
    padding: 16px 20px;
  }
  
  .section-header h2 {
    font-size: 16px;
  }
  
  .section-content {
    padding: 14px 16px;
    min-height: 150px;
  }
  
  .parcel-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
    padding: 12px 14px;
  }
  
  .parcel-code {
    width: 100%;
    flex-direction: row;
    justify-content: space-between;
  }
  
  .notice-marquee {
    padding: 8px 12px;
  }
  
  .marquee-item {
    font-size: 13px;
  }
}

@media (max-width: 480px) {
  .welcome-section h1 {
    font-size: 22px;
  }
  
  .card {
    padding: 12px 14px;
    gap: 12px;
  }
  
  .card-icon[src] {
    width: 28px;
    height: 28px;
  }
  
  .card h3 {
    font-size: 14px;
  }
  
  .card p {
    font-size: 12px;
  }
  
  .card-count {
    font-size: 18px;
  }
  
  .section-header {
    padding: 14px 16px;
  }
  
  .view-more-btn {
    font-size: 13px;
    padding: 4px 8px;
  }
  
  .parcel-company {
    font-size: 14px;
  }
  
  .parcel-tracking {
    font-size: 12px;
  }
  
  .notice-title {
    font-size: 14px;
  }
  
  .notice-content {
    font-size: 12px;
  }
}
</style>
