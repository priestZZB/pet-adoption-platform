import request from '@/api/request'

export function getPetComments(petId, params) {
  return request.get(`/pets/${petId}/comments`, { params })
}

export function addPetComment(petId, data) {
  return request.post(`/pets/${petId}/comments`, data)
}

export function replyComment(commentId, data) {
  return request.post(`/pets/comments/${commentId}/reply`, data)
}

export function likeComment(commentId) {
  return request.post(`/pets/comments/${commentId}/like`)
}

export function unlikeComment(commentId) {
  return request.delete(`/pets/comments/${commentId}/like`)
}

export function dislikeComment(commentId) {
  return request.post(`/pets/comments/${commentId}/dislike`)
}

export function undislikeComment(commentId) {
  return request.delete(`/pets/comments/${commentId}/dislike`)
}

export function hideComment(commentId) {
  return request.put(`/pets/comments/${commentId}/hide`)
}

export function deleteComment(commentId) {
  return request.delete(`/pets/comments/${commentId}`)
}
