#!/usr/bin/env python3
"""
初始化案件队列数据
为每个甲方创建默认的案件队列配置
"""
import os
import sys

# 添加项目根目录到 Python 路径
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from sqlalchemy.orm import sessionmaker
from app.core.database import engine
from app.models import Tenant, CaseQueue

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

def init_case_queues():
    """初始化所有甲方的案件队列"""
    print("=" * 80)
    print("初始化案件队列数据")
    print("=" * 80)
    
    try:
        # 创建session
        Session = sessionmaker(bind=engine)
        session = Session()
        
        # 获取所有甲方
        tenants = session.query(Tenant).filter(Tenant.is_active == True).all()
        
        if not tenants:
            print("❌ 错误：数据库中没有甲方数据，请先运行 init_all_data.py 初始化基础数据")
            return False
        
        print(f"\n找到 {len(tenants)} 个甲方，开始创建队列...")
        
        # 清空现有队列数据
        print("\n1. 清空现有队列数据...")
        deleted_count = session.query(CaseQueue).delete()
        session.commit()
        print(f"✓ 已删除 {deleted_count} 条现有队列数据")
        
        # 为每个甲方创建默认队列
        print("\n2. 为每个甲方创建默认队列...")
        total_queues = 0
        for tenant in tenants:
            print(f"\n  为甲方 [{tenant.tenant_code}] {tenant.tenant_name} 创建队列...")
            queues = create_default_queues_for_tenant(session, tenant.id)
            total_queues += len(queues)
            print(f"  ✓ 成功创建 {len(queues)} 个队列")
            
            # 显示创建的队列详情
            for queue in queues:
                start_str = "-∞" if queue.overdue_days_start is None else str(queue.overdue_days_start)
                end_str = "+∞" if queue.overdue_days_end is None else str(queue.overdue_days_end)
                print(f"    - {queue.queue_code} ({queue.queue_name}): {start_str} ~ {end_str} 天")
        
        # 提交所有更改
        session.commit()
        
        # 验证数据
        print("\n3. 验证数据...")
        queue_count = session.query(CaseQueue).count()
        print(f"✓ 数据库中共有 {queue_count} 个队列")
        
        session.close()
        
        print("\n" + "=" * 80)
        print("✅ 案件队列数据初始化完成！")
        print("=" * 80)
        print(f"\n统计信息：")
        print(f"  - 甲方数量: {len(tenants)}")
        print(f"  - 队列总数: {total_queues}")
        print(f"  - 每个甲方: 5 个默认队列 (C, S0, S1, L1, M1)")
        print("\n队列说明：")
        print("  - C队列：未逾期（-∞ ~ -1天）")
        print("  - S0队列：当日到期（0 ~ 0天）")
        print("  - S1队列：轻度逾期（1 ~ 5天）")
        print("  - L1队列：中度逾期（6 ~ 90天）")
        print("  - M1队列：重度逾期（91 ~ +∞天）")
        print("\n" + "=" * 80)
        
        return True
        
    except Exception as e:
        print(f"\n❌ 初始化失败: {e}")
        import traceback
        traceback.print_exc()
        return False

if __name__ == "__main__":
    success = init_case_queues()
    sys.exit(0 if success else 1)

