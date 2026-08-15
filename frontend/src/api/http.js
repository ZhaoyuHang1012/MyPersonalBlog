import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

const http = axios.create({
  baseURL: '/api',
  timeout: 15000
})

// 请求拦截：附带 JWT
http.interceptors.request.use((config) => {
  const token = localStorage.getItem('blog_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截：统一处理 Result 结构
http.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code === 0) {
      return res.data
    }
    ElMessage.error(res.message || '请求失败')
    return Promise.reject(new Error(res.message || '请求失败'))
  },
  (error) => {
    const status = error.response?.status
    if (status === 401) {
      localStorage.removeItem('blog_token')
      localStorage.removeItem('blog_user')
      if (router.currentRoute.value.path.startsWith('/admin')) {
        router.push('/admin/login')
      }
      ElMessage.error('未登录或登录已过期')
    } else {
      ElMessage.error(error.response?.data?.message || '网络错误，请稍后重试')
    }
    return Promise.reject(error)
  }
)

export default http
