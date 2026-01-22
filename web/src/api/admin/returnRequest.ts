/**
 * 管理员退货申请管理相关接口
 */
import { request } from '@/utls/request'

/**
 * 退货申请信息接口
 */
export interface ReturnRequest {
  id: number
  username: string
  phone: string
  trackingNumber: string
  reason: string
  status: number // 0: 待审核, 1: 已同意, 2: 已拒绝
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
 * 分页查询退货申请列表
 * @param page 页码（从 0 开始）
 * @param size 每页数量
 * @param status 状态过滤（可选）：0=待审核，1=已同意，2=已拒绝
 */
export function getReturnRequestList(page: number = 0, size: number = 10, status?: number) {
  return request<PageResponse<ReturnRequest>>('/api/admin/return-request', {
    params: { page, size, ...(status !== undefined && { status }) },
  })
}

/**
 * 审核退货申请（同意/拒绝）
 * @param id 退货申请 ID
 * @param status 状态：1=已同意，2=已拒绝
 */
export function updateReturnRequestStatus(id: number, status: 1 | 2) {
  return request<ReturnRequest>(`/api/admin/return-request/${id}/status`, {
    method: 'PUT',
    params: { status },
  })
}
