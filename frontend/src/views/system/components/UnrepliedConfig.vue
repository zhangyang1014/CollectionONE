<template>
  <div class="unreplied-config">
    <el-form :model="form" label-width="200px" v-loading="loading">
      <!-- 基础配置 -->
      <el-divider content-position="left">基础配置</el-divider>
      
      <el-form-item label="启用通知">
        <el-switch v-model="form.is_enabled" />
      </el-form-item>

      <el-form-item label="通知对象">
        <el-checkbox-group v-model="form.config_data.notify_roles">
          <el-checkbox label="collector">催员</el-checkbox>
          <el-checkbox label="team_leader">小组长</el-checkbox>
          <el-checkbox label="agency_admin">机构管理员</el-checkbox>
          <el-checkbox label="tenant_admin">甲方管理员</el-checkbox>
        </el-checkbox-group>
      </el-form-item>

      <el-form-item label="通知渠道">
        <el-checkbox-group v-model="form.config_data.notify_channels">
          <el-checkbox label="in_app">站内通知</el-checkbox>
          <el-checkbox label="email">邮件</el-checkbox>
          <el-checkbox label="sms">短信</el-checkbox>
          <el-checkbox label="push">系统推送</el-checkbox>
        </el-checkbox-group>
      </el-form-item>

      <el-form-item label="通知优先级">
        <el-select v-model="form.config_data.priority" style="width: 200px;">
          <el-option label="高" value="high" />
          <el-option label="中" value="medium" />
          <el-option label="低" value="low" />
        </el-select>
      </el-form-item>

      <!-- 特定配置 -->
      <el-divider content-position="left">触发条件</el-divider>

      <el-form-item label="触发延迟时间（分钟）">
        <el-input-number 
          v-model="form.config_data.trigger_delay_minutes" 
          :min="1" 
          :max="1440"
          style="width: 200px;"
        />
        <span style="margin-left: 10px; color: #909399;">客户消息发送后多长时间未回复才触发</span>
      </el-form-item>

      <el-form-item label="监控渠道">
        <el-checkbox-group v-model="form.config_data.monitored_channels">
          <el-checkbox label="whatsapp">WhatsApp</el-checkbox>
          <el-checkbox label="sms">SMS</el-checkbox>
          <el-checkbox label="rcs">RCS</el-checkbox>
          <el-checkbox label="call">电话</el-checkbox>
        </el-checkbox-group>
      </el-form-item>

      <el-form-item label="重复提醒间隔（分钟）">
        <el-input-number 
          v-model="form.config_data.repeat_interval_minutes" 
          :min="1" 
          :max="1440"
          style="width: 200px;"
        />
        <span style="margin-left: 10px; color: #909399;">如果仍未回复，多长时间后再次提醒</span>
      </el-form-item>

      <el-form-item label="最大提醒次数">
        <el-input-number 
          v-model="form.config_data.max_notify_count" 
          :min="1" 
          :max="100"
          style="width: 200px;"
        />
        <span style="margin-left: 10px; color: #909399;">最多提醒多少次后停止（0表示不限制）</span>
      </el-form-item>

      <el-form-item label="通知时间范围">
        <el-radio-group v-model="form.config_data.notify_time_range.type">
          <el-radio label="working_hours">工作时间</el-radio>
          <el-radio label="all_day">全天</el-radio>
          <el-radio label="custom">自定义</el-radio>
        </el-radio-group>
        <div v-if="form.config_data.notify_time_range.type === 'custom'" style="margin-top: 10px;">
          <el-time-picker
            v-model="customTimeRange"
            is-range
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            format="HH:mm"
            value-format="HH:mm"
            style="width: 300px;"
          />
        </div>
      </el-form-item>

      <el-form-item label="通知模板">
        <el-input
          v-model="form.config_data.template"
          type="textarea"
          :rows="3"
          placeholder="支持变量：{case_id}, {channel}, {contact_name}"
        />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="handleSave">保存配置</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import type { NotificationConfig } from '@/types/notification'

const props = defineProps<{
  config: NotificationConfig | null
}>()

const emit = defineEmits<{
  save: [data: any]
}>()

const loading = ref(false)

const form = ref({
  is_enabled: true,
  config_data: {
    notify_roles: ['collector', 'team_leader'],
    notify_channels: ['in_app'],
    priority: 'high',
    trigger_delay_minutes: 10,
    monitored_channels: ['whatsapp', 'sms', 'rcs'],
    repeat_interval_minutes: 30,
    max_notify_count: 3,
    notify_time_range: {
      type: 'working_hours',
      custom_start: '09:00',
      custom_end: '18:00'
    },
    template: '案件 {case_id} 的客户在 {channel} 渠道有未回复消息'
  }
})

const customTimeRange = computed({
  get: () => {
    if (form.value.config_data.notify_time_range.type === 'custom') {
      return [
        form.value.config_data.notify_time_range.custom_start,
        form.value.config_data.notify_time_range.custom_end
      ]
    }
    return null
  },
  set: (value: string[] | null) => {
    if (value && value.length === 2) {
      form.value.config_data.notify_time_range.custom_start = value[0]
      form.value.config_data.notify_time_range.custom_end = value[1]
    }
  }
})

watch(() => props.config, (newConfig) => {
  if (newConfig) {
    form.value.is_enabled = newConfig.is_enabled
    form.value.config_data = {
      ...form.value.config_data,
      ...newConfig.config_data
    }
  }
}, { immediate: true })

const handleSave = () => {
  emit('save', {
    is_enabled: form.value.is_enabled,
    config_data: form.value.config_data
  })
}
</script>

<style scoped>
.unreplied-config {
  padding: 20px;
}
</style>

