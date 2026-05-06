import request from './request'

// AI对话（发送问题→返回回答，对接DeepSeek API）
export const chat = (data) => request.post('/ai/chat', data)
// 对话历史（分页）
export const getChatHistory = (params) => request.get('/ai/history', { params })
// 清空对话历史
export const clearChatHistory = () => request.delete('/ai/history')
