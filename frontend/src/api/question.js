import request from './request'

export function getQuestionList() {
  return request.get('/questions')
}

export function getQuestionDetail(id) {
  return request.get(`/questions/${id}`)
}
