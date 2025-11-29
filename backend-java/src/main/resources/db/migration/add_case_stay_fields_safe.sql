-- 为案件表添加停留相关字段（安全版本，如果字段已存在则跳过）
-- 执行时间：2025-11-27

USE cco_system;

-- 检查并添加 is_stay 字段
SET @dbname = DATABASE();
SET @tablename = 'cases';
SET @columnname = 'is_stay';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (table_name = @tablename)
      AND (table_schema = @dbname)
      AND (column_name = @columnname)
  ) > 0,
  'SELECT 1', -- 字段已存在，不执行
  CONCAT('ALTER TABLE `', @tablename, '` ADD COLUMN `', @columnname, '` TINYINT(1) DEFAULT 0 COMMENT ''是否停留（独立状态字段，与case_status分离）'' AFTER `next_follow_up_at`')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 检查并添加 stay_at 字段
SET @columnname = 'stay_at';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (table_name = @tablename)
      AND (table_schema = @dbname)
      AND (column_name = @columnname)
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE `', @tablename, '` ADD COLUMN `', @columnname, '` DATETIME NULL COMMENT ''停留时间'' AFTER `is_stay`')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 检查并添加 stay_by 字段
SET @columnname = 'stay_by';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (table_name = @tablename)
      AND (table_schema = @dbname)
      AND (column_name = @columnname)
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE `', @tablename, '` ADD COLUMN `', @columnname, '` BIGINT NULL COMMENT ''停留操作人ID'' AFTER `stay_at`')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 检查并添加 stay_released_at 字段
SET @columnname = 'stay_released_at';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (table_name = @tablename)
      AND (table_schema = @dbname)
      AND (column_name = @columnname)
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE `', @tablename, '` ADD COLUMN `', @columnname, '` DATETIME NULL COMMENT ''解放停留时间'' AFTER `stay_by`')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 检查并添加 stay_released_by 字段
SET @columnname = 'stay_released_by';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (table_name = @tablename)
      AND (table_schema = @dbname)
      AND (column_name = @columnname)
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE `', @tablename, '` ADD COLUMN `', @columnname, '` BIGINT NULL COMMENT ''解放停留操作人ID'' AFTER `stay_released_at`')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 添加索引（如果不存在）
-- 注意：MySQL不支持IF NOT EXISTS for indexes，所以如果索引已存在会报错，但可以忽略
ALTER TABLE `cases` ADD INDEX `idx_is_stay` (`is_stay`);
ALTER TABLE `cases` ADD INDEX `idx_stay_at` (`stay_at`);
ALTER TABLE `cases` ADD INDEX `idx_stay_by` (`stay_by`);

-- 更新现有数据：确保is_stay字段默认值为0（false）
UPDATE `cases` SET `is_stay` = 0 WHERE `is_stay` IS NULL;

