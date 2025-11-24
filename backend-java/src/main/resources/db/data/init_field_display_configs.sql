-- 初始化甲方字段展示配置数据
-- 为甲方A创建三个场景的默认配置

-- ============================================
-- 场景1: 控台案件管理列表 (admin_case_list)
-- ============================================

INSERT INTO `tenant_field_display_configs` 
(`tenant_id`, `scene_type`, `scene_name`, `field_key`, `field_name`, `field_data_type`, `field_source`, 
 `sort_order`, `display_width`, `color_type`, `format_rule`, `is_searchable`, `is_filterable`, `is_range_searchable`, `created_by`) 
VALUES
-- 案件编号
(1, 'admin_case_list', '控台案件管理列表', 'case_code', '案件编号', 'String', 'standard', 
 1, 180, 'normal', NULL, 1, 0, 0, 'system'),

-- 客户姓名
(1, 'admin_case_list', '控台案件管理列表', 'user_name', '客户姓名', 'String', 'standard', 
 2, 120, 'normal', NULL, 1, 0, 0, 'system'),

-- 手机号码
(1, 'admin_case_list', '控台案件管理列表', 'mobile', '手机号码', 'String', 'standard', 
 3, 140, 'normal', NULL, 1, 0, 0, 'system'),

-- 贷款金额
(1, 'admin_case_list', '控台案件管理列表', 'loan_amount', '贷款金额', 'Decimal', 'standard', 
 4, 120, 'normal', '{"format_type": "currency", "prefix": "¥", "suffix": ""}', 0, 0, 1, 'system'),

-- 未还金额
(1, 'admin_case_list', '控台案件管理列表', 'outstanding_amount', '未还金额', 'Decimal', 'standard', 
 5, 120, 'red', '{"format_type": "currency", "prefix": "¥", "suffix": ""}', 0, 0, 1, 'system'),

-- 逾期天数
(1, 'admin_case_list', '控台案件管理列表', 'overdue_days', '逾期天数', 'Integer', 'standard', 
 6, 100, 'red', NULL, 0, 0, 1, 'system'),

-- 案件状态
(1, 'admin_case_list', '控台案件管理列表', 'case_status', '案件状态', 'Enum', 'standard', 
 7, 110, 'normal', NULL, 0, 1, 0, 'system'),

-- 产品名称
(1, 'admin_case_list', '控台案件管理列表', 'product_name', '产品名称', 'String', 'standard', 
 8, 130, 'normal', NULL, 1, 0, 0, 'system'),

-- App名称
(1, 'admin_case_list', '控台案件管理列表', 'app_name', 'App名称', 'String', 'standard', 
 9, 130, 'normal', NULL, 1, 0, 0, 'system'),

-- 到期日期
(1, 'admin_case_list', '控台案件管理列表', 'due_date', '到期日期', 'Date', 'standard', 
 10, 120, 'normal', NULL, 0, 0, 1, 'system');


-- ============================================
-- 场景2: 催员案件列表 (collector_case_list)
-- ============================================

INSERT INTO `tenant_field_display_configs` 
(`tenant_id`, `scene_type`, `scene_name`, `field_key`, `field_name`, `field_data_type`, `field_source`, 
 `sort_order`, `display_width`, `color_type`, `format_rule`, `is_searchable`, `is_filterable`, `is_range_searchable`, `created_by`) 
VALUES
-- 案件编号
(1, 'collector_case_list', '催员案件列表', 'case_code', '案件编号', 'String', 'standard', 
 1, 160, 'normal', NULL, 1, 0, 0, 'system'),

-- 客户姓名
(1, 'collector_case_list', '催员案件列表', 'user_name', '客户姓名', 'String', 'standard', 
 2, 100, 'normal', NULL, 1, 0, 0, 'system'),

-- 手机号码
(1, 'collector_case_list', '催员案件列表', 'mobile', '手机号码', 'String', 'standard', 
 3, 130, 'normal', NULL, 1, 0, 0, 'system'),

-- 应还金额
(1, 'collector_case_list', '催员案件列表', 'outstanding_amount', '应还金额', 'Decimal', 'standard', 
 4, 120, 'normal', '{"format_type": "currency", "prefix": "¥", "suffix": ""}', 0, 0, 1, 'system'),

-- 逾期天数
(1, 'collector_case_list', '催员案件列表', 'overdue_days', '逾期天数', 'Integer', 'standard', 
 5, 100, 'red', NULL, 0, 0, 1, 'system'),

-- 案件状态
(1, 'collector_case_list', '催员案件列表', 'case_status', '案件状态', 'Enum', 'standard', 
 6, 90, 'normal', NULL, 0, 1, 0, 'system'),

-- 队列名称
(1, 'collector_case_list', '催员案件列表', 'queue_name', '队列', 'String', 'standard', 
 7, 80, 'normal', NULL, 0, 1, 0, 'system'),

-- 产品名称
(1, 'collector_case_list', '催员案件列表', 'product_name', '产品', 'String', 'standard', 
 8, 110, 'normal', NULL, 1, 0, 0, 'system');


-- ============================================
-- 场景3: 催员案件详情 (collector_case_detail)
-- ============================================

INSERT INTO `tenant_field_display_configs` 
(`tenant_id`, `scene_type`, `scene_name`, `field_key`, `field_name`, `field_data_type`, `field_source`, 
 `sort_order`, `display_width`, `color_type`, `format_rule`, `is_searchable`, `is_filterable`, `is_range_searchable`, `created_by`) 
VALUES
-- 客户信息组
(1, 'collector_case_detail', '催员案件详情', 'user_name', '客户姓名', 'String', 'standard', 
 1, 0, 'normal', NULL, 0, 0, 0, 'system'),

(1, 'collector_case_detail', '催员案件详情', 'mobile', '手机号码', 'String', 'standard', 
 2, 0, 'normal', NULL, 0, 0, 0, 'system'),

(1, 'collector_case_detail', '催员案件详情', 'id_number', '证件号码', 'String', 'standard', 
 3, 0, 'normal', NULL, 0, 0, 0, 'system'),

(1, 'collector_case_detail', '催员案件详情', 'email', '邮箱', 'String', 'standard', 
 4, 0, 'normal', NULL, 0, 0, 0, 'system'),

(1, 'collector_case_detail', '催员案件详情', 'address', '地址', 'String', 'standard', 
 5, 0, 'normal', NULL, 0, 0, 0, 'system'),

-- 贷款信息组
(1, 'collector_case_detail', '催员案件详情', 'loan_amount', '贷款金额', 'Decimal', 'standard', 
 11, 0, 'normal', '{"format_type": "currency", "prefix": "¥", "suffix": ""}', 0, 0, 0, 'system'),

(1, 'collector_case_detail', '催员案件详情', 'outstanding_amount', '未还金额', 'Decimal', 'standard', 
 12, 0, 'red', '{"format_type": "currency", "prefix": "¥", "suffix": ""}', 0, 0, 0, 'system'),

(1, 'collector_case_detail', '催员案件详情', 'overdue_days', '逾期天数', 'Integer', 'standard', 
 13, 0, 'red', NULL, 0, 0, 0, 'system'),

(1, 'collector_case_detail', '催员案件详情', 'due_date', '到期日期', 'Date', 'standard', 
 14, 0, 'normal', NULL, 0, 0, 0, 'system'),

(1, 'collector_case_detail', '催员案件详情', 'loan_date', '放款日期', 'Date', 'standard', 
 15, 0, 'normal', NULL, 0, 0, 0, 'system'),

(1, 'collector_case_detail', '催员案件详情', 'product_name', '产品名称', 'String', 'standard', 
 16, 0, 'normal', NULL, 0, 0, 0, 'system'),

(1, 'collector_case_detail', '催员案件详情', 'app_name', 'App名称', 'String', 'standard', 
 17, 0, 'normal', NULL, 0, 0, 0, 'system');

COMMIT;






