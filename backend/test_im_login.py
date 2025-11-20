"""
IM端登录接口测试脚本
用于验证IM端催员登录功能是否正常工作
"""
import json
from datetime import datetime
from urllib.request import urlopen, Request
from urllib.error import URLError, HTTPError
import urllib.parse

# 配置
BASE_URL = "http://localhost:8000"  # 后端服务地址
LOGIN_ENDPOINT = f"{BASE_URL}/api/v1/im/auth/login"

# IM端测试账号
TEST_ACCOUNTS = [
    {
        "name": "BTQ001 - Carlos Méndez (高级催员)",
        "tenantId": "1",
        "collectorId": "BTQ001",
        "password": "123456"
    },
    {
        "name": "BTQ002 - María González (催员)",
        "tenantId": "1",
        "collectorId": "BTQ002",
        "password": "123456"
    },
    {
        "name": "BTQ003 - José Ramírez (催员)",
        "tenantId": "1",
        "collectorId": "BTQ003",
        "password": "123456"
    },
    {
        "name": "BTSK001 - Raj Sharma (团队长)",
        "tenantId": "2",
        "collectorId": "BTSK001",
        "password": "123456"
    },
    {
        "name": "BTSK002 - Priya Patel (高级催员)",
        "tenantId": "2",
        "collectorId": "BTSK002",
        "password": "123456"
    },
    {
        "name": "BTSK003 - Amit Kumar (催员)",
        "tenantId": "2",
        "collectorId": "BTSK003",
        "password": "123456"
    },
]


def test_im_login(tenant_id: str, collector_id: str, password: str, account_name: str):
    """测试IM端登录接口"""
    print(f"\n{'='*70}")
    print(f"测试账号: {account_name}")
    print(f"机构ID: {tenant_id}, 催员ID: {collector_id}")
    print(f"{'='*70}")
    
    try:
        # 准备请求数据
        request_data = {
            "tenantId": tenant_id,
            "collectorId": collector_id,
            "password": password
        }
        json_data = json.dumps(request_data).encode('utf-8')
        
        # 发送登录请求
        req = Request(
            LOGIN_ENDPOINT,
            data=json_data,
            headers={
                "Content-Type": "application/json"
            }
        )
        
        response = urlopen(req, timeout=10)
        
        # 打印响应状态
        status_code = response.getcode()
        print(f"状态码: {status_code}")
        
        # 解析响应
        if status_code == 200:
            data = json.loads(response.read().decode('utf-8'))
            print(f"✅ 登录成功！")
            print(f"\n返回数据:")
            print(json.dumps(data, indent=2, ensure_ascii=False))
            
            # 验证必要字段
            test_results = []
            
            # 1. 检查响应结构
            if data.get("code") == 200:
                test_results.append(("响应code", "✓", "200"))
            else:
                test_results.append(("响应code", "✗", f"期望200，实际{data.get('code')}"))
            
            # 2. 检查data字段
            if "data" in data:
                test_results.append(("data字段", "✓", "存在"))
                
                # 3. 检查token
                if "token" in data["data"]:
                    token = data["data"]["token"]
                    test_results.append(("token", "✓", f"{token[:30]}..."))
                else:
                    test_results.append(("token", "✗", "缺失"))
                
                # 4. 检查user字段
                if "user" in data["data"]:
                    user = data["data"]["user"]
                    test_results.append(("user字段", "✓", "存在"))
                    
                    # 验证user的关键字段
                    required_fields = [
                        "id", "collectorId", "collectorName", "tenantId", 
                        "tenantName", "role", "permissions"
                    ]
                    
                    for field in required_fields:
                        if field in user:
                            value = user[field]
                            if field == "permissions":
                                test_results.append((f"user.{field}", "✓", f"{len(value)}个权限"))
                            else:
                                test_results.append((f"user.{field}", "✓", str(value)))
                        else:
                            test_results.append((f"user.{field}", "✗", "缺失"))
                else:
                    test_results.append(("user字段", "✗", "缺失"))
            else:
                test_results.append(("data字段", "✗", "缺失"))
            
            # 打印测试结果表格
            print(f"\n{'='*70}")
            print("字段验证结果:")
            print(f"{'='*70}")
            print(f"{'字段':<25} {'状态':<8} {'值'}")
            print(f"{'-'*70}")
            for field, status, value in test_results:
                print(f"{field:<25} {status:<8} {value}")
            print(f"{'='*70}")
            
            # 统计测试结果
            passed = sum(1 for _, status, _ in test_results if status == "✓")
            failed = sum(1 for _, status, _ in test_results if status == "✗")
            
            if failed == 0:
                print(f"\n✅ 所有验证通过 ({passed}/{len(test_results)})")
                return True
            else:
                print(f"\n⚠️  部分验证失败 (通过: {passed}, 失败: {failed})")
                return False
            
            
    except HTTPError as e:
        print(f"❌ 登录失败！")
        print(f"状态码: {e.code}")
        print(f"响应内容: {e.read().decode('utf-8')}")
        return False
    except URLError as e:
        print(f"❌ 连接失败！")
        print(f"错误: {e.reason}")
        print(f"请确保后端服务正在运行: {BASE_URL}")
        print(f"启动命令: cd backend && python -m uvicorn app.main:app --reload --host 0.0.0.0 --port 8000")
        return False
    except Exception as e:
        print(f"❌ 发生错误: {str(e)}")
        import traceback
        traceback.print_exc()
        return False


def test_health_check():
    """测试后端服务是否运行"""
    print(f"\n{'='*70}")
    print(f"检查后端服务状态")
    print(f"{'='*70}")
    
    try:
        response = urlopen(f"{BASE_URL}/health", timeout=5)
        status_code = response.getcode()
        if status_code == 200:
            print(f"✅ 后端服务正常运行")
            data = json.loads(response.read().decode('utf-8'))
            print(f"响应: {data}")
            return True
        else:
            print(f"⚠️  后端服务响应异常: {status_code}")
            return False
    except URLError as e:
        print(f"❌ 无法连接到后端服务: {BASE_URL}")
        print(f"错误: {e.reason}")
        print(f"\n请先启动后端服务:")
        print(f"  cd backend")
        print(f"  source venv/bin/activate")
        print(f"  python -m uvicorn app.main:app --reload --host 0.0.0.0 --port 8000")
        return False
    except Exception as e:
        print(f"❌ 检查失败: {str(e)}")
        return False


def test_invalid_credentials():
    """测试错误凭据"""
    print(f"\n{'='*70}")
    print(f"测试错误凭据（预期失败）")
    print(f"{'='*70}")
    
    test_cases = [
        {
            "name": "错误的密码",
            "tenantId": "1",
            "collectorId": "BTQ001",
            "password": "wrong_password",
            "expected_status": 401
        },
        {
            "name": "不存在的催员ID",
            "tenantId": "1",
            "collectorId": "NOTEXIST",
            "password": "123456",
            "expected_status": 401
        },
        {
            "name": "错误的机构ID",
            "tenantId": "999",
            "collectorId": "BTQ001",
            "password": "123456",
            "expected_status": 401
        }
    ]
    
    passed = 0
    failed = 0
    
    for test_case in test_cases:
        try:
            print(f"\n测试: {test_case['name']}")
            request_data = {
                "tenantId": test_case["tenantId"],
                "collectorId": test_case["collectorId"],
                "password": test_case["password"]
            }
            json_data = json.dumps(request_data).encode('utf-8')
            
            req = Request(
                LOGIN_ENDPOINT,
                data=json_data,
                headers={"Content-Type": "application/json"}
            )
            
            try:
                response = urlopen(req, timeout=10)
                status_code = response.getcode()
                
                if status_code == test_case["expected_status"]:
                    print(f"  ✓ 正确返回 {status_code}")
                    passed += 1
                else:
                    print(f"  ✗ 期望 {test_case['expected_status']}, 实际 {status_code}")
                    failed += 1
            except HTTPError as e:
                if e.code == test_case["expected_status"]:
                    print(f"  ✓ 正确返回 {e.code}")
                    passed += 1
                else:
                    print(f"  ✗ 期望 {test_case['expected_status']}, 实际 {e.code}")
                    failed += 1
                
        except Exception as e:
            print(f"  ✗ 测试失败: {str(e)}")
            failed += 1
    
    print(f"\n错误凭据测试: 通过 {passed}/{len(test_cases)}")
    return failed == 0


def main():
    """主函数"""
    print("\n" + "="*70)
    print("CCO-IM 端登录接口测试工具")
    print(f"测试时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    print("="*70)
    
    # 1. 检查后端服务
    if not test_health_check():
        print("\n❌ 后端服务未运行，无法继续测试")
        return
    
    # 2. 测试所有账号
    print(f"\n{'='*70}")
    print("测试所有IM端账号登录")
    print(f"{'='*70}")
    
    success_count = 0
    fail_count = 0
    
    for account in TEST_ACCOUNTS:
        result = test_im_login(
            tenant_id=account["tenantId"],
            collector_id=account["collectorId"],
            password=account["password"],
            account_name=account["name"]
        )
        if result:
            success_count += 1
        else:
            fail_count += 1
    
    # 3. 测试错误凭据
    test_invalid_credentials()
    
    # 4. 输出测试结果
    print(f"\n{'='*70}")
    print(f"测试结果汇总")
    print(f"{'='*70}")
    print(f"✅ 成功: {success_count}/{len(TEST_ACCOUNTS)} 个账号")
    print(f"❌ 失败: {fail_count}/{len(TEST_ACCOUNTS)} 个账号")
    print(f"{'='*70}\n")
    
    if fail_count > 0:
        print("⚠️  部分账号登录失败，请检查:")
        print("  1. 后端服务是否正常运行")
        print("  2. 数据库是否已初始化IM端催员数据")
        print("  3. 账号密码是否正确")
        print("\n初始化IM端催员数据命令:")
        print("  cd backend")
        print("  source venv/bin/activate")
        print("  python create_im_collectors.py")
    else:
        print("🎉 所有测试通过！")


if __name__ == "__main__":
    main()

