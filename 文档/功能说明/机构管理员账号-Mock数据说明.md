# 机构管理员账号 - Mock数据说明

## 概述

已为所有机构创建管理员账号，现在机构列表中的"机构管理员"列会显示管理员姓名。

## 创建的管理员账号

### 百腾企业的机构
| 机构编码 | 机构名称 | 管理员姓名 | 登录ID | 邮箱 | 默认密码 |
|---------|---------|-----------|--------|------|---------|
| AG101 | 百腾企业-机构1 | 百腾企业-机构1-管理员 | admin_ag101 | admin_ag101@example.com | 123456 |
| AG102 | 百腾企业-机构2 | 百腾企业-机构2-管理员 | admin_ag102 | admin_ag102@example.com | 123456 |

### 百腾数科的机构
| 机构编码 | 机构名称 | 管理员姓名 | 登录ID | 邮箱 | 默认密码 |
|---------|---------|-----------|--------|------|---------|
| AG201 | 百腾数科-机构1 | 百腾数科-机构1-管理员 | admin_ag201 | admin_ag201@example.com | 123456 |
| AG202 | 百腾数科-机构2 | 百腾数科-机构2-管理员 | admin_ag202 | admin_ag202@example.com | 123456 |

### 星耀企业的机构
| 机构编码 | 机构名称 | 管理员姓名 | 登录ID | 邮箱 | 默认密码 |
|---------|---------|-----------|--------|------|---------|
| AG301 | 星耀企业-机构1 | 星耀企业-机构1-管理员 | admin_ag301 | admin_ag301@example.com | 123456 |
| AG302 | 星耀企业-机构2 | 星耀企业-机构2-管理员 | admin_ag302 | admin_ag302@example.com | 123456 |

## 账号规则

### 命名规则
- **账号编码**: `ADMIN_{机构编码}`
  - 示例: `ADMIN_AG101`
  
- **账号名称**: `{机构名称}-管理员`
  - 示例: `百腾企业-机构1-管理员`
  
- **登录ID**: `admin_{机构编码小写}`
  - 示例: `admin_ag101`
  
- **邮箱**: `{登录ID}@example.com`
  - 示例: `admin_ag101@example.com`
  
- **手机号**: `138{机构ID补齐8位}`
  - 示例: `13800000001`

### 账号属性
- **角色**: `agency_admin` (机构管理员)
- **默认密码**: `123456`
- **状态**: 启用 (`is_active = true`)
- **关联关系**:
  - 有 `tenant_id` (所属甲方)
  - 有 `agency_id` (所属机构)
  - 无 `team_group_id` (不属于小组群)
  - 无 `team_id` (不属于小组)

## 数据验证

### SQL查询验证
```sql
SELECT 
    a.agency_code,
    a.agency_name,
    t.account_name as admin_name,
    t.login_id,
    t.email
FROM collection_agencies a
LEFT JOIN team_admin_accounts t ON (
    t.agency_id = a.id 
    AND t.team_group_id IS NULL 
    AND t.team_id IS NULL
)
WHERE a.tenant_id = 1;
```

### API验证
```bash
curl http://localhost:8000/api/v1/tenants/1/agencies | jq '.[] | {agency_name, admin_name}'
```

**返回示例**:
```json
{
  "agency_name": "百腾企业-机构1",
  "admin_name": "百腾企业-机构1-管理员"
}
```

## 创建脚本

### SQL脚本
**文件**: 在数据库中直接执行

```sql
INSERT INTO team_admin_accounts (
    tenant_id, agency_id, team_group_id, team_id, 
    account_code, account_name, login_id, password_hash, 
    role, email, mobile, is_active, created_at, updated_at
)
SELECT 
    tenant_id,
    id as agency_id,
    NULL as team_group_id,
    NULL as team_id,
    'ADMIN_' || agency_code as account_code,
    agency_name || '-管理员' as account_name,
    'admin_' || LOWER(agency_code) as login_id,
    '$2b$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5uyWGJ8xmfSja' as password_hash,
    'agency_admin' as role,
    'admin_' || LOWER(agency_code) || '@example.com' as email,
    '138' || printf('%08d', id) as mobile,
    1 as is_active,
    datetime('now') as created_at,
    datetime('now') as updated_at
FROM collection_agencies
WHERE id NOT IN (
    SELECT agency_id FROM team_admin_accounts 
    WHERE agency_id IS NOT NULL 
    AND team_group_id IS NULL 
    AND team_id IS NULL
);
```

### Python脚本
**文件**: `backend/add_agency_admins.py`

可以运行此脚本为新创建的机构自动添加管理员账号。

## 界面显示效果

刷新机构管理页面后，"机构管理员"列会显示：
- ✅ 百腾企业-机构1-管理员
- ✅ 百腾企业-机构2-管理员
- ✅ 百腾数科-机构1-管理员
- ✅ 百腾数科-机构2-管理员
- ✅ 星耀企业-机构1-管理员
- ✅ 星耀企业-机构2-管理员

## 登录测试

可以使用以下账号登录测试：
- **登录ID**: `admin_ag101`
- **密码**: `123456`

## 注意事项

1. **密码安全**: 这些是测试账号，实际生产环境中应使用更安全的密码
2. **唯一性**: 每个机构只有一个机构级别的管理员账号
3. **层级关系**: 机构管理员 < 甲方管理员，权限层级不同
4. **数据一致性**: 如果删除机构，需要同时删除关联的管理员账号

## 相关API

### 获取机构管理员账号列表
```
GET /api/v1/agencies/{agency_id}/admin-accounts
```

返回指定机构的所有管理员账号（机构级别，不包含小组群和小组的）。

### 获取甲方下的机构列表（含管理员信息）
```
GET /api/v1/tenants/{tenant_id}/agencies
```

返回机构列表，每个机构包含 `admin_name` 字段显示管理员姓名。

---

**创建日期**: 2025-11-20  
**Mock账号数量**: 6个  
**状态**: ✅ 已完成并验证

