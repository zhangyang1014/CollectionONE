-- 创建小组群表
CREATE TABLE IF NOT EXISTS team_groups (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    tenant_id INTEGER NOT NULL,
    agency_id INTEGER NOT NULL,
    group_code VARCHAR(100) NOT NULL,
    group_name VARCHAR(200) NOT NULL,
    group_name_en VARCHAR(200),
    spv_id INTEGER,
    description TEXT,
    sort_order INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    FOREIGN KEY (agency_id) REFERENCES collection_agencies (id),
    FOREIGN KEY (spv_id) REFERENCES collectors (id)
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_team_groups_tenant_id ON team_groups(tenant_id);
CREATE INDEX IF NOT EXISTS idx_team_groups_agency_id ON team_groups(agency_id);
CREATE INDEX IF NOT EXISTS idx_team_groups_is_active ON team_groups(is_active);

-- 检查collection_teams表是否已有team_group_id字段
-- 如果没有，则添加
-- 注意：SQLite不支持IF NOT EXISTS for ALTER TABLE
-- 所以如果字段已存在，这条命令会失败，但不影响其他操作

-- 尝试添加team_group_id字段到collection_teams表
-- 如果字段已存在，这个命令会失败，可以忽略错误
ALTER TABLE collection_teams ADD COLUMN team_group_id INTEGER REFERENCES team_groups(id);

-- 创建索引（即使字段已存在，索引可能还不存在）
CREATE INDEX IF NOT EXISTS idx_collection_teams_team_group_id ON collection_teams(team_group_id);

-- 查看表结构
SELECT '小组群表 (team_groups) 结构:' as info;
PRAGMA table_info(team_groups);

SELECT '小组表 (collection_teams) 结构:' as info;
PRAGMA table_info(collection_teams);

