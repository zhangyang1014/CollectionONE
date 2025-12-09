<template>
  <div class="face-compare-page">
    <el-card class="filter-card" shadow="never">
      <el-form :inline="true" :model="queryForm" label-width="80px">
        <el-form-item label="所属机构">
          <el-select
            v-model="queryForm.agencyId"
            placeholder="请选择机构"
            clearable
            filterable
            style="width: 200px"
            @change="handleAgencyChange"
          >
            <el-option
              v-for="item in agencies"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="小组群">
          <el-select
            v-model="queryForm.teamGroupId"
            placeholder="请选择小组群"
            clearable
            filterable
            style="width: 200px"
            @change="handleTeamGroupChange"
          >
            <el-option
              v-for="item in teamGroups"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="小组">
          <el-select
            v-model="queryForm.teamId"
            placeholder="请选择小组"
            clearable
            filterable
            style="width: 200px"
            @change="handleTeamChange"
          >
            <el-option
              v-for="item in teams"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="催员">
          <el-select
            v-model="queryForm.collectorId"
            placeholder="请选择催员"
            clearable
            filterable
            style="width: 220px"
          >
            <el-option
              v-for="item in collectors"
              :key="item.id"
              :label="`${item.account || item.username || ''} ${item.name || ''}`.trim()"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="比对结果">
          <el-select
            v-model="queryForm.compareResult"
            placeholder="请选择"
            clearable
            style="width: 160px"
          >
            <el-option label="匹配" value="match" />
            <el-option label="不匹配" value="mismatch" />
            <el-option label="未知/未比对" value="unknown" />
          </el-select>
        </el-form-item>
        <el-form-item label="是否禁用">
          <el-select
            v-model="queryForm.locked"
            placeholder="请选择"
            clearable
            style="width: 160px"
          >
            <el-option label="禁用" :value="true" />
            <el-option label="启用" :value="false" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">清空</el-button>
          <el-button type="warning" @click="handleLock(true)">禁用</el-button>
          <el-button type="success" @click="handleLock(false)">启用</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <el-table
        v-loading="loading"
        :data="tableData"
        border
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" />
        <el-table-column prop="collectorId" label="催员ID" width="100" show-overflow-tooltip />
        <el-table-column prop="organizationName" label="所属机构" width="140" show-overflow-tooltip />
        <el-table-column prop="teamGroupName" label="小组群" width="140" show-overflow-tooltip />
        <el-table-column prop="teamName" label="小组" width="140" show-overflow-tooltip />
        <el-table-column prop="compareResultLabel" label="比对结果" width="100" />
        <el-table-column label="首次照片" width="140">
          <template #default="{ row }">
            <PhotoCell :url="row.firstPhotoUrl" :face-id="row.faceId" />
          </template>
        </el-table-column>
        <el-table-column label="当日照片" width="140">
          <template #default="{ row }">
            <PhotoCell :url="row.todayPhotoUrl" :face-id="row.faceId" />
          </template>
        </el-table-column>
        <el-table-column label="昨日照片" width="140">
          <template #default="{ row }">
            <PhotoCell :url="row.yesterdayPhotoUrl" :face-id="row.faceId" />
          </template>
        </el-table-column>
        <el-table-column label="前日照片" width="140">
          <template #default="{ row }">
            <PhotoCell :url="row.beforeYesterdayPhotoUrl" :face-id="row.faceId" />
          </template>
        </el-table-column>
        <el-table-column label="是否禁用" width="100">
          <template #default="{ row }">
            <el-tag :type="row.locked ? 'danger' : 'success'">
              {{ row.locked ? '禁用' : '启用' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>

      <div class="table-footer">
        <el-pagination
          layout="prev, pager, next, sizes, total"
          :current-page="queryForm.pageNum"
          :page-size="queryForm.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { defineComponent, h, onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElImage, ElMessage, ElMessageBox } from 'element-plus'
import {
  fetchFaceCompareList,
  updateFaceLockStatus,
  type FaceCompareItem,
  type FaceCompareQuery,
} from '@/api/aiQuality'
import {
  getTenantAgencies,
  getAgencyTeamGroups,
  getTeamGroups,
  getTeams,
  getTeamGroupTeams,
  getCollectors,
} from '@/api/organization'
import { useTenantStore } from '@/stores/tenant'

const loading = ref(false)
const tableData = ref<FaceCompareItem[]>([])
const total = ref(0)
const selectedRows = ref<FaceCompareItem[]>([])
const route = useRoute()

const queryForm = reactive<
  FaceCompareQuery & {
    agencyId?: number
    teamGroupId?: number
    teamId?: number
    collectorId?: number
  }
>({
  agencyId: undefined,
  teamGroupId: undefined,
  teamId: undefined,
  collectorId: undefined,
  compareResult: '',
  locked: undefined,
  pageNum: 1,
  pageSize: 10,
})

const tenantStore = useTenantStore()
const agencies = ref<any[]>([])
const teamGroups = ref<any[]>([])
const teams = ref<any[]>([])
const collectors = ref<any[]>([])

const compareResultMap: Record<string, string> = {
  match: '匹配',
  mismatch: '不匹配',
  unknown: '-',
}

const normalizeRow = (item: FaceCompareItem) => {
  return {
    ...item,
    compareResultLabel: compareResultMap[item.compareResult || 'unknown'] || '-',
    organizationName:
      item.organizationName || (item as any).organization || (item as any).orgName || '',
    teamGroupName:
      (item as any).teamGroupName ||
      (item as any).team_group_name ||
      (item as any).groupName ||
      '',
    teamName:
      (item as any).teamName ||
      (item as any).team_name ||
      (item as any).group ||
      '',
    locked: typeof item.locked === 'boolean' ? item.locked : false,
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await fetchFaceCompareList({
      collectorId: queryForm.collectorId,
      teamId: queryForm.teamId,
      teamGroupId: queryForm.teamGroupId,
      agencyId: queryForm.agencyId,
      compareResult: queryForm.compareResult || undefined,
      locked: queryForm.locked,
      pageNum: queryForm.pageNum,
      pageSize: queryForm.pageSize,
    })
    tableData.value = (res.items || []).map(normalizeRow)
    total.value = res.total || tableData.value.length
  } catch (error: any) {
    console.error('[AI质检] 加载人脸比对列表失败', error)
    ElMessage.error(error?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryForm.pageNum = 1
  loadData()
}

const handleReset = () => {
  queryForm.agencyId = undefined
  queryForm.teamGroupId = undefined
  queryForm.teamId = undefined
  queryForm.collectorId = undefined
  queryForm.compareResult = ''
  queryForm.locked = undefined
  queryForm.pageNum = 1
  queryForm.pageSize = 10
  selectedRows.value = []
  loadData()
}

const handleSelectionChange = (rows: FaceCompareItem[]) => {
  selectedRows.value = rows
}

const handlePageChange = (page: number) => {
  queryForm.pageNum = page
  loadData()
}

const handleSizeChange = (size: number) => {
  queryForm.pageSize = size
  queryForm.pageNum = 1
  loadData()
}

const loadAgencies = async () => {
  try {
    agencies.value = []
    teamGroups.value = []
    teams.value = []
    collectors.value = []
    const tenantId = tenantStore.currentTenantId
    if (!tenantId) {
      return
    }
    const res = await getTenantAgencies(tenantId)
    agencies.value = Array.isArray(res) ? res : res?.data || []
  } catch (error) {
    console.warn('[AI质检] 加载机构失败', error)
  }
}

const loadTeamGroups = async (agencyId?: number) => {
  try {
    teamGroups.value = []
    teams.value = []
    collectors.value = []
    if (!agencyId) {
      return
    }
    const res = await getAgencyTeamGroups(agencyId)
    teamGroups.value = Array.isArray(res) ? res : res?.data || []
  } catch (error) {
    console.warn('[AI质检] 加载小组群失败', error)
  }
}

const loadTeams = async (teamGroupId?: number) => {
  try {
    teams.value = []
    collectors.value = []
    if (!teamGroupId) {
      return
    }
    const res = await getTeamGroupTeams(teamGroupId)
    teams.value = Array.isArray(res) ? res : res?.data || []
  } catch (error) {
    console.warn('[AI质检] 加载小组失败', error)
  }
}

const loadCollectors = async (teamId?: number, agencyId?: number) => {
  try {
    collectors.value = []
    const res = await getCollectors({
      team_id: teamId,
      agency_id: agencyId,
      limit: 200,
      skip: 0,
    } as any)
    const data = Array.isArray(res) ? res : res?.data || []
    collectors.value = data
  } catch (error) {
    console.warn('[AI质检] 加载催员失败', error)
  }
}

const handleAgencyChange = (agencyId?: number) => {
  queryForm.teamGroupId = undefined
  queryForm.teamId = undefined
  queryForm.collectorId = undefined
  loadTeamGroups(agencyId)
}

const handleTeamGroupChange = (teamGroupId?: number) => {
  queryForm.teamId = undefined
  queryForm.collectorId = undefined
  loadTeams(teamGroupId)
}

const handleTeamChange = (teamId?: number) => {
  queryForm.collectorId = undefined
  loadCollectors(teamId, queryForm.agencyId)
}

const applyRouteQuery = async () => {
  const { agencyId, teamGroupId, teamId, collectorId } = route.query
  const parsedAgencyId = agencyId ? Number(agencyId) : undefined
  const parsedTeamGroupId = teamGroupId ? Number(teamGroupId) : undefined
  const parsedTeamId = teamId ? Number(teamId) : undefined
  const parsedCollectorId = collectorId ? Number(collectorId) : undefined

  queryForm.agencyId = parsedAgencyId
  queryForm.teamGroupId = parsedTeamGroupId
  queryForm.teamId = parsedTeamId
  queryForm.collectorId = parsedCollectorId

  if (parsedAgencyId) {
    await loadTeamGroups(parsedAgencyId)
  } else {
    teamGroups.value = []
    teams.value = []
    collectors.value = []
  }

  if (parsedTeamGroupId) {
    await loadTeams(parsedTeamGroupId)
  }

  if (parsedTeamId || parsedCollectorId || parsedAgencyId) {
    await loadCollectors(parsedTeamId, parsedAgencyId)
  }
}

const handleLock = (lock: boolean) => {
  return async () => {
    if (!selectedRows.value.length) {
      ElMessage.warning('请选择需要操作的催员')
      return
    }
    const actionText = lock ? '禁用' : '启用'
    const confirmType = lock ? 'warning' : 'info'
    try {
      await ElMessageBox.confirm(
        `确认${actionText}选中的催员吗？`,
        '提示',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: confirmType,
        }
      )
      const requests = selectedRows.value.map((row) =>
        updateFaceLockStatus({
          collectorId: row.collectorId,
          lock,
        })
      )
      await Promise.all(requests)
      ElMessage.success(`${actionText}成功`)
      loadData()
    } catch (error: any) {
      if (error !== 'cancel') {
        console.error('[AI质检] 更新锁定状态失败', error)
        ElMessage.error(error?.message || '操作失败')
      }
    }
  }
}

onMounted(async () => {
  await loadAgencies()
  await applyRouteQuery()
  loadData()
})

watch(
  () => route.query,
  async () => {
    await applyRouteQuery()
    loadData()
  }
)

const PhotoCell = defineComponent({
  name: 'PhotoCell',
  props: {
    url: {
      type: String,
      default: '',
    },
    faceId: {
      type: String,
      default: '',
    },
  },
  setup(props) {
    const renderPlaceholder = () => h('span', '-')
    return () => {
      return h('div', { class: 'photo-cell' }, [
        props.url
          ? h(ElImage, {
              src: props.url,
              fit: 'cover',
              style: 'width: 70px; height: 70px; border-radius: 4px',
              previewSrcList: [props.url],
              lazy: true,
              onError: () => renderPlaceholder(),
            })
          : renderPlaceholder(),
        h('div', { class: 'face-id' }, props.faceId || '-'),
      ])
    }
  },
})
</script>

<style scoped>
.face-compare-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.filter-card {
  margin-bottom: 0;
}

.table-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.photo-cell {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #606266;
}

.photo-cell .face-id {
  line-height: 16px;
  text-align: center;
  word-break: break-all;
}
</style>
