#!/usr/bin/env python3
"""
CCO 登录接口通用测试工具
支持测试 Python 后端和 Java 后端
"""
import requests
import json
from datetime import datetime

# 配置
PYTHON_BACKEND = "http://localhost:8000"
JAVA_BACKEND = "http://localhost:8080"

# 测试账号配置
TEST_ACCOUNTS = {
    "管理控台": [
        {
            "name": "SuperAdmin 账号",
            "loginId": "superadmin",
            "password": "123456",
            "endpoint": "/api/admin/auth/login"  # Python
        },
        {
            "name": "SuperAdmin 账号 (Java)",
            "loginId": "superadmin",
            "password": "123456",
            "endpoint": "/api/v1/admin/auth/login"  # Java
        },
    ],
    "IM端": [
        {
            "name": "BTQ001 - Carlos Méndez",
            "tenantId": "1",
            "collectorId": "BTQ001",
            "password": "123456",
            "endpoint": "/api/v1/im/auth/login"
        },
    ]
}


def detect_backend():
    """检测哪个后端正在运行"""
    backends = {
        "Python": PYTHON_BACKEND,
        "Java": JAVA_BACKEND
    }
    
    available = []
    
    print(f"\n{'='*70}")
    print("检测后端服务状态")
    print(f"{'='*70}")
    
    for name, url in backends.items():
        try:
            # 尝试多个健康检查端点
            endpoints_to_try = [
                f"{url}/api/v1/admin/auth/login",  # 尝试登录接口（POST会返回错误但说明服务在运行）
                f"{url}/",
                f"{url}/health",
                f"{url}/api/v1/health",
                f"{url}/actuator/health"
            ]
            
            for endpoint in endpoints_to_try:
                try:
                    if "login" in endpoint:
                        # 对于登录接口，使用POST请求
                        response = requests.post(endpoint, json={}, timeout=2)
                    else:
                        response = requests.get(endpoint, timeout=2)
                    # 任何响应（包括400/404/500）都说明服务在运行
                    if response.status_code is not None:
                        print(f"✅ {name} 后端运行中: {url}")
                        available.append((name, url))
                        break
                except requests.exceptions.ConnectionError:
                    continue
                except:
                    # 其他异常也说明服务可能在运行
                    print(f"✅ {name} 后端运行中: {url}")
                    available.append((name, url))
                    break
            else:
                print(f"❌ {name} 后端未运行: {url}")
        except Exception as e:
            print(f"❌ {name} 后端未运行: {url}")
    
    return available


def test_login(base_url: str, endpoint: str, credentials: dict, account_name: str):
    """测试登录接口"""
    full_url = f"{base_url}{endpoint}"
    
    print(f"\n{'='*70}")
    print(f"测试账号: {account_name}")
    print(f"接口地址: {full_url}")
    print(f"{'='*70}")
    
    # 准备请求数据
    if "tenantId" in credentials:
        # IM端登录
        request_data = {
            "tenantId": credentials["tenantId"],
            "collectorId": credentials["collectorId"],
            "password": credentials["password"]
        }
        print(f"机构ID: {credentials['tenantId']}, 催员ID: {credentials['collectorId']}")
    else:
        # 管理控台登录
        request_data = {
            "loginId": credentials["loginId"],
            "password": credentials["password"]
        }
        print(f"登录ID: {credentials['loginId']}")
    
    try:
        response = requests.post(
            full_url,
            json=request_data,
            headers={"Content-Type": "application/json"},
            timeout=10
        )
        
        print(f"状态码: {response.status_code}")
        
        if response.status_code == 200:
            data = response.json()
            print(f"✅ 登录成功！")
            print(f"\n响应数据:")
            print(json.dumps(data, indent=2, ensure_ascii=False))
            
            # 验证响应结构
            test_results = []
            
            # 检查不同的响应格式
            # Python格式: {"access_token": "...", "data": {"user": {...}}}
            # Java格式: {"code": 200, "data": {"token": "...", "user": {...}}}
            
            if "access_token" in data:
                test_results.append(("access_token", "✓", data["access_token"][:50] + "..."))
            elif "data" in data and "token" in data["data"]:
                test_results.append(("token", "✓", data["data"]["token"][:50] + "..."))
            else:
                test_results.append(("token/access_token", "✗", "缺失"))
            
            # 检查用户信息
            user_data = None
            if "data" in data and "user" in data["data"]:
                user_data = data["data"]["user"]
            elif "user" in data:
                user_data = data["user"]
            
            if user_data:
                test_results.append(("用户信息", "✓", "存在"))
                for field in ["id", "loginId", "username", "role", "name", "collectorId", "collectorName"]:
                    if field in user_data:
                        test_results.append((f"  {field}", "✓", str(user_data[field])))
            else:
                test_results.append(("用户信息", "✗", "缺失"))
            
            # 打印验证结果
            print(f"\n{'='*70}")
            print("字段验证结果:")
            print(f"{'='*70}")
            print(f"{'字段':<25} {'状态':<8} {'值'}")
            print(f"{'-'*70}")
            for field, status, value in test_results:
                display_value = value if len(str(value)) < 40 else str(value)[:40] + "..."
                print(f"{field:<25} {status:<8} {display_value}")
            print(f"{'='*70}")
            
            passed = sum(1 for _, status, _ in test_results if status == "✓")
            failed = sum(1 for _, status, _ in test_results if status == "✗")
            
            if failed == 0:
                print(f"\n✅ 所有验证通过 ({passed}/{len(test_results)})")
                return True
            else:
                print(f"\n⚠️  部分验证失败 (通过: {passed}, 失败: {failed})")
                return False
        
        else:
            print(f"❌ 登录失败！")
            try:
                error_data = response.json()
                print(f"错误信息: {json.dumps(error_data, indent=2, ensure_ascii=False)}")
            except:
                print(f"响应内容: {response.text}")
            return False
            
    except requests.exceptions.ConnectionError:
        print(f"❌ 连接失败！无法连接到: {base_url}")
        return False
    except requests.exceptions.Timeout:
        print(f"❌ 请求超时！")
        return False
    except Exception as e:
        print(f"❌ 发生错误: {str(e)}")
        import traceback
        traceback.print_exc()
        return False


def main():
    """主函数"""
    print("\n" + "="*70)
    print("CCO 登录接口通用测试工具")
    print(f"测试时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    print("="*70)
    
    # 1. 检测后端服务
    available_backends = detect_backend()
    
    if not available_backends:
        print("\n❌ 没有检测到运行中的后端服务！")
        print("\n请启动后端服务:")
        print("\nPython 后端:")
        print("  cd backend")
        print("  source venv/bin/activate")
        print("  python -m uvicorn app.main:app --reload --host 0.0.0.0 --port 8000")
        print("\nJava 后端:")
        print("  cd backend-java")
        print("  mvn spring-boot:run")
        print("  或在 IDE 中运行 CcoApplication.java")
        return
    
    # 2. 测试每个可用后端
    overall_success = 0
    overall_fail = 0
    
    for backend_name, backend_url in available_backends:
        print(f"\n{'='*70}")
        print(f"测试 {backend_name} 后端: {backend_url}")
        print(f"{'='*70}")
        
        success_count = 0
        fail_count = 0
        
        # 测试管理控台登录
        for account in TEST_ACCOUNTS["管理控台"]:
            # 根据后端类型选择合适的账号
            if backend_name == "Python" and "Java" in account["name"]:
                continue
            if backend_name == "Java" and "Java" not in account["name"]:
                continue
            
            result = test_login(
                base_url=backend_url,
                endpoint=account["endpoint"],
                credentials={k: v for k, v in account.items() if k not in ["name", "endpoint"]},
                account_name=account["name"]
            )
            if result:
                success_count += 1
                overall_success += 1
            else:
                fail_count += 1
                overall_fail += 1
        
        # 测试IM端登录
        for account in TEST_ACCOUNTS["IM端"]:
            result = test_login(
                base_url=backend_url,
                endpoint=account["endpoint"],
                credentials={k: v for k, v in account.items() if k not in ["name", "endpoint"]},
                account_name=account["name"]
            )
            if result:
                success_count += 1
                overall_success += 1
            else:
                fail_count += 1
                overall_fail += 1
        
        print(f"\n{backend_name} 后端测试结果: ✅ {success_count} 成功, ❌ {fail_count} 失败")
    
    # 3. 总结
    print(f"\n{'='*70}")
    print(f"总体测试结果")
    print(f"{'='*70}")
    print(f"✅ 成功: {overall_success} 个测试")
    print(f"❌ 失败: {overall_fail} 个测试")
    print(f"{'='*70}\n")
    
    if overall_fail > 0:
        print("⚠️  部分测试失败，请检查:")
        print("  1. 后端服务是否正常运行")
        print("  2. 数据库是否已初始化")
        print("  3. 账号密码是否正确")
        print("\n初始化数据库命令:")
        print("  cd backend")
        print("  python init_database.py  # 初始化管理控台数据")
        print("  python create_im_collectors.py  # 初始化IM端催员数据")
    else:
        print("🎉 所有测试通过！")


if __name__ == "__main__":
    main()

