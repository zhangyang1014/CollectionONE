from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from typing import List
from app.core.database import get_db
from app.models.field_group import FieldGroup
from app.schemas.field_group import FieldGroupCreate, FieldGroupUpdate, FieldGroupResponse

router = APIRouter(prefix="/field-groups", tags=["字段分组"])


@router.get("", response_model=List[FieldGroupResponse])
def get_field_groups(
    skip: int = 0,
    limit: int = 100,
    db: Session = Depends(get_db)
):
    """获取字段分组列表"""
    groups = db.query(FieldGroup).filter(FieldGroup.is_active == True).offset(skip).limit(limit).all()
    return groups


@router.get("/{group_id}", response_model=FieldGroupResponse)
def get_field_group(group_id: int, db: Session = Depends(get_db)):
    """获取单个字段分组"""
    group = db.query(FieldGroup).filter(FieldGroup.id == group_id).first()
    if not group:
        raise HTTPException(status_code=404, detail="字段分组不存在")
    return group


@router.post("", response_model=FieldGroupResponse)
def create_field_group(group: FieldGroupCreate, db: Session = Depends(get_db)):
    """创建字段分组"""
    # 检查 group_key 是否已存在
    existing = db.query(FieldGroup).filter(FieldGroup.group_key == group.group_key).first()
    if existing:
        raise HTTPException(status_code=400, detail="分组标识已存在")
    
    db_group = FieldGroup(**group.model_dump())
    db.add(db_group)
    db.commit()
    db.refresh(db_group)
    return db_group


@router.put("/{group_id}", response_model=FieldGroupResponse)
def update_field_group(
    group_id: int,
    group: FieldGroupUpdate,
    db: Session = Depends(get_db)
):
    """更新字段分组"""
    db_group = db.query(FieldGroup).filter(FieldGroup.id == group_id).first()
    if not db_group:
        raise HTTPException(status_code=404, detail="字段分组不存在")
    
    update_data = group.model_dump(exclude_unset=True)
    for field, value in update_data.items():
        setattr(db_group, field, value)
    
    db.commit()
    db.refresh(db_group)
    return db_group


@router.delete("/{group_id}")
def delete_field_group(group_id: int, db: Session = Depends(get_db)):
    """删除字段分组"""
    db_group = db.query(FieldGroup).filter(FieldGroup.id == group_id).first()
    if not db_group:
        raise HTTPException(status_code=404, detail="字段分组不存在")
    
    db_group.is_active = False
    db.commit()
    return {"message": "字段分组已删除"}

