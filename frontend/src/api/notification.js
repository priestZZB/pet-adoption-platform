import request from './request'

// 我的通知列表
export const getNotifications = () => request.get('/notifications')

// 未读通知数量
export const getUnreadCount = () => request.get('/notifications/unread-count')

// 标记单条已读
export const markAsRead = (id) => request.put('/notifications/' + id + '/read')

// 全部标记已读
export const markAllAsRead = () => request.put('/notifications/read-all')

// 删除单条通知
export const deleteNotification = (id) => request.delete('/notifications/' + id)

// 一键删除已读通知
export const deleteAllReadNotifications = () => request.delete('/notifications/read-all')

// 一键删除全部通知
export const deleteAllNotifications = () => request.delete('/notifications/all')
