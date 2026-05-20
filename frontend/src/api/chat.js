import request from './request'

export const sendMessage = (data) => request.post('/chat/send', null, { params: data })
export const getConversation = (params) => request.get('/chat/conversation', { params })
export const getConversations = () => request.get('/chat/conversations')
export const getChatUnreadCount = () => request.get('/chat/unread-count')
export const markChatAsRead = (params) => request.put('/chat/read', null, { params })
export const deleteConversation = (params) => request.delete('/chat/conversation', { params })
export const getOnlineUsers = () => request.get('/chat/online-users')
