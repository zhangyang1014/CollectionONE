"""甲方字段展示配置API"""
from typing import List, Optional
from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.orm import Session
from app.core.database import get_db
from app.models.tenant_field_display_config import TenantFieldDisplayConfig
from app.models.standard_field import StandardField
from app.models.custom_field import CustomField
from app.models.field_group import FieldGroup
from app.schemas.field_display import (
    FieldDisplayConfigCreate,
    FieldDisplayConfigUpdate,
    FieldDisplayConfigResponse,
    FieldDisplayConfigBatchUpdate,
    SceneType,
    AvailableFieldOption
)

router = APIRouter(prefix="/field-display-configs", tags=["字段展示配置"])


@router.get("/scene-types", response_model=List[SceneType])
async def get_scene_types():
    """获取所有场景类型"""
    return [
        {
            "key": "admin_case_list",
            "name": "控台案件管理列表",
            "description": "管理后台的案件列表页面"
        },
        {
            "key": "collector_case_list",
            "name": "催员案件列表",
            "description": "催员端的案件列表页面"
        },
        {
            "key": "collector_case_detail",
            "name": "催员案件详情",
            "description": "催员端的案件详情页面"
        }
    ]


@router.get("/available-fields", response_model=List[AvailableFieldOption])
async def get_available_fields(
    tenant_id: Optional[int] = Query(None, description="甲方ID（用于获取自定义字段）"),
    db: Session = Depends(get_db)
):
    """获取可用字段选项列表（包括标准字段和自定义字段）"""
    result = []
    
    # 获取标准字段（包括扩展字段）
    standard_fields = db.query(StandardField, FieldGroup.group_name).join(
        FieldGroup, StandardField.field_group_id == FieldGroup.id
    ).filter(
        StandardField.is_active == True,
        StandardField.is_deleted == False
    ).order_by(
        StandardField.field_group_id,
        StandardField.sort_order
    ).all()
    
    for field, group_name in standard_fields:
        result.append({
            "field_key": field.field_key,
            "field_name": field.field_name,
            "field_type": field.field_type,
            "field_source": "extended" if field.is_extended else "standard",
            "field_group_name": group_name,
            "is_extended": field.is_extended,
            "is_required": field.is_required,
            "enum_options": field.enum_options,
            "description": field.description
        })
    
    # 如果提供了tenant_id，获取自定义字段
    if tenant_id:
        custom_fields = db.query(CustomField, FieldGroup.group_name).join(
            FieldGroup, CustomField.field_group_id == FieldGroup.id
        ).filter(
            CustomField.tenant_id == tenant_id,
            CustomField.is_active == True,
            CustomField.is_deleted == False
        ).order_by(
            CustomField.field_group_id,
            CustomField.sort_order
        ).all()
        
        for field, group_name in custom_fields:
            result.append({
                "field_key": field.field_key,
                "field_name": field.field_name,
                "field_type": field.field_type,
                "field_source": "custom",
                "field_group_name": group_name,
                "is_extended": False,
                "is_required": field.is_required,
                "enum_options": field.enum_options,
                "description": field.description
            })
    
    return result


@router.get("", response_model=List[FieldDisplayConfigResponse])
async def get_field_display_configs(
    tenant_id: Optional[int] = Query(None, description="甲方ID"),
    scene_type: Optional[str] = Query(None, description="场景类型"),
    field_key: Optional[str] = Query(None, description="字段标识"),
    is_enabled: Optional[bool] = Query(None, description="是否启用"),
    db: Session = Depends(get_db)
):
    """获取字段展示配置列表"""
    query = db.query(TenantFieldDisplayConfig)
    
    if tenant_id:
        query = query.filter(TenantFieldDisplayConfig.tenant_id == tenant_id)
    if scene_type:
        query = query.filter(TenantFieldDisplayConfig.scene_type == scene_type)
    if field_key:
        query = query.filter(TenantFieldDisplayConfig.field_key == field_key)
    if is_enabled is not None:
        query = query.filter(TenantFieldDisplayConfig.is_enabled == is_enabled)
    
    configs = query.order_by(
        TenantFieldDisplayConfig.scene_type,
        TenantFieldDisplayConfig.sort_order,
        TenantFieldDisplayConfig.id
    ).all()
    
    return configs


@router.get("/{config_id}", response_model=FieldDisplayConfigResponse)
async def get_field_display_config(
    config_id: int,
    db: Session = Depends(get_db)
):
    """获取单个字段展示配置"""
    config = db.query(TenantFieldDisplayConfig).filter(
        TenantFieldDisplayConfig.id == config_id
    ).first()
    
    if not config:
        raise HTTPException(status_code=404, detail="配置不存在")
    
    return config


@router.post("", response_model=FieldDisplayConfigResponse)
async def create_field_display_config(
    config: FieldDisplayConfigCreate,
    db: Session = Depends(get_db)
):
    """创建字段展示配置"""
    # 检查是否已存在相同配置
    existing = db.query(TenantFieldDisplayConfig).filter(
        TenantFieldDisplayConfig.tenant_id == config.tenant_id,
        TenantFieldDisplayConfig.scene_type == config.scene_type,
        TenantFieldDisplayConfig.field_key == config.field_key
    ).first()
    
    if existing:
        raise HTTPException(status_code=400, detail="该场景下已存在相同字段的配置")
    
    # 转换 Pydantic 模型为字典
    config_dict = config.model_dump()
    
    # 处理嵌套对象
    if config_dict.get('color_rule'):
        config_dict['color_rule'] = [rule.dict() if hasattr(rule, 'dict') else rule for rule in config_dict['color_rule']]
    if config_dict.get('hide_rule'):
        config_dict['hide_rule'] = [rule.dict() if hasattr(rule, 'dict') else rule for rule in config_dict['hide_rule']]
    if config_dict.get('format_rule'):
        config_dict['format_rule'] = config_dict['format_rule'].dict() if hasattr(config_dict['format_rule'], 'dict') else config_dict['format_rule']
    
    db_config = TenantFieldDisplayConfig(**config_dict)
    db.add(db_config)
    db.commit()
    db.refresh(db_config)
    
    return db_config


@router.put("/{config_id}", response_model=FieldDisplayConfigResponse)
async def update_field_display_config(
    config_id: int,
    config: FieldDisplayConfigUpdate,
    db: Session = Depends(get_db)
):
    """更新字段展示配置"""
    db_config = db.query(TenantFieldDisplayConfig).filter(
        TenantFieldDisplayConfig.id == config_id
    ).first()
    
    if not db_config:
        raise HTTPException(status_code=404, detail="配置不存在")
    
    # 更新字段
    update_data = config.model_dump(exclude_unset=True)
    
    # 处理嵌套对象
    if 'color_rule' in update_data and update_data['color_rule']:
        update_data['color_rule'] = [rule.dict() if hasattr(rule, 'dict') else rule for rule in update_data['color_rule']]
    if 'hide_rule' in update_data and update_data['hide_rule']:
        update_data['hide_rule'] = [rule.dict() if hasattr(rule, 'dict') else rule for rule in update_data['hide_rule']]
    if 'format_rule' in update_data and update_data['format_rule']:
        update_data['format_rule'] = update_data['format_rule'].dict() if hasattr(update_data['format_rule'], 'dict') else update_data['format_rule']
    
    for field, value in update_data.items():
        setattr(db_config, field, value)
    
    db.commit()
    db.refresh(db_config)
    
    return db_config


@router.delete("/{config_id}")
async def delete_field_display_config(
    config_id: int,
    db: Session = Depends(get_db)
):
    """删除字段展示配置"""
    db_config = db.query(TenantFieldDisplayConfig).filter(
        TenantFieldDisplayConfig.id == config_id
    ).first()
    
    if not db_config:
        raise HTTPException(status_code=404, detail="配置不存在")
    
    db.delete(db_config)
    db.commit()
    
    return {"message": "删除成功"}


@router.post("/batch", response_model=List[FieldDisplayConfigResponse])
async def batch_create_or_update_configs(
    tenant_id: str,
    scene_type: str,
    configs: List[FieldDisplayConfigCreate],
    db: Session = Depends(get_db)
):
    """批量创建或更新字段展示配置"""
    result = []
    
    for config in configs:
        # 检查是否已存在
        existing = db.query(TenantFieldDisplayConfig).filter(
            TenantFieldDisplayConfig.tenant_id == config.tenant_id,
            TenantFieldDisplayConfig.scene_type == config.scene_type,
            TenantFieldDisplayConfig.field_key == config.field_key
        ).first()
        
        if existing:
            # 更新
            update_data = config.model_dump(exclude={'tenant_id', 'scene_type', 'field_key'})
            
            # 处理嵌套对象
            if update_data.get('color_rule'):
                update_data['color_rule'] = [rule.dict() if hasattr(rule, 'dict') else rule for rule in update_data['color_rule']]
            if update_data.get('hide_rule'):
                update_data['hide_rule'] = [rule.dict() if hasattr(rule, 'dict') else rule for rule in update_data['hide_rule']]
            if update_data.get('format_rule'):
                update_data['format_rule'] = update_data['format_rule'].dict() if hasattr(update_data['format_rule'], 'dict') else update_data['format_rule']
            
            for field, value in update_data.items():
                setattr(existing, field, value)
            result.append(existing)
        else:
            # 创建
            config_dict = config.model_dump()
            
            # 处理嵌套对象
            if config_dict.get('color_rule'):
                config_dict['color_rule'] = [rule.dict() if hasattr(rule, 'dict') else rule for rule in config_dict['color_rule']]
            if config_dict.get('hide_rule'):
                config_dict['hide_rule'] = [rule.dict() if hasattr(rule, 'dict') else rule for rule in config_dict['hide_rule']]
            if config_dict.get('format_rule'):
                config_dict['format_rule'] = config_dict['format_rule'].dict() if hasattr(config_dict['format_rule'], 'dict') else config_dict['format_rule']
            
            db_config = TenantFieldDisplayConfig(**config_dict)
            db.add(db_config)
            result.append(db_config)
    
    db.commit()
    
    # 刷新所有对象
    for config in result:
        db.refresh(config)
    
    return result


@router.post("/copy")
async def copy_scene_config(
    from_scene: str = Query(..., description="源场景类型"),
    to_scene: str = Query(..., description="目标场景类型"),
    tenant_id: str = Query(..., description="甲方ID"),
    db: Session = Depends(get_db)
):
    """复制场景配置"""
    # 获取源场景的所有配置
    source_configs = db.query(TenantFieldDisplayConfig).filter(
        TenantFieldDisplayConfig.tenant_id == tenant_id,
        TenantFieldDisplayConfig.scene_type == from_scene
    ).all()
    
    if not source_configs:
        raise HTTPException(status_code=404, detail="源场景没有配置")
    
    # 删除目标场景的现有配置
    db.query(TenantFieldDisplayConfig).filter(
        TenantFieldDisplayConfig.tenant_id == tenant_id,
        TenantFieldDisplayConfig.scene_type == to_scene
    ).delete()
    
    # 复制配置
    for source in source_configs:
        new_config = TenantFieldDisplayConfig(
            tenant_id=source.tenant_id,
            scene_type=to_scene,
            scene_name=source.scene_name,
            field_key=source.field_key,
            field_name=source.field_name,
            is_visible=source.is_visible,
            sort_order=source.sort_order,
            display_width=source.display_width,
            color_type=source.color_type,
            color_rule=source.color_rule,
            hide_rule=source.hide_rule,
            hide_for_queues=source.hide_for_queues,
            hide_for_agencies=source.hide_for_agencies,
            hide_for_teams=source.hide_for_teams,
            is_fixed=source.is_fixed,
            align=source.align,
            format_rule=source.format_rule,
            is_enabled=source.is_enabled
        )
        db.add(new_config)
    
    db.commit()
    
    return {"message": f"成功复制 {len(source_configs)} 个配置"}

