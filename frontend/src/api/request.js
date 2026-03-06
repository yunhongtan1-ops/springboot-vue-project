import axios from 'axios'
import { clearAuth, getStoredToken } from '@/utils/auth'

const request = axios.create({
  baseURL: '/api',
  timeout: 5000,
})

request.interceptors.request.use((config) => {
  const token = getStoredToken()

  if (token) {
    config.headers = config.headers || {}
    config.headers.Authorization = `Bearer ${token}`
  }

  return config
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
    if (error.response?.status === 401) {
      clearAuth()
      window.dispatchEvent(new Event('auth-changed'))
    }

    const message = error.response?.data?.message || error.message || '网络请求失败'
    return Promise.reject(new Error(message))
  },
)

export default request
