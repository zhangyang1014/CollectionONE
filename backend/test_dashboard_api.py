"""测试数据看板API是否可用"""
import sys
import requests
from pathlib import Path

# 测试API端点
BASE_URL = "http://localhost:8000"

def test_api_endpoints():
    """测试所有数据看板API端点"""
    endpoints = [
        "/api/v1/communications/",
        "/api/v1/ptp/",
        "/api/v1/quality-inspections/",
        "/api/v1/performance/collector/1",
        "/api/v1/analytics/custom-dimensions/fields",
        "/api/v1/alerts/collector/1",
    ]
    
    print("=" * 60)
    print("测试数据看板API端点")
    print("=" * 60)
    
    for endpoint in endpoints:
        url = f"{BASE_URL}{endpoint}"
        try:
            # 对于需要参数的端点，添加测试参数
            if "collector/1" in endpoint:
                if "performance" in endpoint:
                    url += "?start_date=2025-01-01&end_date=2025-01-12&period=daily"
                response = requests.get(url, timeout=5)
            elif "fields" in endpoint:
                url += "?tenant_id=1"
                response = requests.get(url, timeout=5)
            else:
                response = requests.get(url, timeout=5)
            
            status = "✅" if response.status_code < 400 else "❌"
            print(f"{status} {endpoint:50s} - {response.status_code}")
            
            if response.status_code == 404:
                print(f"   ⚠️  404 Not Found - 路由未注册或路径错误")
            elif response.status_code >= 500:
                print(f"   ⚠️  服务器错误: {response.text[:100]}")
                
        except requests.exceptions.ConnectionError:
            print(f"❌ {endpoint:50s} - 无法连接后端服务")
            print(f"   ⚠️  请确认后端服务正在运行: {BASE_URL}")
        except Exception as e:
            print(f"❌ {endpoint:50s} - 错误: {str(e)[:50]}")
    
    print("=" * 60)
    print("\n如果看到404错误，请：")
    print("1. 确认后端服务已启动")
    print("2. 检查 main.py 中是否注册了数据看板API路由")
    print("3. 重启后端服务")

if __name__ == "__main__":
    test_api_endpoints()

