<template>
  <div class="case-update-config">
    <el-form :model="form" label-width="200px" v-loading="loading">
      <!-- 基础配置 -->
      <el-divider content-position="left">基础配置</el-divider>
      
      <el-form-item label="启用通知">
        <el-switch v-model="form.is_enabled" />
      </el-form-item>

      <!-- 新案件分配通知 -->
      <el-divider content-position="left">新案件分配通知</el-divider>

      <el-form-item label="启用">
        <el-switch v-model="form.config_data.case_assigned.enabled" />
      </el-form-item>

      <el-form-item label="通知对象">
        <el-checkbox-group v-model="form.config_data.case_assigned.notify_roles">
          <el-checkbox label="collector">催员</el-checkbox>
          <el-checkbox label="team_leader">小组长</el-checkbox>
        </el-checkbox-group>
      </el-form-item>

      <el-form-item label="通知模板">
        <el-input
          v-model="form.config_data.case_assigned.template"
          type="textarea"
          :rows="2"
          placeholder="支持变量：{case_id}"
        />
      </el-form-item>

      <!-- 收到还款通知 -->
      <el-divider content-position="left">收到还款通知</el-divider>

      <el-form-item label="启用">
        <el-switch v-model="form.config_data.payment_received.enabled" />
      </el-form-item>

      <el-form-item label="触发金额阈值">
        <el-input-number 
          v-model="form.config_data.payment_received.amount_threshold" 
          :min="0"
          style="width: 200px;"
          :precision="0"
        />
        <span style="margin-left: 10px; color: #909399;">超过多少金额才通知（0或空表示全部）</span>
      </el-form-item>

      <el-form-item label="通知对象">
        <el-checkbox-group v-model="form.config_data.payment_received.notify_roles">
          <el-checkbox label="collector">催员</el-checkbox>
          <el-checkbox label="team_leader">小组长</el-checkbox>
          <el-checkbox label="agency_admin">机构管理员</el-checkbox>
        </el-checkbox-group>
      </el-form-item>

      <el-form-item label="通知模板">
        <el-input
          v-model="form.config_data.payment_received.template"
          type="textarea"
          :rows="2"
          placeholder="支持变量：{case_id}, {amount}"
        />
      </el-form-item>

      <!-- 案件标签更新通知 -->
      <el-divider content-position="left">案件标签更新通知</el-divider>

      <el-form-item label="启用">
        <el-switch v-model="form.config_data.tag_updated.enabled" />
      </el-form-item>

      <el-form-item label="通知对象">
        <el-checkbox-group v-model="form.config_data.tag_updated.notify_roles">
          <el-checkbox label="collector">催员</el-checkbox>
          <el-checkbox label="team_leader">小组长</el-checkbox>
        </el-checkbox-group>
      </el-form-item>

      <el-form-item label="通知模板">
        <el-input
          v-model="form.config_data.tag_updated.template"
          type="textarea"
          :rows="2"
          placeholder="支持变量：{case_id}"
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
    case_assigned: {
      enabled: true,
      notify_roles: ['collector', 'team_leader'],
      template: '新案件分配：{case_id}'
    },
    payment_received: {
      enabled: true,
      amount_threshold: null,
      notify_roles: ['collector', 'team_leader', 'agency_admin'],
      template: '案件 {case_id} 已收到还款 {amount}'
    },
    tag_updated: {
      enabled: true,
      notify_roles: ['collector', 'team_leader'],
      template: '案件 {case_id} 的标签已更新'
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
.case-update-config {
  padding: 20px;
}
</style>

