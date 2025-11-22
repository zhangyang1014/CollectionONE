#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
创建还款渠道和还款码相关表（SQLite版本）
"""
import sys
import os
from pathlib import Path

# 添加项目根目录到Python路径
project_root = Path(__file__).parent
sys.path.insert(0, str(project_root))

from sqlalchemy import create_engine, text
from app.core.database import Base, engine
from app.models.payment_channel import PaymentChannel
from app.models.payment_code import PaymentCode

def create_tables():
    """创建表"""
    print("开始创建还款相关表...")
    
    try:
        # 创建所有表
        Base.metadata.create_all(bind=engine)
        print("✅ 表创建成功！")
        
        # 验证表是否创建成功
        with engine.connect() as conn:
            # 检查payment_channels表
            result = conn.execute(text("SELECT name FROM sqlite_master WHERE type='table' AND name='payment_channels'"))
            if result.fetchone():
                print("✅ payment_channels 表已创建")
            
            # 检查payment_codes表
            result = conn.execute(text("SELECT name FROM sqlite_master WHERE type='table' AND name='payment_codes'"))
            if result.fetchone():
                print("✅ payment_codes 表已创建")
        
        return True
    except Exception as e:
        print(f"❌ 创建表失败: {str(e)}")
        import traceback
        traceback.print_exc()
        return False

def insert_test_data():
    """插入测试数据"""
    from sqlalchemy.orm import Session
    
    print("\n开始插入测试数据...")
    
    try:
        session = Session(bind=engine)
        
        # 检查是否已有数据
        existing = session.query(PaymentChannel).filter(PaymentChannel.party_id == 1).count()
        if existing > 0:
            print(f"⚠️  甲方1已有 {existing} 条渠道数据，跳过插入")
            session.close()
            return True
        
        # 测试渠道数据
        test_channels = [
            {
                "party_id": 1,
                "channel_name": "GCash",
                "channel_icon": "https://upload.wikimedia.org/wikipedia/commons/thumb/5/5c/GCash_logo.svg/200px-GCash_logo.svg.png",
                "channel_type": "VA",
                "service_provider": "Xendit",
                "description": "菲律宾最流行的电子钱包支付方式",
                "api_url": "https://api.example.com/payment/gcash/create",
                "api_method": "POST",
                "auth_type": "API_KEY",
                "auth_config": {"api_key": "test_api_key_encrypted"},
                "request_params": {
                    "loan_id": "{loan_id}",
                    "case_id": "{case_id}",
                    "installment_number": "{installment_number}",
                    "amount": "{amount}",
                    "customer_name": "{customer_name}",
                    "customer_phone": "{customer_phone}"
                },
                "is_enabled": True,
                "sort_order": 1
            },
            {
                "party_id": 1,
                "channel_name": "BCA Virtual Account",
                "channel_icon": "https://upload.wikimedia.org/wikipedia/commons/thumb/5/5c/Bank_Central_Asia.svg/200px-Bank_Central_Asia.svg.png",
                "channel_type": "VA",
                "service_provider": "Midtrans",
                "description": "印尼BCA银行虚拟账户",
                "api_url": "https://api.example.com/payment/bca-va/create",
                "api_method": "POST",
                "auth_type": "BEARER",
                "auth_config": {"token": "test_bearer_token_encrypted"},
                "request_params": {
                    "loan_id": "{loan_id}",
                    "case_id": "{case_id}",
                    "installment_number": "{installment_number}",
                    "amount": "{amount}",
                    "customer_id": "{customer_id}"
                },
                "is_enabled": True,
                "sort_order": 2
            },
            {
                "party_id": 1,
                "channel_name": "OXXO Pay",
                "channel_icon": "https://upload.wikimedia.org/wikipedia/commons/thumb/c/ce/Oxxo_Logo.svg/200px-Oxxo_Logo.svg.png",
                "channel_type": "H5",
                "service_provider": "Conekta",
                "description": "墨西哥便利店现金支付",
                "api_url": "https://api.example.com/payment/oxxo/create",
                "api_method": "POST",
                "auth_type": "API_KEY",
                "auth_config": {"api_key": "test_api_key_encrypted"},
                "request_params": {
                    "loan_id": "{loan_id}",
                    "amount": "{amount}",
                    "customer_name": "{customer_name}"
                },
                "is_enabled": True,
                "sort_order": 3
            },
            {
                "party_id": 1,
                "channel_name": "QRIS",
                "channel_icon": "https://qris.id/homepage/assets/images/logo-qris.png",
                "channel_type": "QR",
                "service_provider": "Xendit",
                "description": "印尼统一二维码支付",
                "api_url": "https://api.example.com/payment/qris/create",
                "api_method": "POST",
                "auth_type": "API_KEY",
                "auth_config": {"api_key": "test_api_key_encrypted"},
                "request_params": {
                    "loan_id": "{loan_id}",
                    "case_id": "{case_id}",
                    "amount": "{amount}"
                },
                "is_enabled": True,
                "sort_order": 4
            }
        ]
        
        # 插入数据
        for channel_data in test_channels:
            channel = PaymentChannel(**channel_data)
            session.add(channel)
        
        session.commit()
        print(f"✅ 成功插入 {len(test_channels)} 条测试渠道数据")
        
        session.close()
        return True
        
    except Exception as e:
        print(f"❌ 插入测试数据失败: {str(e)}")
        import traceback
        traceback.print_exc()
        return False

def main():
    """主函数"""
    print("=" * 60)
    print("还款渠道和还款码表初始化脚本")
    print("=" * 60)
    
    # 1. 创建表
    if not create_tables():
        print("\n❌ 初始化失败！")
        return 1
    
    # 2. 插入测试数据
    if not insert_test_data():
        print("\n❌ 插入测试数据失败！")
        return 1
    
    print("\n" + "=" * 60)
    print("✅ 初始化完成！")
    print("=" * 60)
    print("\n可以开始使用还款码功能了！")
    print("- 管理控台：配置更多渠道")
    print("- IM端：为案件请求还款码")
    
    return 0

if __name__ == "__main__":
    sys.exit(main())

