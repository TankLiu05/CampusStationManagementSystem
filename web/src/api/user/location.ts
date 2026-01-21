/**
 * 用户收货地址相关接口
 */
import { request } from '@/utls/request'

/**
 * 收货地址信息
 */
export interface UserLocation {
  id: number
  userId: number
  username: string
  phone: string
  province?: string
  city?: string
  street?: string
  detailAddress: string
  isDefault?: boolean
  createTime?: string
  updateTime?: string
}

/**
 * 创建收货地址请求参数
 */
export interface CreateLocationRequest {
  username?: string    // 收件人姓名，不填默认用当前用户名
  phone?: string       // 收件人手机号，不填默认用当前用户手机号
  province?: string    // 省份
  city?: string        // 城市
  street?: string      // 街道
  detailAddress: string // 详细地址（必填）
  isDefault?: boolean  // 是否设为默认地址
}

/**
 * 修改收货地址请求参数
 */
export interface UpdateLocationRequest {
  username?: string
  phone?: string
  province?: string
  city?: string
  street?: string
  detailAddress?: string
  isDefault?: boolean
}

/**
 * 创建收货地址
 * @param data 收货地址信息
 */
export function createLocation(data: CreateLocationRequest) {
  return request<UserLocation>('/api/user/location', {
    method: 'POST',
    body: JSON.stringify(data),
  })
}

/**
 * 获取当前用户收货地址列表
 */
export function getLocationList() {
  return request<UserLocation[]>('/api/user/location', {
    method: 'GET',
  })
}

/**
 * 获取当前用户默认收货地址
 */
export function getDefaultLocation() {
  return request<UserLocation>('/api/user/location/default', {
    method: 'GET',
  })
}

/**
 * 获取收货地址详情
 * @param id 地址ID
 */
export function getLocationById(id: number) {
  return request<UserLocation>(`/api/user/location/${id}`, {
    method: 'GET',
  })
}

/**
 * 修改收货地址
 * @param id 地址ID
 * @param data 修改内容
 */
export function updateLocation(id: number, data: UpdateLocationRequest) {
  return request<UserLocation>(`/api/user/location/${id}`, {
    method: 'PUT',
    body: JSON.stringify(data),
  })
}

/**
 * 删除收货地址
 * @param id 地址ID
 */
export function deleteLocation(id: number) {
  return request<void>(`/api/user/location/${id}`, {
    method: 'DELETE',
  })
}

/**
 * 设为默认地址
 * @param id 地址ID
 */
export function setDefaultLocation(id: number) {
  return request<void>(`/api/user/location/${id}/default`, {
    method: 'POST',
  })
}
