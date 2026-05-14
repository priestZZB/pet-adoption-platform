import request from './request'

// ===== 用户管理 =====
// 用户列表（分页+搜索）
export const getUserList = (params) => request.get('/admin/users', { params })
// 禁用/启用用户
export const toggleUserStatus = (id) => request.put('/admin/user/' + id + '/status')
// 角色列表
export const getRoles = () => request.get('/admin/roles')
// 修改用户角色
export const assignRole = (id, roleId) => request.put('/admin/user/' + id + '/role', null, { params: { roleId } })

// ===== 志愿者审核 =====
// 志愿者申请列表
export const getVolunteerApplies = () => request.get('/admin/volunteer/applies')
// 审核志愿者申请（通过/驳回）
export const reviewVolunteer = (id, data) => request.put('/admin/volunteer/apply/' + id, data)

// ===== 送养人审核 =====
// 送养人申请列表
export const getDonorApplies = () => request.get('/admin/donor/applies')
// 审核送养人申请（通过/驳回）
export const reviewDonor = (id, data) => request.put('/admin/donor/apply/' + id, data)

// ===== 宠物分类管理 =====
// 新增宠物分类
export const addPetCategory = (params) => request.post('/admin/pet/categories', null, { params })
// 编辑宠物分类
export const updatePetCategory = (id, params) => request.put('/admin/pet/categories/' + id, null, { params })
// 删除宠物分类
export const deletePetCategory = (id) => request.delete('/admin/pet/categories/' + id)

// ===== 宠物管理 =====
// 所有宠物列表
export const getAllPets = (params) => request.get('/admin/pets', { params })
// 上架/下架/删除宠物
export const updatePetStatus = (id, status) => request.put('/admin/pets/' + id + '/status', null, { params: { status } })
// 终审（通过/打回）
export const finalReview = (id, data) => request.post('/admin/pets/' + id + '/final-review', data)

// ===== 试题管理 =====
// 试题列表（分页）
export const getQuestions = (params) => request.get('/admin/adopt/questions', { params })
// 新增试题
export const addQuestion = (params) => request.post('/admin/adopt/questions', null, { params })
// 编辑试题
export const updateQuestion = (id, params) => request.put('/admin/adopt/questions/' + id, null, { params })
// 删除试题
export const deleteQuestion = (id) => request.delete('/admin/adopt/questions/' + id)

// ===== 领养管理 =====
// 所有领养申请列表
export const getAllApplications = (params) => request.get('/admin/adopt/applications', { params })
// 领养申请详情
export const getAdminAppDetail = (id) => request.get('/admin/adopt/applications/' + id)
// 干预审核（通过/拒绝）
export const adminReviewApplication = (id, action) => request.put('/admin/adopt/applications/' + id, null, { params: { action } })

// ===== 商品分类管理 =====
// 新增商品分类
export const addMallCategory = (params) => request.post('/admin/mall/categories', null, { params })
// 编辑商品分类
export const updateMallCategory = (id, params) => request.put('/admin/mall/categories/' + id, null, { params })
// 删除商品分类
export const deleteMallCategory = (id) => request.delete('/admin/mall/categories/' + id)

// ===== 商品管理 =====
// 新增商品
export const addProduct = (params) => request.post('/admin/mall/products', null, { params })
// 编辑商品
export const updateProduct = (id, params) => request.put('/admin/mall/products/' + id, null, { params })
// 上架/下架商品
export const toggleProductStatus = (id) => request.put('/admin/mall/products/' + id + '/status')

// ===== 订单管理 =====
// 所有订单列表
export const getAllOrders = (params) => request.get('/admin/mall/orders', { params })
// 订单详情
export const getAdminOrderDetail = (id) => request.get('/admin/mall/orders/' + id)
// 发货（填物流单号）
export const shipOrder = (id, data) => request.put('/admin/mall/orders/' + id + '/ship', data)

// ===== 公告管理 =====
// 发布公告
export const sendNotice = (params) => request.post('/admin/notices', null, { params })
// 编辑公告
export const updateNotice = (id, params) => request.put('/admin/notices/' + id, null, { params })
// 删除公告
export const deleteNotice = (id) => request.delete('/admin/notices/' + id)
// 所有公告列表（管理员用，含隐藏）
export const getAllNotices = () => request.get('/admin/notices')

// ===== 反馈管理 =====
// 所有反馈列表
export const getAllFeedbacks = () => request.get('/admin/feedbacks')
// 回复反馈
export const replyFeedback = (id, reply) => request.post('/admin/feedback/' + id + '/reply', null, { params: { reply } })

// ===== 日志 =====
// 操作日志列表
export const getLogs = (params) => request.get('/admin/logs', { params })

// ===== AI记录 =====
// 所有AI问答记录（分页）
export const getAIRecords = (params) => request.get('/ai/admin/records', { params })
// 所有对话列表（按 session 分组）
export const getAdminSessions = () => request.get('/ai/admin/sessions')
// 获取某次对话的消息列表（管理员，包含已删除）
export const getAdminSessionMessages = (sessionId) => request.get('/ai/admin/sessions/' + sessionId + '/messages')

// ===== 轮播管理 =====
// 轮播图列表（管理员）
export const getBanners = () => request.get('/admin/banners')
// 新增轮播图
export const addBanner = (data) => request.post('/admin/banners', data)
// 编辑轮播图
export const updateBanner = (id, data) => request.put('/admin/banners/' + id, data)
// 删除轮播图
export const deleteBanner = (id) => request.delete('/admin/banners/' + id)
