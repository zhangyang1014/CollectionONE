import axios from 'axios'
import type { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'

/**
 * 管理端专用的Axios实例
 * 催员端请使用 imRequest.ts
 */
const service: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// 管理端请求拦截器
service.interceptors.request.use(
  (config) => {
    // 使用管理端专用的Token
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    console.error('[Admin Request] Request error:', error)
    return Promise.reject(error)
  }
)

// 管理端响应拦截器
service.interceptors.response.use(
  (response: AxiosResponse) => {
    const res = response.data
    // 如果响应是数组，直接返回
    if (Array.isArray(res)) {
      return res
    }
    // 如果响应有code字段且不等于200，则报错
    if (res.code && res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || 'Error'))
    }
    // 特殊处理：登录接口需要访问code字段，返回整个响应对象
    const url = response.config.url || ''
    if (url.includes('/admin/auth/login') || url.includes('/admin/auth/logout') || url.includes('/admin/auth/me')) {
      return res
    }
    // 其他接口：如果响应有data字段，返回data（Java后端格式）
    if (res.data !== undefined) {
      return res.data
    }
    // 否则返回整个响应对象
    return res
  },
  (error) => {
    console.error('[Admin Request] Response error:', error)
    if (error.response) {
      const status = error.response.status
      if (status === 401) {
        // 管理端401错误处理：清除管理端Token，跳转到管理端登录页
        ElMessage.error('登录已过期，请重新登录')
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        
        // 只跳转到管理端登录页
        window.location.href = '/admin/login'
      } else if (status === 403) {
        ElMessage.error('权限不足')
      } else if (status === 404) {
        ElMessage.error('请求的资源不存在')
      } else if (status >= 500) {
        ElMessage.error('服务器错误')
      } else {
        ElMessage.error(error.response.data.message || '请求失败')
      }
    } else {
      ElMessage.error('网络错误，请检查网络连接')
    }
    return Promise.reject(error)
  }
)

export default service

