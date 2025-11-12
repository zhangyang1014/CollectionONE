"""
添加渠道供应商mock数据
"""
import sys
import os

# 添加项目根目录到路径
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from app.core.config import settings
from app.models.channel_supplier import ChannelSupplier, ChannelTypeEnum
from app.models.tenant import Tenant

def main():
    """添加渠道供应商数据"""
    print("="*60)
    print("添加渠道供应商mock数据")
    print("="*60)
    
    # 创建数据库引擎
    engine = create_engine(settings.DATABASE_URL)
    SessionLocal = sessionmaker(bind=engine)
    session = SessionLocal()
    
    try:
        # 获取所有甲方
        tenants = session.query(Tenant).filter(Tenant.is_active == True).all()
        
        print(f"找到 {len(tenants)} 个活跃甲方")
        
        # 为每个甲方和每个渠道类型创建供应商
        suppliers_data = {
            'sms': [
                {
                    'supplier_name': '阿里云短信',
                    'api_url': 'https://dysmsapi.aliyuncs.com',
                    'api_key': 'LTAI5txxxxxxxxxxxxx',
                    'secret_key': 'xxxxxxxxxxxxxxxxxxxxxxxxxxxxx',
                    'remark': '阿里云短信服务提供商',
                    'sort_order': 0
                },
                {
                    'supplier_name': '腾讯云短信',
                    'api_url': 'https://sms.tencentcloudapi.com',
                    'api_key': 'AKIDxxxxxxxxxxxxxxxxxxxxx',
                    'secret_key': 'xxxxxxxxxxxxxxxxxxxxxxxxxxxxx',
                    'remark': '腾讯云短信服务提供商',
                    'sort_order': 1
                }
            ],
            'rcs': [
                {
                    'supplier_name': '华为RCS',
                    'api_url': 'https://rcs-api.huawei.com',
                    'api_key': 'HUAWEIxxxxxxxxxxxxx',
                    'secret_key': 'xxxxxxxxxxxxxxxxxxxxxxxxxxxxx',
                    'remark': '华为RCS消息服务',
                    'sort_order': 0
                }
            ],
            'whatsapp': [
                {
                    'supplier_name': 'Twilio WhatsApp',
                    'api_url': 'https://api.twilio.com',
                    'api_key': 'ACxxxxxxxxxxxxxxxxxxxxxxxxxxxxx',
                    'secret_key': 'xxxxxxxxxxxxxxxxxxxxxxxxxxxxx',
                    'remark': 'Twilio WhatsApp Business API',
                    'sort_order': 0
                },
                {
                    'supplier_name': 'Meta WhatsApp',
                    'api_url': 'https://graph.facebook.com',
                    'api_key': 'EAAGxxxxxxxxxxxxxxxxxxxxx',
                    'secret_key': 'xxxxxxxxxxxxxxxxxxxxxxxxxxxxx',
                    'remark': 'Meta WhatsApp Business API',
                    'sort_order': 1
                }
            ],
            'call': [
                {
                    'supplier_name': '阿里云语音',
                    'api_url': 'https://dyvmsapi.aliyuncs.com',
                    'api_key': 'LTAI5txxxxxxxxxxxxx',
                    'secret_key': 'xxxxxxxxxxxxxxxxxxxxxxxxxxxxx',
                    'remark': '阿里云语音外呼服务',
                    'sort_order': 0
                },
                {
                    'supplier_name': '腾讯云语音',
                    'api_url': 'https://cloud.tencent.com/api',
                    'api_key': 'AKIDxxxxxxxxxxxxxxxxxxxxx',
                    'secret_key': 'xxxxxxxxxxxxxxxxxxxxxxxxxxxxx',
                    'remark': '腾讯云语音外呼服务',
                    'sort_order': 1
                }
            ]
        }
        
        total_count = 0
        
        for tenant in tenants:
            print(f"\n为 {tenant.tenant_name} (ID: {tenant.id}) 创建供应商...")
            
            for channel_type, suppliers in suppliers_data.items():
                for supplier_data in suppliers:
                    # 检查是否已存在
                    existing = session.query(ChannelSupplier).filter(
                        ChannelSupplier.tenant_id == tenant.id,
                        ChannelSupplier.channel_type == channel_type,
                        ChannelSupplier.supplier_name == supplier_data['supplier_name']
                    ).first()
                    
                    if existing:
                        print(f"  ⚠️  {channel_type.upper()} - {supplier_data['supplier_name']} 已存在，跳过")
                        continue
                    
                    supplier = ChannelSupplier(
                        tenant_id=tenant.id,
                        channel_type=channel_type,
                        supplier_name=supplier_data['supplier_name'],
                        api_url=supplier_data['api_url'],
                        api_key=supplier_data['api_key'],
                        secret_key=supplier_data['secret_key'],
                        remark=supplier_data['remark'],
                        sort_order=supplier_data['sort_order'],
                        is_active=True
                    )
                    session.add(supplier)
                    total_count += 1
                    print(f"  ✅ {channel_type.upper()} - {supplier_data['supplier_name']}")
        
        session.commit()
        
        # 显示所有供应商
        all_suppliers = session.query(ChannelSupplier).all()
        print(f"\n当前渠道供应商总数: {len(all_suppliers)}")
        
        # 按甲方和渠道类型分组显示
        for tenant in tenants:
            print(f"\n{tenant.tenant_name}:")
            for channel_type in ['sms', 'rcs', 'whatsapp', 'call']:
                suppliers = session.query(ChannelSupplier).filter(
                    ChannelSupplier.tenant_id == tenant.id,
                    ChannelSupplier.channel_type == channel_type
                ).all()
                if suppliers:
                    print(f"  {channel_type.upper()}: {len(suppliers)} 个")
                    for s in suppliers:
                        print(f"    - {s.supplier_name}")
        
        print("\n" + "="*60)
        print("✅ 完成！")
        print("="*60)
        
    except Exception as e:
        print(f"\n❌ 添加失败: {e}")
        import traceback
        traceback.print_exc()
        session.rollback()
    finally:
        session.close()


if __name__ == "__main__":
    main()

