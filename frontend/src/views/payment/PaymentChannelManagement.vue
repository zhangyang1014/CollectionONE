<template>
  <div class="payment-channel-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>还款渠道配置</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增渠道
          </el-button>
        </div>
      </template>

      <!-- 筛选器 -->
      <div class="filter-bar">
        <el-radio-group v-model="filterStatus" @change="loadChannels">
          <el-radio-button value="">全部</el-radio-button>
          <el-radio-button :value="true">已启用</el-radio-button>
          <el-radio-button :value="false">已禁用</el-radio-button>
        </el-radio-group>
      </div>

      <!-- 渠道列表 -->
      <el-table
        v-loading="loading"
        :data="channels"
        row-key="id"
        class="channel-table"
      >
        <el-table-column label="排序" width="60">
          <template #default="{ row }">
            <el-icon class="drag-handle"><Rank /></el-icon>
          </template>
        </el-table-column>

        <el-table-column label="渠道图标" width="80">
          <template #default="{ row }">
            <el-image
              v-if="row.channel_icon"
              :src="row.channel_icon"
              fit="contain"
              style="width: 48px; height: 48px"
            />
            <span v-else>-</span>
          </template>
        </el-table-column>

        <el-table-column prop="channel_name" label="支付名称" min-width="120" />

        <el-table-column label="支付类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getChannelTypeTag(row.channel_type)">
              {{ PAYMENT_TYPE_TEXT[row.channel_type] }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="service_provider" label="服务公司" min-width="120" />

        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-switch
              v-model="row.is_enabled"
              @change="handleToggle(row)"
            />
          </template>
        </el-table-column>

        <el-table-column label="排序权重" width="100">
          <template #default="{ row }">
            {{ row.sort_order }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="800px"
      @close="resetForm"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
      >
        <el-form-item label="支付名称" prop="channel_name">
          <el-input v-model="formData.channel_name" placeholder="如：GCash、BCA VA" />
        </el-form-item>

        <el-form-item label="支付图标" prop="channel_icon">
          <div class="icon-upload-container">
            <!-- 图片预览 -->
            <div v-if="formData.channel_icon" class="icon-preview">
              <img :src="formData.channel_icon" alt="图标预览" />
              <el-button
                type="danger"
                size="small"
                text
                @click="handleRemoveIcon"
                class="remove-icon-btn"
              >
                删除
              </el-button>
            </div>
            <!-- 上传组件 -->
            <el-upload
              v-if="!formData.channel_icon"
              :auto-upload="false"
              :show-file-list="false"
              accept="image/*"
              :on-change="handleIconChange"
              :before-upload="beforeIconUpload"
            >
              <el-button type="primary">
                <el-icon><Upload /></el-icon>
                上传图标
              </el-button>
              <template #tip>
                <el-text type="info" size="small">支持jpg/png格式，建议尺寸：64x64px，文件大小不超过2MB</el-text>
              </template>
            </el-upload>
            <!-- 或者输入URL -->
            <div class="icon-url-input" style="margin-top: 10px">
              <el-input
                v-model="formData.channel_icon"
                placeholder="或直接输入图标URL"
                clearable
              />
            </div>
          </div>
        </el-form-item>

        <el-form-item label="支付类型" prop="channel_type">
          <el-select v-model="formData.channel_type" placeholder="请选择">
            <el-option
              v-for="item in PAYMENT_TYPE_OPTIONS"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="服务公司" prop="service_provider">
          <el-input v-model="formData.service_provider" placeholder="如：Xendit、Midtrans" />
        </el-form-item>

        <el-form-item label="渠道描述">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="2"
            placeholder="简短说明该渠道的特点"
          />
        </el-form-item>

        <el-divider content-position="left">接口配置</el-divider>

        <el-form-item label="API地址" prop="api_url">
          <el-input v-model="formData.api_url" placeholder="https://api.example.com/payment/create" />
        </el-form-item>

        <el-form-item label="请求方法" prop="api_method">
          <el-radio-group v-model="formData.api_method">
            <el-radio value="POST">POST</el-radio>
            <el-radio value="GET">GET</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="认证方式" prop="auth_type">
          <el-select v-model="formData.auth_type" placeholder="请选择">
            <el-option
              v-for="item in AUTH_TYPE_OPTIONS"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="认证配置" prop="auth_config">
          <el-input
            v-model="authConfigStr"
            type="textarea"
            :rows="3"
            placeholder='{"api_key": "your_api_key"}'
          />
          <el-text type="info" size="small">JSON格式</el-text>
        </el-form-item>

        <el-form-item label="接口入参" prop="request_params">
          <el-input
            v-model="requestParamsStr"
            type="textarea"
            :rows="6"
            placeholder='{"loan_id": "{loan_id}", "amount": "{amount}"}'
          />
          <el-text type="info" size="small">
            支持占位符：{loan_id}、{case_id}、{installment_number}、{amount}等
          </el-text>
        </el-form-item>

        <el-divider content-position="left">状态配置</el-divider>

        <el-form-item label="是否启用">
          <el-switch v-model="formData.is_enabled" />
        </el-form-item>

        <el-form-item label="排序权重" prop="sort_order">
          <el-input-number v-model="formData.sort_order" :min="0" />
          <el-text type="info" size="small">越小越靠前</el-text>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { Plus, Rank, Upload } from '@element-plus/icons-vue'
import Sortable from 'sortablejs'
import {
  getPaymentChannels,
  createPaymentChannel,
  updatePaymentChannel,
  deletePaymentChannel,
  togglePaymentChannel,
  sortPaymentChannels
} from '@/api/payment'
import {
  PAYMENT_TYPE_OPTIONS,
  AUTH_TYPE_OPTIONS,
  PAYMENT_TYPE_TEXT,
  type PaymentChannel,
  type PaymentChannelForm
} from '@/types/payment'
import { useTenantStore } from '@/stores/tenant'

const tenantStore = useTenantStore()

// 状态
const loading = ref(false)
const channels = ref<PaymentChannel[]>([])
const filterStatus = ref<boolean | ''>('')
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const currentId = ref<number>()

// 表单
const formRef = ref<FormInstance>()
const formData = reactive<PaymentChannelForm>({
  channel_name: '',
  channel_icon: '',
  channel_type: 'VA',
  service_provider: '',
  description: '',
  api_url: '',
  api_method: 'POST',
  auth_type: 'API_KEY',
  auth_config: {},
  request_params: {},
  is_enabled: true,
  sort_order: 0
})

// JSON字符串
const authConfigStr = ref('')
const requestParamsStr = ref('')

// 表单验证规则
const formRules = {
  channel_name: [{ required: true, message: '请输入支付名称', trigger: 'blur' }],
  channel_type: [{ required: true, message: '请选择支付类型', trigger: 'change' }],
  api_url: [
    { required: true, message: '请输入API地址', trigger: 'blur' },
    { type: 'url', message: '请输入正确的URL格式', trigger: 'blur' }
  ],
  auth_type: [{ required: true, message: '请选择认证方式', trigger: 'change' }]
}

// 加载渠道列表
const loadChannels = async () => {
  loading.value = true
  try {
    const params: any = {
      party_id: tenantStore.currentTenant?.id || 1,
      page: 1,
      page_size: 100
    }
    if (filterStatus.value !== '') {
      params.is_enabled = filterStatus.value
    }

    const res = await getPaymentChannels(params)
    // 兼容不同的响应格式：request拦截器会返回res.data，所以这里res已经是data对象
    let channelList: PaymentChannel[] = []
    if (res && res.list) {
      channelList = res.list
    } else if (Array.isArray(res)) {
      // 如果直接返回数组
      channelList = res
    }
    
    // 转换is_enabled类型：后端返回0/1，前端需要true/false
    channels.value = channelList.map(channel => ({
      ...channel,
      is_enabled: channel.is_enabled === 1 || channel.is_enabled === true
    }))
    
    // 初始化拖拽排序
    if (channels.value.length > 0) {
      initSortable()
    }
  } catch (error) {
    ElMessage.error('加载渠道列表失败')
  } finally {
    loading.value = false
  }
}

// 初始化拖拽排序
const initSortable = () => {
  const table = document.querySelector('.channel-table .el-table__body-wrapper tbody') as HTMLElement
  if (table) {
    Sortable.create(table, {
      handle: '.drag-handle',
      animation: 150,
      onEnd: async (evt: any) => {
        const { oldIndex, newIndex } = evt
        if (oldIndex !== newIndex) {
          // 更新本地顺序
          const movedItem = channels.value.splice(oldIndex, 1)[0]
          channels.value.splice(newIndex, 0, movedItem)
          
          // 提交到后端
          await saveSortOrder()
        }
      }
    })
  }
}

// 保存排序
const saveSortOrder = async () => {
  try {
    const channelIds = channels.value.map(ch => ch.id)
    await sortPaymentChannels({
      party_id: tenantStore.currentTenant?.id || 1,
      channel_ids: channelIds
    })
    ElMessage.success('排序已保存')
  } catch (error) {
    ElMessage.error('保存排序失败')
    // 重新加载列表
    loadChannels()
  }
}

// 新增
const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增还款渠道'
  resetForm()
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row: PaymentChannel) => {
  isEdit.value = true
  currentId.value = row.id
  dialogTitle.value = '编辑还款渠道'
  
  Object.assign(formData, {
    channel_name: row.channel_name,
    channel_icon: row.channel_icon,
    channel_type: row.channel_type,
    service_provider: row.service_provider,
    description: row.description,
    api_url: row.api_url,
    api_method: row.api_method,
    auth_type: row.auth_type,
    auth_config: row.auth_config,
    request_params: row.request_params,
    is_enabled: row.is_enabled,
    sort_order: row.sort_order
  })
  
  authConfigStr.value = JSON.stringify(row.auth_config || {}, null, 2)
  requestParamsStr.value = JSON.stringify(row.request_params || {}, null, 2)
  
  dialogVisible.value = true
}

// 切换状态
const handleToggle = async (row: PaymentChannel) => {
  // v-model已经改变了row.is_enabled的值，所以这里保存的是新值
  const newStatus = row.is_enabled
  const oldStatus = !newStatus
  
  try {
    const res = await togglePaymentChannel(row.id)
    // 如果后端返回了更新后的数据，使用后端数据确保同步
    // 后端返回的is_enabled可能是0/1（Integer），需要转换为boolean
    if (res && typeof res === 'object') {
      let updatedStatus = res.is_enabled
      if (updatedStatus === undefined && res.data) {
        updatedStatus = res.data.is_enabled
      }
      if (updatedStatus !== undefined) {
        // 转换为boolean：1 -> true, 0 -> false
        row.is_enabled = updatedStatus === 1 || updatedStatus === true
      }
    }
    ElMessage.success(row.is_enabled ? '已启用' : '已禁用')
    // 重新加载列表以确保数据同步
    await loadChannels()
  } catch (error) {
    // 恢复原状态
    row.is_enabled = oldStatus
    ElMessage.error('操作失败')
  }
}

// 删除
const handleDelete = async (row: PaymentChannel) => {
  try {
    await ElMessageBox.confirm('确定要删除该渠道吗？', '提示', {
      type: 'warning'
    })
    
    await deletePaymentChannel(row.id)
    ElMessage.success('删除成功')
    loadChannels()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 处理图标上传
const handleIconChange = (file: any) => {
  const rawFile = file.raw || file
  if (!rawFile) return
  
  // 检查文件类型
  const isImage = rawFile.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return
  }
  
  // 检查文件大小（2MB）
  const isLt2M = rawFile.size / 1024 / 1024 < 2
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过2MB')
    return
  }
  
  // 读取文件并转换为base64
  const reader = new FileReader()
  reader.onload = (e) => {
    const result = e.target?.result
    if (typeof result === 'string') {
      formData.channel_icon = result
    }
  }
  reader.onerror = () => {
    ElMessage.error('图片读取失败')
  }
  reader.readAsDataURL(rawFile)
}

// 上传前验证
const beforeIconUpload = (file: File) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2
  
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过2MB')
    return false
  }
  return false // 返回false阻止自动上传，我们手动处理
}

// 删除图标
const handleRemoveIcon = () => {
  formData.channel_icon = ''
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    try {
      // 解析JSON
      try {
        formData.auth_config = JSON.parse(authConfigStr.value || '{}')
        formData.request_params = JSON.parse(requestParamsStr.value || '{}')
      } catch (e) {
        ElMessage.error('JSON格式错误')
        return
      }
      
      const data = {
        ...formData,
        party_id: tenantStore.currentTenant?.id || 1
      }
      
      if (isEdit.value && currentId.value) {
        await updatePaymentChannel(currentId.value, data)
        ElMessage.success('更新成功')
      } else {
        await createPaymentChannel(data)
        ElMessage.success('创建成功')
      }
      
      dialogVisible.value = false
      loadChannels()
    } catch (error) {
      ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
    }
  })
}

// 重置表单
const resetForm = () => {
  formRef.value?.resetFields()
  Object.assign(formData, {
    channel_name: '',
    channel_icon: '',
    channel_type: 'VA',
    service_provider: '',
    description: '',
    api_url: '',
    api_method: 'POST',
    auth_type: 'API_KEY',
    auth_config: {},
    request_params: {},
    is_enabled: true,
    sort_order: 0
  })
  authConfigStr.value = ''
  requestParamsStr.value = ''
}

// 获取渠道类型标签
const getChannelTypeTag = (type: string) => {
  const map: Record<string, string> = {
    VA: 'success',
    H5: 'primary',
    QR: 'warning'
  }
  return map[type] || ''
}

onMounted(() => {
  loadChannels()
})
</script>

<style scoped lang="scss">
.payment-channel-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filter-bar {
  margin-bottom: 20px;
}

.drag-handle {
  cursor: move;
  font-size: 20px;
  color: #999;
  &:hover {
    color: #409eff;
  }
}

.channel-table {
  :deep(.el-table__body-wrapper) {
    tbody {
      tr {
        &.sortable-ghost {
          opacity: 0.5;
          background: #f5f5f5;
        }
      }
    }
  }
}

.icon-upload-container {
  .icon-preview {
    position: relative;
    display: inline-block;
    margin-bottom: 10px;
    
    img {
      width: 64px;
      height: 64px;
      object-fit: contain;
      border: 1px solid #dcdfe6;
      border-radius: 4px;
      padding: 4px;
      background: #f5f7fa;
    }
    
    .remove-icon-btn {
      position: absolute;
      top: -8px;
      right: -8px;
      padding: 0;
      width: 20px;
      height: 20px;
      border-radius: 50%;
      background: #f56c6c;
      color: white;
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }
  
  .icon-url-input {
    margin-top: 10px;
  }
}
</style>

