import { defineStore } from 'pinia'
import { ref } from 'vue'
import { imLogin, imLogout, getImUserInfo } from '@/api/im'

export interface ImUser {
  id: string
  collectorId: string
  collectorIdNumeric?: number // 数字格式的collectorId，用于API调用
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
  // 时区信息
  agencyTimezone?: string // 机构时区（IANA时区名称，如：America/Mexico_City）
  agencyTimezoneShort?: string // 机构时区缩写（如：CST）
  agencyTimezoneOffset?: number // 机构时区UTC偏移量（如：-6）
  tenantTimezone?: string // 甲方时区（IANA时区名称，如：America/Mexico_City）
  tenantTimezoneShort?: string // 甲方时区缩写（如：CST）
  tenantTimezoneOffset?: number // 甲方时区UTC偏移量（如：-6）
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
      try {
        token.value = storedToken
        user.value = JSON.parse(storedUser)
        isLoggedIn.value = true
        return true
      } catch (e) {
        console.error('恢复IM用户状态失败:', e)
        return false
      }
    }
    return false
  }

  // 登录
  const login = async (credentials: {
    tenantId: string
    collectorId: string
    password: string
  }) => {
    try {
      // imRequest已经返回了response.data，所以response就是后端返回的完整对象
      // 响应格式: { code: 200, message: "success", data: { token: "...", user: {...} } }
      const response = await imLogin(credentials) as {
        code?: number
        message?: string
        data?: {
          token?: string
          user?: any
        }
      }
      
      if (response.code === 200 && response.data) {
        // 先保存到localStorage（确保路由守卫能立即读取到）
        const tokenValue = response.data.token
        const userValue = response.data.user
        
        if (tokenValue && userValue) {
          localStorage.setItem('im_token', tokenValue)
          localStorage.setItem('im_user', JSON.stringify(userValue))
          
          // 然后更新响应式状态
          token.value = tokenValue
          user.value = userValue as ImUser
          isLoggedIn.value = true
          
          console.log('[IM Store] 登录成功，状态已更新:', {
            isLoggedIn: isLoggedIn.value,
            hasToken: !!token.value,
            hasUser: !!user.value,
            tokenLength: token.value?.length || 0
          })
        } else {
          throw new Error('登录响应数据不完整')
        }

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
      const response = await getImUserInfo() as {
        code?: number
        data?: any
      }
      if (response.code === 200 && response.data) {
        user.value = { ...user.value, ...response.data } as ImUser
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
    hasAllPermissions,
    initFromStorage  // ✅ 导出initFromStorage方法
  }
})

