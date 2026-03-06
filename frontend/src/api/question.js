import request from './request'

export function getQuestionList() {
  return request.get('/questions')
}

export function getQuestionDetail(id) {
  return request.get(`/questions/${id}`)
}

export function createAdminQuestion(data) {
  return request.post('/admin/questions', data)
}

export function updateAdminQuestion(id, data) {
  return request.put(`/admin/questions/${id}`, data)
}

export function deleteAdminQuestion(id) {
  return request.delete(`/admin/questions/${id}`)
}