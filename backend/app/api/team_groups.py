"""小组群管理 API"""
from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.orm import Session
from sqlalchemy import func
from typing import List, Optional

from app.core.database import get_db
from app.core.response import success_response
from app.models import TeamGroup, CollectionTeam, Collector, TeamAdminAccount
from app.schemas.organization import (
    TeamGroup as TeamGroupSchema,
    TeamGroupCreate,
    TeamGroupUpdate,
)
from app.core.security import get_password_hash

router = APIRouter(prefix="/team-groups", tags=["小组群管理"])


@router.get("", response_model=List[TeamGroupSchema])
async def list_team_groups(
    tenant_id: Optional[int] = Query(None, description="甲方ID"),
    agency_id: Optional[int] = Query(None, description="机构ID"),
    is_active: Optional[bool] = Query(None, description="是否启用"),
    skip: int = Query(0, ge=0),
    limit: int = Query(100, ge=1, le=1000),
    db: Session = Depends(get_db)
):
    """获取小组群列表"""
    query = db.query(TeamGroup)
    
    if tenant_id is not None:
        query = query.filter(TeamGroup.tenant_id == tenant_id)
    
    if agency_id is not None:
        query = query.filter(TeamGroup.agency_id == agency_id)
    
    if is_active is not None:
        query = query.filter(TeamGroup.is_active == is_active)
    
    team_groups = query.order_by(TeamGroup.sort_order).offset(skip).limit(limit).all()
    
    # 添加统计数据
    result = []
    for group in team_groups:
        # 获取机构名称
        agency_name = None
        if group.agency:
            agency_name = group.agency.agency_name
        
        # 获取SPV管理员账号信息
        spv_account_name = None
        spv_login_id = None
        spv_accounts = db.query(TeamAdminAccount).filter(
            TeamAdminAccount.team_group_id == group.id,
            TeamAdminAccount.role == 'spv'
        ).first()
        if spv_accounts:
            spv_account_name = spv_accounts.account_name
            spv_login_id = spv_accounts.login_id
        
        # 统计小组数量
        team_count = db.query(func.count(CollectionTeam.id)).filter(
            CollectionTeam.team_group_id == group.id
        ).scalar() or 0
        
        # 统计催员数量
        collector_count = db.query(func.count(Collector.id)).join(
            CollectionTeam, Collector.team_id == CollectionTeam.id
        ).filter(CollectionTeam.team_group_id == group.id).scalar() or 0
        
        group_dict = {
            **group.__dict__,
            "agency_name": agency_name,
            "spv_account_name": spv_account_name,
            "spv_login_id": spv_login_id,
            "team_count": team_count,
            "collector_count": collector_count,
        }
        result.append(group_dict)
    
    return result


@router.post("")
async def create_team_group(
    team_group: TeamGroupCreate,
    db: Session = Depends(get_db)
):
    """创建小组群并同时创建SPV管理员账号"""
    # 检查小组群编码是否已存在
    existing = db.query(TeamGroup).filter(
        TeamGroup.tenant_id == team_group.tenant_id,
        TeamGroup.agency_id == team_group.agency_id,
        TeamGroup.group_code == team_group.group_code
    ).first()
    
    if existing:
        raise HTTPException(status_code=400, detail="小组群编码已存在")
    
    # 检查SPV登录ID是否已存在
    existing_spv = db.query(TeamAdminAccount).filter(
        TeamAdminAccount.login_id == team_group.spv_login_id
    ).first()
    
    if existing_spv:
        raise HTTPException(status_code=400, detail="SPV登录ID已存在")
    
    # 创建小组群
    db_team_group = TeamGroup(
        tenant_id=team_group.tenant_id,
        agency_id=team_group.agency_id,
        group_code=team_group.group_code,
        group_name=team_group.group_name,
        group_name_en=team_group.group_name_en,
        description=team_group.description,
        sort_order=team_group.sort_order,
        is_active=team_group.is_active
    )
    db.add(db_team_group)
    db.flush()  # 先flush获取ID，但不提交
    
    # 自动生成SPV账号编码（使用login_id作为账号编码）
    spv_account_code = f"SPV_{team_group.spv_login_id}"
    
    # 创建SPV管理员账号
    spv_account = TeamAdminAccount(
        tenant_id=team_group.tenant_id,
        agency_id=team_group.agency_id,
        team_group_id=db_team_group.id,
        account_code=spv_account_code,
        account_name=team_group.spv_account_name,
        login_id=team_group.spv_login_id,
        password_hash=get_password_hash(team_group.spv_password),
        role='spv',
        mobile=team_group.spv_mobile,
        email=team_group.spv_email,
        remark=team_group.spv_remark,
        is_active=True
    )
    db.add(spv_account)
    db.commit()
    db.refresh(db_team_group)
    
    # 获取机构名称
    agency_name = None
    if db_team_group.agency:
        agency_name = db_team_group.agency.agency_name
    
    group_dict = {
        "id": db_team_group.id,
        "tenant_id": db_team_group.tenant_id,
        "agency_id": db_team_group.agency_id,
        "group_code": db_team_group.group_code,
        "group_name": db_team_group.group_name,
        "group_name_en": db_team_group.group_name_en,
        "description": db_team_group.description,
        "sort_order": db_team_group.sort_order,
        "is_active": db_team_group.is_active,
        "agency_name": agency_name,
        "spv_account_name": team_group.spv_account_name,
        "spv_login_id": team_group.spv_login_id,
        "team_count": 0,
        "collector_count": 0,
        "created_at": db_team_group.created_at.isoformat() if db_team_group.created_at else None,
        "updated_at": db_team_group.updated_at.isoformat() if db_team_group.updated_at else None
    }
    
    return success_response(data=group_dict, message="创建成功")


@router.get("/{team_group_id}", response_model=TeamGroupSchema)
async def get_team_group(
    team_group_id: int,
    db: Session = Depends(get_db)
):
    """获取小组群详情"""
    team_group = db.query(TeamGroup).filter(TeamGroup.id == team_group_id).first()
    
    if not team_group:
        raise HTTPException(status_code=404, detail="小组群不存在")
    
    # 获取机构名称
    agency_name = None
    if team_group.agency:
        agency_name = team_group.agency.agency_name
    
    # 获取SPV管理员账号信息
    spv_account_name = None
    spv_login_id = None
    spv_email = None
    spv_mobile = None
    spv_accounts = db.query(TeamAdminAccount).filter(
        TeamAdminAccount.team_group_id == team_group.id,
        TeamAdminAccount.role == 'spv'
    ).first()
    if spv_accounts:
        spv_account_name = spv_accounts.account_name
        spv_login_id = spv_accounts.login_id
        spv_email = spv_accounts.email
        spv_mobile = spv_accounts.mobile
    
    # 统计小组数量
    team_count = db.query(func.count(CollectionTeam.id)).filter(
        CollectionTeam.team_group_id == team_group.id
    ).scalar() or 0
    
    # 统计催员数量
    collector_count = db.query(func.count(Collector.id)).join(
        CollectionTeam, Collector.team_id == CollectionTeam.id
    ).filter(CollectionTeam.team_group_id == team_group.id).scalar() or 0
    
    return {
        **team_group.__dict__,
        "agency_name": agency_name,
        "spv_account_name": spv_account_name,
        "spv_login_id": spv_login_id,
        "spv_email": spv_email,
        "spv_mobile": spv_mobile,
        "team_count": team_count,
        "collector_count": collector_count,
    }


@router.put("/{team_group_id}", response_model=TeamGroupSchema)
async def update_team_group(
    team_group_id: int,
    team_group_update: TeamGroupUpdate,
    db: Session = Depends(get_db)
):
    """更新小组群"""
    db_team_group = db.query(TeamGroup).filter(TeamGroup.id == team_group_id).first()
    
    if not db_team_group:
        raise HTTPException(status_code=404, detail="小组群不存在")
    
    update_data = team_group_update.dict(exclude_unset=True)
    for field, value in update_data.items():
        setattr(db_team_group, field, value)
    
    db.commit()
    db.refresh(db_team_group)
    
    # 获取机构名称
    agency_name = None
    if db_team_group.agency:
        agency_name = db_team_group.agency.agency_name
    
    # 获取SPV管理员账号信息
    spv_account_name = None
    spv_login_id = None
    spv_accounts = db.query(TeamAdminAccount).filter(
        TeamAdminAccount.team_group_id == db_team_group.id,
        TeamAdminAccount.role == 'spv'
    ).first()
    if spv_accounts:
        spv_account_name = spv_accounts.account_name
        spv_login_id = spv_accounts.login_id
    
    # 统计小组数量
    team_count = db.query(func.count(CollectionTeam.id)).filter(
        CollectionTeam.team_group_id == db_team_group.id
    ).scalar() or 0
    
    # 统计催员数量
    collector_count = db.query(func.count(Collector.id)).join(
        CollectionTeam, Collector.team_id == CollectionTeam.id
    ).filter(CollectionTeam.team_group_id == db_team_group.id).scalar() or 0
    
    return {
        **db_team_group.__dict__,
        "agency_name": agency_name,
        "spv_account_name": spv_account_name,
        "spv_login_id": spv_login_id,
        "team_count": team_count,
        "collector_count": collector_count
    }


@router.delete("/{team_group_id}")
async def delete_team_group(
    team_group_id: int,
    db: Session = Depends(get_db)
):
    """删除小组群"""
    team_group = db.query(TeamGroup).filter(TeamGroup.id == team_group_id).first()
    
    if not team_group:
        raise HTTPException(status_code=404, detail="小组群不存在")
    
    # 检查是否有关联的小组
    team_count = db.query(func.count(CollectionTeam.id)).filter(
        CollectionTeam.team_group_id == team_group_id
    ).scalar()
    
    if team_count > 0:
        raise HTTPException(status_code=400, detail="该小组群下还有小组，无法删除")
    
    db.delete(team_group)
    db.commit()
    
    return {"message": "删除成功"}


@router.get("/{team_group_id}/teams")
async def get_team_group_teams(
    team_group_id: int,
    db: Session = Depends(get_db)
):
    """获取小组群下的所有小组"""
    team_group = db.query(TeamGroup).filter(TeamGroup.id == team_group_id).first()
    
    if not team_group:
        raise HTTPException(status_code=404, detail="小组群不存在")
    
    teams = db.query(CollectionTeam).filter(
        CollectionTeam.team_group_id == team_group_id
    ).all()
    
    result = []
    for team in teams:
        team_dict = {
            **team.__dict__,
            "agency_name": team.agency.agency_name if team.agency else None,
            "team_group_name": team_group.group_name,
            "team_leader_name": team.team_leader.collector_name if team.team_leader else None,
            "collector_count": db.query(func.count(Collector.id)).filter(
                Collector.team_id == team.id
            ).scalar() or 0,
        }
        result.append(team_dict)
    
    return result

