# IM端完整数据初始化说明

## 📋 概述

本文档记录了IM端催员登录和测试数据的完整初始化流程。

## ✅ 已完成的工作

### 1. 催员账号创建

**脚本**: `create_im_collectors.py`

创建了6个IM端测试催员账号，包括BTQ（墨西哥）和BTSK（印度）两个机构：

| 催员ID | 姓名 | 机构ID | 密码 | 角色 |
|-------|------|--------|------|------|
| BTQ001 | Carlos Méndez | 1 | 123456 | 高级催员 |
| BTQ002 | María González | 1 | 123456 | 催员 |
| BTQ003 | José Ramírez | 1 | 123456 | 催员 |
| BTSK001 | Raj Sharma | 2 | 123456 | 团队长 |
| BTSK002 | Priya Patel | 2 | 123456 | 高级催员 |
| BTSK003 | Amit Kumar | 2 | 123456 | 催员 |

**运行方式**:
```bash
cd backend
source venv/bin/activate
python create_im_collectors.py
```

### 2. 案件和相关数据创建

**脚本**: `create_im_cases_simple.py`

使用SQL直接插入方式（避免SQLAlchemy 2.x的RETURNING子句问题），为每个催员创建了完整的测试数据：

- **案件数量**: 每个催员10个案件，共60个
- **联系人**: 每个案件2-3个联系人，共149个
- **沟通记录**: 每个联系人1-2条记录，共233条

**数据特征**:
- 案件状态：pending_repayment、partial_repayment
- 逾期天数：1-90天随机
- 贷款金额：5,000-50,000随机
- 联系人关系：本人、配偶、朋友、同事、亲属
- 沟通渠道：phone、sms、whatsapp
- 沟通结果：answered、no_answer、busy

**运行方式**:
```bash
cd backend
python3 create_im_cases_simple.py
```

### 3. 登录测试

**脚本**: `test_im_login.py`

提供自动化登录测试，验证所有6个账号的登录功能：

```bash
cd backend
python3 test_im_login.py
```

**测试结果**:
```
✅ 成功: 6/6 个账号
❌ 失败: 0/6 个账号
🎉 所有测试通过！
```

## 🚀 快速开始

### 完整初始化流程

```bash
# 1. 进入后端目录
cd backend

# 2. 激活虚拟环境
source venv/bin/activate

# 3. 创建IM催员账号
python create_im_collectors.py

# 4. 创建案件和相关数据
python3 create_im_cases_simple.py

# 5. 测试登录（可选）
python3 test_im_login.py
```

### 登录使用

1. 访问 IM端登录页: http://localhost:5173/im/login
2. 输入测试账号:
   - 机构ID: `1`
   - 催员ID: `BTQ001`
   - 密码: `123456`
3. 完成人脸识别和验证码
4. 登录成功后进入工作台

## 📊 数据统计

### 各催员案件分配

| 催员ID | 姓名 | 案件数 |
|-------|------|--------|
| BTQ001 | Carlos Méndez | 10 |
| BTQ002 | María González | 10 |
| BTQ003 | José Ramírez | 10 |
| BTSK001 | Raj Sharma | 10 |
| BTSK002 | Priya Patel | 10 |
| BTSK003 | Amit Kumar | 10 |

**总计**: 60个案件，149个联系人，233条沟通记录

## 🔧 技术说明

### 密码哈希

使用SHA256哈希存储密码（避免bcrypt兼容性问题）：

```python
import hashlib
password_hash = hashlib.sha256("123456".encode()).hexdigest()
# 结果: 8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92
```

后端登录验证时，会自动检测哈希长度（64字符）并使用对应的验证方式。

### SQLite Autoincrement 问题

由于SQLAlchemy 2.x与SQLite的RETURNING子句兼容性问题，`create_im_cases_simple.py` 使用原生SQL插入：

- 手动管理ID序列
- 避免RETURNING子句
- 直接使用sqlite3模块

## 📁 相关文件

### 数据初始化脚本

| 文件 | 用途 |
|------|------|
| `create_im_collectors.py` | 创建IM端催员账号 |
| `create_im_cases_simple.py` | 创建案件和相关数据 |
| `test_im_login.py` | 测试登录功能 |
| `init_im_test_data.py` | 旧版初始化脚本（有SQLAlchemy问题，已废弃） |

### 数据文件

| 文件 | 内容 |
|------|------|
| `collectors_data.json` | 催员基础信息 |
| `cco_test.db` | SQLite数据库文件 |
| `cco_test.db.backup_*` | 数据库备份文件 |

## 🐛 问题记录

### 问题1: SQLAlchemy RETURNING子句错误

**错误信息**:
```
sqlite3.IntegrityError: NOT NULL constraint failed: case_contacts.id
```

**原因**: SQLAlchemy 2.x使用RETURNING子句，但SQLite的某些表定义不支持

**解决方案**: 使用原生SQL直接插入（`create_im_cases_simple.py`）

### 问题2: bcrypt密码验证错误

**错误信息**:
```
ValueError: password cannot be longer than 72 bytes
```

**原因**: bcrypt模块版本兼容性问题

**解决方案**: 改用SHA256哈希存储密码

### 问题3: 路由守卫拦截

**问题**: IM端登录后跳转到管理后台登录页

**原因**: 路由守卫顺序问题，管理后台检查先于IM端检查

**解决方案**: 调整路由守卫顺序，优先检查IM端路由

## 🔄 数据重置

如需重置数据：

```bash
cd backend

# 1. 备份当前数据库
cp cco_test.db cco_test.db.backup_$(date +%Y%m%d_%H%M%S)

# 2. 恢复到某个备份点（可选）
cp cco_test.db.backup_YYYYMMDD_HHMMSS cco_test.db

# 3. 重新创建催员和案件
source venv/bin/activate
python create_im_collectors.py
python3 create_im_cases_simple.py
```

## 📚 相关文档

- [IM端登录测试说明](./IM端登录测试说明.md)
- [催员测试账号](../说明文档/后端/催员测试账号.md)
- [CCO-IM端登录系统实现说明](../说明文档/前端/CCO-IM端登录系统实现说明.md)
- [催员登录人脸识别功能实现说明](../说明文档/后端/催员登录人脸识别功能实现说明.md)

## ✨ 功能验证

登录后可验证以下功能：

1. ✅ **案件列表**: 显示10个分配给当前催员的案件
2. ✅ **案件详情**: 查看案件的详细信息
3. ✅ **联系人列表**: 每个案件2-3个联系人
4. ✅ **沟通记录**: 每个联系人1-2条历史沟通记录
5. ✅ **IM对话**: 可以查看和发送消息（需要后端WhatsApp集成）

## 🎯 下一步

- [ ] 接入真实的WhatsApp API
- [ ] 添加更多的案件字段数据
- [ ] 创建PTP（承诺还款）记录
- [ ] 添加质检记录
- [ ] 完善绩效统计数据

---

*最后更新: 2025-11-20*
*状态: ✅ 数据初始化完成*
*催员账号: 6个 | 案件: 60个 | 联系人: 149个 | 沟通记录: 233条*

