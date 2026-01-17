/**
 * 管理员个人信息相关接口
 */
import { request } from '@/utls/request'

/**
 * 用户信息接口（基础字段）
 */
export interface UserProfile {
  id: number
  username: string
  password?: string
  phone?: string
  email?: string
  avatar?: string
  role: string
  status: number
  createTime: string
  updateTime: string
}

/**
 * 更新个人信息请求参数
 */
export interface UpdateProfileRequest {
  username?: string
  phone?: string
}

/**
 * 修改密码请求参数
 */
export interface ChangePasswordRequest {
  oldPassword: string
  newPassword: string
}

/**
 * 获取当前管理员个人信息
 */
export function getAdminProfile() {
  return request<UserProfile>('/api/admin/profile')
}

/**
 * 更新管理员个人信息
 * @param data 更新的字段（username, phone）
 */
export function updateAdminProfile(data: UpdateProfileRequest) {
  return request<UserProfile>('/api/admin/profile', {
    method: 'PUT',
    body: JSON.stringify(data),
  })
}

/**
 * 修改管理员密码
 * @param data 旧密码和新密码
 */
export function changeAdminPassword(data: ChangePasswordRequest) {
  return request<string>('/api/admin/profile/password', {
    method: 'POST',
    body: JSON.stringify(data),
  })
}
