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
