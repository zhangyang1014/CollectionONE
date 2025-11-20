from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.orm import Session
from typing import List, Optional
from datetime import datetime
from app.core.database import get_db
from app.models.standard_field import StandardField
from app.schemas.standard_field import StandardFieldCreate, StandardFieldUpdate, StandardFieldResponse

router = APIRouter(prefix="/standard-fields", tags=["标准字段"])


@router.get("", response_model=List[StandardFieldResponse])
def get_standard_fields(
    skip: int = 0,
    limit: int = 100,
    field_group_id: Optional[int] = Query(None, description="字段分组ID"),
    include_deleted: bool = Query(False, description="是否包含已删除字段"),
    db: Session = Depends(get_db)
):
    """获取标准字段列表"""
    query = db.query(StandardField)
    
    if not include_deleted:
        query = query.filter(StandardField.is_deleted == False)
    
    if field_group_id:
        query = query.filter(StandardField.field_group_id == field_group_id)
    
    fields = query.order_by(StandardField.sort_order).offset(skip).limit(limit).all()
    return fields


@router.get("/{field_id}", response_model=StandardFieldResponse)
def get_standard_field(field_id: int, db: Session = Depends(get_db)):
    """获取单个标准字段"""
    field = db.query(StandardField).filter(StandardField.id == field_id).first()
    if not field:
        raise HTTPException(status_code=404, detail="标准字段不存在")
    return field


@router.post("", response_model=StandardFieldResponse)
def create_standard_field(field: StandardFieldCreate, db: Session = Depends(get_db)):
    """创建标准字段"""
    # 检查 field_key 是否已存在
    existing = db.query(StandardField).filter(StandardField.field_key == field.field_key).first()
    if existing:
        raise HTTPException(status_code=400, detail="字段标识已存在")
    
    # 在创建新字段之前，调整同一分组内排序值>=新字段排序值的字段
    # 将它们都+1，为新字段让出位置
    if field.sort_order is not None and field.sort_order > 0:
        # 查询同一分组内，未删除的，且排序值>=新字段排序值的字段
        fields_to_update = db.query(StandardField).filter(
            StandardField.field_group_id == field.field_group_id,
            StandardField.is_deleted == False,
            StandardField.sort_order >= field.sort_order
        ).all()
        
        # 将这些字段的排序值都+1
        for existing_field in fields_to_update:
            existing_field.sort_order += 1
    
    db_field = StandardField(**field.model_dump())
    db.add(db_field)
    db.commit()
    db.refresh(db_field)
    return db_field


@router.put("/{field_id}", response_model=StandardFieldResponse)
def update_standard_field(
    field_id: int,
    field: StandardFieldUpdate,
    db: Session = Depends(get_db)
):
    """更新标准字段"""
    db_field = db.query(StandardField).filter(StandardField.id == field_id).first()
    if not db_field:
        raise HTTPException(status_code=404, detail="标准字段不存在")
    
    update_data = field.model_dump(exclude_unset=True)
    for field_name, value in update_data.items():
        setattr(db_field, field_name, value)
    
    db.commit()
    db.refresh(db_field)
    return db_field


@router.delete("/{field_id}")
def delete_standard_field(field_id: int, db: Session = Depends(get_db)):
    """软删除标准字段"""
    db_field = db.query(StandardField).filter(StandardField.id == field_id).first()
    if not db_field:
        raise HTTPException(status_code=404, detail="标准字段不存在")
    
    # 在删除字段之前，调整同一分组内排序值>被删除字段排序值的字段
    # 将它们都-1，填补删除后的空缺
    if db_field.sort_order is not None and db_field.sort_order > 0:
        # 查询同一分组内，未删除的，且排序值>被删除字段排序值的字段
        fields_to_update = db.query(StandardField).filter(
            StandardField.field_group_id == db_field.field_group_id,
            StandardField.is_deleted == False,
            StandardField.id != field_id,  # 排除当前要删除的字段
            StandardField.sort_order > db_field.sort_order
        ).all()
        
        # 将这些字段的排序值都-1
        for existing_field in fields_to_update:
            existing_field.sort_order -= 1
    
    db_field.is_deleted = True
    db_field.deleted_at = datetime.now()
    db.commit()
    return {"message": "标准字段已删除"}

