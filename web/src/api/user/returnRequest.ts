/**
 * 用户退货申请相关接口
 */
import { request } from '@/utls/request'

/**
 * 退货申请信息接口
 */
export interface ReturnRequest {
  id?: number
  username?: string
  phone?: string
  trackingNumber: string
  reason: string
  status?: number // 0: 待审核, 1: 已同意, 2: 已拒绝
  createTime?: string
  updateTime?: string
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
 * 创建退货申请请求参数
 */
export interface CreateReturnRequestRequest {
  trackingNumber: string
  reason: string
  username?: string
  phone?: string
}

/**
 * 提交退货申请
 * @param data 退货申请数据
 */
export function createReturnRequest(data: CreateReturnRequestRequest) {
  return request<ReturnRequest>('/api/user/return-request', {
    method: 'POST',
    body: JSON.stringify(data),
  })
}

/**
 * 查询我的退货申请
 * @param page 页码（从 0 开始）
 * @param size 每页数量
 */
export function getMyReturnRequests(page: number = 0, size: number = 10) {
  return request<PageResponse<ReturnRequest>>('/api/user/return-request', {
    params: { page, size },
  })
}
