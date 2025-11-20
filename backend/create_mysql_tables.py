#!/usr/bin/env python3
"""
直接创建 MySQL 表
"""
import sys
import os

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from app.core.database import Base, engine
from app import models

print("=" * 60)
print("创建 MySQL 表结构")
print("=" * 60)

try:
    print("\n正在创建所有表...")
    Base.metadata.create_all(bind=engine)
    print("✓ 表创建成功!")
    
    # 验证
    from sqlalchemy import inspect
    inspector = inspect(engine)
    tables = inspector.get_table_names()
    
    print(f"\n✓ 成功创建 {len(tables)} 个表:")
    for table in sorted(tables):
        print(f"   - {table}")
    
    print("\n" + "=" * 60)
    print("✓ 完成!")
    print("=" * 60)
    
except Exception as e:
    print(f"\n✗ 错误: {e}")
    import traceback
    traceback.print_exc()
    sys.exit(1)

