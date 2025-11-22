<template>
  <div class="payment-code-tab">
    <!-- 请求还款码按钮 -->
    <div class="action-bar">
      <el-button type="primary" @click="handleRequestClick">
        <el-icon><Plus /></el-icon>
        请求还款码
      </el-button>
    </div>

    <!-- 筛选器 -->
    <div class="filter-bar">
      <el-radio-group v-model="filterStatus" @change="loadPaymentCodes">
        <el-radio-button label="">全部</el-radio-button>
        <el-radio-button label="PENDING">待支付</el-radio-button>
        <el-radio-button label="PAID">已支付</el-radio-button>
        <el-radio-button label="EXPIRED">已过期</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 还款码列表 -->
    <el-table
      v-loading="loading"
      :data="paymentCodes"
      class="payment-code-table"
    >
      <el-table-column label="渠道" width="200">
        <template #default="{ row }">
          <div class="channel-info">
            <el-image
              v-if="row.channel_icon"
              :src="row.channel_icon"
              fit="contain"
              style="width: 32px; height: 32px; margin-right: 8px"
            />
            <div>
              <div class="channel-name">{{ row.channel_name }}</div>
              <el-tag size="small" :type="getTypeTag(row.payment_type)">
                {{ PAYMENT_TYPE_TEXT[row.payment_type] }}
              </el-tag>
            </div>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="期数" width="80">
        <template #default="{ row }">
          <span v-if="row.installment_number">第{{ row.installment_number }}期</span>
          <span v-else>-</span>
        </template>
      </el-table-column>

      <el-table-column label="金额" width="150">
        <template #default="{ row }">
          <div class="amount">
            {{ row.currency }} {{ formatAmount(row.amount) }}
          </div>
        </template>
      </el-table-column>

      <el-table-column label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="PAYMENT_STATUS_TAG_TYPE[row.status]">
            {{ PAYMENT_STATUS_TEXT[row.status] }}
          </el-tag>
          <div v-if="row.status === 'PENDING' && row.remaining_seconds" class="countdown">
            <el-icon><Timer /></el-icon>
            {{ formatCountdown(row.remaining_seconds) }}
          </div>
        </template>
      </el-table-column>

      <el-table-column label="创建时间" width="180">
        <template #default="{ row }">
          {{ formatDateTime(row.created_at) }}
        </template>
      </el-table-column>

      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="showDetail(row)">
            查看详情
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 渠道选择对话框 -->
    <el-dialog
      v-model="showChannelSelector"
      title="选择还款渠道"
      width="600px"
    >
      <el-row :gutter="16">
        <el-col
          v-for="channel in availableChannels"
          :key="channel.id"
          :span="12"
        >
          <el-card
            class="channel-card"
            shadow="hover"
            @click="selectChannel(channel)"
          >
            <div class="channel-card-content">
              <el-image
                v-if="channel.channel_icon"
                :src="channel.channel_icon"
                fit="contain"
                style="width: 48px; height: 48px"
              />
              <div class="channel-card-info">
                <div class="channel-card-name">{{ channel.channel_name }}</div>
                <el-tag size="small">{{ PAYMENT_TYPE_TEXT[channel.channel_type] }}</el-tag>
                <div class="channel-card-provider">{{ channel.service_provider }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </el-dialog>

    <!-- 金额输入对话框 -->
    <el-dialog
      v-model="showAmountDialog"
      title="请求还款码"
      width="500px"
    >
      <el-form :model="requestForm" label-width="120px">
        <el-form-item label="案件编号">
          <el-input :value="caseInfo?.case_no || caseInfo?.case_id || '-'" disabled />
        </el-form-item>

        <el-form-item label="客户姓名">
          <el-input :value="caseInfo?.customer_name || caseInfo?.user_name || '-'" disabled />
        </el-form-item>

        <el-form-item label="期数选择">
          <el-select
            v-model="requestForm.installment_number"
            placeholder="请选择期数"
            @change="handleInstallmentChange"
          >
            <el-option
              v-for="inst in installments"
              :key="inst.number"
              :label="`第${inst.number}期${inst.status === 'OVERDUE' ? '（逾期）' : ''}`"
              :value="inst.number"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="本期应还">
          <div v-if="selectedInstallment" class="installment-detail">
            <div class="detail-item">
              <span>本金：</span>
              <span>{{ formatAmount(selectedInstallment.principal) }}</span>
            </div>
            <div class="detail-item">
              <span>利息：</span>
              <span>{{ formatAmount(selectedInstallment.interest) }}</span>
            </div>
            <div class="detail-item">
              <span>罚息：</span>
              <span>{{ formatAmount(selectedInstallment.penalty) }}</span>
            </div>
            <div class="detail-item">
              <span>费用：</span>
              <span>{{ formatAmount(selectedInstallment.fee) }}</span>
            </div>
            <el-divider />
            <div class="detail-item total">
              <span>合计：</span>
              <span>{{ formatAmount(selectedInstallment.total) }}</span>
            </div>
          </div>
        </el-form-item>

        <el-form-item label="实际还款金额" required>
          <el-input-number
            v-model="requestForm.amount"
            :precision="2"
            :min="0.01"
            :controls="false"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showAmountDialog = false">取消</el-button>
        <el-button type="primary" :loading="requesting" @click="handleRequestCode">
          确认生成
        </el-button>
      </template>
    </el-dialog>

    <!-- 还款码详情对话框 -->
    <el-dialog
      v-model="showDetailDialog"
      title="还款码详情"
      width="600px"
    >
      <div v-if="currentDetail" class="code-detail">
        <div class="detail-header">
          <el-image
            v-if="currentDetail.channel_icon"
            :src="currentDetail.channel_icon"
            fit="contain"
            style="width: 64px; height: 64px"
          />
          <div>
            <div class="detail-channel-name">{{ currentDetail.channel_name }}</div>
            <el-tag :type="getTypeTag(currentDetail.payment_type)">
              {{ PAYMENT_TYPE_TEXT[currentDetail.payment_type] }}
            </el-tag>
          </div>
        </div>

        <el-divider />

        <!-- VA码类型 -->
        <div v-if="currentDetail.payment_type === 'VA'" class="code-display">
          <div class="code-label">虚拟账户号</div>
          <div class="code-value">{{ currentDetail.payment_code }}</div>
          <el-button type="primary" @click="copyCode(currentDetail.payment_code)">
            <el-icon><CopyDocument /></el-icon>
            复制
          </el-button>
        </div>

        <!-- H5链接类型 -->
        <div v-else-if="currentDetail.payment_type === 'H5'" class="code-display">
          <div class="code-label">支付链接</div>
          <div class="code-value link">{{ currentDetail.payment_code }}</div>
          <div class="button-group">
            <el-button type="primary" @click="openLink(currentDetail.payment_code)">
              <el-icon><Link /></el-icon>
              打开链接
            </el-button>
            <el-button @click="copyCode(currentDetail.payment_code)">
              <el-icon><CopyDocument /></el-icon>
              复制
            </el-button>
          </div>
        </div>

        <!-- 二维码类型 -->
        <div v-else-if="currentDetail.payment_type === 'QR'" class="code-display">
          <div class="code-label">支付二维码</div>
          <el-image
            v-if="currentDetail.qr_image_url"
            :src="currentDetail.qr_image_url"
            fit="contain"
            style="width: 200px; height: 200px"
            :preview-src-list="[currentDetail.qr_image_url]"
          />
        </div>

        <el-divider />

        <div class="detail-info">
          <div class="info-item">
            <span class="label">还款金额：</span>
            <span class="value">{{ currentDetail.currency }} {{ formatAmount(currentDetail.amount) }}</span>
          </div>
          <div class="info-item">
            <span class="label">状态：</span>
            <el-tag :type="PAYMENT_STATUS_TAG_TYPE[currentDetail.status]">
              {{ PAYMENT_STATUS_TEXT[currentDetail.status] }}
            </el-tag>
          </div>
          <div class="info-item">
            <span class="label">创建时间：</span>
            <span class="value">{{ formatDateTime(currentDetail.created_at) }}</span>
          </div>
          <div v-if="currentDetail.expired_at" class="info-item">
            <span class="label">有效期至：</span>
            <span class="value">{{ formatDateTime(currentDetail.expired_at) }}</span>
          </div>
          <div v-if="currentDetail.paid_at" class="info-item">
            <span class="label">支付时间：</span>
            <span class="value">{{ formatDateTime(currentDetail.paid_at) }}</span>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Timer, CopyDocument, Link } from '@element-plus/icons-vue'
import {
  getAvailableChannels,
  getCaseInstallments,
  requestPaymentCode,
  getPaymentCodes,
  getPaymentCodeDetail
} from '@/api/payment'
import {
  PAYMENT_TYPE_TEXT,
  PAYMENT_STATUS_TEXT,
  PAYMENT_STATUS_TAG_TYPE,
  type SimpleChannel,
  type PaymentCodeListItem,
  type PaymentCodeDetail,
  type InstallmentInfo
} from '@/types/payment'

const props = defineProps<{
  caseInfo: any
}>()

// 状态
const loading = ref(false)
const requesting = ref(false)
const filterStatus = ref('')
const paymentCodes = ref<PaymentCodeListItem[]>([])
const availableChannels = ref<SimpleChannel[]>([])
const installments = ref<InstallmentInfo[]>([])
const showChannelSelector = ref(false)
const showAmountDialog = ref(false)
const showDetailDialog = ref(false)
const selectedChannel = ref<SimpleChannel>()
const selectedInstallment = computed(() => {
  return installments.value.find(i => i.number === requestForm.installment_number)
})
const currentDetail = ref<PaymentCodeDetail>()

// 请求表单
const requestForm = reactive({
  installment_number: undefined as number | undefined,
  amount: 0
})

// 加载还款码列表
const loadPaymentCodes = async () => {
  if (!props.caseInfo?.id) return
  
  loading.value = true
  try {
    const params: any = {
      case_id: props.caseInfo.id || props.caseInfo.case_id,
      page: 1,
      page_size: 100
    }
    if (filterStatus.value) {
      params.status = filterStatus.value
    }

    const res = await getPaymentCodes(params)
    // 注意：实际后端返回 code: 0 表示成功
    if (res.code === 0) {
      paymentCodes.value = res.data.list || res.data.items || []
    }
  } catch (error: any) {
    console.error('加载还款码列表失败:', error)
    ElMessage.error(error.message || '加载还款码列表失败')
  } finally {
    loading.value = false
  }
}

// 加载可用渠道
const loadAvailableChannels = async () => {
  try {
    // 获取甲方ID，优先使用tenant_id，其次party_id
    const partyId = props.caseInfo?.tenant_id || props.caseInfo?.party_id || 1
    console.log('加载可用渠道 - partyId:', partyId, 'caseInfo:', props.caseInfo)
    
    const res = await getAvailableChannels(partyId)
    console.log('渠道API响应:', res)
    
    // 注意：实际后端返回 code: 0 表示成功
    if (res.code === 0) {
      availableChannels.value = res.data || []
      console.log('可用渠道数量:', availableChannels.value.length)
    } else {
      console.error('API返回错误:', res)
      ElMessage.error(res.message || res.msg || '获取渠道失败')
    }
  } catch (error: any) {
    console.error('加载可用渠道失败:', error)
    ElMessage.error(error.message || '加载可用渠道失败')
  }
}

// 点击请求还款码按钮
const handleRequestClick = async () => {
  // 如果渠道列表为空，先加载渠道
  if (availableChannels.value.length === 0) {
    console.log('渠道列表为空，重新加载...')
    await loadAvailableChannels()
  }
  
  // 检查是否有可用渠道
  if (availableChannels.value.length === 0) {
    ElMessage.warning('暂无可用的支付渠道，请联系管理员配置')
    return
  }
  
  // 显示渠道选择对话框
  showChannelSelector.value = true
}

// 加载期数信息
const loadInstallments = async () => {
  if (!props.caseInfo?.id && !props.caseInfo?.case_id) return
  
  try {
    const caseId = props.caseInfo.id || props.caseInfo.case_id
    const res = await getCaseInstallments(caseId)
    // 注意：实际后端返回 code: 0 表示成功
    if (res.code === 0) {
      installments.value = res.data.installments || []
      // 默认选择当前逾期期数
      if (res.data.current_overdue) {
        requestForm.installment_number = res.data.current_overdue
        handleInstallmentChange()
      }
    }
  } catch (error: any) {
    console.error('加载期数信息失败:', error)
    // 不显示错误提示，因为可能是模拟数据
  }
}

// 选择渠道
const selectChannel = (channel: SimpleChannel) => {
  selectedChannel.value = channel
  showChannelSelector.value = false
  showAmountDialog.value = true
  
  // 如果还没有加载期数信息，现在加载
  if (installments.value.length === 0) {
    loadInstallments()
  }
}

// 期数变化
const handleInstallmentChange = () => {
  if (selectedInstallment.value) {
    requestForm.amount = selectedInstallment.value.total
  }
}

// 请求还款码
const handleRequestCode = async () => {
  if (!selectedChannel.value) return
  if (!requestForm.amount || requestForm.amount <= 0) {
    ElMessage.warning('请输入还款金额')
    return
  }

  requesting.value = true
  try {
    const data = {
      case_id: props.caseInfo?.id || props.caseInfo?.case_id,
      loan_id: props.caseInfo?.loan_id,
      channel_id: selectedChannel.value.id,
      installment_number: requestForm.installment_number,
      amount: requestForm.amount
    }

    const res = await requestPaymentCode(data)
    // 注意：实际后端返回 code: 0 表示成功
    if (res.code === 0) {
      ElMessage.success('还款码生成成功')
      showAmountDialog.value = false
      
      // 显示生成的还款码
      currentDetail.value = res.data as any
      showDetailDialog.value = true
      
      // 刷新列表
      loadPaymentCodes()
    }
  } catch (error: any) {
    console.error('生成还款码失败:', error)
    ElMessage.error(error.message || '生成还款码失败')
  } finally {
    requesting.value = false
  }
}

// 查看详情
const showDetail = async (row: PaymentCodeListItem) => {
  try {
    const res = await getPaymentCodeDetail(row.code_no)
    // 注意：实际后端返回 code: 0 表示成功
    if (res.code === 0) {
      currentDetail.value = res.data
      showDetailDialog.value = true
    }
  } catch (error) {
    ElMessage.error('获取详情失败')
  }
}

// 复制代码
const copyCode = (code?: string) => {
  if (!code) return
  navigator.clipboard.writeText(code).then(() => {
    ElMessage.success('已复制到剪贴板')
  }).catch(() => {
    ElMessage.error('复制失败')
  })
}

// 打开链接
const openLink = (url?: string) => {
  if (!url) return
  window.open(url, '_blank')
}

// 格式化金额
const formatAmount = (amount: number) => {
  return amount.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

// 格式化日期时间
const formatDateTime = (dateStr: string) => {
  return new Date(dateStr).toLocaleString('zh-CN')
}

// 格式化倒计时
const formatCountdown = (seconds: number) => {
  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  const secs = seconds % 60
  return `${hours}时${minutes}分${secs}秒`
}

// 获取类型标签
const getTypeTag = (type: string) => {
  const map: Record<string, string> = {
    VA: 'success',
    H5: 'primary',
    QR: 'warning'
  }
  return map[type] || ''
}

onMounted(() => {
  loadPaymentCodes()
  loadAvailableChannels()
})
</script>

<style scoped lang="scss">
.payment-code-tab {
  padding: 20px;
}

.action-bar {
  margin-bottom: 16px;
}

.filter-bar {
  margin-bottom: 16px;
}

.channel-info {
  display: flex;
  align-items: center;
  
  .channel-name {
    font-weight: 500;
    margin-bottom: 4px;
  }
}

.amount {
  font-size: 16px;
  font-weight: 600;
  color: #409eff;
}

.countdown {
  display: flex;
  align-items: center;
  margin-top: 4px;
  font-size: 12px;
  color: #999;
}

.channel-card {
  margin-bottom: 16px;
  cursor: pointer;
  transition: all 0.3s;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  }
}

.channel-card-content {
  display: flex;
  align-items: center;
  gap: 12px;
}

.channel-card-info {
  flex: 1;
  
  .channel-card-name {
    font-weight: 500;
    margin-bottom: 4px;
  }
  
  .channel-card-provider {
    font-size: 12px;
    color: #999;
    margin-top: 4px;
  }
}

.installment-detail {
  background: #f5f7fa;
  padding: 12px;
  border-radius: 4px;
  
  .detail-item {
    display: flex;
    justify-content: space-between;
    margin-bottom: 8px;
    
    &.total {
      font-weight: 600;
      font-size: 16px;
      color: #409eff;
    }
  }
}

.code-detail {
  .detail-header {
    display: flex;
    align-items: center;
    gap: 16px;
    
    .detail-channel-name {
      font-size: 18px;
      font-weight: 600;
      margin-bottom: 8px;
    }
  }
  
  .code-display {
    text-align: center;
    padding: 20px;
    
    .code-label {
      font-size: 14px;
      color: #666;
      margin-bottom: 12px;
    }
    
    .code-value {
      font-size: 24px;
      font-weight: 600;
      font-family: 'Courier New', monospace;
      margin-bottom: 16px;
      padding: 12px;
      background: #f5f7fa;
      border-radius: 4px;
      
      &.link {
        font-size: 14px;
        word-break: break-all;
      }
    }
    
    .button-group {
      display: flex;
      gap: 12px;
      justify-content: center;
    }
  }
  
  .detail-info {
    .info-item {
      display: flex;
      align-items: center;
      margin-bottom: 12px;
      
      .label {
        width: 100px;
        color: #666;
      }
      
      .value {
        flex: 1;
        font-weight: 500;
      }
    }
  }
}
</style>

