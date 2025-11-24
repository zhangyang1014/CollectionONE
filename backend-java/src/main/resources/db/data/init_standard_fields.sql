-- ============================================
-- 标准字段管理初始化SQL
-- 基于CSV配置文件生成
-- 生成时间: 1763968513.2075665
-- ============================================

USE `cco_system`;

-- 清空现有数据（可选，谨慎使用）
-- DELETE FROM `tenant_field_configs` WHERE `tenant_id` = 1;
-- DELETE FROM `standard_fields`;
-- DELETE FROM `field_groups` WHERE `id` IN (11, 12, 13, 14);
-- DELETE FROM `field_groups` WHERE `id` IN (1, 2, 3, 4, 5);


-- ============================================
-- 字段分组初始化
-- ============================================

-- 一级分组
INSERT INTO `field_groups` (`id`, `group_key`, `group_name`, `group_name_en`, `parent_id`, `sort_order`, `is_active`, `created_at`, `updated_at`) VALUES
(1, 'customer_basic', '客户基础信息', 'Customer Basic Information', NULL, 1, 1, NOW(), NOW()),
(2, 'loan_details', '贷款详情', 'Loan Details', NULL, 2, 1, NOW(), NOW()),
(3, 'borrowing_records', '借款记录', 'Borrowing Records', NULL, 3, 1, NOW(), NOW()),
(4, 'repayment_records', '还款记录', 'Repayment Records', NULL, 4, 1, NOW(), NOW()),
(5, 'installment_details', '分期详情', 'Installment Details', NULL, 5, 1, NOW(), NOW());

-- 二级分组（客户基础信息的子分组）
INSERT INTO `field_groups` (`id`, `group_key`, `group_name`, `group_name_en`, `parent_id`, `sort_order`, `is_active`, `created_at`, `updated_at`) VALUES
(11, 'identity_info', '基础身份信息', 'Identity Information', 1, 1, 1, NOW(), NOW()),
(12, 'education', '教育信息', 'Education', 1, 2, 1, NOW(), NOW()),
(13, 'employment', '职业信息', 'Employment', 1, 3, 1, NOW(), NOW()),
(14, 'user_behavior', '用户行为与信用', 'User Behavior & Credit', 1, 4, 1, NOW(), NOW()),
(15, 'contact_info', '联系方式', 'Contact Information', 1, 5, 1, NOW(), NOW());


-- ============================================
-- 标准字段初始化
-- ============================================

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(1, 'user_id', '用户编号', 'user_id', 'String', 11, 1, 0, '系统内部唯一用户标识', '5983', NULL, 1, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(2, 'user_name', '用户姓名', 'user_name', 'String', 11, 1, 0, '借款人姓名', 'Juan Dela Cruz', NULL, 2, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(3, 'gender', '性别', 'gender', 'Enum', 11, 0, 0, '用户性别', 'Male / Female', '[{"standard_name": "Male", "standard_id": "male", "tenant_name": "Male", "tenant_id": "male"}, {"standard_name": "Female", "standard_id": "female", "tenant_name": "Female", "tenant_id": "female"}]', 3, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(4, 'birth_date', '出生日期', 'birth_date', 'Date', 11, 0, 0, '用户生日', '1980/5/5', NULL, 4, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(5, 'nationality', '国籍', 'nationality', 'String', 11, 0, 1, '国籍或居住国家', 'Philippines', NULL, 5, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(6, 'marital_status', '婚姻状况', 'marital_status', 'Enum', 11, 0, 0, '婚姻状态', 'Single / Married', '[{"standard_name": "Single", "standard_id": "single", "tenant_name": "Single", "tenant_id": "single"}, {"standard_name": "Married", "standard_id": "married", "tenant_name": "Married", "tenant_id": "married"}]', 6, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(7, 'id_type', '证件类型', 'id_type', 'Enum', 11, 0, 0, '身份证件类型', 'National ID / Passport / Driver’s License', '[{"standard_name": "National ID", "standard_id": "national_id", "tenant_name": "National ID", "tenant_id": "national_id"}, {"standard_name": "Passport", "standard_id": "passport", "tenant_name": "Passport", "tenant_id": "passport"}, {"standard_name": "Driver’s License", "standard_id": "driver’s_license", "tenant_name": "Driver’s License", "tenant_id": "driver’s_license"}]', 7, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(8, 'id_number', '证件号码', 'id_number', 'String', 11, 0, 0, '证件号码', 'N2319594759', NULL, 8, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(9, 'address', '居住地址', 'address', 'String', 11, 0, 0, '包含街道、城市、省份、邮编', '123 Main St, Quezon City', NULL, 9, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(10, 'years_at_address', '居住年限', 'years_at_address', 'Integer', 11, 0, 0, '当前居住地年数', '5', NULL, 10, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(11, 'housing_type', '居住类型', 'housing_type', 'Enum', 11, 0, 0, '居住情况', 'Own / Rent / With Family', '[{"standard_name": "Own", "standard_id": "own", "tenant_name": "Own", "tenant_id": "own"}, {"standard_name": "Rent", "standard_id": "rent", "tenant_name": "Rent", "tenant_id": "rent"}, {"standard_name": "With Family", "standard_id": "with_family", "tenant_name": "With Family", "tenant_id": "with_family"}]', 11, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(12, 'mobile_number', '手机号码', 'mobile_number', 'String', 15, 0, 0, '用户注册手机号', '+63 9123456789', NULL, 12, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(13, 'company_phone', '公司联系电话', 'company_phone', 'String', 15, 0, 0, '公司电话', '+63 2 9123456', NULL, 13, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(14, 'education_level', '教育程度', 'education_level', 'Enum', 12, 0, 0, '最高学历', 'University / High School', '[{"standard_name": "University", "standard_id": "university", "tenant_name": "University", "tenant_id": "university"}, {"standard_name": "High School", "standard_id": "high_school", "tenant_name": "High School", "tenant_id": "high_school"}]', 14, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(15, 'school_name', '学校名称', 'school_name', 'String', 12, 0, 0, '毕业学校或当前学校', 'University of the Philippines', NULL, 15, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(16, 'major', '专业', 'major', 'String', 12, 0, 0, '学习或毕业专业', 'Business Administration', NULL, 16, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(17, 'employment_status', '工作状态', 'employment_status', 'Enum', 13, 0, 0, '当前职业状态', 'Employed / Self-employed / Student / Unemployed', '[{"standard_name": "Employed", "standard_id": "employed", "tenant_name": "Employed", "tenant_id": "employed"}, {"standard_name": "Self-employed", "standard_id": "self-employed", "tenant_name": "Self-employed", "tenant_id": "self-employed"}, {"standard_name": "Student", "standard_id": "student", "tenant_name": "Student", "tenant_id": "student"}, {"standard_name": "Unemployed", "standard_id": "unemployed", "tenant_name": "Unemployed", "tenant_id": "unemployed"}]', 17, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(18, 'company_name', '公司名称', 'company_name', 'String', 13, 0, 0, '所在公司', 'ABC Transport', NULL, 18, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(19, 'job_title', '职位', 'job_title', 'String', 13, 0, 0, '当前职位或职称', 'Driver', NULL, 19, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(20, 'industry', '行业类别', 'industry', 'String', 13, 0, 0, '所属行业', 'Transportation', NULL, 20, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(21, 'years_of_employment', '工作年限', 'years_of_employment', 'Integer', 13, 0, 0, '当前岗位工作时长', '3', NULL, 21, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(22, 'work_address', '工作地址', 'work_address', 'String', 13, 0, 0, '工作地点', '15 Pasay Ave, Manila', NULL, 22, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(23, 'income_type', '收入类型', 'income_type', 'Enum', 13, 0, 0, '工资发放周期', 'Monthly / Weekly / Daily', '[{"standard_name": "Monthly", "standard_id": "monthly", "tenant_name": "Monthly", "tenant_id": "monthly"}, {"standard_name": "Weekly", "standard_id": "weekly", "tenant_name": "Weekly", "tenant_id": "weekly"}, {"standard_name": "Daily", "standard_id": "daily", "tenant_name": "Daily", "tenant_id": "daily"}]', 23, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(24, 'payday', '发薪日', 'payday', 'String', 13, 0, 0, '发薪时间', '15th / End of Month', NULL, 24, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(25, 'income_range', '收入', 'income_range', 'String', 13, 0, 0, '每月或每周收入区间', '32000 PHP – 64000 PHP', NULL, 25, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(26, 'income_source', '收入来源', 'income_source', 'Enum', 13, 0, 0, '主或副收入来源', 'Primary / Secondary', '[{"standard_name": "Primary", "standard_id": "primary", "tenant_name": "Primary", "tenant_id": "primary"}, {"standard_name": "Secondary", "standard_id": "secondary", "tenant_name": "Secondary", "tenant_id": "secondary"}]', 26, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(27, 'income_proof_files', '收入证明文件', 'income_proof_files', 'FileList', 13, 0, 1, '上传的收入凭证文件', 'payslip.pdf / bank_statement.jpg', NULL, 27, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(28, 'last_app_open_time', '最近打开时间', 'last_app_open_time', 'Datetime', 14, 0, 0, '用户最近一次打开借款App的时间', '2025/10/23 12:02:21', NULL, 28, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(29, 'last_repayment_page_visit_time', '最近访问还款页时间', 'last_repayment_page_visit_time', 'Datetime', 14, 0, 0, '用户最近访问还款页面的时间', '2025/10/26 02:01:51', NULL, 29, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(30, 'total_loan_count', '历史借款总笔数', 'total_loan_count', 'Integer', 14, 0, 0, '统计借款人累计放款次数', '5', NULL, 30, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(31, 'cleared_loan_count', '已结清笔数', 'cleared_loan_count', 'Integer', 14, 0, 0, '已全额还清的贷款订单数量', '3', NULL, 31, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(32, 'overdue_loan_count', '历史逾期笔数', 'overdue_loan_count', 'Integer', 14, 0, 0, '曾经发生逾期的订单数量', '2', NULL, 32, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(33, 'max_overdue_days', '历史最大逾期天数', 'max_overdue_days', 'Integer', 14, 0, 0, '用户历史上最长一次逾期天数', '15', NULL, 33, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(34, 'avg_loan_amount', '平均借款金额', 'avg_loan_amount', 'Decimal', 14, 0, 0, '用户历次贷款的平均放款金额', '1500', NULL, 34, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(35, 'credit_score_001', '001信用评分', 'credit_score_001', 'Enum', 14, 0, 0, '系统1计算的信用等级', 'A', '[{"standard_name": "A", "standard_id": "a", "tenant_name": "A", "tenant_id": "a"}]', 35, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(36, 'credit_score_002', '002信用评分', 'credit_score_002', 'Enum', 14, 0, 0, '系统2计算的信用等级', 'B', '[{"standard_name": "B", "standard_id": "b", "tenant_name": "B", "tenant_id": "b"}]', 36, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(37, 'credit_score_003', '003信用评分', 'credit_score_003', 'Enum', 14, 0, 0, '系统3计算的信用等级', 'C', '[{"standard_name": "C", "standard_id": "c", "tenant_name": "C", "tenant_id": "c"}]', 37, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(38, 'loan_id', '贷款编号', 'loan_id', 'String', 2, 1, 0, '系统生成的唯一标识', '123', NULL, 1, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(39, 'case_status', '案件状态', 'case_status', 'Enum', 2, 0, 0, '当前借款订单状态', '未结清 / 逾期 / 结清', '[{"standard_name": "未结清", "standard_id": "未结清", "tenant_name": "未结清", "tenant_id": "未结清"}, {"standard_name": "逾期", "standard_id": "逾期", "tenant_name": "逾期", "tenant_id": "逾期"}, {"standard_name": "结清", "standard_id": "结清", "tenant_name": "结清", "tenant_id": "结清"}]', 2, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(40, 'product_type', '产品类别', 'product_type', 'String', 2, 0, 0, '区分借款类型（借款 / 展期等）', '借款订单', NULL, 3, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(41, 'disbursement_time', '放款时间', 'disbursement_time', 'Datetime', 2, 0, 0, '实际放款到账时间', '2025/2/26 01:00:00', NULL, 4, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(42, 'total_due_amount', '应还金额', 'total_due_amount', 'Decimal', 2, 0, 0, '合同约定应还总额', '1460', NULL, 5, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(43, 'total_paid_amount', '已还金额', 'total_paid_amount', 'Decimal', 2, 0, 0, '用户已偿还金额', '975', NULL, 6, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(44, 'outstanding_amount', '应还未还金额', 'outstanding_amount', 'Decimal', 2, 0, 0, '当前未偿还金额', '485', NULL, 7, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(45, 'principal_due', '应收本金', 'principal_due', 'Decimal', 2, 0, 0, '合同本金部分', '1000', NULL, 8, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(46, 'interest_due', '应收利息', 'interest_due', 'Decimal', 2, 0, 0, '合同利息部分', '180', NULL, 9, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(47, 'service_fee', '服务费', 'service_fee', 'Decimal', 2, 0, 0, '手续费或管理费', '270', NULL, 10, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(48, 'penalty_fee', '应收罚息', 'penalty_fee', 'Decimal', 2, 0, 0, '逾期罚息', '10', NULL, 11, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(49, 'account_number', '代扣账号', 'account_number', 'String', 2, 0, 0, '用户绑定还款账号', '98546121', NULL, 12, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(50, 'bank_name', '开户行', 'bank_name', 'String', 2, 0, 0, '代扣银行或电子钱包名称', 'GCash', NULL, 13, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(51, 'loan_app', '借款App', 'loan_app', 'String', 2, 0, 0, '借款来源平台', 'MegaPeso', NULL, 14, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(52, 'contract_no', '合同编号', 'contract_no', 'String', 2, 0, 1, '与外部合同匹配的编号', 'MP20250226001', NULL, 15, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(53, 'disbursement_channel', '放款渠道', 'disbursement_channel', 'String', 2, 0, 1, '资金来源渠道', 'Partner Bank', NULL, 16, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(54, 'collection_entry_time', '入催时间', 'collection_entry_time', 'Datetime', 2, 0, 1, '进入催收系统的时间', '2025/3/1 00:00:00', NULL, 17, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(55, 'overdue_days', '当前逾期天数', 'overdue_days', 'Integer', 2, 0, 1, '系统自动计算', '3', NULL, 18, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(56, 'next_collection_time', '下次催收时间', 'next_collection_time', 'Datetime', 2, 0, 1, '系统生成或人工设定', '2025/3/5 09:00:00', NULL, 19, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(57, 'collection_status', '催收状态', 'collection_status', 'Enum', 2, 0, 1, '当前催收进度', '未联系 / 承诺还款 / 已结清', '[{"standard_name": "未联系", "standard_id": "未联系", "tenant_name": "未联系", "tenant_id": "未联系"}, {"standard_name": "承诺还款", "standard_id": "承诺还款", "tenant_name": "承诺还款", "tenant_id": "承诺还款"}, {"standard_name": "已结清", "standard_id": "已结清", "tenant_name": "已结清", "tenant_id": "已结清"}]', 20, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(58, 'collection_stage', '催收阶段', 'collection_stage', 'Enum', 2, 0, 1, '按逾期天数划分', '早期 / 中期 / 后期', '[{"standard_name": "早期", "standard_id": "早期", "tenant_name": "早期", "tenant_id": "早期"}, {"standard_name": "中期", "standard_id": "中期", "tenant_name": "中期", "tenant_id": "中期"}, {"standard_name": "后期", "standard_id": "后期", "tenant_name": "后期", "tenant_id": "后期"}]', 21, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(59, 'repayment_method', '还款方式', 'repayment_method', 'Enum', 2, 0, 1, '当前订单的还款途径', '银行转账 / 钱包 / 门店', '[{"standard_name": "银行转账", "standard_id": "银行转账", "tenant_name": "银行转账", "tenant_id": "银行转账"}, {"standard_name": "钱包", "standard_id": "钱包", "tenant_name": "钱包", "tenant_id": "钱包"}, {"standard_name": "门店", "standard_id": "门店", "tenant_name": "门店", "tenant_id": "门店"}]', 22, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(60, 'last_payment_date', '最近还款日期', 'last_payment_date', 'Datetime', 2, 0, 1, '上一次部分或全部还款时间', '2025/2/28 14:33:22', NULL, 23, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(61, 'loan_id', '贷款编号', 'loan_id', 'String', 3, 1, 0, '系统内唯一的贷款订单编号', '100023', NULL, 1, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(62, 'user_id', '用户编号', 'user_id', 'String', 3, 1, 0, '用户唯一标识', '5983', NULL, 2, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(63, 'user_name', '用户姓名', 'user_name', 'String', 3, 0, 0, '借款人姓名', 'Juan Dela Cruz', NULL, 3, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(64, 'mobile_number', '手机号码', 'mobile_number', 'String', 3, 0, 0, '用户注册手机号', '+63 9123456789', NULL, 4, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(65, 'app_name', 'App名称', 'app_name', 'String', 3, 0, 0, '借款App来源', 'MegaPeso', NULL, 5, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(66, 'product_name', '产品名称', 'product_name', 'String', 3, 0, 0, '所属产品类型', 'Cash Loan', NULL, 6, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(67, 'merchant_name', '贷超商户', 'merchant_name', 'String', 3, 0, 1, '放款商户或渠道方名称', 'EasyLoan Partner', NULL, 7, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(68, 'system_name', '所属系统', 'system_name', 'String', 3, 0, 1, '当前数据所属业务系统', 'CollectionSystemV2', NULL, 8, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(69, 'app_download_url', 'App下载链接', 'app_download_url', 'String', 3, 0, 1, '便于催员发送下载提醒', 'https://play.google.com/...', NULL, 9, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(70, 'collection_type', '首复催类型', 'collection_type', 'Enum', 3, 0, 0, '标记案件是否为首次或再次催收', '首催 / 复催', '[{"standard_name": "首催", "standard_id": "首催", "tenant_name": "首催", "tenant_id": "首催"}, {"standard_name": "复催", "standard_id": "复催", "tenant_name": "复催", "tenant_id": "复催"}]', 10, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(71, 'reborrow_flag', '是否复借', 'reborrow_flag', 'Boolean', 3, 0, 1, '用户是否为老客再次借款', 'TRUE', NULL, 11, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(72, 'auto_reloan', '自动复借', 'auto_reloan', 'Boolean', 3, 0, 1, '是否系统自动生成复借单', 'FALSE', NULL, 12, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(73, 'first_term_days', '首期期限', 'first_term_days', 'Integer', 3, 0, 1, '首次借款天数或期限', '14', NULL, 13, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(74, 'due_date', '应还款日期', 'due_date', 'Date', 3, 0, 0, '应该完成还款的日期', '2025/3/10', NULL, 14, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(75, 'overdue_days', '逾期天数', 'overdue_days', 'Integer', 3, 0, 0, '超过应还日期的天数', '5', NULL, 15, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(76, 'outstanding_amount', '应还未还金额', 'outstanding_amount', 'Decimal', 3, 0, 0, '当前待还金额', '850', NULL, 16, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(77, 'binder', '绑定人', 'binder', 'String', 3, 0, 0, '当前负责该案件的催员或分配人', 'Agent_001', NULL, 17, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(78, 'case_status', '案件状态', 'case_status', 'Enum', 3, 0, 0, '当前催收案件进度', '进行中 / 已结清 / 逾期', '[{"standard_name": "进行中", "standard_id": "进行中", "tenant_name": "进行中", "tenant_id": "进行中"}, {"standard_name": "已结清", "standard_id": "已结清", "tenant_name": "已结清", "tenant_id": "已结清"}, {"standard_name": "逾期", "standard_id": "逾期", "tenant_name": "逾期", "tenant_id": "逾期"}]', 18, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(79, 'settlement_method', '结清方式', 'settlement_method', 'Enum', 3, 0, 0, '最终还款方式', '自动扣款 / 手动转账 / 第三方收款', '[{"standard_name": "自动扣款", "standard_id": "自动扣款", "tenant_name": "自动扣款", "tenant_id": "自动扣款"}, {"standard_name": "手动转账", "standard_id": "手动转账", "tenant_name": "手动转账", "tenant_id": "手动转账"}, {"standard_name": "第三方收款", "standard_id": "第三方收款", "tenant_name": "第三方收款", "tenant_id": "第三方收款"}]', 19, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(80, 'settlement_time', '结清时间', 'settlement_time', 'Datetime', 3, 0, 0, '最后一次结清操作时间', '2025/3/15 09:33:00', NULL, 20, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(81, 'latest_result', '最新结果', 'latest_result', 'Enum', 3, 0, 0, '催收最近一次结果', '承诺还款 / 无法联系 / 已结清', '[{"standard_name": "承诺还款", "standard_id": "承诺还款", "tenant_name": "承诺还款", "tenant_id": "承诺还款"}, {"standard_name": "无法联系", "standard_id": "无法联系", "tenant_name": "无法联系", "tenant_id": "无法联系"}, {"standard_name": "已结清", "standard_id": "已结清", "tenant_name": "已结清", "tenant_id": "已结清"}]', 21, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(82, 'last_collection_time', '最近催收时间', 'last_collection_time', 'Datetime', 3, 0, 0, '最近一次催收操作时间', '2025/3/14 18:00:00', NULL, 22, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(83, 'operator', '处理人', 'operator', 'String', 3, 0, 0, '执行催收操作的员工', 'Agent_Ana', NULL, 23, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(84, 'remark', '备注', 'remark', 'String', 3, 0, 1, '催员填写的备注信息', '电话无人接听', NULL, 24, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(85, 'principal_due', '应还本金', 'principal_due', 'Decimal', 3, 0, 1, '当前订单本金', '1000', NULL, 25, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(86, 'interest_due', '应收利息', 'interest_due', 'Decimal', 3, 0, 0, '_', '150', NULL, 26, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(87, 'installment_no', '期数', 'installment_no', 'Integer', 5, 0, 0, '分期编号', '1', NULL, 1, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(88, 'installment_status', '状态', 'installment_status', 'Enum', 5, 0, 0, '当前期状态', '待还款 / 已还清', '[{"standard_name": "待还款", "standard_id": "待还款", "tenant_name": "待还款", "tenant_id": "待还款"}, {"standard_name": "已还清", "standard_id": "已还清", "tenant_name": "已还清", "tenant_id": "已还清"}]', 2, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(89, 'due_date', '应还款时间', 'due_date', 'Date', 5, 0, 0, '应还日期', '2025/3/13', NULL, 3, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(90, 'overdue_days', '逾期天数', 'overdue_days', 'Integer', 5, 0, 0, '超过应还日期未还的天数', '1', NULL, 4, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(91, 'due_amount', '应还金额', 'due_amount', 'Decimal', 5, 0, 0, '当前期应还总额', '985', NULL, 5, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(92, 'payment_date', '实际还款时间', 'payment_date', 'Datetime', 5, 0, 0, '实际还款完成时间', '2025/3/14 12:23:36', NULL, 6, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(93, 'paid_amount', '实际已还金额', 'paid_amount', 'Decimal', 5, 0, 0, '当前期已还金额', '975', NULL, 7, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(94, 'early_repayment_flag', '提前还款标志', 'early_repayment_flag', 'Boolean', 5, 0, 0, '是否提前还清该期', 'FALSE', NULL, 8, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(95, 'outstanding', '应还未还', 'outstanding', 'Decimal', 5, 0, 0, '当前期未还金额', '10', NULL, 9, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(96, 'principal', '应收本金', 'principal', 'Decimal', 5, 0, 0, '当前期本金', '678', NULL, 10, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(97, 'interest', '应收利息', 'interest', 'Decimal', 5, 0, 0, '当前期利息', '30', NULL, 11, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(98, 'penalty', '应收罚息', 'penalty', 'Decimal', 5, 0, 0, '当前期罚息', '10', NULL, 12, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(99, 'service_fee', '服务费', 'service_fee', 'Decimal', 5, 0, 0, '当前期服务费', '270', NULL, 13, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(100, 'repayment_code', '获取还款码', 'repayment_code', 'Button', 5, 0, 0, '获取二维码或还款链接', '还款码', NULL, 14, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(101, 'waived_amount', '减免金额', 'waived_amount', 'Decimal', 5, 0, 1, '当前期的费用减免', '50', NULL, 15, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(102, 'installment_channel', '分期还款渠道', 'installment_channel', 'String', 5, 0, 1, '实际支付渠道', 'GCash / Maya', NULL, 16, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(103, 'repayment_time', '还款时间', 'repayment_time', 'Datetime', 4, 1, 0, '实际完成还款的时间', '2025/3/14 12:23:36', NULL, 1, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(104, 'repayment_type', '还款类型', 'repayment_type', 'Enum', 4, 0, 0, '表示还款行为的类型', '部分还款 / 全额还款 / 提前还款', '[{"standard_name": "部分还款", "standard_id": "部分还款", "tenant_name": "部分还款", "tenant_id": "部分还款"}, {"standard_name": "全额还款", "standard_id": "全额还款", "tenant_name": "全额还款", "tenant_id": "全额还款"}, {"standard_name": "提前还款", "standard_id": "提前还款", "tenant_name": "提前还款", "tenant_id": "提前还款"}]', 2, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(105, 'repayment_channel', '还款渠道', 'repayment_channel', 'String', 4, 0, 0, '用户使用的支付方式或通道', 'GCash / Maya / Bank Transfer', NULL, 3, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(106, 'repayment_amount', '还款金额', 'repayment_amount', 'Decimal', 4, 1, 0, '本次实际支付金额', '500', NULL, 4, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(107, 'repayment_result', '还款结果', 'repayment_result', 'Enum', 4, 0, 0, '支付网关返回结果', '成功 / 失败 / 处理中', '[{"standard_name": "成功", "standard_id": "成功", "tenant_name": "成功", "tenant_id": "成功"}, {"standard_name": "失败", "standard_id": "失败", "tenant_name": "失败", "tenant_id": "失败"}, {"standard_name": "处理中", "standard_id": "处理中", "tenant_name": "处理中", "tenant_id": "处理中"}]', 5, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(108, 'bill_id', '对应账单ID', 'bill_id', 'String', 4, 0, 1, '系统内账单唯一标识', 'BILL20250314001', NULL, 6, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(109, 'installment_no', '对应期数', 'installment_no', 'Integer', 4, 0, 0, '该笔还款对应的分期编号', '1', NULL, 7, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(110, 'transaction_id', '交易流水号', 'transaction_id', 'String', 4, 0, 1, '第三方支付或银行流水号', 'TXN123456789', NULL, 8, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(111, 'transaction_time', '交易时间', 'transaction_time', 'Datetime', 4, 0, 1, '第三方返回的实际交易完成时间', '2025/3/14 12:23:39', NULL, 9, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(112, 'transaction_fee', '手续费', 'transaction_fee', 'Decimal', 4, 0, 1, '第三方代收通道费用', '5', NULL, 10, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(113, 'received_amount', '实收金额', 'received_amount', 'Decimal', 4, 0, 1, '平台实际到账金额（扣除手续费后）', '495', NULL, 11, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(114, 'waived_amount', '减免金额', 'waived_amount', 'Decimal', 4, 0, 1, '本次还款中被减免的金额', '10', NULL, 12, 1, 0, NOW(), NOW());

INSERT INTO `standard_fields` (`id`, `field_key`, `field_name`, `field_name_en`, `field_type`, `field_group_id`, `is_required`, `is_extended`, `description`, `example_value`, `enum_options`, `sort_order`, `is_active`, `is_deleted`, `created_at`, `updated_at`) VALUES
(115, 'repayment_remark', '还款备注', 'repayment_remark', 'String', 4, 0, 1, '记录异常或说明性信息', '用户要求延后 / 手动修正', NULL, 13, 1, 0, NOW(), NOW());


-- ============================================
-- Mock甲方数据初始化
-- ============================================

-- 创建Mock甲方
INSERT INTO `tenants` (`id`, `tenant_code`, `tenant_name`, `tenant_name_en`, `country_code`, `timezone`, `currency_code`, `is_active`, `created_at`, `updated_at`) VALUES
(1, 'MOCK_TENANT_A', 'Mock甲方A', 'Mock Tenant A', 'PH', 8, 'PHP', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE `tenant_name` = VALUES(`tenant_name`);

-- 为Mock甲方启用所有标准字段
INSERT INTO `tenant_field_configs` (`tenant_id`, `field_id`, `field_type`, `is_enabled`, `is_required`, `is_readonly`, `is_visible`, `sort_order`, `created_at`, `updated_at`)
SELECT 
    1 as `tenant_id`,
    `id` as `field_id`,
    'standard' as `field_type`,
    1 as `is_enabled`,
    `is_required`,
    0 as `is_readonly`,
    1 as `is_visible`,
    `sort_order`,
    NOW() as `created_at`,
    NOW() as `updated_at`
FROM `standard_fields`
WHERE `is_deleted` = 0 AND `is_active` = 1
ON DUPLICATE KEY UPDATE 
    `is_enabled` = VALUES(`is_enabled`),
    `is_required` = VALUES(`is_required`),
    `is_visible` = VALUES(`is_visible`),
    `sort_order` = VALUES(`sort_order`),
    `updated_at` = NOW();


-- ============================================
-- 初始化完成
-- ============================================
