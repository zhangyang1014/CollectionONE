/**
 * API配置
 * 统一管理API基础URL
 */

/**
 * 获取API基础URL
 */
export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

/**
 * 获取完整的API URL
 * @param path API路径（不含/api/v1前缀）
 * @returns 完整的API URL
 */
export function getApiUrl(path: string): string {
  const cleanPath = path.startsWith('/') ? path : `/${path}`
  return `${API_BASE_URL}/api/v1${cleanPath}`
}

/**
 * API版本前缀
 */
export const API_V1_PREFIX = '/api/v1'


