<template>
  <div class="nudge-config">
    <el-form :model="form" label-width="200px" v-loading="loading">
      <!-- 基础配置 -->
      <el-divider content-position="left">基础配置</el-divider>
      
      <el-form-item label="启用通知">
        <el-switch v-model="form.is_enabled" />
      </el-form-item>

      <!-- PTP提醒配置 -->
      <el-divider content-position="left">PTP提醒配置</el-divider>

      <el-form-item label="提前提醒时间（分钟）">
        <el-input-number 
          v-model="form.config_data.ptp.advance_notify_minutes" 
          :min="1" 
          :max="1440"
          style="width: 200px;"
        />
        <span style="margin-left: 10px; color: #909399;">PTP时间前多长时间提醒</span>
      </el-form-item>

      <el-form-item label="到期后提醒间隔（分钟）">
        <el-input-number 
          v-model="form.config_data.ptp.repeat_interval_minutes" 
          :min="1" 
          :max="1440"
          style="width: 200px;"
        />
        <span style="margin-left: 10px; color: #909399;">PTP时间已过但未还款，多长时间后再次提醒</span>
      </el-form-item>

      <el-form-item label="最大提醒次数">
        <el-input-number 
          v-model="form.config_data.ptp.max_notify_count" 
          :min="1" 
          :max="100"
          style="width: 200px;"
        />
      </el-form-item>

      <el-form-item label="通知对象">
        <el-checkbox-group v-model="form.config_data.ptp.notify_roles">
          <el-checkbox label="collector">催员</el-checkbox>
          <el-checkbox label="team_leader">小组长</el-checkbox>
          <el-checkbox label="agency_admin">机构管理员</el-checkbox>
        </el-checkbox-group>
      </el-form-item>

      <!-- 跟进时间提醒配置 -->
      <el-divider content-position="left">跟进时间提醒配置</el-divider>

      <el-form-item label="提前提醒时间（分钟）">
        <el-input-number 
          v-model="form.config_data.follow_up.advance_notify_minutes" 
          :min="1" 
          :max="1440"
          style="width: 200px;"
        />
        <span style="margin-left: 10px; color: #909399;">跟进时间前多长时间提醒</span>
      </el-form-item>

      <el-form-item label="到期后提醒间隔（分钟）">
        <el-input-number 
          v-model="form.config_data.follow_up.repeat_interval_minutes" 
          :min="1" 
          :max="1440"
          style="width: 200px;"
        />
        <span style="margin-left: 10px; color: #909399;">跟进时间已过但未跟进，多长时间后再次提醒</span>
      </el-form-item>

      <el-form-item label="最大提醒次数">
        <el-input-number 
          v-model="form.config_data.follow_up.max_notify_count" 
          :min="1" 
          :max="100"
          style="width: 200px;"
        />
      </el-form-item>

      <el-form-item label="通知对象">
        <el-checkbox-group v-model="form.config_data.follow_up.notify_roles">
          <el-checkbox label="collector">催员</el-checkbox>
          <el-checkbox label="team_leader">小组长</el-checkbox>
        </el-checkbox-group>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="handleSave">保存配置</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
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
.nudge-config {
  padding: 20px;
}
</style>

