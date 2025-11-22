#!/usr/bin/env python3
"""
直接添加甲方拓展字段到数据库
"""

import sqlite3
import json
from datetime import datetime

def add_extended_fields():
    """添加拓展字段"""
    print("=" * 60)
    print("开始添加拓展字段...")
    print("=" * 60)
    
    conn = sqlite3.connect('cco_test.db')
    cursor = conn.cursor()
    
    try:
        # 备份数据库
        backup_name = f'cco_test.db.backup_extended_fields_{datetime.now().strftime("%Y%m%d_%H%M%S")}'
        print(f"\n📦 创建数据库备份: {backup_name}")
        import shutil
        shutil.copy2('cco_test.db', backup_name)
        
        # 查询现有字段的最大sort_order
        cursor.execute("SELECT MAX(sort_order) FROM standard_fields")
        max_sort = cursor.fetchone()[0] or 0
        start_sort_order = max_sort + 10
        
        # 定义要添加的拓展字段
        # 归属于"用户行为与信用"分组（ID=9）
        extended_fields = [
            {
                'field_key': 'approval_policy',
                'field_name': '通过政策',
                'field_name_en': 'Approval Policy',
                'field_type': 'Enum',
                'field_group_id': 9,  # 用户行为与信用
                'is_required': 0,
                'is_extended': 1,
                'description': '用户审批通过的政策类型',
                'example_value': '正常',
                'enum_options': ['放松', '收紧', '正常'],
                'sort_order': start_sort_order,
                'is_active': 1,
                'is_deleted': 0
            },
            {
                'field_key': 'c_card_label',
                'field_name': 'C卡标签',
                'field_name_en': 'C Card Label',
                'field_type': 'Enum',
                'field_group_id': 9,  # 用户行为与信用
                'is_required': 0,
                'is_extended': 1,
                'description': '用户C卡分类标签',
                'example_value': 'A',
                'enum_options': ['A', 'B', 'C', 'D'],
                'sort_order': start_sort_order + 1,
                'is_active': 1,
                'is_deleted': 0
            },
            {
                'field_key': 'complaint_status',
                'field_name': '是否投诉过',
                'field_name_en': 'Complaint Status',
                'field_type': 'Enum',
                'field_group_id': 9,  # 用户行为与信用
                'is_required': 0,
                'is_extended': 1,
                'description': '用户是否有投诉记录',
                'example_value': '无',
                'enum_options': ['无', '投诉过'],
                'sort_order': start_sort_order + 2,
                'is_active': 1,
                'is_deleted': 0
            }
        ]
        
        # 检查字段是否已存在
        added_count = 0
        for field_data in extended_fields:
            field_key = field_data['field_key']
            
            # 检查是否已存在
            cursor.execute(
                "SELECT id FROM standard_fields WHERE field_key = ?",
                (field_key,)
            )
            existing = cursor.fetchone()
            
            if existing:
                print(f"\n⚠️  字段已存在，跳过: {field_data['field_name']} ({field_key})")
                continue
            
            # 插入新字段
            current_time = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
            cursor.execute("""
                INSERT INTO standard_fields (
                    field_key, field_name, field_name_en, field_type, 
                    field_group_id, is_required, is_extended, description, 
                    example_value, enum_options, validation_rules, 
                    sort_order, is_active, is_deleted, created_at, updated_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """, (
                field_data['field_key'],
                field_data['field_name'],
                field_data['field_name_en'],
                field_data['field_type'],
                field_data['field_group_id'],
                field_data['is_required'],
                field_data['is_extended'],
                field_data['description'],
                field_data['example_value'],
                json.dumps(field_data['enum_options'], ensure_ascii=False),
                None,  # validation_rules
                field_data['sort_order'],
                field_data['is_active'],
                field_data['is_deleted'],
                current_time,
                current_time
            ))
            
            added_count += 1
            print(f"\n✅ 添加字段: {field_data['field_name']} ({field_key})")
            print(f"   类型: {field_data['field_type']}")
            print(f"   枚举选项: {', '.join(field_data['enum_options'])}")
        
        conn.commit()
        
        # 查询所有拓展字段
        print("\n" + "=" * 60)
        print("当前所有拓展字段:")
        print("=" * 60)
        
        cursor.execute("""
            SELECT field_key, field_name, field_type, enum_options
            FROM standard_fields
            WHERE is_extended = 1 AND is_active = 1 AND is_deleted = 0
            ORDER BY sort_order
        """)
        
        all_extended = cursor.fetchall()
        for field_key, field_name, field_type, enum_options in all_extended:
            print(f"• {field_name} ({field_key})")
            print(f"  类型: {field_type}")
            if enum_options:
                options = json.loads(enum_options)
                print(f"  选项: {', '.join(options)}")
        
        print("\n" + "=" * 60)
        print(f"✅ 成功添加 {added_count} 个拓展字段！")
        print(f"✅ 总共 {len(all_extended)} 个拓展字段")
        print("=" * 60)
        
        return True
        
    except Exception as e:
        conn.rollback()
        print(f"\n❌ 错误: {e}")
        import traceback
        traceback.print_exc()
        return False
    finally:
        conn.close()

if __name__ == "__main__":
    import sys
    success = add_extended_fields()
    sys.exit(0 if success else 1)

