/**
 * 用户公告查看 API
 */
import { request } from '@/utls/request'

/**
 * 公告信息接口
 */
export interface Notice {
  id: number
  title: string
  content: string
  creatorId?: number
  creatorName?: string
  createTime: string
  updateTime: string
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
 * 查询公告列表
 * @param page 页码（从0开始）
 * @param size 每页大小
 */
export function listNotices(page: number = 0, size: number = 10) {
  return request<PageResponse<Notice>>('/api/user/notice', {
    method: 'GET',
    params: { page, size },
  })
}

/**
 * 查询公告详情
 * @param id 公告ID
 */
export function getNoticeById(id: number) {
  return request<Notice>(`/api/user/notice/${id}`, {
    method: 'GET',
  })
}
