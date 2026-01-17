/**
 * 管理员用户管理相关接口
 */
import { request } from '@/utls/request'

/**
 * 用户信息接口
 */
export interface User {
  id: number
  username: string
  phone?: string
  email?: string
  avatar?: string
  role: string
  status: number
  createTime: string
  updateTime: string
}

/**
 * 分页响应
 */
export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

/**
 * 创建用户请求参数
 */
export interface CreateUserRequest {
  username: string
  password: string
  phone?: string
  email?: string
  role: string
}

/**
 * 更新用户请求参数
 */
export interface UpdateUserRequest {
  username?: string
  phone?: string
  email?: string
  role?: string
}

/**
 * 重置密码请求参数
 */
export interface ResetPasswordRequest {
  newPassword: string
}

/**
 * 分页查询用户列表
 * @param page 页码（从 0 开始）
 * @param size 每页数量
 */
export function getUserList(page: number = 0, size: number = 10) {
  return request<PageResponse<User>>('/api/admin/user', {
    params: { page, size },
  })
}

/**
 * 修改用户状态
 * @param id 用户 ID
 * @param status 状态（0=正常，1=禁用）
 */
export function changeUserStatus(id: number, status: number) {
  return request<User>(`/api/admin/user/${id}/status`, {
    method: 'POST',
    params: { status },
  })
}

/**
 * 重置用户密码
 * @param id 用户 ID
 * @param data 新密码
 */
export function resetUserPassword(id: number, data: ResetPasswordRequest) {
  return request<string>(`/api/admin/user/${id}/password`, {
    method: 'POST',
    body: JSON.stringify(data),
  })
}

/**
 * 删除用户
 * @param id 用户 ID
 */
export function deleteUser(id: number) {
  return request<void>(`/api/admin/user/${id}`, {
    method: 'DELETE',
  })
}

/**
 * 根据用户名搜索用户
 * @param username 用户名
 */
export function getUserByUsername(username: string) {
  return request<User>('/api/admin/user/byUsername', {
    params: { username },
  })
}

/**
 * 根据手机号搜索用户
 * @param phone 手机号
 */
export function getUserByPhone(phone: string) {
  return request<User>('/api/admin/user/byPhone', {
    params: { phone },
  })
}
