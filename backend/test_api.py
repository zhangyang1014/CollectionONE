#!/usr/bin/env python3
"""快速测试数据看板API是否可用"""
import requests
import json

BASE_URL = "http://localhost:8000"

def test_endpoint(method, url, params=None, data=None):
    """测试API端点"""
    try:
        if method == "GET":
            response = requests.get(url, params=params, timeout=3)
        elif method == "POST":
            response = requests.post(url, json=data, timeout=3)
        
        status_icon = "✅" if response.status_code < 400 else "❌"
        print(f"{status_icon} {method:6s} {url:60s} - {response.status_code}")
        
        if response.status_code == 404:
            print(f"      ⚠️  路由未注册！需要重启后端服务")
            return False
        elif response.status_code >= 500:
            print(f"      ⚠️  服务器错误")
            try:
                error_detail = response.json()
                print(f"      详情: {error_detail.get('detail', 'Unknown error')}")
            except:
                print(f"      响应: {response.text[:100]}")
            return False
        
        return True
    except requests.exceptions.ConnectionError:
        print(f"❌ {method:6s} {url:60s} - 无法连接")
        print(f"      ⚠️  后端服务未运行！请启动后端服务")
        return False
    except Exception as e:
        print(f"❌ {method:6s} {url:60s} - 错误: {str(e)[:50]}")
        return False

print("=" * 80)
print("测试数据看板API端点")
print("=" * 80)
print()

# 测试基础端点
print("1. 测试基础健康检查：")
test_endpoint("GET", f"{BASE_URL}/health")
test_endpoint("GET", f"{BASE_URL}/")
print()

# 测试数据看板API
print("2. 测试数据看板API端点：")
endpoints = [
    ("GET", f"{BASE_URL}/api/v1/communications/", None),
    ("GET", f"{BASE_URL}/api/v1/ptp/", None),
    ("GET", f"{BASE_URL}/api/v1/quality-inspections/", None),
    ("GET", f"{BASE_URL}/api/v1/performance/collector/1", {"start_date": "2025-01-01", "end_date": "2025-01-12", "period": "daily"}),
    ("GET", f"{BASE_URL}/api/v1/alerts/collector/1", None),
]

all_ok = True
for method, url, params in endpoints:
    if not test_endpoint(method, url, params):
        all_ok = False

print()
print("=" * 80)
if all_ok:
    print("✅ 所有API端点都可用！")
    print("   如果前端还是404，请检查前端请求的URL是否正确")
else:
    print("❌ 部分API端点不可用！")
    print()
    print("🔧 修复步骤：")
    print("   1. 找到运行后端的终端窗口")
    print("   2. 按 Ctrl+C 停止服务")
    print("   3. 重新启动：")
    print("      cd backend")
    print("      source venv/bin/activate")
    print("      python -m uvicorn app.main:app --reload --host 0.0.0.0 --port 8000")
    print("   4. 等待看到 'Application startup complete'")
    print("   5. 再次运行此测试脚本验证")
print("=" * 80)

