"""
更新催员密码哈希为bcrypt格式
"""

import sys
import os
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from app.core.database import SessionLocal
from app.core.security import get_password_hash
from app.models.collector import Collector

def update_collector_password():
    """更新COL1111催员的密码哈希"""
    db = SessionLocal()
    
    try:
        collector = db.query(Collector).filter(
            Collector.collector_code == "COL1111"
        ).first()
        
        if collector:
            # 更新密码哈希为bcrypt格式
            collector.password_hash = get_password_hash("123456")
            db.commit()
            print(f"✅ 已更新催员COL1111的密码哈希为bcrypt格式")
            print(f"   新哈希: {collector.password_hash[:30]}...")
        else:
            print("❌ 未找到催员COL1111")
    except Exception as e:
        db.rollback()
        print(f"❌ 错误: {str(e)}")
        import traceback
        traceback.print_exc()
    finally:
        db.close()

if __name__ == "__main__":
    update_collector_password()

