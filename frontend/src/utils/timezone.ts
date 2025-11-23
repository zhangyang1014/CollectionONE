/**
 * 时区工具函数
 */

/**
 * 根据UTC偏移量获取IANA时区名称
 * @param offset UTC偏移量（如：-6表示UTC-6，5表示UTC+5:30）
 * @returns IANA时区名称
 */
export function getTimezoneByOffset(offset: number): string {
  // 常见时区映射
  const timezoneMap: Record<number, string> = {
    '-12': 'Etc/GMT+12',
    '-11': 'Pacific/Midway',
    '-10': 'Pacific/Honolulu',
    '-9': 'America/Anchorage',
    '-8': 'America/Los_Angeles',
    '-7': 'America/Denver',
    '-6': 'America/Mexico_City', // 墨西哥
    '-5': 'America/New_York',
    '-4': 'America/Caracas',
    '-3': 'America/Sao_Paulo',
    '-2': 'Atlantic/South_Georgia',
    '-1': 'Atlantic/Azores',
    '0': 'UTC',
    '1': 'Europe/Paris',
    '2': 'Africa/Cairo',
    '3': 'Europe/Moscow',
    '4': 'Asia/Dubai',
    '5': 'Asia/Kolkata', // 印度（UTC+5:30，这里用5表示）
    '6': 'Asia/Dhaka',
    '7': 'Asia/Bangkok',
    '8': 'Asia/Shanghai',
    '9': 'Asia/Tokyo',
    '10': 'Australia/Sydney',
    '11': 'Pacific/Auckland',
    '12': 'Pacific/Fiji',
    '13': 'Pacific/Tongatapu',
    '14': 'Pacific/Kiritimati'
  }
  
  return timezoneMap[offset] || 'UTC'
}

/**
 * 根据IANA时区名称获取时区缩写
 * @param timezone IANA时区名称
 * @returns 时区缩写
 */
export function getTimezoneShort(timezone: string): string {
  const timezoneShortMap: Record<string, string> = {
    'America/Mexico_City': 'CST',
    'Asia/Kolkata': 'IST',
    'America/Los_Angeles': 'PST',
    'America/New_York': 'EST',
    'Asia/Shanghai': 'CST',
    'Europe/London': 'GMT',
    'UTC': 'UTC'
  }
  
  return timezoneShortMap[timezone] || timezone.split('/').pop()?.substring(0, 3).toUpperCase() || 'UTC'
}

/**
 * 根据tenantId获取默认时区信息
 * @param tenantId 甲方ID
 * @returns 时区信息对象
 */
export function getTimezoneByTenantId(tenantId: number | string): {
  timezone: string
  timezoneShort: string
  timezoneOffset: number
} {
  const id = typeof tenantId === 'string' ? parseInt(tenantId) : tenantId
  
  if (id === 1) {
    // BTQ（墨西哥）
    return {
      timezone: 'America/Mexico_City',
      timezoneShort: 'CST',
      timezoneOffset: -6
    }
  } else if (id === 2) {
    // BTSK（印度）
    return {
      timezone: 'Asia/Kolkata',
      timezoneShort: 'IST',
      timezoneOffset: 5 // UTC+5:30，这里用5表示
    }
  }
  
  // 默认返回UTC
  return {
    timezone: 'UTC',
    timezoneShort: 'UTC',
    timezoneOffset: 0
  }
}

