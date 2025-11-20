#!/usr/bin/env python3
"""
初始化甲方字段展示配置数据
为每个甲方的3个场景创建默认的字段展示配置
"""
import os
import sys

# 添加项目根目录到 Python 路径
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from sqlalchemy.orm import sessionmaker
from app.core.database import engine
from app.models import Tenant, TenantFieldDisplayConfig

def get_admin_case_list_fields():
    """获取控台案件管理列表的字段配置"""
    return [
        {
            "field_key": "case_code",
            "field_name": "案件编号",
            "is_visible": True,
            "sort_order": 1,
            "display_width": 180,
            "color_type": "normal",
            "is_fixed": True,
            "align": "left"
        },
        {
            "field_key": "user_name",
            "field_name": "客户姓名",
            "is_visible": True,
            "sort_order": 2,
            "display_width": 120,
            "color_type": "normal",
            "is_fixed": False,
            "align": "left"
        },
        {
            "field_key": "mobile",
            "field_name": "手机号码",
            "is_visible": True,
            "sort_order": 3,
            "display_width": 140,
            "color_type": "normal",
            "is_fixed": False,
            "align": "left"
        },
        {
            "field_key": "loan_amount",
            "field_name": "贷款金额",
            "is_visible": True,
            "sort_order": 4,
            "display_width": 120,
            "color_type": "normal",
            "is_fixed": False,
            "align": "right",
            "format_rule": {
                "format_type": "currency",
                "format_pattern": "#,##0.00",
                "prefix": "¥",
                "suffix": ""
            }
        },
        {
            "field_key": "outstanding_amount",
            "field_name": "应还未还金额",
            "is_visible": True,
            "sort_order": 5,
            "display_width": 130,
            "color_type": "normal",
            "is_fixed": False,
            "align": "right",
            "format_rule": {
                "format_type": "currency",
                "format_pattern": "#,##0.00",
                "prefix": "¥",
                "suffix": ""
            }
        },
        {
            "field_key": "overdue_days",
            "field_name": "逾期天数",
            "is_visible": True,
            "sort_order": 6,
            "display_width": 100,
            "color_type": "normal",
            "is_fixed": False,
            "align": "center",
            "color_rule": [
                {"condition": "value > 30", "color": "red"},
                {"condition": "value > 15 && value <= 30", "color": "yellow"},
                {"condition": "value > 0 && value <= 15", "color": "green"}
            ]
        },
        {
            "field_key": "case_status",
            "field_name": "案件状态",
            "is_visible": True,
            "sort_order": 7,
            "display_width": 120,
            "color_type": "normal",
            "is_fixed": False,
            "align": "center"
        },
        {
            "field_key": "queue_name",
            "field_name": "所属队列",
            "is_visible": True,
            "sort_order": 8,
            "display_width": 100,
            "color_type": "normal",
            "is_fixed": False,
            "align": "center"
        },
        {
            "field_key": "agency_name",
            "field_name": "催收机构",
            "is_visible": True,
            "sort_order": 9,
            "display_width": 150,
            "color_type": "normal",
            "is_fixed": False,
            "align": "left"
        },
        {
            "field_key": "team_name",
            "field_name": "催收小组",
            "is_visible": True,
            "sort_order": 10,
            "display_width": 120,
            "color_type": "normal",
            "is_fixed": False,
            "align": "left"
        },
        {
            "field_key": "collector_name",
            "field_name": "催员姓名",
            "is_visible": True,
            "sort_order": 11,
            "display_width": 100,
            "color_type": "normal",
            "is_fixed": False,
            "align": "center"
        },
        {
            "field_key": "assigned_at",
            "field_name": "分配时间",
            "is_visible": True,
            "sort_order": 12,
            "display_width": 160,
            "color_type": "normal",
            "is_fixed": False,
            "align": "center",
            "format_rule": {
                "format_type": "date",
                "format_pattern": "YYYY-MM-DD HH:mm",
                "prefix": "",
                "suffix": ""
            }
        },
        {
            "field_key": "due_date",
            "field_name": "应还日期",
            "is_visible": True,
            "sort_order": 13,
            "display_width": 120,
            "color_type": "normal",
            "is_fixed": False,
            "align": "center",
            "format_rule": {
                "format_type": "date",
                "format_pattern": "YYYY-MM-DD",
                "prefix": "",
                "suffix": ""
            }
        },
        {
            "field_key": "last_contact_time",
            "field_name": "最后联系时间",
            "is_visible": False,
            "sort_order": 14,
            "display_width": 160,
            "color_type": "normal",
            "is_fixed": False,
            "align": "center"
        },
        {
            "field_key": "contact_count",
            "field_name": "联系次数",
            "is_visible": False,
            "sort_order": 15,
            "display_width": 100,
            "color_type": "normal",
            "is_fixed": False,
            "align": "center"
        }
    ]

def get_collector_case_list_fields():
    """获取催员案件列表的字段配置"""
    return [
        {
            "field_key": "case_code",
            "field_name": "案件编号",
            "is_visible": True,
            "sort_order": 1,
            "display_width": 160,
            "color_type": "normal",
            "is_fixed": True,
            "align": "left"
        },
        {
            "field_key": "user_name",
            "field_name": "客户姓名",
            "is_visible": True,
            "sort_order": 2,
            "display_width": 100,
            "color_type": "normal",
            "is_fixed": False,
            "align": "left"
        },
        {
            "field_key": "mobile",
            "field_name": "手机号码",
            "is_visible": True,
            "sort_order": 3,
            "display_width": 130,
            "color_type": "normal",
            "is_fixed": False,
            "align": "left"
        },
        {
            "field_key": "outstanding_amount",
            "field_name": "应还金额",
            "is_visible": True,
            "sort_order": 4,
            "display_width": 120,
            "color_type": "normal",
            "is_fixed": False,
            "align": "right",
            "format_rule": {
                "format_type": "currency",
                "format_pattern": "#,##0.00",
                "prefix": "¥",
                "suffix": ""
            }
        },
        {
            "field_key": "overdue_days",
            "field_name": "逾期天数",
            "is_visible": True,
            "sort_order": 5,
            "display_width": 90,
            "color_type": "normal",
            "is_fixed": False,
            "align": "center",
            "color_rule": [
                {"condition": "value > 30", "color": "red"},
                {"condition": "value > 15 && value <= 30", "color": "yellow"},
                {"condition": "value > 0 && value <= 15", "color": "green"}
            ]
        },
        {
            "field_key": "case_status",
            "field_name": "案件状态",
            "is_visible": True,
            "sort_order": 6,
            "display_width": 100,
            "color_type": "normal",
            "is_fixed": False,
            "align": "center"
        },
        {
            "field_key": "queue_name",
            "field_name": "队列",
            "is_visible": True,
            "sort_order": 7,
            "display_width": 80,
            "color_type": "normal",
            "is_fixed": False,
            "align": "center"
        },
        {
            "field_key": "due_date",
            "field_name": "应还日期",
            "is_visible": True,
            "sort_order": 8,
            "display_width": 110,
            "color_type": "normal",
            "is_fixed": False,
            "align": "center",
            "format_rule": {
                "format_type": "date",
                "format_pattern": "YYYY-MM-DD",
                "prefix": "",
                "suffix": ""
            }
        },
        {
            "field_key": "last_contact_time",
            "field_name": "最后联系",
            "is_visible": True,
            "sort_order": 9,
            "display_width": 140,
            "color_type": "normal",
            "is_fixed": False,
            "align": "center"
        },
        {
            "field_key": "contact_count",
            "field_name": "联系次数",
            "is_visible": True,
            "sort_order": 10,
            "display_width": 90,
            "color_type": "normal",
            "is_fixed": False,
            "align": "center"
        },
        {
            "field_key": "agency_name",
            "field_name": "机构",
            "is_visible": False,
            "sort_order": 11,
            "display_width": 120,
            "color_type": "normal",
            "is_fixed": False,
            "align": "left",
            "hide_for_agencies": [],  # 可根据实际情况配置隐藏规则
        },
        {
            "field_key": "team_name",
            "field_name": "小组",
            "is_visible": False,
            "sort_order": 12,
            "display_width": 100,
            "color_type": "normal",
            "is_fixed": False,
            "align": "left",
            "hide_for_teams": [],  # 可根据实际情况配置隐藏规则
        }
    ]

def get_collector_case_detail_fields():
    """获取催员案件详情的字段配置"""
    return [
        {
            "field_key": "user_name",
            "field_name": "客户姓名",
            "is_visible": True,
            "sort_order": 1,
            "display_width": None,
            "color_type": "normal",
            "is_fixed": False,
            "align": "left"
        },
        {
            "field_key": "mobile",
            "field_name": "手机号码",
            "is_visible": True,
            "sort_order": 2,
            "display_width": None,
            "color_type": "normal",
            "is_fixed": False,
            "align": "left"
        },
        {
            "field_key": "id_number",
            "field_name": "证件号码",
            "is_visible": True,
            "sort_order": 3,
            "display_width": None,
            "color_type": "normal",
            "is_fixed": False,
            "align": "left"
        },
        {
            "field_key": "loan_amount",
            "field_name": "贷款金额",
            "is_visible": True,
            "sort_order": 4,
            "display_width": None,
            "color_type": "normal",
            "is_fixed": False,
            "align": "right",
            "format_rule": {
                "format_type": "currency",
                "format_pattern": "#,##0.00",
                "prefix": "¥",
                "suffix": ""
            }
        },
        {
            "field_key": "outstanding_amount",
            "field_name": "应还未还金额",
            "is_visible": True,
            "sort_order": 5,
            "display_width": None,
            "color_type": "normal",
            "is_fixed": False,
            "align": "right",
            "format_rule": {
                "format_type": "currency",
                "format_pattern": "#,##0.00",
                "prefix": "¥",
                "suffix": ""
            }
        },
        {
            "field_key": "overdue_days",
            "field_name": "逾期天数",
            "is_visible": True,
            "sort_order": 6,
            "display_width": None,
            "color_type": "normal",
            "is_fixed": False,
            "align": "center",
            "color_rule": [
                {"condition": "value > 30", "color": "red"},
                {"condition": "value > 15 && value <= 30", "color": "yellow"},
                {"condition": "value > 0 && value <= 15", "color": "green"}
            ]
        },
        {
            "field_key": "case_status",
            "field_name": "案件状态",
            "is_visible": True,
            "sort_order": 7,
            "display_width": None,
            "color_type": "normal",
            "is_fixed": False,
            "align": "center"
        },
        {
            "field_key": "due_date",
            "field_name": "应还日期",
            "is_visible": True,
            "sort_order": 8,
            "display_width": None,
            "color_type": "normal",
            "is_fixed": False,
            "align": "center",
            "format_rule": {
                "format_type": "date",
                "format_pattern": "YYYY-MM-DD",
                "prefix": "",
                "suffix": ""
            }
        },
        {
            "field_key": "loan_date",
            "field_name": "放款日期",
            "is_visible": True,
            "sort_order": 9,
            "display_width": None,
            "color_type": "normal",
            "is_fixed": False,
            "align": "center",
            "format_rule": {
                "format_type": "date",
                "format_pattern": "YYYY-MM-DD",
                "prefix": "",
                "suffix": ""
            }
        },
        {
            "field_key": "address",
            "field_name": "地址",
            "is_visible": True,
            "sort_order": 10,
            "display_width": None,
            "color_type": "normal",
            "is_fixed": False,
            "align": "left"
        },
        {
            "field_key": "company_name",
            "field_name": "公司名称",
            "is_visible": True,
            "sort_order": 11,
            "display_width": None,
            "color_type": "normal",
            "is_fixed": False,
            "align": "left"
        },
        {
            "field_key": "email",
            "field_name": "邮箱",
            "is_visible": False,
            "sort_order": 12,
            "display_width": None,
            "color_type": "normal",
            "is_fixed": False,
            "align": "left"
        },
        {
            "field_key": "emergency_contact_name",
            "field_name": "紧急联系人",
            "is_visible": True,
            "sort_order": 13,
            "display_width": None,
            "color_type": "normal",
            "is_fixed": False,
            "align": "left"
        },
        {
            "field_key": "emergency_contact_phone",
            "field_name": "紧急联系人电话",
            "is_visible": True,
            "sort_order": 14,
            "display_width": None,
            "color_type": "normal",
            "is_fixed": False,
            "align": "left"
        }
    ]

def create_configs_for_tenant(session, tenant_id, tenant_name):
    """为指定甲方创建字段展示配置"""
    scenes = [
        {
            "scene_type": "admin_case_list",
            "scene_name": "控台案件管理列表",
            "fields": get_admin_case_list_fields()
        },
        {
            "scene_type": "collector_case_list",
            "scene_name": "催员案件列表",
            "fields": get_collector_case_list_fields()
        },
        {
            "scene_type": "collector_case_detail",
            "scene_name": "催员案件详情",
            "fields": get_collector_case_detail_fields()
        }
    ]
    
    configs = []
    for scene in scenes:
        print(f"    创建场景 [{scene['scene_name']}] 的字段配置...")
        for field in scene['fields']:
            config = TenantFieldDisplayConfig(
                tenant_id=tenant_id,
                scene_type=scene['scene_type'],
                scene_name=scene['scene_name'],
                field_key=field['field_key'],
                field_name=field['field_name'],
                is_visible=field['is_visible'],
                sort_order=field['sort_order'],
                display_width=field.get('display_width'),
                color_type=field.get('color_type', 'normal'),
                color_rule=field.get('color_rule'),
                hide_rule=field.get('hide_rule'),
                hide_for_queues=field.get('hide_for_queues'),
                hide_for_agencies=field.get('hide_for_agencies'),
                hide_for_teams=field.get('hide_for_teams'),
                is_fixed=field.get('is_fixed', False),
                align=field.get('align', 'left'),
                format_rule=field.get('format_rule'),
                is_enabled=True,
                created_by='system'
            )
            session.add(config)
            configs.append(config)
        print(f"      ✓ 已创建 {len(scene['fields'])} 个字段配置")
    
    return configs

def init_field_display_configs():
    """初始化所有甲方的字段展示配置"""
    print("=" * 80)
    print("初始化甲方字段展示配置数据")
    print("=" * 80)
    
    try:
        # 创建session
        Session = sessionmaker(bind=engine)
        session = Session()
        
        # 获取所有甲方
        tenants = session.query(Tenant).filter(Tenant.is_active == True).all()
        
        if not tenants:
            print("❌ 错误：数据库中没有甲方数据，请先运行 init_all_data.py 初始化基础数据")
            return False
        
        print(f"\n找到 {len(tenants)} 个甲方，开始创建字段展示配置...")
        
        # 清空现有配置数据
        print("\n1. 清空现有字段展示配置...")
        deleted_count = session.query(TenantFieldDisplayConfig).delete()
        session.commit()
        print(f"✓ 已删除 {deleted_count} 条现有配置数据")
        
        # 为每个甲方创建配置
        print("\n2. 为每个甲方创建字段展示配置...")
        total_configs = 0
        
        # 先提取所有甲方信息，避免后续访问触发autoflush
        tenant_info = [(t.id, t.tenant_code, t.tenant_name) for t in tenants]
        
        for tenant_id, tenant_code, tenant_name in tenant_info:
            print(f"\n  为甲方 [{tenant_code}] {tenant_name} 创建配置...")
            configs = create_configs_for_tenant(session, tenant_id, tenant_name)
            total_configs += len(configs)
            print(f"  ✓ 成功创建 {len(configs)} 个配置项")
        
        # 提交所有更改
        session.commit()
        
        # 验证数据
        print("\n3. 验证数据...")
        config_count = session.query(TenantFieldDisplayConfig).count()
        print(f"✓ 数据库中共有 {config_count} 个字段展示配置")
        
        # 按场景统计
        for scene_type, scene_name in [
            ("admin_case_list", "控台案件管理列表"),
            ("collector_case_list", "催员案件列表"),
            ("collector_case_detail", "催员案件详情")
        ]:
            count = session.query(TenantFieldDisplayConfig).filter(
                TenantFieldDisplayConfig.scene_type == scene_type
            ).count()
            print(f"  - {scene_name}: {count} 个配置")
        
        session.close()
        
        print("\n" + "=" * 80)
        print("✅ 字段展示配置数据初始化完成！")
        print("=" * 80)
        print(f"\n统计信息：")
        print(f"  - 甲方数量: {len(tenants)}")
        print(f"  - 配置总数: {total_configs}")
        print(f"  - 场景数量: 3 个（控台案件列表、催员案件列表、催员案件详情）")
        print(f"  - 每个甲方每个场景的字段数:")
        print(f"    * 控台案件列表: {len(get_admin_case_list_fields())} 个字段")
        print(f"    * 催员案件列表: {len(get_collector_case_list_fields())} 个字段")
        print(f"    * 催员案件详情: {len(get_collector_case_detail_fields())} 个字段")
        print("\n配置说明：")
        print("  - 已配置动态颜色规则（逾期天数）")
        print("  - 已配置货币格式化（金额字段）")
        print("  - 已配置日期格式化（日期字段）")
        print("  - 已配置固定列（案件编号）")
        print("  - 支持按需配置隐藏规则")
        print("\n" + "=" * 80)
        
        return True
        
    except Exception as e:
        print(f"\n❌ 初始化失败: {e}")
        import traceback
        traceback.print_exc()
        return False

if __name__ == "__main__":
    success = init_field_display_configs()
    sys.exit(0 if success else 1)

