import request from './request'

// 发送消息
export const sendMessage = (data) => request.post('/chat/send', null, { params: data })
// 获取会话消息列表
export const getConversation = (params) => request.get('/chat/conversation', { params })
// 我的会话列表
export const getConversations = () => request.get('/chat/conversations')
// 未读消息数
export const getChatUnreadCount = () => request.get('/chat/unread-count')
// 标记已读
export const markChatAsRead = (params) => request.put('/chat/read', null, { params })
