import request from './request'

// 公告列表（公开）
export function getNoticeList() {
  return request.get('/notices')
}

// 公告详情
export function getNoticeDetail(id) {
  return request.get(`/notices/${id}`)
}

// 未读公告列表（登录用户）
export function getUnreadNotices() {
  return request.get('/notices/unread')
}

// 标记公告为已读
export function markNoticeRead(id) {
  return request.post(`/notices/${id}/read`)
}
