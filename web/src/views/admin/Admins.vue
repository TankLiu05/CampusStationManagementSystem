<template>
  <AdminLayout>
    <div class="admins-management">
      <div class="page-header">
        <h1>管理员管理</h1>
        <p>管理系统管理员账号</p>
      </div>

      <!-- 操作栏 -->
      <div class="action-bar">
        <div class="search-section">
          <input
            type="text"
            v-model="searchKeyword"
            placeholder="搜索管理员用户名..."
            class="search-input"
          >
          <button class="search-btn" @click="searchAdmins">搜索</button>
        </div>
        <div class="action-buttons">
          <button class="btn-primary" @click="openCreateModal">新增管理员</button>
        </div>
      </div>

      <!-- 管理员列表 -->
      <div class="admins-table">
        <table>
          <thead>
            <tr>
              <th>管理员ID</th>
              <th>用户名</th>
              <th>角色</th>
              <th>手机号</th>
              <th>邮箱</th>
              <th>管辖范围</th>
              <th>状态</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="adminList.length === 0">
              <td colspan="8" class="empty-row">暂无管理员数据</td>
            </tr>
            <tr v-for="admin in filteredAdminList" :key="admin.adminId">
              <td>{{ admin.adminId }}</td>
              <td>{{ adminDetails[admin.adminId]?.username || '-' }}</td>
              <td>
                <span :class="['role-badge', admin.role]">
                  {{ roleNameMap[admin.role] }}
                </span>
              </td>
              <td>{{ adminDetails[admin.adminId]?.phone || '-' }}</td>
              <td>{{ adminDetails[admin.adminId]?.email || '-' }}</td>
              <td>{{ getScopeText(admin) }}</td>
              <td>
                <span :class="['status-badge', adminDetails[admin.adminId]?.status === 1 ? 'ACTIVE' : 'DISABLED']">
                  {{ adminDetails[admin.adminId]?.status === 1 ? '正常' : '禁用' }}
                </span>
              </td>
              <td>
                <div class="action-btns">
                  <button class="btn-edit" @click="openEditModal(admin)">编辑</button>
                  <button class="btn-delete" @click="handleDeleteAdmin(admin.adminId)">删除</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 新增/编辑管理员弹窗 -->
      <div class="modal" v-if="showModal" @click="closeModal">
        <div class="modal-content" @click.stop>
          <div class="modal-header">
            <h3>{{ isEdit ? '编辑管理员' : '新增管理员' }}</h3>
            <button class="close-btn" @click="closeModal">×</button>
          </div>
          <div class="modal-body">
            <div class="form-group" v-if="!isEdit">
              <label>用户名 *</label>
              <input type="text" v-model="adminForm.username" placeholder="请输入用户名">
            </div>
            <div class="form-group" v-if="!isEdit">
              <label>密码 *</label>
              <input type="password" v-model="adminForm.password" placeholder="请输入密码（至少6位）">
            </div>
            <div class="form-group" v-if="!isEdit">
              <label>角色 *</label>
              <select v-model="adminForm.role">
                <option value="">请选择角色</option>
                <option value="MANAGER">省级管理员</option>
                <option value="CITY_ADMIN">市级管理员</option>
                <option value="STREET_ADMIN">站点管理员</option>
              </select>
            </div>
            <div class="form-group">
              <label>手机号</label>
              <input type="text" v-model="adminForm.phone" placeholder="请输入手机号">
            </div>
            <div class="form-group">
              <label>邮箱</label>
              <input type="email" v-model="adminForm.email" placeholder="请输入邮箱">
            </div>
            <div class="form-group">
              <label>省份</label>
              <input type="text" v-model="adminForm.province" placeholder="请输入省份">
            </div>
            <div class="form-group">
              <label>城市</label>
              <input type="text" v-model="adminForm.city" placeholder="请输入城市">
            </div>
            <div class="form-group">
              <label>站点</label>
              <input type="text" v-model="adminForm.station" placeholder="请输入站点详细信息">
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn-cancel" @click="closeModal">取消</button>
            <button class="btn-submit" @click="submitAdmin">确定</button>
          </div>
        </div>
      </div>
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import AdminLayout from '@/layouts/AdminLayout.vue'
import {
  getAdminList,
  getAdminDetail,
  createAdmin,
  updateAdmin,
  deleteAdmin,
  roleNameMap,
  type AdminRoleScope,
  type AdminDetail,
  type AdminRole
} from '@/api/admin/management'
import { useToast } from '@/composables/useToast'
import { useConfirm } from '@/composables/useConfirm'

const { success, error: showError, warning } = useToast()
const { confirm } = useConfirm()

interface AdminForm {
  username: string
  password: string
  role: AdminRole | ''
  phone: string
  email: string
  province: string
  city: string
  station: string
}

const searchKeyword = ref('')
const adminList = ref<AdminRoleScope[]>([])
const adminDetails = ref<Record<number, AdminDetail>>({})
const showModal = ref(false)
const isEdit = ref(false)
const editingAdminId = ref<number | null>(null)

const adminForm = reactive<AdminForm>({
  username: '',
  password: '',
  role: '',
  phone: '',
  email: '',
  province: '',
  city: '',
  station: ''
})

// 过滤管理员列表
const filteredAdminList = computed(() => {
  if (!searchKeyword.value.trim()) {
    return adminList.value
  }
  const keyword = searchKeyword.value.trim().toLowerCase()
  return adminList.value.filter(admin => {
    const detail = adminDetails.value[admin.adminId]
    return detail?.username?.toLowerCase().includes(keyword)
  })
})

// 获取管辖范围文本
const getScopeText = (admin: AdminRoleScope): string => {
  const { role, province, city, station } = admin

  if (role === 'SUPERADMIN') {
    return '全部'
  }

  if (role === 'MANAGER') {
    return province || '未设置'
  }

  if (role === 'CITY_ADMIN') {
    const parts: string[] = []
    if (province) parts.push(province)
    if (city) parts.push(city)
    return parts.length > 0 ? parts.join(' - ') : '未设置'
  }

  if (role === 'STREET_ADMIN') {
    const parts: string[] = []
    if (province) parts.push(province)
    if (city) parts.push(city)
    if (station) parts.push(station)
    return parts.length > 0 ? parts.join(' - ') : '未设置'
  }

  return '未设置'
}

// 加载管理员列表
const loadAdmins = async () => {
  try {
    const list = await getAdminList()
    adminList.value = list

    // 获取每个管理员的详细信息
    for (const admin of list) {
      try {
        const detail = await getAdminDetail(admin.adminId)
        adminDetails.value[admin.adminId] = detail
      } catch (error) {
        console.error(`获取管理员${admin.adminId}详情失败:`, error)
      }
    }
  } catch (error) {
    console.error('获取管理员列表失败:', error)
    showError('获取管理员列表失败，请稍后重试')
  }
}

// 搜索管理员
const searchAdmins = () => {
  // 使用计算属性自动过滤，这里只是触发UI更新
}

// 打开新增弹窗
const openCreateModal = () => {
  isEdit.value = false
  editingAdminId.value = null
  resetForm()
  showModal.value = true
}

// 打开编辑弹窗
const openEditModal = (admin: AdminRoleScope) => {
  isEdit.value = true
  editingAdminId.value = admin.adminId
  const detail = adminDetails.value[admin.adminId]

  adminForm.username = detail?.username || ''
  adminForm.password = ''
  adminForm.role = admin.role
  adminForm.phone = detail?.phone || ''
  adminForm.email = detail?.email || ''
  adminForm.province = admin.province || ''
  adminForm.city = admin.city || ''
  adminForm.station = admin.station || ''
  showModal.value = true
}

// 重置表单
const resetForm = () => {
  adminForm.username = ''
  adminForm.password = ''
  adminForm.role = ''
  adminForm.phone = ''
  adminForm.email = ''
  adminForm.province = ''
  adminForm.city = ''
  adminForm.station = ''
}

// 关闭弹窗
const closeModal = () => {
  showModal.value = false
  resetForm()
}

// 提交表单
const submitAdmin = async () => {
  if (isEdit.value) {
    // 编辑管理员
    if (!editingAdminId.value) return

    try {
      await updateAdmin(editingAdminId.value, {
        phone: adminForm.phone || undefined,
        email: adminForm.email || undefined,
        province: adminForm.province || undefined,
        city: adminForm.city || undefined,
        station: adminForm.station || undefined
      })
      success('更新成功')
      closeModal()
      loadAdmins()
    } catch (error) {
      console.error('更新管理员失败:', error)
      showError('更新管理员失败，请稍后重试')
    }
  } else {
    // 新增管理员
    if (!adminForm.username || !adminForm.password || !adminForm.role) {
      warning('请填写用户名、密码和角色')
      return
    }

    if (adminForm.password.length < 6) {
      warning('密码长度至少为6位')
      return
    }

    if (adminForm.role === 'MANAGER') {
      if (!adminForm.province.trim()) {
        warning('省级管理员必须填写省份')
        return
      }
    }

    if (adminForm.role === 'CITY_ADMIN') {
      if (!adminForm.province.trim() || !adminForm.city.trim()) {
        warning('市级管理员必须填写省份和城市')
        return
      }
    }

    if (adminForm.role === 'STREET_ADMIN') {
      if (!adminForm.province.trim() || !adminForm.city.trim() || !adminForm.station.trim()) {
        warning('站点管理员必须填写省份、城市和站点信息')
        return
      }
    }

    try {
      await createAdmin({
        username: adminForm.username,
        password: adminForm.password,
        role: adminForm.role as AdminRole,
        phone: adminForm.phone || undefined,
        email: adminForm.email || undefined,
        province: adminForm.province || undefined,
        city: adminForm.city || undefined,
        station: adminForm.station || undefined
      })
      success('创建成功')
      closeModal()
      loadAdmins()
    } catch (error) {
      console.error('创建管理员失败:', error)
      showError('创建管理员失败，请稍后重试')
    }
  }
}

// 删除管理员
const handleDeleteAdmin = async (adminId: number) => {
  const confirmed = await confirm({
    title: '删除管理员',
    message: '确定要删除该管理员吗？',
    type: 'danger'
  })
  if (!confirmed) return

  try {
    await deleteAdmin(adminId)
    success('删除成功')
    loadAdmins()
  } catch (error) {
    console.error('删除管理员失败:', error)
    showError('删除管理员失败，请稍后重试')
  }
}

// 初始化加载
onMounted(() => {
  loadAdmins()
})
</script>

<style scoped>
.admins-management {
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

.btn-primary {
  padding: 10px 20px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
  background: #808080;
  color: white;
  border: none;
}

.btn-primary:hover {
  background: #666666;
}

.admins-table {
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

.role-badge.SUPERADMIN {
  background: #fff1f0;
  color: #f5222d;
}

.role-badge.MANAGER {
  background: #fff7e6;
  color: #fa8c16;
}

.role-badge.CITY_ADMIN {
  background: #e6f7ff;
  color: #1890ff;
}

.role-badge.STREET_ADMIN {
  background: #f6ffed;
  color: #52c41a;
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

.btn-edit,
.btn-delete {
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.2s;
}

.btn-edit {
  background: white;
  color: #1890ff;
  border: 1px solid #1890ff;
}

.btn-edit:hover {
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
  box-sizing: border-box;
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
