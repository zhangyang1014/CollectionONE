<template>
  <div class="case-detail-component">
    <!-- 案件概览 -->
    <div class="case-overview">
      <div class="overview-content">
        <!-- 客户信息块 -->
        <div class="overview-block">
          <div class="block-items">
            <div class="overview-item">
              <span class="label">用户名</span>
              <span class="value">{{ caseData.user_name }}</span>
            </div>
            <div class="overview-item">
              <span class="label">性别/年龄</span>
              <span class="value">{{ fullData.customer_basic_info?.gender || '-' }} / {{ fullData.customer_basic_info?.age || '-' }}岁</span>
            </div>
            <div class="overview-item">
              <span class="label">城市</span>
              <span class="value">{{ fullData.customer_basic_info?.city || '-' }}</span>
            </div>
            <div class="overview-item">
              <span class="label">月收入</span>
              <span class="value">{{ formatCurrency(fullData.customer_basic_info?.monthly_income) || '-' }}</span>
            </div>
          </div>
        </div>

        <!-- 贷款信息块 -->
        <div class="overview-block">
          <div class="block-items">
            <div class="overview-item" v-if="fullData.installment_details">
              <span class="label">当前期数</span>
              <span class="value">
                <el-tag type="primary" size="small">
                  第{{ fullData.installment_details?.current_installment }}期 / 共{{ fullData.installment_details?.total_installments }}期
                </el-tag>
              </span>
            </div>
            <div class="overview-item">
              <span class="label">产品名称</span>
              <span class="value">{{ caseData.product_name }}</span>
            </div>
            <div class="overview-item">
              <span class="label">应还总额</span>
              <span class="value">{{ formatCurrency(fullData.loan_details?.total_due_amount) || '-' }}</span>
            </div>
            <div class="overview-item">
              <span class="label">未还金额</span>
              <span class="value highlight-amount">{{ formatCurrency(fullData.loan_details?.outstanding_amount) || '-' }}</span>
            </div>
            <div class="overview-item">
              <span class="label">应还日期</span>
              <span class="value" :class="getOverdueDateClass(fullData.loan_details?.overdue_days)">
                {{ fullData.loan_details?.due_date || '-' }}
                <span v-if="fullData.loan_details?.overdue_days > 0" class="overdue-badge">
                  （逾期{{ fullData.loan_details?.overdue_days }}天）
                </span>
                <span v-else-if="fullData.loan_details?.overdue_days === 0" class="due-today-badge">
                  （今日到期）
                </span>
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <el-tabs v-model="activeTab" type="border-card" class="detail-tabs">
      <!-- 客户信息 -->
      <el-tab-pane label="客户信息" name="customer">
        <div class="customer-info-groups">
          <!-- 第一组：基本信息 -->
          <div class="info-group">
            <div class="group-title">基本信息</div>
            <el-descriptions :column="4" border size="small" class="info-descriptions">
              <el-descriptions-item label="用户ID">{{ caseData.user_id }}</el-descriptions-item>
              <el-descriptions-item label="用户名">{{ caseData.user_name }}</el-descriptions-item>
              <el-descriptions-item label="证件号">{{ fullData.customer_basic_info?.id_number || '-' }}</el-descriptions-item>
              <el-descriptions-item label="证件类型">{{ fullData.customer_basic_info?.id_type || '-' }}</el-descriptions-item>
              <el-descriptions-item label="性别">{{ fullData.customer_basic_info?.gender || '-' }}</el-descriptions-item>
              <el-descriptions-item label="年龄">{{ fullData.customer_basic_info?.age || '-' }}</el-descriptions-item>
              <el-descriptions-item label="手机号">
                <span v-if="!phoneNumberRevealed" class="masked-phone" @click="showPhoneReasonDialog">
                  {{ maskPhoneNumber(caseData.mobile_number) }}
                  <el-icon style="margin-left: 4px; cursor: pointer;"><View /></el-icon>
                </span>
                <span v-else class="revealed-phone">{{ caseData.mobile_number }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="邮箱">{{ fullData.customer_basic_info?.email || '-' }}</el-descriptions-item>
              <el-descriptions-item label="州">{{ fullData.customer_basic_info?.state || '-' }}</el-descriptions-item>
              <el-descriptions-item label="城市">{{ fullData.customer_basic_info?.city || '-' }}</el-descriptions-item>
              <el-descriptions-item label="地址" :span="2">{{ fullData.customer_basic_info?.address || '-' }}</el-descriptions-item>
            </el-descriptions>
          </div>

          <!-- 第二组：工作信息 -->
          <div class="info-group">
            <div class="group-title">工作信息</div>
            <el-descriptions :column="4" border size="small" class="info-descriptions">
              <el-descriptions-item label="教育程度">{{ fullData.customer_basic_info?.education_level || '-' }}</el-descriptions-item>
              <el-descriptions-item label="就业类型">{{ fullData.customer_basic_info?.employment_type || '-' }}</el-descriptions-item>
              <el-descriptions-item label="公司名称">{{ fullData.customer_basic_info?.company_name || '-' }}</el-descriptions-item>
              <el-descriptions-item label="月收入">{{ formatCurrency(fullData.customer_basic_info?.monthly_income) || '-' }}</el-descriptions-item>
              <el-descriptions-item label="信用评分">
                <el-tag :type="getCreditScoreType(fullData.customer_basic_info?.credit_score)">
                  {{ fullData.customer_basic_info?.credit_score || '-' }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="历史借款次数">{{ fullData.customer_basic_info?.total_loans || 0 }}</el-descriptions-item>
            </el-descriptions>
          </div>

          <!-- 第三组：其他信息 -->
          <div class="info-group">
            <div class="group-title">其他信息</div>
            <el-descriptions :column="4" border size="small" class="info-descriptions">
              <el-descriptions-item label="备注" :span="4">{{ fullData.customer_basic_info?.notes || '-' }}</el-descriptions-item>
            </el-descriptions>
          </div>
        </div>
      </el-tab-pane>

      <!-- 影像资料 -->
      <el-tab-pane label="影像资料" name="documents">
        <div class="documents-section">
          <div class="document-images">
            <div class="image-item">
              <div class="image-label">证件照（正面）</div>
              <el-image 
                :src="fullData.document_images?.id_front_image" 
                fit="cover" 
                class="doc-image"
                :preview-src-list="[fullData.document_images?.id_front_image]"
              >
                <template #error>
                  <div class="image-placeholder">
                    <el-icon><Picture /></el-icon>
                    <span>暂无图片</span>
                  </div>
                </template>
              </el-image>
            </div>
            <div class="image-item">
              <div class="image-label">证件照（反面）</div>
              <el-image 
                :src="fullData.document_images?.id_back_image" 
                fit="cover" 
                class="doc-image"
                :preview-src-list="[fullData.document_images?.id_back_image]"
              >
                <template #error>
                  <div class="image-placeholder">
                    <el-icon><Picture /></el-icon>
                    <span>暂无图片</span>
                  </div>
                </template>
              </el-image>
            </div>
            <div class="image-item">
              <div class="image-label">活体照片</div>
              <el-image 
                :src="fullData.document_images?.live_photo" 
                fit="cover" 
                class="doc-image"
                :preview-src-list="[fullData.document_images?.live_photo]"
              >
                <template #error>
                  <div class="image-placeholder">
                    <el-icon><Picture /></el-icon>
                    <span>暂无图片</span>
                  </div>
                </template>
              </el-image>
            </div>
          </div>
          
          <el-divider />
          
          <div class="document-verification">
            <h4>标记证件信息</h4>
            <el-form label-width="120px" size="small">
              <el-form-item label="异常标记：">
                <el-checkbox-group v-model="documentIssues">
                  <el-checkbox value="fake_id">虚假证件信息（如涂改证件号码、人像贴图等）</el-checkbox>
                  <el-checkbox value="fake_live">虚拟活体照片（如照片挡脸）</el-checkbox>
                  <el-checkbox value="mismatch">活体和证件照片不是同一人</el-checkbox>
                  <el-checkbox value="other">其他异常</el-checkbox>
                </el-checkbox-group>
              </el-form-item>
              <el-form-item label="备注：">
                <el-input v-model="documentRemark" type="textarea" :rows="2" placeholder="请输入异常说明" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" size="small" @click="submitDocumentVerification">更新</el-button>
              </el-form-item>
            </el-form>
          </div>
        </div>
      </el-tab-pane>

      <!-- 贷款信息 -->
      <el-tab-pane label="贷款信息" name="loan">
        <el-collapse v-model="activeLoanCollapse">
          <!-- 基本信息 -->
          <el-collapse-item name="basic" title="基本信息">
            <el-descriptions :column="4" border size="small">
              <el-descriptions-item label="贷款编号">{{ caseData.loan_id }}</el-descriptions-item>
              <el-descriptions-item label="贷款类型">{{ fullData.loan_details?.loan_type || '-' }}</el-descriptions-item>
              <el-descriptions-item label="产品名称">{{ caseData.product_name }}</el-descriptions-item>
              <el-descriptions-item label="App名称">{{ caseData.app_name }}</el-descriptions-item>
              <el-descriptions-item label="贷款来源">{{ fullData.loan_details?.loan_source || '-' }}</el-descriptions-item>
              <el-descriptions-item label="利率">{{ fullData.loan_details?.interest_rate || '-' }}</el-descriptions-item>
              <el-descriptions-item label="应还总额">{{ formatCurrency(fullData.loan_details?.total_due_amount) || '-' }}</el-descriptions-item>
              <el-descriptions-item label="未还金额">
                <span class="amount-highlight">{{ formatCurrency(fullData.loan_details?.outstanding_amount) || '-' }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="应还日期">{{ fullData.loan_details?.due_date || '-' }}</el-descriptions-item>
              <el-descriptions-item label="逾期天数">
                <el-tag :type="getOverdueType(fullData.loan_details?.overdue_days)">
                  {{ fullData.loan_details?.overdue_days || 0 }}天
                </el-tag>
              </el-descriptions-item>
            </el-descriptions>
          </el-collapse-item>

          <!-- 合同信息 -->
          <el-collapse-item name="contract" title="借款合同信息">
            <el-descriptions :column="4" border size="small">
              <el-descriptions-item label="合同编号">{{ fullData.loan_details?.contract_number || '-' }}</el-descriptions-item>
              <el-descriptions-item label="签约日期">{{ fullData.loan_details?.contract_sign_date || '-' }}</el-descriptions-item>
              <el-descriptions-item label="合同金额">{{ formatCurrency(fullData.loan_details?.contract_amount) || '-' }}</el-descriptions-item>
              <el-descriptions-item label="合同期限">{{ fullData.loan_details?.contract_term || '-' }}</el-descriptions-item>
              <el-descriptions-item label="服务费">{{ formatCurrency(fullData.loan_details?.service_fee) || '-' }}</el-descriptions-item>
              <el-descriptions-item label="合同文件" :span="3">
                <el-link v-if="fullData.loan_details?.contract_file_url" type="primary" underline="hover" :href="fullData.loan_details?.contract_file_url" target="_blank">
                  <el-icon><Document /></el-icon> 查看合同
                </el-link>
                <span v-else>-</span>
              </el-descriptions-item>
            </el-descriptions>
          </el-collapse-item>

          <!-- 分期信息（多期贷款） -->
          <el-collapse-item v-if="fullData.installment_details" name="installments" title="借款分期信息">
            <div class="installment-summary">
              <el-tag type="info">总期数：{{ fullData.installment_details?.total_installments }}</el-tag>
              <el-tag type="warning">当前期数：{{ fullData.installment_details?.current_installment }}</el-tag>
              <el-tag>每期金额：{{ formatCurrency(fullData.installment_details?.installment_amount) }}</el-tag>
            </div>
            <el-table :data="fullData.installment_details?.installments" border size="small" class="installment-table" max-height="300">
              <el-table-column prop="installment_number" label="期数" width="60" align="center" />
              <el-table-column prop="due_date" label="应还日期" width="110" />
              <el-table-column prop="due_amount" label="应还金额" width="100" align="right">
                <template #default="{ row }">{{ formatCurrency(row.due_amount) }}</template>
              </el-table-column>
              <el-table-column prop="paid_amount" label="已还金额" width="100" align="right">
                <template #default="{ row }">{{ formatCurrency(row.paid_amount) }}</template>
              </el-table-column>
              <el-table-column prop="outstanding_amount" label="未还金额" width="100" align="right">
                <template #default="{ row }">{{ formatCurrency(row.outstanding_amount) }}</template>
              </el-table-column>
              <el-table-column prop="status" label="状态" width="90">
                <template #default="{ row }">
                  <el-tag :type="getInstallmentStatusType(row.status)" size="small">{{ row.status }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="120" fixed="right">
                <template #default="{ row }">
                  <el-button link type="primary" size="small" @click="showPaymentQRCode(row)">
                    <el-icon><Tickets /></el-icon> 还款码
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-collapse-item>

          <!-- 放款凭证 -->
          <el-collapse-item name="disbursement" title="合同放款凭证">
            <el-descriptions :column="4" border size="small">
              <el-descriptions-item label="放款时间">{{ fullData.loan_details?.disbursement_date || '-' }}</el-descriptions-item>
              <el-descriptions-item label="放款状态">
                <el-tag type="success">{{ fullData.loan_details?.disbursement_status || '-' }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="金额">{{ formatCurrency(fullData.loan_details?.disbursement_amount) || '-' }}</el-descriptions-item>
              <el-descriptions-item label="交易流水号">{{ fullData.loan_details?.transaction_id || '-' }}</el-descriptions-item>
              <el-descriptions-item label="收款人姓名">{{ fullData.loan_details?.recipient_name || '-' }}</el-descriptions-item>
              <el-descriptions-item label="银行名">{{ fullData.loan_details?.bank_name || '-' }}</el-descriptions-item>
              <el-descriptions-item label="银行账户" :span="2">{{ fullData.loan_details?.bank_account || '-' }}</el-descriptions-item>
            </el-descriptions>
          </el-collapse-item>
        </el-collapse>
      </el-tab-pane>

      <!-- 历史借款记录 -->
      <el-tab-pane label="历史借款" name="history">
        <el-table v-if="fullData.loan_history && fullData.loan_history.length > 0" :data="fullData.loan_history" border size="small" max-height="400">
          <el-table-column prop="loan_id" label="贷款编号" width="140" />
          <el-table-column prop="loan_date" label="借款日期" width="120" />
          <el-table-column prop="loan_amount" label="借款金额" width="110" align="right">
            <template #default="{ row }">{{ formatCurrency(row.loan_amount) }}</template>
          </el-table-column>
          <el-table-column prop="repay_date" label="还款日期" width="120" />
          <el-table-column prop="repay_amount" label="还款金额" width="110" align="right">
            <template #default="{ row }">{{ formatCurrency(row.repay_amount) }}</template>
          </el-table-column>
          <el-table-column prop="overdue_days" label="逾期天数" width="100" align="center">
            <template #default="{ row }">
              <el-tag v-if="row.overdue_days > 0" type="warning" size="small">{{ row.overdue_days }}天</el-tag>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getLoanStatusType(row.status)" size="small">{{ row.status }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-else description="暂无历史借款记录" />
      </el-tab-pane>

      <!-- 还款记录 -->
      <el-tab-pane label="还款记录" name="payments">
        <el-table v-if="fullData.payment_records && fullData.payment_records.length > 0" :data="fullData.payment_records" border size="small" max-height="400">
          <el-table-column prop="payment_id" label="还款编号" width="150" />
          <el-table-column prop="payment_date" label="还款时间" width="170" />
          <el-table-column prop="due_date" label="到期日" width="120" />
          <el-table-column prop="payment_amount" label="还款金额" width="110" align="right">
            <template #default="{ row }">{{ formatCurrency(row.payment_amount) }}</template>
          </el-table-column>
          <el-table-column prop="payment_method" label="还款方式" width="110" />
          <el-table-column prop="payment_channel" label="渠道" width="80" />
          <el-table-column prop="payment_status" label="状态" width="90">
            <template #default="{ row }">
              <el-tag :type="getPaymentStatusType(row.payment_status)" size="small">{{ row.payment_status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="transaction_id" label="交易流水号" width="160" />
          <el-table-column prop="remark" label="备注" min-width="100" />
        </el-table>
        <el-empty v-else description="暂无还款记录" />
      </el-tab-pane>

      <!-- 还款码 -->
      <el-tab-pane label="还款码" name="payment_codes">
        <PaymentCodeTab :caseInfo="caseData" />
      </el-tab-pane>
    </el-tabs>

    <!-- 还款码弹窗 -->
    <el-dialog v-model="qrCodeDialogVisible" title="查看还款码" width="420px" align-center>
      <div class="qr-code-content">
        <el-image :src="currentQRCode" fit="contain" style="width: 300px; height: 300px; margin: 0 auto;">
          <template #error>
            <div class="image-placeholder">
              <el-icon :size="60"><Tickets /></el-icon>
              <p>还款码加载失败</p>
            </div>
          </template>
        </el-image>
        <p class="qr-tip">请引导客户扫码进行还款</p>
      </div>
    </el-dialog>

    <!-- 查看手机号原因弹窗 -->
    <el-dialog v-model="phoneReasonDialogVisible" title="请选择查看客户手机号原因" width="450px" align-center>
      <el-form :model="phoneReasonForm" label-position="top">
        <el-form-item label="查看原因">
          <el-radio-group v-model="phoneReasonForm.reason">
            <el-radio value="facebook">Facebook查询</el-radio>
            <el-radio value="social_security">社保查询</el-radio>
            <el-radio value="other">其他</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="phoneReasonForm.reason === 'other'" label="请说明原因" required>
          <el-input 
            v-model="phoneReasonForm.otherReason" 
            type="textarea" 
            :rows="3" 
            placeholder="请输入查看手机号的具体原因（必填）" 
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="phoneReasonDialogVisible = false">取消查看</el-button>
        <el-button type="primary" @click="submitPhoneReason">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Picture, Document, Tickets, View } from '@element-plus/icons-vue'
import PaymentCodeTab from '@/views/im/components/PaymentCodeTab.vue'

// Props
const props = defineProps<{
  caseData: any
  fullData: any
}>()

// Tab状态
const activeTab = ref('customer')
const activeLoanCollapse = ref(['basic', 'contract'])

// 影像资料审核
const documentIssues = ref<string[]>([])
const documentRemark = ref('')

// 还款码弹窗
const qrCodeDialogVisible = ref(false)
const currentQRCode = ref('')

// 手机号查看
const phoneNumberRevealed = ref(false)
const phoneReasonDialogVisible = ref(false)
const phoneReasonForm = ref({
  reason: '',
  otherReason: ''
})

// 监听案件变化，重置Tab
watch(() => props.caseData?.id, () => {
  activeTab.value = 'customer'
  documentIssues.value = []
  documentRemark.value = ''
  phoneNumberRevealed.value = false  // 切换案件时重置手机号显示状态
})

// 格式化金额（整数）
const formatCurrency = (amount: number) => {
  if (!amount) return '0'
  return Math.round(amount).toLocaleString('zh-CN')
}

// 证件号脱敏显示
const maskIdNumber = (idNumber: string) => {
  if (!idNumber) return '-'
  if (idNumber.length <= 8) return idNumber
  return idNumber.substring(0, 4) + '****' + idNumber.substring(idNumber.length - 4)
}

// 获取逾期日期的CSS类名
const getOverdueDateClass = (overdueDays: number) => {
  if (!overdueDays || overdueDays < 0) return 'date-normal'
  if (overdueDays === 0) return 'date-due-today'
  if (overdueDays <= 7) return 'date-overdue-short'
  return 'date-overdue-long'
}

// 信用评分类型
const getCreditScoreType = (score: number) => {
  if (!score) return 'info'
  if (score >= 750) return 'success'
  if (score >= 650) return 'warning'
  return 'danger'
}

// 逾期天数类型
const getOverdueType = (days: number) => {
  if (!days || days <= 0) return 'success'
  if (days <= 7) return 'warning'
  return 'danger'
}

// 分期状态类型
const getInstallmentStatusType = (status: string) => {
  const typeMap: Record<string, string> = {
    '已还清': 'success',
    '待还款': 'info',
    '逾期': 'danger'
  }
  return typeMap[status] || 'info'
}

// 贷款状态类型
const getLoanStatusType = (status: string) => {
  const typeMap: Record<string, string> = {
    '已结清': 'success',
    '正常还款': 'success',
    '逾期已还': 'warning'
  }
  return typeMap[status] || 'info'
}

// 还款状态类型
const getPaymentStatusType = (status: string) => {
  const typeMap: Record<string, string> = {
    '成功': 'success',
    '处理中': 'warning',
    '失败': 'danger'
  }
  return typeMap[status] || 'info'
}

// 提交证件审核
const submitDocumentVerification = () => {
  console.log('提交证件审核:', {
    case_id: props.caseData?.case_id,
    issues: documentIssues.value,
    remark: documentRemark.value
  })
  ElMessage.success('提交成功')
}

// 显示还款码
const showPaymentQRCode = (row: any) => {
  currentQRCode.value = props.fullData.installment_details?.payment_qr_code || ''
  qrCodeDialogVisible.value = true
}

// 手机号脱敏
const maskPhoneNumber = (phone: string) => {
  if (!phone) return '-'
  return '****'
}

// 显示手机号查看原因弹窗
const showPhoneReasonDialog = () => {
  phoneReasonDialogVisible.value = true
}

// 提交查看手机号原因
const submitPhoneReason = () => {
  if (!phoneReasonForm.value.reason) {
    ElMessage.warning('请选择查看原因')
    return
  }
  
  if (phoneReasonForm.value.reason === 'other' && !phoneReasonForm.value.otherReason.trim()) {
    ElMessage.warning('请填写具体原因')
    return
  }
  
  // 记录查看原因（可以发送到后端）
  const reasonText = phoneReasonForm.value.reason === 'facebook' ? 'Facebook查询' :
                     phoneReasonForm.value.reason === 'social_security' ? '社保查询' :
                     phoneReasonForm.value.otherReason
  
  console.log('查看手机号原因:', reasonText, '案件ID:', props.caseData.case_id)
  
  // 显示手机号
  phoneNumberRevealed.value = true
  phoneReasonDialogVisible.value = false
  
  // 重置表单
  phoneReasonForm.value = {
    reason: '',
    otherReason: ''
  }
  
  ElMessage.success('已记录查看原因')
}
</script>

<style scoped>
.case-detail-component {
  flex-shrink: 0;
  padding: 16px;
  overflow-y: auto;
  border-bottom: 1px solid #e4e7ed;
  max-height: 40vh;
}

/* 案件概览 */
.case-overview {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 8px;
  padding: 12px 16px;
  margin-bottom: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.overview-header {
  margin-bottom: 10px;
}

.overview-header h3 {
  margin: 0;
  color: #ffffff;
  font-size: 15px;
  font-weight: 600;
}

.overview-content {
  display: flex;
  gap: 12px;
}

.overview-block {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 6px;
  padding: 12px;
}

/* 客户信息块 - 压缩宽度 */
.overview-block:first-child {
  flex: 0 0 35%;
  min-width: 0;
}

/* 贷款信息块 - 扩展宽度 */
.overview-block:last-child {
  flex: 1;
  min-width: 0;
}

.block-items {
  display: flex;
  flex-wrap: wrap;
  gap: 12px 16px;
  overflow: hidden;
}

.overview-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
  flex: 0 0 auto;
}

.overview-item .label {
  font-size: 11px;
  color: #909399;
  font-weight: 500;
}

.overview-item .value {
  font-size: 13px;
  color: #303133;
  font-weight: 600;
  word-break: break-word;
  overflow-wrap: break-word;
}

.overview-item .highlight-amount {
  color: #f56c6c;
  font-size: 15px;
  font-weight: 700;
}

/* 日期状态颜色 */
.date-normal {
  color: #67c23a;
}

.date-due-today {
  color: #e6a23c;
  font-weight: 700;
}

.date-overdue-short {
  color: #e6a23c;
}

.date-overdue-long {
  color: #f56c6c;
}

.overdue-badge {
  color: #f56c6c;
  font-weight: 700;
}

.due-today-badge {
  color: #e6a23c;
  font-weight: 700;
}

.detail-tabs {
  height: 100%;
}

.detail-tabs :deep(.el-tabs__content) {
  height: calc(100% - 40px);
  overflow-y: auto;
  padding: 16px;
}

/* 客户信息分组样式 */
.customer-info-groups {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.info-group {
  background: #ffffff;
  border-radius: 4px;
}

.group-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 2px solid #409eff;
}

.info-descriptions {
  margin-bottom: 16px;
}

/* 影像资料 */
.documents-section {
  padding: 8px;
}

.document-images {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.image-item {
  text-align: center;
}

.image-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
  font-weight: 500;
}

.doc-image {
  width: 100%;
  height: 200px;
  border-radius: 8px;
  border: 1px solid #dcdfe6;
  overflow: hidden;
}

.image-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #909399;
  background: #f5f7fa;
}

.image-placeholder .el-icon {
  font-size: 48px;
  margin-bottom: 8px;
}

/* 证件审核 */
.document-verification {
  background: #f9fafb;
  padding: 16px;
  border-radius: 8px;
}

.document-verification h4 {
  margin: 0 0 16px 0;
  font-size: 14px;
  color: #303133;
}

.document-verification :deep(.el-checkbox) {
  margin-bottom: 8px;
  display: block;
}

/* 贷款信息 */
.installment-summary {
  margin-bottom: 16px;
  display: flex;
  gap: 12px;
}

.installment-table {
  margin-top: 12px;
}

.amount-highlight {
  color: #f56c6c;
  font-weight: 600;
  font-size: 16px;
}

/* 还款码弹窗 */
.qr-code-content {
  text-align: center;
  padding: 20px;
}

.qr-tip {
  margin-top: 16px;
  color: #909399;
  font-size: 14px;
}

/* 手机号显示 */
.masked-phone {
  color: #909399;
  cursor: pointer;
  user-select: none;
  display: inline-flex;
  align-items: center;
}

.masked-phone:hover {
  color: #25D366;
}

.revealed-phone {
  color: #303133;
  font-weight: 600;
}
</style>

