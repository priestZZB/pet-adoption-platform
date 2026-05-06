import request from './request'

// 开始答题（随机10题）
export const startExam = () => request.get('/adopt/exam/start')
// 提交答题
export const submitExam = (data) => request.post('/adopt/exam/submit', data)
// 历史答题记录
export const getExamHistory = () => request.get('/adopt/exam/history')
// 提交领养申请（需考试满分）
export const submitApplication = (data) => request.post('/adopt/applications', data)
// 我的领养申请列表
export const getMyApplications = () => request.get('/adopt/applications')
// 领养申请详情
export const getApplicationDetail = (id) => request.get('/adopt/applications/' + id)
