import request from './request'

// 商品分类列表（公开）
export const getMallCategories = () => request.get('/mall/categories')
// 商品列表（分页+分类筛选，公开）
export const getMallProducts = (params) => request.get('/mall/products', { params })
// 商品详情（公开）
export const getMallProductDetail = (id) => request.get('/mall/products/' + id)
// 加入购物车
export const addToCart = (data) => request.post('/mall/cart', data)
// 购物车列表
export const getCart = () => request.get('/mall/cart')
// 修改购物车商品数量
export const updateCartItem = (id, quantity) => request.put('/mall/cart/' + id, null, { params: { quantity } })
// 删除购物车商品
export const removeCartItem = (id) => request.delete('/mall/cart/' + id)
// 清空购物车
export const clearCart = () => request.delete('/mall/cart')
// 创建订单
export const createOrder = (data) => request.post('/mall/orders', data)
// 我的订单列表
export const getMyOrders = (params) => request.get('/mall/orders', { params })
// 订单详情
export const getOrderDetail = (id) => request.get('/mall/orders/' + id)
// 模拟支付确认
export const payOrder = (id) => request.post('/mall/orders/' + id + '/pay')
// 确认收货
export const receiveOrder = (id) => request.put('/mall/orders/' + id + '/receive')
// 取消订单
export const cancelOrder = (id) => request.put('/mall/orders/' + id + '/cancel')
