-- 为小组群表添加queue_id字段（关联催收队列）
ALTER TABLE team_groups ADD COLUMN queue_id INTEGER REFERENCES case_queues(id);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_team_groups_queue_id ON team_groups(queue_id);

-- 查看更新后的表结构
SELECT '小组群表 (team_groups) 更新后的结构:' as info;
PRAGMA table_info(team_groups);

