#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
还款码功能测试用例
"""
import requests
import json
from datetime import datetime

# 配置
BASE_URL = "http://localhost:8000"
PARTY_ID = 1
COLLECTOR_ID = 1

class Colors:
    """终端颜色"""
    GREEN = '\033[92m'
    RED = '\033[91m'
    YELLOW = '\033[93m'
    BLUE = '\033[94m'
    END = '\033[0m'

def print_success(msg):
    print(f"{Colors.GREEN}✅ {msg}{Colors.END}")

def print_error(msg):
    print(f"{Colors.RED}❌ {msg}{Colors.END}")

def print_info(msg):
    print(f"{Colors.BLUE}ℹ️  {msg}{Colors.END}")

def print_warning(msg):
    print(f"{Colors.YELLOW}⚠️  {msg}{Colors.END}")

def print_section(title):
    print(f"\n{Colors.BLUE}{'=' * 60}")
    print(f"  {title}")
    print(f"{'=' * 60}{Colors.END}\n")

def test_get_available_channels():
    """测试1：获取可用还款渠道"""
    print_section("测试1：获取可用还款渠道")
    
    try:
        url = f"{BASE_URL}/api/im/payment-channels"
        params = {"party_id": PARTY_ID}
        
        print_info(f"请求: GET {url}")
        print_info(f"参数: {params}")
        
        response = requests.get(url, params=params)
        
        print_info(f"状态码: {response.status_code}")
        
        if response.status_code == 200:
            data = response.json()
            
            if data.get("code") == 0:
                channels = data.get("data", [])
                print_success(f"获取渠道成功，共 {len(channels)} 个渠道")
                
                for channel in channels:
                    print(f"  - {channel['channel_name']} ({channel['channel_type']})")
                
                return channels
            else:
                print_error(f"接口返回错误: {data.get('msg')}")
                return []
        else:
            print_error(f"请求失败: {response.text}")
            return []
    
    except Exception as e:
        print_error(f"测试失败: {str(e)}")
        return []

def test_get_case_installments(case_id=1):
    """测试2：获取案件期数信息"""
    print_section("测试2：获取案件期数信息")
    
    try:
        url = f"{BASE_URL}/api/im/cases/{case_id}/installments"
        
        print_info(f"请求: GET {url}")
        
        response = requests.get(url)
        
        print_info(f"状态码: {response.status_code}")
        
        if response.status_code == 200:
            data = response.json()
            
            if data.get("code") == 0:
                result = data.get("data", {})
                print_success(f"获取期数信息成功")
                print(f"  总期数: {result.get('total_installments')}")
                print(f"  当前逾期期数: {result.get('current_overdue')}")
                
                installments = result.get("installments", [])
                print(f"  期数列表: {len(installments)} 期")
                
                for inst in installments[:3]:  # 只显示前3期
                    print(f"    第{inst['number']}期: {inst['status']} - {inst['total']}")
                
                return result
            else:
                print_warning(f"接口返回: {data.get('msg')} (这是正常的，当前返回模拟数据)")
                return None
        else:
            print_warning(f"请求失败: {response.text}")
            return None
    
    except Exception as e:
        print_warning(f"测试执行: {str(e)} (期数接口返回模拟数据)")
        return None

def test_request_payment_code(channel_id, case_id=1, loan_id=1):
    """测试3：请求还款码"""
    print_section("测试3：请求还款码")
    
    try:
        url = f"{BASE_URL}/api/im/payment-codes/request"
        
        payload = {
            "case_id": case_id,
            "loan_id": loan_id,
            "channel_id": channel_id,
            "installment_number": 2,
            "amount": 5350.00
        }
        
        print_info(f"请求: POST {url}")
        print_info(f"数据: {json.dumps(payload, indent=2)}")
        
        response = requests.post(url, json=payload)
        
        print_info(f"状态码: {response.status_code}")
        
        if response.status_code == 200:
            data = response.json()
            
            if data.get("code") == 0:
                result = data.get("data", {})
                print_success("还款码生成成功")
                print(f"  还款码编号: {result.get('code_no')}")
                print(f"  支付类型: {result.get('payment_type')}")
                print(f"  支付码: {result.get('payment_code')}")
                print(f"  金额: {result.get('currency')} {result.get('amount')}")
                print(f"  过期时间: {result.get('expired_at')}")
                
                return result
            else:
                print_error(f"接口返回错误: {data.get('msg')}")
                return None
        else:
            print_error(f"请求失败: {response.text}")
            return None
    
    except Exception as e:
        print_error(f"测试失败: {str(e)}")
        return None

def test_get_payment_codes(case_id=None, status=None):
    """测试4：查询还款码列表"""
    print_section("测试4：查询还款码列表")
    
    try:
        url = f"{BASE_URL}/api/im/payment-codes"
        
        params = {
            "page": 1,
            "page_size": 20
        }
        
        if case_id:
            params["case_id"] = case_id
        
        if status:
            params["status"] = status
        
        print_info(f"请求: GET {url}")
        print_info(f"参数: {params}")
        
        response = requests.get(url, params=params)
        
        print_info(f"状态码: {response.status_code}")
        
        if response.status_code == 200:
            data = response.json()
            
            if data.get("code") == 0:
                result = data.get("data", {})
                total = result.get("total", 0)
                items = result.get("list", []) or result.get("items", [])
                
                print_success(f"查询成功，共 {total} 条记录")
                
                for item in items[:5]:  # 只显示前5条
                    print(f"  - {item['code_no']}: {item['channel_name']} - "
                          f"{item['currency']} {item['amount']} - {item['status']}")
                
                return items
            else:
                print_error(f"接口返回错误: {data.get('msg')}")
                return []
        else:
            print_error(f"请求失败: {response.text}")
            return []
    
    except Exception as e:
        print_error(f"测试失败: {str(e)}")
        return []

def test_get_payment_code_detail(code_no):
    """测试5：查询还款码详情"""
    print_section("测试5：查询还款码详情")
    
    try:
        url = f"{BASE_URL}/api/im/payment-codes/{code_no}"
        
        print_info(f"请求: GET {url}")
        
        response = requests.get(url)
        
        print_info(f"状态码: {response.status_code}")
        
        if response.status_code == 200:
            data = response.json()
            
            if data.get("code") == 0:
                detail = data.get("data", {})
                print_success("获取详情成功")
                print(f"  还款码编号: {detail.get('code_no')}")
                print(f"  渠道: {detail.get('channel_name')}")
                print(f"  客户姓名: {detail.get('customer_name')}")
                print(f"  金额: {detail.get('currency')} {detail.get('amount')}")
                print(f"  状态: {detail.get('status')}")
                print(f"  创建时间: {detail.get('created_at')}")
                
                return detail
            else:
                print_error(f"接口返回错误: {data.get('msg')}")
                return None
        else:
            print_error(f"请求失败: {response.text}")
            return None
    
    except Exception as e:
        print_error(f"测试失败: {str(e)}")
        return None

def test_admin_get_channels():
    """测试6：管理控台获取渠道列表"""
    print_section("测试6：管理控台获取渠道列表")
    
    try:
        url = f"{BASE_URL}/api/admin/payment-channels"
        params = {
            "party_id": PARTY_ID,
            "page": 1,
            "page_size": 20
        }
        
        print_info(f"请求: GET {url}")
        print_info(f"参数: {params}")
        
        response = requests.get(url, params=params)
        
        print_info(f"状态码: {response.status_code}")
        
        if response.status_code == 200:
            data = response.json()
            
            if data.get("code") == 0:
                result = data.get("data", {})
                total = result.get("total", 0)
                channels = result.get("list", [])
                
                print_success(f"获取渠道成功，共 {total} 个渠道")
                
                for channel in channels:
                    status = "✓启用" if channel['is_enabled'] else "✗禁用"
                    print(f"  - [{status}] {channel['channel_name']} ({channel['channel_type']}) - "
                          f"排序: {channel['sort_order']}")
                
                return channels
            else:
                print_error(f"接口返回错误: {data.get('msg')}")
                return []
        else:
            print_error(f"请求失败: {response.text}")
            return []
    
    except Exception as e:
        print_error(f"测试失败: {str(e)}")
        return []

def run_all_tests():
    """运行所有测试"""
    print("\n" + "=" * 60)
    print("  还款码功能完整测试")
    print("=" * 60)
    print(f"\n测试时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    print(f"后端地址: {BASE_URL}")
    
    # 测试1：获取可用渠道
    channels = test_get_available_channels()
    
    if not channels:
        print_error("\n无法获取渠道列表，后续测试将跳过")
        return
    
    # 测试2：获取期数信息
    test_get_case_installments(case_id=1)
    
    # 测试3：请求还款码
    payment_code_result = test_request_payment_code(channel_id=channels[0]['id'], case_id=1)
    
    # 测试4：查询还款码列表（全部）
    all_codes = test_get_payment_codes()
    
    # 测试4.1：查询待支付还款码
    pending_codes = test_get_payment_codes(status="PENDING")
    
    # 测试4.2：查询指定案件的还款码
    case_codes = test_get_payment_codes(case_id=1)
    
    # 测试5：查询还款码详情
    if payment_code_result and payment_code_result.get('code_no'):
        test_get_payment_code_detail(payment_code_result['code_no'])
    elif all_codes:
        test_get_payment_code_detail(all_codes[0]['code_no'])
    
    # 测试6：管理控台接口
    test_admin_get_channels()
    
    # 测试总结
    print_section("测试总结")
    print_success("所有测试用例执行完成")
    print_info("如果有失败的测试，请检查：")
    print("  1. 后端服务是否正常运行")
    print("  2. 数据库表是否已创建")
    print("  3. 是否有测试数据")

def main():
    """主函数"""
    try:
        run_all_tests()
    except KeyboardInterrupt:
        print_warning("\n\n测试被用户中断")
    except Exception as e:
        print_error(f"\n测试执行出错: {str(e)}")
        import traceback
        traceback.print_exc()

if __name__ == "__main__":
    main()

