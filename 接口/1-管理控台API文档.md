# CCO 管理控台 API 文档

## 📋 接口概述

管理控台API用于SuperAdmin和TenantAdmin管理整个催收系统，包括组织架构、字段配置、案件管理、数据看板等功能。

**Base URL**: `http://localhost:8000/api/v1`

**认证方式**: JWT Token (Bearer Authentication)

**通用响应格式**:
```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

---

## 🔐 1. 认证模块

### 1.1 管理员登录

**接口**: `POST /admin/auth/login`

**描述**: SuperAdmin 或 TenantAdmin 登录

**请求参数**:
```json
{
  "loginId": "superadmin",
  "password": "123456"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": 1,
      "loginId": "superadmin",
      "username": "superadmin",
      "role": "SuperAdmin",
      "email": "admin@cco.com",
      "name": "超级管理员"
    }
  }
}
```

### 1.2 登出

**接口**: `POST /admin/auth/logout`

**描述**: 退出登录

**认证**: 需要

**响应示例**:
```json
{
  "code": 200,
  "message": "登出成功"
}
```

### 1.3 获取当前用户信息

**接口**: `GET /admin/auth/me`

**描述**: 获取当前登录用户信息

**认证**: 需要

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "loginId": "superadmin",
    "role": "SuperAdmin",
    "permissions": ["*"]
  }
}
```

---

## 🏢 2. 甲方管理

### 2.1 获取甲方列表

**接口**: `GET /tenants`

**描述**: 获取所有甲方列表（SuperAdmin）或当前甲方信息（TenantAdmin）

**认证**: 需要

**查询参数**:
- `page`: 页码（默认1）
- `size`: 每页数量（默认10）
- `is_active`: 是否启用（可选）

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "total": 2,
    "items": [
      {
        "id": 1,
        "tenant_code": "TENANT_A",
        "tenant_name": "甲方A公司",
        "tenant_name_en": "Tenant A Corp",
        "country_code": "PH",
        "timezone": 8,
        "currency_code": "PHP",
        "is_active": true,
        "created_at": "2025-11-20 10:00:00"
      }
    ]
  }
}
```

### 2.2 创建甲方

**接口**: `POST /tenants`

**描述**: 创建新甲方（仅SuperAdmin）

**认证**: 需要（SuperAdmin）

**请求参数**:
```json
{
  "tenant_code": "TENANT_A",
  "tenant_name": "甲方A公司",
  "tenant_name_en": "Tenant A Corp",
  "country_code": "PH",
  "timezone": 8,
  "currency_code": "PHP",
  "is_active": true
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "id": 1,
    "tenant_code": "TENANT_A",
    "created_at": "2025-11-20 10:00:00"
  }
}
```

### 2.3 更新甲方

**接口**: `PUT /tenants/{tenant_id}`

**描述**: 更新甲方信息

**认证**: 需要（SuperAdmin）

**响应示例**:
```json
{
  "code": 200,
  "message": "更新成功"
}
```

### 2.4 删除甲方

**接口**: `DELETE /tenants/{tenant_id}`

**描述**: 删除甲方（软删除）

**认证**: 需要（SuperAdmin）

---

## 🏛️ 3. 机构管理

### 3.1 获取机构列表

**接口**: `GET /agencies`

**描述**: 获取催收机构列表

**认证**: 需要

**查询参数**:
- `tenant_id`: 甲方ID（SuperAdmin可选，TenantAdmin自动筛选）
- `is_active`: 是否启用
- `page`: 页码
- `size`: 每页数量

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "total": 2,
    "items": [
      {
        "id": 1,
        "tenant_id": 1,
        "agency_code": "AGENCY_001",
        "agency_name": "催收机构1",
        "agency_name_en": "Collection Agency 1",
        "contact_person": "张三",
        "contact_phone": "+63-917-123-4567",
        "contact_email": "agency1@example.com",
        "address": "马尼拉市中心大楼",
        "timezone": 8,
        "agency_type": "real",
        "is_active": true,
        "created_at": "2025-11-20 10:05:00"
      }
    ]
  }
}
```

### 3.2 创建机构

**接口**: `POST /agencies`

**描述**: 创建催收机构

**认证**: 需要

**请求参数**:
```json
{
  "tenant_id": 1,
  "agency_code": "AGENCY_001",
  "agency_name": "催收机构1",
  "agency_name_en": "Collection Agency 1",
  "contact_person": "张三",
  "contact_phone": "+63-917-123-4567",
  "contact_email": "agency1@example.com",
  "address": "马尼拉市中心大楼",
  "timezone": 8,
  "agency_type": "real",
  "is_active": true
}
```

### 3.3 获取机构详情

**接口**: `GET /agencies/{agency_id}`

**描述**: 获取机构详细信息（包含小组数、催员数统计）

**认证**: 需要

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "agency_name": "催收机构1",
    "team_count": 5,
    "collector_count": 25,
    "total_cases": 500,
    "active_cases": 300
  }
}
```

### 3.4 更新机构

**接口**: `PUT /agencies/{agency_id}`

### 3.5 删除机构

**接口**: `DELETE /agencies/{agency_id}`

---

## 👥 4. 小组群管理

### 4.1 获取小组群列表

**接口**: `GET /team-groups`

**查询参数**:
- `agency_id`: 机构ID
- `is_active`: 是否启用

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "items": [
      {
        "id": 1,
        "agency_id": 1,
        "group_code": "GROUP_A",
        "group_name": "A组群",
        "group_name_en": "Group A",
        "spv_id": null,
        "spv_name": null,
        "team_count": 3,
        "is_active": true
      }
    ]
  }
}
```

### 4.2 创建小组群

**接口**: `POST /team-groups`

**请求参数**:
```json
{
  "tenant_id": 1,
  "agency_id": 1,
  "group_code": "GROUP_A",
  "group_name": "A组群",
  "group_name_en": "Group A",
  "description": "负责M1和M2队列",
  "sort_order": 1,
  "is_active": true
}
```

### 4.3 设置小组群长（SPV）

**接口**: `PUT /team-groups/{group_id}/spv`

**请求参数**:
```json
{
  "spv_id": 1
}
```

### 4.4 获取小组群详情

**接口**: `GET /team-groups/{group_id}`

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "group_name": "A组群",
    "spv": {
      "id": 1,
      "collector_name": "王小明",
      "mobile": "+63-917-123-4567"
    },
    "teams": [
      {
        "id": 1,
        "team_name": "M1催收小组",
        "collector_count": 10
      }
    ]
  }
}
```

---

## 🎯 5. 小组管理

### 5.1 获取小组列表

**接口**: `GET /teams`

**查询参数**:
- `agency_id`: 机构ID
- `team_group_id`: 小组群ID
- `queue_id`: 队列ID
- `is_active`: 是否启用

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "items": [
      {
        "id": 1,
        "team_code": "TEAM_M1",
        "team_name": "M1催收小组",
        "team_group_name": "A组群",
        "queue_name": "M1队列",
        "team_leader_name": "王小明",
        "collector_count": 10,
        "current_case_count": 450,
        "max_case_count": 5000,
        "is_active": true
      }
    ]
  }
}
```

### 5.2 创建小组

**接口**: `POST /teams`

**请求参数**:
```json
{
  "tenant_id": 1,
  "agency_id": 1,
  "team_group_id": 1,
  "queue_id": 1,
  "team_code": "TEAM_M1",
  "team_name": "M1催收小组",
  "team_name_en": "M1 Collection Team",
  "team_type": "电催组",
  "description": "专门负责M1队列（1-30天）",
  "max_case_count": 5000,
  "is_active": true
}
```

### 5.3 设置组长

**接口**: `PUT /teams/{team_id}/leader`

**请求参数**:
```json
{
  "team_leader_id": 1
}
```

### 5.4 获取小组详情

**接口**: `GET /teams/{team_id}`

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "team_name": "M1催收小组",
    "team_leader": {
      "id": 1,
      "collector_name": "王小明",
      "mobile": "+63-917-123-4567"
    },
    "queue": {
      "id": 1,
      "queue_name": "M1队列",
      "overdue_days_start": 1,
      "overdue_days_end": 30
    },
    "collectors": [
      {
        "id": 1,
        "collector_name": "王小明",
        "current_case_count": 45,
        "max_case_count": 150
      }
    ],
    "statistics": {
      "total_collectors": 10,
      "total_cases": 450,
      "resolved_cases": 50,
      "resolution_rate": 0.11
    }
  }
}
```

---

## 👤 6. 催员管理

### 6.1 获取催员列表

**接口**: `GET /collectors`

**查询参数**:
- `team_id`: 小组ID
- `agency_id`: 机构ID
- `status`: 状态（active/休假/离职）
- `page`: 页码
- `size`: 每页数量

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "total": 3,
    "items": [
      {
        "id": 1,
        "collector_code": "COL_001",
        "collector_name": "王小明",
        "login_id": "collector001",
        "mobile": "+63-917-123-4567",
        "email": "collector001@example.com",
        "team_name": "M1催收小组",
        "collector_level": "高级",
        "current_case_count": 45,
        "max_case_count": 150,
        "performance_score": 4.5,
        "status": "active",
        "last_login_at": "2025-11-20 09:30:00"
      }
    ]
  }
}
```

### 6.2 创建催员

**接口**: `POST /collectors`

**请求参数**:
```json
{
  "tenant_id": 1,
  "agency_id": 1,
  "team_id": 1,
  "collector_code": "COL_001",
  "collector_name": "王小明",
  "login_id": "collector001",
  "password": "123456",
  "mobile": "+63-917-123-4567",
  "email": "collector001@example.com",
  "employee_no": "EMP001",
  "collector_level": "高级",
  "max_case_count": 150,
  "specialties": ["高额案件", "法务处理"],
  "status": "active",
  "is_active": true
}
```

### 6.3 更新催员信息

**接口**: `PUT /collectors/{collector_id}`

### 6.4 获取催员详情

**接口**: `GET /collectors/{collector_id}`

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "collector_name": "王小明",
    "team_name": "M1催收小组",
    "current_cases": 45,
    "performance": {
      "total_cases": 200,
      "resolved_cases": 50,
      "resolution_rate": 0.25,
      "total_collected_amount": 500000.00,
      "contact_rate": 0.85
    },
    "recent_activities": [
      {
        "date": "2025-11-20",
        "contact_count": 30,
        "ptp_count": 5
      }
    ]
  }
}
```

### 6.5 重置催员密码

**接口**: `POST /collectors/{collector_id}/reset-password`

**请求参数**:
```json
{
  "new_password": "new123456"
}
```

### 6.6 禁用/启用催员

**接口**: `PUT /collectors/{collector_id}/status`

**请求参数**:
```json
{
  "is_active": false,
  "reason": "离职"
}
```

---

## 📋 7. 案件队列管理

### 7.1 获取队列列表

**接口**: `GET /queues`

**查询参数**:
- `tenant_id`: 甲方ID
- `is_active`: 是否启用

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "items": [
      {
        "id": 1,
        "queue_code": "M1",
        "queue_name": "M1队列（1-30天）",
        "overdue_days_start": 1,
        "overdue_days_end": 30,
        "case_count": 150,
        "assigned_count": 120,
        "unassigned_count": 30,
        "sort_order": 1,
        "is_active": true
      }
    ]
  }
}
```

### 7.2 创建队列

**接口**: `POST /queues`

**请求参数**:
```json
{
  "tenant_id": 1,
  "queue_code": "M1",
  "queue_name": "M1队列（1-30天）",
  "queue_name_en": "M1 Queue (1-30 days)",
  "queue_description": "逾期1-30天的案件",
  "overdue_days_start": 1,
  "overdue_days_end": 30,
  "sort_order": 1,
  "is_active": true
}
```

### 7.3 获取队列配置

**接口**: `GET /queues/{queue_id}/config`

**描述**: 获取队列的字段配置

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "queue_id": 1,
    "queue_name": "M1队列",
    "field_configs": [
      {
        "field_key": "case_code",
        "is_visible": true,
        "is_required": true,
        "sort_order": 1
      }
    ]
  }
}
```

---

## 📦 8. 案件管理

### 8.1 获取案件列表

**接口**: `GET /cases`

**查询参数**:
- `tenant_id`: 甲方ID
- `queue_id`: 队列ID
- `agency_id`: 机构ID
- `team_id`: 小组ID
- `collector_id`: 催员ID
- `case_status`: 案件状态
- `overdue_days_min`: 最小逾期天数
- `overdue_days_max`: 最大逾期天数
- `search`: 搜索关键词（案件编号、姓名、手机号）
- `page`: 页码
- `size`: 每页数量

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "total": 10,
    "page": 1,
    "size": 10,
    "items": [
      {
        "id": 1,
        "case_code": "CASE_001",
        "user_name": "陈大明",
        "mobile": "+63-917-111-1111",
        "overdue_days": 15,
        "loan_amount": 5000.00,
        "outstanding_amount": 4000.00,
        "case_status": "pending_repayment",
        "queue_name": "M1队列",
        "collector_name": "王小明",
        "last_contact_at": "2025-11-20 10:00:00",
        "assigned_at": "2025-11-18 09:00:00"
      }
    ]
  }
}
```

### 8.2 导入案件

**接口**: `POST /cases/import`

**描述**: 批量导入案件（通常从甲方核心系统推送）

**请求参数**:
```json
{
  "tenant_id": 1,
  "cases": [
    {
      "case_code": "CASE_001",
      "user_id": "USER_001",
      "user_name": "陈大明",
      "mobile": "+63-917-111-1111",
      "overdue_days": 15,
      "loan_amount": 5000.00,
      "repaid_amount": 1000.00,
      "outstanding_amount": 4000.00,
      "due_date": "2025-11-05T00:00:00",
      "case_status": "pending_repayment"
    }
  ]
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "导入成功",
  "data": {
    "total": 10,
    "success": 10,
    "failed": 0,
    "errors": []
  }
}
```

### 8.3 获取案件详情

**接口**: `GET /cases/{case_id}`

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "case_code": "CASE_001",
    "user_info": {
      "user_id": "USER_001",
      "user_name": "陈大明",
      "mobile": "+63-917-111-1111",
      "id_number": "ID123456",
      "address": "马尼拉市..."
    },
    "loan_info": {
      "loan_amount": 5000.00,
      "repaid_amount": 1000.00,
      "outstanding_amount": 4000.00,
      "overdue_days": 15,
      "due_date": "2025-11-05"
    },
    "assignment_info": {
      "queue_name": "M1队列",
      "agency_name": "催收机构1",
      "team_name": "M1催收小组",
      "collector_name": "王小明",
      "assigned_at": "2025-11-18 09:00:00"
    },
    "communication_history": [
      {
        "id": 1,
        "contact_method": "phone",
        "contact_result": "接通",
        "communication_content": "客户表示下周一还款",
        "collector_name": "王小明",
        "created_at": "2025-11-20 10:00:00"
      }
    ],
    "ptp_records": [
      {
        "id": 1,
        "promise_amount": 2000.00,
        "promise_date": "2025-11-25",
        "promise_status": "pending",
        "created_at": "2025-11-20 10:05:00"
      }
    ]
  }
}
```

### 8.4 手动分配案件

**接口**: `POST /cases/{case_id}/assign`

**请求参数**:
```json
{
  "collector_id": 1,
  "reason": "手动分配"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "分配成功",
  "data": {
    "case_id": 1,
    "case_code": "CASE_001",
    "collector_id": 1,
    "collector_name": "王小明",
    "assigned_at": "2025-11-20 21:30:00"
  }
}
```

### 8.5 批量自动分案

**接口**: `POST /cases/auto-assign`

**请求参数**:
```json
{
  "team_id": 1,
  "queue_id": 1,
  "strategy": "balanced"
}
```

**分配策略**:
- `balanced`: 均衡分配（每个催员案件数尽量相等）
- `performance`: 按绩效分配（绩效高的多分）
- `specialty`: 按专长分配（匹配催员专长）

**响应示例**:
```json
{
  "code": 200,
  "message": "自动分案完成",
  "data": {
    "total_cases": 50,
    "assigned": 48,
    "failed": 2,
    "assignments": [
      {"collector_id": 1, "collector_name": "王小明", "case_count": 16},
      {"collector_id": 2, "collector_name": "李小红", "case_count": 16},
      {"collector_id": 3, "collector_name": "张小刚", "case_count": 16}
    ]
  }
}
```

### 8.6 案件转移

**接口**: `POST /cases/{case_id}/transfer`

**请求参数**:
```json
{
  "from_collector_id": 1,
  "to_collector_id": 2,
  "reason": "催员离职"
}
```

### 8.7 更新案件状态

**接口**: `PUT /cases/{case_id}/status`

**请求参数**:
```json
{
  "case_status": "partial_repayment",
  "repaid_amount": 3000.00,
  "notes": "客户已还款3000"
}
```

### 8.8 案件搜索

**接口**: `POST /cases/search`

**请求参数**:
```json
{
  "filters": {
    "overdue_days": {
      "min": 10,
      "max": 30
    },
    "outstanding_amount": {
      "min": 5000
    },
    "case_status": ["pending_repayment", "partial_repayment"]
  },
  "sort": {
    "field": "overdue_days",
    "order": "desc"
  },
  "page": 1,
  "size": 20
}
```

---

## 🎨 9. 字段管理

### 9.1 获取标准字段列表

**接口**: `GET /fields/standard`

**查询参数**:
- `field_group_id`: 字段分组ID
- `is_active`: 是否启用

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "items": [
      {
        "id": 1,
        "field_key": "user_name",
        "field_name": "客户姓名",
        "field_name_en": "Customer Name",
        "field_type": "String",
        "field_group_name": "客户基础信息",
        "is_required": true,
        "is_extended": false,
        "sort_order": 1,
        "is_active": true
      }
    ]
  }
}
```

### 9.2 更新字段排序

**接口**: `PUT /fields/standard/order`

**请求参数**:
```json
{
  "field_ids": [3, 1, 2, 5, 4]
}
```

### 9.3 获取字段分组

**接口**: `GET /field-groups`

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "items": [
      {
        "id": 1,
        "group_key": "customer_basic",
        "group_name": "客户基础信息",
        "group_name_en": "Customer Basic Info",
        "parent_id": null,
        "children": [],
        "field_count": 10
      }
    ]
  }
}
```

### 9.4 甲方字段展示配置

**接口**: `POST /field-display/batch`

**描述**: 批量配置甲方的字段展示

**请求参数**:
```json
{
  "tenant_id": 1,
  "scene_type": "admin_case_list",
  "fields": [
    {
      "field_key": "case_code",
      "field_name": "案件编号",
      "field_data_type": "String",
      "field_source": "standard",
      "sort_order": 1,
      "display_width": 150,
      "color_type": "normal",
      "is_searchable": true,
      "is_filterable": false,
      "is_range_searchable": false
    }
  ]
}
```

### 9.5 获取甲方字段展示配置

**接口**: `GET /field-display`

**查询参数**:
- `tenant_id`: 甲方ID
- `scene_type`: 场景类型

**响应示例**: 返回该场景的字段配置列表

---

## 🔔 10. 通知系统

### 10.1 获取通知模板列表

**接口**: `GET /notification-templates`

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "items": [
      {
        "id": 1,
        "template_id": "CASE_TAG_CHANGE",
        "template_name": "案件标签变更通知",
        "template_type": "case_tag_change",
        "content_template": "案件 {case_code} 的标签已变更为 {tag_name}",
        "target_type": "collector",
        "is_forced_read": false,
        "priority": "medium",
        "is_enabled": true,
        "total_sent": 150,
        "total_read": 120
      }
    ]
  }
}
```

### 10.2 创建通知模板

**接口**: `POST /notification-templates`

**请求参数**:
```json
{
  "template_id": "CASE_TAG_CHANGE",
  "template_name": "案件标签变更通知",
  "template_type": "case_tag_change",
  "content_template": "案件 {case_code} 的标签已变更为 {tag_name}",
  "target_type": "collector",
  "is_forced_read": false,
  "repeat_interval_minutes": 30,
  "max_remind_count": 3,
  "priority": "medium",
  "display_duration_seconds": 5,
  "is_enabled": true
}
```

### 10.3 推送通知

**接口**: `POST /notifications/push`

**请求参数**:
```json
{
  "template_id": "CASE_TAG_CHANGE",
  "target_collectors": [1, 2, 3],
  "variables": {
    "case_code": "CASE_001",
    "tag_name": "高优先级"
  }
}
```

### 10.4 公共通知管理

**接口**: `GET /public-notifications`

**描述**: 获取公共通知列表（公告、系统通知等）

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "items": [
      {
        "id": 1,
        "title": "系统维护通知",
        "content": "系统将于本周六凌晨2点进行维护...",
        "is_forced_read": true,
        "effective_start_time": "2025-11-20 00:00:00",
        "effective_end_time": "2025-11-27 23:59:59",
        "is_enabled": true
      }
    ]
  }
}
```

### 10.5 创建公共通知

**接口**: `POST /public-notifications`

---

## 📊 11. 数据看板

### 11.1 沟通记录统计

**接口**: `GET /dashboard/communications`

**查询参数**:
- `date`: 日期（YYYY-MM-DD）
- `agency_id`: 机构ID
- `team_id`: 小组ID
- `collector_id`: 催员ID

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "date": "2025-11-20",
    "total_communications": 150,
    "by_method": {
      "phone": 90,
      "whatsapp": 45,
      "sms": 15
    },
    "by_result": {
      "接通": 75,
      "未接通": 50,
      "关机": 25
    },
    "by_hour": [
      {"hour": 9, "count": 20},
      {"hour": 10, "count": 35}
    ]
  }
}
```

### 11.2 PTP统计

**接口**: `GET /dashboard/ptp`

**查询参数同上**

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "date": "2025-11-20",
    "total_ptp": 30,
    "total_promise_amount": 150000.00,
    "ptp_by_status": {
      "pending": 20,
      "kept": 5,
      "broken": 5
    },
    "ptp_by_collector": [
      {
        "collector_id": 1,
        "collector_name": "王小明",
        "ptp_count": 10,
        "kept_count": 3
      }
    ]
  }
}
```

### 11.3 质检统计

**接口**: `GET /dashboard/quality-inspections`

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "total_inspections": 50,
    "average_score": 85.5,
    "by_result": {
      "优秀": 20,
      "良好": 25,
      "一般": 5
    }
  }
}
```

### 11.4 催员绩效统计

**接口**: `GET /dashboard/performance`

**查询参数**:
- `collector_id`: 催员ID
- `month`: 月份（YYYY-MM）

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "collector_id": 1,
    "collector_name": "王小明",
    "month": "2025-11",
    "total_cases": 150,
    "contacted_cases": 135,
    "resolved_cases": 30,
    "contact_rate": 0.90,
    "resolution_rate": 0.20,
    "total_collected_amount": 300000.00,
    "ptp_kept_rate": 0.60
  }
}
```

### 11.5 分析统计

**接口**: `GET /dashboard/analytics`

**描述**: 多维度数据分析

### 11.6 预警统计

**接口**: `GET /dashboard/alerts`

**描述**: 异常预警统计

### 11.7 空闲催员监控

**接口**: `POST /idle-monitor/calculate`

**描述**: 计算催员空闲时间

**请求参数**:
```json
{
  "date": "2025-11-20",
  "agency_id": 1
}
```

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "date": "2025-11-20",
    "agency_name": "催收机构1",
    "idle_collectors": [
      {
        "collector_id": 1,
        "collector_name": "王小明",
        "idle_minutes": 120,
        "idle_reason": "无分配案件",
        "last_activity_at": "2025-11-20 09:00:00"
      }
    ]
  }
}
```

---

## 📈 12. 报表导出

### 12.1 导出案件列表

**接口**: `POST /reports/cases/export`

**描述**: 导出案件列表为Excel

**请求参数**:
```json
{
  "filters": {
    "queue_id": 1,
    "date_range": {
      "start": "2025-11-01",
      "end": "2025-11-30"
    }
  },
  "fields": ["case_code", "user_name", "mobile", "overdue_days", "outstanding_amount"]
}
```

**响应**: Excel文件下载

### 12.2 导出催员绩效报表

**接口**: `POST /reports/performance/export`

---

## ⚙️ 13. 系统配置

### 13.1 获取系统配置

**接口**: `GET /system/config`

### 13.2 更新系统配置

**接口**: `PUT /system/config`

### 13.3 工作时间配置

**接口**: `GET /agency-working-hours`

**描述**: 获取机构工作时间配置

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "items": [
      {
        "id": 1,
        "agency_id": 1,
        "day_of_week": 1,
        "start_time": "09:00:00",
        "end_time": "18:00:00",
        "is_working_day": true
      }
    ]
  }
}
```

---

## 📝 通用说明

### 错误码

| Code | 说明 |
|------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未授权（未登录或token失效） |
| 403 | 禁止访问（权限不足） |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

### 分页参数

所有列表接口支持分页：
- `page`: 页码（从1开始）
- `size`: 每页数量（默认10，最大100）

### 排序参数

支持排序的接口：
- `sort`: 排序字段
- `order`: 排序方向（asc/desc）

### 时间格式

- 日期时间: `YYYY-MM-DD HH:mm:ss`
- 日期: `YYYY-MM-DD`
- 时间: `HH:mm:ss`

### 权限说明

- SuperAdmin: 全部权限
- TenantAdmin: 只能管理自己甲方的数据
- AgencyAdmin: 只能管理自己机构的数据
- TeamAdmin: 只能管理自己小组的数据

---

**文档版本**: v1.0.0  
**最后更新**: 2025-11-20

