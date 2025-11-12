# CCO催收系统设计方案

## 版本历史与变更记录

### v2.1.0 (2024-11-11) - 甲方字段管理重构

**核心变更：**

1. **甲方自定义字段管理重新定义** 🔄
   - 重新设计"甲方自定义字段管理"页面，统一管理标准字段映射和自定义字段
   - 新增字段来源标识：标准字段 / 自定义字段
   - 新增甲方字段映射列：甲方字段标识、甲方字段ID
   - 新增队列可见性配置：支持配置字段在特定队列中隐藏
   - 支持拖拽排序，自动更新`sort_order`
   - **排序继承规则**：新建甲方时自动继承标准字段及排序，甲方调整后不再受标准字段排序约束
   - 标准字段：只能编辑映射关系和排序，不可删除
   - 自定义字段：可完全编辑和删除

2. **字段排序规则系统** 📊
   - 新甲方创建时，自动继承所有标准字段及其当前排序
   - 继承后的`sort_order`为快照，不再跟随标准字段管理的排序变化
   - 甲方可自由调整字段排序（包括标准字段和自定义字段）
   - 支持拖拽交互调整排序
   - 新增的标准字段会自动添加到所有甲方，但排序为最后

### v2.0.0 (2024-11-11) - 架构重大升级

**核心变更：**

1. **字段映射机制** ✨
   - 新增甲方字段映射功能，支持甲方使用自己的字段命名推送数据
   - `tenant_field_configs` 表增加 `tenant_field_key` 和 `tenant_field_name` 字段
   - 数据同步时自动进行字段映射转换
   - 支持批量导入/导出映射配置

2. **字段联动规则可配置化** ⚙️
   - 完全重构 `field_dependencies` 表结构
   - 支持多字段联动（如：沟通状态 + 联系人关系 → 沟通结果）
   - 新增可视化配置界面，无需硬编码
   - 支持三种联动类型：显示/隐藏、选项变化、必填切换
   - 支持规则优先级、启用/禁用、测试功能
   - 支持JSON格式批量导入导出

3. **案件队列管理系统** 📋
   - 新增 `case_queues` 表（M1、M2、M3+、法务队列等）
   - 新增 `queue_field_configs` 表，支持按队列配置字段显示
   - 队列级别字段配置：is_visible、is_required、is_readonly、is_editable
   - 配置优先级：队列配置 > 甲方配置 > 字段默认配置
   - 支持队列间案件自动流转
   - 支持从其他队列复制配置

4. **完整组织架构体系** 🏢
   - 新增 `collection_agencies` 表（催收机构）
   - 新增 `collection_teams` 表（催收小组）
   - 新增 `collectors` 表（催员）
   - 新增 `case_assignment_history` 表（案件分配历史）
   - 支持四级组织层级：甲方 → 机构 → 小组 → 催员
   - `cases` 表新增：agency_id、team_id、collector_id、overdue_days 等字段

5. **案件分配与流转** 🔄
   - 支持逐级分配：机构 → 小组 → 催员
   - 支持自动分配：均衡分配、随机分配、技能匹配
   - 支持案件转移和批量回收
   - 完整的分配历史记录和审计

6. **前端功能增强** 💻
   - 新增"组织管理"模块（机构、小组、催员管理）
   - 新增"队列管理"模块（队列配置、字段配置、分配规则）
   - 新增"报表统计"模块（机构/小组/催员绩效）
   - 完善字段联动配置界面
   - 甲方字段映射配置界面

7. **API接口扩展** 🔌
   - 新增字段映射管理接口（7.2节）
   - 新增字段联动规则接口（7.3节）
   - 新增队列管理接口（7.4节）
   - 新增组织管理接口（7.5节）
   - 新增案件分配/流转接口

**数据库表变更：**
- 新增表：collection_agencies、collection_teams、collectors、case_queues、queue_field_configs、case_assignment_history
- 修改表：tenant_field_configs（+tenant_field_key, +tenant_field_name）
- 修改表：field_dependencies（+rule_name, +rule_description, +priority, +is_enabled，source_field_ids改为JSON数组）
- 修改表：cases（+agency_id, +team_id, +collector_id, +queue_id, +overdue_days, +outstanding_amount等）

**影响范围：**
- 数据库结构：8个新表，3个表结构修改
- 前端页面：新增约15个管理页面
- API接口：新增约40个接口
- 业务流程：案件分配流程、字段配置流程重构

---

## 一、字段分组结构

根据提供的CSV文件，系统包含以下7个字段分组：

1. **客户基本信息** (Customer Basic Info)
   - 基础身份信息（Identity Information）
   - 教育（Education）
   - 职业信息（Employment）
   - 用户行为与信用（User Behavior & Credit）

2. **客户活动信息** (Customer Activity Info)
   - 待补充（根据业务需求）

3. **贷款详情** (Loan Details)

4. **分期详情** (Installment Details)

5. **借款记录** (Loan Record)

6. **还款记录** (Repayment Record)

7. **催记** (Collection Record)

---

## 二、字段类型定义

### 2.1 基础类型
- **String** - 文本类型
- **Integer** - 整数类型
- **Decimal** - 小数类型
- **Boolean** - 布尔类型
- **Date** - 日期类型
- **Datetime** - 日期时间类型
- **Enum** - 枚举选择类型
- **FileList** - 文件列表类型
- **Button** - 按钮类型（特殊交互）

### 2.2 字段属性
- **字段名称** (field_name) - 中文显示名称
- **字段标识** (field_key) - 英文唯一标识（系统内部统一标识）
- **字段类型** (field_type) - 数据类型
- **是否必填** (required) - 必填/非必填
- **是否拓展字段** (is_extended) - 标准字段/拓展字段
- **字段说明** (description) - 字段描述
- **示例值** (example) - 示例数据
- **验证规则** (validation_rules) - JSON格式的验证规则

### 2.3 字段映射机制
由于不同甲方客户的系统中字段命名规范不同，系统支持字段映射功能：

- **系统字段标识** (field_key) - SaaS系统内部统一使用的字段标识（如：`user_id`）
- **甲方字段标识** (tenant_field_key) - 甲方系统中实际使用的字段标识（如：`UID`）
- **映射关系** - 在甲方字段配置中建立映射关系，实现自动转换

**应用场景：**
- 甲方推送数据时，使用自己的字段名（如：`UID`）
- 系统接收后自动映射为标准字段名（如：`user_id`）
- 系统内部统一使用标准字段名处理
- 向甲方返回数据时，可选择使用甲方字段名

---

## 三、数据库表结构设计

### 3.1 字段分组表 (field_groups)

```sql
CREATE TABLE field_groups (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    group_key VARCHAR(100) NOT NULL UNIQUE COMMENT '分组标识',
    group_name VARCHAR(200) NOT NULL COMMENT '分组名称（中文）',
    group_name_en VARCHAR(200) COMMENT '分组名称（英文）',
    parent_id BIGINT DEFAULT NULL COMMENT '父分组ID（支持嵌套分组）',
    sort_order INT DEFAULT 0 COMMENT '排序顺序',
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_parent_id (parent_id),
    INDEX idx_sort_order (sort_order)
) COMMENT '字段分组表';
```

### 3.2 标准字段定义表 (standard_fields)

```sql
CREATE TABLE standard_fields (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    field_key VARCHAR(100) NOT NULL UNIQUE COMMENT '字段唯一标识',
    field_name VARCHAR(200) NOT NULL COMMENT '字段名称（中文）',
    field_name_en VARCHAR(200) COMMENT '字段名称（英文）',
    field_type VARCHAR(50) NOT NULL COMMENT '字段类型',
    field_group_id BIGINT NOT NULL COMMENT '所属分组ID',
    is_required BOOLEAN DEFAULT FALSE COMMENT '是否必填',
    is_extended BOOLEAN DEFAULT FALSE COMMENT '是否为拓展字段',
    description TEXT COMMENT '字段说明',
    example_value TEXT COMMENT '示例值',
    validation_rules JSON COMMENT '验证规则（JSON格式）',
    enum_options JSON COMMENT '枚举选项（如果是Enum类型）',
    sort_order INT DEFAULT 0 COMMENT '排序顺序',
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    is_deleted BOOLEAN DEFAULT FALSE COMMENT '软删除标记',
    deleted_at DATETIME DEFAULT NULL COMMENT '删除时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_group_id (field_group_id),
    INDEX idx_sort_order (sort_order),
    INDEX idx_is_deleted (is_deleted),
    FOREIGN KEY (field_group_id) REFERENCES field_groups(id)
) COMMENT '标准字段定义表';
```

### 3.3 字段联动规则表 (field_dependencies)

```sql
CREATE TABLE field_dependencies (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    rule_name VARCHAR(200) NOT NULL COMMENT '规则名称（便于识别）',
    rule_description TEXT COMMENT '规则描述',
    source_field_ids JSON NOT NULL COMMENT '源字段ID数组（支持多字段联动）',
    target_field_id BIGINT NOT NULL COMMENT '目标字段ID（被联动字段）',
    dependency_type VARCHAR(50) NOT NULL COMMENT '联动类型：show/hide/options_change/required_toggle',
    dependency_rule JSON NOT NULL COMMENT '联动规则配置（JSON格式，包含conditions和actions）',
    priority INT DEFAULT 0 COMMENT '规则优先级（数字越小优先级越高）',
    is_enabled BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_target_field (target_field_id),
    INDEX idx_priority (priority),
    INDEX idx_is_enabled (is_enabled),
    FOREIGN KEY (target_field_id) REFERENCES standard_fields(id)
) COMMENT '字段联动规则表';
```

**字段说明：**
- `rule_name`: 规则名称，如"沟通状态联动沟通结果"
- `source_field_ids`: JSON数组格式，支持单字段或多字段联动，如：`[101, 102]`
- `dependency_rule`: 完整的联动规则配置，包含多个条件规则和对应的动作
- `priority`: 当多个规则应用于同一目标字段时，按优先级执行

**联动规则JSON示例（简单单字段联动）：**
```json
{
  "source_fields": [
    {
      "field_id": 101,
      "field_key": "communication_status"
    }
  ],
  "rules": [
    {
      "priority": 1,
      "conditions": {
        "communication_status": {
          "operator": "=",
          "value": "可联"
        }
      },
      "target_options": [
        {"value": "承诺还款", "label": "承诺还款"},
        {"value": "拒绝还款", "label": "拒绝还款"},
        {"value": "与借款人不相关", "label": "与借款人不相关"},
        {"value": "其它", "label": "其它"}
      ]
    }
  ]
}
```

**联动规则JSON示例（复杂多字段联动）：**
```json
{
  "source_fields": [
    {
      "field_id": 101,
      "field_key": "communication_status"
    },
    {
      "field_id": 102,
      "field_key": "contact_relation"
    }
  ],
  "rules": [
    {
      "priority": 1,
      "conditions": {
        "communication_status": {"operator": "=", "value": "可联"},
        "contact_relation": {"operator": "=", "value": "本人"}
      },
      "target_options": [
        {"value": "承诺还款", "label": "承诺还款", "is_default": false},
        {"value": "拒绝还款", "label": "拒绝还款", "is_default": false},
        {"value": "与借款人不相关", "label": "与借款人不相关", "is_default": false},
        {"value": "其它", "label": "其它", "is_default": false}
      ]
    },
    {
      "priority": 2,
      "conditions": {
        "communication_status": {"operator": "=", "value": "可联"},
        "contact_relation": {"operator": "IN", "value": ["母亲", "父亲", "配偶"]}
      },
      "target_options": [
        {"value": "与借款人相关", "label": "与借款人相关"},
        {"value": "承诺代还", "label": "承诺代还"},
        {"value": "承诺转告", "label": "承诺转告"}
      ]
    }
  ],
  "default_behavior": {
    "type": "show_all"
  }
}
```

### 3.4 甲方配置表 (tenants)

```sql
CREATE TABLE tenants (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_code VARCHAR(100) NOT NULL UNIQUE COMMENT '甲方编码',
    tenant_name VARCHAR(200) NOT NULL COMMENT '甲方名称',
    tenant_name_en VARCHAR(200) COMMENT '甲方名称（英文）',
    country_code VARCHAR(10) COMMENT '国家代码',
    timezone VARCHAR(50) DEFAULT 'UTC' COMMENT '时区',
    currency_code VARCHAR(10) DEFAULT 'USD' COMMENT '货币代码',
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_tenant_code (tenant_code)
) COMMENT '甲方配置表';
```

### 3.4.1 催收机构表 (collection_agencies)

```sql
CREATE TABLE collection_agencies (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL COMMENT '所属甲方ID',
    agency_code VARCHAR(100) NOT NULL COMMENT '机构编码',
    agency_name VARCHAR(200) NOT NULL COMMENT '机构名称',
    agency_name_en VARCHAR(200) COMMENT '机构名称（英文）',
    contact_person VARCHAR(100) COMMENT '联系人',
    contact_phone VARCHAR(50) COMMENT '联系电话',
    contact_email VARCHAR(100) COMMENT '联系邮箱',
    address TEXT COMMENT '机构地址',
    description TEXT COMMENT '机构描述',
    sort_order INT DEFAULT 0 COMMENT '排序顺序',
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_tenant_agency_code (tenant_id, agency_code),
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_is_active (is_active),
    FOREIGN KEY (tenant_id) REFERENCES tenants(id)
) COMMENT '催收机构表';
```

**说明：**
- 一个甲方下可以有多个催收机构（自营机构或外包机构）
- 每个催收机构有独立的编码和联系方式
- 可以配置不同机构处理不同类型的案件

### 3.4.2 催收小组表 (collection_teams)

```sql
CREATE TABLE collection_teams (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    agency_id BIGINT NOT NULL COMMENT '所属催收机构ID',
    team_code VARCHAR(100) NOT NULL COMMENT '小组编码',
    team_name VARCHAR(200) NOT NULL COMMENT '小组名称',
    team_name_en VARCHAR(200) COMMENT '小组名称（英文）',
    team_leader_id BIGINT COMMENT '组长ID（催员ID）',
    team_type VARCHAR(50) COMMENT '小组类型（如：电催组、外访组、法务组等）',
    description TEXT COMMENT '小组描述',
    max_case_count INT DEFAULT 0 COMMENT '最大案件数量（0表示不限制）',
    sort_order INT DEFAULT 0 COMMENT '排序顺序',
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_agency_team_code (agency_id, team_code),
    INDEX idx_agency_id (agency_id),
    INDEX idx_team_leader_id (team_leader_id),
    INDEX idx_is_active (is_active),
    FOREIGN KEY (agency_id) REFERENCES collection_agencies(id)
) COMMENT '催收小组表';
```

**说明：**
- 一个催收机构下可以有多个催收小组
- 小组可以按类型划分（电催、外访、法务等）
- 支持设置小组的最大案件容量

### 3.4.3 催员表 (collectors)

```sql
CREATE TABLE collectors (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    team_id BIGINT NOT NULL COMMENT '所属小组ID',
    user_id BIGINT NOT NULL COMMENT '关联用户ID（系统用户表）',
    collector_code VARCHAR(100) NOT NULL COMMENT '催员编码',
    collector_name VARCHAR(100) NOT NULL COMMENT '催员姓名',
    mobile_number VARCHAR(50) COMMENT '手机号码',
    email VARCHAR(100) COMMENT '邮箱',
    employee_no VARCHAR(50) COMMENT '工号',
    collector_level VARCHAR(50) COMMENT '催员等级（初级/中级/高级/资深）',
    max_case_count INT DEFAULT 100 COMMENT '最大案件数量',
    current_case_count INT DEFAULT 0 COMMENT '当前案件数量',
    specialties JSON COMMENT '擅长领域（JSON数组，如：["高额案件","法务处理"]）',
    performance_score DECIMAL(5,2) COMMENT '绩效评分',
    status VARCHAR(50) DEFAULT 'active' COMMENT '状态：active/休假/离职等',
    hire_date DATE COMMENT '入职日期',
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_team_collector_code (team_id, collector_code),
    UNIQUE KEY uk_user_id (user_id),
    INDEX idx_team_id (team_id),
    INDEX idx_collector_code (collector_code),
    INDEX idx_status (status),
    INDEX idx_is_active (is_active),
    FOREIGN KEY (team_id) REFERENCES collection_teams(id)
) COMMENT '催员表';
```

**说明：**
- 催员归属于催收小组
- 关联系统用户表（用于登录和权限管理）
- 支持设置催员的工作容量和专长
- 记录催员的绩效数据

### 3.5 案件队列表 (case_queues)

```sql
CREATE TABLE case_queues (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL COMMENT '所属甲方ID',
    queue_code VARCHAR(100) NOT NULL COMMENT '队列编码（如：M1, M2, M3+, LEGAL）',
    queue_name VARCHAR(200) NOT NULL COMMENT '队列名称',
    queue_name_en VARCHAR(200) COMMENT '队列名称（英文）',
    queue_description TEXT COMMENT '队列描述',
    overdue_days_min INT COMMENT '逾期天数最小值',
    overdue_days_max INT COMMENT '逾期天数最大值',
    sort_order INT DEFAULT 0 COMMENT '排序顺序',
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_tenant_queue_code (tenant_id, queue_code),
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_sort_order (sort_order),
    FOREIGN KEY (tenant_id) REFERENCES tenants(id)
) COMMENT '案件队列表';
```

**队列配置说明：**
- 不同甲方可以配置不同的案件队列
- 队列通常按逾期天数划分（如：M1=1-30天，M2=31-60天，M3+=61天以上）
- 队列可以是其他维度（如：金额、区域、催收策略等）

**队列示例数据：**
```sql
INSERT INTO case_queues (tenant_id, queue_code, queue_name, queue_name_en, overdue_days_min, overdue_days_max, sort_order)
VALUES 
(1, 'M1', 'M1队列', 'M1 Queue', 1, 30, 1),
(1, 'M2', 'M2队列', 'M2 Queue', 31, 60, 2),
(1, 'M3', 'M3队列', 'M3 Queue', 61, 90, 3),
(1, 'M3+', 'M3+队列', 'M3+ Queue', 91, NULL, 4),
(1, 'LEGAL', '法务队列', 'Legal Queue', NULL, NULL, 5);
```

### 3.6 队列字段配置表 (queue_field_configs)

```sql
CREATE TABLE queue_field_configs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    queue_id BIGINT NOT NULL COMMENT '队列ID',
    field_id BIGINT NOT NULL COMMENT '字段ID',
    field_type ENUM('standard', 'custom') NOT NULL COMMENT '字段类型',
    is_visible BOOLEAN DEFAULT TRUE COMMENT '是否可见',
    is_required BOOLEAN DEFAULT NULL COMMENT '是否必填（NULL表示使用字段默认设置）',
    is_readonly BOOLEAN DEFAULT FALSE COMMENT '是否只读',
    is_editable BOOLEAN DEFAULT TRUE COMMENT '是否可编辑',
    sort_order INT DEFAULT 0 COMMENT '字段排序',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_queue_field (queue_id, field_id, field_type),
    INDEX idx_queue_id (queue_id),
    INDEX idx_field_id (field_id),
    FOREIGN KEY (queue_id) REFERENCES case_queues(id)
) COMMENT '队列字段配置表';
```

**字段配置说明：**
- `is_visible`: 控制字段在该队列中是否显示
- `is_required`: 队列级别的必填控制，NULL表示使用字段的默认必填设置
- `is_readonly`: 字段是否只读（只能查看不能修改）
- `is_editable`: 字段是否可编辑（结合is_readonly使用）
- 同一个字段在不同队列中可以有不同的配置

**配置优先级：**
1. 队列字段配置（queue_field_configs）- 最高优先级
2. 甲方字段配置（tenant_field_configs）- 中优先级
3. 字段默认配置（standard_fields/custom_fields）- 最低优先级

### 3.7 甲方字段启用配置表 (tenant_field_configs)

```sql
CREATE TABLE tenant_field_configs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL COMMENT '甲方ID',
    field_id BIGINT NOT NULL COMMENT '字段ID（标准字段或自定义字段）',
    field_type ENUM('standard', 'custom') NOT NULL COMMENT '字段类型',
    tenant_field_key VARCHAR(100) COMMENT '甲方侧字段标识（用于字段映射，如：甲方系统中用UID对应系统的user_id）',
    tenant_field_name VARCHAR(200) COMMENT '甲方侧字段名称（甲方系统中的显示名称）',
    is_enabled BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    is_required BOOLEAN DEFAULT FALSE COMMENT '是否必填（可覆盖标准字段的必填设置）',
    is_readonly BOOLEAN DEFAULT FALSE COMMENT '是否只读',
    is_visible BOOLEAN DEFAULT TRUE COMMENT '是否可见',
    sort_order INT DEFAULT 0 COMMENT '排序顺序',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_tenant_field (tenant_id, field_id, field_type),
    UNIQUE KEY uk_tenant_field_key (tenant_id, tenant_field_key),
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_field_id (field_id),
    FOREIGN KEY (tenant_id) REFERENCES tenants(id)
) COMMENT '甲方字段启用配置表';
```

**字段映射说明：**
- `tenant_field_key`: 甲方在其系统中使用的字段标识，如甲方使用 `UID` 对应系统标准字段 `user_id`
- `tenant_field_name`: 甲方在其系统中使用的字段显示名称（可选）
- 如果 `tenant_field_key` 为空，则使用标准字段的 `field_key`
- 数据同步时，系统根据映射关系自动转换字段名

### 3.6 自定义字段定义表 (custom_fields)

```sql
CREATE TABLE custom_fields (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL COMMENT '所属甲方ID',
    field_key VARCHAR(100) NOT NULL COMMENT '字段唯一标识',
    field_name VARCHAR(200) NOT NULL COMMENT '字段名称',
    field_name_en VARCHAR(200) COMMENT '字段名称（英文）',
    field_type VARCHAR(50) NOT NULL COMMENT '字段类型',
    field_group_id BIGINT NOT NULL COMMENT '所属分组ID',
    is_required BOOLEAN DEFAULT FALSE COMMENT '是否必填',
    description TEXT COMMENT '字段说明',
    example_value TEXT COMMENT '示例值',
    validation_rules JSON COMMENT '验证规则',
    enum_options JSON COMMENT '枚举选项（如果是Enum类型）',
    sort_order INT DEFAULT 0 COMMENT '排序顺序',
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    is_deleted BOOLEAN DEFAULT FALSE COMMENT '软删除标记',
    deleted_at DATETIME DEFAULT NULL COMMENT '删除时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_tenant_field_key (tenant_id, field_key),
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_group_id (field_group_id),
    INDEX idx_is_deleted (is_deleted),
    FOREIGN KEY (tenant_id) REFERENCES tenants(id),
    FOREIGN KEY (field_group_id) REFERENCES field_groups(id)
) COMMENT '自定义字段定义表';
```

### 3.7 字段值存储表（案件数据）

#### 3.7.1 案件主表 (cases)

```sql
CREATE TABLE cases (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    case_id VARCHAR(100) NOT NULL UNIQUE COMMENT '案件唯一标识',
    tenant_id BIGINT NOT NULL COMMENT '所属甲方ID',
    agency_id BIGINT COMMENT '所属催收机构ID',
    team_id BIGINT COMMENT '所属催收小组ID',
    collector_id BIGINT COMMENT '分配催员ID',
    queue_id BIGINT COMMENT '所属队列ID',
    loan_id VARCHAR(100) COMMENT '贷款编号',
    user_id VARCHAR(100) COMMENT '用户编号',
    user_name VARCHAR(100) COMMENT '用户姓名',
    case_status VARCHAR(50) COMMENT '案件状态：新案件/催收中/已结清/坏账等',
    overdue_days INT COMMENT '逾期天数（用于自动分配队列）',
    outstanding_amount DECIMAL(15,2) COMMENT '逾期金额',
    assigned_at DATETIME COMMENT '分配时间',
    last_contact_at DATETIME COMMENT '最后联系时间',
    next_follow_up_at DATETIME COMMENT '下次跟进时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_agency_id (agency_id),
    INDEX idx_team_id (team_id),
    INDEX idx_collector_id (collector_id),
    INDEX idx_queue_id (queue_id),
    INDEX idx_case_id (case_id),
    INDEX idx_loan_id (loan_id),
    INDEX idx_user_id (user_id),
    INDEX idx_case_status (case_status),
    INDEX idx_overdue_days (overdue_days),
    INDEX idx_assigned_at (assigned_at),
    FOREIGN KEY (tenant_id) REFERENCES tenants(id),
    FOREIGN KEY (agency_id) REFERENCES collection_agencies(id),
    FOREIGN KEY (team_id) REFERENCES collection_teams(id),
    FOREIGN KEY (collector_id) REFERENCES collectors(id),
    FOREIGN KEY (queue_id) REFERENCES case_queues(id)
) COMMENT '案件主表';
```

**组织层级关联说明：**
- `tenant_id`: 所属甲方（必填）
- `agency_id`: 所属催收机构（案件分配到机构时填写）
- `team_id`: 所属催收小组（案件分配到小组时填写）
- `collector_id`: 分配的催员（案件分配到个人时填写）
- `queue_id`: 所属队列，决定字段配置

**案件分配流程：**
```
案件创建 → 甲方 → 催收机构 → 催收小组 → 催员
```

**说明：**
- 案件逐级分配，每一级都会记录
- 案件可以在不同机构、小组、催员间流转
- `queue_id` 决定了该案件展示哪些字段
- 案件状态变化或逾期天数变化时，可以自动流转队列

#### 3.7.2 案件分配历史表 (case_assignment_history)

```sql
CREATE TABLE case_assignment_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    case_id BIGINT NOT NULL COMMENT '案件ID',
    assignment_type VARCHAR(50) NOT NULL COMMENT '分配类型：to_agency/to_team/to_collector/transfer',
    from_agency_id BIGINT COMMENT '原机构ID',
    to_agency_id BIGINT COMMENT '目标机构ID',
    from_team_id BIGINT COMMENT '原小组ID',
    to_team_id BIGINT COMMENT '目标小组ID',
    from_collector_id BIGINT COMMENT '原催员ID',
    to_collector_id BIGINT COMMENT '目标催员ID',
    reason VARCHAR(200) COMMENT '分配/流转原因',
    assigned_by BIGINT COMMENT '分配人ID',
    assigned_by_name VARCHAR(100) COMMENT '分配人姓名',
    assigned_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '分配时间',
    remarks TEXT COMMENT '备注',
    INDEX idx_case_id (case_id),
    INDEX idx_to_agency_id (to_agency_id),
    INDEX idx_to_team_id (to_team_id),
    INDEX idx_to_collector_id (to_collector_id),
    INDEX idx_assigned_at (assigned_at),
    FOREIGN KEY (case_id) REFERENCES cases(id)
) COMMENT '案件分配历史表';
```

**说明：**
- 记录案件在机构、小组、催员之间的所有流转记录
- 支持查询案件的完整分配历史
- 用于审计和绩效分析

#### 3.7.3 标准字段值表 (case_standard_field_values)

```sql
CREATE TABLE case_standard_field_values (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    case_id BIGINT NOT NULL COMMENT '案件ID',
    field_id BIGINT NOT NULL COMMENT '字段ID',
    field_value TEXT COMMENT '字段值（JSON格式存储复杂类型）',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_case_field (case_id, field_id),
    INDEX idx_case_id (case_id),
    INDEX idx_field_id (field_id),
    FOREIGN KEY (case_id) REFERENCES cases(id),
    FOREIGN KEY (field_id) REFERENCES standard_fields(id)
) COMMENT '标准字段值表';
```

#### 3.7.3 自定义字段值表 (case_custom_field_values)

```sql
CREATE TABLE case_custom_field_values (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    case_id BIGINT NOT NULL COMMENT '案件ID',
    field_id BIGINT NOT NULL COMMENT '自定义字段ID',
    field_value TEXT COMMENT '字段值（JSON格式存储复杂类型）',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_case_field (case_id, field_id),
    INDEX idx_case_id (case_id),
    INDEX idx_field_id (field_id),
    FOREIGN KEY (case_id) REFERENCES cases(id),
    FOREIGN KEY (field_id) REFERENCES custom_fields(id)
) COMMENT '自定义字段值表';
```

### 3.8 审计日志表 (audit_logs)

```sql
CREATE TABLE audit_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT COMMENT '甲方ID',
    operation_type VARCHAR(50) NOT NULL COMMENT '操作类型：create/update/delete',
    entity_type VARCHAR(50) NOT NULL COMMENT '实体类型：field/field_group/tenant_config等',
    entity_id BIGINT COMMENT '实体ID',
    field_id BIGINT COMMENT '字段ID（如果是字段相关操作）',
    before_value JSON COMMENT '变更前值',
    after_value JSON COMMENT '变更后值',
    operator_id BIGINT NOT NULL COMMENT '操作人ID',
    operator_name VARCHAR(100) COMMENT '操作人姓名',
    operator_ip VARCHAR(50) COMMENT '操作IP',
    remark TEXT COMMENT '备注',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_entity_type (entity_type),
    INDEX idx_field_id (field_id),
    INDEX idx_created_at (created_at)
) COMMENT '审计日志表';
```

### 3.9 字段筛选配置表 (field_filter_configs)

```sql
CREATE TABLE field_filter_configs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL COMMENT '甲方ID',
    field_id BIGINT NOT NULL COMMENT '字段ID',
    field_type ENUM('standard', 'custom') NOT NULL COMMENT '字段类型',
    filter_type VARCHAR(50) NOT NULL COMMENT '筛选类型：text/number/enum/date',
    filter_config JSON COMMENT '筛选配置（JSON格式）',
    is_enabled BOOLEAN DEFAULT TRUE COMMENT '是否启用筛选',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_tenant_field (tenant_id, field_id, field_type),
    INDEX idx_tenant_id (tenant_id),
    FOREIGN KEY (tenant_id) REFERENCES tenants(id)
) COMMENT '字段筛选配置表';
```

---

## 四、前端菜单结构设计

### 4.1 主菜单结构

```
CCO系统
├── 案件管理
│   ├── 案件列表
│   ├── 案件详情
│   ├── 案件分配
│   └── 批量操作
├── 客户管理
│   ├── 客户列表
│   └── 客户详情
├── 组织管理
│   ├── 催收机构管理
│   │   ├── 机构列表
│   │   ├── 添加/编辑机构
│   │   └── 机构统计
│   ├── 催收小组管理
│   │   ├── 小组列表
│   │   ├── 添加/编辑小组
│   │   └── 小组绩效
│   └── 催员管理
│       ├── 催员列表
│       ├── 添加/编辑催员
│       ├── 催员绩效
│       └── 工作量统计
├── 队列管理
│   ├── 队列列表
│   ├── 队列字段配置
│   └── 队列分配规则
├── 字段配置
│   ├── 标准字段管理
│   ├── 自定义字段管理
│   ├── 字段分组管理
│   ├── 字段联动配置
│   └── 字段筛选配置
├── 权限管理
│   ├── 角色管理
│   ├── 权限配置
│   └── 字段权限配置
├── 甲方管理
│   ├── 甲方列表
│   ├── 甲方字段配置
│   └── 数据同步配置
├── 报表统计
│   ├── 案件统计
│   ├── 机构绩效报表
│   ├── 小组绩效报表
│   ├── 催员绩效报表
│   └── 回款统计
├── 审计日志
│   ├── 操作日志
│   ├── 字段变更日志
│   └── 案件流转日志
└── 系统设置
    ├── 国际化配置
    ├── 时区设置
    └── 系统参数
```

---

## 五、前端界面设计

### 5.1 字段配置主界面

#### 5.1.1 标准字段管理页面

**布局：**
- 左侧：字段分组树形结构（可折叠）
- 右侧：字段列表（表格形式）

**字段列表表格列：**
- 字段名称
- 字段标识
- 字段类型
- 所属分组
- 是否必填
- 是否拓展字段
- 排序
- 操作（编辑、启用/禁用、删除）

**功能：**
- 支持按分组筛选
- 支持字段搜索
- 支持拖拽排序
- 支持批量启用/禁用

#### 5.1.2 自定义字段管理页面

**布局：**
- 顶部：添加自定义字段按钮
- 主体：字段列表（表格形式）

**字段列表表格列：**
- 字段名称
- 字段标识
- 字段类型
- 所属分组
- 是否必填
- 排序
- 操作（编辑、删除）

**添加/编辑字段表单：**
- 字段名称（必填）
- 字段标识（必填，自动生成或手动输入）
- 字段类型（下拉选择：String/Integer/Decimal/Boolean/Date/Datetime/Enum）
- 所属分组（下拉选择）
- 是否必填（开关）
- 字段说明（文本域）
- 示例值（文本输入）
- 验证规则（JSON编辑器，可选）
- 枚举选项（如果是Enum类型，显示选项配置界面）
- 排序（数字输入）

#### 5.1.3 字段分组管理页面

**布局：**
- 树形结构展示分组
- 支持添加、编辑、删除分组
- 支持拖拽调整分组顺序和层级

**分组表单：**
- 分组标识（必填，英文）
- 分组名称（必填，中文）
- 分组名称（英文）
- 父分组（下拉选择，可选）
- 排序（数字输入）

#### 5.1.4 字段联动配置页面

**布局：**
- 顶部：添加联动规则按钮、批量导入按钮
- 主体：联动规则列表（表格形式）
- 右侧：规则预览面板（可选）

**联动规则列表表格列：**
- 规则名称（可自定义）
- 源字段（触发字段）
- 目标字段（被联动字段）
- 联动类型（显示/隐藏/选项变化）
- 联动条件（简要显示）
- 是否启用（开关）
- 操作（编辑、复制、删除、测试）

**添加/编辑联动规则表单：**

*基础信息区：*
- 规则名称（必填，便于识别，如："沟通状态联动沟通结果"）
- 规则描述（可选，如："根据沟通状态和联系人关系动态显示沟通结果选项"）
- 源字段（下拉选择，支持单选或多选）
  - 单源字段：简单联动（如：沟通状态 → 沟通结果）
  - 多源字段：复杂联动（如：沟通状态 + 联系人关系 → 沟通结果）
- 目标字段（下拉选择，必须是Enum类型）
- 联动类型（下拉选择）：
  - **显示/隐藏 (show/hide)** - 控制目标字段是否显示
  - **选项变化 (options_change)** - 动态改变目标字段的可选项
  - **必填切换 (required_toggle)** - 动态改变目标字段是否必填

*联动规则配置区（可视化配置）：*

**类型1：选项变化配置（最常用）**

界面展示为：
```
┌─────────────────────────────────────────────────────┐
│ 联动规则配置                                          │
├─────────────────────────────────────────────────────┤
│                                                     │
│ 当【沟通状态】= "可联" 并且【联系人关系】= "本人" 时   │
│                                                     │
│ 【沟通结果】可选项：                                  │
│   ☑ 承诺还款                    [上移] [下移] [删除]  │
│   ☑ 拒绝还款                    [上移] [下移] [删除]  │
│   ☑ 与借款人不相关              [上移] [下移] [删除]  │
│   ☑ 其它                        [上移] [下移] [删除]  │
│                                                     │
│   [+ 添加选项]  [从目标字段导入所有选项]              │
│                                                     │
│ [+ 添加另一个条件规则]                                │
├─────────────────────────────────────────────────────┤
│ 已配置的规则：                                        │
│                                                     │
│ 规则1: 沟通状态="可联" AND 联系人关系="本人"           │
│   → 选项: 承诺还款, 拒绝还款, 与借款人不相关, 其它     │
│   [编辑] [删除]                                      │
│                                                     │
│ 规则2: 沟通状态="可联" AND 联系人关系 IN ["父亲","母亲"]│
│   → 选项: 与借款人相关, 承诺代还, 承诺转告, 其他       │
│   [编辑] [删除]                                      │
│                                                     │
│ 规则3: 沟通状态="不存在"                              │
│   → 选项: 失联                                       │
│   [编辑] [删除]                                      │
│                                                     │
└─────────────────────────────────────────────────────┘
```

**具体配置步骤：**

1. **选择触发条件**
   - 如果是单源字段：直接选择该字段的某个枚举值
   - 如果是多源字段：为每个源字段配置条件
   - 条件支持：等于(=)、包含于(IN)、不等于(!=)

2. **配置目标选项**
   - 可以手动输入选项
   - 可以从目标字段已有的枚举选项中勾选
   - 支持拖拽排序
   - 每个选项可以配置：
     - 选项值（必填）
     - 显示文本（可选，默认等于选项值）
     - 是否默认选中

3. **配置默认规则（可选）**
   - 如果所有规则都不匹配时的默认行为
   - 选项：显示所有选项 / 隐藏字段 / 显示指定选项

**类型2：显示/隐藏配置**

```
┌─────────────────────────────────────────────────────┐
│ 当【借款状态】= "已结清" 时                           │
│                                                     │
│ 【催记分组】的显示规则：                              │
│   ○ 显示                                            │
│   ● 隐藏                                            │
│                                                     │
└─────────────────────────────────────────────────────┘
```

**类型3：必填切换配置**

```
┌─────────────────────────────────────────────────────┐
│ 当【还款意愿】= "承诺还款" 时                         │
│                                                     │
│ 【承诺还款日期】的必填规则：                          │
│   ● 必填                                            │
│   ○ 非必填                                          │
│                                                     │
└─────────────────────────────────────────────────────┘
```

**额外功能：**
- **规则测试**：点击"测试"按钮，可以模拟选择源字段的值，实时预览目标字段的变化
- **规则导入/导出**：支持JSON格式的批量导入导出
- **规则复制**：可以复制现有规则作为模板快速创建新规则
- **规则优先级**：当有多个规则时，可以设置优先级（数字越小优先级越高）
- **规则冲突检测**：系统自动检测规则冲突并提示

**配置示例（JSON格式，用于导入导出）：**

```json
{
  "rule_name": "沟通状态联动沟通结果",
  "description": "根据沟通状态和联系人关系动态显示沟通结果选项",
  "dependency_type": "options_change",
  "source_fields": [
    {
      "field_id": 101,
      "field_key": "communication_status",
      "field_name": "沟通状态"
    },
    {
      "field_id": 102,
      "field_key": "contact_relation",
      "field_name": "联系人关系"
    }
  ],
  "target_field": {
    "field_id": 103,
    "field_key": "communication_result",
    "field_name": "沟通结果"
  },
  "rules": [
    {
      "priority": 1,
      "conditions": {
        "communication_status": {"operator": "=", "value": "可联"},
        "contact_relation": {"operator": "=", "value": "本人"}
      },
      "target_options": [
        {"value": "承诺还款", "label": "承诺还款", "is_default": false},
        {"value": "拒绝还款", "label": "拒绝还款", "is_default": false},
        {"value": "与借款人不相关", "label": "与借款人不相关", "is_default": false},
        {"value": "其它", "label": "其它", "is_default": false}
      ]
    },
    {
      "priority": 2,
      "conditions": {
        "communication_status": {"operator": "=", "value": "可联"},
        "contact_relation": {"operator": "IN", "value": ["母亲", "父亲", "配偶", "朋友", "同事", "其他"]}
      },
      "target_options": [
        {"value": "与借款人不相关", "label": "与借款人不相关", "is_default": false},
        {"value": "与借款人相关", "label": "与借款人相关", "is_default": false},
        {"value": "承诺代还", "label": "承诺代还", "is_default": false},
        {"value": "承诺转告", "label": "承诺转告", "is_default": false},
        {"value": "其他", "label": "其他", "is_default": false}
      ]
    }
  ],
  "default_behavior": {
    "type": "show_all",
    "options": []
  },
  "is_enabled": true
}
```

### 5.2 甲方字段配置界面

#### 5.2.1 甲方自定义字段管理页面

**页面定位：**
- 统一管理甲方的标准字段映射关系和自定义字段
- 甲方可以配置字段在其系统中的标识和名称
- 甲方可以创建自己的自定义字段
- 甲方可以调整所有字段的显示顺序
- 甲方可以配置字段在特定队列中的可见性

**页面布局：**
```
┌─────────────────────────────────────────────────────────┐
│  甲方自定义字段管理    [选择甲方 ▼]  [添加自定义字段]  │
├──────────┬──────────────────────────────────────────────┤
│ 字段分组  │  字段列表（标准字段+自定义字段）            │
│          │                                              │
│ □ 基础身份│  提示：标准字段可配置映射关系和排序；      │
│ □ 教育信息│      自定义字段可完全编辑。支持拖拽排序   │
│ □ 职业信息│                                              │
│ ...      │  ┌─────────────────────────────────────┐   │
│          │  │ ☰ 字段名 标识 甲方标识 甲方ID ...  │   │
│          │  ├─────────────────────────────────────┤   │
│          │  │ ☰ 用户编号 user_id UID uid_001 ... │   │
│          │  │ ☰ 用户姓名 user_name Name name_002...│   │
│          │  └─────────────────────────────────────┘   │
└──────────┴──────────────────────────────────────────────┘
```

**左侧：字段分组树**
- 显示所有字段分组的树形结构
- 支持展开/收起子分组
- 高亮当前选中的分组
- 点击分组加载对应字段

**右侧：字段配置表格**

**表格列定义：**

| 列名 | 宽度 | 说明 | 可编辑性 |
|-----|------|-----|---------|
| 拖拽手柄 | 50px | ☰ 图标，用于拖拽排序 | - |
| 字段名称 | 150px | 系统标准字段名称 | 标准字段不可编辑，自定义可编辑 |
| 字段标识 | 150px | 系统标准字段标识（field_key） | 标准字段不可编辑，自定义可编辑 |
| **甲方字段标识** | 150px | 甲方系统中的字段标识（tenant_field_key） | **所有字段可编辑** |
| **甲方字段ID** | 150px | 甲方系统中的字段ID（tenant_field_id） | **所有字段可编辑** |
| 字段类型 | 100px | String/Integer/Date/Enum等 | 标准字段不可编辑，自定义可编辑 |
| **来源** | 100px | 标准/自定义（Tag显示） | **不可编辑** |
| **是否必填** | 100px | 开关组件 | **标准字段不可编辑，自定义可编辑** |
| **队列可见性** | 120px | 配置队列按钮 | 所有字段可配置 |
| 排序 | 80px | 数字，支持拖拽自动更新 | 所有字段可调整 |
| 操作 | 150px | 编辑、删除按钮 | 标准字段只能编辑，不能删除 |

**字段来源标识：**
- `standard`（标准字段）：蓝色Tag，显示"标准"
- `custom`（自定义字段）：绿色Tag，显示"自定义"

**交互规则：**

1. **标准字段（field_source = 'standard'）：**
   - ✅ 可编辑：甲方字段标识、甲方字段ID、排序
   - ✅ 可配置：队列可见性
   - ❌ 不可编辑：字段名称、字段标识、字段类型、是否必填
   - ❌ 不可删除
   - 🔒 删除按钮显示为禁用状态

2. **自定义字段（field_source = 'custom'）：**
   - ✅ 可编辑：所有列
   - ✅ 可配置：队列可见性
   - ✅ 可删除
   - ✅ 完全控制权

3. **拖拽排序：**
   - 鼠标移至拖拽手柄（☰）
   - 按住鼠标左键拖动
   - 行背景变为浅蓝色
   - 释放后自动更新`sort_order`
   - 显示"排序已更新"提示
   - 立即保存到数据库

**队列可见性配置：**

点击"配置队列"按钮，弹出对话框：

```
┌────────────────────────────────┐
│  队列可见性配置                 │
├────────────────────────────────┤
│  选择隐藏队列：                 │
│  □ M1队列                      │
│  ☑ M2队列（该字段在M2中隐藏）  │
│  □ M3+队列                     │
│  □ 法务队列                    │
├────────────────────────────────┤
│         [取消]  [保存]         │
└────────────────────────────────┘
```

**添加自定义字段对话框：**

```
┌─────────────────────────────────────┐
│  添加自定义字段                      │
├─────────────────────────────────────┤
│  字段名称：[___________________]    │
│  字段标识：[___________________]    │
│  甲方字段标识：[_______________]    │
│  甲方字段ID：[_________________]    │
│  字段类型：[String ▼]               │
│  所属分组：[基础身份信息 ▼]         │
│  是否必填：[☑]                      │
│  排序：[10]                         │
├─────────────────────────────────────┤
│           [取消]  [保存]            │
└─────────────────────────────────────┘
```

**编辑字段对话框：**

- 标准字段编辑时，字段名称、字段标识、字段类型、是否必填显示为只读（灰色）
- 自定义字段编辑时，所有字段均可编辑
- 对话框标题根据字段类型显示："编辑标准字段映射" 或 "编辑自定义字段"

**功能列表：**
- ✅ 甲方选择器（必选，切换甲方后重新加载字段）
- ✅ 按分组筛选（点击左侧树）
- ✅ 添加自定义字段
- ✅ 编辑字段（标准字段只能编辑映射关系，自定义字段可完全编辑）
- ✅ 删除自定义字段（带二次确认）
- ✅ 拖拽排序（自动保存）
- ✅ Inline编辑（甲方字段标识、甲方字段ID在表格中直接编辑）
- ✅ 配置队列可见性
- 🔜 批量导入映射配置（Excel/JSON格式）
- 🔜 导出当前映射配置
- 🔜 搜索字段

**排序规则详细说明：**

1. **新建甲方时的排序继承：**
   ```
   当创建新甲方时：
   1. 自动为该甲方创建所有标准字段的配置记录
   2. 复制当前标准字段的 sort_order 到 tenant_field_configs.sort_order
   3. 这是一个快照，后续不会自动更新
   ```

2. **甲方调整排序后：**
   ```
   甲方可以自由调整字段排序：
   - 通过拖拽交互调整
   - 直接编辑排序数字
   - 调整后的排序只影响该甲方
   - 不受标准字段管理的排序变化影响
   ```

3. **新增标准字段时：**
   ```
   当系统新增标准字段时：
   1. 自动为所有甲方创建该字段的配置记录
   2. sort_order = MAX(current_sort_order) + 1
   3. 即：排在所有现有字段的最后
   4. 甲方可以自行调整该字段的位置
   ```

4. **排序优先级：**
   ```
   字段显示顺序优先级：
   1. 队列字段配置（queue_field_configs.sort_order）
   2. 甲方字段配置（tenant_field_configs.sort_order）
   3. 标准字段默认排序（standard_fields.sort_order）
   ```

**代码实现要点：**

```python
# 新建甲方时，继承标准字段
def create_tenant_with_standard_fields(tenant_id):
    standard_fields = db.query(StandardField).all()
    for field in standard_fields:
        tenant_config = TenantFieldConfig(
            tenant_id=tenant_id,
            field_id=field.id,
            field_type='standard',
            sort_order=field.sort_order,  # 快照继承
            is_enabled=True
        )
        db.add(tenant_config)
    db.commit()

# 新增标准字段时，自动添加到所有甲方
def add_standard_field_to_all_tenants(field_id):
    tenants = db.query(Tenant).filter(Tenant.is_active == True).all()
    for tenant in tenants:
        # 获取该甲方当前最大排序
        max_sort_order = db.query(func.max(TenantFieldConfig.sort_order))\
            .filter(TenantFieldConfig.tenant_id == tenant.id)\
            .scalar() or 0
        
        tenant_config = TenantFieldConfig(
            tenant_id=tenant.id,
            field_id=field_id,
            field_type='standard',
            sort_order=max_sort_order + 1,  # 排在最后
            is_enabled=True
        )
        db.add(tenant_config)
    db.commit()
```

**数据流示例：**

```
场景：某甲方配置字段映射

1. 用户选择甲方：银行A
2. 点击分组：基础身份信息
3. 加载字段列表（包含标准字段+自定义字段）：
   
   ☰ 用户编号 | user_id | UID | user_id_001 | String | 标准 | 必填 | [配置队列] | 1 | [编辑] [删除❌]
   ☰ 用户姓名 | user_name | UserName | name_002 | String | 标准 | 必填 | [配置队列] | 2 | [编辑] [删除❌]
   ☰ 客户等级 | customer_level | Level | level_101 | Enum | 自定义 | ☐ | [配置队列] | 3 | [编辑] [删除✓]

4. 用户编辑甲方字段标识：
   - 在表格中直接修改 "UID" → "USER_ID"
   - 失焦后自动保存

5. 用户拖拽排序：
   - 将"用户姓名"拖到"用户编号"上方
   - 自动更新排序：用户姓名(1), 用户编号(2), 客户等级(3)

6. 用户配置队列可见性：
   - 点击"客户等级"的"配置队列"按钮
   - 勾选"M1队列"
   - 保存后，该字段在M1队列中隐藏
```

### 5.3 组织管理界面

#### 5.3.1 催收机构管理

**机构列表页面：**

**布局：**
- 顶部：添加机构按钮、甲方选择器、搜索框
- 主体：机构列表（卡片或表格形式）

**机构列表表格列：**
- 机构编码
- 机构名称
- 联系人
- 联系电话
- 小组数量
- 催员数量
- 案件数量
- 是否启用（开关）
- 操作（编辑、查看详情、删除、管理小组）

**添加/编辑机构表单：**
- 机构编码（必填，如：AG001）
- 机构名称（必填，中文）
- 机构名称（英文）
- 联系人（必填）
- 联系电话（必填）
- 联系邮箱
- 机构地址
- 机构描述
- 排序（数字输入）
- 是否启用（开关）

**机构详情页面：**
- 显示机构基本信息
- 显示该机构下的所有小组
- 显示该机构下的所有催员
- 案件统计（总案件数、处理中、已结清等）
- 绩效统计（回款金额、回款率等）

#### 5.3.2 催收小组管理

**小组列表页面：**

**布局：**
- 顶部：添加小组按钮、机构选择器、搜索框
- 主体：小组列表（表格形式）

**小组列表表格列：**
- 小组编码
- 小组名称
- 所属机构
- 组长
- 小组类型（电催组/外访组/法务组等）
- 催员数量
- 案件数量/最大案件数
- 是否启用（开关）
- 操作（编辑、查看详情、删除、管理催员）

**添加/编辑小组表单：**
- 所属机构（下拉选择，必填）
- 小组编码（必填，如：TEAM001）
- 小组名称（必填，中文）
- 小组名称（英文）
- 组长（下拉选择催员）
- 小组类型（下拉选择：电催组/外访组/法务组/其他）
- 小组描述
- 最大案件数量（数字输入，0表示不限制）
- 排序（数字输入）
- 是否启用（开关）

**小组详情页面：**
- 显示小组基本信息
- 显示该小组下的所有催员（表格形式）
- 案件分配情况（每个催员的案件数量）
- 小组绩效统计

#### 5.3.3 催员管理

**催员列表页面：**

**布局：**
- 顶部：添加催员按钮、机构/小组选择器、搜索框
- 左侧：组织树形结构（机构 → 小组 → 催员）
- 右侧：催员列表（表格形式）

**催员列表表格列：**
- 催员编码
- 催员姓名
- 所属机构
- 所属小组
- 工号
- 催员等级（初级/中级/高级/资深）
- 手机号码
- 当前案件数/最大案件数
- 绩效评分
- 状态（在职/休假/离职）
- 是否启用（开关）
- 操作（编辑、查看详情、删除、分配案件）

**添加/编辑催员表单：**

*基本信息：*
- 所属机构（下拉选择，必填）
- 所属小组（下拉选择，根据机构动态加载，必填）
- 催员编码（必填，如：COL001）
- 催员姓名（必填）
- 工号（必填）
- 手机号码（必填）
- 邮箱
- 入职日期

*工作配置：*
- 催员等级（下拉选择：初级/中级/高级/资深）
- 最大案件数量（数字输入，默认100）
- 擅长领域（多选标签：高额案件/法务处理/电话催收/外访催收等）
- 状态（下拉选择：在职/休假/离职）
- 是否启用（开关）

**催员详情页面：**

```
┌─────────────────────────────────────────────────────────────┐
│ 催员详情 - 张三                                              │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│ [基本信息]  [案件统计]  [绩效分析]  [工作记录]              │
│                                                             │
│ ┌─────────────────┐  ┌────────────────────────────────┐   │
│ │ 基本信息        │  │ 案件统计                        │   │
│ ├─────────────────┤  ├────────────────────────────────┤   │
│ │ 姓名：张三      │  │ 当前案件数：45 / 100           │   │
│ │ 工号：E001      │  │ 今日分配：8                    │   │
│ │ 机构：XX机构    │  │ 本周新增：32                   │   │
│ │ 小组：电催组    │  │ 本月结清：15                   │   │
│ │ 等级：高级      │  │                                 │   │
│ │ 手机：138xxx    │  │ 按状态分布：                    │   │
│ │ 入职：2023-01   │  │ - 新案件：10                   │   │
│ │                 │  │ - 催收中：30                   │   │
│ └─────────────────┘  │ - 承诺还款：5                  │   │
│                      └────────────────────────────────┘   │
│                                                             │
│ ┌───────────────────────────────────────────────────────┐ │
│ │ 绩效统计（本月）                                       │ │
│ ├───────────────────────────────────────────────────────┤ │
│ │ 回款金额：￥156,000                                   │ │
│ │ 回款率：45.5%                                          │ │
│ │ 接触次数：320                                          │ │
│ │ 平均接触次数：7.1次/案件                               │ │
│ │ 绩效评分：★★★★☆ 4.5                                  │ │
│ └───────────────────────────────────────────────────────┘ │
│                                                             │
│ [分配新案件]  [查看所有案件]  [导出工作报表]               │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

**催员工作量统计：**
- 案件数量趋势图（按日/周/月）
- 回款金额趋势图
- 案件状态分布饼图
- 与小组平均值对比

### 5.4 队列管理界面

#### 5.4.1 队列列表页面

**布局：**
- 顶部：添加队列按钮、甲方选择器
- 主体：队列列表（卡片或表格形式）

**队列列表表格列：**
- 队列编码（如：M1, M2, M3+）
- 队列名称
- 逾期天数范围（如：1-30天）
- 案件数量（统计）
- 已配置字段数
- 排序
- 是否启用（开关）
- 操作（编辑、配置字段、删除）

**添加/编辑队列表单：**
- 队列编码（必填，如：M1）
- 队列名称（必填，中文）
- 队列名称（英文）
- 队列描述（可选）
- 逾期天数范围：
  - 最小天数（可选）
  - 最大天数（可选）
  - 示例：M1队列=1-30天，M3+队列=91天以上（最大天数留空）
- 排序（数字输入）
- 是否启用（开关）

#### 5.4.2 队列字段配置页面（核心功能）

**入口：** 队列列表 → 点击"配置字段"按钮

**布局：**
- 顶部：
  - 队列信息展示（队列名称、逾期天数范围等）
  - 操作按钮：保存配置、批量设置、从其他队列复制配置
- 左侧：字段分组树形结构（与标准字段管理相同）
- 右侧：字段配置表格

**字段配置表格列：**
- 字段名称
- 字段类型
- **是否可见**（开关）⭐核心功能 - 控制该字段在此队列中是否显示
- **是否必填**（三态选择）
  - 使用默认（NULL）- 使用字段的默认必填设置
  - 必填（TRUE）- 在此队列中强制必填
  - 非必填（FALSE）- 在此队列中强制非必填
- **是否只读**（开关）- 字段只能查看不能编辑
- **是否可编辑**（开关）- 字段是否可以被催员修改
- 排序（数字输入，拖拽排序）

**可视化配置界面示例：**

```
┌─────────────────────────────────────────────────────────────────┐
│ 队列字段配置 - M1队列 (逾期1-30天)                               │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│ [保存配置] [批量设置] [从其他队列复制]                            │
│                                                                 │
│ ┌─────────────┐  ┌─────────────────────────────────────────┐  │
│ │ 字段分组    │  │ 字段配置                                 │  │
│ ├─────────────┤  ├─────────────────────────────────────────┤  │
│ │ ▼ 客户基本  │  │ 字段名称    类型   可见  必填  只读  排序 │  │
│ │   身份信息  │  │ ─────────────────────────────────────── │  │
│ │   教育      │  │ 用户ID     String  ☑   默认   ☐    1   │  │
│ │   职业信息  │  │ 用户姓名   String  ☑   ☑     ☐    2   │  │
│ │ ▼ 贷款详情  │  │ 手机号     String  ☑   ☑     ☐    3   │  │
│ │ ▼ 催记      │  │ 身份证号   String  ☐   默认   ☐    4   │  │
│ │ ▶ 还款记录  │  │ 逾期金额   Decimal ☑   ☑     ☐    5   │  │
│ │             │  │ 逾期天数   Integer ☑   默认   ☑    6   │  │
│ │             │  │ 承诺还款日 Date    ☑   ☐     ☐    7   │  │
│ │             │  │                                         │  │
│ │             │  │ [批量操作]                               │  │
│ │             │  │ □ 全选                                  │  │
│ │             │  │ [批量设置可见] [批量设置必填]            │  │
│ │             │  │                                         │  │
│ └─────────────┘  └─────────────────────────────────────────┘  │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

**批量设置功能：**
- **批量设置可见性**：选中多个字段后，批量设置是否可见
- **批量设置必填**：批量设置必填规则
- **从其他队列复制配置**：
  - 弹出队列选择框
  - 选择要复制的源队列
  - 一键复制该队列的所有字段配置
  - 可以选择性覆盖或合并

**配置预览功能：**
- 点击"预览"按钮，以案件详情页的形式展示当前配置效果
- 实时查看字段的显示、排序、必填效果

#### 5.4.3 队列分配规则配置

**布局：**
- 配置案件如何自动分配到队列

**规则类型：**
1. **基于逾期天数**（最常用）
   - 系统根据案件的逾期天数自动匹配队列
   - 示例：逾期15天 → 自动分配到M1队列（1-30天）

2. **基于金额**（可选）
   - 大额案件分配到特殊队列
   - 示例：逾期金额>10000 → 分配到高额队列

3. **基于区域**（可选）
   - 不同地区分配到不同队列

**自动流转规则：**
- 案件逾期天数变化时，自动流转到对应队列
- 流转时字段配置自动切换
- 流转历史记录

### 5.5 案件详情页面设计

#### 5.5.1 详情页布局

**布局方式：**
- 顶部：案件基本信息（固定区域，包括队列信息）
- 主体：Tab标签页，每个Tab对应一个字段分组
- 每个Tab内：按分组展示字段，支持折叠/展开

**字段展示（基于队列配置）：**
- **队列优先**：优先使用案件所属队列的字段配置
- 标准字段：按标准字段定义显示
- 自定义字段：按甲方自定义字段定义显示
- 已删除字段：单独区域显示（折叠状态）

**字段显示逻辑：**
1. 获取案件的 `queue_id`
2. 从 `queue_field_configs` 表查询该队列的字段配置
3. 根据 `is_visible` 决定字段是否显示
4. 根据 `is_required` 决定字段是否必填
5. 根据 `is_readonly` 决定字段是否只读

**配置优先级：**
```
队列字段配置 > 甲方字段配置 > 字段默认配置
```

**字段编辑：**
- 根据队列字段配置显示/隐藏字段
- 根据队列字段配置控制可编辑/只读
- 支持字段联动（根据联动规则动态显示/隐藏或改变选项）

**示例：**
- M1队列案件：显示基础催收字段（用户信息、贷款金额、联系电话等）
- M3+队列案件：显示更详细的字段（法律相关、资产信息等）
- 法务队列案件：显示法律诉讼相关字段

#### 5.5.2 催记分组特殊处理

**联动字段处理：**
- `communication_status`（沟通状态）字段：下拉选择
- `communication_result`（沟通结果）字段：
  - 根据`communication_status`的值动态显示选项
  - 当选择"本人+可联"时，显示对应选项
  - 当选择"非本人+可联"时，显示对应选项
  - 当选择"不存在"或"未响应"时，显示对应选项

### 5.6 案件列表页面设计

#### 5.6.1 列表页布局

**顶部筛选区：**
- 标准字段筛选（固定字段）
- 自定义字段筛选（根据配置动态显示）
- 支持多条件组合筛选
- 支持保存常用筛选条件

**列表表格：**
- 固定列：案件编号、客户姓名、金额、逾期天数等核心字段
- 可配置列：标准字段和自定义字段（通过列设置选择）
- 支持列排序
- 支持列宽调整

**筛选组件类型：**
- 文本字段：文本输入框（支持模糊搜索）
- 数字字段：范围选择器（最小值-最大值）
- 枚举字段：多选下拉框
- 日期字段：日期范围选择器
- 布尔字段：是/否/全部选择器

### 5.7 审计日志页面设计

#### 5.7.1 日志列表页面

**筛选条件：**
- 时间范围
- 操作类型
- 实体类型
- 操作人
- 字段（如果是字段相关操作）

**日志列表表格列：**
- 操作时间
- 操作类型
- 实体类型
- 操作内容（简要描述）
- 操作人
- 操作IP
- 操作（查看详情）

**详情弹窗：**
- 显示变更前后的完整数据对比
- 支持JSON格式化显示
- 显示操作备注

---

## 六、字段联动规则详细设计

### 6.1 催记分组联动规则

**联动关系：**
- `communication_result` 依赖 `communication_status` 和 `contact_relation`

**联动规则配置：**

```json
{
  "source_fields": [
    {
      "field_key": "communication_status",
      "field_name": "沟通状态"
    },
    {
      "field_key": "contact_relation",
      "field_name": "沟通关系"
    }
  ],
  "target_field": {
    "field_key": "communication_result",
    "field_name": "沟通结果"
  },
  "rules": [
    {
      "conditions": {
        "communication_status": "可联",
        "contact_relation": "本人"
      },
      "options": [
        "承诺还款",
        "拒绝还款",
        "与借款人不相关",
        "其它"
      ]
    },
    {
      "conditions": {
        "communication_status": "可联",
        "contact_relation": ["母亲", "父亲", "配偶", "朋友", "同事", "其他"]
      },
      "options": [
        "与借款人不相关",
        "与借款人相关",
        "承诺代还",
        "承诺转告",
        "其他"
      ]
    },
    {
      "conditions": {
        "communication_status": "不存在"
      },
      "options": [
        "失联"
      ]
    },
    {
      "conditions": {
        "communication_status": "未响应"
      },
      "options": [
        "持续跟进"
      ]
    }
  ]
}
```

### 6.2 联动实现方式

**前端实现：**
1. 监听源字段变化
2. 根据联动规则匹配条件
3. 动态更新目标字段的选项或显示/隐藏状态
4. 清空目标字段的当前值（如果选项变化）

**后端校验：**
1. 提交数据时校验联动规则
2. 确保目标字段的值符合联动规则

---

## 七、数据同步接口设计

### 7.1 同步接口

**接口：** `POST /api/v1/cases/sync`

**请求格式（使用甲方字段名）：**
```json
{
  "tenant_code": "TENANT001",
  "cases": [
    {
      "case_id": "CASE001",
      "loan_id": "LOAN001",
      "fields": {
        "UID": "5983",
        "UserName": "Juan Dela Cruz",
        "Mobile": "+63 9123456789",
        "OutstandingAmt": 850,
        "OverdueDays": 5
      }
    }
  ]
}
```

**请求格式（使用系统标准字段名）：**
```json
{
  "tenant_code": "TENANT001",
  "use_standard_fields": true,
  "cases": [
    {
      "case_id": "CASE001",
      "loan_id": "LOAN001",
      "fields": {
        "user_id": "5983",
        "user_name": "Juan Dela Cruz",
        "mobile_number": "+63 9123456789",
        "outstanding_amount": 850,
        "overdue_days": 5
      }
    }
  ]
}
```

**字段映射处理逻辑：**
1. 接收到数据后，根据 `tenant_code` 查询该甲方的字段映射配置
2. 如果 `use_standard_fields` 为 `true`，直接使用系统标准字段名
3. 如果 `use_standard_fields` 为 `false` 或未设置：
   - 遍历 `fields` 中的每个字段
   - 根据 `tenant_field_key` 查找对应的系统标准字段
   - 将甲方字段名转换为系统标准字段名
   - 如果找不到映射关系，记录错误日志并跳过该字段
4. 转换完成后，按标准字段存储

**响应格式：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "success_count": 10,
    "fail_count": 0,
    "errors": [],
    "field_mapping_errors": [
      {
        "case_id": "CASE001",
        "unmapped_fields": ["UnknownField1", "UnknownField2"]
      }
    ]
  }
}
```

### 7.2 字段映射配置查询接口

**接口：** `GET /api/v1/tenants/{tenant_code}/field-mappings`

**响应格式：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "tenant_code": "TENANT001",
    "field_mappings": [
      {
        "system_field_key": "user_id",
        "system_field_name": "用户ID",
        "tenant_field_key": "UID",
        "tenant_field_name": "用户标识",
        "field_type": "String",
        "is_required": true
      },
      {
        "system_field_key": "user_name",
        "system_field_name": "用户姓名",
        "tenant_field_key": "UserName",
        "tenant_field_name": "姓名",
        "field_type": "String",
        "is_required": true
      }
    ]
  }
}
```

### 7.3 字段联动规则管理接口

#### 7.3.1 获取联动规则列表

**接口：** `GET /api/v1/field-dependencies`

**请求参数：**
- `target_field_id` (可选): 目标字段ID，筛选特定字段的联动规则
- `is_enabled` (可选): 是否启用，筛选启用/禁用的规则
- `page`: 页码
- `page_size`: 每页数量

**响应格式：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 10,
    "items": [
      {
        "id": 1,
        "rule_name": "沟通状态联动沟通结果",
        "rule_description": "根据沟通状态和联系人关系动态显示沟通结果选项",
        "source_fields": [
          {
            "field_id": 101,
            "field_key": "communication_status",
            "field_name": "沟通状态"
          },
          {
            "field_id": 102,
            "field_key": "contact_relation",
            "field_name": "联系人关系"
          }
        ],
        "target_field": {
          "field_id": 103,
          "field_key": "communication_result",
          "field_name": "沟通结果"
        },
        "dependency_type": "options_change",
        "priority": 1,
        "is_enabled": true,
        "created_at": "2024-01-01 10:00:00",
        "updated_at": "2024-01-01 10:00:00"
      }
    ]
  }
}
```

#### 7.3.2 创建联动规则

**接口：** `POST /api/v1/field-dependencies`

**请求格式：**
```json
{
  "rule_name": "沟通状态联动沟通结果",
  "rule_description": "根据沟通状态和联系人关系动态显示沟通结果选项",
  "source_field_ids": [101, 102],
  "target_field_id": 103,
  "dependency_type": "options_change",
  "dependency_rule": {
    "source_fields": [
      {"field_id": 101, "field_key": "communication_status"},
      {"field_id": 102, "field_key": "contact_relation"}
    ],
    "rules": [
      {
        "priority": 1,
        "conditions": {
          "communication_status": {"operator": "=", "value": "可联"},
          "contact_relation": {"operator": "=", "value": "本人"}
        },
        "target_options": [
          {"value": "承诺还款", "label": "承诺还款"},
          {"value": "拒绝还款", "label": "拒绝还款"}
        ]
      }
    ],
    "default_behavior": {"type": "show_all"}
  },
  "priority": 1,
  "is_enabled": true
}
```

**响应格式：**
```json
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "id": 1,
    "rule_name": "沟通状态联动沟通结果"
  }
}
```

#### 7.3.3 更新联动规则

**接口：** `PUT /api/v1/field-dependencies/{id}`

**请求格式：** 同创建接口

#### 7.3.4 删除联动规则

**接口：** `DELETE /api/v1/field-dependencies/{id}`

**响应格式：**
```json
{
  "code": 200,
  "message": "删除成功"
}
```

#### 7.3.5 测试联动规则

**接口：** `POST /api/v1/field-dependencies/{id}/test`

**请求格式：**
```json
{
  "source_values": {
    "communication_status": "可联",
    "contact_relation": "本人"
  }
}
```

**响应格式：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "matched_rule_priority": 1,
    "target_options": [
      {"value": "承诺还款", "label": "承诺还款"},
      {"value": "拒绝还款", "label": "拒绝还款"},
      {"value": "与借款人不相关", "label": "与借款人不相关"},
      {"value": "其它", "label": "其它"}
    ]
  }
}
```

#### 7.3.6 批量导入联动规则

**接口：** `POST /api/v1/field-dependencies/batch-import`

**请求格式：**
```json
{
  "rules": [
    {
      "rule_name": "规则1",
      "source_field_ids": [101],
      "target_field_id": 103,
      "dependency_type": "options_change",
      "dependency_rule": { /* ... */ }
    },
    {
      "rule_name": "规则2",
      "source_field_ids": [102],
      "target_field_id": 104,
      "dependency_type": "show_hide",
      "dependency_rule": { /* ... */ }
    }
  ]
}
```

**响应格式：**
```json
{
  "code": 200,
  "message": "批量导入完成",
  "data": {
    "success_count": 2,
    "fail_count": 0,
    "errors": []
  }
}
```

#### 7.3.7 获取字段的联动规则（前端运行时使用）

**接口：** `GET /api/v1/fields/{field_id}/dependencies`

**用途：** 前端在渲染表单时调用，获取某个字段相关的所有联动规则

**响应格式：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "as_source": [
      {
        "id": 1,
        "target_field_id": 103,
        "target_field_key": "communication_result",
        "dependency_type": "options_change",
        "dependency_rule": { /* ... */ }
      }
    ],
    "as_target": [
      {
        "id": 2,
        "source_field_ids": [101, 102],
        "dependency_type": "options_change",
        "dependency_rule": { /* ... */ }
      }
    ]
  }
}
```

### 7.4 队列管理接口

#### 7.4.1 获取队列列表

**接口：** `GET /api/v1/queues`

**请求参数：**
- `tenant_id` (必填): 甲方ID
- `is_active` (可选): 是否启用

**响应格式：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "items": [
      {
        "id": 1,
        "queue_code": "M1",
        "queue_name": "M1队列",
        "queue_name_en": "M1 Queue",
        "overdue_days_min": 1,
        "overdue_days_max": 30,
        "case_count": 150,
        "configured_field_count": 25,
        "is_active": true
      },
      {
        "id": 2,
        "queue_code": "M2",
        "queue_name": "M2队列",
        "queue_name_en": "M2 Queue",
        "overdue_days_min": 31,
        "overdue_days_max": 60,
        "case_count": 85,
        "configured_field_count": 30,
        "is_active": true
      }
    ]
  }
}
```

#### 7.4.2 创建队列

**接口：** `POST /api/v1/queues`

**请求格式：**
```json
{
  "tenant_id": 1,
  "queue_code": "M1",
  "queue_name": "M1队列",
  "queue_name_en": "M1 Queue",
  "queue_description": "逾期1-30天的案件",
  "overdue_days_min": 1,
  "overdue_days_max": 30,
  "sort_order": 1,
  "is_active": true
}
```

#### 7.4.3 更新队列

**接口：** `PUT /api/v1/queues/{id}`

#### 7.4.4 删除队列

**接口：** `DELETE /api/v1/queues/{id}`

#### 7.4.5 获取队列字段配置

**接口：** `GET /api/v1/queues/{queue_id}/field-configs`

**用途：** 获取指定队列的所有字段配置

**响应格式：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "queue_id": 1,
    "queue_name": "M1队列",
    "fields": [
      {
        "field_id": 1,
        "field_key": "user_id",
        "field_name": "用户ID",
        "field_type": "String",
        "field_source": "standard",
        "is_visible": true,
        "is_required": null,
        "is_readonly": false,
        "is_editable": true,
        "sort_order": 1
      },
      {
        "field_id": 2,
        "field_key": "user_name",
        "field_name": "用户姓名",
        "field_type": "String",
        "field_source": "standard",
        "is_visible": true,
        "is_required": true,
        "is_readonly": false,
        "is_editable": true,
        "sort_order": 2
      }
    ]
  }
}
```

#### 7.4.6 批量更新队列字段配置

**接口：** `PUT /api/v1/queues/{queue_id}/field-configs`

**用途：** 批量更新队列的字段配置

**请求格式：**
```json
{
  "fields": [
    {
      "field_id": 1,
      "field_type": "standard",
      "is_visible": true,
      "is_required": null,
      "is_readonly": false,
      "is_editable": true,
      "sort_order": 1
    },
    {
      "field_id": 2,
      "field_type": "standard",
      "is_visible": true,
      "is_required": true,
      "is_readonly": false,
      "is_editable": true,
      "sort_order": 2
    }
  ]
}
```

**响应格式：**
```json
{
  "code": 200,
  "message": "配置更新成功",
  "data": {
    "updated_count": 2
  }
}
```

#### 7.4.7 从其他队列复制字段配置

**接口：** `POST /api/v1/queues/{target_queue_id}/copy-field-configs`

**用途：** 从源队列复制字段配置到目标队列

**请求格式：**
```json
{
  "source_queue_id": 1,
  "copy_mode": "merge"
}
```

**参数说明：**
- `source_queue_id`: 源队列ID
- `copy_mode`: 复制模式
  - `merge`: 合并模式，保留目标队列已有配置，只添加新配置
  - `replace`: 替换模式，完全覆盖目标队列的配置

**响应格式：**
```json
{
  "code": 200,
  "message": "配置复制成功",
  "data": {
    "copied_count": 25,
    "skipped_count": 3
  }
}
```

#### 7.4.8 获取案件的字段配置（运行时接口）

**接口：** `GET /api/v1/cases/{case_id}/field-configs`

**用途：** 前端渲染案件详情时调用，根据案件所属队列获取应该显示的字段配置

**响应格式：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "case_id": "CASE001",
    "queue_id": 1,
    "queue_name": "M1队列",
    "fields_by_group": [
      {
        "group_id": 1,
        "group_name": "客户基本信息",
        "fields": [
          {
            "field_id": 1,
            "field_key": "user_id",
            "field_name": "用户ID",
            "field_type": "String",
            "is_visible": true,
            "is_required": null,
            "is_readonly": false,
            "is_editable": true,
            "sort_order": 1,
            "value": "5983"
          }
        ]
      }
    ]
  }
}
```

### 7.5 组织管理接口

#### 7.5.1 催收机构管理接口

**获取机构列表：** `GET /api/v1/agencies`

**请求参数：**
- `tenant_id` (必填): 甲方ID
- `is_active` (可选): 是否启用

**创建机构：** `POST /api/v1/agencies`

**更新机构：** `PUT /api/v1/agencies/{id}`

**删除机构：** `DELETE /api/v1/agencies/{id}`

**响应格式示例：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "items": [
      {
        "id": 1,
        "agency_code": "AG001",
        "agency_name": "自营催收中心",
        "contact_person": "张三",
        "contact_phone": "13800138000",
        "team_count": 5,
        "collector_count": 25,
        "case_count": 450,
        "is_active": true
      }
    ]
  }
}
```

#### 7.5.2 催收小组管理接口

**获取小组列表：** `GET /api/v1/teams`

**请求参数：**
- `agency_id` (可选): 机构ID，筛选特定机构下的小组
- `is_active` (可选): 是否启用

**创建小组：** `POST /api/v1/teams`

**更新小组：** `PUT /api/v1/teams/{id}`

**删除小组：** `DELETE /api/v1/teams/{id}`

**响应格式示例：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "items": [
      {
        "id": 1,
        "team_code": "TEAM001",
        "team_name": "电催一组",
        "agency_id": 1,
        "agency_name": "自营催收中心",
        "team_leader_id": 5,
        "team_leader_name": "李四",
        "team_type": "电催组",
        "collector_count": 8,
        "case_count": 120,
        "max_case_count": 200,
        "is_active": true
      }
    ]
  }
}
```

#### 7.5.3 催员管理接口

**获取催员列表：** `GET /api/v1/collectors`

**请求参数：**
- `team_id` (可选): 小组ID，筛选特定小组下的催员
- `agency_id` (可选): 机构ID，筛选特定机构下的催员
- `status` (可选): 催员状态（active/休假/离职）
- `is_active` (可选): 是否启用

**创建催员：** `POST /api/v1/collectors`

**请求格式：**
```json
{
  "team_id": 1,
  "user_id": 100,
  "collector_code": "COL001",
  "collector_name": "张三",
  "mobile_number": "13800138000",
  "email": "zhangsan@example.com",
  "employee_no": "E001",
  "collector_level": "高级",
  "max_case_count": 100,
  "specialties": ["高额案件", "法务处理"],
  "hire_date": "2023-01-15",
  "is_active": true
}
```

**更新催员：** `PUT /api/v1/collectors/{id}`

**删除催员：** `DELETE /api/v1/collectors/{id}`

**获取催员详情（包含统计）：** `GET /api/v1/collectors/{id}/detail`

**响应格式：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "collector_code": "COL001",
    "collector_name": "张三",
    "agency_name": "自营催收中心",
    "team_name": "电催一组",
    "collector_level": "高级",
    "mobile_number": "13800138000",
    "current_case_count": 45,
    "max_case_count": 100,
    "performance_score": 4.5,
    "status": "active",
    "statistics": {
      "today_assigned": 8,
      "week_assigned": 32,
      "month_settled": 15,
      "month_amount_collected": 156000.00,
      "month_contact_count": 320,
      "case_status_distribution": {
        "新案件": 10,
        "催收中": 30,
        "承诺还款": 5
      }
    }
  }
}
```

#### 7.5.4 案件分配接口

**分配案件到机构：** `POST /api/v1/cases/assign-to-agency`

**请求格式：**
```json
{
  "case_ids": [1, 2, 3],
  "agency_id": 1,
  "reason": "按区域分配"
}
```

**分配案件到小组：** `POST /api/v1/cases/assign-to-team`

**请求格式：**
```json
{
  "case_ids": [1, 2, 3],
  "team_id": 1,
  "reason": "按案件类型分配"
}
```

**分配案件到催员：** `POST /api/v1/cases/assign-to-collector`

**请求格式：**
```json
{
  "case_ids": [1, 2, 3],
  "collector_id": 5,
  "reason": "手动分配"
}
```

**自动分配案件：** `POST /api/v1/cases/auto-assign`

**请求格式：**
```json
{
  "case_ids": [1, 2, 3],
  "assignment_rule": "balanced",
  "target_level": "collector"
}
```

**参数说明：**
- `assignment_rule`: 分配规则
  - `balanced`: 均衡分配（按工作量平衡）
  - `random`: 随机分配
  - `skill_match`: 技能匹配（根据催员擅长领域）
- `target_level`: 分配层级
  - `agency`: 分配到机构
  - `team`: 分配到小组
  - `collector`: 分配到催员

**响应格式：**
```json
{
  "code": 200,
  "message": "分配成功",
  "data": {
    "assigned_count": 3,
    "assignments": [
      {
        "case_id": 1,
        "assigned_to_collector_id": 5,
        "assigned_to_collector_name": "张三"
      },
      {
        "case_id": 2,
        "assigned_to_collector_id": 6,
        "assigned_to_collector_name": "李四"
      }
    ]
  }
}
```

#### 7.5.5 案件流转接口

**转移案件：** `POST /api/v1/cases/transfer`

**请求格式：**
```json
{
  "case_ids": [1, 2, 3],
  "from_collector_id": 5,
  "to_collector_id": 6,
  "reason": "工作量调整",
  "remarks": "原催员工作量已饱和"
}
```

**批量回收案件：** `POST /api/v1/cases/reclaim`

**请求格式：**
```json
{
  "case_ids": [1, 2, 3],
  "reason": "催员离职",
  "target_level": "team"
}
```

**获取案件分配历史：** `GET /api/v1/cases/{case_id}/assignment-history`

**响应格式：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "case_id": 1,
    "history": [
      {
        "id": 1,
        "assignment_type": "to_collector",
        "from_collector_name": null,
        "to_collector_name": "张三",
        "to_team_name": "电催一组",
        "to_agency_name": "自营催收中心",
        "reason": "初次分配",
        "assigned_by_name": "系统管理员",
        "assigned_at": "2024-01-15 10:00:00"
      },
      {
        "id": 2,
        "assignment_type": "transfer",
        "from_collector_name": "张三",
        "to_collector_name": "李四",
        "to_team_name": "电催一组",
        "to_agency_name": "自营催收中心",
        "reason": "工作量调整",
        "assigned_by_name": "组长王五",
        "assigned_at": "2024-01-20 14:30:00"
      }
    ]
  }
}
```

---

## 八、技术实现要点

### 8.1 前端技术栈

- **框架：** Vue 3 + TypeScript
- **UI组件库：** Element Plus
- **状态管理：** Pinia
- **表单处理：** 动态表单组件（支持配置驱动）
- **国际化：** vue-i18n
- **时区处理：** dayjs + timezone插件

### 8.2 字段映射实现

**后端实现：**
1. **映射关系缓存**
   - 使用Redis缓存甲方字段映射关系
   - Key格式：`tenant:{tenant_code}:field_mappings`
   - 缓存结构：`{tenant_field_key: system_field_key}` 哈希表
   - 映射配置更新时自动刷新缓存

2. **数据同步服务**
   ```python
   class FieldMappingService:
       def get_field_mappings(self, tenant_code: str) -> dict:
           """获取甲方字段映射关系（带缓存）"""
           pass
       
       def map_tenant_fields_to_system(self, tenant_code: str, tenant_data: dict) -> dict:
           """将甲方字段转换为系统标准字段"""
           pass
       
       def map_system_fields_to_tenant(self, tenant_code: str, system_data: dict) -> dict:
           """将系统标准字段转换为甲方字段（用于数据导出）"""
           pass
   ```

3. **数据验证**
   - 接收数据时，验证甲方字段是否在映射配置中
   - 未映射的字段记录到错误日志
   - 支持配置是否允许未映射字段（严格模式/宽松模式）

**前端实现：**
1. **映射配置界面**
   - 提供可视化的字段映射配置界面
   - 支持批量导入映射配置（Excel/JSON）
   - 实时预览映射效果

2. **数据展示**
   - 在案件详情页面，可选择显示系统字段名或甲方字段名
   - 提供字段名切换开关

### 8.3 性能优化

- **字段配置缓存：** 使用Redis缓存字段配置和映射关系，减少数据库查询
- **映射关系预加载：** 在甲方数据同步时批量预加载映射关系
- **列表虚拟滚动：** 处理大量案件数据
- **懒加载：** 详情页按需加载字段数据

### 8.4 注意事项

**字段映射相关：**
1. **唯一性约束**
   - 同一甲方下，`tenant_field_key` 必须唯一
   - 避免多个系统字段映射到同一个甲方字段

2. **兼容性处理**
   - 如果甲方未配置字段映射，默认使用系统标准字段名
   - 支持部分字段映射，未映射字段使用标准字段名

3. **映射变更影响**
   - 修改映射关系时，需要评估对历史数据的影响
   - 提供映射关系变更审计日志
   - 支持映射关系版本管理（可选）

4. **大小写敏感性**
   - 建议甲方字段标识保持大小写一致性
   - 系统支持配置是否忽略大小写（可选）

---

## 九、字段映射应用示例

### 9.1 场景说明

假设有一个菲律宾甲方客户（TENANT_PH001），他们的系统字段命名规范与我们的标准不同：

| 系统标准字段 | 系统字段标识 | 甲方字段标识 | 甲方字段名称 |
|------------|------------|------------|------------|
| 用户ID | user_id | UID | User ID |
| 用户姓名 | user_name | UserName | Name |
| 手机号码 | mobile_number | Mobile | Phone Number |
| 逾期金额 | outstanding_amount | OutstandingAmt | Outstanding Amount |
| 逾期天数 | overdue_days | OverdueDays | Days Past Due |

### 9.2 配置步骤

**步骤1：在系统中配置甲方字段映射**

进入"甲方管理" > "甲方字段配置"，为 TENANT_PH001 配置字段映射：

```sql
-- 插入字段映射配置示例
INSERT INTO tenant_field_configs (tenant_id, field_id, field_type, tenant_field_key, tenant_field_name, is_enabled, is_required)
VALUES 
(1, 1, 'standard', 'UID', 'User ID', TRUE, TRUE),
(1, 2, 'standard', 'UserName', 'Name', TRUE, TRUE),
(1, 3, 'standard', 'Mobile', 'Phone Number', TRUE, FALSE),
(1, 4, 'standard', 'OutstandingAmt', 'Outstanding Amount', TRUE, TRUE),
(1, 5, 'standard', 'OverdueDays', 'Days Past Due', TRUE, TRUE);
```

**步骤2：甲方推送数据**

甲方使用他们的字段名推送数据：

```json
POST /api/v1/cases/sync
{
  "tenant_code": "TENANT_PH001",
  "cases": [
    {
      "case_id": "PH-CASE-001",
      "loan_id": "PH-LOAN-001",
      "fields": {
        "UID": "5983",
        "UserName": "Juan Dela Cruz",
        "Mobile": "+63 9123456789",
        "OutstandingAmt": 850.50,
        "OverdueDays": 5
      }
    }
  ]
}
```

**步骤3：系统自动映射转换**

系统接收到数据后，根据映射配置自动转换：

```json
// 系统内部存储格式
{
  "case_id": "PH-CASE-001",
  "loan_id": "PH-LOAN-001",
  "fields": {
    "user_id": "5983",
    "user_name": "Juan Dela Cruz",
    "mobile_number": "+63 9123456789",
    "outstanding_amount": 850.50,
    "overdue_days": 5
  }
}
```

### 9.3 批量导入映射配置

支持通过Excel或JSON文件批量导入映射配置：

**Excel格式示例：**

| 系统字段标识 | 系统字段名称 | 甲方字段标识 | 甲方字段名称 | 是否启用 | 是否必填 |
|------------|------------|------------|------------|---------|---------|
| user_id | 用户ID | UID | User ID | 是 | 是 |
| user_name | 用户姓名 | UserName | Name | 是 | 是 |
| mobile_number | 手机号码 | Mobile | Phone Number | 是 | 否 |

**JSON格式示例：**

```json
{
  "tenant_code": "TENANT_PH001",
  "field_mappings": [
    {
      "system_field_key": "user_id",
      "tenant_field_key": "UID",
      "tenant_field_name": "User ID",
      "is_enabled": true,
      "is_required": true
    },
    {
      "system_field_key": "user_name",
      "tenant_field_key": "UserName",
      "tenant_field_name": "Name",
      "is_enabled": true,
      "is_required": true
    },
    {
      "system_field_key": "mobile_number",
      "tenant_field_key": "Mobile",
      "tenant_field_name": "Phone Number",
      "is_enabled": true,
      "is_required": false
    }
  ]
}
```

### 9.4 数据导出支持

系统也支持将数据导出时转换回甲方的字段名：

**接口：** `GET /api/v1/tenants/{tenant_code}/cases/{case_id}?use_tenant_fields=true`

**响应（使用甲方字段名）：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "case_id": "PH-CASE-001",
    "fields": {
      "UID": "5983",
      "UserName": "Juan Dela Cruz",
      "Mobile": "+63 9123456789",
      "OutstandingAmt": 850.50,
      "OverdueDays": 5
    }
  }
}
```

### 9.5 未映射字段处理

如果甲方推送了未配置映射的字段，系统会记录警告但不会中断处理：

**推送数据包含未映射字段：**

```json
{
  "tenant_code": "TENANT_PH001",
  "cases": [
    {
      "case_id": "PH-CASE-001",
      "fields": {
        "UID": "5983",
        "UserName": "Juan Dela Cruz",
        "UnknownField": "some value"  // 未配置映射
      }
    }
  ]
}
```

**响应包含警告信息：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "success_count": 1,
    "fail_count": 0,
    "errors": [],
    "field_mapping_warnings": [
      {
        "case_id": "PH-CASE-001",
        "unmapped_fields": ["UnknownField"],
        "message": "以下字段未配置映射关系，已自动忽略"
      }
    ]
  }
}
```