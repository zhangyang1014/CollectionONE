<template>
  <el-dialog
    v-model="visible"
    title="智能匹配建议"
    width="80%"
    :close-on-click-modal="false"
  >
    <div class="auto-match-dialog" v-loading="isLoading">
      <!-- 匹配建议说明 -->
      <el-alert
        title="💡 提示：请勾选要应用的映射建议，然后点击「确认选中项」按钮。"
        type="info"
        :closable="false"
        show-icon
      />

      <!-- 匹配建议列表 -->
      <div class="suggestions-list" style="margin-top: 20px;">
        <el-table
          :data="suggestions"
          @selection-change="handleSelectionChange"
          border
          stripe
        >
          <el-table-column type="selection" width="55" />
          <el-table-column label="标准字段" width="200">
            <template #default="scope">
              <div>
                <strong>{{ scope.row.standard_field_name }}</strong>
                <div class="field-key-text">{{ scope.row.standard_field_key }}</div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="甲方字段" width="200">
            <template #default="scope">
              <div>
                <strong>{{ scope.row.tenant_field_name }}</strong>
                <div class="field-key-text">{{ scope.row.tenant_field_key }}</div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="匹配方式" width="120">
            <template #default="scope">
              <el-tag :type="getMatchTypeTag(scope.row.match_type)">
                {{ getMatchTypeText(scope.row.match_type) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="相似度" width="120">
            <template #default="scope">
              <el-progress 
                :percentage="scope.row.similarity * 100" 
                :color="getSimilarityColor(scope.row.similarity)"
              />
            </template>
          </el-table-column>
          <el-table-column label="置信度" width="100">
            <template #default="scope">
              <el-tag :type="getConfidenceTag(scope.row.confidence)">
                {{ scope.row.confidence }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="说明" min-width="200">
            <template #default="scope">
              {{ scope.row.reason || '-' }}
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 操作按钮 -->
      <div class="dialog-footer" style="margin-top: 20px; text-align: right;">
        <el-button @click="visible = false">取消</el-button>
        <el-button 
          type="primary" 
          :disabled="selectedSuggestions.length === 0"
          @click="handleConfirm"
        >
          确认选中项 ({{ selectedSuggestions.length }})
        </el-button>
      </div>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { autoSuggestMapping, batchConfirmMapping } from '@/api/field-mapping'

// Props
const props = defineProps<{
  modelValue: boolean
  tenantId?: number
}>()

// Emits
const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'confirm'): void
}>()

// 响应式状态
const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const isLoading = ref(false)
const suggestions = ref<any[]>([])
const selectedSuggestions = ref<any[]>([])

// 获取匹配建议
const loadSuggestions = async () => {
  if (!props.tenantId) {
    return
  }

  isLoading.value = true
  try {
    const res = await autoSuggestMapping(props.tenantId)
    const data = res?.data || []
    suggestions.value = Array.isArray(data) ? data : data.list || []
    
    console.log('匹配建议加载成功:', suggestions.value)
  } catch (error) {
    console.error('加载匹配建议失败:', error)
    ElMessage.error('加载匹配建议失败')
  } finally {
    isLoading.value = false
  }
}

// 选择变化
const handleSelectionChange = (selection: any[]) => {
  selectedSuggestions.value = selection
}

// 确认应用
const handleConfirm = async () => {
  if (!props.tenantId || selectedSuggestions.value.length === 0) {
    return
  }

  isLoading.value = true
  try {
    await batchConfirmMapping(props.tenantId, {
      mappings: selectedSuggestions.value.map(s => ({
        field_key: s.standard_field_key,
        tenant_field_key: s.tenant_field_key
      }))
    })
    
    ElMessage.success(`成功应用 ${selectedSuggestions.value.length} 个映射建议`)
    visible.value = false
    emit('confirm')
  } catch (error) {
    console.error('应用映射建议失败:', error)
    ElMessage.error('应用映射建议失败')
  } finally {
    isLoading.value = false
  }
}

// 辅助函数
const getMatchTypeTag = (type: string) => {
  const typeMap: Record<string, string> = {
    exact: 'success',
    similar: 'primary',
    synonym: 'warning'
  }
  return typeMap[type] || 'info'
}

const getMatchTypeText = (type: string) => {
  const textMap: Record<string, string> = {
    exact: '完全匹配',
    similar: '相似匹配',
    synonym: '同义词'
  }
  return textMap[type] || type
}

const getSimilarityColor = (similarity: number) => {
  if (similarity >= 0.9) return '#67c23a'
  if (similarity >= 0.7) return '#e6a23c'
  return '#f56c6c'
}

const getConfidenceTag = (confidence: string) => {
  const tagMap: Record<string, string> = {
    high: 'success',
    medium: 'warning',
    low: 'danger'
  }
  return tagMap[confidence] || 'info'
}

// 暴露方法供父组件调用
defineExpose({
  loadSuggestions
})
</script>

<style scoped>
.auto-match-dialog {
  padding: 0 4px;
}

.field-key-text {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>
