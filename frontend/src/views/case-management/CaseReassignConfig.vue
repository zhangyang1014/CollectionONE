<template>
  <div class="case-reassign-config">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>案件重新分案配置</span>
          <el-button 
            type="primary" 
            @click="handleAdd" 
            :disabled="!currentTenantId"
          >
            创建配置
          </el-button>
        </div>
      </template>

      <!-- 筛选器 -->
      <el-form :model="filters" class="filter-form" label-width="100px" inline>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">刷新</el-button>
        </el-form-item>
      </el-form>

      <!-- 配置列表 -->
      <el-table :data="configs" border style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="target_name" label="队列名称" width="200">
          <template #default="{ row }">
            {{ row.target_name || `队列ID: ${row.targetId || row.target_id}` }}
          </template>
        </el-table-column>
        <el-table-column prop="reassignDays" label="重新分案天数" width="120" align="center">
          <template #default="{ row }">
            <el-tag type="warning">{{ row.reassignDays || row.reassign_days }} 天</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="effectiveDate" label="生效日期" width="120" align="center">
          <template #default="{ row }">
            <span :class="{ 'effective-today': isEffectiveToday(row.effectiveDate || row.effective_date) }">
              {{ formatDate(row.effectiveDate || row.effective_date) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="isActive" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="(row.isActive !== undefined ? row.isActive : row.is_active) ? 'success' : 'info'">
              {{ (row.isActive !== undefined ? row.isActive : row.is_active) ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ row.createdAt || row.created_at }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)" size="small">
              编辑
            </el-button>
            <el-button 
              link 
              type="danger" 
              @click="handleDelete(row)" 
              size="small"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 创建/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="form" label-width="140px" :rules="rules" ref="formRef">
        <el-form-item label="队列" prop="target_id">
          <el-select 
            v-model="form.target_id" 
            placeholder="请选择队列"
            style="width: 100%"
            :loading="targetLoading"
            @change="handleQueueChange"
          >
            <el-option
              v-for="item in targetOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
          <div style="margin-top: 5px; color: #909399; font-size: 12px;">
            选择要配置重新分案规则的队列
          </div>
        </el-form-item>

        <el-form-item label="生效小组" prop="team_ids" v-if="form.target_id">
          <el-select 
            v-model="form.team_ids" 
            placeholder="请选择小组（不选则针对该队列下所有小组）"
            style="width: 100%"
            multiple
            :loading="teamLoading"
            clearable
            collapse-tags
            collapse-tags-tooltip
          >
            <el-option
              v-for="team in queueTeams"
              :key="team.id"
              :label="team.team_name || team.teamName"
              :value="team.id"
            />
          </el-select>
          <div style="margin-top: 5px; color: #909399; font-size: 12px;">
            选择针对哪些小组生效，不选择则针对该队列下所有小组
          </div>
        </el-form-item>

        <el-form-item label="重新分案天数" prop="reassign_days">
          <el-input-number 
            v-model="form.reassign_days" 
            :min="1" 
            :max="365"
            :precision="0"
            placeholder="请输入天数"
            style="width: 100%"
          />
          <div style="margin-top: 5px; color: #909399; font-size: 12px;">
            案件在催员手里超过此天数后，将自动重新分配给其他催员（整数天）
          </div>
        </el-form-item>

        <el-form-item label="是否启用" prop="is_active">
          <el-switch v-model="form.is_active" />
          <div style="margin-top: 5px; color: #909399; font-size: 12px;">
            禁用后，该配置将不会生效
          </div>
        </el-form-item>

        <el-alert
          v-if="!isEdit"
          title="生效时间说明"
          type="info"
          :closable="false"
          style="margin-bottom: 20px;"
        >
          <template #default>
            <div style="font-size: 12px;">
              配置创建后，将在 <strong>T+1日</strong> 生效（即明天生效）<br>
              定时任务将在每天 <strong>02:00</strong> 执行重新分案
            </div>
          </template>
        </el-alert>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useTenantStore } from '@/stores/tenant'
import {
  getCaseReassignConfigs,
  createCaseReassignConfig,
  updateCaseReassignConfig,
  deleteCaseReassignConfig
} from '@/api/case'
import { getTenantQueues } from '@/api/queue'
import { getTenantAgencies, getTeams, getAgencyTeams } from '@/api/organization'

const tenantStore = useTenantStore()
const currentTenantId = computed(() => tenantStore.currentTenantId)

// 数据
const loading = ref(false)
const configs = ref<any[]>([])
const filters = reactive({})

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('创建配置')
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref()

// 表单数据
const form = reactive({
  id: null as number | null,
  tenant_id: null as number | null,
  config_type: 'queue' as 'queue',  // 固定为队列
  target_id: null as number | null,
  team_ids: [] as number[],  // 小组ID列表
  reassign_days: null as number | null,
  is_active: true
})

// 目标选项
const targetOptions = ref<any[]>([])
const targetLoading = ref(false)

// 队列下的小组
const queueTeams = ref<any[]>([])
const teamLoading = ref(false)

// 表单验证规则
const rules = {
  target_id: [
    { required: true, message: '请选择队列', trigger: 'change' }
  ],
  reassign_days: [
    { required: true, message: '请输入重新分案天数', trigger: 'blur' },
    { type: 'number', min: 1, max: 365, message: '天数必须在1-365之间', trigger: 'blur' }
  ]
}


// 格式化日期
const formatDate = (date: string) => {
  if (!date) return '-'
  return date.split('T')[0]
}

// 判断是否今天生效
const isEffectiveToday = (date: string) => {
  if (!date) return false
  const today = new Date().toISOString().split('T')[0]
  return date.split('T')[0] === today
}

// 加载队列选项
const loadTargetOptions = async () => {
  if (!currentTenantId.value) {
    targetOptions.value = []
    return
  }

  targetLoading.value = true
  try {
    const response = await getTenantQueues(currentTenantId.value)
    targetOptions.value = (Array.isArray(response) ? response : response.items || []).map((q: any) => ({
      id: q.id,
      name: q.queue_name || q.queueCode
    }))
  } catch (error) {
    console.error('加载队列选项失败:', error)
    ElMessage.error('加载队列选项失败')
  } finally {
    targetLoading.value = false
  }
}

// 队列变化时，加载该队列下的小组
const handleQueueChange = async (queueId: number | null) => {
  form.team_ids = []
  queueTeams.value = []
  
  if (!queueId || !currentTenantId.value) {
    return
  }

  teamLoading.value = true
  try {
    // 加载所有机构
    const agencies = await getTenantAgencies(currentTenantId.value)
    const agencyList = Array.isArray(agencies) ? agencies : []
    
    // 遍历所有机构，加载每个机构的小组，然后过滤出属于该队列的小组
    const allTeams: any[] = []
    for (const agency of agencyList) {
      try {
        const teams = await getAgencyTeams(agency.id)
        const teamList = Array.isArray(teams) ? teams : (teams.data || [])
        
        // 过滤出属于该队列的小组
        const queueTeamList = teamList.filter((t: any) => 
          (t.queue_id || t.queueId) === queueId && (t.is_active !== false)
        )
        
        allTeams.push(...queueTeamList)
      } catch (e) {
        console.error(`加载机构 ${agency.id} 的小组失败:`, e)
      }
    }
    
    queueTeams.value = allTeams
    console.log(`队列 ${queueId} 下的小组数量:`, allTeams.length)
  } catch (error) {
    console.error('加载队列小组失败:', error)
    ElMessage.error('加载队列小组失败')
  } finally {
    teamLoading.value = false
  }
}

// 查询配置列表
const handleQuery = async () => {
  if (!currentTenantId.value) {
    ElMessage.warning('请先选择甲方')
    return
  }

  loading.value = true
  try {
    const params: any = {
      tenant_id: currentTenantId.value,
      config_type: 'queue'  // 固定查询队列配置
    }

    const response = await getCaseReassignConfigs(params)
    console.log('查询配置列表响应:', response)
    
    // request.ts 已经提取了 data 字段，所以 response 就是 data 数组
    let configList: any[] = []
    if (Array.isArray(response)) {
      configList = response
    } else if (response && response.items && Array.isArray(response.items)) {
      configList = response.items
    } else if (response && response.data && Array.isArray(response.data)) {
      configList = response.data
    }
    
    console.log('解析后的配置列表:', configList)
    
    // 加载队列名称
    const queues = await getTenantQueues(currentTenantId.value)
    const queueList = Array.isArray(queues) ? queues : (queues.items || [])
    console.log('队列列表:', queueList)
    
    for (const config of configList) {
      // 后端返回的是驼峰命名 targetId，需要兼容处理
      const targetId = config.targetId || config.target_id
      const queue = queueList.find((q: any) => q.id === targetId)
      config.target_name = queue?.queue_name || queue?.queueCode || `队列ID: ${targetId}`
      console.log(`配置 ${config.id}: targetId=${targetId}, queue=`, queue, 'target_name=', config.target_name)
    }

    configs.value = configList
    console.log('最终配置列表:', configs.value)
  } catch (error) {
    console.error('查询配置列表失败:', error)
    ElMessage.error('查询配置列表失败')
  } finally {
    loading.value = false
  }
}

// 创建配置
const handleAdd = () => {
  if (!currentTenantId.value) {
    ElMessage.warning('请先选择甲方')
    return
  }

  isEdit.value = false
  dialogTitle.value = '创建配置'
  Object.assign(form, {
    id: null,
    tenant_id: currentTenantId.value,
    config_type: 'queue',  // 固定为队列
    target_id: null,
    team_ids: [],  // 小组ID列表
    reassign_days: null,
    is_active: true
  })
  queueTeams.value = []
  loadTargetOptions()
  dialogVisible.value = true
}

// 编辑配置
const handleEdit = (row: any) => {
  isEdit.value = true
  dialogTitle.value = '编辑配置'
  // 后端返回的是驼峰命名，需要兼容处理
  const teamIdsStr = row.teamIds || row.team_ids
  let teamIds: number[] = []
  if (teamIdsStr) {
    try {
      teamIds = typeof teamIdsStr === 'string' ? JSON.parse(teamIdsStr) : teamIdsStr
    } catch (e) {
      console.error('解析teamIds失败:', e)
    }
  }
  
  Object.assign(form, {
    id: row.id,
    tenant_id: row.tenantId || row.tenant_id,
    config_type: row.configType || row.config_type || 'queue',
    target_id: row.targetId || row.target_id,
    team_ids: teamIds,
    reassign_days: row.reassignDays || row.reassign_days,
    is_active: row.isActive !== undefined ? row.isActive : (row.is_active !== undefined ? row.is_active : true)
  })
  loadTargetOptions()
  // 加载队列下的小组
  if (form.target_id) {
    handleQueueChange(form.target_id)
  }
  dialogVisible.value = true
}

// 删除配置
const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除该配置吗？删除后该配置将不再生效。`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await deleteCaseReassignConfig(row.id)
    ElMessage.success('删除成功')
    handleQuery()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除配置失败:', error)
      ElMessage.error('删除配置失败')
    }
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  // 确保 tenantId 存在
  if (!currentTenantId.value) {
    ElMessage.warning('请先选择甲方')
    return
  }

  try {
    await formRef.value.validate()

    // 强制设置 tenant_id（确保始终使用当前选择的甲方）
    const tenantId = currentTenantId.value
    if (!tenantId) {
      ElMessage.error('无法获取甲方ID，请刷新页面后重试')
      return
    }

    submitting.value = true
    try {
      const submitData = {
        tenant_id: tenantId,
        config_type: 'queue' as const,  // 固定为队列
        target_id: form.target_id!,
        team_ids: form.team_ids && form.team_ids.length > 0 ? form.team_ids : undefined,
        reassign_days: form.reassign_days!,
        is_active: form.is_active
      }

      if (isEdit.value) {
        await updateCaseReassignConfig(form.id!, submitData)
        ElMessage.success('更新成功')
      } else {
        // 先尝试创建，如果有冲突会返回409错误
        try {
          await createCaseReassignConfig(submitData, false)
          ElMessage.success('创建成功')
        } catch (error: any) {
          // 检查是否是冲突错误（409）
          if (error.response?.status === 409 || error.code === 409 || (error.response?.data?.code === 409)) {
            const conflictData = error.response?.data?.data || error.data?.data || {}
            const conflicts = conflictData.conflicts || []
            
            if (conflicts.length > 0) {
              // 显示冲突信息，询问用户是否替换
              const conflictInfo = conflicts.map((c: any) => {
                const teamIdsStr = c.teamIds || c.team_ids || '[]'
                let teamIds: number[] = []
                try {
                  teamIds = typeof teamIdsStr === 'string' ? JSON.parse(teamIdsStr) : teamIdsStr
                } catch (e) {}
                return `配置ID ${c.id}（重新分案天数：${c.reassignDays || c.reassign_days}天，小组：${teamIds.length > 0 ? teamIds.join(', ') : '全部小组'}）`
              }).join('\n')
              
              await ElMessageBox.confirm(
                `存在冲突的配置：\n${conflictInfo}\n\n是否替换为当前配置？替换后，冲突的配置将被删除。`,
                '发现冲突配置',
                {
                  confirmButtonText: '替换',
                  cancelButtonText: '取消',
                  type: 'warning',
                  dangerouslyUseHTMLString: false
                }
              )
              
              // 用户确认替换，使用 replace=true 参数重新创建
              await createCaseReassignConfig(submitData, true)
              ElMessage.success('创建成功，已替换冲突配置')
            } else {
              throw error
            }
          } else {
            throw error
          }
        }
      }
      dialogVisible.value = false
      handleQuery()
    } catch (error: any) {
      console.error('提交失败:', error)
      ElMessage.error(error.message || '操作失败')
    } finally {
      submitting.value = false
    }
  } catch (error) {
    console.error('表单验证失败:', error)
  }
}

// 监听 currentTenantId 变化，自动查询
watch(
  () => currentTenantId.value,
  (newTenantId) => {
    if (newTenantId) {
      handleQuery()
    } else {
      configs.value = []
    }
  },
  { immediate: true }
)

// 初始化
onMounted(() => {
  // 确保从 localStorage 恢复 tenantId
  tenantStore.restoreFromStorage()
  
  // 如果还是没有 tenantId，尝试从 MainLayout 获取
  if (!currentTenantId.value) {
    // 等待一下，让 MainLayout 先初始化
    setTimeout(() => {
      if (currentTenantId.value) {
        handleQuery()
      }
    }, 500)
  } else {
    handleQuery()
  }
})
</script>

<style scoped>
.case-reassign-config {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filter-form {
  margin-bottom: 20px;
}

.effective-today {
  color: #67c23a;
  font-weight: bold;
}
</style>

