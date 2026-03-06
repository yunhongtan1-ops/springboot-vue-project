import request from './request'

export function submitPracticeAnswer(data) {
  return request.post('/practice/submit', data)
}

export function getPracticeHistory() {
  return request.get('/practice/history')
}

export function getPracticeReview(recordId) {
  return request.get(`/practice/review/${recordId}`)
}