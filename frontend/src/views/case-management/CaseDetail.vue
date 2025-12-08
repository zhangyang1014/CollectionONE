<template>
  <div class="case-detail">
    <el-card v-loading="loading || configLoading">
      <template #header>
        <div class="card-header">
          <span>案件详情</span>
          <el-button @click="$router.back()">返回</el-button>
        </div>
      </template>

      <el-tabs v-model="activeTab">
        <el-tab-pane label="字段信息" name="fields">
          <el-descriptions :column="2" border>
            <el-descriptions-item
              v-for="config in visibleConfigs"
              :key="config.field_key"
              :label="config.field_name"
            >
              {{ formatFieldValue(config.field_key, getFieldValue(config.field_key)) }}
            </el-descriptions-item>
          </el-descriptions>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getCase } from '@/api/case'
import { useTenantStore } from '@/stores/tenant'
import { useCaseDetailFieldConfig } from '@/composables/useCaseDetailFieldConfig'

const route = useRoute()
const caseId = Number(route.params.id)
const activeTab = ref('fields')
const loading = ref(false)
const caseData = ref<Record<string, any>>({})

const tenantStore = useTenantStore()
const currentTenantId = computed(() => tenantStore.currentTenantId)

const {
  loading: configLoading,
  visibleConfigs,
  formatFieldValue,
  loadConfigs
} = useCaseDetailFieldConfig({
  tenantId: currentTenantId,
  sceneType: 'admin_case_detail',
  autoLoad: true
})

const getFieldValue = (fieldKey: string) => {
  const data = caseData.value || {}
  if (fieldKey in data) return data[fieldKey]
  if (data.standard_fields && fieldKey in data.standard_fields) return data.standard_fields[fieldKey]
  if (data.custom_fields && fieldKey in data.custom_fields) return data.custom_fields[fieldKey]
  return '-'
}

const loadCase = async () => {
  try {
    loading.value = true
    const res = await getCase(caseId)
    // 兼容接口直接返回对象或 data 包装
    caseData.value = res?.data || res || {}
    // 重新加载字段配置（确保依赖当前租户）
    loadConfigs()
  } catch (error) {
    ElMessage.error('加载案件详情失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadCase()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
