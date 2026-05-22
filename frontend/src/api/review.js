import request from '@/api/request'

export function getProductReviews(productId, params) {
  return request.get(`/mall/products/${productId}/reviews`, { params })
}

export function getReviewStats(productId) {
  return request.get(`/mall/products/${productId}/review-stats`)
}

export function addReview(data) {
  return request.post('/mall/reviews', data)
}

export function addAdditionalReview(reviewId, data) {
  return request.post(`/mall/reviews/${reviewId}/additional`, data)
}

export function getMyReviews(params) {
  return request.get('/mall/reviews/my', { params })
}
