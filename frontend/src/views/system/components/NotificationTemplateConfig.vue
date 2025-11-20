<template>
  <div class="notification-template-config">
    <div class="header-actions">
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        添加通知模板
      </el-button>
    </div>

    <el-table
      :data="templates"
      border
      stripe
      v-loading="loading"
      style="width: 100%"
    >
      <el-table-column prop="template_name" label="模板名称" min-width="180" />

      <el-table-column prop="template_id" label="模板ID" width="150" />

      <el-table-column prop="template_type" label="模板类型" width="150">
        <template #default="{ row }">
          <el-tag>{{ getTemplateTypeLabel(row.template_type) }}</el-tag>
        </template>
      </el-table-column>

      <el-table-column prop="target_type" label="发送对象" width="120" align="center">
        <template #default="{ row }">
          <el-tag :type="getTargetTypeColor(row.target_type)">
            {{ getTargetTypeLabel(row.target_type) }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column prop="priority" label="优先级" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="getPriorityColor(row.priority)">
            {{ getPriorityLabel(row.priority) }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column prop="is_forced_read" label="强制阅读" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.is_forced_read ? 'danger' : 'info'">
            {{ row.is_forced_read ? '是' : '否' }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column label="统计" width="120" align="center">
        <template #default="{ row }">
          <div style="font-size: 12px;">
            <div>发送: {{ row.total_sent }}</div>
            <div>阅读: {{ row.total_read }}</div>
          </div>
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
      width="900px"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="150px"
      >
        <!-- 是否启用 -->
        <el-form-item label="是否启用">
          <el-switch v-model="form.is_enabled" />
          <span style="margin-left: 10px; color: #909399;">
            关闭后，该模板将不会接收推送
          </span>
        </el-form-item>

        <el-divider />

        <!-- 基本信息 -->
        <el-form-item label="模板ID" prop="template_id">
          <el-input 
            v-model="form.template_id" 
            placeholder="请输入模板唯一标识，如：case_tag_change"
            :disabled="!!editingId"
          />
          <div style="margin-top: 5px; color: #909399; font-size: 12px;">
            模板ID创建后不可修改，用于甲方系统调用
          </div>
        </el-form-item>

        <el-form-item label="模板名称" prop="template_name">
          <el-input v-model="form.template_name" placeholder="请输入模板名称" />
        </el-form-item>

        <el-form-item label="模板类型" prop="template_type">
          <el-select 
            v-model="form.template_type" 
            placeholder="请选择模板类型"
            style="width: 100%;"
            @change="handleTemplateTypeChange"
          >
            <el-option
              v-for="type in templateTypes"
              :key="type.value"
              :label="type.label"
              :value="type.value"
            >
              <div>
                <div>{{ type.label }}</div>
                <div style="font-size: 12px; color: #909399;">{{ type.description }}</div>
              </div>
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="模板描述">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="2"
            placeholder="请输入模板描述"
          />
        </el-form-item>

        <el-divider />

        <!-- 通知内容 -->
        <el-form-item label="通知正文模板" prop="content_template">
          <el-input
            v-model="form.content_template"
            type="textarea"
            :rows="4"
            placeholder="请输入通知正文模板，支持变量如：{case_id}、{amount}等"
          />
          <div v-if="currentTemplateType" style="margin-top: 5px; font-size: 12px;">
            <div style="color: #909399;">可用变量：</div>
            <el-tag
              v-for="(desc, key) in currentTemplateType.variables"
              :key="key"
              size="small"
              style="margin: 2px 4px;"
              @click="insertVariable(key)"
            >
              {{'{'}}{{ key }}{{'}'}}: {{ desc }}
            </el-tag>
          </div>
        </el-form-item>

        <el-form-item label="跳转URL模板">
          <el-input
            v-model="form.jump_url_template"
            placeholder="点击通知后跳转的URL，支持变量"
          />
          <div style="margin-top: 5px; color: #909399; font-size: 12px;">
            示例：/cases/{case_id}
          </div>
        </el-form-item>

        <el-divider />

        <!-- 发送对象配置 -->
        <el-form-item label="发送对象类型" prop="target_type">
          <el-radio-group v-model="form.target_type">
            <el-radio value="agency">机构</el-radio>
            <el-radio value="team">小组</el-radio>
            <el-radio value="collector">指定催员</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item v-if="form.target_type === 'agency'" label="目标机构">
          <el-select
            v-model="form.target_agencies"
            multiple
            placeholder="请选择目标机构，不选则发送给所有机构"
            style="width: 100%;"
          >
            <el-option label="机构A" :value="1" />
            <el-option label="机构B" :value="2" />
          </el-select>
        </el-form-item>

        <el-form-item v-if="form.target_type === 'team'" label="目标小组">
          <el-select
            v-model="form.target_teams"
            multiple
            placeholder="请选择目标小组，不选则发送给所有小组"
            style="width: 100%;"
          >
            <el-option label="小组1" :value="1" />
            <el-option label="小组2" :value="2" />
          </el-select>
        </el-form-item>

        <el-form-item v-if="form.target_type === 'collector'" label="目标催员">
          <el-select
            v-model="form.target_collectors"
            multiple
            placeholder="请选择目标催员"
            style="width: 100%;"
          >
            <el-option label="催员1" :value="1" />
            <el-option label="催员2" :value="2" />
          </el-select>
        </el-form-item>

        <el-divider />

        <!-- 优先级和展示 -->
        <el-form-item label="优先级">
          <el-radio-group v-model="form.priority">
            <el-radio :value="1">高</el-radio>
            <el-radio :value="2">中</el-radio>
            <el-radio :value="3">低</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="展示时长（秒）">
          <el-input-number
            v-model="form.display_duration_seconds"
            :min="0"
            :max="60"
            style="width: 200px;"
          />
          <span style="margin-left: 10px; color: #909399;">
            通知在屏幕上显示的时长
          </span>
        </el-form-item>

        <el-divider />

        <!-- 阅读机制 -->
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
import { Plus } from '@element-plus/icons-vue'
import { useTenantStore } from '@/stores/tenant'
import {
  getNotificationTemplates,
  createNotificationTemplate,
  updateNotificationTemplate,
  deleteNotificationTemplate,
  getTemplateTypes
} from '@/api/notificationTemplate'
import type { 
  NotificationTemplate, 
  NotificationTemplateCreate,
  NotificationTemplateUpdate,
  TemplateType
} from '@/types/notification'
import type { FormInstance, FormRules } from 'element-plus'

const tenantStore = useTenantStore()
const loading = ref(false)
const templates = ref<NotificationTemplate[]>([])
const templateTypes = ref<TemplateType[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('添加通知模板')
const formRef = ref<FormInstance>()
const editingId = ref<number | null>(null)

const form = ref<NotificationTemplateCreate>({
  template_id: '',
  template_name: '',
  template_type: '',
  description: null,
  content_template: '',
  jump_url_template: null,
  target_type: 'agency',
  target_agencies: null,
  target_teams: null,
  target_collectors: null,
  is_forced_read: false,
  repeat_interval_minutes: null,
  max_remind_count: null,
  notify_time_start: null,
  notify_time_end: null,
  priority: 2,
  display_duration_seconds: 5,
  is_enabled: true,
  available_variables: null
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

// 当前选中的模板类型
const currentTemplateType = computed(() => {
  return templateTypes.value.find(t => t.value === form.value.template_type)
})

const rules: FormRules = {
  template_id: [
    { required: true, message: '请输入模板ID', trigger: 'blur' }
  ],
  template_name: [
    { required: true, message: '请输入模板名称', trigger: 'blur' }
  ],
  template_type: [
    { required: true, message: '请选择模板类型', trigger: 'change' }
  ],
  content_template: [
    { required: true, message: '请输入通知正文模板', trigger: 'blur' }
  ],
  target_type: [
    { required: true, message: '请选择发送对象类型', trigger: 'change' }
  ]
}

// 加载模板类型
const loadTemplateTypes = async () => {
  try {
    const response = await getTemplateTypes()
    templateTypes.value = response.types
  } catch (error: any) {
    console.error('加载模板类型失败：', error)
  }
}

// 加载列表
const loadTemplates = async () => {
  try {
    loading.value = true
    const tenantId = tenantStore.currentTenantId || undefined
    const response = await getNotificationTemplates({ tenant_id: tenantId })
    templates.value = Array.isArray(response) ? response : []
  } catch (error: any) {
    console.error('加载通知模板失败：', error)
    ElMessage.error('加载通知模板失败：' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 添加
const handleAdd = () => {
  editingId.value = null
  dialogTitle.value = '添加通知模板'
  form.value = {
    template_id: '',
    template_name: '',
    template_type: '',
    description: null,
    content_template: '',
    jump_url_template: null,
    target_type: 'agency',
    target_agencies: null,
    target_teams: null,
    target_collectors: null,
    is_forced_read: false,
    repeat_interval_minutes: null,
    max_remind_count: null,
    notify_time_start: null,
    notify_time_end: null,
    priority: 2,
    display_duration_seconds: 5,
    is_enabled: true,
    available_variables: null
  }
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row: NotificationTemplate) => {
  editingId.value = row.id
  dialogTitle.value = '编辑通知模板'
  form.value = {
    template_id: row.template_id,
    template_name: row.template_name,
    template_type: row.template_type,
    description: row.description || null,
    content_template: row.content_template,
    jump_url_template: row.jump_url_template || null,
    target_type: row.target_type,
    target_agencies: row.target_agencies || null,
    target_teams: row.target_teams || null,
    target_collectors: row.target_collectors || null,
    is_forced_read: row.is_forced_read,
    repeat_interval_minutes: row.repeat_interval_minutes || null,
    max_remind_count: row.max_remind_count || null,
    notify_time_start: row.notify_time_start || null,
    notify_time_end: row.notify_time_end || null,
    priority: row.priority,
    display_duration_seconds: row.display_duration_seconds,
    is_enabled: row.is_enabled,
    available_variables: row.available_variables || null
  }
  dialogVisible.value = true
}

// 删除
const handleDelete = async (row: NotificationTemplate) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除模板"${row.template_name}"吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await deleteNotificationTemplate(row.id)
    ElMessage.success('删除成功')
    await loadTemplates()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除通知模板失败：', error)
      ElMessage.error('删除失败：' + (error.message || '未知错误'))
    }
  }
}

// 切换启用状态
const handleToggleStatus = async (row: NotificationTemplate) => {
  try {
    await updateNotificationTemplate(row.id, {
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

// 模板类型改变
const handleTemplateTypeChange = (value: string) => {
  const type = templateTypes.value.find(t => t.value === value)
  if (type) {
    form.value.available_variables = type.variables
  }
}

// 插入变量
const insertVariable = (varName: string) => {
  const variable = `{${varName}}`
  form.value.content_template += variable
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (editingId.value) {
          // 更新
          await updateNotificationTemplate(editingId.value, form.value as NotificationTemplateUpdate)
          ElMessage.success('更新成功')
        } else {
          // 创建
          await createNotificationTemplate(form.value)
          ElMessage.success('创建成功')
        }
        dialogVisible.value = false
        await loadTemplates()
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

// 辅助函数
const getTemplateTypeLabel = (type: string) => {
  const found = templateTypes.value.find(t => t.value === type)
  return found ? found.label : type
}

const getTargetTypeLabel = (type: string) => {
  const map: Record<string, string> = {
    agency: '机构',
    team: '小组',
    collector: '催员'
  }
  return map[type] || type
}

const getTargetTypeColor = (type: string) => {
  const map: Record<string, string> = {
    agency: 'success',
    team: 'warning',
    collector: 'primary'
  }
  return map[type] || ''
}

const getPriorityLabel = (priority: number) => {
  const map: Record<number, string> = {
    1: '高',
    2: '中',
    3: '低'
  }
  return map[priority] || String(priority)
}

const getPriorityColor = (priority: number) => {
  const map: Record<number, string> = {
    1: 'danger',
    2: 'warning',
    3: 'info'
  }
  return map[priority] || ''
}

onMounted(() => {
  loadTemplateTypes()
  loadTemplates()
})
</script>

<style scoped>
.notification-template-config {
  padding: 20px;
}

.header-actions {
  margin-bottom: 20px;
}
</style>

