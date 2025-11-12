/**
 * 管理后台认证 API
 */
import request from '@/utils/request'

export interface AdminLoginRequest {
  loginId: string
  password: string
}

export interface AdminLoginResponse {
  code: number
  message: string
  data: {
    token: string
    user: {
      id: number
      loginId: string
      username: string
      role: string
      email?: string
      name?: string
    }
  }
}

/**
 * 管理后台登录
 */
export function adminLogin(data: AdminLoginRequest) {
  return request<AdminLoginResponse>({
    url: '/api/v1/admin/auth/login',
    method: 'post',
    data
  })
}

/**
 * 管理后台登出
 */
export function adminLogout() {
  return request({
    url: '/api/v1/admin/auth/logout',
    method: 'post'
  })
}

/**
 * 获取当前用户信息
 */
export function getAdminUserInfo() {
  return request({
    url: '/api/v1/admin/auth/me',
    method: 'get'
  })
}

