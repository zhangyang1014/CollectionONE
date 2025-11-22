# Infinity号段配置优化说明

## 📋 变更概述

根据用户需求，对Infinity外呼配置进行了优化：
1. **移除**"默认主叫号码"和"主叫号码池"字段
2. **添加**"号段起始"和"号段结束"字段，用于配置外显号码范围

## 🔄 主要变更

### 1. 前端字段变更

**移除字段：**
- `default_caller_number` - 默认主叫号码
- `caller_number_pool` - 主叫号码池（字符串数组）

**新增字段：**
- `caller_number_range_start` - 号段起始
- `caller_number_range_end` - 号段结束

### 2. 界面变更

#### 配置表单 (`InfinityCallConfigContent.vue` & `InfinityCallConfig.vue`)

```vue
<!-- 旧字段（已移除） -->
<el-form-item label="默认主叫号码">
  <el-input v-model="configForm.default_caller_number" />
</el-form-item>
<el-form-item label="主叫号码池">
  <!-- 标签列表 + 添加按钮 -->
</el-form-item>

<!-- 新字段 -->
<el-form-item label="号段起始">
  <el-input v-model="configForm.caller_number_range_start" placeholder="如：4001234000" />
  <el-tooltip content="外显号码的起始号码，系统将从这个号段范围内选择号码作为主叫显示" />
</el-form-item>
<el-form-item label="号段结束">
  <el-input v-model="configForm.caller_number_range_end" placeholder="如：4001234999" />
  <el-tooltip content="外显号码的结束号码，与起始号码组成可用的号码范围" />
</el-form-item>
```

#### 配置展示

```vue
<!-- 旧展示（已移除） -->
<el-descriptions-item label="默认主叫号码">
  {{ config.default_caller_number || '-' }}
</el-descriptions-item>
<el-descriptions-item label="主叫号码池">
  <el-tag v-for="number in config.caller_number_pool">{{ number }}</el-tag>
</el-descriptions-item>

<!-- 新展示 -->
<el-descriptions-item label="号段范围">
  <span v-if="config.caller_number_range_start && config.caller_number_range_end">
    {{ config.caller_number_range_start }} ~ {{ config.caller_number_range_end }}
  </span>
  <span v-else>-</span>
</el-descriptions-item>
```

### 3. 后端变更

#### 数据库 Schema (`add_infinity_call_tables.sql`)

```sql
-- 旧字段（已移除）
`default_caller_number` VARCHAR(50) NULL COMMENT '默认主叫号码',
`caller_number_pool` JSON NULL COMMENT '主叫号码池（JSON数组）',

-- 新字段
`caller_number_range_start` VARCHAR(50) NULL COMMENT '号段起始（如：4001234000）',
`caller_number_range_end` VARCHAR(50) NULL COMMENT '号段结束（如：4001234999）',
```

#### 数据模型 (`infinity_call_config.py`)

```python
# 旧字段（已移除）
default_caller_number = Column(String(50), comment="默认主叫号码")
caller_number_pool = Column(JSON, comment="主叫号码池（JSON数组）")

# 新字段
caller_number_range_start = Column(String(50), comment="号段起始")
caller_number_range_end = Column(String(50), comment="号段结束")
```

#### Pydantic Schema (`schemas/infinity.py`)

```python
# InfinityCallConfigBase
class InfinityCallConfigBase(BaseModel):
    api_url: str = Field(..., max_length=500, description="Infinity API地址")
    access_token: str = Field(..., max_length=500, description="API访问令牌")
    app_id: str = Field(..., max_length=100, description="应用ID（必填）")
    caller_number_range_start: Optional[str] = Field(None, max_length=50, description="号段起始")
    caller_number_range_end: Optional[str] = Field(None, max_length=50, description="号段结束")
    callback_url: Optional[str] = Field(None, max_length=500, description="Infinity回调地址")
    # ... 其他字段
```

#### TypeScript 类型 (`types/infinity.ts`)

```typescript
export interface InfinityCallConfig {
  id?: number
  tenant_id: number
  api_url: string
  access_token: string
  app_id: string // 必填
  caller_number_range_start?: string // 号段起始
  caller_number_range_end?: string   // 号段结束
  callback_url?: string
  // ... 其他字段
}
```

### 4. 移除相关函数

**前端移除函数：**
- `addCallerNumber()` - 添加主叫号码到号码池
- `removeCallerNumber(index)` - 从号码池移除主叫号码
- `watch(showCallerNumberInput)` - 监听号码输入框显示状态

**相关状态移除：**
- `showCallerNumberInput` - 是否显示号码输入框
- `newCallerNumber` - 新号码输入值
- `callerNumberInputRef` - 号码输入框引用

## 📊 号段范围说明

### 使用方式

管理员配置号段范围后，系统会：
1. 在发起外呼时，从号段范围内选择一个号码作为主叫号码（disnumber）
2. 号段范围格式示例：
   - 起始：`4001234000`
   - 结束：`4001234999`
   - 可用范围：4001234000 到 4001234999 之间的所有号码

### 号码选择策略

系统可以采用以下策略从号段中选择号码：
- **轮询（Round Robin）**：按顺序循环使用号段内的号码
- **随机（Random）**：从号段中随机选择号码
- **负载均衡**：根据号码使用频率动态选择

## 🗄️ 数据库迁移

### 迁移 SQL

```sql
-- 移除旧字段
ALTER TABLE `infinity_call_configs`
DROP COLUMN `default_caller_number`,
DROP COLUMN `caller_number_pool`;

-- 添加新字段
ALTER TABLE `infinity_call_configs`
ADD COLUMN `caller_number_range_start` VARCHAR(50) NULL COMMENT '号段起始（如：4001234000）' AFTER `app_id`,
ADD COLUMN `caller_number_range_end` VARCHAR(50) NULL COMMENT '号段结束（如：4001234999）' AFTER `caller_number_range_start`;
```

## ✅ 测试建议

1. **配置创建测试**
   - 创建配置时，输入号段起始和结束
   - 验证配置保存成功

2. **配置编辑测试**
   - 编辑现有配置的号段范围
   - 验证修改保存成功

3. **配置展示测试**
   - 查看配置详情，验证号段范围显示格式正确
   - 格式：`{起始号码} ~ {结束号码}`

4. **外呼测试**
   - 发起外呼，验证系统从号段中选择号码作为主叫
   - 检查通话记录中的主叫号码是否在配置的号段范围内

## 📝 注意事项

1. **号段格式**：建议使用纯数字格式，便于系统计算和选择
2. **号段合法性校验**：
   - 起始号码应小于结束号码
   - 号码长度应符合电话号码规范
   - 可以添加前端和后端校验逻辑
3. **号码可用性**：管理员应确保配置的号段是已购买且可用的

## 🔗 相关文件

### 前端文件
- `/frontend/src/views/channel-config/InfinityCallConfigContent.vue`
- `/frontend/src/views/channel-config/InfinityCallConfig.vue`
- `/frontend/src/types/infinity.ts`

### 后端文件
- `/backend/app/models/infinity_call_config.py`
- `/backend/app/schemas/infinity.py`
- `/backend/migrations/add_infinity_call_tables.sql`

---

**更新时间**：2024年
**更新人员**：AI Assistant

