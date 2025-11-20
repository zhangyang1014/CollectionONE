-- 更新小组群和小组的表结构

-- 1. 移除team_groups表的queue_id字段（如果存在）
-- SQLite不支持直接删除列，所以需要重建表
-- 但由于我们可能刚创建这个字段，先尝试创建一个新表

-- 2. 为team_admin_accounts表添加team_group_id字段
ALTER TABLE team_admin_accounts ADD COLUMN team_group_id INTEGER REFERENCES team_groups(id);

-- 3. 为team_admin_accounts表创建索引
CREATE INDEX IF NOT EXISTS idx_team_admin_accounts_team_group_id ON team_admin_accounts(team_group_id);

-- 4. 修改team_id为可选（已经是可选的，但确保正确）
-- SQLite不支持修改列约束，只能在创建时设置

-- 5. 为collection_teams表添加queue_id字段（必选）
ALTER TABLE collection_teams ADD COLUMN queue_id INTEGER NOT NULL DEFAULT 0 REFERENCES case_queues(id);

-- 6. 为collection_teams表创建索引
CREATE INDEX IF NOT EXISTS idx_collection_teams_queue_id ON collection_teams(queue_id);

-- 查看更新后的表结构
SELECT '更新后的team_groups表结构:' as info;
PRAGMA table_info(team_groups);

SELECT '更新后的team_admin_accounts表结构:' as info;
PRAGMA table_info(team_admin_accounts);

SELECT '更新后的collection_teams表结构:' as info;
PRAGMA table_info(collection_teams);

