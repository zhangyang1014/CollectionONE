-- 补全案件信息
-- 更新所有缺失的字段：客户姓名、产品名称、案件状态、App名称、逾期天数等

USE cco_system;

-- 首先检查是否需要添加product_name和app_name字段到cases表
-- 如果字段不存在，先添加字段
SET @dbname = DATABASE();
SET @tablename = "cases";
SET @columnname = "product_name";
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (table_name = @tablename)
      AND (table_schema = @dbname)
      AND (column_name = @columnname)
  ) > 0,
  "SELECT 'Column product_name already exists.';",
  "ALTER TABLE cases ADD COLUMN product_name VARCHAR(100) COMMENT '产品名称' AFTER case_status;"
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

SET @columnname = "app_name";
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (table_name = @tablename)
      AND (table_schema = @dbname)
      AND (column_name = @columnname)
  ) > 0,
  "SELECT 'Column app_name already exists.';",
  "ALTER TABLE cases ADD COLUMN app_name VARCHAR(100) COMMENT 'App名称' AFTER product_name;"
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 产品名称列表
SET @product_names = 'Cash Loan,Personal Loan,Payday Loan,Installment Loan,Quick Loan';

-- App名称列表
SET @app_names = 'MegaPeso,FlashCash,QuickMoney,PayEasy,LoanPro';

-- 更新所有案件信息
UPDATE cases 
SET 
    -- 补全客户姓名（如果为空）
    user_name = CASE 
        WHEN user_name IS NULL OR user_name = '' THEN
            CASE (id % 20)
                WHEN 0 THEN '李明'
                WHEN 1 THEN '王芳'
                WHEN 2 THEN '张伟'
                WHEN 3 THEN '刘静'
                WHEN 4 THEN '陈强'
                WHEN 5 THEN '杨丽'
                WHEN 6 THEN '赵军'
                WHEN 7 THEN '孙敏'
                WHEN 8 THEN '周涛'
                WHEN 9 THEN '吴艳'
                WHEN 10 THEN '郑华'
                WHEN 11 THEN '冯磊'
                WHEN 12 THEN '韩雪'
                WHEN 13 THEN '朱明'
                WHEN 14 THEN '马超'
                WHEN 15 THEN '林芳'
                WHEN 16 THEN '黄伟'
                WHEN 17 THEN '徐静'
                WHEN 18 THEN '高强'
                WHEN 19 THEN '罗敏'
                ELSE '唐军'
            END
        ELSE user_name
    END,
    
    -- 补全案件状态（确保有值）
    case_status = CASE 
        WHEN case_status IS NULL OR case_status = '' THEN
            CASE 
                WHEN outstanding_amount > 0 AND repaid_amount = 0 THEN 'pending_repayment'
                WHEN outstanding_amount > 0 AND repaid_amount > 0 THEN 'partial_repayment'
                WHEN outstanding_amount = 0 AND settlement_date IS NOT NULL THEN 'normal_settlement'
                ELSE 'pending_repayment'
            END
        ELSE case_status
    END,
    
    -- 补全逾期天数（如果为空，根据due_date计算）
    overdue_days = CASE 
        WHEN overdue_days IS NULL THEN
            CASE 
                WHEN due_date IS NOT NULL AND due_date < NOW() THEN
                    DATEDIFF(NOW(), due_date)
                WHEN outstanding_amount > 0 THEN
                    CASE (id % 30)
                        WHEN 0 THEN 1
                        WHEN 1 THEN 3
                        WHEN 2 THEN 5
                        WHEN 3 THEN 7
                        WHEN 4 THEN 12
                        WHEN 5 THEN 15
                        WHEN 6 THEN 18
                        WHEN 7 THEN 20
                        WHEN 8 THEN 22
                        WHEN 9 THEN 25
                        WHEN 10 THEN 28
                        WHEN 11 THEN 30
                        WHEN 12 THEN 35
                        WHEN 13 THEN 42
                        WHEN 14 THEN 50
                        WHEN 15 THEN 60
                        WHEN 16 THEN 65
                        WHEN 17 THEN 72
                        ELSE 15
                    END
                ELSE 0
            END
        ELSE overdue_days
    END,
    
    -- 补全产品名称
    product_name = CASE 
        WHEN product_name IS NULL OR product_name = '' THEN
            CASE (id % 5)
                WHEN 0 THEN 'Cash Loan'
                WHEN 1 THEN 'Personal Loan'
                WHEN 2 THEN 'Payday Loan'
                WHEN 3 THEN 'Installment Loan'
                ELSE 'Quick Loan'
            END
        ELSE product_name
    END,
    
    -- 补全App名称
    app_name = CASE 
        WHEN app_name IS NULL OR app_name = '' THEN
            CASE (id % 5)
                WHEN 0 THEN 'MegaPeso'
                WHEN 1 THEN 'FlashCash'
                WHEN 2 THEN 'QuickMoney'
                WHEN 3 THEN 'PayEasy'
                ELSE 'LoanPro'
            END
        ELSE app_name
    END,
    
    -- 更新due_date（如果为空，根据overdue_days计算）
    due_date = CASE 
        WHEN due_date IS NULL AND overdue_days IS NOT NULL THEN
            DATE_SUB(NOW(), INTERVAL overdue_days DAY)
        ELSE due_date
    END,
    
    -- 更新updated_at
    updated_at = NOW()
WHERE tenant_id = 1;

-- 验证更新结果
SELECT 
    COUNT(*) as total_cases,
    COUNT(CASE WHEN user_name IS NOT NULL AND user_name != '' THEN 1 END) as has_user_name,
    COUNT(CASE WHEN case_status IS NOT NULL AND case_status != '' THEN 1 END) as has_case_status,
    COUNT(CASE WHEN overdue_days IS NOT NULL THEN 1 END) as has_overdue_days,
    COUNT(CASE WHEN product_name IS NOT NULL AND product_name != '' THEN 1 END) as has_product_name,
    COUNT(CASE WHEN app_name IS NOT NULL AND app_name != '' THEN 1 END) as has_app_name
FROM cases 
WHERE tenant_id = 1;

-- 显示更新后的样本数据
SELECT 
    id,
    case_code,
    user_name,
    case_status,
    overdue_days,
    product_name,
    app_name,
    loan_amount,
    outstanding_amount
FROM cases 
WHERE tenant_id = 1 
ORDER BY id 
LIMIT 10;

