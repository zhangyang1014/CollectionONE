#!/usr/bin/env python3
"""
简化版mock服务器 - 用于演示CCO系统
不需要额外依赖，使用Python标准库
"""
import json
from http.server import HTTPServer, BaseHTTPRequestHandler
from urllib.parse import urlparse, parse_qs
import datetime

# Mock 数据
MOCK_DATA = {
    "field_groups": [
        {
            "id": 1,
            "group_key": "customer_basic",
            "group_name": "客户基本信息",
            "group_name_en": "Customer Basic Info",
            "parent_id": None,
            "sort_order": 1,
            "is_active": True,
            "created_at": "2025-01-01T00:00:00",
            "updated_at": "2025-01-01T00:00:00"
        },
        {
            "id": 2,
            "group_key": "loan_details",
            "group_name": "贷款详情",
            "group_name_en": "Loan Details",
            "parent_id": None,
            "sort_order": 2,
            "is_active": True,
            "created_at": "2025-01-01T00:00:00",
            "updated_at": "2025-01-01T00:00:00"
        },
        {
            "id": 3,
            "group_key": "collection_record",
            "group_name": "催记",
            "group_name_en": "Collection Record",
            "parent_id": None,
            "sort_order": 3,
            "is_active": True,
            "created_at": "2025-01-01T00:00:00",
            "updated_at": "2025-01-01T00:00:00"
        }
    ],
    "standard_fields": [
        {
            "id": 1,
            "field_key": "user_name",
            "field_name": "客户姓名",
            "field_name_en": "User Name",
            "field_type": "String",
            "field_group_id": 1,
            "is_required": True,
            "is_extended": False,
            "description": "借款人姓名",
            "example_value": "张三",
            "sort_order": 1,
            "is_active": True,
            "is_deleted": False,
            "deleted_at": None,
            "created_at": "2025-01-01T00:00:00",
            "updated_at": "2025-01-01T00:00:00"
        },
        {
            "id": 2,
            "field_key": "mobile_number",
            "field_name": "手机号码",
            "field_name_en": "Mobile Number",
            "field_type": "String",
            "field_group_id": 1,
            "is_required": True,
            "is_extended": False,
            "description": "用户注册手机号",
            "example_value": "+86 13800138000",
            "sort_order": 2,
            "is_active": True,
            "is_deleted": False,
            "deleted_at": None,
            "created_at": "2025-01-01T00:00:00",
            "updated_at": "2025-01-01T00:00:00"
        },
        {
            "id": 3,
            "field_key": "loan_amount",
            "field_name": "贷款金额",
            "field_name_en": "Loan Amount",
            "field_type": "Decimal",
            "field_group_id": 2,
            "is_required": True,
            "is_extended": False,
            "description": "贷款本金",
            "example_value": "10000",
            "sort_order": 1,
            "is_active": True,
            "is_deleted": False,
            "deleted_at": None,
            "created_at": "2025-01-01T00:00:00",
            "updated_at": "2025-01-01T00:00:00"
        }
    ],
    "tenants": [
        {
            "id": 1,
            "tenant_code": "TENANT001",
            "tenant_name": "测试甲方A",
            "tenant_name_en": "Test Tenant A",
            "country_code": "CN",
            "timezone": "Asia/Shanghai",
            "currency_code": "CNY",
            "is_active": True,
            "created_at": "2025-01-01T00:00:00",
            "updated_at": "2025-01-01T00:00:00"
        },
        {
            "id": 2,
            "tenant_code": "TENANT002",
            "tenant_name": "测试甲方B",
            "tenant_name_en": "Test Tenant B",
            "country_code": "PH",
            "timezone": "Asia/Manila",
            "currency_code": "PHP",
            "is_active": True,
            "created_at": "2025-01-01T00:00:00",
            "updated_at": "2025-01-01T00:00:00"
        }
    ],
    "cases": [
        {
            "id": 1,
            "case_id": "CASE001",
            "tenant_id": 1,
            "loan_id": "LOAN001",
            "user_id": "USER001",
            "case_status": "进行中",
            "standard_fields": {
                "user_name": "张三",
                "mobile_number": "+86 13800138000",
                "loan_amount": "10000"
            },
            "custom_fields": {},
            "created_at": "2025-01-01T00:00:00",
            "updated_at": "2025-01-01T00:00:00"
        },
        {
            "id": 2,
            "case_id": "CASE002",
            "tenant_id": 1,
            "loan_id": "LOAN002",
            "user_id": "USER002",
            "case_status": "已结清",
            "standard_fields": {
                "user_name": "李四",
                "mobile_number": "+86 13900139000",
                "loan_amount": "5000"
            },
            "custom_fields": {},
            "created_at": "2025-01-01T00:00:00",
            "updated_at": "2025-01-01T00:00:00"
        }
    ],
    "custom_fields": [],
    "field_dependencies": [],
    "tenant_field_configs": []
}


class MockAPIHandler(BaseHTTPRequestHandler):
    def _set_headers(self, status=200):
        self.send_response(status)
        self.send_header('Content-type', 'application/json')
        self.send_header('Access-Control-Allow-Origin', '*')
        self.send_header('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS')
        self.send_header('Access-Control-Allow-Headers', 'Content-Type, Authorization')
        self.end_headers()

    def do_OPTIONS(self):
        self._set_headers()

    def do_GET(self):
        parsed_path = urlparse(self.path)
        path = parsed_path.path
        query_params = parse_qs(parsed_path.query)

        # 路由处理
        if path == '/' or path == '/api/v1':
            self._set_headers()
            response = {"message": "CCO System API", "version": "1.0.0"}
            self.wfile.write(json.dumps(response, ensure_ascii=False).encode())
        
        elif path == '/health':
            self._set_headers()
            response = {"status": "healthy"}
            self.wfile.write(json.dumps(response, ensure_ascii=False).encode())
        
        elif path == '/api/v1/field-groups':
            self._set_headers()
            response = {"data": MOCK_DATA["field_groups"]}
            self.wfile.write(json.dumps(response, ensure_ascii=False).encode())
        
        elif path == '/api/v1/standard-fields':
            self._set_headers()
            response = {"data": MOCK_DATA["standard_fields"]}
            self.wfile.write(json.dumps(response, ensure_ascii=False).encode())
        
        elif path == '/api/v1/custom-fields':
            self._set_headers()
            response = {"data": MOCK_DATA["custom_fields"]}
            self.wfile.write(json.dumps(response, ensure_ascii=False).encode())
        
        elif path == '/api/v1/tenants':
            self._set_headers()
            response = {"data": MOCK_DATA["tenants"]}
            self.wfile.write(json.dumps(response, ensure_ascii=False).encode())
        
        elif path == '/api/v1/cases':
            self._set_headers()
            tenant_id = query_params.get('tenant_id', [None])[0]
            cases = MOCK_DATA["cases"]
            if tenant_id:
                cases = [c for c in cases if c["tenant_id"] == int(tenant_id)]
            response = {"data": cases}
            self.wfile.write(json.dumps(response, ensure_ascii=False).encode())
        
        elif path == '/api/v1/field-dependencies':
            self._set_headers()
            response = {"data": MOCK_DATA["field_dependencies"]}
            self.wfile.write(json.dumps(response, ensure_ascii=False).encode())
        
        elif path.startswith('/api/v1/tenants/') and path.endswith('/field-configs'):
            self._set_headers()
            response = {"data": MOCK_DATA["tenant_field_configs"]}
            self.wfile.write(json.dumps(response, ensure_ascii=False).encode())
        
        else:
            self._set_headers(404)
            response = {"detail": "Not found"}
            self.wfile.write(json.dumps(response, ensure_ascii=False).encode())

    def log_message(self, format, *args):
        print(f"{self.address_string()} - {format % args}")


def run_server(port=8000):
    server_address = ('', port)
    httpd = HTTPServer(server_address, MockAPIHandler)
    print(f'🚀 CCO Mock API Server 启动成功！')
    print(f'📡 服务地址: http://localhost:{port}')
    print(f'📖 API 文档: http://localhost:{port}/api/v1')
    print(f'💚 健康检查: http://localhost:{port}/health')
    print(f'\n按 Ctrl+C 停止服务...\n')
    try:
        httpd.serve_forever()
    except KeyboardInterrupt:
        print('\n👋 服务已停止')
        httpd.shutdown()


if __name__ == '__main__':
    run_server()

