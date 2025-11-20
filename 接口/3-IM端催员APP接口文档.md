# CCO IM端催员APP - API接口文档

## 📋 接口概述

IM端API专为催员移动端APP（iOS/Android）和Web端工作台设计，提供催员日常工作所需的全部功能。

**Base URL**: `http://localhost:8000/api/v1/im`

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

### 1.1 催员登录

**接口**: `POST /im/auth/login`

**描述**: 催员通过工号和密码登录

**请求参数**:
```json
{
  "loginId": "collector001",
  "password": "123456",
  "device_type": "mobile",
  "device_id": "DEVICE_ABC123",
  "device_model": "iPhone 13",
  "os_version": "iOS 16.0",
  "app_version": "1.0.0"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expires_in": 86400,
    "user": {
      "id": 1,
      "collector_code": "COL_001",
      "collector_name": "王小明",
      "login_id": "collector001",
      "mobile": "+63-917-123-4567",
      "email": "collector001@example.com",
      "avatar_url": "https://cdn.example.com/avatar/001.jpg",
      "team_id": 1,
      "team_name": "M1催收小组",
      "agency_name": "催收机构1",
      "collector_level": "高级",
      "max_case_count": 150,
      "current_case_count": 45,
      "status": "active"
    },
    "permissions": [
      "view_my_cases",
      "add_communication",
      "add_ptp",
      "view_customer_info"
    ]
  }
}
```

### 1.2 催员登录（人脸识别）

**接口**: `POST /im/auth/face-login`

**描述**: 使用人脸识别登录（生物识别认证）

**请求参数**:
```json
{
  "loginId": "collector001",
  "face_image": "base64_encoded_image_data",
  "device_type": "mobile",
  "device_id": "DEVICE_ABC123"
}
```

**响应示例**: 同普通登录

### 1.3 刷新Token

**接口**: `POST /im/auth/refresh`

**描述**: 刷新过期的access_token

**请求参数**:
```json
{
  "refresh_token": "refresh_token_string"
}
```

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "token": "new_access_token",
    "expires_in": 86400
  }
}
```

### 1.4 退出登录

**接口**: `POST /im/auth/logout`

**认证**: 需要

**响应示例**:
```json
{
  "code": 200,
  "message": "登出成功"
}
```

### 1.5 获取当前催员信息

**接口**: `GET /im/auth/me`

**认证**: 需要

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "collector_name": "王小明",
    "team_name": "M1催收小组",
    "current_case_count": 45,
    "max_case_count": 150,
    "today_statistics": {
      "contact_count": 30,
      "ptp_count": 5,
      "collected_amount": 15000.00
    }
  }
}
```

### 1.6 修改密码

**接口**: `POST /im/auth/change-password`

**认证**: 需要

**请求参数**:
```json
{
  "old_password": "123456",
  "new_password": "new123456",
  "confirm_password": "new123456"
}
```

---

## 📱 2. 工作台首页

### 2.1 获取工作台数据

**接口**: `GET /im/dashboard`

**描述**: 获取催员工作台首页数据（今日统计、待办事项等）

**认证**: 需要

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "collector_info": {
      "collector_name": "王小明",
      "team_name": "M1催收小组",
      "current_case_count": 45,
      "work_status": "online"
    },
    
    "today_statistics": {
      "date": "2025-11-20",
      "contact_count": 30,
      "effective_contact_count": 20,
      "contact_rate": 0.67,
      "ptp_count": 5,
      "collected_amount": 15000.00,
      "resolved_cases": 2,
      "working_hours": 7.5
    },
    
    "month_statistics": {
      "month": "2025-11",
      "total_contact_count": 450,
      "total_ptp_count": 80,
      "total_collected_amount": 300000.00,
      "total_resolved_cases": 30,
      "performance_score": 4.5,
      "ranking": 3
    },
    
    "todo_list": [
      {
        "type": "follow_up",
        "title": "跟进PTP到期案件",
        "count": 3,
        "priority": "high"
      },
      {
        "type": "new_case",
        "title": "处理新分配案件",
        "count": 5,
        "priority": "medium"
      },
      {
        "type": "uncontacted",
        "title": "未联系案件",
        "count": 10,
        "priority": "medium"
      }
    ],
    
    "recent_activities": [
      {
        "time": "2025-11-20 14:30:00",
        "type": "repayment",
        "content": "客户陈大明已还款2000元"
      },
      {
        "time": "2025-11-20 13:15:00",
        "type": "ptp",
        "content": "获得客户李小红的还款承诺"
      }
    ],
    
    "notifications": [
      {
        "id": 1,
        "title": "新案件分配",
        "content": "您有5个新案件待处理",
        "is_read": false,
        "created_at": "2025-11-20 09:00:00"
      }
    ]
  }
}
```

### 2.2 获取排行榜

**接口**: `GET /im/dashboard/ranking`

**查询参数**:
- `type`: 排行类型（contact/ptp/collected_amount/resolution）
- `scope`: 范围（team/agency/all）
- `period`: 时间段（today/week/month）

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "type": "collected_amount",
    "period": "month",
    "my_ranking": {
      "rank": 3,
      "collector_name": "王小明",
      "value": 300000.00
    },
    "top_10": [
      {
        "rank": 1,
        "collector_name": "张三",
        "value": 500000.00
      },
      {
        "rank": 2,
        "collector_name": "李四",
        "value": 400000.00
      },
      {
        "rank": 3,
        "collector_name": "王小明",
        "value": 300000.00
      }
    ]
  }
}
```

---

## 📋 3. 案件管理

### 3.1 获取我的案件列表

**接口**: `GET /im/cases/my`

**描述**: 获取分配给当前催员的所有案件

**认证**: 需要

**查询参数**:
- `status`: 案件状态（pending/in_progress/resolved）
- `priority`: 优先级（high/medium/low）
- `overdue_days_min`: 最小逾期天数
- `overdue_days_max`: 最大逾期天数
- `last_contact_days`: 最后联系距今天数
- `has_ptp`: 是否有PTP承诺（true/false）
- `search`: 搜索关键词（姓名、手机号、案件编号）
- `sort`: 排序字段（overdue_days/outstanding_amount/last_contact_at）
- `order`: 排序方向（asc/desc）
- `page`: 页码
- `size`: 每页数量

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "total": 45,
    "page": 1,
    "size": 20,
    "summary": {
      "total_outstanding_amount": 450000.00,
      "high_priority_count": 10,
      "need_follow_up_count": 8
    },
    "items": [
      {
        "id": 1,
        "case_code": "CASE_001",
        "user_name": "陈大明",
        "mobile": "+63-917-***-**11",
        "overdue_days": 15,
        "loan_amount": 5000.00,
        "outstanding_amount": 4000.00,
        "case_status": "pending_repayment",
        "case_priority": "high",
        "case_tags": ["首逾", "高优先级"],
        
        "assignment_info": {
          "assigned_at": "2025-11-18 09:00:00",
          "days_in_hand": 2
        },
        
        "contact_info": {
          "last_contact_at": "2025-11-20 10:00:00",
          "last_contact_result": "接通",
          "total_contact_count": 5,
          "effective_contact_count": 3
        },
        
        "ptp_info": {
          "has_ptp": true,
          "latest_ptp_date": "2025-11-25",
          "latest_ptp_amount": 2000.00,
          "ptp_status": "pending"
        },
        
        "next_action": {
          "action_type": "follow_up",
          "action_time": "2025-11-25 10:00:00",
          "action_note": "跟进PTP承诺"
        }
      }
    ]
  }
}
```

### 3.2 获取案件详情

**接口**: `GET /im/cases/{case_id}`

**描述**: 查看案件的完整详细信息

**认证**: 需要（仅能查看自己的案件）

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "case_code": "CASE_001",
    "case_status": "pending_repayment",
    "case_priority": "high",
    "case_tags": ["首逾", "高优先级"],
    
    "customer_info": {
      "user_id": "USER_001",
      "user_name": "陈大明",
      "mobile": "+63-917-111-1111",
      "email": "user001@example.com",
      "id_number": "ID***456",
      "gender": "M",
      "date_of_birth": "1990-05-15",
      "address": "马尼拉市***",
      "kyc_level": "2"
    },
    
    "employment_info": {
      "employer": "ABC公司",
      "position": "软件工程师",
      "monthly_income": 15000.00
    },
    
    "emergency_contacts": [
      {
        "id": 1,
        "contact_name": "王小红",
        "relationship": "配偶",
        "mobile": "+63-917-222-2222",
        "is_verified": true,
        "last_contact_at": "2025-11-19 15:00:00",
        "last_contact_result": "接通"
      },
      {
        "id": 2,
        "contact_name": "陈大勇",
        "relationship": "兄弟",
        "mobile": "+63-917-333-3333",
        "is_verified": false,
        "last_contact_at": null
      }
    ],
    
    "loan_info": {
      "loan_id": "LOAN_001",
      "loan_product_name": "快速贷",
      "loan_amount": 5000.00,
      "loan_date": "2025-10-01",
      "loan_term": 30,
      "due_date": "2025-11-05",
      "interest_rate": 0.15
    },
    
    "repayment_info": {
      "repaid_amount": 1000.00,
      "outstanding_principal": 4000.00,
      "outstanding_interest": 500.00,
      "outstanding_penalty": 200.00,
      "outstanding_amount": 4700.00,
      "overdue_days": 15,
      "overdue_start_date": "2025-11-06",
      "daily_penalty": 10.00
    },
    
    "repayment_history": [
      {
        "id": 1,
        "repayment_date": "2025-10-15",
        "repayment_amount": 1000.00,
        "repayment_channel": "bank_transfer",
        "transaction_no": "TXN123456"
      }
    ],
    
    "communication_history": [
      {
        "id": 1,
        "contact_time": "2025-11-20 10:00:00",
        "contact_method": "phone",
        "contact_phone": "+63-917-111-1111",
        "contact_result": "接通",
        "communication_content": "客户表示下周一还款",
        "collector_name": "王小明",
        "call_duration_seconds": 180,
        "audio_url": "https://cdn.example.com/audio/call_001.mp3"
      }
    ],
    
    "ptp_records": [
      {
        "id": 1,
        "promise_date": "2025-11-25",
        "promise_amount": 2000.00,
        "promise_method": "bank_transfer",
        "promise_status": "pending",
        "created_at": "2025-11-20 10:05:00",
        "collector_name": "王小明",
        "notes": "客户承诺下周一还款2000"
      }
    ],
    
    "assignment_history": [
      {
        "id": 1,
        "assigned_to": "王小明",
        "assigned_at": "2025-11-18 09:00:00",
        "assigned_by": "系统自动分配"
      }
    ]
  }
}
```

### 3.3 案件快速筛选

**接口**: `GET /im/cases/quick-filter`

**描述**: 提供常用筛选条件快速筛选案件

**查询参数**:
- `filter_type`: 筛选类型

**筛选类型**:
- `today_assigned`: 今日新分配
- `never_contacted`: 从未联系
- `need_follow_up`: 需要跟进（有PTP或预约回拨）
- `high_priority`: 高优先级
- `long_overdue`: 长期逾期（>60天）
- `high_amount`: 大额案件（>10000）
- `recently_contacted`: 最近联系过（3天内）

**响应示例**: 同案件列表格式

### 3.4 搜索案件

**接口**: `POST /im/cases/search`

**描述**: 高级搜索功能

**请求参数**:
```json
{
  "keyword": "陈大明",
  "filters": {
    "overdue_days": {
      "min": 10,
      "max": 30
    },
    "outstanding_amount": {
      "min": 5000
    },
    "case_tags": ["首逾"]
  }
}
```

---

## 📞 4. 沟通管理

### 4.1 添加沟通记录

**接口**: `POST /im/cases/{case_id}/communication`

**描述**: 记录与客户的沟通情况

**认证**: 需要

**请求参数**:
```json
{
  "contact_method": "phone",
  "contact_phone": "+63-917-111-1111",
  "contact_result": "接通",
  "communication_content": "客户表示下周一还款2000元，态度良好",
  "call_duration_seconds": 180,
  "audio_url": "https://cdn.example.com/audio/call_001.mp3",
  "contact_person": "本人",
  "next_follow_up_at": "2025-11-25T10:00:00",
  "tags": ["还款意愿强", "有还款能力"]
}
```

**字段说明**:
- `contact_method`: 联系方式（phone/whatsapp/sms/email/visit）
- `contact_result`: 联系结果（接通/未接通/关机/空号/拒接/忙线）
- `contact_person`: 联系人（本人/配偶/家人/朋友/同事/其他）

**响应示例**:
```json
{
  "code": 200,
  "message": "沟通记录保存成功",
  "data": {
    "id": 1,
    "case_id": 1,
    "created_at": "2025-11-20 10:00:00"
  }
}
```

### 4.2 获取沟通历史

**接口**: `GET /im/cases/{case_id}/communications`

**描述**: 查看案件的所有沟通历史

**查询参数**:
- `contact_method`: 联系方式筛选
- `contact_result`: 联系结果筛选
- `page`: 页码

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "total": 15,
    "items": [
      {
        "id": 1,
        "contact_time": "2025-11-20 10:00:00",
        "contact_method": "phone",
        "contact_result": "接通",
        "communication_content": "客户表示下周一还款",
        "collector_name": "王小明",
        "call_duration_seconds": 180
      }
    ]
  }
}
```

### 4.3 拨打电话

**接口**: `POST /im/cases/{case_id}/call`

**描述**: 发起通话（集成电话系统）

**请求参数**:
```json
{
  "phone_number": "+63-917-111-1111",
  "contact_person": "本人"
}
```

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "call_id": "CALL_001",
    "call_url": "sip:call@pbx.example.com",
    "status": "connecting"
  }
}
```

### 4.4 发送WhatsApp消息

**接口**: `POST /im/cases/{case_id}/whatsapp`

**描述**: 通过WhatsApp发送消息

**请求参数**:
```json
{
  "phone_number": "+63-917-111-1111",
  "message_template": "REMIND_PAYMENT",
  "variables": {
    "customer_name": "陈大明",
    "outstanding_amount": "4700.00"
  }
}
```

### 4.5 发送SMS

**接口**: `POST /im/cases/{case_id}/sms`

**描述**: 发送短信提醒

**请求参数**:
```json
{
  "phone_number": "+63-917-111-1111",
  "message_template": "REMIND_PAYMENT",
  "variables": {
    "customer_name": "陈大明",
    "outstanding_amount": "4700.00"
  }
}
```

---

## 🤝 5. PTP管理

### 5.1 添加PTP承诺

**接口**: `POST /im/cases/{case_id}/ptp`

**描述**: 记录客户的还款承诺

**认证**: 需要

**请求参数**:
```json
{
  "promise_date": "2025-11-25",
  "promise_amount": 2000.00,
  "promise_method": "bank_transfer",
  "confidence_level": "high",
  "notes": "客户承诺下周一还款2000，态度诚恳"
}
```

**字段说明**:
- `promise_method`: 还款方式（bank_transfer/ewallet/cash/other）
- `confidence_level`: 信心程度（high/medium/low）

**响应示例**:
```json
{
  "code": 200,
  "message": "PTP记录保存成功",
  "data": {
    "id": 1,
    "case_id": 1,
    "promise_date": "2025-11-25",
    "created_at": "2025-11-20 10:05:00"
  }
}
```

### 5.2 获取PTP列表

**接口**: `GET /im/ptp/my`

**描述**: 查看我的所有PTP承诺

**查询参数**:
- `status`: PTP状态（pending/kept/broken/expired）
- `date_range`: 承诺日期范围
- `page`: 页码

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "total": 20,
    "summary": {
      "pending_count": 10,
      "kept_count": 6,
      "broken_count": 4,
      "total_promise_amount": 100000.00
    },
    "items": [
      {
        "id": 1,
        "case_code": "CASE_001",
        "user_name": "陈大明",
        "promise_date": "2025-11-25",
        "promise_amount": 2000.00,
        "promise_status": "pending",
        "days_until_due": 5,
        "created_at": "2025-11-20 10:05:00"
      }
    ]
  }
}
```

### 5.3 更新PTP状态

**接口**: `PUT /im/ptp/{ptp_id}/status`

**描述**: 更新PTP履行状态

**请求参数**:
```json
{
  "promise_status": "kept",
  "actual_repayment_date": "2025-11-25",
  "actual_repayment_amount": 2000.00,
  "notes": "客户按时还款"
}
```

### 5.4 PTP到期提醒

**接口**: `GET /im/ptp/due-soon`

**描述**: 获取即将到期的PTP（需要跟进）

**查询参数**:
- `days`: 未来X天内到期（默认3天）

**响应示例**: 同PTP列表格式

---

## 💰 6. 还款管理

### 6.1 查看还款记录

**接口**: `GET /im/cases/{case_id}/repayments`

**描述**: 查看案件的还款历史

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "total_repayments": 2,
    "total_repaid_amount": 1000.00,
    "items": [
      {
        "id": 1,
        "repayment_date": "2025-10-15",
        "repayment_amount": 500.00,
        "repayment_channel": "bank_transfer",
        "transaction_no": "TXN123456",
        "status": "success"
      }
    ]
  }
}
```

### 6.2 生成还款链接

**接口**: `POST /im/cases/{case_id}/payment-link`

**描述**: 生成还款链接发送给客户

**请求参数**:
```json
{
  "payment_amount": 2000.00,
  "expire_hours": 24
}
```

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "payment_link": "https://pay.example.com/p/ABC123",
    "qr_code_url": "https://cdn.example.com/qr/ABC123.png",
    "expire_at": "2025-11-21 10:00:00"
  }
}
```

### 6.3 确认线下还款

**接口**: `POST /im/cases/{case_id}/offline-repayment`

**描述**: 催员确认客户线下还款（需要后台审核）

**请求参数**:
```json
{
  "repayment_amount": 2000.00,
  "repayment_date": "2025-11-20",
  "repayment_method": "cash",
  "receipt_image_url": "https://cdn.example.com/receipt/001.jpg",
  "notes": "客户到机构现金还款"
}
```

---

## 🔔 7. 通知消息

### 7.1 获取未读通知

**接口**: `GET /im/notifications/unread`

**描述**: 获取催员的未读通知列表

**认证**: 需要

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "total_unread": 5,
    "items": [
      {
        "id": 1,
        "notification_type": "case_assignment",
        "title": "新案件分配",
        "content": "您有5个新案件待处理",
        "is_forced_read": false,
        "priority": "medium",
        "created_at": "2025-11-20 09:00:00",
        "action_url": "/cases/my?filter=today_assigned"
      },
      {
        "id": 2,
        "notification_type": "repayment",
        "title": "还款通知",
        "content": "客户陈大明已还款2000元",
        "is_forced_read": false,
        "priority": "high",
        "created_at": "2025-11-20 14:30:00",
        "action_url": "/cases/1"
      },
      {
        "id": 3,
        "notification_type": "ptp_due",
        "title": "PTP到期提醒",
        "content": "您有3个PTP今日到期，请及时跟进",
        "is_forced_read": true,
        "priority": "high",
        "created_at": "2025-11-20 08:00:00",
        "action_url": "/ptp/due-soon"
      }
    ]
  }
}
```

### 7.2 获取通知历史

**接口**: `GET /im/notifications/history`

**查询参数**:
- `notification_type`: 通知类型
- `is_read`: 已读/未读
- `page`: 页码

### 7.3 标记通知已读

**接口**: `POST /im/notifications/{notification_id}/read`

**描述**: 标记单条通知为已读

**响应示例**:
```json
{
  "code": 200,
  "message": "已标记为已读"
}
```

### 7.4 批量标记已读

**接口**: `POST /im/notifications/read-batch`

**请求参数**:
```json
{
  "notification_ids": [1, 2, 3]
}
```

### 7.5 获取公共通知

**接口**: `GET /im/public-notifications`

**描述**: 获取系统公告、公共通知

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "items": [
      {
        "id": 1,
        "title": "系统维护通知",
        "content": "系统将于本周六凌晨2点进行维护，预计1小时",
        "is_forced_read": true,
        "effective_start_time": "2025-11-20 00:00:00",
        "effective_end_time": "2025-11-27 23:59:59",
        "created_at": "2025-11-19 10:00:00"
      }
    ]
  }
}
```

---

## 📊 8. 统计报表

### 8.1 我的绩效统计

**接口**: `GET /im/statistics/performance`

**查询参数**:
- `period`: 统计周期（today/week/month/custom）
- `start_date`: 开始日期（custom时必填）
- `end_date`: 结束日期（custom时必填）

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "period": "month",
    "start_date": "2025-11-01",
    "end_date": "2025-11-30",
    
    "case_statistics": {
      "total_cases": 150,
      "new_cases": 20,
      "resolved_cases": 30,
      "resolution_rate": 0.20
    },
    
    "contact_statistics": {
      "total_contacts": 450,
      "effective_contacts": 300,
      "contact_rate": 0.67,
      "average_call_duration": 120
    },
    
    "ptp_statistics": {
      "total_ptp": 80,
      "kept_ptp": 48,
      "broken_ptp": 20,
      "pending_ptp": 12,
      "ptp_kept_rate": 0.60
    },
    
    "collection_statistics": {
      "total_collected_amount": 300000.00,
      "collection_rate": 0.25,
      "average_case_amount": 2000.00
    },
    
    "working_hours": {
      "total_hours": 160,
      "average_daily_hours": 8.0,
      "online_rate": 0.95
    },
    
    "ranking": {
      "team_rank": 3,
      "team_total": 10,
      "agency_rank": 8,
      "agency_total": 50
    }
  }
}
```

### 8.2 日报统计

**接口**: `GET /im/statistics/daily`

**查询参数**:
- `date`: 日期（默认今天）

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "date": "2025-11-20",
    "contact_count": 30,
    "effective_contact_count": 20,
    "ptp_count": 5,
    "collected_amount": 15000.00,
    "resolved_cases": 2,
    "working_hours": 7.5,
    
    "hourly_distribution": [
      {"hour": 9, "contact_count": 5},
      {"hour": 10, "contact_count": 8},
      {"hour": 11, "contact_count": 6}
    ]
  }
}
```

### 8.3 案件统计

**接口**: `GET /im/statistics/cases`

**描述**: 获取案件分布统计

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "total_cases": 45,
    
    "by_status": {
      "pending_repayment": 30,
      "partial_repayment": 10,
      "ptp_pending": 5
    },
    
    "by_overdue_days": {
      "dpd_1_15": 15,
      "dpd_16_30": 20,
      "dpd_31_plus": 10
    },
    
    "by_amount": {
      "under_5000": 20,
      "5000_10000": 15,
      "over_10000": 10
    },
    
    "by_contact_status": {
      "never_contacted": 10,
      "contacted_today": 15,
      "last_contact_3_days": 12,
      "last_contact_7_days": 8
    }
  }
}
```

---

## 👤 9. 个人中心

### 9.1 获取个人资料

**接口**: `GET /im/profile`

**认证**: 需要

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "collector_code": "COL_001",
    "collector_name": "王小明",
    "login_id": "collector001",
    "mobile": "+63-917-123-4567",
    "email": "collector001@example.com",
    "avatar_url": "https://cdn.example.com/avatar/001.jpg",
    "gender": "M",
    "date_of_birth": "1992-03-15",
    
    "work_info": {
      "employee_no": "EMP001",
      "team_name": "M1催收小组",
      "agency_name": "催收机构1",
      "collector_level": "高级",
      "join_date": "2024-01-01",
      "max_case_count": 150,
      "specialties": ["高额案件", "法务处理"]
    },
    
    "statistics_summary": {
      "total_cases_handled": 500,
      "total_collected_amount": 1500000.00,
      "average_resolution_rate": 0.22,
      "performance_score": 4.5
    }
  }
}
```

### 9.2 更新个人资料

**接口**: `PUT /im/profile`

**请求参数**:
```json
{
  "mobile": "+63-917-123-4567",
  "email": "newemail@example.com",
  "avatar_url": "https://cdn.example.com/avatar/new.jpg"
}
```

### 9.3 上传头像

**接口**: `POST /im/profile/avatar`

**请求参数**: multipart/form-data

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "avatar_url": "https://cdn.example.com/avatar/001.jpg"
  }
}
```

### 9.4 绑定人脸

**接口**: `POST /im/profile/bind-face`

**描述**: 绑定人脸用于人脸识别登录

**请求参数**:
```json
{
  "face_images": [
    "base64_encoded_image_1",
    "base64_encoded_image_2",
    "base64_encoded_image_3"
  ]
}
```

### 9.5 工作状态管理

**接口**: `PUT /im/profile/work-status`

**描述**: 更新工作状态（在线/忙碌/离开/下班）

**请求参数**:
```json
{
  "work_status": "online"
}
```

**工作状态枚举**:
- `online`: 在线
- `busy`: 忙碌
- `away`: 离开
- `offline`: 下班

---

## 📚 10. 知识库

### 10.1 获取话术模板

**接口**: `GET /im/knowledge/scripts`

**描述**: 获取催收话术模板

**查询参数**:
- `category`: 话术分类（opening/negotiation/closing/objection_handling）
- `scenario`: 场景（first_contact/follow_up/ptp/threat）

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "items": [
      {
        "id": 1,
        "title": "首次联系开场白",
        "category": "opening",
        "scenario": "first_contact",
        "content": "您好，{customer_name}，我是{company_name}的{collector_name}...",
        "tips": "态度要礼貌、专业，语气要平和",
        "use_count": 150
      }
    ]
  }
}
```

### 10.2 获取FAQ

**接口**: `GET /im/knowledge/faq`

**描述**: 常见问题解答

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "items": [
      {
        "id": 1,
        "question": "客户说没钱还怎么办？",
        "answer": "1. 表示理解客户困难\n2. 了解客户实际情况\n3. 建议部分还款\n4. 协商还款计划",
        "category": "objection_handling"
      }
    ]
  }
}
```

### 10.3 搜索知识库

**接口**: `POST /im/knowledge/search`

**请求参数**:
```json
{
  "keyword": "客户拒绝还款"
}
```

---

## 🛠️ 11. 工具功能

### 11.1 还款计算器

**接口**: `POST /im/tools/calculate-repayment`

**描述**: 计算还款金额

**请求参数**:
```json
{
  "principal": 5000.00,
  "interest_rate": 0.15,
  "days": 15,
  "penalty_rate": 0.05
}
```

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "principal": 5000.00,
    "interest": 750.00,
    "penalty": 750.00,
    "total_amount": 6500.00
  }
}
```

### 11.2 获取工作时间配置

**接口**: `GET /im/tools/working-hours`

**描述**: 获取可联系客户的工作时间

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "timezone": 8,
    "working_days": [
      {
        "day_of_week": 1,
        "start_time": "09:00:00",
        "end_time": "18:00:00"
      }
    ],
    "current_time": "2025-11-20T10:30:00",
    "is_working_time": true
  }
}
```

### 11.3 号码验证

**接口**: `POST /im/tools/validate-phone`

**描述**: 验证手机号格式和有效性

**请求参数**:
```json
{
  "phone_number": "+63-917-123-4567"
}
```

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "is_valid": true,
    "country_code": "PH",
    "carrier": "Globe",
    "number_type": "mobile"
  }
}
```

---

## 🔧 12. 系统功能

### 12.1 版本检查

**接口**: `GET /im/system/version`

**描述**: 检查APP版本更新

**查询参数**:
- `current_version`: 当前版本号
- `platform`: 平台（ios/android）

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "latest_version": "1.1.0",
    "current_version": "1.0.0",
    "has_update": true,
    "is_force_update": false,
    "update_url": "https://app.example.com/download/v1.1.0",
    "update_notes": "1. 修复已知问题\n2. 优化性能"
  }
}
```

### 12.2 意见反馈

**接口**: `POST /im/system/feedback`

**请求参数**:
```json
{
  "feedback_type": "bug",
  "title": "通话记录保存失败",
  "content": "在通话结束后点击保存，系统提示保存失败",
  "images": [
    "https://cdn.example.com/feedback/001.jpg"
  ],
  "device_info": {
    "device_model": "iPhone 13",
    "os_version": "iOS 16.0",
    "app_version": "1.0.0"
  }
}
```

**反馈类型**:
- `bug`: 问题反馈
- `feature`: 功能建议
- `complaint`: 投诉
- `other`: 其他

### 12.3 帮助中心

**接口**: `GET /im/system/help`

**描述**: 获取帮助文档列表

---

## 📝 通用说明

### 错误码

| Code | 说明 |
|------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未授权（未登录或token失效） |
| 403 | 禁止访问（无权限访问该案件） |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

### 分页参数

所有列表接口支持分页：
- `page`: 页码（从1开始）
- `size`: 每页数量（默认20，最大100）

### 时间格式

- 日期时间: `YYYY-MM-DD HH:mm:ss`
- 日期: `YYYY-MM-DD`

### 数据权限

- 催员只能查看和操作分配给自己的案件
- 无法访问其他催员的案件和数据
- 组长可以查看本组所有催员的数据

### 离线支持

- 支持离线缓存案件列表
- 支持离线记录沟通（网络恢复后自动同步）
- 支持离线查看案件详情

### 实时同步

- 案件状态变化实时推送（WebSocket）
- 还款记录实时更新
- 通知消息实时推送

---

## 🔐 安全说明

### 敏感信息脱敏

- 手机号: `+63-917-***-**11`
- 身份证号: `ID***456`
- 银行卡号: `****1234`
- 地址: 显示部分信息

### Token管理

- Access Token有效期: 24小时
- Refresh Token有效期: 7天
- Token过期自动刷新

### 设备绑定

- 支持最多3台设备同时登录
- 新设备登录需要短信验证
- 可以远程踢出其他设备

---

**文档版本**: v1.0.0  
**最后更新**: 2025-11-20  
**维护团队**: CCO技术团队

