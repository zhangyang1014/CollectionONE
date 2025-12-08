<template>
  <div class="detail-tenant-fields-view">
    <el-card>
      <template #header>
        <div class="card-header">
          <div class="header-info">
            <span class="title">案件详情甲方字段查看</span>
            <div class="version-info" v-if="currentVersion">
              <el-tag type="success" size="small">
                当前版本：{{ currentVersion.version_number }}
              </el-tag>
              <span class="info-text">
                上传时间：{{ formatDate(currentVersion.uploaded_at) }}
              </span>
              <span class="info-text">
                字段数：{{ currentVersion.total_fields }}个
              </span>
              <span class="info-text">
                分组数：{{ currentVersion.total_groups }}个
              </span>
            </div>
            <el-alert
              v-else
              title="当前甲方未上传字段配置，显示标准字段作为参考"
              type="info"
              :closable="false"
              show-icon
              style="margin-top: 10px"
            />
          </div>
          <el-space>
            <el-button @click="handleDownloadTemplate">
              <el-icon><Download /></el-icon>
              下载JSON模板
            </el-button>
            <el-button type="primary" @click="handleShowUpload">
              <el-icon><Upload /></el-icon>
              上传JSON文件
            </el-button>
            <el-button @click="handleShowVersions">
              <el-icon><List /></el-icon>
              版本管理
            </el-button>
            <el-button type="success" @click="loadData">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </el-space>
        </div>
      </template>

      <el-row :gutter="20">
        <!-- 左侧分组树 -->
        <el-col :span="5">
          <el-card shadow="never">
            <template #header>字段分组</template>
            <el-tree
              :data="groupTree"
              :props="{ label: 'label', children: 'children' }"
              node-key="key"
              :default-expand-all="true"
              :expand-on-click-node="false"
              highlight-current
              @node-click="handleGroupClick"
              class="field-group-tree"
            />
          </el-card>
        </el-col>

        <!-- 右侧字段表格 -->
        <el-col :span="19">
          <div v-loading="loading">
            <!-- 搜索栏 -->
            <el-row :gutter="10" style="margin-bottom: 15px">
              <el-col :span="8">
                <el-input
                  v-model="searchText"
                  placeholder="搜索字段名称或标识"
                  clearable
                  @input="handleSearch"
                >
                  <template #prefix>
                    <el-icon><Search /></el-icon>
                  </template>
                </el-input>
              </el-col>
              <el-col :span="6">
                <el-select v-model="filterType" placeholder="筛选字段类型" clearable>
                  <el-option label="全部类型" value="" />
                  <el-option label="String" value="String" />
                  <el-option label="Integer" value="Integer" />
                  <el-option label="Decimal" value="Decimal" />
                  <el-option label="Date" value="Date" />
                  <el-option label="Datetime" value="Datetime" />
                  <el-option label="Enum" value="Enum" />
                  <el-option label="Boolean" value="Boolean" />
                </el-select>
              </el-col>
              <el-col :span="6">
                <el-select v-model="filterRequired" placeholder="筛选必填" clearable>
                  <el-option label="全部" value="" />
                  <el-option label="必填" value="true" />
                  <el-option label="非必填" value="false" />
                </el-select>
              </el-col>
            </el-row>

            <!-- 字段表格 -->
            <el-table :data="paginatedFields" border style="width: 100%" class="block-table">
              <el-table-column type="index" label="序号" width="60" />
              <el-table-column 
                v-if="activeGroup === 'all'" 
                prop="group_name" 
                label="分组" 
                width="120" 
              />
              <el-table-column prop="field_name" label="字段名称" min-width="150" />
              <el-table-column prop="field_key" label="字段标识" min-width="180" />
              <el-table-column prop="field_type" label="字段类型" width="100" />
              <el-table-column label="枚举值" min-width="180" show-overflow-tooltip>
                <template #default="{ row }">
                  <span v-if="row.field_type === 'Enum' && row.enum_values">
                    {{ formatEnumValues(row.enum_values) }}
                  </span>
                  <span v-else style="color: #c0c4cc;">-</span>
                </template>
              </el-table-column>
              <el-table-column label="必填" width="70" align="center">
                <template #default="{ row }">
                  <el-tag :type="row.is_required ? 'danger' : 'info'" size="small">
                    {{ row.is_required ? '✓' : '-' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="sort_order" label="排序" width="70" align="center" />
              <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
            </el-table>

            <!-- 分页 -->
            <el-pagination
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :total="filteredFields.length"
              :page-sizes="[20, 50, 100]"
              layout="total, sizes, prev, pager, next, jumper"
              style="margin-top: 15px; justify-content: flex-end"
            />
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 上传弹窗 -->
    <el-dialog
      v-model="uploadDialogVisible"
      title="上传案件详情甲方字段配置"
      width="800px"
      :close-on-click-modal="false"
    >
      <div class="upload-dialog-content">
        <!-- 当前甲方信息 -->
        <el-descriptions title="当前甲方信息" :column="2" border>
          <el-descriptions-item label="甲方名称">{{ currentTenant?.name || '-' }}</el-descriptions-item>
          <el-descriptions-item label="甲方ID">{{ currentTenantId }}</el-descriptions-item>
          <el-descriptions-item label="配置场景">案件详情</el-descriptions-item>
          <el-descriptions-item label="当前版本">{{ currentVersion?.version_number || '无' }}</el-descriptions-item>
          <el-descriptions-item label="当前字段数">{{ currentVersion?.total_fields || 0 }}个</el-descriptions-item>
          <el-descriptions-item label="当前分组数">{{ currentVersion?.total_groups || 0 }}个</el-descriptions-item>
        </el-descriptions>

        <!-- 上传历史记录 -->
        <div class="upload-history" v-if="recentVersions.length > 0">
          <div class="history-header">
            <span>上传历史记录（最近5次）</span>
            <el-button link type="primary" @click="handleShowVersions">
              查看全部历史 <el-icon><ArrowRight /></el-icon>
            </el-button>
          </div>
          <div class="history-list">
            <div 
              v-for="version in recentVersions" 
              :key="version.id"
              class="history-item"
              :class="{ active: version.is_active }"
            >
              <div class="history-info">
                <div class="version-badge">
                  <el-tag 
                    :type="version.is_active ? 'success' : 'info'" 
                    size="small"
                  >
                    {{ version.is_active ? '● ' : '○ ' }}版本{{ version.version_number }}
                  </el-tag>
                  <el-tag v-if="version.is_active" type="success" size="small">当前使用</el-tag>
                </div>
                <div class="version-details">
                  <span>{{ formatDate(version.uploaded_at) }}</span>
                  <span>上传人：{{ version.uploaded_by || '-' }}</span>
                  <span>字段数：{{ version.total_fields }}</span>
                  <span>分组数：{{ version.total_groups }}</span>
                </div>
              </div>
              <div class="history-actions">
                <el-button link type="primary" size="small" @click="handleViewVersion(version)">查看</el-button>
                <el-button link type="primary" size="small" @click="handleDownloadVersion(version)">下载</el-button>
              </div>
            </div>
          </div>
        </div>

        <!-- 文件上传 -->
        <div class="upload-section">
          <div class="section-title">上传新文件</div>
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :on-change="handleFileChange"
            :before-upload="beforeUpload"
            :limit="1"
            accept=".json"
            drag
          >
            <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
            <div class="el-upload__text">
              点击或拖拽上传JSON文件
            </div>
            <template #tip>
              <div class="el-upload__tip">
                支持.json格式，文件大小不超过5MB<br>
                必须包含分组结构（groups数组）<br>
                上传后将创建新版本并自动设为当前使用版本
              </div>
            </template>
          </el-upload>

          <!-- 文件预览 -->
          <div v-if="uploadFile" class="file-preview">
            <el-alert title="文件已选择" type="success" :closable="false">
              <div>文件名：{{ uploadFile.name }}</div>
              <div>文件大小：{{ formatFileSize(uploadFile.size) }}</div>
              <div v-if="validationResult">
                <div v-if="validationResult.valid" style="color: #67c23a;">
                  ✓ 验证通过 - 字段数：{{ validationResult.totalFields }}，分组数：{{ validationResult.totalGroups }}
                </div>
                <div v-else style="color: #f56c6c;">
                  ✗ 验证失败：{{ validationResult.error }}
                </div>
              </div>
            </el-alert>
          </div>
        </div>

        <!-- 上传选项 -->
        <div class="upload-options">
          <el-checkbox v-model="uploadOptions.validate" disabled>上传前验证JSON格式和分组结构</el-checkbox>
          <el-checkbox v-model="uploadOptions.autoActivate" disabled>上传成功后自动设为当前使用版本</el-checkbox>
          <el-checkbox v-model="uploadOptions.showComparison">上传后显示与上一版本的对比</el-checkbox>
        </div>
      </div>

      <template #footer>
        <el-button @click="uploadDialogVisible = false">取消</el-button>
        <el-button 
          type="primary" 
          @click="handleConfirmUpload"
          :disabled="!uploadFile || (validationResult && !validationResult.valid)"
          :loading="uploading"
        >
          确认上传
        </el-button>
      </template>
    </el-dialog>

    <!-- 版本管理抽屉 -->
    <el-drawer
      v-model="versionDrawerVisible"
      title="版本管理 - 案件详情字段配置"
      size="600px"
      direction="rtl"
    >
      <div class="version-management">
        <!-- 搜索和筛选 -->
        <el-row :gutter="10" style="margin-bottom: 15px">
          <el-col :span="24">
            <el-input
              v-model="versionSearch"
              placeholder="搜索版本号或备注"
              clearable
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </el-col>
        </el-row>

        <!-- 版本列表 -->
        <div v-loading="versionsLoading" class="versions-list">
          <div 
            v-for="version in displayedVersions" 
            :key="version.id"
            class="version-card"
            :class="{ active: version.is_active }"
          >
            <div class="version-header">
              <div class="version-title">
                <el-tag 
                  :type="version.is_active ? 'success' : 'info'" 
                  size="large"
                >
                  {{ version.is_active ? '● ' : '○ ' }}版本{{ version.version_number }}
                </el-tag>
                <el-tag v-if="version.is_active" type="success" size="small">当前使用</el-tag>
              </div>
            </div>
            <div class="version-body">
              <div class="version-info-item">
                <span class="label">上传时间：</span>
                <span>{{ formatDate(version.uploaded_at) }}</span>
              </div>
              <div class="version-info-item">
                <span class="label">上传人：</span>
                <span>{{ version.uploaded_by || '-' }}</span>
              </div>
              <div class="version-info-item">
                <span class="label">字段数：</span>
                <span>{{ version.total_fields }}个</span>
                <span class="label" style="margin-left: 20px">分组数：</span>
                <span>{{ version.total_groups }}个</span>
              </div>
              <div v-if="version.description" class="version-info-item">
                <span class="label">说明：</span>
                <span>{{ version.description }}</span>
              </div>
            </div>
            <div class="version-actions">
              <el-button link type="primary" size="small" @click="handleViewVersion(version)">查看详情</el-button>
              <el-button link type="primary" size="small" @click="handleDownloadVersion(version)">下载JSON</el-button>
              <el-button 
                v-if="!version.is_active" 
                link 
                type="warning" 
                size="small" 
                @click="handleActivateVersion(version)"
              >
                设为当前版本
              </el-button>
              <el-button 
                link 
                type="info" 
                size="small" 
                @click="handleCompareVersion(version)"
              >
                对比
              </el-button>
            </div>
          </div>

          <!-- 分页 -->
          <el-pagination
            v-if="versions.length > 10"
            v-model:current-page="versionPage"
            :page-size="10"
            :total="versions.length"
            layout="prev, pager, next"
            style="margin-top: 15px; justify-content: center"
          />
        </div>
      </div>
    </el-drawer>

    <!-- 版本详情弹窗 -->
    <el-dialog
      v-model="versionDetailVisible"
      :title="`版本详情 - 版本${viewingVersion?.version_number || ''}`"
      width="900px"
    >
      <div v-if="viewingVersion" class="version-detail">
        <el-descriptions :column="3" border>
          <el-descriptions-item label="上传时间">{{ formatDate(viewingVersion.uploaded_at) }}</el-descriptions-item>
          <el-descriptions-item label="上传人">{{ viewingVersion.uploaded_by || '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="viewingVersion.is_active ? 'success' : 'info'">
              {{ viewingVersion.is_active ? '当前使用' : '历史版本' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="字段数">{{ viewingVersion.total_fields }}个</el-descriptions-item>
          <el-descriptions-item label="分组数">{{ viewingVersion.total_groups }}个</el-descriptions-item>
          <el-descriptions-item label="说明">{{ viewingVersion.description || '-' }}</el-descriptions-item>
        </el-descriptions>

        <!-- 分组和字段展示 -->
        <div style="margin-top: 20px">
          <div v-for="group in viewingVersionGroups" :key="group.group_key" class="group-section">
            <div class="group-title">
              <el-icon><Folder /></el-icon>
              <span>{{ group.group_name }}</span>
              <el-tag size="small" type="info">{{ group.fields.length }}个字段</el-tag>
            </div>
            <el-table :data="group.fields" border size="small">
              <el-table-column type="index" label="序号" width="60" />
              <el-table-column prop="field_name" label="字段名称" width="150" />
              <el-table-column prop="field_key" label="字段标识" width="180" />
              <el-table-column prop="field_type" label="类型" width="100" />
              <el-table-column label="必填" width="70" align="center">
                <template #default="{ row }">
                  {{ row.is_required ? '✓' : '-' }}
                </template>
              </el-table-column>
              <el-table-column prop="description" label="描述" show-overflow-tooltip />
            </el-table>
          </div>
        </div>
      </div>

      <template #footer>
        <el-button @click="versionDetailVisible = false">关闭</el-button>
        <el-button type="primary" @click="handleDownloadVersion(viewingVersion)">下载此版本</el-button>
      </template>
    </el-dialog>

    <!-- 版本对比弹窗 -->
    <el-dialog
      v-model="compareDialogVisible"
      title="版本对比"
      width="1000px"
      :close-on-click-modal="false"
    >
      <div class="version-compare">
        <!-- 版本选择 -->
        <el-row :gutter="20" style="margin-bottom: 20px">
          <el-col :span="12">
            <el-select v-model="compareVersion1" placeholder="选择基准版本" style="width: 100%">
              <el-option
                v-for="version in versions"
                :key="version.id"
                :label="`版本${version.version_number} (${formatDate(version.uploaded_at)})`"
                :value="version.id"
              />
            </el-select>
          </el-col>
          <el-col :span="12">
            <el-select v-model="compareVersion2" placeholder="选择对比版本" style="width: 100%">
              <el-option
                v-for="version in versions"
                :key="version.id"
                :label="`版本${version.version_number} (${formatDate(version.uploaded_at)})`"
                :value="version.id"
              />
            </el-select>
          </el-col>
        </el-row>

        <el-button 
          type="primary" 
          @click="handleDoCompare"
          :disabled="!compareVersion1 || !compareVersion2 || compareVersion1 === compareVersion2"
          :loading="comparing"
          style="margin-bottom: 20px"
        >
          开始对比
        </el-button>

        <!-- 对比结果 -->
        <div v-if="compareResult" class="compare-result">
          <!-- 变更摘要 -->
          <el-alert title="变更摘要" type="info" :closable="false">
            <ul>
              <li>新增字段：{{ compareResult.summary.added_fields }}个</li>
              <li>删除字段：{{ compareResult.summary.deleted_fields }}个</li>
              <li>修改字段：{{ compareResult.summary.modified_fields }}个</li>
              <li>分组变更：{{ compareResult.summary.group_changes }}个</li>
            </ul>
          </el-alert>

          <!-- 详细对比 -->
          <div style="margin-top: 20px">
            <!-- 新增字段 -->
            <div v-if="compareResult.added.length > 0" class="compare-section">
              <div class="section-title">
                <el-tag type="success">🟢 新增字段（{{ compareResult.added.length }}个）</el-tag>
              </div>
              <div v-for="item in compareResult.added" :key="item.field_key" class="compare-item added">
                <div class="field-header">
                  <strong>+ {{ item.field_name }} ({{ item.field_key }})</strong>
                  <el-tag size="small" type="info">分组：{{ item.group_name }}</el-tag>
                </div>
                <div class="field-details">
                  类型：{{ item.field_type }} | 必填：{{ item.is_required ? '是' : '否' }} | 排序：{{ item.sort_order }}
                </div>
              </div>
            </div>

            <!-- 删除字段 -->
            <div v-if="compareResult.deleted.length > 0" class="compare-section">
              <div class="section-title">
                <el-tag type="danger">🔴 删除字段（{{ compareResult.deleted.length }}个）</el-tag>
              </div>
              <div v-for="item in compareResult.deleted" :key="item.field_key" class="compare-item deleted">
                <div class="field-header">
                  <strong>- {{ item.field_name }} ({{ item.field_key }})</strong>
                  <el-tag size="small" type="info">分组：{{ item.group_name }}</el-tag>
                </div>
                <div class="field-details">
                  类型：{{ item.field_type }} | 必填：{{ item.is_required ? '是' : '否' }} | 排序：{{ item.sort_order }}
                </div>
              </div>
            </div>

            <!-- 修改字段 -->
            <div v-if="compareResult.modified.length > 0" class="compare-section">
              <div class="section-title">
                <el-tag type="warning">🟡 修改字段（{{ compareResult.modified.length }}个）</el-tag>
              </div>
              <div v-for="item in compareResult.modified" :key="item.field_key" class="compare-item modified">
                <div class="field-header">
                  <strong>≈ {{ item.field_name }} ({{ item.field_key }})</strong>
                  <el-tag size="small" type="info">分组：{{ item.group_name }}</el-tag>
                </div>
                <div class="field-changes">
                  <div v-for="change in item.changes" :key="change.property" class="change-item">
                    <span class="change-label">{{ change.property }}：</span>
                    <span class="old-value">- {{ change.old_value }}</span>
                    <span class="new-value">+ {{ change.new_value }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <el-button @click="compareDialogVisible = false">关闭</el-button>
        <el-button v-if="compareResult" type="primary">导出对比报告</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Download, Upload, List, Refresh, Search, 
  ArrowRight, UploadFilled, Folder 
} from '@element-plus/icons-vue'
import { useTenantStore } from '@/stores/tenant'
import {
  getDetailTenantFieldsJson,
  uploadDetailTenantFieldsJson,
  getDetailFieldVersions,
  getDetailFieldVersion,
  activateDetailFieldVersion,
  compareDetailFieldVersions,
  downloadDetailFieldTemplate,
  validateDetailFieldJson
} from '@/api/detailTenantFields'
import { getDetailFieldGroups } from '@/api/detailFieldGroup'
import { getCaseDetailStandardFields } from '@/api/field'

const tenantStore = useTenantStore()
const currentTenantId = computed(() => tenantStore.currentTenantId || 1)
const currentTenant = computed(() => tenantStore.currentTenant)

// 数据状态
const loading = ref(false)
const fields = ref<any[]>([])
const allGroups = ref<any[]>([])
const currentVersion = ref<any>(null)
const activeGroup = ref<string | number>('all')

// 搜索和筛选
const searchText = ref('')
const filterType = ref('')
const filterRequired = ref('')
const currentPage = ref(1)
const pageSize = ref(20)

// 上传相关
const uploadDialogVisible = ref(false)
const uploadFile = ref<any>(null)
const uploadRef = ref()
const uploading = ref(false)
const validationResult = ref<any>(null)
const uploadOptions = ref({
  validate: true,
  autoActivate: true,
  showComparison: false
})

// 版本管理
const versionDrawerVisible = ref(false)
const versions = ref<any[]>([])
const recentVersions = ref<any[]>([])
const versionsLoading = ref(false)
const versionSearch = ref('')
const versionPage = ref(1)

// 版本详情
const versionDetailVisible = ref(false)
const viewingVersion = ref<any>(null)
const viewingVersionGroups = ref<any[]>([])

// 版本对比
const compareDialogVisible = ref(false)
const compareVersion1 = ref<number | null>(null)
const compareVersion2 = ref<number | null>(null)
const comparing = ref(false)
const compareResult = ref<any>(null)

// 计算属性
const groupTree = computed(() => {
  const roots = allGroups.value.filter(g => !g.parent_id)
  const buildChildren = (parentId: number) => {
    return allGroups.value
      .filter(g => g.parent_id === parentId)
      .sort((a, b) => (a.sort_order || 0) - (b.sort_order || 0))
      .map(g => ({
        key: g.id,
        label: `${g.group_name} (${getGroupFieldCount(g.id)}个)`,
        groupKey: g.group_key,
        children: buildChildren(g.id)
      }))
  }
  
  const tree = roots
    .sort((a, b) => (a.sort_order || 0) - (b.sort_order || 0))
    .map(g => ({
      key: g.id,
      label: `${g.group_name} (${getGroupFieldCount(g.id)}个)`,
      groupKey: g.group_key,
      children: buildChildren(g.id)
    }))
  
  return [{ 
    key: 'all', 
    label: `全部字段 (${fields.value.length}个)`, 
    children: tree 
  }]
})

const getGroupFieldCount = (groupId: number) => {
  return fields.value.filter(f => f.field_group_id === groupId).length
}

const getGroupAndChildrenIds = (groupId: number): number[] => {
  const ids = [groupId]
  const children = allGroups.value.filter(g => g.parent_id === groupId)
  children.forEach(child => ids.push(...getGroupAndChildrenIds(child.id)))
  return ids
}

const filteredFields = computed(() => {
  let result = fields.value

  // 分组筛选
  if (activeGroup.value !== 'all') {
    const groupIds = getGroupAndChildrenIds(Number(activeGroup.value))
    result = result.filter(f => f.field_group_id && groupIds.includes(f.field_group_id))
  }

  // 搜索
  if (searchText.value) {
    const search = searchText.value.toLowerCase()
    result = result.filter(f => 
      f.field_name?.toLowerCase().includes(search) || 
      f.field_key?.toLowerCase().includes(search)
    )
  }

  // 类型筛选
  if (filterType.value) {
    result = result.filter(f => f.field_type === filterType.value)
  }

  // 必填筛选
  if (filterRequired.value !== '') {
    const required = filterRequired.value === 'true'
    result = result.filter(f => f.is_required === required)
  }

  return result
})

const paginatedFields = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredFields.value.slice(start, end)
})

const displayedVersions = computed(() => {
  let result = versions.value
  if (versionSearch.value) {
    const search = versionSearch.value.toLowerCase()
    result = result.filter(v => 
      v.version_number?.toString().includes(search) ||
      v.description?.toLowerCase().includes(search)
    )
  }
  const start = (versionPage.value - 1) * 10
  const end = start + 10
  return result.slice(start, end)
})

// 方法
const loadData = async () => {
  loading.value = true
  try {
    const res = await getDetailTenantFieldsJson(Number(currentTenantId.value))
    
    if (res?.groups && res.groups.length > 0) {
      // 显示甲方上传的字段
      parseFieldsFromGroups(res.groups)
      currentVersion.value = {
        version_number: res.version || '1',
        uploaded_at: res.fetched_at || new Date().toISOString(),
        total_fields: calculateTotalFields(res.groups),
        total_groups: res.groups.length
      }
    } else {
      // 兜底：使用标准字段
      const standardRes = await getCaseDetailStandardFields()
      const standardGroups = standardRes?.groups || []
      parseFieldsFromGroups(standardGroups)
      currentVersion.value = null
    }
  } catch (e: any) {
    ElMessage.error(e.message || '加载失败')
    // 兜底
    currentVersion.value = null
  } finally {
    loading.value = false
  }
}

const loadGroups = async () => {
  try {
    const data = await getDetailFieldGroups({ tenantId: Number(currentTenantId.value) })
    allGroups.value = Array.isArray(data) ? data : (data?.data || [])
  } catch (e) {
    console.error('加载分组失败：', e)
    allGroups.value = []
  }
}

const parseFieldsFromGroups = (groups: any[]) => {
  const parsed: any[] = []
  groups.forEach(group => {
    if (group.fields && Array.isArray(group.fields)) {
      group.fields.forEach((field: any) => {
        parsed.push({
          ...field,
          group_name: group.group_name,
          group_key: group.group_key,
          field_group_id: findGroupIdByKey(group.group_key)
        })
      })
    }
  })
  fields.value = parsed
}

const findGroupIdByKey = (groupKey: string) => {
  const group = allGroups.value.find(g => g.group_key === groupKey)
  return group?.id || null
}

const calculateTotalFields = (groups: any[]) => {
  return groups.reduce((sum, group) => sum + (group.fields?.length || 0), 0)
}

const handleGroupClick = (node: any) => {
  activeGroup.value = node.key
}

const handleSearch = () => {
  currentPage.value = 1
}

const formatEnumValues = (values: any) => {
  if (!values) return '-'
  const arr = Array.isArray(values) ? values : []
  if (arr.length === 0) return '-'
  if (arr.length <= 2) return arr.join(', ')
  return `${arr.slice(0, 2).join(', ')} 等${arr.length}个`
}

const formatDate = (date: string) => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}

const formatFileSize = (bytes: number) => {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(2) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(2) + ' MB'
}

// 下载模板
const handleDownloadTemplate = async () => {
  try {
    const blob = await downloadDetailFieldTemplate()
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = 'tenant_fields_detail_template.json'
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('模板下载成功')
  } catch (e: any) {
    ElMessage.error(e.message || '下载失败')
  }
}

// 显示上传弹窗
const handleShowUpload = async () => {
  uploadDialogVisible.value = true
  uploadFile.value = null
  validationResult.value = null
  await loadRecentVersions()
}

const loadRecentVersions = async () => {
  try {
    const data = await getDetailFieldVersions(Number(currentTenantId.value), { limit: 5 })
    recentVersions.value = Array.isArray(data) ? data : (data?.data || [])
  } catch (e) {
    console.error('加载历史版本失败：', e)
  }
}

// 文件变化
const handleFileChange = async (file: any) => {
  uploadFile.value = file.raw
  
  // 验证文件
  if (uploadOptions.value.validate) {
    try {
      const fileContent = await readFileAsText(file.raw)
      const jsonData = JSON.parse(fileContent)
      
      const result = await validateDetailFieldJson(jsonData)
      validationResult.value = result
    } catch (e: any) {
      validationResult.value = {
        valid: false,
        error: e.message || '文件格式错误'
      }
    }
  }
}

const beforeUpload = (file: any) => {
  const isJSON = file.type === 'application/json' || file.name.endsWith('.json')
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isJSON) {
    ElMessage.error('只能上传JSON文件')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('文件大小不能超过5MB')
    return false
  }
  return true
}

const readFileAsText = (file: File): Promise<string> => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = (e) => resolve(e.target?.result as string)
    reader.onerror = reject
    reader.readAsText(file)
  })
}

// 确认上传
const handleConfirmUpload = async () => {
  if (!uploadFile.value) {
    ElMessage.warning('请选择文件')
    return
  }

  if (validationResult.value && !validationResult.value.valid) {
    ElMessage.error('文件验证失败，请检查文件格式')
    return
  }

  try {
    uploading.value = true
    const fileContent = await readFileAsText(uploadFile.value)
    const jsonData = JSON.parse(fileContent)
    
    await uploadDetailTenantFieldsJson(Number(currentTenantId.value), jsonData)
    ElMessage.success('上传成功')
    uploadDialogVisible.value = false
    
    // 刷新数据
    await loadData()
    
    // 如果选择了显示对比
    if (uploadOptions.value.showComparison && recentVersions.value.length > 1) {
      compareDialogVisible.value = true
      compareVersion1.value = recentVersions.value[1].id
      compareVersion2.value = recentVersions.value[0].id
      await handleDoCompare()
    }
  } catch (e: any) {
    ElMessage.error(e.message || '上传失败')
  } finally {
    uploading.value = false
  }
}

// 版本管理
const handleShowVersions = async () => {
  versionDrawerVisible.value = true
  await loadVersions()
}

const loadVersions = async () => {
  versionsLoading.value = true
  try {
    const data = await getDetailFieldVersions(Number(currentTenantId.value))
    versions.value = Array.isArray(data) ? data : (data?.data || [])
  } catch (e: any) {
    ElMessage.error(e.message || '加载版本列表失败')
  } finally {
    versionsLoading.value = false
  }
}

// 查看版本
const handleViewVersion = async (version: any) => {
  try {
    const data = await getDetailFieldVersion(Number(currentTenantId.value), version.id)
    viewingVersion.value = version
    viewingVersionGroups.value = data?.groups || []
    versionDetailVisible.value = true
  } catch (e: any) {
    ElMessage.error(e.message || '加载版本详情失败')
  }
}

// 下载版本
const handleDownloadVersion = async (version: any) => {
  try {
    // 获取版本详情
    const data = await getDetailFieldVersion(Number(currentTenantId.value), version.id)
    const jsonContent = JSON.stringify(data, null, 2)
    
    // 创建下载链接
    const blob = new Blob([jsonContent], { type: 'application/json' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `tenant_fields_detail_v${version.version_number}_${currentTenantId.value}.json`
    link.click()
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('下载成功')
  } catch (e: any) {
    ElMessage.error(e.message || '下载失败')
  }
}

// 激活版本
const handleActivateVersion = async (version: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要将版本${version.version_number}设为当前使用版本吗？`,
      '提示',
      { type: 'warning' }
    )
    
    await activateDetailFieldVersion(Number(currentTenantId.value), version.id)
    ElMessage.success('版本切换成功')
    await loadVersions()
    await loadData()
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error(e.message || '切换失败')
    }
  }
}

// 对比版本
const handleCompareVersion = (version: any) => {
  compareDialogVisible.value = true
  compareVersion2.value = version.id
  
  // 自动选择上一个版本作为基准
  const index = versions.value.findIndex(v => v.id === version.id)
  if (index < versions.value.length - 1) {
    compareVersion1.value = versions.value[index + 1].id
  }
}

const handleDoCompare = async () => {
  if (!compareVersion1.value || !compareVersion2.value) {
    ElMessage.warning('请选择两个版本')
    return
  }

  if (compareVersion1.value === compareVersion2.value) {
    ElMessage.warning('请选择不同的版本')
    return
  }

  comparing.value = true
  try {
    const result = await compareDetailFieldVersions(
      Number(currentTenantId.value),
      compareVersion1.value,
      compareVersion2.value
    )
    compareResult.value = result
  } catch (e: any) {
    ElMessage.error(e.message || '对比失败')
  } finally {
    comparing.value = false
  }
}

onMounted(() => {
  loadGroups()
  loadData()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.header-info {
  flex: 1;
}

.header-info .title {
  font-size: 18px;
  font-weight: 600;
  display: block;
  margin-bottom: 10px;
}

.version-info {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-top: 10px;
}

.info-text {
  color: #606266;
  font-size: 14px;
}

.block-table :deep(.el-table__row) {
  height: 50px;
}

.block-table :deep(.el-table__cell) {
  padding: 10px 8px;
  font-size: 14px;
}

.field-group-tree :deep(.el-tree-node__content) {
  height: 36px;
}

/* 上传弹窗样式 */
.upload-dialog-content {
  max-height: 600px;
  overflow-y: auto;
}

.upload-history {
  margin: 20px 0;
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  font-weight: 600;
}

.history-list {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}

.history-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  border-bottom: 1px solid #ebeef5;
}

.history-item:last-child {
  border-bottom: none;
}

.history-item.active {
  background-color: #f0f9ff;
}

.history-info {
  flex: 1;
}

.version-badge {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 5px;
}

.version-details {
  display: flex;
  gap: 15px;
  font-size: 13px;
  color: #606266;
}

.history-actions {
  display: flex;
  gap: 5px;
}

.upload-section {
  margin: 20px 0;
}

.section-title {
  font-weight: 600;
  margin-bottom: 10px;
}

.file-preview {
  margin-top: 15px;
}

.upload-options {
  margin-top: 20px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

/* 版本管理样式 */
.version-management {
  padding: 0 10px;
}

.versions-list {
  max-height: calc(100vh - 200px);
  overflow-y: auto;
}

.version-card {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 15px;
  margin-bottom: 15px;
  transition: all 0.3s;
}

.version-card:hover {
  border-color: #409eff;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.version-card.active {
  background-color: #f0f9ff;
  border-color: #67c23a;
}

.version-header {
  margin-bottom: 10px;
}

.version-title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.version-body {
  margin: 10px 0;
}

.version-info-item {
  margin: 5px 0;
  font-size: 14px;
  color: #606266;
}

.version-info-item .label {
  font-weight: 600;
  color: #303133;
}

.version-actions {
  display: flex;
  gap: 10px;
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid #ebeef5;
}

/* 版本详情样式 */
.version-detail {
  max-height: 600px;
  overflow-y: auto;
}

.group-section {
  margin-bottom: 20px;
}

.group-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 10px;
  padding: 8px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

/* 版本对比样式 */
.version-compare {
  max-height: 600px;
  overflow-y: auto;
}

.compare-result {
  margin-top: 20px;
}

.compare-section {
  margin: 20px 0;
}

.compare-section .section-title {
  margin-bottom: 10px;
}

.compare-item {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 12px;
  margin-bottom: 10px;
}

.compare-item.added {
  background-color: #f0f9ff;
  border-color: #67c23a;
}

.compare-item.deleted {
  background-color: #fef0f0;
  border-color: #f56c6c;
}

.compare-item.modified {
  background-color: #fdf6ec;
  border-color: #e6a23c;
}

.field-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.field-details {
  font-size: 13px;
  color: #606266;
}

.field-changes {
  margin-top: 10px;
}

.change-item {
  margin: 5px 0;
  font-size: 13px;
}

.change-label {
  font-weight: 600;
  margin-right: 10px;
}

.old-value {
  color: #f56c6c;
  text-decoration: line-through;
  margin-right: 10px;
}

.new-value {
  color: #67c23a;
}
</style>
