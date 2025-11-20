#!/usr/bin/env python3
"""
初始化数据库并插入测试数据
"""
import os
import sys

# 添加项目根目录到 Python 路径
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from sqlalchemy.orm import sessionmaker
from app.core.database import engine, Base
from app.models import *  # 导入所有模型
from app.models.notification_template import NotificationTemplate

def init_database():
    """初始化数据库"""
    print("=" * 80)
    print("数据库初始化")
    print("=" * 80)
    
    try:
        # 创建所有表
        print("\n1. 创建数据库表...")
        Base.metadata.create_all(bind=engine)
        print("✓ 所有表创建成功")
        
        # 创建session
        Session = sessionmaker(bind=engine)
        session = Session()
        
        # 插入通知模板测试数据
        print("\n2. 插入通知模板测试数据...")
        
        # 清空现有数据
        session.query(NotificationTemplate).delete()
        
        templates = [
            # 1. 案件标签变化模板
            NotificationTemplate(
                tenant_id='tenant_a',
                template_id='TPL_CASE_TAG_CHANGE',
                template_name='案件标签变化通知',
                template_type='case_tag_change',
                description='您的案件 {{case_number}} 标签已更新为 {{new_tag}}',
                content_template='案件编号 {{case_number}} 的标签从 {{old_tag}} 变更为 {{new_tag}}，请及时查看处理。',
                jump_url_template='/cases/{{case_id}}/detail',
                target_type='agency',
                target_agencies='["agency_001","agency_002"]',
                target_teams=None,
                target_collectors=None,
                is_forced_read=False,
                repeat_interval_minutes=30,
                max_remind_count=3,
                notify_time_start='09:00',
                notify_time_end='21:00',
                priority=2,
                display_duration_seconds=5,
                is_enabled=True,
                available_variables='["case_number","case_id","old_tag","new_tag"]',
                total_sent=0,
                total_read=0
            ),
            # 2. 案件还款模板
            NotificationTemplate(
                tenant_id='tenant_a',
                template_id='TPL_CASE_REPAYMENT',
                template_name='案件还款通知',
                template_type='case_repayment',
                description='案件 {{case_number}} 收到还款 {{amount}} 元',
                content_template='恭喜!案件 {{case_number}} 于 {{repayment_time}} 收到还款 {{amount}} 元，当前欠款余额 {{remaining_amount}} 元。',
                jump_url_template='/cases/{{case_id}}/repayment',
                target_type='team',
                target_agencies=None,
                target_teams='["team_001","team_002","team_003"]',
                target_collectors=None,
                is_forced_read=True,
                repeat_interval_minutes=None,
                max_remind_count=None,
                notify_time_start=None,
                notify_time_end=None,
                priority=1,
                display_duration_seconds=10,
                is_enabled=True,
                available_variables='["case_number","case_id","amount","repayment_time","remaining_amount"]',
                total_sent=0,
                total_read=0
            ),
            # 3. 用户访问APP模板
            NotificationTemplate(
                tenant_id='tenant_a',
                template_id='TPL_USER_ACCESS_APP',
                template_name='客户访问APP通知',
                template_type='user_access_app',
                description='客户 {{customer_name}} 已登录APP',
                content_template='您负责的客户 {{customer_name}} ({{customer_phone}}) 于 {{access_time}} 登录了APP，建议及时跟进。',
                jump_url_template='/cases/{{case_id}}/customer',
                target_type='collector',
                target_agencies=None,
                target_teams=None,
                target_collectors='["collector_001","collector_002"]',
                is_forced_read=False,
                repeat_interval_minutes=60,
                max_remind_count=2,
                notify_time_start='08:00',
                notify_time_end='22:00',
                priority=3,
                display_duration_seconds=5,
                is_enabled=True,
                available_variables='["customer_name","customer_phone","case_id","access_time"]',
                total_sent=0,
                total_read=0
            ),
            # 4. 用户访问还款页模板
            NotificationTemplate(
                tenant_id='tenant_a',
                template_id='TPL_USER_ACCESS_REPAY_PAGE',
                template_name='客户访问还款页通知',
                template_type='user_access_repay_page',
                description='客户 {{customer_name}} 正在查看还款页面',
                content_template='您负责的客户 {{customer_name}} 于 {{access_time}} 访问了还款页面，还款意愿较强，请抓住机会联系催收!',
                jump_url_template='/cases/{{case_id}}/detail',
                target_type='collector',
                target_agencies=None,
                target_teams=None,
                target_collectors='["collector_003","collector_004","collector_005"]',
                is_forced_read=False,
                repeat_interval_minutes=15,
                max_remind_count=5,
                notify_time_start='00:00',
                notify_time_end='23:59',
                priority=1,
                display_duration_seconds=8,
                is_enabled=True,
                available_variables='["customer_name","customer_phone","case_id","access_time"]',
                total_sent=0,
                total_read=0
            ),
            # 5. 案件分配通知
            NotificationTemplate(
                tenant_id='tenant_a',
                template_id='TPL_CASE_ASSIGNED',
                template_name='案件分配通知',
                template_type='case_assigned',
                description='您有新的案件待处理',
                content_template='系统已将 {{case_count}} 个案件分配给您，总金额 {{total_amount}} 元，请及时登录系统查看并开始催收工作。',
                jump_url_template='/cases/my-cases',
                target_type='collector',
                target_agencies=None,
                target_teams=None,
                target_collectors=None,
                is_forced_read=True,
                repeat_interval_minutes=None,
                max_remind_count=None,
                notify_time_start=None,
                notify_time_end=None,
                priority=1,
                display_duration_seconds=0,
                is_enabled=True,
                available_variables='["case_count","total_amount","assign_time"]',
                total_sent=0,
                total_read=0
            ),
            # 6. 逾期提醒模板
            NotificationTemplate(
                tenant_id='tenant_b',
                template_id='TPL_OVERDUE_REMINDER',
                template_name='案件逾期提醒',
                template_type='overdue_reminder',
                description='案件 {{case_number}} 已逾期 {{overdue_days}} 天',
                content_template='案件 {{case_number}} 已逾期 {{overdue_days}} 天，当前欠款 {{amount}} 元，请加强催收力度。',
                jump_url_template='/cases/{{case_id}}/detail',
                target_type='agency',
                target_agencies='["agency_003"]',
                target_teams=None,
                target_collectors=None,
                is_forced_read=False,
                repeat_interval_minutes=120,
                max_remind_count=2,
                notify_time_start='09:00',
                notify_time_end='18:00',
                priority=2,
                display_duration_seconds=5,
                is_enabled=True,
                available_variables='["case_number","case_id","overdue_days","amount"]',
                total_sent=0,
                total_read=0
            ),
            # 7. 承诺还款提醒
            NotificationTemplate(
                tenant_id='tenant_b',
                template_id='TPL_PROMISE_REMINDER',
                template_name='承诺还款到期提醒',
                template_type='promise_reminder',
                description='客户 {{customer_name}} 承诺今日还款',
                content_template='客户 {{customer_name}} 承诺于今日还款 {{promise_amount}} 元，请及时跟进确认。',
                jump_url_template='/cases/{{case_id}}/promise',
                target_type='collector',
                target_agencies=None,
                target_teams=None,
                target_collectors=None,
                is_forced_read=False,
                repeat_interval_minutes=30,
                max_remind_count=3,
                notify_time_start='09:00',
                notify_time_end='20:00',
                priority=1,
                display_duration_seconds=5,
                is_enabled=True,
                available_variables='["customer_name","case_id","promise_amount","promise_date"]',
                total_sent=0,
                total_read=0
            ),
            # 8. 系统维护通知
            NotificationTemplate(
                tenant_id='tenant_a',
                template_id='TPL_SYSTEM_MAINTENANCE',
                template_name='系统维护通知',
                template_type='system_maintenance',
                description='系统将于 {{maintenance_time}} 进行维护',
                content_template='系统将于 {{maintenance_time}} 进行维护升级，预计持续 {{duration}} 小时，期间将无法访问，请提前做好工作安排。',
                jump_url_template='/system/notice',
                target_type='agency',
                target_agencies=None,
                target_teams=None,
                target_collectors=None,
                is_forced_read=True,
                repeat_interval_minutes=None,
                max_remind_count=None,
                notify_time_start=None,
                notify_time_end=None,
                priority=1,
                display_duration_seconds=0,
                is_enabled=True,
                available_variables='["maintenance_time","duration","maintenance_content"]',
                total_sent=0,
                total_read=0
            ),
            # 9. 催收目标达成通知
            NotificationTemplate(
                tenant_id='tenant_a',
                template_id='TPL_TARGET_ACHIEVED',
                template_name='催收目标达成通知',
                template_type='target_achieved',
                description='恭喜!您已完成本月催收目标',
                content_template='恭喜!您本月已完成催收目标，回款金额 {{collected_amount}} 元，完成率 {{completion_rate}}%，继续加油!',
                jump_url_template='/performance/my-stats',
                target_type='collector',
                target_agencies=None,
                target_teams=None,
                target_collectors=None,
                is_forced_read=True,
                repeat_interval_minutes=None,
                max_remind_count=None,
                notify_time_start=None,
                notify_time_end=None,
                priority=1,
                display_duration_seconds=10,
                is_enabled=True,
                available_variables='["collected_amount","completion_rate","target_amount"]',
                total_sent=0,
                total_read=0
            ),
            # 10. 客户投诉通知
            NotificationTemplate(
                tenant_id='tenant_b',
                template_id='TPL_CUSTOMER_COMPLAINT',
                template_name='客户投诉通知',
                template_type='customer_complaint',
                description='收到客户投诉，请及时处理',
                content_template='客户 {{customer_name}} 于 {{complaint_time}} 提交投诉，投诉内容: {{complaint_content}}，请在 {{deadline}} 前处理完毕。',
                jump_url_template='/complaints/{{complaint_id}}/detail',
                target_type='team',
                target_agencies=None,
                target_teams='["team_004"]',
                target_collectors=None,
                is_forced_read=True,
                repeat_interval_minutes=None,
                max_remind_count=None,
                notify_time_start=None,
                notify_time_end=None,
                priority=1,
                display_duration_seconds=0,
                is_enabled=True,
                available_variables='["customer_name","complaint_time","complaint_content","complaint_id","deadline"]',
                total_sent=0,
                total_read=0
            ),
        ]
        
        # 批量插入
        session.add_all(templates)
        session.commit()
        
        print(f"✓ 成功插入 {len(templates)} 条通知模板数据")
        
        # 验证
        count = session.query(NotificationTemplate).count()
        print(f"✓ 数据库中共有 {count} 条通知模板")
        
        session.close()
        
        print("\n" + "=" * 80)
        print("✓ 数据库初始化完成!")
        print("=" * 80)
        
        return True
        
    except Exception as e:
        print(f"\n✗ 初始化失败: {e}")
        import traceback
        traceback.print_exc()
        return False

if __name__ == "__main__":
    success = init_database()
    sys.exit(0 if success else 1)
