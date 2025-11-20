<template>
  <div class="public-notification-config">
    <div class="header-actions">
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        添加公共通知
      </el-button>
    </div>

    <el-table
      :data="notifications"
      border
      stripe
      v-loading="loading"
      style="width: 100%"
    >
      <el-table-column prop="sort_order" label="排序" width="80" align="center">
        <template #default="{ row, $index }">
          <el-button-group>
            <el-button 
              size="small" 
              :disabled="$index === 0"
              @click="handleMoveUp(row)"
            >
              <el-icon><ArrowUp /></el-icon>
            </el-button>
            <el-button 
              size="small" 
              :disabled="$index === notifications.length - 1"
              @click="handleMoveDown(row)"
            >
              <el-icon><ArrowDown /></el-icon>
            </el-button>
          </el-button-group>
        </template>
      </el-table-column>

      <el-table-column prop="title" label="通知标题" min-width="200" />

      <el-table-column prop="carousel_interval_seconds" label="轮播间隔（秒）" width="150" align="center" />

      <el-table-column prop="is_forced_read" label="强制阅读" width="120" align="center">
        <template #default="{ row }">
          <el-tag :type="row.is_forced_read ? 'danger' : 'info'">
            {{ row.is_forced_read ? '是' : '否' }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column prop="is_enabled" label="启用状态" width="100" align="center">
        <template #default="{ row }">
          <el-switch
            v-model="row.is_enabled"
            @change="handleToggleStatus(row)"
          />
        </template>
      </el-table-column>

      <el-table-column label="操作" width="200" fixed="right">
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

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="800px"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="150px"
      >
        <!-- 是否启用 - 放在最上面 -->
        <el-form-item label="是否启用">
          <el-switch v-model="form.is_enabled" />
          <span style="margin-left: 10px; color: #909399;">
            关闭后，该通知将不会显示给用户
          </span>
        </el-form-item>

        <el-divider />

        <el-form-item label="通知标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入通知标题" />
        </el-form-item>

        <el-form-item label="通知正文" prop="content">
          <el-input
            v-model="form.content"
            placeholder="请输入通知正文内容"
            type="textarea"
            :rows="6"
          />
        </el-form-item>

        <el-form-item label="H5链接地址">
          <el-input
            v-model="form.h5_content"
            placeholder="请输入H5链接地址（可选）"
            type="textarea"
            :rows="2"
          />
          <div style="margin-top: 5px; color: #909399; font-size: 12px;">
            可选项，填写后用户点击通知可跳转到指定链接
          </div>
        </el-form-item>

        <el-divider />

        <el-form-item label="轮播间隔（秒）" prop="carousel_interval_seconds">
          <el-input-number
            v-model="form.carousel_interval_seconds"
            :min="1"
            :max="3600"
            style="width: 200px;"
          />
          <span style="margin-left: 10px; color: #909399;">
            多条通知时的轮播切换间隔
          </span>
        </el-form-item>

        <el-form-item label="强制阅读">
          <el-switch v-model="form.is_forced_read" />
          <span style="margin-left: 10px; color: #909399;">
            开启后，用户必须点击"已阅读"才能关闭通知
          </span>
        </el-form-item>

        <!-- 非强制阅读时的配置 -->
        <template v-if="!form.is_forced_read">
          <el-form-item label="重复提醒间隔">
            <el-input-number
              v-model="form.repeat_interval_minutes"
              :min="1"
              :max="1440"
              placeholder="分钟"
              style="width: 200px;"
            />
            <span style="margin-left: 10px; color: #909399;">
              单位：分钟，用户关闭后多久再次提醒
            </span>
          </el-form-item>

          <el-form-item label="最大提醒次数">
            <el-input-number
              v-model="form.max_remind_count"
              :min="1"
              :max="100"
              placeholder="次数"
              style="width: 200px;"
            />
            <span style="margin-left: 10px; color: #909399;">
              最多提醒多少次后不再显示
            </span>
          </el-form-item>

          <el-form-item label="通知时间范围">
            <el-time-picker
              v-model="notifyTimeStart"
              placeholder="开始时间"
              format="HH:mm"
              value-format="HH:mm"
              style="width: 150px;"
            />
            <span style="margin: 0 10px;">至</span>
            <el-time-picker
              v-model="notifyTimeEnd"
              placeholder="结束时间"
              format="HH:mm"
              value-format="HH:mm"
              style="width: 150px;"
            />
            <div style="margin-top: 5px; color: #909399; font-size: 12px;">
              仅在指定时间段内显示通知，不填则全天显示
            </div>
          </el-form-item>
        </template>

        <el-divider />

        <el-form-item label="生效时间范围">
          <el-date-picker
            v-model="effectiveTimeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DDTHH:mm:ss"
            style="width: 100%;"
          />
          <div style="margin-top: 5px;">
            <el-button text size="small" @click="clearEffectiveTime">清空</el-button>
            <span style="margin-left: 10px; color: #909399; font-size: 12px;">
              不填则长期有效
            </span>
          </div>
        </el-form-item>

        <el-form-item label="通知对象">
          <el-checkbox-group v-model="form.notify_roles">
            <el-checkbox label="collector">催员</el-checkbox>
            <el-checkbox label="team_leader">小组长</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, ArrowUp, ArrowDown } from '@element-plus/icons-vue'
import { useTenantStore } from '@/stores/tenant'
import {
  getPublicNotifications,
  createPublicNotification,
  updatePublicNotification,
  deletePublicNotification,
  updatePublicNotificationSort
} from '@/api/notification'
import type { PublicNotification, PublicNotificationCreate, PublicNotificationUpdate } from '@/types/notification'
import type { FormInstance, FormRules } from 'element-plus'

const tenantStore = useTenantStore()
const loading = ref(false)
const notifications = ref<PublicNotification[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('添加公共通知')
const formRef = ref<FormInstance>()
const editingId = ref<number | null>(null)

const form = ref<PublicNotificationCreate>({
  title: '',
  content: '',
  h5_content: null,
  carousel_interval_seconds: 30,
  is_forced_read: false,
  is_enabled: true,
  repeat_interval_minutes: null,
  max_remind_count: null,
  notify_time_start: null,
  notify_time_end: null,
  effective_start_time: null,
  effective_end_time: null,
  notify_roles: ['collector'],
  sort_order: 0
})

const effectiveTimeRange = computed({
  get: () => {
    if (form.value.effective_start_time && form.value.effective_end_time) {
      return [form.value.effective_start_time, form.value.effective_end_time]
    }
    return null
  },
  set: (value: string[] | null) => {
    if (value && value.length === 2) {
      form.value.effective_start_time = value[0]
      form.value.effective_end_time = value[1]
    } else {
      form.value.effective_start_time = null
      form.value.effective_end_time = null
    }
  }
})

// 通知时间范围的计算属性
const notifyTimeStart = computed({
  get: () => form.value.notify_time_start,
  set: (value: string | null) => {
    form.value.notify_time_start = value
  }
})

const notifyTimeEnd = computed({
  get: () => form.value.notify_time_end,
  set: (value: string | null) => {
    form.value.notify_time_end = value
  }
})

const rules: FormRules = {
  title: [
    { required: true, message: '请输入通知标题', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入通知正文', trigger: 'blur' }
  ]
}

// 加载列表
const loadNotifications = async () => {
  try {
    loading.value = true
    const tenantId = tenantStore.currentTenantId || undefined
    const response: any = await getPublicNotifications({ tenant_id: tenantId })
    notifications.value = Array.isArray(response) ? response : (response.data || [])
  } catch (error: any) {
    console.error('加载公共通知失败：', error)
    ElMessage.error('加载公共通知失败：' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 添加
const handleAdd = () => {
  editingId.value = null
  dialogTitle.value = '添加公共通知'
  form.value = {
    title: '',
    content: '',
    h5_content: null,
    carousel_interval_seconds: 30,
    is_forced_read: false,
    is_enabled: true,
    repeat_interval_minutes: null,
    max_remind_count: null,
    notify_time_start: null,
    notify_time_end: null,
    effective_start_time: null,
    effective_end_time: null,
    notify_roles: ['collector'],
    sort_order: notifications.value.length > 0 ? Math.max(...notifications.value.map(n => n.sort_order)) + 1 : 0
  }
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row: PublicNotification) => {
  editingId.value = row.id
  dialogTitle.value = '编辑公共通知'
  form.value = {
    title: row.title,
    content: row.content || '',
    h5_content: row.h5_content || null,
    carousel_interval_seconds: row.carousel_interval_seconds,
    is_forced_read: row.is_forced_read,
    is_enabled: row.is_enabled,
    repeat_interval_minutes: row.repeat_interval_minutes || null,
    max_remind_count: row.max_remind_count || null,
    notify_time_start: row.notify_time_start || null,
    notify_time_end: row.notify_time_end || null,
    effective_start_time: row.effective_start_time || null,
    effective_end_time: row.effective_end_time || null,
    notify_roles: row.notify_roles || [],
    sort_order: row.sort_order
  }
  dialogVisible.value = true
}

// 删除
const handleDelete = async (row: PublicNotification) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除通知"${row.title}"吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await deletePublicNotification(row.id)
    ElMessage.success('删除成功')
    await loadNotifications()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除公共通知失败：', error)
      ElMessage.error('删除失败：' + (error.message || '未知错误'))
    }
  }
}

// 切换启用状态
const handleToggleStatus = async (row: PublicNotification) => {
  try {
    await updatePublicNotification(row.id, {
      is_enabled: row.is_enabled
    })
    ElMessage.success('更新成功')
  } catch (error: any) {
    console.error('更新状态失败：', error)
    ElMessage.error('更新失败：' + (error.message || '未知错误'))
    // 恢复原状态
    row.is_enabled = !row.is_enabled
  }
}

// 上移
const handleMoveUp = async (row: PublicNotification) => {
  const index = notifications.value.findIndex(n => n.id === row.id)
  if (index > 0) {
    const prevRow = notifications.value[index - 1]
    const newSortOrder = prevRow.sort_order
    const prevSortOrder = row.sort_order
    
    try {
      await updatePublicNotificationSort(row.id, newSortOrder)
      await updatePublicNotificationSort(prevRow.id, prevSortOrder)
      await loadNotifications()
    } catch (error: any) {
      console.error('移动失败：', error)
      ElMessage.error('移动失败：' + (error.message || '未知错误'))
    }
  }
}

// 下移
const handleMoveDown = async (row: PublicNotification) => {
  const index = notifications.value.findIndex(n => n.id === row.id)
  if (index < notifications.value.length - 1) {
    const nextRow = notifications.value[index + 1]
    const newSortOrder = nextRow.sort_order
    const nextSortOrder = row.sort_order
    
    try {
      await updatePublicNotificationSort(row.id, newSortOrder)
      await updatePublicNotificationSort(nextRow.id, nextSortOrder)
      await loadNotifications()
    } catch (error: any) {
      console.error('移动失败：', error)
      ElMessage.error('移动失败：' + (error.message || '未知错误'))
    }
  }
}

// 清空生效时间
const clearEffectiveTime = () => {
  effectiveTimeRange.value = null
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (editingId.value) {
          // 更新
          await updatePublicNotification(editingId.value, form.value as PublicNotificationUpdate)
          ElMessage.success('更新成功')
        } else {
          // 创建
          await createPublicNotification(form.value)
          ElMessage.success('创建成功')
        }
        dialogVisible.value = false
        await loadNotifications()
      } catch (error: any) {
        console.error('保存失败：', error)
        ElMessage.error('保存失败：' + (error.message || '未知错误'))
      }
    }
  })
}

// 对话框关闭
const handleDialogClose = () => {
  formRef.value?.resetFields()
  editingId.value = null
}

onMounted(() => {
  loadNotifications()
})
</script>

<style scoped>
.public-notification-config {
  padding: 20px;
}

.header-actions {
  margin-bottom: 20px;
}
</style>

