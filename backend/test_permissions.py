#!/usr/bin/env python3
"""
权限系统测试脚本
测试权限配置、API和权限验证
"""
import sys
import os
sys.path.append(os.path.dirname(os.path.abspath(__file__)))

import sqlite3
from datetime import datetime

# 数据库文件路径
DB_PATH = os.path.join(os.path.dirname(__file__), 'cco_test.db')


def test_database_tables():
    """测试数据库表是否创建成功"""
    print("\n" + "="*60)
    print("测试 1: 数据库表结构")
    print("="*60)
    
    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()
    
    try:
        # 检查表是否存在
        tables = ['permission_modules', 'permission_items', 'role_permission_configs']
        
        for table in tables:
            cursor.execute(f"SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name='{table}'")
            exists = cursor.fetchone()[0]
            
            if exists:
                cursor.execute(f"SELECT COUNT(*) FROM {table}")
                count = cursor.fetchone()[0]
                print(f"✓ 表 {table} 存在，记录数: {count}")
            else:
                print(f"✗ 表 {table} 不存在")
                return False
        
        return True
    finally:
        conn.close()


def test_permission_data():
    """测试权限数据是否正确导入"""
    print("\n" + "="*60)
    print("测试 2: 权限数据完整性")
    print("="*60)
    
    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()
    
    try:
        # 检查模块数量
        cursor.execute("SELECT COUNT(*) FROM permission_modules")
        module_count = cursor.fetchone()[0]
        print(f"权限模块数: {module_count} (预期: 11)")
        
        # 检查权限项数量
        cursor.execute("SELECT COUNT(*) FROM permission_items")
        item_count = cursor.fetchone()[0]
        print(f"权限项数: {item_count} (预期: 67)")
        
        # 检查默认配置数量
        cursor.execute("SELECT COUNT(*) FROM role_permission_configs WHERE tenant_id IS NULL")
        config_count = cursor.fetchone()[0]
        print(f"默认权限配置数: {config_count} (预期: 206)")
        
        # 检查每个模块
        cursor.execute("""
            SELECT m.module_name, COUNT(i.id) as item_count
            FROM permission_modules m
            LEFT JOIN permission_items i ON m.id = i.module_id
            GROUP BY m.id, m.module_name
            ORDER BY m.sort_order
        """)
        
        print("\n各模块权限项统计:")
        for row in cursor.fetchall():
            print(f"  - {row[0]}: {row[1]} 项")
        
        return module_count == 11 and item_count == 67
    finally:
        conn.close()


def test_role_permissions():
    """测试各角色的权限配置"""
    print("\n" + "="*60)
    print("测试 3: 角色权限配置")
    print("="*60)
    
    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()
    
    try:
        roles = ['SUPER_ADMIN', 'TENANT_ADMIN', 'AGENCY_ADMIN', 'TEAM_LEADER', 
                 'QUALITY_INSPECTOR', 'DATA_SOURCE', 'COLLECTOR']
        
        print("\n各角色权限统计:")
        for role in roles:
            cursor.execute("""
                SELECT 
                    COUNT(*) as total,
                    SUM(CASE WHEN permission_level = 'editable' THEN 1 ELSE 0 END) as editable,
                    SUM(CASE WHEN permission_level = 'readonly' THEN 1 ELSE 0 END) as readonly,
                    SUM(CASE WHEN permission_level = 'none' THEN 1 ELSE 0 END) as none
                FROM role_permission_configs
                WHERE role_code = ? AND tenant_id IS NULL
            """, (role,))
            
            result = cursor.fetchone()
            print(f"\n{role}:")
            print(f"  总计: {result[0]}")
            print(f"  可编辑: {result[1]}")
            print(f"  仅可见: {result[2]}")
            print(f"  不可见: {result[3]}")
        
        return True
    finally:
        conn.close()


def test_specific_permissions():
    """测试特定权限配置"""
    print("\n" + "="*60)
    print("测试 4: 特定权限检查")
    print("="*60)
    
    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()
    
    try:
        # 测试案例
        test_cases = [
            ("SUPER_ADMIN", "SYS_CONFIG", "editable"),
            ("TENANT_ADMIN", "TENANT_VIEW", "readonly"),
            ("AGENCY_ADMIN", "AGENCY_VIEW", "readonly"),
            ("TEAM_LEADER", "TEAM_VIEW", "readonly"),
            ("COLLECTOR", "CASE_VIEW_SELF", "editable"),
            ("COLLECTOR", "CASE_DELETE", None),  # 应该没有此权限
        ]
        
        print("\n权限检查结果:")
        all_passed = True
        
        for role_code, item_key, expected_level in test_cases:
            cursor.execute("""
                SELECT c.permission_level
                FROM role_permission_configs c
                JOIN permission_items i ON c.permission_item_id = i.id
                WHERE c.role_code = ? 
                AND i.item_key = ? 
                AND c.tenant_id IS NULL
            """, (role_code, item_key))
            
            result = cursor.fetchone()
            actual_level = result[0] if result else None
            
            passed = actual_level == expected_level
            status = "✓" if passed else "✗"
            
            print(f"{status} {role_code} -> {item_key}: {actual_level} (预期: {expected_level})")
            
            if not passed:
                all_passed = False
        
        return all_passed
    finally:
        conn.close()


def test_permission_levels():
    """测试权限级别分布"""
    print("\n" + "="*60)
    print("测试 5: 权限级别分布")
    print("="*60)
    
    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()
    
    try:
        cursor.execute("""
            SELECT permission_level, COUNT(*) as count
            FROM role_permission_configs
            WHERE tenant_id IS NULL
            GROUP BY permission_level
        """)
        
        print("\n权限级别分布:")
        for row in cursor.fetchall():
            level_name = {
                'none': '不可见',
                'readonly': '仅可见',
                'editable': '可编辑'
            }.get(row[0], row[0])
            print(f"  {level_name} ({row[0]}): {row[1]}")
        
        return True
    finally:
        conn.close()


def test_api_endpoints():
    """测试 API 端点（需要后端服务运行）"""
    print("\n" + "="*60)
    print("测试 6: API 端点测试（需要后端服务）")
    print("="*60)
    
    try:
        import requests
        
        base_url = "http://localhost:8000/api/v1/permissions"
        
        # 测试获取权限模块
        print("\n测试 GET /api/v1/permissions/modules")
        try:
            response = requests.get(f"{base_url}/modules", timeout=5)
            if response.status_code == 200:
                data = response.json()
                print(f"✓ 成功获取 {len(data)} 个权限模块")
            else:
                print(f"✗ 失败: 状态码 {response.status_code}")
        except Exception as e:
            print(f"⚠ 无法连接到后端服务: {e}")
            print("  提示: 请确保后端服务正在运行")
            return None
        
        # 测试获取权限项
        print("\n测试 GET /api/v1/permissions/items")
        try:
            response = requests.get(f"{base_url}/items", timeout=5)
            if response.status_code == 200:
                data = response.json()
                print(f"✓ 成功获取 {len(data)} 个权限项")
            else:
                print(f"✗ 失败: 状态码 {response.status_code}")
        except Exception as e:
            print(f"✗ 错误: {e}")
        
        # 测试获取权限配置
        print("\n测试 GET /api/v1/permissions/configs")
        try:
            response = requests.get(f"{base_url}/configs", timeout=5)
            if response.status_code == 200:
                data = response.json()
                print(f"✓ 成功获取 {len(data)} 条权限配置")
            else:
                print(f"✗ 失败: 状态码 {response.status_code}")
        except Exception as e:
            print(f"✗ 错误: {e}")
        
        # 测试获取权限矩阵
        print("\n测试 GET /api/v1/permissions/matrix")
        try:
            response = requests.get(f"{base_url}/matrix", timeout=5)
            if response.status_code == 200:
                data = response.json()
                print(f"✓ 成功获取权限矩阵")
                print(f"  - 模块数: {len(data.get('modules', []))}")
                print(f"  - 权限项数: {len(data.get('items', []))}")
                print(f"  - 配置数: {len(data.get('configs', []))}")
            else:
                print(f"✗ 失败: 状态码 {response.status_code}")
        except Exception as e:
            print(f"✗ 错误: {e}")
        
        return True
    except ImportError:
        print("⚠ 未安装 requests 库，跳过 API 测试")
        print("  安装方法: pip install requests")
        return None


def main():
    """主函数"""
    print("\n")
    print("="*60)
    print("权限系统测试")
    print("="*60)
    print(f"数据库: {DB_PATH}")
    print(f"时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    
    results = {}
    
    # 运行所有测试
    results['数据库表结构'] = test_database_tables()
    results['权限数据完整性'] = test_permission_data()
    results['角色权限配置'] = test_role_permissions()
    results['特定权限检查'] = test_specific_permissions()
    results['权限级别分布'] = test_permission_levels()
    
    api_result = test_api_endpoints()
    if api_result is not None:
        results['API端点测试'] = api_result
    
    # 输出测试总结
    print("\n" + "="*60)
    print("测试总结")
    print("="*60)
    
    passed = sum(1 for v in results.values() if v is True)
    failed = sum(1 for v in results.values() if v is False)
    total = len(results)
    
    for test_name, result in results.items():
        status = "✓ 通过" if result else "✗ 失败"
        print(f"{status}: {test_name}")
    
    print(f"\n总计: {total} 个测试")
    print(f"通过: {passed}")
    print(f"失败: {failed}")
    print(f"通过率: {passed/total*100:.1f}%")
    
    if failed == 0:
        print("\n🎉 所有测试通过！")
        return 0
    else:
        print(f"\n⚠️ {failed} 个测试失败")
        return 1


if __name__ == "__main__":
    sys.exit(main())

