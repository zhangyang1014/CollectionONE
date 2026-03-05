<template>
  <div class="phone-management">
    <!-- 第一行：号码生命周期分布（互斥分桶，合计 = 总 Instant 数） -->
    <div class="stats-section">
      <div class="stats-section-header">
        <span class="stats-section-title">号码生命周期</span>
        <span class="stats-section-total">共 <strong>{{ stats.totalInstants }}</strong> 个 Instant</span>
      </div>
      <div class="stats-panel">
        <div class="stat-item">
          <span class="stat-dot" style="background:#c0c4cc"></span>
          <div class="stat-value">{{ stats.configuring }}</div>
          <div class="stat-label">云机配置中</div>
          <div class="stat-desc">待绑 IP / 待激活</div>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-item">
          <span class="stat-dot" style="background:#a0cfff"></span>
          <div class="stat-value">{{ stats.pendingNurture }}</div>
          <div class="stat-label">待投养</div>
          <div class="stat-desc">激活后等待投养</div>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-item">
          <span class="stat-dot" style="background:#f3d19e"></span>
          <div class="stat-value">{{ stats.nurturing }}</div>
          <div class="stat-label">投养中</div>
          <div class="stat-desc">正在进行投养</div>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-item stat-item-clickable" @click="activeTab = 'pendingAssign'; handleTabChange('pendingAssign')">
          <span class="stat-dot" style="background:#d3adf7"></span>
          <div class="stat-value">{{ stats.pendingAssign }}</div>
          <div class="stat-label">待分配</div>
          <div class="stat-desc">投养完成待分催员</div>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-item">
          <span class="stat-dot" style="background:#b3e19d"></span>
          <div class="stat-value">{{ stats.inUse }}</div>
          <div class="stat-label">使用中</div>
          <div class="stat-desc">已分配催员使用</div>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-item">
          <span class="stat-dot" style="background:#fab6b6"></span>
          <div class="stat-value stat-value-danger">{{ stats.problematic }}</div>
          <div class="stat-label">问题号</div>
          <div class="stat-desc">封号 / 申诉 / 停用</div>
        </div>
      </div>
    </div>

    <!-- 第二行：当前健康状态（质量指标，独立统计） -->
    <div class="stats-section">
      <div class="stats-section-header">
        <span class="stats-section-title">健康状态</span>
      </div>
      <div class="stats-panel">
        <div class="stat-item">
          <span class="stat-dot" style="background:#b3e19d"></span>
          <div class="stat-value-row">
            <span class="stat-value">{{ stats.nurtureCompleted }}</span>
            <el-tooltip :content="rates.nurtureCompleted.tip" placement="top" effect="dark" :show-after="100">
              <span class="stat-rate">{{ rates.nurtureCompleted.value }}</span>
            </el-tooltip>
          </div>
          <div class="stat-label">已养成</div>
          <div class="stat-desc">完成投养的总量</div>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-item">
          <span class="stat-dot" style="background:#67c23a"></span>
          <div class="stat-value-row">
            <span class="stat-value">{{ stats.activeInUse }}</span>
            <el-tooltip :content="rates.activeInUse.tip" placement="top" effect="dark" :show-after="100">
              <span class="stat-rate">{{ rates.activeInUse.value }}</span>
            </el-tooltip>
          </div>
          <div class="stat-label">正常使用中</div>
          <div class="stat-desc">在线且已分配</div>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-item">
          <span class="stat-dot" style="background:#f3d19e"></span>
          <div class="stat-value-row">
            <span class="stat-value">{{ stats.offline }}</span>
            <el-tooltip :content="rates.offline.tip" placement="top" effect="dark" :show-after="100">
              <span class="stat-rate">{{ rates.offline.value }}</span>
            </el-tooltip>
          </div>
          <div class="stat-label">掉线中</div>
          <div class="stat-desc">WA 连接中断</div>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-item">
          <span class="stat-dot" style="background:#fab6b6"></span>
          <div class="stat-value-row">
            <span class="stat-value stat-value-danger">{{ stats.banned }}</span>
            <el-tooltip :content="rates.banned.tip" placement="top" effect="dark" :show-after="100">
              <span class="stat-rate">{{ rates.banned.value }}</span>
            </el-tooltip>
          </div>
          <div class="stat-label">封号中</div>
          <div class="stat-desc">待提交申诉</div>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-item">
          <span class="stat-dot" style="background:#f0a020"></span>
          <div class="stat-value-row">
            <span class="stat-value">{{ stats.appealing }}</span>
            <el-tooltip :content="rates.appealing.tip" placement="top" effect="dark" :show-after="100">
              <span class="stat-rate">{{ rates.appealing.value }}</span>
            </el-tooltip>
          </div>
          <div class="stat-label">解封中</div>
          <div class="stat-desc">等待申诉结果</div>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-item">
          <span class="stat-dot" style="background:#c0c4cc"></span>
          <div class="stat-value-row">
            <span class="stat-value stat-value-danger">{{ stats.disabled }}</span>
            <el-tooltip :content="rates.disabled.tip" placement="top" effect="dark" :show-after="100">
              <span class="stat-rate">{{ rates.disabled.value }}</span>
            </el-tooltip>
          </div>
          <div class="stat-label">彻底停用</div>
          <div class="stat-desc">已永久停用</div>
        </div>
      </div>
    </div>

    <!-- 操作区 -->
    <el-card style="margin-bottom: 16px">
      <!-- 第一行：搜索筛选（左）+ 快捷操作（右） -->
      <div class="toolbar">
        <div class="toolbar-left">
          <el-input
            v-model="searchParams.keyword"
            placeholder="搜索 ID / 手机号 / 催员"
            clearable
            style="width: 200px"
            @clear="loadList"
            @keyup.enter="loadList"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-select
            v-model="searchParams.purchaseChannelId"
            placeholder="购买渠道"
            clearable
            style="width: 120px"
            @change="loadList"
          >
            <el-option
              v-for="ch in purchaseChannels"
              :key="ch.id"
              :label="ch.channelName"
              :value="ch.id"
            />
          </el-select>
          <el-select
            v-model="searchParams.phoneStatus"
            placeholder="PHONE状态"
            clearable
            style="width: 110px"
            @change="loadList"
          >
            <el-option label="待绑定IP" value="PENDING_IP" />
            <el-option label="待激活" value="PENDING_ACTIVATION" />
            <el-option label="已激活" value="ACTIVATED" />
          </el-select>
          <el-select
            v-model="searchParams.nurtureStatus"
            placeholder="投养状态"
            clearable
            style="width: 105px"
            @change="loadList"
          >
            <el-option label="待投养" value="PENDING" />
            <el-option label="投养中" value="NURTURING" />
            <el-option label="投养完成" value="COMPLETED" />
          </el-select>
          <el-select
            v-model="searchParams.waStatus"
            placeholder="WA状态"
            clearable
            style="width: 95px"
            @change="loadList"
          >
            <el-option label="正常" value="NORMAL" />
            <el-option label="封号待申诉" value="BANNED" />
            <el-option label="申诉中" value="APPEALING" />
            <el-option label="已停用" value="DISABLED" />
          </el-select>
          <el-select
            v-model="searchParams.assignStatus"
            placeholder="分配状态"
            clearable
            style="width: 100px"
            @change="loadList"
          >
            <el-option label="待分配" value="UNASSIGNED" />
            <el-option label="已分配" value="ASSIGNED" />
          </el-select>
        </div>
        <div class="toolbar-right">
          <span v-if="selectedRows.length > 0" class="selection-tip">
            已选择 {{ selectedRows.length }} 项
          </span>
          <el-button type="primary" @click="showRegisterDialog = true">
            <el-icon><Plus /></el-icon>
            云号码登记
          </el-button>
          <el-button
            :disabled="selectedRows.length === 0"
            @click="handleBatchBindIp"
          >
            批量绑定IP
          </el-button>
          <el-button
            :disabled="selectedRows.length === 0"
            @click="handleBatchAssign"
          >
            批量分配
          </el-button>
          <el-button
            v-if="activeTab === 'banned'"
            type="warning"
            :disabled="selectedRows.length === 0 || !selectedRows.every(r => r.waStatus === 'BANNED')"
            @click="handleBatchAppeal"
          >
            批量申诉
          </el-button>
          <el-button
            v-if="activeTab === 'appealing'"
            type="warning"
            :disabled="selectedRows.length === 0 || !selectedRows.every(r => r.waStatus === 'APPEALING')"
            @click="handleBatchFillAppealResult"
          >
            批量填写申诉结果
          </el-button>
        </div>
      </div>

      <!-- 第二行：状态 Tab -->
      <div class="filter-bar">
        <el-tabs v-model="activeTab" class="stage-tabs" @tab-change="handleTabChange">
          <el-tab-pane label="全部" name="all" />
          <el-tab-pane name="pendingIp">
            <template #label>
              <span>待绑定IP</span>
              <el-badge v-if="stats.pendingIp > 0" :value="stats.pendingIp" class="tab-badge" />
            </template>
          </el-tab-pane>
          <el-tab-pane label="待激活" name="pendingAct" />
          <el-tab-pane label="待投养" name="pendingNurture" />
          <el-tab-pane label="投养中" name="nurturing" />
          <el-tab-pane name="pendingAssign">
            <template #label>
              <span>待分配</span>
              <el-badge v-if="stats.pendingAssign > 0" :value="stats.pendingAssign" class="tab-badge" />
            </template>
          </el-tab-pane>
          <el-tab-pane label="封号待申诉" name="banned" />
          <el-tab-pane name="appealing">
            <template #label>
              <span>等待申诉结果</span>
              <el-badge v-if="stats.appealing > 0" :value="stats.appealing" class="tab-badge" />
            </template>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-card>

    <!-- 数据表格 -->
    <el-card>
      <el-table
        ref="tableRef"
        v-loading="loading"
        :data="phoneList"
        row-key="id"
        @selection-change="handleSelectionChange"
        style="width: 100%"
        :row-class-name="getRowClassName"
      >
        <!-- 复选框：独立，不参与分组 -->
        <el-table-column v-if="visibleColumns.includes('selection')" type="selection" width="45" fixed="left" />

        <!-- ── 分组：云机信息 ── -->
        <el-table-column
          label-class-name="group-header-cloud"
          align="center"
        >
          <template #header>
            <span class="group-header-content">
              <span class="group-header-title">云机信息</span>
              <span
                :class="['group-toggle-chip', 'group-toggle-chip--cloud', { 'is-collapsed': cloudCollapsed }]"
                @click.stop="cloudCollapsed = !cloudCollapsed"
              >
                <el-icon :size="10"><ArrowRight v-if="cloudCollapsed" /><ArrowDown v-else /></el-icon>
                {{ cloudCollapsed ? '展开' : '收起' }}
              </span>
            </span>
          </template>
          <el-table-column
            v-if="visibleColumns.includes('instantId')"
            prop="instantId"
            label="Instant ID"
            width="160"
          />
          <el-table-column
            v-if="visibleColumns.includes('channel')"
            prop="purchaseChannelName"
            label="渠道"
            width="110"
          />
          <el-table-column v-if="visibleColumns.includes('ip')" label="关联IP" width="140">
            <template #default="{ row }">
              {{ row.ipAddress || '-' }}
            </template>
          </el-table-column>
          <el-table-column v-if="visibleColumns.includes('phoneStatus')" label="PHONE状态" width="110" align="center">
            <template #default="{ row }">
              <el-tag :type="PHONE_STATUS_MAP[row.phoneStatus].type" size="small">
                {{ PHONE_STATUS_MAP[row.phoneStatus].label }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column v-if="visibleColumns.includes('phone')" prop="phone" label="Phone" width="140">
            <template #default="{ row }">
              {{ row.phone || '-' }}
            </template>
          </el-table-column>
        </el-table-column>

        <!-- ── 分组：WA 投养 ── -->
        <el-table-column
          label-class-name="group-header-nurture"
          align="center"
        >
          <template #header>
            <span class="group-header-content">
              <span class="group-header-title">WA 投养</span>
              <span
                :class="['group-toggle-chip', 'group-toggle-chip--nurture', { 'is-collapsed': nurtureCollapsed }]"
                @click.stop="nurtureCollapsed = !nurtureCollapsed"
              >
                <el-icon :size="10"><ArrowRight v-if="nurtureCollapsed" /><ArrowDown v-else /></el-icon>
                {{ nurtureCollapsed ? '展开' : '收起' }}
              </span>
            </span>
          </template>
          <el-table-column v-if="visibleColumns.includes('nurtureStatus')" label="投养状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag
                v-if="row.phoneStatus === 'ACTIVATED'"
                :type="NURTURE_STATUS_MAP[row.nurtureStatus].type"
                size="small"
              >
                {{ NURTURE_STATUS_MAP[row.nurtureStatus].label }}
              </el-tag>
              <span v-else class="text-muted">-</span>
            </template>
          </el-table-column>
          <el-table-column v-if="visibleColumns.includes('activatedAt')" prop="activatedAt" label="激活时间" width="110">
            <template #default="{ row }">
              <span v-if="row.activatedAt" class="datetime-cell">
                <span class="dt-date">{{ row.activatedAt.slice(0, 10) }}</span>
                <span class="dt-time">{{ row.activatedAt.slice(11, 16) }}</span>
              </span>
              <span v-else class="text-muted">-</span>
            </template>
          </el-table-column>
          <el-table-column v-if="visibleColumns.includes('nurtureStartedAt')" prop="nurtureStartedAt" label="投养时间" width="110">
            <template #default="{ row }">
              <span v-if="row.nurtureStartedAt" class="datetime-cell">
                <span class="dt-date">{{ row.nurtureStartedAt.slice(0, 10) }}</span>
                <span class="dt-time">{{ row.nurtureStartedAt.slice(11, 16) }}</span>
              </span>
              <span v-else class="text-muted">-</span>
            </template>
          </el-table-column>
          <el-table-column v-if="visibleColumns.includes('nurtureDays')" prop="nurtureDays" label="投养天数" width="90" align="center">
            <template #default="{ row }">
              <span v-if="row.nurtureDays > 0">{{ row.nurtureDays }} 天</span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column v-if="visibleColumns.includes('acquisitionCount')" prop="acquisitionCount" label="获客数量" width="90" align="center">
            <template #default="{ row }">
              <span v-if="row.acquisitionCount > 0" class="acquisition-count">{{ row.acquisitionCount }}</span>
              <span v-else class="text-muted">-</span>
            </template>
          </el-table-column>
        </el-table-column>

        <!-- ── 分组：WA 使用 ── -->
        <el-table-column
          label-class-name="group-header-usage"
          align="center"
        >
          <template #header>
            <span class="group-header-content">
              <span class="group-header-title">WA 使用</span>
              <span
                :class="['group-toggle-chip', 'group-toggle-chip--usage', { 'is-collapsed': usageCollapsed }]"
                @click.stop="usageCollapsed = !usageCollapsed"
              >
                <el-icon :size="10"><ArrowRight v-if="usageCollapsed" /><ArrowDown v-else /></el-icon>
                {{ usageCollapsed ? '展开' : '收起' }}
              </span>
            </span>
          </template>
          <el-table-column v-if="visibleColumns.includes('waStatus')" label="WA号码状态" width="148" align="center">
            <template #default="{ row }">
              <template v-if="row.phoneStatus === 'ACTIVATED'">
                <el-tag :type="WA_STATUS_MAP[row.waStatus].type" size="small">
                  {{ WA_STATUS_MAP[row.waStatus].label }}
                </el-tag>
                <!-- 申诉中：顶级展示申诉时间和等待时长 -->
                <template v-if="row.waStatus === 'APPEALING' && row.appealedAt">
                  <div class="appeal-time-info">
                    <span class="appeal-time-label">申诉于 {{ row.appealedAt.slice(0, 16) }}</span>
                    <span class="appeal-elapsed">已等待 {{ formatElapsedTime(row.appealedAt) }}</span>
                  </div>
                </template>
              </template>
              <span v-else class="text-muted">-</span>
            </template>
          </el-table-column>
          <el-table-column v-if="visibleColumns.includes('assignStatus')" label="分配状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="ASSIGN_STATUS_MAP[row.assignStatus].type" size="small">
                {{ ASSIGN_STATUS_MAP[row.assignStatus].label }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column v-if="visibleColumns.includes('assignedAt')" prop="assignedAt" label="分配时间" width="110">
            <template #default="{ row }">
              <span v-if="row.assignedAt" class="datetime-cell">
                <span class="dt-date">{{ row.assignedAt.slice(0, 10) }}</span>
                <span class="dt-time">{{ row.assignedAt.slice(11, 16) }}</span>
              </span>
              <span v-else class="text-muted">-</span>
            </template>
          </el-table-column>
          <el-table-column v-if="visibleColumns.includes('assignedCollectorName')" prop="assignedCollectorName" label="当前CCO员工" width="120">
            <template #default="{ row }">{{ row.assignedCollectorName || '-' }}</template>
          </el-table-column>
          <el-table-column v-if="visibleColumns.includes('cumulativeUsageHours')" label="累计使用时间" width="120" align="center">
            <template #default="{ row }">
              <span v-if="row.cumulativeUsageHours > 0">{{ formatHours(row.cumulativeUsageHours) }}</span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column v-if="visibleColumns.includes('offlineAt')" label="掉线时间" width="110">
            <template #default="{ row }">
              <span v-if="row.offlineAt" class="datetime-cell">
                <span class="dt-date offline-text">{{ row.offlineAt.slice(0, 10) }}</span>
                <span class="dt-time offline-text">{{ row.offlineAt.slice(11, 16) }}</span>
              </span>
              <span v-else>-</span>
            </template>
          </el-table-column>
        </el-table-column>

        <!-- ── 操作列：独立，不参与分组 ── -->
        <el-table-column v-if="visibleColumns.includes('actions')" label="操作" width="340" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleBindIp(row)">
              {{ row.ipId ? '换绑IP' : '绑定IP' }}
            </el-button>
            <el-button link type="primary" size="small" @click="handleViewIpLogs(row)">
              IP记录
            </el-button>
            <el-button link type="primary" size="small" @click="handleViewAssignLogs(row)">
              分配记录
            </el-button>
            <el-button link type="info" size="small" @click="handleViewLifecycle(row)">
              生命周期
            </el-button>
            <el-button
              v-if="row.phoneStatus === 'PENDING_ACTIVATION'"
              link
              type="success"
              size="small"
              @click="handleActivate(row)"
            >
              激活
            </el-button>
            <el-button
              v-if="row.nurtureStatus === 'COMPLETED' && !row.assignedCollectorId"
              link
              type="warning"
              size="small"
              @click="handleAssign(row)"
            >
              分配
            </el-button>
            <el-button
              v-if="row.waStatus === 'BANNED'"
              link
              type="warning"
              size="small"
              @click="handleAppeal(row)"
            >
              申诉
            </el-button>
            <el-button
              v-if="row.waStatus === 'APPEALING'"
              link
              type="warning"
              size="small"
              @click="handleFillAppealResult(row)"
            >
              填写申诉结果
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="searchParams.page"
          v-model:page-size="searchParams.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadList"
          @current-change="loadList"
        />
      </div>
    </el-card>

    <!-- 弹窗组件 -->
    <PhoneRegisterDialog v-model="showRegisterDialog" @success="onRegisterSuccess" />
    <IpBindDialog v-model="showIpBindDialog" :phone="currentPhone" :phones="phonesToBindIp" @success="onBindIpSuccess" />
    <IpChangeLogDialog v-model="showIpLogDialog" :phone="currentPhone" />
    <PhoneAssignDialog v-model="showAssignDialog" :phones="phonesToAssign" @success="onAssignSuccess" />
    <PhoneAssignLogDialog v-model="showAssignLogDialog" :phone="currentPhone" />
    <PhoneLifecycleDialog v-model="showLifecycleDialog" :phone="currentPhone" />

    <!-- 填写申诉结果弹窗 -->
    <el-dialog
      v-model="showAppealResultDialog"
      title="填写申诉结果"
      width="520px"
      :close-on-click-modal="false"
    >
      <div v-if="currentPhone">
        <el-alert type="info" :closable="false" show-icon style="margin-bottom: 20px">
          <template #title>
            请根据在 WhatsApp 中实际查看到的账号状态，选择对应的申诉结果。
          </template>
        </el-alert>
        <el-descriptions :column="1" size="small" border style="margin-bottom: 20px">
          <el-descriptions-item label="手机号">{{ currentPhone.phone }}</el-descriptions-item>
          <el-descriptions-item label="申诉时间">{{ currentPhone.appealedAt }}</el-descriptions-item>
          <el-descriptions-item label="已等待">{{ currentPhone.appealedAt ? formatElapsedTime(currentPhone.appealedAt) : '-' }}</el-descriptions-item>
        </el-descriptions>
        <div class="appeal-result-options">
          <el-card
            class="appeal-result-card appeal-success"
            shadow="hover"
            @click="confirmAppealResult('SUCCESS')"
          >
            <div class="appeal-result-title">
              <el-icon color="#67c23a" size="20"><CircleCheck /></el-icon>
              <span>申诉成功</span>
            </div>
            <div class="appeal-result-desc">
              打开 WhatsApp，账号状态恢复正常可用，即可选择此项。
            </div>
            <div class="appeal-result-action">点击确认 → 状态将变为「正常」</div>
          </el-card>
          <el-card
            class="appeal-result-card appeal-failure"
            shadow="hover"
            @click="confirmAppealResult('FAILURE')"
          >
            <div class="appeal-result-title">
              <el-icon color="#f56c6c" size="20"><CircleClose /></el-icon>
              <span>申诉失败</span>
            </div>
            <div class="appeal-result-desc">
              <p>存在以下任一情况时，判定申诉失败：</p>
              <p>① 打开 WhatsApp，账号显示「已被永久封禁（Permanently banned）」</p>
              <p>② 更换设备后登录，系统提示「出于安全原因，您的账号已被禁止登录」</p>
            </div>
            <div class="appeal-result-action">点击确认 → 状态将变为「已停用」</div>
          </el-card>
        </div>
      </div>
      <template #footer>
        <el-button @click="showAppealResultDialog = false">取消</el-button>
      </template>
    </el-dialog>

    <!-- 批量填写申诉结果弹窗 -->
    <el-dialog
      v-model="showBatchAppealResultDialog"
      title="批量填写申诉结果"
      width="560px"
      :close-on-click-modal="false"
    >
      <el-alert type="info" :closable="false" show-icon style="margin-bottom: 16px">
        <template #title>
          已选择 {{ selectedRows.length }} 个申诉中的号码，请根据 WhatsApp 实际状态统一选择申诉结果。
        </template>
      </el-alert>
      <div class="appeal-result-options">
        <el-card
          class="appeal-result-card appeal-success"
          shadow="hover"
          @click="confirmBatchAppealResult('SUCCESS')"
        >
          <div class="appeal-result-title">
            <el-icon color="#67c23a" size="20"><CircleCheck /></el-icon>
            <span>全部申诉成功</span>
          </div>
          <div class="appeal-result-desc">
            所选号码均已恢复正常，打开 WhatsApp 可正常使用。
          </div>
          <div class="appeal-result-action">点击确认 → 状态将变为「正常」</div>
        </el-card>
        <el-card
          class="appeal-result-card appeal-failure"
          shadow="hover"
          @click="confirmBatchAppealResult('FAILURE')"
        >
          <div class="appeal-result-title">
            <el-icon color="#f56c6c" size="20"><CircleClose /></el-icon>
            <span>全部申诉失败</span>
          </div>
          <div class="appeal-result-desc">
            所选号码均被永久封禁，无法恢复使用。
          </div>
          <div class="appeal-result-action">点击确认 → 状态将变为「已停用」</div>
        </el-card>
      </div>
      <template #footer>
        <el-button @click="showBatchAppealResultDialog = false">取消</el-button>
      </template>
    </el-dialog>

    <!-- 激活弹窗 -->
    <el-dialog v-model="showActivateDialog" title="激活号码" width="450px" :close-on-click-modal="false">
      <el-alert type="info" :closable="false" show-icon style="margin-bottom: 16px">
        <template #title>
          请先到 Geelark 平台完成激活，获取手机号后填入下方。
        </template>
      </el-alert>
      <div v-if="currentPhone" style="margin-bottom: 12px">
        <el-descriptions :column="1" size="small" border>
          <el-descriptions-item label="Instant ID">{{ currentPhone.instantId }}</el-descriptions-item>
          <el-descriptions-item label="关联IP">{{ currentPhone.ipAddress }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <el-form :model="activateForm" label-width="80px">
        <el-form-item label="手机号" required>
          <el-input v-model="activateForm.phoneNumber" placeholder="例如 +52 55 1234 5678" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showActivateDialog = false">取消</el-button>
        <el-button
          type="primary"
          :loading="activating"
          :disabled="!activateForm.phoneNumber"
          @click="confirmActivate"
        >
          确认激活
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, CircleCheck, CircleClose, ArrowDown, ArrowRight } from '@element-plus/icons-vue'
import type { WaPhone, WaPhoneStats, WaPurchaseChannel, PhoneListParams, AppealResult } from '@/types/wa-management'
import { PHONE_STATUS_MAP, NURTURE_STATUS_MAP, WA_STATUS_MAP, ASSIGN_STATUS_MAP } from '@/types/wa-management'
import {
  getPhoneList,
  getPhoneStats,
  activatePhone,
  appealPhone,
  submitAppealResult,
  batchAppealPhones,
  batchResolveAppeal,
  getPurchaseChannels,
} from '@/api/wa-management'
import PhoneRegisterDialog from './components/PhoneRegisterDialog.vue'
import IpBindDialog from './components/IpBindDialog.vue'
import IpChangeLogDialog from './components/IpChangeLogDialog.vue'
import PhoneAssignDialog from './components/PhoneAssignDialog.vue'
import PhoneAssignLogDialog from './components/PhoneAssignLogDialog.vue'
import PhoneLifecycleDialog from './components/PhoneLifecycleDialog.vue'

const route = useRoute()

const loading = ref(false)
const phoneList = ref<WaPhone[]>([])
const total = ref(0)
const selectedRows = ref<WaPhone[]>([])
const stats = ref<WaPhoneStats>({
  totalInstants: 0,
  // Row 1
  configuring: 0,
  pendingNurture: 0,
  nurturing: 0,
  pendingAssign: 0,
  inUse: 0,
  problematic: 0,
  // Row 2
  nurtureCompleted: 0,
  activeInUse: 0,
  offline: 0,
  banned: 0,
  appealing: 0,
  disabled: 0,
  // IP 资源
  availableIps: 0,
  pendingIp: 0,
})
const purchaseChannels = ref<WaPurchaseChannel[]>([])

const searchParams = reactive<PhoneListParams>({
  page: 1,
  pageSize: 20,
  phoneStatus: '',
  nurtureStatus: '',
  waStatus: '',
  assignStatus: '',
  keyword: '',
  purchaseChannelId: '',
})

/** 格式化百分比，分母为 0 时返回 '-' */
const fmtRate = (numerator: number, denominator: number): string => {
  if (!denominator) return '-'
  return (numerator / denominator * 100).toFixed(1) + '%'
}

/** 第二行健康卡片的转化率与 tooltip 公式 */
const rates = computed(() => {
  const s = stats.value
  const activated = s.totalInstants - s.configuring
  return {
    nurtureCompleted: {
      value: fmtRate(s.nurtureCompleted, activated),
      tip: `养成率 = 已养成 ÷ 已激活\n= ${s.nurtureCompleted} ÷ ${activated}`,
    },
    activeInUse: {
      value: fmtRate(s.activeInUse, s.nurtureCompleted),
      tip: `使用率 = 正常使用中 ÷ 已养成\n= ${s.activeInUse} ÷ ${s.nurtureCompleted}`,
    },
    offline: {
      value: fmtRate(s.offline, s.inUse),
      tip: `掉线率 = 掉线中 ÷ 使用中（含掉线）\n= ${s.offline} ÷ ${s.inUse}`,
    },
    banned: {
      value: fmtRate(s.banned, s.nurtureCompleted),
      tip: `封号率 = 封号中 ÷ 已养成\n= ${s.banned} ÷ ${s.nurtureCompleted}`,
    },
    appealing: {
      value: fmtRate(s.appealing, s.problematic),
      tip: `申诉率 = 解封中 ÷ 问题号总数\n= ${s.appealing} ÷ ${s.problematic}`,
    },
    disabled: {
      value: fmtRate(s.disabled, s.nurtureCompleted),
      tip: `停用率 = 彻底停用 ÷ 已养成\n= ${s.disabled} ÷ ${s.nurtureCompleted}`,
    },
  }
})

// 阶段 Tab：切换时自动更新筛选条件并只展示该阶段相关列
const activeTab = ref<string>('all')

// 各分组的折叠状态：false = 展开，true = 折叠
const cloudCollapsed = ref(false)
const nurtureCollapsed = ref(false)
const usageCollapsed = ref(false)

/** Tab 对应的分组默认折叠状态 [云机信息, WA投养, WA使用] */
const TAB_COLLAPSE_CONFIG: Record<string, [boolean, boolean, boolean]> = {
  all:           [false, false, false],
  pendingIp:     [false, true,  true ],
  pendingAct:    [false, true,  true ],
  pendingNurture:[false, false, true ],
  nurturing:     [true,  false, true ],
  pendingAssign: [true,  true,  false],
  banned:        [true,  true,  false],
  appealing:     [true,  true,  false],
}

const TAB_CONFIG: Record<string, {
  phoneStatus: '' | PhoneListParams['phoneStatus']
  nurtureStatus: '' | PhoneListParams['nurtureStatus']
  waStatus: '' | PhoneListParams['waStatus']
  assignStatus: '' | PhoneListParams['assignStatus']
}> = {
  all:           { phoneStatus: '',                   nurtureStatus: '',          waStatus: '',       assignStatus: '' },
  pendingIp:     { phoneStatus: 'PENDING_IP',          nurtureStatus: '',          waStatus: '',       assignStatus: '' },
  pendingAct:    { phoneStatus: 'PENDING_ACTIVATION',  nurtureStatus: '',          waStatus: '',       assignStatus: '' },
  pendingNurture:{ phoneStatus: 'ACTIVATED',           nurtureStatus: 'PENDING',   waStatus: '',       assignStatus: '' },
  nurturing:     { phoneStatus: 'ACTIVATED',           nurtureStatus: 'NURTURING', waStatus: '',       assignStatus: '' },
  pendingAssign: { phoneStatus: 'ACTIVATED',           nurtureStatus: 'COMPLETED', waStatus: 'NORMAL', assignStatus: 'UNASSIGNED' },
  banned:        { phoneStatus: '',                    nurtureStatus: '',          waStatus: 'BANNED',     assignStatus: '' },
  appealing:     { phoneStatus: '',                    nurtureStatus: '',          waStatus: 'APPEALING',  assignStatus: '' },
}

const handleTabChange = (tabName: string) => {
  const config = TAB_CONFIG[tabName]
  if (config) {
    // Tab 切换时将筛选器同步到 Tab 预设值，让下拉筛选器显示当前过滤状态
    searchParams.phoneStatus = config.phoneStatus
    searchParams.nurtureStatus = config.nurtureStatus
    searchParams.waStatus = config.waStatus
    searchParams.assignStatus = config.assignStatus
    searchParams.page = 1
    loadList()
  }
  // 同步各分组的默认折叠状态
  const collapse = TAB_COLLAPSE_CONFIG[tabName]
  if (collapse) {
    ;[cloudCollapsed.value, nurtureCollapsed.value, usageCollapsed.value] = collapse
  }
}

/**
 * 根据折叠状态动态计算可见列名。
 * 三个分组始终显示，折叠时只保留各自的最小列集合。
 */
const visibleColumns = computed<string[]>(() => {
  const cols: string[] = []
  // 复选框：在需要批量操作的 Tab 下显示
  if (['all', 'pendingIp', 'pendingAssign', 'banned', 'appealing'].includes(activeTab.value)) cols.push('selection')
  // 云机信息分组：最小集 instantId + phone；展开时追加 channel、ip、phoneStatus
  cols.push('instantId', 'phone')
  if (!cloudCollapsed.value) cols.push('channel', 'ip', 'phoneStatus')
  // WA 投养分组：最小集 nurtureStatus；展开时追加时间/天数等
  cols.push('nurtureStatus')
  if (!nurtureCollapsed.value) cols.push('activatedAt', 'nurtureStartedAt', 'nurtureDays', 'acquisitionCount')
  // WA 使用分组：最小集 waStatus + assignStatus；展开时追加时间/人员等
  cols.push('waStatus', 'assignStatus')
  if (!usageCollapsed.value) cols.push('assignedAt', 'assignedCollectorName', 'cumulativeUsageHours', 'offlineAt')
  cols.push('actions')
  return cols
})

// 弹窗状态
const showRegisterDialog = ref(false)
const showIpBindDialog = ref(false)
const showIpLogDialog = ref(false)
const showAssignDialog = ref(false)
const showActivateDialog = ref(false)
const showAssignLogDialog = ref(false)
const showLifecycleDialog = ref(false)
const currentPhone = ref<WaPhone | null>(null)
const phonesToAssign = ref<WaPhone[]>([])
const phonesToBindIp = ref<WaPhone[]>([])

// 激活表单
const activateForm = reactive({ phoneNumber: '' })
const activating = ref(false)

const loadList = async () => {
  loading.value = true
  try {
    const res = await getPhoneList(searchParams)
    phoneList.value = res.list
    total.value = res.total
  } finally {
    loading.value = false
  }
}

const loadStats = async () => {
  stats.value = await getPhoneStats()
}

const loadChannels = async () => {
  purchaseChannels.value = await getPurchaseChannels()
}

const handleSelectionChange = (rows: WaPhone[]) => {
  selectedRows.value = rows
}

const getRowClassName = ({ row }: { row: WaPhone }) => {
  if (row.offlineAt && row.waStatus === 'NORMAL') return 'offline-row'
  if (row.waStatus === 'BANNED') return 'banned-row'
  return ''
}

const formatHours = (hours: number): string => {
  if (hours < 24) return `${hours}小时`
  const days = Math.floor(hours / 24)
  const h = hours % 24
  return h > 0 ? `${days}天${h}小时` : `${days}天`
}

/** 计算从指定时间到现在的已等待时长，返回如"5天3小时"或"2小时30分" */
const formatElapsedTime = (from: string): string => {
  if (!from) return '-'
  const fromMs = new Date(from.replace(' ', 'T')).getTime()
  const nowMs = Date.now()
  const diffMs = nowMs - fromMs
  if (diffMs <= 0) return '刚刚'
  const totalMinutes = Math.floor(diffMs / 60000)
  const totalHours = Math.floor(totalMinutes / 60)
  const days = Math.floor(totalHours / 24)
  const hours = totalHours % 24
  const minutes = totalMinutes % 60
  if (days > 0) {
    return hours > 0 ? `${days}天${hours}小时` : `${days}天`
  }
  if (totalHours > 0) {
    return minutes > 0 ? `${totalHours}小时${minutes}分` : `${totalHours}小时`
  }
  return `${minutes}分钟`
}

// 行操作
const handleBindIp = (row: WaPhone) => {
  currentPhone.value = row
  phonesToBindIp.value = []
  showIpBindDialog.value = true
}

const handleViewIpLogs = (row: WaPhone) => {
  currentPhone.value = row
  showIpLogDialog.value = true
}

const handleViewAssignLogs = (row: WaPhone) => {
  currentPhone.value = row
  showAssignLogDialog.value = true
}

const handleViewLifecycle = (row: WaPhone) => {
  currentPhone.value = row
  showLifecycleDialog.value = true
}

const handleActivate = (row: WaPhone) => {
  currentPhone.value = row
  activateForm.phoneNumber = ''
  showActivateDialog.value = true
}

const confirmActivate = async () => {
  if (!currentPhone.value || !activateForm.phoneNumber) return
  activating.value = true
  try {
    await activatePhone(currentPhone.value.id, activateForm.phoneNumber)
    ElMessage.success('激活成功')
    showActivateDialog.value = false
    loadList()
    loadStats()
  } catch {
    ElMessage.error('激活失败')
  } finally {
    activating.value = false
  }
}

const handleAssign = (row: WaPhone) => {
  phonesToAssign.value = [row]
  showAssignDialog.value = true
}

const handleAppeal = async (row: WaPhone) => {
  await ElMessageBox.confirm(
    `确定对号码 ${row.phone} 发起申诉？\n提交后状态变为「申诉中」，请在 WhatsApp 中查看申诉结果后，再回来填写结果。`,
    '发起申诉',
    { type: 'warning' }
  )
  await appealPhone(row.id)
  ElMessage.success('申诉已提交，号码进入「申诉中」状态，请等待 WhatsApp 官方处理结果')
  loadList()
  loadStats()
}

const showAppealResultDialog = ref(false)
const showBatchAppealResultDialog = ref(false)

const handleFillAppealResult = (row: WaPhone) => {
  currentPhone.value = row
  showAppealResultDialog.value = true
}

const confirmAppealResult = async (result: AppealResult) => {
  if (!currentPhone.value) return
  const label = result === 'SUCCESS' ? '申诉成功' : '申诉失败'
  const targetStatus = result === 'SUCCESS' ? '正常' : '已停用'
  try {
    await ElMessageBox.confirm(
      `确认选择「${label}」？号码状态将变更为「${targetStatus}」，操作不可撤销。`,
      '确认申诉结果',
      { type: result === 'SUCCESS' ? 'success' : 'error', confirmButtonText: '确认', cancelButtonText: '返回' }
    )
    await submitAppealResult(currentPhone.value.id, result)
    ElMessage.success(`已记录申诉结果：${label}，号码状态已更新为「${targetStatus}」`)
    showAppealResultDialog.value = false
    loadList()
    loadStats()
  } catch {
    // 用户点击"返回"，不做处理
  }
}

// 批量操作
const handleBatchBindIp = () => {
  if (selectedRows.value.length === 0) return
  // 多选时传入全部选中行，触发批量绑定逻辑；单选时降级为单号码模式
  currentPhone.value = selectedRows.value[0]
  phonesToBindIp.value = [...selectedRows.value]
  showIpBindDialog.value = true
}

const handleBatchAssign = () => {
  if (selectedRows.value.length === 0) return
  phonesToAssign.value = [...selectedRows.value]
  showAssignDialog.value = true
}

const handleBatchAppeal = async () => {
  const count = selectedRows.value.length
  if (count === 0) return
  const nonBanned = selectedRows.value.filter(r => r.waStatus !== 'BANNED')
  if (nonBanned.length > 0) {
    ElMessage.warning(`已过滤 ${nonBanned.length} 条非封号状态的号码，仅对封号号码发起申诉`)
  }
  const targets = selectedRows.value.filter(r => r.waStatus === 'BANNED')
  if (targets.length === 0) return
  await ElMessageBox.confirm(
    `确定对选中的 ${targets.length} 个封号号码批量发起申诉？\n提交后状态将变为「申诉中」，请在 WhatsApp 中查看结果后，再逐一或批量填写申诉结果。`,
    '批量申诉',
    { type: 'warning', confirmButtonText: '确认发起', cancelButtonText: '取消' }
  )
  await batchAppealPhones(targets.map(r => r.id))
  ElMessage.success(`已对 ${targets.length} 个号码发起申诉，请等待 WhatsApp 官方处理结果`)
  loadList()
  loadStats()
}

const handleBatchFillAppealResult = () => {
  const targets = selectedRows.value.filter(r => r.waStatus === 'APPEALING')
  if (targets.length === 0) {
    ElMessage.warning('请先勾选申诉中的号码')
    return
  }
  showBatchAppealResultDialog.value = true
}

const confirmBatchAppealResult = async (result: AppealResult) => {
  const targets = selectedRows.value.filter(r => r.waStatus === 'APPEALING')
  if (targets.length === 0) return
  const label = result === 'SUCCESS' ? '全部申诉成功' : '全部申诉失败'
  const targetStatus = result === 'SUCCESS' ? '正常' : '已停用'
  try {
    await ElMessageBox.confirm(
      `确认选择「${label}」？${targets.length} 个号码状态将变更为「${targetStatus}」，操作不可撤销。`,
      '确认批量申诉结果',
      { type: result === 'SUCCESS' ? 'success' : 'error', confirmButtonText: '确认', cancelButtonText: '返回' }
    )
    await batchResolveAppeal(targets.map(r => r.id), result)
    ElMessage.success(`已记录批量申诉结果：${label}，${targets.length} 个号码状态已更新为「${targetStatus}」`)
    showBatchAppealResultDialog.value = false
    loadList()
    loadStats()
  } catch {
    // 用户点击"返回"，不做处理
  }
}

// 回调
const onRegisterSuccess = () => {
  loadList()
  loadStats()
}

const onBindIpSuccess = () => {
  loadList()
  loadStats()
}

const onAssignSuccess = () => {
  loadList()
  loadStats()
}

onMounted(() => {
  // 按初始 tab 设置分组默认折叠状态
  const initCollapse = TAB_COLLAPSE_CONFIG[activeTab.value]
  if (initCollapse) {
    ;[cloudCollapsed.value, nurtureCollapsed.value, usageCollapsed.value] = initCollapse
  }
  // 支持从渠道管理页跳转并自动筛选该渠道
  const channelId = route.query.purchaseChannelId
  if (channelId) {
    searchParams.purchaseChannelId = Number(channelId)
  }
  loadList()
  loadStats()
  loadChannels()
})
</script>

<style scoped>
/* ── 统计区块 ── */
.stats-section {
  margin-bottom: 8px;
}

.stats-section-header {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 5px;
}

.stats-section-title {
  font-size: 11px;
  font-weight: 600;
  color: #909399;
  letter-spacing: 1px;
  text-transform: uppercase;
}

.stats-section-total {
  font-size: 11px;
  color: #c0c4cc;
}

.stats-section-total strong {
  color: #606266;
  font-size: 12px;
}

/* ── 扁平面板 ── */
.stats-panel {
  display: flex;
  align-items: stretch;
  background: #ffffff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  overflow: hidden;
}

.stat-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 10px 6px 8px;
  position: relative;
  transition: background 0.15s;
}

.stat-item:hover {
  background: #f5f7fa;
}

.stat-item-clickable {
  cursor: pointer;
}

.stat-item-clickable:hover {
  background: #f3eefe;
}

.stat-divider {
  width: 1px;
  background: #ebeef5;
  flex-shrink: 0;
  margin: 8px 0;
}

/* ── 色点 ── */
.stat-dot {
  display: inline-block;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  margin-bottom: 4px;
  flex-shrink: 0;
}

/* ── 数值 ── */
.stat-value-row {
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 4px;
}

.stat-value {
  font-size: 22px;
  font-weight: 700;
  line-height: 1.2;
  color: #1d2129;
}

.stat-value-danger {
  color: #f56c6c;
}

.stat-rate {
  font-size: 10px;
  font-weight: 400;
  color: #c0c4cc;
  cursor: default;
  white-space: nowrap;
}

.stat-rate:hover {
  color: #909399;
}

.stat-label {
  font-size: 11px;
  font-weight: 500;
  color: #606266;
  margin-top: 2px;
}

.stat-desc {
  font-size: 10px;
  color: #c0c4cc;
  margin-top: 1px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  min-width: 0;
  overflow: hidden;
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.selection-tip {
  font-size: 13px;
  color: #409eff;
}

.filter-bar {
  display: flex;
  align-items: center;
}

.stage-tabs {
  flex: 1;
  min-width: 0;
}

.stage-tabs :deep(.el-tabs__header) {
  margin-bottom: 0;
}

.stage-tabs :deep(.el-tabs__nav-wrap) {
  margin-bottom: 0;
}

/* 仅用 Tab 做阶段筛选，不展示 Tab 内容区 */
.stage-tabs :deep(.el-tabs__content) {
  display: none;
}

.stage-tabs :deep(.el-tabs__item) {
  padding: 0 16px;
}

.tab-badge {
  margin-left: 6px;
  vertical-align: middle;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.filter-item-channel {
  flex-shrink: 0;
}

.filter-label {
  font-size: 13px;
  color: #606266;
  white-space: nowrap;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.text-muted {
  color: #c0c4cc;
}

.offline-text {
  color: #f56c6c;
  font-weight: 500;
}

/* 时间字段两行显示 */
.datetime-cell {
  display: flex;
  flex-direction: column;
  line-height: 1.4;
}

.dt-date {
  font-size: 13px;
  color: #303133;
}

.dt-time {
  font-size: 11px;
  color: #909399;
}

.acquisition-count {
  font-size: 14px;
  font-weight: 600;
  color: #409eff;
}

:deep(.offline-row) {
  background-color: #fef0f0 !important;
}

:deep(.banned-row) {
  background-color: #fdf6ec !important;
}

/* ── 分组表头颜色 ── */
:deep(.group-header-cloud) {
  background: linear-gradient(135deg, #e8f4ff 0%, #d6eaff 100%) !important;
  color: #2d7dd2 !important;
  border-bottom: 2px solid #bbd9ff !important;
}

:deep(.group-header-nurture) {
  background: linear-gradient(135deg, #fdf3e3 0%, #fde9c8 100%) !important;
  color: #b8720a !important;
  border-bottom: 2px solid #f5cc88 !important;
}

:deep(.group-header-usage) {
  background: linear-gradient(135deg, #f5e8ff 0%, #ead4ff 100%) !important;
  color: #7b1fa2 !important;
  border-bottom: 2px solid #d6a8f5 !important;
}

/* ── 分组表头内容布局 ── */
.group-header-content {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 100%;
}

.group-header-title {
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.3px;
}

/* 展开/收起 chip 徽章 */
.group-toggle-chip {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  font-size: 11px;
  font-weight: 500;
  padding: 2px 9px;
  border-radius: 20px;
  cursor: pointer;
  user-select: none;
  line-height: 1.6;
  transition: background 0.15s, opacity 0.15s;
}

.group-toggle-chip--cloud {
  background: rgba(64, 158, 255, 0.14);
  color: #409eff;
  border: 1px solid rgba(64, 158, 255, 0.3);
}

.group-toggle-chip--cloud:hover {
  background: rgba(64, 158, 255, 0.25);
}

.group-toggle-chip--nurture {
  background: rgba(230, 162, 60, 0.14);
  color: #d48806;
  border: 1px solid rgba(230, 162, 60, 0.3);
}

.group-toggle-chip--nurture:hover {
  background: rgba(230, 162, 60, 0.25);
}

.group-toggle-chip--usage {
  background: rgba(156, 39, 176, 0.12);
  color: #9c27b0;
  border: 1px solid rgba(156, 39, 176, 0.25);
}

.group-toggle-chip--usage:hover {
  background: rgba(156, 39, 176, 0.22);
}

/* 折叠时 chip 稍微变暗以作区分 */
.group-toggle-chip.is-collapsed {
  opacity: 0.75;
}

/* 分组列头 cell 内容不裁剪 */
:deep(.group-header-cloud .cell),
:deep(.group-header-nurture .cell),
:deep(.group-header-usage .cell) {
  overflow: visible;
  padding: 6px 8px;
}

/* 申诉中：状态列下方的时间信息 */
.appeal-time-info {
  margin-top: 4px;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.appeal-time-label {
  font-size: 11px;
  color: #909399;
  line-height: 1.4;
}

.appeal-elapsed {
  font-size: 11px;
  color: #e6a23c;
  font-weight: 600;
  line-height: 1.4;
}

/* 填写申诉结果弹窗 */
.appeal-result-options {
  display: flex;
  gap: 16px;
}

.appeal-result-card {
  flex: 1;
  cursor: pointer;
  transition: transform 0.15s, box-shadow 0.15s;
  border-radius: 8px;
}

.appeal-result-card:hover {
  transform: translateY(-2px);
}

.appeal-success:hover {
  box-shadow: 0 4px 16px rgba(103, 194, 58, 0.3) !important;
  border-color: #67c23a !important;
}

.appeal-failure:hover {
  box-shadow: 0 4px 16px rgba(245, 108, 108, 0.3) !important;
  border-color: #f56c6c !important;
}

.appeal-result-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 10px;
}

.appeal-success .appeal-result-title {
  color: #67c23a;
}

.appeal-failure .appeal-result-title {
  color: #f56c6c;
}

.appeal-result-desc {
  font-size: 13px;
  color: #606266;
  line-height: 1.7;
  margin-bottom: 12px;
}

.appeal-result-desc p {
  margin: 0 0 4px 0;
}

.appeal-result-action {
  font-size: 12px;
  color: #909399;
  border-top: 1px solid #ebeef5;
  padding-top: 8px;
}
</style>
