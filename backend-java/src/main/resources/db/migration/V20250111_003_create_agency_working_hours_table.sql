-- 创建机构作息时间表
-- 根据PRD要求，机构作息时间用于质检、通知等环节的时间控制
-- PRD要求：day_of_week 1-7（1=周一），start_time和end_time为Time类型
CREATE TABLE IF NOT EXISTS `agency_working_hours` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `agency_id` INT NOT NULL COMMENT '机构ID',
    `day_of_week` TINYINT NOT NULL COMMENT '星期几（1-7，1=周一，7=周日）',
    `start_time` TIME COMMENT '开始时间（HH:MM格式，NULL表示不工作）',
    `end_time` TIME COMMENT '结束时间（HH:MM格式，NULL表示不工作）',
    `is_active` TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_agency_id` (`agency_id`),
    KEY `idx_agency_day` (`agency_id`, `day_of_week`),
    UNIQUE KEY `uk_agency_day` (`agency_id`, `day_of_week`)
    -- 注意：以下外键约束在collection_agencies表创建后需要手动添加
    -- CONSTRAINT `fk_working_hours_agency` FOREIGN KEY (`agency_id`) REFERENCES `collection_agencies` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='机构作息时间表';

