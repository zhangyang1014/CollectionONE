import imRequest from '@/utils/imRequest'

// 登录
export const imLogin = (data: {
  collectorId: string
  password: string
}) => {
  return imRequest({
    url: '/api/v1/im/auth/login',
    method: 'post',
    data
  })
}

// 登出
export const imLogout = () => {
  return imRequest({
    url: '/api/v1/im/auth/logout',
    method: 'post'
  })
}

// 获取用户信息
export const getImUserInfo = () => {
  return imRequest({
    url: '/api/v1/im/auth/user-info',
    method: 'get'
  })
}

// 刷新Token
export const refreshToken = () => {
  return imRequest({
    url: '/api/v1/im/auth/refresh-token',
    method: 'post'
  })
}

// 检查会话状态
export const checkSession = () => {
  return imRequest({
    url: '/api/v1/im/auth/check-session',
    method: 'get'
  })
}

// 人脸检测（上传人脸照片，返回人脸ID）
export const detectFace = (imageBase64: string) => {
  return imRequest<{ face_id: string }>({
    url: '/api/v1/im/face/detect',
    method: 'post',
    data: {
      image: imageBase64
    }
  })
}

// 上传登录人脸记录
export const uploadLoginFace = (data: {
  collector_id: string
  face_image: string // base64 图片
  face_id: string
  login_time: string
}) => {
  return imRequest({
    url: '/api/v1/im/face/login-record',
    method: 'post',
    data
  })
}

