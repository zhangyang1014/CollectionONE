-- 快速检查停留字段是否存在
USE cco_system;

-- 检查字段是否存在
SELECT 
    COLUMN_NAME,
    COLUMN_TYPE,
    IS_NULLABLE,
    COLUMN_DEFAULT,
    COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'cco_system'
  AND TABLE_NAME = 'cases'
  AND COLUMN_NAME IN ('is_stay', 'stay_at', 'stay_by', 'stay_released_at', 'stay_released_by')
ORDER BY COLUMN_NAME;

-- 如果上面的查询返回0行，说明字段不存在，需要执行迁移SQL
-- 执行: mysql -u root -p cco_system < backend-java/src/main/resources/db/migration/add_case_stay_fields.sql

