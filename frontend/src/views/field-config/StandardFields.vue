<template>
  <div class="case-standard-fields">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>案件列表标准字段管理（只读）</span>
        </div>
      </template>

      <el-alert
        type="info"
        :closable="false"
        show-icon
        title="字段由CCO统一维护，所有甲方继承，仅支持查看"
        description="不可新增、编辑、删除或拖拽排序。下表即为统一的案件列表标准字段。"
        class="tip-alert"
      />

      <el-table :data="fields" border style="width: 100%; margin-top: 16px" v-loading="loading">
        <el-table-column type="index" label="#" width="60" />
        <el-table-column prop="fieldName" label="字段名称" min-width="140" />
        <el-table-column prop="fieldKey" label="字段标识" min-width="160" />
        <el-table-column prop="fieldDataType" label="数据类型" width="110" />
        <el-table-column label="必填" width="90">
          <template #default="{ row }">
            <el-tag :type="row.required ? 'success' : 'info'">
              {{ row.required ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="fieldSource" label="来源" width="100" />
        <el-table-column prop="description" label="说明" min-width="200" show-overflow-tooltip />
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getCaseListStandardFields } from '@/api/field'

type CaseStandardField = {
  id: number
  sceneType: string
  sceneName: string
  fieldKey: string
  fieldName: string
  fieldDataType: string
  fieldSource: string
  sortOrder: number
  displayWidth: number
  required?: boolean
  searchable?: boolean
  filterable?: boolean
  rangeSearchable?: boolean
  colorType?: string
  description?: string
}

const fields = ref<CaseStandardField[]>([])
const loading = ref(false)

const loadData = async () => {
  loading.value = true
  try {
    const res = await getCaseListStandardFields()
    const raw = Array.isArray(res) ? res : (res?.data || [])
    fields.value = raw.map((item: any) => normalizeField(item))
  } catch (error: any) {
    ElMessage.error(error?.message || '加载标准字段失败')
  } finally {
    loading.value = false
  }
}

const normalizeField = (item: any): CaseStandardField => {
  return {
    id: Number(item.id ?? 0),
    sceneType: item.sceneType ?? item.scene_type ?? '',
    sceneName: item.sceneName ?? item.scene_name ?? '',
    fieldKey: item.fieldKey ?? item.field_key ?? '',
    fieldName: item.fieldName ?? item.field_name ?? '',
    fieldDataType: item.fieldDataType ?? item.field_data_type ?? '',
    fieldSource: item.fieldSource ?? item.field_source ?? '',
    sortOrder: Number(item.sortOrder ?? item.sort_order ?? 0),
    displayWidth: Number(item.displayWidth ?? item.display_width ?? 0),
    required: Boolean(item.required ?? item.is_required ?? false),
    searchable: Boolean(item.searchable ?? item.is_searchable ?? false),
    filterable: Boolean(item.filterable ?? item.is_filterable ?? false),
    rangeSearchable: Boolean(item.rangeSearchable ?? item.is_range_searchable ?? false),
    colorType: item.colorType ?? item.color_type ?? '',
    description: item.description ?? '',
  }
}

onMounted(loadData)
</script>

<style scoped>
.card-header {
  font-weight: 600;
  font-size: 16px;
}

.tip-alert {
  margin-bottom: 12px;
}
</style>

