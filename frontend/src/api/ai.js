import request from './request'

// AI对话（发送问题→返回回答）
export const chat = (data) => request.post('/ai/chat', data)
// 对话历史（分页，兼容旧版）
export const getChatHistory = (params) => request.get('/ai/history', { params })
// 清空全部对话历史
export const clearChatHistory = () => request.delete('/ai/history')

// 获取对话列表（按 session 分组）
export const getSessions = () => request.get('/ai/sessions')
// 获取某次对话的消息列表
export const getSessionMessages = (sessionId) => request.get('/ai/sessions/' + sessionId + '/messages')
// 删除某次对话
export const deleteSession = (sessionId) => request.delete('/ai/sessions/' + sessionId)
