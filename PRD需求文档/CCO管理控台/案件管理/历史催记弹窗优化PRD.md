# 历史催记弹窗优化PRD

## 一、产品概述

### 1. 产品背景

当前系统在控台端案件列表和催员IM端都提供了查看历史催记的功能。现有的历史催记弹窗包含了搜索框，用于搜索不同案件的催记。但在实际使用场景中，用户点击"查看催记"时，已经明确了要查看的是当前案件的催记，搜索框反而增加了操作复杂度。

### 2. 产品目标

- **简化操作流程**：去除不必要的搜索功能，让用户直接查看当前案件催记
- **提升用户体验**：在弹窗标题明确显示当前案件ID，让用户清楚了解正在查看哪个案件的催记
- **统一体验**：控台端和催员IM端保持一致的交互体验

### 3. 产品定位

优化现有的历史催记查看功能，使其更符合用户的实际使用场景，提升操作效率。

---

## 二、功能需求描述（PRD）

### 1. 核心功能（Core Features）

#### 1.1 历史催记弹窗优化

**功能描述**：
- 移除弹窗中的搜索框，用户无需再输入案件ID进行搜索
- 弹窗标题从"历史催记"改为"历史催记 - {案件ID}"，明确显示当前查看的案件
- 保留原有的筛选功能（触达人、触达渠道、状态、结果、日期范围）

**适用场景**：
- **控台端**：案件列表 → 点击"查看催记"按钮
- **催员IM端**：IM面板 → 点击"历史催记"按钮

---

### 2. 用户场景（User Scenarios）

**场景1：管理员在控台查看案件催记**
- **入口**：案件列表 → 点击某个案件的"查看催记"按钮
- **期望**：直接看到该案件的所有催记，标题显示"历史催记 - BTQ-202411-001"
- **操作**：可以通过筛选器按触达人、渠道等维度筛选催记

**场景2：催员在IM端查看当前案件催记**
- **入口**：IM面板右侧催记区域 → 点击"历史催记"按钮
- **期望**：直接看到当前聊天案件的所有催记，标题显示"历史催记 - BTQ-202411-001"
- **操作**：可以通过筛选器查看不同维度的催记信息

---

### 3. 业务流程（User Flow）

#### 3.1 控台端查看催记流程

```
管理员在案件列表中找到目标案件
    ↓
点击该案件的"查看催记"按钮
    ↓
打开历史催记弹窗（标题显示"历史催记 - {案件ID}"）
    ↓
自动加载该案件的所有催记记录
    ↓
（可选）使用筛选器按维度筛选催记
    ↓
查看催记详情
```

#### 3.2 催员IM端查看催记流程

```
催员在IM面板中与客户沟通
    ↓
点击右侧催记区域的"历史催记"按钮
    ↓
打开历史催记弹窗（标题显示"历史催记 - {当前案件ID}"）
    ↓
自动加载当前案件的所有催记记录
    ↓
（可选）使用筛选器按维度筛选催记
    ↓
查看催记详情
```

---

### 4. 界面设计（UI Design）

#### 4.1 历史催记弹窗布局

**优化前**：
```
┌─────────────────────────────────────────────────┐
│ 历史催记                                    [X] │
├─────────────────────────────────────────────────┤
│ 🔍 [搜索案件ID________________]                │  ← 移除这一行
│                                                 │
│ 筛选器：[触达人▾] [触达渠道▾] [状态▾] [结果▾]  │
│         [日期范围选择器]                        │
│                                                 │
│ ┌─────────────────────────────────────────────┐ │
│ │ 催记列表表格                                 │ │
│ └─────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────┘
```

**优化后**：
```
┌─────────────────────────────────────────────────┐
│ 历史催记 - BTQ-202411-001                  [X] │  ← 标题显示案件ID
├─────────────────────────────────────────────────┤
│ 筛选器：[触达人▾] [触达渠道▾] [状态▾] [结果▾]  │  ← 筛选器直接在顶部
│         [日期范围选择器]                        │
│                                                 │
│ ┌─────────────────────────────────────────────┐ │
│ │ 催记列表表格                                 │ │
│ └─────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────┘
```

#### 4.2 组件层级

```
el-dialog (历史催记弹窗)
├── title: "历史催记 - {案件ID}"
└── content
    ├── 筛选器区域 (.history-filters)
    │   ├── 触达人下拉选择器
    │   ├── 触达渠道下拉选择器
    │   ├── 状态下拉选择器
    │   ├── 结果下拉选择器
    │   └── 日期范围选择器
    │
    └── 催记列表区域 (.history-notes-list)
        └── el-table (催记表格)
            ├── 登记时间列
            ├── 案件ID列
            ├── 触达人列
            ├── 触达渠道列
            ├── 状态列
            ├── 结果列
            ├── 备注列
            └── 下次跟进时间列
```

---

### 5. 数据结构（Data Structure）

#### 5.1 Props（控台端 - CaseList.vue）

```typescript
// 历史催记弹窗相关
const showHistoryNotesDialog = ref(false)
const currentViewingCaseId = ref<string>('')  // 当前查看的案件ID

// 点击查看催记按钮时
const handleViewNotes = (row: any) => {
  currentViewingCaseId.value = row.case_code  // 保存案件ID
  showHistoryNotesDialog.value = true
  // 自动加载该案件的催记
  loadHistoryNotes(row.case_code)
}
```

#### 5.2 Props（催员IM端 - IMPanel.vue）

```typescript
// 历史催记弹窗相关
const showHistoryNotesDialog = ref(false)

// 当前案件ID（从props获取）
const currentCaseId = computed(() => {
  return props.caseData?.loan_id || ''
})

// 打开历史催记弹窗时
const openHistoryNotes = () => {
  showHistoryNotesDialog.value = true
  // 自动加载当前案件的催记
  loadHistoryNotes(currentCaseId.value)
}
```

#### 5.3 筛选器数据结构（保持不变）

```typescript
const historyFilters = ref({
  collector: '',      // 触达人
  channel: '',        // 触达渠道
  status: '',         // 状态
  result: '',         // 结果
  dateRange: null as [Date, Date] | null  // 日期范围
})
```

---

### 6. 接口设计（API Design）

#### 6.1 获取案件催记列表

**接口地址**：`GET /api/v1/cases/{case_id}/notes`

**请求参数**：
```typescript
{
  case_id: string              // 案件ID（必填，从URL路径获取）
  collector?: string           // 触达人筛选（可选）
  channel?: string             // 触达渠道筛选（可选）
  status?: string              // 状态筛选（可选）
  result?: string              // 结果筛选（可选）
  start_date?: string          // 开始日期（可选，格式：YYYY-MM-DD）
  end_date?: string            // 结束日期（可选，格式：YYYY-MM-DD）
  page?: number                // 页码（可选，默认1）
  page_size?: number           // 每页条数（可选，默认20）
}
```

**响应数据**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "items": [
      {
        "id": 1,
        "register_time": "2025-01-15 10:30:25",
        "case_id": "BTQ-202411-001",
        "collector": "张三",
        "channel": "WhatsApp",
        "status": "reachable",
        "result": "promise_repay",
        "remark": "客户承诺今天下午还款",
        "next_follow_up": "2025-01-15 14:00:00"
      }
    ],
    "total": 50,
    "page": 1,
    "page_size": 20
  }
}
```

---

### 7. 交互说明（Interaction Design）

#### 7.1 控台端交互

1. **打开弹窗**：
   - 用户点击案件列表中某行的"查看催记"按钮
   - 弹窗标题显示"历史催记 - {案件编号}"
   - 自动加载该案件的催记列表

2. **筛选操作**：
   - 用户选择筛选条件后，立即触发筛选
   - 筛选在前端进行（如果数据量不大）或调用API重新加载数据

3. **关闭弹窗**：
   - 用户点击X按钮或点击遮罩层关闭弹窗
   - 清空筛选条件（可选，根据产品需求决定）

#### 7.2 催员IM端交互

1. **打开弹窗**：
   - 催员点击右侧催记区域的"历史催记"按钮
   - 弹窗标题显示"历史催记 - {当前案件ID}"
   - 自动加载当前案件的催记列表

2. **筛选操作**：
   - 同控台端

3. **关闭弹窗**：
   - 同控台端

---

### 8. 非功能性需求（Non-Functional Requirements）

#### 8.1 性能要求

- 催记列表加载时间≤2秒（100条催记，P95）
- 筛选操作响应时间≤500ms（前端筛选）
- 弹窗打开动画流畅，无卡顿

#### 8.2 兼容性要求

- 支持Chrome、Safari、Edge最新两个版本
- 响应式适配（最小宽度1200px）

#### 8.3 安全要求

- 只能查看当前用户有权限访问的案件催记
- 控台端：管理员可查看所有案件催记
- 催员IM端：催员只能查看分配给自己的案件催记

---

## 三、技术实现方案（TRD）

### 1. 前端技术栈

- **框架**：Vue 3 (Composition API)
- **UI组件库**：Element Plus
- **状态管理**：组件内部ref/reactive
- **HTTP请求**：axios

### 2. 关键实现点

#### 2.1 控台端（CaseList.vue）

**修改点**：
1. 移除搜索框相关代码：
   - 移除`historySearchKeyword` ref
   - 移除`handleHistorySearch`方法
   - 移除`<div class="history-search">`整个区域
   
2. 修改弹窗标题：
   - 从静态`title="历史催记"`改为动态`:title="'历史催记 - ' + currentViewingCaseId"`
   
3. 修改筛选逻辑：
   - `filteredHistoryNotes` computed中移除按案件ID搜索的逻辑
   - 只保留按筛选条件过滤的逻辑

**代码示例**：
```vue
<!-- 历史催记对话框 -->
<el-dialog 
  v-model="showHistoryNotesDialog" 
  :title="`历史催记 - ${currentViewingCaseId}`"
  width="1200px" 
  top="5vh"
  class="history-notes-dialog"
>
  <div class="history-notes-content">
    <!-- 直接展示筛选器，移除搜索框 -->
    <div class="history-filters">
      <!-- 筛选器内容保持不变 -->
    </div>
    
    <!-- 催记列表 -->
    <div class="history-notes-list">
      <!-- 表格内容保持不变 -->
    </div>
  </div>
</el-dialog>

<script setup lang="ts">
// 当前查看的案件ID
const currentViewingCaseId = ref<string>('')

// 点击查看催记
const handleViewNotes = (row: any) => {
  currentViewingCaseId.value = row.case_code
  showHistoryNotesDialog.value = true
  // 加载催记
  loadHistoryNotes(row.case_code)
}

// 筛选后的催记列表（移除搜索关键词筛选）
const filteredHistoryNotes = computed(() => {
  let result = historyNotes.value

  // 移除：搜索案件ID的逻辑
  
  // 保留：筛选触达人
  if (historyFilters.value.collector) {
    result = result.filter(note => 
      note.collector === historyFilters.value.collector
    )
  }

  // ... 其他筛选条件保持不变
  
  return result
})
</script>
```

#### 2.2 催员IM端（IMPanel.vue）

**修改点**：
1. 移除搜索框相关代码（同控台端）
2. 修改弹窗标题：
   - 使用computed属性获取当前案件ID
   - 标题动态显示`:title="'历史催记 - ' + currentCaseId"`
3. 移除监听案件变化更新搜索关键词的逻辑

**代码示例**：
```vue
<!-- 历史催记对话框 -->
<el-dialog 
  v-model="showHistoryNotesDialog" 
  :title="`历史催记 - ${currentCaseId}`"
  width="1200px" 
  top="5vh"
  class="history-notes-dialog"
>
  <!-- 同控台端 -->
</el-dialog>

<script setup lang="ts">
// 当前案件ID（从props获取）
const currentCaseId = computed(() => {
  return props.caseData?.loan_id || ''
})

// 移除：watch监听案件变化更新搜索关键词
// 移除：historySearchKeyword ref
// 移除：handleHistorySearch方法

// 筛选后的催记列表（移除搜索关键词筛选）
const filteredHistoryNotes = computed(() => {
  let result = historyNotes.value

  // 移除：搜索案件ID的逻辑
  
  // 保留：筛选触达人
  if (historyFilters.value.collector) {
    result = result.filter(note => 
      note.collector === historyFilters.value.collector
    )
  }

  // ... 其他筛选条件保持不变
  
  return result
})
</script>
```

### 3. 样式调整

由于移除了搜索框，筛选器区域上移，需要调整样式：

```scss
.history-notes-content {
  .history-filters {
    margin-bottom: 20px; // 原来是16px，现在增加一点间距
    // 其他样式保持不变
  }
}
```

---

## 四、测试方案（Test Plan）

### 1. 功能测试

#### 1.1 控台端测试

- [ ] 点击"查看催记"按钮，弹窗正确打开
- [ ] 弹窗标题正确显示"历史催记 - {案件ID}"
- [ ] 催记列表正确显示当前案件的所有催记
- [ ] 筛选器功能正常（触达人、渠道、状态、结果、日期范围）
- [ ] 筛选后的结果正确
- [ ] 关闭弹窗功能正常
- [ ] 表格排序功能正常
- [ ] 表格分页功能正常（如果有）

#### 1.2 催员IM端测试

- [ ] 点击"历史催记"按钮，弹窗正确打开
- [ ] 弹窗标题正确显示"历史催记 - {当前案件ID}"
- [ ] 催记列表正确显示当前案件的所有催记
- [ ] 筛选器功能正常
- [ ] 切换案件后，点击"历史催记"显示正确的案件ID
- [ ] 关闭弹窗功能正常

### 2. 兼容性测试

- [ ] Chrome浏览器测试
- [ ] Safari浏览器测试
- [ ] Edge浏览器测试
- [ ] 不同分辨率测试（1920x1080, 1366x768）

### 3. 性能测试

- [ ] 加载100条催记，响应时间≤2秒
- [ ] 筛选操作响应时间≤500ms
- [ ] 弹窗打开动画流畅

---

## 五、上线计划（Release Plan）

### 1. 开发阶段

- **时间**：2天
- **任务**：
  - Day 1：修改控台端代码，测试验证
  - Day 2：修改催员IM端代码，测试验证

### 2. 测试阶段

- **时间**：1天
- **任务**：
  - 功能测试
  - 兼容性测试
  - 性能测试

### 3. 发布阶段

- **时间**：0.5天
- **任务**：
  - 代码合并
  - 部署上线
  - 验证功能

### 4. 上线检查清单

- [ ] 控台端历史催记弹窗功能正常
- [ ] 催员IM端历史催记弹窗功能正常
- [ ] 筛选器功能正常
- [ ] 性能指标达标
- [ ] 无严重bug

---

## 六、变更记录（Change Log）

### v1.0.0 - 2025-12-04

**新增功能**：
- ✅ 移除历史催记弹窗中的搜索框
- ✅ 弹窗标题显示当前案件ID
- ✅ 优化弹窗布局和交互

**影响范围**：
- 控台端：案件列表页面
- 催员IM端：IM面板组件

**向后兼容**：
- ✅ 筛选器功能保持不变
- ✅ 催记列表展示方式保持不变
- ✅ 数据接口保持不变

---

## 七、附录（Appendix）

### 1. 相关文档

- [案件列表功能PRD](./案件列表功能PRD.md)
- [催员IM端功能说明](../../CCO催员IM端/)

### 2. 设计稿

待补充

### 3. 技术文档

待补充

