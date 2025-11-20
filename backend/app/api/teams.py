"""催收小组管理 API"""
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from sqlalchemy import func
from typing import List

from app.core.database import get_db
from app.models.collection_team import CollectionTeam
from app.models.collector import Collector
from app.models.team_admin_account import TeamAdminAccount
from app.models.team_group import TeamGroup
from app.models.case_queue import CaseQueue
from app.schemas.organization import (
    CollectionTeam as CollectionTeamSchema,
    CollectionTeamCreate,
    CollectionTeamUpdate
)

router = APIRouter(prefix="/teams", tags=["催收小组管理"])


@router.get("/{team_id}/collectors")
def get_team_collectors(
    team_id: int,
    db: Session = Depends(get_db)
):
    """获取小组下的催员列表"""
    # 验证小组是否存在
    team = db.query(CollectionTeam).filter(CollectionTeam.id == team_id).first()
    if not team:
        raise HTTPException(status_code=404, detail="催收小组不存在")
    
    collectors = db.query(Collector).filter(
        Collector.team_id == team_id,
        Collector.is_active.is_(True)
    ).order_by(Collector.collector_code).all()
    
    return collectors


@router.get("/{team_id}/admin-accounts")
def get_team_admin_accounts(
    team_id: int,
    db: Session = Depends(get_db)
):
    """获取小组下的管理员账号列表"""
    # 验证小组是否存在
    team = db.query(CollectionTeam).filter(CollectionTeam.id == team_id).first()
    if not team:
        raise HTTPException(status_code=404, detail="催收小组不存在")
    
    # 查询该小组的管理员账号
    accounts = db.query(TeamAdminAccount).filter(
        TeamAdminAccount.team_id == team_id,
        TeamAdminAccount.is_active.is_(True)
    ).order_by(TeamAdminAccount.account_code).all()
    
    # 转换为字典格式，添加机构和小组名称
    result = []
    for account in accounts:
        account_dict = {
            "id": account.id,
            "account_code": account.account_code,
            "account_name": account.account_name,
            "login_id": account.login_id,
            "tenant_id": account.tenant_id,
            "tenant_name": account.tenant.tenant_name if account.tenant else "",
            "agency_id": account.agency_id,
            "agency_name": account.agency.agency_name if account.agency else "",
            "team_id": account.team_id,
            "team_name": account.team.team_name if account.team else "",
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


@router.post("", response_model=CollectionTeamSchema)
async def create_team(
    team: CollectionTeamCreate,
    db: Session = Depends(get_db)
):
    """创建催收小组"""
    # 检查小组编码是否已存在
    existing_team = db.query(CollectionTeam).filter(
        CollectionTeam.team_code == team.team_code
    ).first()
    
    if existing_team:
        raise HTTPException(status_code=400, detail="小组编码已存在")
    
    # 创建小组
    db_team = CollectionTeam(**team.dict())
    db.add(db_team)
    db.commit()
    db.refresh(db_team)
    
    # 构造返回数据
    return _build_team_response(db_team, db)


@router.put("/{team_id}", response_model=CollectionTeamSchema)
async def update_team(
    team_id: int,
    team_update: CollectionTeamUpdate,
    db: Session = Depends(get_db)
):
    """更新催收小组"""
    # 查找小组
    db_team = db.query(CollectionTeam).filter(CollectionTeam.id == team_id).first()
    if not db_team:
        raise HTTPException(status_code=404, detail="催收小组不存在")
    
    # 更新字段
    update_data = team_update.dict(exclude_unset=True)
    for field, value in update_data.items():
        setattr(db_team, field, value)
    
    db.commit()
    db.refresh(db_team)
    
    # 构造返回数据
    return _build_team_response(db_team, db)


@router.get("/{team_id}", response_model=CollectionTeamSchema)
async def get_team(
    team_id: int,
    db: Session = Depends(get_db)
):
    """获取单个催收小组详情"""
    db_team = db.query(CollectionTeam).filter(CollectionTeam.id == team_id).first()
    if not db_team:
        raise HTTPException(status_code=404, detail="催收小组不存在")
    
    return _build_team_response(db_team, db)


def _build_team_response(team: CollectionTeam, db: Session) -> dict:
    """构造小组响应数据"""
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
    
    return {
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
        "case_count": 0,
        "created_at": team.created_at,
        "updated_at": team.updated_at
    }

