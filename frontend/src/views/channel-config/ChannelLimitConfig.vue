<template>
  <div class="channel-limit-config">
    <div class="page-header">
      <h2>渠道发送限制配置</h2>
    </div>

    <div class="config-content">
      <!-- 筛选器 -->
      <div class="filters-section">
        <el-form :inline="true" :model="filters" class="filter-form">
          <el-form-item label="渠道">
            <el-select v-model="filters.channel" placeholder="全部" clearable style="width: 180px;">
              <el-option label="全部" value="" />
              <el-option
                v-for="channel in primaryChannels"
                :key="channel.value"
                :label="channel.label"
                :value="channel.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="队列">
            <el-select v-model="filters.queue" placeholder="全部" clearable style="width: 150px;">
              <el-option label="全部" value="" />
              <el-option 
                v-for="queue in availableQueues" 
                :key="queue.id" 
                :label="queue.queue_name" 
                :value="queue.queue_code" 
              />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button @click="handleResetFilters">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 配置表格 -->
      <div class="config-table-section">
        <el-table
          :data="filteredConfigs"
          border
          stripe
          style="width: 100%"
          v-loading="loading"
        >
          <el-table-column prop="channel" label="渠道" width="120" align="center">
            <template #default="{ row }">
              <el-tag :type="getChannelTagType(row.channel)">
                {{ getChannelLabel(row.channel) }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column prop="queue" label="队列" width="150" align="center">
            <template #default="{ row }">
              <el-tag>{{ row.queue_name }}</el-tag>
            </template>
          </el-table-column>
          
          <el-table-column label="每日每案件限制数量" width="200" align="center">
            <template #default="{ row }">
              <el-input-number
                v-model="row.daily_limit_per_case"
                :min="0"
                :max="100000"
                :step="10"
                controls-position="right"
                style="width: 100%"
                placeholder="不限制"
                :disabled="row.daily_limit_per_case_unlimited"
                @change="handleFieldChange"
              />
              <div style="margin-top: 4px;">
                <el-checkbox 
                  v-model="row.daily_limit_per_case_unlimited"
                  @change="handleUnlimitedChange(row, 'daily_limit_per_case')"
                >
                  不限制
                </el-checkbox>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column label="每日每联系人限制数量" width="200" align="center">
            <template #default="{ row }">
              <el-input-number
                v-model="row.daily_limit_per_contact"
                :min="0"
                :max="100000"
                :step="10"
                controls-position="right"
                style="width: 100%"
                placeholder="不限制"
                :disabled="row.daily_limit_per_contact_unlimited"
                @change="handleFieldChange"
              />
              <div style="margin-top: 4px;">
                <el-checkbox 
                  v-model="row.daily_limit_per_contact_unlimited"
                  @change="handleUnlimitedChange(row, 'daily_limit_per_contact')"
                >
                  不限制
                </el-checkbox>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column label="发送时间间隔（秒）" width="200" align="center">
            <template #default="{ row }">
              <el-input-number
                v-model="row.send_interval"
                :min="0"
                :max="86400"
                :step="10"
                controls-position="right"
                style="width: 100%"
                placeholder="不限制"
                :disabled="row.send_interval_unlimited"
                @change="handleFieldChange"
              />
              <div style="margin-top: 4px;">
                <el-checkbox 
                  v-model="row.send_interval_unlimited"
                  @change="handleUnlimitedChange(row, 'send_interval')"
                >
                  不限制
                </el-checkbox>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-switch
                v-model="row.enabled"
                @change="handleFieldChange"
              />
            </template>
          </el-table-column>
          
          <el-table-column label="操作" width="120" align="center" fixed="right">
            <template #default="{ row }">
              <el-button 
                type="primary" 
                size="small" 
                @click="handleSaveRow(row)"
                :disabled="!hasRowChanges(row)"
              >
                保存
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useTenantStore } from '@/stores/tenant'
import request from '@/utils/request'
import type { ChannelType } from '@/types/channel'

const tenantStore = useTenantStore()

// 加载状态
const loading = ref(false)

// 筛选器
const filters = ref({
  channel: '' as ChannelType | '',
  queue: ''
})

// 甲方一级触达渠道（与“甲方触达渠道管理”一级标签保持一致顺序）
const primaryChannels: Array<{ value: ChannelType; label: string; tagType: string }> = [
  { value: 'sms', label: '短信', tagType: 'primary' },
  { value: 'call', label: '电话外呼', tagType: 'warning' },
  { value: 'rcs', label: 'RCS', tagType: 'success' },
  { value: 'waba', label: 'WABA', tagType: 'success' },
  { value: 'whatsapp', label: 'WhatsApp', tagType: 'success' },
  { value: 'email', label: '邮件', tagType: 'info' },
  { value: 'mobile_calendar', label: '手机日历', tagType: 'info' }
]

const channelOrderMap = new Map(primaryChannels.map((item, index) => [item.value, index]))

const sortConfigsByChannel = (items: any[]) => {
  return [...items].sort((a, b) => {
    const orderA = channelOrderMap.get(a.channel) ?? Number.MAX_SAFE_INTEGER
    const orderB = channelOrderMap.get(b.channel) ?? Number.MAX_SAFE_INTEGER
    if (orderA !== orderB) {
      return orderA - orderB
    }
    return (a.queue_name || '').localeCompare(b.queue_name || '')
  })
}

// 队列列表
const queues = ref<any[]>([])

// 确保所有一级渠道与队列组合都有配置（若后端缺失则补齐）
const ensureAllChannelQueueConfigs = (items: any[]) => {
  const result = [...items]
  let nextId = Math.max(0, ...result.map(cfg => (typeof cfg.id === 'number' ? cfg.id : 0))) + 1

  primaryChannels.forEach(({ value: channel }) => {
    queues.value.forEach(queue => {
      const exists = result.find(
        cfg => cfg.channel === channel && cfg.queue_code === queue.queue_code
      )
      if (!exists) {
        result.push({
          id: nextId++,
          channel,
          queue_id: queue.id,
          queue_code: queue.queue_code,
          queue_name: queue.queue_name,
          daily_limit_per_case: null,
          daily_limit_per_case_unlimited: true,
          daily_limit_per_contact: null,
          daily_limit_per_contact_unlimited: true,
          send_interval: null,
          send_interval_unlimited: true,
          enabled: true
        })
      }
    })
  })

  return sortConfigsByChannel(result)
}

// 可用的队列选项（用于筛选器）
const availableQueues = computed(() => {
  return queues.value.filter(q => q.is_active)
})

// 配置数据
const configs = ref<any[]>([])
// 原始配置数据（用于比较是否有修改）
const originalConfigs = ref<any[]>([])

// 筛选后的配置
const filteredConfigs = computed(() => {
  const filtered = configs.value.filter(config => {
    if (filters.value.channel && config.channel !== filters.value.channel) return false
    if (filters.value.queue && config.queue_code !== filters.value.queue) return false
    return true
  })
  return sortConfigsByChannel(filtered)
})

// 监听队列变化，自动补齐缺失渠道-队列组合
watch(queues, (newVal, oldVal) => {
  if (newVal !== oldVal) {
    configs.value = ensureAllChannelQueueConfigs(configs.value)
    originalConfigs.value = JSON.parse(JSON.stringify(configs.value))
  }
})

// 监听配置与队列同时变化，确保渠道列完整（避免仅显示部分渠道）
watch(
  () => [configs.value, queues.value],
  ([newConfigs]) => {
    const ensured = ensureAllChannelQueueConfigs(newConfigs)
    // 通过长度与核心键比对，避免无意义的重复赋值触发
    const changed =
      ensured.length !== newConfigs.length ||
      ensured.some((cfg: any, idx: number) => {
        const orig = newConfigs[idx]
        return !orig || cfg.channel !== orig.channel || cfg.queue_code !== orig.queue_code
      })
    if (changed) {
      configs.value = ensured
      originalConfigs.value = JSON.parse(JSON.stringify(configs.value))
    }
  }
)

// 获取渠道标签
const getChannelLabel = (channel: string) => {
  const found = primaryChannels.find(item => item.value === channel)
  return found?.label || channel
}

// 获取渠道标签类型
const getChannelTagType = (channel: string) => {
  const found = primaryChannels.find(item => item.value === channel)
  return found?.tagType || 'info'
}

// 重置筛选器
const handleResetFilters = () => {
  filters.value = {
    channel: '',
    queue: ''
  }
}

// 字段变化处理
const handleFieldChange = () => {
  // 字段变化时自动触发hasChanges计算
}

// 不限制选项变化处理
const handleUnlimitedChange = (row: any, field: string) => {
  if (field === 'daily_limit_per_case') {
    if (row.daily_limit_per_case_unlimited) {
      // 勾选"不限制"时，清空数值并禁用输入框
      row.daily_limit_per_case = null
    }
    // 取消"不限制"时，输入框会自动启用（通过:disabled绑定）
  } else if (field === 'daily_limit_per_contact') {
    if (row.daily_limit_per_contact_unlimited) {
      // 勾选"不限制"时，清空数值并禁用输入框
      row.daily_limit_per_contact = null
    }
  } else if (field === 'send_interval') {
    if (row.send_interval_unlimited) {
      // 勾选"不限制"时，清空数值并禁用输入框
      row.send_interval = null
    }
  }
  handleFieldChange()
}

// 加载队列列表
const loadQueues = async () => {
  if (!tenantStore.currentTenantId) {
    ElMessage.warning('请先选择甲方')
    return
  }

  try {
    loading.value = true
    // 使用正确的API路径：/api/v1/tenants/{tenant_id}/queues
    const response: any = await request({
      url: `/api/v1/tenants/${tenantStore.currentTenantId}/queues`,
      method: 'get'
    })
    
    // API直接返回数组，不是{data: [...]}格式
    const queueData = Array.isArray(response) ? response : (response.data || [])
    
    if (queueData.length > 0 || Array.isArray(queueData)) {
      queues.value = queueData
      // 加载配置数据
      await loadConfigs()
    } else {
      queues.value = []
      ElMessage.warning('暂无队列数据')
    }
  } catch (error: any) {
    console.error('加载队列失败：', error)
    ElMessage.error('加载队列失败：' + (error.message || '未知错误'))
    queues.value = []
  } finally {
    loading.value = false
  }
}

// 加载配置数据
const loadConfigs = async () => {
  if (!tenantStore.currentTenantId) {
    return
  }

  try {
    loading.value = true
    // 调用后端API获取配置数据
    const response: any = await request({
      url: `/api/v1/channel-limit-configs/tenants/${tenantStore.currentTenantId}`,
      method: 'get'
    })
    
    // 处理响应数据
    const configData = Array.isArray(response) ? response : (response.data || [])
    
    if (configData.length > 0) {
      // 如果后端有数据，使用后端数据并补齐缺失渠道
      const normalized = configData.map((config: any) => ({
        ...config,
        // 确保所有字段都存在
        daily_limit_per_case: config.daily_limit_per_case ?? null,
        daily_limit_per_case_unlimited: config.daily_limit_per_case_unlimited ?? (config.daily_limit_per_case == null),
        daily_limit_per_contact: config.daily_limit_per_contact ?? null,
        daily_limit_per_contact_unlimited: config.daily_limit_per_contact_unlimited ?? (config.daily_limit_per_contact == null),
        send_interval: config.send_interval ?? null,
        send_interval_unlimited: config.send_interval_unlimited ?? (config.send_interval == null),
        enabled: config.enabled ?? true
      }))
      configs.value = ensureAllChannelQueueConfigs(normalized)
    } else {
      // 如果后端没有数据，根据队列生成默认配置
      generateConfigs()
    }
    // 统一补齐渠道-队列组合并排序（防止后端返回缺失项）
    configs.value = ensureAllChannelQueueConfigs(configs.value)
    
    // 保存原始配置用于比较
    originalConfigs.value = JSON.parse(JSON.stringify(configs.value))
  } catch (error: any) {
    console.error('加载配置失败：', error)
    // 如果API调用失败，使用前端生成的方式
    generateConfigs()
  } finally {
    loading.value = false
  }
}

// 生成配置数据（每个渠道下的队列不重复）
const generateConfigs = () => {
  const newConfigs: any[] = ensureAllChannelQueueConfigs([])

  configs.value = sortConfigsByChannel(newConfigs)
  // 保存原始配置用于比较
  originalConfigs.value = JSON.parse(JSON.stringify(configs.value))
}

// 检查单行是否有修改
const hasRowChanges = (row: any) => {
  // 通过ID匹配原始配置（因为筛选后索引可能不同）
  const originalRow = originalConfigs.value.find(
    orig => orig.id === row.id
  )
  
  if (!originalRow) return false
  
  return (
    row.daily_limit_per_case !== originalRow.daily_limit_per_case ||
    row.daily_limit_per_case_unlimited !== originalRow.daily_limit_per_case_unlimited ||
    row.daily_limit_per_contact !== originalRow.daily_limit_per_contact ||
    row.daily_limit_per_contact_unlimited !== originalRow.daily_limit_per_contact_unlimited ||
    row.send_interval !== originalRow.send_interval ||
    row.send_interval_unlimited !== originalRow.send_interval_unlimited ||
    row.enabled !== originalRow.enabled
  )
}

// 保存单行
const handleSaveRow = async (row: any) => {
  if (!hasRowChanges(row)) {
    ElMessage.info('该行没有需要保存的修改')
    return
  }

  if (!tenantStore.currentTenantId) {
    ElMessage.warning('请先选择甲方')
    return
  }

  try {
    loading.value = true
    
    // 准备保存的数据
    const saveData: any = {
      tenant_id: tenantStore.currentTenantId,
      channel: row.channel,
      queue_id: row.queue_id,
      queue_code: row.queue_code,
      queue_name: row.queue_name,
      daily_limit_per_case: row.daily_limit_per_case_unlimited ? null : row.daily_limit_per_case,
      daily_limit_per_case_unlimited: row.daily_limit_per_case_unlimited,
      daily_limit_per_contact: row.daily_limit_per_contact_unlimited ? null : row.daily_limit_per_contact,
      daily_limit_per_contact_unlimited: row.daily_limit_per_contact_unlimited,
      send_interval: row.send_interval_unlimited ? null : row.send_interval,
      send_interval_unlimited: row.send_interval_unlimited,
      enabled: row.enabled
    }
    
    // 调用后端API保存配置
    let savedConfig: any
    if (row.id) {
      // 更新现有配置
      const response: any = await request({
        url: `/api/v1/channel-limit-configs/${row.id}`,
        method: 'put',
        data: saveData
      })
      savedConfig = Array.isArray(response) ? response : (response.data || response)
    } else {
      // 创建新配置
      const response: any = await request({
        url: `/api/v1/channel-limit-configs`,
        method: 'post',
        data: saveData
      })
      savedConfig = Array.isArray(response) ? response : (response.data || response)
    }
    
    // 更新本地数据
    const index = configs.value.findIndex(c => c.id === row.id || 
      (c.channel === row.channel && c.queue_code === row.queue_code))
    if (index !== -1) {
      configs.value[index] = {
        ...savedConfig,
        daily_limit_per_case: savedConfig.daily_limit_per_case ?? null,
        daily_limit_per_case_unlimited: savedConfig.daily_limit_per_case_unlimited ?? (savedConfig.daily_limit_per_case == null),
        daily_limit_per_contact: savedConfig.daily_limit_per_contact ?? null,
        daily_limit_per_contact_unlimited: savedConfig.daily_limit_per_contact_unlimited ?? (savedConfig.daily_limit_per_contact == null),
        send_interval: savedConfig.send_interval ?? null,
        send_interval_unlimited: savedConfig.send_interval_unlimited ?? (savedConfig.send_interval == null),
        enabled: savedConfig.enabled ?? true
      }
    }
    
    // 更新该行的原始配置
    const originalIndex = originalConfigs.value.findIndex(orig => orig.id === row.id || 
      (orig.channel === row.channel && orig.queue_code === row.queue_code))
    if (originalIndex !== -1) {
      originalConfigs.value[originalIndex] = JSON.parse(JSON.stringify(configs.value[index]))
    } else if (index !== -1) {
      originalConfigs.value.push(JSON.parse(JSON.stringify(configs.value[index])))
    }
    
    ElMessage.success('保存成功')
  } catch (error: any) {
    console.error('保存失败：', error)
    ElMessage.error('保存失败：' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 监听甲方变化
watch(() => tenantStore.currentTenantId, (newTenantId) => {
  if (newTenantId) {
    loadQueues()
  } else {
    configs.value = []
    originalConfigs.value = []
  }
}, { immediate: true })

onMounted(() => {
  if (tenantStore.currentTenantId) {
    loadQueues()
    }
})
</script>

<style scoped>
.channel-limit-config {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  background: #ffffff;
  padding: 16px 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  color: #303133;
}

.config-content {
  background: #ffffff;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.filters-section {
  padding: 16px 20px;
  border-bottom: 1px solid #e4e7ed;
  background: #fafafa;
}

.filter-form {
  margin: 0;
}

.config-table-section {
  padding: 20px;
}

.config-table-section :deep(.el-table) {
  font-size: 14px;
}

.config-table-section :deep(.el-table th) {
  background: #f5f7fa;
  color: #606266;
  font-weight: 600;
}
</style>

