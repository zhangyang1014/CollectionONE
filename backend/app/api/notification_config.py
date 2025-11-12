"""通知配置管理 API"""
from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.orm import Session
from typing import List, Optional
from app.core.database import get_db
from app.models.notification_config import NotificationConfig
from app.schemas.notification import (
    NotificationConfigResponse,
    NotificationConfigCreate,
    NotificationConfigUpdate
)

router = APIRouter(prefix="/notification-configs", tags=["通知配置管理"])


@router.get("", response_model=List[NotificationConfigResponse])
def list_notification_configs(
    tenant_id: Optional[int] = Query(None, description="甲方ID（不传则返回全局配置）"),
    notification_type: Optional[str] = Query(None, description="通知类型"),
    db: Session = Depends(get_db)
):
    """获取通知配置列表"""
    query = db.query(NotificationConfig)
    
    if tenant_id is not None:
        query = query.filter(NotificationConfig.tenant_id == tenant_id)
    else:
        # 如果未指定tenant_id，返回全局配置（tenant_id为NULL）
        query = query.filter(NotificationConfig.tenant_id.is_(None))
    
    if notification_type:
        query = query.filter(NotificationConfig.notification_type == notification_type)
    
    configs = query.order_by(NotificationConfig.notification_type).all()
    return configs


@router.get("/{config_id}", response_model=NotificationConfigResponse)
def get_notification_config(
    config_id: int,
    db: Session = Depends(get_db)
):
    """获取通知配置详情"""
    config = db.query(NotificationConfig).filter(NotificationConfig.id == config_id).first()
    
    if not config:
        raise HTTPException(status_code=404, detail="通知配置不存在")
    
    return config


@router.post("", response_model=NotificationConfigResponse)
def create_notification_config(
    config: NotificationConfigCreate,
    db: Session = Depends(get_db)
):
    """创建通知配置"""
    # 检查是否已存在相同类型的配置
    existing = db.query(NotificationConfig).filter(
        NotificationConfig.tenant_id == config.tenant_id,
        NotificationConfig.notification_type == config.notification_type
    ).first()
    
    if existing:
        raise HTTPException(status_code=400, detail="该类型的通知配置已存在")
    
    db_config = NotificationConfig(**config.dict())
    db.add(db_config)
    db.commit()
    db.refresh(db_config)
    
    return db_config


@router.put("/{config_id}", response_model=NotificationConfigResponse)
def update_notification_config(
    config_id: int,
    config_update: NotificationConfigUpdate,
    db: Session = Depends(get_db)
):
    """更新通知配置"""
    db_config = db.query(NotificationConfig).filter(NotificationConfig.id == config_id).first()
    
    if not db_config:
        raise HTTPException(status_code=404, detail="通知配置不存在")
    
    update_data = config_update.dict(exclude_unset=True)
    for field, value in update_data.items():
        setattr(db_config, field, value)
    
    db.commit()
    db.refresh(db_config)
    
    return db_config


@router.delete("/{config_id}")
def delete_notification_config(
    config_id: int,
    db: Session = Depends(get_db)
):
    """删除通知配置"""
    config = db.query(NotificationConfig).filter(NotificationConfig.id == config_id).first()
    
    if not config:
        raise HTTPException(status_code=404, detail="通知配置不存在")
    
    db.delete(config)
    db.commit()
    
    return {"message": "删除成功"}

