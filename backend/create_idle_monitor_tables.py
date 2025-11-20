"""
创建空闲催员监控相关数据表
"""
import sys
import os

# 添加项目根目录到Python路径
sys.path.append(os.path.dirname(os.path.abspath(__file__)))

from app.core.database import engine, Base
from app.models.idle_monitor_config import IdleMonitorConfig
from app.models.collector_idle_record import CollectorIdleRecord, CollectorIdleStats


def create_idle_monitor_tables():
    """创建空闲监控相关表"""
    print("开始创建空闲监控相关数据表...")
    
    try:
        # 创建表
        Base.metadata.create_all(bind=engine, tables=[
            IdleMonitorConfig.__table__,
            CollectorIdleRecord.__table__,
            CollectorIdleStats.__table__
        ])
        
        print("✅ 空闲监控数据表创建成功！")
        print("\n创建的表:")
        print("  - idle_monitor_configs (空闲监控配置表)")
        print("  - collector_idle_records (催员空闲记录表)")
        print("  - collector_idle_stats (催员空闲统计表)")
        
        return True
        
    except Exception as e:
        print(f"❌ 创建表时出错: {str(e)}")
        import traceback
        traceback.print_exc()
        return False


if __name__ == "__main__":
    success = create_idle_monitor_tables()
    sys.exit(0 if success else 1)

