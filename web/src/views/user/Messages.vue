<template>
  <UserLayout>
    <div class="messages-page">
      <div class="page-header">
        <h1>留言信息</h1>
        <p>查看您的留言与管理员回复</p>
      </div>

      <!-- 操作栏 -->
      <div class="action-bar">
        <div class="filter-section">
          <select v-model="filterStatus" class="filter-select">
            <option value="">全部状态</option>
            <option value="pending">待回复</option>
            <option value="replied">已回复</option>
          </select>
        </div>
        <button class="btn-primary" @click="showNewMessageModal = true">
           新建留言
        </button>
      </div>

      <!-- 统计卡片 -->
      <div class="stats-cards">
        <div class="stat-card">
          <div class="stat-icon total">
            <img src="@/assets/icons/email.png" alt="全部" class="icon-img" />
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.total }}</div>
            <div class="stat-label">全部留言</div>
          </div>
        </div>
        <div class="stat-card" style="cursor: pointer;" @click="filterStatus = filterStatus === 'pending' ? '' : 'pending'">
          <div class="stat-icon pending">
            <img src="@/assets/icons/16.png" alt="待回复" class="icon-img" />
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.pending }}</div>
            <div class="stat-label">待回复</div>
          </div>
        </div>
        <div class="stat-card" style="cursor: pointer;" @click="filterStatus = filterStatus === 'replied' ? '' : 'replied'">
          <div class="stat-icon replied">
            <img src="@/assets/icons/17.png" alt="已回复" class="icon-img" />
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.replied }}</div>
            <div class="stat-label">已回复</div>
          </div>
        </div>
      </div>

      <!-- 留言列表 -->
      <div class="messages-list">
        <div class="message-card" v-for="message in filteredMessages" :key="message.id">
          <div class="message-header">
            <div class="message-info">
              <span :class="['status-badge', message.status]">{{ getStatusLabel(message.status) }}</span>
            </div>
            <span class="message-time">{{ message.createTime }}</span>
          </div>
          
          <div class="message-content">
            <p>{{ message.content }}</p>
          </div>

          <div class="message-reply" v-if="message.reply">
            <div class="reply-header">
              <span class="reply-label">管理员回复</span>
              <span class="reply-time">{{ message.replyTime }}</span>
            </div>
            <p class="reply-content">{{ message.reply }}</p>
          </div>

          <div class="message-footer">
            <button class="btn-detail" @click="viewMessage(message)">查看详情</button>
          </div>
        </div>

        <div class="empty-state" v-if="filteredMessages.length === 0">
          <p>暂无留言记录</p>
        </div>
      </div>

      <!-- 新建留言弹窗 -->
      <div class="modal" v-if="showNewMessageModal" @click="showNewMessageModal = false">
        <div class="modal-content" @click.stop>
          <div class="modal-header">
            <h3>新建留言</h3>
            <button class="close-btn" @click="showNewMessageModal = false">×</button>
          </div>
          <div class="modal-body">
            <div class="form-group">
              <label>留言内容 *</label>
              <textarea 
                v-model="newMessage.content" 
                placeholder="请输入您的留言内容..." 
                rows="8"
              ></textarea>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn-cancel" @click="showNewMessageModal = false">取消</button>
            <button class="btn-submit" @click="submitMessage">提交留言</button>
          </div>
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
              <div class="detail-row">
                <span class="label">当前状态：</span>
                <span :class="['status-badge', currentMessage?.status]">
                  {{ getStatusLabel(currentMessage?.status) }}
                </span>
              </div>
              <div class="detail-row">
                <span class="label">提交时间：</span>
                <span class="value">{{ currentMessage?.createTime }}</span>
              </div>
            </div>
            
            <div class="detail-section">
              <h4>留言内容</h4>
              <div class="content-box">
                <p>{{ currentMessage?.content }}</p>
              </div>
            </div>

            <div class="detail-section" v-if="currentMessage?.reply">
              <h4>管理员回复</h4>
              <div class="reply-box">
                <div class="reply-header">
                  <span class="reply-time">{{ currentMessage?.replyTime }}</span>
                </div>
                <p>{{ currentMessage?.reply }}</p>
              </div>
            </div>

            <div class="detail-section waiting-reply" v-else>
              <p>等待管理员回复中...</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </UserLayout>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import UserLayout from '@/layouts/UserLayout.vue'
import { useToast } from '@/composables/useToast'
import { 
  getMyMessages, 
  createMessage,
  type Message as ApiMessage
} from '@/api/user/message'

const { success, warning } = useToast()

interface Message {
  id: number
  content: string
  status: string
  createTime: string
  reply?: string
  replyTime?: string
}

const filterStatus = ref('')
const loading = ref(false)

const showNewMessageModal = ref(false)
const showMessageDetail = ref(false)
const currentMessage = ref<Message | null>(null)

const newMessage = reactive({
  content: ''
})

const stats = reactive({
  total: 0,
  pending: 0,
  replied: 0
})

const messageList = ref<Message[]>([])

// 转换API数据为页面数据格式
const convertApiToMessage = (apiMsg: ApiMessage): Message => {
  return {
    id: apiMsg.id,
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
    const response = await getMyMessages()
    messageList.value = response.map(convertApiToMessage)
    
    // 更新统计数据
    stats.total = messageList.value.length
    stats.pending = messageList.value.filter(m => m.status === 'pending').length
    stats.replied = messageList.value.filter(m => m.status === 'replied').length
  } catch (error) {
    warning('加载留言列表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadMessages()
})

const filteredMessages = computed(() => {
  let result = messageList.value
  if (filterStatus.value) {
    result = result.filter(m => m.status === filterStatus.value)
  }
  return result
})

const getStatusLabel = (status?: string) => {
  const labels: Record<string, string> = {
    'pending': '待回复',
    'replied': '已回复'
  }
  return labels[status || ''] || '未知'
}

const viewMessage = (message: Message) => {
  currentMessage.value = message
  showMessageDetail.value = true
}

const submitMessage = async () => {
  if (!newMessage.content.trim()) {
    warning('请输入留言内容')
    return
  }
  
  try {
    await createMessage({ content: newMessage.content })
    success('留言提交成功')
    showNewMessageModal.value = false
    newMessage.content = ''
    loadMessages()
  } catch (error) {
    warning('留言提交失败')
    console.error(error)
  }
}
</script>

<style scoped>
.messages-page {
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

.filter-section {
  display: flex;
  gap: 12px;
}

.filter-select {
  padding: 10px 16px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
  background: white;
  min-width: 120px;
}

.btn-primary {
  padding: 10px 20px;
  background: #808080;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: all 0.2s;
}

.btn-primary:hover {
  background: #666666;
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
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  background: #f5f5f5;
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

/* Message List Styles */
.messages-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.message-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.message-info {
  display: flex;
  gap: 10px;
  align-items: center;
}

.type-tag {
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.type-tag.feedback {
  background: #e6f7ff;
  color: #1890ff;
}

.type-tag.complaint {
  background: #fff1f0;
  color: #f5222d;
}

.type-tag.inquiry {
  background: #fff7e6;
  color: #fa8c16;
}

.type-tag.praise {
  background: #f6ffed;
  color: #52c41a;
}

.type-tag.other {
  background: #f0f0f0;
  color: #666;
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

.message-time {
  font-size: 13px;
  color: #999;
}

.message-content {
  margin-bottom: 16px;
}

.message-content p {
  font-size: 14px;
  color: #333;
  line-height: 1.6;
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
  justify-content: flex-end;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.btn-detail {
  padding: 8px 16px;
  background: white;
  color: #808080;
  border: 1px solid #808080;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.2s;
}

.btn-detail:hover {
  background: #808080;
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
  margin-bottom: 20px;
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
  width: 550px;
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
  font-weight: 500;
  color: #333;
  margin-bottom: 8px;
}

.form-group select,
.form-group textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
  box-sizing: border-box;
}

.form-group textarea {
  resize: vertical;
  min-height: 120px;
}

.btn-cancel {
  padding: 10px 20px;
  background: white;
  color: #333;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
}

.btn-submit {
  padding: 10px 20px;
  background: #808080;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
}

.btn-submit:hover {
  background: #666666;
}

/* Detail Styles */
.detail-section {
  margin-bottom: 24px;
}

.detail-section h4 {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
}

.detail-row {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.detail-row .label {
  font-size: 14px;
  color: #666;
  min-width: 80px;
}

.detail-row .value {
  font-size: 14px;
  color: #333;
}

.content-box {
  background: #f9f9f9;
  padding: 16px;
  border-radius: 8px;
}

.content-box p {
  font-size: 14px;
  color: #333;
  line-height: 1.6;
}

.reply-box {
  background: #f0f9eb;
  padding: 16px;
  border-radius: 8px;
}

.reply-box .reply-header {
  margin-bottom: 8px;
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

.waiting-reply {
  background: #fffbe6;
  padding: 16px;
  border-radius: 8px;
  text-align: center;
}

.waiting-reply p {
  font-size: 14px;
  color: #fa8c16;
  margin: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .page-header h1 {
    font-size: 24px;
  }
  
  .page-header p {
    font-size: 14px;
  }
  
  .action-bar {
    flex-direction: column;
    gap: 12px;
  }
  
  .filter-section {
    width: 100%;
    flex-wrap: wrap;
  }
  
  .filter-select {
    flex: 1;
    min-width: 100px;
  }
  
  .btn-primary {
    width: 100%;
    justify-content: center;
  }
  
  .stats-cards {
    grid-template-columns: repeat(3, 1fr);
    gap: 12px;
  }
  
  .stat-card {
    padding: 16px;
    flex-direction: column;
    text-align: center;
    gap: 8px;
  }
  
  .stat-icon {
    width: 48px;
    height: 48px;
  }
  
  .stat-icon .icon-img {
    width: 24px;
    height: 24px;
  }
  
  .stat-value {
    font-size: 20px;
  }
  
  .message-card {
    padding: 16px;
  }
  
  .message-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .modal-content {
    width: 95%;
  }
}

@media (max-width: 480px) {
  .page-header h1 {
    font-size: 22px;
  }
  
  .stats-cards {
    grid-template-columns: 1fr;
  }
  
  .stat-card {
    flex-direction: row;
    text-align: left;
  }
  
  .filter-section {
    flex-direction: column;
  }
  
  .filter-select {
    width: 100%;
  }
  
  .type-tag,
  .status-badge {
    font-size: 11px;
    padding: 3px 8px;
  }
  
  .message-content p {
    font-size: 13px;
  }
  
  .modal-content {
    max-height: 85vh;
  }
  
  .modal-header {
    padding: 16px;
  }
  
  .modal-body {
    padding: 16px;
  }
  
  .modal-footer {
    padding: 12px 16px;
  }
}
</style>
