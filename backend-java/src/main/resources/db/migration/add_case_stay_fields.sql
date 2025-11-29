-- 为案件表添加停留相关字段
-- 执行时间：2025-11-27

USE cco_system;

-- 添加停留相关字段
ALTER TABLE `cases` 
ADD COLUMN `is_stay` TINYINT(1) DEFAULT 0 COMMENT '是否停留（独立状态字段，与case_status分离）' AFTER `next_follow_up_at`,
ADD COLUMN `stay_at` DATETIME NULL COMMENT '停留时间' AFTER `is_stay`,
ADD COLUMN `stay_by` BIGINT NULL COMMENT '停留操作人ID' AFTER `stay_at`,
ADD COLUMN `stay_released_at` DATETIME NULL COMMENT '解放停留时间' AFTER `stay_by`,
ADD COLUMN `stay_released_by` BIGINT NULL COMMENT '解放停留操作人ID' AFTER `stay_released_at`;

-- 添加索引以优化查询性能
ALTER TABLE `cases` 
ADD INDEX `idx_is_stay` (`is_stay`),
ADD INDEX `idx_stay_at` (`stay_at`),
ADD INDEX `idx_stay_by` (`stay_by`);

-- 更新现有数据：确保is_stay字段默认值为0（false）
UPDATE `cases` SET `is_stay` = 0 WHERE `is_stay` IS NULL;

