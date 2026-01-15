// 前端接口封装：sysUser

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

// 默认通过 Vite 代理走相对路径，生产环境可设置 VITE_API_BASE_URL
const API_BASE = (import.meta as any).env?.VITE_API_BASE_URL ?? ''
const prefix = `${API_BASE}/api/sysUser`

async function request<T>(url: string, init?: RequestInit): Promise<T> {
  const resp = await fetch(url, {
    headers: { 'Content-Type': 'application/json' },
    ...init,
  })
  if (!resp.ok) {
    const text = await resp.text()
    throw new Error(text || `HTTP ${resp.status}`)
  }
  // 某些删除接口无正文
  const contentType = resp.headers.get('content-type') || ''
  if (!contentType.includes('application/json')) {
    return undefined as unknown as T
  }
  return resp.json() as Promise<T>
}

// 创建普通用户（后端会强制 role=USER）
export function createUser(body: SysUserCreate): Promise<SysUser> {
  return request<SysUser>(`${prefix}`, {
    method: 'POST',
    body: JSON.stringify(body),
  })
}

// 创建管理员用户（后端会强制 role=ADMIN）
export function createAdmin(body: SysUserCreate): Promise<SysUser> {
  return request<SysUser>(`${prefix}/admin`, {
    method: 'POST',
    body: JSON.stringify(body),
  })
}

export function getUser(id: number): Promise<SysUser> {
  return request<SysUser>(`${prefix}/${id}`)
}

export function updateUser(id: number, body: Partial<SysUser>): Promise<SysUser> {
  return request<SysUser>(`${prefix}/${id}`, {
    method: 'PUT',
    body: JSON.stringify(body),
  })
}

export function deleteUser(id: number): Promise<void> {
  return request<void>(`${prefix}/${id}`, { method: 'DELETE' })
}

export function listUsers(page = 0, size = 10): Promise<PageResult<SysUser>> {
  const url = new URL(`${prefix}`)
  url.searchParams.set('page', String(page))
  url.searchParams.set('size', String(size))
  return request<PageResult<SysUser>>(url.toString())
}

export function changeStatus(id: number, status: 0 | 1): Promise<SysUser> {
  const url = new URL(`${prefix}/${id}/status`)
  url.searchParams.set('status', String(status))
  return request<SysUser>(url.toString(), { method: 'PATCH' })
}

// 手机号登录
export function loginByPhone(phone: string, password: string): Promise<SysUser> {
  return request<SysUser>(`${prefix}/loginByPhone`, {
    method: 'POST',
    body: JSON.stringify({ phone, password }),
  })
}