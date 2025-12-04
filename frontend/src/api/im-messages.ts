import imService from '@/utils/imRequest'

/**
 * IM消息相关API
 * 催员端发送WhatsApp/SMS/RCS消息
 */

// ========== 类型定义 ==========

/**
 * 发送消息请求参数
 */
export interface SendMessageRequest {
  contactId: number
  messageType: 'text' | 'image' | 'voice' | 'video'
  content: string
  waAccountType?: 'platform' | 'personal'
  waAccountId?: string
  senderId: string
  caseId: number
  tenantId: number
  queueId: number
}

/**
 * 发送消息响应数据
 */
export interface SendMessageResponse {
  messageId: string
  status: string
  sentAt: string
  waAccountType?: string
  waAccountId?: string
}

/**
 * 上传图片响应
 */
export interface UploadImageResponse {
  url: string
  fileSize: number
  fileName: string
}

/**
 * 渠道触达限制信息
 */
export interface ChannelLimitInfo {
  sentCount: number
  maxCount: number
  nextSendTime: string | null
  remainingSeconds: number
}

// ========== API函数 ==========

/**
 * 发送消息
 * @param data 发送消息请求参数
 * @returns 发送消息响应
 */
export function sendMessage(data: SendMessageRequest): Promise<SendMessageResponse> {
  return imService({
    url: '/api/v1/im/messages/send',
    method: 'post',
    data
  }).then((res: any) => {
    // 兼容不同的响应格式
    return res.data || res
  })
}

/**
 * 上传图片
 * @param file 图片文件
 * @returns 图片URL和元数据
 */
export function uploadImage(file: File): Promise<UploadImageResponse> {
  const formData = new FormData()
  formData.append('file', file)
  
  return imService({
    url: '/api/v1/im/upload/image',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  }).then((res: any) => {
    return res.data || res
  })
}

/**
 * 获取渠道触达限制信息
 * @param params 查询参数
 * @returns 渠道限制信息
 */
export function getChannelLimitInfo(params: {
  caseId: number
  contactId: number
  channel: string
  tenantId: number
  queueId: number
}): Promise<ChannelLimitInfo> {
  return imService({
    url: '/api/v1/im/channel-limit/info',
    method: 'get',
    params
  }).then((res: any) => {
    return res.data || res
  })
}

/**
 * 获取消息状态
 * @param messageId 消息ID
 * @returns 消息状态信息
 */
export function getMessageStatus(messageId: string): Promise<any> {
  return imService({
    url: `/api/v1/im/messages/${messageId}/status`,
    method: 'get'
  }).then((res: any) => {
    return res.data || res
  })
}

/**
 * 批量获取消息状态
 * @param messageIds 消息ID列表
 * @returns 消息状态列表
 */
export function getMessagesStatus(messageIds: string[]): Promise<any[]> {
  return imService({
    url: '/api/v1/im/messages/batch-status',
    method: 'post',
    data: { messageIds }
  }).then((res: any) => {
    return res.data || res
  })
}

/**
 * 查询新消息
 * @param params 查询参数
 * @returns 新消息列表
 */
export function getNewMessages(params: {
  contactId: number
  lastMessageId?: string
  limit?: number
}): Promise<{
  messages: any[]
  hasMore: boolean
  unreadCount: number
}> {
  return imService({
    url: '/api/v1/im/messages/new',
    method: 'get',
    params: {
      contactId: params.contactId,
      lastMessageId: params.lastMessageId,
      limit: params.limit || 20
    }
  }).then((res: any) => {
    return res.data || res
  })
}

/**
 * 标记消息已读
 * @param contactId 联系人ID
 * @returns 标记结果
 */
export function markMessagesAsRead(contactId: number): Promise<any> {
  return imService({
    url: '/api/v1/im/messages/mark-read',
    method: 'post',
    data: { contactId }
  }).then((res: any) => {
    return res.data || res
  })
}

/**
 * 获取未读消息数
 * @param caseId 案件ID
 * @returns 未读消息数统计
 */
export function getUnreadCount(caseId: number): Promise<{
  total: number
  byContact: Record<number, number>
}> {
  return imService({
    url: '/api/v1/im/messages/unread-count',
    method: 'get',
    params: { caseId }
  }).then((res: any) => {
    return res.data || res
  })
}

