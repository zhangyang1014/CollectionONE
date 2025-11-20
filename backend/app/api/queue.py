# -*- coding: utf-8 -*-
"""案件队列管理 API"""
from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.orm import Session
from sqlalchemy import func
from typing import List, Optional
from pydantic import BaseModel

from app.core.database import get_db
from app.core.response import success_response
from app.models.case_queue import CaseQueue
from app.models.case import Case

router = APIRouter(prefix="/queues", tags=["案件队列管理"])


class QueueCreate(BaseModel):
    """队列创建模型"""
    tenant_id: int
    queue_code: str
    queue_name: str
    queue_name_en: Optional[str] = None
    queue_description: Optional[str] = None
    overdue_days_start: int
    overdue_days_end: Optional[int] = None
    sort_order: int = 0
    is_active: bool = True


class QueueUpdate(BaseModel):
    """队列更新模型"""
    queue_name: Optional[str] = None
    queue_name_en: Optional[str] = None
    queue_description: Optional[str] = None
    overdue_days_start: Optional[int] = None
    overdue_days_end: Optional[int] = None
    sort_order: Optional[int] = None
    is_active: Optional[bool] = None


@router.get("")
async def list_queues(
    tenant_id: Optional[int] = Query(None, description="甲方ID"),
    is_active: Optional[bool] = Query(None, description="是否启用"),
    skip: int = Query(0, ge=0),
    limit: int = Query(100, ge=1, le=1000),
    db: Session = Depends(get_db)
):
    """获取案件队列列表"""
    query = db.query(CaseQueue)
    
    if tenant_id is not None:
        query = query.filter(CaseQueue.tenant_id == tenant_id)
    
    if is_active is not None:
        query = query.filter(CaseQueue.is_active == is_active)
    
    queues = query.order_by(CaseQueue.sort_order).offset(skip).limit(limit).all()
    
    # 添加统计数据
    result = []
    for queue in queues:
        queue_dict = {
            "id": queue.id,
            "tenant_id": queue.tenant_id,
            "queue_code": queue.queue_code,
            "queue_name": queue.queue_name,
            "queue_name_en": queue.queue_name_en,
            "queue_description": queue.queue_description,
            "overdue_days_start": queue.overdue_days_start,
            "overdue_days_end": queue.overdue_days_end,
            "sort_order": queue.sort_order,
            "is_active": queue.is_active,
            "case_count": db.query(func.count(Case.id)).filter(
                Case.queue_id == queue.id
            ).scalar() or 0,
            "created_at": queue.created_at.isoformat() if queue.created_at else None,
            "updated_at": queue.updated_at.isoformat() if queue.updated_at else None
        }
        result.append(queue_dict)
    
    return success_response(data=result)


@router.post("")
async def create_queue(
    queue: QueueCreate,
    db: Session = Depends(get_db)
):
    """创建案件队列"""
    # 检查队列编码是否已存在
    existing = db.query(CaseQueue).filter(
        CaseQueue.tenant_id == queue.tenant_id,
        CaseQueue.queue_code == queue.queue_code
    ).first()
    
    if existing:
        raise HTTPException(status_code=400, detail="队列编码已存在")
    
    db_queue = CaseQueue(**queue.dict())
    db.add(db_queue)
    db.commit()
    db.refresh(db_queue)
    
    queue_dict = {
        "id": db_queue.id,
        "tenant_id": db_queue.tenant_id,
        "queue_code": db_queue.queue_code,
        "queue_name": db_queue.queue_name,
        "queue_name_en": db_queue.queue_name_en,
        "queue_description": db_queue.queue_description,
        "overdue_days_start": db_queue.overdue_days_start,
        "overdue_days_end": db_queue.overdue_days_end,
        "sort_order": db_queue.sort_order,
        "is_active": db_queue.is_active,
        "created_at": db_queue.created_at.isoformat() if db_queue.created_at else None,
        "updated_at": db_queue.updated_at.isoformat() if db_queue.updated_at else None
    }
    
    return success_response(data=queue_dict, message="创建成功")


@router.get("/{queue_id}")
async def get_queue(
    queue_id: int,
    db: Session = Depends(get_db)
):
    """获取队列详情"""
    queue = db.query(CaseQueue).filter(CaseQueue.id == queue_id).first()
    
    if not queue:
        raise HTTPException(status_code=404, detail="队列不存在")
    
    queue_dict = {
        "id": queue.id,
        "tenant_id": queue.tenant_id,
        "queue_code": queue.queue_code,
        "queue_name": queue.queue_name,
        "queue_name_en": queue.queue_name_en,
        "queue_description": queue.queue_description,
        "overdue_days_start": queue.overdue_days_start,
        "overdue_days_end": queue.overdue_days_end,
        "sort_order": queue.sort_order,
        "is_active": queue.is_active,
        "case_count": db.query(func.count(Case.id)).filter(
            Case.queue_id == queue.id
        ).scalar() or 0,
        "created_at": queue.created_at.isoformat() if queue.created_at else None,
        "updated_at": queue.updated_at.isoformat() if queue.updated_at else None
    }
    
    return success_response(data=queue_dict)


@router.put("/{queue_id}")
async def update_queue(
    queue_id: int,
    queue: QueueUpdate,
    db: Session = Depends(get_db)
):
    """更新队列"""
    db_queue = db.query(CaseQueue).filter(CaseQueue.id == queue_id).first()
    
    if not db_queue:
        raise HTTPException(status_code=404, detail="队列不存在")
    
    update_data = queue.dict(exclude_unset=True)
    for field, value in update_data.items():
        setattr(db_queue, field, value)
    
    db.commit()
    db.refresh(db_queue)
    
    queue_dict = {
        "id": db_queue.id,
        "tenant_id": db_queue.tenant_id,
        "queue_code": db_queue.queue_code,
        "queue_name": db_queue.queue_name,
        "queue_name_en": db_queue.queue_name_en,
        "queue_description": db_queue.queue_description,
        "overdue_days_start": db_queue.overdue_days_start,
        "overdue_days_end": db_queue.overdue_days_end,
        "sort_order": db_queue.sort_order,
        "is_active": db_queue.is_active,
        "created_at": db_queue.created_at.isoformat() if db_queue.created_at else None,
        "updated_at": db_queue.updated_at.isoformat() if db_queue.updated_at else None
    }
    
    return success_response(data=queue_dict, message="更新成功")


@router.delete("/{queue_id}")
async def delete_queue(
    queue_id: int,
    db: Session = Depends(get_db)
):
    """删除队列（软删除）"""
    db_queue = db.query(CaseQueue).filter(CaseQueue.id == queue_id).first()
    
    if not db_queue:
        raise HTTPException(status_code=404, detail="队列不存在")
    
    db_queue.is_active = False
    db.commit()
    
    return success_response(message="删除成功")

