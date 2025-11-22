#!/usr/bin/env python3
"""
权限系统初始化脚本
从 frontend/src/views/system/permission-data.ts 导入权限数据到数据库
"""
import sys
import os
sys.path.append(os.path.dirname(os.path.abspath(__file__)))

from sqlalchemy.orm import Session
from app.core.database import SessionLocal, engine, Base
from app.models.permission import (
    PermissionModule, PermissionItem, RolePermissionConfig,
    RoleCode, PermissionLevel
)


# 权限数据定义（从 permission-data.ts 转换）
PERMISSION_DATA = {
    # 系统管理权限
    "system": {
        "module_name": "系统管理",
        "sort_order": 1,
        "items": [
            {"key": "SYS_CONFIG", "name": "系统配置管理", "description": "管理系统全局配置参数", "sort": 1},
            {"key": "USER_MANAGE", "name": "用户管理", "description": "管理系统用户账号", "sort": 2},
            {"key": "ROLE_MANAGE", "name": "角色管理", "description": "管理系统角色和权限配置", "sort": 3},
            {"key": "LOG_VIEW", "name": "日志查看", "description": "查看系统操作日志和审计日志", "sort": 4},
            {"key": "DATA_BACKUP", "name": "数据备份与恢复", "description": "执行数据备份和恢复操作", "sort": 5},
        ]
    },
    # 甲方管理权限
    "tenant": {
        "module_name": "甲方管理",
        "sort_order": 2,
        "items": [
            {"key": "TENANT_VIEW", "name": "查看甲方列表", "description": "查看所有甲方信息", "sort": 1},
            {"key": "TENANT_CREATE", "name": "创建甲方", "description": "创建新的甲方账号", "sort": 2},
            {"key": "TENANT_EDIT", "name": "编辑甲方", "description": "修改甲方配置信息", "sort": 3},
            {"key": "TENANT_DELETE", "name": "删除甲方", "description": "删除甲方账号", "sort": 4},
            {"key": "TENANT_SWITCH", "name": "切换甲方", "description": "在不同甲方之间切换", "sort": 5},
            {"key": "TENANT_FIELD_CONFIG", "name": "甲方字段配置", "description": "配置甲方自定义字段", "sort": 6},
            {"key": "TENANT_CHANNEL_MANAGE", "name": "甲方渠道管理", "description": "管理甲方的渠道供应商配置", "sort": 7},
            {"key": "TENANT_QUEUE_MANAGE", "name": "甲方队列管理", "description": "管理甲方的案件队列配置", "sort": 8},
        ]
    },
    # 机构管理权限
    "agency": {
        "module_name": "机构管理",
        "sort_order": 3,
        "items": [
            {"key": "AGENCY_VIEW", "name": "查看机构列表", "description": "查看机构信息列表", "sort": 1},
            {"key": "AGENCY_CREATE", "name": "创建机构", "description": "创建新的机构", "sort": 2},
            {"key": "AGENCY_EDIT", "name": "编辑机构", "description": "修改机构信息", "sort": 3},
            {"key": "AGENCY_DELETE", "name": "删除机构", "description": "删除机构", "sort": 4},
            {"key": "AGENCY_TIMEZONE", "name": "机构时区配置", "description": "配置机构时区", "sort": 5},
            {"key": "AGENCY_WORKING_HOURS", "name": "机构作息时间管理", "description": "配置机构工作时间和作息安排", "sort": 6},
            {"key": "AGENCY_ADMIN_MANAGE", "name": "机构管理员管理", "description": "管理机构管理员账号", "sort": 7},
        ]
    },
    # 小组管理权限
    "team": {
        "module_name": "小组管理",
        "sort_order": 4,
        "items": [
            {"key": "TEAM_VIEW", "name": "查看小组列表", "description": "查看小组信息列表", "sort": 1},
            {"key": "TEAM_CREATE", "name": "创建小组", "description": "创建新的小组", "sort": 2},
            {"key": "TEAM_EDIT", "name": "编辑小组", "description": "修改小组信息", "sort": 3},
            {"key": "TEAM_DELETE", "name": "删除小组", "description": "删除小组", "sort": 4},
            {"key": "TEAM_ADMIN_MANAGE", "name": "小组管理员管理", "description": "管理小组管理员账号", "sort": 5},
            {"key": "TEAM_MEMBER_ADD", "name": "添加组员", "description": "向小组添加催员", "sort": 6},
            {"key": "TEAM_MEMBER_REMOVE", "name": "移除组员", "description": "从小组移除催员", "sort": 7},
        ]
    },
    # 催员管理权限
    "collector": {
        "module_name": "催员管理",
        "sort_order": 5,
        "items": [
            {"key": "COLLECTOR_VIEW", "name": "查看催员列表", "description": "查看催员信息列表", "sort": 1},
            {"key": "COLLECTOR_CREATE", "name": "创建催员", "description": "创建新的催员账号", "sort": 2},
            {"key": "COLLECTOR_EDIT", "name": "编辑催员", "description": "修改催员信息", "sort": 3},
            {"key": "COLLECTOR_DELETE", "name": "删除催员", "description": "删除催员账号", "sort": 4},
            {"key": "COLLECTOR_PASSWORD_RESET", "name": "重置催员密码", "description": "重置催员登录密码", "sort": 5},
            {"key": "COLLECTOR_EXPORT", "name": "导出催员账号密码", "description": "导出催员账号和密码信息", "sort": 6},
            {"key": "COLLECTOR_STATUS", "name": "催员状态管理", "description": "启用/禁用催员账号", "sort": 7},
        ]
    },
    # 案件管理权限
    "case": {
        "module_name": "案件管理",
        "sort_order": 6,
        "items": [
            {"key": "CASE_VIEW_ALL", "name": "查看全部案件", "description": "查看所有案件信息", "sort": 1},
            {"key": "CASE_VIEW_AGENCY", "name": "查看机构案件", "description": "查看机构下所有案件", "sort": 2},
            {"key": "CASE_VIEW_TEAM", "name": "查看小组案件", "description": "查看小组下所有案件", "sort": 3},
            {"key": "CASE_VIEW_SELF", "name": "查看个人案件", "description": "查看分配给自己的案件", "sort": 4},
            {"key": "CASE_ASSIGN", "name": "案件分配", "description": "分配案件到机构/小组/催员", "sort": 5},
            {"key": "CASE_TRANSFER", "name": "案件转移", "description": "转移案件到其他催员", "sort": 6},
            {"key": "CASE_RECLAIM", "name": "案件回收", "description": "回收案件到小组或机构", "sort": 7},
            {"key": "CASE_COLLECT", "name": "案件催收操作", "description": "执行催收相关操作（联系、记录等）", "sort": 8},
            {"key": "CASE_EDIT", "name": "案件编辑", "description": "编辑案件信息", "sort": 9},
            {"key": "CASE_DELETE", "name": "案件删除", "description": "删除案件", "sort": 10},
            {"key": "CASE_EXPORT", "name": "案件导出", "description": "导出案件数据", "sort": 11},
        ]
    },
    # 字段配置权限
    "field": {
        "module_name": "字段配置",
        "sort_order": 7,
        "items": [
            {"key": "FIELD_STANDARD", "name": "标准字段管理", "description": "管理系统标准字段", "sort": 1},
            {"key": "FIELD_CUSTOM", "name": "自定义字段管理", "description": "管理甲方自定义字段", "sort": 2},
            {"key": "FIELD_GROUP", "name": "字段分组管理", "description": "管理字段分组配置", "sort": 3},
            {"key": "FIELD_QUEUE_CONFIG", "name": "队列字段配置", "description": "配置队列字段显示和隐藏", "sort": 4},
        ]
    },
    # 渠道配置权限
    "channel": {
        "module_name": "渠道配置",
        "sort_order": 8,
        "items": [
            {"key": "CHANNEL_SUPPLIER", "name": "渠道供应商管理", "description": "管理渠道供应商信息", "sort": 1},
            {"key": "CHANNEL_LIMIT", "name": "渠道发送限制配置", "description": "配置渠道发送限制规则", "sort": 2},
            {"key": "CHANNEL_STATISTICS", "name": "渠道使用统计", "description": "查看渠道使用统计数据", "sort": 3},
        ]
    },
    # 业绩查看权限
    "performance": {
        "module_name": "业绩查看",
        "sort_order": 9,
        "items": [
            {"key": "PERFORMANCE_VIEW_ALL", "name": "查看全部业绩", "description": "查看所有业绩数据", "sort": 1},
            {"key": "PERFORMANCE_VIEW_AGENCY", "name": "查看机构业绩", "description": "查看机构业绩统计", "sort": 2},
            {"key": "PERFORMANCE_VIEW_TEAM", "name": "查看小组业绩", "description": "查看小组业绩统计", "sort": 3},
            {"key": "PERFORMANCE_VIEW_SELF", "name": "查看个人业绩", "description": "查看个人业绩统计", "sort": 4},
            {"key": "PERFORMANCE_EXPORT", "name": "业绩报表导出", "description": "导出业绩报表数据", "sort": 5},
            {"key": "PERFORMANCE_ANALYSIS", "name": "业绩数据分析", "description": "查看业绩分析图表", "sort": 6},
        ]
    },
    # 聊天内容查看权限
    "chat": {
        "module_name": "聊天内容查看",
        "sort_order": 10,
        "items": [
            {"key": "CHAT_VIEW_ALL", "name": "查看全部聊天", "description": "查看所有聊天记录", "sort": 1},
            {"key": "CHAT_VIEW_AGENCY", "name": "查看机构聊天", "description": "查看机构下所有聊天记录", "sort": 2},
            {"key": "CHAT_VIEW_TEAM", "name": "查看小组聊天", "description": "查看小组下所有聊天记录", "sort": 3},
            {"key": "CHAT_VIEW_MEMBER", "name": "查看组员聊天", "description": "查看指定组员的聊天记录", "sort": 4},
            {"key": "CHAT_VIEW_SELF", "name": "查看个人聊天", "description": "查看自己的聊天记录", "sort": 5},
            {"key": "CHAT_EXPORT", "name": "聊天记录导出", "description": "导出聊天记录数据", "sort": 6},
        ]
    },
    # 工作台权限
    "dashboard": {
        "module_name": "工作台",
        "sort_order": 11,
        "items": [
            {"key": "DASHBOARD_VIEW", "name": "查看工作台", "description": "访问工作台首页", "sort": 1},
            {"key": "TODO_VIEW", "name": "查看待办事项", "description": "查看待处理的案件和任务", "sort": 2},
            {"key": "STATISTICS_VIEW", "name": "查看统计概览", "description": "查看数据统计概览", "sort": 3},
        ]
    }
}

# 默认角色权限配置（从 permission-data.ts 转换，yes -> editable, no -> none, limited -> readonly）
DEFAULT_ROLE_PERMISSIONS = {
    # 格式: "item_key": {"role_code": "permission_level"}
    "SYS_CONFIG": {"SUPER_ADMIN": "editable"},
    "USER_MANAGE": {"SUPER_ADMIN": "editable"},
    "ROLE_MANAGE": {"SUPER_ADMIN": "editable"},
    "LOG_VIEW": {"SUPER_ADMIN": "editable", "TENANT_ADMIN": "editable"},
    "DATA_BACKUP": {"SUPER_ADMIN": "editable"},
    
    "TENANT_VIEW": {"SUPER_ADMIN": "editable", "TENANT_ADMIN": "readonly"},
    "TENANT_CREATE": {"SUPER_ADMIN": "editable"},
    "TENANT_EDIT": {"SUPER_ADMIN": "editable", "TENANT_ADMIN": "readonly"},
    "TENANT_DELETE": {"SUPER_ADMIN": "editable"},
    "TENANT_SWITCH": {"SUPER_ADMIN": "editable"},
    "TENANT_FIELD_CONFIG": {"SUPER_ADMIN": "editable", "TENANT_ADMIN": "editable"},
    "TENANT_CHANNEL_MANAGE": {"SUPER_ADMIN": "editable", "TENANT_ADMIN": "editable"},
    "TENANT_QUEUE_MANAGE": {"SUPER_ADMIN": "editable", "TENANT_ADMIN": "editable"},
    
    "AGENCY_VIEW": {"SUPER_ADMIN": "editable", "TENANT_ADMIN": "editable", "AGENCY_ADMIN": "readonly"},
    "AGENCY_CREATE": {"SUPER_ADMIN": "editable", "TENANT_ADMIN": "editable"},
    "AGENCY_EDIT": {"SUPER_ADMIN": "editable", "TENANT_ADMIN": "editable", "AGENCY_ADMIN": "readonly"},
    "AGENCY_DELETE": {"SUPER_ADMIN": "editable", "TENANT_ADMIN": "editable"},
    "AGENCY_TIMEZONE": {"SUPER_ADMIN": "editable", "TENANT_ADMIN": "editable", "AGENCY_ADMIN": "readonly"},
    "AGENCY_WORKING_HOURS": {"SUPER_ADMIN": "editable", "TENANT_ADMIN": "editable", "AGENCY_ADMIN": "readonly"},
    "AGENCY_ADMIN_MANAGE": {"SUPER_ADMIN": "editable", "TENANT_ADMIN": "editable", "AGENCY_ADMIN": "readonly"},
    
    "TEAM_VIEW": {"SUPER_ADMIN": "editable", "TENANT_ADMIN": "editable", "AGENCY_ADMIN": "editable", "TEAM_LEADER": "readonly"},
    "TEAM_CREATE": {"SUPER_ADMIN": "editable", "TENANT_ADMIN": "editable", "AGENCY_ADMIN": "editable"},
    "TEAM_EDIT": {"SUPER_ADMIN": "editable", "TENANT_ADMIN": "editable", "AGENCY_ADMIN": "editable"},
    "TEAM_DELETE": {"SUPER_ADMIN": "editable", "TENANT_ADMIN": "editable", "AGENCY_ADMIN": "editable"},
    "TEAM_ADMIN_MANAGE": {"SUPER_ADMIN": "editable", "TENANT_ADMIN": "editable", "AGENCY_ADMIN": "editable"},
    "TEAM_MEMBER_ADD": {"SUPER_ADMIN": "editable", "TENANT_ADMIN": "editable", "AGENCY_ADMIN": "editable"},
    "TEAM_MEMBER_REMOVE": {"SUPER_ADMIN": "editable", "TENANT_ADMIN": "editable", "AGENCY_ADMIN": "editable"},
    
    "COLLECTOR_VIEW": {"SUPER_ADMIN": "editable", "TENANT_ADMIN": "editable", "AGENCY_ADMIN": "editable", "TEAM_LEADER": "readonly"},
    "COLLECTOR_CREATE": {"SUPER_ADMIN": "editable", "TENANT_ADMIN": "editable", "AGENCY_ADMIN": "editable"},
    "COLLECTOR_EDIT": {"SUPER_ADMIN": "editable", "TENANT_ADMIN": "editable", "AGENCY_ADMIN": "editable"},
    "COLLECTOR_DELETE": {"SUPER_ADMIN": "editable", "TENANT_ADMIN": "editable", "AGENCY_ADMIN": "editable"},
    "COLLECTOR_PASSWORD_RESET": {"SUPER_ADMIN": "editable", "TENANT_ADMIN": "editable", "AGENCY_ADMIN": "editable"},
    "COLLECTOR_EXPORT": {"SUPER_ADMIN": "editable", "TENANT_ADMIN": "editable", "AGENCY_ADMIN": "editable"},
    "COLLECTOR_STATUS": {"SUPER_ADMIN": "editable", "TENANT_ADMIN": "editable", "AGENCY_ADMIN": "editable"},
    
    "CASE_VIEW_ALL": {"SUPER_ADMIN": "editable", "TENANT_ADMIN": "editable"},
    "CASE_VIEW_AGENCY": {"SUPER_ADMIN": "editable", "TENANT_ADMIN": "editable", "AGENCY_ADMIN": "editable"},
    "CASE_VIEW_TEAM": {"SUPER_ADMIN": "editable", "TENANT_ADMIN": "editable", "AGENCY_ADMIN": "editable", "TEAM_LEADER": "editable"},
    "CASE_VIEW_SELF": {"SUPER_ADMIN": "editable", "TENANT_ADMIN": "editable", "AGENCY_ADMIN": "editable", "TEAM_LEADER": "editable", "COLLECTOR": "editable"},
    "CASE_ASSIGN": {"SUPER_ADMIN": "editable", "TENANT_ADMIN": "editable", "AGENCY_ADMIN": "editable"},
    "CASE_TRANSFER": {"SUPER_ADMIN": "editable", "TENANT_ADMIN": "editable", "AGENCY_ADMIN": "editable", "TEAM_LEADER": "editable"},
    "CASE_RECLAIM": {"SUPER_ADMIN": "editable", "TENANT_ADMIN": "editable", "AGENCY_ADMIN": "editable", "TEAM_LEADER": "editable"},
    "CASE_COLLECT": {"SUPER_ADMIN": "editable", "TENANT_ADMIN": "editable", "AGENCY_ADMIN": "editable", "TEAM_LEADER": "editable", "COLLECTOR": "editable"},
    "CASE_EDIT": {"SUPER_ADMIN": "editable", "TENANT_ADMIN": "editable", "AGENCY_ADMIN": "editable", "TEAM_LEADER": "editable", "COLLECTOR": "readonly"},
    "CASE_DELETE": {"SUPER_ADMIN": "editable", "TENANT_ADMIN": "editable", "AGENCY_ADMIN": "editable"},
    "CASE_EXPORT": {"SUPER_ADMIN": "editable", "TENANT_ADMIN": "editable", "AGENCY_ADMIN": "editable", "TEAM_LEADER": "editable", "COLLECTOR": "readonly"},
    
    "FIELD_STANDARD": {"SUPER_ADMIN": "editable"},
    "FIELD_CUSTOM": {"SUPER_ADMIN": "editable", "TENANT_ADMIN": "editable"},
    "FIELD_GROUP": {"SUPER_ADMIN": "editable"},
    "FIELD_QUEUE_CONFIG": {"SUPER_ADMIN": "editable", "TENANT_ADMIN": "editable"},
    
    "CHANNEL_SUPPLIER": {"SUPER_ADMIN": "editable", "TENANT_ADMIN": "editable"},
    "CHANNEL_LIMIT": {"SUPER_ADMIN": "editable", "TENANT_ADMIN": "editable"},
    "CHANNEL_STATISTICS": {"SUPER_ADMIN": "editable", "TENANT_ADMIN": "editable", "AGENCY_ADMIN": "editable", "TEAM_LEADER": "editable"},
    
    "PERFORMANCE_VIEW_ALL": {"SUPER_ADMIN": "editable", "TENANT_ADMIN": "editable"},
    "PERFORMANCE_VIEW_AGENCY": {"SUPER_ADMIN": "editable", "TENANT_ADMIN": "editable", "AGENCY_ADMIN": "editable"},
    "PERFORMANCE_VIEW_TEAM": {"SUPER_ADMIN": "editable", "TENANT_ADMIN": "editable", "AGENCY_ADMIN": "editable", "TEAM_LEADER": "readonly", "DATA_SOURCE": "editable"},
    "PERFORMANCE_VIEW_SELF": {"SUPER_ADMIN": "editable", "TENANT_ADMIN": "editable", "AGENCY_ADMIN": "editable", "TEAM_LEADER": "readonly", "DATA_SOURCE": "editable", "COLLECTOR": "editable"},
    "PERFORMANCE_EXPORT": {"SUPER_ADMIN": "editable", "TENANT_ADMIN": "editable", "AGENCY_ADMIN": "editable", "TEAM_LEADER": "editable", "DATA_SOURCE": "editable"},
    "PERFORMANCE_ANALYSIS": {"SUPER_ADMIN": "editable", "TENANT_ADMIN": "editable", "AGENCY_ADMIN": "editable", "TEAM_LEADER": "editable", "DATA_SOURCE": "editable"},
    
    "CHAT_VIEW_ALL": {"SUPER_ADMIN": "editable", "TENANT_ADMIN": "editable"},
    "CHAT_VIEW_AGENCY": {"SUPER_ADMIN": "editable", "TENANT_ADMIN": "editable", "AGENCY_ADMIN": "editable"},
    "CHAT_VIEW_TEAM": {"SUPER_ADMIN": "editable", "TENANT_ADMIN": "editable", "AGENCY_ADMIN": "editable", "TEAM_LEADER": "readonly"},
    "CHAT_VIEW_MEMBER": {"SUPER_ADMIN": "editable", "TENANT_ADMIN": "editable", "AGENCY_ADMIN": "editable", "TEAM_LEADER": "readonly", "QUALITY_INSPECTOR": "readonly"},
    "CHAT_VIEW_SELF": {"SUPER_ADMIN": "editable", "TENANT_ADMIN": "editable", "AGENCY_ADMIN": "editable", "TEAM_LEADER": "editable", "COLLECTOR": "editable"},
    "CHAT_EXPORT": {"SUPER_ADMIN": "editable", "TENANT_ADMIN": "editable", "AGENCY_ADMIN": "editable", "TEAM_LEADER": "editable", "QUALITY_INSPECTOR": "editable"},
    
    "DASHBOARD_VIEW": {"SUPER_ADMIN": "editable", "TENANT_ADMIN": "editable", "AGENCY_ADMIN": "editable", "TEAM_LEADER": "editable", "QUALITY_INSPECTOR": "editable", "DATA_SOURCE": "editable", "COLLECTOR": "editable"},
    "TODO_VIEW": {"SUPER_ADMIN": "editable", "TENANT_ADMIN": "editable", "AGENCY_ADMIN": "editable", "TEAM_LEADER": "editable", "COLLECTOR": "editable"},
    "STATISTICS_VIEW": {"SUPER_ADMIN": "editable", "TENANT_ADMIN": "editable", "AGENCY_ADMIN": "editable", "TEAM_LEADER": "editable", "DATA_SOURCE": "editable"},
}


def init_permissions(db: Session):
    """初始化权限数据"""
    print("开始初始化权限系统...")
    
    # 检查是否已经初始化
    existing_modules = db.query(PermissionModule).count()
    if existing_modules > 0:
        print(f"检测到已有 {existing_modules} 个权限模块，跳过初始化")
        return
    
    # 1. 创建权限模块和权限项
    print("\n1. 创建权限模块和权限项...")
    item_id_map = {}  # 用于存储 item_key -> item_id 的映射
    
    for module_key, module_data in PERMISSION_DATA.items():
        # 创建模块
        module = PermissionModule(
            module_key=module_key,
            module_name=module_data["module_name"],
            sort_order=module_data["sort_order"]
        )
        db.add(module)
        db.flush()  # 获取模块 ID
        print(f"  ✓ 创建模块: {module.module_name} ({module_key})")
        
        # 创建该模块下的权限项
        for item_data in module_data["items"]:
            item = PermissionItem(
                module_id=module.id,
                item_key=item_data["key"],
                item_name=item_data["name"],
                description=item_data["description"],
                sort_order=item_data["sort"]
            )
            db.add(item)
            db.flush()  # 获取权限项 ID
            item_id_map[item_data["key"]] = item.id
            print(f"    ✓ 创建权限项: {item.item_name} ({item_data['key']})")
    
    db.commit()
    print(f"  共创建 {len(PERMISSION_DATA)} 个模块，{len(item_id_map)} 个权限项")
    
    # 2. 创建系统默认角色权限配置
    print("\n2. 创建系统默认角色权限配置（tenant_id = NULL）...")
    config_count = 0
    
    for item_key, role_permissions in DEFAULT_ROLE_PERMISSIONS.items():
        item_id = item_id_map.get(item_key)
        if not item_id:
            print(f"  ⚠ 警告: 权限项 {item_key} 未找到，跳过")
            continue
        
        for role_code, permission_level in role_permissions.items():
            config = RolePermissionConfig(
                tenant_id=None,  # NULL 表示系统默认配置
                role_code=role_code,
                permission_item_id=item_id,
                permission_level=permission_level
            )
            db.add(config)
            config_count += 1
    
    db.commit()
    print(f"  共创建 {config_count} 条系统默认权限配置")
    
    print("\n✓ 权限系统初始化完成！")
    print(f"\n统计信息:")
    print(f"  - 权限模块: {db.query(PermissionModule).count()} 个")
    print(f"  - 权限项: {db.query(PermissionItem).count()} 个")
    print(f"  - 默认权限配置: {db.query(RolePermissionConfig).filter(RolePermissionConfig.tenant_id.is_(None)).count()} 条")


def main():
    """主函数"""
    # 创建表
    Base.metadata.create_all(bind=engine)
    
    # 初始化数据
    db = SessionLocal()
    try:
        init_permissions(db)
    except Exception as e:
        print(f"\n✗ 初始化失败: {e}")
        import traceback
        traceback.print_exc()
        db.rollback()
    finally:
        db.close()


if __name__ == "__main__":
    main()

