// 前端接口封装：sysUser
import { request } from '@/utls/request'

export interface SysUser {
  id: number
  username: string
  password: string
  phone?: string | null
  email?: string | null
  avatar?: string | null
  role: 'USER' | 'ADMIN' | string
  status: number
  createTime?: string
  updateTime?: string
}

export interface SysUserCreate {
  username?: string
  password: string
  phone?: string
  email?: string
  avatar?: string
}

export interface PageResult<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

const prefix = '/api/sysUser'

// 用户登录
export function login(username: string, password: string): Promise<SysUser> {
  return request<SysUser>(`${prefix}/login`, {
    method: 'POST',
    body: JSON.stringify({ username, password }),
  })
}

// 普通用户注册
export function register(body: SysUserCreate): Promise<SysUser> {
  return request<SysUser>(prefix, {
    method: 'POST',
    body: JSON.stringify(body),
  })
}

// 获取当前登录用户信息
export function getCurrentUser(): Promise<SysUser> {
  return request<SysUser>(`${prefix}/current`)
}

// 用户登出
export function logout(): Promise<void> {
  return request<void>(`${prefix}/logout`, { method: 'POST' })
}