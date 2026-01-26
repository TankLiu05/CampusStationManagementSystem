<template>
  <AdminLayout>
    <div class="messages-management">
      <div class="page-header">
        <h1>留言管理</h1>
        <p>管理用户留言与反馈信息</p>
      </div>

      <!-- 操作栏 -->
      <div class="action-bar">
        <div class="search-section">
          <input 
            type="text" 
            v-model="searchKeyword" 
            placeholder="搜索留言内容或用户..."
            class="search-input"
            @keyup.enter="searchMessages"
          >
          <select v-model="filterStatus" class="filter-select">
            <option value="">全部状态</option>
            <option value="pending">待回复</option>
            <option value="replied">已回复</option>
            <option value="closed">已关闭</option>
          </select>
          <button class="search-btn" @click="searchMessages">搜索</button>
          <button class="reset-btn" @click="resetFilters">重置</button>
        </div>
      </div>

      <!-- 统计卡片 -->
      <div class="stats-cards">
        <div class="stat-card" @click="filterStatus = 'pending'; loadMessages()">
          <div class="stat-icon pending">
            <img src="@/assets/icons/16.png" alt="待回复" class="icon-img" />
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.pending }}</div>
            <div class="stat-label">待回复</div>
          </div>
        </div>
        <div class="stat-card" @click="filterStatus = 'replied'; loadMessages()">
          <div class="stat-icon replied">
            <img src="@/assets/icons/17.png" alt="已回复" class="icon-img" />
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.replied }}</div>
            <div class="stat-label">已回复</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon today">
            <img src="@/assets/icons/8.png" alt="今日新增" class="icon-img" />
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.todayNew }}</div>
            <div class="stat-label">今日新增</div>
          </div>
        </div>
      </div>

      <!-- 留言列表 -->
      <div class="messages-list">
        <div class="message-card" v-for="message in filteredMessages" :key="message.id">
          <div class="message-header">
            <div class="user-info">
              <div class="avatar">{{ message.userName.charAt(0) }}</div>
              <div class="user-detail">
                <span class="user-name">{{ message.userName }}</span>
                <span class="user-phone">{{ message.userPhone }}</span>
              </div>
            </div>
            <div class="message-meta">
              <span :class="['status-badge', message.status]">{{ getStatusLabel(message.status) }}</span>
            </div>
          </div>
          
          <div class="message-content">
            <p>{{ message.content }}</p>
            <div class="message-images" v-if="message.images && message.images.length > 0">
              <div class="image-thumb" v-for="(img, idx) in message.images" :key="idx">
                <span>📷</span>
              </div>
            </div>
          </div>

          <div class="message-reply" v-if="message.reply">
            <div class="reply-header">
              <span class="reply-label">管理员回复</span>
              <span class="reply-time">{{ message.replyTime }}</span>
            </div>
            <p class="reply-content">{{ message.reply }}</p>
          </div>

          <div class="message-footer">
            <span class="message-time">{{ message.createTime }}</span>
            <div class="message-actions">
              <button class="btn-view" @click="viewMessage(message)">查看详情</button>
              <button 
                v-if="message.status === 'pending'" 
                class="btn-reply" 
                @click="replyMessage(message)"
              >回复</button>
              <button 
                v-if="message.status !== 'closed'" 
                class="btn-close" 
                @click="closeMessage(message)"
              >关闭</button>
              <button class="btn-delete" @click="deleteMessageHandler(message.id)">删除</button>
            </div>
          </div>
        </div>

        <div class="empty-state" v-if="filteredMessages.length === 0">
          <p>暂无留言数据</p>
        </div>

        <!-- 分页 -->
        <div class="pagination" v-if="filteredMessages.length > 0">
          <button class="page-btn" :disabled="currentPage === 0" @click="currentPage--; loadMessages()">上一页</button>
          <span class="page-info">第 {{ currentPage + 1 }} / {{ totalPages }} 页,共 {{ total }} 条</span>
          <button class="page-btn" :disabled="currentPage >= totalPages - 1" @click="currentPage++; loadMessages()">下一页</button>
        </div>
      </div>

      <!-- 留言详情弹窗 -->
      <div class="modal" v-if="showMessageDetail" @click="showMessageDetail = false">
        <div class="modal-content detail-modal" @click.stop>
          <div class="modal-header">
            <h3>留言详情</h3>
            <button class="close-btn" @click="showMessageDetail = false">×</button>
          </div>
          <div class="modal-body">
            <div class="detail-section">
              <h4>用户信息</h4>
              <div class="detail-grid">
                <div class="detail-item">
                  <span class="label">用户名：</span>
                  <span class="value">{{ currentMessage?.userName }}</span>
                </div>
                <div class="detail-item">
                  <span class="label">联系电话：</span>
                  <span class="value">{{ currentMessage?.userPhone }}</span>
                </div>
                <div class="detail-item">
                  <span class="label">当前状态：</span>
                  <span :class="['status-badge', currentMessage?.status]">
                    {{ getStatusLabel(currentMessage?.status) }}
                  </span>
                </div>
              </div>
            </div>
            
            <div class="detail-section">
              <h4>留言内容</h4>
              <div class="content-box">
                <p>{{ currentMessage?.content }}</p>
              </div>
              <div class="detail-item">
                <span class="label">提交时间：</span>
                <span class="value">{{ currentMessage?.createTime }}</span>
              </div>
            </div>

            <div class="detail-section" v-if="currentMessage?.images && currentMessage.images.length > 0">
              <h4>附件图片</h4>
              <div class="image-gallery">
                <div class="image-item" v-for="(img, idx) in currentMessage.images" :key="idx">
                  <span>图片 {{ idx + 1 }}</span>
                </div>
              </div>
            </div>

            <div class="detail-section" v-if="currentMessage?.reply">
              <h4>回复记录</h4>
              <div class="reply-box">
                <div class="reply-header">
                  <span class="reply-by">管理员回复</span>
                  <span class="reply-time">{{ currentMessage.replyTime }}</span>
                </div>
                <p>{{ currentMessage.reply }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 回复弹窗 -->
      <div class="modal" v-if="showReplyModal" @click="showReplyModal = false">
        <div class="modal-content" @click.stop>
          <div class="modal-header">
            <h3>回复留言</h3>
            <button class="close-btn" @click="showReplyModal = false">×</button>
          </div>
          <div class="modal-body">
            <div class="original-message">
              <div class="message-user">
                <span class="user">{{ currentMessage?.userName }}</span>
                <span class="time">{{ currentMessage?.createTime }}</span>
              </div>
              <p>{{ currentMessage?.content }}</p>
            </div>
            <div class="form-group">
              <label>回复内容 *</label>
              <textarea 
                v-model="replyContent" 
                placeholder="请输入回复内容..." 
                rows="5"
              ></textarea>
            </div>
            <div class="quick-replies">
              <span class="quick-label">快捷回复：</span>
              <button 
                class="quick-btn" 
                v-for="(reply, idx) in quickReplies" 
                :key="idx"
                @click="replyContent = reply"
              >{{ reply.substring(0, 10) }}...</button>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn-cancel" @click="showReplyModal = false">取消</button>
            <button class="btn-submit" @click="submitReply">发送回复</button>
          </div>
        </div>
      </div>
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import AdminLayout from '@/layouts/AdminLayout.vue'
import { useToast } from '@/composables/useToast'
import { useConfirm } from '@/composables/useConfirm'
import { useAutoRefresh } from '@/composables/useAutoRefresh'
import { 
  getMessageList, 
  replyMessage as apiReplyMessage, 
  deleteMessage as apiDeleteMessage,
  type Message as ApiMessage,
  type PageResponse
} from '@/api/admin/message'

const { success, warning, info } = useToast()
const { confirm } = useConfirm()

interface Message {
  id: number
  userName: string
  userPhone: string
  content: string
  images?: string[]
  status: string
  createTime: string
  reply?: string
  replyTime?: string
}

const searchKeyword = ref('')
const filterStatus = ref('')
const currentPage = ref(0) // 后端分页从0开始
const totalPages = ref(0)
const total = ref(0)
const pageSize = ref(10)
const loading = ref(false)

const showMessageDetail = ref(false)
const showReplyModal = ref(false)
const currentMessage = ref<Message | null>(null)
const replyContent = ref('')

const stats = reactive({
  pending: 12,
  replied: 45,
  todayNew: 8
})

const quickReplies = [
  '感谢您的反馈，我们会尽快处理您的问题。',
  '您好，您的问题已收到，我们将在24小时内回复您。',
  '非常抱歉给您带来不便，我们已记录您的反馈并会改进服务。',
  '感谢您的表扬，我们会继续努力提供更好的服务！',
  '您的建议非常宝贵，我们会认真考虑并改进。'
]

const messageList = ref<Message[]>([])

// 转换API数据为页面数据格式
const convertApiToMessage = (apiMsg: ApiMessage): Message => {
  return {
    id: apiMsg.id,
    userName: apiMsg.username || '未知用户',
    userPhone: apiMsg.phone || '未提供',
    content: apiMsg.content,
    status: apiMsg.status === 0 ? 'pending' : 'replied',
    createTime: apiMsg.createTime,
    reply: apiMsg.replyContent,
    replyTime: apiMsg.replyContent ? apiMsg.updateTime : undefined
  }
}

// 加载留言列表
const loadMessages = async () => {
  loading.value = true
  try {
    const statusFilter = filterStatus.value === 'pending' ? 0 : filterStatus.value === 'replied' ? 1 : undefined
    const response = await getMessageList(currentPage.value, pageSize.value, statusFilter)
    messageList.value = response.content.map(convertApiToMessage)
    total.value = response.totalElements
    totalPages.value = response.totalPages
    
    // 更新统计数据
    updateStats()
  } catch (error) {
    warning('加载留言列表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 更新统计数据
const updateStats = async () => {
  try {
    const pendingRes = await getMessageList(0, 1, 0)
    stats.pending = pendingRes.totalElements
    
    const repliedRes = await getMessageList(0, 1, 1)
    stats.replied = repliedRes.totalElements
    
    // 今日新增暂时使用模拟数据,后端可能需要额外接口
    stats.todayNew = 0
  } catch (error) {
    console.error('更新统计数据失败', error)
  }
}

// 使用自动刷新功能
useAutoRefresh(loadMessages)

onMounted(() => {
  // 初始加载已由useAutoRefresh处理
})

const filteredMessages = computed(() => {
  let result = messageList.value
  if (filterStatus.value) {
    result = result.filter(m => m.status === filterStatus.value)
  }
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(m => 
      m.content.toLowerCase().includes(keyword) ||
      m.userName.toLowerCase().includes(keyword)
    )
  }
  return result
})

const getStatusLabel = (status?: string) => {
  const labels: Record<string, string> = {
    'pending': '待回复',
    'replied': '已回复',
    'closed': '已关闭'
  }
  return labels[status || ''] || '未知'
}

const searchMessages = () => {
  currentPage.value = 0
  loadMessages()
}

const resetFilters = () => {
  searchKeyword.value = ''
  filterStatus.value = ''
  currentPage.value = 0
  loadMessages()
}

const viewMessage = (message: Message) => {
  currentMessage.value = message
  showMessageDetail.value = true
}

const replyMessage = (message: Message) => {
  currentMessage.value = message
  replyContent.value = ''
  showReplyModal.value = true
}

const submitReply = async () => {
  if (!replyContent.value.trim()) {
    warning('请输入回复内容')
    return
  }
  if (!currentMessage.value) return
  
  try {
    await apiReplyMessage(currentMessage.value.id, { replyContent: replyContent.value })
    success('回复成功')
    showReplyModal.value = false
    replyContent.value = ''
    loadMessages()
  } catch (error) {
    warning('回复失败')
    console.error(error)
  }
}

const closeMessage = async (message: Message) => {
  const confirmed = await confirm({
    title: '关闭留言',
    message: '确定要关闭该留言吗？关闭后用户将无法继续追问。',
    type: 'warning'
  })
  if (confirmed) {
    success('关闭成功（模拟）')
  }
}

const deleteMessageHandler = async (id: number) => {
  const confirmed = await confirm({
    title: '删除留言',
    message: '确定要删除该留言吗?',
    type: 'danger'
  })
  if (confirmed) {
    try {
      await apiDeleteMessage(id)
      success('删除成功')
      loadMessages()
    } catch (error) {
      warning('删除失败')
      console.error(error)
    }
  }
}

const exportMessages = () => {
  info('导出数据（模拟）')
}
</script>

<style scoped>
.messages-management {
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
  min-width: 120px;
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

.btn-secondary {
  padding: 10px 20px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  background: white;
  color: #333;
  border: 1px solid #e0e0e0;
  transition: all 0.2s;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
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
  cursor: pointer;
  transition: all 0.2s;
}

.stat-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
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

.stat-icon.pending {
  background: #f5f5f5;
}

.stat-icon.replied {
  background: #f5f5f5;
}

.stat-icon.complaint {
  background: #f5f5f5;
}

.stat-icon.today {
  background: #f5f5f5;
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

/* Message List Styles */
.messages-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.message-card {
  background: white;
  border-radius: 10px;
  padding: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.avatar {
  width: 40px;
  height: 40px;
  background: #808080;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 16px;
}

.user-detail {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.user-name {
  font-weight: 600;
  color: #333;
  font-size: 15px;
}

.user-phone {
  font-size: 13px;
  color: #999;
}

.message-meta {
  display: flex;
  gap: 10px;
  align-items: center;
}

.status-badge {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.status-badge.pending {
  background: #fff7e6;
  color: #fa8c16;
}

.status-badge.replied {
  background: #f6ffed;
  color: #52c41a;
}

.status-badge.closed {
  background: #f0f0f0;
  color: #666;
}

.message-content {
  margin-bottom: 16px;
}

.message-content p {
  font-size: 14px;
  color: #333;
  line-height: 1.6;
  margin-bottom: 12px;
}

.message-images {
  display: flex;
  gap: 8px;
}

.image-thumb {
  width: 60px;
  height: 60px;
  background: #f5f5f5;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}

.message-reply {
  background: #f9f9f9;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
}

.message-reply .reply-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.message-reply .reply-label {
  font-size: 13px;
  font-weight: 600;
  color: #666;
}

.message-reply .reply-time {
  font-size: 12px;
  color: #999;
}

.message-reply .reply-content {
  font-size: 14px;
  color: #333;
  line-height: 1.5;
}

.message-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.message-time {
  font-size: 13px;
  color: #999;
}

.message-actions {
  display: flex;
  gap: 8px;
}

.btn-view, .btn-reply, .btn-close, .btn-delete {
  padding: 6px 14px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
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

.btn-reply {
  background: white;
  color: #52c41a;
  border: 1px solid #52c41a;
}

.btn-reply:hover {
  background: #52c41a;
  color: white;
}

.btn-close {
  background: white;
  color: #808080;
  border: 1px solid #808080;
}

.btn-close:hover {
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

.empty-state {
  text-align: center;
  padding: 60px 20px;
  background: white;
  border-radius: 12px;
}

.empty-state span {
  font-size: 48px;
  display: block;
  margin-bottom: 16px;
}

.empty-state p {
  color: #999;
  font-size: 15px;
}

.pagination {
  padding: 10px;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px;
  background: white;
  border-radius: 10px;
  margin-top: 5px;
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

/* Detail Section Styles */
.detail-section {
  margin-bottom: 24px;
}

.detail-section h4 {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.detail-item {
  font-size: 14px;
}

.detail-item .label {
  color: #999;
  margin-right: 8px;
}

.detail-item .value {
  color: #333;
}

.content-box {
  background: #f9f9f9;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
}

.content-box p {
  font-size: 14px;
  color: #333;
  line-height: 1.6;
}

.image-gallery {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.image-item {
  width: 80px;
  height: 80px;
  background: #f5f5f5;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: #666;
}

.reply-box {
  background: #f6ffed;
  border: 1px solid #b7eb8f;
  border-radius: 8px;
  padding: 16px;
}

.reply-box .reply-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.reply-box .reply-by {
  font-weight: 600;
  color: #52c41a;
  font-size: 13px;
}

.reply-box .reply-time {
  font-size: 12px;
  color: #999;
}

.reply-box p {
  font-size: 14px;
  color: #333;
  line-height: 1.5;
}

/* Reply Modal Styles */
.original-message {
  background: #f9f9f9;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 20px;
}

.original-message .message-user {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.original-message .user {
  font-weight: 600;
  color: #333;
}

.original-message .time {
  font-size: 12px;
  color: #999;
}

.original-message p {
  font-size: 14px;
  color: #666;
  line-height: 1.5;
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

.form-group textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
  resize: vertical;
}

.quick-replies {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.quick-label {
  font-size: 13px;
  color: #666;
}

.quick-btn {
  padding: 6px 12px;
  background: #f5f5f5;
  border: 1px solid #e0e0e0;
  border-radius: 16px;
  font-size: 12px;
  color: #666;
  cursor: pointer;
  transition: all 0.2s;
}

.quick-btn:hover {
  background: #e0e0e0;
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
  .stats-cards {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .stats-cards {
    grid-template-columns: 1fr;
  }
  .detail-grid {
    grid-template-columns: 1fr;
  }
  .message-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  .message-footer {
    flex-direction: column;
    gap: 12px;
  }
  .message-actions {
    width: 100%;
    justify-content: flex-end;
  }
}
</style>
