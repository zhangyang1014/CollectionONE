import request from '@/utils/request'

// 登录
export const imLogin = (data: {
  tenantId: string
  collectorId: string
  password: string
}) => {
  return request({
    url: '/api/v1/im/auth/login',
    method: 'post',
    data
  })
}

// 登出
export const imLogout = () => {
  return request({
    url: '/api/v1/im/auth/logout',
    method: 'post'
  })
}

// 获取用户信息
export const getImUserInfo = () => {
  return request({
    url: '/api/v1/im/auth/user-info',
    method: 'get'
  })
}

// 刷新Token
export const refreshToken = () => {
  return request({
    url: '/api/v1/im/auth/refresh-token',
    method: 'post'
  })
}

// 检查会话状态
export const checkSession = () => {
  return request({
    url: '/api/v1/im/auth/check-session',
    method: 'get'
  })
}

// 人脸检测（上传人脸照片，返回人脸ID）
export const detectFace = (imageFile: File) => {
  const formData = new FormData()
  formData.append('image', imageFile)
  
  return request<{ face_id: string }>({
    url: '/api/v1/im/face/detect',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 上传登录人脸记录
export const uploadLoginFace = (data: {
  collector_id: string
  tenant_id: string
  face_image: string // base64 图片
  face_id: string
  login_time: string
}) => {
  return request({
    url: '/api/v1/im/face/login-record',
    method: 'post',
    data
  })
}

