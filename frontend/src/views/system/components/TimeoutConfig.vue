<template>
  <div class="timeout-config">
    <el-form :model="form" label-width="200px" v-loading="loading">
      <!-- 基础配置 -->
      <el-divider content-position="left">基础配置</el-divider>
      
      <el-form-item label="启用通知">
        <el-switch v-model="form.is_enabled" />
      </el-form-item>

      <!-- 超时阈值配置 -->
      <el-divider content-position="left">超时阈值配置（多级）</el-divider>

      <el-form-item label="第一级超时（分钟）">
        <el-input-number 
          v-model="form.config_data.timeout_levels[0].minutes" 
          :min="1" 
          :max="1440"
          style="width: 200px;"
        />
        <span style="margin-left: 10px; color: #909399;">30分钟未回复</span>
      </el-form-item>

      <el-form-item label="重复提醒间隔（分钟）">
        <el-input-number 
          v-model="form.config_data.timeout_levels[0].repeat_interval_minutes" 
          :min="1" 
          :max="1440"
          style="width: 200px;"
        />
      </el-form-item>

      <el-form-item label="通知对象">
        <el-checkbox-group v-model="form.config_data.timeout_levels[0].notify_roles">
          <el-checkbox label="collector">催员</el-checkbox>
        </el-checkbox-group>
      </el-form-item>

      <el-divider />

      <el-form-item label="第二级超时（分钟）">
        <el-input-number 
          v-model="form.config_data.timeout_levels[1].minutes" 
          :min="1" 
          :max="1440"
          style="width: 200px;"
        />
        <span style="margin-left: 10px; color: #909399;">1小时未回复</span>
      </el-form-item>

      <el-form-item label="重复提醒间隔（分钟）">
        <el-input-number 
          v-model="form.config_data.timeout_levels[1].repeat_interval_minutes" 
          :min="1" 
          :max="1440"
          style="width: 200px;"
        />
      </el-form-item>

      <el-form-item label="通知对象">
        <el-checkbox-group v-model="form.config_data.timeout_levels[1].notify_roles">
          <el-checkbox label="collector">催员</el-checkbox>
          <el-checkbox label="team_leader">小组长</el-checkbox>
        </el-checkbox-group>
      </el-form-item>

      <el-divider />

      <el-form-item label="第三级超时（分钟）">
        <el-input-number 
          v-model="form.config_data.timeout_levels[2].minutes" 
          :min="1" 
          :max="1440"
          style="width: 200px;"
        />
        <span style="margin-left: 10px; color: #909399;">2小时未回复</span>
      </el-form-item>

      <el-form-item label="重复提醒间隔（分钟）">
        <el-input-number 
          v-model="form.config_data.timeout_levels[2].repeat_interval_minutes" 
          :min="1" 
          :max="1440"
          style="width: 200px;"
        />
      </el-form-item>

      <el-form-item label="通知对象">
        <el-checkbox-group v-model="form.config_data.timeout_levels[2].notify_roles">
          <el-checkbox label="collector">催员</el-checkbox>
          <el-checkbox label="team_leader">小组长</el-checkbox>
          <el-checkbox label="agency_admin">机构管理员</el-checkbox>
        </el-checkbox-group>
      </el-form-item>

      <!-- 其他配置 -->
      <el-divider content-position="left">其他配置</el-divider>

      <el-form-item label="最大提醒次数">
        <el-input-number 
          v-model="form.config_data.max_notify_count" 
          :min="1" 
          :max="100"
          style="width: 200px;"
        />
        <span style="margin-left: 10px; color: #909399;">最多提醒多少次后停止</span>
      </el-form-item>

      <el-form-item label="通知升级时间（分钟）">
        <el-input-number 
          v-model="form.config_data.escalation_minutes" 
          :min="1" 
          :max="1440"
          style="width: 200px;"
        />
        <span style="margin-left: 10px; color: #909399;">超过多长时间后通知更高级别管理员</span>
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
    timeout_levels: [
      { minutes: 30, repeat_interval_minutes: 30, notify_roles: ['collector'] },
      { minutes: 60, repeat_interval_minutes: 60, notify_roles: ['collector', 'team_leader'] },
      { minutes: 120, repeat_interval_minutes: 120, notify_roles: ['collector', 'team_leader', 'agency_admin'] }
    ],
    max_notify_count: 5,
    escalation_minutes: 240
  }
})

watch(() => props.config, (newConfig) => {
  if (newConfig) {
    form.value.is_enabled = newConfig.is_enabled
    form.value.config_data = {
      ...form.value.config_data,
      ...newConfig.config_data
    }
    // 确保timeout_levels数组有3个元素
    if (!form.value.config_data.timeout_levels || form.value.config_data.timeout_levels.length < 3) {
      form.value.config_data.timeout_levels = [
        { minutes: 30, repeat_interval_minutes: 30, notify_roles: ['collector'] },
        { minutes: 60, repeat_interval_minutes: 60, notify_roles: ['collector', 'team_leader'] },
        { minutes: 120, repeat_interval_minutes: 120, notify_roles: ['collector', 'team_leader', 'agency_admin'] }
      ]
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
.timeout-config {
  padding: 20px;
}
</style>

