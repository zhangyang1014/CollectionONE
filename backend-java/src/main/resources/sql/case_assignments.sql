-- 案件分配记录表
CREATE TABLE IF NOT EXISTS `case_assignments` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `case_id` BIGINT NOT NULL COMMENT '案件ID',
  `collector_id` BIGINT NOT NULL COMMENT '催员ID',
  `assigned_by` BIGINT NOT NULL COMMENT '分配人ID',
  `assigned_at` DATETIME NOT NULL COMMENT '分配时间',
  `ignore_queue_limit` TINYINT(1) DEFAULT 0 COMMENT '是否忽略队列限制',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_case_id` (`case_id`),
  KEY `idx_collector_id` (`collector_id`),
  KEY `idx_assigned_at` (`assigned_at`),
  KEY `idx_assigned_by` (`assigned_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='案件分配记录表';









