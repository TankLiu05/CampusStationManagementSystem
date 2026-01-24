import { request } from '@/utls/request'

// 管理员角色类型
export type AdminRole = 'SUPERADMIN' | 'MANAGER' | 'STREET_ADMIN'

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
  station: string | null
}

// 管理员角色范围
export interface AdminRoleScope {
  id: number
  adminId: number
  parentAdminId: number | null
  role: AdminRole
  province: string | null
  city: string | null
  station: string | null
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
  station?: string
}

// 更新管理员参数
export interface UpdateAdminParams {
  phone?: string
  email?: string
  province?: string
  city?: string
  station?: string
}

export interface AdminStation {
  id: number
  province: string
  city: string
  station: string
  phone: string | null
  username: string | null
}

// 获取所有站点列表
export function getAllStations(): Promise<AdminStation[]> {
  return request('/api/admin/management/stations/all', { method: 'GET' })
}

// 获取可管理的管理员列表
export function getAdminList(): Promise<AdminRoleScope[]> {
  return request('/api/admin/management', { method: 'GET' })
}

// 获取管理员详情
export function getAdminDetail(adminId: number): Promise<AdminDetail> {
  return request(`/api/admin/management/${adminId}`, { method: 'GET' })
}

export function getCurrentAdminDetail(): Promise<AdminDetail> {
  return request('/api/admin/management/me', { method: 'GET' })
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
  MANAGER: '省级管理员',
  STREET_ADMIN: '站点管理员'
}
