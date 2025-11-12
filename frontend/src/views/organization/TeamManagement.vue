<template>
  <div class="team-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>小组管理</span>
          <el-space>
            <el-select 
              v-model="currentAgencyId" 
              placeholder="全部机构" 
              @change="handleAgencyChange"
              style="width: 160px"
              clearable
            >
              <el-option label="全部机构" :value="undefined" />
              <el-option
                v-for="agency in agencies"
                :key="agency.id"
                :label="agency.agency_name"
                :value="agency.id"
              />
            </el-select>
            <el-select 
              v-model="currentTeamId" 
              placeholder="全部小组" 
              @change="loadTeams"
              style="width: 160px"
              clearable
              :disabled="!currentAgencyId"
            >
              <el-option label="全部小组" :value="undefined" />
              <el-option
                v-for="team in filteredTeams"
                :key="team.id"
                :label="team.team_name"
                :value="team.id"
              />
            </el-select>
            <el-button 
              type="primary" 
              @click="handleAdd" 
              :disabled="!currentTenantId"
            >
              创建小组
            </el-button>
          </el-space>
        </div>
      </template>

      <el-table :data="teams" border style="width: 100%">
        <el-table-column prop="team_code" label="小组ID" width="120" />
        <el-table-column prop="team_name" label="小组名" width="180" />
        <el-table-column prop="tenant_name" label="所属甲方" width="150" />
        <el-table-column prop="agency_name" label="所属机构" width="150" />
        <el-table-column prop="collector_count" label="催员账号数量" width="120" align="center" />
        <el-table-column prop="created_at" label="创建时间" width="180" />
        <el-table-column prop="updated_at" label="最近修改时间" width="180" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.is_active ? 'success' : 'info'">
              {{ row.is_active ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)" size="small">
              编辑
            </el-button>
            <el-button 
              link 
              :type="row.is_active ? 'warning' : 'success'" 
              @click="handleToggleStatus(row)" 
              size="small"
            >
              {{ row.is_active ? '禁用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 创建/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="form" label-width="120px" :rules="rules" ref="formRef">
        <el-form-item label="小组编码" prop="team_code">
          <el-input 
            v-model="form.team_code" 
            placeholder="如：TEAM001" 
            maxlength="50"
            :disabled="isEdit"
          />
        </el-form-item>

        <el-form-item label="小组名称" prop="team_name">
          <el-input v-model="form.team_name" placeholder="请输入小组名称" maxlength="100" />
        </el-form-item>

        <el-form-item label="所属机构" prop="agency_id">
          <el-select v-model="form.agency_id" placeholder="选择机构" style="width: 100%">
            <el-option
              v-for="agency in agencies"
              :key="agency.id"
              :label="agency.agency_name"
              :value="agency.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="备注">
          <el-input 
            v-model="form.remark" 
            type="textarea" 
            :rows="3"
            placeholder="请输入备注信息"
            maxlength="500"
          />
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
import { ref, reactive, watch, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { useTenantStore } from '@/stores/tenant'

const tenantStore = useTenantStore()
const agencies = ref<any[]>([])
const currentTenantId = ref<number | undefined>(tenantStore.currentTenantId)
const currentAgencyId = ref<number | undefined>(undefined) // 默认全选
const currentTeamId = ref<number | undefined>(undefined) // 默认全选
const allTeams = ref<any[]>([]) // 存储所有小组（用于筛选）
const teams = ref<any[]>([]) // 显示在表格中的小组
const dialogVisible = ref(false)
const dialogTitle = ref('')
const saving = ref(false)
const formRef = ref<FormInstance>()
const isEdit = ref(false)

// 计算属性：用于小组选择器的选项（根据选择的机构筛选）
const filteredTeams = computed(() => {
  if (!currentAgencyId.value) {
    return allTeams.value
  }
  return allTeams.value.filter(team => team.agency_id === currentAgencyId.value)
})

// 监听全局甲方变化
watch(
  () => tenantStore.currentTenantId,
  async (newTenantId, oldTenantId) => {
    currentTenantId.value = newTenantId
    currentAgencyId.value = undefined // 重置为全选
    currentTeamId.value = undefined // 重置为全选
    teams.value = []
    allTeams.value = []
    agencies.value = []
    
    if (newTenantId) {
      await loadAgencies()
      await loadAllTeams() // 默认加载所有小组
      await loadTeams() // 应用筛选
    }
  }
)

// 初始加载
onMounted(async () => {
  if (currentTenantId.value) {
    await loadAgencies()
    await loadAllTeams() // 默认加载所有小组
    await loadTeams() // 应用筛选
  }
})

// 模拟组长数据
const leaders = ref([
  { id: 1, name: '组长A' },
  { id: 2, name: '组长B' },
  { id: 3, name: '组长C' }
])

const form = ref({
  id: undefined as number | undefined,
  team_code: '',
  team_name: '',
  agency_id: undefined as number | undefined,
  leader_id: undefined as number | undefined,
  target_performance: 0,
  remark: '',
  is_active: true
})

const rules = reactive({
  team_code: [
    { required: true, message: '请输入小组编码', trigger: 'blur' }
  ],
  team_name: [
    { required: true, message: '请输入小组名称', trigger: 'blur' }
  ],
  agency_id: [
    { required: true, message: '请选择所属机构', trigger: 'change' }
  ],
  leader_id: [
    { required: true, message: '请选择小组组长', trigger: 'change' }
  ]
})

// 加载机构列表
const loadAgencies = async () => {
  if (!currentTenantId.value) {
    agencies.value = []
    return
  }

  try {
    const url = `http://localhost:8000/api/v1/tenants/${currentTenantId.value}/agencies`
    const response = await fetch(url)
    const result = await response.json()
    
    // API直接返回数组，不是{data: [...]}格式
    agencies.value = Array.isArray(result) ? result : (result.data || [])
  } catch (error) {
    console.error('加载机构失败：', error)
    ElMessage.error('加载机构失败')
  }
}

// 加载所有小组（所有机构的）
const loadAllTeams = async () => {
  if (!currentTenantId.value) {
    allTeams.value = []
    return
  }

  try {
    await loadAgencies()
    const allTeamsList: any[] = []
    
    // 遍历所有机构，加载每个机构的小组
    for (const agency of agencies.value) {
      try {
        const teamsUrl = `http://localhost:8000/api/v1/agencies/${agency.id}/teams`
        const teamsResponse = await fetch(teamsUrl)
        const teamsResult = await teamsResponse.json()
        // API直接返回数组，不是{data: [...]}格式
        const agencyTeams = Array.isArray(teamsResult) ? teamsResult : (teamsResult.data || [])
        
        // 为每个小组添加机构信息
        agencyTeams.forEach((team: any) => {
          team.agency_name = agency.agency_name
          team.agency_id = agency.id
        })
        
        allTeamsList.push(...agencyTeams)
      } catch (error) {
        console.error(`加载机构 ${agency.id} 的小组失败：`, error)
      }
    }
    
    allTeams.value = allTeamsList
    console.log(`已加载 ${allTeams.value.length} 个小组`)
  } catch (error) {
    console.error('加载小组失败：', error)
    ElMessage.error('加载小组失败')
  }
}

// 机构切换时
const handleAgencyChange = async () => {
  currentTeamId.value = undefined
  await loadTeams() // 重新应用筛选
}

// 加载小组列表（根据筛选条件）
const loadTeams = async () => {
  if (!currentTenantId.value) {
    teams.value = []
    return
  }

  // 如果没有加载所有小组，先加载
  if (allTeams.value.length === 0) {
    await loadAllTeams()
  }

  // 根据筛选条件过滤小组
  let filtered = [...allTeams.value]

  // 如果选择了机构，筛选该机构的小组
  if (currentAgencyId.value) {
    filtered = filtered.filter(team => team.agency_id === currentAgencyId.value)
  }

  // 如果选择了小组，只显示该小组
  if (currentTeamId.value) {
    filtered = filtered.filter(team => team.id === currentTeamId.value)
  }

  teams.value = filtered
  console.log(`已筛选 ${teams.value.length} 个小组`)
}

// 创建小组
const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '创建小组'
  form.value = {
    id: undefined,
    team_code: '',
    team_name: '',
    agency_id: undefined,
    leader_id: undefined,
    target_performance: 0,
    remark: '',
    is_active: true
  }
  dialogVisible.value = true
}

// 编辑小组
const handleEdit = (row: any) => {
  isEdit.value = true
  dialogTitle.value = '编辑小组'
  form.value = {
    id: row.id,
    team_code: row.team_code,
    team_name: row.team_name,
    agency_id: row.agency_id,
    leader_id: row.leader_id,
    target_performance: row.target_performance || 0,
    remark: row.remark || '',
    is_active: row.is_active
  }
  dialogVisible.value = true
}

// 保存小组
const handleSave = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    saving.value = true

    console.log('保存小组：', form.value)
    
    // TODO: 调用API保存
    ElMessage.success('保存成功')
    dialogVisible.value = false
    await loadAllTeams() // 重新加载所有小组
    await loadTeams() // 应用筛选
  } catch (error) {
    console.error('保存失败：', error)
  } finally {
    saving.value = false
  }
}

// 启用/禁用小组
const handleToggleStatus = async (row: any) => {
  try {
    const action = row.is_active ? '禁用' : '启用'
    await ElMessageBox.confirm(
      `确定要${action}小组"${row.team_name}"吗？${row.is_active ? '禁用后该小组下的所有催员将无法工作。' : ''}`,
      `${action}确认`,
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    // TODO: 调用API更新状态
    row.is_active = !row.is_active
    ElMessage.success(`${action}成功`)
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

