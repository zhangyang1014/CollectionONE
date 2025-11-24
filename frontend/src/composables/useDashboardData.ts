import { ref, computed } from 'vue'
import dayjs from 'dayjs'

/**
 * 核心数据看板数据管理
 */
export interface DashboardData {
  teamRank: number
  totalCases: number
  unpaidCases: number
  caseRecoveryRate: number
  amountRank: number
  totalAmount: number
  unpaidAmount: number
  amountRecoveryRate: number
}

/**
 * 使用核心数据看板的组合式函数
 */
export function useDashboardData() {
  // 看板数据
  const dashboardData = ref<DashboardData>({
    teamRank: 2,
    totalCases: 28,
    unpaidCases: 15,
    caseRecoveryRate: 46.4,
    amountRank: 3,
    totalAmount: 245000,
    unpaidAmount: 131250,
    amountRecoveryRate: 46.4
  })

  // 上次刷新时间
  const lastRefreshTime = ref('')

  /**
   * 刷新看板数据
   */
  const refreshDashboard = () => {
    lastRefreshTime.value = dayjs().format('HH:mm:ss')
    // TODO: 这里可以添加实际的API调用来获取最新数据
    // 目前使用Mock数据
    return Promise.resolve()
  }

  /**
   * 格式化货币金额
   */
  const formatCurrency = (amount: number) => {
    if (!amount) return '0'
    return Math.round(amount).toLocaleString('zh-CN')
  }

  /**
   * 初始化刷新时间
   */
  const initRefreshTime = () => {
    lastRefreshTime.value = dayjs().format('HH:mm:ss')
  }

  return {
    dashboardData,
    lastRefreshTime,
    refreshDashboard,
    formatCurrency,
    initRefreshTime
  }
}





