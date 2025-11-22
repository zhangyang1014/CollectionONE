-- 甲方还款渠道配置管理 - 数据库迁移脚本
-- 创建日期：2025-11-21
-- 说明：创建还款渠道配置表和还款码记录表

-- 1. 创建还款渠道配置表
CREATE TABLE IF NOT EXISTS payment_channels (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  party_id BIGINT NOT NULL COMMENT '甲方ID',
  channel_name VARCHAR(100) NOT NULL COMMENT '支付名称',
  channel_icon VARCHAR(500) COMMENT '图标URL',
  channel_type ENUM('VA', 'H5', 'QR') NOT NULL COMMENT '支付类型：VA-虚拟账户，H5-H5链接，QR-二维码',
  service_provider VARCHAR(100) COMMENT '服务公司',
  description TEXT COMMENT '渠道描述',
  api_url VARCHAR(500) NOT NULL COMMENT 'API地址',
  api_method ENUM('GET', 'POST') DEFAULT 'POST' COMMENT '请求方法',
  auth_type ENUM('API_KEY', 'BEARER', 'BASIC') NOT NULL COMMENT '认证方式',
  auth_config JSON COMMENT '认证配置（加密存储）',
  request_params JSON COMMENT '接口入参模板',
  is_enabled TINYINT(1) DEFAULT 1 COMMENT '是否启用：1-启用，0-禁用',
  sort_order INT DEFAULT 0 COMMENT '排序权重，越小越靠前',
  created_by BIGINT COMMENT '创建人ID',
  updated_by BIGINT COMMENT '更新人ID',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_party_enabled (party_id, is_enabled, sort_order),
  INDEX idx_party_id (party_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='还款渠道配置表';

-- 2. 创建还款码记录表
CREATE TABLE IF NOT EXISTS payment_codes (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  code_no VARCHAR(100) UNIQUE NOT NULL COMMENT '还款码编号',
  party_id BIGINT NOT NULL COMMENT '甲方ID',
  channel_id BIGINT NOT NULL COMMENT '渠道ID',
  case_id BIGINT NOT NULL COMMENT '案件ID',
  loan_id BIGINT NOT NULL COMMENT '借款ID',
  customer_id BIGINT NOT NULL COMMENT '客户ID',
  collector_id BIGINT NOT NULL COMMENT '催员ID',
  installment_number INT COMMENT '期数',
  amount DECIMAL(15,2) NOT NULL COMMENT '还款金额',
  currency VARCHAR(10) DEFAULT 'IDR' COMMENT '币种',
  payment_type ENUM('VA', 'H5', 'QR') NOT NULL COMMENT '支付类型：VA-虚拟账户，H5-H5链接，QR-二维码',
  payment_code VARCHAR(500) COMMENT '支付码内容（VA码/链接地址）',
  qr_image_url VARCHAR(500) COMMENT '二维码图片URL',
  status ENUM('PENDING', 'PAID', 'EXPIRED') DEFAULT 'PENDING' COMMENT '状态：PENDING-待支付，PAID-已支付，EXPIRED-已过期',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  expired_at TIMESTAMP COMMENT '过期时间（由第三方接口返回）',
  paid_at TIMESTAMP COMMENT '支付时间',
  third_party_order_id VARCHAR(200) COMMENT '第三方订单ID',
  third_party_response JSON COMMENT '第三方接口完整返回',
  request_params JSON COMMENT '请求参数快照',
  INDEX idx_case_loan (case_id, loan_id),
  INDEX idx_collector_time (collector_id, created_at),
  INDEX idx_status_expired (status, expired_at),
  INDEX idx_code_no (code_no),
  INDEX idx_third_party_order (third_party_order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='还款码记录表';

-- 3. 插入测试数据（可选，用于开发测试）

-- 为甲方A插入几个测试渠道
INSERT INTO payment_channels (
  party_id, channel_name, channel_icon, channel_type, service_provider, 
  description, api_url, api_method, auth_type, auth_config, request_params,
  is_enabled, sort_order, created_by
) VALUES 
(
  1, 
  'GCash', 
  'https://example.com/icons/gcash.png',
  'VA',
  'Xendit',
  '菲律宾最流行的电子钱包支付方式',
  'https://api.example.com/payment/gcash/create',
  'POST',
  'API_KEY',
  '{"api_key": "test_api_key_encrypted"}',
  '{"loan_id": "{loan_id}", "case_id": "{case_id}", "installment_number": "{installment_number}", "amount": "{amount}", "customer_name": "{customer_name}", "customer_phone": "{customer_phone}"}',
  1,
  1,
  1
),
(
  1,
  'BCA Virtual Account',
  'https://example.com/icons/bca.png',
  'VA',
  'Midtrans',
  '印尼BCA银行虚拟账户',
  'https://api.example.com/payment/bca-va/create',
  'POST',
  'BEARER',
  '{"token": "test_bearer_token_encrypted"}',
  '{"loan_id": "{loan_id}", "case_id": "{case_id}", "installment_number": "{installment_number}", "amount": "{amount}", "customer_id": "{customer_id}"}',
  1,
  2,
  1
),
(
  1,
  'OXXO Pay',
  'https://example.com/icons/oxxo.png',
  'H5',
  'Conekta',
  '墨西哥便利店现金支付',
  'https://api.example.com/payment/oxxo/create',
  'POST',
  'API_KEY',
  '{"api_key": "test_api_key_encrypted"}',
  '{"loan_id": "{loan_id}", "amount": "{amount}", "customer_name": "{customer_name}"}',
  1,
  3,
  1
),
(
  1,
  'QRIS',
  'https://example.com/icons/qris.png',
  'QR',
  'Xendit',
  '印尼统一二维码支付',
  'https://api.example.com/payment/qris/create',
  'POST',
  'API_KEY',
  '{"api_key": "test_api_key_encrypted"}',
  '{"loan_id": "{loan_id}", "case_id": "{case_id}", "amount": "{amount}"}',
  1,
  4,
  1
);

-- 4. 添加权限点（如果有权限表）
-- INSERT INTO permissions (code, name, description, module) VALUES
-- ('payment_channel:view', '查看还款渠道', '查看还款渠道配置列表', 'payment'),
-- ('payment_channel:add', '新增还款渠道', '新增还款渠道配置', 'payment'),
-- ('payment_channel:edit', '编辑还款渠道', '编辑还款渠道配置', 'payment'),
-- ('payment_channel:delete', '删除还款渠道', '删除还款渠道配置', 'payment'),
-- ('payment_channel:toggle', '启用禁用渠道', '启用或禁用还款渠道', 'payment'),
-- ('payment_code:request', '请求还款码', '为案件请求还款码', 'payment'),
-- ('payment_code:view', '查看还款码', '查看还款码记录', 'payment');

