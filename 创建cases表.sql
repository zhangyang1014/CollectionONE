-- 创建案件主表
-- 请在MySQL客户端中执行此SQL

USE cco_system;

CREATE TABLE IF NOT EXISTS `cases` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `case_code` VARCHAR(100) NOT NULL UNIQUE COMMENT '案件唯一标识',
    `tenant_id` BIGINT NOT NULL COMMENT '所属甲方ID',
    `agency_id` BIGINT COMMENT '所属催收机构ID',
    `team_id` BIGINT COMMENT '所属催收小组ID',
    `collector_id` BIGINT COMMENT '分配催员ID',
    `queue_id` BIGINT COMMENT '所属队列ID',
    `user_id` VARCHAR(100) COMMENT '用户编号',
    `user_name` VARCHAR(100) COMMENT '用户姓名',
    `mobile` VARCHAR(50) COMMENT '手机号',
    `case_status` VARCHAR(50) COMMENT '案件状态',
    `overdue_days` INT COMMENT '逾期天数（用于自动分配队列）',
    `loan_amount` DECIMAL(15, 2) COMMENT '贷款金额',
    `repaid_amount` DECIMAL(15, 2) DEFAULT 0 COMMENT '已还款金额',
    `outstanding_amount` DECIMAL(15, 2) COMMENT '逾期金额',
    `due_date` DATETIME COMMENT '到期日期',
    `settlement_date` DATETIME COMMENT '结清日期',
    `assigned_at` DATETIME COMMENT '分配时间',
    `last_contact_at` DATETIME COMMENT '最后联系时间',
    `next_follow_up_at` DATETIME COMMENT '下次跟进时间',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_case_code` (`case_code`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_agency_id` (`agency_id`),
    KEY `idx_team_id` (`team_id`),
    KEY `idx_collector_id` (`collector_id`),
    KEY `idx_queue_id` (`queue_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_case_status` (`case_status`),
    KEY `idx_overdue_days` (`overdue_days`),
    KEY `idx_due_date` (`due_date`),
    KEY `idx_assigned_at` (`assigned_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='案件主表';



