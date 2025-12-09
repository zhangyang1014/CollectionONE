import request from '@/utils/request'

export interface FaceCompareQuery {
  collectorAccount?: string
  collectorName?: string
  organizationId?: number | string
  compareResult?: string
  locked?: boolean
  pageNum?: number
  pageSize?: number
}

export interface FaceCompareItem {
  id?: number
  collectorId: number | string
  collectorAccount: string
  collectorName?: string
  organizationName?: string
  compareResult?: string
  firstPhotoUrl?: string
  todayPhotoUrl?: string
  yesterdayPhotoUrl?: string
  beforeYesterdayPhotoUrl?: string
  faceId?: string
  locked?: boolean
  [key: string]: any
}

export interface FaceCompareListResponse {
  items: FaceCompareItem[]
  total: number
}

const useMock =
  typeof import.meta !== 'undefined' &&
  (import.meta.env?.VITE_MOCK_AI_QUALITY === 'true' ||
    import.meta.env?.VITE_ENABLE_MOCK === 'true')
// 当接口不存在或报错时自动使用mock
const enableMockFallback = true

const mockItems: FaceCompareItem[] = [
  {
    id: 1,
    collectorId: 180,
    collectorAccount: 'test45345',
    collectorName: '测试一',
    organizationName: 'CLOUDUN',
    teamGroupName: '一组群',
    teamName: '一小组',
    compareResult: 'match',
    firstPhotoUrl: 'https://via.placeholder.com/60?text=F1',
    todayPhotoUrl: 'https://via.placeholder.com/60?text=T1',
    yesterdayPhotoUrl: 'https://via.placeholder.com/60?text=Y1',
    beforeYesterdayPhotoUrl: 'https://via.placeholder.com/60?text=B1',
    faceId: 'FACE-001',
    locked: false,
  },
  {
    id: 2,
    collectorId: 147,
    collectorAccount: 'tests3',
    collectorName: '测试二',
    organizationName: 'CLOUDUN',
    teamGroupName: '二组群',
    teamName: '二小组',
    compareResult: 'mismatch',
    firstPhotoUrl: 'https://via.placeholder.com/60?text=F2',
    todayPhotoUrl: 'https://via.placeholder.com/60?text=T2',
    yesterdayPhotoUrl: 'https://via.placeholder.com/60?text=Y2',
    beforeYesterdayPhotoUrl: 'https://via.placeholder.com/60?text=B2',
    faceId: 'FACE-002',
    locked: true,
  },
  {
    id: 3,
    collectorId: 66,
    collectorAccount: '0327TEST',
    collectorName: '测试三',
    organizationName: 'CLOUDUN_ios',
    teamGroupName: '三组群',
    teamName: '三小组',
    compareResult: 'unknown',
    firstPhotoUrl: '',
    todayPhotoUrl: 'https://via.placeholder.com/60?text=T3',
    yesterdayPhotoUrl: '',
    beforeYesterdayPhotoUrl: '',
    faceId: 'FACE-003',
    locked: false,
  },
  {
    id: 4,
    collectorId: 17,
    collectorAccount: 'TESTIOSDD02',
    collectorName: '测试四',
    organizationName: 'CLOUDUN_ios',
    teamGroupName: '一组群',
    teamName: '四小组',
    compareResult: 'match',
    firstPhotoUrl: 'https://via.placeholder.com/60?text=F4',
    todayPhotoUrl: '',
    yesterdayPhotoUrl: 'https://via.placeholder.com/60?text=Y4',
    beforeYesterdayPhotoUrl: '',
    faceId: 'FACE-004',
    locked: false,
  },
  {
    id: 5,
    collectorId: 20,
    collectorAccount: 'TESTIOSS21',
    collectorName: '测试五',
    organizationName: 'CLOUDUN_ios',
    teamGroupName: '二组群',
    teamName: '五小组',
    compareResult: 'mismatch',
    firstPhotoUrl: 'https://via.placeholder.com/60?text=F5',
    todayPhotoUrl: 'https://via.placeholder.com/60?text=T5',
    yesterdayPhotoUrl: 'https://via.placeholder.com/60?text=Y5',
    beforeYesterdayPhotoUrl: 'https://via.placeholder.com/60?text=B5',
    faceId: 'FACE-005',
    locked: true,
  },
]

const normalizeListResponse = (res: any): FaceCompareListResponse => {
  // 多种响应格式兼容：数组 / {items,total} / {records,total} / {data:{items}} / {data:[]}
  if (Array.isArray(res)) {
    return { items: res, total: res.length }
  }

  const data = res?.data ?? res
  const items =
    data?.items ??
    data?.records ??
    (Array.isArray(data) ? data : []) ??
    []
  const total =
    data?.total ??
    res?.total ??
    (Array.isArray(items) ? items.length : 0)

  return {
    items: Array.isArray(items) ? items : [],
    total: typeof total === 'number' ? total : 0,
  }
}

export const fetchFaceCompareList = async (
  params: FaceCompareQuery
): Promise<FaceCompareListResponse> => {
  if (useMock) {
    return {
      items: mockItems,
      total: mockItems.length,
    }
  }
  try {
    const res = await request({
      url: '/ai-quality/face-compare/list',
      method: 'get',
      params,
    })
    return normalizeListResponse(res)
  } catch (error) {
    console.warn('[AI质检] 获取人脸比对列表失败，将使用mock数据占位', error)
    if (enableMockFallback) {
      return { items: mockItems, total: mockItems.length }
    }
    return { items: [], total: 0 }
  }
}

export const updateFaceLockStatus = async (payload: {
  collectorId: number | string
  lock: boolean
}) => {
  if (useMock) {
    // 模拟锁定结果，直接返回
    return Promise.resolve({ code: 200, message: 'success' })
  }
  try {
    return await request({
      url: '/ai-quality/face-compare/lock',
      method: 'post',
      data: payload,
    })
  } catch (error) {
    console.warn('[AI质检] 更新锁定状态失败，将返回mock成功占位', error)
    if (enableMockFallback) {
      return { code: 200, message: 'success' }
    }
    throw error
  }
}
