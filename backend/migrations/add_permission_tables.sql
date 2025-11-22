-- ============================================
-- 权限系统数据库表创建脚本
-- 创建日期: 2025-01-20
-- 说明: 创建权限管理所需的3张核心表
-- ============================================

-- 表1: permission_modules (权限模块表)
-- 用于组织和分类权限项
CREATE TABLE IF NOT EXISTS permission_modules (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    module_key VARCHAR(50) NOT NULL UNIQUE,
    module_name VARCHAR(100) NOT NULL,
    sort_order INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建模块表索引
CREATE INDEX IF NOT EXISTS idx_module_key ON permission_modules(module_key);
CREATE INDEX IF NOT EXISTS idx_module_sort ON permission_modules(sort_order);

-- 表2: permission_items (权限项表)
-- 存储具体的权限功能点
CREATE TABLE IF NOT EXISTS permission_items (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    module_id INTEGER NOT NULL,
    item_key VARCHAR(50) NOT NULL,
    item_name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    sort_order INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (module_id) REFERENCES permission_modules(id) ON DELETE CASCADE
);

-- 创建权限项表索引和唯一约束
CREATE UNIQUE INDEX IF NOT EXISTS uk_module_item ON permission_items(module_id, item_key);
CREATE INDEX IF NOT EXISTS idx_item_module ON permission_items(module_id);
CREATE INDEX IF NOT EXISTS idx_item_sort ON permission_items(sort_order);

-- 表3: role_permission_configs (角色权限配置表)
-- 存储角色对各权限项的访问级别
CREATE TABLE IF NOT EXISTS role_permission_configs (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    tenant_id INTEGER,
    role_code VARCHAR(50) NOT NULL,
    permission_item_id INTEGER NOT NULL,
    permission_level VARCHAR(20) NOT NULL DEFAULT 'none',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by INTEGER,
    updated_by INTEGER,
    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_item_id) REFERENCES permission_items(id) ON DELETE CASCADE,
    CHECK (permission_level IN ('none', 'readonly', 'editable'))
);

-- 创建角色权限配置表索引和唯一约束
CREATE UNIQUE INDEX IF NOT EXISTS uk_tenant_role_item ON role_permission_configs(tenant_id, role_code, permission_item_id);
CREATE INDEX IF NOT EXISTS idx_tenant_role ON role_permission_configs(tenant_id, role_code);
CREATE INDEX IF NOT EXISTS idx_permission_item ON role_permission_configs(permission_item_id);
CREATE INDEX IF NOT EXISTS idx_role_code ON role_permission_configs(role_code);

-- 创建更新时间触发器 (SQLite)
-- permission_modules 更新触发器
CREATE TRIGGER IF NOT EXISTS update_permission_modules_timestamp 
AFTER UPDATE ON permission_modules
FOR EACH ROW
BEGIN
    UPDATE permission_modules SET updated_at = CURRENT_TIMESTAMP WHERE id = OLD.id;
END;

-- permission_items 更新触发器
CREATE TRIGGER IF NOT EXISTS update_permission_items_timestamp 
AFTER UPDATE ON permission_items
FOR EACH ROW
BEGIN
    UPDATE permission_items SET updated_at = CURRENT_TIMESTAMP WHERE id = OLD.id;
END;

-- role_permission_configs 更新触发器
CREATE TRIGGER IF NOT EXISTS update_role_permission_configs_timestamp 
AFTER UPDATE ON role_permission_configs
FOR EACH ROW
BEGIN
    UPDATE role_permission_configs SET updated_at = CURRENT_TIMESTAMP WHERE id = OLD.id;
END;

-- ============================================
-- 说明：
-- 1. permission_level 字段值：
--    - 'none': 不可见，完全无权访问
--    - 'readonly': 仅可见，只能查看不能修改
--    - 'editable': 可编辑，可以查看和修改
--
-- 2. tenant_id 为 NULL 表示系统默认配置
--    所有甲方如果没有自定义配置，则使用系统默认配置
--
-- 3. 角色代码 (role_code) 包括：
--    - SUPER_ADMIN: 超级管理员
--    - TENANT_ADMIN: 甲方管理员
--    - AGENCY_ADMIN: 机构管理员
--    - TEAM_LEADER: 小组长
--    - QUALITY_INSPECTOR: 质检员
--    - DATA_SOURCE: 数据源
--    - COLLECTOR: 催员
-- ============================================

