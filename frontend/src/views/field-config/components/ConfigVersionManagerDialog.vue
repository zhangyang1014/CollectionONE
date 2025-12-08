<template>
  <el-dialog
    v-model="visible"
    title="映射配置版本管理"
    width="900px"
    :close-on-click-modal="false"
  >
    <div class="version-manager">
      <!-- 当前生效版本 -->
      <el-alert
        v-if="currentVersion"
        :title="`当前生效版本：v${currentVersion.version}`"
        type="success"
        :closable="false"
        show-icon
        style="margin-bottom: 20px"
      >
        <template #default>
          <p>保存时间：{{ formatDateTime(currentVersion.created_at) }}</p>
          <p>映射完成度：{{ currentVersion.mapped_count }}/{{ currentVersion.total_count }}</p>
        </template>
      </el-alert>

      <el-alert
        v-else
        title="暂无已保存的配置版本"
        type="info"
        :closable="false"
        show-icon
        style="margin-bottom: 20px"
      />

      <!-- 版本列表 -->
      <div class="version-list-header">
        <h3>历史版本记录</h3>
        <el-button size="small" @click="loadVersions">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>

      <el-table
        :data="versions"
        v-loading="loading"
        border
        stripe
        style="width: 100%"
      >
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column label="版本号" width="100">
          <template #default="{ row }">
            <el-tag :type="row.is_active ? 'success' : 'info'">
              v{{ row.version }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="保存时间" min-width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.created_at) }}
          </template>
        </el-table-column>
        <el-table-column label="映射完成度" width="120">
          <template #default="{ row }">
            {{ row.mapped_count }}/{{ row.total_count }}
          </template>
        </el-table-column>
        <el-table-column prop="created_by_name" label="保存人" width="120" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.is_active" type="success" size="small">生效中</el-tag>
            <el-tag v-else type="info" size="small">历史版本</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button 
              type="primary" 
              size="small" 
              @click="handleViewDetail(row)"
            >
              查看详情
            </el-button>
            <el-button
              v-if="!row.is_active"
              type="success"
              size="small"
              @click="handleRestore(row)"
            >
              恢复版本
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 空状态 -->
      <el-empty 
        v-if="versions.length === 0 && !loading" 
        description="暂无历史版本"
      />
    </div>

    <template #footer>
      <el-button @click="visible = false">关闭</el-button>
    </template>

    <!-- 版本详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      :title="`版本 v${currentDetailVersion?.version} 详情`"
      width="800px"
      append-to-body
    >
      <div v-if="currentDetailVersion">
        <el-descriptions :column="2" border style="margin-bottom: 20px">
          <el-descriptions-item label="版本号">v{{ currentDetailVersion.version }}</el-descriptions-item>
          <el-descriptions-item label="保存时间">
            {{ formatDateTime(currentDetailVersion.created_at) }}
          </el-descriptions-item>
          <el-descriptions-item label="映射完成度">
            {{ currentDetailVersion.mapped_count }}/{{ currentDetailVersion.total_count }}
          </el-descriptions-item>
          <el-descriptions-item label="保存人">
            {{ currentDetailVersion.created_by_name }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="currentDetailVersion.is_active ? 'success' : 'info'">
              {{ currentDetailVersion.is_active ? '生效中' : '历史版本' }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>

        <h4>字段映射配置</h4>
        <el-alert
          title="详细的字段映射配置将在此显示"
          type="info"
          :closable="false"
        />
      </div>
    </el-dialog>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'

// Props
interface Props {
  modelValue: boolean
  tenantId: number | null
  currentVersion?: any
}

const props = defineProps<Props>()
const emit = defineEmits(['update:modelValue', 'restore'])

// 响应式数据
const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const loading = ref(false)
const versions = ref<any[]>([])
const detailDialogVisible = ref(false)
const currentDetailVersion = ref<any>(null)

// 格式化日期时间
const formatDateTime = (dateStr: string | null | undefined) => {
  if (!dateStr) return '-'
  try {
    const date = new Date(dateStr)
    if (isNaN(date.getTime())) return dateStr
    
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    const hours = String(date.getHours()).padStart(2, '0')
    const minutes = String(date.getMinutes()).padStart(2, '0')
    const seconds = String(date.getSeconds()).padStart(2, '0')
    
    return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
  } catch {
    return dateStr
  }
}

// 加载版本列表
const loadVersions = async () => {
  if (!props.tenantId) {
    return
  }

  loading.value = true
  try {
    // Mock数据
    await new Promise(resolve => setTimeout(resolve, 500))
    
    // 生成Mock历史版本
    const mockVersions = []
    for (let i = 5; i >= 1; i--) {
      mockVersions.push({
        version: i,
        created_at: new Date(Date.now() - (6 - i) * 24 * 60 * 60 * 1000).toISOString(),
        created_by_name: '管理员',
        mapped_count: Math.min(15, 10 + i),
        total_count: 15,
        is_active: i === 5
      })
    }
    
    versions.value = mockVersions
  } catch (error) {
    console.error('加载版本列表失败:', error)
    ElMessage.error('加载版本列表失败')
  } finally {
    loading.value = false
  }
}

// 查看详情
const handleViewDetail = (version: any) => {
  currentDetailVersion.value = version
  detailDialogVisible.value = true
}

// 恢复版本
const handleRestore = async (version: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要恢复到版本 v${version.version} 吗？恢复后，当前配置将被替换为该历史版本的配置。`,
      '确认恢复',
      {
        confirmButtonText: '确认恢复',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    loading.value = true
    try {
      // Mock恢复操作
      await new Promise(resolve => setTimeout(resolve, 1000))
      
      ElMessage.success(`已恢复到版本 v${version.version}`)
      emit('restore', version)
      visible.value = false
    } catch (error) {
      console.error('恢复版本失败:', error)
      ElMessage.error('恢复版本失败')
    } finally {
      loading.value = false
    }
  } catch {
    // 用户取消
  }
}

// 监听对话框打开
watch(visible, (val) => {
  if (val) {
    loadVersions()
  }
})
</script>

<style scoped>
.version-manager {
  min-height: 400px;
}

.version-list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.version-list-header h3 {
  margin: 0;
  font-size: 16px;
  color: #303133;
}
</style>
