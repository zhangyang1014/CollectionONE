from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from typing import List
from app.core.database import get_db
from app.models.field_dependency import FieldDependency
from app.schemas.field_dependency import (
    FieldDependencyCreate,
    FieldDependencyUpdate,
    FieldDependencyResponse
)

router = APIRouter(prefix="/field-dependencies", tags=["字段联动"])


@router.get("", response_model=List[FieldDependencyResponse])
def get_field_dependencies(
    skip: int = 0,
    limit: int = 100,
    db: Session = Depends(get_db)
):
    """获取字段联动规则列表"""
    dependencies = db.query(FieldDependency).offset(skip).limit(limit).all()
    return dependencies


@router.get("/by-source/{source_field_id}", response_model=List[FieldDependencyResponse])
def get_dependencies_by_source(source_field_id: int, db: Session = Depends(get_db)):
    """根据源字段获取联动规则"""
    dependencies = db.query(FieldDependency).filter(
        FieldDependency.source_field_id == source_field_id
    ).all()
    return dependencies


@router.get("/by-target/{target_field_id}", response_model=List[FieldDependencyResponse])
def get_dependencies_by_target(target_field_id: int, db: Session = Depends(get_db)):
    """根据目标字段获取联动规则"""
    dependencies = db.query(FieldDependency).filter(
        FieldDependency.target_field_id == target_field_id
    ).all()
    return dependencies


@router.get("/{dependency_id}", response_model=FieldDependencyResponse)
def get_field_dependency(dependency_id: int, db: Session = Depends(get_db)):
    """获取单个联动规则"""
    dependency = db.query(FieldDependency).filter(FieldDependency.id == dependency_id).first()
    if not dependency:
        raise HTTPException(status_code=404, detail="联动规则不存在")
    return dependency


@router.post("", response_model=FieldDependencyResponse)
def create_field_dependency(dependency: FieldDependencyCreate, db: Session = Depends(get_db)):
    """创建字段联动规则"""
    db_dependency = FieldDependency(**dependency.model_dump())
    db.add(db_dependency)
    db.commit()
    db.refresh(db_dependency)
    return db_dependency


@router.put("/{dependency_id}", response_model=FieldDependencyResponse)
def update_field_dependency(
    dependency_id: int,
    dependency: FieldDependencyUpdate,
    db: Session = Depends(get_db)
):
    """更新字段联动规则"""
    db_dependency = db.query(FieldDependency).filter(FieldDependency.id == dependency_id).first()
    if not db_dependency:
        raise HTTPException(status_code=404, detail="联动规则不存在")
    
    update_data = dependency.model_dump(exclude_unset=True)
    for field, value in update_data.items():
        setattr(db_dependency, field, value)
    
    db.commit()
    db.refresh(db_dependency)
    return db_dependency


@router.delete("/{dependency_id}")
def delete_field_dependency(dependency_id: int, db: Session = Depends(get_db)):
    """删除字段联动规则"""
    db_dependency = db.query(FieldDependency).filter(FieldDependency.id == dependency_id).first()
    if not db_dependency:
        raise HTTPException(status_code=404, detail="联动规则不存在")
    
    db.delete(db_dependency)
    db.commit()
    return {"message": "联动规则已删除"}

