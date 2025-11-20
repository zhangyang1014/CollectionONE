#!/usr/bin/env python3
"""
修复数据库字段类型不匹配问题
将所有主键统一为 BigInteger
"""
import os
import re
from pathlib import Path

def fix_model_files():
    """修复所有模型文件的字段类型"""
    models_dir = Path("app/models")
    fixed_files = []
    
    print("=" * 80)
    print("修复数据库字段类型")
    print("=" * 80)
    
    print("\n策略: 将所有 Integer 主键改为 BigInteger")
    print("这样可以确保所有外键类型匹配\n")
    
    for model_file in models_dir.glob("*.py"):
        if model_file.name == "__init__.py":
            continue
            
        with open(model_file, 'r', encoding='utf-8') as f:
            content = f.read()
            original_content = content
            
        # 修复主键类型: Integer -> BigInteger
        # 只修改主键定义
        content = re.sub(
            r'(id\s*=\s*Column\()Integer(\s*,\s*primary_key\s*=\s*True)',
            r'\1BigInteger\2',
            content
        )
        
        # 如果文件有修改
        if content != original_content:
            # 确保导入了 BigInteger
            if 'BigInteger' not in content:
                # 在 Integer 后面添加 BigInteger
                content = re.sub(
                    r'from sqlalchemy import (.*\bInteger\b)',
                    r'from sqlalchemy import \1, BigInteger',
                    content,
                    count=1
                )
            
            with open(model_file, 'w', encoding='utf-8') as f:
                f.write(content)
                
            fixed_files.append(model_file.name)
            print(f"✓ 修复: {model_file.name}")
    
    print(f"\n共修复 {len(fixed_files)} 个文件")
    
    if fixed_files:
        print("\n修复的文件:")
        for file in fixed_files:
            print(f"   - {file}")
    
    return len(fixed_files)

if __name__ == "__main__":
    os.chdir(os.path.dirname(os.path.abspath(__file__)))
    count = fix_model_files()
    
    print("\n" + "=" * 80)
    if count > 0:
        print("✓ 修复完成!")
        print("=" * 80)
        print("\n下一步:")
        print("   1. 运行 python3 check_field_types.py 验证修复")
        print("   2. 如果使用 MySQL,需要重新创建数据库表")
        print("   3. 如果使用 SQLite,需要重新创建数据库或迁移数据")
    else:
        print("✓ 没有需要修复的文件")
        print("=" * 80)

