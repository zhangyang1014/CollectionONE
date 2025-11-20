"""催收机构管理 API"""
from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.orm import Session
from sqlalchemy import func
from typing import List, Optional

from app.core.database import get_db
from app.core.response import success_response
from app.models import CollectionAgency, CollectionTeam, Collector, Case
from app.models.team_admin_account import TeamAdminAccount
from app.models.team_group import TeamGroup
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


@router.post("")
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
    
    agency_dict = {
        "id": db_agency.id,
        "tenant_id": db_agency.tenant_id,
        "agency_code": db_agency.agency_code,
        "agency_name": db_agency.agency_name,
        "agency_name_en": db_agency.agency_name_en,
        "contact_person": db_agency.contact_person,
        "contact_phone": db_agency.contact_phone,
        "contact_email": db_agency.contact_email,
        "address": db_agency.address,
        "timezone": db_agency.timezone,
        "agency_type": db_agency.agency_type,
        "is_active": db_agency.is_active,
        "team_count": 0,
        "collector_count": 0,
        "case_count": 0,
        "created_at": db_agency.created_at.isoformat() if db_agency.created_at else None,
        "updated_at": db_agency.updated_at.isoformat() if db_agency.updated_at else None
    }
    
    return success_response(data=agency_dict, message="创建成功")


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
        "team_group_count": db.query(func.count(TeamGroup.id)).filter(
            TeamGroup.agency_id == agency.id
        ).scalar() or 0,
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


# 机构管理员账号相关接口
@router.get("/{agency_id}/admin-accounts")
async def get_agency_admin_accounts(
    agency_id: int,
    db: Session = Depends(get_db)
):
    """获取机构的管理员账号列表"""
    # 验证机构是否存在
    agency = db.query(CollectionAgency).filter(CollectionAgency.id == agency_id).first()
    if not agency:
        raise HTTPException(status_code=404, detail="催收机构不存在")
    
    # 查询该机构的管理员账号（没有team_group_id和team_id的机构级别管理员）
    accounts = db.query(TeamAdminAccount).filter(
        TeamAdminAccount.agency_id == agency_id,
        TeamAdminAccount.team_group_id.is_(None),
        TeamAdminAccount.team_id.is_(None),
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


# 机构下的小组和催员相关接口
@router.get("/{agency_id}/teams")
async def get_agency_teams(
    agency_id: int,
    db: Session = Depends(get_db)
):
    """获取机构下的小组列表"""
    from app.models.team_group import TeamGroup
    from app.models.case_queue import CaseQueue
    from app.models.collector import Collector
    from sqlalchemy import func
    
    # 验证机构是否存在
    agency = db.query(CollectionAgency).filter(CollectionAgency.id == agency_id).first()
    if not agency:
        raise HTTPException(status_code=404, detail="催收机构不存在")
    
    teams = db.query(CollectionTeam).filter(
        CollectionTeam.agency_id == agency_id,
        CollectionTeam.is_active.is_(True)
    ).order_by(CollectionTeam.sort_order).all()
    
    # 构造返回数据，包含关联信息
    result = []
    for team in teams:
        # 获取甲方名称
        tenant_name = team.tenant.tenant_name if team.tenant else None
        
        # 获取机构名称
        agency_name = team.agency.agency_name if team.agency else None
        
        # 获取小组群名称
        team_group_name = None
        if team.team_group_id:
            team_group = db.query(TeamGroup).filter(TeamGroup.id == team.team_group_id).first()
            if team_group:
                team_group_name = team_group.group_name
        
        # 获取队列名称
        queue_name = None
        if team.queue_id:
            queue = db.query(CaseQueue).filter(CaseQueue.id == team.queue_id).first()
            if queue:
                queue_name = queue.queue_name
        
        # 获取催员数量
        collector_count = db.query(func.count(Collector.id)).filter(
            Collector.team_id == team.id
        ).scalar() or 0
        
        team_dict = {
            "id": team.id,
            "tenant_id": team.tenant_id,
            "tenant_name": tenant_name,
            "agency_id": team.agency_id,
            "agency_name": agency_name,
            "team_group_id": team.team_group_id,
            "team_group_name": team_group_name,
            "queue_id": team.queue_id,
            "queue_name": queue_name,
            "team_code": team.team_code,
            "team_name": team.team_name,
            "team_name_en": team.team_name_en,
            "team_leader_id": team.team_leader_id,
            "team_type": team.team_type,
            "description": team.description,
            "max_case_count": team.max_case_count,
            "sort_order": team.sort_order,
            "is_active": team.is_active,
            "collector_count": collector_count,
            "case_count": 0,  # TODO: 计算案件数量
            "created_at": team.created_at,
            "updated_at": team.updated_at
        }
        result.append(team_dict)
    
    return result

