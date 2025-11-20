#!/usr/bin/env python3
"""
检查数据库字段类型是否符合 MySQL 规范
"""
import os
import re
from pathlib import Path

def check_model_files():
    """检查所有模型文件的字段类型"""
    models_dir = Path("app/models")
    issues = []
    
    print("=" * 80)
    print("数据库字段类型检查")
    print("=" * 80)
    
    # 检查外键类型不匹配问题
    print("\n1. 检查外键类型不匹配问题...")
    
    # 收集所有表的主键类型
    primary_keys = {}
    foreign_keys = []
    
    for model_file in models_dir.glob("*.py"):
        if model_file.name == "__init__.py":
            continue
            
        with open(model_file, 'r', encoding='utf-8') as f:
            content = f.read()
            
        # 查找表名
        table_match = re.search(r'__tablename__\s*=\s*["\'](\w+)["\']', content)
        if not table_match:
            continue
            
        table_name = table_match.group(1)
        
        # 查找主键类型
        id_match = re.search(r'id\s*=\s*Column\((Integer|BigInteger)', content)
        if id_match:
            pk_type = id_match.group(1)
            primary_keys[table_name] = pk_type
            
        # 查找外键
        fk_pattern = r'(\w+)\s*=\s*Column\((Integer|BigInteger)[^)]*ForeignKey\(["\'](\w+)\.(\w+)["\']'
        for match in re.finditer(fk_pattern, content):
            fk_name = match.group(1)
            fk_type = match.group(2)
            ref_table = match.group(3)
            ref_column = match.group(4)
            foreign_keys.append({
                'file': model_file.name,
                'table': table_name,
                'fk_name': fk_name,
                'fk_type': fk_type,
                'ref_table': ref_table,
                'ref_column': ref_column
            })
    
    # 检查外键类型是否匹配
    print(f"\n找到 {len(primary_keys)} 个表的主键")
    print(f"找到 {len(foreign_keys)} 个外键")
    
    mismatches = []
    for fk in foreign_keys:
        ref_table = fk['ref_table']
        if ref_table in primary_keys:
            pk_type = primary_keys[ref_table]
            if fk['fk_type'] != pk_type:
                mismatches.append(fk)
                issues.append(
                    f"❌ {fk['file']} - {fk['table']}.{fk['fk_name']} ({fk['fk_type']}) "
                    f"引用 {ref_table}.{fk['ref_column']} ({pk_type})"
                )
    
    if mismatches:
        print(f"\n❌ 发现 {len(mismatches)} 个外键类型不匹配:")
        for issue in issues:
            print(f"   {issue}")
    else:
        print("\n✅ 所有外键类型匹配正确")
    
    # 检查字段长度
    print("\n2. 检查字段长度...")
    length_issues = []
    
    for model_file in models_dir.glob("*.py"):
        if model_file.name == "__init__.py":
            continue
            
        with open(model_file, 'r', encoding='utf-8') as f:
            content = f.read()
            
        # 查找 String 字段
        string_pattern = r'(\w+)\s*=\s*Column\(String\((\d+)\)'
        for match in re.finditer(string_pattern, content):
            field_name = match.group(1)
            length = int(match.group(2))
            
            # 检查是否有异常长度
            if length > 5000:
                length_issues.append(f"⚠️  {model_file.name} - {field_name}: String({length}) 长度过大")
    
    if length_issues:
        print(f"\n⚠️  发现 {len(length_issues)} 个字段长度问题:")
        for issue in length_issues:
            print(f"   {issue}")
    else:
        print("\n✅ 字段长度正常")
    
    # 检查 Text 类型使用
    print("\n3. 检查 Text 类型使用...")
    text_fields = []
    
    for model_file in models_dir.glob("*.py"):
        if model_file.name == "__init__.py":
            continue
            
        with open(model_file, 'r', encoding='utf-8') as f:
            content = f.read()
            
        # 查找 Text 字段
        text_pattern = r'(\w+)\s*=\s*Column\(Text'
        for match in re.finditer(text_pattern, content):
            field_name = match.group(1)
            text_fields.append(f"   {model_file.name} - {field_name}")
    
    print(f"\n找到 {len(text_fields)} 个 Text 类型字段:")
    for field in text_fields[:10]:  # 只显示前10个
        print(field)
    if len(text_fields) > 10:
        print(f"   ... 还有 {len(text_fields) - 10} 个")
    
    # 检查 JSON 类型
    print("\n4. 检查 JSON 类型...")
    json_fields = []
    
    for model_file in models_dir.glob("*.py"):
        if model_file.name == "__init__.py":
            continue
            
        with open(model_file, 'r', encoding='utf-8') as f:
            content = f.read()
            
        # 查找 JSON 字段
        json_pattern = r'(\w+)\s*=\s*Column\(JSON'
        for match in re.finditer(json_pattern, content):
            field_name = match.group(1)
            json_fields.append(f"   {model_file.name} - {field_name}")
    
    print(f"\n找到 {len(json_fields)} 个 JSON 类型字段:")
    for field in json_fields[:10]:
        print(field)
    if len(json_fields) > 10:
        print(f"   ... 还有 {len(json_fields) - 10} 个")
    
    # 总结
    print("\n" + "=" * 80)
    print("检查总结")
    print("=" * 80)
    
    if mismatches:
        print(f"\n❌ 发现 {len(mismatches)} 个外键类型不匹配问题")
        print("   这会导致 MySQL 无法创建外键约束")
        print("\n建议:")
        print("   1. 统一所有主键为 BigInteger")
        print("   2. 或者统一所有主键为 Integer")
        print("   3. 确保外键类型与引用的主键类型完全一致")
    else:
        print("\n✅ 所有字段类型检查通过")
    
    if length_issues:
        print(f"\n⚠️  发现 {len(length_issues)} 个字段长度问题")
    
    print(f"\n📊 统计:")
    print(f"   - 表数量: {len(primary_keys)}")
    print(f"   - 外键数量: {len(foreign_keys)}")
    print(f"   - Text 字段: {len(text_fields)}")
    print(f"   - JSON 字段: {len(json_fields)}")
    
    return len(mismatches) == 0

if __name__ == "__main__":
    os.chdir(os.path.dirname(os.path.abspath(__file__)))
    success = check_model_files()
    exit(0 if success else 1)

