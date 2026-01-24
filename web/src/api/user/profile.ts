/**
 * 用户个人信息相关接口
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
 * 获取当前用户个人信息
 */
export function getUserProfile() {
  return request<UserProfile>('/api/user/profile')
}

/**
 * 更新用户个人信息
 * @param data 更新的字段（username, phone）
 */
export function updateUserProfile(data: UpdateProfileRequest) {
  return request<UserProfile>('/api/user/profile', {
    method: 'PUT',
    body: JSON.stringify(data),
  })
}

/**
 * 修改用户密码
 * @param data 旧密码和新密码
 */
export function changeUserPassword(data: ChangePasswordRequest) {
  return request<string>('/api/user/profile/password', {
    method: 'POST',
    body: JSON.stringify(data),
  })
}

/**
 * 升级为超级管理员
 * @param adminPassword 超级管理员密码
 */
export function upgradeToSuperAdmin(adminPassword: string) {
  return request<string>('/api/user/profile/upgrade-to-admin', {
    method: 'POST',
    body: JSON.stringify({ adminPassword }),
  })
}
