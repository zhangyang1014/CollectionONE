<template>
  <el-dialog
    v-model="dialogVisible"
    title="自动匹配建议"
    width="900px"
    :close-on-click-modal="false"
  >
    <div class="auto-match-dialog">
      <!-- 匹配摘要 -->
      <div class="match-summary">
        <el-alert
          title="匹配摘要"
          type="info"
          :closable="false"
        >
          <div class="summary-stats">
            <div class="stat-item success">
              <el-icon><CircleCheck /></el-icon>
              <span>自动映射（相似度≥80%）：<strong>{{ highConfidenceCount }}</strong> 个</span>
            </div>
            <div class="stat-item warning">
              <el-icon><Warning /></el-icon>
              <span>建议映射（相似度60-80%）：<strong>{{ mediumConfidenceCount }}</strong> 个</span>
            </div>
            <div class="stat-item danger">
              <el-icon><CircleClose /></el-icon>
              <span>未匹配：<strong>{{ unmatchedCount }}</strong> 个标准字段</span>
            </div>
          </div>
        </el-alert>
      </div>

      <el-divider />

      <!-- 高置信度自动映射 -->
      <div v-if="highConfidenceSuggestions.length > 0" class="match-group">
        <div class="group-header success-header">
          <el-icon><CircleCheck /></el-icon>
          <span>自动映射（{{ highConfidenceSuggestions.length }}个）</span>
          <el-button link type="primary" size="small" @click="selectAllHigh">
            全部确认
          </el-button>
        </div>
        <div class="suggestions-list">
          <div 
            v-for="(item, index) in highConfidenceSuggestions" 
            :key="'high-' + index"
            class="suggestion-item"
          >
            <el-checkbox v-model="item.selected">
              <div class="suggestion-content">
                <div class="mapping-arrow">
                  <div class="field-box standard">
                    <div class="field-label">标准字段</div>
                    <div class="field-name">{{ item.field_name }}</div>
                    <div class="field-key">{{ item.field_key }}</div>
                  </div>
                  <el-icon class="arrow-icon"><Right /></el-icon>
                  <div class="field-box tenant">
                    <div class="field-label">甲方字段</div>
                    <div class="field-name">{{ item.tenant_field_name }}</div>
                    <div class="field-key">{{ item.tenant_field_key }}</div>
                  </div>
                </div>
                <div class="match-info">
                  <el-progress 
                    :percentage="item.similarity" 
                    :color="getProgressColor(item.similarity)"
                    :stroke-width="8"
                    :show-text="false"
                  />
                  <div class="match-details">
                    <el-tag type="success" size="small">
                      相似度：{{ item.similarity }}%
                    </el-tag>
                    <el-tag type="info" size="small">
                      {{ getMatchTypeText(item.match_type) }}
                    </el-tag>
                    <el-tag v-if="item.field_type" size="small">
                      类型：{{ item.field_type }} ✓
                    </el-tag>
                  </div>
                </div>
              </div>
            </el-checkbox>
          </div>
        </div>
      </div>

      <!-- 中置信度建议映射 -->
      <div v-if="mediumConfidenceSuggestions.length > 0" class="match-group">
        <div class="group-header warning-header">
          <el-icon><Warning /></el-icon>
          <span>建议映射（{{ mediumConfidenceSuggestions.length }}个，需确认）</span>
        </div>
        <div class="suggestions-list">
          <div 
            v-for="(item, index) in mediumConfidenceSuggestions" 
            :key="'medium-' + index"
            class="suggestion-item"
          >
            <el-checkbox v-model="item.selected">
              <div class="suggestion-content">
                <div class="mapping-arrow">
                  <div class="field-box standard">
                    <div class="field-label">标准字段</div>
                    <div class="field-name">{{ item.field_name }}</div>
                    <div class="field-key">{{ item.field_key }}</div>
                  </div>
                  <el-icon class="arrow-icon"><Right /></el-icon>
                  <div class="field-box tenant">
                    <div class="field-label">甲方字段</div>
                    <div class="field-name">{{ item.tenant_field_name }}</div>
                    <div class="field-key">{{ item.tenant_field_key }}</div>
                  </div>
                </div>
                <div class="match-info">
                  <el-progress 
                    :percentage="item.similarity" 
                    :color="getProgressColor(item.similarity)"
                    :stroke-width="8"
                    :show-text="false"
                  />
                  <div class="match-details">
                    <el-tag type="warning" size="small">
                      相似度：{{ item.similarity }}%
                    </el-tag>
                    <el-tag type="info" size="small">
                      {{ getMatchTypeText(item.match_type) }}
                    </el-tag>
                    <el-button 
                      link 
                      type="primary" 
                      size="small"
                      @click="modifyMapping(item)"
                    >
                      修改
                    </el-button>
                    <el-button 
                      link 
                      type="danger" 
                      size="small"
                      @click="item.selected = false"
                    >
                      取消
                    </el-button>
                  </div>
                </div>
              </div>
            </el-checkbox>
          </div>
        </div>
      </div>

      <!-- 未匹配提示 -->
      <div v-if="unmatchedCount > 0" class="match-group">
        <div class="group-header danger-header">
          <el-icon><CircleClose /></el-icon>
          <span>未匹配的标准字段（{{ unmatchedCount }}个）</span>
        </div>
        <el-alert
          type="warning"
          :closable="false"
          show-icon
        >
          <p>这些必填字段需要手动配置映射关系：</p>
          <ul class="unmapped-list">
            <li v-for="field in unmatchedFields" :key="field">{{ field }}</li>
          </ul>
        </el-alert>
      </div>

      <!-- 提示 -->
      <el-alert
        v-if="selectedCount === 0"
        title="💡 提示：请勾选要应用的映射建议，然后点击「确认选中项」按钮。"
        type="info"
        :closable="false"
        show-icon
        style="margin-top: 20px;"
      />
    </div>

    <template #footer>
      <div class="dialog-footer">
        <div class="footer-left">
          <span class="selected-info">
            已选择 <strong>{{ selectedCount }}</strong> 个映射
          </span>
        </div>
        <div class="footer-right">
          <el-button @click="handleCancel">取消</el-button>
          <el-button 
            type="primary" 
            @click="handleConfirmSelected"
            :disabled="selectedCount === 0"
          >
            确认选中项 ({{ selectedCount }})
          </el-button>
          <el-button 
            type="success" 
            @click="handleConfirmAll"
            :disabled="allSuggestions.length === 0"
          >
            确认全部
          </el-button>
        </div>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { CircleCheck, Warning, CircleClose, Right } from '@element-plus/icons-vue'

interface MatchSuggestion {
  field_key: string
  field_name: string
  tenant_field_key: string
  tenant_field_name: string
  field_type?: string
  similarity: number
  match_type: string
  confidence: 'high' | 'medium' | 'low'
  selected: boolean
}

const props = defineProps<{
  modelValue: boolean
  suggestions: MatchSuggestion[]
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'confirm', mappings: any[]): void
}>()

const dialogVisible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

// 处理建议数据
const allSuggestions = ref<MatchSuggestion[]>([])

watch(() => props.suggestions, (newSuggestions) => {
  allSuggestions.value = newSuggestions.map(item => ({
    ...item,
    selected: item.confidence === 'high' // 自动选中高置信度的
  }))
}, { immediate: true, deep: true })

// 分类建议
const highConfidenceSuggestions = computed(() => 
  allSuggestions.value.filter(item => item.similarity >= 80)
)

const mediumConfidenceSuggestions = computed(() => 
  allSuggestions.value.filter(item => item.similarity >= 60 && item.similarity < 80)
)

// 统计
const highConfidenceCount = computed(() => highConfidenceSuggestions.value.length)
const mediumConfidenceCount = computed(() => mediumConfidenceSuggestions.value.length)
const unmatchedCount = computed(() => unmatchedFields.value.length)
const selectedCount = computed(() => allSuggestions.value.filter(item => item.selected).length)

// 未匹配字段（示例数据，实际应从props传入）
const unmatchedFields = ref<string[]>([
  'outstanding_amount (未还金额)',
  'due_date (到期日期)',
  'total_installments (期数)'
])

// 全选高置信度
const selectAllHigh = () => {
  highConfidenceSuggestions.value.forEach(item => {
    item.selected = true
  })
}

// 修改映射
const modifyMapping = (item: MatchSuggestion) => {
  // TODO: 打开修改对话框
  console.log('修改映射:', item)
}

// 获取匹配类型文本
const getMatchTypeText = (type: string) => {
  const typeMap: Record<string, string> = {
    'exact': '完全匹配',
    'synonym': '同义词匹配',
    'contains': '包含匹配',
    'similar': '相似匹配',
    'levenshtein': '编辑距离匹配'
  }
  return typeMap[type] || type
}

// 获取进度条颜色
const getProgressColor = (percentage: number) => {
  if (percentage >= 80) return '#67c23a'
  if (percentage >= 60) return '#e6a23c'
  return '#f56c6c'
}

// 确认选中项
const handleConfirmSelected = () => {
  const selectedMappings = allSuggestions.value
    .filter(item => item.selected)
    .map(item => ({
      field_key: item.field_key,
      tenant_field_key: item.tenant_field_key,
      mapping_status: item.similarity >= 80 ? 'auto_mapped' : 'manual_mapped'
    }))
  
  emit('confirm', selectedMappings)
  dialogVisible.value = false
}

// 确认全部
const handleConfirmAll = () => {
  const allMappings = allSuggestions.value.map(item => ({
    field_key: item.field_key,
    tenant_field_key: item.tenant_field_key,
    mapping_status: item.similarity >= 80 ? 'auto_mapped' : 'manual_mapped'
  }))
  
  emit('confirm', allMappings)
  dialogVisible.value = false
}

// 取消
const handleCancel = () => {
  dialogVisible.value = false
}
</script>

<style scoped>
.auto-match-dialog {
  padding: 10px 0;
}

/* 匹配摘要 */
.match-summary {
  margin-bottom: 20px;
}

.summary-stats {
  display: flex;
  gap: 24px;
  margin-top: 12px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.stat-item.success {
  color: #67c23a;
}

.stat-item.warning {
  color: #e6a23c;
}

.stat-item.danger {
  color: #f56c6c;
}

.stat-item strong {
  font-size: 18px;
  font-weight: 600;
}

/* 分组 */
.match-group {
  margin-bottom: 24px;
}

.group-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  border-radius: 4px;
  font-weight: 600;
  margin-bottom: 12px;
}

.success-header {
  background-color: #f0f9ff;
  color: #67c23a;
  border-left: 4px solid #67c23a;
}

.warning-header {
  background-color: #fdf6ec;
  color: #e6a23c;
  border-left: 4px solid #e6a23c;
}

.danger-header {
  background-color: #fef0f0;
  color: #f56c6c;
  border-left: 4px solid #f56c6c;
}

/* 建议列表 */
.suggestions-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.suggestion-item {
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  padding: 16px;
  transition: all 0.3s;
}

.suggestion-item:hover {
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
}

.suggestion-content {
  width: 100%;
  margin-left: 8px;
}

.mapping-arrow {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 12px;
}

.field-box {
  flex: 1;
  padding: 12px;
  border-radius: 4px;
  border: 1px solid #e4e7ed;
}

.field-box.standard {
  background-color: #f0f9ff;
  border-color: #b3d8ff;
}

.field-box.tenant {
  background-color: #f5f7fa;
  border-color: #dcdfe6;
}

.field-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.field-name {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 2px;
}

.field-key {
  font-size: 12px;
  color: #606266;
  font-family: 'Courier New', monospace;
}

.arrow-icon {
  font-size: 20px;
  color: #409eff;
  flex-shrink: 0;
}

/* 匹配信息 */
.match-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.match-details {
  display: flex;
  gap: 8px;
  align-items: center;
  flex-wrap: wrap;
}

/* 未匹配列表 */
.unmapped-list {
  margin: 8px 0 0 0;
  padding-left: 24px;
}

.unmapped-list li {
  margin-bottom: 4px;
  color: #606266;
}

/* 对话框底部 */
.dialog-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.selected-info {
  font-size: 14px;
  color: #606266;
}

.selected-info strong {
  color: #409eff;
  font-size: 16px;
}

.footer-right {
  display: flex;
  gap: 12px;
}

/* 响应式 */
@media (max-width: 768px) {
  .mapping-arrow {
    flex-direction: column;
  }

  .arrow-icon {
    transform: rotate(90deg);
  }

  .summary-stats {
    flex-direction: column;
    gap: 12px;
  }

  .dialog-footer {
    flex-direction: column;
    gap: 12px;
  }

  .footer-right {
    width: 100%;
    flex-direction: column;
  }

  .footer-right .el-button {
    width: 100%;
  }
}
</style>
