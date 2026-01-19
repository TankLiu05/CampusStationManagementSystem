<template>
  <UserLayout>
    <div class="profile">
    <div class="page-header">
      <h1>个人中心</h1>
      <p>管理您的个人信息和账户设置</p>
    </div>

    <div class="profile-container">
      <!-- 个人信息卡片 -->
      <div class="info-card">
        <div class="card-header">
          <h2>基本信息</h2>
          <button class="edit-btn" @click="toggleEdit" v-if="!isEditing">编辑</button>
          <div v-else class="edit-actions">
            <button class="save-btn" @click="saveProfile">保存</button>
            <button class="cancel-btn" @click="cancelEdit">取消</button>
          </div>
        </div>

        <div class="card-body">
          <div class="avatar-section">
          </div>

          <div class="info-grid">
            <div class="info-item">
              <label>用户名</label>
              <input 
                v-if="isEditing" 
                v-model="editForm.username" 
                type="text"
                disabled
              >
              <span v-else>{{ userInfo.username }}</span>
            </div>

            <div class="info-item">
              <label>手机号</label>
              <input 
                v-if="isEditing" 
                v-model="editForm.phone" 
                type="tel"
              >
              <span v-else>{{ userInfo.phone || '未设置' }}</span>
            </div>

            <div class="info-item">
              <label>邮箱</label>
              <input 
                v-if="isEditing" 
                v-model="editForm.email" 
                type="email"
                disabled
              >
              <span v-else>{{ userInfo.email || '未设置' }}</span>
            </div>

            <div class="info-item">
              <label>注册时间</label>
              <span>{{ userInfo.createTime }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 账户安全 -->
      <div class="security-card">
        <div class="card-header">
          <h2>账户安全</h2>
        </div>
        <div class="card-body">
          <div class="security-item">
            <div class="security-left">
              <div class="security-info">
                <h3>登录密码</h3>
                <p>定期更换密码可以保护账户安全</p>
              </div>
            </div>
            <button class="action-btn" @click="showChangePassword = true">修改密码</button>
          </div>
        </div>
      </div>

    </div>

    <!-- 修改密码弹窗 -->
    <div class="modal" v-if="showChangePassword" @click="showChangePassword = false">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>修改密码</h3>
          <button class="close-btn" @click="showChangePassword = false">×</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>旧密码</label>
            <input type="password" v-model="passwordForm.oldPassword" placeholder="请输入旧密码">
          </div>
          <div class="form-group">
            <label>新密码</label>
            <input type="password" v-model="passwordForm.newPassword" placeholder="请输入新密码">
          </div>
          <div class="form-group">
            <label>确认密码</label>
            <input type="password" v-model="passwordForm.confirmPassword" placeholder="请再次输入新密码">
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn-cancel" @click="showChangePassword = false">取消</button>
          <button class="btn-submit" @click="changePassword">确认修改</button>
        </div>
      </div>
    </div>
    </div>
  </UserLayout>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import UserLayout from '@/layouts/UserLayout.vue'
import { getUserProfile, updateUserProfile, changeUserPassword, type UserProfile as UserProfileType, type ChangePasswordRequest } from '@/api/user/profile'

interface UserInfo {
  username: string
  phone?: string
  email?: string
  avatar?: string
  createTime: string
}

interface EditForm {
  username?: string
  phone?: string
  email?: string
}

interface PasswordForm {
  oldPassword: string
  newPassword: string
  confirmPassword: string
}

const isEditing = ref(false)
const showChangePassword = ref(false)

const userInfo = reactive<UserInfo>({
  username: 'user123',
  phone: '',
  email: '',
  avatar: '',
  createTime: '2024-01-01'
})

const editForm = reactive<EditForm>({
  username: '',
  phone: '',
  email: ''
})

const passwordForm = reactive<PasswordForm>({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 页面加载时获取用户信息
onMounted(async () => {
  await loadProfile()
})

// 加载用户个人信息
const loadProfile = async () => {
  try {
    const data = await getUserProfile()
    // 格式化时间
    const formattedTime = data.createTime ? new Date(data.createTime).toLocaleString('zh-CN') : ''
    Object.assign(userInfo, {
      username: data.username,
      phone: data.phone || '',
      email: data.email || '',
      avatar: data.avatar || '',
      createTime: formattedTime
    })
  } catch (error) {
    console.error('获取用户信息失败:', error)
    alert('获取用户信息失败，请稍后重试')
  }
}

const toggleEdit = () => {
  isEditing.value = true
  Object.assign(editForm, userInfo)
}

const cancelEdit = () => {
  isEditing.value = false
}

const saveProfile = async () => {
  try {
    const updateData = {
      username: editForm.username,
      phone: editForm.phone
    }
    
    await updateUserProfile(updateData)
    
    // 重新加载最新数据
    await loadProfile()
    
    isEditing.value = false
    alert('保存成功')
  } catch (error) {
    console.error('保存失败:', error)
    alert('保存失败，请稍后重试')
  }
}

const changePassword = async () => {
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    alert('两次输入的密码不一致')
    return
  }
  
  if (!passwordForm.oldPassword || !passwordForm.newPassword) {
    alert('请填写完整的密码信息')
    return
  }
  
  try {
    const data: ChangePasswordRequest = {
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    }
    
    await changeUserPassword(data)
    
    // 清空表单
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
    
    showChangePassword.value = false
    alert('密码修改成功')
    
    // 可选：跳转到登录页
    // window.location.href = '/login'
  } catch (error) {
    console.error('修改密码失败:', error)
    alert('修改密码失败，请检查旧密码是否正确')
  }
}
</script>

<style scoped>
.profile {
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

.profile-container {
  display: grid;
  gap: 24px;
}

.info-card,
.security-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  overflow: hidden;
}

.card-header {
  padding: 24px 30px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a1a;
}

.edit-btn,
.save-btn,
.cancel-btn {
  padding: 8px 20px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.edit-btn {
  background: white;
  border: 1px solid #e0e0e0;
  color: #333;
}

.edit-btn:hover {
  border-color: #10b981;
  color: #10b981;
}

.edit-actions {
  display: flex;
  gap: 12px;
}

.save-btn {
  background: #10b981;
  color: white;
  border: none;
}

.save-btn:hover {
  background: #059669;
}

.cancel-btn {
  background: white;
  border: 1px solid #e0e0e0;
  color: #333;
}

.cancel-btn:hover {
  background: #f5f5f5;
}

.card-body {
  padding: 30px;
}

.avatar-section {
  text-align: center;
  margin-bottom: 30px;
}

.avatar {
  display: inline-block;
  position: relative;
}

.avatar img {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  object-fit: cover;
  border: 4px solid #f0f0f0;
}

.change-avatar-btn {
  position: absolute;
  bottom: 0;
  right: 0;
  padding: 6px 12px;
  background: white;
  border: 1px solid #e0e0e0;
  border-radius: 20px;
  cursor: pointer;
  font-size: 12px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-item label {
  font-size: 14px;
  font-weight: 500;
  color: #666;
}

.info-item input {
  padding: 10px 16px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
  color: #333;
}

.info-item input:disabled {
  background: #f5f5f5;
  cursor: not-allowed;
}

.info-item span {
  padding: 10px 0;
  font-size: 15px;
  color: #333;
}

.security-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 0;
  border-bottom: 1px solid #f0f0f0;
}

.security-item:last-child {
  border-bottom: none;
}

.security-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.security-info h3 {
  font-size: 15px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 4px;
}

.security-info p {
  font-size: 13px;
  color: #999;
}

.action-btn {
  padding: 8px 24px;
  background: white;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  color: #333;
  transition: all 0.2s;
}

.action-btn:hover {
  border-color: #666;
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
  max-width: 500px;
  max-height: 90vh;
  overflow: auto;
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
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-body {
  padding: 24px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group:last-child {
  margin-bottom: 0;
}

.form-group label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 8px;
}

.form-group input {
  width: 100%;
  padding: 10px 16px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
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
  transition: all 0.2s;
}

.btn-cancel {
  background: white;
  border: 1px solid #e0e0e0;
  color: #333;
}

.btn-submit {
  background: #666;
  color: white;
  border: none;
}

.btn-submit:hover {
  background: #555;
}

@media (max-width: 768px) {
  .info-grid {
    grid-template-columns: 1fr;
  }
  
  .page-header h1 {
    font-size: 24px;
  }
  
  .page-header p {
    font-size: 14px;
  }
  
  .card-header {
    padding: 20px;
  }
  
  .card-header h2 {
    font-size: 18px;
  }
  
  .card-body {
    padding: 20px;
  }
  
  .info-item label {
    font-size: 13px;
  }
  
  .info-item span {
    font-size: 14px;
  }
  
  .security-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .action-btn {
    width: 100%;
    text-align: center;
  }
  
  .modal-content {
    width: 95%;
  }
}

@media (max-width: 480px) {
  .page-header h1 {
    font-size: 22px;
  }
  
  .card-header {
    padding: 16px;
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .card-body {
    padding: 16px;
  }
  
  .edit-actions {
    width: 100%;
  }
  
  .save-btn,
  .cancel-btn,
  .edit-btn {
    flex: 1;
    text-align: center;
  }
  
  .info-item input {
    padding: 8px 12px;
    font-size: 13px;
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
