"""生成数据看板测试数据的脚本"""
import sys
import random
from pathlib import Path
from datetime import datetime, timedelta, date
from decimal import Decimal

# 添加项目根目录到Python路径
sys.path.append(str(Path(__file__).parent))

from sqlalchemy.orm import Session
from app.core.database import engine, SessionLocal
from app.models import (
    Collector,
    Case,
    CaseContact,
    CommunicationRecord,
    PTPRecord,
    QualityInspectionRecord,
)

def generate_test_data():
    """生成测试数据"""
    db: Session = SessionLocal()
    
    try:
        print("开始生成数据看板测试数据...")
        
        # 获取所有催员
        collectors = db.query(Collector).limit(5).all()
        if not collectors:
            print("错误：数据库中没有催员数据，请先运行 generate_mock_data.py")
            return
        
        # 获取所有案件
        cases = db.query(Case).limit(50).all()
        if not cases:
            print("错误：数据库中没有案件数据，请先运行 generate_mock_cases.py")
            return
        
        print(f"找到 {len(collectors)} 个催员，{len(cases)} 个案件")
        
        # 生成联系人数据
        print("\n1. 生成联系人数据...")
        contact_count = 0
        for case in cases[:30]:  # 只为前30个案件生成联系人
            # 为每个案件生成1-3个联系人
            for i in range(random.randint(1, 3)):
                contact = CaseContact(
                    case_id=case.id,
                    contact_name=f"联系人{i+1}",
                    phone_number=f"+91 98 {random.randint(1000, 9999)} {random.randint(1000, 9999)}",
                    relation="本人" if i == 0 else random.choice(["配偶", "朋友", "家人", "同事"]),
                    is_primary=(i == 0),
                    available_channels=["whatsapp", "sms", "call"] if i == 0 else random.sample(["whatsapp", "sms", "call"], 2)
                )
                db.add(contact)
                contact_count += 1
        
        db.commit()
        print(f"   ✅ 生成了 {contact_count} 条联系人记录")
        
        # 生成通信记录数据
        print("\n2. 生成通信记录数据...")
        comm_count = 0
        for case in cases[:30]:
            # 为每个案件生成5-15条通信记录
            contacts = db.query(CaseContact).filter(CaseContact.case_id == case.id).all()
            if not contacts:
                continue
            
            for _ in range(random.randint(5, 15)):
                contacted_at = datetime.now() - timedelta(days=random.randint(0, 30), hours=random.randint(0, 23))
                channel = random.choice(["phone", "whatsapp", "sms"])
                is_connected = random.choice([True, False])
                
                comm = CommunicationRecord(
                    case_id=case.id,
                    collector_id=case.collector_id or collectors[0].id,
                    contact_person_id=random.choice(contacts).id,
                    channel=channel,
                    direction=random.choice(["inbound", "outbound"]),
                    call_duration=random.randint(30, 300) if channel == "phone" and is_connected else None,
                    is_connected=is_connected if channel == "phone" else None,
                    is_replied=random.choice([True, False]) if channel in ["whatsapp", "sms"] else None,
                    message_content="测试消息内容" if channel in ["whatsapp", "sms"] else None,
                    contact_result=random.choice(["connected", "not_connected", "replied", "not_replied", "promise_to_pay"]),
                    ttfc_seconds=random.randint(3600, 86400) if comm_count % 10 == 0 else None,  # 只为部分记录生成TTFC
                    contacted_at=contacted_at
                )
                db.add(comm)
                comm_count += 1
        
        db.commit()
        print(f"   ✅ 生成了 {comm_count} 条通信记录")
        
        # 生成PTP记录数据
        print("\n3. 生成PTP记录数据...")
        ptp_count = 0
        for case in cases[:20]:
            # 为部分案件生成1-2条PTP记录
            if random.random() < 0.6:  # 60%的案件有PTP
                for _ in range(random.randint(1, 2)):
                    ptp_date = date.today() + timedelta(days=random.randint(-10, 10))
                    ptp_amount = Decimal(str(random.randint(1000, 10000)))
                    
                    # 随机生成PTP状态
                    status = random.choice(["pending", "fulfilled", "broken", "cancelled"])
                    actual_amount = None
                    actual_date = None
                    fulfillment_rate = None
                    fulfilled_at = None
                    
                    if status == "fulfilled":
                        actual_amount = ptp_amount * Decimal(str(random.uniform(0.8, 1.0)))
                        actual_date = ptp_date + timedelta(days=random.randint(0, 3))
                        fulfillment_rate = (actual_amount / ptp_amount) * 100
                        fulfilled_at = datetime.now() - timedelta(days=random.randint(0, 5))
                    
                    ptp = PTPRecord(
                        case_id=case.id,
                        collector_id=case.collector_id or collectors[0].id,
                        ptp_amount=ptp_amount,
                        ptp_date=ptp_date,
                        status=status,
                        actual_payment_amount=actual_amount,
                        actual_payment_date=actual_date,
                        fulfillment_rate=fulfillment_rate,
                        fulfilled_at=fulfilled_at
                    )
                    db.add(ptp)
                    ptp_count += 1
        
        db.commit()
        print(f"   ✅ 生成了 {ptp_count} 条PTP记录")
        
        # 生成质检记录数据
        print("\n4. 生成质检记录数据...")
        quality_count = 0
        
        # 获取有通信记录的案件
        comm_records = db.query(CommunicationRecord).limit(20).all()
        
        for comm in comm_records:
            # 30%的通信记录有质检
            if random.random() < 0.3:
                inspection_type = random.choice(["manual", "ai"])
                quality_score = random.randint(60, 100)
                script_compliance_rate = random.randint(50, 100)
                
                # 生成违规项
                violations = []
                if quality_score < 80:
                    violation_types = ["high_risk", "general"]
                    for _ in range(random.randint(1, 3)):
                        violations.append({
                            "type": random.choice(violation_types),
                            "item": random.choice(["语言不当", "未按流程操作", "信息记录不完整", "态度问题"]),
                            "description": "违规说明"
                        })
                
                quality = QualityInspectionRecord(
                    case_id=comm.case_id,
                    collector_id=comm.collector_id,
                    communication_id=comm.id,
                    inspector_id=random.choice(collectors).id if inspection_type == "manual" else None,
                    inspection_type=inspection_type,
                    quality_score=quality_score,
                    script_compliance_rate=script_compliance_rate,
                    violations=violations if violations else None,
                    compliant_items=[{"item": "礼貌用语", "description": "表现良好"}],
                    feedback="整体表现" + ("良好" if quality_score >= 80 else "需要改进"),
                    inspected_at=comm.contacted_at + timedelta(hours=random.randint(1, 24))
                )
                db.add(quality)
                quality_count += 1
        
        db.commit()
        print(f"   ✅ 生成了 {quality_count} 条质检记录")
        
        print("\n" + "="*60)
        print("✅ 数据看板测试数据生成完成！")
        print("="*60)
        print(f"\n总计生成：")
        print(f"  - 联系人记录：{contact_count} 条")
        print(f"  - 通信记录：{comm_count} 条")
        print(f"  - PTP记录：{ptp_count} 条")
        print(f"  - 质检记录：{quality_count} 条")
        print(f"\n您现在可以访问催员业绩看板页面查看数据了！")
        
    except Exception as e:
        db.rollback()
        print(f"\n❌ 生成测试数据时出错: {e}")
        raise
    finally:
        db.close()

if __name__ == "__main__":
    generate_test_data()

