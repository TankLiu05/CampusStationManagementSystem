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
          <select v-model="filterType" class="filter-select">
            <option value="">全部类型</option>
            <option value="feedback">意见反馈</option>
            <option value="complaint">投诉建议</option>
            <option value="inquiry">咨询问题</option>
            <option value="praise">表扬感谢</option>
            <option value="other">其他</option>
          </select>
          <select v-model="filterStatus" class="filter-select">
            <option value="">全部状态</option>
            <option value="pending">待回复</option>
            <option value="replied">已回复</option>
            <option value="closed">已关闭</option>
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
        <div class="stat-card" @click="filterStatus = 'pending'">
          <div class="stat-icon pending">
            <img src="@/assets/icons/16.png" alt="待回复" class="icon-img" />
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.pending }}</div>
            <div class="stat-label">待回复</div>
          </div>
        </div>
        <div class="stat-card" @click="filterStatus = 'replied'">
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
              <span :class="['type-tag', message.type]">{{ getTypeLabel(message.type) }}</span>
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
          <span>📭</span>
          <p>暂无留言记录</p>
          <button class="btn-primary" @click="showNewMessageModal = true">发表第一条留言</button>
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
              <label>留言类型 *</label>
              <select v-model="newMessage.type">
                <option value="">请选择类型</option>
                <option value="feedback">意见反馈</option>
                <option value="complaint">投诉建议</option>
                <option value="inquiry">咨询问题</option>
                <option value="praise">表扬感谢</option>
                <option value="other">其他</option>
              </select>
            </div>
            <div class="form-group">
              <label>留言内容 *</label>
              <textarea 
                v-model="newMessage.content" 
                placeholder="请输入您的留言内容..." 
                rows="5"
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
                <span class="label">留言类型：</span>
                <span :class="['type-tag', currentMessage?.type]">
                  {{ getTypeLabel(currentMessage?.type) }}
                </span>
              </div>
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
import { ref, reactive, computed } from 'vue'
import UserLayout from '@/layouts/UserLayout.vue'

interface Message {
  id: number
  type: string
  content: string
  status: string
  createTime: string
  reply?: string
  replyTime?: string
}

const filterType = ref('')
const filterStatus = ref('')

const showNewMessageModal = ref(false)
const showMessageDetail = ref(false)
const currentMessage = ref<Message | null>(null)

const newMessage = reactive({
  type: '',
  content: ''
})

const stats = reactive({
  total: 5,
  pending: 2,
  replied: 3
})

// 模拟数据
const messageList = ref<Message[]>([
  {
    id: 1,
    type: 'inquiry',
    content: '请问周末驿站营业吗？营业时间是几点到几点？',
    status: 'replied',
    createTime: '2026-01-19 10:20',
    reply: '您好，驿站周末正常营业，营业时间为08:00-21:00，欢迎您前来取件。',
    replyTime: '2026-01-19 11:00'
  },
  {
    id: 2,
    type: 'feedback',
    content: '建议增加短信提醒功能，每次有快递到了能收到短信通知就更方便了。',
    status: 'replied',
    createTime: '2026-01-18 16:45',
    reply: '感谢您的建议，我们正在开发短信通知功能，预计下个月上线，届时会第一时间通知您。',
    replyTime: '2026-01-18 18:00'
  },
  {
    id: 3,
    type: 'praise',
    content: '今天取件非常快，工作人员服务态度很好，点个赞！希望继续保持！',
    status: 'replied',
    createTime: '2026-01-18 12:30',
    reply: '非常感谢您的认可和鼓励，我们会继续努力，为大家提供更好的服务！',
    replyTime: '2026-01-18 14:00'
  },
  {
    id: 4,
    type: 'complaint',
    content: '今天取件的时候等了很久，希望能改善服务效率。',
    status: 'pending',
    createTime: '2026-01-19 14:30',
  },
  {
    id: 5,
    type: 'inquiry',
    content: '请问大件包裹怎么取？我的包裹比较重，一个人搬不动。',
    status: 'pending',
    createTime: '2026-01-19 15:00',
  },
])

const filteredMessages = computed(() => {
  let result = messageList.value
  if (filterType.value) {
    result = result.filter(m => m.type === filterType.value)
  }
  if (filterStatus.value) {
    result = result.filter(m => m.status === filterStatus.value)
  }
  return result
})

const getTypeLabel = (type?: string) => {
  const labels: Record<string, string> = {
    'feedback': '意见反馈',
    'complaint': '投诉建议',
    'inquiry': '咨询问题',
    'praise': '表扬感谢',
    'other': '其他'
  }
  return labels[type || ''] || '未知'
}

const getStatusLabel = (status?: string) => {
  const labels: Record<string, string> = {
    'pending': '待回复',
    'replied': '已回复',
    'closed': '已关闭'
  }
  return labels[status || ''] || '未知'
}

const viewMessage = (message: Message) => {
  currentMessage.value = message
  showMessageDetail.value = true
}

const submitMessage = () => {
  if (!newMessage.type) {
    alert('请选择留言类型')
    return
  }
  if (!newMessage.content.trim()) {
    alert('请输入留言内容')
    return
  }
  alert('留言提交成功（模拟）')
  showNewMessageModal.value = false
  newMessage.type = ''
  newMessage.content = ''
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
