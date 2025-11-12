"""
数据库初始化脚本
- 创建所有表
- 初始化标准字段数据
- 创建测试数据（2个甲方，每个甲方50个案件）
"""

import sys
import os
from datetime import datetime, timedelta
import random
import hashlib

# 添加项目根目录到路径
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from app.core.config import settings
from app.core.database import Base
from app.models.tenant import Tenant
from app.models.collection_agency import CollectionAgency
from app.models.collection_team import CollectionTeam
from app.models.collector import Collector
from app.models.case_queue import CaseQueue
from app.models.case import Case
from app.models.field_group import FieldGroup
from app.models.standard_field import StandardField
from app.models.tenant_field_config import TenantFieldConfig
from app.models.public_notification import PublicNotification
from mock_field_data import FIELD_GROUPS, STANDARD_FIELDS
import json


def hash_password(password: str) -> str:
    """简单的密码哈希（生产环境应使用bcrypt）"""
    return hashlib.sha256(password.encode()).hexdigest()


def create_tables(engine):
    """创建所有表"""
    print("正在创建数据库表...")
    Base.metadata.drop_all(bind=engine)  # 先删除所有表
    Base.metadata.create_all(bind=engine)
    print("✅ 数据库表创建完成")


def init_field_groups(session):
    """初始化字段分组"""
    print("\n正在初始化字段分组...")
    
    for group_data in FIELD_GROUPS:
        group = FieldGroup(
            id=group_data['id'],
            group_name=group_data['group_name'],
            group_key=group_data['group_key'],
            parent_id=group_data.get('parent_id'),
            sort_order=group_data.get('sort_order', 0),
            is_active=group_data.get('is_active', True)
        )
        # 如果有group_name_en字段，需要单独设置
        if 'group_name_en' in group_data:
            group.group_name_en = group_data['group_name_en']
        session.add(group)
    
    session.commit()
    print(f"✅ 已创建 {len(FIELD_GROUPS)} 个字段分组")


def init_standard_fields(session):
    """初始化标准字段"""
    print("\n正在初始化标准字段...")
    
    for field_data in STANDARD_FIELDS:
        field = StandardField(
            id=field_data['id'],
            field_name=field_data['field_name'],
            field_key=field_data['field_key'],
            field_type=field_data['field_type'],
            field_group_id=field_data['field_group_id'],
            sort_order=field_data.get('sort_order', 0),
            is_required=field_data.get('is_required', False),
            is_extended=field_data.get('is_extended', False),
            is_active=field_data.get('is_active', True),
            is_deleted=field_data.get('is_deleted', False)
        )
        # 设置可选字段
        if 'field_name_en' in field_data:
            field.field_name_en = field_data['field_name_en']
        if 'description' in field_data:
            field.description = field_data['description']
        if 'validation_rules' in field_data:
            field.validation_rules = field_data['validation_rules']
        if 'enum_options' in field_data:
            field.enum_options = field_data['enum_options']
        session.add(field)
    
    session.commit()
    print(f"✅ 已创建 {len(STANDARD_FIELDS)} 个标准字段")


def create_default_queues(session, tenant_id: int):
    """创建默认案件队列"""
    default_queues = [
        {"queue_code": "C", "queue_name": "C队列", "overdue_days_start": None, "overdue_days_end": -1, "sort_order": 1},
        {"queue_code": "S0", "queue_name": "S0队列", "overdue_days_start": 0, "overdue_days_end": 0, "sort_order": 2},
        {"queue_code": "S1", "queue_name": "S1队列", "overdue_days_start": 1, "overdue_days_end": 5, "sort_order": 3},
        {"queue_code": "L1", "queue_name": "L1队列", "overdue_days_start": 6, "overdue_days_end": 90, "sort_order": 4},
        {"queue_code": "M1", "queue_name": "M1队列", "overdue_days_start": 91, "overdue_days_end": None, "sort_order": 5},
    ]
    
    for queue_data in default_queues:
        queue = CaseQueue(
            tenant_id=tenant_id,
            queue_code=queue_data['queue_code'],
            queue_name=queue_data['queue_name'],
            overdue_days_start=queue_data['overdue_days_start'],
            overdue_days_end=queue_data['overdue_days_end'],
            sort_order=queue_data['sort_order']
        )
        session.add(queue)
    
    session.commit()
    return session.query(CaseQueue).filter(CaseQueue.tenant_id == tenant_id).all()


def init_tenant_field_configs(session, tenant_id: int):
    """为甲方初始化字段配置（继承所有标准字段）"""
    standard_fields = session.query(StandardField).all()
    
    for idx, std_field in enumerate(standard_fields):
        config = TenantFieldConfig(
            tenant_id=tenant_id,
            field_id=std_field.id,  # 使用field_id而不是standard_field_id
            field_type='standard',  # 字段类型
            is_visible=True,
            is_required=std_field.is_required,
            is_readonly=False,
            is_enabled=True,
            sort_order=idx + 1
        )
        session.add(config)
    
    session.commit()


def create_test_data(session):
    """创建测试数据"""
    print("\n正在创建测试数据...")
    
    # 1. 创建2个甲方
    tenants = []
    for i in range(1, 3):
        tenant = Tenant(
            tenant_code=f"TENANT00{i}",
            tenant_name=f"示例甲方{chr(64+i)}",  # 甲方A, 甲方B
            country_code="CN",
            timezone=8,  # UTC+8 (中国时区)
            currency_code="CNY",
            is_active=True
        )
        session.add(tenant)
        session.flush()  # flush后id会被自动生成
        tenants.append(tenant)
        
        # 为甲方创建默认队列
        queues = create_default_queues(session, tenant.id)
        
        # 为甲方初始化字段配置
        init_tenant_field_configs(session, tenant.id)
        
        print(f"✅ 已创建甲方: {tenant.tenant_name} (ID: {tenant.id})")
        
        # 2. 为每个甲方创建机构
        agencies = []
        for j in range(1, 3):  # 每个甲方2个机构
            agency = CollectionAgency(
                tenant_id=tenant.id,
                agency_code=f"AGENCY{i}0{j}",
                agency_name=f"{tenant.tenant_name}催收机构{j}",
                is_active=True
            )
            session.add(agency)
            session.flush()
            agencies.append(agency)
            print(f"  ✅ 已创建机构: {agency.agency_name} (ID: {agency.id})")
            
            # 3. 为每个机构创建小组
            teams = []
            for k in range(1, 3):  # 每个机构2个小组
                team = CollectionTeam(
                    tenant_id=tenant.id,
                    agency_id=agency.id,
                    team_code=f"TEAM{i}{j}{k}",
                    team_name=f"{agency.agency_name}小组{k}",
                    is_active=True
                )
                session.add(team)
                session.flush()
                teams.append(team)
                print(f"    ✅ 已创建小组: {team.team_name} (ID: {team.id})")
                
                # 4. 为每个小组创建催员
                collectors = []
                for l in range(1, 4):  # 每个小组3个催员
                    collector = Collector(
                        tenant_id=tenant.id,
                        agency_id=agency.id,
                        team_id=team.id,
                        collector_code=f"COL{i}{j}{k}{l}",
                        collector_name=f"催员{i}{j}{k}{l}",
                        login_id=f"collector{i}{j}{k}{l}",
                        email=f"collector{i}{j}{k}{l}@example.com",
                        password_hash=hash_password("123456"),
                        is_active=True
                    )
                    session.add(collector)
                    session.flush()
                    collectors.append(collector)
                
                print(f"      ✅ 已创建 {len(collectors)} 个催员")
        
        # 5. 为每个甲方创建50个案件
        print(f"\n  正在为 {tenant.tenant_name} 创建50个案件...")
        all_collectors = session.query(Collector).filter(
            Collector.tenant_id == tenant.id
        ).all()
        
        case_statuses = ['pending_repayment', 'partial_repayment', 'normal_settlement', 'extension_settlement']
        
        for case_num in range(1, 51):
            # 随机选择队列
            queue = random.choice(queues)
            
            # 计算逾期天数
            if queue.overdue_days_start is None:
                overdue_days = -random.randint(1, 30)
            elif queue.overdue_days_end is None:
                overdue_days = random.randint(queue.overdue_days_start, queue.overdue_days_start + 200)
            else:
                overdue_days = random.randint(
                    queue.overdue_days_start, 
                    queue.overdue_days_end
                )
            
            # 随机分配催员
            collector = random.choice(all_collectors)
            
            # 随机案件状态
            case_status = random.choice(case_statuses)
            
            # 生成案件信息
            loan_amount = random.uniform(1000, 100000)
            repaid_amount = 0 if case_status == 'pending_repayment' else random.uniform(0, loan_amount)
            if case_status in ['normal_settlement', 'extension_settlement']:
                repaid_amount = loan_amount
            outstanding_amount = loan_amount - repaid_amount
            
            # 生成时间
            due_date = datetime.now() - timedelta(days=overdue_days)
            settlement_date = None
            if case_status in ['normal_settlement', 'extension_settlement']:
                settlement_date = datetime.now() - timedelta(days=random.randint(0, 30))
            
            case = Case(
                tenant_id=tenant.id,
                queue_id=queue.id,
                agency_id=collector.agency_id,
                team_id=collector.team_id,
                collector_id=collector.id,
                case_code=f"CASE{i}{case_num:04d}",
                user_id=f"USER{i}{case_num:04d}",
                user_name=f"客户{i}{case_num:04d}",
                mobile=f"138{i}{case_num:08d}",
                case_status=case_status,
                overdue_days=overdue_days,
                loan_amount=loan_amount,
                repaid_amount=repaid_amount,
                outstanding_amount=outstanding_amount,
                due_date=due_date,
                settlement_date=settlement_date,
                assigned_at=datetime.now() - timedelta(days=random.randint(1, 60)),
                last_contact_at=datetime.now() - timedelta(days=random.randint(0, 10)) if random.random() > 0.3 else None,
                next_follow_up_at=datetime.now() + timedelta(days=random.randint(1, 7)) if case_status == 'pending_repayment' else None
            )
            session.add(case)
        
        session.flush()
        print(f"  ✅ 已为 {tenant.tenant_name} 创建50个案件")
    
    session.commit()
    print("\n✅ 测试数据创建完成")
    
    return tenants


def init_public_notifications(session):
    """初始化公共通知数据"""
    print("\n正在初始化公共通知...")
    
    # 元旦放假通知
    notification1 = PublicNotification(
        tenant_id=None,  # 全局通知
        agency_id=None,
        title="元旦放假通知",
        h5_content="<html><body><h1>元旦放假通知</h1><p>各位同事：</p><p>根据国家法定节假日安排，元旦假期为2025年1月1日（星期三），共1天。</p><p>请各部门提前安排好工作，确保假期期间业务正常运行。</p><p>祝大家元旦快乐！</p></body></html>",
        h5_content_type="html",
        carousel_interval_seconds=30,
        is_forced_read=True,
        is_enabled=True,
        effective_start_time=datetime(2024, 12, 25, 0, 0, 0),
        effective_end_time=datetime(2025, 1, 5, 23, 59, 59),
        notify_roles=json.dumps(['collector', 'team_leader', 'agency_admin', 'tenant_admin']),
        sort_order=1
    )
    session.add(notification1)
    
    # 禁止爆催通知
    notification2 = PublicNotification(
        tenant_id=None,  # 全局通知
        agency_id=None,
        title="禁止爆催通知",
        h5_content="<html><body><h1>禁止爆催通知</h1><p>重要提醒：</p><p>根据相关法律法规和公司规定，严禁对客户进行爆催行为，包括但不限于：</p><ul><li>频繁骚扰客户</li><li>使用威胁、恐吓等不当手段</li><li>在非工作时间联系客户</li><li>泄露客户隐私信息</li></ul><p>请所有催员严格遵守催收规范，文明催收，保护客户合法权益。</p><p>如有违规行为，将严肃处理。</p></body></html>",
        h5_content_type="html",
        carousel_interval_seconds=45,
        is_forced_read=True,
        is_enabled=True,
        effective_start_time=None,  # 永久有效
        effective_end_time=None,
        notify_roles=json.dumps(['collector', 'team_leader', 'agency_admin']),
        sort_order=2
    )
    session.add(notification2)
    
    session.commit()
    print(f"✅ 已创建 2 个公共通知")


def print_summary(session):
    """打印数据摘要"""
    print("\n" + "="*60)
    print("数据库初始化摘要")
    print("="*60)
    
    tenant_count = session.query(Tenant).count()
    agency_count = session.query(CollectionAgency).count()
    team_count = session.query(CollectionTeam).count()
    collector_count = session.query(Collector).count()
    case_count = session.query(Case).count()
    field_group_count = session.query(FieldGroup).count()
    standard_field_count = session.query(StandardField).count()
    notification_count = session.query(PublicNotification).count()
    
    print(f"甲方数量: {tenant_count}")
    print(f"催收机构数量: {agency_count}")
    print(f"催收小组数量: {team_count}")
    print(f"催员数量: {collector_count}")
    print(f"案件数量: {case_count}")
    print(f"字段分组数量: {field_group_count}")
    print(f"标准字段数量: {standard_field_count}")
    print(f"公共通知数量: {notification_count}")
    
    print("\n甲方详情:")
    tenants = session.query(Tenant).all()
    for tenant in tenants:
        tenant_cases = session.query(Case).filter(Case.tenant_id == tenant.id).count()
        tenant_agencies = session.query(CollectionAgency).filter(CollectionAgency.tenant_id == tenant.id).count()
        print(f"  - {tenant.tenant_name} (ID: {tenant.id})")
        print(f"    机构: {tenant_agencies}, 案件: {tenant_cases}")
    
    print("\n" + "="*60)
    print("✅ 数据库初始化完成！")
    print("="*60)


def main():
    """主函数"""
    print("="*60)
    print("CCO系统数据库初始化")
    print("="*60)
    
    # 创建数据库引擎
    engine = create_engine(settings.DATABASE_URL)
    SessionLocal = sessionmaker(bind=engine)
    session = SessionLocal()
    
    try:
        # 1. 创建所有表
        create_tables(engine)
        
        # 2. 初始化字段分组
        init_field_groups(session)
        
        # 3. 初始化标准字段
        init_standard_fields(session)
        
        # 4. 创建测试数据
        create_test_data(session)
        
        # 5. 初始化公共通知
        init_public_notifications(session)
        
        # 6. 打印摘要
        print_summary(session)
        
    except Exception as e:
        print(f"\n❌ 初始化失败: {e}")
        import traceback
        traceback.print_exc()
        session.rollback()
    finally:
        session.close()


if __name__ == "__main__":
    main()

