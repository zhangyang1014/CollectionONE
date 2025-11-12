from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.orm import Session
from typing import List, Optional
from datetime import datetime
from app.core.database import get_db
from app.models.custom_field import CustomField
from app.schemas.custom_field import CustomFieldCreate, CustomFieldUpdate, CustomFieldResponse

router = APIRouter(prefix="/custom-fields", tags=["自定义字段"])


@router.get("", response_model=List[CustomFieldResponse])
def get_custom_fields(
    tenant_id: int = Query(..., description="甲方ID"),
    skip: int = 0,
    limit: int = 100,
    field_group_id: Optional[int] = Query(None, description="字段分组ID"),
    include_deleted: bool = Query(False, description="是否包含已删除字段"),
    db: Session = Depends(get_db)
):
    """获取自定义字段列表"""
    query = db.query(CustomField).filter(CustomField.tenant_id == tenant_id)
    
    if not include_deleted:
        query = query.filter(CustomField.is_deleted == False)
    
    if field_group_id:
        query = query.filter(CustomField.field_group_id == field_group_id)
    
    fields = query.order_by(CustomField.sort_order).offset(skip).limit(limit).all()
    return fields


@router.get("/{field_id}", response_model=CustomFieldResponse)
def get_custom_field(field_id: int, db: Session = Depends(get_db)):
    """获取单个自定义字段"""
    field = db.query(CustomField).filter(CustomField.id == field_id).first()
    if not field:
        raise HTTPException(status_code=404, detail="自定义字段不存在")
    return field


@router.post("", response_model=CustomFieldResponse)
def create_custom_field(field: CustomFieldCreate, db: Session = Depends(get_db)):
    """创建自定义字段"""
    # 检查同一甲方下 field_key 是否已存在
    existing = db.query(CustomField).filter(
        CustomField.tenant_id == field.tenant_id,
        CustomField.field_key == field.field_key
    ).first()
    if existing:
        raise HTTPException(status_code=400, detail="字段标识已存在")
    
    db_field = CustomField(**field.model_dump())
    db.add(db_field)
    db.commit()
    db.refresh(db_field)
    return db_field


@router.put("/{field_id}", response_model=CustomFieldResponse)
def update_custom_field(
    field_id: int,
    field: CustomFieldUpdate,
    db: Session = Depends(get_db)
):
    """更新自定义字段"""
    db_field = db.query(CustomField).filter(CustomField.id == field_id).first()
    if not db_field:
        raise HTTPException(status_code=404, detail="自定义字段不存在")
    
    update_data = field.model_dump(exclude_unset=True)
    for field_name, value in update_data.items():
        setattr(db_field, field_name, value)
    
    db.commit()
    db.refresh(db_field)
    return db_field


@router.delete("/{field_id}")
def delete_custom_field(field_id: int, db: Session = Depends(get_db)):
    """软删除自定义字段"""
    db_field = db.query(CustomField).filter(CustomField.id == field_id).first()
    if not db_field:
        raise HTTPException(status_code=404, detail="自定义字段不存在")
    
    db_field.is_deleted = True
    db_field.deleted_at = datetime.now()
    db.commit()
    return {"message": "自定义字段已删除"}

