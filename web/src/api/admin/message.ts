/**
 * 管理员留言管理相关接口
 */
import { request } from '@/utls/request'

/**
 * 留言信息接口
 */
export interface Message {
  id: number
  userId: number
  username?: string
  phone?: string
  content: string
  replyContent?: string
  status: number // 0: 待回复, 1: 已回复
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
 * 回复留言请求参数
 */
export interface ReplyMessageRequest {
  replyContent: string
}

/**
 * 分页查询留言列表
 * @param page 页码（从 0 开始）
 * @param size 每页数量
 * @param status 状态过滤（可选）：0=待回复，1=已回复
 */
export function getMessageList(page: number = 0, size: number = 10, status?: number) {
  return request<PageResponse<Message>>('/api/admin/message', {
    params: { page, size, ...(status !== undefined && { status }) },
  })
}

/**
 * 回复留言
 * @param id 留言 ID
 * @param data 回复内容
 */
export function replyMessage(id: number, data: ReplyMessageRequest) {
  return request<Message>(`/api/admin/message/${id}/reply`, {
    method: 'POST',
    body: JSON.stringify(data),
  })
}

/**
 * 删除留言
 * @param id 留言 ID
 */
export function deleteMessage(id: number) {
  return request<void>(`/api/admin/message/${id}`, {
    method: 'DELETE',
  })
}
