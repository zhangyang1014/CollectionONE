"""创建数据看板相关表的脚本"""
import sys
from pathlib import Path

# 添加项目根目录到Python路径
sys.path.append(str(Path(__file__).parent))

from app.core.database import engine, Base
from app.models import (
    CaseContact,
    CommunicationRecord,
    PTPRecord,
    QualityInspectionRecord,
    CollectorPerformanceStat,
    CustomDimensionStat,
)

def create_dashboard_tables():
    """创建数据看板相关的所有表"""
    print("开始创建数据看板相关表...")
    
    try:
        # 创建所有表（如果已存在则跳过）
        Base.metadata.create_all(bind=engine, tables=[
            CaseContact.__table__,
            CommunicationRecord.__table__,
            PTPRecord.__table__,
            QualityInspectionRecord.__table__,
            CollectorPerformanceStat.__table__,
            CustomDimensionStat.__table__,
        ])
        
        print("✅ 数据看板相关表创建成功！")
        print("\n已创建的表：")
        print("  1. case_contacts - 案件联系人表")
        print("  2. communication_records - 通信记录表")
        print("  3. ptp_records - PTP记录表")
        print("  4. quality_inspection_records - 质检记录表")
        print("  5. collector_performance_stats - 催员绩效统计表")
        print("  6. custom_dimension_stats - 自定义维度统计表")
        
    except Exception as e:
        print(f"❌ 创建表失败: {e}")
        raise

if __name__ == "__main__":
    create_dashboard_tables()

