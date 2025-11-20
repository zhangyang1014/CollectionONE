#!/usr/bin/env python3
"""
通知模板数据库初始化脚本
用于创建 notification_templates 表并插入测试数据
"""
import sys
import os

# 添加项目根目录到 Python 路径
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from sqlalchemy import text
from app.core.database import engine
from app.models.notification_template import NotificationTemplate

def setup_notification_templates():
    """创建表并插入测试数据"""
    print("开始设置通知模板...")
    
    # 创建表
    print("1. 创建 notification_templates 表...")
    try:
        NotificationTemplate.metadata.create_all(bind=engine)
        print("✓ 表创建成功")
    except Exception as e:
        print(f"✗ 表创建失败: {e}")
        return False
    
    # 插入测试数据
    print("\n2. 插入测试数据...")
    
    # 使用 ORM 插入数据而不是原始 SQL
    from sqlalchemy.orm import sessionmaker
    Session = sessionmaker(bind=engine)
    session = Session()
    
    # 清空现有数据
    session.query(NotificationTemplate).delete()
    session.commit()
    
    # 准备测试数据
    templates_data = [
    -- 1. 案件标签变化模板
    (
        'tenant_a', 'TPL_CASE_TAG_CHANGE', '案件标签变化通知', 'case_tag_change',
        '您的案件 {{case_number}} 标签已更新为 {{new_tag}}',
        '案件编号 {{case_number}} 的标签从 {{old_tag}} 变更为 {{new_tag}}，请及时查看处理。',
        '/cases/{{case_id}}/detail', 'agency', '["agency_001","agency_002"]',
        NULL, NULL, 0, 30, 3, '09:00', '21:00', 2, 5, 1,
        '["case_number","case_id","old_tag","new_tag"]', 0, 0
    ),
    -- 2. 案件还款模板
    (
        'tenant_a', 'TPL_CASE_REPAYMENT', '案件还款通知', 'case_repayment',
        '案件 {{case_number}} 收到还款 {{amount}} 元',
        '恭喜!案件 {{case_number}} 于 {{repayment_time}} 收到还款 {{amount}} 元，当前欠款余额 {{remaining_amount}} 元。',
        '/cases/{{case_id}}/repayment', 'team', NULL,
        '["team_001","team_002","team_003"]', NULL, 1, NULL, NULL, NULL, NULL, 1, 10, 1,
        '["case_number","case_id","amount","repayment_time","remaining_amount"]', 0, 0
    ),
    -- 3. 用户访问APP模板
    (
        'tenant_a', 'TPL_USER_ACCESS_APP', '客户访问APP通知', 'user_access_app',
        '客户 {{customer_name}} 已登录APP',
        '您负责的客户 {{customer_name}} ({{customer_phone}}) 于 {{access_time}} 登录了APP，建议及时跟进。',
        '/cases/{{case_id}}/customer', 'collector', NULL, NULL,
        '["collector_001","collector_002"]', 0, 60, 2, '08:00', '22:00', 3, 5, 1,
        '["customer_name","customer_phone","case_id","access_time"]', 0, 0
    ),
    -- 4. 用户访问还款页模板
    (
        'tenant_a', 'TPL_USER_ACCESS_REPAY_PAGE', '客户访问还款页通知', 'user_access_repay_page',
        '客户 {{customer_name}} 正在查看还款页面',
        '您负责的客户 {{customer_name}} 于 {{access_time}} 访问了还款页面，还款意愿较强，请抓住机会联系催收!',
        '/cases/{{case_id}}/detail', 'collector', NULL, NULL,
        '["collector_003","collector_004","collector_005"]', 0, 15, 5, '00:00', '23:59', 1, 8, 1,
        '["customer_name","customer_phone","case_id","access_time"]', 0, 0
    ),
    -- 5. 案件分配通知
    (
        'tenant_a', 'TPL_CASE_ASSIGNED', '案件分配通知', 'case_assigned',
        '您有新的案件待处理',
        '系统已将 {{case_count}} 个案件分配给您，总金额 {{total_amount}} 元，请及时登录系统查看并开始催收工作。',
        '/cases/my-cases', 'collector', NULL, NULL, NULL, 1, NULL, NULL, NULL, NULL, 1, 0, 1,
        '["case_count","total_amount","assign_time"]', 0, 0
    ),
    -- 6. 逾期提醒模板
    (
        'tenant_b', 'TPL_OVERDUE_REMINDER', '案件逾期提醒', 'overdue_reminder',
        '案件 {{case_number}} 已逾期 {{overdue_days}} 天',
        '案件 {{case_number}} 已逾期 {{overdue_days}} 天，当前欠款 {{amount}} 元，请加强催收力度。',
        '/cases/{{case_id}}/detail', 'agency', '["agency_003"]', NULL, NULL, 0, 120, 2, '09:00', '18:00', 2, 5, 1,
        '["case_number","case_id","overdue_days","amount"]', 0, 0
    ),
    -- 7. 承诺还款提醒
    (
        'tenant_b', 'TPL_PROMISE_REMINDER', '承诺还款到期提醒', 'promise_reminder',
        '客户 {{customer_name}} 承诺今日还款',
        '客户 {{customer_name}} 承诺于今日还款 {{promise_amount}} 元，请及时跟进确认。',
        '/cases/{{case_id}}/promise', 'collector', NULL, NULL, NULL, 0, 30, 3, '09:00', '20:00', 1, 5, 1,
        '["customer_name","case_id","promise_amount","promise_date"]', 0, 0
    ),
    -- 8. 系统维护通知
    (
        'tenant_a', 'TPL_SYSTEM_MAINTENANCE', '系统维护通知', 'system_maintenance',
        '系统将于 {{maintenance_time}} 进行维护',
        '系统将于 {{maintenance_time}} 进行维护升级，预计持续 {{duration}} 小时，期间将无法访问，请提前做好工作安排。',
        '/system/notice', 'agency', NULL, NULL, NULL, 1, NULL, NULL, NULL, NULL, 1, 0, 1,
        '["maintenance_time","duration","maintenance_content"]', 0, 0
    ),
    -- 9. 催收目标达成通知
    (
        'tenant_a', 'TPL_TARGET_ACHIEVED', '催收目标达成通知', 'target_achieved',
        '恭喜!您已完成本月催收目标',
        '恭喜!您本月已完成催收目标，回款金额 {{collected_amount}} 元，完成率 {{completion_rate}}%，继续加油!',
        '/performance/my-stats', 'collector', NULL, NULL, NULL, 1, NULL, NULL, NULL, NULL, 1, 10, 1,
        '["collected_amount","completion_rate","target_amount"]', 0, 0
    ),
    -- 10. 客户投诉通知
    (
        'tenant_b', 'TPL_CUSTOMER_COMPLAINT', '客户投诉通知', 'customer_complaint',
        '收到客户投诉，请及时处理',
        '客户 {{customer_name}} 于 {{complaint_time}} 提交投诉，投诉内容: {{complaint_content}}，请在 {{deadline}} 前处理完毕。',
        '/complaints/{{complaint_id}}/detail', 'team', NULL, '["team_004"]', NULL, 1, NULL, NULL, NULL, NULL, 1, 0, 1,
        '["customer_name","complaint_time","complaint_content","complaint_id","deadline"]', 0, 0
    );
    """
    
    try:
        with engine.connect() as conn:
            # 分割 SQL 语句并逐条执行
            statements = [s.strip() for s in mock_data_sql.split(';') if s.strip()]
            for statement in statements:
                if statement:
                    conn.execute(text(statement))
            conn.commit()
        print("✓ 测试数据插入成功")
        
        # 验证数据
        with engine.connect() as conn:
            result = conn.execute(text("SELECT COUNT(*) FROM notification_templates"))
            count = result.scalar()
            print(f"\n✓ 数据验证成功: 共插入 {count} 条记录")
            
        return True
        
    except Exception as e:
        print(f"✗ 数据插入失败: {e}")
        import traceback
        traceback.print_exc()
        return False

if __name__ == "__main__":
    print("=" * 60)
    print("通知模板数据库初始化")
    print("=" * 60)
    
    success = setup_notification_templates()
    
    if success:
        print("\n" + "=" * 60)
        print("✓ 设置完成!")
        print("=" * 60)
        print("\n下一步:")
        print("1. 重启后端服务: cd backend && bash restart_backend.sh")
        print("2. 访问前端页面: http://localhost:5173/system/notification-config")
        print("3. 切换到 '通知模板' 标签页查看数据")
    else:
        print("\n" + "=" * 60)
        print("✗ 设置失败，请检查错误信息")
        print("=" * 60)
        sys.exit(1)

