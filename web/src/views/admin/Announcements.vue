<template>
  <AdminLayout>
    <div class="announcements-management">
    <div class="page-header">
      <h1>公告管理</h1>
      <p>发布和管理驿站公告信息</p>
    </div>

    <!-- 操作栏 -->
    <div class="action-bar">
      <button class="btn-primary" @click="showAddAnnouncement = true">发布公告</button>
    </div>

    <!-- 公告列表 -->
    <div class="announcements-list">
      <div v-if="announcementList.length === 0" class="empty-state">
        <img src="@/assets/icons/4.png" alt="暂无公告" class="empty-icon" />
        <p>暂无公告数据</p>
      </div>

      <div v-else class="announcement-card" v-for="item in announcementList" :key="item.id">
        <div class="card-header">
          <div class="header-left">
            <span :class="['type-badge', item.type]">{{ item.typeLabel }}</span>
            <h3>{{ item.title }}</h3>
          </div>
          <div class="header-right">
            <span :class="['status-badge', item.status]">
              {{ item.status === 'PUBLISHED' ? '已发布' : '草稿' }}
            </span>
            <button class="menu-btn" @click="toggleMenu(item.id)">⋮</button>
            <div class="dropdown-menu" v-if="activeMenuId === item.id">
              <button @click="editAnnouncement(item)">编辑</button>
              <button @click="toggleStatus(item)">
                {{ item.status === 'PUBLISHED' ? '取消发布' : '发布' }}
              </button>
              <button class="danger" @click="deleteAnnouncement(item.id)">删除</button>
            </div>
          </div>
        </div>
        
        <div class="card-body">
          <p>{{ item.content }}</p>
        </div>

        <div class="card-footer">
          <div class="footer-left">
            <span class="info-item">作者：{{ item.author }}</span>
            <span class="info-item">发布时间：{{ item.publishTime }}</span>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div class="pagination" v-if="announcementList.length > 0">
        <button class="page-btn" :disabled="currentPage === 1" @click="prevPage">上一页</button>
        <span class="page-info">第 {{ currentPage }} / {{ totalPages }} 页</span>
        <button class="page-btn" :disabled="currentPage === totalPages" @click="nextPage">下一页</button>
      </div>
    </div>

    <!-- 添加/编辑公告弹窗 -->
    <div class="modal" v-if="showAddAnnouncement || showEditAnnouncement" @click="closeModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>{{ showAddAnnouncement ? '发布公告' : '编辑公告' }}</h3>
          <button class="close-btn" @click="closeModal">×</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>公告类型 *</label>
            <select v-model="announcementForm.type" required>
              <option value="">请选择公告类型</option>
              <option value="important">重要公告</option>
              <option value="notice">通知公告</option>
              <option value="system">系统公告</option>
            </select>
          </div>

          <div class="form-group">
            <label>公告标题 *</label>
            <input 
              type="text" 
              v-model="announcementForm.title" 
              placeholder="请输入公告标题"
              maxlength="50"
              required
            >
            <div class="char-count">{{ announcementForm.title.length }} / 50</div>
          </div>

          <div class="form-group">
            <label>公告内容 *</label>
            <textarea 
              v-model="announcementForm.content" 
              placeholder="请输入公告内容"
              rows="8"
              maxlength="1000"
              required
            ></textarea>
            <div class="char-count">{{ announcementForm.content.length }} / 1000</div>
          </div>

          <div class="form-group">
            <label>发布状态</label>
            <div class="radio-group">
              <label class="radio-label">
                <input type="radio" v-model="announcementForm.status" value="DRAFT">
                <span>保存为草稿</span>
              </label>
              <label class="radio-label">
                <input type="radio" v-model="announcementForm.status" value="PUBLISHED">
                <span>立即发布</span>
              </label>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn-cancel" @click="closeModal">取消</button>
          <button class="btn-submit" @click="submitAnnouncement">确定</button>
        </div>
      </div>
    </div>
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import AdminLayout from '@/layouts/AdminLayout.vue'
import { 
  listNotices, 
  createNotice, 
  updateNotice, 
  deleteNotice,
  type Notice 
} from '@/api/admin/notice'
import { getCurrentAdminDetail, type AdminRole } from '@/api/admin/management'
import { useToast } from '@/composables/useToast'
import { useConfirm } from '@/composables/useConfirm'

const router = useRouter()
const { success, error: showError, warning, info } = useToast()
const { confirm } = useConfirm()

// 权限检查：需要省级管理员及以上权限
const REQUIRED_ROLE: AdminRole = 'MANAGER'
const roleLevel: Record<AdminRole, number> = {
  SUPERADMIN: 1,
  MANAGER: 2,
  STREET_ADMIN: 3
}
const roleDisplayName: Record<AdminRole, string> = {
  SUPERADMIN: '超级管理员',
  MANAGER: '省级管理员',
  STREET_ADMIN: '站点管理员'
}

const checkPermission = async () => {
  try {
    const detail = await getCurrentAdminDetail()
    const currentLevel = roleLevel[detail.role]
    const requiredLevel = roleLevel[REQUIRED_ROLE]
    if (currentLevel > requiredLevel) {
      warning(`权限不足：「公告管理」需要${roleDisplayName[REQUIRED_ROLE]}及以上权限，您当前是${roleDisplayName[detail.role]}`)
      router.replace('/admin/home')
      return false
    }
    return true
  } catch (error) {
    console.error('权限检查失败:', error)
    router.replace('/admin/home')
    return false
  }
}

interface Announcement {
  id: number
  type: string
  typeLabel: string
  title: string
  content: string
  status: string
  author: string
  publishTime: string
}

interface AnnouncementForm {
  id?: number
  type: string
  title: string
  content: string
  status: string
}

const currentPage = ref(1)
const pageSize = ref(10)
const totalPages = ref(1)
const activeMenuId = ref<number | null>(null)
const loading = ref(false)

const announcementList = ref<Announcement[]>([])
const showAddAnnouncement = ref(false)
const showEditAnnouncement = ref(false)

const announcementForm = reactive<AnnouncementForm>({
  type: '',
  title: '',
  content: '',
  status: 'PUBLISHED'
})

// 格式化日期时间
const formatDateTime = (dateStr: string) => {
  const date = new Date(dateStr)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

// 获取公告类型标签
const getTypeLabel = (type: string) => {
  const typeMap: Record<string, string> = {
    'important': '重要公告',
    'notice': '通知公告',
    'system': '系统公告'
  }
  return typeMap[type] || '通知公告'
}

// 根据标题推测类型
const getTypeFromTitle = (title: string) => {
  if (title.includes('重要') || title.includes('紧急')) return 'important'
  if (title.includes('系统')) return 'system'
  return 'notice'
}

// 加载公告列表
const loadAnnouncements = async () => {
  loading.value = true
  try {
    const response = await listNotices(currentPage.value - 1, pageSize.value)
    
    // 转换后端数据为前端格式
    announcementList.value = response.content.map((notice: Notice) => {
      const type = getTypeFromTitle(notice.title)
      return {
        id: notice.id!,
        type,
        typeLabel: getTypeLabel(type),
        title: notice.title,
        content: notice.content,
        status: 'PUBLISHED', // 后端暂无状态字段，默认已发布
        author: notice.creatorName || '管理员',
        publishTime: formatDateTime(notice.createTime!)
      }
    })
    
    totalPages.value = response.totalPages
  } catch (error) {
    console.error('加载公告列表失败:', error)
    showError('加载公告列表失败，请稍后重试')
    announcementList.value = []
  } finally {
    loading.value = false
  }
}

// 上一页
const prevPage = () => {
  if (currentPage.value > 1) {
    currentPage.value--
    loadAnnouncements()
  }
}

// 下一页
const nextPage = () => {
  if (currentPage.value < totalPages.value) {
    currentPage.value++
    loadAnnouncements()
  }
}

const toggleMenu = (id: number) => {
  activeMenuId.value = activeMenuId.value === id ? null : id
}

const editAnnouncement = (item: Announcement) => {
  announcementForm.id = item.id
  announcementForm.type = item.type
  announcementForm.title = item.title
  announcementForm.content = item.content
  announcementForm.status = item.status
  showEditAnnouncement.value = true
  activeMenuId.value = null
}

const toggleStatus = (item: Announcement) => {
  console.log('切换发布状态:', item)
  // TODO: 后端暂不支持状态切换，需要后续扩展
  info('状态切换功能暂未实现')
  activeMenuId.value = null
}

const deleteAnnouncement = async (id: number) => {
  const confirmed = await confirm({
    title: '删除公告',
    message: '确定要删除该公告吗？',
    type: 'danger'
  })
  if (!confirmed) {
    activeMenuId.value = null
    return
  }
  
  try {
    await deleteNotice(id)
    success('删除成功')
    // 如果当前页删除后为空且不是第一页，返回上一页
    if (announcementList.value.length === 1 && currentPage.value > 1) {
      currentPage.value--
    }
    await loadAnnouncements()
  } catch (error) {
    console.error('删除公告失败:', error)
    showError('删除失败，请稍后重试')
  }
  activeMenuId.value = null
}

const submitAnnouncement = async () => {
  if (!announcementForm.type || !announcementForm.title || !announcementForm.content) {
    warning('请填写完整信息')
    return
  }
  
  try {
    const noticeData: Notice = {
      title: announcementForm.title,
      content: announcementForm.content
    }
    
    if (showEditAnnouncement.value && announcementForm.id) {
      // 编辑模式
      await updateNotice(announcementForm.id, noticeData)
      success('更新成功')
    } else {
      // 新增模式
      await createNotice(noticeData)
      success('创建成功')
      currentPage.value = 1 // 跳转到第一页
    }
    
    closeModal()
    await loadAnnouncements()
  } catch (error) {
    console.error('提交公告失败:', error)
    showError('操作失败，请稍后重试')
  }
}

const closeModal = () => {
  showAddAnnouncement.value = false
  showEditAnnouncement.value = false
  Object.assign(announcementForm, {
    id: undefined,
    type: '',
    title: '',
    content: '',
    status: 'PUBLISHED'
  })
}

// 页面加载时获取公告列表
onMounted(async () => {
  const hasPermission = await checkPermission()
  if (hasPermission) {
    loadAnnouncements()
  }
})
</script>

<style scoped>
.announcements-management {
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
  margin-bottom: 24px;
}

.btn-primary {
  padding: 12px 24px;
  background: #808080;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.btn-primary:hover {
  background: #666666;
}

.announcements-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.empty-state {
  background: white;
  padding: 80px;
  border-radius: 12px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.empty-icon[src] {
  width: 64px;
  height: 64px;
  object-fit: contain;
}

.empty-state p {
  font-size: 16px;
  color: #999;
}

.announcement-card {
  background: white;
  border-radius: 10px;
  padding: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.3s;
}

.announcement-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.type-badge {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  white-space: nowrap;
}

.type-badge.important {
  background: #fff1f0;
  color: #f5222d;
}

.type-badge.notice {
  background: #e6f7ff;
  color: #1890ff;
}

.type-badge.system {
  background: #f6ffed;
  color: #52c41a;
}

.card-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
  position: relative;
}

.status-badge {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.status-badge.PUBLISHED {
  background: #f6ffed;
  color: #52c41a;
}

.status-badge.DRAFT {
  background: #f0f0f0;
  color: #999;
}

.menu-btn {
  width: 30px;
  height: 30px;
  border: none;
  background: none;
  font-size: 20px;
  color: #999;
  cursor: pointer;
  border-radius: 4px;
  transition: all 0.2s;
}

.menu-btn:hover {
  background: #f5f5f5;
}

.dropdown-menu {
  position: absolute;
  top: 100%;
  right: 0;
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  min-width: 120px;
  z-index: 10;
  overflow: hidden;
}

.dropdown-menu button {
  width: 100%;
  padding: 10px 16px;
  border: none;
  background: white;
  text-align: left;
  cursor: pointer;
  font-size: 14px;
  color: #333;
  transition: all 0.2s;
}

.dropdown-menu button:hover {
  background: #f5f5f5;
}

.dropdown-menu button.danger {
  color: #f5222d;
}

.card-body {
  margin-bottom: 16px;
}

.card-body p {
  font-size: 15px;
  color: #666;
  line-height: 1.8;
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.footer-left {
  display: flex;
  gap: 20px;
}

.info-item {
  font-size: 13px;
  color: #999;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px;
  padding: 10px;
  background: white;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.page-btn {
  padding: 8px 20px;
  background: white;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.page-btn:hover:not(:disabled) {
  border-color: #808080;
  color: #808080;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  font-size: 14px;
  color: #666;
}

/* 弹窗样式 */
.modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 12px;
  width: 90%;
  max-width: 700px;
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
  width: 30px;
  height: 30px;
  border: none;
  background: none;
  font-size: 24px;
  color: #999;
  cursor: pointer;
}

.modal-body {
  padding: 24px;
  max-height: 60vh;
  overflow-y: auto;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 8px;
}

.form-group input,
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 10px 16px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
}

.form-group textarea {
  resize: vertical;
  font-family: inherit;
}

.char-count {
  text-align: right;
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.radio-group {
  display: flex;
  gap: 20px;
}

.radio-label {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-size: 14px;
  color: #333;
}

.radio-label input[type="radio"] {
  width: auto;
  margin: 0;
}

.modal-footer {
  padding: 16px 24px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.btn-cancel,
.btn-submit {
  padding: 8px 24px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
}

.btn-cancel {
  background: white;
  border: 1px solid #e0e0e0;
  color: #333;
}

.btn-submit {
  background: #808080;
  color: white;
  border: none;
}

.btn-submit:hover {
  background: #666666;
}
</style>
