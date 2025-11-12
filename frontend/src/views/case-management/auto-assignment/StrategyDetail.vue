<template>
  <div class="strategy-detail">
    <!-- 基础信息 -->
    <el-descriptions title="基础信息" :column="2" border>
      <el-descriptions-item label="策略名称">{{ strategy.strategy_name }}</el-descriptions-item>
      <el-descriptions-item label="所属队列">{{ getQueueName(strategy.queue_id) }}</el-descriptions-item>
      <el-descriptions-item label="状态">
        <el-tag :type="strategy.is_enabled ? 'success' : 'info'">
          {{ strategy.is_enabled ? '开启' : '关闭' }}
        </el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="启动时间">{{ strategy.start_time }}</el-descriptions-item>
      <el-descriptions-item label="创建时间">{{ strategy.created_at }}</el-descriptions-item>
      <el-descriptions-item label="更新时间">{{ strategy.updated_at || '--' }}</el-descriptions-item>
    </el-descriptions>

    <!-- 分配条件 -->
    <el-divider content-position="left">分配条件</el-divider>
    <el-card shadow="never" class="condition-card">
      <div v-if="mockConditions.length > 0">
        <div v-for="(group, groupIndex) in mockConditions" :key="groupIndex" class="condition-group">
          <el-tag type="info" size="small" style="margin-bottom: 10px;">
            条件组 {{ groupIndex + 1 }}（组内 AND）
          </el-tag>
          <el-table :data="group" border size="small">
            <el-table-column prop="field_name" label="字段" width="150" />
            <el-table-column prop="operator" label="运算符" width="120" />
            <el-table-column prop="value" label="值" />
          </el-table>
          <div v-if="groupIndex < mockConditions.length - 1" class="or-divider">
            <el-tag type="warning" size="small">OR</el-tag>
          </div>
        </div>
      </div>
      <el-empty v-else description="未设置分配条件" />
    </el-card>

    <!-- 分配策略 -->
    <el-divider content-position="left">分配策略</el-divider>
    <el-descriptions :column="2" border>
      <el-descriptions-item label="分配层级">{{ strategy.target_level }}</el-descriptions-item>
      <el-descriptions-item label="分配方式">
        <el-tag type="primary">{{ mockStrategy.assignment_mode }}</el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="目标机构/小组" :span="2">
        {{ strategy.target_names }}
      </el-descriptions-item>
      <el-descriptions-item label="优先分配（粘连）">
        <el-tag v-if="mockStrategy.enable_stickiness" type="success">开启</el-tag>
        <el-tag v-else type="info">关闭</el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="粘连粒度" v-if="mockStrategy.enable_stickiness">
        {{ mockStrategy.stickiness_level }}
      </el-descriptions-item>
      <el-descriptions-item label="容量限制">
        <el-tag :type="mockStrategy.capacity_mode === 'hard' ? 'danger' : 'warning'">
          {{ getCapacityModeText(mockStrategy.capacity_mode) }}
        </el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="案件排序">
        {{ mockStrategy.sort_by }}
      </el-descriptions-item>
    </el-descriptions>

    <!-- 执行历史 -->
    <el-divider content-position="left">执行历史（最近 10 次）</el-divider>
    <el-table :data="mockExecutionHistory" border size="small">
      <el-table-column prop="batch_id" label="批次号" width="200" />
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="getExecutionStatusType(row.status)" size="small">
            {{ getExecutionStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="total_cases" label="总案件数" width="100" align="center" />
      <el-table-column prop="assigned_cases" label="已分配" width="100" align="center" />
      <el-table-column prop="failed_cases" label="失败" width="100" align="center" />
      <el-table-column prop="execution_time" label="执行时间" width="180" />
      <el-table-column prop="duration" label="耗时" width="100" />
      <el-table-column label="操作" width="120" align="center">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="viewExecutionDetail(row)">
            查看详情
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 操作日志 -->
    <el-divider content-position="left">操作日志（最近 20 条）</el-divider>
    <el-timeline>
      <el-timeline-item
        v-for="log in mockOperationLogs"
        :key="log.id"
        :timestamp="log.created_at"
        placement="top"
      >
        <el-card shadow="never" size="small">
          <div style="display: flex; justify-content: space-between; align-items: center;">
            <div>
              <el-tag :type="getLogActionType(log.action)" size="small" style="margin-right: 10px;">
                {{ getLogActionText(log.action) }}
              </el-tag>
              <span>{{ log.operator_name }}</span>
            </div>
            <el-text type="info" size="small">{{ log.operator_ip }}</el-text>
          </div>
          <div v-if="log.remarks" style="margin-top: 8px; color: #606266;">
            {{ log.remarks }}
          </div>
        </el-card>
      </el-timeline-item>
    </el-timeline>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

interface Props {
  strategy: any
}

const props = defineProps<Props>()

// Mock 数据
const mockConditions = ref([
  [
    { field_name: '逾期天数', operator: 'between', value: '1-30' },
    { field_name: 'App名称', operator: 'in', value: 'App A, App B' },
  ],
  [
    { field_name: '逾期金额', operator: '>', value: '10000' },
  ],
])

const mockStrategy = ref({
  assignment_mode: '按数量平均',
  enable_stickiness: true,
  stickiness_level: '同客户',
  capacity_mode: 'soft',
  sort_by: '逾期天数倒序',
})

const mockExecutionHistory = ref([
  {
    id: 1,
    batch_id: 'BATCH_20251112_090500_001',
    status: 'success',
    total_cases: 1250,
    assigned_cases: 1180,
    failed_cases: 0,
    execution_time: '2025-11-12 09:05:00',
    duration: '2.3s',
  },
  {
    id: 2,
    batch_id: 'BATCH_20251111_090500_001',
    status: 'success',
    total_cases: 1180,
    assigned_cases: 1180,
    failed_cases: 0,
    execution_time: '2025-11-11 09:05:00',
    duration: '2.1s',
  },
  {
    id: 3,
    batch_id: 'BATCH_20251110_090500_001',
    status: 'partial',
    total_cases: 1300,
    assigned_cases: 1250,
    failed_cases: 50,
    execution_time: '2025-11-10 09:05:00',
    duration: '3.5s',
  },
  {
    id: 4,
    batch_id: 'BATCH_20251109_090500_001',
    status: 'success',
    total_cases: 1280,
    assigned_cases: 1250,
    failed_cases: 0,
    execution_time: '2025-11-09 09:05:00',
    duration: '2.8s',
  },
  {
    id: 5,
    batch_id: 'BATCH_20251108_090500_001',
    status: 'success',
    total_cases: 1200,
    assigned_cases: 1180,
    failed_cases: 0,
    execution_time: '2025-11-08 09:05:00',
    duration: '2.2s',
  },
  {
    id: 6,
    batch_id: 'BATCH_20251107_090500_001',
    status: 'success',
    total_cases: 1220,
    assigned_cases: 1200,
    failed_cases: 0,
    execution_time: '2025-11-07 09:05:00',
    duration: '2.4s',
  },
  {
    id: 7,
    batch_id: 'BATCH_20251106_090500_001',
    status: 'partial',
    total_cases: 1180,
    assigned_cases: 1150,
    failed_cases: 30,
    execution_time: '2025-11-06 09:05:00',
    duration: '3.1s',
  },
])

const mockOperationLogs = ref([
  {
    id: 1,
    action: 'enable',
    operator_name: '张三',
    operator_ip: '192.168.1.100',
    created_at: '2025-11-12 08:30:00',
    remarks: '启用策略',
  },
  {
    id: 2,
    action: 'update',
    operator_name: '张三',
    operator_ip: '192.168.1.100',
    created_at: '2025-11-11 15:20:00',
    remarks: '修改分配条件：调整逾期天数范围为 1-30 天',
  },
  {
    id: 3,
    action: 'disable',
    operator_name: '李四',
    operator_ip: '192.168.1.101',
    created_at: '2025-11-11 10:00:00',
    remarks: '临时关闭策略进行调整',
  },
  {
    id: 4,
    action: 'update',
    operator_name: '李四',
    operator_ip: '192.168.1.101',
    created_at: '2025-11-11 09:45:00',
    remarks: '修改容量限制模式：从硬限制改为软限制',
  },
  {
    id: 5,
    action: 'enable',
    operator_name: '王五',
    operator_ip: '192.168.1.102',
    created_at: '2025-11-10 14:30:00',
    remarks: '重新启用策略',
  },
  {
    id: 6,
    action: 'update',
    operator_name: '王五',
    operator_ip: '192.168.1.102',
    created_at: '2025-11-10 14:00:00',
    remarks: '添加目标小组：电催组2',
  },
  {
    id: 7,
    action: 'disable',
    operator_name: '张三',
    operator_ip: '192.168.1.100',
    created_at: '2025-11-10 13:00:00',
    remarks: '暂停策略：发现分配异常',
  },
  {
    id: 8,
    action: 'update',
    operator_name: '张三',
    operator_ip: '192.168.1.100',
    created_at: '2025-11-10 09:30:00',
    remarks: '开启优先分配（粘连）功能',
  },
  {
    id: 9,
    action: 'create',
    operator_name: '李四',
    operator_ip: '192.168.1.101',
    created_at: '2025-11-10 08:30:00',
    remarks: '创建策略：C队列-机构A优先分配',
  },
])

// 获取队列名称
const getQueueName = (queueId: number) => {
  const queueMap: Record<number, string> = {
    1: 'C队列',
    2: 'S0队列',
    3: 'S1队列',
    4: 'L1队列',
    5: 'M1队列',
  }
  return queueMap[queueId] || '未知队列'
}

// 获取容量模式文本
const getCapacityModeText = (mode: string) => {
  const modeMap: Record<string, string> = {
    hard: '硬限制',
    soft: '软限制（允许超10%）',
    unlimited: '无限制',
  }
  return modeMap[mode] || '未知'
}

// 获取执行状态类型
const getExecutionStatusType = (status: string) => {
  const typeMap: Record<string, any> = {
    success: 'success',
    failed: 'danger',
    partial: 'warning',
    running: 'info',
  }
  return typeMap[status] || 'info'
}

// 获取执行状态文本
const getExecutionStatusText = (status: string) => {
  const textMap: Record<string, string> = {
    success: '成功',
    failed: '失败',
    partial: '部分成功',
    running: '执行中',
  }
  return textMap[status] || '未知'
}

// 获取日志操作类型
const getLogActionType = (action: string) => {
  const typeMap: Record<string, any> = {
    create: 'success',
    update: 'primary',
    delete: 'danger',
    enable: 'success',
    disable: 'warning',
  }
  return typeMap[action] || 'info'
}

// 获取日志操作文本
const getLogActionText = (action: string) => {
  const textMap: Record<string, string> = {
    create: '创建',
    update: '编辑',
    delete: '删除',
    enable: '启用',
    disable: '关闭',
  }
  return textMap[action] || '未知操作'
}

// 查看执行详情
const viewExecutionDetail = (row: any) => {
  ElMessage.info('执行详情功能开发中...')
}
</script>

<style scoped>
.strategy-detail {
  padding: 20px;
}

.condition-card {
  margin-bottom: 20px;
}

.condition-group {
  margin-bottom: 20px;
}

.or-divider {
  text-align: center;
  margin: 15px 0;
}
</style>

