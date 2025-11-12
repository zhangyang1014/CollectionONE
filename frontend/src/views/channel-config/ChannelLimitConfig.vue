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
            <el-select v-model="filters.channel" placeholder="全部" clearable style="width: 150px;">
              <el-option label="全部" value="" />
              <el-option label="短信" value="sms" />
              <el-option label="RCS" value="rcs" />
              <el-option label="WhatsApp" value="whatsapp" />
              <el-option label="电话外呼" value="call" />
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

const tenantStore = useTenantStore()

// 加载状态
const loading = ref(false)

// 筛选器
const filters = ref({
  channel: '',
  queue: ''
})

// 队列列表
const queues = ref<any[]>([])

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
  return configs.value.filter(config => {
    if (filters.value.channel && config.channel !== filters.value.channel) return false
    if (filters.value.queue && config.queue_code !== filters.value.queue) return false
    return true
  })
})

// 获取渠道标签
const getChannelLabel = (channel: string) => {
  const labels: Record<string, string> = {
    'sms': '短信',
    'rcs': 'RCS',
    'whatsapp': 'WhatsApp',
    'call': '电话外呼'
  }
  return labels[channel] || channel
}

// 获取渠道标签类型
const getChannelTagType = (channel: string) => {
  const types: Record<string, string> = {
    'sms': 'primary',
    'rcs': 'success',
    'whatsapp': 'success',
    'call': 'warning'
  }
  return types[channel] || 'info'
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
      // 生成配置数据
      generateConfigs()
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

// 生成配置数据（每个渠道下的队列不重复）
const generateConfigs = () => {
  const channels = ['sms', 'rcs', 'whatsapp', 'call']
  const newConfigs: any[] = []
  let configId = 1

  channels.forEach(channel => {
    queues.value.forEach(queue => {
      // 查找是否已存在该渠道和队列的配置
      const existing = configs.value.find(
        c => c.channel === channel && c.queue_code === queue.queue_code
      )

      if (existing) {
        // 保留现有配置
        newConfigs.push({
          ...existing,
          queue_name: queue.queue_name
        })
      } else {
        // 创建新配置
        newConfigs.push({
          id: configId++,
          channel: channel,
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

  configs.value = newConfigs
  // 保存原始配置用于比较
  originalConfigs.value = JSON.parse(JSON.stringify(newConfigs))
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

  try {
    loading.value = true
    
    // 这里调用API保存单行配置
    // await saveChannelLimit(row)
    
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 300))
    
    // 更新该行的原始配置
    const originalIndex = originalConfigs.value.findIndex(orig => orig.id === row.id)
    if (originalIndex !== -1) {
      originalConfigs.value[originalIndex] = JSON.parse(JSON.stringify(row))
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

