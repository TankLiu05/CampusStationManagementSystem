/**
 * 用户留言相关接口
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
 * 提交留言请求参数
 */
export interface CreateMessageRequest {
  content: string
}

/**
 * 提交留言
 * @param data 留言内容
 */
export function createMessage(data: CreateMessageRequest) {
  return request<Message>('/api/user/message', {
    method: 'POST',
    body: JSON.stringify(data),
  })
}

/**
 * 获取我的留言列表
 */
export function getMyMessages() {
  return request<Message[]>('/api/user/message')
}
