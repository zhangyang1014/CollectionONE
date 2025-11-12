<template>
  <div class="strategy-wizard">
    <el-steps :active="currentStep" finish-status="success" align-center>
      <el-step title="基础信息" />
      <el-step title="分配条件" />
      <el-step title="机构与策略" />
    </el-steps>

    <div class="wizard-content">
      <!-- 第一步：基础信息 -->
      <div v-show="currentStep === 0" class="step-content">
        <el-form :model="formData" :rules="rules" ref="step1FormRef" label-width="120px">
          <el-form-item label="所属队列" prop="queue_id">
            <el-select v-model="formData.queue_id" placeholder="选择队列" disabled style="width: 300px">
              <el-option
                v-for="queue in queues"
                :key="queue.id"
                :label="`${queue.queue_name} (${queue.queue_code})`"
                :value="queue.id"
              />
            </el-select>
            <el-text type="info" size="small" style="margin-left: 10px;">
              时区：UTC+8（北京时间）
            </el-text>
          </el-form-item>

          <el-form-item label="策略名称" prop="strategy_name">
            <el-input
              v-model="formData.strategy_name"
              placeholder="请输入策略名称（1-50字，支持中英文）"
              maxlength="50"
              show-word-limit
              style="width: 500px"
            />
          </el-form-item>

          <el-form-item label="启动时间" prop="start_time">
            <el-date-picker
              v-model="formData.start_time"
              type="datetime"
              placeholder="选择启动时间"
              :disabled-date="disabledDate"
              style="width: 300px"
            />
            <el-text type="info" size="small" style="margin-left: 10px;">
              不得早于当前时间
            </el-text>
          </el-form-item>

          <el-form-item label="策略描述">
            <el-input
              v-model="formData.description"
              type="textarea"
              :rows="3"
              placeholder="请输入策略描述（可选）"
              maxlength="200"
              show-word-limit
              style="width: 500px"
            />
          </el-form-item>
        </el-form>
      </div>

      <!-- 第二步：分配条件 -->
      <div v-show="currentStep === 1" class="step-content">
        <el-alert
          title="提示：多个条件组之间为 OR 关系（任一满足），组内条件为 AND 关系（全部满足）"
          type="info"
          :closable="false"
          show-icon
          style="margin-bottom: 20px"
        />

        <div v-for="(group, groupIndex) in formData.condition_groups" :key="groupIndex" class="condition-group">
          <div class="group-header">
            <el-tag type="primary" size="small">条件组 {{ groupIndex + 1 }}</el-tag>
            <el-space>
              <el-button
                link
                type="primary"
                size="small"
                :icon="Plus"
                @click="addCondition(groupIndex)"
              >
                添加条件
              </el-button>
              <el-button
                link
                type="success"
                size="small"
                :icon="CopyDocument"
                @click="copyGroup(groupIndex)"
              >
                复制组
              </el-button>
              <el-button
                link
                type="danger"
                size="small"
                :icon="Delete"
                @click="deleteGroup(groupIndex)"
                :disabled="formData.condition_groups.length === 1"
              >
                删除组
              </el-button>
            </el-space>
          </div>

          <el-table :data="group.conditions" border size="small" style="margin-top: 10px;">
            <el-table-column label="字段" width="200">
              <template #default="{ row }">
                <el-select v-model="row.field_key" placeholder="选择字段" size="small">
                  <el-option-group
                    v-for="fieldGroup in availableFields"
                    :key="fieldGroup.label"
                    :label="fieldGroup.label"
                  >
                    <el-option
                      v-for="field in fieldGroup.options"
                      :key="field.key"
                      :label="field.name"
                      :value="field.key"
                    />
                  </el-option-group>
                </el-select>
              </template>
            </el-table-column>

            <el-table-column label="运算符" width="150">
              <template #default="{ row }">
                <el-select v-model="row.operator" placeholder="选择运算符" size="small">
                  <el-option label="等于 (=)" value="eq" />
                  <el-option label="不等于 (!=)" value="ne" />
                  <el-option label="大于 (>)" value="gt" />
                  <el-option label="大于等于 (>=)" value="gte" />
                  <el-option label="小于 (<)" value="lt" />
                  <el-option label="小于等于 (<=)" value="lte" />
                  <el-option label="区间 (between)" value="between" />
                  <el-option label="包含 (contains)" value="contains" />
                  <el-option label="不包含 (not contains)" value="not_contains" />
                  <el-option label="在列表中 (in)" value="in" />
                  <el-option label="不在列表中 (not in)" value="not_in" />
                </el-select>
              </template>
            </el-table-column>

            <el-table-column label="值">
              <template #default="{ row }">
                <el-input v-model="row.value" placeholder="请输入值" size="small" />
              </template>
            </el-table-column>

            <el-table-column label="操作" width="80" align="center">
              <template #default="{ $index }">
                <el-button
                  link
                  type="danger"
                  size="small"
                  :icon="Delete"
                  @click="deleteCondition(groupIndex, $index)"
                  :disabled="group.conditions.length === 1"
                />
              </template>
            </el-table-column>
          </el-table>

          <div v-if="groupIndex < formData.condition_groups.length - 1" class="or-divider">
            <el-divider>
              <el-tag type="warning">OR</el-tag>
            </el-divider>
          </div>
        </div>

        <el-button
          type="primary"
          :icon="Plus"
          @click="addGroup"
          style="margin-top: 20px; width: 100%;"
        >
          添加条件组
        </el-button>

        <!-- 实时预览 -->
        <el-card shadow="never" style="margin-top: 20px; background: #f5f7fa;">
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center;">
              <span>条件预览（符合条件的案件数量）</span>
              <el-button size="small" @click="previewCases">刷新预览</el-button>
            </div>
          </template>
          <el-statistic title="预计匹配案件数" :value="previewCaseCount" />
        </el-card>
      </div>

      <!-- 第三步：机构与策略 -->
      <div v-show="currentStep === 2" class="step-content">
        <el-form :model="formData" :rules="rules" ref="step3FormRef" label-width="140px">
          <el-divider content-position="left">目标选择</el-divider>

          <el-form-item label="分配层级" prop="target_level">
            <el-radio-group v-model="formData.target_level">
              <el-radio label="agency">机构</el-radio>
              <el-radio label="team">小组</el-radio>
              <el-radio label="collector">催员</el-radio>
            </el-radio-group>
          </el-form-item>

          <el-form-item label="目标机构" prop="target_agencies">
            <el-select
              v-model="formData.target_agencies"
              multiple
              placeholder="选择目标机构"
              style="width: 500px"
              @change="handleAgencyChange"
            >
              <el-option
                v-for="agency in mockAgencies"
                :key="agency.id"
                :label="`${agency.agency_name} (${agency.collector_count}人)`"
                :value="agency.id"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="目标小组" prop="target_teams" v-if="formData.target_level !== 'agency'">
            <el-select
              v-model="formData.target_teams"
              multiple
              placeholder="选择目标小组"
              style="width: 500px"
              :disabled="formData.target_agencies.length === 0"
            >
              <el-option
                v-for="team in filteredTeams"
                :key="team.id"
                :label="`${team.team_name} (${team.collector_count}人)`"
                :value="team.id"
              />
            </el-select>
          </el-form-item>

          <el-divider content-position="left">分配策略</el-divider>

          <el-form-item label="分配方式" prop="assignment_mode">
            <el-radio-group v-model="formData.assignment_mode">
              <el-radio label="by_count">按数量平均</el-radio>
              <el-radio label="by_amount">按金额平均</el-radio>
            </el-radio-group>
          </el-form-item>

          <el-form-item label="优先分配（粘连）">
            <el-switch v-model="formData.enable_stickiness" />
            <el-text type="info" size="small" style="margin-left: 10px;">
              开启后，案件优先分配给历史跟进的催员
            </el-text>
          </el-form-item>

          <el-form-item label="粘连粒度" v-if="formData.enable_stickiness">
            <el-radio-group v-model="formData.stickiness_level">
              <el-radio label="customer">同客户</el-radio>
              <el-radio label="customer_product" disabled>同客户+产品（阶段三）</el-radio>
              <el-radio label="customer_merchant" disabled>同客户+商户（阶段三）</el-radio>
            </el-radio-group>
          </el-form-item>

          <el-form-item label="历史窗口" v-if="formData.enable_stickiness">
            <el-select v-model="formData.stickiness_window" style="width: 200px">
              <el-option label="7 天" :value="7" />
              <el-option label="30 天" :value="30" />
              <el-option label="90 天" :value="90" />
            </el-select>
          </el-form-item>

          <el-form-item label="容量限制模式" prop="capacity_mode">
            <el-radio-group v-model="formData.capacity_mode">
              <el-radio label="hard">硬限制（严格上限）</el-radio>
              <el-radio label="soft">软限制（允许超10%）</el-radio>
              <el-radio label="unlimited">无限制（仅统计）</el-radio>
            </el-radio-group>
          </el-form-item>

          <el-form-item label="案件排序">
            <el-select v-model="formData.sort_by" style="width: 300px">
              <el-option label="逾期天数倒序" value="overdue_days_desc" />
              <el-option label="逾期天数升序" value="overdue_days_asc" />
              <el-option label="金额倒序" value="amount_desc" />
              <el-option label="金额升序" value="amount_asc" />
              <el-option label="应还日期升序" value="due_date_asc" />
            </el-select>
          </el-form-item>

          <!-- 容量预警 -->
          <el-alert
            v-if="capacityWarning"
            :title="capacityWarning"
            type="warning"
            :closable="false"
            show-icon
            style="margin-top: 20px"
          />
        </el-form>
      </div>
    </div>

    <!-- 底部按钮 -->
    <div class="wizard-footer">
      <el-button @click="handleCancel">取消</el-button>
      <el-button v-if="currentStep > 0" @click="prevStep">上一步</el-button>
      <el-button v-if="currentStep < 2" type="primary" @click="nextStep">下一步</el-button>
      <el-button v-if="currentStep === 2" type="primary" :loading="submitting" @click="handleSubmit">
        提交
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Delete, CopyDocument } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'

interface Props {
  queueId: number
  strategy?: any
}

const props = defineProps<Props>()
const emit = defineEmits(['success', 'cancel'])

const currentStep = ref(0)
const submitting = ref(false)
const step1FormRef = ref<FormInstance>()
const step3FormRef = ref<FormInstance>()
const previewCaseCount = ref(0)

// 队列列表
const queues = ref([
  { id: 1, queue_code: 'C', queue_name: 'C队列' },
  { id: 2, queue_code: 'S0', queue_name: 'S0队列' },
  { id: 3, queue_code: 'S1', queue_name: 'S1队列' },
  { id: 4, queue_code: 'L1', queue_name: 'L1队列' },
  { id: 5, queue_code: 'M1', queue_name: 'M1队列' },
])

// Mock 机构数据
const mockAgencies = ref([
  { id: 1, agency_name: '机构A', collector_count: 50 },
  { id: 2, agency_name: '机构B', collector_count: 30 },
  { id: 3, agency_name: '机构C', collector_count: 20 },
])

// Mock 小组数据
const mockTeams = ref([
  { id: 1, agency_id: 1, team_name: '电催组1', collector_count: 15 },
  { id: 2, agency_id: 1, team_name: '电催组2', collector_count: 20 },
  { id: 3, agency_id: 1, team_name: '外访组', collector_count: 15 },
  { id: 4, agency_id: 2, team_name: '催收组1', collector_count: 15 },
  { id: 5, agency_id: 2, team_name: '催收组2', collector_count: 15 },
  { id: 6, agency_id: 3, team_name: '综合组', collector_count: 20 },
])

// 可用字段
const availableFields = ref([
  {
    label: '系统字段',
    options: [
      { key: 'queue_code', name: '队列编码' },
      { key: 'overdue_days', name: '逾期天数' },
      { key: 'outstanding_amount', name: '逾期金额' },
    ],
  },
  {
    label: '标准字段',
    options: [
      { key: 'app_name', name: 'App名称' },
      { key: 'product_name', name: '产品名称' },
      { key: 'collection_type', name: '首复催类型' },
      { key: 'merchant_name', name: '贷超商户' },
    ],
  },
])

// 表单数据
const formData = reactive({
  queue_id: props.queueId,
  strategy_name: props.strategy?.strategy_name || '',
  start_time: props.strategy?.start_time || null,
  description: props.strategy?.description || '',
  condition_groups: [
    {
      conditions: [
        { field_key: '', operator: '', value: '' },
      ],
    },
  ],
  target_level: 'collector',
  target_agencies: [] as number[],
  target_teams: [] as number[],
  assignment_mode: 'by_count',
  enable_stickiness: false,
  stickiness_level: 'customer',
  stickiness_window: 30,
  capacity_mode: 'soft',
  sort_by: 'overdue_days_desc',
})

// 表单验证规则
const rules: FormRules = {
  strategy_name: [
    { required: true, message: '请输入策略名称', trigger: 'blur' },
    { min: 1, max: 50, message: '长度在 1 到 50 个字符', trigger: 'blur' },
  ],
  start_time: [
    { required: true, message: '请选择启动时间', trigger: 'change' },
  ],
  target_level: [
    { required: true, message: '请选择分配层级', trigger: 'change' },
  ],
  target_agencies: [
    { required: true, message: '请选择目标机构', trigger: 'change' },
  ],
  assignment_mode: [
    { required: true, message: '请选择分配方式', trigger: 'change' },
  ],
  capacity_mode: [
    { required: true, message: '请选择容量限制模式', trigger: 'change' },
  ],
}

// 过滤小组
const filteredTeams = computed(() => {
  return mockTeams.value.filter(team =>
    formData.target_agencies.includes(team.agency_id)
  )
})

// 容量预警
const capacityWarning = computed(() => {
  if (formData.target_agencies.length === 0) return ''
  
  const totalCollectors = mockAgencies.value
    .filter(a => formData.target_agencies.includes(a.id))
    .reduce((sum, a) => sum + a.collector_count, 0)
  
  if (totalCollectors < 10) {
    return `当前选择的催员数量较少（${totalCollectors}人），可能无法满足分配需求`
  }
  
  return ''
})

// 禁用早于当前的日期
const disabledDate = (time: Date) => {
  return time.getTime() < Date.now() - 24 * 60 * 60 * 1000
}

// 添加条件
const addCondition = (groupIndex: number) => {
  formData.condition_groups[groupIndex].conditions.push({
    field_key: '',
    operator: '',
    value: '',
  })
}

// 删除条件
const deleteCondition = (groupIndex: number, conditionIndex: number) => {
  formData.condition_groups[groupIndex].conditions.splice(conditionIndex, 1)
}

// 添加条件组
const addGroup = () => {
  formData.condition_groups.push({
    conditions: [
      { field_key: '', operator: '', value: '' },
    ],
  })
}

// 删除条件组
const deleteGroup = (groupIndex: number) => {
  formData.condition_groups.splice(groupIndex, 1)
}

// 复制条件组
const copyGroup = (groupIndex: number) => {
  const group = JSON.parse(JSON.stringify(formData.condition_groups[groupIndex]))
  formData.condition_groups.push(group)
}

// 预览案件
const previewCases = () => {
  // Mock 数据
  previewCaseCount.value = Math.floor(Math.random() * 1000) + 500
  ElMessage.success('预览已刷新')
}

// 机构变化处理
const handleAgencyChange = () => {
  // 清空小组选择
  formData.target_teams = formData.target_teams.filter(teamId =>
    filteredTeams.value.some(t => t.id === teamId)
  )
}

// 下一步
const nextStep = async () => {
  if (currentStep.value === 0) {
    // 验证第一步
    if (!step1FormRef.value) return
    await step1FormRef.value.validate((valid) => {
      if (valid) {
        currentStep.value++
      }
    })
  } else if (currentStep.value === 1) {
    // 验证第二步（条件不为空）
    const hasValidCondition = formData.condition_groups.some(group =>
      group.conditions.some(c => c.field_key && c.operator)
    )
    
    if (!hasValidCondition) {
      ElMessage.warning('请至少配置一个有效的分配条件')
      return
    }
    
    currentStep.value++
  }
}

// 上一步
const prevStep = () => {
  currentStep.value--
}

// 取消
const handleCancel = () => {
  emit('cancel')
}

// 提交
const handleSubmit = async () => {
  if (!step3FormRef.value) return
  
  await step3FormRef.value.validate(async (valid) => {
    if (!valid) return
    
    try {
      submitting.value = true
      
      // TODO: 调用实际 API
      // await createStrategy(formData)
      
      await new Promise(resolve => setTimeout(resolve, 1000))
      
      ElMessage.success('策略创建成功')
      emit('success')
    } catch (error) {
      console.error('创建策略失败：', error)
      ElMessage.error('创建策略失败')
    } finally {
      submitting.value = false
    }
  })
}
</script>

<style scoped>
.strategy-wizard {
  padding: 20px;
}

.wizard-content {
  margin: 30px 0;
  min-height: 400px;
}

.step-content {
  padding: 20px;
}

.condition-group {
  margin-bottom: 30px;
  padding: 15px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background: #fafafa;
}

.group-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.or-divider {
  margin: 20px 0;
}

.wizard-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding-top: 20px;
  border-top: 1px solid #dcdfe6;
}
</style>

