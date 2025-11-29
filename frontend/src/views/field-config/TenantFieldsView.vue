<template>
  <div class="tenant-fields-view">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>甲方字段查看</span>
          <el-button 
            type="primary" 
            @click="handleUploadClick"
            :disabled="!currentTenantId"
          >
            <el-icon><Upload /></el-icon>
            上传JSON文件
          </el-button>
        </div>
      </template>

      <!-- 最新版本信息 -->
      <div class="version-info" v-if="lastFetchTime">
        <el-alert
          :title="`当前最新版本JSON的获取时间：${formatDateTime(lastFetchTime)}`"
          type="info"
          :closable="false"
          show-icon
          style="margin-bottom: 20px"
        />
      </div>

      <el-row :gutter="20">
        <el-col :span="6">
          <el-card shadow="never">
            <template #header>字段分组</template>
            <el-tree
              :data="treeData"
              :props="{ label: 'group_name', children: 'children' }"
              node-key="id"
              :default-expand-all="true"
              :expand-on-click-node="false"
              @node-click="handleGroupClick"
              highlight-current
              class="field-group-tree"
            />
          </el-card>
        </el-col>

        <el-col :span="18">
          <div class="table-header">
            <el-alert
              title="提示：此页面显示甲方通过API接口传入的字段JSON数据"
              type="info"
              :closable="false"
              show-icon
            />
          </div>
          <el-table 
            :data="fields" 
            row-key="id"
            border 
            style="width: 100%"
            v-loading="loading"
          >
            <el-table-column width="50" align="center">
              <template #default>
                <el-icon class="drag-handle" style="cursor: move;">
                  <Rank />
                </el-icon>
              </template>
            </el-table-column>
            <el-table-column prop="field_name" label="字段名称" />
            <el-table-column prop="field_key" label="字段标识" />
            <el-table-column prop="field_type" label="字段类型" width="100" />
            <el-table-column prop="enum_values" label="枚举值" width="200">
              <template #default="{ row }">
                <span v-if="row.field_type === 'Enum' && row.enum_values && row.enum_values.length > 0">
                  <el-tag 
                    v-for="(item, index) in row.enum_values.slice(0, 2)" 
                    :key="index"
                    size="small"
                    style="margin-right: 4px"
                  >
                    {{ item.standard_name || item.name || item.value }}
                  </el-tag>
                  <el-tag v-if="row.enum_values.length > 2" size="small" type="info">
                    +{{ row.enum_values.length - 2 }}
                  </el-tag>
                </span>
                <span v-else style="color: #909399;">-</span>
              </template>
            </el-table-column>
            <el-table-column prop="is_required" label="是否必填" width="100">
              <template #default="{ row }">
                <el-tag :type="row.is_required ? 'success' : 'info'">
                  {{ row.is_required ? '必填' : '非必填' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="sort_order" label="排序" width="80" />
          </el-table>
        </el-col>
      </el-row>
    </el-card>

    <!-- JSON上传弹窗 -->
    <el-dialog
      v-model="uploadDialogVisible"
      title="上传JSON文件"
      width="800px"
      :close-on-click-modal="false"
    >
      <div v-loading="uploadLoading">
        <!-- 文件选择 -->
        <div style="margin-bottom: 20px">
          <el-upload
            :auto-upload="false"
            :on-change="handleFileChange"
            accept=".json"
            :limit="1"
            :file-list="uploadFile ? [{ name: uploadFile.name }] : []"
          >
            <template #trigger>
              <el-button type="primary">选择文件</el-button>
            </template>
            <template #tip>
              <div class="el-upload__tip">只能上传.json文件，且不超过10MB</div>
            </template>
          </el-upload>
        </div>

        <!-- 校验结果 -->
        <div v-if="validateResult">
          <div v-if="!validateResult.valid" style="margin-bottom: 20px">
            <el-alert
              title="JSON格式校验失败"
              type="error"
              :closable="false"
              show-icon
            >
              <div style="margin-top: 10px">
                <div v-for="(error, index) in validateResult.errors" :key="index" style="margin-bottom: 5px">
                  <strong>{{ error.path }}:</strong> {{ error.message }}
                </div>
              </div>
            </el-alert>
          </div>
          
          <div v-if="validateResult.valid && compareResult" style="margin-bottom: 20px">
            <el-alert
              title="版本对比结果"
              type="info"
              :closable="false"
              show-icon
            >
              <div style="margin-top: 10px">
                <div style="margin-bottom: 10px">
                  <strong>统计信息：</strong>
                  <ul style="margin: 5px 0; padding-left: 20px">
                    <li>新增字段：{{ compareResult.addedFields?.length || 0 }}个</li>
                    <li>删除字段：{{ compareResult.deletedFields?.length || 0 }}个</li>
                    <li>修改字段：{{ compareResult.modifiedFields?.length || 0 }}个</li>
                  </ul>
                </div>
                
                <!-- 新增字段 -->
                <div v-if="compareResult.addedFields?.length > 0" style="margin-bottom: 10px">
                  <el-tag type="success" style="margin-bottom: 5px">新增字段（{{ compareResult.addedFields.length }}个）：</el-tag>
                  <div v-for="(field, index) in compareResult.addedFields" :key="index" style="margin-left: 20px; margin-bottom: 3px">
                    • {{ field.field_key }} - {{ field.field_name }} ({{ field.field_type }})
                  </div>
                </div>
                
                <!-- 删除字段 -->
                <div v-if="compareResult.deletedFields?.length > 0" style="margin-bottom: 10px">
                  <el-tag type="danger" style="margin-bottom: 5px">删除字段（{{ compareResult.deletedFields.length }}个）：</el-tag>
                  <div v-for="(field, index) in compareResult.deletedFields" :key="index" style="margin-left: 20px; margin-bottom: 3px; text-decoration: line-through">
                    • {{ field.field_key }} - {{ field.field_name }} (已删除)
                  </div>
                </div>
                
                <!-- 修改字段 -->
                <div v-if="compareResult.modifiedFields?.length > 0" style="margin-bottom: 10px">
                  <el-tag type="warning" style="margin-bottom: 5px">修改字段（{{ compareResult.modifiedFields.length }}个）：</el-tag>
                  <div v-for="(field, index) in compareResult.modifiedFields" :key="index" style="margin-left: 20px; margin-bottom: 5px">
                    <div><strong>{{ field.field_key }}</strong></div>
                    <div v-for="(change, key) in field.changes" :key="key" style="margin-left: 20px; margin-top: 3px">
                      <span v-if="key !== 'enum_values'">
                        - {{ key }}: "{{ change.old }}" → "{{ change.new_ }}"
                      </span>
                      <span v-else>
                        - 枚举值变化：
                        <span v-if="change.added?.length > 0">
                          <span style="color: green">+ 新增: {{ change.added.map((e: any) => e.label || e.value).join(', ') }}</span>
                        </span>
                        <span v-if="change.deleted?.length > 0">
                          <span style="color: red">- 删除: {{ change.deleted.map((e: any) => e.label || e.value).join(', ') }}</span>
                        </span>
                        <span v-if="change.modified?.length > 0">
                          <span style="color: orange">~ 修改: {{ change.modified.map((e: any) => `${e.old?.label || e.old?.value} → ${e.new_?.label || e.new_?.value}`).join(', ') }}</span>
                        </span>
                      </span>
                    </div>
                  </div>
                </div>
              </div>
            </el-alert>
          </div>
        </div>
      </div>

      <template #footer>
        <el-button @click="handleCancelUpload">取消</el-button>
        <el-button 
          type="primary" 
          @click="handleConfirmSave"
          :disabled="!validateResult?.valid || uploadLoading"
        >
          确认保存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { ElMessage, ElDialog, ElButton, ElUpload } from 'element-plus'
import { Rank, Upload } from '@element-plus/icons-vue'
import { getFieldGroups } from '@/api/field'
import { useTenantStore } from '@/stores/tenant'
import request from '@/utils/request'
import axios from 'axios'

const tenantStore = useTenantStore()
const currentTenantId = computed(() => tenantStore.currentTenantId)

const fields = ref<any[]>([])
const allGroups = ref<any[]>([])
const treeData = ref<any[]>([])
const currentGroupId = ref<number>()
const loading = ref(false)
const lastFetchTime = ref<string>('')

// JSON上传相关
const uploadDialogVisible = ref(false)
const uploadFile = ref<File | null>(null)
const validateResult = ref<any>(null)
const compareResult = ref<any>(null)
const uploadLoading = ref(false)
const fileInputRef = ref<HTMLInputElement | null>(null)

// 加载字段分组
const loadGroups = async () => {
  try {
    const res = await getFieldGroups()
    // API直接返回数组，不是{data: [...]}格式
    allGroups.value = Array.isArray(res) ? res : (res.data || [])
    treeData.value = buildTree(allGroups.value)
    
    // 自动选中第一个分组
    if (treeData.value.length > 0) {
      currentGroupId.value = treeData.value[0].id
      loadTenantFields()
    }
  } catch (error) {
    ElMessage.error('加载分组失败')
  }
}

// 构建树形数据
const buildTree = (groups: any[]) => {
  const map = new Map()
  const roots: any[] = []

  groups.forEach((group) => {
    map.set(group.id, { ...group, children: [] })
  })

  groups.forEach((group) => {
    const node = map.get(group.id)
    if (group.parent_id) {
      const parent = map.get(group.parent_id)
      if (parent) {
        parent.children.push(node)
      }
    } else {
      roots.push(node)
    }
  })

  return roots
}

// 加载甲方字段JSON数据
const loadTenantFields = async () => {
  if (!currentTenantId.value) {
    ElMessage.warning('请先选择甲方')
    return
  }

  loading.value = true
  try {
    // 调用API获取甲方字段JSON数据
    const response = await request({
      url: `/api/v1/tenants/${currentTenantId.value}/fields-json`,
      method: 'get',
    })
    
    // API返回格式：{fetched_at: "...", fields: [...]}
    // 响应拦截器已经处理了response.data，所以这里response就是实际数据
    const data = response || {}
    
    // 记录获取时间
    lastFetchTime.value = data.fetched_at || new Date().toISOString()
    
    // 解析JSON字段数据
    const fieldsData = Array.isArray(data.fields) ? data.fields : (Array.isArray(data) ? data : [])
    
    // 如果指定了分组ID，只显示该分组的字段
    if (currentGroupId.value) {
      fields.value = fieldsData.filter((field: any) => {
        return field.field_group_id === currentGroupId.value
      })
    } else {
      fields.value = fieldsData
    }
    
    // 如果没有字段，尝试从分组中查找
    if (fields.value.length === 0 && currentGroupId.value) {
      const group = findGroupById(treeData.value, currentGroupId.value)
      if (group && group.children) {
        const childGroupIds = group.children.map((child: any) => child.id)
        fields.value = fieldsData.filter((field: any) => {
          return childGroupIds.includes(field.field_group_id)
        })
      }
    }
    
  } catch (error: any) {
    console.error('加载甲方字段失败：', error)
    ElMessage.error(error.message || '加载甲方字段失败')
    fields.value = []
  } finally {
    loading.value = false
  }
}

// 查找分组
const findGroupById = (groups: any[], id: number): any => {
  for (const group of groups) {
    if (group.id === id) {
      return group
    }
    if (group.children) {
      const found = findGroupById(group.children, id)
      if (found) return found
    }
  }
  return null
}

// 处理分组点击
const handleGroupClick = (data: any) => {
  currentGroupId.value = data.id
  loadTenantFields()
}

// 格式化日期时间
const formatDateTime = (datetime: string) => {
  if (!datetime) return '-'
  try {
    const date = new Date(datetime)
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    const hours = String(date.getHours()).padStart(2, '0')
    const minutes = String(date.getMinutes()).padStart(2, '0')
    const seconds = String(date.getSeconds()).padStart(2, '0')
    return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
  } catch (e) {
    return datetime
  }
}

onMounted(() => {
  loadGroups()
  // 如果已有甲方ID，加载字段数据
  if (currentTenantId.value) {
    loadTenantFields()
  }
})

// 监听甲方变化
watch(currentTenantId, (newTenantId) => {
  if (newTenantId) {
    loadTenantFields()
  } else {
    fields.value = []
    lastFetchTime.value = ''
  }
})

// 处理上传按钮点击
const handleUploadClick = () => {
  if (!currentTenantId.value) {
    ElMessage.warning('请先选择甲方')
    return
  }
  uploadDialogVisible.value = true
  uploadFile.value = null
  validateResult.value = null
  compareResult.value = null
}

// 处理文件选择（el-upload组件的on-change事件）
const handleFileChange = (file: any) => {
  const rawFile = file.raw || file
  if (!rawFile) return
  
  // 检查文件格式
  if (!rawFile.name.endsWith('.json')) {
    ElMessage.error('只能上传.json格式的文件')
    return
  }
  
  // 检查文件大小（10MB）
  if (rawFile.size > 10 * 1024 * 1024) {
    ElMessage.error('文件大小不能超过10MB')
    return
  }
  
  uploadFile.value = rawFile
  validateAndCompare()
}

// 校验并对比
const validateAndCompare = async () => {
  if (!uploadFile.value || !currentTenantId.value) return
  
  uploadLoading.value = true
  try {
    // 1. 读取文件内容
    const content = await uploadFile.value.text()
    
    // 2. 前端基础格式校验
    let jsonData: any
    try {
      jsonData = JSON.parse(content)
    } catch (e: any) {
      ElMessage.error(`JSON格式错误：${e.message}`)
      uploadLoading.value = false
      return
    }
    
    // 3. 发送到后端进行详细校验
    const formData = new FormData()
    formData.append('file', uploadFile.value)
    
    const validateResponse = await axios.post(
      `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'}/api/v1/tenants/${currentTenantId.value}/fields-json/validate`,
      formData,
      {
        headers: {
          'Content-Type': 'multipart/form-data',
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
      }
    )
    
    if (validateResponse.data.code !== 200 || !validateResponse.data.data.valid) {
      validateResult.value = {
        valid: false,
        errors: validateResponse.data.data.errors || []
      }
      uploadLoading.value = false
      return
    }
    
    validateResult.value = validateResponse.data.data
    
    // 4. 版本对比
    const compareResponse = await request({
      url: `/api/v1/tenants/${currentTenantId.value}/fields-json/compare`,
      method: 'post',
      data: {
        fieldsJson: jsonData
      }
    })
    
    compareResult.value = compareResponse
    
  } catch (error: any) {
    console.error('校验或对比失败：', error)
    ElMessage.error(error.response?.data?.message || error.message || '校验失败')
  } finally {
    uploadLoading.value = false
  }
}

// 确认保存
const handleConfirmSave = async () => {
  if (!uploadFile.value || !currentTenantId.value || !validateResult.value?.valid) return
  
  uploadLoading.value = true
  try {
    // 读取文件内容
    const content = await uploadFile.value.text()
    const jsonData = JSON.parse(content)
    
    // 上传并保存
    await request({
      url: `/api/v1/tenants/${currentTenantId.value}/fields-json/upload`,
      method: 'post',
      data: {
        version: jsonData.version,
        syncTime: jsonData.sync_time,
        fields: jsonData.fields
      }
    })
    
    ElMessage.success('保存成功')
    uploadDialogVisible.value = false
    uploadFile.value = null
    validateResult.value = null
    compareResult.value = null
    
    // 重新加载字段数据
    loadTenantFields()
    
  } catch (error: any) {
    console.error('保存失败：', error)
    ElMessage.error(error.response?.data?.message || error.message || '保存失败')
  } finally {
    uploadLoading.value = false
  }
}

// 取消上传
const handleCancelUpload = () => {
  uploadDialogVisible.value = false
  uploadFile.value = null
  validateResult.value = null
  compareResult.value = null
  if (fileInputRef.value) {
    fileInputRef.value.value = ''
  }
}
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.version-info {
  margin-bottom: 20px;
}

.table-header {
  margin-bottom: 15px;
}

.field-group-tree {
  cursor: pointer;
  user-select: none;
}

.field-group-tree :deep(.el-tree-node__content) {
  height: 40px;
  cursor: pointer;
  padding: 0 10px;
  transition: all 0.3s;
}

.field-group-tree :deep(.el-tree-node__content:hover) {
  background-color: #f5f7fa;
}

.field-group-tree :deep(.el-tree-node.is-current > .el-tree-node__content) {
  background-color: #ecf5ff;
  color: #409eff;
  font-weight: 500;
}

.field-group-tree :deep(.el-tree-node__label) {
  cursor: pointer;
  flex: 1;
}

.drag-handle {
  color: #909399;
  font-size: 18px;
}
</style>

