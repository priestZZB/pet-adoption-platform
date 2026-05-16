import request from './request'

// 我的通知列表
export const getNotifications = () => request.get('/notifications')

// 未读通知数量
export const getUnreadCount = () => request.get('/notifications/unread-count')

// 标记单条已读
export const markAsRead = (id) => request.put('/notifications/' + id + '/read')

// 全部标记已读
export const markAllAsRead = () => request.put('/notifications/read-all')
