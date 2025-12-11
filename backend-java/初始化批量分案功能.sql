-- ================================
-- 批量分案功能数据库初始化脚本
-- ================================

-- 1. 创建案件分配记录表
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

-- 2. 检查必要的表是否存在
-- 如果不存在，请先执行相应的建表SQL

-- 检查cases表
SELECT 'cases表' AS 表名, COUNT(*) AS 记录数 FROM cases;

-- 检查collectors表
SELECT 'collectors表' AS 表名, COUNT(*) AS 记录数 FROM collectors;

-- 检查collection_teams表
SELECT 'collection_teams表' AS 表名, COUNT(*) AS 记录数 FROM collection_teams;

-- 检查collection_agencies表
SELECT 'collection_agencies表' AS 表名, COUNT(*) AS 记录数 FROM collection_agencies;

-- 检查case_queues表
SELECT 'case_queues表' AS 表名, COUNT(*) AS 记录数 FROM case_queues;

-- 3. 查看现有数据（可选）
-- 查看案件数据
SELECT 
    id,
    case_code,
    user_name,
    collector_id,
    team_id,
    agency_id,
    queue_id,
    case_status,
    assigned_at
FROM cases
LIMIT 10;

-- 查看催员数据
SELECT 
    c.id,
    c.collector_name,
    c.collector_code,
    c.agency_id,
    ca.agency_name,
    c.team_id,
    ct.team_name,
    ct.queue_id,
    cq.queue_name,
    c.status,
    c.is_active
FROM collectors c
LEFT JOIN collection_agencies ca ON c.agency_id = ca.id
LEFT JOIN collection_teams ct ON c.team_id = ct.id
LEFT JOIN case_queues cq ON ct.queue_id = cq.id
WHERE c.is_active = 1 AND c.status = 'active'
LIMIT 10;

-- ================================
-- 批量分案功能测试数据（可选）
-- ================================

-- 如果需要测试数据，可以执行以下脚本

-- 插入测试甲方
INSERT IGNORE INTO tenants (id, tenant_code, tenant_name, tenant_name_en, contact_person, contact_phone, contact_email, is_active, created_at, updated_at)
VALUES (1, 'T001', '测试甲方', 'Test Tenant', '张三', '13800138000', 'test@example.com', 1, NOW(), NOW());

-- 插入测试机构
INSERT IGNORE INTO collection_agencies (id, tenant_id, agency_code, agency_name, agency_name_en, contact_person, contact_phone, is_active, created_at, updated_at)
VALUES (1, 1, 'A001', '测试机构A', 'Agency A', '李四', '13800138001', 1, NOW(), NOW());

-- 插入测试队列
INSERT IGNORE INTO case_queues (id, tenant_id, queue_code, queue_name, queue_name_en, overdue_days_start, overdue_days_end, is_active, created_at, updated_at)
VALUES 
(1, 1, 'M1', 'M1队列', 'M1 Queue', 1, 30, 1, NOW(), NOW()),
(2, 1, 'M2', 'M2队列', 'M2 Queue', 31, 60, 1, NOW(), NOW());

-- 插入测试小组
INSERT IGNORE INTO collection_teams (id, tenant_id, agency_id, queue_id, team_code, team_name, team_name_en, team_type, is_active, created_at, updated_at)
VALUES 
(1, 1, 1, 1, 'TEAM001', '测试小组1', 'Team 1', '电催组', 1, NOW(), NOW()),
(2, 1, 1, 2, 'TEAM002', '测试小组2', 'Team 2', '电催组', 1, NOW(), NOW());

-- 插入测试催员
INSERT IGNORE INTO collectors (id, tenant_id, agency_id, team_id, collector_code, collector_name, login_id, password_hash, mobile, status, is_active, created_at, updated_at)
VALUES 
(1, 1, 1, 1, 'C001', '催员张三', 'collector1', '$2a$10$abcdefghijklmnopqrstuvwxyz', '13800138010', 'active', 1, NOW(), NOW()),
(2, 1, 1, 1, 'C002', '催员李四', 'collector2', '$2a$10$abcdefghijklmnopqrstuvwxyz', '13800138011', 'active', 1, NOW(), NOW()),
(3, 1, 1, 2, 'C003', '催员王五', 'collector3', '$2a$10$abcdefghijklmnopqrstuvwxyz', '13800138012', 'active', 1, NOW(), NOW());

-- 插入测试案件
INSERT IGNORE INTO cases (id, case_code, tenant_id, agency_id, team_id, collector_id, queue_id, user_id, user_name, mobile, case_status, product_name, app_name, overdue_days, loan_amount, repaid_amount, outstanding_amount, due_date, created_at, updated_at)
VALUES 
(1, 'CASE001', 1, NULL, NULL, NULL, 1, 'U001', '客户A', '13900139001', 'pending_repayment', '测试产品', '测试APP', 15, 10000.00, 0.00, 10000.00, DATE_SUB(NOW(), INTERVAL 15 DAY), NOW(), NOW()),
(2, 'CASE002', 1, NULL, NULL, NULL, 1, 'U002', '客户B', '13900139002', 'pending_repayment', '测试产品', '测试APP', 20, 15000.00, 0.00, 15000.00, DATE_SUB(NOW(), INTERVAL 20 DAY), NOW(), NOW()),
(3, 'CASE003', 1, NULL, NULL, NULL, 1, 'U003', '客户C', '13900139003', 'pending_repayment', '测试产品', '测试APP', 25, 20000.00, 0.00, 20000.00, DATE_SUB(NOW(), INTERVAL 25 DAY), NOW(), NOW()),
(4, 'CASE004', 1, NULL, NULL, NULL, 2, 'U004', '客户D', '13900139004', 'pending_repayment', '测试产品', '测试APP', 35, 12000.00, 0.00, 12000.00, DATE_SUB(NOW(), INTERVAL 35 DAY), NOW(), NOW()),
(5, 'CASE005', 1, NULL, NULL, NULL, 2, 'U005', '客户E', '13900139005', 'pending_repayment', '测试产品', '测试APP', 40, 18000.00, 0.00, 18000.00, DATE_SUB(NOW(), INTERVAL 40 DAY), NOW(), NOW());

-- ================================
-- 验证数据
-- ================================

-- 查看测试案件
SELECT 
    id,
    case_code,
    user_name,
    queue_id,
    case_status,
    outstanding_amount
FROM cases
WHERE case_code LIKE 'CASE%'
ORDER BY id;

-- 查看测试催员（含小组队列信息）
SELECT 
    c.id,
    c.collector_name,
    c.collector_code,
    ct.team_name,
    ct.queue_id,
    cq.queue_name
FROM collectors c
LEFT JOIN collection_teams ct ON c.team_id = ct.id
LEFT JOIN case_queues cq ON ct.queue_id = cq.id
WHERE c.collector_code LIKE 'C%'
ORDER BY c.id;

-- ================================
-- 测试查询（验证功能可用）
-- ================================

-- 1. 测试获取催员列表（模拟API查询）
SELECT 
    c.id,
    c.collector_name AS collectorName,
    c.collector_code AS collectorCode,
    c.agency_id AS agencyId,
    ca.agency_name AS agencyName,
    c.team_id AS teamId,
    ct.team_name AS teamName,
    ct.queue_id AS queueId,
    cq.queue_name AS queueName,
    c.status,
    COALESCE(
        (SELECT COUNT(*) 
         FROM cases cs 
         WHERE cs.collector_id = c.id 
           AND cs.case_status NOT IN ('normal_settlement', 'extension_settlement')
           AND DATE(cs.assigned_at) = CURDATE()
        ), 0
    ) AS currentCaseCount
FROM collectors c
LEFT JOIN collection_agencies ca ON c.agency_id = ca.id
LEFT JOIN collection_teams ct ON c.team_id = ct.id
LEFT JOIN case_queues cq ON ct.queue_id = cq.id
WHERE c.is_active = 1
  AND c.status = 'active'
ORDER BY c.id;

-- 2. 测试队列限制检查（查看案件和催员的队列是否匹配）
SELECT 
    cs.id AS case_id,
    cs.case_code,
    cs.queue_id AS case_queue_id,
    cq1.queue_name AS case_queue_name,
    c.id AS collector_id,
    c.collector_name,
    ct.queue_id AS collector_team_queue_id,
    cq2.queue_name AS collector_team_queue_name,
    IF(cs.queue_id = ct.queue_id, '匹配', '不匹配') AS match_status
FROM cases cs
CROSS JOIN collectors c
LEFT JOIN case_queues cq1 ON cs.queue_id = cq1.id
LEFT JOIN collection_teams ct ON c.team_id = ct.id
LEFT JOIN case_queues cq2 ON ct.queue_id = cq2.id
WHERE cs.case_code IN ('CASE001', 'CASE002', 'CASE004')
  AND c.collector_code IN ('C001', 'C002', 'C003')
  AND cs.queue_id != ct.queue_id  -- 只显示不匹配的
ORDER BY cs.id, c.id;

-- ================================
-- 说明
-- ================================
-- 1. 执行本脚本后，case_assignments表将被创建
-- 2. 如果执行了测试数据部分，将插入：
--    - 1个甲方、1个机构、2个队列、2个小组、3个催员、5个案件
-- 3. 测试数据说明：
--    - CASE001-CASE003属于M1队列（队列ID=1）
--    - CASE004-CASE005属于M2队列（队列ID=2）
--    - C001、C002属于小组1（关联M1队列）
--    - C003属于小组2（关联M2队列）
--    - 如果将CASE001分配给C003，会触发队列限制提示
-- 4. 验证功能：
--    - 后端服务启动后，访问：http://localhost:8080/api/v1/cases/collectors-for-assign
--    - 应该能看到3个催员的列表
--    - 使用Postman测试批量分案接口
























