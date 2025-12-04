import request from '@/utils/request'

/**
 * 获取白名单IP配置列表
 */
export function getCollectorLoginWhitelistList(tenantId: number) {
  return request({
    url: '/collector-login-whitelist',
    method: 'get',
    params: {
      tenant_id: tenantId
    }
  })
}

/**
 * 获取白名单IP配置详情
 */
export function getCollectorLoginWhitelist(id: number) {
  return request({
    url: `/collector-login-whitelist/${id}`,
    method: 'get'
  })
}

/**
 * 创建白名单IP配置
 */
export function createCollectorLoginWhitelist(data: {
  tenant_id: number
  ip_address: string
  description?: string
  is_active?: boolean
  is_enabled?: boolean
}) {
  return request({
    url: '/collector-login-whitelist',
    method: 'post',
    data: {
      tenantId: data.tenant_id,
      ipAddress: data.ip_address,
      description: data.description,
      isActive: data.is_active !== undefined ? data.is_active : true,
      isEnabled: data.is_enabled !== undefined ? data.is_enabled : false
    }
  })
}

/**
 * 更新白名单IP配置
 */
export function updateCollectorLoginWhitelist(id: number, data: {
  ip_address?: string
  description?: string
  is_active?: boolean
  is_enabled?: boolean
}) {
  const updateData: any = {}
  if (data.ip_address !== undefined) {
    updateData.ipAddress = data.ip_address
  }
  if (data.description !== undefined) {
    updateData.description = data.description
  }
  if (data.is_active !== undefined) {
    updateData.isActive = data.is_active
  }
  if (data.is_enabled !== undefined) {
    updateData.isEnabled = data.is_enabled
  }
  
  return request({
    url: `/collector-login-whitelist/${id}`,
    method: 'put',
    data: updateData
  })
}

/**
 * 删除白名单IP配置
 */
export function deleteCollectorLoginWhitelist(id: number) {
  return request({
    url: `/collector-login-whitelist/${id}`,
    method: 'delete'
  })
}

/**
 * 启用/禁用甲方的白名单IP登录管理
 */
export function setWhitelistEnabled(tenantId: number, enabled: boolean) {
  return request({
    url: '/collector-login-whitelist/enable',
    method: 'put',
    params: {
      tenant_id: tenantId,
      enabled: enabled
    }
  })
}

/**
 * 检查甲方是否启用了白名单IP登录管理
 */
export function checkWhitelistEnabled(tenantId: number) {
  return request({
    url: '/collector-login-whitelist/check-enabled',
    method: 'get',
    params: {
      tenant_id: tenantId
    }
  })
}



