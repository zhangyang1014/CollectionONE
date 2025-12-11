-- 插入还款渠道测试数据
-- 创建日期：2025-11-25
-- 说明：为甲方1插入测试还款渠道数据

-- 清空现有测试数据（可选）
-- DELETE FROM payment_channels WHERE party_id = 1;

-- 插入测试数据
INSERT INTO `payment_channels` (
  `party_id`, `channel_name`, `channel_icon`, `channel_type`, `service_provider`, 
  `description`, `api_url`, `api_method`, `auth_type`, `auth_config`, `request_params`,
  `is_enabled`, `sort_order`, `created_by`
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
),
(
  1,
  'Maya',
  'https://example.com/icons/maya.png',
  'VA',
  'Xendit',
  '菲律宾Maya电子钱包',
  'https://api.example.com/payment/maya/create',
  'POST',
  'API_KEY',
  '{"api_key": "test_api_key_encrypted"}',
  '{"loan_id": "{loan_id}", "case_id": "{case_id}", "amount": "{amount}", "customer_name": "{customer_name}"}',
  1,
  5,
  1
);
























