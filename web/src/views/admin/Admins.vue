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
                <option v-if="currentUserRole === 'SUPERADMIN'" value="MANAGER">省级管理员</option>
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
              <select v-if="currentUserRole === 'SUPERADMIN'" v-model="adminForm.province" @change="onProvinceChange">
                <option value="">请选择省份</option>
                <option v-for="province in provinces" :key="province" :value="province">
                  {{ province }}
                </option>
              </select>
              <input v-else type="text" v-model="adminForm.province" disabled class="disabled-input">
            </div>
            <div class="form-group">
              <label>城市</label>
              <select v-model="adminForm.city" :disabled="!adminForm.province">
                <option value="">请选择城市</option>
                <option v-for="city in availableCities" :key="city" :value="city">
                  {{ city }}
                </option>
              </select>
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
import { useRouter } from 'vue-router'
import AdminLayout from '@/layouts/AdminLayout.vue'
import {
  getAdminList,
  getAdminDetail,
  createAdmin,
  updateAdmin,
  deleteAdmin,
  getCurrentAdminDetail,
  roleNameMap,
  type AdminRoleScope,
  type AdminDetail,
  type AdminRole
} from '@/api/admin/management'
import { useToast } from '@/composables/useToast'
import { useConfirm } from '@/composables/useConfirm'

const router = useRouter()
const { success, error: showError, warning } = useToast()
const { confirm } = useConfirm()

// 权限检查：需要市级管理员及以上权限
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
    currentUserRole.value = detail.role
    currentUserProvince.value = detail.province || ''
    const currentLevel = roleLevel[detail.role]
    const requiredLevel = roleLevel[REQUIRED_ROLE]
    if (currentLevel > requiredLevel) {
      warning(`权限不足：「管理员管理」需要${roleDisplayName[REQUIRED_ROLE]}及以上权限，您当前是${roleDisplayName[detail.role]}`)
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
const currentUserRole = ref<AdminRole | null>(null)
const currentUserProvince = ref<string>('')
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

// 中国省市数据
const provinceCityMap: Record<string, string[]> = {
  '北京': ['北京市'],
  '上海': ['上海市'],
  '天津': ['天津市'],
  '重庆': ['重庆市'],
  '河北': ['石家庄', '唐山', '秦皇岛', '邯郸', '邢台', '保定', '张家口', '承德', '沧州', '廊坊', '衡水'],
  '河南': ['郑州', '开封', '洛阳', '平顶山', '安阳', '鹤壁', '新乡', '焦作', '濮阳', '许昌', '漯河', '三门峡', '南阳', '商丘', '信阳', '周口', '驻马店'],
  '山西': ['太原', '大同', '阳泉', '长治', '晋城', '朔州', '晋中', '运城', '忻州', '临汾', '吕梁'],
  '内蒙古': ['呼和浩特', '包头', '乌海', '赤峰', '通辽', '鄂尔多斯', '呼伦贝尔', '巴彦淖尔', '乌兰察布', '兴安盟', '阿拉善盟', '锡林郭勒盟'],
  '辽宁': ['沈阳', '大连', '鞍山', '抚顺', '本溪', '丹东', '锦州', '营口', '阜新', '辽阳', '盘锦', '铁岭', '朝阳', '葫芦岛'],
  '吉林': ['长春', '吉林', '四平', '辽源', '通化', '白山', '松原', '白城', '延边'],
  '黑龙江': ['哈尔滨', '齐齐哈尔', '鸡西', '鹤岗', '双鸭山', '大庆', '伊春', '佳木斯', '七台河', '牡丹江', '黑河', '绥化', '大兴安岭'],
  '江苏': ['南京', '无锡', '徐州', '常州', '苏州', '南通', '连云港', '淮安', '盐城', '扬州', '镇江', '泰州', '宿迁'],
  '浙江': ['杭州', '宁波', '温州', '嘉兴', '湖州', '绍兴', '金华', '衢州', '舟山', '台州', '丽水'],
  '安徽': ['合肥', '芜湖', '蚌埠', '淮南', '马鞍山', '淮北', '铜陵', '安庆', '黄山', '阜阳', '宿州', '滁州', '六安', '亳州', '池州', '宣城'],
  '福建': ['福州', '厦门', '莆田', '三明', '泉州', '漳州', '南平', '龙岩', '宁德'],
  '江西': ['南昌', '景德镇', '萍乡', '九江', '新余', '鹰潭', '赣州', '吉安', '宜春', '抚州', '上饶'],
  '山东': ['济南', '青岛', '淄博', '枣庄', '东营', '烟台', '潍坊', '济宁', '泰安', '威海', '日照', '临沂', '德州', '聊城', '滨州', '菏泽'],
  '湖北': ['武汉', '黄石', '十堰', '宜昌', '襄阳', '鄂州', '荆门', '孝感', '荆州', '黄冈', '咸宁', '随州', '恩施', '仙桃', '潜江', '天门'],
  '湖南': ['长沙', '株洲', '湘潭', '衡阳', '邵阳', '岳阳', '常德', '张家界', '益阳', '郴州', '永州', '怀化', '娄底', '湘西'],
  '广东': ['广州', '韶关', '深圳', '珠海', '汕头', '佛山', '江门', '湛江', '茂名', '肇庆', '惠州', '梅州', '汕尾', '河源', '阳江', '清远', '东莞', '中山', '潮州', '揭阳', '云浮'],
  '广西': ['南宁', '柳州', '桂林', '梧州', '北海', '防城港', '钦州', '贵港', '玉林', '百色', '贺州', '河池', '来宾', '崇左'],
  '海南': ['海口', '三亚', '三沙', '儋州', '五指山', '文昌', '琼海', '万宁', '东方', '定安', '屯昌', '澄迈', '临高', '白沙', '昌江', '乐东', '陵水', '保亭', '琼中'],
  '四川': ['成都', '自贡', '攀枝花', '泸州', '德阳', '绵阳', '广元', '遂宁', '内江', '乐山', '南充', '眉山', '宜宾', '广安', '达州', '雅安', '巴中', '资阳', '阿坝', '甘孜', '凉山'],
  '贵州': ['贵阳', '六盘水', '遵义', '安顺', '黔南', '铜仁', '黔西南', '毕节', '黔东南'],
  '云南': ['昆明', '曲靖', '玉溪', '保山', '昭通', '丽江', '普洱', '临沧', '楚雄', '红河', '文山', '西双版纳', '大理', '德宏', '怒江', '迪庆'],
  '西藏': ['拉萨', '日喀则', '昌都', '林芝', '山南', '那曲', '阿里'],
  '陕西': ['西安', '铜川', '宝鸡', '咸阳', '渭南', '延安', '汉中', '榆林', '安康', '商洛'],
  '甘肃': ['兰州', '嘉峪关', '金昌', '白银', '天水', '武威', '张掖', '平凉', '酒泉', '庆阳', '定西', '陇南', '甘南', '临夏'],
  '青海': ['西宁', '海东', '海北', '黄南', '海南', '果洛', '玉树', '海西'],
  '宁夏': ['银川', '石嘴山', '吴忠', '固原', '中卫'],
  '新疆': ['乌鲁木齐', '克拉玛依', '吐鲁番', '哈密', '昌吉', '博尔塔拉', '巴音郭楞', '阿克苏', '克孜勒苏', '喀什', '和田', '伊犁', '塔城', '阿勒泰', '石河子'],
  '台湾': ['台北', '高雄', '台中', '台南', '基隆', '新竹', '嘉义'],
  '香港': ['香港岛', '九龙', '新界'],
  '澳门': ['澳门半岛', '离岛']
}

// 省份列表
const provinces = Object.keys(provinceCityMap)

// 当前选中省份的城市列表
const availableCities = computed(() => {
  if (!adminForm.province) {
    return []
  }
  return provinceCityMap[adminForm.province] || []
})

// 省份变化时清空城市
const onProvinceChange = () => {
  adminForm.city = ''
}

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
  
  // 如果是省级管理员，自动填入省份
  if (currentUserRole.value === 'MANAGER' && currentUserProvince.value) {
    // 尝试匹配前端定义的省份名称（处理"河北"与"河北省"的不一致）
    const userProv = currentUserProvince.value
    // 1. 直接匹配
    if (provinceCityMap[userProv]) {
      adminForm.province = userProv
    } else {
      // 2. 前缀匹配 (例如后端存"河北省", 前端map是"河北")
      const matchedProv = Object.keys(provinceCityMap).find(p => userProv.startsWith(p))
      if (matchedProv) {
        adminForm.province = matchedProv
      } else {
        // 3. 兜底
        adminForm.province = userProv
      }
    }
  }
  
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
        phone: adminForm.phone,
        email: adminForm.email,
        province: adminForm.province,
        city: adminForm.city,
        station: adminForm.station
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
onMounted(async () => {
  const hasPermission = await checkPermission()
  if (hasPermission) {
    loadAdmins()
  }
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

.disabled-input {
  background-color: #f5f5f5;
  color: #666;
  cursor: not-allowed;
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
