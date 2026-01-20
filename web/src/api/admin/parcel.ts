/**
 * 管理员包裹管理 API
 */
import { request } from '@/utls/request'

/**
 * 包裹状态枚举（后端）
 * 0: 待发货, 1: 已发货, 2: 已入库, 3: 退回/异常
 */
export enum ParcelStatus {
  PENDING_SHIP = 0,
  SHIPPED = 1,
  STORED = 2,
  RETURNED = 3
}

/**
 * 包裹前端状态（用于界面展示）
 */
export type ParcelUIStatus = 'PENDING' | 'PICKED' | 'EXPIRED'

/**
 * 包裹数据结构（后端）
 */
export interface Parcel {
  id: number
  trackingNumber: string
  company: string
  receiverId?: number
  receiverName: string
  receiverPhone: string
  origin?: string
  destination?: string
  location?: string // 存放位置
  pickupCode?: string // 取件码
  status: number // 0: 待发货, 1: 已发货, 2: 已入库, 3: 退回/异常
  isSigned: number // 0: 未签收, 1: 已签收
  createTime: string
  updateTime: string
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
 * 包裹创建请求数据（对应后端 AdminParcelCreateRequest）
 */
export interface ParcelCreateRequest {
  trackingNumber: string
  company: string
  receiverUsername?: string // 收件人用户名（优先使用）
  receiverPhone?: string // 收件人手机号（备用查找方式）
  receiverName?: string // 收件人姓名（可选，默认使用用户的username）
   origin?: string
   destination?: string
  status?: number // 快递状态（可选）
  isSigned?: number // 是否签收（可选）
}

/**
 * 包裹更新请求数据
 */
export interface ParcelUpdateRequest {
  trackingNumber?: string
  company?: string
  receiverId?: number
  receiverName?: string
  receiverPhone?: string
  status?: number
  isSigned?: number
}

/**
 * 包裹 API
 */
export const parcelApi = {
  /**
   * 分页查询包裹列表
   * @param page 页码（从0开始）
   * @param size 每页大小
   */
  list(page: number = 0, size: number = 10) {
    return request<PageResponse<Parcel>>('/api/admin/parcel', {
      params: { page, size }
    })
  },

  /**
   * 根据ID获取包裹详情
   * @param id 包裹ID
   */
  getById(id: number) {
    return request<Parcel>(`/api/admin/parcel/${id}`)
  },

  /**
   * 创建包裹
   * @param data 包裹数据
   */
  create(data: ParcelCreateRequest) {
    return request<Parcel>('/api/admin/parcel', {
      method: 'POST',
      body: JSON.stringify(data)
    })
  },

  /**
   * 更新包裹信息
   * @param id 包裹ID
   * @param data 更新数据
   */
  update(id: number, data: ParcelUpdateRequest) {
    return request<Parcel>(`/api/admin/parcel/${id}`, {
      method: 'PUT',
      body: JSON.stringify(data)
    })
  },

  /**
   * 删除包裹
   * @param id 包裹ID
   */
  delete(id: number) {
    return request<void>(`/api/admin/parcel/${id}`, {
      method: 'DELETE'
    })
  },

  /**
   * 批量删除包裹
   * @param ids 包裹ID数组
   */
  deleteBatch(ids: number[]) {
    return request<void>('/api/admin/parcel', {
      method: 'DELETE',
      body: JSON.stringify(ids)
    })
  },

  /**
   * 修改包裹状态
   * @param id 包裹ID
   * @param status 状态值 (0: 待发货, 1: 已发货, 2: 已入库, 3: 退回/异常)
   */
  changeStatus(id: number, status: number) {
    return request<Parcel>(`/api/admin/parcel/${id}/status`, {
      method: 'POST',
      params: { status }
    })
  },

  /**
   * 为快递生成存放位置和取件码（自动生成）
   * @param id 包裹ID
   * @description 仅对状态为"已入库"且未签收的包裹有效，自动生成唯一的存放位置和取件码
   */
  createPickupInfo(id: number) {
    return request<Parcel>(`/api/admin/parcel/${id}/pickup`, {
      method: 'POST'
    })
  },

  /**
   * 根据快递单号搜索包裹
   * @param trackingNumber 快递单号
   */
  searchByTrackingNumber(trackingNumber: string) {
    return request<Parcel>('/api/admin/parcel/search', {
      params: { trackingNumber }
    })
  }
}
