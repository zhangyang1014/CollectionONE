"""
简单的Mock服务器 - 用于演示前端界面
无需安装复杂依赖，使用Python内置库
从CSV文件导入的完整字段数据
"""
from http.server import BaseHTTPRequestHandler, HTTPServer
import json
from urllib.parse import urlparse, parse_qs
from datetime import datetime
from mock_field_data import FIELD_GROUPS, STANDARD_FIELDS

# Mock 数据 - 甲方
TENANTS = [
    {"id": 1, "tenant_code": "TENANT001", "tenant_name": "示例甲方A", "tenant_name_en": "Demo Tenant A", "country_code": "CN", "timezone": "Asia/Shanghai", "currency_code": "CNY", "is_active": True, "created_at": "2025-01-01T00:00:00", "updated_at": "2025-01-01T00:00:00"},
    {"id": 2, "tenant_code": "TENANT002", "tenant_name": "示例甲方B", "tenant_name_en": "Demo Tenant B", "country_code": "PH", "timezone": "Asia/Manila", "currency_code": "PHP", "is_active": True, "created_at": "2025-01-01T00:00:00", "updated_at": "2025-01-01T00:00:00"},
]

# Mock 数据 - 案件
CASES = [
    {"id": 1, "case_id": "CASE001", "tenant_id": 1, "loan_id": "LOAN001", "user_id": "USER001", "case_status": "进行中", "standard_fields": {"user_name": "张三", "mobile_number": "+86 13800138000", "outstanding_amount": "5000", "overdue_days": "15"}, "custom_fields": {}, "created_at": "2025-01-01T00:00:00", "updated_at": "2025-01-01T00:00:00"},
    {"id": 2, "case_id": "CASE002", "tenant_id": 1, "loan_id": "LOAN002", "user_id": "USER002", "case_status": "已结清", "standard_fields": {"user_name": "李四", "mobile_number": "+86 13900139000", "outstanding_amount": "0", "overdue_days": "0"}, "custom_fields": {}, "created_at": "2025-01-01T00:00:00", "updated_at": "2025-01-01T00:00:00"},
]

class APIHandler(BaseHTTPRequestHandler):
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
        
        if path == '/api/v1/field-groups':
            self._set_headers()
            self.wfile.write(json.dumps({"data": FIELD_GROUPS}).encode())
        elif path == '/api/v1/standard-fields':
            # 支持按分组筛选
            group_id = query_params.get('field_group_id')
            if group_id:
                group_id = int(group_id[0])
                filtered = [f for f in STANDARD_FIELDS if f['field_group_id'] == group_id]
                self._set_headers()
                self.wfile.write(json.dumps({"data": filtered}).encode())
            else:
                self._set_headers()
                self.wfile.write(json.dumps({"data": STANDARD_FIELDS}).encode())
        elif path == '/api/v1/tenants':
            self._set_headers()
            self.wfile.write(json.dumps({"data": TENANTS}).encode())
        elif path == '/api/v1/cases':
            self._set_headers()
            self.wfile.write(json.dumps({"data": CASES}).encode())
        elif path.startswith('/api/v1/cases/'):
            self._set_headers()
            case_id = int(path.split('/')[-1])
            case = next((c for c in CASES if c['id'] == case_id), None)
            if case:
                self.wfile.write(json.dumps({"data": case}).encode())
            else:
                self._set_headers(404)
                self.wfile.write(json.dumps({"detail": "案件不存在"}).encode())
        elif path == '/api/v1/custom-fields':
            self._set_headers()
            self.wfile.write(json.dumps({"data": []}).encode())
        elif path == '/api/v1/field-dependencies':
            self._set_headers()
            self.wfile.write(json.dumps({"data": []}).encode())
        elif path.startswith('/api/v1/tenants/') and path.endswith('/field-configs'):
            self._set_headers()
            self.wfile.write(json.dumps({"data": []}).encode())
        else:
            self._set_headers(404)
            self.wfile.write(json.dumps({"detail": "Not found"}).encode())

    def log_message(self, format, *args):
        print(f"[{datetime.now().strftime('%Y-%m-%d %H:%M:%S')}] {format % args}")

def run(port=8000):
    server_address = ('', port)
    httpd = HTTPServer(server_address, APIHandler)
    print(f'✅ Mock API 服务器启动成功！')
    print(f'📡 监听地址: http://localhost:{port}')
    print(f'📝 API文档: http://localhost:{port}/api/v1/')
    print(f'🔄 前端地址: http://localhost:5173')
    print(f'📊 字段数据: 已导入 {len(FIELD_GROUPS)} 个字段分组, {len(STANDARD_FIELDS)} 个标准字段')
    print(f'\n按 Ctrl+C 停止服务器\n')
    httpd.serve_forever()

if __name__ == '__main__':
    run()

