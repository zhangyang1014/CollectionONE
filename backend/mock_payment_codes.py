#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
创建还款码Mock数据
"""
import sys
import os
from pathlib import Path
from datetime import datetime, timedelta
import random
import uuid

# 添加项目根目录到Python路径
project_root = Path(__file__).parent
sys.path.insert(0, str(project_root))

from sqlalchemy.orm import Session
from app.core.database import engine
from app.models.payment_channel import PaymentChannel
from app.models.payment_code import PaymentCode
from app.models.case import Case

def generate_payment_code_no():
    """生成还款码编号"""
    return f"PAY{datetime.now().strftime('%Y%m%d')}{str(uuid.uuid4())[:8].upper()}"

def generate_va_code():
    """生成VA码"""
    return f"8808{random.randint(100000000000, 999999999999)}"

def generate_h5_link():
    """生成H5链接"""
    code = str(uuid.uuid4())[:12]
    return f"https://payment.example.com/pay/{code}"

def generate_qr_url():
    """生成二维码图片URL"""
    code = str(uuid.uuid4())[:16]
    return f"https://payment.example.com/qr/{code}.png"

def create_mock_payment_codes():
    """创建Mock还款码数据"""
    print("开始创建Mock还款码数据...")
    
    session = Session(bind=engine)
    
    try:
        # 获取渠道列表
        channels = session.query(PaymentChannel).filter(
            PaymentChannel.party_id == 1,
            PaymentChannel.is_enabled == True
        ).all()
        
        if not channels:
            print("⚠️  没有可用的支付渠道，请先运行 create_payment_tables.py")
            return False
        
        print(f"找到 {len(channels)} 个可用渠道")
        
        # 获取案件列表（前10个）
        cases = session.query(Case).limit(10).all()
        
        if not cases:
            print("⚠️  没有可用的案件数据")
            return False
        
        print(f"找到 {len(cases)} 个案件")
        
        # 检查已有数据
        existing_count = session.query(PaymentCode).count()
        if existing_count > 0:
            print(f"⚠️  已存在 {existing_count} 条还款码数据")
            response = input("是否清除现有数据并重新生成？(y/n): ")
            if response.lower() == 'y':
                session.query(PaymentCode).delete()
                session.commit()
                print("✅ 已清除现有数据")
            else:
                print("取消操作")
                return False
        
        # 为每个案件生成1-3条还款码记录
        mock_codes = []
        
        for case in cases:
            num_codes = random.randint(1, 3)
            
            for i in range(num_codes):
                channel = random.choice(channels)
                
                # 随机状态
                status_list = ['PENDING', 'PAID', 'EXPIRED']
                weights = [0.5, 0.3, 0.2]  # 50%待支付，30%已支付，20%已过期
                status = random.choices(status_list, weights=weights)[0]
                
                # 生成还款码内容
                payment_code = None
                qr_image_url = None
                
                if channel.channel_type == 'VA':
                    payment_code = generate_va_code()
                elif channel.channel_type == 'H5':
                    payment_code = generate_h5_link()
                elif channel.channel_type == 'QR':
                    payment_code = generate_va_code()  # QR码也需要一个code
                    qr_image_url = generate_qr_url()
                
                # 生成时间
                days_ago = random.randint(0, 30)
                created_at = datetime.now() - timedelta(days=days_ago)
                
                # 过期时间
                if status == 'EXPIRED':
                    expired_at = created_at + timedelta(hours=random.randint(1, 24))
                else:
                    expired_at = created_at + timedelta(hours=24)
                
                # 支付时间
                paid_at = None
                if status == 'PAID':
                    paid_at = created_at + timedelta(hours=random.randint(1, 20))
                
                # 随机金额（1000-10000）
                amount = round(random.uniform(1000, 10000), 2)
                
                # 随机期数（1-12）
                installment_number = random.randint(1, 12) if random.random() > 0.3 else None
                
                # 第三方订单ID
                third_party_order_id = f"TPO{datetime.now().strftime('%Y%m%d%H%M%S')}{random.randint(1000, 9999)}"
                
                # 第三方响应
                third_party_response = {
                    "status": "success",
                    "order_id": third_party_order_id,
                    "payment_code": payment_code,
                    "expired_at": expired_at.isoformat()
                }
                
                if channel.channel_type == 'QR':
                    third_party_response["qr_image_url"] = qr_image_url
                
                # 请求参数
                request_params = {
                    "loan_id": case.loan_id,
                    "case_id": case.id,
                    "amount": amount
                }
                if installment_number:
                    request_params["installment_number"] = installment_number
                
                # 创建还款码记录
                payment_code_record = PaymentCode(
                    code_no=generate_payment_code_no(),
                    party_id=channel.party_id,
                    channel_id=channel.id,
                    case_id=case.id,
                    loan_id=case.loan_id,
                    customer_id=case.user_id,
                    collector_id=1,  # 默认催员ID
                    installment_number=installment_number,
                    amount=amount,
                    currency="IDR",
                    payment_type=channel.channel_type,
                    payment_code=payment_code,
                    qr_image_url=qr_image_url,
                    status=status,
                    created_at=created_at,
                    expired_at=expired_at,
                    paid_at=paid_at,
                    third_party_order_id=third_party_order_id,
                    third_party_response=third_party_response,
                    request_params=request_params
                )
                
                mock_codes.append(payment_code_record)
        
        # 批量插入
        session.add_all(mock_codes)
        session.commit()
        
        print(f"✅ 成功创建 {len(mock_codes)} 条Mock还款码数据")
        
        # 统计信息
        pending_count = sum(1 for code in mock_codes if code.status == 'PENDING')
        paid_count = sum(1 for code in mock_codes if code.status == 'PAID')
        expired_count = sum(1 for code in mock_codes if code.status == 'EXPIRED')
        
        print("\n统计信息:")
        print(f"  待支付: {pending_count}")
        print(f"  已支付: {paid_count}")
        print(f"  已过期: {expired_count}")
        
        return True
        
    except Exception as e:
        session.rollback()
        print(f"❌ 创建Mock数据失败: {str(e)}")
        import traceback
        traceback.print_exc()
        return False
    finally:
        session.close()

def main():
    """主函数"""
    print("=" * 60)
    print("还款码Mock数据生成脚本")
    print("=" * 60)
    
    if create_mock_payment_codes():
        print("\n" + "=" * 60)
        print("✅ Mock数据生成完成！")
        print("=" * 60)
        print("\n现在可以在IM端查看还款码列表了！")
        return 0
    else:
        print("\n❌ Mock数据生成失败！")
        return 1

if __name__ == "__main__":
    sys.exit(main())

