#!/usr/bin/env python3
"""
完整版mock服务器 - 加载所有CSV字段数据和生成的案件数据
"""
import json
from http.server import HTTPServer, BaseHTTPRequestHandler
from urllib.parse import urlparse, parse_qs
import os

# 加载完整数据
script_dir = os.path.dirname(os.path.abspath(__file__))
with open(os.path.join(script_dir, 'full_mock_data.json'), 'r', encoding='utf-8') as f:
    full_data = json.load(f)

# 加载生成的案件数据
with open(os.path.join(script_dir, 'generated_cases.json'), 'r', encoding='utf-8') as f:
    cases_data = json.load(f)

# 加载自定义字段配置
with open(os.path.join(script_dir, 'custom_fields_config.json'), 'r', encoding='utf-8') as f:
    custom_fields_config = json.load(f)

# 加载催员数据
with open(os.path.join(script_dir, 'collectors_data.json'), 'r', encoding='utf-8') as f:
    collectors_data = json.load(f)

# Mock 数据
MOCK_DATA = {
    "field_groups": full_data["field_groups"],
    "standard_fields": full_data["standard_fields"],
    "tenants": [
        {
            "id": 1,
            "tenant_code": "BTQ",
            "tenant_name": "BTQ（墨西哥）",
            "tenant_name_en": "BTQ (Mexico)",
            "country_code": "MX",
            "country": "墨西哥",
            "timezone": "America/Mexico_City",
            "currency_code": "MXN",
            "contact_person": "Carlos Rodriguez",
            "contact_email": "carlos@btq.mx",
            "contact_phone": "+52 55 1234 5678",
            "is_active": True,
            "loan_type": "单期",
            "created_at": "2024-06-01T00:00:00",
            "updated_at": "2025-01-01T00:00:00"
        },
        {
            "id": 2,
            "tenant_code": "BTSK",
            "tenant_name": "BTSK（印度）",
            "tenant_name_en": "BTSK (India)",
            "country_code": "IN",
            "country": "印度",
            "timezone": "Asia/Kolkata",
            "currency_code": "INR",
            "contact_person": "Raj Kumar",
            "contact_email": "raj@btsk.in",
            "contact_phone": "+91 22 1234 5678",
            "is_active": True,
            "loan_type": "多期",
            "created_at": "2024-08-01T00:00:00",
            "updated_at": "2025-01-01T00:00:00"
        }
    ],
    "cases": cases_data["all_cases"],
    "custom_fields": custom_fields_config["btq_custom_fields"] + custom_fields_config["btsk_custom_fields"],
    "field_dependencies": [],
    "tenant_field_configs": []
}

print(f"✅ 加载完成：{full_data['total_groups']}个分组，{full_data['total_fields']}个字段")
print(f"✅ 案件数据：BTQ {cases_data['summary']['btq_count']}个，BTSK {cases_data['summary']['btsk_count']}个，共 {cases_data['summary']['total_count']}个案件")


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
    
    def do_POST(self):
        parsed_path = urlparse(self.path)
        path = parsed_path.path
        
        # 读取请求体
        content_length = int(self.headers.get('Content-Length', 0))
        post_data = self.rfile.read(content_length).decode('utf-8')
        
        try:
            request_data = json.loads(post_data) if post_data else {}
        except:
            request_data = {}
        
        # IM端登录
        if path == '/api/v1/im/auth/login':
            self._set_headers()
            tenant_id = request_data.get('tenantId', '')
            collector_id = request_data.get('collectorId', '')
            password = request_data.get('password', '')
            
            # 查找催员
            collector = None
            for c in collectors_data['collectors']:
                if (c['tenantId'] == tenant_id and 
                    c['collectorId'] == collector_id and 
                    c['password'] == password):
                    collector = c
                    break
            
            if collector:
                # 生成Token
                import hashlib
                import time
                token_str = f"{collector_id}_{tenant_id}_{time.time()}"
                token = hashlib.md5(token_str.encode()).hexdigest()
                
                # 返回用户信息
                user_info = {
                    'id': collector['id'],
                    'collectorId': collector['collectorId'],
                    'collectorName': collector['collectorName'],
                    'tenantId': collector['tenantId'],
                    'tenantName': collector['tenantName'],
                    'role': collector['role'],
                    'team': collector['team'],
                    'permissions': collector['permissions'],
                    'email': collector.get('email'),
                    'phone': collector.get('phone'),
                    'whatsappConnected': collector.get('whatsappConnected', False),
                    'token': token
                }
                
                response = {
                    'code': 200,
                    'message': '登录成功',
                    'data': {
                        'token': token,
                        'user': user_info
                    }
                }
            else:
                response = {
                    'code': 401,
                    'message': '机构ID、催员ID或密码错误'
                }
            
            self.wfile.write(json.dumps(response, ensure_ascii=False).encode())
        
        # IM端登出
        elif path == '/api/v1/im/auth/logout':
            self._set_headers()
            response = {
                'code': 200,
                'message': '登出成功'
            }
            self.wfile.write(json.dumps(response, ensure_ascii=False).encode())
        
        else:
            self._set_headers(404)
            response = {"detail": "Not found"}
            self.wfile.write(json.dumps(response, ensure_ascii=False).encode())

    def do_GET(self):
        parsed_path = urlparse(self.path)
        path = parsed_path.path
        query_params = parse_qs(parsed_path.query)

        # 路由处理
        if path == '/' or path == '/api/v1':
            self._set_headers()
            response = {"message": "CCO System API (Full Data)", "version": "1.0.0", "total_fields": len(MOCK_DATA["standard_fields"])}
            self.wfile.write(json.dumps(response, ensure_ascii=False).encode())
        
        elif path == '/health':
            self._set_headers()
            response = {"status": "healthy", "fields_count": len(MOCK_DATA["standard_fields"])}
            self.wfile.write(json.dumps(response, ensure_ascii=False).encode())
        
        elif path == '/api/v1/field-groups':
            self._set_headers()
            response = {"data": MOCK_DATA["field_groups"]}
            self.wfile.write(json.dumps(response, ensure_ascii=False).encode())
        
        elif path == '/api/v1/standard-fields':
            self._set_headers()
            # 支持按分组筛选
            field_group_id = query_params.get('field_group_id', [None])[0]
            fields = MOCK_DATA["standard_fields"]
            if field_group_id:
                fields = [f for f in fields if f["field_group_id"] == int(field_group_id)]
            response = {"data": fields}
            self.wfile.write(json.dumps(response, ensure_ascii=False).encode())
        
        elif path == '/api/v1/custom-fields':
            self._set_headers()
            # 支持按甲方筛选
            tenant_id = query_params.get('tenant_id', [None])[0]
            fields = MOCK_DATA["custom_fields"]
            if tenant_id:
                fields = [f for f in fields if f["tenant_id"] == int(tenant_id)]
            response = {"data": fields}
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
    print(f'\n🚀 CCO Mock API Server 启动成功！（完整数据版）')
    print(f'📡 服务地址: http://localhost:{port}')
    print(f'📖 API 文档: http://localhost:{port}/api/v1')
    print(f'💚 健康检查: http://localhost:{port}/health')
    print(f'📊 字段统计: {len(MOCK_DATA["standard_fields"])}个标准字段，{len(MOCK_DATA["field_groups"])}个分组')
    print(f'📋 案件统计: {len(MOCK_DATA["cases"])}个案件（BTQ: {cases_data["summary"]["btq_count"]}个，BTSK: {cases_data["summary"]["btsk_count"]}个）')
    print(f'🏢 甲方: {len(MOCK_DATA["tenants"])}个（BTQ-墨西哥单期，BTSK-印度多期）')
    print(f'\n按 Ctrl+C 停止服务...\n')
    try:
        httpd.serve_forever()
    except KeyboardInterrupt:
        print('\n👋 服务已停止')
        httpd.shutdown()


if __name__ == '__main__':
    run_server()

