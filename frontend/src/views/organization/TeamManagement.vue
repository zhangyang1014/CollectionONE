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
        <el-table-column prop="team_group_name" label="所属小组群" width="150" />
        <el-table-column prop="queue_name" label="催收队列" width="150" />
        <el-table-column prop="password_rotate_days" label="密码自动更换" width="140">
          <template #default="{ row }">
            <span>{{ formatPasswordRotate(row.password_rotate_days) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="催收限制" min-width="260">
          <template #default="{ row }">
            <div class="limit-cell">
              <template v-if="hasRestriction(row)">
                <div v-if="normalizeToArray(row.allowed_systems).length">
                  所属系统：{{ formatArrayDisplay(row.allowed_systems) }}
                </div>
                <div v-if="normalizeToArray(row.allowed_term_days).length">
                  当期天数：{{ formatArrayDisplay(row.allowed_term_days, '天') }}
                </div>
                <div v-if="normalizeToArray(row.allowed_products).length">
                  产品：{{ formatArrayDisplay(row.allowed_products) }}
                </div>
                <div v-if="normalizeToArray(row.allowed_apps).length">
                  APP：{{ formatArrayDisplay(row.allowed_apps) }}
                </div>
                <div v-if="normalizeToArray(row.allowed_merchants).length">
                  商户：{{ formatArrayDisplay(row.allowed_merchants) }}
                </div>
              </template>
              <span v-else class="limit-empty">全部</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="collector_count" label="催员数" width="100" align="center">
          <template #default="{ row }">
            <el-button 
              link 
              type="primary" 
              @click="handleViewCollectors(row)"
              :disabled="!row.collector_count || row.collector_count === 0"
            >
              {{ row.collector_count || 0 }}
            </el-button>
          </template>
        </el-table-column>
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
            placeholder="请输入自定义部分（如：TM001）" 
            maxlength="50"
            :disabled="isEdit"
          >
            <template #prepend v-if="!isEdit && tenantPrefix">{{ tenantPrefix }}-</template>
          </el-input>
          <div v-if="!isEdit" style="margin-top: 5px; color: #909399; font-size: 12px;">
            完整编码：{{ tenantPrefix || '甲方编码' }}-{{ form.team_code || '自定义部分' }}
          </div>
          <div v-if="isEdit" style="margin-top: 5px; color: #909399; font-size: 12px;">
            小组编码不可修改
          </div>
        </el-form-item>

        <el-form-item label="小组名称" prop="team_name">
          <el-input v-model="form.team_name" placeholder="请输入小组名称" maxlength="100" />
        </el-form-item>

        <el-form-item label="所属机构" prop="agency_id">
          <el-select v-model="form.agency_id" placeholder="选择机构" style="width: 100%" @change="handleAgencySelectChange">
            <el-option
              v-for="agency in agencies"
              :key="agency.id"
              :label="agency.agency_name"
              :value="agency.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="所属小组群" prop="team_group_id">
          <el-select v-model="form.team_group_id" placeholder="请选择所属小组群（必选）" style="width: 100%">
            <el-option
              v-for="group in teamGroups"
              :key="group.id"
              :label="group.group_name"
              :value="group.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="催收队列" prop="queue_id">
          <el-select v-model="form.queue_id" placeholder="选择催收队列（必选）" style="width: 100%">
            <el-option
              v-for="queue in queues"
              :key="queue.id"
              :label="queue.queue_name"
              :value="queue.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="密码自动更换">
          <el-select
            v-model="form.password_rotate_days"
            placeholder="请选择"
            style="width: 100%"
          >
            <el-option
              v-for="opt in passwordRotateOptions"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>
          <div class="tip-text">到期后自动生成新密码；“永久”表示不自动更换</div>
        </el-form-item>

        <el-divider content-position="left">催收范围限制</el-divider>

        <el-form-item label="所属系统">
          <el-select
            v-model="form.limit_systems"
            multiple
            filterable
            allow-create
            default-first-option
            placeholder="留空表示不限制"
            style="width: 100%"
          >
            <el-option
              v-for="opt in systemOptions"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="当期天数">
          <el-select
            v-model="form.limit_term_days"
            multiple
            filterable
            allow-create
            default-first-option
            placeholder="留空表示不限制"
            style="width: 100%"
          >
            <el-option
              v-for="opt in termDayOptions"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="产品">
          <el-select
            v-model="form.limit_products"
            multiple
            filterable
            allow-create
            default-first-option
            placeholder="留空表示不限制"
            style="width: 100%"
          >
            <el-option
              v-for="opt in productOptions"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="APP">
          <el-select
            v-model="form.limit_apps"
            multiple
            filterable
            allow-create
            default-first-option
            placeholder="留空表示不限制"
            style="width: 100%"
          >
            <el-option
              v-for="opt in appOptions"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="商户">
          <el-select
            v-model="form.limit_merchants"
            multiple
            filterable
            allow-create
            default-first-option
            placeholder="留空表示不限制"
            style="width: 100%"
          >
            <el-option
              v-for="opt in merchantOptions"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
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
import { useRouter, useRoute } from 'vue-router'
import { useTenantStore } from '@/stores/tenant'
import request from '@/utils/request'

const router = useRouter()
const route = useRoute()
const tenantStore = useTenantStore()
const agencies = ref<any[]>([])
const teamGroups = ref<any[]>([]) // 小组群列表
const queues = ref<any[]>([]) // 催收队列列表
const currentTenantId = ref<number | undefined>(tenantStore.currentTenantId)
const currentTenant = computed(() => tenantStore.currentTenant)
const tenantPrefix = computed(() => currentTenant.value?.tenant_code || '')
const currentAgencyId = ref<number | undefined>(undefined) // 默认全选
const currentTeamId = ref<number | undefined>(undefined) // 默认全选
const allTeams = ref<any[]>([]) // 存储所有小组（用于筛选）
const teams = ref<any[]>([]) // 显示在表格中的小组
const dialogVisible = ref(false)
const dialogTitle = ref('')
const saving = ref(false)
const formRef = ref<FormInstance>()
const isEdit = ref(false)
const optionPreset = {
  system: [
    { label: 'iOS', value: 'ios' },
    { label: 'Android', value: 'android' },
    { label: 'Web', value: 'web' }
  ],
  termDays: ['1', '3', '7', '15', '30']
}
const defaultTermDayOptions = ['1', '3', '7', '15', '30']
const restrictionOptions = reactive({
  system: [] as { label: string; value: string }[],
  termDays: [] as { label: string; value: string }[],
  product: [] as { label: string; value: string }[],
  app: [] as { label: string; value: string }[],
  merchant: [] as { label: string; value: string }[]
})
const passwordRotateOptions = [
  { label: '永久', value: 0 },
  { label: '1天', value: 1 },
  { label: '2天', value: 2 },
  { label: '3天', value: 3 },
  { label: '4天', value: 4 },
  { label: '5天', value: 5 },
  { label: '6天', value: 6 },
  { label: '7天', value: 7 },
  { label: '8天', value: 8 },
  { label: '9天', value: 9 },
  { label: '10天', value: 10 },
  { label: '15天', value: 15 },
  { label: '30天', value: 30 }
]

// 将任意值安全转换为字符串数组，便于统一展示和提交
const normalizeToArray = (value: any): string[] => {
  if (!value) return []
  if (Array.isArray(value)) return value.map(v => String(v))
  return [String(value)]
}

// 去除空值并做简单清洗，避免提交脏数据
const cleanArray = (value: any): string[] => {
  return normalizeToArray(value)
    .map(item => item.trim())
    .filter(item => item.length > 0)
}

// 显示用的数组格式化，支持附加后缀（例如“天”）
const formatArrayDisplay = (value: any, suffix = ''): string => {
  const list = normalizeToArray(value)
  if (!list.length) return ''
  return list.map(item => `${item}${suffix ? suffix : ''}`).join('、')
}

// 格式化密码自动更换天数
const formatPasswordRotate = (days: any): string => {
  const num = Number(days)
  if (!days || num === 0) return '永久'
  return `${num}天`
}

// 判断行是否设置了任何限制
const hasRestriction = (row: any): boolean => {
  return [
    row?.allowed_systems,
    row?.allowed_term_days,
    row?.allowed_products,
    row?.allowed_apps,
    row?.allowed_merchants
  ].some(item => normalizeToArray(item).length > 0)
}

// 从“案件列表字段映射配置”读取下拉选项，保证与映射一致
const loadRestrictionOptionsFromMapping = async () => {
  restrictionOptions.system = []
  restrictionOptions.termDays = []
  restrictionOptions.product = []
  restrictionOptions.app = []
  restrictionOptions.merchant = []

  if (!currentTenantId.value) return

  try {
    const { getApiUrl } = await import('@/config/api')
    const url = getApiUrl(`tenants/${currentTenantId.value}/fields-json`)
    const resp = await request({
      url,
      method: 'get'
    })
    const fields = Array.isArray(resp?.fields) ? resp.fields : (Array.isArray(resp) ? resp : [])

    const pickOptions = (fieldKey: string, appendSuffix?: string) => {
      const field = fields.find((f: any) => f.field_key === fieldKey || f.fieldKey === fieldKey)
      if (!field) return []
      const enums = field.enum_values || field.enumValues || field.enum_options
      if (!Array.isArray(enums)) return []
      return enums
        .map((item: any) => {
          const value = item.value ?? item.standard_value ?? item.standardValue ?? item.label ?? item.standard_name
          if (!value && value !== 0) return null
          const label = item.label ?? item.standard_name ?? item.standardName ?? value
          return {
            label: appendSuffix ? `${label}${appendSuffix}` : String(label),
            value: String(value)
          }
        })
        .filter(Boolean) as { label: string; value: string }[]
    }

    restrictionOptions.system = pickOptions('system_name')
    restrictionOptions.termDays = pickOptions('term_days', '天')
    restrictionOptions.product = pickOptions('product_name')
    restrictionOptions.app = pickOptions('app_name')
    restrictionOptions.merchant = pickOptions('merchant_name')

    // 如果映射未提供枚举，回退到默认预设，确保仍可选择/输入
    if (restrictionOptions.system.length === 0) {
      restrictionOptions.system = optionPreset.system
    }
    if (restrictionOptions.termDays.length === 0) {
      restrictionOptions.termDays = optionPreset.termDays.map(v => ({ label: `${v}天`, value: v }))
    }
  } catch (error) {
    console.error('加载字段映射选项失败：', error)
    // 失败时仍保留预设，避免界面空白
    restrictionOptions.system = optionPreset.system
    restrictionOptions.termDays = optionPreset.termDays.map(v => ({ label: `${v}天`, value: v }))
  }
}

// 动态下拉选项，基于已有小组的限制值做提示，同时保留可自由输入
const systemOptions = computed(() => {
  if (restrictionOptions.system.length > 0) return restrictionOptions.system
  return optionPreset.system
})

const termDayOptions = computed(() => {
  if (restrictionOptions.termDays.length > 0) return restrictionOptions.termDays
  const set = new Set<string>(defaultTermDayOptions)
  allTeams.value.forEach(team => {
    normalizeToArray(team.allowed_term_days).forEach((day: string) => set.add(String(day)))
  })
  return Array.from(set).map(v => ({ label: `${v}天`, value: v }))
})

const productOptions = computed(() => {
  if (restrictionOptions.product.length > 0) return restrictionOptions.product
  const set = new Set<string>()
  allTeams.value.forEach(team => {
    normalizeToArray(team.allowed_products).forEach((item: string) => set.add(String(item)))
  })
  return Array.from(set).map(v => ({ label: v, value: v }))
})

const appOptions = computed(() => {
  if (restrictionOptions.app.length > 0) return restrictionOptions.app
  const set = new Set<string>()
  allTeams.value.forEach(team => {
    normalizeToArray(team.allowed_apps).forEach((item: string) => set.add(String(item)))
  })
  return Array.from(set).map(v => ({ label: v, value: v }))
})

const merchantOptions = computed(() => {
  if (restrictionOptions.merchant.length > 0) return restrictionOptions.merchant
  const set = new Set<string>()
  allTeams.value.forEach(team => {
    normalizeToArray(team.allowed_merchants).forEach((item: string) => set.add(String(item)))
  })
  return Array.from(set).map(v => ({ label: v, value: v }))
})

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
    queues.value = []
    
    if (newTenantId) {
      await loadRestrictionOptionsFromMapping()
      await loadAgencies()
      await loadQueues()
      await loadAllTeams() // 默认加载所有小组
      await loadTeams() // 应用筛选
    }
  }
)

// 初始加载
onMounted(async () => {
  if (currentTenantId.value) {
    await loadRestrictionOptionsFromMapping()
    await loadAgencies()
    await loadQueues()
    await loadAllTeams() // 默认加载所有小组
    
    // 从URL参数中获取agencyId并设置筛选
    const agencyIdParam = route.query.agencyId
    if (agencyIdParam) {
      currentAgencyId.value = Number(agencyIdParam)
    }
    
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
  team_group_id: undefined as number | undefined,
  queue_id: undefined as number | undefined,
  leader_id: undefined as number | undefined,
  target_performance: 0,
  password_rotate_days: 0,
  limit_systems: [] as string[],
  limit_term_days: [] as string[],
  limit_products: [] as string[],
  limit_apps: [] as string[],
  limit_merchants: [] as string[],
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
  team_group_id: [
    { required: true, message: '请选择所属小组群', trigger: 'change' }
  ],
  queue_id: [
    { required: true, message: '请选择催收队列', trigger: 'change' }
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
    const { getApiUrl } = await import('@/config/api')
    const url = getApiUrl(`tenants/${currentTenantId.value}/agencies`)
    const response = await fetch(url)
    const result = await response.json()
    
    // API直接返回数组，不是{data: [...]}格式
    agencies.value = Array.isArray(result) ? result : (result.data || [])
  } catch (error) {
    console.error('加载机构失败：', error)
    ElMessage.error('加载机构失败')
  }
}

// 加载小组群列表
const loadTeamGroups = async (agencyId?: number) => {
  if (!currentTenantId.value) {
    teamGroups.value = []
    return
  }

  try {
    const { getApiUrl } = await import('@/config/api')
    let url = `${getApiUrl('team-groups')}?tenant_id=${currentTenantId.value}`
    
    if (agencyId) {
      url += `&agency_id=${agencyId}`
    }
    
    const response = await fetch(url)
    const result = await response.json()
    
    teamGroups.value = Array.isArray(result) ? result : (result.data || [])
  } catch (error) {
    console.error('加载小组群失败：', error)
    ElMessage.error('加载小组群失败')
  }
}

// 加载队列列表
const loadQueues = async () => {
  if (!currentTenantId.value) {
    queues.value = []
    return
  }

  try {
    const { getApiUrl } = await import('@/config/api')
    const url = getApiUrl(`tenants/${currentTenantId.value}/queues`)
    const response = await fetch(url)
    const result = await response.json()
    
    queues.value = Array.isArray(result) ? result : (result.data || [])
  } catch (error) {
    console.error('加载队列失败：', error)
    ElMessage.error('加载队列失败')
  }
}

// 机构选择变化时加载对应的小组群
const handleAgencySelectChange = (agencyId: number) => {
  form.value.team_group_id = undefined // 清空小组群选择
  loadTeamGroups(agencyId)
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
        const { getApiUrl } = await import('@/config/api')
        const teamsUrl = getApiUrl(`agencies/${agency.id}/teams`)
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
    team_group_id: undefined,
    queue_id: undefined,
    leader_id: undefined,
    target_performance: 0,
    password_rotate_days: 0,
    limit_systems: [],
    limit_term_days: [],
    limit_products: [],
    limit_apps: [],
    limit_merchants: [],
    remark: '',
    is_active: true
  }
  teamGroups.value = [] // 清空小组群列表
  dialogVisible.value = true
}

// 查看催员
const handleViewCollectors = (row: any) => {
  router.push({
    path: '/organization/collectors',
    query: {
      agencyId: row.agency_id,
      teamId: row.id
    }
  })
}

// 编辑小组
const handleEdit = async (row: any) => {
  isEdit.value = true
  dialogTitle.value = '编辑小组'
  form.value = {
    id: row.id,
    team_code: row.team_code,
    team_name: row.team_name,
    agency_id: row.agency_id,
    team_group_id: row.team_group_id,
    queue_id: row.queue_id,
    leader_id: row.leader_id,
    target_performance: row.target_performance || 0,
    password_rotate_days: row.password_rotate_days ?? 0,
    limit_systems: normalizeToArray(row.allowed_systems || row.allowedSystems),
    limit_term_days: normalizeToArray(row.allowed_term_days || row.allowedTermDays),
    limit_products: normalizeToArray(row.allowed_products || row.allowedProducts),
    limit_apps: normalizeToArray(row.allowed_apps || row.allowedApps),
    limit_merchants: normalizeToArray(row.allowed_merchants || row.allowedMerchants),
    remark: row.remark || '',
    is_active: row.is_active
  }
  
  // 加载对应机构的小组群
  if (row.agency_id) {
    await loadTeamGroups(row.agency_id)
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
    
    // 构造请求数据
    const requestData = {
      agency_id: form.value.agency_id,
      team_group_id: form.value.team_group_id || null,
      queue_id: form.value.queue_id,
      team_code: !isEdit.value && tenantPrefix.value ? tenantPrefix.value + '-' + form.value.team_code : form.value.team_code,
      team_name: form.value.team_name,
      team_leader_id: form.value.leader_id || null,
      description: form.value.remark || null,
      max_case_count: form.value.target_performance || 0,
      password_rotate_days: Number(form.value.password_rotate_days || 0),
      sort_order: 0,
      is_active: form.value.is_active,
      allowed_systems: cleanArray(form.value.limit_systems),
      allowed_term_days: cleanArray(form.value.limit_term_days),
      allowed_products: cleanArray(form.value.limit_products),
      allowed_apps: cleanArray(form.value.limit_apps),
      allowed_merchants: cleanArray(form.value.limit_merchants)
    }
    
    if (isEdit.value && form.value.id) {
      // 更新小组
      const { getApiUrl } = await import('@/config/api')
      const url = getApiUrl(`teams/${form.value.id}`)
      const response = await fetch(url, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(requestData)
      })
      
      if (!response.ok) {
        const error = await response.json()
        throw new Error(error.detail || '更新失败')
      }
      
      ElMessage.success('更新成功')
    } else {
      // 创建小组
      const { getApiUrl } = await import('@/config/api')
      const url = getApiUrl('teams')
      const response = await fetch(url, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(requestData)
      })
      
      if (!response.ok) {
        const error = await response.json()
        throw new Error(error.detail || '创建失败')
      }
      
      ElMessage.success('创建成功')
    }
    
    dialogVisible.value = false
    await loadAllTeams() // 重新加载所有小组
    await loadTeams() // 应用筛选
  } catch (error: any) {
    console.error('保存失败：', error)
    ElMessage.error(error.message || '保存失败')
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

.limit-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
  line-height: 1.4;
}

.limit-empty {
  color: #909399;
}
</style>

