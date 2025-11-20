# 小组管理员Mock数据说明

## 📊 创建的数据

已为每个小组创建了 **3个不同角色的管理员**：

### 角色类型
1. **主管 (Supervisor)** - `supervisor`
2. **组长 (Team Leader)** - `team_leader`
3. **质检员 (Quality Inspector)** - `quality_inspector`

---

## 👥 小组1：百腾企业-机构1-小组1

### 主管 - 王五
- **登录ID**: `team1_supervisor`
- **密码**: `password123`
- **账号编码**: `ADMIN_team1_supervisor`
- **手机**: 13800000010
- **邮箱**: team1_supervisor@example.com
- **角色**: supervisor (主管)
- **所属小组**: 百腾企业-机构1-小组1
- **所属小组群**: 高额案件组
- **状态**: 启用

### 组长 - 赵六
- **登录ID**: `team1_leader`
- **密码**: `password123`
- **账号编码**: `ADMIN_team1_leader`
- **手机**: 13800000011
- **邮箱**: team1_leader@example.com
- **角色**: team_leader (组长)
- **所属小组**: 百腾企业-机构1-小组1
- **所属小组群**: 高额案件组
- **状态**: 启用

### 质检员 - 孙七
- **登录ID**: `team1_inspector`
- **密码**: `password123`
- **账号编码**: `ADMIN_team1_inspector`
- **手机**: 13800000012
- **邮箱**: team1_inspector@example.com
- **角色**: quality_inspector (质检员)
- **所属小组**: 百腾企业-机构1-小组1
- **所属小组群**: 高额案件组
- **状态**: 启用

---

## 👥 小组2：百腾企业-机构1-小组2

### 主管 - 周八
- **登录ID**: `team2_supervisor`
- **密码**: `password123`
- **账号编码**: `ADMIN_team2_supervisor`
- **手机**: 13800000013
- **邮箱**: team2_supervisor@example.com
- **角色**: supervisor (主管)
- **所属小组**: 百腾企业-机构1-小组2
- **所属小组群**: 普通案件组
- **状态**: 启用

### 组长 - 吴九
- **登录ID**: `team2_leader`
- **密码**: `password123`
- **账号编码**: `ADMIN_team2_leader`
- **手机**: 13800000014
- **邮箱**: team2_leader@example.com
- **角色**: team_leader (组长)
- **所属小组**: 百腾企业-机构1-小组2
- **所属小组群**: 普通案件组
- **状态**: 启用

### 质检员 - 郑十
- **登录ID**: `team2_inspector`
- **密码**: `password123`
- **账号编码**: `ADMIN_team2_inspector`
- **手机**: 13800000015
- **邮箱**: team2_inspector@example.com
- **角色**: quality_inspector (质检员)
- **所属小组**: 百腾企业-机构1-小组2
- **所属小组群**: 普通案件组
- **状态**: 启用

---

## 📈 数据统计

- **小组数量**: 2个
- **管理员总数**: 6个
- **每小组管理员**: 3个
  - 主管: 1个
  - 组长: 1个
  - 质检员: 1个

### 按角色统计
| 角色 | 数量 | 登录ID列表 |
|------|------|-----------|
| 主管 (supervisor) | 2 | `team1_supervisor`, `team2_supervisor` |
| 组长 (team_leader) | 2 | `team1_leader`, `team2_leader` |
| 质检员 (quality_inspector) | 2 | `team1_inspector`, `team2_inspector` |

---

## 🔐 快速登录表

| 姓名 | 登录ID | 密码 | 角色 | 所属小组 |
|------|--------|------|------|----------|
| 王五 | `team1_supervisor` | `password123` | 主管 | 小组1 |
| 赵六 | `team1_leader` | `password123` | 组长 | 小组1 |
| 孙七 | `team1_inspector` | `password123` | 质检员 | 小组1 |
| 周八 | `team2_supervisor` | `password123` | 主管 | 小组2 |
| 吴九 | `team2_leader` | `password123` | 组长 | 小组2 |
| 郑十 | `team2_inspector` | `password123` | 质检员 | 小组2 |

---

## 📍 如何查看和使用

### 1. 在管理后台查看
1. 登录管理控台
2. 进入 **人员与机构管理 → 小组管理员管理**
3. 可以看到所有创建的小组管理员
4. 可以按照机构、小组、角色进行筛选

### 2. 使用管理员账号登录
1. 在登录页面选择对应的登录方式
2. 使用任意管理员的登录ID和密码登录
3. 密码统一为: `password123`

### 3. 权限说明
- **主管 (supervisor)**: 拥有小组的全面管理权限
- **组长 (team_leader)**: 负责小组日常运营和团队管理
- **质检员 (quality_inspector)**: 负责案件质量检查和评估

---

## 🔧 数据关联

### 小组1 (百腾企业-机构1-小组1)
```
甲方: 百腾企业 (ID: 1)
  └─ 机构: 百腾企业-机构1 (ID: 1)
      └─ 小组群: 高额案件组 (ID: 1)
          └─ 小组: 百腾企业-机构1-小组1 (ID: 1)
              ├─ 主管: 王五 (team1_supervisor)
              ├─ 组长: 赵六 (team1_leader)
              └─ 质检员: 孙七 (team1_inspector)
```

### 小组2 (百腾企业-机构1-小组2)
```
甲方: 百腾企业 (ID: 1)
  └─ 机构: 百腾企业-机构1 (ID: 1)
      └─ 小组群: 普通案件组 (ID: 2)
          └─ 小组: 百腾企业-机构1-小组2 (ID: 2)
              ├─ 主管: 周八 (team2_supervisor)
              ├─ 组长: 吴九 (team2_leader)
              └─ 质检员: 郑十 (team2_inspector)
```

---

## 📝 脚本说明

### 创建脚本
- **文件**: `backend/create_team_admins_mock_data.py`
- **功能**:
  1. 读取所有小组
  2. 为每个小组创建3个不同角色的管理员
  3. 自动生成唯一的登录ID和账号编码
  4. 设置默认密码为 `password123`
  5. 自动关联到小组和小组群

### 命名规则
- **登录ID**: `team{小组ID}_{角色后缀}`
  - 主管: `team{ID}_supervisor`
  - 组长: `team{ID}_leader`
  - 质检员: `team{ID}_inspector`
- **账号编码**: `ADMIN_{登录ID}`
- **邮箱**: `{登录ID}@example.com`
- **手机**: 自动生成（138开头）

---

## ⚠️ 注意事项

1. **统一密码**: 所有管理员的初始密码都是 `password123`，建议在生产环境中修改
2. **唯一性检查**: 脚本会自动检查登录ID是否已存在，避免重复创建
3. **数据关联**: 管理员会自动关联到所属的小组和小组群
4. **角色权限**: 不同角色的具体权限需要在系统中配置
5. **邮箱域名**: 使用的是测试域名 `@example.com`

---

## 🚀 测试建议

1. ✅ 使用不同角色的账号登录系统
2. ✅ 验证每个角色的权限范围
3. ✅ 测试跨小组的数据访问权限
4. ✅ 验证小组群级别的数据查看权限
5. ✅ 测试管理员的增删改查功能

---

## 📅 创建时间
2025年11月20日

## 🔄 相关文档
- [小组群Mock数据说明.md](./小组群Mock数据说明.md) - 小组群和SPV管理员的Mock数据
- [小组群功能说明.md](./小组群功能说明.md) - 小组群功能的详细说明

