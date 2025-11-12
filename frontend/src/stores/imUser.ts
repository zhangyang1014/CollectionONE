import { defineStore } from 'pinia'
import { ref } from 'vue'
import { imLogin, imLogout, getImUserInfo } from '@/api/im'

export interface ImUser {
  id: string
  collectorId: string
  collectorName: string
  tenantId: string
  tenantName: string
  role: string
  team: string
  permissions: string[]
  avatar?: string
  email?: string
  phone?: string
  token: string
  whatsappConnected?: boolean
}

export const useImUserStore = defineStore('imUser', () => {
  const user = ref<ImUser | null>(null)
  const token = ref<string>('')
  const isLoggedIn = ref(false)

  // 从localStorage恢复登录状态
  const initFromStorage = () => {
    const storedToken = localStorage.getItem('im_token')
    const storedUser = localStorage.getItem('im_user')
    
    if (storedToken && storedUser) {
      token.value = storedToken
      user.value = JSON.parse(storedUser)
      isLoggedIn.value = true
    }
  }

  // 登录
  const login = async (credentials: {
    tenantId: string
    collectorId: string
    password: string
  }) => {
    try {
      const response = await imLogin(credentials)
      
      if (response.code === 200) {
        token.value = response.data.token
        user.value = response.data.user
        isLoggedIn.value = true

        // 保存到localStorage
        localStorage.setItem('im_token', token.value)
        localStorage.setItem('im_user', JSON.stringify(user.value))

        return response.data
      } else {
        throw new Error(response.message || '登录失败')
      }
    } catch (error: any) {
      console.error('登录错误:', error)
      throw error
    }
  }

  // 登出
  const logout = async () => {
    try {
      await imLogout()
    } catch (error) {
      console.error('登出错误:', error)
    } finally {
      // 清除状态
      user.value = null
      token.value = ''
      isLoggedIn.value = false
      
      // 清除localStorage
      localStorage.removeItem('im_token')
      localStorage.removeItem('im_user')
    }
  }

  // 更新用户信息
  const updateUserInfo = async () => {
    try {
      const response = await getImUserInfo()
      if (response.code === 200) {
        user.value = { ...user.value, ...response.data }
        localStorage.setItem('im_user', JSON.stringify(user.value))
      }
    } catch (error) {
      console.error('更新用户信息错误:', error)
    }
  }

  // 检查权限
  const hasPermission = (permission: string): boolean => {
    if (!user.value) return false
    return user.value.permissions.includes(permission) || user.value.permissions.includes('*')
  }

  // 检查多个权限（满足任一即可）
  const hasAnyPermission = (permissions: string[]): boolean => {
    if (!user.value) return false
    return permissions.some(p => hasPermission(p))
  }

  // 检查多个权限（需全部满足）
  const hasAllPermissions = (permissions: string[]): boolean => {
    if (!user.value) return false
    return permissions.every(p => hasPermission(p))
  }

  // 初始化
  initFromStorage()

  return {
    user,
    token,
    isLoggedIn,
    login,
    logout,
    updateUserInfo,
    hasPermission,
    hasAnyPermission,
    hasAllPermissions
  }
})

