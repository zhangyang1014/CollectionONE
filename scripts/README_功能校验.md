# 功能校验脚本使用指南

## 📋 脚本列表

### 1. `verify_console_features_v3.py` - 主校验脚本（推荐）

**功能**: 校验Excel中的控台功能点与实际实现的差异，并将结果写入Excel的G列和H列。

**使用方法**:
```bash
python3 scripts/verify_console_features_v3.py
```

**输出**:
- 在Excel的G列写入实现状态（已实现/未实现/部分实现等）
- 在Excel的H列写入详细差异说明（新增）
- 用不同颜色标识状态（绿色=已实现，橙色=部分实现，蓝色=Mock，红色=未实现）
- 在终端显示统计信息（包括差异统计）

---

### 2. `read_excel_features.py` - 读取功能点列表

**功能**: 读取并显示Excel中所有功能点的列表。

**使用方法**:
```bash
python3 scripts/read_excel_features.py
```

**输出**:
- 显示Excel的前10行数据
- 按模块分组显示所有功能点
- 显示总功能数

---

### 3. `generate_implementation_report.py` - 生成详细报告

**功能**: 按模块分类生成功能实现情况的详细报告。

**使用方法**:
```bash
python3 scripts/generate_implementation_report.py
```

**输出**:
- 按模块分类显示实现情况
- 显示每个模块的完成率
- 列出已实现、未实现、Mock等功能

---

### 4. `show_differences.py` - 显示差异功能（新增）

**功能**: 显示所有有差异的功能点，帮助快速定位需要关注的功能。

**使用方法**:
```bash
python3 scripts/show_differences.py
```

**输出**:
- 实现方式不同的功能列表
- 部分差异的功能列表
- 命名差异的功能列表
- 差异汇总统计

---

## 🔄 典型工作流程

### 初次校验

```bash
# 1. 读取功能点列表（了解有哪些功能）
python3 scripts/read_excel_features.py

# 2. 运行校验（写入G列和H列）
python3 scripts/verify_console_features_v3.py

# 3. 查看差异功能
python3 scripts/show_differences.py

# 4. 生成详细报告
python3 scripts/generate_implementation_report.py
```

### 更新实现后重新校验

```bash
# 直接运行校验脚本即可
python3 scripts/verify_console_features_v3.py
```

### 快速查看差异

```bash
# 只查看有差异的功能
python3 scripts/show_differences.py
```

---

## 📊 Excel输出格式

### G列（实现状态）内容格式

#### 已实现
```
已实现
```
- 颜色: 绿色 (008000)
- 表示功能已完整实现

#### 部分实现
```
部分实现
```
- 颜色: 橙色 (FF8C00)
- 表示功能部分实现

#### 后端Mock
```
后端Mock
```
- 颜色: 蓝色 (0000FF)
- 表示后端Mock接口

#### 未实现
```
未实现
```
- 颜色: 红色 (FF0000)
- 表示功能未实现

#### 待定
```
待定
```
- 颜色: 灰色 (808080)
- 表示功能待定

### H列（实现差异详细说明）内容格式（新增）

#### 无差异
```
✅ 无差异 - 完整实现登录、Token管理、登出功能
```
- 颜色: 绿色 (008000)
- 表示实现与需求完全一致

#### 部分差异
```
⚠️ 部分差异 - 工作台首页已实现，但月度绩效数据使用Mock数据
```
- 颜色: 橙色 (FF8C00)
- 表示功能实现但部分内容Mock或简化

#### 实现方式不同
```
⚠️ 实现方式不同 - 原设计是自动匹配JSON，现在是手动配置
```
- 颜色: 橙色 (FF8C00)
- 表示功能实现但方式与需求不同

#### 命名差异
```
⚠️ 命名差异 - 页面名称为'字段映射配置'，功能是管理自定义拓展字段
```
- 颜色: 橙色 (FF8C00)
- 表示功能实现但命名不同

#### 未实现
```
❌ 未实现 - 手动分案功能不在当前版本范围
```
- 颜色: 红色 (FF0000)
- 说明未实现原因

#### 待定
```
⚪ 待定 - 功能需求待明确
```
- 颜色: 灰色 (808080)
- 表示功能待定

---

## 🛠️ 自定义校验规则

如果需要修改校验规则，请编辑 `verify_console_features_v2.py` 中的 `check_feature_implementation()` 函数。

### 添加新功能的校验规则

```python
# 在 check_feature_implementation() 函数中添加
if "您的功能关键词" in str(description):
    return ("已实现", "已实现 - /your/route/path")
```

### 状态类型

- `"已实现"` - 功能已完全实现
- `"部分实现"` - 功能部分实现
- `"未实现"` - 功能未实现
- `"后端Mock"` - 后端Mock接口
- `"待定"` - 功能待定

---

## 📈 统计说明

### 总体完成率计算

```
完成率 = (已实现 + 部分实现 + 后端Mock) / 总功能数 * 100%
```

**说明**:
- **已实现**: 前端+后端完整实现
- **部分实现**: 部分功能已实现
- **后端Mock**: 后端接口已Mock，前端可能未实现
- **未实现**: 完全未实现
- **待定**: 功能需求待定

---

## 🔍 常见问题

### Q1: 为什么有些功能显示"待确认"？

A: 脚本无法自动判断该功能的实现情况，需要人工确认。通常是因为：
- 功能描述不够明确
- 功能可能在多个模块中实现
- 需要深入代码才能确认

### Q2: 如何修改Excel文件路径？

A: 修改脚本中的以下代码：
```python
excel_path = project_root / "PRD需求文档" / "CCO 系统功能设计.xlsx"
```

### Q3: 如何只校验特定模块？

A: 在 `check_feature_implementation()` 函数开头添加模块过滤：
```python
if module and "您要跳过的模块" in str(module):
    return ("跳过", "未校验此模块")
```

---

## 📝 维护建议

### 定期更新

每次实现新功能后，应该：
1. 更新 `check_feature_implementation()` 中的规则
2. 重新运行校验脚本
3. 查看并更新Excel中的G列
4. 生成新的报告文档

### 版本控制

建议将校验结果文档提交到Git，以便追踪功能实现进度：

```bash
git add "✅-控台功能点校验完成.md"
git add "PRD需求文档/CCO 系统功能设计.xlsx"
git commit -m "更新功能校验结果"
```

---

## 🎯 下一步

查看详细的校验结果，请阅读:
- [✅-控台功能点校验完成.md](../✅-控台功能点校验完成.md)

---

**创建时间**: 2025-11-27  
**最后更新**: 2025-11-27


