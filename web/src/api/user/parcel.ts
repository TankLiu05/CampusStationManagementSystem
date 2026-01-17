/**
 * 用户快递管理 API
 */
import { request } from '@/utls/request'

/**
 * 快递信息接口
 */
export interface Parcel {
  id: number
  trackingNumber: string // 快递单号
  company: string // 快递公司
  receiverId?: number
  receiverName?: string
  receiverPhone?: string
  status: number // 0: 待发货, 1: 已发货, 2: 已入库, 3: 退回/异常
  isSigned: number // 0: 未签收, 1: 已签收
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
 * 查询我的快递列表
 * @param page 页码（从0开始）
 * @param size 每页大小
 */
export function listMyParcels(page: number = 0, size: number = 10) {
  return request<PageResponse<Parcel>>('/api/user/parcel', {
    method: 'GET',
    params: { page, size },
  })
}

/**
 * 根据快递单号查询我的快递
 * @param trackingNumber 快递单号
 */
export function searchMyParcelByTrackingNumber(trackingNumber: string) {
  return request<Parcel>('/api/user/parcel/search', {
    method: 'GET',
    params: { trackingNumber },
  })
}

/**
 * 查询快递详情
 * @param id 快递ID
 */
export function getParcelById(id: number) {
  return request<Parcel>(`/api/user/parcel/${id}`, {
    method: 'GET',
  })
}

/**
 * 签收快递
 * @param id 快递ID
 */
export function signParcel(id: number) {
  return request<Parcel>(`/api/user/parcel/${id}/sign`, {
    method: 'POST',
  })
}
