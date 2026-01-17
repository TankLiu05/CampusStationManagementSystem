/**
 * 管理员公告管理 API
 */
import { request } from '@/utls/request'

/**
 * 公告信息接口
 */
export interface Notice {
  id?: number
  title: string
  content: string
  creatorId?: number
  creatorName?: string
  createTime?: string
  updateTime?: string
}

/**
 * 分页响应接口
 */
export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
  first: boolean
  last: boolean
  empty: boolean
}

/**
 * 创建公告
 * @param notice 公告信息
 */
export function createNotice(notice: Notice) {
  return request<Notice>('/api/admin/notice', {
    method: 'POST',
    body: JSON.stringify(notice),
  })
}

/**
 * 更新公告
 * @param id 公告ID
 * @param notice 公告信息
 */
export function updateNotice(id: number, notice: Notice) {
  return request<Notice>(`/api/admin/notice/${id}`, {
    method: 'PUT',
    body: JSON.stringify(notice),
  })
}

/**
 * 删除公告
 * @param id 公告ID
 */
export function deleteNotice(id: number) {
  return request<void>(`/api/admin/notice/${id}`, {
    method: 'DELETE',
  })
}

/**
 * 分页查询公告列表
 * @param page 页码（从0开始）
 * @param size 每页大小
 */
export function listNotices(page: number = 0, size: number = 10) {
  return request<PageResponse<Notice>>('/api/admin/notice', {
    method: 'GET',
    params: { page, size },
  })
}

/**
 * 查询公告详情
 * @param id 公告ID
 */
export function getNoticeById(id: number) {
  return request<Notice>(`/api/admin/notice/${id}`, {
    method: 'GET',
  })
}
