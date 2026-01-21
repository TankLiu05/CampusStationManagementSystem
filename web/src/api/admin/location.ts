/**
 * 管理员地址管理相关接口
 */
import { request } from '@/utls/request'

/**
 * 收货地址信息
 */
export interface Location {
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
 * 管理员查询用户默认收货地址
 * @param userId 用户ID
 */
export function getUserDefaultLocation(userId: number) {
  return request<Location>(`/api/admin/user/${userId}/location/default`, {
    method: 'GET',
  })
}
