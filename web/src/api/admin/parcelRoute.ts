/**
 * 管理员快递流转记录 API
 */
import { request } from '@/utls/request'

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

/**
 * 创建快递流转记录请求
 */
export interface ParcelRouteCreateRequest {
  trackingNumber: string
  currentStation: string
  nextStation: string
  etaNextStation: string
  etaDelivered: string
}

/**
 * 更新预计送达时间请求
 */
export interface ParcelRouteUpdateEtaRequest {
  etaDelivered: string
}

/**
 * 分页响应数据结构
 */
export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
  first: boolean
  last: boolean
}

/**
 * 快递流转记录 API
 */
export const parcelRouteApi = {
  /**
   * 创建快递流转记录
   * @param data 流转记录数据
   */
  create(data: ParcelRouteCreateRequest) {
    return request<ParcelRoute>('/api/admin/parcelRoute', {
      method: 'POST',
      body: JSON.stringify(data)
    })
  },

  /**
   * 修改快递预计送达时间
   * @param id 流转记录ID
   * @param etaDelivered 预计送达时间
   */
  updateEtaDelivered(id: number, etaDelivered: string) {
    return request<ParcelRoute>(`/api/admin/parcelRoute/${id}/etaDelivered`, {
      method: 'PUT',
      body: JSON.stringify({ etaDelivered })
    })
  },

  /**
   * 根据快递单号查询流转记录
   * @param trackingNumber 快递单号
   */
  getByTrackingNumber(trackingNumber: string) {
    return request<ParcelRoute[]>(`/api/admin/parcelRoute/tracking/${trackingNumber}`, {
      method: 'GET'
    })
  }
}
