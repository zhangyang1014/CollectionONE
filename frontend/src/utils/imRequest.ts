import axios from 'axios'
import type { AxiosInstance, AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'

/**
 * 催员端（IM端）专用的Axios实例
 * 与管理端完全分离，避免混淆
 */
const imService: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// 催员端请求拦截器
imService.interceptors.request.use(
  (config) => {
    // 使用催员端专用的Token
    const imToken = localStorage.getItem('im_token')
    if (imToken) {
      config.headers.Authorization = `Bearer ${imToken}`
    }
    return config
  },
  (error) => {
    console.error('[IM Request] Request error:', error)
    return Promise.reject(error)
  }
)

// 催员端响应拦截器
imService.interceptors.response.use(
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
    return res
  },
  (error) => {
    console.error('[IM Request] Response error:', error)
    if (error.response) {
      const status = error.response.status
      if (status === 401) {
        // 催员端401错误处理：清除催员端Token，跳转到催员端登录页
        ElMessage.error('登录已过期，请重新登录')
        localStorage.removeItem('im_token')
        localStorage.removeItem('im_user')
        
        // 只跳转到催员端登录页
        window.location.href = '/im/login'
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

export default imService


