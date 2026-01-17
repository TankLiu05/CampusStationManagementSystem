<template>
  <AdminLayout>
    <div class="users-management">
    <div class="page-header">
      <h1>用户管理</h1>
      <p>管理系统用户信息</p>
    </div>

    <!-- 操作栏 -->
    <div class="action-bar">
      <div class="search-section">
        <input 
          type="text" 
          v-model="searchKeyword" 
          placeholder="搜索用户名、手机号、学号..."
          class="search-input"
        >
        <button class="search-btn" @click="searchUsers">搜索</button>
      </div>
    </div>

    <!-- 筛选区域 -->
    <div class="filter-section">
      <select v-model="roleFilter" class="filter-select">
        <option value="">全部角色</option>
        <option value="USER">普通用户</option>
        <option value="ADMIN">管理员</option>
      </select>
    </div>

    <!-- 用户列表 -->
    <div class="users-table">
      <table>
        <thead>
          <tr>
            <th>用户ID</th>
            <th>用户名</th>
            <th>手机号</th>
            <th>邮箱</th>
            <th>角色</th>
            <th>注册时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="userList.length === 0">
            <td colspan="8" class="empty-row">暂无用户数据</td>
          </tr>
          <tr v-for="user in userList" :key="user.id">
            <td>{{ user.id }}</td>
            <td>{{ user.username }}</td>
            <td>{{ user.phone || '-' }}</td>
            <td>{{ user.email || '-' }}</td>
            <td>
              <span :class="['role-badge', user.role]">
                {{ user.role === 'ADMIN' ? '管理员' : '普通用户' }}
              </span>
            </td>
            <td>{{ new Date(user.createTime).toLocaleString('zh-CN') }}</td>
            <td>
              <div class="action-btns">
                <button class="btn-reset" @click="openResetPassword(user.id)">重置密码</button>
                <button class="btn-delete" @click="handleDeleteUser(user.id)">删除</button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>

      <!-- 分页 -->
      <div class="pagination">
        <button class="page-btn" :disabled="currentPage === 1" @click="currentPage--">上一页</button>
        <span class="page-info">第 {{ currentPage }} / {{ totalPages }} 页，共 {{ total }} 条</span>
        <button class="page-btn" :disabled="currentPage === totalPages" @click="currentPage++">下一页</button>
      </div>
    </div>

    <!-- 重置密码弹窗 -->
    <div class="modal" v-if="showResetPassword" @click="closeModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>重置用户密码</h3>
          <button class="close-btn" @click="closeModal">×</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>新密码 *</label>
            <input type="password" v-model="newPassword" placeholder="请输入新密码（至少6位）">
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn-cancel" @click="closeModal">取消</button>
          <button class="btn-submit" @click="submitResetPassword">确定</button>
        </div>
      </div>
    </div>
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import AdminLayout from '@/layouts/AdminLayout.vue'
import { getUserList, deleteUser as deleteUserApi, resetUserPassword, type User } from '@/api/admin/user'

interface UserForm {
  id?: number
  username: string
  password: string
  phone: string
  email: string
  role: string
}

const searchKeyword = ref('')
const roleFilter = ref('')
const statusFilter = ref('')
const currentPage = ref(1)
const totalPages = ref(1)
const total = ref(0)
const pageSize = ref(10)

const userList = ref<User[]>([])
const showResetPassword = ref(false)
const selectedUserId = ref<number | null>(null)

const userForm = reactive<UserForm>({
  username: '',
  password: '',
  phone: '',
  email: '',
  role: 'USER'
})

const newPassword = ref('')

// 加载用户列表
const loadUsers = async () => {
  try {
    const result = await getUserList(currentPage.value - 1, pageSize.value)
    userList.value = result.content
    total.value = result.totalElements
    totalPages.value = result.totalPages
  } catch (error) {
    console.error('获取用户列表失败:', error)
    alert('获取用户列表失败，请稍后重试')
  }
}

// 搜索用户
const searchUsers = () => {
  console.log('搜索用户:', searchKeyword.value)
  // TODO: 后端需要支持搜索功能
  loadUsers()
}



// 删除用户
const handleDeleteUser = async (id: number) => {
  if (!confirm('确定要删除该用户吗？')) {
    return
  }
  
  try {
    await deleteUserApi(id)
    alert('删除成功')
    loadUsers()
  } catch (error) {
    console.error('删除用户失败:', error)
    alert('删除用户失败，请稍后重试')
  }
}

// 重置密码
const openResetPassword = (id: number) => {
  selectedUserId.value = id
  newPassword.value = ''
  showResetPassword.value = true
}

const submitResetPassword = async () => {
  if (!selectedUserId.value) return
  
  if (!newPassword.value || newPassword.value.length < 6) {
    alert('密码长度至少为6位')
    return
  }
  
  try {
    await resetUserPassword(selectedUserId.value, { newPassword: newPassword.value })
    alert('密码重置成功')
    showResetPassword.value = false
    newPassword.value = ''
  } catch (error) {
    console.error('重置密码失败:', error)
    alert('重置密码失败，请稍后重试')
  }
}

// 提交用户信息
const submitUser = () => {
  console.log('提交用户信息:', userForm)
  // TODO: 后端需要实现创建和更新用户接口
  alert('该功能暂未实现，后端需要添加创建/更新用户接口')
  closeModal()
}

// 关闭弹窗
const closeModal = () => {
  showResetPassword.value = false
  Object.assign(userForm, {
    id: undefined,
    username: '',
    password: '',
    phone: '',
    email: '',
    role: 'USER'
  })
}

// 监听页码变化
watch(currentPage, () => {
  loadUsers()
})

// 监听筛选条件变化
watch([roleFilter, statusFilter], () => {
  currentPage.value = 1
  // TODO: 后端需要支持按角色和状态筛选
  loadUsers()
})

// 初始化加载
onMounted(() => {
  loadUsers()
})
</script>

<style scoped>
.users-management {
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

.search-btn {
  padding: 10px 24px;
  background: #808080;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.search-btn:hover {
  background: #666666;
}

.action-buttons {
  display: flex;
  gap: 12px;
}

.btn-primary,
.btn-secondary {
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

.filter-section {
  background: white;
  padding: 16px 20px;
  border-radius: 12px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  display: flex;
  gap: 12px;
}

.filter-select {
  padding: 8px 16px;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
}

.users-table {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  overflow: hidden;
}

table {
  width: 100%;
  border-collapse: collapse;
}

thead {
  background: #f5f7fa;
}

th {
  padding: 16px;
  text-align: left;
  font-size: 14px;
  font-weight: 600;
  color: #666;
  height: 52px;
  box-sizing: border-box;
}

td {
  padding: 16px;
  border-top: 1px solid #f0f0f0;
  font-size: 14px;
  color: #333;
}

.empty-row {
  text-align: center;
  color: #999;
  padding: 60px;
}

.role-badge,
.status-badge {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.role-badge.ADMIN {
  background: #fff1f0;
  color: #f5222d;
}

.role-badge.USER {
  background: #e6f7ff;
  color: #1890ff;
}

.status-badge.ACTIVE {
  background: #f6ffed;
  color: #52c41a;
}

.status-badge.DISABLED {
  background: #f0f0f0;
  color: #999;
}

.action-btns {
  display: flex;
  gap: 8px;
}

.btn-reset,
.btn-delete {
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.2s;
}

.btn-reset {
  background: white;
  color: #1890ff;
  border: 1px solid #1890ff;
}

.btn-reset:hover {
  background: #1890ff;
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
  max-width: 600px;
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
.form-group select {
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
