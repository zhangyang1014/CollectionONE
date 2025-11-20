#!/usr/bin/env python3
"""
SQLite 到 MySQL 数据迁移脚本
将现有的 SQLite 数据库迁移到 MySQL
"""
import sys
import os
from datetime import datetime

# 添加项目根目录到 Python 路径
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from sqlalchemy import create_engine, text, inspect
from sqlalchemy.orm import sessionmaker
from app.core.config import settings

# SQLite 数据库路径
SQLITE_DB = "sqlite:///./cco_test.db"

def migrate_data():
    """迁移数据从 SQLite 到 MySQL"""
    print("=" * 60)
    print("数据迁移: SQLite → MySQL")
    print("=" * 60)
    
    # 检查 SQLite 数据库是否存在
    sqlite_path = "./cco_test.db"
    if not os.path.exists(sqlite_path):
        print(f"⚠️  SQLite 数据库不存在: {sqlite_path}")
        print("跳过数据迁移,将创建新的 MySQL 数据库")
        return create_tables_only()
    
    print(f"\n1. 连接到 SQLite 数据库: {sqlite_path}")
    sqlite_engine = create_engine(SQLITE_DB)
    SQLiteSession = sessionmaker(bind=sqlite_engine)
    sqlite_session = SQLiteSession()
    
    print(f"2. 连接到 MySQL 数据库: {settings.DATABASE_URL}")
    mysql_engine = create_engine(settings.DATABASE_URL)
    MySQLSession = sessionmaker(bind=mysql_engine)
    mysql_session = MySQLSession()
    
    # 获取 SQLite 中的所有表
    inspector = inspect(sqlite_engine)
    tables = inspector.get_table_names()
    
    print(f"\n3. 发现 {len(tables)} 个表需要迁移:")
    for table in tables:
        print(f"   - {table}")
    
    # 创建所有表结构
    print("\n4. 在 MySQL 中创建表结构...")
    from app.core.database import Base
    Base.metadata.create_all(bind=mysql_engine)
    print("✓ 表结构创建完成")
    
    # 迁移每个表的数据
    print("\n5. 开始迁移数据...")
    total_rows = 0
    
    for table_name in tables:
        try:
            # 从 SQLite 读取数据
            result = sqlite_session.execute(text(f"SELECT * FROM {table_name}"))
            rows = result.fetchall()
            
            if not rows:
                print(f"   - {table_name}: 0 行 (跳过)")
                continue
            
            # 获取列名
            columns = result.keys()
            
            # 插入到 MySQL
            for row in rows:
                # 构建插入语句
                placeholders = ", ".join([f":{col}" for col in columns])
                insert_sql = f"INSERT INTO {table_name} ({', '.join(columns)}) VALUES ({placeholders})"
                
                # 将行转换为字典
                row_dict = dict(zip(columns, row))
                
                # 执行插入
                mysql_session.execute(text(insert_sql), row_dict)
            
            mysql_session.commit()
            total_rows += len(rows)
            print(f"   ✓ {table_name}: {len(rows)} 行")
            
        except Exception as e:
            print(f"   ✗ {table_name}: 迁移失败 - {str(e)}")
            mysql_session.rollback()
            continue
    
    print(f"\n6. 数据迁移完成!")
    print(f"   总共迁移: {total_rows} 行数据")
    
    # 关闭连接
    sqlite_session.close()
    mysql_session.close()
    
    return True

def create_tables_only():
    """仅创建表结构,不迁移数据"""
    print("\n创建 MySQL 表结构...")
    
    try:
        from app.core.database import Base, engine
        
        # 导入所有模型以确保它们被注册
        from app.models import (
            admin, agency, team, collector, case, 
            case_assignment_history, call_record, follow_up_record,
            notification_config, public_notification, notification_template
        )
        
        # 创建所有表
        Base.metadata.create_all(bind=engine)
        
        print("✓ 表结构创建成功")
        
        # 显示创建的表
        from sqlalchemy import inspect
        inspector = inspect(engine)
        tables = inspector.get_table_names()
        
        print(f"\n创建了 {len(tables)} 个表:")
        for table in tables:
            print(f"   - {table}")
        
        return True
        
    except Exception as e:
        print(f"✗ 表结构创建失败: {e}")
        import traceback
        traceback.print_exc()
        return False

def verify_migration():
    """验证迁移结果"""
    print("\n" + "=" * 60)
    print("验证迁移结果")
    print("=" * 60)
    
    try:
        from app.core.database import engine
        from sqlalchemy import inspect, text
        
        inspector = inspect(engine)
        tables = inspector.get_table_names()
        
        print(f"\n✓ MySQL 数据库包含 {len(tables)} 个表")
        
        # 统计每个表的记录数
        with engine.connect() as conn:
            print("\n表记录统计:")
            for table in sorted(tables):
                try:
                    result = conn.execute(text(f"SELECT COUNT(*) FROM {table}"))
                    count = result.scalar()
                    print(f"   - {table:30s}: {count:5d} 行")
                except Exception as e:
                    print(f"   - {table:30s}: 错误 - {str(e)}")
        
        return True
        
    except Exception as e:
        print(f"\n✗ 验证失败: {e}")
        return False

if __name__ == "__main__":
    print("\n" + "=" * 60)
    print("CCO System - 数据库迁移工具")
    print("=" * 60)
    print(f"时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    print(f"目标数据库: {settings.DATABASE_URL}")
    print("=" * 60)
    
    # 确认操作
    print("\n⚠️  警告: 此操作将在 MySQL 中创建/覆盖数据库表")
    response = input("是否继续? (yes/no): ")
    
    if response.lower() not in ['yes', 'y']:
        print("操作已取消")
        sys.exit(0)
    
    # 执行迁移
    success = migrate_data()
    
    if success:
        # 验证迁移
        verify_migration()
        
        print("\n" + "=" * 60)
        print("✓ 迁移完成!")
        print("=" * 60)
        print("\n下一步:")
        print("1. 重启后端服务: bash restart_backend.sh")
        print("2. 访问前端页面验证功能")
        print("3. 检查数据是否正确迁移")
    else:
        print("\n" + "=" * 60)
        print("✗ 迁移失败")
        print("=" * 60)
        sys.exit(1)

