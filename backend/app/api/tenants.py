from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from typing import List, Dict, Any
from datetime import datetime, timedelta
from app.core.database import get_db
from app.models.tenant import Tenant
from app.models.tenant_field_config import TenantFieldConfig
from app.models.case_queue import CaseQueue
from app.models.collection_agency import CollectionAgency
from app.models.standard_field import StandardField
from app.models.custom_field import CustomField
from app.models.team_admin_account import TeamAdminAccount
from app.models.team_group import TeamGroup
from app.models.collection_team import CollectionTeam
from app.models.collector import Collector
from sqlalchemy import func
from app.schemas.tenant import (
    TenantCreate,
    TenantUpdate,
    TenantResponse,
    TenantFieldConfigCreate,
    TenantFieldConfigUpdate,
    TenantFieldConfigResponse
)

router = APIRouter(prefix="/tenants", tags=["甲方管理"])


@router.get("", response_model=List[TenantResponse])
def get_tenants(
    skip: int = 0,
    limit: int = 100,
    db: Session = Depends(get_db)
):
    """获取甲方列表"""
    # 使用is_(True)而不是== True，避免None值比较问题
    from sqlalchemy import true
    tenants = db.query(Tenant).filter(Tenant.is_active.is_(True)).offset(skip).limit(limit).all()
    return tenants


@router.get("/{tenant_id}", response_model=TenantResponse)
def get_tenant(tenant_id: int, db: Session = Depends(get_db)):
    """获取单个甲方"""
    tenant = db.query(Tenant).filter(Tenant.id == tenant_id).first()
    if not tenant:
        raise HTTPException(status_code=404, detail="甲方不存在")
    return tenant


@router.post("", response_model=TenantResponse)
def create_tenant(tenant: TenantCreate, db: Session = Depends(get_db)):
    """创建甲方"""
    # 检查 tenant_code 是否已存在
    existing = db.query(Tenant).filter(Tenant.tenant_code == tenant.tenant_code).first()
    if existing:
        raise HTTPException(status_code=400, detail="甲方编码已存在")
    
    db_tenant = Tenant(**tenant.model_dump())
    db.add(db_tenant)
    db.commit()
    db.refresh(db_tenant)
    return db_tenant


@router.put("/{tenant_id}", response_model=TenantResponse)
def update_tenant(
    tenant_id: int,
    tenant: TenantUpdate,
    db: Session = Depends(get_db)
):
    """更新甲方"""
    db_tenant = db.query(Tenant).filter(Tenant.id == tenant_id).first()
    if not db_tenant:
        raise HTTPException(status_code=404, detail="甲方不存在")
    
    update_data = tenant.model_dump(exclude_unset=True)
    for field, value in update_data.items():
        setattr(db_tenant, field, value)
    
    db.commit()
    db.refresh(db_tenant)
    return db_tenant


@router.delete("/{tenant_id}")
def delete_tenant(tenant_id: int, db: Session = Depends(get_db)):
    """删除甲方"""
    db_tenant = db.query(Tenant).filter(Tenant.id == tenant_id).first()
    if not db_tenant:
        raise HTTPException(status_code=404, detail="甲方不存在")
    
    db_tenant.is_active = False
    db.commit()
    return {"message": "甲方已删除"}


# 甲方字段配置相关接口
@router.get("/{tenant_id}/field-configs", response_model=List[TenantFieldConfigResponse])
def get_tenant_field_configs(tenant_id: int, db: Session = Depends(get_db)):
    """获取甲方字段配置列表"""
    configs = db.query(TenantFieldConfig).filter(
        TenantFieldConfig.tenant_id == tenant_id
    ).all()
    return configs


@router.post("/{tenant_id}/field-configs", response_model=TenantFieldConfigResponse)
def create_tenant_field_config(
    tenant_id: int,
    config: TenantFieldConfigCreate,
    db: Session = Depends(get_db)
):
    """创建甲方字段配置"""
    if config.tenant_id != tenant_id:
        raise HTTPException(status_code=400, detail="tenant_id不匹配")
    
    # 检查是否已存在
    existing = db.query(TenantFieldConfig).filter(
        TenantFieldConfig.tenant_id == tenant_id,
        TenantFieldConfig.field_id == config.field_id,
        TenantFieldConfig.field_type == config.field_type
    ).first()
    if existing:
        raise HTTPException(status_code=400, detail="字段配置已存在")
    
    db_config = TenantFieldConfig(**config.model_dump())
    db.add(db_config)
    db.commit()
    db.refresh(db_config)
    return db_config


@router.put("/{tenant_id}/field-configs/{config_id}", response_model=TenantFieldConfigResponse)
def update_tenant_field_config(
    tenant_id: int,
    config_id: int,
    config: TenantFieldConfigUpdate,
    db: Session = Depends(get_db)
):
    """更新甲方字段配置"""
    db_config = db.query(TenantFieldConfig).filter(
        TenantFieldConfig.id == config_id,
        TenantFieldConfig.tenant_id == tenant_id
    ).first()
    if not db_config:
        raise HTTPException(status_code=404, detail="字段配置不存在")
    
    update_data = config.model_dump(exclude_unset=True)
    for field, value in update_data.items():
        setattr(db_config, field, value)
    
    db.commit()
    db.refresh(db_config)
    return db_config


@router.delete("/{tenant_id}/field-configs/{config_id}")
def delete_tenant_field_config(tenant_id: int, config_id: int, db: Session = Depends(get_db)):
    """删除甲方字段配置"""
    db_config = db.query(TenantFieldConfig).filter(
        TenantFieldConfig.id == config_id,
        TenantFieldConfig.tenant_id == tenant_id
    ).first()
    if not db_config:
        raise HTTPException(status_code=404, detail="字段配置不存在")
    
    db.delete(db_config)
    db.commit()
    return {"message": "字段配置已删除"}


# 甲方队列相关接口
@router.get("/{tenant_id}/queues")
def get_tenant_queues(tenant_id: int, db: Session = Depends(get_db)):
    """获取甲方的案件队列列表"""
    # 验证甲方是否存在
    tenant = db.query(Tenant).filter(Tenant.id == tenant_id).first()
    if not tenant:
        raise HTTPException(status_code=404, detail="甲方不存在")
    
    queues = db.query(CaseQueue).filter(
        CaseQueue.tenant_id == tenant_id,
        CaseQueue.is_active.is_(True)
    ).order_by(CaseQueue.sort_order).all()
    
    return queues


# 甲方管理员账号相关接口
@router.get("/{tenant_id}/admin-accounts")
def get_tenant_admin_accounts(tenant_id: int, db: Session = Depends(get_db)):
    """获取甲方的管理员账号列表"""
    # 验证甲方是否存在
    tenant = db.query(Tenant).filter(Tenant.id == tenant_id).first()
    if not tenant:
        raise HTTPException(status_code=404, detail="甲方不存在")
    
    # 查询该甲方的管理员账号（没有agency_id，或者agency_id为null的甲方级别管理员）
    accounts = db.query(TeamAdminAccount).filter(
        TeamAdminAccount.tenant_id == tenant_id,
        TeamAdminAccount.agency_id.is_(None),
        TeamAdminAccount.is_active.is_(True)
    ).order_by(TeamAdminAccount.account_code).all()
    
    # 转换为字典格式
    result = []
    for account in accounts:
        account_dict = {
            "id": account.id,
            "account_code": account.account_code,
            "account_name": account.account_name,
            "login_id": account.login_id,
            "role": account.role,
            "mobile": account.mobile,
            "email": account.email,
            "remark": account.remark,
            "is_active": account.is_active,
            "created_at": account.created_at.isoformat() if account.created_at else None,
            "updated_at": account.updated_at.isoformat() if account.updated_at else None
        }
        result.append(account_dict)
    
    return result


# 甲方机构相关接口
@router.get("/{tenant_id}/agencies")
def get_tenant_agencies(tenant_id: int, db: Session = Depends(get_db)):
    """获取甲方的催收机构列表（包含统计信息）"""
    # 验证甲方是否存在
    tenant = db.query(Tenant).filter(Tenant.id == tenant_id).first()
    if not tenant:
        raise HTTPException(status_code=404, detail="甲方不存在")
    
    agencies = db.query(CollectionAgency).filter(
        CollectionAgency.tenant_id == tenant_id,
        CollectionAgency.is_active.is_(True)
    ).order_by(CollectionAgency.sort_order).all()
    
    # 添加统计数据和关联信息
    result = []
    for agency in agencies:
        # 获取机构管理员信息
        admin_name = None
        admin_account = db.query(TeamAdminAccount).filter(
            TeamAdminAccount.agency_id == agency.id,
            TeamAdminAccount.team_group_id.is_(None),
            TeamAdminAccount.team_id.is_(None),
            TeamAdminAccount.is_active.is_(True)
        ).first()
        if admin_account:
            admin_name = admin_account.account_name
        
        agency_dict = {
            **agency.__dict__,
            "tenant_name": tenant.tenant_name,
            "admin_name": admin_name,
            "team_group_count": db.query(func.count(TeamGroup.id)).filter(
                TeamGroup.agency_id == agency.id
            ).scalar() or 0,
            "team_count": db.query(func.count(CollectionTeam.id)).filter(
                CollectionTeam.agency_id == agency.id
            ).scalar() or 0,
            "collector_count": db.query(func.count(Collector.id)).join(
                CollectionTeam, Collector.team_id == CollectionTeam.id
            ).filter(CollectionTeam.agency_id == agency.id).scalar() or 0,
        }
        result.append(agency_dict)
    
    return result


# 甲方字段JSON数据接口
@router.get("/{tenant_id}/fields-json")
def get_tenant_fields_json(tenant_id: int, db: Session = Depends(get_db)):
    """获取甲方的字段JSON数据（用于显示甲方通过API传入的字段）"""
    # 验证甲方是否存在
    tenant = db.query(Tenant).filter(Tenant.id == tenant_id).first()
    if not tenant:
        raise HTTPException(status_code=404, detail="甲方不存在")
    
    fields_list = []
    
    # 获取该甲方的字段配置
    field_configs = db.query(TenantFieldConfig).filter(
        TenantFieldConfig.tenant_id == tenant_id,
        TenantFieldConfig.is_enabled.is_(True)
    ).order_by(TenantFieldConfig.sort_order).all()
    
    # 处理每个字段配置
    for config in field_configs:
        field_data: Dict[str, Any] = {}
        
        if config.field_type == "standard":
            # 查询标准字段
            standard_field = db.query(StandardField).filter(
                StandardField.id == config.field_id,
                StandardField.is_active.is_(True),
                StandardField.is_deleted.is_(False)
            ).first()
            
            if standard_field:
                field_data = {
                    "id": standard_field.id,
                    "field_name": standard_field.field_name,
                    "field_key": standard_field.field_key,
                    "field_type": standard_field.field_type,
                    "field_group_id": standard_field.field_group_id,
                    "is_required": config.is_required if config.is_required is not None else standard_field.is_required,
                    "sort_order": config.sort_order if config.sort_order is not None else standard_field.sort_order,
                    "enum_values": standard_field.enum_options if standard_field.enum_options else [],
                    "description": standard_field.description,
                    "example_value": standard_field.example_value,
                }
        
        elif config.field_type == "custom":
            # 查询自定义字段
            custom_field = db.query(CustomField).filter(
                CustomField.id == config.field_id,
                CustomField.tenant_id == tenant_id,
                CustomField.is_active.is_(True),
                CustomField.is_deleted.is_(False)
            ).first()
            
            if custom_field:
                field_data = {
                    "id": custom_field.id,
                    "field_name": custom_field.field_name,
                    "field_key": custom_field.field_key,
                    "field_type": custom_field.field_type,
                    "field_group_id": custom_field.field_group_id,
                    "is_required": config.is_required if config.is_required is not None else custom_field.is_required,
                    "sort_order": config.sort_order if config.sort_order is not None else custom_field.sort_order,
                    "enum_values": custom_field.enum_options if custom_field.enum_options else [],
                    "description": custom_field.description,
                    "example_value": custom_field.example_value,
                }
        
        if field_data:
            fields_list.append(field_data)
    
    # 如果没有配置，返回所有标准字段（作为默认值）
    if not fields_list:
        standard_fields = db.query(StandardField).filter(
            StandardField.is_active.is_(True),
            StandardField.is_deleted.is_(False)
        ).order_by(StandardField.sort_order).all()
        
        for sf in standard_fields:
            fields_list.append({
                "id": sf.id,
                "field_name": sf.field_name,
                "field_key": sf.field_key,
                "field_type": sf.field_type,
                "field_group_id": sf.field_group_id,
                "is_required": sf.is_required,
                "sort_order": sf.sort_order,
                "enum_values": sf.enum_options if sf.enum_options else [],
                "description": sf.description,
                "example_value": sf.example_value,
            })
    
    return {
        "fetched_at": datetime.now().isoformat(),
        "fields": fields_list
    }


# 甲方扩展字段接口
@router.get("/{tenant_id}/extended-fields")
def get_tenant_extended_fields(tenant_id: int, db: Session = Depends(get_db)):
    """获取甲方的扩展字段列表"""
    # 验证甲方是否存在
    tenant = db.query(Tenant).filter(Tenant.id == tenant_id).first()
    if not tenant:
        raise HTTPException(status_code=404, detail="甲方不存在")
    
    # 查询扩展字段（is_extended=True的标准字段）
    extended_fields = db.query(StandardField).filter(
        StandardField.is_extended.is_(True),
        StandardField.is_active.is_(True),
        StandardField.is_deleted.is_(False)
    ).order_by(StandardField.sort_order).all()
    
    # 转换为响应格式
    result = []
    for field in extended_fields:
        result.append({
            "id": field.id,
            "field_alias": field.field_key,
            "tenant_field_key": field.field_key,  # 使用标准字段的field_key作为默认值
            "tenant_field_name": field.field_name,
            "field_type": field.field_type,
            "privacy_label": "PII",  # 默认值，可以从配置中读取
            "retention_days": 365,  # 默认值，可以从配置中读取
            "allow_report": True,  # 默认值
            "allow_query_filter": False  # 默认值
        })
    
    # 如果没有扩展字段，返回mock数据
    if not result:
        result = [
            {
                "id": 1001,
                "field_alias": "company_name",
                "tenant_field_key": "COMP_NAME",
                "tenant_field_name": "公司名称",
                "field_type": "String",
                "privacy_label": "PII",
                "retention_days": 365,
                "allow_report": True,
                "allow_query_filter": False
            },
            {
                "id": 1002,
                "field_alias": "company_address",
                "tenant_field_key": "COMP_ADDR",
                "tenant_field_name": "公司地址",
                "field_type": "String",
                "privacy_label": "公开",
                "retention_days": 730,
                "allow_report": True,
                "allow_query_filter": True
            },
            {
                "id": 1003,
                "field_alias": "emergency_contact",
                "tenant_field_key": "EMERGENCY_CONTACT",
                "tenant_field_name": "紧急联系人",
                "field_type": "String",
                "privacy_label": "PII",
                "retention_days": 365,
                "allow_report": False,
                "allow_query_filter": False
            },
            {
                "id": 1004,
                "field_alias": "social_media_account",
                "tenant_field_key": "SOCIAL_MEDIA",
                "tenant_field_name": "社交媒体账号",
                "field_type": "String",
                "privacy_label": "敏感",
                "retention_days": 180,
                "allow_report": False,
                "allow_query_filter": False
            },
            {
                "id": 1005,
                "field_alias": "referral_source",
                "tenant_field_key": "REFERRAL_SOURCE",
                "tenant_field_name": "推荐来源",
                "field_type": "Enum",
                "privacy_label": "公开",
                "retention_days": 1095,
                "allow_report": True,
                "allow_query_filter": True
            }
        ]
    
    return result


# 甲方未映射字段接口
@router.get("/{tenant_id}/unmapped-fields")
def get_tenant_unmapped_fields(tenant_id: int, db: Session = Depends(get_db)):
    """获取甲方未映射的字段列表（甲方字段中未匹配到标准字段的）"""
    # 验证甲方是否存在
    tenant = db.query(Tenant).filter(Tenant.id == tenant_id).first()
    if not tenant:
        raise HTTPException(status_code=404, detail="甲方不存在")
    
    # 获取甲方字段JSON数据
    tenant_fields_response = get_tenant_fields_json(tenant_id, db)
    tenant_fields = tenant_fields_response.get("fields", [])
    
    # 获取所有标准字段的field_key
    standard_fields = db.query(StandardField).filter(
        StandardField.is_active.is_(True),
        StandardField.is_deleted.is_(False)
    ).all()
    standard_field_keys = {sf.field_key.lower() for sf in standard_fields}
    
    # 查找未映射的字段（甲方字段中不在标准字段列表中的）
    unmapped_fields = []
    for tf in tenant_fields:
        field_key = tf.get("field_key", "").lower()
        if field_key and field_key not in standard_field_keys:
            unmapped_fields.append({
                "tenant_field_key": tf.get("field_key"),
                "tenant_field_name": tf.get("field_name"),
                "field_type": tf.get("field_type"),
                "is_required": tf.get("is_required", False),
                "tenant_updated_at": tf.get("updated_at") or datetime.now().isoformat()
            })
    
    # 如果没有未映射字段，返回mock数据
    if not unmapped_fields:
        unmapped_fields = [
            {
                "tenant_field_key": "EXTRA_FIELD_1",
                "tenant_field_name": "额外字段1",
                "field_type": "String",
                "is_required": False,
                "tenant_updated_at": (datetime.now() - timedelta(days=5)).isoformat()
            },
            {
                "tenant_field_key": "EXTRA_FIELD_2",
                "tenant_field_name": "额外字段2",
                "field_type": "Integer",
                "is_required": True,
                "tenant_updated_at": (datetime.now() - timedelta(days=3)).isoformat()
            },
            {
                "tenant_field_key": "CUSTOM_REFERENCE_ID",
                "tenant_field_name": "自定义参考ID",
                "field_type": "String",
                "is_required": False,
                "tenant_updated_at": (datetime.now() - timedelta(days=10)).isoformat()
            },
            {
                "tenant_field_key": "INTERNAL_NOTES",
                "tenant_field_name": "内部备注",
                "field_type": "Text",
                "is_required": False,
                "tenant_updated_at": (datetime.now() - timedelta(days=1)).isoformat()
            },
            {
                "tenant_field_key": "THIRD_PARTY_ID",
                "tenant_field_name": "第三方ID",
                "field_type": "String",
                "is_required": False,
                "tenant_updated_at": (datetime.now() - timedelta(days=7)).isoformat()
            },
            {
                "tenant_field_key": "LEGACY_SYSTEM_CODE",
                "tenant_field_name": "遗留系统编码",
                "field_type": "String",
                "is_required": False,
                "tenant_updated_at": (datetime.now() - timedelta(days=15)).isoformat()
            },
            {
                "tenant_field_key": "SPECIAL_FLAG",
                "tenant_field_name": "特殊标记",
                "field_type": "Enum",
                "is_required": False,
                "tenant_updated_at": (datetime.now() - timedelta(days=2)).isoformat()
            },
            {
                "tenant_field_key": "BACKUP_PHONE",
                "tenant_field_name": "备用电话",
                "field_type": "String",
                "is_required": False,
                "tenant_updated_at": datetime.now().isoformat()
            }
        ]
    
    return unmapped_fields

