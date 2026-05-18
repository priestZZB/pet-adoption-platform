import request from './request'

// 宠物分类列表（公开）
export const getCategories = () => request.get('/pet/categories')
// 待领养宠物列表（分页+筛选+搜索，公开）
export const getPetList = (params) => request.get('/pets', { params })
// 宠物详情（含多图+送养人，公开）
export const getPetDetail = (id) => request.get('/pets/' + id)
// 收藏宠物
export const favorite = (id) => request.post('/pets/' + id + '/favorite')
// 取消收藏
export const unfavorite = (id) => request.delete('/pets/' + id + '/favorite')
// 我的收藏列表
export const getFavorites = () => request.get('/pets/favorites')
// 发布送养宠物（含多图上传）
export const publishPet = (data) => request.post('/donate/pets', data)
// 编辑宠物信息
export const updatePet = (id, data) => request.put('/donate/pets/' + id, data)
// 下架宠物
export const offlinePet = (id) => request.put('/donate/pets/' + id + '/offline')
// 撤回送养（仅未审核可撤回）
export const withdrawPet = (id) => request.put('/donate/pets/' + id + '/withdraw')
// 我发布的宠物列表
export const getMyPets = () => request.get('/donate/pets')
// 查看领养申请列表
export const getPetApplications = (petId) => request.get('/donate/pets/' + petId + '/applications')
// 同意/拒绝领养申请
export const reviewApplication = (id, action) => request.put('/donate/application/' + id, null, { params: { action } })
