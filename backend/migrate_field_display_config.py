"""
数据库迁移脚本：更新甲方字段展示配置表
添加新字段：field_data_type, field_source, is_searchable, is_filterable
删除字段：is_visible, is_fixed, align, is_enabled
修改字段：display_width 默认值改为0
"""
import sqlite3
from datetime import datetime

def migrate():
    """执行数据库迁移"""
    db_path = 'cco_test.db'
    
    # 备份数据库
    backup_path = f'cco_test.db.backup_field_display_{datetime.now().strftime("%Y%m%d_%H%M%S")}'
    import shutil
    shutil.copy2(db_path, backup_path)
    print(f"✅ 数据库已备份到: {backup_path}")
    
    conn = sqlite3.connect(db_path)
    cursor = conn.cursor()
    
    try:
        # 1. 创建新表
        print("\n📝 创建新表...")
        cursor.execute("""
            CREATE TABLE IF NOT EXISTS tenant_field_display_configs_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                tenant_id INTEGER NOT NULL,
                scene_type VARCHAR(50) NOT NULL,
                scene_name VARCHAR(100) NOT NULL,
                field_key VARCHAR(100) NOT NULL,
                field_name VARCHAR(200) NOT NULL,
                field_data_type VARCHAR(50),
                field_source VARCHAR(20),
                sort_order INTEGER DEFAULT 0 NOT NULL,
                display_width INTEGER DEFAULT 0 NOT NULL,
                color_type VARCHAR(20) DEFAULT 'normal' NOT NULL,
                color_rule TEXT,
                hide_rule TEXT,
                hide_for_queues TEXT,
                hide_for_agencies TEXT,
                hide_for_teams TEXT,
                format_rule TEXT,
                is_searchable BOOLEAN DEFAULT 0 NOT NULL,
                is_filterable BOOLEAN DEFAULT 0 NOT NULL,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                created_by VARCHAR(100),
                updated_by VARCHAR(100),
                FOREIGN KEY (tenant_id) REFERENCES tenants(id)
            )
        """)
        print("✅ 新表创建成功")
        
        # 2. 迁移数据
        print("\n📦 迁移数据...")
        cursor.execute("""
            INSERT INTO tenant_field_display_configs_new (
                id, tenant_id, scene_type, scene_name, field_key, field_name,
                sort_order, display_width, color_type, color_rule, hide_rule,
                hide_for_queues, hide_for_agencies, hide_for_teams, format_rule,
                created_at, updated_at, created_by, updated_by
            )
            SELECT 
                id, tenant_id, scene_type, scene_name, field_key, field_name,
                sort_order, 
                CASE WHEN display_width IS NULL THEN 0 ELSE display_width END,
                color_type, color_rule, hide_rule,
                hide_for_queues, hide_for_agencies, hide_for_teams, format_rule,
                created_at, updated_at, created_by, updated_by
            FROM tenant_field_display_configs
        """)
        
        migrated_count = cursor.rowcount
        print(f"✅ 迁移了 {migrated_count} 条记录")
        
        # 3. 删除旧表
        print("\n🗑️  删除旧表...")
        cursor.execute("DROP TABLE tenant_field_display_configs")
        print("✅ 旧表已删除")
        
        # 4. 重命名新表
        print("\n📝 重命名新表...")
        cursor.execute("""
            ALTER TABLE tenant_field_display_configs_new 
            RENAME TO tenant_field_display_configs
        """)
        print("✅ 新表已重命名")
        
        # 5. 创建索引
        print("\n📑 创建索引...")
        cursor.execute("""
            CREATE INDEX IF NOT EXISTS idx_tenant_field_display_configs_tenant_id 
            ON tenant_field_display_configs(tenant_id)
        """)
        cursor.execute("""
            CREATE INDEX IF NOT EXISTS idx_tenant_field_display_configs_scene_type 
            ON tenant_field_display_configs(scene_type)
        """)
        cursor.execute("""
            CREATE INDEX IF NOT EXISTS idx_tenant_field_display_configs_field_key 
            ON tenant_field_display_configs(field_key)
        """)
        print("✅ 索引创建成功")
        
        # 提交事务
        conn.commit()
        print("\n✅ 数据库迁移成功！")
        
        # 显示迁移摘要
        print("\n📊 迁移摘要:")
        print("=" * 60)
        print("新增字段:")
        print("  - field_data_type (字段数据类型)")
        print("  - field_source (字段来源：standard/extended/custom)")
        print("  - is_searchable (是否可搜索)")
        print("  - is_filterable (是否可筛选)")
        print("\n删除字段:")
        print("  - is_visible (是否显示)")
        print("  - is_fixed (是否固定列)")
        print("  - align (对齐方式)")
        print("  - is_enabled (是否启用)")
        print("\n修改字段:")
        print("  - display_width 默认值改为 0（表示自动）")
        print("=" * 60)
        
    except Exception as e:
        print(f"\n❌ 迁移失败: {str(e)}")
        conn.rollback()
        raise
    finally:
        conn.close()

if __name__ == '__main__':
    print("=" * 60)
    print("开始执行数据库迁移：更新甲方字段展示配置表")
    print("=" * 60)
    migrate()

