"""催收机构管理 API"""
from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.orm import Session
from sqlalchemy import func
from typing import List, Optional

from app.core.database import get_db
from app.models import CollectionAgency, CollectionTeam, Collector, Case
from app.schemas.organization import (
    CollectionAgency as CollectionAgencySchema,
    CollectionAgencyCreate,
    CollectionAgencyUpdate,
)

router = APIRouter(prefix="/agencies", tags=["催收机构管理"])


@router.get("", response_model=List[CollectionAgencySchema])
async def list_agencies(
    tenant_id: int = Query(..., description="甲方ID"),
    is_active: Optional[bool] = Query(None, description="是否启用"),
    skip: int = Query(0, ge=0),
    limit: int = Query(100, ge=1, le=1000),
    db: Session = Depends(get_db)
):
    """获取催收机构列表"""
    query = db.query(CollectionAgency).filter(CollectionAgency.tenant_id == tenant_id)
    
    if is_active is not None:
        query = query.filter(CollectionAgency.is_active == is_active)
    
    agencies = query.order_by(CollectionAgency.sort_order).offset(skip).limit(limit).all()
    
    # 添加统计数据
    result = []
    for agency in agencies:
        agency_dict = {
            **agency.__dict__,
            "team_count": db.query(func.count(CollectionTeam.id)).filter(
                CollectionTeam.agency_id == agency.id
            ).scalar() or 0,
            "collector_count": db.query(func.count(Collector.id)).join(
                CollectionTeam, Collector.team_id == CollectionTeam.id
            ).filter(CollectionTeam.agency_id == agency.id).scalar() or 0,
            "case_count": db.query(func.count(Case.id)).filter(
                Case.agency_id == agency.id
            ).scalar() or 0,
        }
        result.append(agency_dict)
    
    return result


@router.post("", response_model=CollectionAgencySchema)
async def create_agency(
    agency: CollectionAgencyCreate,
    db: Session = Depends(get_db)
):
    """创建催收机构"""
    # 检查机构编码是否已存在
    existing = db.query(CollectionAgency).filter(
        CollectionAgency.tenant_id == agency.tenant_id,
        CollectionAgency.agency_code == agency.agency_code
    ).first()
    
    if existing:
        raise HTTPException(status_code=400, detail="机构编码已存在")
    
    db_agency = CollectionAgency(**agency.dict())
    db.add(db_agency)
    db.commit()
    db.refresh(db_agency)
    
    return {**db_agency.__dict__, "team_count": 0, "collector_count": 0, "case_count": 0}


@router.get("/{agency_id}", response_model=CollectionAgencySchema)
async def get_agency(
    agency_id: int,
    db: Session = Depends(get_db)
):
    """获取催收机构详情"""
    agency = db.query(CollectionAgency).filter(CollectionAgency.id == agency_id).first()
    
    if not agency:
        raise HTTPException(status_code=404, detail="催收机构不存在")
    
    return {
        **agency.__dict__,
        "team_count": db.query(func.count(CollectionTeam.id)).filter(
            CollectionTeam.agency_id == agency.id
        ).scalar() or 0,
        "collector_count": db.query(func.count(Collector.id)).join(
            CollectionTeam, Collector.team_id == CollectionTeam.id
        ).filter(CollectionTeam.agency_id == agency.id).scalar() or 0,
        "case_count": db.query(func.count(Case.id)).filter(
            Case.agency_id == agency.id
        ).scalar() or 0,
    }


@router.put("/{agency_id}", response_model=CollectionAgencySchema)
async def update_agency(
    agency_id: int,
    agency_update: CollectionAgencyUpdate,
    db: Session = Depends(get_db)
):
    """更新催收机构"""
    db_agency = db.query(CollectionAgency).filter(CollectionAgency.id == agency_id).first()
    
    if not db_agency:
        raise HTTPException(status_code=404, detail="催收机构不存在")
    
    update_data = agency_update.dict(exclude_unset=True)
    for field, value in update_data.items():
        setattr(db_agency, field, value)
    
    db.commit()
    db.refresh(db_agency)
    
    return {**db_agency.__dict__, "team_count": 0, "collector_count": 0, "case_count": 0}


@router.delete("/{agency_id}")
async def delete_agency(
    agency_id: int,
    db: Session = Depends(get_db)
):
    """删除催收机构"""
    agency = db.query(CollectionAgency).filter(CollectionAgency.id == agency_id).first()
    
    if not agency:
        raise HTTPException(status_code=404, detail="催收机构不存在")
    
    # 检查是否有关联的小组
    team_count = db.query(func.count(CollectionTeam.id)).filter(
        CollectionTeam.agency_id == agency_id
    ).scalar()
    
    if team_count > 0:
        raise HTTPException(status_code=400, detail="该机构下还有小组，无法删除")
    
    db.delete(agency)
    db.commit()
    
    return {"message": "删除成功"}


# 机构下的小组和催员相关接口
@router.get("/{agency_id}/teams")
async def get_agency_teams(
    agency_id: int,
    db: Session = Depends(get_db)
):
    """获取机构下的小组列表"""
    # 验证机构是否存在
    agency = db.query(CollectionAgency).filter(CollectionAgency.id == agency_id).first()
    if not agency:
        raise HTTPException(status_code=404, detail="催收机构不存在")
    
    teams = db.query(CollectionTeam).filter(
        CollectionTeam.agency_id == agency_id,
        CollectionTeam.is_active.is_(True)
    ).order_by(CollectionTeam.sort_order).all()
    
    return teams

