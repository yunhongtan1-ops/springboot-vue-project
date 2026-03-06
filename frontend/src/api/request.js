import axios from 'axios'

const request = axios.create({
  baseURL: '/api',
  timeout: 5000,
})

request.interceptors.response.use(
  (response) => {
    const result = response.data

    if (result.code !== 0) {
      return Promise.reject(new Error(result.message || '请求失败'))
    }

    return result.data
  },
  (error) => {
    const message = error.response?.data?.message || error.message || '网络请求失败'
    return Promise.reject(new Error(message))
  },
)

export default request
