import request from './request'

// 提交走访记录（含多图）
export const submitVisit = (data) => request.post('/volunteer/visits', data)
// 我的走访记录列表（分页）
export const getMyVisits = (params) => request.get('/volunteer/visits', { params })
// 走访记录详情
export const getVisitDetail = (id) => request.get('/volunteer/visits/' + id)
