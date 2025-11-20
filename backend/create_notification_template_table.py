"""
创建通知模板表
用于接收甲方核心系统推送的通知
"""
import sys
from sqlalchemy import text
from app.core.database import engine


def create_notification_template_table():
    """创建 notification_templates 表"""
    
    with engine.connect() as conn:
        try:
            # 检查表是否已存在
            result = conn.execute(text("""
                SELECT COUNT(*) as count
                FROM information_schema.tables 
                WHERE table_schema = DATABASE() 
                AND table_name = 'notification_templates'
            """))
            exists = result.fetchone()[0] > 0
            
            if exists:
                print("⚠️  notification_templates 表已存在，跳过创建")
                return
            
            print("开始创建 notification_templates 表...")
            
            # 创建表
            conn.execute(text("""
                CREATE TABLE notification_templates (
                    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
                    tenant_id INT NULL COMMENT '甲方ID（NULL表示全局模板）',
                    
                    -- 模板基本信息
                    template_id VARCHAR(100) NOT NULL UNIQUE COMMENT '模板ID（唯一标识）',
                    template_name VARCHAR(200) NOT NULL COMMENT '模板名称',
                    template_type VARCHAR(50) NOT NULL COMMENT '模板类型',
                    description TEXT NULL COMMENT '模板描述',
                    
                    -- 模板内容
                    content_template TEXT NOT NULL COMMENT '通知正文模板，支持变量',
                    jump_url_template TEXT NULL COMMENT '点击后跳转的URL模板',
                    
                    -- 发送对象配置
                    target_type VARCHAR(20) NOT NULL DEFAULT 'agency' COMMENT '发送对象类型：agency/team/collector',
                    target_agencies JSON NULL COMMENT '目标机构ID列表（JSON数组）',
                    target_teams JSON NULL COMMENT '目标小组ID列表（JSON数组）',
                    target_collectors JSON NULL COMMENT '目标催员ID列表（JSON数组）',
                    
                    -- 阅读机制配置
                    is_forced_read BOOLEAN DEFAULT FALSE COMMENT '是否强制阅读',
                    repeat_interval_minutes INT NULL COMMENT '非强制阅读时的重复提醒间隔（分钟）',
                    max_remind_count INT NULL COMMENT '非强制阅读时的最大提醒次数',
                    notify_time_start VARCHAR(5) NULL COMMENT '通知时间范围开始（HH:MM）',
                    notify_time_end VARCHAR(5) NULL COMMENT '通知时间范围结束（HH:MM）',
                    
                    -- 优先级和展示
                    priority VARCHAR(20) DEFAULT 'medium' COMMENT '优先级：high/medium/low',
                    display_duration_seconds INT DEFAULT 5 COMMENT '展示时长（秒）',
                    
                    -- 启用状态
                    is_enabled BOOLEAN DEFAULT TRUE COMMENT '是否启用',
                    
                    -- 变量说明
                    available_variables JSON NULL COMMENT '可用变量列表及说明（JSON）',
                    
                    -- 统计信息
                    total_sent INT DEFAULT 0 COMMENT '累计发送次数',
                    total_read INT DEFAULT 0 COMMENT '累计阅读次数',
                    
                    -- 时间戳
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                    created_by INT NULL COMMENT '创建人ID',
                    
                    -- 索引
                    INDEX idx_tenant_id (tenant_id),
                    INDEX idx_template_id (template_id),
                    INDEX idx_template_type (template_type)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知模板表';
            """))
            conn.commit()
            
            print("✅ notification_templates 表创建成功！")
            
            # 插入示例模板
            print("\n插入示例模板...")
            conn.execute(text("""
                INSERT INTO notification_templates (
                    template_id, template_name, template_type, description,
                    content_template, jump_url_template,
                    target_type, priority, is_enabled,
                    available_variables
                ) VALUES 
                (
                    'case_tag_change',
                    '案件标签变化模板',
                    'case_tag_change',
                    '当案件标签发生变化时发送通知',
                    '案件 {case_number} 的标签已从 {old_tag} 更改为 {new_tag}，操作人：{operator}',
                    '/cases/{case_id}',
                    'agency',
                    'medium',
                    TRUE,
                    '{"case_id":"案件ID","case_number":"案件编号","tag_name":"标签名称","old_tag":"旧标签","new_tag":"新标签","operator":"操作人"}'
                ),
                (
                    'case_payment',
                    '案件还款模板',
                    'case_payment',
                    '当案件收到还款时发送通知',
                    '案件 {case_number} 收到还款 ￥{amount}，还款时间：{payment_time}',
                    '/cases/{case_id}',
                    'agency',
                    'high',
                    TRUE,
                    '{"case_id":"案件ID","case_number":"案件编号","amount":"还款金额","payment_time":"还款时间","payment_channel":"还款渠道","debtor_name":"债务人姓名"}'
                ),
                (
                    'user_app_visit',
                    '用户访问APP模板',
                    'user_app_visit',
                    '当用户访问APP时发送通知',
                    '用户 {user_name} ({user_phone}) 访问了APP，案件：{case_number}',
                    '/cases/{case_id}',
                    'collector',
                    'medium',
                    TRUE,
                    '{"case_id":"案件ID","case_number":"案件编号","user_name":"用户姓名","user_phone":"用户手机号","visit_time":"访问时间","device_type":"设备类型"}'
                ),
                (
                    'user_payment_page_visit',
                    '用户访问还款页模板',
                    'user_payment_page_visit',
                    '当用户访问还款页面时发送通知',
                    '用户 {user_name} 访问了还款页面，待还金额：￥{outstanding_amount}',
                    '/cases/{case_id}',
                    'collector',
                    'high',
                    TRUE,
                    '{"case_id":"案件ID","case_number":"案件编号","user_name":"用户姓名","user_phone":"用户手机号","visit_time":"访问时间","outstanding_amount":"待还金额"}'
                )
            """))
            conn.commit()
            
            print("✅ 示例模板插入成功！")
            print("\n已创建以下示例模板：")
            print("  1. 案件标签变化模板 (case_tag_change)")
            print("  2. 案件还款模板 (case_payment)")
            print("  3. 用户访问APP模板 (user_app_visit)")
            print("  4. 用户访问还款页模板 (user_payment_page_visit)")
            
        except Exception as e:
            print(f"\n❌ 创建失败: {str(e)}")
            conn.rollback()
            sys.exit(1)


if __name__ == "__main__":
    print("=" * 60)
    print("创建通知模板表")
    print("=" * 60)
    create_notification_template_table()
    print("=" * 60)

