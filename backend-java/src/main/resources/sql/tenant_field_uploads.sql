-- 甲方字段上传记录表
CREATE TABLE IF NOT EXISTS tenant_field_uploads (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  tenant_id VARCHAR(64) NOT NULL COMMENT '甲方ID',
  scene VARCHAR(20) NOT NULL DEFAULT 'list' COMMENT '场景：list-案件列表, detail-案件详情',
  version INT NOT NULL COMMENT '版本号，按甲方+场景自增',
  file_name VARCHAR(255) NOT NULL COMMENT '原始文件名',
  file_size INT NOT NULL COMMENT '文件大小（字节）',
  file_path VARCHAR(512) NOT NULL COMMENT '存储路径',
  fields_count INT NOT NULL COMMENT '字段数量',
  uploaded_by VARCHAR(64) NOT NULL COMMENT '上传人ID',
  uploaded_by_name VARCHAR(100) COMMENT '上传人姓名',
  uploaded_at DATETIME NOT NULL COMMENT '上传时间',
  json_content JSON NOT NULL COMMENT 'JSON文件内容',
  is_active BOOLEAN DEFAULT FALSE COMMENT '是否为当前生效版本',
  version_note VARCHAR(500) COMMENT '版本说明（可选）',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  
  INDEX idx_tenant_scene (tenant_id, scene),
  INDEX idx_tenant_scene_version (tenant_id, scene, version),
  INDEX idx_tenant_scene_active (tenant_id, scene, is_active),
  INDEX idx_uploaded_at (uploaded_at),
  
  UNIQUE KEY uk_tenant_scene_version (tenant_id, scene, version)
) COMMENT '甲方字段上传记录表';

-- 插入测试数据（可选）
-- 甲方1的案件列表字段配置 - 版本1
INSERT INTO tenant_field_uploads (
  tenant_id, scene, version, file_name, file_size, file_path, 
  fields_count, uploaded_by, uploaded_by_name, uploaded_at, 
  json_content, is_active, version_note
) VALUES (
  '1', 'list', 1, 'fields_v1.json', 2048, 
  '/uploads/tenant-fields/1/list/v1/fields.json',
  10, 'admin', '管理员', '2025-12-01 09:00:00',
  JSON_OBJECT(
    'version', '1.0',
    'scene', 'list',
    'tenant_id', '1',
    'updated_at', '2025-12-01T09:00:00Z',
    'fields', JSON_ARRAY(
      JSON_OBJECT('field_name', '案件编号', 'field_key', 'case_id', 'field_type', 'String', 'is_required', true, 'sort_order', 1),
      JSON_OBJECT('field_name', '案件状态', 'field_key', 'case_status', 'field_type', 'Enum', 'enum_values', JSON_ARRAY('待分配', '催收中', '已完成'), 'is_required', true, 'sort_order', 2),
      JSON_OBJECT('field_name', '借款金额', 'field_key', 'loan_amount', 'field_type', 'Number', 'is_required', true, 'sort_order', 3),
      JSON_OBJECT('field_name', '逾期天数', 'field_key', 'overdue_days', 'field_type', 'Integer', 'is_required', false, 'sort_order', 4),
      JSON_OBJECT('field_name', '客户姓名', 'field_key', 'customer_name', 'field_type', 'String', 'is_required', true, 'sort_order', 5),
      JSON_OBJECT('field_name', '联系电话', 'field_key', 'phone', 'field_type', 'String', 'is_required', true, 'sort_order', 6),
      JSON_OBJECT('field_name', '产品名称', 'field_key', 'product_name', 'field_type', 'String', 'is_required', false, 'sort_order', 7),
      JSON_OBJECT('field_name', '到期日期', 'field_key', 'due_date', 'field_type', 'Date', 'is_required', true, 'sort_order', 8),
      JSON_OBJECT('field_name', '未还金额', 'field_key', 'outstanding_amount', 'field_type', 'Number', 'is_required', true, 'sort_order', 9),
      JSON_OBJECT('field_name', '催收员', 'field_key', 'collector', 'field_type', 'String', 'is_required', false, 'sort_order', 10)
    )
  ),
  false,
  '初始版本'
);

-- 甲方1的案件列表字段配置 - 版本2
INSERT INTO tenant_field_uploads (
  tenant_id, scene, version, file_name, file_size, file_path, 
  fields_count, uploaded_by, uploaded_by_name, uploaded_at, 
  json_content, is_active, version_note
) VALUES (
  '1', 'list', 2, 'fields_v2.json', 2560, 
  '/uploads/tenant-fields/1/list/v2/fields.json',
  12, 'admin', '管理员', '2025-12-05 10:20:00',
  JSON_OBJECT(
    'version', '1.0',
    'scene', 'list',
    'tenant_id', '1',
    'updated_at', '2025-12-05T10:20:00Z',
    'fields', JSON_ARRAY(
      JSON_OBJECT('field_name', '案件编号', 'field_key', 'case_id', 'field_type', 'String', 'is_required', true, 'sort_order', 1),
      JSON_OBJECT('field_name', '案件状态', 'field_key', 'case_status', 'field_type', 'Enum', 'enum_values', JSON_ARRAY('待分配', '催收中', '已完成'), 'is_required', true, 'sort_order', 2),
      JSON_OBJECT('field_name', '借款金额', 'field_key', 'loan_amount', 'field_type', 'Number', 'is_required', true, 'sort_order', 3, 'description', '借款本金'),
      JSON_OBJECT('field_name', '客户姓名', 'field_key', 'customer_name', 'field_type', 'String', 'is_required', true, 'sort_order', 5),
      JSON_OBJECT('field_name', '联系电话', 'field_key', 'phone', 'field_type', 'String', 'is_required', true, 'sort_order', 6),
      JSON_OBJECT('field_name', '逾期天数', 'field_key', 'overdue_days', 'field_type', 'Integer', 'is_required', false, 'sort_order', 7),
      JSON_OBJECT('field_name', '产品名称', 'field_key', 'product_name', 'field_type', 'String', 'is_required', false, 'sort_order', 8),
      JSON_OBJECT('field_name', '到期日期', 'field_key', 'due_date', 'field_type', 'Date', 'is_required', true, 'sort_order', 9),
      JSON_OBJECT('field_name', '未还金额', 'field_key', 'outstanding_amount', 'field_type', 'Number', 'is_required', true, 'sort_order', 10),
      JSON_OBJECT('field_name', '催收员', 'field_key', 'collector', 'field_type', 'String', 'is_required', false, 'sort_order', 11),
      JSON_OBJECT('field_name', 'APP名称', 'field_key', 'app_name', 'field_type', 'String', 'is_required', false, 'sort_order', 12),
      JSON_OBJECT('field_name', '商户名称', 'field_key', 'merchant_name', 'field_type', 'String', 'is_required', false, 'sort_order', 13)
    )
  ),
  false,
  '调整字段顺序，新增APP名称和商户名称'
);

-- 甲方1的案件列表字段配置 - 版本3（当前使用）
INSERT INTO tenant_field_uploads (
  tenant_id, scene, version, file_name, file_size, file_path, 
  fields_count, uploaded_by, uploaded_by_name, uploaded_at, 
  json_content, is_active, version_note
) VALUES (
  '1', 'list', 3, 'fields_v3.json', 3072, 
  '/uploads/tenant-fields/1/list/v3/fields.json',
  14, 'admin', '管理员', '2025-12-07 15:30:00',
  JSON_OBJECT(
    'version', '1.0',
    'scene', 'list',
    'tenant_id', '1',
    'updated_at', '2025-12-07T15:30:00Z',
    'fields', JSON_ARRAY(
      JSON_OBJECT('field_name', '案件编号', 'field_key', 'case_id', 'field_type', 'String', 'is_required', true, 'sort_order', 1),
      JSON_OBJECT('field_name', '案件状态', 'field_key', 'case_status', 'field_type', 'Enum', 'enum_values', JSON_ARRAY('待分配', '催收中', '已完成', '已关闭'), 'is_required', true, 'sort_order', 2),
      JSON_OBJECT('field_name', '借款金额', 'field_key', 'loan_amount', 'field_type', 'Number', 'is_required', true, 'sort_order', 3, 'description', '借款本金，单位：元'),
      JSON_OBJECT('field_name', '借款人姓名', 'field_key', 'borrower_name', 'field_type', 'String', 'is_required', true, 'sort_order', 4),
      JSON_OBJECT('field_name', '客户姓名', 'field_key', 'customer_name', 'field_type', 'String', 'is_required', true, 'sort_order', 5),
      JSON_OBJECT('field_name', '借款人手机', 'field_key', 'borrower_phone', 'field_type', 'String', 'is_required', true, 'sort_order', 6),
      JSON_OBJECT('field_name', '联系电话', 'field_key', 'phone', 'field_type', 'String', 'is_required', true, 'sort_order', 7),
      JSON_OBJECT('field_name', '逾期天数', 'field_key', 'overdue_days', 'field_type', 'Integer', 'is_required', false, 'sort_order', 8),
      JSON_OBJECT('field_name', '产品名称', 'field_key', 'product_name', 'field_type', 'String', 'is_required', false, 'sort_order', 9),
      JSON_OBJECT('field_name', '到期日期', 'field_key', 'due_date', 'field_type', 'Date', 'is_required', true, 'sort_order', 10),
      JSON_OBJECT('field_name', '未还金额', 'field_key', 'outstanding_amount', 'field_type', 'Number', 'is_required', true, 'sort_order', 11),
      JSON_OBJECT('field_name', '催收员', 'field_key', 'collector', 'field_type', 'String', 'is_required', false, 'sort_order', 12),
      JSON_OBJECT('field_name', 'APP名称', 'field_key', 'app_name', 'field_type', 'String', 'is_required', false, 'sort_order', 13),
      JSON_OBJECT('field_name', '商户名称', 'field_key', 'merchant_name', 'field_type', 'String', 'is_required', false, 'sort_order', 14)
    )
  ),
  true,
  '新增借款人姓名和借款人手机字段，案件状态新增已关闭选项'
);



