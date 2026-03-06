import request from './request'

export function submitPracticeAnswer(data) {
  return request.post('/practice/submit', data)
}

export function getPracticeHistory(username) {
  return request.get('/practice/history', {
    params: {
      username,
    },
  })
}
