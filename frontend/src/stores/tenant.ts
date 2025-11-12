import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useTenantStore = defineStore('tenant', () => {
  // 当前选择的甲方ID
  const currentTenantId = ref<number | undefined>(undefined)
  
  // 当前选择的甲方信息
  const currentTenant = ref<any>(null)

  // 设置当前甲方
  const setCurrentTenant = (tenantId: number | undefined, tenant: any = null) => {
    currentTenantId.value = tenantId
    currentTenant.value = tenant
    
    // 保存到localStorage，刷新页面后保持选择
    if (tenantId) {
      localStorage.setItem('currentTenantId', String(tenantId))
      if (tenant) {
        localStorage.setItem('currentTenant', JSON.stringify(tenant))
      }
    } else {
      localStorage.removeItem('currentTenantId')
      localStorage.removeItem('currentTenant')
    }
  }

  // 从localStorage恢复选择
  const restoreFromStorage = () => {
    const savedTenantId = localStorage.getItem('currentTenantId')
    const savedTenant = localStorage.getItem('currentTenant')
    
    if (savedTenantId) {
      currentTenantId.value = parseInt(savedTenantId)
      if (savedTenant) {
        try {
          currentTenant.value = JSON.parse(savedTenant)
        } catch (e) {
          console.error('解析保存的甲方信息失败', e)
        }
      }
    }
  }

  // 清除当前甲方
  const clearCurrentTenant = () => {
    setCurrentTenant(undefined, null)
  }

  return {
    currentTenantId,
    currentTenant,
    setCurrentTenant,
    restoreFromStorage,
    clearCurrentTenant
  }
})
