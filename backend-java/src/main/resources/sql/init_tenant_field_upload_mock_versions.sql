-- ================================================================
-- Mock历史版本数据 - 案件列表字段配置版本管理
-- 用于前端版本管理界面展示
-- 执行前请确保 tenant_field_uploads 表已创建
-- ================================================================

-- 清空现有测试数据（可选）
-- DELETE FROM tenant_field_uploads WHERE tenant_id = '1' AND scene = 'list';

-- ================================================================
-- 版本 1 (2025-11-15) - 初始版本
-- ================================================================
INSERT INTO tenant_field_uploads (
  tenant_id, scene, version, file_name, file_size, file_path, 
  fields_count, uploaded_by, uploaded_by_name, uploaded_at, 
  json_content, is_active, version_note
) VALUES (
  '1', 'list', 1, 'case_list_fields_v1.json', 2048, 
  '/uploads/tenant-fields/1/list/v1/fields.json',
  8, 'admin', '系统管理员', '2025-11-15 09:00:00',
  JSON_OBJECT(
    'version', '1.0',
    'scene', 'list',
    'tenant_id', '1',
    'tenant_name', '测试甲方公司',
    'updated_at', '2025-11-15T09:00:00',
    'description', '案件列表字段配置',
    'fields', JSON_ARRAY(
      JSON_OBJECT('field_name', '案件编号', 'field_key', 'case_id', 'field_type', 'String', 'is_required', true, 'sort_order', 1, 'description', '案件唯一标识'),
      JSON_OBJECT('field_name', '案件状态', 'field_key', 'case_status', 'field_type', 'Enum', 'enum_values', JSON_ARRAY('待分配', '催收中', '已完成'), 'is_required', true, 'sort_order', 2, 'description', '案件当前状态'),
      JSON_OBJECT('field_name', '客户姓名', 'field_key', 'customer_name', 'field_type', 'String', 'is_required', true, 'sort_order', 3, 'description', '客户真实姓名'),
      JSON_OBJECT('field_name', '联系电话', 'field_key', 'phone', 'field_type', 'String', 'is_required', true, 'sort_order', 4, 'description', '客户联系电话'),
      JSON_OBJECT('field_name', '借款金额', 'field_key', 'loan_amount', 'field_type', 'Number', 'is_required', true, 'sort_order', 5, 'description', '借款本金（元）'),
      JSON_OBJECT('field_name', '逾期天数', 'field_key', 'overdue_days', 'field_type', 'Integer', 'is_required', false, 'sort_order', 6, 'description', '当前逾期天数'),
      JSON_OBJECT('field_name', '到期日期', 'field_key', 'due_date', 'field_type', 'Date', 'is_required', true, 'sort_order', 7, 'description', '还款到期日期'),
      JSON_OBJECT('field_name', '未还金额', 'field_key', 'outstanding_amount', 'field_type', 'Number', 'is_required', true, 'sort_order', 8, 'description', '剩余未还金额')
    )
  ),
  false,
  '初始版本 - 包含基础8个字段'
) ON DUPLICATE KEY UPDATE uploaded_at = uploaded_at;

-- ================================================================
-- 版本 2 (2025-11-18) - 新增产品和催收员字段
-- ================================================================
INSERT INTO tenant_field_uploads (
  tenant_id, scene, version, file_name, file_size, file_path, 
  fields_count, uploaded_by, uploaded_by_name, uploaded_at, 
  json_content, is_active, version_note
) VALUES (
  '1', 'list', 2, 'case_list_fields_v2.json', 2304, 
  '/uploads/tenant-fields/1/list/v2/fields.json',
  10, 'zhangsan', '张三', '2025-11-18 14:30:00',
  JSON_OBJECT(
    'version', '1.0',
    'scene', 'list',
    'tenant_id', '1',
    'tenant_name', '测试甲方公司',
    'updated_at', '2025-11-18T14:30:00',
    'description', '案件列表字段配置',
    'fields', JSON_ARRAY(
      JSON_OBJECT('field_name', '案件编号', 'field_key', 'case_id', 'field_type', 'String', 'is_required', true, 'sort_order', 1, 'description', '案件唯一标识'),
      JSON_OBJECT('field_name', '案件状态', 'field_key', 'case_status', 'field_type', 'Enum', 'enum_values', JSON_ARRAY('待分配', '催收中', '已完成'), 'is_required', true, 'sort_order', 2, 'description', '案件当前状态'),
      JSON_OBJECT('field_name', '客户姓名', 'field_key', 'customer_name', 'field_type', 'String', 'is_required', true, 'sort_order', 3, 'description', '客户真实姓名'),
      JSON_OBJECT('field_name', '联系电话', 'field_key', 'phone', 'field_type', 'String', 'is_required', true, 'sort_order', 4, 'description', '客户联系电话'),
      JSON_OBJECT('field_name', '借款金额', 'field_key', 'loan_amount', 'field_type', 'Number', 'is_required', true, 'sort_order', 5, 'description', '借款本金（元）'),
      JSON_OBJECT('field_name', '逾期天数', 'field_key', 'overdue_days', 'field_type', 'Integer', 'is_required', false, 'sort_order', 6, 'description', '当前逾期天数'),
      JSON_OBJECT('field_name', '产品名称', 'field_key', 'product_name', 'field_type', 'String', 'is_required', false, 'sort_order', 7, 'description', '借款产品名称'),
      JSON_OBJECT('field_name', '到期日期', 'field_key', 'due_date', 'field_type', 'Date', 'is_required', true, 'sort_order', 8, 'description', '还款到期日期'),
      JSON_OBJECT('field_name', '未还金额', 'field_key', 'outstanding_amount', 'field_type', 'Number', 'is_required', true, 'sort_order', 9, 'description', '剩余未还金额'),
      JSON_OBJECT('field_name', '催收员', 'field_key', 'collector', 'field_type', 'String', 'is_required', false, 'sort_order', 10, 'description', '当前负责催收员')
    )
  ),
  false,
  '新增产品名称和催收员字段'
) ON DUPLICATE KEY UPDATE uploaded_at = uploaded_at;

-- ================================================================
-- 版本 3 (2025-11-22) - 新增APP和商户字段
-- ================================================================
INSERT INTO tenant_field_uploads (
  tenant_id, scene, version, file_name, file_size, file_path, 
  fields_count, uploaded_by, uploaded_by_name, uploaded_at, 
  json_content, is_active, version_note
) VALUES (
  '1', 'list', 3, 'case_list_fields_v3.json', 2560, 
  '/uploads/tenant-fields/1/list/v3/fields.json',
  12, 'lisi', '李四', '2025-11-22 10:15:00',
  JSON_OBJECT(
    'version', '1.0',
    'scene', 'list',
    'tenant_id', '1',
    'tenant_name', '测试甲方公司',
    'updated_at', '2025-11-22T10:15:00',
    'description', '案件列表字段配置',
    'fields', JSON_ARRAY(
      JSON_OBJECT('field_name', '案件编号', 'field_key', 'case_id', 'field_type', 'String', 'is_required', true, 'sort_order', 1, 'description', '案件唯一标识'),
      JSON_OBJECT('field_name', '案件状态', 'field_key', 'case_status', 'field_type', 'Enum', 'enum_values', JSON_ARRAY('待分配', '催收中', '已完成'), 'is_required', true, 'sort_order', 2, 'description', '案件当前状态'),
      JSON_OBJECT('field_name', '客户姓名', 'field_key', 'customer_name', 'field_type', 'String', 'is_required', true, 'sort_order', 3, 'description', '客户真实姓名'),
      JSON_OBJECT('field_name', '联系电话', 'field_key', 'phone', 'field_type', 'String', 'is_required', true, 'sort_order', 4, 'description', '客户联系电话'),
      JSON_OBJECT('field_name', '借款金额', 'field_key', 'loan_amount', 'field_type', 'Number', 'is_required', true, 'sort_order', 5, 'description', '借款本金（元）'),
      JSON_OBJECT('field_name', '逾期天数', 'field_key', 'overdue_days', 'field_type', 'Integer', 'is_required', false, 'sort_order', 6, 'description', '当前逾期天数'),
      JSON_OBJECT('field_name', '产品名称', 'field_key', 'product_name', 'field_type', 'String', 'is_required', false, 'sort_order', 7, 'description', '借款产品名称'),
      JSON_OBJECT('field_name', '到期日期', 'field_key', 'due_date', 'field_type', 'Date', 'is_required', true, 'sort_order', 8, 'description', '还款到期日期'),
      JSON_OBJECT('field_name', '未还金额', 'field_key', 'outstanding_amount', 'field_type', 'Number', 'is_required', true, 'sort_order', 9, 'description', '剩余未还金额'),
      JSON_OBJECT('field_name', '催收员', 'field_key', 'collector', 'field_type', 'String', 'is_required', false, 'sort_order', 10, 'description', '当前负责催收员'),
      JSON_OBJECT('field_name', 'APP名称', 'field_key', 'app_name', 'field_type', 'String', 'is_required', false, 'sort_order', 11, 'description', '借款APP应用名称'),
      JSON_OBJECT('field_name', '商户名称', 'field_key', 'merchant_name', 'field_type', 'String', 'is_required', false, 'sort_order', 12, 'description', '关联商户名称')
    )
  ),
  false,
  '新增APP名称和商户名称字段，满足多平台管理需求'
) ON DUPLICATE KEY UPDATE uploaded_at = uploaded_at;

-- ================================================================
-- 版本 4 (2025-11-25) - 调整字段顺序，优化展示
-- ================================================================
INSERT INTO tenant_field_uploads (
  tenant_id, scene, version, file_name, file_size, file_path, 
  fields_count, uploaded_by, uploaded_by_name, uploaded_at, 
  json_content, is_active, version_note
) VALUES (
  '1', 'list', 4, 'case_list_fields_v4.json', 2560, 
  '/uploads/tenant-fields/1/list/v4/fields.json',
  12, 'admin', '系统管理员', '2025-11-25 16:45:00',
  JSON_OBJECT(
    'version', '1.0',
    'scene', 'list',
    'tenant_id', '1',
    'tenant_name', '测试甲方公司',
    'updated_at', '2025-11-25T16:45:00',
    'description', '案件列表字段配置',
    'fields', JSON_ARRAY(
      JSON_OBJECT('field_name', '案件编号', 'field_key', 'case_id', 'field_type', 'String', 'is_required', true, 'sort_order', 1, 'description', '案件唯一标识'),
      JSON_OBJECT('field_name', '案件状态', 'field_key', 'case_status', 'field_type', 'Enum', 'enum_values', JSON_ARRAY('待分配', '催收中', '已完成'), 'is_required', true, 'sort_order', 2, 'description', '案件当前状态'),
      JSON_OBJECT('field_name', '逾期天数', 'field_key', 'overdue_days', 'field_type', 'Integer', 'is_required', false, 'sort_order', 3, 'description', '当前逾期天数'),
      JSON_OBJECT('field_name', '借款金额', 'field_key', 'loan_amount', 'field_type', 'Number', 'is_required', true, 'sort_order', 4, 'description', '借款本金（元）'),
      JSON_OBJECT('field_name', '未还金额', 'field_key', 'outstanding_amount', 'field_type', 'Number', 'is_required', true, 'sort_order', 5, 'description', '剩余未还金额'),
      JSON_OBJECT('field_name', '客户姓名', 'field_key', 'customer_name', 'field_type', 'String', 'is_required', true, 'sort_order', 6, 'description', '客户真实姓名'),
      JSON_OBJECT('field_name', '联系电话', 'field_key', 'phone', 'field_type', 'String', 'is_required', true, 'sort_order', 7, 'description', '客户联系电话'),
      JSON_OBJECT('field_name', '产品名称', 'field_key', 'product_name', 'field_type', 'String', 'is_required', false, 'sort_order', 8, 'description', '借款产品名称'),
      JSON_OBJECT('field_name', 'APP名称', 'field_key', 'app_name', 'field_type', 'String', 'is_required', false, 'sort_order', 9, 'description', '借款APP应用名称'),
      JSON_OBJECT('field_name', '商户名称', 'field_key', 'merchant_name', 'field_type', 'String', 'is_required', false, 'sort_order', 10, 'description', '关联商户名称'),
      JSON_OBJECT('field_name', '到期日期', 'field_key', 'due_date', 'field_type', 'Date', 'is_required', true, 'sort_order', 11, 'description', '还款到期日期'),
      JSON_OBJECT('field_name', '催收员', 'field_key', 'collector', 'field_type', 'String', 'is_required', false, 'sort_order', 12, 'description', '当前负责催收员')
    )
  ),
  false,
  '调整字段显示顺序，将重要金额和逾期信息前置'
) ON DUPLICATE KEY UPDATE uploaded_at = uploaded_at;

-- ================================================================
-- 版本 5 (2025-11-28) - 新增案件关闭状态
-- ================================================================
INSERT INTO tenant_field_uploads (
  tenant_id, scene, version, file_name, file_size, file_path, 
  fields_count, uploaded_by, uploaded_by_name, uploaded_at, 
  json_content, is_active, version_note
) VALUES (
  '1', 'list', 5, 'case_list_fields_v5.json', 2688, 
  '/uploads/tenant-fields/1/list/v5/fields.json',
  12, 'wangwu', '王五', '2025-11-28 11:20:00',
  JSON_OBJECT(
    'version', '1.0',
    'scene', 'list',
    'tenant_id', '1',
    'tenant_name', '测试甲方公司',
    'updated_at', '2025-11-28T11:20:00',
    'description', '案件列表字段配置',
    'fields', JSON_ARRAY(
      JSON_OBJECT('field_name', '案件编号', 'field_key', 'case_id', 'field_type', 'String', 'is_required', true, 'sort_order', 1, 'description', '案件唯一标识'),
      JSON_OBJECT('field_name', '案件状态', 'field_key', 'case_status', 'field_type', 'Enum', 'enum_values', JSON_ARRAY('待分配', '催收中', '已完成', '已关闭'), 'is_required', true, 'sort_order', 2, 'description', '案件当前状态'),
      JSON_OBJECT('field_name', '逾期天数', 'field_key', 'overdue_days', 'field_type', 'Integer', 'is_required', false, 'sort_order', 3, 'description', '当前逾期天数'),
      JSON_OBJECT('field_name', '借款金额', 'field_key', 'loan_amount', 'field_type', 'Number', 'is_required', true, 'sort_order', 4, 'description', '借款本金（元）'),
      JSON_OBJECT('field_name', '未还金额', 'field_key', 'outstanding_amount', 'field_type', 'Number', 'is_required', true, 'sort_order', 5, 'description', '剩余未还金额'),
      JSON_OBJECT('field_name', '客户姓名', 'field_key', 'customer_name', 'field_type', 'String', 'is_required', true, 'sort_order', 6, 'description', '客户真实姓名'),
      JSON_OBJECT('field_name', '联系电话', 'field_key', 'phone', 'field_type', 'String', 'is_required', true, 'sort_order', 7, 'description', '客户联系电话'),
      JSON_OBJECT('field_name', '产品名称', 'field_key', 'product_name', 'field_type', 'String', 'is_required', false, 'sort_order', 8, 'description', '借款产品名称'),
      JSON_OBJECT('field_name', 'APP名称', 'field_key', 'app_name', 'field_type', 'String', 'is_required', false, 'sort_order', 9, 'description', '借款APP应用名称'),
      JSON_OBJECT('field_name', '商户名称', 'field_key', 'merchant_name', 'field_type', 'String', 'is_required', false, 'sort_order', 10, 'description', '关联商户名称'),
      JSON_OBJECT('field_name', '到期日期', 'field_key', 'due_date', 'field_type', 'Date', 'is_required', true, 'sort_order', 11, 'description', '还款到期日期'),
      JSON_OBJECT('field_name', '催收员', 'field_key', 'collector', 'field_type', 'String', 'is_required', false, 'sort_order', 12, 'description', '当前负责催收员')
    )
  ),
  false,
  '案件状态枚举中新增"已关闭"选项，支持案件关闭流程'
) ON DUPLICATE KEY UPDATE uploaded_at = uploaded_at;

-- ================================================================
-- 版本 6 (2025-12-02) - 新增借款人信息字段
-- ================================================================
INSERT INTO tenant_field_uploads (
  tenant_id, scene, version, file_name, file_size, file_path, 
  fields_count, uploaded_by, uploaded_by_name, uploaded_at, 
  json_content, is_active, version_note
) VALUES (
  '1', 'list', 6, 'case_list_fields_v6.json', 2944, 
  '/uploads/tenant-fields/1/list/v6/fields.json',
  14, 'zhangsan', '张三', '2025-12-02 09:30:00',
  JSON_OBJECT(
    'version', '1.0',
    'scene', 'list',
    'tenant_id', '1',
    'tenant_name', '测试甲方公司',
    'updated_at', '2025-12-02T09:30:00',
    'description', '案件列表字段配置',
    'fields', JSON_ARRAY(
      JSON_OBJECT('field_name', '案件编号', 'field_key', 'case_id', 'field_type', 'String', 'is_required', true, 'sort_order', 1, 'description', '案件唯一标识'),
      JSON_OBJECT('field_name', '案件状态', 'field_key', 'case_status', 'field_type', 'Enum', 'enum_values', JSON_ARRAY('待分配', '催收中', '已完成', '已关闭'), 'is_required', true, 'sort_order', 2, 'description', '案件当前状态'),
      JSON_OBJECT('field_name', '逾期天数', 'field_key', 'overdue_days', 'field_type', 'Integer', 'is_required', false, 'sort_order', 3, 'description', '当前逾期天数'),
      JSON_OBJECT('field_name', '借款金额', 'field_key', 'loan_amount', 'field_type', 'Number', 'is_required', true, 'sort_order', 4, 'description', '借款本金（元）'),
      JSON_OBJECT('field_name', '未还金额', 'field_key', 'outstanding_amount', 'field_type', 'Number', 'is_required', true, 'sort_order', 5, 'description', '剩余未还金额'),
      JSON_OBJECT('field_name', '借款人姓名', 'field_key', 'borrower_name', 'field_type', 'String', 'is_required', true, 'sort_order', 6, 'description', '借款人真实姓名'),
      JSON_OBJECT('field_name', '借款人手机', 'field_key', 'borrower_phone', 'field_type', 'String', 'is_required', true, 'sort_order', 7, 'description', '借款人联系手机'),
      JSON_OBJECT('field_name', '客户姓名', 'field_key', 'customer_name', 'field_type', 'String', 'is_required', true, 'sort_order', 8, 'description', '客户真实姓名'),
      JSON_OBJECT('field_name', '联系电话', 'field_key', 'phone', 'field_type', 'String', 'is_required', true, 'sort_order', 9, 'description', '客户联系电话'),
      JSON_OBJECT('field_name', '产品名称', 'field_key', 'product_name', 'field_type', 'String', 'is_required', false, 'sort_order', 10, 'description', '借款产品名称'),
      JSON_OBJECT('field_name', 'APP名称', 'field_key', 'app_name', 'field_type', 'String', 'is_required', false, 'sort_order', 11, 'description', '借款APP应用名称'),
      JSON_OBJECT('field_name', '商户名称', 'field_key', 'merchant_name', 'field_type', 'String', 'is_required', false, 'sort_order', 12, 'description', '关联商户名称'),
      JSON_OBJECT('field_name', '到期日期', 'field_key', 'due_date', 'field_type', 'Date', 'is_required', true, 'sort_order', 13, 'description', '还款到期日期'),
      JSON_OBJECT('field_name', '催收员', 'field_key', 'collector', 'field_type', 'String', 'is_required', false, 'sort_order', 14, 'description', '当前负责催收员')
    )
  ),
  false,
  '新增借款人姓名和借款人手机字段，区分借款人与客户信息'
) ON DUPLICATE KEY UPDATE uploaded_at = uploaded_at;

-- ================================================================
-- 版本 7 (2025-12-05) - 新增风险等级字段
-- ================================================================
INSERT INTO tenant_field_uploads (
  tenant_id, scene, version, file_name, file_size, file_path, 
  fields_count, uploaded_by, uploaded_by_name, uploaded_at, 
  json_content, is_active, version_note
) VALUES (
  '1', 'list', 7, 'case_list_fields_v7.json', 3072, 
  '/uploads/tenant-fields/1/list/v7/fields.json',
  15, 'lisi', '李四', '2025-12-05 14:00:00',
  JSON_OBJECT(
    'version', '1.0',
    'scene', 'list',
    'tenant_id', '1',
    'tenant_name', '测试甲方公司',
    'updated_at', '2025-12-05T14:00:00',
    'description', '案件列表字段配置',
    'fields', JSON_ARRAY(
      JSON_OBJECT('field_name', '案件编号', 'field_key', 'case_id', 'field_type', 'String', 'is_required', true, 'sort_order', 1, 'description', '案件唯一标识'),
      JSON_OBJECT('field_name', '案件状态', 'field_key', 'case_status', 'field_type', 'Enum', 'enum_values', JSON_ARRAY('待分配', '催收中', '已完成', '已关闭'), 'is_required', true, 'sort_order', 2, 'description', '案件当前状态'),
      JSON_OBJECT('field_name', '风险等级', 'field_key', 'risk_level', 'field_type', 'Enum', 'enum_values', JSON_ARRAY('低风险', '中风险', '高风险'), 'is_required', false, 'sort_order', 3, 'description', '案件风险评级'),
      JSON_OBJECT('field_name', '逾期天数', 'field_key', 'overdue_days', 'field_type', 'Integer', 'is_required', false, 'sort_order', 4, 'description', '当前逾期天数'),
      JSON_OBJECT('field_name', '借款金额', 'field_key', 'loan_amount', 'field_type', 'Number', 'is_required', true, 'sort_order', 5, 'description', '借款本金，单位：元'),
      JSON_OBJECT('field_name', '未还金额', 'field_key', 'outstanding_amount', 'field_type', 'Number', 'is_required', true, 'sort_order', 6, 'description', '剩余未还金额'),
      JSON_OBJECT('field_name', '借款人姓名', 'field_key', 'borrower_name', 'field_type', 'String', 'is_required', true, 'sort_order', 7, 'description', '借款人真实姓名'),
      JSON_OBJECT('field_name', '借款人手机', 'field_key', 'borrower_phone', 'field_type', 'String', 'is_required', true, 'sort_order', 8, 'description', '借款人联系手机'),
      JSON_OBJECT('field_name', '客户姓名', 'field_key', 'customer_name', 'field_type', 'String', 'is_required', true, 'sort_order', 9, 'description', '客户真实姓名'),
      JSON_OBJECT('field_name', '联系电话', 'field_key', 'phone', 'field_type', 'String', 'is_required', true, 'sort_order', 10, 'description', '客户联系电话'),
      JSON_OBJECT('field_name', '产品名称', 'field_key', 'product_name', 'field_type', 'String', 'is_required', false, 'sort_order', 11, 'description', '借款产品名称'),
      JSON_OBJECT('field_name', 'APP名称', 'field_key', 'app_name', 'field_type', 'String', 'is_required', false, 'sort_order', 12, 'description', '借款APP应用名称'),
      JSON_OBJECT('field_name', '商户名称', 'field_key', 'merchant_name', 'field_type', 'String', 'is_required', false, 'sort_order', 13, 'description', '关联商户名称'),
      JSON_OBJECT('field_name', '到期日期', 'field_key', 'due_date', 'field_type', 'Date', 'is_required', true, 'sort_order', 14, 'description', '还款到期日期'),
      JSON_OBJECT('field_name', '催收员', 'field_key', 'collector', 'field_type', 'String', 'is_required', false, 'sort_order', 15, 'description', '当前负责催收员')
    )
  ),
  true,
  '新增风险等级字段，支持案件风险分级管理（当前使用版本）'
) ON DUPLICATE KEY UPDATE uploaded_at = uploaded_at;

-- ================================================================
-- 数据验证
-- ================================================================
SELECT 
  version AS '版本号',
  fields_count AS '字段数',
  uploaded_by_name AS '上传人',
  uploaded_at AS '上传时间',
  is_active AS '是否激活',
  version_note AS '版本说明'
FROM tenant_field_uploads
WHERE tenant_id = '1' AND scene = 'list'
ORDER BY version DESC;
