-- 插入测试案件数据
-- 假设"测试甲方1"的tenant_id是1（如果不是，请修改）

USE cco_system;

-- 插入10条测试案件数据
INSERT INTO cases (
    case_code, tenant_id, user_id, user_name, mobile, 
    case_status, overdue_days, loan_amount, repaid_amount, outstanding_amount,
    due_date, created_at, updated_at
) VALUES
('CASE001', 1, 'USER001', '张三', '13800000001', 'pending_repayment', 5, 10000.00, 0.00, 10000.00, DATE_SUB(NOW(), INTERVAL 5 DAY), NOW(), NOW()),
('CASE002', 1, 'USER002', '李四', '13800000002', 'partial_repayment', 10, 20000.00, 5000.00, 15000.00, DATE_SUB(NOW(), INTERVAL 10 DAY), NOW(), NOW()),
('CASE003', 1, 'USER003', '王五', '13800000003', 'pending_repayment', 15, 15000.00, 0.00, 15000.00, DATE_SUB(NOW(), INTERVAL 15 DAY), NOW(), NOW()),
('CASE004', 1, 'USER004', '赵六', '13800000004', 'normal_settlement', 0, 8000.00, 8000.00, 0.00, DATE_SUB(NOW(), INTERVAL 30 DAY), NOW(), NOW()),
('CASE005', 1, 'USER005', '钱七', '13800000005', 'pending_repayment', 20, 25000.00, 0.00, 25000.00, DATE_SUB(NOW(), INTERVAL 20 DAY), NOW(), NOW()),
('CASE006', 1, 'USER006', '孙八', '13800000006', 'partial_repayment', 8, 12000.00, 3000.00, 9000.00, DATE_SUB(NOW(), INTERVAL 8 DAY), NOW(), NOW()),
('CASE007', 1, 'USER007', '周九', '13800000007', 'pending_repayment', 12, 18000.00, 0.00, 18000.00, DATE_SUB(NOW(), INTERVAL 12 DAY), NOW(), NOW()),
('CASE008', 1, 'USER008', '吴十', '13800000008', 'extension_settlement', 0, 9000.00, 9000.00, 0.00, DATE_SUB(NOW(), INTERVAL 25 DAY), NOW(), NOW()),
('CASE009', 1, 'USER009', '郑一', '13800000009', 'pending_repayment', 25, 30000.00, 0.00, 30000.00, DATE_SUB(NOW(), INTERVAL 25 DAY), NOW(), NOW()),
('CASE010', 1, 'USER010', '王二', '13800000010', 'partial_repayment', 3, 6000.00, 2000.00, 4000.00, DATE_SUB(NOW(), INTERVAL 3 DAY), NOW(), NOW());



