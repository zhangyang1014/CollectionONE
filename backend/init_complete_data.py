#!/usr/bin/env python3
"""
完整数据初始化脚本
包含：甲方、机构、小组、催员、案件、字段分组、标准字段、通知模板
"""

import sys
from datetime import datetime, timedelta
from sqlalchemy.orm import Session
from app.core.database import engine, Base
from app.models import (
    Tenant, CollectionAgency, CollectionTeam, Collector, Case, CaseQueue,
    FieldGroup, StandardField, NotificationTemplate
)
# from app.core.security import get_password_hash  # 不需要，使用预计算的哈希

def create_default_queues_for_tenant(session, tenant_id):
    """为指定甲方创建默认队列配置"""
    default_queues = [
        {
            "queue_code": "C",
            "queue_name": "C队列",
            "queue_name_en": "C Queue",
            "queue_description": "未逾期，提前还款客户",
            "overdue_days_start": None,  # -∞
            "overdue_days_end": -1,
            "sort_order": 1,
            "is_active": True
        },
        {
            "queue_code": "S0",
            "queue_name": "S0队列",
            "queue_name_en": "S0 Queue",
            "queue_description": "当日到期，需重点关注",
            "overdue_days_start": 0,
            "overdue_days_end": 0,
            "sort_order": 2,
            "is_active": True
        },
        {
            "queue_code": "S1",
            "queue_name": "S1队列",
            "queue_name_en": "S1 Queue",
            "queue_description": "轻度逾期，友好提醒（1-5天）",
            "overdue_days_start": 1,
            "overdue_days_end": 5,
            "sort_order": 3,
            "is_active": True
        },
        {
            "queue_code": "L1",
            "queue_name": "L1队列",
            "queue_name_en": "L1 Queue",
            "queue_description": "中度逾期，加强催收（6-90天）",
            "overdue_days_start": 6,
            "overdue_days_end": 90,
            "sort_order": 4,
            "is_active": True
        },
        {
            "queue_code": "M1",
            "queue_name": "M1队列",
            "queue_name_en": "M1 Queue",
            "queue_description": "重度逾期，专项处理（91天以上）",
            "overdue_days_start": 91,
            "overdue_days_end": None,  # +∞
            "sort_order": 5,
            "is_active": True
        }
    ]
    
    queues = []
    for queue_data in default_queues:
        queue = CaseQueue(
            tenant_id=tenant_id,
            **queue_data
        )
        session.add(queue)
        queues.append(queue)
    
    return queues

def init_complete_data():
    """初始化完整的mock数据"""
    print("=" * 60)
    print("开始初始化完整数据...")
    print("=" * 60)
    
    # 创建数据库会话
    db = Session(engine)
    
    try:
        # 1. 初始化甲方数据
        print("\n1. 初始化甲方数据...")
        tenants_data = [
            {
                "tenant_code": "BTQ",
                "tenant_name": "百腾企业",
                "tenant_name_en": "BTQ Enterprise",
                "country_code": "MX",
                "timezone": -6,  # Mexico City UTC-6
                "currency_code": "MXN",
                "is_active": True
            },
            {
                "tenant_code": "BTSK",
                "tenant_name": "百腾数科",
                "tenant_name_en": "BTSK Technology",
                "country_code": "IN",
                "timezone": 5,  # India UTC+5:30 (使用5)
                "currency_code": "INR",
                "is_active": True
            },
            {
                "tenant_code": "XYQY",
                "tenant_name": "星耀企业",
                "tenant_name_en": "Xingyao Enterprise",
                "country_code": "PH",
                "timezone": 8,  # Philippines UTC+8
                "currency_code": "PHP",
                "is_active": True
            }
        ]
        
        tenants = []
        for data in tenants_data:
            tenant = Tenant(**data)
            db.add(tenant)
            tenants.append(tenant)
        db.flush()
        print(f"✓ 已创建 {len(tenants)} 个甲方")
        
        # 2. 初始化机构数据
        print("\n2. 初始化机构数据...")
        agencies_data = []
        for i, tenant in enumerate(tenants):
            for j in range(1, 3):  # 每个甲方2个机构
                agencies_data.append({
                    "tenant_id": tenant.id,
                    "agency_code": f"AG{i+1}{j:02d}",
                    "agency_name": f"{tenant.tenant_name}-机构{j}",
                    "agency_name_en": f"{tenant.tenant_name_en} Agency {j}",
                    "is_active": True
                })
        
        agencies = []
        for data in agencies_data:
            agency = CollectionAgency(**data)
            db.add(agency)
            agencies.append(agency)
        db.flush()
        print(f"✓ 已创建 {len(agencies)} 个机构")
        
        # 3. 初始化小组数据
        print("\n3. 初始化小组数据...")
        teams_data = []
        for agency in agencies:
            for i in range(1, 3):  # 每个机构2个小组
                teams_data.append({
                    "tenant_id": agency.tenant_id,
                    "agency_id": agency.id,
                    "team_code": f"{agency.agency_code}T{i}",
                    "team_name": f"{agency.agency_name}-小组{i}",
                    "team_name_en": f"{agency.agency_name_en} Team {i}",
                    "is_active": True
                })
        
        teams = []
        for data in teams_data:
            team = CollectionTeam(**data)
            db.add(team)
            teams.append(team)
        db.flush()
        print(f"✓ 已创建 {len(teams)} 个小组")
        
        # 4. 初始化催员数据
        print("\n4. 初始化催员数据...")
        # 使用预先计算的密码哈希（123456）
        default_password = "$2b$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYzNW6NRWfS"
        collectors_data = []
        collector_idx = 1
        for team in teams:
            for i in range(1, 4):  # 每个小组3个催员
                collectors_data.append({
                    "tenant_id": team.tenant_id,
                    "agency_id": team.agency_id,
                    "team_id": team.id,
                    "collector_code": f"COL{collector_idx:03d}",
                    "login_id": f"collector{collector_idx:03d}",
                    "password_hash": default_password,
                    "collector_name": f"催员{collector_idx:03d}",
                    "mobile": f"+52155{collector_idx:07d}",
                    "email": f"collector{collector_idx:03d}@example.com",
                    "status": "active"
                })
                collector_idx += 1
        
        collectors = []
        for data in collectors_data:
            collector = Collector(**data)
            db.add(collector)
            collectors.append(collector)
        db.flush()
        print(f"✓ 已创建 {len(collectors)} 个催员")
        
        # 5. 初始化案件队列数据
        print("\n5. 初始化案件队列数据...")
        all_queues = []
        for tenant in tenants:
            queues = create_default_queues_for_tenant(db, tenant.id)
            all_queues.extend(queues)
        db.flush()
        print(f"✓ 已创建 {len(all_queues)} 个队列（每个甲方5个默认队列）")
        
        # 6. 初始化案件数据
        print("\n6. 初始化案件数据...")
        cases_data = []
        for i, tenant in enumerate(tenants):
            for j in range(1, 11):  # 每个甲方10个案件
                case_idx = i * 10 + j
                cases_data.append({
                    "tenant_id": tenant.id,
                    "case_code": f"CASE{case_idx:05d}",
                    "user_id": f"USER{case_idx:05d}",
                    "user_name": f"借款人{case_idx:03d}",
                    "mobile": f"+52155{case_idx:07d}",
                    "loan_amount": 5000 + (case_idx * 100),
                    "outstanding_amount": 3000 + (case_idx * 50),
                    "overdue_days": case_idx % 30 + 1,
                    "case_status": "pending_repayment",
                    "assigned_at": datetime.now() - timedelta(days=case_idx)
                })
        
        cases = []
        for data in cases_data:
            case = Case(**data)
            db.add(case)
            cases.append(case)
        db.flush()
        print(f"✓ 已创建 {len(cases)} 个案件")
        
        # 7. 初始化字段分组
        print("\n7. 初始化字段分组...")
        field_groups_data = [
            {'group_key': 'customer_basic', 'group_name': '客户基础信息', 'group_name_en': 'Customer Basic Info', 'parent_id': None, 'sort_order': 1},
            {'group_key': 'loan_details', 'group_name': '贷款详情', 'group_name_en': 'Loan Details', 'parent_id': None, 'sort_order': 2},
            {'group_key': 'borrowing_records', 'group_name': '借款记录', 'group_name_en': 'Borrowing Records', 'parent_id': None, 'sort_order': 3},
            {'group_key': 'repayment_records', 'group_name': '还款记录', 'group_name_en': 'Repayment Records', 'parent_id': None, 'sort_order': 4},
            {'group_key': 'installment_details', 'group_name': '分期详情', 'group_name_en': 'Installment Details', 'parent_id': None, 'sort_order': 5},
        ]
        
        field_groups = []
        for data in field_groups_data:
            group = FieldGroup(**data, is_active=True)
            db.add(group)
            field_groups.append(group)
        db.flush()
        
        # 添加子分组
        sub_groups_data = [
            {'group_key': 'identity_info', 'group_name': '基础身份信息', 'group_name_en': 'Identity Information', 'parent_id': field_groups[0].id, 'sort_order': 1},
            {'group_key': 'education', 'group_name': '教育信息', 'group_name_en': 'Education', 'parent_id': field_groups[0].id, 'sort_order': 2},
            {'group_key': 'employment', 'group_name': '职业信息', 'group_name_en': 'Employment', 'parent_id': field_groups[0].id, 'sort_order': 3},
            {'group_key': 'user_behavior', 'group_name': '用户行为与信用', 'group_name_en': 'User Behavior & Credit', 'parent_id': field_groups[0].id, 'sort_order': 4},
        ]
        
        for data in sub_groups_data:
            group = FieldGroup(**data, is_active=True)
            db.add(group)
            field_groups.append(group)
        db.flush()
        print(f"✓ 已创建 {len(field_groups)} 个字段分组")
        
        # 8. 初始化标准字段（只创建部分核心字段）
        print("\n8. 初始化标准字段...")
        standard_fields_data = [
            # 基础身份信息
            {'field_key': 'user_id', 'field_name': '用户编号', 'field_name_en': 'user_id', 'field_type': 'String', 'field_group_id': field_groups[5].id, 'is_required': True, 'sort_order': 1},
            {'field_key': 'user_name', 'field_name': '用户姓名', 'field_name_en': 'user_name', 'field_type': 'String', 'field_group_id': field_groups[5].id, 'is_required': True, 'sort_order': 2},
            {'field_key': 'gender', 'field_name': '性别', 'field_name_en': 'gender', 'field_type': 'Enum', 'field_group_id': field_groups[5].id, 'is_required': False, 'sort_order': 3},
            {'field_key': 'mobile_number', 'field_name': '手机号码', 'field_name_en': 'mobile_number', 'field_type': 'String', 'field_group_id': field_groups[5].id, 'is_required': True, 'sort_order': 4},
            {'field_key': 'id_number', 'field_name': '证件号码', 'field_name_en': 'id_number', 'field_type': 'String', 'field_group_id': field_groups[5].id, 'is_required': False, 'sort_order': 5},
            # 贷款详情
            {'field_key': 'loan_id', 'field_name': '贷款编号', 'field_name_en': 'loan_id', 'field_type': 'String', 'field_group_id': field_groups[1].id, 'is_required': True, 'sort_order': 1},
            {'field_key': 'case_status', 'field_name': '案件状态', 'field_name_en': 'case_status', 'field_type': 'Enum', 'field_group_id': field_groups[1].id, 'is_required': False, 'sort_order': 2},
            {'field_key': 'outstanding_amount', 'field_name': '应还未还金额', 'field_name_en': 'outstanding_amount', 'field_type': 'Decimal', 'field_group_id': field_groups[1].id, 'is_required': True, 'sort_order': 3},
            {'field_key': 'overdue_days', 'field_name': '逾期天数', 'field_name_en': 'overdue_days', 'field_type': 'Integer', 'field_group_id': field_groups[1].id, 'is_required': False, 'sort_order': 4},
        ]
        
        standard_fields = []
        for data in standard_fields_data:
            field = StandardField(**data, is_extended=False, is_active=True, is_deleted=False)
            db.add(field)
            standard_fields.append(field)
        db.flush()
        print(f"✓ 已创建 {len(standard_fields)} 个标准字段")
        
        # 9. 初始化通知模板
        print("\n9. 初始化通知模板...")
        templates_data = [
            {
                "tenant_id": tenants[0].id,
                "template_id": "TPL_CASE_TAG_CHANGE",
                "template_name": "案件标签变化通知",
                "template_type": "case_tag_change",
                "description": "当案件标签发生变化时通知催员",
                "content_template": "案件 {{case_code}} 的标签已从 {{old_tag}} 变更为 {{new_tag}}",
                "jump_url_template": "/cases/{{case_id}}",
                "target_type": "agency",
                "target_agencies": None,
                "target_teams": None,
                "target_collectors": None,
                "is_forced_read": False,
                "repeat_interval_minutes": 60,
                "max_remind_count": 3,
                "notify_time_start": "09:00",
                "notify_time_end": "21:00",
                "priority": 2,
                "display_duration_seconds": 5,
                "is_enabled": True,
                "available_variables": ["case_code", "old_tag", "new_tag", "case_id"]
            },
            {
                "tenant_id": tenants[0].id,
                "template_id": "TPL_CASE_REPAYMENT",
                "template_name": "案件还款通知",
                "template_type": "case_repayment",
                "description": "当案件有还款时通知催员",
                "content_template": "案件 {{case_code}} 收到还款 {{amount}} 元",
                "jump_url_template": "/cases/{{case_id}}/repayment",
                "target_type": "collector",
                "target_agencies": None,
                "target_teams": None,
                "target_collectors": None,
                "is_forced_read": True,
                "repeat_interval_minutes": None,
                "max_remind_count": None,
                "notify_time_start": None,
                "notify_time_end": None,
                "priority": 1,
                "display_duration_seconds": 10,
                "is_enabled": True,
                "available_variables": ["case_code", "amount", "case_id"]
            },
            {
                "tenant_id": tenants[1].id,
                "template_id": "TPL_USER_APP_ACCESS",
                "template_name": "用户访问APP通知",
                "template_type": "user_app_access",
                "description": "当用户访问APP时通知催员",
                "content_template": "用户 {{user_name}} 于 {{access_time}} 访问了APP",
                "jump_url_template": "/cases/{{case_id}}",
                "target_type": "team",
                "target_agencies": None,
                "target_teams": None,
                "target_collectors": None,
                "is_forced_read": False,
                "repeat_interval_minutes": 120,
                "max_remind_count": 2,
                "notify_time_start": "08:00",
                "notify_time_end": "22:00",
                "priority": 3,
                "display_duration_seconds": 3,
                "is_enabled": True,
                "available_variables": ["user_name", "access_time", "case_id"]
            }
        ]
        
        templates = []
        for data in templates_data:
            template = NotificationTemplate(**data)
            db.add(template)
            templates.append(template)
        db.flush()
        print(f"✓ 已创建 {len(templates)} 个通知模板")
        
        # 提交所有更改
        db.commit()
        
        print("\n" + "=" * 60)
        print("✅ 数据初始化完成！")
        print("=" * 60)
        print(f"\n数据统计:")
        print(f"  - 甲方: {len(tenants)} 个")
        print(f"  - 机构: {len(agencies)} 个")
        print(f"  - 小组: {len(teams)} 个")
        print(f"  - 催员: {len(collectors)} 个 (默认密码: 123456)")
        print(f"  - 队列: {len(all_queues)} 个")
        print(f"  - 案件: {len(cases)} 个")
        print(f"  - 字段分组: {len(field_groups)} 个")
        print(f"  - 标准字段: {len(standard_fields)} 个")
        print(f"  - 通知模板: {len(templates)} 个")
        print("\n" + "=" * 60)
        
        return True
        
    except Exception as e:
        db.rollback()
        print(f"\n❌ 错误: {e}")
        import traceback
        traceback.print_exc()
        return False
    finally:
        db.close()

if __name__ == "__main__":
    # 清空现有数据
    print("\n⚠️  警告: 此操作将清空数据库中的所有数据！")
    confirm = input("确认继续? (yes/no): ")
    if confirm.lower() != "yes":
        print("操作已取消")
        sys.exit(0)
    
    print("\n清空现有数据...")
    db = Session(engine)
    try:
        # 按依赖顺序删除
        db.query(NotificationTemplate).delete()
        db.query(StandardField).delete()
        db.query(FieldGroup).delete()
        db.query(Case).delete()
        db.query(CaseQueue).delete()
        db.query(Collector).delete()
        db.query(CollectionTeam).delete()
        db.query(CollectionAgency).delete()
        db.query(Tenant).delete()
        db.commit()
        print("✓ 已清空现有数据")
    except Exception as e:
        db.rollback()
        print(f"❌ 清空数据失败: {e}")
        sys.exit(1)
    finally:
        db.close()
    
    # 初始化数据
    success = init_complete_data()
    sys.exit(0 if success else 1)

