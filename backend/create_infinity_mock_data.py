#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
创建 Infinity 外呼系统的Mock数据
"""
import sys
import os
sys.path.insert(0, os.path.dirname(__file__))

from app.core.database import engine, SessionLocal
from app.models.infinity_call_config import InfinityCallConfig
from app.models.infinity_extension_pool import InfinityExtensionPool, ExtensionStatusEnum
from app.models.tenant import Tenant
from sqlalchemy import text

def create_mock_data():
    """创建Infinity外呼系统的Mock数据"""
    db = SessionLocal()
    
    try:
        print("=" * 60)
        print("开始创建 Infinity 外呼系统 Mock 数据")
        print("=" * 60)
        
        # 1. 检查是否有甲方数据
        tenant = db.query(Tenant).first()
        if not tenant:
            print("❌ 错误：系统中没有甲方数据，请先创建甲方")
            return
        
        print(f"\n✅ 找到甲方: {tenant.tenant_name} (ID: {tenant.id})")
        
        # 2. 检查是否已存在配置
        existing_config = db.query(InfinityCallConfig).filter(
            InfinityCallConfig.tenant_id == tenant.id
        ).first()
        
        if existing_config:
            print(f"\n⚠️  甲方 {tenant.tenant_name} 已存在 Infinity 配置 (ID: {existing_config.id})")
            print("是否删除现有配置并重新创建? (y/n): ", end="")
            choice = input().lower()
            if choice == 'y':
                # 删除相关分机池
                db.query(InfinityExtensionPool).filter(
                    InfinityExtensionPool.config_id == existing_config.id
                ).delete()
                db.delete(existing_config)
                db.commit()
                print("✅ 已删除旧配置")
            else:
                print("取消操作")
                return
        
        # 3. 创建 Infinity 配置
        print("\n" + "=" * 60)
        print("创建 Infinity 外呼配置")
        print("=" * 60)
        
        infinity_config = InfinityCallConfig(
            tenant_id=tenant.id,
            supplier_id=None,
            api_url="http://127.0.0.1:8080",
            access_token="test_token_123456",
            app_id="btq",
            caller_number_range_start="1",
            caller_number_range_end="133",
            callback_url="http://your-domain.com/api/v1/infinity/callback/call-record",
            recording_callback_url="http://your-domain.com/api/v1/infinity/callback/recording",
            max_concurrent_calls=100,
            call_timeout_seconds=60,
            is_active=True
        )
        
        db.add(infinity_config)
        db.commit()
        db.refresh(infinity_config)
        
        print(f"\n✅ Infinity 配置创建成功 (ID: {infinity_config.id})")
        print(f"   甲方ID: {infinity_config.tenant_id}")
        print(f"   API地址: {infinity_config.api_url}")
        print(f"   应用ID: {infinity_config.app_id}")
        print(f"   号段范围: {infinity_config.caller_number_range_start} ~ {infinity_config.caller_number_range_end}")
        print(f"   回调地址: {infinity_config.callback_url}")
        print(f"   最大并发: {infinity_config.max_concurrent_calls}")
        print(f"   超时时间: {infinity_config.call_timeout_seconds}秒")
        print(f"   状态: {'启用' if infinity_config.is_active else '禁用'}")
        
        # 4. 创建分机池
        print("\n" + "=" * 60)
        print("创建分机池")
        print("=" * 60)
        
        extension_numbers = [
            "8001", "8002", "8003", "8004", "8005",
            "8006", "8007", "8008", "8009", "8010"
        ]
        
        created_count = 0
        for ext_num in extension_numbers:
            extension = InfinityExtensionPool(
                tenant_id=tenant.id,
                config_id=infinity_config.id,
                infinity_extension_number=ext_num,
                status=ExtensionStatusEnum.AVAILABLE,
                current_collector_id=None
            )
            db.add(extension)
            created_count += 1
        
        db.commit()
        
        print(f"\n✅ 成功创建 {created_count} 个分机")
        print(f"   分机号: {', '.join(extension_numbers)}")
        
        # 5. 显示统计信息
        print("\n" + "=" * 60)
        print("分机池统计")
        print("=" * 60)
        
        total_count = db.query(InfinityExtensionPool).filter(
            InfinityExtensionPool.config_id == infinity_config.id
        ).count()
        
        available_count = db.query(InfinityExtensionPool).filter(
            InfinityExtensionPool.config_id == infinity_config.id,
            InfinityExtensionPool.status == ExtensionStatusEnum.AVAILABLE
        ).count()
        
        in_use_count = db.query(InfinityExtensionPool).filter(
            InfinityExtensionPool.config_id == infinity_config.id,
            InfinityExtensionPool.status == ExtensionStatusEnum.IN_USE
        ).count()
        
        offline_count = db.query(InfinityExtensionPool).filter(
            InfinityExtensionPool.config_id == infinity_config.id,
            InfinityExtensionPool.status == ExtensionStatusEnum.OFFLINE
        ).count()
        
        print(f"\n总分机数: {total_count}")
        print(f"可用分机: {available_count}")
        print(f"使用中分机: {in_use_count}")
        print(f"离线分机: {offline_count}")
        
        # 6. 更新 collectors 表（添加回呼号码）
        print("\n" + "=" * 60)
        print("更新催员回呼号码（示例）")
        print("=" * 60)
        
        with engine.connect() as conn:
            # 更新前3个催员的回呼号码
            result = conn.execute(text("""
                UPDATE collectors 
                SET callback_number = '13800138' || SUBSTR('000' || id, -3)
                WHERE tenant_id = :tenant_id
                LIMIT 5
            """), {"tenant_id": tenant.id})
            conn.commit()
            print(f"✅ 已更新 {result.rowcount} 个催员的回呼号码")
        
        print("\n" + "=" * 60)
        print("✅ Mock 数据创建完成！")
        print("=" * 60)
        print("\n📌 下一步:")
        print("   1. 在前端刷新页面")
        print("   2. 查看 Infinity 外呼配置")
        print("   3. 查看分机池管理")
        print("\n💡 测试建议:")
        print("   - 可以在 IMPanel 中测试发起外呼")
        print("   - 分机会自动分配给催员")
        print("   - 通话结束后分机会自动释放")
        
    except Exception as e:
        print(f"\n❌ 错误: {e}")
        import traceback
        traceback.print_exc()
        db.rollback()
    finally:
        db.close()

if __name__ == "__main__":
    create_mock_data()

