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

      <!-- 收货地址 -->
      <div class="address-card">
        <div class="card-header">
          <h2>收货地址</h2>
          <button class="add-btn" @click="showAddAddress = true">添加地址</button>
        </div>
        <div class="card-body">
          <!-- 地址列表 -->
          <div v-if="addressList.length > 0" class="address-list">
            <div v-for="addr in addressList" :key="addr.id" class="address-item" :class="{ 'is-default': addr.isDefault }">
              <div class="address-info">
                <div class="address-name-phone">
                  <span class="name">{{ addr.username }}</span>
                  <span class="phone">{{ addr.phone }}</span>
                  <span v-if="addr.isDefault" class="default-tag">默认</span>
                </div>
                <div class="address-detail">
                  {{ [addr.province, addr.city, addr.street, addr.detailAddress].filter(Boolean).join(' ') }}
                </div>
              </div>
              <div class="address-actions">
                <button v-if="!addr.isDefault" class="action-link" @click="handleSetDefault(addr.id)">设为默认</button>
                <button class="action-link" @click="openEditAddress(addr)">编辑</button>
                <button class="action-link delete" @click="handleDeleteAddress(addr.id)">删除</button>
              </div>
            </div>
          </div>
          <!-- 无地址提示 -->
          <div v-else class="address-tip">
            <p>添加收货地址后，管理员可以更方便地为您发送快递</p>
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

    <!-- 添加收货地址弹窗 -->
    <div class="modal" v-if="showAddAddress" @click="showAddAddress = false">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>添加收货地址</h3>
          <button class="close-btn" @click="showAddAddress = false">×</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>收件人姓名</label>
            <input type="text" v-model="addressForm.username" placeholder="默认使用当前用户名">
          </div>
          <div class="form-group">
            <label>手机号</label>
            <input type="tel" v-model="addressForm.phone" placeholder="默认使用当前手机号">
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>省份</label>
              <input type="text" v-model="addressForm.province" placeholder="如：广东省">
            </div>
            <div class="form-group">
              <label>城市</label>
              <input type="text" v-model="addressForm.city" placeholder="如：深圳市">
            </div>
          </div>
          <div class="form-group">
            <label>街道/区域</label>
            <input type="text" v-model="addressForm.street" placeholder="如：南山区">
          </div>
          <div class="form-group">
            <label>详细地址 <span class="required">*</span></label>
            <input type="text" v-model="addressForm.detailAddress" placeholder="如：xx路xx号xx栋xx室">
          </div>
          <div class="form-group checkbox-group">
            <label class="checkbox-label">
              <input type="checkbox" v-model="addressForm.isDefault">
              <span>设为默认地址</span>
            </label>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn-cancel" @click="showAddAddress = false">取消</button>
          <button class="btn-submit" @click="addAddress">确认添加</button>
        </div>
      </div>
    </div>

    <!-- 编辑收货地址弹窗 -->
    <div class="modal" v-if="showEditAddress" @click="showEditAddress = false">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>编辑收货地址</h3>
          <button class="close-btn" @click="showEditAddress = false">×</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>收件人姓名</label>
            <input type="text" v-model="editAddressForm.username" placeholder="收件人姓名">
          </div>
          <div class="form-group">
            <label>手机号</label>
            <input type="tel" v-model="editAddressForm.phone" placeholder="手机号">
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>省份</label>
              <input type="text" v-model="editAddressForm.province" placeholder="如：广东省">
            </div>
            <div class="form-group">
              <label>城市</label>
              <input type="text" v-model="editAddressForm.city" placeholder="如：深圳市">
            </div>
          </div>
          <div class="form-group">
            <label>街道/区域</label>
            <input type="text" v-model="editAddressForm.street" placeholder="如：南山区">
          </div>
          <div class="form-group">
            <label>详细地址 <span class="required">*</span></label>
            <input type="text" v-model="editAddressForm.detailAddress" placeholder="如：xx路xx号xx栋xx室">
          </div>
          <div class="form-group checkbox-group">
            <label class="checkbox-label">
              <input type="checkbox" v-model="editAddressForm.isDefault">
              <span>设为默认地址</span>
            </label>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn-cancel" @click="showEditAddress = false">取消</button>
          <button class="btn-submit" @click="updateAddress">保存修改</button>
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
import { 
  createLocation, 
  getLocationList, 
  updateLocation, 
  deleteLocation, 
  setDefaultLocation,
  type CreateLocationRequest,
  type UpdateLocationRequest,
  type UserLocation 
} from '@/api/user/location'
import { useToast } from '@/composables/useToast'

const { success, error: showError, warning } = useToast()

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
const showAddAddress = ref(false)

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

// 收货地址表单
const addressForm = reactive<CreateLocationRequest>({
  username: '',
  phone: '',
  province: '',
  city: '',
  street: '',
  detailAddress: '',
  isDefault: false
})

// 编辑地址弹窗
const showEditAddress = ref(false)
const editingAddressId = ref<number | null>(null)
const editAddressForm = reactive<UpdateLocationRequest>({
  username: '',
  phone: '',
  province: '',
  city: '',
  street: '',
  detailAddress: '',
  isDefault: false
})

// 已添加的收货地址列表
const addressList = ref<UserLocation[]>([])

// 页面加载时获取用户信息和地址列表
onMounted(async () => {
  await Promise.all([loadProfile(), loadAddressList()])
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
    showError('获取用户信息失败，请稍后重试')
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
    success('保存成功')
  } catch (error) {
    console.error('保存失败:', error)
    showError('保存失败，请稍后重试')
  }
}

const changePassword = async () => {
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    warning('两次输入的密码不一致')
    return
  }
  
  if (!passwordForm.oldPassword || !passwordForm.newPassword) {
    warning('请填写完整的密码信息')
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
    success('密码修改成功')
    
    // 可选：跳转到登录页
    // window.location.href = '/login'
  } catch (error) {
    console.error('修改密码失败:', error)
    showError('修改密码失败，请检查旧密码是否正确')
  }
}

// 加载地址列表
const loadAddressList = async () => {
  try {
    const data = await getLocationList()
    addressList.value = data
  } catch (error) {
    console.error('获取地址列表失败:', error)
  }
}

// 添加收货地址
const addAddress = async () => {
  if (!addressForm.detailAddress || !addressForm.detailAddress.trim()) {
    warning('请填写详细地址')
    return
  }
  
  try {
    await createLocation({
      username: addressForm.username || undefined,
      phone: addressForm.phone || undefined,
      province: addressForm.province || undefined,
      city: addressForm.city || undefined,
      street: addressForm.street || undefined,
      detailAddress: addressForm.detailAddress.trim(),
      isDefault: addressForm.isDefault
    })
    
    // 重新加载地址列表
    await loadAddressList()
    
    // 清空表单
    addressForm.username = ''
    addressForm.phone = ''
    addressForm.province = ''
    addressForm.city = ''
    addressForm.street = ''
    addressForm.detailAddress = ''
    addressForm.isDefault = false
    
    showAddAddress.value = false
    success('收货地址添加成功')
  } catch (error) {
    console.error('添加收货地址失败:', error)
    showError('添加收货地址失败，请稍后重试')
  }
}

// 打开编辑地址弹窗
const openEditAddress = (addr: UserLocation) => {
  editingAddressId.value = addr.id
  editAddressForm.username = addr.username
  editAddressForm.phone = addr.phone
  editAddressForm.province = addr.province
  editAddressForm.city = addr.city
  editAddressForm.street = addr.street
  editAddressForm.detailAddress = addr.detailAddress
  editAddressForm.isDefault = addr.isDefault
  showEditAddress.value = true
}

// 更新地址
const updateAddress = async () => {
  if (!editAddressForm.detailAddress || !editAddressForm.detailAddress.trim()) {
    warning('请填写详细地址')
    return
  }
  
  if (!editingAddressId.value) return
  
  try {
    await updateLocation(editingAddressId.value, {
      username: editAddressForm.username,
      phone: editAddressForm.phone,
      province: editAddressForm.province,
      city: editAddressForm.city,
      street: editAddressForm.street,
      detailAddress: editAddressForm.detailAddress.trim(),
      isDefault: editAddressForm.isDefault
    })
    
    await loadAddressList()
    showEditAddress.value = false
    success('地址修改成功')
  } catch (error) {
    console.error('修改地址失败:', error)
    showError('修改地址失败，请稍后重试')
  }
}

// 删除地址
const handleDeleteAddress = async (id: number) => {
  if (!confirm('确定要删除这个地址吗？')) return
  
  try {
    await deleteLocation(id)
    await loadAddressList()
    success('地址删除成功')
  } catch (error) {
    console.error('删除地址失败:', error)
    showError('删除地址失败，请稍后重试')
  }
}

// 设为默认地址
const handleSetDefault = async (id: number) => {
  try {
    await setDefaultLocation(id)
    await loadAddressList()
    success('已设为默认地址')
  } catch (error) {
    console.error('设置默认地址失败:', error)
    showError('设置默认地址失败，请稍后重试')
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
.security-card,
.address-card {
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
  background: white;
  border: 1px solid #e0e0e0;
  color: #333;
}

.edit-btn:hover,
.save-btn:hover,
.cancel-btn:hover {
  border-color: #10b981;
  color: #10b981;
}

.edit-actions {
  display: flex;
  gap: 12px;
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
  border-color: #10b981;
  color: #10b981;
}

/* 添加地址按钮 */
.add-btn {
  padding: 8px 20px;
  background: white;
  color: #333;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.add-btn:hover {
  border-color: #10b981;
  color: #10b981;
}

/* 地址列表 */
.address-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.address-item {
  padding: 16px;
  background: #f9f9f9;
  border-radius: 8px;
  border: 1px solid #eee;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.address-item.is-default {
  border-color: #10b981;
  background: #f0fdf4;
}

.address-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.address-name-phone {
  display: flex;
  align-items: center;
  gap: 16px;
}

.address-name-phone .name {
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

.address-name-phone .phone {
  font-size: 14px;
  color: #666;
}

.address-detail {
  font-size: 14px;
  color: #666;
  line-height: 1.5;
}

.default-tag {
  display: inline-block;
  padding: 2px 8px;
  background: #10b981;
  color: white;
  font-size: 12px;
  border-radius: 4px;
}

.address-actions {
  display: flex;
  gap: 12px;
  flex-shrink: 0;
}

.action-link {
  background: none;
  border: none;
  color: #10b981;
  font-size: 13px;
  cursor: pointer;
  padding: 0;
}

.action-link:hover {
  text-decoration: underline;
}

.action-link.delete {
  color: #ef4444;
}

.action-link.delete:hover {
  color: #dc2626;
}

/* 复选框样式 */
.checkbox-group {
  margin-top: 8px;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-weight: normal;
}

.checkbox-label input[type="checkbox"] {
  width: 16px;
  height: 16px;
  cursor: pointer;
}

.checkbox-label span {
  font-size: 14px;
  color: #333;
}

/* 地址提示 */
.address-tip {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: #f5f5f5;
  border-radius: 8px;
  color: #666;
}

.address-tip p {
  font-size: 14px;
  margin: 0;
}

/* 表单行 */
.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.form-row .form-group {
  margin-bottom: 20px;
}

.required {
  color: #ef4444;
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
  background: white;
  border: 1px solid #e0e0e0;
  color: #333;
}

.btn-cancel:hover,
.btn-submit:hover {
  border-color: #10b981;
  color: #10b981;
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
