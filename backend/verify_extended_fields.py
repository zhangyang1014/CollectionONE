#!/usr/bin/env python3
"""
验证拓展字段功能
"""

import sqlite3
import json
import requests

def verify_database():
    """验证数据库中的拓展字段"""
    print("=" * 80)
    print("1. 验证数据库中的拓展字段")
    print("=" * 80)
    
    conn = sqlite3.connect('cco_test.db')
    cursor = conn.cursor()
    
    cursor.execute("""
        SELECT 
            sf.id, 
            sf.field_key, 
            sf.field_name, 
            sf.field_type, 
            sf.is_extended,
            sf.enum_options,
            fg.group_name
        FROM standard_fields sf
        LEFT JOIN field_groups fg ON sf.field_group_id = fg.id
        WHERE sf.is_extended = 1 
        AND sf.is_active = 1 
        AND sf.is_deleted = 0
        ORDER BY sf.sort_order
    """)
    
    rows = cursor.fetchall()
    if rows:
        print(f"\n✅ 找到 {len(rows)} 个拓展字段:\n")
        for row in rows:
            field_id, field_key, field_name, field_type, is_extended, enum_options, group_name = row
            print(f"  • {field_name} ({field_key})")
            print(f"    ID: {field_id}")
            print(f"    类型: {field_type}")
            print(f"    所属分组: {group_name}")
            if enum_options:
                options = json.loads(enum_options)
                print(f"    枚举选项: {', '.join(options)}")
            print()
    else:
        print("\n❌ 未找到拓展字段")
        return False
    
    conn.close()
    return True

def verify_api():
    """验证API接口"""
    print("=" * 80)
    print("2. 验证API接口")
    print("=" * 80)
    
    base_url = "http://localhost:8000"
    
    # 测试拓展字段接口
    print("\n测试: GET /api/v1/tenants/1/extended-fields")
    try:
        response = requests.get(f"{base_url}/api/v1/tenants/1/extended-fields")
        if response.status_code == 200:
            data = response.json()
            print(f"✅ 接口正常，返回 {len(data)} 个拓展字段")
            for field in data:
                print(f"  • {field['tenant_field_name']} ({field['field_alias']})")
        else:
            print(f"❌ 接口返回错误状态码: {response.status_code}")
            return False
    except Exception as e:
        print(f"❌ 接口调用失败: {e}")
        return False
    
    # 测试可用字段接口
    print("\n测试: GET /api/v1/field-display-configs/available-fields")
    try:
        response = requests.get(f"{base_url}/api/v1/field-display-configs/available-fields?tenant_id=1")
        if response.status_code == 200:
            data = response.json()
            extended_fields = [f for f in data if f.get('field_source') == 'extended']
            print(f"✅ 接口正常，返回 {len(extended_fields)} 个拓展字段")
            for field in extended_fields:
                print(f"  • {field['field_name']} ({field['field_key']}) - {field['field_type']}")
                if field.get('enum_options'):
                    print(f"    选项: {', '.join(field['enum_options'])}")
        else:
            print(f"❌ 接口返回错误状态码: {response.status_code}")
            return False
    except Exception as e:
        print(f"❌ 接口调用失败: {e}")
        return False
    
    return True

def main():
    """主函数"""
    print("\n" + "=" * 80)
    print("拓展字段功能验证")
    print("=" * 80 + "\n")
    
    # 验证数据库
    db_ok = verify_database()
    
    # 验证API
    api_ok = verify_api()
    
    # 总结
    print("\n" + "=" * 80)
    print("验证结果总结")
    print("=" * 80)
    print(f"数据库: {'✅ 通过' if db_ok else '❌ 失败'}")
    print(f"API接口: {'✅ 通过' if api_ok else '❌ 失败'}")
    
    if db_ok and api_ok:
        print("\n" + "🎉 " * 10)
        print("所有验证通过！拓展字段功能正常工作！")
        print("🎉 " * 10)
        return True
    else:
        print("\n⚠️ 部分验证失败，请检查上述错误信息")
        return False

if __name__ == "__main__":
    import sys
    success = main()
    sys.exit(0 if success else 1)

