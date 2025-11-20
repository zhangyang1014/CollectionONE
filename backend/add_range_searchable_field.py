"""
数据库迁移脚本：添加 is_range_searchable 字段
支持数字和时间类型的范围检索功能
"""
import sqlite3
from datetime import datetime

def migrate():
    """执行数据库迁移"""
    db_path = 'cco_test.db'
    
    # 备份数据库
    backup_path = f'cco_test.db.backup_range_search_{datetime.now().strftime("%Y%m%d_%H%M%S")}'
    import shutil
    shutil.copy2(db_path, backup_path)
    print(f"✅ 数据库已备份到: {backup_path}")
    
    conn = sqlite3.connect(db_path)
    cursor = conn.cursor()
    
    try:
        # 1. 检查字段是否已存在
        cursor.execute("PRAGMA table_info(tenant_field_display_configs)")
        columns = [row[1] for row in cursor.fetchall()]
        
        if 'is_range_searchable' in columns:
            print("⚠️  字段 is_range_searchable 已存在，跳过迁移")
            return
        
        print("\n📝 添加 is_range_searchable 字段...")
        
        # 2. 添加新字段
        cursor.execute("""
            ALTER TABLE tenant_field_display_configs 
            ADD COLUMN is_range_searchable BOOLEAN DEFAULT 0 NOT NULL
        """)
        print("✅ 字段添加成功")
        
        # 3. 根据字段类型自动设置 is_range_searchable
        print("\n📦 根据字段类型自动设置范围检索...")
        
        # 获取所有配置
        cursor.execute("""
            SELECT id, field_key, field_name, field_data_type 
            FROM tenant_field_display_configs
        """)
        configs = cursor.fetchall()
        
        updated_count = 0
        range_types = ['Integer', 'Decimal', 'Date', 'Datetime']
        
        for config_id, field_key, field_name, field_data_type in configs:
            if field_data_type in range_types:
                cursor.execute("""
                    UPDATE tenant_field_display_configs 
                    SET is_range_searchable = 1
                    WHERE id = ?
                """, (config_id,))
                updated_count += 1
                print(f"  ✅ 启用范围检索: {field_name} ({field_key}) - {field_data_type}")
        
        # 提交事务
        conn.commit()
        
        print("\n" + "=" * 60)
        print(f"✅ 数据库迁移成功！")
        print(f"  - 总配置数: {len(configs)}")
        print(f"  - 启用范围检索: {updated_count} 条")
        print("=" * 60)
        
        # 验证结果
        print("\n🔍 验证迁移结果...")
        cursor.execute("""
            SELECT 
                field_data_type,
                COUNT(*) as count,
                SUM(CASE WHEN is_range_searchable = 1 THEN 1 ELSE 0 END) as range_enabled
            FROM tenant_field_display_configs
            WHERE field_data_type IS NOT NULL
            GROUP BY field_data_type
            ORDER BY field_data_type
        """)
        
        results = cursor.fetchall()
        print("\n按字段类型统计:")
        for field_type, count, range_enabled in results:
            print(f"  {field_type:12} - 总数: {count:3}, 启用范围检索: {range_enabled:3}")
        
    except Exception as e:
        print(f"\n❌ 迁移失败: {str(e)}")
        conn.rollback()
        raise
    finally:
        conn.close()

if __name__ == '__main__':
    print("=" * 60)
    print("开始执行数据库迁移：添加范围检索功能")
    print("=" * 60)
    migrate()

