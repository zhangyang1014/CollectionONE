<template>
  <div class="infinity-config-container">
    <el-card class="config-card">
      <template #header>
        <div class="card-header">
          <span class="title">
            <el-icon><Phone /></el-icon>
            Infinity外呼配置
          </span>
          <el-button 
            v-if="!hasConfig" 
            type="primary" 
            @click="showConfigDialog = true"
            :disabled="!currentTenantId"
          >
            创建配置
          </el-button>
          <div v-else>
            <el-button @click="showConfigDialog = true">编辑配置</el-button>
            <el-button @click="testConnection" :loading="testingConnection">测试连接</el-button>
          </div>
        </div>
      </template>

      <!-- 未选择甲方提示 -->
      <el-empty 
        v-if="!currentTenantId" 
        description="请先选择甲方"
        :image-size="120"
      />

      <!-- 配置信息展示 -->
      <div v-else-if="hasConfig" class="config-info">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="API地址">
            {{ config.api_url }}
          </el-descriptions-item>
          <el-descriptions-item label="访问令牌">
            {{ maskToken(config.access_token) }}
          </el-descriptions-item>
          <el-descriptions-item label="应用ID">
            {{ config.app_id || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="号段范围">
            <span v-if="config.caller_number_range_start && config.caller_number_range_end">
              {{ config.caller_number_range_start }} ~ {{ config.caller_number_range_end }}
            </span>
            <span v-else>-</span>
          </el-descriptions-item>
          <el-descriptions-item label="回调地址">
            {{ config.callback_url || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="最大并发呼叫数">
            {{ config.max_concurrent_calls }}
          </el-descriptions-item>
          <el-descriptions-item label="呼叫超时时间">
            {{ config.call_timeout_seconds }} 秒
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="config.is_active ? 'success' : 'danger'">
              {{ config.is_active ? '启用' : '禁用' }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <!-- 未配置提示 -->
      <el-empty 
        v-else 
        description="暂未配置 Infinity 外呼系统"
        :image-size="120"
      >
        <el-button type="primary" @click="showConfigDialog = true">
          立即配置
        </el-button>
      </el-empty>
    </el-card>

    <!-- 分机池管理卡片 -->
    <el-card v-if="hasConfig" class="extension-card">
      <template #header>
        <div class="card-header">
          <span class="title">
            <el-icon><Grid /></el-icon>
            分机池管理
          </span>
          <el-button type="primary" @click="showExtensionDialog = true">
            批量导入分机
          </el-button>
        </div>
      </template>

      <!-- 统计信息 -->
      <div class="statistics">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-statistic title="总分机数" :value="statistics.total_extensions">
              <template #prefix>
                <el-icon><Phone /></el-icon>
              </template>
            </el-statistic>
          </el-col>
          <el-col :span="6">
            <el-statistic title="空闲" :value="statistics.available_count">
              <template #prefix>
                <el-icon style="color: #67c23a"><SuccessFilled /></el-icon>
              </template>
            </el-statistic>
          </el-col>
          <el-col :span="6">
            <el-statistic title="使用中" :value="statistics.in_use_count">
              <template #prefix>
                <el-icon style="color: #409eff"><Loading /></el-icon>
              </template>
            </el-statistic>
          </el-col>
          <el-col :span="6">
            <el-statistic title="使用率" :value="statistics.usage_rate" suffix="%">
              <template #prefix>
                <el-icon><TrendCharts /></el-icon>
              </template>
            </el-statistic>
          </el-col>
        </el-row>
      </div>

      <!-- 分机列表 -->
      <div class="extension-list">
        <el-table :data="extensions" border style="width: 100%; margin-top: 20px">
          <el-table-column prop="infinity_extension_number" label="分机号" width="150" />
          <el-table-column prop="status" label="状态" width="120">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.status)">
                {{ getStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="current_collector_id" label="使用催员" width="120">
            <template #default="{ row }">
              {{ row.current_collector_id || '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="last_used_at" label="最后使用时间" width="180">
            <template #default="{ row }">
              {{ row.last_used_at ? formatDateTime(row.last_used_at) : '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="created_at" label="创建时间" width="180">
            <template #default="{ row }">
              {{ formatDateTime(row.created_at) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <el-button 
                v-if="row.status === 'in_use'" 
                link 
                type="warning" 
                @click="handleReleaseExtension(row)"
              >
                释放
              </el-button>
              <el-button 
                link 
                type="danger" 
                @click="handleDeleteExtension(row)"
                :disabled="row.status === 'in_use'"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>

    <!-- 配置对话框 -->
    <el-dialog
      v-model="showConfigDialog"
      :title="hasConfig ? '编辑配置' : '创建配置'"
      width="800px"
      @close="resetConfigForm"
    >
      <el-form ref="configFormRef" :model="configForm" :rules="configRules" label-width="140px">
        <el-form-item label="API地址" prop="api_url">
          <el-input v-model="configForm.api_url" placeholder="如：http://127.0.0.1:8080" />
        </el-form-item>
        <el-form-item label="访问令牌" prop="access_token">
          <el-input 
            v-model="configForm.access_token" 
            type="password" 
            show-password
            placeholder="请输入 Infinity API 访问令牌"
          />
        </el-form-item>
        <el-form-item label="应用ID" prop="app_id">
          <el-input v-model="configForm.app_id" placeholder="请输入应用ID" />
        </el-form-item>
        <el-form-item prop="caller_number_range_start">
          <template #label>
            <span>号段起始</span>
            <el-tooltip placement="top">
              <template #content>
                外显号码的起始号码，<br/>
                系统将从这个号段范围内选择号码作为主叫显示
              </template>
              <el-icon style="margin-left: 4px; cursor: help;"><QuestionFilled /></el-icon>
            </el-tooltip>
          </template>
          <el-input v-model="configForm.caller_number_range_start" placeholder="如：4001234000" />
        </el-form-item>
        <el-form-item prop="caller_number_range_end">
          <template #label>
            <span>号段结束</span>
            <el-tooltip placement="top">
              <template #content>
                外显号码的结束号码，<br/>
                与起始号码组成可用的号码范围
              </template>
              <el-icon style="margin-left: 4px; cursor: help;"><QuestionFilled /></el-icon>
            </el-tooltip>
          </template>
          <el-input v-model="configForm.caller_number_range_end" placeholder="如：4001234999" />
        </el-form-item>
        <el-form-item prop="callback_url">
          <template #label>
            <span>回调地址</span>
            <el-tooltip placement="top">
              <template #content>
                Infinity系统通话结束后，<br/>
                推送通话记录的回调URL地址
              </template>
              <el-icon style="margin-left: 4px; cursor: help;"><QuestionFilled /></el-icon>
            </el-tooltip>
          </template>
          <el-input 
            v-model="configForm.callback_url" 
            placeholder="如：http://your-domain.com/api/v1/infinity/callback/call-record"
          />
        </el-form-item>
        <el-form-item prop="max_concurrent_calls">
          <template #label>
            <span>最大并发呼叫数</span>
            <el-tooltip placement="top">
              <template #content>
                系统允许的最大同时外呼数量，<br/>
                超过此数量的外呼请求将被限制
              </template>
              <el-icon style="margin-left: 4px; cursor: help;"><QuestionFilled /></el-icon>
            </el-tooltip>
          </template>
          <el-input-number 
            v-model="configForm.max_concurrent_calls" 
            :min="1" 
            :max="1000"
          />
        </el-form-item>
        <el-form-item prop="call_timeout_seconds">
          <template #label>
            <span>呼叫超时时间</span>
            <el-tooltip placement="top">
              <template #content>
                发起呼叫后等待接听的超时时间，<br/>
                超时后将自动挂断
              </template>
              <el-icon style="margin-left: 4px; cursor: help;"><QuestionFilled /></el-icon>
            </el-tooltip>
          </template>
          <el-input-number 
            v-model="configForm.call_timeout_seconds" 
            :min="10" 
            :max="300"
          />
          <span style="margin-left: 10px; color: #909399">秒</span>
        </el-form-item>
        <el-form-item label="是否启用" prop="is_active">
          <el-switch v-model="configForm.is_active" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showConfigDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSaveConfig" :loading="saving">
          保存
        </el-button>
      </template>
    </el-dialog>

    <!-- 批量导入分机对话框 -->
    <el-dialog
      v-model="showExtensionDialog"
      title="批量导入分机号"
      width="600px"
      @close="resetExtensionForm"
    >
      <el-form :model="extensionForm" label-width="100px">
        <el-form-item label="分机号列表">
          <el-input
            v-model="extensionForm.extensionText"
            type="textarea"
            :rows="10"
            placeholder="每行一个分机号，例如：&#10;8001&#10;8002&#10;8003"
          />
          <div style="margin-top: 10px; color: #909399; font-size: 12px">
            每行输入一个分机号，系统会自动去重
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showExtensionDialog = false">取消</el-button>
        <el-button type="primary" @click="handleImportExtensions" :loading="importing">
          导入
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { useTenantStore } from '@/stores/tenant'
import {
  Phone, Grid, SuccessFilled, Loading, TrendCharts, QuestionFilled
} from '@element-plus/icons-vue'
import {
  getInfinityConfigByTenant,
  createInfinityConfig,
  updateInfinityConfig,
  testInfinityConnection,
  getExtensions,
  getExtensionStatistics,
  batchImportExtensions,
  releaseExtension,
  deleteExtension
} from '@/api/infinity'
import type {
  InfinityCallConfig,
  ExtensionPool,
  ExtensionPoolStatistics
} from '@/types/infinity'

const tenantStore = useTenantStore()
const currentTenantId = computed(() => tenantStore.currentTenantId)

// 配置相关
const config = ref<InfinityCallConfig | null>(null)
const hasConfig = computed(() => !!config.value)
const showConfigDialog = ref(false)
const configFormRef = ref<FormInstance>()
const saving = ref(false)
const testingConnection = ref(false)

// 配置表单
const configForm = ref({
  api_url: '',
  access_token: '',
  app_id: '',
  caller_number_range_start: '',
  caller_number_range_end: '',
  callback_url: '',
  max_concurrent_calls: 100,
  call_timeout_seconds: 60,
  is_active: true
})

const configRules: FormRules = {
  api_url: [
    { required: true, message: '请输入API地址', trigger: 'blur' }
  ],
  access_token: [
    { required: true, message: '请输入访问令牌', trigger: 'blur' }
  ],
  app_id: [
    { required: true, message: '请输入应用ID', trigger: 'blur' }
  ]
}

// 分机池相关
const extensions = ref<ExtensionPool[]>([])
const statistics = ref<ExtensionPoolStatistics>({
  tenant_id: 0,
  config_id: 0,
  total_extensions: 0,
  available_count: 0,
  in_use_count: 0,
  offline_count: 0,
  usage_rate: 0
})
const showExtensionDialog = ref(false)
const importing = ref(false)
const extensionForm = ref({
  extensionText: ''
})

// 监听甲方切换
watch(currentTenantId, (newVal) => {
  if (newVal) {
    loadConfig()
  } else {
    config.value = null
    extensions.value = []
  }
})

// 加载配置
const loadConfig = async () => {
  if (!currentTenantId.value) return
  
  try {
    config.value = await getInfinityConfigByTenant(currentTenantId.value)
    loadExtensions()
    loadStatistics()
  } catch (error: any) {
    if (error.response?.status === 404) {
      config.value = null
    } else {
      ElMessage.error('加载配置失败')
    }
  }
}

// 加载分机列表
const loadExtensions = async () => {
  if (!currentTenantId.value || !config.value) return
  
  try {
    extensions.value = await getExtensions(currentTenantId.value, config.value.id)
  } catch (error) {
    ElMessage.error('加载分机列表失败')
  }
}

// 加载统计信息
const loadStatistics = async () => {
  if (!currentTenantId.value || !config.value) return
  
  try {
    statistics.value = await getExtensionStatistics(currentTenantId.value, config.value.id)
  } catch (error) {
    ElMessage.error('加载统计信息失败')
  }
}

// 保存配置
const handleSaveConfig = async () => {
  if (!configFormRef.value) return
  
  await configFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    saving.value = true
    try {
      if (hasConfig.value && config.value) {
        // 更新
        await updateInfinityConfig(config.value.id!, configForm.value)
        ElMessage.success('配置更新成功')
      } else {
        // 创建
        await createInfinityConfig({
          tenant_id: currentTenantId.value!,
          ...configForm.value
        })
        ElMessage.success('配置创建成功')
      }
      showConfigDialog.value = false
      loadConfig()
    } catch (error) {
      ElMessage.error('保存配置失败')
    } finally {
      saving.value = false
    }
  })
}

// 测试连接
const testConnection = async () => {
  if (!config.value) return
  
  testingConnection.value = true
  try {
    const result = await testInfinityConnection({
      api_url: config.value.api_url,
      access_token: config.value.access_token
    })
    
    if (result.success) {
      ElMessage.success(`${result.message}（响应时间：${result.response_time_ms}ms）`)
    } else {
      ElMessage.error(result.message)
    }
  } catch (error) {
    ElMessage.error('测试连接失败')
  } finally {
    testingConnection.value = false
  }
}

// 批量导入分机
const handleImportExtensions = async () => {
  if (!extensionForm.value.extensionText.trim()) {
    ElMessage.warning('请输入分机号')
    return
  }
  
  const lines = extensionForm.value.extensionText
    .split('\n')
    .map(line => line.trim())
    .filter(line => line.length > 0)
  
  if (lines.length === 0) {
    ElMessage.warning('请输入至少一个分机号')
    return
  }
  
  importing.value = true
  try {
    const result = await batchImportExtensions({
      tenant_id: currentTenantId.value!,
      config_id: config.value!.id!,
      extension_numbers: lines
    })
    
    ElMessage.success(
      `导入完成：成功 ${result.created_count} 个，跳过 ${result.skipped_count} 个`
    )
    showExtensionDialog.value = false
    loadExtensions()
    loadStatistics()
  } catch (error) {
    ElMessage.error('导入失败')
  } finally {
    importing.value = false
  }
}

// 释放分机
const handleReleaseExtension = async (row: ExtensionPool) => {
  try {
    await ElMessageBox.confirm(
      `确定要释放分机 ${row.infinity_extension_number} 吗？`,
      '提示',
      { type: 'warning' }
    )
    
    await releaseExtension(row.id)
    ElMessage.success('分机已释放')
    loadExtensions()
    loadStatistics()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('释放分机失败')
    }
  }
}

// 删除分机
const handleDeleteExtension = async (row: ExtensionPool) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除分机 ${row.infinity_extension_number} 吗？`,
      '警告',
      { type: 'warning' }
    )
    
    await deleteExtension(row.id)
    ElMessage.success('分机已删除')
    loadExtensions()
    loadStatistics()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('删除分机失败')
    }
  }
}

// 重置配置表单
const resetConfigForm = () => {
  if (hasConfig.value && config.value) {
    configForm.value = {
      api_url: config.value.api_url,
      access_token: config.value.access_token,
      app_id: config.value.app_id || '',
      caller_number_range_start: config.value.caller_number_range_start || '',
      caller_number_range_end: config.value.caller_number_range_end || '',
      callback_url: config.value.callback_url || '',
      max_concurrent_calls: config.value.max_concurrent_calls,
      call_timeout_seconds: config.value.call_timeout_seconds,
      is_active: config.value.is_active
    }
  } else {
    configForm.value = {
      api_url: '',
      access_token: '',
      app_id: '',
      caller_number_range_start: '',
      caller_number_range_end: '',
      callback_url: '',
      max_concurrent_calls: 100,
      call_timeout_seconds: 60,
      is_active: true
    }
  }
}

// 重置分机表单
const resetExtensionForm = () => {
  extensionForm.value.extensionText = ''
}

// 格式化日期时间
const formatDateTime = (datetime: string) => {
  if (!datetime) return '-'
  return datetime.replace('T', ' ').substring(0, 19)
}

// 掩码显示令牌
const maskToken = (token: string) => {
  if (!token || token.length < 10) return '******'
  return token.substring(0, 6) + '******' + token.substring(token.length - 4)
}

// 获取状态类型
const getStatusType = (status: string) => {
  const map: Record<string, any> = {
    available: 'success',
    in_use: 'warning',
    offline: 'danger'
  }
  return map[status] || 'info'
}

// 获取状态文本
const getStatusText = (status: string) => {
  const map: Record<string, string> = {
    available: '空闲',
    in_use: '使用中',
    offline: '离线'
  }
  return map[status] || status
}

onMounted(() => {
  if (currentTenantId.value) {
    loadConfig()
  }
})
</script>

<style scoped lang="scss">
.infinity-config-container {
  padding: 20px;
}

.config-card,
.extension-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  .title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 16px;
    font-weight: 500;
  }
}

.config-info {
  padding: 10px 0;
}

.statistics {
  padding: 20px 0;
}

.extension-list {
  margin-top: 20px;
}
</style>

