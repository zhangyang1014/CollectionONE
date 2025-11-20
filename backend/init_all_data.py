#!/usr/bin/env python3
"""
初始化所有测试数据
"""
import os
import sys
from datetime import datetime, timedelta

# 添加项目根目录到 Python 路径
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from sqlalchemy.orm import sessionmaker
from app.core.database import engine, Base
from app.models import *

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

def init_all_data():
    """初始化所有测试数据"""
    print("=" * 80)
    print("初始化所有测试数据")
    print("=" * 80)
    
    try:
        # 创建所有表
        print("\n1. 创建数据库表...")
        Base.metadata.create_all(bind=engine)
        print("✓ 所有表创建成功")
        
        # 创建session
        Session = sessionmaker(bind=engine)
        session = Session()
        
        # 清空现有数据（除了通知模板）
        print("\n2. 清空现有数据...")
        session.query(Case).delete()
        session.query(CaseQueue).delete()
        session.query(Collector).delete()
        session.query(CollectionTeam).delete()
        session.query(CollectionAgency).delete()
        session.query(Tenant).delete()
        session.commit()
        print("✓ 数据清空完成")
        
        # 插入甲方数据
        print("\n3. 插入甲方数据...")
        tenants = [
            Tenant(
                id=1,
                tenant_code="BTQ",
                tenant_name="百腾企业",
                tenant_name_en="Baiteng Enterprise",
                country_code="CN",
                timezone=8,
                currency_code="CNY",
                is_active=True
            ),
            Tenant(
                id=2,
                tenant_code="BTSK",
                tenant_name="百腾数科",
                tenant_name_en="Baiteng Digital",
                country_code="CN",
                timezone=8,
                currency_code="CNY",
                is_active=True
            ),
            Tenant(
                id=3,
                tenant_code="XYQY",
                tenant_name="星耀企业",
                tenant_name_en="Xingyao Enterprise",
                country_code="CN",
                timezone=8,
                currency_code="CNY",
                is_active=True
            )
        ]
        session.add_all(tenants)
        session.commit()
        print(f"✓ 成功插入 {len(tenants)} 个甲方")
        
        # 插入机构数据
        print("\n4. 插入机构数据...")
        agencies = [
            # BTQ的机构
            CollectionAgency(
                id=1,
                tenant_id=1,
                agency_code="BTQ_A001",
                agency_name="北京分公司",
                contact_person="赵主管",
                contact_phone="13900139001",
                is_active=True
            ),
            CollectionAgency(
                id=2,
                tenant_id=1,
                agency_code="BTQ_A002",
                agency_name="上海分公司",
                contact_person="钱主管",
                contact_phone="13900139002",
                is_active=True
            ),
            # BTSK的机构
            CollectionAgency(
                id=3,
                tenant_id=2,
                agency_code="BTSK_A001",
                agency_name="深圳分公司",
                contact_person="孙主管",
                contact_phone="13900139003",
                is_active=True
            ),
            CollectionAgency(
                id=4,
                tenant_id=2,
                agency_code="BTSK_A002",
                agency_name="广州分公司",
                contact_person="李主管",
                contact_phone="13900139004",
                is_active=True
            ),
            # XYQY的机构
            CollectionAgency(
                id=5,
                tenant_id=3,
                agency_code="XYQY_A001",
                agency_name="成都分公司",
                contact_person="周主管",
                contact_phone="13900139005",
                is_active=True
            )
        ]
        session.add_all(agencies)
        session.commit()
        print(f"✓ 成功插入 {len(agencies)} 个机构")
        
        # 插入小组数据
        print("\n5. 插入小组数据...")
        teams = [
            # 北京分公司的小组
            CollectionTeam(
                id=1,
                tenant_id=1,
                agency_id=1,
                team_code="BTQ_T001",
                team_name="北京一组",
                team_leader_id=None,
                is_active=True
            ),
            CollectionTeam(
                id=2,
                tenant_id=1,
                agency_id=1,
                team_code="BTQ_T002",
                team_name="北京二组",
                team_leader_id=None,
                is_active=True
            ),
            # 上海分公司的小组
            CollectionTeam(
                id=3,
                tenant_id=1,
                agency_id=2,
                team_code="BTQ_T003",
                team_name="上海一组",
                team_leader_id=None,
                is_active=True
            ),
            # 深圳分公司的小组
            CollectionTeam(
                id=4,
                tenant_id=2,
                agency_id=3,
                team_code="BTSK_T001",
                team_name="深圳一组",
                team_leader_id=None,
                is_active=True
            ),
            CollectionTeam(
                id=5,
                tenant_id=2,
                agency_id=3,
                team_code="BTSK_T002",
                team_name="深圳二组",
                team_leader_id=None,
                is_active=True
            ),
            # 广州分公司的小组
            CollectionTeam(
                id=6,
                tenant_id=2,
                agency_id=4,
                team_code="BTSK_T003",
                team_name="广州一组",
                team_leader_id=None,
                is_active=True
            ),
            # 成都分公司的小组
            CollectionTeam(
                id=7,
                tenant_id=3,
                agency_id=5,
                team_code="XYQY_T001",
                team_name="成都一组",
                team_leader_id=None,
                is_active=True
            )
        ]
        session.add_all(teams)
        session.commit()
        print(f"✓ 成功插入 {len(teams)} 个小组")
        
        # 插入催员数据
        print("\n6. 插入催员数据...")
        # 默认密码: 123456 的哈希值
        default_password_hash = "$2b$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYzS7sFxD7u"
        
        collectors = [
            # 北京一组
            Collector(
                id=1,
                tenant_id=1,
                agency_id=1,
                team_id=1,
                collector_code="C001",
                collector_name="张三",
                login_id="zhangsan",
                password_hash=default_password_hash,
                mobile="13700137001",
                email="zhangsan@btq.com",
                is_active=True
            ),
            Collector(
                id=2,
                tenant_id=1,
                agency_id=1,
                team_id=1,
                collector_code="C002",
                collector_name="李四",
                login_id="lisi",
                password_hash=default_password_hash,
                mobile="13700137002",
                email="lisi@btq.com",
                is_active=True
            ),
            # 北京二组
            Collector(
                id=3,
                tenant_id=1,
                agency_id=1,
                team_id=2,
                collector_code="C003",
                collector_name="王五",
                login_id="wangwu",
                password_hash=default_password_hash,
                mobile="13700137003",
                email="wangwu@btq.com",
                is_active=True
            ),
            # 上海一组
            Collector(
                id=4,
                tenant_id=1,
                agency_id=2,
                team_id=3,
                collector_code="C004",
                collector_name="赵六",
                login_id="zhaoliu",
                password_hash=default_password_hash,
                mobile="13700137004",
                email="zhaoliu@btq.com",
                is_active=True
            ),
            # 深圳一组
            Collector(
                id=5,
                tenant_id=2,
                agency_id=3,
                team_id=4,
                collector_code="C005",
                collector_name="孙七",
                login_id="sunqi",
                password_hash=default_password_hash,
                mobile="13700137005",
                email="sunqi@btsk.com",
                is_active=True
            ),
            Collector(
                id=6,
                tenant_id=2,
                agency_id=3,
                team_id=4,
                collector_code="C006",
                collector_name="周八",
                login_id="zhouba",
                password_hash=default_password_hash,
                mobile="13700137006",
                email="zhouba@btsk.com",
                is_active=True
            ),
            # 深圳二组
            Collector(
                id=7,
                tenant_id=2,
                agency_id=3,
                team_id=5,
                collector_code="C007",
                collector_name="吴九",
                login_id="wujiu",
                password_hash=default_password_hash,
                mobile="13700137007",
                email="wujiu@btsk.com",
                is_active=True
            ),
            # 广州一组
            Collector(
                id=8,
                tenant_id=2,
                agency_id=4,
                team_id=6,
                collector_code="C008",
                collector_name="郑十",
                login_id="zhengshi",
                password_hash=default_password_hash,
                mobile="13700137008",
                email="zhengshi@btsk.com",
                is_active=True
            ),
            # 成都一组
            Collector(
                id=9,
                tenant_id=3,
                agency_id=5,
                team_id=7,
                collector_code="C009",
                collector_name="冯十一",
                login_id="fengshiyi",
                password_hash=default_password_hash,
                mobile="13700137009",
                email="fengshiyi@xyqy.com",
                is_active=True
            ),
            Collector(
                id=10,
                tenant_id=3,
                agency_id=5,
                team_id=7,
                collector_code="C010",
                collector_name="陈十二",
                login_id="chenshier",
                password_hash=default_password_hash,
                mobile="13700137010",
                email="chenshier@xyqy.com",
                is_active=True
            )
        ]
        session.add_all(collectors)
        session.commit()
        print(f"✓ 成功插入 {len(collectors)} 个催员")
        
        # 插入案件队列数据
        print("\n7. 插入案件队列数据...")
        all_queues = []
        for tenant in tenants:
            queues = create_default_queues_for_tenant(session, tenant.id)
            all_queues.extend(queues)
        session.commit()
        print(f"✓ 成功插入 {len(all_queues)} 个队列（每个甲方5个默认队列）")
        
        # 插入案件数据
        print("\n8. 插入案件数据...")
        cases = []
        base_date = datetime.now() - timedelta(days=30)
        
        # 为每个甲方创建10个案件
        for tenant_idx, tenant in enumerate(tenants):
            for i in range(10):
                case_code = f"{tenant.tenant_code}_{datetime.now().strftime('%Y%m%d')}_{i+1:04d}"
                
                # 随机分配机构、小组、催员
                agency = agencies[tenant_idx * 2] if tenant_idx * 2 < len(agencies) else agencies[0]
                team = teams[tenant_idx * 2] if tenant_idx * 2 < len(teams) else teams[0]
                collector = collectors[tenant_idx * 3] if tenant_idx * 3 < len(collectors) else collectors[0]
                
                case = Case(
                    tenant_id=tenant.id,
                    agency_id=agency.id,
                    team_id=team.id,
                    collector_id=collector.id,
                    case_code=case_code,
                    user_id=f"U{tenant_idx * 10 + i + 1:04d}",
                    user_name=f"客户{tenant_idx * 10 + i + 1}",
                    mobile=f"138{tenant_idx:02d}{i:02d}12345",
                    loan_amount=10000 + i * 1000,
                    outstanding_amount=10000 + i * 1000,
                    overdue_days=i * 5,
                    case_status="pending_repayment",
                    due_date=base_date + timedelta(days=i),
                    assigned_at=base_date + timedelta(days=i+1)
                )
                cases.append(case)
        
        session.add_all(cases)
        session.commit()
        print(f"✓ 成功插入 {len(cases)} 个案件")
        
        # 验证数据
        print("\n9. 验证数据...")
        tenant_count = session.query(Tenant).count()
        agency_count = session.query(CollectionAgency).count()
        team_count = session.query(CollectionTeam).count()
        collector_count = session.query(Collector).count()
        queue_count = session.query(CaseQueue).count()
        case_count = session.query(Case).count()
        template_count = session.query(NotificationTemplate).count()
        
        print(f"✓ 甲方数量: {tenant_count}")
        print(f"✓ 机构数量: {agency_count}")
        print(f"✓ 小组数量: {team_count}")
        print(f"✓ 催员数量: {collector_count}")
        print(f"✓ 队列数量: {queue_count}")
        print(f"✓ 案件数量: {case_count}")
        print(f"✓ 通知模板数量: {template_count}")
        
        session.close()
        
        print("\n" + "=" * 80)
        print("✓ 所有测试数据初始化完成!")
        print("=" * 80)
        
        return True
        
    except Exception as e:
        print(f"\n✗ 初始化失败: {e}")
        import traceback
        traceback.print_exc()
        return False

if __name__ == "__main__":
    success = init_all_data()
    sys.exit(0 if success else 1)

