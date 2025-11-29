-- 为案件重新分案配置表添加小组ID列表字段
ALTER TABLE `case_reassign_configs` 
ADD COLUMN `team_ids` JSON COMMENT '小组ID列表（JSON数组），为空表示该队列下所有小组' AFTER `target_id`;

-- 添加索引以支持按队列和小组查询
ALTER TABLE `case_reassign_configs`
ADD INDEX `idx_queue_team` (`target_id`, `config_type`);

