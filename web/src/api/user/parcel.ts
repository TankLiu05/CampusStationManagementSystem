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
  location?: string // 存放位置
  pickupCode?: string // 取件码
  status: number // 0: 待发货, 1: 已发货, 2: 已入库, 3: 退回/异常
  isSigned: number // 0: 未签收, 1: 已签收
  isReturned?: number // 0: 未退货, 1: 已退货
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

/**
 * 查询已签收的快递列表
 * @param page 页码（从0开始）
 * @param size 每页大小
 */
export function listSignedParcels(page: number = 0, size: number = 10) {
  return request<PageResponse<Parcel>>('/api/user/parcel/signed', {
    method: 'GET',
    params: { page, size },
  })
}

/**
 * 查询未签收的快递列表
 * @param page 页码（从0开始）
 * @param size 每页大小
 */
export function listUnsignedParcels(page: number = 0, size: number = 10) {
  return request<PageResponse<Parcel>>('/api/user/parcel/unsigned', {
    method: 'GET',
    params: { page, size },
  })
}

/**
 * 查询快递流转记录（物流轨迹）
 * @param trackingNumber 快递单号
 */
export function getParcelRoutes(trackingNumber: string) {
  return request<ParcelRoute[]>(`/api/user/parcel/tracking/${trackingNumber}/routes`, {
    method: 'GET',
  })
}

/**
 * 快递流转记录数据结构
 */
export interface ParcelRoute {
  id: number
  trackingNumber: string
  currentStation: string
  nextStation?: string
  etaNextStation?: string
  etaDelivered?: string
  createTime: string
  updateTime: string
}
