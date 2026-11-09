import axios from 'axios'
import { ElMessage } from 'element-plus'

// admin 后台专用 axios 实例：使用独立的 adminToken，与用户 token 隔离
const adminRequest = axios.create({
  baseURL: 'http://146.56.197.54:8888/api',
  timeout: 10000
})

adminRequest.interceptors.request.use(
  config => {
    const token = localStorage.getItem('adminToken')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => Promise.reject(error)
)

adminRequest.interceptors.response.use(
  response => {
    const { code, message, data } = response.data
    if (code === 200) {
      return data
    }
    ElMessage.error(message || '请求失败')
    return Promise.reject(new Error(message))
  },
  error => {
    if (error.response?.status === 401) {
      localStorage.removeItem('adminToken')
    }
    ElMessage.error(error.message || '网络错误')
    return Promise.reject(error)
  }
)

export default adminRequest
