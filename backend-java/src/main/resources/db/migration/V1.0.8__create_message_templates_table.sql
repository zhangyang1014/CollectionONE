-- 创建消息模板配置表
CREATE TABLE IF NOT EXISTS message_templates (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  tenant_id BIGINT NOT NULL COMMENT '甲方ID',
  
  -- 基础信息
  template_name VARCHAR(200) NOT NULL COMMENT '模板名称',
  template_type ENUM('organization', 'personal') NOT NULL COMMENT '模板类型：organization-组织模板，personal-个人模板',
  agency_ids JSON COMMENT '适用机构ID列表，如[1,2,3]，NULL表示全部机构',
  
  -- 分类维度（对应IM端筛选器）
  case_stage VARCHAR(20) NOT NULL COMMENT '案件阶段：C/S0/S1-3/S3+',
  scene VARCHAR(50) NOT NULL COMMENT '场景：greeting/reminder/strong',
  time_slot VARCHAR(20) NOT NULL COMMENT '时间点：morning/afternoon/evening',
  
  -- 模板内容
  content TEXT NOT NULL COMMENT '模板内容，支持变量占位符',
  variables JSON COMMENT '可用变量列表，如["客户名","贷款编号","逾期天数"]',
  
  -- 状态与统计
  is_enabled TINYINT(1) DEFAULT 1 COMMENT '是否启用：1-启用，0-禁用',
  sort_order INT DEFAULT 0 COMMENT '排序权重，越小越靠前',
  usage_count INT DEFAULT 0 COMMENT '使用次数统计',
  
  -- 时间戳
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  created_by BIGINT COMMENT '创建人ID（催员ID）',
  updated_by BIGINT COMMENT '更新人ID（催员ID）',
  
  -- 索引
  INDEX idx_tenant_type (tenant_id, template_type),
  INDEX idx_stage_scene (case_stage, scene),
  INDEX idx_enabled (is_enabled),
  INDEX idx_sort (sort_order, created_at),
  UNIQUE KEY uk_tenant_name (tenant_id, template_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息模板配置表';

-- 插入初始化数据（Mock数据示例）
-- 变量值基于"甲方字段展示配置"中定义的标准字段
INSERT INTO message_templates (tenant_id, template_name, template_type, agency_ids, case_stage, scene, time_slot, content, variables, is_enabled, sort_order, created_by) VALUES
-- 组织模板示例
(1, '早安问候 + 还款提醒', 'organization', NULL, 'S0', 'greeting', 'morning', 
 '您好{客户名}，早上好！您在{App名称}的{产品名称}（贷款编号：{贷款编号}）已逾期{逾期天数}天，应还金额{应还金额}，请在{到期日期}前完成还款。谢谢您的配合！', 
 '["客户名","App名称","产品名称","贷款编号","逾期天数","应还金额","到期日期"]', 1, 10, 1),

(1, '下午催款提醒', 'organization', '[1,2,3]', 'S1-3', 'reminder', 'afternoon', 
 '{客户名}您好，您的{产品名称}（贷款编号：{贷款编号}）逾期已{逾期天数}天，贷款金额{贷款金额}，未还金额{应还金额}（本金{本金}+罚息{罚息}），请今日内完成还款，如有困难请联系我们！', 
 '["客户名","产品名称","贷款编号","逾期天数","贷款金额","应还金额","本金","罚息"]', 1, 20, 1),

(1, '晚间强度提醒', 'organization', '[1,2]', 'S3+', 'strong', 'evening', 
 '{客户名}，您在{App名称}的贷款（{贷款编号}）已严重逾期{逾期天数}天，未还金额{应还金额}，如不立即还款我们将采取法律措施！', 
 '["客户名","App名称","贷款编号","逾期天数","应还金额"]', 1, 30, 1),

(1, '承诺还款确认', 'organization', NULL, 'C', 'greeting', 'afternoon', 
 '{客户名}您好，感谢您承诺在{到期日期}前还款{应还金额}。我们会持续关注，期待您的履约。', 
 '["客户名","到期日期","应还金额"]', 1, 40, 1),

(1, '逾期初期温和提醒', 'organization', NULL, 'S1-3', 'reminder', 'morning', 
 '尊敬的{客户名}，您好！您在{App名称}的{产品名称}（贷款编号：{贷款编号}）已逾期{逾期天数}天，应还金额{应还金额}。为避免影响个人信用，请尽快安排还款。', 
 '["客户名","App名称","产品名称","贷款编号","逾期天数","应还金额"]', 1, 50, 1),

(1, '贷款金额明细提醒', 'organization', NULL, 'S1-3', 'reminder', 'afternoon', 
 '{客户名}您好，您的贷款详情：贷款金额{贷款金额}，本金{本金}，罚息{罚息}，应还总额{应还金额}，请在{到期日期}前还款。', 
 '["客户名","贷款金额","本金","罚息","应还金额","到期日期"]', 1, 60, 1),

-- 个人模板示例（agency_ids对个人模板无效）
(1, '个人问候（上午）', 'personal', NULL, 'S0', 'greeting', 'morning', 
 '您好{客户名}，早上好！关于您在{App名称}的{产品名称}还款事宜想跟您沟通一下，现在方便吗？', 
 '["客户名","App名称","产品名称"]', 1, 100, 1001),

(1, '还款确认模板', 'personal', NULL, 'C', 'greeting', 'afternoon', 
 '{客户名}您好，我们已收到您的还款{应还金额}，感谢您的配合！', 
 '["客户名","应还金额"]', 1, 110, 1001);

