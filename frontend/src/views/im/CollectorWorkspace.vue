<template>
  <div class="collector-workspace">
    <!-- 顶部条 -->
    <div class="workspace-header">
      <div class="header-left">
        <el-icon :size="28" class="logo-icon"><ChatDotRound /></el-icon>
        <span class="system-name">催收ONE</span>
      </div>

      <div class="header-center">
        <!-- 中间区域可以放置其他内容 -->
      </div>

      <div class="header-right">
        <!-- 通知信息 -->
        <div class="notification-center" @mouseenter="handleNotificationMouseEnter" @mouseleave="handleNotificationMouseLeave">
          <!-- 未读通知轮播 -->
          <div v-if="unreadNotifications > 0" class="notification-carousel" @click="showNotificationPanel = true">
            <div class="carousel-content">
              <el-icon class="carousel-icon"><Bell /></el-icon>
              <div class="carousel-text">
                <span class="carousel-message">{{ currentCarouselNotification?.content }}</span>
              </div>
              <div class="carousel-indicator">
                <span>{{ carouselIndex + 1 }}/{{ unreadNotificationsList.length }}</span>
              </div>
            </div>
          </div>
          
          <el-badge :value="unreadNotifications" :hidden="unreadNotifications === 0">
            <el-button :icon="Bell" circle />
          </el-badge>
          
          <!-- 通知面板 -->
          <div v-if="showNotificationPanel" class="notification-panel" @mouseenter="handleNotificationPanelEnter" @mouseleave="handleNotificationMouseLeave">
            <!-- 面板头部 -->
            <div class="notification-panel-header">
              <div class="panel-title">
                <span>通知</span>
                <el-badge :value="unreadNotifications" :hidden="unreadNotifications === 0" class="header-badge" />
              </div>
              <el-button text size="small" @click="markAllAsRead" :disabled="unreadNotifications === 0">
                全部已读
              </el-button>
            </div>

            <!-- 分类筛选 -->
            <div class="notification-filters">
              <el-scrollbar>
                <el-radio-group v-model="notificationFilter" size="small" class="filter-group">
                  <el-radio-button label="all">全部</el-radio-button>
                  <el-radio-button label="unreplied">案件有待回复信息</el-radio-button>
                  <el-radio-button label="nudge">催办机制</el-radio-button>
                  <el-radio-button label="case_update">案件信息更新</el-radio-button>
                  <el-radio-button label="performance">组织绩效通知</el-radio-button>
                  <el-radio-button label="timeout">长时间未响应</el-radio-button>
                </el-radio-group>
              </el-scrollbar>
            </div>

            <!-- 通知列表 -->
            <div class="notification-list">
              <el-scrollbar max-height="500px">
                <div v-if="filteredNotifications.length === 0" class="empty-notifications">
                  <el-empty description="暂无通知" :image-size="80" />
                </div>
                <div v-for="notification in filteredNotifications" :key="notification.id" 
                     class="notification-item" 
                     :class="{ unread: !notification.is_read }"
                     @click="handleNotificationClick(notification)">
                  <div class="notification-content">
                    <div class="notification-header">
                      <span class="notification-time">{{ formatNotificationTime(notification.created_at) }}</span>
                      <el-tag :type="getNotificationTypeTag(notification.type)" size="small">
                        {{ getNotificationTypeLabel(notification.type) }}
                      </el-tag>
                    </div>
                    <div class="notification-body">
                      <div class="notification-text">{{ notification.content }}</div>
                      <div v-if="notification.case_id" class="notification-case">
                        案件：{{ notification.case_id }}
                      </div>
                    </div>
                  </div>
                  <div class="notification-actions" @click.stop>
                    <el-button 
                      v-if="!notification.is_read" 
                      text 
                      size="small" 
                      type="primary"
                      @click="markAsRead(notification)"
                    >
                      已读
                    </el-button>
                    <el-button 
                      text 
                      size="small" 
                      type="primary"
                      @click="viewNotification(notification)"
                    >
                      查看
                    </el-button>
                  </div>
                </div>
              </el-scrollbar>
            </div>
          </div>
        </div>

        <!-- 时区显示 -->
        <div class="timezone-display">
          <el-icon><Clock /></el-icon>
          <span>{{ currentTime }}</span>
          <el-tooltip :content="`时区: ${currentTimezone}`" placement="bottom">
            <span class="timezone-label">{{ timezoneShort }}</span>
          </el-tooltip>
        </div>

        <!-- 语言切换 -->
        <el-dropdown @command="handleLanguageChange">
          <div class="language-selector">
            <el-icon><Grid /></el-icon>
            <span>{{ currentLanguage }}</span>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="zh-CN">
                <span class="flag">🇨🇳</span> 中文
              </el-dropdown-item>
              <el-dropdown-item command="en-US">
                <span class="flag">🇺🇸</span> English
              </el-dropdown-item>
              <el-dropdown-item command="es-MX">
                <span class="flag">🇲🇽</span> Español
              </el-dropdown-item>
              <el-dropdown-item command="id-ID">
                <span class="flag">🇮🇩</span> Indonesia
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>

        <!-- 账号信息 -->
        <el-dropdown @command="handleAccountCommand" trigger="hover">
          <div class="account-info">
            <el-avatar :size="32">{{ user?.collectorId?.[0] }}</el-avatar>
            <span class="collector-id">{{ user?.collectorId }}</span>
            <el-icon><ArrowDown /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item disabled>
                <div class="account-details">
                  <div><strong>{{ user?.collectorName }}</strong></div>
                  <div class="text-secondary">{{ user?.tenantName }}</div>
                  <div class="text-secondary">{{ user?.team }}</div>
                </div>
              </el-dropdown-item>
              <el-dropdown-item divided>
                <el-icon><Connection /></el-icon>
                WhatsApp: {{ user?.whatsappConnected ? '已连接' : '未连接' }}
              </el-dropdown-item>
              <el-dropdown-item divided command="logout-whatsapp" v-if="user?.whatsappConnected">
                <el-icon><SwitchButton /></el-icon>
                登出 WhatsApp 账号
              </el-dropdown-item>
              <el-dropdown-item command="logout-all">
                <el-icon style="color: #F56C6C"><SwitchButton /></el-icon>
                <span style="color: #F56C6C">登出催收账号</span>
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <!-- 主工作区 -->
    <div class="workspace-main">
      <!-- 左侧：案件列表 -->
      <div class="case-list-section" :style="{ width: `${leftPanelWidth}px` }">
        <!-- 看板数据 -->
        <div class="dashboard-card">
          <div class="dashboard-header">
            <h3>核心数据</h3>
            <div class="dashboard-actions">
              <span class="refresh-time">上次刷新: {{ lastRefreshTime }}</span>
              <el-button text @click="refreshDashboard">
                <el-icon><Refresh /></el-icon>
              </el-button>
              <el-button text type="primary" @click="viewMoreReports">
                更多数据 <el-icon><Right /></el-icon>
              </el-button>
            </div>
          </div>
          
          <div class="dashboard-metrics">
            <!-- 排名块 -->
            <div class="metric-block">
              <div class="block-title">排名</div>
              <div class="block-dimensions">
                <div class="dimension-item">
                  <div class="dimension-value primary">{{ dashboardData.teamRank }}</div>
                </div>
                <div class="dimension-item">
                  <div class="dimension-value primary">{{ dashboardData.amountRank }}</div>
                </div>
              </div>
            </div>

            <!-- 应催块 -->
            <div class="metric-block">
              <div class="block-title">应催</div>
              <div class="block-dimensions">
                <div class="dimension-item">
                  <div class="dimension-value">{{ dashboardData.totalCases }}</div>
                </div>
                <div class="dimension-item">
                  <div class="dimension-value">{{ formatCurrency(dashboardData.totalAmount) }}</div>
                </div>
              </div>
            </div>

            <!-- 未还块 -->
            <div class="metric-block">
              <div class="block-title">未还</div>
              <div class="block-dimensions">
                <div class="dimension-item">
                  <div class="dimension-value warning">{{ dashboardData.unpaidCases }}</div>
                </div>
                <div class="dimension-item">
                  <div class="dimension-value warning">{{ formatCurrency(dashboardData.unpaidAmount) }}</div>
                </div>
              </div>
            </div>

            <!-- 回收率块 -->
            <div class="metric-block">
              <div class="block-title">回收率</div>
              <div class="block-dimensions">
                <div class="dimension-item">
                  <div class="dimension-value success">{{ dashboardData.caseRecoveryRate }}%</div>
                </div>
                <div class="dimension-item">
                  <div class="dimension-value success">{{ dashboardData.amountRecoveryRate }}%</div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 搜索框 -->
        <div class="search-box">
          <el-input
            v-model="searchKeyword"
            placeholder="输入精准用户编号、贷款编号、手机号进行搜索"
            clearable
            @blur="handleSearch"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>

        <!-- 过滤器 -->
        <div class="filters-section">
          <div class="filters-compact">
            <!-- 第一行：主要筛选（默认显示4个） -->
            <div class="filter-row">
              <div class="filter-item">
                <el-select v-model="filters.caseStatus" placeholder="案件状态" size="small" clearable multiple collapse-tags collapse-tags-tooltip :max-collapse-tags="1" @change="handleFilterChange">
                  <el-option label="全部" value="" />
                  <el-option label="待催收" value="pending" />
                  <el-option label="新入催" value="new" />
                  <el-option label="催收中" value="collecting" />
                  <el-option label="承诺还款" value="promised" />
                  <el-option label="已结清" value="settled" />
                </el-select>
              </div>
              <div class="filter-item">
                <el-select v-model="filters.paymentIntention" placeholder="还款意向" size="small" clearable multiple collapse-tags collapse-tags-tooltip :max-collapse-tags="1" @change="handleFilterChange">
                  <el-option label="全部" value="" />
                  <el-option label="已还款" value="claimed_paid" />
                  <el-option label="承诺今日" value="promise_today" />
                  <el-option label="拒绝" value="refused" />
                  <el-option label="有意愿" value="willing" />
                </el-select>
              </div>
              <div class="filter-item">
                <el-select v-model="filters.userReply" placeholder="用户回复" size="small" clearable @change="handleFilterChange">
                  <el-option label="全部" value="" />
                  <el-option label="无回复" value="none" />
                  <el-option label="有回复" value="has" />
                </el-select>
              </div>
              <div class="filter-item">
                <el-select v-model="filters.hasPTP" placeholder="PTP" size="small" clearable @change="handleFilterChange">
                  <el-option label="全部" value="" />
                  <el-option label="无" value="none" />
                  <el-option label="今日" value="today" />
                  <el-option label="1-3日" value="1-3days" />
                  <el-option label="3+" value="3plus" />
                </el-select>
              </div>
            </div>

            <!-- 更多筛选 -->
            <div v-if="showMoreFilters" class="filter-row filter-row-more">
              <div class="filter-item">
                <el-select v-model="filters.borrowType" placeholder="首复借" size="small" clearable @change="handleFilterChange">
                  <el-option label="全部" value="" />
                  <el-option label="新客" value="new_customer" />
                  <el-option label="复借" value="repeat" />
                  <el-option label="老转新" value="renewal" />
                </el-select>
              </div>
              <div class="filter-item">
                <el-select v-model="filters.recentPayment" placeholder="近期还款" size="small" clearable @change="handleFilterChange">
                  <el-option label="全部" value="" />
                  <el-option label="无" value="none" />
                  <el-option label="1天内" value="1day" />
                  <el-option label="2天内" value="2days" />
                  <el-option label="3天内" value="3days" />
                  <el-option label="7天内" value="7days" />
                </el-select>
              </div>
              <div class="filter-item">
                <el-select v-model="filters.product" placeholder="产品" size="small" clearable @change="handleFilterChange">
                  <el-option label="全部" value="" />
                  <el-option v-for="product in productList" :key="product" :label="product" :value="product" />
                </el-select>
              </div>
              <div class="filter-item">
                <el-select v-model="filters.app" placeholder="App" size="small" clearable @change="handleFilterChange">
                  <el-option label="全部" value="" />
                  <el-option v-for="app in appList" :key="app" :label="app" :value="app" />
                </el-select>
              </div>
              <div class="filter-item">
                <el-select v-model="filters.firstTerm" placeholder="首期期限" size="small" clearable @change="handleFilterChange">
                  <el-option label="全部" value="" />
                  <el-option label="7天" value="7" />
                  <el-option label="14天" value="14" />
                </el-select>
              </div>
              <div class="filter-item filter-item-date">
                <el-date-picker
                  v-model="filters.paymentDateRange"
                  type="daterange"
                  range-separator="-"
                  start-placeholder="还款开始"
                  end-placeholder="还款结束"
                  size="small"
                  @change="handleFilterChange"
                />
              </div>
            </div>

            <!-- 操作按钮行 -->
            <div class="filter-actions">
              <el-button size="small" @click="showMoreFilters = !showMoreFilters">
                <el-icon><ArrowUp v-if="showMoreFilters" /><ArrowDown v-else /></el-icon>
                {{ showMoreFilters ? '收起' : '更多' }}筛选
              </el-button>
              <el-button-group size="small">
                <el-button @click="handleSaveView">
                  <el-icon><FolderAdd /></el-icon>
                  保存视图
                </el-button>
                <el-button @click="handleResetFilters">
                  <el-icon><RefreshLeft /></el-icon>
                  重置
                </el-button>
              </el-button-group>
            </div>
          </div>
        </div>

        <!-- 案件列表 -->
        <div class="case-list-table">
          <div class="list-header">
            <el-button-group v-if="selectedCases.length > 0" size="small">
              <el-button type="primary" @click="handleBulkCall">
                <el-icon><Phone /></el-icon>
                批量外呼 ({{ selectedCases.length }})
              </el-button>
              <el-button type="success" @click="handleBulkMessage">
                <el-icon><ChatDotRound /></el-icon>
                批量发送消息
              </el-button>
            </el-button-group>
          </div>

          <el-table
            :data="paginatedCases"
            :height="tableHeight"
            @selection-change="handleSelectionChange"
            @row-click="handleRowClick"
            :row-class-name="getRowClassName"
            highlight-current-row
          >
            <el-table-column type="selection" width="45" />
            <el-table-column prop="loan_id" label="贷款编号" width="100">
              <template #default="{ row }">
                <div class="loan-id-cell">
                  <span>{{ row.loan_id || '-' }}</span>
                  <span 
                    v-if="row.loan_id && hasUnreadMessagesForLoan(row.loan_id)" 
                    class="case-unread-dot"
                  ></span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="user_name" label="用户名" width="100" show-overflow-tooltip>
              <template #default="{ row }">
                <div class="user-name-cell">
                  <span class="user-name">{{ row.user_name }}</span>
                  <span class="user-id">{{ row.user_id }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="case_status" label="状态" width="80" sortable>
              <template #default="{ row }">
                <el-tag size="small">{{ row.case_status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="contact_channels" label="应答渠道" width="80" sortable align="center">
              <template #default="{ row }">
                <el-badge :value="row.contact_channels || 0" type="primary" />
              </template>
            </el-table-column>
            <el-table-column prop="outstanding_amount" label="未还金额" width="95" sortable>
              <template #default="{ row }">
                <span class="amount">{{ formatCurrency(row.outstanding_amount) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="total_due_amount" label="应还金额" width="95" sortable>
              <template #default="{ row }">
                <span class="amount">{{ formatCurrency(row.total_due_amount) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="overdue_days" label="逾期天数" width="85" sortable>
              <template #default="{ row }">
                <el-tag :type="getOverdueType(row.overdue_days)" size="small">
                  {{ row.overdue_days }}天
                </el-tag>
              </template>
            </el-table-column>

            <!-- 更多字段（可展开） -->
            <el-table-column v-if="showMoreFields" prop="product_name" label="产品" width="110" />
            <el-table-column v-if="showMoreFields" prop="app_name" label="App" width="110" />
            <el-table-column v-if="showMoreFields" prop="settlement_method" label="结清方式" width="95" />
            <el-table-column v-if="showMoreFields" prop="settlement_time" label="结清时间" width="150" />
          </el-table>

          <!-- 分页器 -->
          <div class="pagination-bar">
            <div class="pagination-left">
              <el-checkbox v-model="selectAll" @change="handleSelectAll">全选</el-checkbox>
              <span class="case-count">共 {{ filteredCases.length }} 个案件</span>
            </div>
            <el-pagination
              v-model:current-page="pagination.page"
              v-model:page-size="pagination.pageSize"
              :page-sizes="[30, 50, 100]"
              layout="total, sizes, prev, pager, next"
              :total="filteredCases.length"
            />
          </div>
        </div>
      </div>

      <!-- 可拖动分割线 -->
      <el-tooltip 
        content="拖动调整宽度 · 双击恢复默认" 
        placement="right"
        :show-after="500"
      >
        <div 
          class="resizer" 
          @mousedown="startResize"
          @dblclick="resetPanelWidth"
        >
          <div class="resizer-handle"></div>
        </div>
      </el-tooltip>

      <!-- 右侧：案件详情 + IM -->
      <div class="detail-section">
        <div v-if="selectedCase" class="unified-panel">
          <!-- 上方：案件详情 -->
          <div class="case-detail-panel" :style="{ height: `${topPanelHeight}px` }">
            <CaseDetail 
              :caseData="selectedCase" 
              :fullData="mockFullCaseData" 
            />
          </div>

          <!-- 可拖动水平分割线 -->
          <el-tooltip 
            content="拖动调整高度 · 双击恢复默认" 
            placement="bottom"
            :show-after="500"
          >
            <div 
              class="horizontal-resizer" 
              @mousedown="startHorizontalResize"
              @dblclick="resetPanelHeight"
            >
              <div class="horizontal-resizer-handle"></div>
            </div>
          </el-tooltip>

          <!-- 下方：IM面板 -->
          <div class="im-panel-container" :style="{ height: `calc(100% - ${topPanelHeight}px - 8px)` }">
            <IMPanel ref="imPanelRef" :caseData="selectedCase" />
          </div>
        </div>
        <div v-else class="no-case-selected">
          <el-icon :size="48" color="#909399"><ChatDotRound /></el-icon>
          <p>请从左侧列表选择案件</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ChatDotRound,
  Bell,
  Clock,
  Grid,
  ArrowDown,
  ArrowUp,
  Connection,
  SwitchButton,
  Refresh,
  Right,
  Search,
  FolderAdd,
  RefreshLeft,
  Phone
} from '@element-plus/icons-vue'
import { useImUserStore } from '@/stores/imUser'
import { getCases } from '@/api/case'
import CaseDetail from '@/components/CaseDetail.vue'
import IMPanel from '@/components/IMPanel.vue'
import type { Case } from '@/types'
import dayjs from 'dayjs'
import utc from 'dayjs/plugin/utc'
import timezonePlugin from 'dayjs/plugin/timezone'

dayjs.extend(utc)
dayjs.extend(timezonePlugin)

const router = useRouter()
const imUserStore = useImUserStore()
const user = computed(() => imUserStore.user)

// IMPanel引用
const imPanelRef = ref<any>(null)

// 时区和时间
const currentTimezone = ref('America/Mexico_City')
const timezoneShort = ref('CST')
const currentTime = ref('')

const updateTime = () => {
  currentTime.value = dayjs().tz(currentTimezone.value).format('HH:mm:ss')
}

// 语言
const currentLanguage = ref('中文')
const languageMap: Record<string, string> = {
  'zh-CN': '中文',
  'en-US': 'English',
  'es-MX': 'Español',
  'id-ID': 'Indonesia'
}

// 通知相关
const showNotificationPanel = ref(false)
const notificationFilter = ref('all')
const notificationPanelTimer = ref<number | null>(null)

// 通知类型定义
type NotificationType = 'unreplied' | 'nudge' | 'case_update' | 'performance' | 'timeout'

interface Notification {
  id: number
  type: NotificationType
  content: string
  case_id?: string
  contact_id?: number
  channel?: string
  is_read: boolean
  created_at: string
  metadata?: any // 存储额外信息，如PTP时间、跟进时间等
}

// 通知列表
const notifications = ref<Notification[]>([
  // 案件有待回复信息
  {
    id: 1,
    type: 'unreplied',
    content: '案件 BTSK-200100 的客户在 WhatsApp 渠道有未回复消息',
    case_id: 'BTSK-200100',
    contact_id: 1,
    channel: 'whatsapp',
    is_read: false,
    created_at: dayjs().subtract(2, 'hour').format('YYYY-MM-DD HH:mm:ss')
  },
  {
    id: 2,
    type: 'unreplied',
    content: '案件 BTSK-200101 的联系人"张三"在 SMS 渠道有未回复消息',
    case_id: 'BTSK-200101',
    contact_id: 2,
    channel: 'sms',
    is_read: false,
    created_at: dayjs().subtract(1, 'hour').format('YYYY-MM-DD HH:mm:ss')
  },
  // 催办机制
  {
    id: 3,
    type: 'nudge',
    content: '案件 BTSK-200100 的 PTP 时间已到，客户尚未还款',
    case_id: 'BTSK-200100',
    is_read: false,
    created_at: dayjs().subtract(30, 'minute').format('YYYY-MM-DD HH:mm:ss'),
    metadata: { nudge_type: 'ptp', ptp_time: dayjs().subtract(30, 'minute').format('YYYY-MM-DD HH:mm:ss') }
  },
  {
    id: 4,
    type: 'nudge',
    content: '案件 BTQ-300200 的下次跟进时间已到',
    case_id: 'BTQ-300200',
    is_read: false,
    created_at: dayjs().subtract(15, 'minute').format('YYYY-MM-DD HH:mm:ss'),
    metadata: { nudge_type: 'follow_up', follow_up_time: dayjs().subtract(15, 'minute').format('YYYY-MM-DD HH:mm:ss') }
  },
  // 案件信息更新
  {
    id: 5,
    type: 'case_update',
    content: '新案件分配：BTSK-200102',
    case_id: 'BTSK-200102',
    is_read: false,
    created_at: dayjs().subtract(3, 'hour').format('YYYY-MM-DD HH:mm:ss')
  },
  {
    id: 6,
    type: 'case_update',
    content: '案件 BTSK-200100 已收到还款 5,000',
    case_id: 'BTSK-200100',
    is_read: true,
    created_at: dayjs().subtract(4, 'hour').format('YYYY-MM-DD HH:mm:ss'),
    metadata: { update_type: 'payment', amount: 5000 }
  },
  {
    id: 7,
    type: 'case_update',
    content: '案件 BTQ-300200 的标签已更新',
    case_id: 'BTQ-300200',
    is_read: true,
    created_at: dayjs().subtract(5, 'hour').format('YYYY-MM-DD HH:mm:ss'),
    metadata: { update_type: 'tag' }
  },
  // 组织绩效通知
  {
    id: 8,
    type: 'performance',
    content: '恭喜【第一组】的【催员小王】催回金额 50,000',
    is_read: false,
    created_at: dayjs().subtract(6, 'hour').format('YYYY-MM-DD HH:mm:ss'),
    metadata: { team: '第一组', collector: '催员小王', amount: 50000 }
  },
  {
    id: 9,
    type: 'performance',
    content: '恭喜【第二组】的【催员小李】催回金额 30,000',
    is_read: true,
    created_at: dayjs().subtract(8, 'hour').format('YYYY-MM-DD HH:mm:ss'),
    metadata: { team: '第二组', collector: '催员小李', amount: 30000 }
  },
  // 长时间未响应提醒
  {
    id: 10,
    type: 'timeout',
    content: '案件 BTSK-200100 的客户消息超过 30 分钟未回复，请及时处理',
    case_id: 'BTSK-200100',
    contact_id: 1,
    channel: 'whatsapp',
    is_read: false,
    created_at: dayjs().subtract(35, 'minute').format('YYYY-MM-DD HH:mm:ss'),
    metadata: { timeout_minutes: 30, first_notify_time: dayjs().subtract(35, 'minute').format('YYYY-MM-DD HH:mm:ss') }
  },
  {
    id: 11,
    type: 'timeout',
    content: '案件 BTSK-200101 的客户消息超过 1 小时未回复，请及时处理',
    case_id: 'BTSK-200101',
    contact_id: 2,
    channel: 'sms',
    is_read: false,
    created_at: dayjs().subtract(65, 'minute').format('YYYY-MM-DD HH:mm:ss'),
    metadata: { timeout_minutes: 60, first_notify_time: dayjs().subtract(65, 'minute').format('YYYY-MM-DD HH:mm:ss') }
  }
])

// 未读通知数量
const unreadNotifications = computed(() => {
  return notifications.value.filter(n => !n.is_read).length
})

// 未读通知列表（用于轮播）
const unreadNotificationsList = computed(() => {
  return notifications.value
    .filter(n => !n.is_read)
    .sort((a, b) => dayjs(b.created_at).valueOf() - dayjs(a.created_at).valueOf())
})

// 轮播相关
const carouselIndex = ref(0)
const carouselTimer = ref<number | null>(null)
const carouselInterval = 5000 // 5秒切换一次

// 当前轮播通知
const currentCarouselNotification = computed(() => {
  if (unreadNotificationsList.value.length === 0) return null
  return unreadNotificationsList.value[carouselIndex.value]
})

// 启动轮播
const startCarousel = () => {
  stopCarousel()
  if (unreadNotificationsList.value.length <= 1) return
  
  carouselTimer.value = window.setInterval(() => {
    carouselIndex.value = (carouselIndex.value + 1) % unreadNotificationsList.value.length
  }, carouselInterval)
}

// 停止轮播
const stopCarousel = () => {
  if (carouselTimer.value) {
    clearInterval(carouselTimer.value)
    carouselTimer.value = null
  }
}

// 监听未读通知变化，重新启动轮播
watch(unreadNotificationsList, (newList) => {
  if (newList.length === 0) {
    stopCarousel()
    carouselIndex.value = 0
  } else {
    // 确保索引不越界
    if (carouselIndex.value >= newList.length) {
      carouselIndex.value = 0
    }
    startCarousel()
  }
}, { immediate: true })

// 筛选后的通知列表
const filteredNotifications = computed(() => {
  let result = notifications.value

  // 按分类筛选
  if (notificationFilter.value !== 'all') {
    result = result.filter(n => n.type === notificationFilter.value)
  }

  // 按时间倒序排列
  return result.sort((a, b) => dayjs(b.created_at).valueOf() - dayjs(a.created_at).valueOf())
})

// 格式化通知时间
const formatNotificationTime = (time: string) => {
  const now = dayjs()
  const notificationTime = dayjs(time)
  const diffMinutes = now.diff(notificationTime, 'minute')
  const diffHours = now.diff(notificationTime, 'hour')
  const diffDays = now.diff(notificationTime, 'day')

  if (diffMinutes < 1) return '刚刚'
  if (diffMinutes < 60) return `${diffMinutes}分钟前`
  if (diffHours < 24) return `${diffHours}小时前`
  if (diffDays < 7) return `${diffDays}天前`
  return notificationTime.format('YYYY-MM-DD HH:mm')
}

// 获取通知类型标签
const getNotificationTypeLabel = (type: NotificationType) => {
  const labels: Record<NotificationType, string> = {
    'unreplied': '案件有待回复信息',
    'nudge': '催办机制',
    'case_update': '案件信息更新',
    'performance': '组织绩效通知',
    'timeout': '长时间未响应'
  }
  return labels[type] || type
}

// 获取通知类型标签颜色
const getNotificationTypeTag = (type: NotificationType) => {
  const tags: Record<NotificationType, string> = {
    'unreplied': 'danger',
    'nudge': 'warning',
    'case_update': 'info',
    'performance': 'success',
    'timeout': 'danger'
  }
  return tags[type] || ''
}

// 处理通知中心鼠标进入
const handleNotificationMouseEnter = () => {
  // 清除关闭定时器
  if (notificationPanelTimer.value) {
    clearTimeout(notificationPanelTimer.value)
    notificationPanelTimer.value = null
  }
  showNotificationPanel.value = true
}

// 处理通知面板鼠标进入
const handleNotificationPanelEnter = () => {
  // 清除关闭定时器
  if (notificationPanelTimer.value) {
    clearTimeout(notificationPanelTimer.value)
    notificationPanelTimer.value = null
  }
}

// 处理通知面板鼠标离开
const handleNotificationMouseLeave = () => {
  // 延迟关闭，避免鼠标移动到面板时立即关闭
  notificationPanelTimer.value = window.setTimeout(() => {
    showNotificationPanel.value = false
    notificationPanelTimer.value = null
  }, 200)
}

// 标记单个通知为已读
const markAsRead = (notification: Notification) => {
  notification.is_read = true
  ElMessage.success('已标记为已读')
}

// 标记所有通知为已读
const markAllAsRead = () => {
  notifications.value.forEach(n => {
    n.is_read = true
  })
  ElMessage.success('已全部标记为已读')
}

// 查看通知
const viewNotification = (notification: Notification) => {
  if (notification.case_id) {
    // 找到对应的案件
    const targetCase = cases.value.find((c: any) => c.loan_id === notification.case_id)
    if (targetCase) {
      selectedCase.value = targetCase
      // 如果有关联的联系人和渠道，需要跳转到对应的聊天窗口
      if (notification.contact_id && notification.channel) {
        // 等待 IMPanel 组件更新后切换联系人和渠道
        nextTick(() => {
          if (imPanelRef.value && imPanelRef.value.switchToContactAndChannel) {
            imPanelRef.value.switchToContactAndChannel(notification.contact_id!, notification.channel!)
          }
        })
      }
      // 关闭通知面板
      showNotificationPanel.value = false
      ElMessage.success(`已跳转到案件 ${notification.case_id}`)
    } else {
      ElMessage.warning('未找到对应案件')
    }
  } else {
    ElMessage.info('该通知无关联案件')
  }
}

// 处理通知项点击
const handleNotificationClick = (notification: Notification) => {
  if (!notification.is_read) {
    markAsRead(notification)
  }
}

// 面板宽度调整
const leftPanelWidth = ref(600) // 默认600px
const isResizing = ref(false)
const minPanelWidth = 400 // 最小宽度
const maxPanelWidth = computed(() => window.innerWidth * 0.7) // 最大宽度为窗口宽度的70%

// 从localStorage恢复宽度
const savedWidth = localStorage.getItem('leftPanelWidth')
if (savedWidth) {
  leftPanelWidth.value = parseInt(savedWidth)
}

// 面板高度调整（水平分割线）
const topPanelHeight = ref(400) // 默认400px
const isHorizontalResizing = ref(false)
const minPanelHeight = 200 // 最小高度
const maxPanelHeight = computed(() => {
  // 最大高度为窗口高度减去顶部条(60px)和最小底部面板高度(200px)和分割线(8px)
  return window.innerHeight - 60 - 200 - 8
})

// 从localStorage恢复高度
const savedHeight = localStorage.getItem('topPanelHeight')
if (savedHeight) {
  const parsedHeight = parseInt(savedHeight)
  if (parsedHeight >= minPanelHeight && parsedHeight <= maxPanelHeight.value) {
    topPanelHeight.value = parsedHeight
  }
}

const startResize = (e: MouseEvent) => {
  e.preventDefault()
  isResizing.value = true
  document.body.style.cursor = 'col-resize'
  document.body.style.userSelect = 'none'
  
  // 添加一个遮罩层防止iframe等元素干扰鼠标事件
  const overlay = document.createElement('div')
  overlay.style.cssText = 'position: fixed; top: 0; left: 0; right: 0; bottom: 0; z-index: 9999; cursor: col-resize;'
  document.body.appendChild(overlay)
  
  const handleMouseMove = (e: MouseEvent) => {
    if (!isResizing.value) return
    
    const newWidth = e.clientX
    // 确保宽度在合理范围内
    if (newWidth >= minPanelWidth && newWidth <= maxPanelWidth.value) {
      leftPanelWidth.value = newWidth
    }
  }
  
  const handleMouseUp = () => {
    if (isResizing.value) {
      isResizing.value = false
      document.body.style.cursor = ''
      document.body.style.userSelect = ''
      
      // 移除遮罩层
      if (overlay && overlay.parentNode) {
        document.body.removeChild(overlay)
      }
      
      // 保存到localStorage
      localStorage.setItem('leftPanelWidth', leftPanelWidth.value.toString())
      
      document.removeEventListener('mousemove', handleMouseMove)
      document.removeEventListener('mouseup', handleMouseUp)
    }
  }
  
  document.addEventListener('mousemove', handleMouseMove)
  document.addEventListener('mouseup', handleMouseUp)
}

const resetPanelWidth = () => {
  leftPanelWidth.value = 600
  localStorage.setItem('leftPanelWidth', '600')
  ElMessage.success('已重置面板宽度')
}

const startHorizontalResize = (e: MouseEvent) => {
  e.preventDefault()
  e.stopPropagation()
  isHorizontalResizing.value = true
  document.body.style.cursor = 'row-resize'
  document.body.style.userSelect = 'none'
  
  // 添加一个遮罩层防止iframe等元素干扰鼠标事件
  const overlay = document.createElement('div')
  overlay.style.cssText = 'position: fixed; top: 0; left: 0; right: 0; bottom: 0; z-index: 9999; cursor: row-resize;'
  document.body.appendChild(overlay)
  
  const detailSection = document.querySelector('.detail-section') as HTMLElement
  const detailSectionRect = detailSection?.getBoundingClientRect()
  const detailSectionTop = detailSectionRect?.top || 0
  
  const handleMouseMove = (e: MouseEvent) => {
    if (!isHorizontalResizing.value) return
    
    const newHeight = e.clientY - detailSectionTop
    // 确保高度在合理范围内
    const maxHeight = detailSectionRect ? detailSectionRect.height - 200 - 8 : maxPanelHeight.value
    if (newHeight >= minPanelHeight && newHeight <= maxHeight) {
      topPanelHeight.value = newHeight
    } else if (newHeight < minPanelHeight) {
      topPanelHeight.value = minPanelHeight
    } else if (newHeight > maxHeight) {
      topPanelHeight.value = maxHeight
    }
  }
  
  const handleMouseUp = () => {
    if (isHorizontalResizing.value) {
      isHorizontalResizing.value = false
      document.body.style.cursor = ''
      document.body.style.userSelect = ''
      
      // 移除遮罩层
      if (overlay && overlay.parentNode) {
        document.body.removeChild(overlay)
      }
      
      // 保存到localStorage
      localStorage.setItem('topPanelHeight', topPanelHeight.value.toString())
      
      document.removeEventListener('mousemove', handleMouseMove)
      document.removeEventListener('mouseup', handleMouseUp)
    }
  }
  
  document.addEventListener('mousemove', handleMouseMove)
  document.addEventListener('mouseup', handleMouseUp)
}

const resetPanelHeight = () => {
  topPanelHeight.value = 400
  localStorage.setItem('topPanelHeight', '400')
  ElMessage.success('已重置面板高度')
}

// 看板数据
const dashboardData = ref({
  teamRank: 2,
  totalCases: 28,
  unpaidCases: 15,
  caseRecoveryRate: 46.4,
  amountRank: 3,
  totalAmount: 245000,
  unpaidAmount: 131250,
  amountRecoveryRate: 46.4
})
const lastRefreshTime = ref('')

// 搜索和过滤
const searchKeyword = ref('')
const showMoreFilters = ref(false)
const filters = ref({
  paymentDateRange: [],
  userReply: '',
  hasPTP: '',
  caseStatus: [],
  paymentIntention: [],
  product: '',
  app: '',
  borrowType: '',
  recentPayment: '',
  firstTerm: ''
})

const productList = ref(['Préstamo Rápido', 'Cash Express', 'Dinero Ya'])
const appList = ref(['PesoMex', 'DineroFácil', 'CashMexico'])

// 案件列表
const cases = ref<Case[]>([])
const selectedCases = ref<Case[]>([])
const selectAll = ref(false)
const showMoreFields = ref(false)
const selectedCase = ref<Case | null>(null)

// Mock完整案件数据（用于详情页展示）
const mockFullCaseData = computed(() => {
  if (!selectedCase.value) return {}
  
  const currentCase = selectedCase.value
  // 这里根据选中的案件生成mock完整数据
  const isMultiTerm = currentCase.custom_fields?.loan_term === '多期'
  
  return {
    customer_basic_info: {
      user_id: currentCase.user_id,
      user_name: currentCase.user_name,
      id_number: `CURP${Math.floor(Math.random() * 10000000000)}`,
      id_type: currentCase.tenant_id === 1 ? 'CURP' : 'Aadhaar',
      birth_date: '1990-05-15',
      gender: Math.random() > 0.5 ? '男' : '女',
      age: Math.floor(Math.random() * 20) + 25,
      mobile_number: currentCase.mobile_number,
      whatsapp_number: currentCase.mobile_number,
      email: `user${Math.floor(Math.random() * 1000)}@example.com`,
      state: currentCase.tenant_id === 1 ? 'Ciudad de México' : 'Maharashtra',
      city: currentCase.tenant_id === 1 ? 'México' : 'Mumbai',
      address: `Street ${Math.floor(Math.random() * 100)} #${Math.floor(Math.random() * 500)}`,
      postal_code: `${Math.floor(Math.random() * 90000) + 10000}`,
      education_level: ['高中', '大专', '本科'][Math.floor(Math.random() * 3)],
      employment_type: ['全职', '兼职', '自雇'][Math.floor(Math.random() * 3)],
      company_name: `Company ${Math.floor(Math.random() * 100)}`,
      monthly_income: Math.floor(Math.random() * 20000) + 10000,
      work_years: Math.floor(Math.random() * 10) + 1,
      credit_score: Math.floor(Math.random() * 250) + 550,
      total_loans: Math.floor(Math.random() * 5) + 1,
      overdue_times: Math.floor(Math.random() * 3),
      emergency_contact_name: '张三',
      emergency_contact_relation: ['配偶', '父亲', '母亲', '朋友'][Math.floor(Math.random() * 4)],
      emergency_contact_phone: currentCase.mobile_number,
    },
    document_images: {
      id_front_image: 'https://via.placeholder.com/400x300/4CAF50/ffffff?text=ID+Front',
      id_back_image: 'https://via.placeholder.com/400x300/2196F3/ffffff?text=ID+Back',
      live_photo: 'https://via.placeholder.com/400x300/FF9800/ffffff?text=Live+Photo',
      document_status: ['待审核', '已审核', '需复核'][Math.floor(Math.random() * 3)],
      document_verification: {
        is_fake_id: false,
        is_fake_live_photo: false,
        is_mismatch: false,
        has_other_issue: false,
        verified_at: dayjs().format('YYYY-MM-DD HH:mm:ss'),
        verified_by: '系统自动审核',
        remark: ''
      }
    },
    loan_details: {
      loan_id: currentCase.loan_id,
      loan_type: currentCase.custom_fields?.loan_term || '单期',
      loan_source: ['App直接申请', '贷超导流', '线下推广'][Math.floor(Math.random() * 3)],
      product_name: currentCase.product_name,
      app_name: currentCase.app_name,
      contract_number: `CON${Math.floor(Math.random() * 900000000) + 100000000}`,
      contract_sign_date: dayjs().subtract(Math.floor(Math.random() * 60), 'day').format('YYYY-MM-DD'),
      contract_amount: currentCase.custom_fields?.disbursement_amount || Math.floor(Math.random() * 10000) + 3000,
      contract_term: isMultiTerm ? `${Math.floor(Math.random() * 10) + 3}个月` : '7天',
      interest_rate: currentCase.custom_fields?.interest_rate || `${Math.floor(Math.random() * 20) + 20}%`,
      service_fee: Math.floor(Math.random() * 400) + 100,
      contract_file_url: 'https://example.com/contracts/sample.pdf',
      disbursement_amount: currentCase.custom_fields?.disbursement_amount || Math.floor(Math.random() * 10000) + 3000,
      disbursement_date: dayjs().subtract(Math.floor(Math.random() * 50), 'day').format('YYYY-MM-DD'),
      disbursement_status: '已放款',
      transaction_id: `TXN${Math.floor(Math.random() * 90000000000) + 10000000000}`,
      recipient_name: currentCase.user_name,
      bank_name: currentCase.tenant_id === 1 ? 'BBVA Bancomer' : 'State Bank of India',
      bank_account: `****${Math.floor(Math.random() * 9000) + 1000}`,
      total_due_amount: currentCase.total_due_amount || 0,
      outstanding_amount: currentCase.outstanding_amount || 0,
      due_date: dayjs().subtract(Math.floor(Math.random() * 30) - 10, 'day').format('YYYY-MM-DD'),
      overdue_days: currentCase.overdue_days || 0,
      overdue_penalty: Math.floor(Math.random() * 2000),
    },
    loan_history: Array.from({ length: Math.floor(Math.random() * 3) }, () => ({
      loan_id: `${currentCase.tenant_id === 1 ? 'BTQ' : 'BTSK'}-${Math.floor(Math.random() * 900000) + 100000}`,
      loan_date: dayjs().subtract(Math.floor(Math.random() * 365) + 180, 'day').format('YYYY-MM-DD'),
      loan_amount: Math.floor(Math.random() * 8000) + 2000,
      repay_date: dayjs().subtract(Math.floor(Math.random() * 300) + 150, 'day').format('YYYY-MM-DD'),
      repay_amount: Math.floor(Math.random() * 10000) + 2600,
      status: ['已结清', '正常还款', '逾期已还'][Math.floor(Math.random() * 3)],
      overdue_days: Math.floor(Math.random() * 30),
    })),
    payment_records: Array.from({ length: Math.floor(Math.random() * 4) }, () => ({
      payment_id: `PAY${Math.floor(Math.random() * 90000000) + 10000000}`,
      payment_date: dayjs().subtract(Math.floor(Math.random() * 30), 'day').format('YYYY-MM-DD HH:mm:ss'),
      payment_amount: Math.floor(Math.random() * 3000) + 500,
      payment_method: currentCase.tenant_id === 1 
        ? ['SPEI', 'OXXO', '银行转账'][Math.floor(Math.random() * 3)]
        : ['UPI', 'Net Banking', 'Debit Card'][Math.floor(Math.random() * 3)],
      transaction_id: `TXN${Math.floor(Math.random() * 90000000000) + 10000000000}`,
      payment_status: ['成功', '处理中', '失败'][Math.floor(Math.random() * 3)],
      payment_channel: ['App', '网页', 'ATM'][Math.floor(Math.random() * 3)],
      remark: ['正常还款', '部分还款', '逾期还款', ''][Math.floor(Math.random() * 4)],
    })),
    installment_details: isMultiTerm ? {
      total_installments: 6,
      current_installment: 3,
      installment_amount: Math.floor(Math.random() * 1500) + 800,
      installments: Array.from({ length: 6 }, (_, i) => {
        const isPaid = i < 2
        const isCurrent = i === 2
        return {
          installment_number: i + 1,
          due_date: dayjs().subtract((6 - i - 1) * 30, 'day').format('YYYY-MM-DD'),
          due_amount: Math.floor(Math.random() * 1500) + 800,
          paid_amount: isPaid ? Math.floor(Math.random() * 1500) + 800 : 0,
          outstanding_amount: isPaid ? 0 : Math.floor(Math.random() * 1500) + 800,
          status: isPaid ? '已还清' : (isCurrent ? '逾期' : '待还款'),
          payment_date: isPaid ? dayjs().subtract((6 - i - 1) * 30 + 2, 'day').format('YYYY-MM-DD') : null,
          overdue_days: isCurrent ? Math.floor(Math.random() * 15) : 0,
        }
      }),
      payment_qr_code: 'https://via.placeholder.com/300x300/673AB7/ffffff?text=Payment+QR+Code'
    } : null
  }
})

// 分页
const pagination = ref({
  page: 1,
  pageSize: 50
})

// 动态计算表格高度
const tableHeight = computed(() => {
  // 基础高度 = 100vh - 顶部条(60) - 看板(200) - 搜索(52) - 基础过滤器(52) - 按钮行(40) - 列表头(48) - 分页(56) - 边距(20)
  const baseDeduction = 528
  // 如果显示更多过滤器，额外减去52px
  const extraDeduction = showMoreFilters.value ? 52 : 0
  return `calc(100vh - ${baseDeduction + extraDeduction}px)`
})

const filteredCases = computed(() => {
  let result = cases.value

  // 搜索过滤
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter((c: any) =>
      c.user_id?.toLowerCase().includes(keyword) ||
      c.loan_id?.toLowerCase().includes(keyword) ||
      c.mobile_number?.toLowerCase().includes(keyword)
    )
  }

  // TODO: 应用其他过滤器

  return result
})

const paginatedCases = computed(() => {
  const start = (pagination.value.page - 1) * pagination.value.pageSize
  const end = start + pagination.value.pageSize
  return filteredCases.value.slice(start, end)
})

// 方法
const handleLanguageChange = (lang: string) => {
  currentLanguage.value = languageMap[lang]
  ElMessage.success(`已切换到${currentLanguage.value}`)
}

const handleAccountCommand = async (command: string) => {
  if (command === 'logout-whatsapp') {
    await ElMessageBox.confirm('确定要登出 WhatsApp 账号吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    ElMessage.success('已登出 WhatsApp 账号')
  } else if (command === 'logout-all') {
    await ElMessageBox.confirm(
      '登出催收账号将同时登出 WhatsApp 账号，确定要继续吗？',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await imUserStore.logout()
    router.push('/im/login')
    ElMessage.success('已登出所有账号')
  }
}

const refreshDashboard = () => {
  lastRefreshTime.value = dayjs().format('HH:mm:ss')
  ElMessage.success('数据已刷新')
}

const viewMoreReports = () => {
  ElMessage.info('跳转到报表页面功能开发中')
}

const handleSearch = () => {
  pagination.value.page = 1
}

const handleFilterChange = () => {
  pagination.value.page = 1
}

const handleSaveView = () => {
  ElMessage.info('保存视图功能开发中')
}

const handleResetFilters = () => {
  filters.value = {
    paymentDateRange: [],
    userReply: '',
    hasPTP: '',
    caseStatus: [],
    paymentIntention: [],
    product: '',
    app: '',
    borrowType: '',
    recentPayment: '',
    firstTerm: ''
  }
  searchKeyword.value = ''
  handleFilterChange()
}

const handleSelectAll = () => {
  // 实现全选逻辑
}

const handleSelectionChange = (selection: Case[]) => {
  selectedCases.value = selection
}

const handleRowClick = (row: Case) => {
  selectedCase.value = row
}

const handleBulkCall = () => {
  ElMessage.info(`批量外呼 ${selectedCases.value.length} 个案件`)
}

const handleBulkMessage = () => {
  ElMessage.info(`批量发送消息给 ${selectedCases.value.length} 个案件`)
}

const getRowClassName = ({ row }: { row: Case }) => {
  return selectedCase.value?.id === row.id ? 'selected-row' : ''
}

const getOverdueType = (days: number) => {
  if (days < 0) return 'success'
  if (days === 0) return 'warning'
  return 'danger'
}

const formatCurrency = (amount: number) => {
  if (!amount) return '0'
  return Math.round(amount).toLocaleString('zh-CN')
}

// 未读消息状态映射（loan_id -> hasUnread）
const unreadMessagesMap = ref<Record<string, boolean>>({})

// 检查案件是否有未读消息
const hasUnreadMessagesForLoan = (loanId: string) => {
  // 如果当前选中的案件是这个案件，且 IMPanel 检测到有未读消息，返回 true
  if (selectedCase.value && selectedCase.value.loan_id === loanId && imPanelRef.value?.hasUnreadMessagesForCase) {
    unreadMessagesMap.value[loanId] = true
    return true
  }
  
  // 否则返回映射中的状态（如果之前检测过）
  return unreadMessagesMap.value[loanId] || false
}

// 监听选中案件的变化，更新未读消息状态
watch(() => selectedCase.value, (newCase) => {
  if (newCase && imPanelRef.value) {
    const loanId = newCase.loan_id
    if (loanId) {
      // 等待 IMPanel 更新后检查未读消息
      nextTick(() => {
        // 延迟检查，确保 IMPanel 已经完全更新
        setTimeout(() => {
          if (imPanelRef.value?.hasUnreadMessagesForCase) {
            unreadMessagesMap.value[loanId] = true
          } else {
            // 如果没有未读消息，清除状态
            unreadMessagesMap.value[loanId] = false
          }
        }, 100)
      })
    }
  }
}, { deep: true })

// 监听 IMPanel 的未读消息状态变化
watch(() => imPanelRef.value?.hasUnreadMessagesForCase, (hasUnread) => {
  if (selectedCase.value) {
    const loanId = selectedCase.value.loan_id
    if (loanId) {
      // 更新未读状态（包括清除未读状态）
      if (hasUnread) {
        unreadMessagesMap.value[loanId] = true
      } else {
        // 当消息被回复后，清除未读状态
        unreadMessagesMap.value[loanId] = false
      }
    }
  }
})

const loadCases = async () => {
  try {
    if (!user.value?.tenantId || !user.value?.id) {
      console.warn('用户信息不完整，无法加载案件')
      cases.value = []
      return
    }
    
    console.log('开始加载案件, tenantId:', user.value.tenantId, 'collectorId:', user.value.id)
    
    // 构建查询参数：只查询当前催员的案件
    const params: any = {
      tenant_id: parseInt(user.value.tenantId),  // 转换为整数
      collector_id: parseInt(user.value.id as string)  // 催员ID
    }
    
    const res = await getCases(params)
    console.log('案件加载响应:', res)
    
    // 后端直接返回数组，不是 {data: []} 格式
    cases.value = Array.isArray(res) ? res : (res.data || [])
    console.log('加载的案件数量:', cases.value.length)
    
    if (cases.value.length > 0) {
      selectedCase.value = cases.value[0]
    }
  } catch (error) {
    console.error('加载案件失败:', error)
    const errorMessage = error instanceof Error ? error.message : '未知错误'
    ElMessage.error('加载案件失败: ' + errorMessage)
    cases.value = []
  }
}

onMounted(() => {
  updateTime()
  setInterval(updateTime, 1000)
  refreshDashboard()
  loadCases()

  // 根据机构设置时区
  if (user.value?.tenantId === '1' || (user.value as any)?.tenantCode === 'BTQ') {
    currentTimezone.value = 'America/Mexico_City'
    timezoneShort.value = 'CST'
  } else if (user.value?.tenantId === '2' || (user.value as any)?.tenantCode === 'BTSK') {
    currentTimezone.value = 'Asia/Kolkata'
    timezoneShort.value = 'IST'
  }
})

onUnmounted(() => {
  // 清理通知面板定时器
  if (notificationPanelTimer.value) {
    clearTimeout(notificationPanelTimer.value)
  }
  // 清理轮播定时器
  stopCarousel()
})
</script>

<style scoped>
.collector-workspace {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
}

/* 顶部条 */
.workspace-header {
  height: 60px;
  background: #ffffff;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-icon {
  color: #25D366;
  animation: pulse 2s ease-in-out infinite;
  transition: all 0.3s ease;
}

.logo-icon:hover {
  transform: scale(1.1);
  filter: drop-shadow(0 0 8px rgba(37, 211, 102, 0.6));
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.8;
    transform: scale(1.05);
  }
}

.system-name {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.header-center {
  flex: 1;
  display: flex;
  justify-content: center;
}

.notification-center {
  position: relative;
  display: flex;
  align-items: center;
  gap: 12px;
}

.notification-center :deep(.el-badge__content) {
  background-color: #F56C6C;
  border: 2px solid #fff;
}

/* 通知轮播 */
.notification-carousel {
  position: relative;
  min-width: 280px;
  max-width: 450px;
  height: 36px;
  background: linear-gradient(135deg, #fff5f5 0%, #ffe5e5 100%);
  border: 1px solid #ffcccc;
  border-radius: 18px;
  padding: 0 14px;
  display: flex;
  align-items: center;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(245, 108, 108, 0.15);
  z-index: 100;
  animation: slideInRight 0.5s ease-out;
}

.notification-carousel:hover {
  background: linear-gradient(135deg, #ffe5e5 0%, #ffcccc 100%);
  box-shadow: 0 4px 12px rgba(245, 108, 108, 0.25);
  transform: translateX(-4px);
}

@keyframes slideInRight {
  from {
    opacity: 0;
    transform: translateX(20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.carousel-content {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
  overflow: hidden;
}

.carousel-icon {
  color: #F56C6C;
  font-size: 18px;
  flex-shrink: 0;
  animation: bellRing 2s ease-in-out infinite;
}

@keyframes bellRing {
  0%, 100% {
    transform: rotate(0deg);
  }
  10%, 30% {
    transform: rotate(-10deg);
  }
  20%, 40% {
    transform: rotate(10deg);
  }
  50% {
    transform: rotate(0deg);
  }
}

.carousel-text {
  flex: 1;
  overflow: hidden;
  min-width: 0;
}

.carousel-message {
  display: block;
  font-size: 13px;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  animation: fadeInOut 0.5s ease-in-out;
}

@keyframes fadeInOut {
  0% {
    opacity: 0;
    transform: translateX(10px);
  }
  100% {
    opacity: 1;
    transform: translateX(0);
  }
}

.carousel-indicator {
  flex-shrink: 0;
  font-size: 11px;
  color: #909399;
  background: rgba(255, 255, 255, 0.8);
  padding: 2px 8px;
  border-radius: 10px;
  font-weight: 500;
}

/* 通知面板 */
.notification-panel {
  position: absolute;
  top: calc(100% + 12px);
  right: 0;
  width: 600px;
  max-height: 650px;
  background: #ffffff;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  z-index: 1000;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  animation: slideDownFade 0.3s ease-out;
}

.notification-panel::before {
  content: '';
  position: absolute;
  top: -8px;
  right: 20px;
  width: 0;
  height: 0;
  border-left: 8px solid transparent;
  border-right: 8px solid transparent;
  border-bottom: 8px solid #ffffff;
  filter: drop-shadow(0 -2px 4px rgba(0, 0, 0, 0.1));
}

@keyframes slideDownFade {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.notification-panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #e4e7ed;
  background: #f8f9fa;
}

.panel-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.header-badge {
  margin-left: 4px;
}

.notification-filters {
  padding: 12px 20px;
  border-bottom: 1px solid #e4e7ed;
  background: #ffffff;
}

.notification-filters :deep(.el-scrollbar) {
  width: 100%;
}

.filter-group {
  display: flex;
  gap: 8px;
  white-space: nowrap;
}

.notification-filters :deep(.el-radio-button__inner) {
  padding: 6px 12px;
  font-size: 12px;
  border-radius: 4px;
  white-space: nowrap;
}

.notification-list {
  flex: 1;
  overflow: hidden;
}

.empty-notifications {
  padding: 40px 20px;
  text-align: center;
}

.notification-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background 0.2s;
  gap: 12px;
}

.notification-item:hover {
  background: #f5f7fa;
}

.notification-item.unread {
  background: #f0f9ff;
  border-left: 3px solid #25D366;
}

.notification-item.unread:hover {
  background: #e8f5e9;
}

.notification-content {
  flex: 1;
  min-width: 0;
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  gap: 12px;
}

.notification-time {
  font-size: 12px;
  color: #909399;
  white-space: nowrap;
}

.notification-body {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.notification-text {
  font-size: 14px;
  color: #303133;
  line-height: 1.5;
  word-wrap: break-word;
}

.notification-case {
  font-size: 12px;
  color: #606266;
  font-weight: 500;
}

.notification-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.notification-actions .el-button {
  padding: 4px 12px;
  font-size: 12px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.timezone-display {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: #f5f7fa;
  border-radius: 20px;
  font-size: 14px;
  color: #606266;
}

.timezone-label {
  color: #909399;
  font-size: 12px;
}

.language-selector {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  cursor: pointer;
  border-radius: 20px;
  transition: background 0.3s;
}

.language-selector:hover {
  background: #f5f7fa;
}

.flag {
  font-size: 16px;
}

.account-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 12px;
  cursor: pointer;
  border-radius: 20px;
  transition: background 0.3s;
}

.account-info:hover {
  background: rgba(37, 211, 102, 0.1);
}

.collector-id {
  font-weight: 500;
  color: #303133;
}

.account-details {
  padding: 8px;
  line-height: 1.6;
}

.text-secondary {
  color: #909399;
  font-size: 12px;
}

/* 主工作区 */
.workspace-main {
  flex: 1;
  display: flex;
  overflow: hidden;
}

/* 左侧案件列表 */
.case-list-section {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  background: #ffffff;
}

/* 看板 */
.dashboard-card {
  padding: 16px;
  border-bottom: 1px solid #e4e7ed;
}

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.dashboard-header h3 {
  margin: 0;
  font-size: 16px;
  color: #303133;
}

.dashboard-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.refresh-time {
  font-size: 12px;
  color: #909399;
}

.dashboard-metrics {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

.metric-block {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.block-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  text-align: center;
  padding-bottom: 8px;
  border-bottom: 1px solid #e4e7ed;
}

.block-dimensions {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.dimension-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}

.dimension-label {
  font-size: 12px;
  color: #909399;
}

.dimension-value {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.dimension-value.primary {
  color: #25D366;
}

.dimension-value.warning {
  color: #E6A23C;
}

.dimension-value.success {
  color: #67C23A;
}

/* 搜索和过滤 */
.search-box {
  padding: 12px 16px;
  border-bottom: 1px solid #e4e7ed;
  background: #ffffff;
}

.search-box :deep(.el-input__wrapper) {
  border-radius: 20px;
  box-shadow: 0 2px 8px rgba(37, 211, 102, 0.1);
  transition: all 0.3s ease;
}

.search-box :deep(.el-input__wrapper:hover) {
  box-shadow: 0 2px 12px rgba(37, 211, 102, 0.15);
}

.search-box :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 2px 12px rgba(37, 211, 102, 0.2);
}

.filters-section {
  border-bottom: 1px solid #e4e7ed;
  background: linear-gradient(to bottom, #ffffff, #f9fafb);
}

.filters-compact {
  padding: 12px 16px;
}

.filter-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
  margin-bottom: 10px;
}

.filter-row-more {
  animation: slideDown 0.3s ease-out;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.filter-item {
  min-width: 0;
}

.filter-item-date {
  grid-column: span 2;
}

.filter-item :deep(.el-select),
.filter-item :deep(.el-date-editor) {
  width: 100%;
}

.filter-item :deep(.el-input__wrapper) {
  border-radius: 6px;
  transition: all 0.3s ease;
  border: 1px solid #dcdfe6;
}

.filter-item :deep(.el-input__wrapper:hover) {
  border-color: #25D366;
  box-shadow: 0 0 0 1px rgba(37, 211, 102, 0.1);
}

.filter-item :deep(.el-input__wrapper.is-focus) {
  border-color: #25D366;
  box-shadow: 0 0 0 2px rgba(37, 211, 102, 0.15);
}

.filter-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 4px;
  gap: 8px;
}

.filter-actions .el-button {
  transition: all 0.3s ease;
}

.filter-actions .el-button:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(37, 211, 102, 0.15);
}

/* 案件列表表格 */
.case-list-table {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.list-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-bottom: 1px solid #e4e7ed;
  background: #fafafa;
}

.case-count {
  font-size: 14px;
  color: #606266;
  margin-right: auto;
}

.case-list-table :deep(.el-table) {
  flex: 1;
}

.case-list-table :deep(.selected-row) {
  background-color: rgba(37, 211, 102, 0.1);
}

/* 确保其他列内容完整显示，不换行 */
.case-list-table :deep(.el-table__cell) {
  padding: 8px 0;
}

/* 用户名列的单元格允许内容溢出 */
.case-list-table :deep(.el-table__body-wrapper tr td:nth-child(3)) {
  overflow: hidden;
}

.amount {
  color: #F56C6C;
  font-weight: 500;
  white-space: nowrap;
}

.loan-id-cell {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  position: relative;
}

.case-unread-dot {
  width: 8px;
  height: 8px;
  background: #F56C6C;
  border-radius: 50%;
  flex-shrink: 0;
}

.user-name-cell {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.user-name-cell .user-name {
  font-size: 14px;
  color: #303133;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 100%;
}

.user-name-cell .user-id {
  font-size: 12px;
  color: #909399;
  line-height: 1.2;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 100%;
}

.pagination-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-top: 1px solid #e4e7ed;
}

.pagination-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

/* 可拖动分割线 */
.resizer {
  width: 8px;
  background: linear-gradient(to right, #f5f7fa, #e4e7ed, #f5f7fa);
  cursor: col-resize;
  position: relative;
  flex-shrink: 0;
  transition: all 0.2s ease;
  user-select: none;
  z-index: 10;
}

.resizer::before {
  content: '';
  position: absolute;
  top: 0;
  left: 3px;
  width: 2px;
  height: 100%;
  background: rgba(228, 231, 237, 0.5);
}

.resizer:hover {
  background: linear-gradient(to right, rgba(37, 211, 102, 0.1), rgba(37, 211, 102, 0.3), rgba(37, 211, 102, 0.1));
  width: 10px;
}

.resizer:hover::before {
  background: rgba(37, 211, 102, 0.6);
}

.resizer:active {
  background: linear-gradient(to right, rgba(32, 189, 90, 0.2), rgba(32, 189, 90, 0.4), rgba(32, 189, 90, 0.2));
  width: 10px;
}

.resizer:active::before {
  background: rgba(32, 189, 90, 0.8);
}

.resizer-handle {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 4px;
  height: 80px;
  background: #fff;
  border-radius: 3px;
  pointer-events: none;
  opacity: 0;
  transition: all 0.2s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.resizer:hover .resizer-handle {
  opacity: 0.9;
  height: 100px;
}

.resizer:active .resizer-handle {
  opacity: 1;
  height: 120px;
  box-shadow: 0 4px 12px rgba(37, 211, 102, 0.3);
}

/* 右侧详情区域 */
.detail-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
  background: #ffffff;
  overflow: hidden;
}

.unified-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
  overflow: hidden;
}

.case-detail-panel {
  flex-shrink: 0;
  overflow: auto;
  display: flex;
  flex-direction: column;
}

.im-panel-container {
  flex: 1;
  min-height: 0;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

/* 可拖动水平分割线 */
.horizontal-resizer {
  height: 8px;
  background: linear-gradient(to bottom, #f5f7fa, #e4e7ed, #f5f7fa);
  cursor: row-resize;
  position: relative;
  flex-shrink: 0;
  transition: all 0.2s ease;
  user-select: none;
  z-index: 10;
}

.horizontal-resizer::before {
  content: '';
  position: absolute;
  left: 0;
  top: 3px;
  width: 100%;
  height: 2px;
  background: rgba(228, 231, 237, 0.5);
}

.horizontal-resizer:hover {
  background: linear-gradient(to bottom, rgba(37, 211, 102, 0.1), rgba(37, 211, 102, 0.3), rgba(37, 211, 102, 0.1));
  height: 10px;
}

.horizontal-resizer:hover::before {
  background: rgba(37, 211, 102, 0.6);
}

.horizontal-resizer:active {
  background: linear-gradient(to bottom, rgba(32, 189, 90, 0.2), rgba(32, 189, 90, 0.4), rgba(32, 189, 90, 0.2));
  height: 10px;
}

.horizontal-resizer:active::before {
  background: rgba(32, 189, 90, 0.8);
}

.horizontal-resizer-handle {
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  height: 4px;
  width: 80px;
  background: #fff;
  border-radius: 3px;
  pointer-events: none;
  opacity: 0;
  transition: all 0.2s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.horizontal-resizer:hover .horizontal-resizer-handle {
  opacity: 0.9;
  width: 100px;
}

.horizontal-resizer:active .horizontal-resizer-handle {
  opacity: 1;
  width: 120px;
  box-shadow: 0 4px 12px rgba(37, 211, 102, 0.3);
}

.no-case-selected {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #909399;
}

.no-case-selected p {
  margin-top: 16px;
  font-size: 14px;
}
</style>

