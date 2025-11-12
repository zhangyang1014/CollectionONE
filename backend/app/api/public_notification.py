"""公共通知管理 API"""
from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.orm import Session
from typing import List, Optional
from app.core.database import get_db
from app.models.public_notification import PublicNotification
from app.schemas.notification import (
    PublicNotificationResponse,
    PublicNotificationCreate,
    PublicNotificationUpdate
)
import json

router = APIRouter(prefix="/public-notifications", tags=["公共通知管理"])


@router.get("", response_model=List[PublicNotificationResponse])
def list_public_notifications(
    tenant_id: Optional[int] = Query(None, description="甲方ID"),
    agency_id: Optional[int] = Query(None, description="机构ID"),
    is_enabled: Optional[bool] = Query(None, description="是否启用"),
    db: Session = Depends(get_db)
):
    """获取公共通知列表"""
    query = db.query(PublicNotification)
    
    if tenant_id is not None:
        query = query.filter(PublicNotification.tenant_id == tenant_id)
    
    if agency_id is not None:
        query = query.filter(PublicNotification.agency_id == agency_id)
    
    if is_enabled is not None:
        query = query.filter(PublicNotification.is_enabled == is_enabled)
    
    notifications = query.order_by(
        PublicNotification.sort_order.asc(),
        PublicNotification.created_at.desc()
    ).all()
    
    # 处理notify_roles JSON字段
    result = []
    for notification in notifications:
        notification_dict = {
            **{c.name: getattr(notification, c.name) for c in notification.__table__.columns},
            'notify_roles': json.loads(notification.notify_roles) if notification.notify_roles else []
        }
        result.append(PublicNotificationResponse(**notification_dict))
    
    return result


@router.get("/{notification_id}", response_model=PublicNotificationResponse)
def get_public_notification(
    notification_id: int,
    db: Session = Depends(get_db)
):
    """获取公共通知详情"""
    notification = db.query(PublicNotification).filter(
        PublicNotification.id == notification_id
    ).first()
    
    if not notification:
        raise HTTPException(status_code=404, detail="公共通知不存在")
    
    notification_dict = {
        **{c.name: getattr(notification, c.name) for c in notification.__table__.columns},
        'notify_roles': json.loads(notification.notify_roles) if notification.notify_roles else []
    }
    
    return PublicNotificationResponse(**notification_dict)


@router.post("", response_model=PublicNotificationResponse)
def create_public_notification(
    notification: PublicNotificationCreate,
    db: Session = Depends(get_db)
):
    """创建公共通知"""
    notification_data = notification.dict()
    notify_roles = notification_data.pop('notify_roles', [])
    
    db_notification = PublicNotification(
        **notification_data,
        notify_roles=json.dumps(notify_roles) if notify_roles else None
    )
    
    db.add(db_notification)
    db.commit()
    db.refresh(db_notification)
    
    notification_dict = {
        **{c.name: getattr(db_notification, c.name) for c in db_notification.__table__.columns},
        'notify_roles': json.loads(db_notification.notify_roles) if db_notification.notify_roles else []
    }
    
    return PublicNotificationResponse(**notification_dict)


@router.put("/{notification_id}", response_model=PublicNotificationResponse)
def update_public_notification(
    notification_id: int,
    notification_update: PublicNotificationUpdate,
    db: Session = Depends(get_db)
):
    """更新公共通知"""
    db_notification = db.query(PublicNotification).filter(
        PublicNotification.id == notification_id
    ).first()
    
    if not db_notification:
        raise HTTPException(status_code=404, detail="公共通知不存在")
    
    update_data = notification_update.dict(exclude_unset=True)
    
    # 处理notify_roles
    if 'notify_roles' in update_data:
        notify_roles = update_data.pop('notify_roles')
        update_data['notify_roles'] = json.dumps(notify_roles) if notify_roles else None
    
    for field, value in update_data.items():
        setattr(db_notification, field, value)
    
    db.commit()
    db.refresh(db_notification)
    
    notification_dict = {
        **{c.name: getattr(db_notification, c.name) for c in db_notification.__table__.columns},
        'notify_roles': json.loads(db_notification.notify_roles) if db_notification.notify_roles else []
    }
    
    return PublicNotificationResponse(**notification_dict)


@router.delete("/{notification_id}")
def delete_public_notification(
    notification_id: int,
    db: Session = Depends(get_db)
):
    """删除公共通知"""
    notification = db.query(PublicNotification).filter(
        PublicNotification.id == notification_id
    ).first()
    
    if not notification:
        raise HTTPException(status_code=404, detail="公共通知不存在")
    
    db.delete(notification)
    db.commit()
    
    return {"message": "删除成功"}


@router.put("/{notification_id}/sort")
def update_notification_sort(
    notification_id: int,
    sort_order: int = Query(..., description="排序顺序"),
    db: Session = Depends(get_db)
):
    """更新公共通知排序"""
    notification = db.query(PublicNotification).filter(
        PublicNotification.id == notification_id
    ).first()
    
    if not notification:
        raise HTTPException(status_code=404, detail="公共通知不存在")
    
    notification.sort_order = sort_order
    db.commit()
    
    return {"message": "排序更新成功"}

