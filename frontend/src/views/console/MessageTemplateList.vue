<template>
  <div class="message-template-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>消息模板配置管理</h2>
      <el-button type="primary" @click="handleCreate">
        <el-icon><Plus /></el-icon>
        新增模板
      </el-button>
    </div>

    <!-- 筛选器 -->
    <el-card class="filter-card" shadow="never">
      <el-form :model="filters" inline>
        <el-form-item label="案件阶段">
          <el-select v-model="filters.caseStage" placeholder="全部阶段" clearable style="width: 140px;">
            <el-option label="全部阶段" value="" />
            <el-option label="C（催收前）" value="C" />
            <el-option label="S0（首次联系）" value="S0" />
            <el-option label="S1-3（初期）" value="S1-3" />
            <el-option label="S3+（后期）" value="S3+" />
          </el-select>
        </el-form-item>

        <el-form-item label="模板类型">
          <el-select v-model="filters.templateType" placeholder="全部" clearable style="width: 120px;">
            <el-option label="全部" value="" />
            <el-option label="组织模板" value="organization" />
          </el-select>
        </el-form-item>

        <el-form-item label="渠道">
          <el-select v-model="filters.channelType" placeholder="全部渠道" clearable style="width: 120px;">
            <el-option label="全部渠道" value="" />
            <el-option label="短信" value="sms" />
            <el-option label="RCS" value="rcs" />
            <el-option label="WABA" value="waba" />
            <el-option label="WhatsApp" value="whatsapp" />
            <el-option label="邮件" value="email" />
            <el-option label="手机日历" value="mobile_calendar" />
          </el-select>
        </el-form-item>

        <el-form-item label="场景">
          <el-select v-model="filters.scene" placeholder="全部场景" clearable style="width: 120px;">
            <el-option label="全部场景" value="" />
            <el-option label="问候" value="greeting" />
            <el-option label="提醒" value="reminder" />
            <el-option label="强度" value="strong" />
          </el-select>
        </el-form-item>

        <el-form-item label="时间点">
          <el-select v-model="filters.timeSlot" placeholder="全部时间" clearable style="width: 120px;">
            <el-option label="全部时间" value="" />
            <el-option label="上午" value="morning" />
            <el-option label="下午" value="afternoon" />
            <el-option label="晚上" value="evening" />
          </el-select>
        </el-form-item>

        <el-form-item label="启用状态">
          <el-select v-model="filters.isEnabled" placeholder="全部" clearable style="width: 120px;">
            <el-option label="全部" :value="null" />
            <el-option label="已启用" :value="true" />
            <el-option label="已禁用" :value="false" />
          </el-select>
        </el-form-item>

        <el-form-item label="搜索">
          <el-input 
            v-model="filters.keyword" 
            placeholder="模板名称/内容" 
            clearable 
            style="width: 200px;"
            @keyup.enter="loadTemplates"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 列表表格 -->
    <el-card class="table-card" shadow="never">
      <el-table 
        :data="templateList" 
        v-loading="loading"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="templateName" label="模板名称" width="200" />
        
        <el-table-column prop="templateType" label="模板类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.templateType === 'organization' ? 'success' : 'warning'" size="small">
              {{ row.templateType === 'organization' ? '组织模板' : '个人模板' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="channelType" label="渠道" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.channelType" size="small" type="info">{{ getChannelLabel(row.channelType) }}</el-tag>
            <span v-else style="color: #909399;">未配置</span>
          </template>
        </el-table-column>

        <el-table-column label="供应商配置" width="120">
          <template #default="{ row }">
            <el-tooltip 
              v-if="row.supplierTemplateMappings && row.supplierTemplateMappings.length > 0" 
              :content="getSupplierMappingsTooltip(row.supplierTemplateMappings)" 
              placement="top"
            >
              <span style="cursor: pointer;">
                {{ row.supplierTemplateMappings.length }}个供应商
              </span>
            </el-tooltip>
            <span v-else style="color: #909399;">未配置</span>
          </template>
        </el-table-column>

        <el-table-column prop="teamNames" label="适用小组" width="150">
          <template #default="{ row }">
            <el-tooltip v-if="row.teamIds && row.teamIds.length > 0" :content="getTeamTooltip(row.teamIds)" placement="top">
              <span>{{ row.teamNames || '部分小组' }}</span>
            </el-tooltip>
            <span v-else>{{ row.teamNames || '全部小组' }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="caseStage" label="案件阶段" width="100">
          <template #default="{ row }">
            <el-tag size="small" type="info">{{ getCaseStageLabel(row.caseStage) }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="scene" label="场景" width="80">
          <template #default="{ row }">
            {{ getSceneLabel(row.scene) }}
          </template>
        </el-table-column>

        <el-table-column prop="timeSlot" label="时间点" width="80">
          <template #default="{ row }">
            {{ getTimeSlotLabel(row.timeSlot) }}
          </template>
        </el-table-column>

        <el-table-column prop="content" label="模板内容预览" width="300">
          <template #default="{ row }">
            <el-tooltip :content="row.content" placement="top">
              <span class="content-preview">{{ row.content.substring(0, 50) }}{{ row.content.length > 50 ? '...' : '' }}</span>
            </el-tooltip>
          </template>
        </el-table-column>

        <el-table-column prop="usageCount" label="使用次数" width="80" align="center" />

        <el-table-column prop="isEnabled" label="启用状态" width="80" align="center">
          <template #default="{ row }">
            <el-switch 
              v-model="row.isEnabled" 
              @change="handleToggle(row)"
              :loading="row.toggleLoading"
            />
          </template>
        </el-table-column>

        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadTemplates"
          @current-change="loadTemplates"
        />
      </div>
    </el-card>

    <!-- 模板表单抽屉 -->
    <el-drawer
      v-model="drawerVisible"
      :title="isEdit ? '编辑模板' : '新增模板'"
      size="600px"
      :before-close="handleDrawerClose"
    >
      <el-form 
        ref="templateFormRef"
        :model="templateForm" 
        :rules="formRules"
        label-width="100px"
      >
        <!-- 基础信息 -->
        <div class="form-section">
          <div class="section-title">基础信息</div>
          
          <el-form-item label="模板名称" prop="templateName">
            <el-input 
              v-model="templateForm.templateName" 
              placeholder="请输入模板名称"
              maxlength="100"
              show-word-limit
            />
          </el-form-item>

          <el-form-item label="模板类型" prop="templateType">
            <el-radio-group v-model="templateForm.templateType">
              <el-radio label="organization">组织模板</el-radio>
              <el-radio 
                v-if="templateForm.templateType === 'personal'"
                label="personal"
                disabled
              >
                个人模板（暂不支持新建）
              </el-radio>
            </el-radio-group>
            <div class="form-tip">
              组织模板：选定机构的催员可见；个人模板暂不支持新建
            </div>
          </el-form-item>

          <el-form-item 
            label="适用小组" 
            prop="teamIds"
            v-if="templateForm.templateType === 'organization'"
          >
            <div class="team-select-row">
              <el-select
                v-model="selectedAgencyId"
                placeholder="按机构筛选小组"
                clearable
                style="width: 220px"
                @change="handleAgencyFilterChange"
              >
                <el-option :value="null" label="全部机构" />
                <el-option 
                  v-for="agency in agencyOptions" 
                  :key="agency.id" 
                  :label="agency.agency_name || agency.agencyName || agency.name"
                  :value="agency.id" 
                />
              </el-select>
              <el-select 
                v-model="templateForm.teamIds" 
                multiple 
                filterable
                collapse-tags
                collapse-tags-tooltip
                :loading="teamLoading"
                placeholder="请选择适用小组（含案件队列）"
                style="flex: 1"
              >
                <el-option 
                  v-for="team in filteredTeams" 
                  :key="team.id" 
                  :label="formatTeamLabel(team)" 
                  :value="team.id" 
                />
              </el-select>
            </div>
            <div class="form-actions">
              <el-button link type="primary" @click="selectAllTeams">全选</el-button>
              <el-button link @click="clearTeams">清空</el-button>
            </div>
            <div class="form-tip">选择小组即可精确到案件队列；机构仅用于筛选</div>
          </el-form-item>
        </div>

        <!-- 渠道配置 -->
        <div class="form-section">
          <div class="section-title">渠道配置</div>
          
          <el-form-item label="渠道类型" prop="channelType">
            <el-select 
              v-model="templateForm.channelType" 
              placeholder="请选择渠道类型"
              @change="handleChannelChange"
              style="width: 100%"
            >
              <el-option label="短信(SMS)" value="sms" />
              <el-option label="RCS" value="rcs" />
              <el-option label="WABA" value="waba" />
              <el-option label="WhatsApp" value="whatsapp" />
              <el-option label="邮件" value="email" />
              <el-option label="手机日历" value="mobile_calendar" />
            </el-select>
          </el-form-item>

          <el-form-item label="供应商配置" prop="supplierTemplateMappings" v-if="templateForm.channelType">
            <div class="supplier-mappings">
              <div 
                v-for="(mapping, index) in templateForm.supplierTemplateMappings" 
                :key="index"
                class="supplier-mapping-item"
              >
                <el-select 
                  v-model="mapping.supplierId" 
                  placeholder="选择供应商"
                  @change="handleSupplierChange(index)"
                  style="width: 200px"
                >
                  <el-option 
                    v-for="supplier in filteredSuppliers" 
                    :key="supplier.id" 
                    :label="supplier.supplier_name" 
                    :value="supplier.id" 
                  />
                </el-select>
                
                <el-input 
                  v-model="mapping.templateId" 
                  placeholder="供应商侧模板ID"
                  style="width: 250px; margin-left: 10px"
                />
                
                <el-input-number 
                  v-model="mapping.priority" 
                  :min="1"
                  :max="99"
                  placeholder="优先级"
                  style="width: 120px; margin-left: 10px"
                />
                <span style="margin-left: 5px; color: #909399; font-size: 12px;">优先级</span>
                
                <el-button 
                  type="danger" 
                  link 
                  @click="removeSupplierMapping(index)"
                  style="margin-left: 10px"
                >
                  删除
                </el-button>
              </div>
              
              <el-button 
                type="primary" 
                link 
                @click="addSupplierMapping"
                style="margin-top: 10px"
              >
                + 添加供应商配置
              </el-button>
            </div>
            <div class="form-tip">
              优先级数值越小越优先，发送时优先使用优先级高的供应商，失败后自动切换到次优先级供应商
            </div>
          </el-form-item>
        </div>

        <!-- 分类维度 -->
        <div class="form-section">
          <div class="section-title">分类维度</div>

          <el-form-item label="案件阶段" prop="caseStage">
            <el-select v-model="templateForm.caseStage" placeholder="请选择案件阶段" style="width: 100%">
              <el-option label="C（催收前）" value="C" />
              <el-option label="S0（首次联系）" value="S0" />
              <el-option label="S1-3（初期）" value="S1-3" />
              <el-option label="S3+（后期）" value="S3+" />
            </el-select>
          </el-form-item>

          <el-form-item label="场景" prop="scene">
            <el-select v-model="templateForm.scene" placeholder="请选择场景" style="width: 100%">
              <el-option label="问候" value="greeting" />
              <el-option label="提醒" value="reminder" />
              <el-option label="强度" value="strong" />
            </el-select>
          </el-form-item>

          <el-form-item label="时间点" prop="timeSlot">
            <el-select v-model="templateForm.timeSlot" placeholder="请选择时间点" style="width: 100%">
              <el-option label="上午（8:00-12:00）" value="morning" />
              <el-option label="下午（12:00-18:00）" value="afternoon" />
              <el-option label="晚上（18:00-22:00）" value="evening" />
            </el-select>
          </el-form-item>
        </div>

        <!-- 模板内容 -->
        <div class="form-section">
          <div class="section-title">模板内容</div>

          <el-form-item label="可用变量">
            <div class="variable-tags">
              <el-tag 
                v-for="variable in availableVariables" 
                :key="variable.key"
                class="variable-tag"
                @click="insertVariable(variable.key)"
                style="cursor: pointer; margin: 4px;"
              >
                {{ variable.key }}
              </el-tag>
            </div>
            <div class="form-tip">点击变量标签插入到模板内容</div>
          </el-form-item>

          <el-form-item label="模板内容" prop="content">
            <el-input 
              ref="contentInputRef"
              v-model="templateForm.content" 
              type="textarea"
              :rows="6"
              placeholder="请输入模板内容，可使用变量如：{客户名}、{贷款编号}、{逾期天数}、{应还金额}、{产品名称} 等"
              maxlength="1000"
              show-word-limit
            />
          </el-form-item>

          <el-form-item label="内容预览">
            <div class="content-preview-box">
              {{ renderPreview(templateForm.content) }}
            </div>
          </el-form-item>
        </div>

        <!-- 状态配置 -->
        <div class="form-section">
          <div class="section-title">状态配置</div>

          <el-form-item label="启用状态">
            <el-switch v-model="templateForm.isEnabled" />
            <span style="margin-left: 10px; color: #909399;">
              {{ templateForm.isEnabled ? '启用' : '禁用' }}
            </span>
          </el-form-item>

          <el-form-item label="排序权重" prop="sortOrder">
            <el-input-number 
              v-model="templateForm.sortOrder" 
              :min="0" 
              :max="9999"
              placeholder="数值越小越靠前"
            />
            <div class="form-tip">数值越小，模板在列表中排序越靠前</div>
          </el-form-item>
        </div>
      </el-form>

      <template #footer>
        <div class="drawer-footer">
          <el-button @click="handleDrawerClose">取消</el-button>
          <el-button type="primary" @click="handleSave" :loading="saveLoading">保存</el-button>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick, computed } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { getAgencies, getAgencyTeams } from '@/api/organization'
import type { CollectionAgency, CollectionTeam } from '@/types/organization'
import { getChannelSuppliers } from '@/api/channel'
import type { ChannelSupplier, ChannelType } from '@/types/channel'

// ==================== 数据定义 ====================

interface MessageTemplate {
  id?: number
  tenantId: number
  templateName: string
  templateType: string
  teamIds: number[] | null
  teamNames?: string
  agencyIds?: number[] | null
  agencyNames?: string
  caseStage: string
  scene: string
  timeSlot: string
  channelType?: string
  supplierTemplateMappings?: SupplierTemplateMapping[]
  content: string
  variables: string[]
  isEnabled: boolean
  sortOrder: number
  usageCount?: number
  createdAt?: string
  updatedAt?: string
  createdBy?: number
  createdByName?: string
  toggleLoading?: boolean
}

interface SupplierTemplateMapping {
  supplierId: number | null
  supplierName?: string
  templateId: string
  priority: number
}

interface Variable {
  name: string
  key: string
  type: string
  example: string
  description: string
}

interface TeamOption extends CollectionTeam {
  displayName: string
}

// 列表数据
const templateList = ref<MessageTemplate[]>([])
const loading = ref(false)

// 筛选条件
const filters = reactive({
  caseStage: '',
  templateType: '',
  scene: '',
  timeSlot: '',
  channelType: '',
  isEnabled: null as boolean | null,
  keyword: ''
})

// 分页
const pagination = reactive({
  page: 1,
  pageSize: 20,
  total: 0
})

// 表单
const drawerVisible = ref(false)
const isEdit = ref(false)
const templateFormRef = ref<FormInstance>()
const contentInputRef = ref()
const saveLoading = ref(false)

const templateForm = reactive<MessageTemplate>({
  tenantId: 1, // 从登录信息获取
  templateName: '',
  templateType: 'organization',
  teamIds: null,
  caseStage: '',
  scene: '',
  timeSlot: '',
  channelType: '',
  supplierTemplateMappings: [],
  content: '',
  variables: [],
  isEnabled: true,
  sortOrder: 0
})

// 可用变量列表
const availableVariables = ref<Variable[]>([])

// 机构与小组数据
const agencyOptions = ref<CollectionAgency[]>([])
const teamOptions = ref<TeamOption[]>([])
const teamLoading = ref(false)
const selectedAgencyId = ref<number | null>(null)
const filteredTeams = computed(() => {
  if (!selectedAgencyId.value) return teamOptions.value
  return teamOptions.value.filter(team => team.agency_id === selectedAgencyId.value)
})

// 渠道供应商数据
const channelSuppliers = ref<ChannelSupplier[]>([])
const filteredSuppliers = computed(() => {
  if (!templateForm.channelType) return []
  return channelSuppliers.value.filter(s => 
    s.channel_type === templateForm.channelType && s.is_active
  )
})

// 表单校验规则
const formRules: FormRules = {
  templateName: [
    { required: true, message: '请输入模板名称', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  templateType: [
    { required: true, message: '请选择模板类型', trigger: 'change' }
  ],
  teamIds: [
    { 
      required: true, 
      message: '请选择适用小组', 
      trigger: 'change',
      validator: (rule, value, callback) => {
        if (templateForm.templateType === 'organization' && (!value || value.length === 0)) {
          callback(new Error('请至少选择一个小组'))
        } else {
          callback()
        }
      }
    }
  ],
  caseStage: [
    { required: true, message: '请选择案件阶段', trigger: 'change' }
  ],
  scene: [
    { required: true, message: '请选择场景', trigger: 'change' }
  ],
  timeSlot: [
    { required: true, message: '请选择时间点', trigger: 'change' }
  ],
  channelType: [
    { required: true, message: '请选择渠道类型', trigger: 'change' }
  ],
  supplierTemplateMappings: [
    { 
      type: 'array',
      required: true, 
      message: '请至少配置一个供应商', 
      trigger: 'change',
      validator: (rule, value, callback) => {
        if (!value || value.length === 0) {
          callback(new Error('请至少配置一个供应商'))
        } else {
          // 检查每个配置是否完整
          for (let i = 0; i < value.length; i++) {
            const mapping = value[i]
            if (!mapping.supplierId) {
              callback(new Error(`第${i + 1}个供应商未选择`))
              return
            }
            if (!mapping.templateId || mapping.templateId.trim() === '') {
              callback(new Error(`第${i + 1}个供应商模板ID不能为空`))
              return
            }
            if (!mapping.priority || mapping.priority < 1) {
              callback(new Error(`第${i + 1}个供应商优先级必须大于0`))
              return
            }
          }
          // 检查优先级是否重复
          const priorities = value.map(m => m.priority)
          const uniquePriorities = [...new Set(priorities)]
          if (priorities.length !== uniquePriorities.length) {
            callback(new Error('供应商优先级不能重复'))
            return
          }
          callback()
        }
      }
    }
  ],
  content: [
    { required: true, message: '请输入模板内容', trigger: 'blur' },
    { max: 1000, message: '内容不能超过1000个字符', trigger: 'blur' }
  ],
  sortOrder: [
    { type: 'number', message: '排序权重必须是数字', trigger: 'blur' }
  ]
}

// ==================== 生命周期 ====================

onMounted(async () => {
  await loadOrganizations()
  await loadTemplates()
  await loadVariables()
})

// ==================== 方法 ====================

/**
 * 加载机构与小组（用于小组选择）
 */
const loadOrganizations = async () => {
  teamLoading.value = true
  try {
    const agenciesResp = await getAgencies({ tenant_id: templateForm.tenantId })
    agencyOptions.value = agenciesResp.data || agenciesResp || []

    const teamPromises = agencyOptions.value.map(async (agency) => {
      const resp = await getAgencyTeams(agency.id)
      const teams = resp.data || resp || []
      return teams.map((team: CollectionTeam) => ({
        ...team,
        displayName: `${team.team_name}${team.queue_name ? `（${team.queue_name}）` : ''}`
      }))
    })

    const teamResults = await Promise.allSettled(teamPromises)
    const allTeams: TeamOption[] = []
    teamResults.forEach(result => {
      if (result.status === 'fulfilled') {
        allTeams.push(...result.value)
      }
    })
    teamOptions.value = allTeams
  } catch (error) {
    console.error('加载机构/小组失败:', error)
    ElMessage.error('加载小组列表失败')
  } finally {
    teamLoading.value = false
  }
}

/**
 * 加载模板列表
 */
const loadTemplates = async () => {
  loading.value = true
  try {
    const params: any = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      tenantId: 1 // 从登录信息获取
    }

    if (filters.caseStage) params.caseStage = filters.caseStage
    if (filters.templateType) params.templateType = filters.templateType
    if (filters.channelType) params.channelType = filters.channelType
    if (filters.scene) params.scene = filters.scene
    if (filters.timeSlot) params.timeSlot = filters.timeSlot
    if (filters.isEnabled !== null) params.isEnabled = filters.isEnabled
    if (filters.keyword) params.keyword = filters.keyword

    const response = await request({
      url: '/api/v1/console/message-templates',
      method: 'get',
      params
    })

    const result = response.data || response
    templateList.value = result.list || []
    pagination.total = result.total || 0
  } catch (error) {
    console.error('加载模板列表失败:', error)
    ElMessage.error('加载模板列表失败')
  } finally {
    loading.value = false
  }
}

/**
 * 加载可用变量
 */
const loadVariables = async () => {
  try {
    const response = await request({
      url: '/api/v1/message-templates/variables',
      method: 'get'
    })

    availableVariables.value = response.data || response || []
  } catch (error) {
    console.error('加载变量列表失败:', error)
  }
}

/**
 * 查询
 */
const handleSearch = () => {
  pagination.page = 1
  loadTemplates()
}

/**
 * 重置筛选条件
 */
const handleReset = () => {
  filters.caseStage = ''
  filters.templateType = ''
  filters.channelType = ''
  filters.scene = ''
  filters.timeSlot = ''
  filters.isEnabled = null
  filters.keyword = ''
  pagination.page = 1
  loadTemplates()
}

/**
 * 新增模板
 */
const handleCreate = () => {
  isEdit.value = false
  resetForm()
  drawerVisible.value = true
}

/**
 * 编辑模板
 */
const handleEdit = (row: MessageTemplate) => {
  isEdit.value = true
  Object.assign(templateForm, {
    id: row.id,
    tenantId: row.tenantId,
    templateName: row.templateName,
    templateType: row.templateType,
    teamIds: row.teamIds ? [...row.teamIds] : null,
    caseStage: row.caseStage,
    scene: row.scene,
    timeSlot: row.timeSlot,
    channelType: row.channelType,
    supplierTemplateMappings: row.supplierTemplateMappings ? JSON.parse(JSON.stringify(row.supplierTemplateMappings)) : [],
    content: row.content,
    variables: row.variables ? [...row.variables] : [],
    isEnabled: row.isEnabled,
    sortOrder: row.sortOrder
  })
  
  // 如果有渠道类型，加载对应的供应商列表
  if (row.channelType) {
    loadChannelSuppliers(row.channelType as ChannelType)
  }
  
  drawerVisible.value = true
}

/**
 * 删除模板
 */
const handleDelete = (row: MessageTemplate) => {
  ElMessageBox.confirm(
    `确定删除模板【${row.templateName}】吗？删除后催员将无法继续使用此模板。`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await request({
        url: `/api/v1/console/message-templates/${row.id}`,
        method: 'delete',
        params: { tenantId: row.tenantId }
      })

      ElMessage.success('删除成功')
      loadTemplates()
    } catch (error) {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }).catch(() => {
    // 取消删除
  })
}

/**
 * 切换启用状态
 */
const handleToggle = async (row: MessageTemplate) => {
  row.toggleLoading = true
  try {
    await request({
      url: `/api/v1/console/message-templates/${row.id}/toggle`,
      method: 'patch',
      data: {
        tenantId: row.tenantId,
        isEnabled: row.isEnabled
      }
    })

    ElMessage.success(row.isEnabled ? '已启用' : '已禁用')
  } catch (error) {
    console.error('切换状态失败:', error)
    row.isEnabled = !row.isEnabled // 回滚状态
    ElMessage.error('切换状态失败')
  } finally {
    row.toggleLoading = false
  }
}

/**
 * 保存模板
 */
const handleSave = async () => {
  if (!templateFormRef.value) return

  await templateFormRef.value.validate(async (valid) => {
    if (!valid) {
      ElMessage.warning('请完善表单信息')
      return
    }

    saveLoading.value = true
    try {
      // 提取模板中使用的变量
      const usedVariables = extractVariables(templateForm.content)
      templateForm.variables = usedVariables

      const url = isEdit.value 
        ? `/api/v1/console/message-templates/${templateForm.id}`
        : '/api/v1/console/message-templates'
      
      const method = isEdit.value ? 'put' : 'post'

      const requestData: any = {
        tenantId: templateForm.tenantId,
        templateName: templateForm.templateName,
        templateType: templateForm.templateType,
        teamIds: templateForm.templateType === 'organization' ? templateForm.teamIds : null,
        agencyIds: templateForm.templateType === 'organization' ? deriveAgencyIdsFromTeams(templateForm.teamIds) : null,
        caseStage: templateForm.caseStage,
        scene: templateForm.scene,
        timeSlot: templateForm.timeSlot,
        channelType: templateForm.channelType,
        supplierTemplateMappings: templateForm.supplierTemplateMappings,
        content: templateForm.content,
        variables: templateForm.variables,
        isEnabled: templateForm.isEnabled,
        sortOrder: templateForm.sortOrder,
        createdBy: 1, // 从登录信息获取
        updatedBy: 1
      }

      await request({
        url,
        method,
        data: requestData
      })

      ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
      drawerVisible.value = false
      loadTemplates()
    } catch (error: any) {
      console.error('保存失败:', error)
      const errorMsg = error.response?.data?.message || error.message || '保存失败'
      ElMessage.error(errorMsg)
    } finally {
      saveLoading.value = false
    }
  })
}

/**
 * 关闭抽屉
 */
const handleDrawerClose = (done?: () => void) => {
  ElMessageBox.confirm('当前有未保存的修改，确定要离开吗？')
    .then(() => {
      resetForm()
      if (done) done()
      else drawerVisible.value = false
    })
    .catch(() => {
      // 取消关闭
    })
}

/**
 * 重置表单
 */
const resetForm = () => {
  Object.assign(templateForm, {
    id: undefined,
    tenantId: 1,
    templateName: '',
    templateType: 'organization',
    teamIds: null,
    caseStage: '',
    scene: '',
    timeSlot: '',
    channelType: '',
    supplierTemplateMappings: [],
    content: '',
    variables: [],
    isEnabled: true,
    sortOrder: 0
  })
  selectedAgencyId.value = null
  channelSuppliers.value = []
  templateFormRef.value?.resetFields()
}

/**
 * 全选小组
 */
const selectAllTeams = () => {
  templateForm.teamIds = filteredTeams.value.map(team => team.id)
}

/**
 * 清空小组
 */
const clearTeams = () => {
  templateForm.teamIds = []
}

/**
 * 根据小组反推所属机构（用于兼容老的机构字段）
 */
const deriveAgencyIdsFromTeams = (teamIds?: number[] | null): number[] | null => {
  if (!teamIds || teamIds.length === 0) return null
  const ids = teamIds
    .map(id => teamOptions.value.find(team => team.id === id)?.agency_id)
    .filter((id): id is number => typeof id === 'number')
  const unique = Array.from(new Set(ids))
  return unique.length ? unique : null
}

/**
 * 插入变量到光标位置
 */
const insertVariable = (variableKey: string) => {
  const textarea = contentInputRef.value?.$el?.querySelector('textarea')
  if (!textarea) {
    templateForm.content += variableKey
    return
  }

  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const text = templateForm.content

  templateForm.content = text.substring(0, start) + variableKey + text.substring(end)

  nextTick(() => {
    textarea.focus()
    const newPos = start + variableKey.length
    textarea.setSelectionRange(newPos, newPos)
  })
}

/**
 * 渲染内容预览
 */
const renderPreview = (content: string) => {
  if (!content) return ''

  const exampleData: { [key: string]: string } = {
    '{客户名}': '张三',
    '{贷款编号}': 'BTSK-200100',
    '{逾期天数}': '23',
    '{到期日期}': '2025-01-15',
    '{贷款金额}': '50,000',
    '{应还金额}': '10,529',
    '{本金}': '50,000',
    '{罚息}': '529',
    '{产品名称}': '快速贷',
    '{App名称}': 'MegaPeso'
  }

  let preview = content
  Object.keys(exampleData).forEach(key => {
    preview = preview.replace(new RegExp(key.replace(/[{}]/g, '\\$&'), 'g'), exampleData[key])
  })

  return preview
}

/**
 * 从内容中提取使用的变量
 */
const extractVariables = (content: string): string[] => {
  const regex = /\{([^}]+)\}/g
  const variables: string[] = []
  let match

  while ((match = regex.exec(content)) !== null) {
    if (!variables.includes(match[1])) {
      variables.push(match[1])
    }
  }

  return variables
}

/**
 * 加载渠道供应商
 */
const loadChannelSuppliers = async (channelType: ChannelType) => {
  try {
    const response = await getChannelSuppliers(1, channelType) // tenantId从登录信息获取
    channelSuppliers.value = response.data || response || []
  } catch (error) {
    console.error('加载供应商失败:', error)
    ElMessage.error('加载供应商列表失败')
  }
}

/**
 * 渠道变更时加载供应商
 */
const handleChannelChange = (channelType: string) => {
  templateForm.supplierTemplateMappings = []
  if (channelType) {
    loadChannelSuppliers(channelType as ChannelType)
  }
}

/**
 * 添加供应商配置
 */
const addSupplierMapping = () => {
  if (!templateForm.supplierTemplateMappings) {
    templateForm.supplierTemplateMappings = []
  }
  templateForm.supplierTemplateMappings.push({
    supplierId: null,
    templateId: '',
    priority: templateForm.supplierTemplateMappings.length + 1
  })
}

/**
 * 删除供应商配置
 */
const removeSupplierMapping = (index: number) => {
  templateForm.supplierTemplateMappings?.splice(index, 1)
}

/**
 * 供应商选择变更时更新名称
 */
const handleSupplierChange = (index: number) => {
  const mapping = templateForm.supplierTemplateMappings?.[index]
  if (mapping) {
    const supplier = channelSuppliers.value.find(s => s.id === mapping.supplierId)
    if (supplier) {
      mapping.supplierName = supplier.supplier_name
    }
  }
}

/**
 * 获取渠道标签
 */
const getChannelLabel = (channelType: string): string => {
  const map: { [key: string]: string } = {
    'sms': '短信',
    'rcs': 'RCS',
    'waba': 'WABA',
    'whatsapp': 'WhatsApp',
    'email': '邮件',
    'mobile_calendar': '手机日历'
  }
  return map[channelType] || channelType
}

/**
 * 获取供应商配置提示信息
 */
const getSupplierMappingsTooltip = (mappings: SupplierTemplateMapping[]): string => {
  if (!mappings || mappings.length === 0) return ''
  return mappings
    .sort((a, b) => a.priority - b.priority)
    .map((m, i) => `${i + 1}. ${m.supplierName || '供应商' + m.supplierId} (ID: ${m.templateId})`)
    .join('\n')
}

/**
 * 获取案件阶段标签
 */
const getCaseStageLabel = (stage: string): string => {
  const map: { [key: string]: string } = {
    'C': 'C（催收前）',
    'S0': 'S0（首次联系）',
    'S1-3': 'S1-3（初期）',
    'S3+': 'S3+（后期）'
  }
  return map[stage] || stage
}

/**
 * 获取场景标签
 */
const getSceneLabel = (scene: string): string => {
  const map: { [key: string]: string } = {
    'greeting': '问候',
    'reminder': '提醒',
    'strong': '强度'
  }
  return map[scene] || scene
}

/**
 * 获取时间点标签
 */
const getTimeSlotLabel = (timeSlot: string): string => {
  const map: { [key: string]: string } = {
    'morning': '上午',
    'afternoon': '下午',
    'evening': '晚上'
  }
  return map[timeSlot] || timeSlot
}

/**
 * 获取小组提示信息
 */
const getTeamTooltip = (teamIds: number[]): string => {
  const names = teamIds
    .map(id => teamOptions.value.find(a => a.id === id)?.displayName)
    .filter(Boolean)
  return names.join('、')
}

/**
 * 机构筛选变化（保留已选项，避免误删）
 */
const handleAgencyFilterChange = () => {}

/**
 * 格式化小组显示名称
 */
const formatTeamLabel = (team: TeamOption) => {
  return team.displayName || team.team_name
}
</script>

<style scoped lang="scss">
.message-template-container {
  padding: 20px;
  
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    
    h2 {
      margin: 0;
      font-size: 20px;
      font-weight: 600;
      color: #303133;
    }
  }
  
  .filter-card {
    margin-bottom: 16px;
  }
  
  .table-card {
    .content-preview {
      cursor: pointer;
      
      &:hover {
        color: #409EFF;
      }
    }
  }
  
  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: center;
  }
  
  .form-section {
    margin-bottom: 24px;
    
    .section-title {
      font-size: 14px;
      font-weight: 600;
      color: #303133;
      margin-bottom: 16px;
      padding-bottom: 8px;
      border-bottom: 1px solid #EBEEF5;
    }
  }
  
  .form-tip {
    font-size: 12px;
    color: #909399;
    margin-top: 4px;
  }
  
  .form-actions {
    margin-top: 4px;
  }

  .supplier-mappings {
    width: 100%;
    
    .supplier-mapping-item {
      display: flex;
      align-items: center;
      margin-bottom: 10px;
      padding: 10px;
      background-color: #f5f7fa;
      border-radius: 4px;
    }
  }

  .team-select-row {
    display: flex;
    gap: 12px;
  }
  
  .variable-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }
  
  .variable-tag {
    cursor: pointer;
    user-select: none;
    
    &:hover {
      opacity: 0.8;
      transform: scale(1.05);
    }
  }
  
  .content-preview-box {
    background-color: #F5F7FA;
    border: 1px solid #DCDFE6;
    border-radius: 4px;
    padding: 12px;
    min-height: 100px;
    color: #606266;
    font-size: 13px;
    line-height: 1.6;
    white-space: pre-wrap;
  }
  
  .drawer-footer {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
  }
}
</style>

