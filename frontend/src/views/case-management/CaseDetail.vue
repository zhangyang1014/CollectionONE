<template>
  <div class="case-detail">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>案件详情</span>
          <el-button @click="$router.back()">返回</el-button>
        </div>
      </template>

      <el-tabs v-model="activeTab">
        <el-tab-pane label="基本信息" name="basic">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="案件编号">{{ caseData.case_id }}</el-descriptions-item>
            <el-descriptions-item label="贷款编号">{{ caseData.loan_id }}</el-descriptions-item>
            <el-descriptions-item label="用户ID">{{ caseData.user_id }}</el-descriptions-item>
            <el-descriptions-item label="案件状态">{{ caseData.case_status }}</el-descriptions-item>
          </el-descriptions>
        </el-tab-pane>

        <el-tab-pane label="标准字段" name="standard">
          <el-descriptions :column="2" border>
            <el-descriptions-item
              v-for="(value, key) in caseData.standard_fields"
              :key="key"
              :label="key"
            >
              {{ value }}
            </el-descriptions-item>
          </el-descriptions>
        </el-tab-pane>

        <el-tab-pane label="自定义字段" name="custom">
          <el-descriptions :column="2" border>
            <el-descriptions-item
              v-for="(value, key) in caseData.custom_fields"
              :key="key"
              :label="key"
            >
              {{ value }}
            </el-descriptions-item>
          </el-descriptions>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getCase } from '@/api/case'

const route = useRoute()
const caseId = Number(route.params.id)
const activeTab = ref('basic')
const caseData = ref({
  case_id: '',
  loan_id: '',
  user_id: '',
  case_status: '',
  standard_fields: {},
  custom_fields: {},
})

const loadCase = async () => {
  try {
    const res = await getCase(caseId)
    caseData.value = res.data
  } catch (error) {
    ElMessage.error('加载案件详情失败')
  }
}

onMounted(() => {
  loadCase()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>

