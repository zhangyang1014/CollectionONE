"""
为COL1111催员创建或分配20个案件
如果催员不存在，则先创建催员
"""

import sys
import os
from datetime import datetime, timedelta
import random

# 添加项目根目录到路径
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from sqlalchemy.orm import Session
from app.core.database import SessionLocal
from app.core.security import get_password_hash
from app.models.tenant import Tenant
from app.models.collection_agency import CollectionAgency
from app.models.collection_team import CollectionTeam
from app.models.collector import Collector
from app.models.case_queue import CaseQueue
from app.models.case import Case


def create_collector_and_cases():
    """创建或查找COL1111催员，并为其创建20个案件"""
    db: Session = SessionLocal()
    
    try:
        # 1. 查找或创建COL1111催员
        collector = db.query(Collector).filter(
            Collector.collector_code == "COL1111"
        ).first()
        
        if collector:
            # 如果催员存在但登录ID不是COL1111，更新登录ID
            if collector.login_id != "COL1111":
                print(f"发现催员COL1111，但登录ID为 {collector.login_id}，正在更新为 COL1111...")
                collector.login_id = "COL1111"
                db.flush()
        
        if not collector:
            print("催员COL1111不存在，正在创建...")
            
            # 查找第一个可用的甲方、机构、小组
            tenant = db.query(Tenant).filter(Tenant.is_active == True).first()
            if not tenant:
                print("❌ 错误：数据库中没有可用的甲方，请先初始化数据库")
                return
            
            agency = db.query(CollectionAgency).filter(
                CollectionAgency.tenant_id == tenant.id,
                CollectionAgency.is_active == True
            ).first()
            if not agency:
                print("❌ 错误：数据库中没有可用的机构，请先初始化数据库")
                return
            
            team = db.query(CollectionTeam).filter(
                CollectionTeam.tenant_id == tenant.id,
                CollectionTeam.agency_id == agency.id,
                CollectionTeam.is_active == True
            ).first()
            if not team:
                print("❌ 错误：数据库中没有可用的小组，请先初始化数据库")
                return
            
            # 创建催员
            collector = Collector(
                tenant_id=tenant.id,
                agency_id=agency.id,
                team_id=team.id,
                collector_code="COL1111",
                collector_name="催员COL1111",
                login_id="COL1111",  # 登录ID使用催员编码
                password_hash=get_password_hash("123456"),  # 默认密码123456
                mobile="13800001111",
                email="col1111@example.com",
                collector_level="中级",
                max_case_count=100,
                current_case_count=0,
                status="active",
                is_active=True
            )
            db.add(collector)
            db.flush()
            print(f"✅ 已创建催员COL1111 (ID: {collector.id})")
        else:
            print(f"✅ 找到催员COL1111 (ID: {collector.id})")
        
        # 2. 获取催员所属的甲方和队列
        tenant_id = collector.tenant_id
        queues = db.query(CaseQueue).filter(
            CaseQueue.tenant_id == tenant_id
        ).all()
        
        if not queues:
            print("❌ 错误：该甲方没有配置队列，请先初始化队列")
            return
        
        # 3. 创建20个案件并分配给COL1111催员
        print(f"\n正在为催员COL1111创建20个案件...")
        
        case_statuses = ['pending_repayment', 'partial_repayment', 'normal_settlement', 'extension_settlement']
        created_count = 0
        
        # 获取当前最大的案件编号（避免重复）
        existing_cases = db.query(Case).filter(
            Case.tenant_id == tenant_id
        ).all()
        max_case_num = 0
        for case in existing_cases:
            if case.case_code and case.case_code.startswith("CASE"):
                try:
                    num = int(case.case_code.replace("CASE", ""))
                    max_case_num = max(max_case_num, num)
                except:
                    pass
        
        for i in range(1, 21):
            # 生成唯一的案件编号
            case_num = max_case_num + i
            case_code = f"CASE{case_num:06d}"
            
            # 检查案件编号是否已存在
            existing = db.query(Case).filter(Case.case_code == case_code).first()
            if existing:
                case_code = f"CASE{case_num:06d}_{i}"
            
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
            
            # 随机案件状态
            case_status = random.choice(case_statuses)
            
            # 生成案件信息
            loan_amount = round(random.uniform(1000, 100000), 2)
            repaid_amount = 0 if case_status == 'pending_repayment' else round(random.uniform(0, loan_amount), 2)
            if case_status in ['normal_settlement', 'extension_settlement']:
                repaid_amount = loan_amount
            outstanding_amount = round(loan_amount - repaid_amount, 2)
            
            # 生成时间
            due_date = datetime.now() - timedelta(days=overdue_days)
            settlement_date = None
            if case_status in ['normal_settlement', 'extension_settlement']:
                settlement_date = datetime.now() - timedelta(days=random.randint(0, 30))
            
            case = Case(
                tenant_id=collector.tenant_id,
                queue_id=queue.id,
                agency_id=collector.agency_id,
                team_id=collector.team_id,
                collector_id=collector.id,
                case_code=case_code,
                user_id=f"USER{case_num:06d}",
                user_name=f"客户{case_num:06d}",
                mobile=f"138{case_num:08d}",
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
            db.add(case)
            created_count += 1
        
        # 更新催员的当前案件数量
        collector.current_case_count = db.query(Case).filter(
            Case.collector_id == collector.id
        ).count()
        
        db.commit()
        
        print(f"✅ 已成功创建 {created_count} 个案件并分配给催员COL1111")
        print(f"✅ 催员当前案件数量: {collector.current_case_count}")
        
        # 4. 输出登录信息
        print("\n" + "="*60)
        print("催员登录信息")
        print("="*60)
        print(f"催员编码: {collector.collector_code}")
        print(f"催员姓名: {collector.collector_name}")
        print(f"登录ID: {collector.login_id}")
        print(f"密码: 123456")
        print(f"所属甲方ID: {collector.tenant_id}")
        print(f"所属机构ID: {collector.agency_id}")
        print(f"所属小组ID: {collector.team_id}")
        print("="*60)
        print("\n💡 提示：")
        print("   - IM端登录地址: http://localhost:5173/im/login")
        print("   - 登录时使用: 机构ID={}, 催员ID={}, 密码=123456".format(
            collector.tenant_id, collector.login_id
        ))
        
    except Exception as e:
        db.rollback()
        print(f"❌ 错误: {str(e)}")
        import traceback
        traceback.print_exc()
    finally:
        db.close()


if __name__ == "__main__":
    create_collector_and_cases()

