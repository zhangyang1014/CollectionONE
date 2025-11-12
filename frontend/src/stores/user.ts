import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref<string>('')
  const userInfo = ref<any>(null)
  const permissions = ref<string[]>([])

  // 从localStorage恢复用户信息
  const initFromStorage = () => {
    const savedToken = localStorage.getItem('token')
    const savedUserInfo = localStorage.getItem('userInfo')
    
    if (savedToken) {
      token.value = savedToken
    }
    
    if (savedUserInfo) {
      try {
        userInfo.value = JSON.parse(savedUserInfo)
      } catch (e) {
        console.error('解析用户信息失败', e)
      }
    }
  }

  const setToken = (newToken: string) => {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  const setUserInfo = (info: any) => {
    userInfo.value = info
    if (info) {
      localStorage.setItem('userInfo', JSON.stringify(info))
    } else {
      localStorage.removeItem('userInfo')
    }
  }

  const setPermissions = (perms: string[]) => {
    permissions.value = perms
  }

  const logout = () => {
    token.value = ''
    userInfo.value = null
    permissions.value = []
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  // 初始化
  initFromStorage()

  return {
    token,
    userInfo,
    permissions,
    setToken,
    setUserInfo,
    setPermissions,
    logout,
    initFromStorage
  }
})

