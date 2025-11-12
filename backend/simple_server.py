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
    {"id": 1, "tenant_code": "TENANT001", "tenant_name": "示例甲方A", "tenant_name_en": "Demo Tenant A", "country_code": "CN", "timezone": 8, "currency_code": "CNY", "is_active": True, "created_at": "2025-01-01T00:00:00", "updated_at": "2025-01-01T00:00:00"},
    {"id": 2, "tenant_code": "TENANT002", "tenant_name": "示例甲方B", "tenant_name_en": "Demo Tenant B", "country_code": "PH", "timezone": 8, "currency_code": "PHP", "is_active": True, "created_at": "2025-01-01T00:00:00", "updated_at": "2025-01-01T00:00:00"},
]

# Mock 数据 - 甲方A的App列表
TENANT_A_APPS = [
    {"id": 1, "tenant_id": 1, "app_code": "APP001", "app_name": "快贷通", "app_name_en": "QuickLoan", "app_type": "mobile", "platform": "iOS", "is_active": True, "created_at": "2024-01-01T00:00:00", "updated_at": "2024-01-01T00:00:00"},
    {"id": 2, "tenant_id": 1, "app_code": "APP002", "app_name": "信用钱包", "app_name_en": "CreditWallet", "app_type": "mobile", "platform": "Android", "is_active": True, "created_at": "2024-02-01T00:00:00", "updated_at": "2024-02-01T00:00:00"},
    {"id": 3, "tenant_id": 1, "app_code": "APP003", "app_name": "现金贷", "app_name_en": "CashLoan", "app_type": "web", "platform": "Web", "is_active": True, "created_at": "2024-03-01T00:00:00", "updated_at": "2024-03-01T00:00:00"},
    {"id": 4, "tenant_id": 1, "app_code": "APP004", "app_name": "分期购", "app_name_en": "InstallmentBuy", "app_type": "mobile", "platform": "iOS", "is_active": True, "created_at": "2024-04-01T00:00:00", "updated_at": "2024-04-01T00:00:00"},
    {"id": 5, "tenant_id": 1, "app_code": "APP005", "app_name": "微贷助手", "app_name_en": "MicroLoanHelper", "app_type": "mobile", "platform": "Android", "is_active": False, "created_at": "2023-12-01T00:00:00", "updated_at": "2024-11-01T00:00:00"},
]

# Mock 数据 - 甲方A的产品列表
TENANT_A_PRODUCTS = [
    {"id": 1, "tenant_id": 1, "product_code": "PROD001", "product_name": "极速贷", "product_name_en": "ExpressLoan", "product_type": "cash_loan", "min_amount": 1000, "max_amount": 50000, "interest_rate": 0.05, "term_days": 30, "is_active": True, "created_at": "2024-01-01T00:00:00", "updated_at": "2024-01-01T00:00:00"},
    {"id": 2, "tenant_id": 1, "product_code": "PROD002", "product_name": "分期贷", "product_name_en": "InstallmentLoan", "product_type": "installment", "min_amount": 5000, "max_amount": 100000, "interest_rate": 0.08, "term_days": 90, "is_active": True, "created_at": "2024-01-15T00:00:00", "updated_at": "2024-01-15T00:00:00"},
    {"id": 3, "tenant_id": 1, "product_code": "PROD003", "product_name": "信用贷", "product_name_en": "CreditLoan", "product_type": "credit_loan", "min_amount": 10000, "max_amount": 200000, "interest_rate": 0.06, "term_days": 180, "is_active": True, "created_at": "2024-02-01T00:00:00", "updated_at": "2024-02-01T00:00:00"},
    {"id": 4, "tenant_id": 1, "product_code": "PROD004", "product_name": "小额贷", "product_name_en": "SmallLoan", "product_type": "cash_loan", "min_amount": 500, "max_amount": 5000, "interest_rate": 0.12, "term_days": 14, "is_active": True, "created_at": "2024-03-01T00:00:00", "updated_at": "2024-03-01T00:00:00"},
    {"id": 5, "tenant_id": 1, "product_code": "PROD005", "product_name": "大额贷", "product_name_en": "LargeLoan", "product_type": "credit_loan", "min_amount": 50000, "max_amount": 500000, "interest_rate": 0.04, "term_days": 365, "is_active": True, "created_at": "2024-04-01T00:00:00", "updated_at": "2024-04-01T00:00:00"},
    {"id": 6, "tenant_id": 1, "product_code": "PROD006", "product_name": "学生贷", "product_name_en": "StudentLoan", "product_type": "installment", "min_amount": 2000, "max_amount": 20000, "interest_rate": 0.03, "term_days": 60, "is_active": True, "created_at": "2024-05-01T00:00:00", "updated_at": "2024-05-01T00:00:00"},
]

# Mock 数据 - 渠道供应商
# 格式: {tenant_id: {channel_type: [suppliers]}}
CHANNEL_SUPPLIERS = {
    1: {
        'sms': [
            {
                'id': 1,
                'tenant_id': 1,
                'channel_type': 'sms',
                'supplier_name': '阿里云短信',
                'api_url': 'https://dysmsapi.aliyuncs.com',
                'api_key': 'LTAI5txxxxxxxxxxxxx',
                'secret_key': 'xxxxxxxxxxxxxxxxxxxxxxxxxxxxx',
                'remark': '阿里云短信服务提供商',
                'sort_order': 0,
                'is_active': True,
                'created_at': '2025-01-01T00:00:00',
                'updated_at': '2025-01-01T00:00:00'
            },
            {
                'id': 2,
                'tenant_id': 1,
                'channel_type': 'sms',
                'supplier_name': '腾讯云短信',
                'api_url': 'https://sms.tencentcloudapi.com',
                'api_key': 'AKIDxxxxxxxxxxxxxxxxxxxxx',
                'secret_key': 'xxxxxxxxxxxxxxxxxxxxxxxxxxxxx',
                'remark': '腾讯云短信服务提供商',
                'sort_order': 1,
                'is_active': True,
                'created_at': '2025-01-01T00:00:00',
                'updated_at': '2025-01-01T00:00:00'
            }
        ],
        'rcs': [
            {
                'id': 3,
                'tenant_id': 1,
                'channel_type': 'rcs',
                'supplier_name': '华为RCS',
                'api_url': 'https://rcs-api.huawei.com',
                'api_key': 'HUAWEIxxxxxxxxxxxxx',
                'secret_key': 'xxxxxxxxxxxxxxxxxxxxxxxxxxxxx',
                'remark': '华为RCS消息服务',
                'sort_order': 0,
                'is_active': True,
                'created_at': '2025-01-01T00:00:00',
                'updated_at': '2025-01-01T00:00:00'
            }
        ],
        'whatsapp': [
            {
                'id': 4,
                'tenant_id': 1,
                'channel_type': 'whatsapp',
                'supplier_name': 'Twilio WhatsApp',
                'api_url': 'https://api.twilio.com',
                'api_key': 'ACxxxxxxxxxxxxxxxxxxxxxxxxxxxxx',
                'secret_key': 'xxxxxxxxxxxxxxxxxxxxxxxxxxxxx',
                'remark': 'Twilio WhatsApp Business API',
                'sort_order': 0,
                'is_active': True,
                'created_at': '2025-01-01T00:00:00',
                'updated_at': '2025-01-01T00:00:00'
            },
            {
                'id': 5,
                'tenant_id': 1,
                'channel_type': 'whatsapp',
                'supplier_name': 'Meta WhatsApp',
                'api_url': 'https://graph.facebook.com',
                'api_key': 'EAAGxxxxxxxxxxxxxxxxxxxxx',
                'secret_key': 'xxxxxxxxxxxxxxxxxxxxxxxxxxxxx',
                'remark': 'Meta WhatsApp Business API',
                'sort_order': 1,
                'is_active': True,
                'created_at': '2025-01-01T00:00:00',
                'updated_at': '2025-01-01T00:00:00'
            }
        ],
        'call': [
            {
                'id': 6,
                'tenant_id': 1,
                'channel_type': 'call',
                'supplier_name': '阿里云语音',
                'api_url': 'https://dyvmsapi.aliyuncs.com',
                'api_key': 'LTAI5txxxxxxxxxxxxx',
                'secret_key': 'xxxxxxxxxxxxxxxxxxxxxxxxxxxxx',
                'remark': '阿里云语音外呼服务',
                'sort_order': 0,
                'is_active': True,
                'created_at': '2025-01-01T00:00:00',
                'updated_at': '2025-01-01T00:00:00'
            }
        ]
    }
}

# 全局供应商ID计数器
_supplier_id_counter = 10

def get_channel_suppliers(tenant_id, channel_type):
    """获取指定甲方和渠道类型的供应商列表"""
    if tenant_id not in CHANNEL_SUPPLIERS:
        return []
    if channel_type not in CHANNEL_SUPPLIERS[tenant_id]:
        return []
    return CHANNEL_SUPPLIERS[tenant_id][channel_type]

# Mock 数据 - 机构作息时间
# 格式: {agency_id: [{day_of_week: 0-6, time_slots: [...]}, ...]}
AGENCY_WORKING_HOURS = {
    1001: [  # 机构ID 1001
        {'id': 1, 'agency_id': 1001, 'day_of_week': 0, 'time_slots': [{'start': '09:00', 'end': '12:00'}, {'start': '14:00', 'end': '18:00'}]},
        {'id': 2, 'agency_id': 1001, 'day_of_week': 1, 'time_slots': [{'start': '09:00', 'end': '12:00'}, {'start': '14:00', 'end': '18:00'}]},
        {'id': 3, 'agency_id': 1001, 'day_of_week': 2, 'time_slots': [{'start': '09:00', 'end': '12:00'}, {'start': '14:00', 'end': '18:00'}]},
        {'id': 4, 'agency_id': 1001, 'day_of_week': 3, 'time_slots': [{'start': '09:00', 'end': '12:00'}, {'start': '14:00', 'end': '18:00'}]},
        {'id': 5, 'agency_id': 1001, 'day_of_week': 4, 'time_slots': [{'start': '09:00', 'end': '12:00'}, {'start': '14:00', 'end': '18:00'}]},
        {'id': 6, 'agency_id': 1001, 'day_of_week': 5, 'time_slots': [{'start': '09:00', 'end': '12:00'}]},
        {'id': 7, 'agency_id': 1001, 'day_of_week': 6, 'time_slots': []},
    ]
}

_working_hours_id_counter = 100

def get_agency_working_hours(agency_id):
    """获取机构的作息时间（返回7天的数据）"""
    if agency_id in AGENCY_WORKING_HOURS:
        return AGENCY_WORKING_HOURS[agency_id]
    # 返回默认的7天空数据
    result = []
    for day in range(7):
        result.append({
            'id': 0,
            'agency_id': agency_id,
            'day_of_week': day,
            'time_slots': [],
            'created_at': datetime.now().strftime('%Y-%m-%dT%H:%M:%S'),
            'updated_at': datetime.now().strftime('%Y-%m-%dT%H:%M:%S')
        })
    return result

# Mock 数据 - 通知配置
# 格式: {tenant_id: {notification_type: config}, ...}
# tenant_id为None表示全局配置
NOTIFICATION_CONFIGS = {
    None: {  # 全局默认配置
        'unreplied': {
            'id': 1,
            'tenant_id': None,
            'notification_type': 'unreplied',
            'is_enabled': True,
            'config_data': {
                'trigger_delay_minutes': 10,
                'monitored_channels': ['whatsapp', 'sms', 'rcs'],
                'repeat_interval_minutes': 30,
                'max_notify_count': 3,
                'notify_time_range': {
                    'type': 'working_hours',
                    'custom_start': '09:00',
                    'custom_end': '18:00'
                },
                'notify_roles': ['collector', 'team_leader'],
                'notify_channels': ['in_app'],
                'priority': 'high',
                'template': '案件 {case_id} 的客户在 {channel} 渠道有未回复消息'
            },
            'created_at': '2025-01-01T00:00:00',
            'updated_at': '2025-01-01T00:00:00'
        },
        'nudge': {
            'id': 2,
            'tenant_id': None,
            'notification_type': 'nudge',
            'is_enabled': True,
            'config_data': {
                'ptp': {
                    'advance_notify_minutes': 60,
                    'repeat_interval_minutes': 120,
                    'max_notify_count': 3,
                    'notify_roles': ['collector', 'team_leader']
                },
                'follow_up': {
                    'advance_notify_minutes': 30,
                    'repeat_interval_minutes': 60,
                    'max_notify_count': 2,
                    'notify_roles': ['collector']
                }
            },
            'created_at': '2025-01-01T00:00:00',
            'updated_at': '2025-01-01T00:00:00'
        },
        'case_update': {
            'id': 3,
            'tenant_id': None,
            'notification_type': 'case_update',
            'is_enabled': True,
            'config_data': {
                'case_assigned': {
                    'enabled': True,
                    'notify_roles': ['collector', 'team_leader'],
                    'template': '新案件分配：{case_id}'
                },
                'payment_received': {
                    'enabled': True,
                    'amount_threshold': None,
                    'notify_roles': ['collector', 'team_leader', 'agency_admin'],
                    'template': '案件 {case_id} 已收到还款 {amount}'
                },
                'tag_updated': {
                    'enabled': True,
                    'notify_roles': ['collector', 'team_leader'],
                    'template': '案件 {case_id} 的标签已更新'
                }
            },
            'created_at': '2025-01-01T00:00:00',
            'updated_at': '2025-01-01T00:00:00'
        },
        'performance': {
            'id': 4,
            'tenant_id': None,
            'notification_type': 'performance',
            'is_enabled': True,
            'config_data': {
                'amount_threshold': 1000,
                'notify_scope': 'team',
                'notify_roles': ['collector', 'team_leader', 'agency_admin'],
                'template': '恭喜【{team}】的【{collector}】催回金额 {amount}',
                'notify_frequency_minutes': 60
            },
            'created_at': '2025-01-01T00:00:00',
            'updated_at': '2025-01-01T00:00:00'
        },
        'timeout': {
            'id': 5,
            'tenant_id': None,
            'notification_type': 'timeout',
            'is_enabled': True,
            'config_data': {
                'timeout_levels': [
                    {'minutes': 30, 'repeat_interval_minutes': 30, 'notify_roles': ['collector']},
                    {'minutes': 60, 'repeat_interval_minutes': 60, 'notify_roles': ['collector', 'team_leader']},
                    {'minutes': 120, 'repeat_interval_minutes': 120, 'notify_roles': ['collector', 'team_leader', 'agency_admin']}
                ],
                'max_notify_count': 5,
                'escalation_minutes': 240
            },
            'created_at': '2025-01-01T00:00:00',
            'updated_at': '2025-01-01T00:00:00'
        }
    }
}

_notification_config_id_counter = 100

# Mock 数据 - 公共通知
PUBLIC_NOTIFICATIONS = [
    {
        'id': 1,
        'tenant_id': None,
        'agency_id': None,
        'title': '春节放假通知',
        'h5_content': 'https://example.com/notifications/spring-festival',
        'h5_content_type': 'url',
        'carousel_interval_seconds': 30,
        'is_forced_read': True,
        'is_enabled': True,
        'effective_start_time': '2025-01-20T00:00:00',
        'effective_end_time': '2025-02-10T23:59:59',
        'notify_roles': json.dumps(['collector', 'team_leader']),
        'sort_order': 1,
        'created_at': '2025-01-15T00:00:00',
        'updated_at': '2025-01-15T00:00:00',
        'created_by': 1
    },
    {
        'id': 2,
        'tenant_id': None,
        'agency_id': None,
        'title': '反欺诈提醒',
        'h5_content': '<html><body><h1>反欺诈提醒</h1><p>请注意防范各类诈骗手段...</p></body></html>',
        'h5_content_type': 'html',
        'carousel_interval_seconds': 60,
        'is_forced_read': False,
        'is_enabled': True,
        'effective_start_time': None,
        'effective_end_time': None,
        'notify_roles': json.dumps(['collector', 'team_leader', 'agency_admin']),
        'sort_order': 2,
        'created_at': '2025-01-10T00:00:00',
        'updated_at': '2025-01-10T00:00:00',
        'created_by': 1
    },
    {
        'id': 3,
        'tenant_id': None,
        'agency_id': None,
        'title': '产品更新通知',
        'h5_content': 'https://example.com/notifications/product-update',
        'h5_content_type': 'url',
        'carousel_interval_seconds': 45,
        'is_forced_read': False,
        'is_enabled': True,
        'effective_start_time': None,
        'effective_end_time': None,
        'notify_roles': json.dumps(['collector']),
        'sort_order': 3,
        'created_at': '2025-01-05T00:00:00',
        'updated_at': '2025-01-05T00:00:00',
        'created_by': 1
    }
]

_public_notification_id_counter = 100

def get_public_notifications(tenant_id=None, agency_id=None, is_enabled=None):
    """获取公共通知列表"""
    result = []
    for n in PUBLIC_NOTIFICATIONS:
        # tenant_id过滤：如果指定了tenant_id，返回全局通知（tenant_id为None）或匹配的通知
        if tenant_id is not None:
            if n['tenant_id'] is not None and n['tenant_id'] != tenant_id:
                continue
        # agency_id过滤：如果指定了agency_id，返回全局通知（agency_id为None）或匹配的通知
        if agency_id is not None:
            if n['agency_id'] is not None and n['agency_id'] != agency_id:
                continue
        # is_enabled过滤
        if is_enabled is not None and n['is_enabled'] != is_enabled:
            continue
        result.append(n.copy())
    
    # 按sort_order和created_at排序
    result.sort(key=lambda x: (x['sort_order'], -datetime.strptime(x['created_at'], '%Y-%m-%dT%H:%M:%S').timestamp()))
    
    return result

def get_notification_configs(tenant_id=None, notification_type=None):
    """获取通知配置列表"""
    result = []
    
    # 获取全局配置
    if tenant_id is None:
        for ntype, config in NOTIFICATION_CONFIGS.get(None, {}).items():
            if notification_type is None or ntype == notification_type:
                result.append(config)
    else:
        # 获取指定甲方的配置，如果没有则返回全局配置
        tenant_configs = NOTIFICATION_CONFIGS.get(tenant_id, {})
        for ntype in ['unreplied', 'nudge', 'case_update', 'performance', 'timeout']:
            if notification_type is None or ntype == notification_type:
                if ntype in tenant_configs:
                    result.append(tenant_configs[ntype])
                elif ntype in NOTIFICATION_CONFIGS.get(None, {}):
                    # 返回全局配置
                    result.append(NOTIFICATION_CONFIGS[None][ntype])
    
    return result

# Mock 数据 - 案件队列（默认配置）
def get_default_queues(tenant_id):
    """获取甲方的默认队列配置"""
    return [
        {
            "id": tenant_id * 100 + 1,
            "tenant_id": tenant_id,
            "queue_code": "C",
            "queue_name": "C队列",
            "overdue_days_start": None,  # -∞
            "overdue_days_end": -1,
            "sort_order": 1,
            "is_active": True,
            "created_at": "2025-01-01T00:00:00",
            "updated_at": "2025-01-01T00:00:00"
        },
        {
            "id": tenant_id * 100 + 2,
            "tenant_id": tenant_id,
            "queue_code": "S0",
            "queue_name": "S0队列",
            "overdue_days_start": 0,
            "overdue_days_end": 0,
            "sort_order": 2,
            "is_active": True,
            "created_at": "2025-01-01T00:00:00",
            "updated_at": "2025-01-01T00:00:00"
        },
        {
            "id": tenant_id * 100 + 3,
            "tenant_id": tenant_id,
            "queue_code": "S1",
            "queue_name": "S1队列",
            "overdue_days_start": 1,
            "overdue_days_end": 5,
            "sort_order": 3,
            "is_active": True,
            "created_at": "2025-01-01T00:00:00",
            "updated_at": "2025-01-01T00:00:00"
        },
        {
            "id": tenant_id * 100 + 4,
            "tenant_id": tenant_id,
            "queue_code": "L1",
            "queue_name": "L1队列",
            "overdue_days_start": 6,
            "overdue_days_end": 90,
            "sort_order": 4,
            "is_active": True,
            "created_at": "2025-01-01T00:00:00",
            "updated_at": "2025-01-01T00:00:00"
        },
        {
            "id": tenant_id * 100 + 5,
            "tenant_id": tenant_id,
            "queue_code": "M1",
            "queue_name": "M1队列",
            "overdue_days_start": 91,
            "overdue_days_end": None,  # +∞
            "sort_order": 5,
            "is_active": True,
            "created_at": "2025-01-01T00:00:00",
            "updated_at": "2025-01-01T00:00:00"
        }
    ]

# Mock 数据 - 催收机构
def get_default_agencies(tenant_id):
    """获取甲方的默认机构配置"""
    return [
        {
            "id": tenant_id * 1000 + 1,
            "tenant_id": tenant_id,
            "agency_code": f"AGENCY{tenant_id:03d}_001",
            "agency_name": f"示例机构A-{tenant_id}",
            "tenant_name": f"示例甲方{chr(64+tenant_id)}",
            "admin_id": 1,
            "admin_name": "张三",
            "team_count": 2,
            "collector_count": 5,
            "contact_phone": "+86 13800138001",
            "contact_email": "agency1@example.com",
            "remark": "主要催收机构",
            "agency_type": "real",
            "timezone": 8,
            "is_active": True,
            "created_at": "2025-01-01T00:00:00",
            "updated_at": "2025-01-10T00:00:00"
        },
        {
            "id": tenant_id * 1000 + 2,
            "tenant_id": tenant_id,
            "agency_code": f"AGENCY{tenant_id:03d}_002",
            "agency_name": f"示例机构B-{tenant_id}",
            "tenant_name": f"示例甲方{chr(64+tenant_id)}",
            "admin_id": 2,
            "admin_name": "李四",
            "team_count": 1,
            "collector_count": 3,
            "contact_phone": "+86 13800138002",
            "contact_email": "agency2@example.com",
            "remark": "次要催收机构",
            "agency_type": "real",
            "timezone": 8,
            "is_active": True,
            "created_at": "2025-01-05T00:00:00",
            "updated_at": "2025-01-10T00:00:00"
        },
        {
            "id": tenant_id * 1000 + 3,
            "tenant_id": tenant_id,
            "agency_code": f"VIRTUAL{tenant_id:03d}_001",
            "agency_name": f"虚拟机构A-{tenant_id}",
            "tenant_name": f"示例甲方{chr(64+tenant_id)}",
            "admin_id": 3,
            "admin_name": "王五",
            "team_count": 0,
            "collector_count": 0,
            "contact_phone": "+86 13800138003",
            "contact_email": "virtual1@example.com",
            "remark": "虚拟机构用于测试",
            "agency_type": "virtual",
            "timezone": 8,
            "is_active": True,
            "created_at": "2025-01-08T00:00:00",
            "updated_at": "2025-01-08T00:00:00"
        },
        {
            "id": tenant_id * 1000 + 4,
            "tenant_id": tenant_id,
            "agency_code": f"VIRTUAL{tenant_id:03d}_002",
            "agency_name": f"虚拟机构B-{tenant_id}",
            "tenant_name": f"示例甲方{chr(64+tenant_id)}",
            "admin_id": 4,
            "admin_name": "赵六",
            "team_count": 0,
            "collector_count": 0,
            "contact_phone": "+86 13800138004",
            "contact_email": "virtual2@example.com",
            "remark": "虚拟机构用于演示",
            "agency_type": "virtual",
            "timezone": 8,
            "is_active": True,
            "created_at": "2025-01-09T00:00:00",
            "updated_at": "2025-01-09T00:00:00"
        }
    ]

# Mock 数据 - 催收小组
def get_default_teams(agency_id):
    """获取机构的默认小组配置"""
    return [
        {
            "id": agency_id * 100 + 1,
            "agency_id": agency_id,
            "team_code": f"TEAM{agency_id:04d}_001",
            "team_name": f"催收一组",
            "tenant_name": "示例甲方A",
            "agency_name": "示例机构A",
            "leader_id": 1,
            "leader_name": "组长A",
            "collector_count": 3,
            "target_performance": 50.0,
            "remark": "主力小组",
            "is_active": True,
            "created_at": "2025-01-01T00:00:00",
            "updated_at": "2025-01-10T00:00:00"
        },
        {
            "id": agency_id * 100 + 2,
            "agency_id": agency_id,
            "team_code": f"TEAM{agency_id:04d}_002",
            "team_name": f"催收二组",
            "tenant_name": "示例甲方A",
            "agency_name": "示例机构A",
            "leader_id": 2,
            "leader_name": "组长B",
            "collector_count": 2,
            "target_performance": 30.0,
            "remark": "辅助小组",
            "is_active": True,
            "created_at": "2025-01-03T00:00:00",
            "updated_at": "2025-01-10T00:00:00"
        }
    ]

# Mock 数据 - 催员
def get_default_collectors(team_id):
    """获取小组的默认催员配置"""
    return [
        {
            "id": team_id * 10 + 1,
            "team_id": team_id,
            "collector_code": f"COL{team_id:05d}_001",
            "collector_name": "催员001",
            "username": f"collector{team_id}_001",
            "tenant_name": "示例甲方A",
            "agency_id": 1,
            "agency_name": "示例机构A",
            "team_name": "催收一组",
            "role": "collector",
            "mobile": "+86 13900139001",
            "email": "col001@example.com",
            "remark": "资深催员",
            "is_active": True,
            "created_at": "2025-01-01T00:00:00",
            "updated_at": "2025-01-10T00:00:00",
            "last_login_at": "2025-01-11T08:30:00"
        },
        {
            "id": team_id * 10 + 2,
            "team_id": team_id,
            "collector_code": f"COL{team_id:05d}_002",
            "collector_name": "催员002",
            "username": f"collector{team_id}_002",
            "tenant_name": "示例甲方A",
            "agency_id": 1,
            "agency_name": "示例机构A",
            "team_name": "催收一组",
            "role": "collector",
            "mobile": "+86 13900139002",
            "email": "col002@example.com",
            "remark": "新手催员",
            "is_active": True,
            "created_at": "2025-01-05T00:00:00",
            "updated_at": "2025-01-10T00:00:00",
            "last_login_at": "2025-01-11T09:00:00"
        }
    ]

# Mock 数据 - 权利账号
def get_default_admin_accounts(team_id):
    """获取小组的默认权利账号配置"""
    return [
        {
            "id": team_id * 10 + 1001,
            "team_id": team_id,
            "account_code": f"ADMIN{team_id:05d}_001",
            "account_name": "小组长001",
            "login_id": f"teamleader{team_id}_001",
            "tenant_name": "示例甲方A",
            "agency_id": 1,
            "agency_name": "示例机构A",
            "team_name": "催收一组",
            "role": "team_leader",
            "mobile": "+86 13800138001",
            "email": "teamleader001@example.com",
            "remark": "资深小组长",
            "is_active": True,
            "created_at": "2025-01-01T00:00:00",
            "updated_at": "2025-01-10T00:00:00"
        },
        {
            "id": team_id * 10 + 1002,
            "team_id": team_id,
            "account_code": f"ADMIN{team_id:05d}_002",
            "account_name": "质检员001",
            "login_id": f"qc{team_id}_001",
            "tenant_name": "示例甲方A",
            "agency_id": 1,
            "agency_name": "示例机构A",
            "team_name": "催收一组",
            "role": "quality_inspector",
            "mobile": "+86 13800138002",
            "email": "qc001@example.com",
            "remark": "质量检查",
            "is_active": True,
            "created_at": "2025-01-02T00:00:00",
            "updated_at": "2025-01-10T00:00:00"
        },
        {
            "id": team_id * 10 + 1003,
            "team_id": team_id,
            "account_code": f"ADMIN{team_id:05d}_003",
            "account_name": "统计员001",
            "login_id": f"stat{team_id}_001",
            "tenant_name": "示例甲方A",
            "agency_id": 1,
            "agency_name": "示例机构A",
            "team_name": "催收一组",
            "role": "statistician",
            "mobile": "+86 13800138003",
            "email": "stat001@example.com",
            "remark": "数据统计分析",
            "is_active": True,
            "created_at": "2025-01-03T00:00:00",
            "updated_at": "2025-01-10T00:00:00"
        }
    ]

# Mock 数据 - 案件
def generate_mock_cases():
    """生成完整的mock案件数据"""
    from datetime import datetime, timedelta
    import random
    
    # 获取队列ID（假设tenant_id=1）
    queues = get_default_queues(1)
    queue_map = {q['queue_code']: q['id'] for q in queues}
    
    # 获取机构、小组、催员ID（假设tenant_id=1）
    agencies = get_default_agencies(1)
    teams = []
    collectors = []
    for agency in agencies:
        agency_teams = get_default_teams(agency['id'])
        teams.extend(agency_teams)
        for team in agency_teams:
            team_collectors = get_default_collectors(team['id'])
            collectors.extend(team_collectors)
    
    # App和Product数据
    apps = [
        {"id": 1, "app_name": "快贷通"},
        {"id": 2, "app_name": "信用钱包"},
        {"id": 3, "app_name": "现金贷"},
        {"id": 4, "app_name": "分期购"},
    ]
    products = [
        {"id": 1, "product_name": "极速贷"},
        {"id": 2, "product_name": "分期贷"},
        {"id": 3, "product_name": "信用贷"},
        {"id": 4, "product_name": "小额贷"},
        {"id": 5, "product_name": "大额贷"},
        {"id": 6, "product_name": "学生贷"},
    ]
    
    # 案件状态映射
    status_map = {
        "pending_repayment": "待还款",
        "partial_repayment": "部分还款",
        "normal_settlement": "正常结清",
        "extension_settlement": "展期结清",
    }
    
    cases = []
    base_date = datetime(2024, 12, 1)
    
    # 生成50个案件
    for i in range(1, 51):
        case_id = i
        loan_id = f"LOAN{i:05d}"
        user_id = f"USER{i:05d}"
        
        # 随机选择状态
        status_key = random.choice(list(status_map.keys()))
        case_status = status_map[status_key]
        
        # 根据状态设置逾期天数
        if status_key == "pending_repayment":
            overdue_days = random.randint(1, 90)
            queue_id = None
            for q in queues:
                start = q['overdue_days_start'] if q['overdue_days_start'] is not None else -999999
                end = q['overdue_days_end'] if q['overdue_days_end'] is not None else 999999
                if start <= overdue_days <= end:
                    queue_id = q['id']
                    break
        elif status_key == "partial_repayment":
            overdue_days = random.randint(1, 60)
            queue_id = queue_map.get('S1')
        elif status_key in ["normal_settlement", "extension_settlement"]:
            overdue_days = random.randint(-30, 0)  # 已结清，逾期天数可能为负
            queue_id = None
        else:
            overdue_days = 0
            queue_id = None
        
        # 随机选择机构、小组、催员
        agency = random.choice(agencies) if agencies else None
        team = random.choice([t for t in teams if t['agency_id'] == agency['id']]) if agency and teams else None
        collector = random.choice([c for c in collectors if c['team_id'] == team['id']]) if team and collectors else None
        
        # 贷款金额和还款金额
        loan_amount = round(random.uniform(1000, 100000), 2)
        if status_key == "pending_repayment":
            repaid_amount = round(random.uniform(0, loan_amount * 0.3), 2)
            outstanding_amount = round(loan_amount - repaid_amount, 2)
        elif status_key == "partial_repayment":
            repaid_amount = round(random.uniform(loan_amount * 0.3, loan_amount * 0.8), 2)
            outstanding_amount = round(loan_amount - repaid_amount, 2)
        else:
            repaid_amount = loan_amount
            outstanding_amount = 0
        
        # 日期计算
        due_date = base_date + timedelta(days=random.randint(-60, 30))
        if status_key in ["normal_settlement", "extension_settlement"]:
            settlement_date = due_date + timedelta(days=random.randint(-10, 5))
        else:
            settlement_date = None
        
        assigned_at = base_date + timedelta(days=random.randint(-30, 0))
        last_contact_at = base_date + timedelta(days=random.randint(-7, 0)) if status_key != "normal_settlement" else None
        next_follow_up_at = base_date + timedelta(days=random.randint(1, 7)) if status_key == "pending_repayment" else None
        
        # 随机选择App和Product
        app = random.choice(apps)
        product = random.choice(products)
        
        # 结清方式
        settlement_methods = ["正常还款", "提前还款", "展期还款", "部分还款", "减免结清"]
        settlement_method = random.choice(settlement_methods) if status_key in ["normal_settlement", "extension_settlement"] else None
        
        # 用户姓名
        surnames = ["张", "李", "王", "刘", "陈", "杨", "赵", "黄", "周", "吴"]
        given_names = ["三", "四", "五", "六", "七", "八", "九", "十", "明", "华", "强", "伟", "芳", "丽", "静"]
        user_name = random.choice(surnames) + random.choice(given_names)
        
        # 手机号
        mobile = f"+86 1{random.randint(3, 9)}{random.randint(100000000, 999999999)}"
        
        case = {
            "id": case_id,
            "case_code": f"CASE{i:05d}",
            "tenant_id": 1,
            "agency_id": agency['id'] if agency else None,
            "team_id": team['id'] if team else None,
            "collector_id": collector['id'] if collector else None,
            "queue_id": queue_id,
            "loan_id": loan_id,
            "user_id": user_id,
            "user_name": user_name,
            "mobile": mobile,
            "case_status": case_status,
            "overdue_days": overdue_days,
            "loan_amount": str(loan_amount),
            "repaid_amount": str(repaid_amount),
            "outstanding_amount": str(outstanding_amount),
            "total_due_amount": str(loan_amount),  # 应还金额
            "due_date": due_date.strftime("%Y-%m-%d"),
            "settlement_date": settlement_date.strftime("%Y-%m-%d") if settlement_date else None,
            "settlement_time": settlement_date.strftime("%Y-%m-%dT%H:%M:%S") if settlement_date else None,
            "settlement_method": settlement_method,
            "assigned_at": assigned_at.strftime("%Y-%m-%dT%H:%M:%S"),
            "last_contact_at": last_contact_at.strftime("%Y-%m-%dT%H:%M:%S") if last_contact_at else None,
            "next_follow_up_at": next_follow_up_at.strftime("%Y-%m-%dT%H:%M:%S") if next_follow_up_at else None,
            "app_id": app['id'],
            "app_name": app['app_name'],
            "product_id": product['id'],
            "product_name": product['product_name'],
            "created_at": assigned_at.strftime("%Y-%m-%dT%H:%M:%S"),
            "updated_at": (assigned_at + timedelta(days=random.randint(0, 7))).strftime("%Y-%m-%dT%H:%M:%S"),
        }
        cases.append(case)
    
    return cases

CASES = generate_mock_cases()

# 为催员001（甲方1、机构1001、小组100101）添加10个案件
def add_cases_for_collector_001():
    """为催员001添加10个案件"""
    from datetime import datetime, timedelta
    import random
    
    # 催员001的信息
    tenant_id = 1
    agency_id = 1001  # 示例机构A-1
    team_id = 100101  # 催收一组 (agency_id * 100 + 1 = 1001 * 100 + 1)
    collector_id = 1001011  # 催员001 (team_id * 10 + 1 = 100101 * 10 + 1)
    
    # 获取队列
    queues = get_default_queues(tenant_id)
    queue_map = {q['queue_code']: q['id'] for q in queues}
    
    # App和Product数据
    apps = [
        {"id": 1, "app_name": "快贷通"},
        {"id": 2, "app_name": "信用钱包"},
        {"id": 3, "app_name": "现金贷"},
        {"id": 4, "app_name": "分期购"},
    ]
    products = [
        {"id": 1, "product_name": "极速贷"},
        {"id": 2, "product_name": "分期贷"},
        {"id": 3, "product_name": "信用贷"},
        {"id": 4, "product_name": "小额贷"},
        {"id": 5, "product_name": "大额贷"},
        {"id": 6, "product_name": "学生贷"},
    ]
    
    # 案件状态映射（主要为待还款和部分还款）
    status_map = {
        "pending_repayment": "待还款",
        "partial_repayment": "部分还款",
    }
    
    base_date = datetime(2024, 12, 1)
    start_case_id = len(CASES) + 1  # 从现有案件数量+1开始
    
    # 生成10个案件
    for i in range(10):
        case_id = start_case_id + i
        loan_id = f"LOAN{case_id:05d}"
        user_id = f"USER{case_id:05d}"
        
        # 随机选择状态（主要是待还款和部分还款）
        status_key = random.choice(list(status_map.keys()))
        case_status = status_map[status_key]
        
        # 根据状态设置逾期天数
        if status_key == "pending_repayment":
            overdue_days = random.randint(1, 90)
            queue_id = None
            for q in queues:
                start = q['overdue_days_start'] if q['overdue_days_start'] is not None else -999999
                end = q['overdue_days_end'] if q['overdue_days_end'] is not None else 999999
                if start <= overdue_days <= end:
                    queue_id = q['id']
                    break
        elif status_key == "partial_repayment":
            overdue_days = random.randint(1, 60)
            queue_id = queue_map.get('S1')
        else:
            overdue_days = random.randint(1, 90)
            queue_id = queue_map.get('S1')
        
        # 贷款金额和还款金额
        loan_amount = round(random.uniform(1000, 100000), 2)
        if status_key == "pending_repayment":
            repaid_amount = round(random.uniform(0, loan_amount * 0.3), 2)
            outstanding_amount = round(loan_amount - repaid_amount, 2)
        elif status_key == "partial_repayment":
            repaid_amount = round(random.uniform(loan_amount * 0.3, loan_amount * 0.8), 2)
            outstanding_amount = round(loan_amount - repaid_amount, 2)
        else:
            repaid_amount = round(random.uniform(0, loan_amount * 0.3), 2)
            outstanding_amount = round(loan_amount - repaid_amount, 2)
        
        # 日期计算
        due_date = base_date + timedelta(days=random.randint(-60, 30))
        settlement_date = None
        
        assigned_at = base_date + timedelta(days=random.randint(-30, 0))
        last_contact_at = base_date + timedelta(days=random.randint(-7, 0))
        next_follow_up_at = base_date + timedelta(days=random.randint(1, 7))
        
        # 随机选择App和Product
        app = random.choice(apps)
        product = random.choice(products)
        
        # 用户姓名
        surnames = ["张", "李", "王", "刘", "陈", "杨", "赵", "黄", "周", "吴"]
        given_names = ["三", "四", "五", "六", "七", "八", "九", "十", "明", "华", "强", "伟", "芳", "丽", "静"]
        user_name = random.choice(surnames) + random.choice(given_names)
        
        # 手机号
        mobile = f"+86 1{random.randint(3, 9)}{random.randint(100000000, 999999999)}"
        
        case = {
            "id": case_id,
            "case_code": f"CASE{case_id:05d}",
            "tenant_id": tenant_id,
            "agency_id": agency_id,
            "team_id": team_id,
            "collector_id": collector_id,
            "queue_id": queue_id,
            "loan_id": loan_id,
            "user_id": user_id,
            "user_name": user_name,
            "mobile": mobile,
            "case_status": case_status,
            "overdue_days": overdue_days,
            "loan_amount": str(loan_amount),
            "repaid_amount": str(repaid_amount),
            "outstanding_amount": str(outstanding_amount),
            "total_due_amount": str(loan_amount),
            "due_date": due_date.strftime("%Y-%m-%d"),
            "settlement_date": settlement_date.strftime("%Y-%m-%d") if settlement_date else None,
            "settlement_time": settlement_date.strftime("%Y-%m-%dT%H:%M:%S") if settlement_date else None,
            "settlement_method": None,
            "assigned_at": assigned_at.strftime("%Y-%m-%dT%H:%M:%S"),
            "last_contact_at": last_contact_at.strftime("%Y-%m-%dT%H:%M:%S") if last_contact_at else None,
            "next_follow_up_at": next_follow_up_at.strftime("%Y-%m-%dT%H:%M:%S") if next_follow_up_at else None,
            "app_id": app['id'],
            "app_name": app['app_name'],
            "product_id": product['id'],
            "product_name": product['product_name'],
            "created_at": assigned_at.strftime("%Y-%m-%dT%H:%M:%S"),
            "updated_at": (assigned_at + timedelta(days=random.randint(0, 7))).strftime("%Y-%m-%dT%H:%M:%S"),
        }
        CASES.append(case)

# 添加案件
add_cases_for_collector_001()

# 为BTSK001（tenant_id=2）添加10个案件
def add_cases_for_btsk001():
    """为BTSK001添加10个案件"""
    from datetime import datetime, timedelta
    import random
    
    # BTSK001的信息（tenant_id=2，BTSK印度）
    tenant_id = 2
    agency_id = 2001  # BTSK机构（假设tenant_id=2的机构ID从2001开始）
    team_id = 200101  # Alpha Team (agency_id * 100 + 1)
    collector_id = 2000001  # BTSK001的特殊ID（使用2000001作为BTSK001的ID）
    
    # 获取队列（tenant_id=2）
    queues = get_default_queues(tenant_id)
    queue_map = {q['queue_code']: q['id'] for q in queues}
    
    # App和Product数据
    apps = [
        {"id": 1, "app_name": "快贷通"},
        {"id": 2, "app_name": "信用钱包"},
        {"id": 3, "app_name": "现金贷"},
        {"id": 4, "app_name": "分期购"},
    ]
    products = [
        {"id": 1, "product_name": "极速贷"},
        {"id": 2, "product_name": "分期贷"},
        {"id": 3, "product_name": "信用贷"},
        {"id": 4, "product_name": "小额贷"},
        {"id": 5, "product_name": "大额贷"},
        {"id": 6, "product_name": "学生贷"},
    ]
    
    # 案件状态映射（主要为待还款和部分还款）
    status_map = {
        "pending_repayment": "待还款",
        "partial_repayment": "部分还款",
    }
    
    base_date = datetime(2024, 12, 1)
    start_case_id = len(CASES) + 1  # 从现有案件数量+1开始
    
    # 生成10个案件
    for i in range(10):
        case_id = start_case_id + i
        loan_id = f"LOAN{case_id:05d}"
        user_id = f"USER{case_id:05d}"
        
        # 随机选择状态（主要是待还款和部分还款）
        status_key = random.choice(list(status_map.keys()))
        case_status = status_map[status_key]
        
        # 根据状态设置逾期天数
        if status_key == "pending_repayment":
            overdue_days = random.randint(1, 90)
            queue_id = None
            for q in queues:
                start = q['overdue_days_start'] if q['overdue_days_start'] is not None else -999999
                end = q['overdue_days_end'] if q['overdue_days_end'] is not None else 999999
                if start <= overdue_days <= end:
                    queue_id = q['id']
                    break
        elif status_key == "partial_repayment":
            overdue_days = random.randint(1, 60)
            queue_id = queue_map.get('S1')
        else:
            overdue_days = random.randint(1, 90)
            queue_id = queue_map.get('S1')
        
        # 贷款金额和还款金额
        loan_amount = round(random.uniform(1000, 100000), 2)
        if status_key == "pending_repayment":
            repaid_amount = round(random.uniform(0, loan_amount * 0.3), 2)
            outstanding_amount = round(loan_amount - repaid_amount, 2)
        elif status_key == "partial_repayment":
            repaid_amount = round(random.uniform(loan_amount * 0.3, loan_amount * 0.8), 2)
            outstanding_amount = round(loan_amount - repaid_amount, 2)
        else:
            repaid_amount = round(random.uniform(0, loan_amount * 0.3), 2)
            outstanding_amount = round(loan_amount - repaid_amount, 2)
        
        # 日期计算
        due_date = base_date + timedelta(days=random.randint(-60, 30))
        settlement_date = None
        
        assigned_at = base_date + timedelta(days=random.randint(-30, 0))
        last_contact_at = base_date + timedelta(days=random.randint(-7, 0))
        next_follow_up_at = base_date + timedelta(days=random.randint(1, 7))
        
        # 随机选择App和Product
        app = random.choice(apps)
        product = random.choice(products)
        
        # 用户姓名（印度风格）
        surnames = ["Sharma", "Patel", "Kumar", "Singh", "Gupta", "Verma", "Reddy", "Mehta", "Joshi", "Malik"]
        given_names = ["Raj", "Priya", "Amit", "Anjali", "Rahul", "Sneha", "Vikram", "Kavita", "Rohit", "Divya"]
        user_name = random.choice(given_names) + " " + random.choice(surnames)
        
        # 手机号（印度格式）
        mobile = f"+91 {random.randint(7000000000, 9999999999)}"
        
        case = {
            "id": case_id,
            "case_code": f"CASE{case_id:05d}",
            "tenant_id": tenant_id,
            "agency_id": agency_id,
            "team_id": team_id,
            "collector_id": collector_id,
            "queue_id": queue_id,
            "loan_id": loan_id,
            "user_id": user_id,
            "user_name": user_name,
            "mobile": mobile,
            "case_status": case_status,
            "overdue_days": overdue_days,
            "loan_amount": str(loan_amount),
            "repaid_amount": str(repaid_amount),
            "outstanding_amount": str(outstanding_amount),
            "total_due_amount": str(loan_amount),
            "due_date": due_date.strftime("%Y-%m-%d"),
            "settlement_date": settlement_date.strftime("%Y-%m-%d") if settlement_date else None,
            "settlement_time": settlement_date.strftime("%Y-%m-%dT%H:%M:%S") if settlement_date else None,
            "settlement_method": None,
            "assigned_at": assigned_at.strftime("%Y-%m-%dT%H:%M:%S"),
            "last_contact_at": last_contact_at.strftime("%Y-%m-%dT%H:%M:%S") if last_contact_at else None,
            "next_follow_up_at": next_follow_up_at.strftime("%Y-%m-%dT%H:%M:%S") if next_follow_up_at else None,
            "app_id": app['id'],
            "app_name": app['app_name'],
            "product_id": product['id'],
            "product_name": product['product_name'],
            "created_at": assigned_at.strftime("%Y-%m-%dT%H:%M:%S"),
            "updated_at": (assigned_at + timedelta(days=random.randint(0, 7))).strftime("%Y-%m-%dT%H:%M:%S"),
        }
        CASES.append(case)

# 添加BTSK001的案件
add_cases_for_btsk001()

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
            # 支持查询参数过滤
            filtered_cases = CASES.copy()
            
            # 按tenant_id过滤
            tenant_id = query_params.get('tenant_id')
            if tenant_id:
                tenant_id = int(tenant_id[0])
                filtered_cases = [c for c in filtered_cases if c['tenant_id'] == tenant_id]
            
            # 按case_status过滤
            case_status = query_params.get('case_status')
            if case_status:
                case_status = case_status[0]
                filtered_cases = [c for c in filtered_cases if c['case_status'] == case_status]
            
            # 按queue_id过滤
            queue_id = query_params.get('queue_id')
            if queue_id:
                queue_id = int(queue_id[0])
                filtered_cases = [c for c in filtered_cases if c.get('queue_id') == queue_id]
            
            # 按agency_id过滤
            agency_id = query_params.get('agency_id')
            if agency_id:
                agency_id = int(agency_id[0])
                filtered_cases = [c for c in filtered_cases if c.get('agency_id') == agency_id]
            
            # 按team_id过滤
            team_id = query_params.get('team_id')
            if team_id:
                team_id = int(team_id[0])
                filtered_cases = [c for c in filtered_cases if c.get('team_id') == team_id]
            
            # 按collector_id过滤
            collector_id = query_params.get('collector_id')
            if collector_id:
                collector_id = int(collector_id[0])
                filtered_cases = [c for c in filtered_cases if c.get('collector_id') == collector_id]
            
            # 按due_date范围过滤
            due_date_start = query_params.get('due_date_start')
            due_date_end = query_params.get('due_date_end')
            if due_date_start or due_date_end:
                if due_date_start:
                    due_date_start = due_date_start[0]
                if due_date_end:
                    due_date_end = due_date_end[0]
                filtered_cases = [c for c in filtered_cases if c.get('due_date') and (
                    (not due_date_start or c['due_date'] >= due_date_start) and
                    (not due_date_end or c['due_date'] <= due_date_end)
                )]
            
            # 按settlement_date范围过滤
            settlement_date_start = query_params.get('settlement_date_start')
            settlement_date_end = query_params.get('settlement_date_end')
            if settlement_date_start or settlement_date_end:
                if settlement_date_start:
                    settlement_date_start = settlement_date_start[0]
                if settlement_date_end:
                    settlement_date_end = settlement_date_end[0]
                filtered_cases = [c for c in filtered_cases if c.get('settlement_date') and (
                    (not settlement_date_start or c['settlement_date'] >= settlement_date_start) and
                    (not settlement_date_end or c['settlement_date'] <= settlement_date_end)
                )]
            
            self._set_headers()
            self.wfile.write(json.dumps({"data": filtered_cases}).encode())
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
        elif path.startswith('/api/v1/tenants/') and '/fields' in path:
            # GET /api/v1/tenants/{tenant_id}/fields?field_group_id={group_id}
            # 返回甲方的所有字段配置（标准字段+自定义字段）
            tenant_id = int(path.split('/')[4])
            group_id = query_params.get('field_group_id')
            
            # 构建字段列表：所有标准字段 + 自定义字段
            fields_config = []
            
            # 1. 先添加所有标准字段（100%继承）
            for field in STANDARD_FIELDS:
                # 如果指定了分组ID，只返回该分组的字段
                if group_id and field['field_group_id'] != int(group_id[0]):
                    continue
                    
                # 为每个标准字段生成甲方配置
                fields_config.append({
                    'id': field['id'],  # 配置ID
                    'field_id': field['id'],  # 标准字段ID
                    'field_group_id': field['field_group_id'],
                    'field_name': field['field_name'],
                    'field_key': field['field_key'],
                    'tenant_field_key': field['field_key'].upper(),  # 默认映射为大写
                    'tenant_field_id': f"{field['field_key']}_{field['id']:03d}",
                    'field_type': field['field_type'],
                    'field_source': 'standard',  # 标记为标准字段
                    'is_required': field['is_required'],
                    'sort_order': field['sort_order'],
                    'hidden_queues': []
                })
            
            # 2. 再添加自定义字段（Mock示例）
            custom_fields_examples = [
                {
                    'id': 1001,
                    'field_group_id': 11,  # 基础身份信息
                    'field_name': '客户等级',
                    'field_key': 'customer_level',
                    'tenant_field_key': 'Level',
                    'tenant_field_id': 'level_1001',
                    'field_type': 'Enum',
                    'field_source': 'custom',  # 标记为自定义字段
                    'is_required': False,
                    'sort_order': 100,
                    'hidden_queues': [1]
                },
                {
                    'id': 1002,
                    'field_group_id': 11,
                    'field_name': '客户来源',
                    'field_key': 'customer_source',
                    'tenant_field_key': 'Source',
                    'tenant_field_id': 'source_1002',
                    'field_type': 'String',
                    'field_source': 'custom',
                    'is_required': False,
                    'sort_order': 101,
                    'hidden_queues': []
                }
            ]
            
            # 只添加匹配分组的自定义字段
            for custom_field in custom_fields_examples:
                if group_id and custom_field['field_group_id'] != int(group_id[0]):
                    continue
                fields_config.append(custom_field)
            
            self._set_headers()
            self.wfile.write(json.dumps({"data": fields_config}).encode())
        elif path.startswith('/api/v1/tenants/') and path.endswith('/fields-json'):
            # GET /api/v1/tenants/{tenant_id}/fields-json
            # 获取甲方字段JSON（甲方通过API推送的原始字段数据）
            parts = path.split('/')
            tenant_id = int(parts[4])
            
            # Mock甲方字段JSON数据
            tenant_fields_json = {
                'fetched_at': datetime.now().isoformat(),
                'fields': []
            }
            
            # 从标准字段中mock一些字段数据
            for field in STANDARD_FIELDS[:20]:  # 只取前20个作为示例
                tenant_fields_json['fields'].append({
                    'id': field['id'],
                    'field_name': field['field_name'],
                    'field_key': field['field_key'],
                    'field_type': field['field_type'],
                    'field_group_id': field['field_group_id'],
                    'is_required': field.get('is_required', False),
                    'sort_order': field.get('sort_order', 0),
                    'enum_values': field.get('enum_values', [])
                })
            
            self._set_headers()
            self.wfile.write(json.dumps(tenant_fields_json).encode())
        elif path.startswith('/api/v1/tenants/') and '/unmapped-fields' in path:
            # GET /api/v1/tenants/{tenant_id}/unmapped-fields
            # 获取未映射的甲方字段
            parts = path.split('/')
            tenant_id = int(parts[4])
            
            # Mock未映射字段数据
            unmapped_fields = [
                {
                    'tenant_field_key': 'EXTRA_FIELD_1',
                    'tenant_field_name': '额外字段1',
                    'field_type': 'String',
                    'is_required': False,
                    'tenant_updated_at': '2024-01-15T10:30:00Z'
                },
                {
                    'tenant_field_key': 'EXTRA_FIELD_2',
                    'tenant_field_name': '额外字段2',
                    'field_type': 'Integer',
                    'is_required': True,
                    'tenant_updated_at': '2024-01-16T14:20:00Z'
                },
                {
                    'tenant_field_key': 'COMPANY_NAME',
                    'tenant_field_name': '公司名称',
                    'field_type': 'String',
                    'is_required': False,
                    'tenant_updated_at': '2024-01-17T09:15:00Z'
                }
            ]
            
            self._set_headers()
            self.wfile.write(json.dumps({"data": unmapped_fields}).encode())
        elif path.startswith('/api/v1/tenants/') and '/extended-fields' in path:
            # GET /api/v1/tenants/{tenant_id}/extended-fields
            # 获取扩展字段
            parts = path.split('/')
            tenant_id = int(parts[4])
            
            # Mock扩展字段数据
            extended_fields = [
                {
                    'id': 1,
                    'tenant_id': tenant_id,
                    'field_alias': 'company_name',
                    'tenant_field_key': 'COMP_NAME',
                    'tenant_field_name': '公司名称',
                    'field_type': 'String',
                    'privacy_label': 'PII',
                    'retention_days': 365,
                    'allow_report': True,
                    'allow_query_filter': False,
                    'created_at': '2024-01-10T10:00:00Z',
                    'updated_at': '2024-01-10T10:00:00Z'
                },
                {
                    'id': 2,
                    'tenant_id': tenant_id,
                    'field_alias': 'customer_level',
                    'tenant_field_key': 'CUST_LEVEL',
                    'tenant_field_name': '客户等级',
                    'field_type': 'String',
                    'privacy_label': '公开',
                    'retention_days': 730,
                    'allow_report': True,
                    'allow_query_filter': True,
                    'created_at': '2024-01-11T14:30:00Z',
                    'updated_at': '2024-01-11T14:30:00Z'
                }
            ]
            
            self._set_headers()
            self.wfile.write(json.dumps({"data": extended_fields}).encode())
        elif path.startswith('/api/v1/tenants/') and '/queues' in path:
            # GET /api/v1/tenants/{tenant_id}/queues
            # 获取甲方的队列配置
            tenant_id = int(path.split('/')[4])
            queues = get_default_queues(tenant_id)
            self._set_headers()
            self.wfile.write(json.dumps({"data": queues}).encode())
        elif path.startswith('/api/v1/agencies/') and '/working-hours' in path:
            # GET /api/v1/agencies/{agency_id}/working-hours
            # 获取机构作息时间（必须在/teams之前匹配）
            parts = path.split('/')
            # 路径格式: /api/v1/agencies/{agency_id}/working-hours
            # parts[0]='', parts[1]='api', parts[2]='v1', parts[3]='agencies', parts[4]=agency_id, parts[5]='working-hours'
            if len(parts) >= 6 and parts[5] == 'working-hours':
                try:
                    agency_id = int(parts[4])
                    working_hours = get_agency_working_hours(agency_id)
                    self._set_headers()
                    self.wfile.write(json.dumps(working_hours).encode())
                except (ValueError, IndexError) as e:
                    self._set_headers(400)
                    self.wfile.write(json.dumps({"detail": f"无效的机构ID: {str(e)}"}).encode())
                except Exception as e:
                    self._set_headers(500)
                    import traceback
                    self.wfile.write(json.dumps({"detail": f"服务器错误: {str(e)}", "traceback": traceback.format_exc()}).encode())
            else:
                self._set_headers(404)
                self.wfile.write(json.dumps({"detail": f"路径格式错误: path={path}, parts={parts}, len={len(parts)}"}).encode())
        elif path.startswith('/api/v1/tenants/') and '/agencies' in path:
            # GET /api/v1/tenants/{tenant_id}/agencies
            # 获取甲方的机构列表
            tenant_id = int(path.split('/')[4])
            agencies = get_default_agencies(tenant_id)
            self._set_headers()
            self.wfile.write(json.dumps({"data": agencies}).encode())
        elif path.startswith('/api/v1/agencies/') and '/teams' in path:
            # GET /api/v1/agencies/{agency_id}/teams
            # 获取机构的小组列表
            agency_id = int(path.split('/')[4])
            teams = get_default_teams(agency_id)
            self._set_headers()
            self.wfile.write(json.dumps({"data": teams}).encode())
        elif path.startswith('/api/v1/teams/') and '/collectors' in path:
            # GET /api/v1/teams/{team_id}/collectors
            # 获取小组的催员列表
            team_id = int(path.split('/')[4])
            collectors = get_default_collectors(team_id)
            self._set_headers()
            self.wfile.write(json.dumps({"data": collectors}).encode())
        elif path.startswith('/api/v1/teams/') and '/admin-accounts' in path:
            # GET /api/v1/teams/{team_id}/admin-accounts
            # 获取小组的权利账号列表
            team_id = int(path.split('/')[4])
            accounts = get_default_admin_accounts(team_id)
            self._set_headers()
            self.wfile.write(json.dumps({"data": accounts}).encode())
        elif path.startswith('/api/v1/tenants/') and '/apps' in path:
            # GET /api/v1/tenants/{tenant_id}/apps
            # 获取甲方的App列表
            tenant_id = int(path.split('/')[4])
            if tenant_id == 1:
                apps = TENANT_A_APPS
            else:
                apps = []
            self._set_headers()
            self.wfile.write(json.dumps({"data": apps}).encode())
        elif path.startswith('/api/v1/tenants/') and '/products' in path:
            # GET /api/v1/tenants/{tenant_id}/products
            # 获取甲方的产品列表
            tenant_id = int(path.split('/')[4])
            if tenant_id == 1:
                products = TENANT_A_PRODUCTS
            else:
                products = []
            self._set_headers()
            self.wfile.write(json.dumps({"data": products}).encode())
        elif path.startswith('/api/v1/channel-suppliers/tenants/') and '/channels/' in path and '/suppliers' in path:
            # GET /api/v1/channel-suppliers/tenants/{tenant_id}/channels/{channel_type}/suppliers
            # 获取渠道供应商列表
            parts = path.split('/')
            # 路径格式: /api/v1/channel-suppliers/tenants/{tenant_id}/channels/{channel_type}/suppliers
            # parts[0]='', parts[1]='api', parts[2]='v1', parts[3]='channel-suppliers', 
            # parts[4]='tenants', parts[5]=tenant_id, parts[6]='channels', parts[7]=channel_type, parts[8]='suppliers'
            tenant_id = int(parts[5])
            channel_type = parts[7]
            
            # 检查是否是排序更新接口
            if path.endswith('/order'):
                # PUT /api/v1/channel-suppliers/tenants/{tenant_id}/channels/{channel_type}/suppliers/order
                # 这个在do_PUT中处理
                self._set_headers(404)
                self.wfile.write(json.dumps({"detail": "Not found"}).encode())
            else:
                suppliers = get_channel_suppliers(tenant_id, channel_type)
                # 按sort_order排序
                suppliers = sorted(suppliers, key=lambda x: x['sort_order'])
                self._set_headers()
                self.wfile.write(json.dumps({"data": suppliers}).encode())
        elif path.startswith('/api/v1/channel-suppliers/') and path.count('/') == 3 and '/working-hours' not in path:
            # GET /api/v1/channel-suppliers/{supplier_id}
            # 获取单个供应商
            supplier_id = int(path.split('/')[-1])
            found = None
            for tenant_id, channels in CHANNEL_SUPPLIERS.items():
                for channel_type, suppliers in channels.items():
                    for supplier in suppliers:
                        if supplier['id'] == supplier_id:
                            found = supplier
                            break
                    if found:
                        break
                if found:
                    break
            
            if found:
                self._set_headers()
                self.wfile.write(json.dumps({"data": found}).encode())
            else:
                self._set_headers(404)
                self.wfile.write(json.dumps({"detail": "供应商不存在"}).encode())
        elif path.startswith('/api/v1/tenants/') and path.endswith('/field-configs'):
            self._set_headers()
            self.wfile.write(json.dumps({"data": []}).encode())
        elif path.startswith('/api/v1/notification-configs'):
            # GET /api/v1/notification-configs?tenant_id=xxx&notification_type=xxx
            # 获取通知配置列表
            tenant_id = query_params.get('tenant_id')
            notification_type = query_params.get('notification_type')
            
            if tenant_id:
                tenant_id = int(tenant_id[0])
            else:
                tenant_id = None
            
            if notification_type:
                notification_type = notification_type[0]
            else:
                notification_type = None
            
            configs = get_notification_configs(tenant_id, notification_type)
            self._set_headers()
            self.wfile.write(json.dumps(configs).encode())
        elif path.startswith('/api/v1/public-notifications/') and len(path.split('/')) == 5:
            # GET /api/v1/public-notifications/{id}
            parts = path.split('/')
            if parts[4].isdigit():
                notification_id = int(parts[4])
                notifications = get_public_notifications()
                notification = next((n for n in notifications if n['id'] == notification_id), None)
                if notification:
                    # 处理notify_roles JSON字段
                    notification_copy = notification.copy()
                    if notification_copy.get('notify_roles'):
                        try:
                            notification_copy['notify_roles'] = json.loads(notification_copy['notify_roles'])
                        except:
                            notification_copy['notify_roles'] = []
                    else:
                        notification_copy['notify_roles'] = []
                    self._set_headers()
                    self.wfile.write(json.dumps(notification_copy).encode())
                else:
                    self._set_headers(404)
                    self.wfile.write(json.dumps({"detail": "公共通知不存在"}).encode())
            else:
                self._set_headers(404)
                self.wfile.write(json.dumps({"detail": "Not found"}).encode())
        elif path == '/api/v1/public-notifications':
            # GET /api/v1/public-notifications?tenant_id=xxx&agency_id=xxx&is_enabled=xxx
            # 获取公共通知列表
            tenant_id = query_params.get('tenant_id')
            agency_id = query_params.get('agency_id')
            is_enabled = query_params.get('is_enabled')
            
            if tenant_id:
                tenant_id = int(tenant_id[0])
            else:
                tenant_id = None
            
            if agency_id:
                agency_id = int(agency_id[0])
            else:
                agency_id = None
            
            if is_enabled:
                is_enabled = is_enabled[0].lower() == 'true'
            else:
                is_enabled = None
            
            notifications = get_public_notifications(tenant_id, agency_id, is_enabled)
            # 处理notify_roles JSON字段，转换为数组
            result = []
            for n in notifications:
                n_copy = n.copy()
                if n_copy.get('notify_roles'):
                    try:
                        n_copy['notify_roles'] = json.loads(n_copy['notify_roles'])
                    except:
                        n_copy['notify_roles'] = []
                else:
                    n_copy['notify_roles'] = []
                result.append(n_copy)
            self._set_headers()
            self.wfile.write(json.dumps(result).encode())
        else:
            self._set_headers(404)
            self.wfile.write(json.dumps({"detail": "Not found"}).encode())

    def do_POST(self):
        parsed_path = urlparse(self.path)
        path = parsed_path.path
        
        # 读取请求体
        content_length = int(self.headers.get('Content-Length', 0))
        post_data = self.rfile.read(content_length)
        
        try:
            request_body = json.loads(post_data.decode('utf-8'))
        except:
            self._set_headers(400)
            self.wfile.write(json.dumps({"detail": "Invalid JSON"}).encode())
            return
        
        if path == '/api/v1/admin/auth/login':
            # POST /api/v1/admin/auth/login - 管理后台登录
            login_id = request_body.get('loginId', '').lower()
            password = request_body.get('password', '')
            
            # 验证账号密码
            if login_id == 'superadmin' and password == '123456':
                # SuperAdmin登录
                import hashlib
                import time
                token_str = f"{login_id}_{time.time()}"
                token = hashlib.md5(token_str.encode()).hexdigest()
                
                user_info = {
                    'id': 1,
                    'loginId': 'superadmin',
                    'username': 'superadmin',
                    'role': 'SuperAdmin',
                    'email': 'admin@cco.com',
                    'name': '超级管理员'
                }
                
                self._set_headers(200)
                self.wfile.write(json.dumps({
                    'code': 200,
                    'message': '登录成功',
                    'data': {
                        'token': token,
                        'user': user_info
                    }
                }).encode())
            elif login_id == 'tenantadmin' and password == 'admin123':
                # TenantAdmin登录
                import hashlib
                import time
                token_str = f"{login_id}_{time.time()}"
                token = hashlib.md5(token_str.encode()).hexdigest()
                
                user_info = {
                    'id': 2,
                    'loginId': 'tenantadmin',
                    'username': 'tenantadmin',
                    'role': 'TenantAdmin',
                    'email': 'tenant@cco.com',
                    'name': '甲方管理员'
                }
                
                self._set_headers(200)
                self.wfile.write(json.dumps({
                    'code': 200,
                    'message': '登录成功',
                    'data': {
                        'token': token,
                        'user': user_info
                    }
                }).encode())
            else:
                # 登录失败
                self._set_headers(401)
                self.wfile.write(json.dumps({
                    'detail': '登录ID或密码错误'
                }).encode())
        elif path.startswith('/api/v1/channel-suppliers/tenants/') and '/channels/' in path and '/suppliers' in path and not path.endswith('/order'):
            # POST /api/v1/channel-suppliers/tenants/{tenant_id}/channels/{channel_type}/suppliers
            # 创建渠道供应商
            parts = path.split('/')
            tenant_id = int(parts[5])
            channel_type = parts[7]
            
            # 确保tenant_id和channel_type在数据结构中存在
            if tenant_id not in CHANNEL_SUPPLIERS:
                CHANNEL_SUPPLIERS[tenant_id] = {}
            if channel_type not in CHANNEL_SUPPLIERS[tenant_id]:
                CHANNEL_SUPPLIERS[tenant_id][channel_type] = []
            
            # 获取当前最大sort_order
            existing_suppliers = CHANNEL_SUPPLIERS[tenant_id][channel_type]
            max_sort_order = max([s.get('sort_order', 0) for s in existing_suppliers], default=-1)
            
            # 生成新ID
            global _supplier_id_counter
            new_id = _supplier_id_counter
            _supplier_id_counter += 1
            
            # 创建新供应商
            new_supplier = {
                'id': new_id,
                'tenant_id': tenant_id,
                'channel_type': channel_type,
                'supplier_name': request_body.get('supplier_name', ''),
                'api_url': request_body.get('api_url', ''),
                'api_key': request_body.get('api_key', ''),
                'secret_key': request_body.get('secret_key', ''),
                'remark': request_body.get('remark', ''),
                'sort_order': max_sort_order + 1,
                'is_active': request_body.get('is_active', True),
                'created_at': datetime.now().strftime('%Y-%m-%dT%H:%M:%S'),
                'updated_at': datetime.now().strftime('%Y-%m-%dT%H:%M:%S')
            }
            
            CHANNEL_SUPPLIERS[tenant_id][channel_type].append(new_supplier)
            
            self._set_headers(201)
            self.wfile.write(json.dumps({"data": new_supplier}).encode())
        elif path == '/api/v1/admin/auth/logout':
            # POST /api/v1/admin/auth/logout - 管理后台登出
            self._set_headers(200)
            self.wfile.write(json.dumps({
                'code': 200,
                'message': '登出成功'
            }).encode())
        elif path == '/api/v1/tenants':
            # POST /api/v1/tenants - 创建甲方（含管理员账号）
            # 生成新的甲方ID
            new_tenant_id = max([t['id'] for t in TENANTS]) + 1 if TENANTS else 1
            
            # 创建甲方数据
            new_tenant = {
                "id": new_tenant_id,
                "tenant_code": request_body.get("tenant_code"),
                "tenant_name": request_body.get("tenant_name"),
                "tenant_name_en": request_body.get("tenant_name", ""),
                "country_code": request_body.get("country_code"),
                "timezone": request_body.get("timezone"),
                "currency_code": request_body.get("currency_code"),
                "is_active": True,
                "created_at": datetime.now().strftime("%Y-%m-%dT%H:%M:%S"),
                "updated_at": datetime.now().strftime("%Y-%m-%dT%H:%M:%S")
            }
            TENANTS.append(new_tenant)
            
            # 创建管理员账号数据
            admin_data = request_body.get("admin", {})
            new_admin = {
                "id": new_tenant_id,  # 使用相同ID作为示例
                "tenant_id": new_tenant_id,
                "admin_name": admin_data.get("admin_name"),
                "login_id": admin_data.get("login_id"),
                "email": admin_data.get("email"),
                "is_active": True,
                "created_at": datetime.now().strftime("%Y-%m-%dT%H:%M:%S"),
                "updated_at": datetime.now().strftime("%Y-%m-%dT%H:%M:%S"),
                "last_login_at": None
            }
            
            # 返回创建成功响应
            self._set_headers(201)
            self.wfile.write(json.dumps({
                "code": 0,
                "message": "创建成功",
                "data": {
                    "tenant": new_tenant,
                    "admin": new_admin
                }
            }).encode())
        elif path == '/api/v1/public-notifications':
            # POST /api/v1/public-notifications - 创建公共通知
            global PUBLIC_NOTIFICATIONS, _public_notification_id_counter
            
            new_notification = {
                'id': _public_notification_id_counter,
                'tenant_id': request_body.get('tenant_id'),
                'agency_id': request_body.get('agency_id'),
                'title': request_body.get('title'),
                'h5_content': request_body.get('h5_content'),
                'h5_content_type': request_body.get('h5_content_type', 'url'),
                'carousel_interval_seconds': request_body.get('carousel_interval_seconds', 30),
                'is_forced_read': request_body.get('is_forced_read', False),
                'is_enabled': request_body.get('is_enabled', True),
                'effective_start_time': request_body.get('effective_start_time'),
                'effective_end_time': request_body.get('effective_end_time'),
                'notify_roles': json.dumps(request_body.get('notify_roles', [])) if request_body.get('notify_roles') else None,
                'sort_order': request_body.get('sort_order', 0),
                'created_at': datetime.now().strftime('%Y-%m-%dT%H:%M:%S'),
                'updated_at': datetime.now().strftime('%Y-%m-%dT%H:%M:%S'),
                'created_by': 1
            }
            
            _public_notification_id_counter += 1
            PUBLIC_NOTIFICATIONS.append(new_notification)
            
            self._set_headers(201)
            self.wfile.write(json.dumps(new_notification).encode())
        elif path.startswith('/api/v1/tenants/') and '/fields/auto-suggest-mapping' in path:
            # POST /api/v1/tenants/{tenant_id}/fields/auto-suggest-mapping
            # 一键建议映射未匹配字段
            parts = path.split('/')
            tenant_id = int(parts[4])
            
            # Mock返回建议映射结果
            self._set_headers()
            self.wfile.write(json.dumps({
                "code": 200,
                "message": "成功",
                "data": {
                    "count": 3,  # 建议映射的字段数量
                    "suggestions": [
                        {"tenant_field_key": "EXTRA_FIELD_1", "target_field_key": "custom_field_1"},
                        {"tenant_field_key": "EXTRA_FIELD_2", "target_field_key": "custom_field_2"},
                        {"tenant_field_key": "COMPANY_NAME", "target_field_key": "company_name"}
                    ]
                }
            }).encode())
        elif path.startswith('/api/v1/tenants/') and '/fields/match' in path:
            # POST /api/v1/tenants/{tenant_id}/fields/match
            # 手动匹配字段
            # Mock返回成功
            self._set_headers()
            self.wfile.write(json.dumps({
                "code": 200,
                "message": "匹配成功",
                "data": {
                    "tenant_field_key": request_body.get('tenant_field_key'),
                    "target_field_id": request_body.get('target_field_id')
                }
            }).encode())
        elif path.startswith('/api/v1/tenants/') and '/extended-fields' in path:
            # POST /api/v1/tenants/{tenant_id}/extended-fields
            # 创建扩展字段
            # Mock返回创建成功
            self._set_headers(201)
            self.wfile.write(json.dumps({
                "code": 200,
                "message": "创建成功",
                "data": {
                    "id": 100,  # Mock ID
                    **request_body
                }
            }).encode())
        else:
            self._set_headers(404)
            self.wfile.write(json.dumps({"detail": "Not found"}).encode())

    def do_PUT(self):
        parsed_path = urlparse(self.path)
        path = parsed_path.path
        
        # 读取请求体
        content_length = int(self.headers.get('Content-Length', 0))
        put_data = self.rfile.read(content_length)
        
        try:
            request_body = json.loads(put_data.decode('utf-8'))
        except:
            self._set_headers(400)
            self.wfile.write(json.dumps({"detail": "Invalid JSON"}).encode())
            return
        
        if path.startswith('/api/v1/tenants/') and '/extended-fields/' in path and not path.endswith('/extended-fields'):
            # PUT /api/v1/tenants/{tenant_id}/extended-fields/{field_id}
            # 更新扩展字段
            self._set_headers()
            self.wfile.write(json.dumps({
                "code": 200,
                "message": "更新成功",
                "data": request_body
            }).encode())
        elif path.startswith('/api/v1/agencies/') and '/working-hours' in path:
            # PUT /api/v1/agencies/{agency_id}/working-hours 或 PUT /api/v1/agencies/{agency_id}/working-hours/{day_of_week}
            global AGENCY_WORKING_HOURS, _working_hours_id_counter
            parts = path.split('/')
            agency_id = int(parts[4])
            
            # 检查是否是单天更新
            if len(parts) == 7 and parts[6].isdigit():
                # PUT /api/v1/agencies/{agency_id}/working-hours/{day_of_week}
                day_of_week = int(parts[6])
                if not (0 <= day_of_week <= 6):
                    self._set_headers(400)
                    self.wfile.write(json.dumps({"detail": "day_of_week必须在0-6之间"}).encode())
                    return
                
                # 更新单天
                if agency_id not in AGENCY_WORKING_HOURS:
                    AGENCY_WORKING_HOURS[agency_id] = []
                
                # 查找或创建该天的记录
                day_record = None
                for wh in AGENCY_WORKING_HOURS[agency_id]:
                    if wh['day_of_week'] == day_of_week:
                        day_record = wh
                        break
                
                if day_record:
                    day_record['time_slots'] = request_body.get('time_slots', [])
                    day_record['updated_at'] = datetime.now().strftime('%Y-%m-%dT%H:%M:%S')
                else:
                    global _working_hours_id_counter
                    day_record = {
                        'id': _working_hours_id_counter,
                        'agency_id': agency_id,
                        'day_of_week': day_of_week,
                        'time_slots': request_body.get('time_slots', []),
                        'created_at': datetime.now().strftime('%Y-%m-%dT%H:%M:%S'),
                        'updated_at': datetime.now().strftime('%Y-%m-%dT%H:%M:%S')
                    }
                    _working_hours_id_counter += 1
                    AGENCY_WORKING_HOURS[agency_id].append(day_record)
                
                self._set_headers(200)
                self.wfile.write(json.dumps(day_record).encode())
            else:
                # PUT /api/v1/agencies/{agency_id}/working-hours - 批量更新
                working_hours_list = request_body.get('working_hours', [])
                
                if len(working_hours_list) != 7:
                    self._set_headers(400)
                    self.wfile.write(json.dumps({"detail": "必须提供7天的作息时间配置"}).encode())
                    return
                
                # 删除旧的作息时间
                if agency_id in AGENCY_WORKING_HOURS:
                    del AGENCY_WORKING_HOURS[agency_id]
                
                # 创建新的作息时间
                new_records = []
                for wh_data in working_hours_list:
                    if wh_data.get('agency_id') != agency_id:
                        self._set_headers(400)
                        self.wfile.write(json.dumps({"detail": "机构ID不匹配"}).encode())
                        return
                    
                    new_record = {
                        'id': _working_hours_id_counter,
                        'agency_id': agency_id,
                        'day_of_week': wh_data.get('day_of_week'),
                        'time_slots': wh_data.get('time_slots', []),
                        'created_at': datetime.now().strftime('%Y-%m-%dT%H:%M:%S'),
                        'updated_at': datetime.now().strftime('%Y-%m-%dT%H:%M:%S')
                    }
                    _working_hours_id_counter += 1
                    new_records.append(new_record)
                
                AGENCY_WORKING_HOURS[agency_id] = new_records
                
                self._set_headers(200)
                self.wfile.write(json.dumps(new_records).encode())
        elif path.startswith('/api/v1/notification-configs/'):
            # PUT /api/v1/notification-configs/{config_id}
            # 更新通知配置
            parts = path.split('/')
            if len(parts) >= 5 and parts[4].isdigit():
                config_id = int(parts[4])
                
                # 查找配置
                found_config = None
                for tenant_id_key, configs in NOTIFICATION_CONFIGS.items():
                    for ntype, config in configs.items():
                        if config['id'] == config_id:
                            found_config = config
                            break
                    if found_config:
                        break
                
                if not found_config:
                    self._set_headers(404)
                    self.wfile.write(json.dumps({"detail": "通知配置不存在"}).encode())
                    return
                
                # 更新配置
                if 'is_enabled' in request_body:
                    found_config['is_enabled'] = request_body['is_enabled']
                if 'config_data' in request_body:
                    found_config['config_data'] = request_body['config_data']
                found_config['updated_at'] = datetime.now().strftime('%Y-%m-%dT%H:%M:%S')
                
                self._set_headers(200)
                self.wfile.write(json.dumps(found_config).encode())
            else:
                self._set_headers(404)
                self.wfile.write(json.dumps({"detail": "路径格式错误"}).encode())
        elif path.startswith('/api/v1/public-notifications/') and len(path.split('/')) == 5:
            # PUT /api/v1/public-notifications/{id} 或 PUT /api/v1/public-notifications/{id}/sort
            parts = path.split('/')
            if len(parts) >= 5 and parts[4].isdigit():
                notification_id = int(parts[4])
                
                # 检查是否是排序接口
                if len(parts) == 6 and parts[5] == 'sort':
                    # PUT /api/v1/public-notifications/{id}/sort?sort_order=xxx
                    parsed_path = urlparse(self.path)
                    query_params = parse_qs(parsed_path.query)
                    sort_order = query_params.get('sort_order')
                    if sort_order:
                        sort_order = int(sort_order[0])
                        notification = next((n for n in PUBLIC_NOTIFICATIONS if n['id'] == notification_id), None)
                        if notification:
                            notification['sort_order'] = sort_order
                            notification['updated_at'] = datetime.now().strftime('%Y-%m-%dT%H:%M:%S')
                            self._set_headers(200)
                            self.wfile.write(json.dumps({"message": "排序更新成功"}).encode())
                        else:
                            self._set_headers(404)
                            self.wfile.write(json.dumps({"detail": "公共通知不存在"}).encode())
                    else:
                        self._set_headers(400)
                        self.wfile.write(json.dumps({"detail": "缺少sort_order参数"}).encode())
                else:
                    # PUT /api/v1/public-notifications/{id} - 更新公共通知
                    notification = next((n for n in PUBLIC_NOTIFICATIONS if n['id'] == notification_id), None)
                    if notification:
                        # 更新字段
                        if 'title' in request_body:
                            notification['title'] = request_body['title']
                        if 'h5_content' in request_body:
                            notification['h5_content'] = request_body['h5_content']
                        if 'h5_content_type' in request_body:
                            notification['h5_content_type'] = request_body['h5_content_type']
                        if 'carousel_interval_seconds' in request_body:
                            notification['carousel_interval_seconds'] = request_body['carousel_interval_seconds']
                        if 'is_forced_read' in request_body:
                            notification['is_forced_read'] = request_body['is_forced_read']
                        if 'is_enabled' in request_body:
                            notification['is_enabled'] = request_body['is_enabled']
                        if 'effective_start_time' in request_body:
                            notification['effective_start_time'] = request_body['effective_start_time']
                        if 'effective_end_time' in request_body:
                            notification['effective_end_time'] = request_body['effective_end_time']
                        if 'notify_roles' in request_body:
                            notification['notify_roles'] = json.dumps(request_body['notify_roles']) if request_body['notify_roles'] else None
                        if 'sort_order' in request_body:
                            notification['sort_order'] = request_body['sort_order']
                        notification['updated_at'] = datetime.now().strftime('%Y-%m-%dT%H:%M:%S')
                        
                        self._set_headers(200)
                        self.wfile.write(json.dumps(notification).encode())
                    else:
                        self._set_headers(404)
                        self.wfile.write(json.dumps({"detail": "公共通知不存在"}).encode())
            else:
                self._set_headers(404)
                self.wfile.write(json.dumps({"detail": "路径格式错误"}).encode())
        else:
            self._set_headers(404)
            self.wfile.write(json.dumps({"detail": "Not found"}).encode())

    def do_DELETE(self):
        parsed_path = urlparse(self.path)
        path = parsed_path.path
        
        if path.startswith('/api/v1/tenants/') and '/extended-fields/' in path:
            # DELETE /api/v1/tenants/{tenant_id}/extended-fields/{field_id}
            # 删除扩展字段
            self._set_headers()
            self.wfile.write(json.dumps({
                "code": 200,
                "message": "删除成功"
            }).encode())
        elif path.startswith('/api/v1/public-notifications/'):
            # DELETE /api/v1/public-notifications/{id}
            parts = path.split('/')
            if len(parts) >= 5 and parts[4].isdigit():
                notification_id = int(parts[4])
                global PUBLIC_NOTIFICATIONS
                notification = next((n for n in PUBLIC_NOTIFICATIONS if n['id'] == notification_id), None)
                if notification:
                    PUBLIC_NOTIFICATIONS.remove(notification)
                    self._set_headers(200)
                    self.wfile.write(json.dumps({"message": "删除成功"}).encode())
                else:
                    self._set_headers(404)
                    self.wfile.write(json.dumps({"detail": "公共通知不存在"}).encode())
            else:
                self._set_headers(404)
                self.wfile.write(json.dumps({"detail": "路径格式错误"}).encode())
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

