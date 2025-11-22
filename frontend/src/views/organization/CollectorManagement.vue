<template>
  <div class="collector-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>催员管理</span>
          <el-space>
            <el-input
              v-model="searchKeyword"
              placeholder="搜索催员登录id或催员名"
              style="width: 240px"
              clearable
              @input="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-select 
              v-model="statusFilter" 
              placeholder="全部状态" 
              style="width: 130px"
            >
              <el-option label="全部" :value="undefined" />
              <el-option label="启用" :value="true" />
              <el-option label="禁用" :value="false" />
            </el-select>
            <el-select 
              v-model="currentAgencyId" 
              placeholder="全部机构" 
              @change="handleAgencyChange"
              style="width: 160px"
              clearable
              v-if="showAgencySelector"
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
              @change="loadCollectors"
              style="width: 160px"
              clearable
              :disabled="!currentAgencyId"
              v-if="showTeamSelector"
            >
              <el-option label="全部小组" :value="undefined" />
              <el-option
                v-for="team in teams"
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
              创建催员
            </el-button>
            <el-button 
              type="success" 
              @click="handleExportAccounts"
              :disabled="!currentTenantId"
            >
              导出催员账号与密码
            </el-button>
          </el-space>
        </div>
      </template>

      <!-- 催员统计信息 -->
      <div class="collector-stats">
        <el-text type="info">
          当前筛选条件下，共有 <el-text type="primary" style="font-weight: 600;">{{ filteredCollectors.length }}</el-text> 位催员
        </el-text>
      </div>

      <el-table :data="filteredCollectors" border style="width: 100%">
        <el-table-column prop="collector_code" label="催员登录id" width="120" />
        <el-table-column prop="collector_name" label="催员名" width="120" />
        <el-table-column prop="last_login_at" label="最近登录时间" width="140">
          <template #default="{ row }">
            <div v-if="row.last_login_at" class="login-time-cell">
              <div class="date-line">{{ formatDate(row.last_login_at) }}</div>
              <div class="time-line">{{ formatTime(row.last_login_at) }}</div>
            </div>
            <span v-else>--</span>
          </template>
        </el-table-column>
        <el-table-column prop="tenant_name" label="所属甲方" width="130" />
        <el-table-column prop="agency_name" label="所属机构" width="130" />
        <el-table-column prop="team_name" label="所属小组" width="130" />
        <el-table-column prop="created_at" label="创建时间" width="160" />
        <el-table-column prop="updated_at" label="最近修改时间" width="160" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.is_active ? 'success' : 'info'">
              {{ row.is_active ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="460" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)" size="small">
              编辑
            </el-button>
            <el-button link type="warning" @click="handleResetPassword(row)" size="small">
              修改密码
            </el-button>
            <el-button link type="info" @click="handleViewLoginFaces(row)" size="small">
              登录人脸查询
            </el-button>
            <el-button link type="success" @click="handleViewIm(row)" size="small">
              查看IM端
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
        <el-form-item label="催员登录id" prop="collector_code">
          <el-input 
            v-model="form.collector_code" 
            placeholder="如：COL001" 
            maxlength="50"
            :disabled="isEdit"
          />
        </el-form-item>

        <el-form-item label="催员姓名" prop="collector_name">
          <el-input v-model="form.collector_name" placeholder="请输入催员姓名" maxlength="50" />
        </el-form-item>

        <el-form-item label="登录密码" prop="password" v-if="!isEdit">
          <div style="display: flex; gap: 8px;">
          <el-input 
            v-model="form.password" 
            type="password"
              placeholder="请输入登录密码或点击生成" 
            maxlength="50"
            show-password
              style="flex: 1;"
          />
            <el-button @click="generatePassword" type="info" plain>生成密码</el-button>
          </div>
        </el-form-item>

        <el-form-item label="所属机构" prop="agency_id">
          <el-select v-model="form.agency_id" placeholder="选择机构" style="width: 100%" @change="handleFormAgencyChange">
            <el-option
              v-for="agency in agencies"
              :key="agency.id"
              :label="agency.agency_name"
              :value="agency.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="所属小组" prop="team_id">
          <el-select v-model="form.team_id" placeholder="选择小组" style="width: 100%">
            <el-option
              v-for="team in formTeams"
              :key="team.id"
              :label="team.team_name"
              :value="team.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="手机号">
          <el-input v-model="form.mobile" placeholder="请输入手机号" maxlength="20" />
        </el-form-item>

        <el-form-item label="邮箱">
          <el-input v-model="form.email" placeholder="请输入邮箱" maxlength="100" />
        </el-form-item>

        <el-form-item label="回呼号码">
          <el-input 
            v-model="form.callback_number" 
            placeholder="请输入回呼号码（用于外呼时接听电话）" 
            maxlength="50"
          >
            <template #append>
              <el-tooltip 
                content="催员接听外呼电话的号码（手机/座机），启用外呼功能时必填" 
                placement="top"
              >
                <el-icon><QuestionFilled /></el-icon>
              </el-tooltip>
            </template>
          </el-input>
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

    <!-- 修改密码对话框 -->
    <el-dialog v-model="passwordDialogVisible" title="修改密码" width="500px">
      <el-form :model="passwordForm" label-width="100px" :rules="passwordRules" ref="passwordFormRef">
        <el-form-item label="新密码" prop="new_password">
          <el-input 
            v-model="passwordForm.new_password" 
            type="password"
            placeholder="请输入新密码" 
            show-password
          />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirm_password">
          <el-input 
            v-model="passwordForm.confirm_password" 
            type="password"
            placeholder="请再次输入新密码" 
            show-password
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSavePassword" :loading="savingPassword">确定</el-button>
      </template>
    </el-dialog>

    <!-- 登录人脸查询对话框 -->
    <el-dialog
      v-model="faceRecordsDialogVisible"
      :title="`${currentCollectorForFace?.collector_name || ''} - 登录人脸查询`"
      width="80%"
      destroy-on-close
    >
      <div v-loading="loadingFaceRecords">
        <el-empty v-if="faceRecords.length === 0 && !loadingFaceRecords" description="暂无登录记录" />
        
        <el-timeline v-else>
          <el-timeline-item
            v-for="record in faceRecords"
            :key="record.id"
            :timestamp="record.login_time"
            placement="top"
            size="large"
          >
            <el-card shadow="hover">
              <div class="face-record-item">
                <div class="face-image-container">
                  <img
                    :src="record.face_image"
                    alt="人脸照片"
                    class="face-image"
                    @error="handleImageError"
                  />
                </div>
                <div class="face-info">
                  <div class="info-row">
                    <span class="label">登录时间：</span>
                    <span class="value">{{ formatDateTime(record.login_time) }}</span>
                  </div>
                  <div class="info-row">
                    <span class="label">人脸ID：</span>
                    <el-tag type="primary" size="small">{{ record.face_id }}</el-tag>
                  </div>
                  <div class="info-row">
                    <span class="label">记录时间：</span>
                    <span class="value">{{ formatDateTime(record.created_at) }}</span>
                  </div>
                </div>
              </div>
            </el-card>
          </el-timeline-item>
        </el-timeline>
      </div>
      
      <template #footer>
        <el-button @click="faceRecordsDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { Search, QuestionFilled } from '@element-plus/icons-vue'
import { useRoute } from 'vue-router'
import { useTenantStore } from '@/stores/tenant'
import { useUserStore } from '@/stores/user'
// import { getCollectorLoginFaceRecords } from '@/api/organization' // TODO: 后续替换Mock数据时使用

const route = useRoute()

const tenantStore = useTenantStore()
const userStore = useUserStore()
const agencies = ref<any[]>([])
const teams = ref<any[]>([])
const formTeams = ref<any[]>([]) // 表单中的小组选项
const currentTenantId = ref<number | undefined>(tenantStore.currentTenantId)
const currentAgencyId = ref<number | undefined>(undefined) // 默认全选
const currentTeamId = ref<number | undefined>(undefined) // 默认全选
const collectors = ref<any[]>([])
const searchKeyword = ref('') // 搜索关键词
const statusFilter = ref<boolean | undefined>(true) // 状态筛选：true=启用, false=禁用, undefined=全部
const dialogVisible = ref(false)
const passwordDialogVisible = ref(false)
const faceRecordsDialogVisible = ref(false)
const dialogTitle = ref('')
const saving = ref(false)
const savingPassword = ref(false)
const loadingFaceRecords = ref(false)
const formRef = ref<FormInstance>()
const passwordFormRef = ref<FormInstance>()
const isEdit = ref(false)
const currentCollector = ref<any>(null)
const currentCollectorForFace = ref<any>(null)
const faceRecords = ref<Array<{
  id: number
  collector_id: number
  login_time: string
  face_image: string
  face_id: string
  created_at: string
}>>([])

// 获取当前用户信息
const currentUser = computed(() => userStore.userInfo)

// 检查用户角色
const userRole = computed(() => {
  return currentUser.value?.role || ''
})

// 获取用户所属机构ID
const userAgencyId = computed(() => {
  return currentUser.value?.agency_id || currentUser.value?.agencyId || null
})

// 获取用户所属小组ID
const userTeamId = computed(() => {
  return currentUser.value?.team_id || currentUser.value?.teamId || null
})

// 是否显示机构选择器（机构管理员不显示）
const showAgencySelector = computed(() => {
  // 如果是机构管理员，不显示机构选择器
  if (userRole.value === 'agency_admin' || userRole.value === 'AgencyAdmin') {
    return false
  }
  return true
})

// 是否显示小组选择器（小组管理员不显示）
const showTeamSelector = computed(() => {
  // 如果是小组管理员，不显示小组选择器
  if (userRole.value === 'team_admin' || userRole.value === 'TeamAdmin' || 
      userRole.value === 'team_leader' || userRole.value === 'TeamLeader') {
    return false
  }
  return true
})

// 过滤后的催员列表（根据搜索关键词和状态筛选）
const filteredCollectors = computed(() => {
  let result = collectors.value
  
  // 状态筛选
  if (statusFilter.value !== undefined) {
    result = result.filter((collector) => collector.is_active === statusFilter.value)
  }
  
  // 关键词搜索
  if (searchKeyword.value && searchKeyword.value.trim() !== '') {
    const keyword = searchKeyword.value.toLowerCase().trim()
    result = result.filter((collector) => {
      // 搜索催员登录id和催员名
      const collectorCode = (collector.collector_code || '').toLowerCase()
      const collectorName = (collector.collector_name || '').toLowerCase()
      
      return collectorCode.includes(keyword) || collectorName.includes(keyword)
    })
  }
  
  return result
})

// 监听全局甲方变化
watch(
  () => tenantStore.currentTenantId,
  async (newTenantId, _oldTenantId) => {
    currentTenantId.value = newTenantId
    currentAgencyId.value = undefined // 重置为全选
    currentTeamId.value = undefined // 重置为全选
    searchKeyword.value = '' // 清空搜索关键词
    statusFilter.value = true // 重置为启用状态
    collectors.value = []
    agencies.value = []
    teams.value = []
    
    if (newTenantId) {
      await loadAgencies()
      await loadCollectors() // 默认加载所有催员
    }
  }
)

// 初始加载
onMounted(async () => {
  if (currentTenantId.value) {
    await loadAgencies()
    
    // 检查URL参数中是否有筛选条件
    const urlAgencyId = route.query.agencyId ? Number(route.query.agencyId) : undefined
    const urlTeamId = route.query.teamId ? Number(route.query.teamId) : undefined
    
    // 优先使用URL参数，否则根据用户角色设置默认值
    if (urlAgencyId) {
      currentAgencyId.value = urlAgencyId
      await loadTeams()
      
      if (urlTeamId) {
        currentTeamId.value = urlTeamId
      }
    } else if (userAgencyId.value && showAgencySelector.value === false) {
      // 机构管理员：自动设置为自己的机构
      currentAgencyId.value = userAgencyId.value
      await loadTeams()
    }
    
    if (userTeamId.value && showTeamSelector.value === false && !urlTeamId) {
      // 小组管理员：自动设置为自己的小组（如果URL没有指定）
      currentTeamId.value = userTeamId.value
    }
    
    await loadCollectors() // 默认加载所有催员
  }
})

const form = ref({
  id: undefined as number | undefined,
  collector_code: '',
  collector_name: '',
  password: '',
  agency_id: undefined as number | undefined,
  team_id: undefined as number | undefined,
  role: 'collector',
  mobile: '',
  callback_number: '',
  email: '',
  remark: '',
  is_active: true
})

const passwordForm = ref({
  new_password: '',
  confirm_password: ''
})

const rules = reactive({
  collector_code: [
    { required: true, message: '请输入催员登录id', trigger: 'blur' }
  ],
  collector_name: [
    { required: true, message: '请输入催员姓名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入登录密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  agency_id: [
    { required: true, message: '请选择所属机构', trigger: 'change' }
  ],
  team_id: [
    { required: true, message: '请选择所属小组', trigger: 'change' }
  ]
})

const passwordRules = reactive({
  new_password: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirm_password: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { 
      validator: (_rule: any, value: any, callback: any) => {
        if (value !== passwordForm.value.new_password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
})

// 机构切换时加载小组
const handleAgencyChange = async () => {
  currentTeamId.value = undefined
  collectors.value = []
  searchKeyword.value = '' // 清空搜索关键词
  
  if (currentAgencyId.value) {
    // 选择了机构，加载该机构的小组
    await loadTeams()
    // 加载该机构的所有催员（小组为全选）
    await loadCollectors()
  } else {
    // 选择"全部机构"，清空小组列表，加载所有催员
    teams.value = []
    await loadCollectors()
  }
}

// 表单中机构切换时加载小组
const handleFormAgencyChange = async () => {
  form.value.team_id = undefined
  if (form.value.agency_id) {
    await loadFormTeams(form.value.agency_id)
  } else {
    formTeams.value = []
  }
}

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
    let allAgencies = Array.isArray(result) ? result : (result.data || [])
    
    // 如果是机构管理员，只显示自己所属的机构
    if (userAgencyId.value && showAgencySelector.value === false) {
      allAgencies = allAgencies.filter((agency: any) => agency.id === userAgencyId.value)
    }
    
    agencies.value = allAgencies
  } catch (error) {
    console.error('加载机构失败：', error)
    ElMessage.error('加载机构失败')
  }
}

// 加载小组列表
const loadTeams = async () => {
  if (!currentAgencyId.value) {
    teams.value = []
    return
  }

  try {
    const url = `http://localhost:8000/api/v1/agencies/${currentAgencyId.value}/teams`
    const response = await fetch(url)
    const result = await response.json()
    
    // API直接返回数组，不是{data: [...]}格式
    let allTeams = Array.isArray(result) ? result : (result.data || [])
    
    // 如果是小组管理员，只显示自己所属的小组
    if (userTeamId.value && showTeamSelector.value === false) {
      allTeams = allTeams.filter((team: any) => team.id === userTeamId.value)
    }
    
    teams.value = allTeams
  } catch (error) {
    console.error('加载小组失败：', error)
    ElMessage.error('加载小组失败')
  }
}

// 加载表单中的小组列表
const loadFormTeams = async (agencyId: number) => {
  if (!agencyId) {
    formTeams.value = []
    return
  }

  try {
    const url = `http://localhost:8000/api/v1/agencies/${agencyId}/teams`
    const response = await fetch(url)
    const result = await response.json()
    
    // API直接返回数组，不是{data: [...]}格式
    formTeams.value = Array.isArray(result) ? result : (result.data || [])
  } catch (error) {
    console.error('加载小组失败：', error)
  }
}

// 搜索处理（计算属性会自动响应搜索关键词变化）
const handleSearch = () => {
  // 搜索功能由 filteredCollectors 计算属性自动处理
  // 这里可以添加额外的搜索逻辑，如日志记录
  console.log('搜索关键词：', searchKeyword.value)
}

// 加载催员列表（支持全选）
const loadCollectors = async () => {
  if (!currentTenantId.value) {
    collectors.value = []
    return
  }

  try {
    let allCollectors: any[] = []
    
    // 获取当前甲方名称
    const tenantName = tenantStore.currentTenant?.tenant_name || 
                       tenantStore.currentTenant?.name || 
                       tenantStore.currentTenant?.tenantName || ''
    
    if (!currentAgencyId.value && !currentTeamId.value) {
      // 全选：加载所有机构的催员
      await loadAgencies()
      
      for (const agency of agencies.value) {
        try {
          // 加载该机构的小组
          const teamsUrl = `http://localhost:8000/api/v1/agencies/${agency.id}/teams`
          const teamsResponse = await fetch(teamsUrl)
          const teamsResult = await teamsResponse.json()
          // API直接返回数组，不是{data: [...]}格式
          const agencyTeams = Array.isArray(teamsResult) ? teamsResult : (teamsResult.data || [])
          
          // 遍历该机构的所有小组，加载催员
          for (const team of agencyTeams) {
            try {
              const collectorsUrl = `http://localhost:8000/api/v1/teams/${team.id}/collectors`
              const collectorsResponse = await fetch(collectorsUrl)
              const collectorsResult = await collectorsResponse.json()
              // API直接返回数组，不是{data: [...]}格式
              const teamCollectors = Array.isArray(collectorsResult) ? collectorsResult : (collectorsResult.data || [])
              
              // 为每个催员添加甲方、机构和小组信息
              teamCollectors.forEach((collector: any) => {
                collector.tenant_name = tenantName
                collector.agency_name = agency.agency_name
                collector.team_name = team.team_name
              })
              
              allCollectors.push(...teamCollectors)
            } catch (error) {
              console.error(`加载小组 ${team.id} 的催员失败：`, error)
            }
          }
        } catch (error) {
          console.error(`加载机构 ${agency.id} 的小组失败：`, error)
        }
      }
    } else if (currentAgencyId.value && !currentTeamId.value) {
      // 只选择了机构：加载该机构所有小组的催员
      await loadTeams()
      
      for (const team of teams.value) {
        try {
          const collectorsUrl = `http://localhost:8000/api/v1/teams/${team.id}/collectors`
          const collectorsResponse = await fetch(collectorsUrl)
          const collectorsResult = await collectorsResponse.json()
          // API直接返回数组，不是{data: [...]}格式
          const teamCollectors = Array.isArray(collectorsResult) ? collectorsResult : (collectorsResult.data || [])
          
          // 为每个催员添加甲方、机构和小组信息
          const agency = agencies.value.find(a => a.id === currentAgencyId.value)
          teamCollectors.forEach((collector: any) => {
            collector.tenant_name = tenantName
            collector.agency_name = agency?.agency_name || ''
            collector.team_name = team.team_name
          })
          
          allCollectors.push(...teamCollectors)
        } catch (error) {
          console.error(`加载小组 ${team.id} 的催员失败：`, error)
        }
      }
    } else if (currentAgencyId.value && currentTeamId.value) {
      // 选择了机构和小组：加载该小组的催员
      const collectorsUrl = `http://localhost:8000/api/v1/teams/${currentTeamId.value}/collectors`
      const collectorsResponse = await fetch(collectorsUrl)
      const collectorsResult = await collectorsResponse.json()
      // API直接返回数组，不是{data: [...]}格式
      const teamCollectors = Array.isArray(collectorsResult) ? collectorsResult : (collectorsResult.data || [])
      
      // 为每个催员添加甲方、机构和小组信息
      const agency = agencies.value.find(a => a.id === currentAgencyId.value)
      const team = teams.value.find(t => t.id === currentTeamId.value)
      teamCollectors.forEach((collector: any) => {
        collector.tenant_name = tenantName
        collector.agency_name = agency?.agency_name || ''
        collector.team_name = team?.team_name || ''
      })
      
      allCollectors = teamCollectors
    }
    
    collectors.value = allCollectors
    console.log(`已加载 ${collectors.value.length} 个催员`)
  } catch (error) {
    console.error('加载催员失败：', error)
    ElMessage.error('加载催员失败')
  }
}

// 生成随机密码（包含数字、大小写字母，8位以内）
const generatePassword = () => {
  const lowercase = 'abcdefghijklmnopqrstuvwxyz'
  const uppercase = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'
  const numbers = '0123456789'
  const allChars = lowercase + uppercase + numbers
  
  // 确保至少包含一个大写字母、一个小写字母和一个数字
  let password = ''
  password += uppercase[Math.floor(Math.random() * uppercase.length)]
  password += lowercase[Math.floor(Math.random() * lowercase.length)]
  password += numbers[Math.floor(Math.random() * numbers.length)]
  
  // 随机生成剩余字符，总长度在6-8位之间
  const length = Math.floor(Math.random() * 3) + 6 // 6-8位
  for (let i = password.length; i < length; i++) {
    password += allChars[Math.floor(Math.random() * allChars.length)]
  }
  
  // 打乱字符顺序
  password = password.split('').sort(() => Math.random() - 0.5).join('')
  
  form.value.password = password
}

// 创建催员
const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '创建催员'
  form.value = {
    id: undefined,
    collector_code: '',
    collector_name: '',
    password: '',
    agency_id: undefined,
    team_id: undefined,
    role: 'collector',
    mobile: '',
    callback_number: '',
    email: '',
    remark: '',
    is_active: true
  }
  // 自动生成密码
  generatePassword()
  dialogVisible.value = true
}

// 编辑催员
const handleEdit = async (row: any) => {
  isEdit.value = true
  dialogTitle.value = '编辑催员'
  form.value = {
    id: row.id,
    collector_code: row.collector_code,
    collector_name: row.collector_name,
    password: '',
    agency_id: row.agency_id,
    team_id: row.team_id,
    role: row.role,
    mobile: row.mobile || '',
    callback_number: row.callback_number || '',
    email: row.email || '',
    remark: row.remark || '',
    is_active: row.is_active
  }
  await loadFormTeams(row.agency_id)
  dialogVisible.value = true
}

// 保存催员
const handleSave = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    saving.value = true

    console.log('保存催员：', form.value)
    
    // TODO: 调用API保存
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadCollectors()
  } catch (error) {
    console.error('保存失败：', error)
  } finally {
    saving.value = false
  }
}

// 修改密码
const handleResetPassword = (row: any) => {
  currentCollector.value = row
  passwordForm.value = {
    new_password: '',
    confirm_password: ''
  }
  passwordDialogVisible.value = true
}

// 保存密码
const handleSavePassword = async () => {
  if (!passwordFormRef.value) return
  
  try {
    await passwordFormRef.value.validate()
    
    savingPassword.value = true

    console.log('修改密码：', {
      collector_id: currentCollector.value.id,
      new_password: passwordForm.value.new_password
    })
    
    // TODO: 调用API保存密码
    ElMessage.success('密码修改成功')
    passwordDialogVisible.value = false
  } catch (error) {
    console.error('修改密码失败：', error)
  } finally {
    savingPassword.value = false
  }
}

// 导出催员账号与密码
const handleExportAccounts = () => {
  // TODO: 实现导出功能
  ElMessage.info('导出功能待实现')
}

// 查看IM端
const handleViewIm = (row: any) => {
  // 构建URL参数
  const params = new URLSearchParams({
    collectorId: row.collector_code || row.collector_id || '',
    tenantId: String(row.tenant_id || currentTenantId.value || ''),
    simulate: 'true' // 标记为模拟登录
  })
  
  // 打开新标签页
  const imLoginUrl = `${window.location.origin}/im/login?${params.toString()}`
  window.open(imLoginUrl, '_blank')
}

// 查看登录人脸记录
const handleViewLoginFaces = async (row: any) => {
  currentCollectorForFace.value = row
  faceRecordsDialogVisible.value = true
  faceRecords.value = []
  
  try {
    loadingFaceRecords.value = true
    
    // TODO: 调用实际API
    // const records = await getCollectorLoginFaceRecords(row.id)
    // faceRecords.value = records
    
    // Mock 数据
    await new Promise(resolve => setTimeout(resolve, 500))
    faceRecords.value = [
      {
        id: 1,
        collector_id: row.id,
        login_time: '2025-11-12T09:15:30Z',
        face_image: 'data:image/jpeg;base64,/9j/4AAQSkZJRg==',
        face_id: 'FACE_20251112_001',
        created_at: '2025-11-12T09:15:35Z'
      },
      {
        id: 2,
        collector_id: row.id,
        login_time: '2025-11-11T08:30:20Z',
        face_image: 'data:image/jpeg;base64,/9j/4AAQSkZJRg==',
        face_id: 'FACE_20251111_001',
        created_at: '2025-11-11T08:30:25Z'
      },
      {
        id: 3,
        collector_id: row.id,
        login_time: '2025-11-10T09:00:10Z',
        face_image: 'data:image/jpeg;base64,/9j/4AAQSkZJRg==',
        face_id: 'FACE_20251110_001',
        created_at: '2025-11-10T09:00:15Z'
      },
    ]
  } catch (error) {
    console.error('加载登录人脸记录失败：', error)
    ElMessage.error('加载登录人脸记录失败')
  } finally {
    loadingFaceRecords.value = false
  }
}

// 格式化日期（年-月-日）
const formatDate = (dateTime: string) => {
  if (!dateTime) return '--'
  const date = new Date(dateTime)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}

// 格式化时间（时:分:秒）
const formatTime = (dateTime: string) => {
  if (!dateTime) return '--'
  const date = new Date(dateTime)
  return date.toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

// 格式化日期时间
const formatDateTime = (dateTime: string) => {
  if (!dateTime) return '--'
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

// 处理图片加载错误
const handleImageError = (event: Event) => {
  const img = event.target as HTMLImageElement
  img.src = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCIgZmlsbD0iI2Y1ZjVmNSIvPjx0ZXh0IHg9IjUwJSIgeT0iNTAlIiBmb250LXNpemU9IjE0IiBmaWxsPSIjOTA5Mzk5IiB0ZXh0LWFuY2hvcj0ibWlkZGxlIiBkeT0iLjNlbSI+5Zu+54mH5pyq5Yqg6L29PC90ZXh0Pjwvc3ZnPg=='
}

// 启用/禁用催员
const handleToggleStatus = async (row: any) => {
  try {
    const action = row.is_active ? '禁用' : '启用'
    await ElMessageBox.confirm(
      `确定要${action}催员"${row.collector_name}"吗？${row.is_active ? '禁用后该催员将无法登录系统。' : ''}`,
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

/* 催员统计信息 */
.collector-stats {
  padding: 12px 16px;
  background-color: #f5f7fa;
  border-radius: 4px;
  margin-bottom: 16px;
}

/* 登录时间单元格样式 */
.login-time-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
  line-height: 1.4;
}

.login-time-cell .date-line {
  font-weight: 500;
  color: #303133;
}

.login-time-cell .time-line {
  font-size: 12px;
  color: #909399;
}

/* 登录人脸记录样式 */
.face-record-item {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

.face-image-container {
  flex-shrink: 0;
  width: 150px;
  height: 150px;
  border-radius: 8px;
  overflow: hidden;
  border: 2px solid #e4e7ed;
  background: #f5f7fa;
}

.face-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.face-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.info-row .label {
  font-weight: 500;
  color: #606266;
  min-width: 80px;
}

.info-row .value {
  color: #303133;
}

/* 时间轴样式优化 */
:deep(.el-timeline-item__timestamp) {
  font-size: 14px;
  color: #909399;
  font-weight: 500;
}

:deep(.el-timeline-item__wrapper) {
  padding-left: 20px;
}

:deep(.el-timeline-item__tail) {
  border-left: 2px solid #e4e7ed;
}

:deep(.el-timeline-item__node) {
  background-color: #409eff;
  border-color: #409eff;
}
</style>

