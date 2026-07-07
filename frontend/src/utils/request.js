import axios from 'axios'
import { createDiscreteApi } from 'naive-ui'
import router from '@/router'

const { message } = createDiscreteApi(['message'])

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || '',
  timeout: 30000
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('adminToken')
    if (token) {
      config.headers['Authorization'] = 'Bearer ' + token
    }
    return config
  },
  error => Promise.reject(error)
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const res = response.data
    // 后端统一返回 { code, msg, data }
    if (res.code && res.code !== 200) {
      message.error(res.msg || '请求失败')
      if (res.code === 401) {
        localStorage.removeItem('adminToken')
        localStorage.removeItem('adminInfo')
        router.push('/admin/login')
      }
      return Promise.reject(new Error(res.msg))
    }
    return res
  },
  error => {
    if (error.response) {
      const { status } = error.response
      if (status === 401) {
        localStorage.removeItem('adminToken')
        localStorage.removeItem('adminInfo')
        router.push('/admin/login')
        message.error('登录已过期，请重新登录')
      } else if (status === 403) {
        message.error('没有权限执行此操作')
      } else if (status === 500) {
        message.error('服务器错误，请稍后重试')
      } else {
        message.error(error.response.data?.msg || '请求失败')
      }
    } else {
      message.error('网络错误，请检查网络连接')
    }
    return Promise.reject(error)
  }
)

export default request
