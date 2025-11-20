"""
测试登录接口
用于验证后端登录功能是否正常工作
"""
import requests
import json

# 配置
BASE_URL = "http://localhost:8000"  # 后端服务地址
LOGIN_ENDPOINT = f"{BASE_URL}/api/admin/auth/login"

# 测试账号
TEST_ACCOUNTS = [
    {
        "name": "SuperAdmin 账号",
        "loginId": "superadmin",
        "password": "123456"
    },
    {
        "name": "TenantAdmin 账号",
        "loginId": "tenantadmin",
        "password": "admin123"
    },
    {
        "name": "AgencyAdmin 账号",
        "loginId": "agencyadmin",
        "password": "admin123"
    },
    {
        "name": "TeamLeader 账号",
        "loginId": "teamleader",
        "password": "admin123"
    },
]

def test_login(login_id: str, password: str, account_name: str):
    """测试登录接口"""
    print(f"\n{'='*60}")
    print(f"测试账号: {account_name}")
    print(f"登录ID: {login_id}")
    print(f"{'='*60}")
    
    try:
        # 发送登录请求
        response = requests.post(
            LOGIN_ENDPOINT,
            json={
                "loginId": login_id,
                "password": password
            },
            headers={
                "Content-Type": "application/json"
            },
            timeout=10
        )
        
        # 打印响应状态
        print(f"状态码: {response.status_code}")
        
        # 解析响应
        if response.status_code == 200:
            data = response.json()
            print(f"✅ 登录成功！")
            print(f"\n返回数据:")
            print(json.dumps(data, indent=2, ensure_ascii=False))
            
            # 验证必要字段
            if "access_token" in data:
                print(f"\n✓ Token: {data['access_token'][:50]}...")
            else:
                print(f"\n⚠️  警告: 响应中缺少 access_token")
            
            if "data" in data and "user" in data["data"]:
                user = data["data"]["user"]
                print(f"✓ 用户信息:")
                print(f"  - ID: {user.get('id')}")
                print(f"  - 登录ID: {user.get('loginId')}")
                print(f"  - 用户名: {user.get('username')}")
                print(f"  - 角色: {user.get('role')}")
                print(f"  - 姓名: {user.get('name')}")
            else:
                print(f"\n⚠️  警告: 响应中缺少 user 信息")
            
            return True
        else:
            print(f"❌ 登录失败！")
            print(f"响应内容: {response.text}")
            return False
            
    except requests.exceptions.ConnectionError:
        print(f"❌ 连接失败！")
        print(f"请确保后端服务正在运行: {BASE_URL}")
        print(f"启动命令: cd backend && python -m uvicorn app.main:app --reload --host 0.0.0.0 --port 8000")
        return False
    except requests.exceptions.Timeout:
        print(f"❌ 请求超时！")
        return False
    except Exception as e:
        print(f"❌ 发生错误: {str(e)}")
        return False

def test_health_check():
    """测试后端服务是否运行"""
    print(f"\n{'='*60}")
    print(f"检查后端服务状态")
    print(f"{'='*60}")
    
    try:
        response = requests.get(f"{BASE_URL}/", timeout=5)
        if response.status_code == 200:
            print(f"✅ 后端服务正常运行")
            return True
        else:
            print(f"⚠️  后端服务响应异常: {response.status_code}")
            return False
    except requests.exceptions.ConnectionError:
        print(f"❌ 无法连接到后端服务: {BASE_URL}")
        print(f"\n请先启动后端服务:")
        print(f"  cd backend")
        print(f"  python -m uvicorn app.main:app --reload --host 0.0.0.0 --port 8000")
        return False
    except Exception as e:
        print(f"❌ 检查失败: {str(e)}")
        return False

def main():
    """主函数"""
    print("\n" + "="*60)
    print("CCO 登录接口测试工具")
    print("="*60)
    
    # 1. 检查后端服务
    if not test_health_check():
        print("\n❌ 后端服务未运行，无法继续测试")
        return
    
    # 2. 测试所有账号
    success_count = 0
    fail_count = 0
    
    for account in TEST_ACCOUNTS:
        result = test_login(
            login_id=account["loginId"],
            password=account["password"],
            account_name=account["name"]
        )
        if result:
            success_count += 1
        else:
            fail_count += 1
    
    # 3. 输出测试结果
    print(f"\n{'='*60}")
    print(f"测试结果汇总")
    print(f"{'='*60}")
    print(f"✅ 成功: {success_count} 个账号")
    print(f"❌ 失败: {fail_count} 个账号")
    print(f"{'='*60}\n")
    
    if fail_count > 0:
        print("⚠️  部分账号登录失败，请检查:")
        print("  1. 后端服务是否正常运行")
        print("  2. 数据库是否已初始化")
        print("  3. 账号密码是否正确")
        print("\n初始化数据库命令:")
        print("  cd backend")
        print("  python init_database.py")

if __name__ == "__main__":
    main()

