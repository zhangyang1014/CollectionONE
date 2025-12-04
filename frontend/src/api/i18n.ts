/**
 * 多语言配置管理API
 */
import request from '@/utils/request'

// ==================== 类型定义 ====================

/** 语言信息 */
export interface Language {
  id: number
  locale: string
  name: string
  flagIcon?: string
  textDirection: 'ltr' | 'rtl'
  isEnabled: boolean
  isDefault: boolean
  sortOrder: number
  responsibleUserId?: number
  translationProgress?: number
  missingKeysCount?: number
  version?: string
  createdAt?: string
  updatedAt?: string
  createdBy?: number
  updatedBy?: number
  remarks?: string
}

/** 新增/编辑语言表单 */
export interface LanguageFormData {
  locale: string
  name: string
  flagIcon?: string
  textDirection: 'ltr' | 'rtl'
  isEnabled: boolean
  responsibleUserId?: number
  remarks?: string
}

/** 语言包信息 */
export interface TranslationBundle {
  id: number
  languageId: number
  namespace: string
  keyPath: string
  value: string
  description?: string
  priority?: 'P0' | 'P1' | 'P2'
  status?: 'draft' | 'published'
  version?: string
  createdAt?: string
  updatedAt?: string
  updatedBy?: number
}

/** 语言包版本信息 */
export interface TranslationVersion {
  id: number
  languageId: number
  version: string
  bundleJson: string
  changeSummary?: string
  uploadedBy?: number
  uploadedAt: string
  isCurrent: boolean
}

/** 翻译统计信息 */
export interface TranslationStatistics {
  locale: string
  languageName: string
  totalKeys: number
  translatedKeys: number
  missingKeys: number
  translationProgress: number
  version?: string
  lastUpdatedAt?: string
}

/** 缺失翻译项 */
export interface MissingTranslation {
  keyPath: string
  baseValue: string
  namespace: string
  priority: 'P0' | 'P1' | 'P2'
}

/** 语言包上传校验结果 */
export interface BundleUploadValidation {
  isValid: boolean
  totalKeys: number
  newKeys: number
  updatedKeys: number
  unknownKeys: string[]
  warnings: string[]
  errors: string[]
  progressBefore: number
  progressAfter: number
}

/** 翻译质量问题 */
export interface QualityIssue {
  type: 'missing_variable' | 'length_exceeded' | 'html_tag' | 'plural_missing' | 'untranslated'
  keyPath: string
  baseValue: string
  translatedValue: string
  suggestion?: string
}

// ==================== 语言管理API ====================

/**
 * 获取语言列表
 * @param params 查询参数
 */
export function getLanguageList(params?: {
  keyword?: string
  status?: 'all' | 'enabled' | 'disabled'
  direction?: 'all' | 'ltr' | 'rtl'
  page?: number
  pageSize?: number
}) {
  return request({
    url: '/admin/languages',
    method: 'get',
    params,
  })
}

/**
 * 获取语言详情
 * @param id 语言ID
 */
export function getLanguageDetail(id: number) {
  return request({
    url: `/admin/languages/${id}`,
    method: 'get',
  })
}

/**
 * 新增语言
 * @param data 语言表单数据
 */
export function createLanguage(data: LanguageFormData) {
  return request({
    url: '/admin/languages',
    method: 'post',
    data,
  })
}

/**
 * 编辑语言
 * @param id 语言ID
 * @param data 语言表单数据
 */
export function updateLanguage(id: number, data: Partial<LanguageFormData>) {
  return request({
    url: `/admin/languages/${id}`,
    method: 'put',
    data,
  })
}

/**
 * 启用语言
 * @param id 语言ID
 */
export function enableLanguage(id: number) {
  return request({
    url: `/admin/languages/${id}/enable`,
    method: 'put',
  })
}

/**
 * 停用语言
 * @param id 语言ID
 */
export function disableLanguage(id: number) {
  return request({
    url: `/admin/languages/${id}/disable`,
    method: 'put',
  })
}

/**
 * 批量启用/停用语言
 * @param ids 语言ID数组
 * @param enabled 是否启用
 */
export function batchUpdateLanguageStatus(ids: number[], enabled: boolean) {
  return request({
    url: '/admin/languages/batch-status',
    method: 'put',
    data: { ids, enabled },
  })
}

/**
 * 设置默认语言
 * @param id 语言ID
 */
export function setDefaultLanguage(id: number) {
  return request({
    url: '/admin/languages/default',
    method: 'put',
    data: { languageId: id },
  })
}

/**
 * 删除语言（谨慎使用）
 * @param id 语言ID
 */
export function deleteLanguage(id: number) {
  return request({
    url: `/admin/languages/${id}`,
    method: 'delete',
  })
}

/**
 * 更新语言排序
 * @param data 排序数据 [{id, sortOrder}]
 */
export function updateLanguageSort(data: { id: number; sortOrder: number }[]) {
  return request({
    url: '/admin/languages/sort',
    method: 'put',
    data,
  })
}

// ==================== 语言包管理API ====================

/**
 * 获取语言包列表（按语言）
 * @param params 查询参数
 */
export function getTranslationBundleList(params?: {
  baseLocale?: string
  page?: number
  pageSize?: number
}) {
  return request({
    url: '/admin/translations/bundles',
    method: 'get',
    params,
  })
}

/**
 * 下载语言包
 * @param locale 语言代码
 * @param options 下载选项
 */
export function downloadTranslationBundle(
  locale: string,
  options: {
    type: 'full' | 'missing'
    format: 'json' | 'excel'
    namespaces?: string[]
  }
) {
  return request({
    url: `/admin/languages/${locale}/bundle`,
    method: 'get',
    params: options,
    responseType: 'blob',
  })
}

/**
 * 上传语言包（校验）
 * @param locale 语言代码
 * @param file 文件
 * @param options 导入选项
 */
export function validateTranslationBundle(
  locale: string,
  file: File,
  options: {
    strategy: 'overwrite' | 'add_only' | 'replace'
    version?: string
  }
) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('strategy', options.strategy)
  if (options.version) {
    formData.append('version', options.version)
  }

  return request({
    url: `/admin/languages/${locale}/bundle/validate`,
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })
}

/**
 * 确认上传语言包
 * @param locale 语言代码
 * @param validationId 校验ID
 * @param remarks 备注说明
 */
export function confirmUploadTranslationBundle(
  locale: string,
  validationId: string,
  remarks?: string
) {
  return request({
    url: `/admin/languages/${locale}/bundle/confirm`,
    method: 'post',
    data: { validationId, remarks },
  })
}

/**
 * 获取语言包版本历史
 * @param locale 语言代码
 * @param params 查询参数
 */
export function getTranslationVersionHistory(
  locale: string,
  params?: {
    page?: number
    pageSize?: number
  }
) {
  return request({
    url: `/admin/languages/${locale}/versions`,
    method: 'get',
    params,
  })
}

/**
 * 下载指定版本的语言包
 * @param locale 语言代码
 * @param version 版本号
 */
export function downloadTranslationVersion(locale: string, version: string) {
  return request({
    url: `/admin/languages/${locale}/versions/${version}`,
    method: 'get',
    responseType: 'blob',
  })
}

/**
 * 回滚到指定版本
 * @param locale 语言代码
 * @param version 版本号
 */
export function rollbackTranslationVersion(locale: string, version: string) {
  return request({
    url: `/admin/languages/${locale}/versions/${version}/rollback`,
    method: 'post',
  })
}

// ==================== 翻译统计API ====================

/**
 * 获取翻译统计概览
 */
export function getTranslationStatistics() {
  return request({
    url: '/admin/translations/statistics',
    method: 'get',
  })
}

/**
 * 获取命名空间统计
 * @param baseLocale 基准语言
 */
export function getNamespaceStatistics(baseLocale?: string) {
  return request({
    url: '/admin/translations/statistics/namespaces',
    method: 'get',
    params: { baseLocale },
  })
}

/**
 * 获取缺失翻译列表
 * @param locale 语言代码
 * @param params 查询参数
 */
export function getMissingTranslations(
  locale: string,
  params?: {
    namespace?: string
    priority?: 'P0' | 'P1' | 'P2' | 'all'
    keyword?: string
    page?: number
    pageSize?: number
  }
) {
  return request({
    url: `/admin/translations/missing`,
    method: 'get',
    params: { locale, ...params },
  })
}

/**
 * 导出缺失翻译列表
 * @param locale 语言代码
 * @param format 导出格式
 */
export function exportMissingTranslations(
  locale: string,
  format: 'csv' | 'excel'
) {
  return request({
    url: `/admin/translations/missing/export`,
    method: 'get',
    params: { locale, format },
    responseType: 'blob',
  })
}

/**
 * 获取翻译质量问题
 * @param locale 语言代码
 * @param params 查询参数
 */
export function getQualityIssues(
  locale: string,
  params?: {
    type?: string
    page?: number
    pageSize?: number
  }
) {
  return request({
    url: `/admin/translations/quality-issues`,
    method: 'get',
    params: { locale, ...params },
  })
}

/**
 * 重新检测翻译质量
 * @param locale 语言代码
 */
export function recheckQuality(locale: string) {
  return request({
    url: `/admin/translations/quality-issues/recheck`,
    method: 'post',
    data: { locale },
  })
}

// ==================== 前端公共API ====================

/**
 * 获取可用语言列表（前端用）
 */
export function getAvailableLanguages() {
  return request({
    url: '/i18n/languages',
    method: 'get',
  })
}

/**
 * 获取语言包（前端用）
 * @param locale 语言代码
 */
export function getLanguageBundle(locale: string) {
  return request({
    url: `/i18n/locales/${locale}`,
    method: 'get',
  })
}

/**
 * 保存用户语言偏好
 * @param locale 语言代码
 */
export function saveUserLanguagePreference(locale: string) {
  return request({
    url: '/user/language',
    method: 'put',
    data: { locale },
  })
}

/**
 * 获取用户语言偏好
 */
export function getUserLanguagePreference() {
  return request({
    url: '/user/language',
    method: 'get',
  })
}

