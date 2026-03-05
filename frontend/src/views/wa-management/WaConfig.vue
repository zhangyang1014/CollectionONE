<template>
  <div class="wa-config">
    <!-- 基础配置 -->
    <el-card style="margin-bottom: 16px">
      <template #header>
        <span>基础配置</span>
      </template>

      <el-form
        ref="configFormRef"
        :model="config"
        label-width="200px"
        label-position="left"
        style="max-width: 600px"
      >
        <el-form-item label="一个IP可被多少Phone使用">
          <el-input-number v-model="config.phonesPerIp" :min="1" :max="20" />
          <span class="form-tip">当前值: {{ config.phonesPerIp }}</span>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 自动化配置 -->
    <el-card style="margin-bottom: 16px">
      <template #header>
        <span>自动化配置</span>
      </template>

      <el-form
        :model="config"
        label-width="200px"
        label-position="left"
        style="max-width: 700px"
      >
        <el-form-item label="自动IP分配">
          <div class="switch-row">
            <el-switch v-model="config.autoIpAssign" />
            <span class="switch-desc">
              {{ config.autoIpAssign ? '开启：系统自动按阈值分配IP给新登记的Phone' : '关闭：需运营手动给instant分配IP' }}
            </span>
          </div>
        </el-form-item>

        <el-form-item label="自动激活">
          <div class="switch-row">
            <el-switch v-model="config.autoActivate" />
            <span class="switch-desc">
              {{ config.autoActivate ? '开启：调用Geelark API自动激活' : '关闭：需运营去Geelark平台手动激活' }}
            </span>
          </div>
        </el-form-item>

        <el-form-item label="自动进入投养">
          <div class="switch-row">
            <el-switch v-model="config.autoNurture" />
            <span class="switch-desc">
              {{ config.autoNurture ? '开启：激活后自动调用API进入投养流程' : '关闭：需手动触发投养' }}
            </span>
          </div>
        </el-form-item>

        <el-form-item label="自动分配投养完成的账号">
          <div class="switch-row">
            <el-switch v-model="config.autoAssign" />
            <span class="switch-desc">
              {{ config.autoAssign ? '开启：投养完成后自动分配给CCO坐席' : '关闭：需运营手动分配' }}
            </span>
          </div>
        </el-form-item>

        <el-form-item v-if="config.autoAssign" label="自动分配规则">
          <el-select v-model="config.autoAssignRule" style="width: 240px">
            <el-option value="high_performance_first" label="高业绩催员优先" />
            <el-option value="low_load_first" label="低负载催员优先" />
            <el-option value="round_robin" label="轮询分配" />
          </el-select>
        </el-form-item>
      </el-form>

      <div class="config-actions">
        <el-button type="primary" :loading="savingConfig" @click="handleSaveConfig">
          保存配置
        </el-button>
      </div>
    </el-card>

  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { WaConfig } from '@/types/wa-management'
import { getWaConfig, updateWaConfig } from '@/api/wa-management'

const config = reactive<WaConfig>({
  phonesPerIp: 5,
  autoIpAssign: false,
  autoActivate: false,
  autoNurture: false,
  autoAssign: false,
  autoAssignRule: 'high_performance_first',
})
const savingConfig = ref(false)

const loadConfig = async () => {
  const data = await getWaConfig()
  Object.assign(config, data)
}

const handleSaveConfig = async () => {
  savingConfig.value = true
  try {
    await updateWaConfig({ ...config })
    ElMessage.success('配置保存成功')
  } catch {
    ElMessage.error('保存失败')
  } finally {
    savingConfig.value = false
  }
}

onMounted(() => {
  loadConfig()
})
</script>

<style scoped>
.switch-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.switch-desc {
  font-size: 13px;
  color: #909399;
}

.form-tip {
  margin-left: 12px;
  font-size: 13px;
  color: #909399;
}

.config-actions {
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}
</style>
