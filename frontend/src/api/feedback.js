import request from './request'

// 提交意见反馈
export function submitFeedback(data) {
  return request.post('/feedback', data)
}

// 我的反馈列表
export function getMyFeedback() {
  return request.get('/feedback/my')
}
