import { request } from '@/utls/request'

// 管理员角色类型
export type AdminRole = 'SUPERADMIN' | 'MANAGER' | 'CITY_ADMIN' | 'STREET_ADMIN'

// 管理员详情
export interface AdminDetail {
  id: number
  username: string
  phone: string | null
  email: string | null
  status: number
  role: AdminRole
  province: string | null
  city: string | null
  stationId: number | null
}

// 管理员角色范围
export interface AdminRoleScope {
  id: number
  adminId: number
  parentAdminId: number | null
  role: AdminRole
  province: string | null
  city: string | null
  stationId: number | null
}

// 创建管理员参数
export interface CreateAdminParams {
  username: string
  password: string
  phone?: string
  email?: string
  role: AdminRole
  province?: string
  city?: string
  stationId?: number
}

// 更新管理员参数
export interface UpdateAdminParams {
  phone?: string
  email?: string
  province?: string
  city?: string
  stationId?: number
}

// 获取可管理的管理员列表
export function getAdminList(): Promise<AdminRoleScope[]> {
  return request('/api/admin/management', { method: 'GET' })
}

// 获取管理员详情
export function getAdminDetail(adminId: number): Promise<AdminDetail> {
  return request(`/api/admin/management/${adminId}`, { method: 'GET' })
}

// 创建管理员
export function createAdmin(params: CreateAdminParams): Promise<AdminDetail> {
  return request('/api/admin/management', {
    method: 'POST',
    body: JSON.stringify(params),
    headers: { 'Content-Type': 'application/json' }
  })
}

// 更新管理员
export function updateAdmin(adminId: number, params: UpdateAdminParams): Promise<AdminDetail> {
  return request(`/api/admin/management/${adminId}`, {
    method: 'PUT',
    body: JSON.stringify(params),
    headers: { 'Content-Type': 'application/json' }
  })
}

// 删除管理员
export function deleteAdmin(adminId: number): Promise<void> {
  return request(`/api/admin/management/${adminId}`, { method: 'DELETE' })
}

// 角色显示名称映射
export const roleNameMap: Record<AdminRole, string> = {
  SUPERADMIN: '超级管理员',
  MANAGER: '管理员',
  CITY_ADMIN: '城市管理员',
  STREET_ADMIN: '站点管理员'
}
