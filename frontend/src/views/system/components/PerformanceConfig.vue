<template>
  <div class="performance-config">
    <el-form :model="form" label-width="200px" v-loading="loading">
      <!-- 基础配置 -->
      <el-divider content-position="left">基础配置</el-divider>
      
      <el-form-item label="启用通知">
        <el-switch v-model="form.is_enabled" />
      </el-form-item>

      <el-form-item label="触发金额阈值">
        <el-input-number 
          v-model="form.config_data.amount_threshold" 
          :min="0"
          style="width: 200px;"
          :precision="0"
        />
        <span style="margin-left: 10px; color: #909399;">超过多少金额才通知（0表示全部）</span>
      </el-form-item>

      <el-form-item label="通知范围">
        <el-select v-model="form.config_data.notify_scope" style="width: 200px;">
          <el-option label="仅本人" value="self" />
          <el-option label="小组内" value="team" />
          <el-option label="机构内" value="agency" />
          <el-option label="全部" value="all" />
        </el-select>
      </el-form-item>

      <el-form-item label="通知对象">
        <el-checkbox-group v-model="form.config_data.notify_roles">
          <el-checkbox label="collector">催员</el-checkbox>
          <el-checkbox label="team_leader">小组长</el-checkbox>
          <el-checkbox label="agency_admin">机构管理员</el-checkbox>
          <el-checkbox label="tenant_admin">甲方管理员</el-checkbox>
        </el-checkbox-group>
      </el-form-item>

      <el-form-item label="通知频率（分钟）">
        <el-input-number 
          v-model="form.config_data.notify_frequency_minutes" 
          :min="1" 
          :max="1440"
          style="width: 200px;"
        />
        <span style="margin-left: 10px; color: #909399;">多长时间内同一催员最多通知一次（0表示不限制）</span>
      </el-form-item>

      <el-form-item label="通知模板">
        <el-input
          v-model="form.config_data.template"
          type="textarea"
          :rows="3"
          placeholder="支持变量：{team}, {collector}, {amount}"
        />
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
    amount_threshold: 1000,
    notify_scope: 'team',
    notify_roles: ['collector', 'team_leader', 'agency_admin'],
    template: '恭喜【{team}】的【{collector}】催回金额 {amount}',
    notify_frequency_minutes: 60
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
.performance-config {
  padding: 20px;
}
</style>

