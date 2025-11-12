"""催收小组管理 API"""
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from typing import List

from app.core.database import get_db
from app.models.collection_team import CollectionTeam
from app.models.collector import Collector
from app.models.team_admin_account import TeamAdminAccount

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

