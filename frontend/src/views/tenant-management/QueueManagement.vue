<template>
  <div class="queue-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>甲方案件队列管理</span>
          <el-button 
            type="primary" 
            @click="handleAdd" 
            :disabled="!currentTenantId"
          >
            添加队列
          </el-button>
        </div>
      </template>

      <el-alert
        title="说明：队列用于根据逾期天数自动分类案件。逾期天数范围不可重合。不填写开始/结束代表无穷大（-∞ 或 +∞）。"
        type="info"
        :closable="false"
        show-icon
        style="margin-bottom: 20px"
      />

      <el-table :data="queues" border style="width: 100%">
        <el-table-column prop="queue_code" label="队列编码" width="120" />
        <el-table-column prop="queue_name" label="队列名称" width="150" />
        <el-table-column label="逾期天数范围" width="250">
          <template #default="{ row }">
            <span>{{ formatRange(row) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="sort_order" label="排序" width="100" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.is_active ? 'success' : 'info'">
              {{ row.is_active ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)" size="small">
              编辑
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)" size="small">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="form" label-width="120px" :rules="rules" ref="formRef">
        <el-form-item label="队列编码" prop="queue_code">
          <el-input v-model="form.queue_code" placeholder="如：S1, M1, M2" maxlength="50" />
        </el-form-item>

        <el-form-item label="队列名称" prop="queue_name">
          <el-input v-model="form.queue_name" placeholder="如：S1队列" maxlength="100" />
        </el-form-item>

        <el-form-item label="逾期天数范围" required>
          <div style="display: flex; gap: 10px; align-items: center;">
            <div>
              <el-input-number 
                v-model="form.overdue_days_start" 
                :disabled="form.start_infinity"
                placeholder="开始"
                :min="-999"
                :max="999"
                style="width: 150px"
              />
              <div style="margin-top: 5px;">
                <el-checkbox v-model="form.start_infinity" @change="handleStartInfinityChange">
                  负无穷（-∞）
                </el-checkbox>
              </div>
            </div>
            <span>~</span>
            <div>
              <el-input-number 
                v-model="form.overdue_days_end" 
                :disabled="form.end_infinity"
                placeholder="结束"
                :min="-999"
                :max="999"
                style="width: 150px"
              />
              <div style="margin-top: 5px;">
                <el-checkbox v-model="form.end_infinity" @change="handleEndInfinityChange">
                  正无穷（+∞）
                </el-checkbox>
              </div>
            </div>
          </div>
          <div style="margin-top: 10px; color: #909399; font-size: 12px;">
            示例：C队列(-∞ ~ -1), S0队列(0 ~ 0), M1队列(91 ~ +∞)
          </div>
        </el-form-item>

        <el-form-item label="排序" prop="sort_order">
          <el-input-number v-model="form.sort_order" :min="0" />
          <div style="margin-top: 5px; color: #909399; font-size: 12px;">
            建议按逾期天数从小到大排序
          </div>
        </el-form-item>

        <el-form-item label="是否启用">
          <el-switch v-model="form.is_active" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { useTenantStore } from '@/stores/tenant'

const tenantStore = useTenantStore()
const currentTenantId = ref<number | undefined>(tenantStore.currentTenantId)
const queues = ref<any[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const saving = ref(false)
const formRef = ref<FormInstance>()

// 监听全局甲方变化
watch(
  () => tenantStore.currentTenantId,
  (newTenantId) => {
    currentTenantId.value = newTenantId
    loadQueues()
  }
)

// 初始加载
onMounted(() => {
  loadQueues()
})

const form = ref({
  queue_code: '',
  queue_name: '',
  overdue_days_start: null as number | null,
  overdue_days_end: null as number | null,
  start_infinity: false,
  end_infinity: false,
  sort_order: 0,
  is_active: true
})

const rules = reactive({
  queue_code: [
    { required: true, message: '请输入队列编码', trigger: 'blur' }
  ],
  queue_name: [
    { required: true, message: '请输入队列名称', trigger: 'blur' }
  ],
  sort_order: [
    { required: true, message: '请输入排序', trigger: 'blur' }
  ]
})

// 加载队列列表
const loadQueues = async () => {
  if (!currentTenantId.value) {
    queues.value = []
    return
  }

  try {
    const url = `http://localhost:8000/api/v1/tenants/${currentTenantId.value}/queues`
    const response = await fetch(url)
    const result = await response.json()
    
    // API直接返回数组，不是{data: [...]}格式
    queues.value = Array.isArray(result) ? result : (result.data || [])
    console.log(`已加载 ${queues.value.length} 个队列`)
  } catch (error) {
    console.error('加载队列失败：', error)
    ElMessage.error('加载队列失败')
  }
}

// 格式化范围显示
const formatRange = (queue: any) => {
  const start = queue.overdue_days_start === null ? '-∞' : queue.overdue_days_start
  const end = queue.overdue_days_end === null ? '+∞' : queue.overdue_days_end
  return `${start} ~ ${end} 天`
}

// 处理开始无穷大复选框
const handleStartInfinityChange = (value: boolean) => {
  if (value) {
    form.value.overdue_days_start = null
  }
}

// 处理结束无穷大复选框
const handleEndInfinityChange = (value: boolean) => {
  if (value) {
    form.value.overdue_days_end = null
  }
}

// 添加队列
const handleAdd = () => {
  dialogTitle.value = '添加队列'
  form.value = {
    queue_code: '',
    queue_name: '',
    overdue_days_start: null,
    overdue_days_end: null,
    start_infinity: false,
    end_infinity: false,
    sort_order: queues.value.length + 1,
    is_active: true
  }
  dialogVisible.value = true
}

// 编辑队列
const handleEdit = (row: any) => {
  dialogTitle.value = '编辑队列'
  form.value = {
    ...row,
    start_infinity: row.overdue_days_start === null,
    end_infinity: row.overdue_days_end === null
  }
  dialogVisible.value = true
}

// 验证范围合法性
const validateRange = () => {
  const start = form.value.start_infinity ? null : form.value.overdue_days_start
  const end = form.value.end_infinity ? null : form.value.overdue_days_end

  // 两个都为空
  if (start === null && end === null) {
    ElMessage.warning('开始天数和结束天数不能都为无穷大')
    return false
  }

  // 如果都有值，开始不能大于结束
  if (start !== null && end !== null && start > end) {
    ElMessage.warning('开始天数不能大于结束天数')
    return false
  }

  return true
}

// 检查范围是否重合
const checkRangeOverlap = (start1: number | null, end1: number | null, start2: number | null, end2: number | null) => {
  const s1 = start1 === null ? Number.NEGATIVE_INFINITY : start1
  const e1 = end1 === null ? Number.POSITIVE_INFINITY : end1
  const s2 = start2 === null ? Number.NEGATIVE_INFINITY : start2
  const e2 = end2 === null ? Number.POSITIVE_INFINITY : end2

  // 两个区间重合的条件：s1 <= e2 && s2 <= e1
  return s1 <= e2 && s2 <= e1
}

// 保存队列
const handleSave = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    // 验证范围合法性
    if (!validateRange()) {
      return
    }

    const start = form.value.start_infinity ? null : form.value.overdue_days_start
    const end = form.value.end_infinity ? null : form.value.overdue_days_end

    // 检查与其他队列是否重合
    for (const queue of queues.value) {
      // 跳过自己（编辑时）
      if (form.value.id && queue.id === form.value.id) {
        continue
      }

      if (checkRangeOverlap(start, end, queue.overdue_days_start, queue.overdue_days_end)) {
        ElMessage.warning(`逾期天数范围与队列"${queue.queue_name}"重合，请调整范围`)
        return
      }
    }

    saving.value = true

    // 准备提交数据
    const submitData = {
      queue_code: form.value.queue_code,
      queue_name: form.value.queue_name,
      overdue_days_start: start,
      overdue_days_end: end,
      sort_order: form.value.sort_order,
      is_active: form.value.is_active
    }

    console.log('保存队列：', submitData)
    
    // TODO: 调用API保存
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadQueues()
  } catch (error) {
    console.error('保存失败：', error)
  } finally {
    saving.value = false
  }
}

// 删除队列
const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除队列"${row.queue_name}"吗？删除后该队列下的案件将无法归类。`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    // TODO: 调用API删除
    ElMessage.success('删除成功')
    loadQueues()
  } catch (error) {
    // 用户取消
  }
}
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>

