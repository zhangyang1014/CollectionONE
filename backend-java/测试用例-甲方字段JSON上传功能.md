# 甲方字段JSON上传功能测试用例

## 测试环境准备

1. 确保Java后端服务已启动（端口8080）
2. 确保前端服务已启动（端口5173）
3. 确保已登录管理控台
4. 确保已选择甲方

## 测试用例

### 测试用例1：JSON格式校验 - 缺少必填字段

**测试步骤**：
1. 进入"字段配置" → "甲方字段查看"页面
2. 点击"上传JSON文件"按钮
3. 选择以下JSON文件（缺少version字段）：
```json
{
  "sync_time": "2025-11-25T10:30:00Z",
  "fields": []
}
```

**预期结果**：
- 显示校验失败
- 错误提示：缺少必填字段：version

---

### 测试用例2：JSON格式校验 - 字段类型无效

**测试步骤**：
1. 进入"字段配置" → "甲方字段查看"页面
2. 点击"上传JSON文件"按钮
3. 选择以下JSON文件（field_type无效）：
```json
{
  "version": "1.0",
  "sync_time": "2025-11-25T10:30:00Z",
  "fields": [
    {
      "field_key": "test_field",
      "field_name": "测试字段",
      "field_type": "InvalidType",
      "updated_at": "2025-11-25T10:30:00Z"
    }
  ]
}
```

**预期结果**：
- 显示校验失败
- 错误提示：字段类型无效：InvalidType，必须是String/Integer/Decimal/Date/Datetime/Boolean/Enum

---

### 测试用例3：JSON格式校验 - Enum类型缺少enum_values

**测试步骤**：
1. 进入"字段配置" → "甲方字段查看"页面
2. 点击"上传JSON文件"按钮
3. 选择以下JSON文件（Enum类型缺少enum_values）：
```json
{
  "version": "1.0",
  "sync_time": "2025-11-25T10:30:00Z",
  "fields": [
    {
      "field_key": "status",
      "field_name": "状态",
      "field_type": "Enum",
      "updated_at": "2025-11-25T10:30:00Z"
    }
  ]
}
```

**预期结果**：
- 显示校验失败
- 错误提示：Enum类型字段必须包含enum_values数组

---

### 测试用例4：JSON格式校验 - 格式正确

**测试步骤**：
1. 进入"字段配置" → "甲方字段查看"页面
2. 点击"上传JSON文件"按钮
3. 选择以下JSON文件（格式正确）：
```json
{
  "version": "1.0",
  "sync_time": "2025-11-25T10:30:00Z",
  "fields": [
    {
      "field_key": "user_id",
      "field_name": "用户ID",
      "field_type": "String",
      "field_group_id": 1,
      "is_required": true,
      "updated_at": "2025-11-25T10:30:00Z"
    },
    {
      "field_key": "case_status",
      "field_name": "案件状态",
      "field_type": "Enum",
      "field_group_id": 1,
      "is_required": true,
      "updated_at": "2025-11-25T10:30:00Z",
      "enum_values": [
        {
          "value": "PENDING",
          "label": "待还款"
        },
        {
          "value": "SETTLED",
          "label": "已结清"
        }
      ]
    }
  ]
}
```

**预期结果**：
- 显示校验通过
- 显示版本对比结果
- 如果当前没有版本，所有字段标记为"新增"
- 如果当前有版本，显示差异对比

---

### 测试用例5：版本对比 - 新增字段

**测试步骤**：
1. 确保当前已有版本（通过测试用例4上传）
2. 上传新版本JSON，包含新字段：
```json
{
  "version": "1.1",
  "sync_time": "2025-11-26T10:30:00Z",
  "fields": [
    {
      "field_key": "user_id",
      "field_name": "用户ID",
      "field_type": "String",
      "field_group_id": 1,
      "is_required": true,
      "updated_at": "2025-11-26T10:30:00Z"
    },
    {
      "field_key": "new_field",
      "field_name": "新字段",
      "field_type": "String",
      "field_group_id": 1,
      "is_required": false,
      "updated_at": "2025-11-26T10:30:00Z"
    }
  ]
}
```

**预期结果**：
- 显示版本对比结果
- 新增字段：new_field - 新字段 (String)
- 修改字段：无
- 删除字段：无（如果旧版本有case_status字段，则显示删除）

---

### 测试用例6：版本对比 - 修改字段

**测试步骤**：
1. 确保当前已有版本
2. 上传新版本JSON，修改字段名称：
```json
{
  "version": "1.2",
  "sync_time": "2025-11-27T10:30:00Z",
  "fields": [
    {
      "field_key": "user_id",
      "field_name": "客户ID",
      "field_type": "String",
      "field_group_id": 1,
      "is_required": true,
      "updated_at": "2025-11-27T10:30:00Z"
    }
  ]
}
```

**预期结果**：
- 显示版本对比结果
- 修改字段：user_id
  - field_name: "用户ID" → "客户ID"

---

### 测试用例7：版本对比 - 枚举值变化

**测试步骤**：
1. 确保当前已有版本（包含Enum字段）
2. 上传新版本JSON，修改枚举值：
```json
{
  "version": "1.3",
  "sync_time": "2025-11-28T10:30:00Z",
  "fields": [
    {
      "field_key": "case_status",
      "field_name": "案件状态",
      "field_type": "Enum",
      "field_group_id": 1,
      "is_required": true,
      "updated_at": "2025-11-28T10:30:00Z",
      "enum_values": [
        {
          "value": "PENDING",
          "label": "待处理"
        },
        {
          "value": "SETTLED",
          "label": "已结清"
        },
        {
          "value": "CANCELLED",
          "label": "已取消"
        }
      ]
    }
  ]
}
```

**预期结果**：
- 显示版本对比结果
- 修改字段：case_status
  - 枚举值变化：
    - 新增：已取消
    - 删除：待还款（如果旧版本有）
    - 修改：待处理（如果旧版本是"待还款"）

---

### 测试用例8：保存JSON文件

**测试步骤**：
1. 上传格式正确的JSON文件
2. 查看版本对比结果
3. 点击"确认保存"按钮

**预期结果**：
- 显示"保存成功"提示
- 弹窗关闭
- 字段列表自动刷新
- 当前版本更新为新上传的版本

---

### 测试用例9：取消保存

**测试步骤**：
1. 上传格式正确的JSON文件
2. 查看版本对比结果
3. 点击"取消"按钮

**预期结果**：
- 弹窗关闭
- 数据未保存
- 字段列表未更新

---

### 测试用例10：获取历史版本列表

**测试步骤**：
1. 确保已有多个版本（通过多次上传）
2. 调用API：`GET /api/v1/tenants/{tenantId}/fields-json/history`

**预期结果**：
- 返回历史版本列表（不包括当前版本）
- 只保留1个历史版本（最旧的已删除）

---

## 接口测试

### 接口1：校验JSON文件格式

```bash
curl -X POST "http://localhost:8080/api/v1/tenants/1/fields-json/validate" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -F "file=@test.json"
```

### 接口2：对比版本差异

```bash
curl -X POST "http://localhost:8080/api/v1/tenants/1/fields-json/compare" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "fieldsJson": {
      "version": "1.0",
      "sync_time": "2025-11-25T10:30:00Z",
      "fields": [...]
    }
  }'
```

### 接口3：上传并保存JSON文件

```bash
curl -X POST "http://localhost:8080/api/v1/tenants/1/fields-json/upload" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "version": "1.0",
    "sync_time": "2025-11-25T10:30:00Z",
    "fields": [...]
  }'
```

### 接口4：获取历史版本列表

```bash
curl -X GET "http://localhost:8080/api/v1/tenants/1/fields-json/history" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## 测试数据示例

### 完整的JSON文件示例

```json
{
  "version": "1.0",
  "sync_time": "2025-11-25T10:30:00Z",
  "fields": [
    {
      "field_key": "USER_ID",
      "field_name": "用户ID",
      "field_type": "String",
      "field_group_id": 1,
      "is_required": true,
      "updated_at": "2025-11-25T10:30:00Z",
      "description": "用户的唯一标识",
      "example_value": "12345678"
    },
    {
      "field_key": "CASE_STATUS",
      "field_name": "案件状态",
      "field_type": "Enum",
      "field_group_id": 1,
      "is_required": true,
      "updated_at": "2025-11-25T10:30:00Z",
      "enum_values": [
        {
          "value": "PENDING",
          "label": "待还款"
        },
        {
          "value": "PARTIAL",
          "label": "部分还款"
        },
        {
          "value": "SETTLED",
          "label": "正常结清"
        }
      ]
    }
  ]
}
```

---

## 验收标准

1. ✅ JSON格式校验准确率100%：所有格式错误的JSON都能准确识别并提示
2. ✅ 版本对比准确率100%：所有字段差异都能准确识别
3. ✅ 文件上传成功率≥99%：格式正确的JSON文件上传成功率≥99%
4. ✅ 版本管理正确：只保留当前版本和1个历史版本
5. ✅ 前端界面友好：错误提示清晰，对比结果展示直观

---

## 注意事项

1. 文件大小限制：最大10MB
2. 文件格式限制：只允许.json文件
3. 权限要求：SuperAdmin或TenantAdmin才能上传
4. 版本管理：上传新版本时，自动将当前版本标记为历史，删除最旧的历史版本

