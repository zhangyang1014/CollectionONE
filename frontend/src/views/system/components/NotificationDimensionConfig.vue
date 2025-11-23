<template>
  <div class="notification-dimension-config">
    <el-alert 
      type="info" 
      :closable="false" 
      style="margin-bottom: 20px;"
      title="消息维度提醒说明"
    >
      <template #default>
        <div style="line-height: 1.8;">
          <p>消息维度提醒是指根据不同场景自动触发的系统通知，包括：</p>
          <ul style="margin: 8px 0; padding-left: 20px;">
            <li><strong>未回复消息提醒：</strong>客户消息超时未回复时自动提醒</li>
            <li><strong>催办提醒：</strong>PTP承诺和跟进任务的定时提醒</li>
            <li><strong>案件更新通知：</strong>案件分配、收款、标签变更等通知</li>
            <li><strong>业绩通知：</strong>达到业绩目标时的提醒</li>
            <li><strong>超时提醒：</strong>案件处理超时的通知</li>
          </ul>
        </div>
      </template>
    </el-alert>

    <el-card
      v-for="config in notificationTypes"
      :key="config.type"
      class="config-card"
      shadow="never"
    >
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-tag :type="getTagType(config.type)" size="large">
              {{ config.label }}
            </el-tag>
            <span class="config-description">{{ config.description }}</span>
          </div>
          <el-switch
            :model-value="config.enabled"
            @change="handleToggleStatus(config.type, $event)"
            active-text="启用"
            inactive-text="禁用"
          />
        </div>
      </template>

      <div v-loading="loading">
        <!-- 未回复消息提醒配置 -->
        <div v-if="config.type === 'unreplied'" class="config-content">
          <el-form :model="config.data" label-width="160px">
            <el-form-item label="触发延迟时间">
              <el-input-number
                v-model="config.data.trigger_delay_minutes"
                :min="1"
                :max="1440"
              />
              <span class="form-item-tip">分钟（客户发送消息后多久未回复则触发提醒）</span>
            </el-form-item>

            <el-form-item label="监控渠道">
              <el-checkbox-group v-model="config.data.monitored_channels">
                <el-checkbox label="whatsapp">WhatsApp</el-checkbox>
                <el-checkbox label="sms">SMS</el-checkbox>
                <el-checkbox label="rcs">RCS</el-checkbox>
              </el-checkbox-group>
            </el-form-item>

            <el-form-item label="重复提醒间隔">
              <el-input-number
                v-model="config.data.repeat_interval_minutes"
                :min="5"
                :max="1440"
              />
              <span class="form-item-tip">分钟</span>
            </el-form-item>

            <el-form-item label="最大提醒次数">
              <el-input-number
                v-model="config.data.max_notify_count"
                :min="1"
                :max="10"
              />
              <span class="form-item-tip">次</span>
            </el-form-item>

            <el-form-item label="通知时间范围">
              <el-radio-group v-model="config.data.notify_time_range.type">
                <el-radio value="working_hours">工作时间</el-radio>
                <el-radio value="all_day">全天</el-radio>
                <el-radio value="custom">自定义</el-radio>
              </el-radio-group>
              <div v-if="config.data.notify_time_range.type === 'custom'" style="margin-top: 10px;">
                <el-time-select
                  v-model="config.data.notify_time_range.custom_start"
                  start="00:00"
                  step="00:30"
                  end="23:30"
                  placeholder="开始时间"
                  style="width: 120px; margin-right: 10px;"
                />
                <span>至</span>
                <el-time-select
                  v-model="config.data.notify_time_range.custom_end"
                  start="00:00"
                  step="00:30"
                  end="23:30"
                  placeholder="结束时间"
                  style="width: 120px; margin-left: 10px;"
                />
              </div>
            </el-form-item>

            <el-form-item label="优先级">
              <el-radio-group v-model="config.data.priority">
                <el-radio value="high">高</el-radio>
                <el-radio value="medium">中</el-radio>
                <el-radio value="low">低</el-radio>
              </el-radio-group>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="handleSaveConfig(config.type)">
                保存配置
              </el-button>
            </el-form-item>
          </el-form>
        </div>

        <!-- 催办提醒配置 -->
        <div v-else-if="config.type === 'nudge'" class="config-content">
          <el-tabs type="border-card">
            <el-tab-pane label="PTP承诺提醒">
              <el-form :model="config.data.ptp" label-width="160px">
                <el-form-item label="提前提醒时间">
                  <el-input-number
                    v-model="config.data.ptp.advance_notify_minutes"
                    :min="5"
                    :max="1440"
                  />
                  <span class="form-item-tip">分钟（在承诺时间前多久提醒）</span>
                </el-form-item>

                <el-form-item label="重复提醒间隔">
                  <el-input-number
                    v-model="config.data.ptp.repeat_interval_minutes"
                    :min="5"
                    :max="1440"
                  />
                  <span class="form-item-tip">分钟</span>
                </el-form-item>

                <el-form-item label="最大提醒次数">
                  <el-input-number
                    v-model="config.data.ptp.max_notify_count"
                    :min="1"
                    :max="10"
                  />
                  <span class="form-item-tip">次</span>
                </el-form-item>
              </el-form>
            </el-tab-pane>

            <el-tab-pane label="跟进任务提醒">
              <el-form :model="config.data.follow_up" label-width="160px">
                <el-form-item label="提前提醒时间">
                  <el-input-number
                    v-model="config.data.follow_up.advance_notify_minutes"
                    :min="5"
                    :max="1440"
                  />
                  <span class="form-item-tip">分钟（在跟进时间前多久提醒）</span>
                </el-form-item>

                <el-form-item label="重复提醒间隔">
                  <el-input-number
                    v-model="config.data.follow_up.repeat_interval_minutes"
                    :min="5"
                    :max="1440"
                  />
                  <span class="form-item-tip">分钟</span>
                </el-form-item>

                <el-form-item label="最大提醒次数">
                  <el-input-number
                    v-model="config.data.follow_up.max_notify_count"
                    :min="1"
                    :max="10"
                  />
                  <span class="form-item-tip">次</span>
                </el-form-item>
              </el-form>
            </el-tab-pane>
          </el-tabs>

          <div style="margin-top: 20px;">
            <el-button type="primary" @click="handleSaveConfig(config.type)">
              保存配置
            </el-button>
          </div>
        </div>

        <!-- 案件更新通知配置 -->
        <div v-else-if="config.type === 'case_update'" class="config-content">
          <el-form label-width="160px">
            <el-form-item label="案件分配通知">
              <el-switch v-model="config.data.case_assigned.enabled" />
              <div v-if="config.data.case_assigned.enabled" style="margin-top: 10px;">
                <el-input
                  v-model="config.data.case_assigned.template"
                  type="textarea"
                  :rows="2"
                  placeholder="通知模板，支持变量：{case_id}, {case_number}, {collector_name}"
                />
              </div>
            </el-form-item>

            <el-form-item label="收款通知">
              <el-switch v-model="config.data.payment_received.enabled" />
              <div v-if="config.data.payment_received.enabled" style="margin-top: 10px;">
                <el-form-item label="金额阈值" label-width="100px">
                  <el-input-number
                    v-model="config.data.payment_received.amount_threshold"
                    :min="0"
                    placeholder="不填表示任意金额"
                  />
                  <span class="form-item-tip">大于此金额才发送通知</span>
                </el-form-item>
                <el-input
                  v-model="config.data.payment_received.template"
                  type="textarea"
                  :rows="2"
                  placeholder="通知模板，支持变量：{case_id}, {amount}, {payment_time}"
                />
              </div>
            </el-form-item>

            <el-form-item label="标签变更通知">
              <el-switch v-model="config.data.tag_updated.enabled" />
              <div v-if="config.data.tag_updated.enabled" style="margin-top: 10px;">
                <el-input
                  v-model="config.data.tag_updated.template"
                  type="textarea"
                  :rows="2"
                  placeholder="通知模板，支持变量：{case_id}, {old_tag}, {new_tag}"
                />
              </div>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="handleSaveConfig(config.type)">
                保存配置
              </el-button>
            </el-form-item>
          </el-form>
        </div>

        <!-- 业绩通知配置 -->
        <div v-else-if="config.type === 'performance'" class="config-content">
          <el-form :model="config.data" label-width="160px">
            <el-form-item label="业绩金额阈值">
              <el-input-number
                v-model="config.data.amount_threshold"
                :min="0"
              />
              <span class="form-item-tip">达到此金额时发送通知</span>
            </el-form-item>

            <el-form-item label="通知范围">
              <el-radio-group v-model="config.data.notify_scope">
                <el-radio value="self">仅本人</el-radio>
                <el-radio value="team">小组</el-radio>
                <el-radio value="agency">机构</el-radio>
                <el-radio value="all">全部</el-radio>
              </el-radio-group>
            </el-form-item>

            <el-form-item label="通知频率">
              <el-input-number
                v-model="config.data.notify_frequency_minutes"
                :min="5"
                :max="1440"
              />
              <span class="form-item-tip">分钟（避免频繁通知）</span>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="handleSaveConfig(config.type)">
                保存配置
              </el-button>
            </el-form-item>
          </el-form>
        </div>

        <!-- 超时提醒配置 -->
        <div v-else-if="config.type === 'timeout'" class="config-content">
          <el-form :model="config.data" label-width="160px">
            <el-form-item label="超时级别配置">
              <el-button 
                type="primary" 
                size="small" 
                @click="handleAddTimeoutLevel"
              >
                添加超时级别
              </el-button>
              
              <div 
                v-for="(level, index) in config.data.timeout_levels" 
                :key="index"
                class="timeout-level-item"
              >
                <el-form :model="level" :inline="true">
                  <el-form-item label="超时时长">
                    <el-input-number
                      v-model="level.minutes"
                      :min="1"
                      :max="10080"
                    />
                    <span class="form-item-tip">分钟</span>
                  </el-form-item>

                  <el-form-item label="重复间隔">
                    <el-input-number
                      v-model="level.repeat_interval_minutes"
                      :min="5"
                      :max="1440"
                    />
                    <span class="form-item-tip">分钟</span>
                  </el-form-item>

                  <el-form-item>
                    <el-button 
                      type="danger" 
                      size="small" 
                      @click="handleRemoveTimeoutLevel(index)"
                    >
                      删除
                    </el-button>
                  </el-form-item>
                </el-form>
              </div>
            </el-form-item>

            <el-form-item label="升级时长">
              <el-input-number
                v-model="config.data.escalation_minutes"
                :min="60"
                :max="10080"
              />
              <span class="form-item-tip">分钟（超过此时长升级至上级）</span>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="handleSaveConfig(config.type)">
                保存配置
              </el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useTenantStore } from '@/stores/tenant'
import {
  getNotificationConfigs,
  updateNotificationConfig,
  createNotificationConfig
} from '@/api/notification'
import type { NotificationConfig } from '@/types/notification'

const tenantStore = useTenantStore()
const loading = ref(false)

// 通知类型配置
const notificationTypes = ref([
  {
    type: 'unreplied',
    label: '未回复消息提醒',
    description: '客户消息超时未回复时自动提醒催员',
    enabled: true,
    id: null as number | null,
    data: {
      trigger_delay_minutes: 10,
      monitored_channels: ['whatsapp', 'sms', 'rcs'],
      repeat_interval_minutes: 30,
      max_notify_count: 3,
      notify_time_range: {
        type: 'working_hours',
        custom_start: '09:00',
        custom_end: '18:00'
      },
      priority: 'high',
      template: '案件 {case_id} 的客户在 {channel} 渠道有未回复消息'
    }
  },
  {
    type: 'nudge',
    label: '催办提醒',
    description: 'PTP承诺和跟进任务的定时提醒',
    enabled: true,
    id: null as number | null,
    data: {
      ptp: {
        advance_notify_minutes: 60,
        repeat_interval_minutes: 120,
        max_notify_count: 3,
        notify_roles: ['collector', 'team_leader']
      },
      follow_up: {
        advance_notify_minutes: 30,
        repeat_interval_minutes: 60,
        max_notify_count: 2,
        notify_roles: ['collector']
      }
    }
  },
  {
    type: 'case_update',
    label: '案件更新通知',
    description: '案件分配、收款、标签变更等通知',
    enabled: true,
    id: null as number | null,
    data: {
      case_assigned: {
        enabled: true,
        notify_roles: ['collector'],
        template: '您有新案件：{case_number}，请及时处理'
      },
      payment_received: {
        enabled: true,
        amount_threshold: null,
        notify_roles: ['collector', 'team_leader'],
        template: '案件 {case_number} 收到还款 {amount} 元'
      },
      tag_updated: {
        enabled: true,
        notify_roles: ['collector'],
        template: '案件 {case_number} 标签从 {old_tag} 变更为 {new_tag}'
      }
    }
  },
  {
    type: 'performance',
    label: '业绩通知',
    description: '达到业绩目标时的提醒',
    enabled: true,
    id: null as number | null,
    data: {
      amount_threshold: 10000,
      notify_scope: 'self',
      notify_frequency_minutes: 60
    }
  },
  {
    type: 'timeout',
    label: '超时提醒',
    description: '案件处理超时的通知',
    enabled: true,
    id: null as number | null,
    data: {
      timeout_levels: [
        {
          minutes: 60,
          repeat_interval_minutes: 30,
          notify_roles: ['collector']
        }
      ],
      escalation_minutes: 240
    }
  }
])

// 获取标签类型
const getTagType = (type: string) => {
  const typeMap: Record<string, any> = {
    'unreplied': 'danger',
    'nudge': 'warning',
    'case_update': 'success',
    'performance': 'primary',
    'timeout': 'info'
  }
  return typeMap[type] || 'info'
}

// 加载配置
const loadConfigs = async () => {
  try {
    loading.value = true
    const tenantId = tenantStore.currentTenantId || undefined
    const response: any = await getNotificationConfigs({ tenant_id: tenantId })
    const configs = Array.isArray(response) ? response : (response.data || [])
    
    // 更新配置数据
    configs.forEach((config: NotificationConfig) => {
      const typeConfig = notificationTypes.value.find(t => t.type === config.notification_type)
      if (typeConfig) {
        typeConfig.id = config.id
        typeConfig.enabled = config.is_enabled
        typeConfig.data = { ...typeConfig.data, ...config.config_data }
      }
    })
  } catch (error: any) {
    console.error('加载通知配置失败：', error)
    ElMessage.error('加载通知配置失败：' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 切换启用状态
const handleToggleStatus = async (type: string, enabled: boolean) => {
  const config = notificationTypes.value.find(c => c.type === type)
  if (!config) return

  try {
    config.enabled = enabled
    
    if (config.id) {
      await updateNotificationConfig(config.id, {
        is_enabled: enabled
      })
      ElMessage.success('状态更新成功')
    }
  } catch (error: any) {
    console.error('更新状态失败：', error)
    ElMessage.error('更新状态失败：' + (error.message || '未知错误'))
    // 恢复状态
    config.enabled = !enabled
  }
}

// 保存配置
const handleSaveConfig = async (type: string) => {
  const config = notificationTypes.value.find(c => c.type === type)
  if (!config) return

  try {
    loading.value = true
    const tenantId = tenantStore.currentTenantId || null

    const configData = {
      tenant_id: tenantId,
      notification_type: config.type,
      is_enabled: config.enabled,
      config_data: config.data
    }

    if (config.id) {
      // 更新
      await updateNotificationConfig(config.id, configData)
      ElMessage.success('配置更新成功')
    } else {
      // 创建
      const result: any = await createNotificationConfig(configData)
      config.id = result.id || result.data?.id
      ElMessage.success('配置创建成功')
    }

    await loadConfigs()
  } catch (error: any) {
    console.error('保存配置失败：', error)
    ElMessage.error('保存配置失败：' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 添加超时级别
const handleAddTimeoutLevel = () => {
  const config = notificationTypes.value.find(c => c.type === 'timeout')
  if (config) {
    config.data.timeout_levels.push({
      minutes: 120,
      repeat_interval_minutes: 60,
      notify_roles: ['collector']
    })
  }
}

// 删除超时级别
const handleRemoveTimeoutLevel = (index: number) => {
  const config = notificationTypes.value.find(c => c.type === 'timeout')
  if (config && config.data.timeout_levels.length > 1) {
    config.data.timeout_levels.splice(index, 1)
  } else {
    ElMessage.warning('至少保留一个超时级别')
  }
}

onMounted(() => {
  loadConfigs()
})
</script>

<style scoped>
.notification-dimension-config {
  padding: 0;
}

.config-card {
  margin-bottom: 20px;
  border: 1px solid #e4e7ed;
}

.config-card:last-child {
  margin-bottom: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.config-description {
  color: #606266;
  font-size: 14px;
}

.config-content {
  padding: 16px 0;
}

.form-item-tip {
  margin-left: 8px;
  color: #909399;
  font-size: 12px;
}

.timeout-level-item {
  margin-top: 16px;
  padding: 16px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.timeout-level-item:first-child {
  margin-top: 8px;
}

:deep(.el-form-item) {
  margin-bottom: 18px;
}

:deep(.el-card__body) {
  padding-bottom: 10px;
}
</style>

