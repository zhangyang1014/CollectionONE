"""
初始化IM端完整测试数据
为BTQ001、BTQ002、BTQ003、BTSK001、BTSK002、BTSK003创建完整的测试数据
包括：案件、联系人、沟通记录、PTP记录等
"""
from datetime import datetime, timedelta
from app.core.database import SessionLocal
from app.models.case import Case
from app.models.collector import Collector
from app.models.case_queue import CaseQueue
from app.models.case_contact import CaseContact
from app.models.communication_record import CommunicationRecord
from app.models.ptp_record import PTPRecord
import random


def create_im_test_data():
    """创建IM端测试数据"""
    db = SessionLocal()
    
    try:
        print("=" * 80)
        print("初始化IM端完整测试数据")
        print("=" * 80)
        
        # 1. 获取IM端催员
        collectors = db.query(Collector).filter(
            Collector.login_id.in_(['BTQ001', 'BTQ002', 'BTQ003', 'BTSK001', 'BTSK002', 'BTSK003'])
        ).all()
        
        if not collectors:
            print("❌ 错误：未找到IM端催员，请先运行 create_im_collectors.py")
            return
        
        print(f"\n找到 {len(collectors)} 个IM端催员")
        
        # 2. 为每个催员创建案件和相关数据
        total_cases = 0
        total_contacts = 0
        total_communications = 0
        total_ptps = 0
        
        for collector in collectors:
            print(f"\n{'='*80}")
            print(f"处理催员: {collector.login_id} ({collector.collector_name})")
            print(f"  - 甲方ID: {collector.tenant_id}")
            print(f"  - 机构ID: {collector.agency_id}")
            print(f"  - 小组ID: {collector.team_id}")
            
            # 获取该甲方的队列
            queues = db.query(CaseQueue).filter(
                CaseQueue.tenant_id == collector.tenant_id
            ).all()
            
            if not queues:
                print(f"  ⚠️  警告：甲方 {collector.tenant_id} 没有配置队列，跳过")
                continue
            
            # 为每个催员创建10个案件
            cases_count = 10
            print(f"  - 创建 {cases_count} 个案件...")
            
            for i in range(cases_count):
                # 随机选择队列
                queue = random.choice(queues)
                
                # 生成案件编号
                case_code = f"{collector.login_id}_CASE_{i+1:03d}_{datetime.now().strftime('%Y%m%d')}"
                
                # 检查案件是否已存在
                existing_case = db.query(Case).filter(Case.case_code == case_code).first()
                if existing_case:
                    print(f"    案件 {case_code} 已存在，跳过")
                    continue
                
                # 创建案件
                case_status_options = ['pending_repayment', 'partial_repayment', 'normal_settlement']
                overdue_days = random.randint(1, 90)
                loan_amount = random.randint(5000, 50000)
                repaid_amount = random.randint(0, int(loan_amount * 0.8))
                outstanding_amount = loan_amount - repaid_amount
                
                case = Case(
                    case_code=case_code,
                    tenant_id=collector.tenant_id,
                    agency_id=collector.agency_id,
                    team_id=collector.team_id,
                    collector_id=collector.id,
                    queue_id=queue.id,
                    user_id=f"USER_{collector.tenant_id}_{i+1:05d}",
                    user_name=f"用户{i+1:03d}",
                    mobile=f"+52 55 {random.randint(1000, 9999)} {random.randint(1000, 9999)}",
                    case_status=random.choice(case_status_options),
                    overdue_days=overdue_days,
                    loan_amount=loan_amount,
                    repaid_amount=repaid_amount,
                    outstanding_amount=outstanding_amount,
                    due_date=datetime.now() - timedelta(days=overdue_days),
                    assigned_at=datetime.now() - timedelta(days=random.randint(1, 30)),
                    last_contact_at=datetime.now() - timedelta(days=random.randint(0, 7)),
                    next_follow_up_at=datetime.now() + timedelta(days=random.randint(1, 3))
                )
                
                db.add(case)
                db.flush()
                total_cases += 1
                
                # 为案件创建2-3个联系人
                contact_count = random.randint(2, 3)
                contacts = []
                
                for j in range(contact_count):
                    contact_types = ['本人', '配偶', '朋友', '同事', '亲属']
                    contact_type = contact_types[j] if j < len(contact_types) else '其他'
                    
                    available_channels = ['phone', 'sms']
                    if random.random() < 0.6:  # 60%的联系人有WhatsApp
                        available_channels.append('whatsapp')
                    
                    contact = CaseContact(
                        case_id=case.id,
                        contact_name=f"{case.user_name}的{contact_type}",
                        phone_number=f"+52 55 {random.randint(1000, 9999)} {random.randint(1000, 9999)}",
                        relation=contact_type,
                        is_primary=(j == 0),
                        available_channels=available_channels,
                        remark=f"{contact_type}联系方式"
                    )
                    
                    db.add(contact)
                    db.flush()
                    contacts.append(contact)
                    total_contacts += 1
                
                # 为案件创建沟通记录
                communication_count = random.randint(3, 8)
                for k in range(communication_count):
                    contact = random.choice(contacts)
                    
                    channel_types = ['phone', 'sms', 'whatsapp']
                    channel = random.choice(channel_types)
                    
                    result_options = ['answered', 'no_answer', 'busy', 'invalid_number', 'promised_to_pay']
                    result = random.choice(result_options)
                    
                    is_connected = (result == 'answered')
                    call_duration = random.randint(30, 300) if is_connected else 0
                    
                    comm = CommunicationRecord(
                        case_id=case.id,
                        collector_id=collector.id,
                        contact_person_id=contact.id,
                        channel=channel,
                        direction='outbound',
                        contact_result=result,
                        is_connected=is_connected if channel == 'phone' else None,
                        call_duration=call_duration if channel == 'phone' else None,
                        is_replied=(random.random() < 0.5) if channel in ['whatsapp', 'sms'] else None,
                        message_content=f"催收消息{k+1}" if channel in ['whatsapp', 'sms'] else None,
                        remark=f"第{k+1}次联系，{result}",
                        contacted_at=datetime.now() - timedelta(days=random.randint(0, 15))
                    )
                    
                    db.add(comm)
                    total_communications += 1
                
                # 30%的案件创建PTP记录
                if random.random() < 0.3:
                    contact = random.choice(contacts)
                    promise_amount = random.randint(1000, int(outstanding_amount)) if outstanding_amount > 0 else 1000
                    promise_date = datetime.now() + timedelta(days=random.randint(1, 7))
                    
                    ptp_status_options = ['pending', 'kept', 'broken']
                    ptp_status = random.choice(ptp_status_options)
                    
                    # 查找最近的一次沟通记录作为关联
                    recent_comm = db.query(CommunicationRecord).filter(
                        CommunicationRecord.case_id == case.id
                    ).order_by(CommunicationRecord.contacted_at.desc()).first()
                    
                    ptp = PTPRecord(
                        case_id=case.id,
                        collector_id=collector.id,
                        contact_person_id=contact.id,
                        communication_id=recent_comm.id if recent_comm else None,
                        promise_amount=promise_amount,
                        promise_date=promise_date,
                        status=ptp_status,
                        remark=f"承诺还款 {promise_amount}",
                        created_at=datetime.now() - timedelta(days=random.randint(1, 5))
                    )
                    
                    db.add(ptp)
                    total_ptps += 1
            
            # 更新催员的当前案件数
            collector.current_case_count = cases_count
            
            print(f"  ✓ 完成: 创建了 {cases_count} 个案件")
        
        # 提交所有更改
        db.commit()
        
        print("\n" + "=" * 80)
        print("数据初始化完成！")
        print("=" * 80)
        print(f"总计创建:")
        print(f"  - 案件: {total_cases} 个")
        print(f"  - 联系人: {total_contacts} 个")
        print(f"  - 沟通记录: {total_communications} 条")
        print(f"  - PTP记录: {total_ptps} 条")
        print("=" * 80)
        
        # 显示每个催员的案件数
        print("\n各催员案件分配情况:")
        print("-" * 80)
        for collector in collectors:
            case_count = db.query(Case).filter(Case.collector_id == collector.id).count()
            print(f"  {collector.login_id} ({collector.collector_name}): {case_count} 个案件")
        print("-" * 80)
        
    except Exception as e:
        print(f"\n❌ 错误: {str(e)}")
        import traceback
        traceback.print_exc()
        db.rollback()
    finally:
        db.close()


if __name__ == "__main__":
    create_im_test_data()

