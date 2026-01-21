import { request } from '@/utls/request'

/**
 * 仓库库存信息
 */
export interface StationStorage {
  id?: number
  trackingNumber: string
  receiverName?: string
  receiverPhone?: string
  pickupCode?: string
  isSigned?: number // 0: 未签收, 1: 已签收
  area?: string // A-D区
  shelf?: string // 1-10号货架
  position?: string // 四位数位置
  createTime?: string
  updateTime?: string
}

/**
 * 仓库搜索参数
 */
export interface StationStorageSearchParams {
  area?: string
  shelf?: string
  position?: string
  receiverName?: string
  receiverPhone?: string
}

/**
 * 根据快递单号查询仓库信息
 * @param trackingNumber 快递单号
 */
export function getStorageByTrackingNumber(trackingNumber: string) {
  return request<StationStorage>(`/api/admin/station-storage/tracking/${trackingNumber}`)
}

/**
 * 多条件查询仓库库存
 * @param params 搜索参数
 */
export function searchStorage(params?: StationStorageSearchParams) {
  return request<StationStorage[]>('/api/admin/station-storage/search', {
    params,
  })
}
