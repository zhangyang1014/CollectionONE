"""
修复字段展示配置数据：填充 field_data_type 和 field_source
"""
import sqlite3
from datetime import datetime

def fix_field_display_data():
    """修复字段展示配置数据"""
    db_path = 'cco_test.db'
    
    # 备份数据库
    backup_path = f'cco_test.db.backup_fix_display_{datetime.now().strftime("%Y%m%d_%H%M%S")}'
    import shutil
    shutil.copy2(db_path, backup_path)
    print(f"✅ 数据库已备份到: {backup_path}")
    
    conn = sqlite3.connect(db_path)
    cursor = conn.cursor()
    
    try:
        # 1. 获取所有需要修复的配置
        cursor.execute("""
            SELECT id, field_key, field_name
            FROM tenant_field_display_configs 
            WHERE field_data_type IS NULL OR field_source IS NULL
        """)
        configs = cursor.fetchall()
        
        print(f"\n📦 需要修复 {len(configs)} 条配置记录")
        
        fixed_count = 0
        not_found_count = 0
        not_found_fields = []
        
        for config_id, field_key, field_name in configs:
            # 2. 先从标准字段表查询
            cursor.execute("""
                SELECT field_type, is_extended 
                FROM standard_fields 
                WHERE field_key = ? AND is_active = 1 AND is_deleted = 0
            """, (field_key,))
            
            result = cursor.fetchone()
            
            if result:
                field_type, is_extended = result
                field_source = 'extended' if is_extended else 'standard'
                
                # 更新配置
                cursor.execute("""
                    UPDATE tenant_field_display_configs 
                    SET field_data_type = ?, field_source = ?
                    WHERE id = ?
                """, (field_type, field_source, config_id))
                
                fixed_count += 1
                print(f"  ✅ 修复: {field_name} ({field_key}) -> {field_type} ({field_source})")
            else:
                # 3. 如果不是标准字段，查询自定义字段表
                cursor.execute("""
                    SELECT field_type 
                    FROM custom_fields 
                    WHERE field_key = ? AND is_active = 1 AND is_deleted = 0
                    LIMIT 1
                """, (field_key,))
                
                custom_result = cursor.fetchone()
                
                if custom_result:
                    field_type = custom_result[0]
                    field_source = 'custom'
                    
                    cursor.execute("""
                        UPDATE tenant_field_display_configs 
                        SET field_data_type = ?, field_source = ?
                        WHERE id = ?
                    """, (field_type, field_source, config_id))
                    
                    fixed_count += 1
                    print(f"  ✅ 修复: {field_name} ({field_key}) -> {field_type} (custom)")
                else:
                    not_found_count += 1
                    not_found_fields.append(f"{field_name} ({field_key})")
                    print(f"  ⚠️  未找到字段: {field_name} ({field_key})")
        
        # 提交事务
        conn.commit()
        
        print("\n" + "=" * 60)
        print(f"✅ 数据修复完成！")
        print(f"  - 成功修复: {fixed_count} 条")
        print(f"  - 未找到字段: {not_found_count} 条")
        
        if not_found_fields:
            print("\n⚠️  未找到的字段列表:")
            for field in not_found_fields:
                print(f"    - {field}")
        
        print("=" * 60)
        
        # 验证修复结果
        print("\n🔍 验证修复结果...")
        cursor.execute("""
            SELECT 
                COUNT(*) as total,
                SUM(CASE WHEN field_data_type IS NOT NULL THEN 1 ELSE 0 END) as has_type,
                SUM(CASE WHEN field_source IS NOT NULL THEN 1 ELSE 0 END) as has_source
            FROM tenant_field_display_configs
        """)
        total, has_type, has_source = cursor.fetchone()
        print(f"  - 总配置数: {total}")
        print(f"  - 有字段类型: {has_type}")
        print(f"  - 有字段来源: {has_source}")
        
        if has_type == total and has_source == total:
            print("\n✅ 所有配置都已修复！")
        else:
            print(f"\n⚠️  还有 {total - has_type} 条配置缺少字段类型")
            print(f"⚠️  还有 {total - has_source} 条配置缺少字段来源")
        
    except Exception as e:
        print(f"\n❌ 修复失败: {str(e)}")
        conn.rollback()
        raise
    finally:
        conn.close()

if __name__ == '__main__':
    print("=" * 60)
    print("开始修复字段展示配置数据")
    print("=" * 60)
    fix_field_display_data()

