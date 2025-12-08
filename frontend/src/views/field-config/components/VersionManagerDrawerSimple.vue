<template>
  <el-drawer
    v-model="visible"
    title="版本管理 - 案件列表字段配置"
    size="60%"
    :close-on-click-modal="false"
  >
    <div class="version-manager" v-loading="isLoading">
      <!-- 当前版本信息 -->
      <el-alert
        v-if="currentVersionInfo"
        :title="`当前使用版本: ${currentVersionInfo.version}`"
        type="success"
        :closable="false"
        show-icon
      >
        <p>上传时间: {{ currentVersionInfo.upload_time }}</p>
        <p>字段数量: {{ currentVersionInfo.field_count }}</p>
        <p>上传人: {{ currentVersionInfo.uploader }}</p>
      </el-alert>

      <el-divider />

      <!-- 版本历史列表 -->
      <div class="version-list">
        <h3>历史版本</h3>
        <el-empty v-if="versionList.length === 0" description="暂无历史版本" />
        
        <div v-for="version in versionList" :key="version.version" class="version-item">
          <el-card :class="{ 'active-version': version.is_active }">
            <template #header>
              <div class="version-header">
                <span class="version-title">
                  <el-tag v-if="version.is_active" type="success">当前</el-tag>
                  版本 {{ version.version }}
                </span>
                <span class="version-time">{{ version.upload_time }}</span>
              </div>
            </template>
            
            <div class="version-info">
              <p><strong>字段数量:</strong> {{ version.field_count }}</p>
              <p><strong>上传人:</strong> {{ version.uploader }}</p>
              <p v-if="version.note"><strong>说明:</strong> {{ version.note }}</p>
            </div>

            <div class="version-actions">
              <el-button 
                size="small" 
                type="primary" 
                @click="handleViewDetail(version)"
              >
                查看详情
              </el-button>
              <el-button 
                v-if="!version.is_active" 
                size="small" 
                type="success"
                @click="handleActivate(version)"
              >
                切换到此版本
              </el-button>
              <el-button 
                size="small"
                @click="handleDownload(version)"
              >
                下载
              </el-button>
            </div>
          </el-card>
        </div>
      </div>

      <!-- 版本详情对话框 -->
      <el-dialog
        v-model="detailDialogVisible"
        title="版本详情"
        width="50%"
      >
        <div v-if="selectedVersion">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="版本号">{{ selectedVersion.version }}</el-descriptions-item>
            <el-descriptions-item label="上传时间">{{ selectedVersion.upload_time }}</el-descriptions-item>
            <el-descriptions-item label="字段数量">{{ selectedVersion.field_count }}</el-descriptions-item>
            <el-descriptions-item label="上传人">{{ selectedVersion.uploader }}</el-descriptions-item>
          </el-descriptions>
          
          <div v-if="selectedVersion.note" style="margin-top: 16px;">
            <h4>版本说明</h4>
            <p>{{ selectedVersion.note }}</p>
          </div>
        </div>
      </el-dialog>
    </div>
  </el-drawer>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  getFieldsJsonHistory, 
  activateFieldsJsonVersion,
  downloadFieldsJsonVersion 
} from '@/api/field-mapping'

// Props
const props = defineProps<{
  modelValue: boolean
  tenantId?: number
  currentVersion?: string
}>()

// Emits
const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'version-changed'): void
}>()

// 响应式状态
const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const isLoading = ref(false)
const versionList = ref<any[]>([])
const currentVersionInfo = ref<any>(null)
const detailDialogVisible = ref(false)
const selectedVersion = ref<any>(null)

// 加载版本列表
const loadVersionList = async () => {
  if (!props.tenantId) {
    return
  }

  isLoading.value = true
  try {
    const res = await getFieldsJsonHistory(props.tenantId)
    const data = res?.data || []
    versionList.value = Array.isArray(data) ? data : data.list || []
    
    // 设置当前版本信息
    currentVersionInfo.value = versionList.value.find(v => v.is_active || v.version === props.currentVersion)
    
    console.log('版本列表加载成功:', versionList.value)
  } catch (error) {
    console.error('加载版本列表失败:', error)
    ElMessage.error('加载版本列表失败')
  } finally {
    isLoading.value = false
  }
}

// 查看详情
const handleViewDetail = (version: any) => {
  selectedVersion.value = version
  detailDialogVisible.value = true
}

// 切换版本
const handleActivate = async (version: any) => {
  if (!props.tenantId) {
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定要切换到版本 ${version.version} 吗？`,
      '提示',
      {
        type: 'warning'
      }
    )

    isLoading.value = true
    await activateFieldsJsonVersion(props.tenantId, version.version)
    ElMessage.success('版本切换成功')
    
    // 重新加载版本列表
    await loadVersionList()
    emit('version-changed')
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('切换版本失败:', error)
      ElMessage.error('切换版本失败')
    }
  } finally {
    isLoading.value = false
  }
}

// 下载版本
const handleDownload = async (version: any) => {
  if (!props.tenantId) {
    return
  }

  try {
    isLoading.value = true
    await downloadFieldsJsonVersion(props.tenantId, version.version)
    ElMessage.success('下载成功')
  } catch (error) {
    console.error('下载失败:', error)
    ElMessage.error('下载失败')
  } finally {
    isLoading.value = false
  }
}

// 监听抽屉打开，加载数据
watch(visible, (newVal) => {
  if (newVal) {
    loadVersionList()
  }
})
</script>

<style scoped>
.version-manager {
  padding: 0 4px;
}

.version-list {
  margin-top: 16px;
}

.version-item {
  margin-bottom: 16px;
}

.active-version {
  border: 2px solid #67c23a;
}

.version-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.version-title {
  font-weight: bold;
  display: flex;
  align-items: center;
  gap: 8px;
}

.version-time {
  color: #909399;
  font-size: 14px;
}

.version-info {
  margin: 12px 0;
}

.version-info p {
  margin: 8px 0;
}

.version-actions {
  display: flex;
  gap: 8px;
  margin-top: 12px;
}
</style>
