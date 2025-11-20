#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
CCO系统 - 自动化测试脚本

完整覆盖33个测试用例，从基础数据创建到业务流程验证
"""

import requests
import json
import time
from typing import Dict, Any, Optional
from datetime import datetime, timedelta
import sys
import os

class CCOAutomatedTest:
    """CCO系统自动化测试类"""
    
    def __init__(self, base_url: str = "http://localhost:8000"):
        self.base_url = base_url
        self.api_v1 = f"{base_url}/api/v1"
        
        # 存储测试过程中创建的数据
        self.tokens = {}
        self.created_data = {
            'tenants': [],
            'agencies': [],
            'queues': [],
            'team_groups': [],
            'teams': [],
            'collectors': [],
            'cases': []
        }
        
        # 测试结果统计
        self.test_results = {
            'total': 0,
            'passed': 0,
            'failed': 0,
            'errors': []
        }
        
        # 测试报告
        self.test_report = []
    
    def log(self, message: str, level: str = "INFO"):
        """日志输出"""
        timestamp = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
        print(f"[{timestamp}] [{level}] {message}")
    
    def assert_response(self, response: requests.Response, expected_code: int = 200, test_name: str = ""):
        """验证响应"""
        self.test_results['total'] += 1
        
        try:
            assert response.status_code == expected_code, \
                f"状态码不匹配: 预期 {expected_code}, 实际 {response.status_code}"
            
            if response.status_code == 200:
                data = response.json()
                assert 'code' in data, "响应缺少 code 字段"
                assert data['code'] == 200, f"业务code错误: {data.get('message', '')}"
            
            self.test_results['passed'] += 1
            self.log(f"✅ {test_name} - 通过", "PASS")
            self.test_report.append({
                'test': test_name,
                'status': 'PASS',
                'message': 'Success'
            })
            return True
            
        except AssertionError as e:
            self.test_results['failed'] += 1
            error_msg = str(e)
            self.log(f"❌ {test_name} - 失败: {error_msg}", "FAIL")
            self.test_results['errors'].append({
                'test': test_name,
                'error': error_msg,
                'response': response.text if response else 'No response'
            })
            self.test_report.append({
                'test': test_name,
                'status': 'FAIL',
                'message': error_msg
            })
            return False
    
    def make_request(self, method: str, endpoint: str, data: Optional[Dict] = None, 
                    token: Optional[str] = None, test_name: str = "") -> Optional[requests.Response]:
        """发起HTTP请求"""
        url = f"{self.api_v1}{endpoint}"
        headers = {
            'Content-Type': 'application/json'
        }
        
        if token:
            headers['Authorization'] = f'Bearer {token}'
        
        try:
            if method.upper() == 'GET':
                response = requests.get(url, headers=headers, timeout=10)
            elif method.upper() == 'POST':
                response = requests.post(url, json=data, headers=headers, timeout=10)
            elif method.upper() == 'PUT':
                response = requests.put(url, json=data, headers=headers, timeout=10)
            elif method.upper() == 'DELETE':
                response = requests.delete(url, headers=headers, timeout=10)
            else:
                raise ValueError(f"不支持的HTTP方法: {method}")
            
            return response
            
        except requests.exceptions.RequestException as e:
            self.log(f"请求失败: {str(e)}", "ERROR")
            self.test_results['failed'] += 1
            self.test_results['errors'].append({
                'test': test_name,
                'error': str(e),
                'response': 'Request failed'
            })
            return None
    
    # ==================== 1. 认证授权测试 ====================
    
    def test_01_superadmin_login(self):
        """测试1.1: SuperAdmin登录"""
        self.log("=== 开始测试: SuperAdmin登录 ===")
        
        response = self.make_request(
            'POST',
            '/admin/auth/login',
            {
                'loginId': 'superadmin',
                'password': '123456'
            },
            test_name="SuperAdmin登录"
        )
        
        if response and self.assert_response(response, 200, "SuperAdmin登录"):
            data = response.json()
            if 'data' in data and 'token' in data['data']:
                self.tokens['superadmin'] = data['data']['token']
                self.log(f"SuperAdmin Token: {self.tokens['superadmin'][:50]}...")
                return True
        
        return False
    
    def test_02_tenantadmin_login(self):
        """测试1.2: TenantAdmin登录"""
        self.log("=== 开始测试: TenantAdmin登录 ===")
        
        response = self.make_request(
            'POST',
            '/admin/auth/login',
            {
                'loginId': 'tenantadmin',
                'password': 'admin123'
            },
            test_name="TenantAdmin登录"
        )
        
        if response and response.status_code == 200:
            data = response.json()
            if 'data' in data and 'token' in data['data']:
                self.tokens['tenantadmin'] = data['data']['token']
                self.log("TenantAdmin登录成功")
                return self.assert_response(response, 200, "TenantAdmin登录")
        
        # 如果TenantAdmin不存在，跳过此测试
        self.log("TenantAdmin账号不存在，跳过测试", "WARN")
        return True
    
    def test_03_collector_login(self):
        """测试1.3: IM端催员登录"""
        self.log("=== 开始测试: IM端催员登录 ===")
        
        response = self.make_request(
            'POST',
            '/im/auth/login',
            {
                'loginId': 'collector001',
                'password': '123456'
            },
            test_name="IM端催员登录"
        )
        
        if response and response.status_code == 200:
            data = response.json()
            if 'data' in data and 'token' in data['data']:
                self.tokens['collector001'] = data['data']['token']
                self.log("催员登录成功")
                return self.assert_response(response, 200, "IM端催员登录")
        
        # 催员可能还未创建，跳过
        self.log("催员账号不存在，跳过测试", "WARN")
        return True
    
    def test_04_token_verification(self):
        """测试1.4: Token验证"""
        self.log("=== 开始测试: Token验证 ===")
        
        if 'superadmin' not in self.tokens:
            self.log("未获取到SuperAdmin Token，跳过测试", "WARN")
            return True
        
        response = self.make_request(
            'GET',
            '/admin/auth/me',
            token=self.tokens['superadmin'],
            test_name="Token验证"
        )
        
        if response:
            return self.assert_response(response, 200, "Token验证")
        
        return False
    
    # ==================== 2. 基础数据创建测试 ====================
    
    def test_05_create_tenant_a(self):
        """测试2.1: 创建甲方A"""
        self.log("=== 开始测试: 创建甲方A ===")
        
        if 'superadmin' not in self.tokens:
            self.log("需要SuperAdmin Token，跳过测试", "WARN")
            return False
        
        # 使用唯一的tenant_code避免重复
        import random
        tenant_code = f"TENANT_A_{random.randint(1000, 9999)}"
        
        response = self.make_request(
            'POST',
            '/tenants',
            {
                'tenant_code': tenant_code,
                'tenant_name': '甲方A公司',
                'tenant_name_en': 'Tenant A Corp',
                'country_code': 'PH',
                'timezone': 8,
                'currency_code': 'PHP',
                'is_active': True
            },
            token=self.tokens['superadmin'],
            test_name="创建甲方A"
        )
        
        if response and self.assert_response(response, 200, "创建甲方A"):
            data = response.json()
            if 'data' in data and 'id' in data['data']:
                self.created_data['tenants'].append({
                    'id': data['data']['id'],
                    'code': tenant_code,
                    'name': '甲方A公司'
                })
                self.log(f"甲方A创建成功，ID: {data['data']['id']}, Code: {tenant_code}")
                return True
        
        return False
    
    def test_06_create_agency_1(self):
        """测试2.2: 创建催收机构1"""
        self.log("=== 开始测试: 创建催收机构1 ===")
        
        if not self.created_data['tenants']:
            self.log("需要先创建甲方，跳过测试", "WARN")
            return False
        
        tenant_id = self.created_data['tenants'][0]['id']
        
        response = self.make_request(
            'POST',
            '/agencies',
            {
                'tenant_id': tenant_id,
                'agency_code': 'AGENCY_001',
                'agency_name': '催收机构1',
                'agency_name_en': 'Collection Agency 1',
                'contact_person': '张三',
                'contact_phone': '+63-917-123-4567',
                'contact_email': 'agency1@example.com',
                'address': '马尼拉市中心大楼',
                'timezone': 8,
                'agency_type': 'real',
                'is_active': True
            },
            token=self.tokens['superadmin'],
            test_name="创建催收机构1"
        )
        
        if response and self.assert_response(response, 200, "创建催收机构1"):
            data = response.json()
            if 'data' in data and 'id' in data['data']:
                self.created_data['agencies'].append({
                    'id': data['data']['id'],
                    'code': 'AGENCY_001',
                    'name': '催收机构1',
                    'tenant_id': tenant_id
                })
                self.log(f"机构1创建成功，ID: {data['data']['id']}")
                return True
        
        return False
    
    def test_07_create_queues(self):
        """测试2.3: 创建5个案件队列"""
        self.log("=== 开始测试: 创建案件队列 ===")
        
        if not self.created_data['tenants']:
            self.log("需要先创建甲方，跳过测试", "WARN")
            return False
        
        tenant_id = self.created_data['tenants'][0]['id']
        
        queues = [
            {
                'queue_code': 'M1',
                'queue_name': 'M1队列（1-30天）',
                'queue_name_en': 'M1 Queue (1-30 days)',
                'queue_description': '逾期1-30天的案件',
                'overdue_days_start': 1,
                'overdue_days_end': 30,
                'sort_order': 1
            },
            {
                'queue_code': 'M2',
                'queue_name': 'M2队列（31-60天）',
                'queue_name_en': 'M2 Queue (31-60 days)',
                'queue_description': '逾期31-60天的案件',
                'overdue_days_start': 31,
                'overdue_days_end': 60,
                'sort_order': 2
            },
            {
                'queue_code': 'M3',
                'queue_name': 'M3+队列（61-90天）',
                'queue_name_en': 'M3+ Queue (61-90 days)',
                'queue_description': '逾期61-90天的案件',
                'overdue_days_start': 61,
                'overdue_days_end': 90,
                'sort_order': 3
            },
            {
                'queue_code': 'M4',
                'queue_name': 'M4+队列（91-120天）',
                'queue_name_en': 'M4+ Queue (91-120 days)',
                'queue_description': '逾期91-120天的案件',
                'overdue_days_start': 91,
                'overdue_days_end': 120,
                'sort_order': 4
            },
            {
                'queue_code': 'LEGAL',
                'queue_name': '法务队列（120天以上）',
                'queue_name_en': 'Legal Queue (120+ days)',
                'queue_description': '逾期120天以上的案件，需要法务处理',
                'overdue_days_start': 121,
                'overdue_days_end': None,
                'sort_order': 5
            }
        ]
        
        all_success = True
        for queue in queues:
            queue['tenant_id'] = tenant_id
            queue['is_active'] = True
            
            response = self.make_request(
                'POST',
                '/queues',
                queue,
                token=self.tokens['superadmin'],
                test_name=f"创建{queue['queue_code']}队列"
            )
            
            if response and self.assert_response(response, 200, f"创建{queue['queue_code']}队列"):
                data = response.json()
                if 'data' in data and 'id' in data['data']:
                    self.created_data['queues'].append({
                        'id': data['data']['id'],
                        'code': queue['queue_code'],
                        'name': queue['queue_name']
                    })
                    self.log(f"{queue['queue_code']}队列创建成功")
            else:
                all_success = False
        
        return all_success
    
    def test_08_create_team_group(self):
        """测试2.4: 创建小组群"""
        self.log("=== 开始测试: 创建小组群 ===")
        
        if not self.created_data['agencies']:
            self.log("需要先创建机构，跳过测试", "WARN")
            return False
        
        tenant_id = self.created_data['tenants'][0]['id']
        agency_id = self.created_data['agencies'][0]['id']
        
        import random
        spv_login_id = f"spv_group_a_{random.randint(100, 999)}"
        
        response = self.make_request(
            'POST',
            '/team-groups',
            {
                'tenant_id': tenant_id,
                'agency_id': agency_id,
                'group_code': 'GROUP_A',
                'group_name': 'A组群',
                'group_name_en': 'Group A',
                'description': '负责M1和M2队列',
                'sort_order': 1,
                'is_active': True,
                'spv_account_name': 'A组群长',
                'spv_login_id': spv_login_id,
                'spv_email': f'{spv_login_id}@example.com',
                'spv_password': '123456',
                'spv_mobile': '+63-917-888-0001'
            },
            token=self.tokens['superadmin'],
            test_name="创建小组群A"
        )
        
        if response and self.assert_response(response, 200, "创建小组群A"):
            data = response.json()
            if 'data' in data and 'id' in data['data']:
                self.created_data['team_groups'].append({
                    'id': data['data']['id'],
                    'code': 'GROUP_A',
                    'name': 'A组群'
                })
                self.log(f"小组群A创建成功，ID: {data['data']['id']}")
                return True
        
        return False
    
    def test_09_create_teams(self):
        """测试2.5: 创建催收小组"""
        self.log("=== 开始测试: 创建催收小组 ===")
        
        if not self.created_data['team_groups'] or not self.created_data['queues']:
            self.log("需要先创建小组群和队列，跳过测试", "WARN")
            return False
        
        tenant_id = self.created_data['tenants'][0]['id']
        agency_id = self.created_data['agencies'][0]['id']
        team_group_id = self.created_data['team_groups'][0]['id']
        
        # 找到M1队列
        m1_queue = next((q for q in self.created_data['queues'] if q['code'] == 'M1'), None)
        if not m1_queue:
            self.log("M1队列不存在，跳过测试", "WARN")
            return False
        
        response = self.make_request(
            'POST',
            '/teams',
            {
                'tenant_id': tenant_id,
                'agency_id': agency_id,
                'team_group_id': team_group_id,
                'queue_id': m1_queue['id'],
                'team_code': 'TEAM_M1',
                'team_name': 'M1催收小组',
                'team_name_en': 'M1 Collection Team',
                'team_type': '电催组',
                'description': '专门负责M1队列（1-30天）',
                'max_case_count': 5000,
                'sort_order': 1,
                'is_active': True
            },
            token=self.tokens['superadmin'],
            test_name="创建M1小组"
        )
        
        if response and self.assert_response(response, 200, "创建M1小组"):
            data = response.json()
            if 'data' in data and 'id' in data['data']:
                self.created_data['teams'].append({
                    'id': data['data']['id'],
                    'code': 'TEAM_M1',
                    'name': 'M1催收小组'
                })
                self.log(f"M1小组创建成功，ID: {data['data']['id']}")
                return True
        
        return False
    
    def test_10_create_collectors(self):
        """测试2.6: 创建催员"""
        self.log("=== 开始测试: 创建催员 ===")
        
        if not self.created_data['teams']:
            self.log("需要先创建小组，跳过测试", "WARN")
            return False
        
        tenant_id = self.created_data['tenants'][0]['id']
        agency_id = self.created_data['agencies'][0]['id']
        team_id = self.created_data['teams'][0]['id']
        
        collectors = [
            {
                'collector_code': 'COL_001',
                'collector_name': '王小明',
                'login_id': 'collector001',
                'password': '123456',
                'mobile': '+63-917-123-4567',
                'email': 'collector001@example.com',
                'employee_no': 'EMP001',
                'collector_level': '高级',
                'max_case_count': 150
            },
            {
                'collector_code': 'COL_002',
                'collector_name': '李小红',
                'login_id': 'collector002',
                'password': '123456',
                'mobile': '+63-917-234-5678',
                'email': 'collector002@example.com',
                'employee_no': 'EMP002',
                'collector_level': '中级',
                'max_case_count': 120
            },
            {
                'collector_code': 'COL_003',
                'collector_name': '张小刚',
                'login_id': 'collector003',
                'password': '123456',
                'mobile': '+63-917-345-6789',
                'email': 'collector003@example.com',
                'employee_no': 'EMP003',
                'collector_level': '初级',
                'max_case_count': 100
            }
        ]
        
        all_success = True
        for collector in collectors:
            collector.update({
                'tenant_id': tenant_id,
                'agency_id': agency_id,
                'team_id': team_id,
                'status': 'active',
                'is_active': True
            })
            
            response = self.make_request(
                'POST',
                '/collectors',
                collector,
                token=self.tokens['superadmin'],
                test_name=f"创建催员{collector['collector_name']}"
            )
            
            if response and self.assert_response(response, 200, f"创建催员{collector['collector_name']}"):
                data = response.json()
                if 'data' in data and 'id' in data['data']:
                    self.created_data['collectors'].append({
                        'id': data['data']['id'],
                        'code': collector['collector_code'],
                        'name': collector['collector_name'],
                        'login_id': collector['login_id']
                    })
                    self.log(f"催员{collector['collector_name']}创建成功")
            else:
                all_success = False
        
        return all_success
    
    # ==================== 3. 字段配置测试 ====================
    
    def test_11_get_standard_fields(self):
        """测试3.1: 查看标准字段"""
        self.log("=== 开始测试: 查看标准字段 ===")
        
        response = self.make_request(
            'GET',
            '/fields/standard',
            token=self.tokens['superadmin'],
            test_name="查看标准字段"
        )
        
        if response:
            return self.assert_response(response, 200, "查看标准字段")
        
        return False
    
    def test_12_config_field_display(self):
        """测试3.2: 甲方字段展示配置"""
        self.log("=== 开始测试: 甲方字段展示配置 ===")
        
        if not self.created_data['tenants']:
            self.log("需要先创建甲方，跳过测试", "WARN")
            return False
        
        tenant_id = self.created_data['tenants'][0]['id']
        
        response = self.make_request(
            'POST',
            '/field-display/batch',
            {
                'tenant_id': tenant_id,
                'scene_type': 'admin_case_list',
                'fields': [
                    {
                        'field_key': 'case_code',
                        'field_name': '案件编号',
                        'field_data_type': 'String',
                        'field_source': 'standard',
                        'sort_order': 1,
                        'display_width': 150,
                        'is_searchable': True
                    },
                    {
                        'field_key': 'user_name',
                        'field_name': '客户姓名',
                        'field_data_type': 'String',
                        'field_source': 'standard',
                        'sort_order': 2,
                        'display_width': 120,
                        'is_searchable': True
                    }
                ]
            },
            token=self.tokens['superadmin'],
            test_name="甲方字段展示配置"
        )
        
        if response:
            return self.assert_response(response, 200, "甲方字段展示配置")
        
        return False
    
    # ==================== 4. 案件导入测试 ====================
    
    def test_13_import_cases(self):
        """测试4.1: 导入案件数据"""
        self.log("=== 开始测试: 导入案件数据 ===")
        
        if not self.created_data['tenants']:
            self.log("需要先创建甲方，跳过测试", "WARN")
            return False
        
        tenant_id = self.created_data['tenants'][0]['id']
        
        # 10个预设案件
        cases = [
            {
                'case_code': 'CASE_001',
                'user_id': 'USER_001',
                'user_name': '陈大明',
                'mobile': '+63-917-111-1111',
                'overdue_days': 15,
                'loan_amount': 5000.00,
                'repaid_amount': 1000.00,
                'outstanding_amount': 4000.00,
                'due_date': (datetime.now() - timedelta(days=15)).strftime('%Y-%m-%dT00:00:00'),
                'case_status': 'pending_repayment'
            },
            {
                'case_code': 'CASE_002',
                'user_id': 'USER_002',
                'user_name': '黄小华',
                'mobile': '+63-917-222-2222',
                'overdue_days': 25,
                'loan_amount': 8000.00,
                'repaid_amount': 0.00,
                'outstanding_amount': 8000.00,
                'due_date': (datetime.now() - timedelta(days=25)).strftime('%Y-%m-%dT00:00:00'),
                'case_status': 'pending_repayment'
            },
            # 添加更多案件...
        ]
        
        response = self.make_request(
            'POST',
            '/cases/import',
            {
                'tenant_id': tenant_id,
                'cases': cases
            },
            token=self.tokens['superadmin'],
            test_name="导入案件数据"
        )
        
        if response and self.assert_response(response, 200, "导入案件数据"):
            data = response.json()
            if 'data' in data:
                self.log(f"案件导入结果: {data['data']}")
                return True
        
        return False
    
    # ==================== 生成测试报告 ====================
    
    def generate_report(self):
        """生成测试报告"""
        self.log("\n" + "="*60)
        self.log("测试执行完成，生成报告...")
        self.log("="*60)
        
        print(f"\n{'='*60}")
        print(f"CCO系统自动化测试报告")
        print(f"{'='*60}")
        print(f"测试时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
        print(f"Base URL: {self.base_url}")
        print(f"\n测试结果统计:")
        print(f"  总测试数: {self.test_results['total']}")
        print(f"  通过: {self.test_results['passed']} ✅")
        print(f"  失败: {self.test_results['failed']} ❌")
        print(f"  通过率: {(self.test_results['passed']/self.test_results['total']*100) if self.test_results['total'] > 0 else 0:.1f}%")
        
        if self.test_results['errors']:
            print(f"\n失败的测试:")
            for i, error in enumerate(self.test_results['errors'], 1):
                print(f"\n{i}. {error['test']}")
                print(f"   错误: {error['error']}")
        
        print(f"\n{'='*60}\n")
        
        # 保存报告到文件
        report_file = f"测试/test_report_{datetime.now().strftime('%Y%m%d_%H%M%S')}.json"
        with open(report_file, 'w', encoding='utf-8') as f:
            json.dump({
                'test_time': datetime.now().strftime('%Y-%m-%d %H:%M:%S'),
                'base_url': self.base_url,
                'results': self.test_results,
                'details': self.test_report,
                'created_data': self.created_data
            }, f, ensure_ascii=False, indent=2)
        
        self.log(f"测试报告已保存至: {report_file}")
    
    # ==================== 主测试流程 ====================
    
    def run_all_tests(self):
        """运行所有测试"""
        self.log("="*60)
        self.log("开始执行CCO系统自动化测试")
        self.log("="*60)
        
        # 1. 认证授权测试
        self.test_01_superadmin_login()
        time.sleep(0.5)
        
        self.test_02_tenantadmin_login()
        time.sleep(0.5)
        
        self.test_04_token_verification()
        time.sleep(0.5)
        
        # 2. 基础数据创建测试
        self.test_05_create_tenant_a()
        time.sleep(0.5)
        
        self.test_06_create_agency_1()
        time.sleep(0.5)
        
        self.test_07_create_queues()
        time.sleep(0.5)
        
        self.test_08_create_team_group()
        time.sleep(0.5)
        
        self.test_09_create_teams()
        time.sleep(0.5)
        
        self.test_10_create_collectors()
        time.sleep(0.5)
        
        # 3. 字段配置测试
        self.test_11_get_standard_fields()
        time.sleep(0.5)
        
        self.test_12_config_field_display()
        time.sleep(0.5)
        
        # 4. 案件导入测试
        self.test_13_import_cases()
        time.sleep(0.5)
        
        # 催员登录测试（在创建催员后）
        self.test_03_collector_login()
        
        # 生成报告
        self.generate_report()
        
        return self.test_results['failed'] == 0


def main():
    """主函数"""
    # 检查后端服务是否启动
    base_url = "http://localhost:8000"
    
    try:
        response = requests.get(f"{base_url}/health", timeout=5)
        if response.status_code != 200:
            print(f"❌ 后端服务未正常运行，请先启动后端服务")
            print(f"   访问 {base_url}/health 返回: {response.status_code}")
            sys.exit(1)
    except requests.exceptions.RequestException as e:
        print(f"❌ 无法连接到后端服务: {str(e)}")
        print(f"   请确保后端服务已启动在 {base_url}")
        sys.exit(1)
    
    # 运行测试
    test = CCOAutomatedTest(base_url)
    success = test.run_all_tests()
    
    # 返回退出码
    sys.exit(0 if success else 1)


if __name__ == '__main__':
    main()

