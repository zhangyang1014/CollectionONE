#!/usr/bin/env python3
"""
补充所有标准字段数据
"""

import sys
from sqlalchemy.orm import Session
from app.core.database import engine
from app.models import StandardField

# 导入完整的标准字段数据
from mock_field_data import STANDARD_FIELDS

def add_all_standard_fields():
    """补充所有标准字段"""
    print("=" * 60)
    print("开始补充标准字段...")
    print("=" * 60)
    
    db = Session(engine)
    
    try:
        # 获取当前已有的字段
        existing_fields = db.query(StandardField).all()
        existing_keys = {f.field_key for f in existing_fields}
        print(f"\n当前已有 {len(existing_fields)} 个标准字段")
        
        # 字段分组ID映射（mock数据中的ID -> 实际数据库中的ID）
        # mock数据中: 11=基础身份信息, 12=教育信息, 13=职业信息, 14=用户行为与信用
        # 实际数据库: 6=基础身份信息, 7=教育信息, 8=职业信息, 9=用户行为与信用
        # 2=贷款详情, 3=借款记录, 4=还款记录, 5=分期详情
        group_id_mapping = {
            11: 6,  # 基础身份信息
            12: 7,  # 教育信息
            13: 8,  # 职业信息
            14: 9,  # 用户行为与信用
            2: 2,   # 贷款详情
            3: 3,   # 借款记录
            4: 4,   # 还款记录
            5: 5,   # 分期详情
        }
        
        # 添加新字段
        added_count = 0
        for field_data in STANDARD_FIELDS:
            field_key = field_data['field_key']
            
            # 跳过已存在的字段
            if field_key in existing_keys:
                continue
            
            # 映射分组ID
            old_group_id = field_data['field_group_id']
            new_group_id = group_id_mapping.get(old_group_id, old_group_id)
            
            # 创建新字段（不指定id，让数据库自动生成）
            new_field = StandardField(
                field_key=field_data['field_key'],
                field_name=field_data['field_name'],
                field_name_en=field_data['field_name_en'],
                field_type=field_data['field_type'],
                field_group_id=new_group_id,
                is_required=field_data['is_required'],
                is_extended=field_data['is_extended'],
                sort_order=field_data['sort_order'],
                is_active=field_data['is_active'],
                is_deleted=field_data['is_deleted']
            )
            db.add(new_field)
            added_count += 1
        
        db.commit()
        
        # 统计最终结果
        total_fields = db.query(StandardField).count()
        
        print(f"\n✓ 新增 {added_count} 个标准字段")
        print(f"✓ 总共 {total_fields} 个标准字段")
        
        # 按分组统计
        print("\n按分组统计:")
        group_stats = db.execute("""
            SELECT 
                fg.group_name,
                COUNT(sf.id) as field_count
            FROM field_groups fg
            LEFT JOIN standard_fields sf ON sf.field_group_id = fg.id
            WHERE sf.is_deleted = 0
            GROUP BY fg.id, fg.group_name
            ORDER BY fg.id
        """).fetchall()
        
        for group_name, count in group_stats:
            print(f"  - {group_name}: {count} 个字段")
        
        print("\n" + "=" * 60)
        print("✅ 标准字段补充完成！")
        print("=" * 60)
        
        return True
        
    except Exception as e:
        db.rollback()
        print(f"\n❌ 错误: {e}")
        import traceback
        traceback.print_exc()
        return False
    finally:
        db.close()

if __name__ == "__main__":
    success = add_all_standard_fields()
    sys.exit(0 if success else 1)

