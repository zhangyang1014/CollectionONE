"""IM端认证API"""
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from sqlalchemy import and_
from pydantic import BaseModel
from datetime import datetime
from app.core.database import get_db
from app.core.security import verify_password, create_access_token
from app.core.config import settings
from app.models.collector import Collector
from app.models.tenant import Tenant
from app.models.collection_team import CollectionTeam
from app.models.collection_agency import CollectionAgency
from datetime import timedelta

router = APIRouter(prefix="/im/auth", tags=["IM端认证"])


class ImLoginRequest(BaseModel):
    """IM端登录请求"""
    tenantId: str  # 机构ID（实际是租户ID）
    collectorId: str  # 催员ID（login_id）
    password: str


class ImLoginResponse(BaseModel):
    """IM端登录响应"""
    code: int = 200
    message: str = "登录成功"
    data: dict


@router.post("/login", response_model=ImLoginResponse)
def im_login(login_data: ImLoginRequest, db: Session = Depends(get_db)):
    """IM端催员登录"""
    tenant_id_str = login_data.tenantId
    collector_id = login_data.collectorId
    password = login_data.password
    
    try:
        # 将tenantId转换为整数
        tenant_id = int(tenant_id_str)
    except ValueError:
        raise HTTPException(status_code=400, detail="机构ID格式错误")
    
    # 查找催员：根据tenant_id和login_id查找
    collector = db.query(Collector).filter(
        and_(
            Collector.tenant_id == tenant_id,
            Collector.login_id == collector_id,
            Collector.is_active == True
        )
    ).first()
    
    if not collector:
        raise HTTPException(status_code=401, detail="催员不存在或已被禁用")
    
    # 验证密码（支持SHA256和bcrypt两种格式）
    import hashlib
    password_valid = False
    
    # 检查是否是SHA256哈希（64字符的十六进制字符串）
    if len(collector.password_hash) == 64:
        # SHA256格式：直接比较哈希值
        password_hash = hashlib.sha256(password.encode()).hexdigest()
        password_valid = (password_hash == collector.password_hash)
    else:
        # bcrypt格式：使用verify_password验证
        try:
            password_valid = verify_password(password, collector.password_hash)
        except Exception:
            # 如果bcrypt验证失败，尝试SHA256
            password_hash = hashlib.sha256(password.encode()).hexdigest()
            password_valid = (password_hash == collector.password_hash)
    
    if not password_valid:
        raise HTTPException(status_code=401, detail="密码错误")
    
    # 检查催员状态
    if collector.status != "active":
        raise HTTPException(status_code=403, detail=f"催员状态异常：{collector.status}")
    
    # 获取催员相关信息
    tenant = db.query(Tenant).filter(Tenant.id == collector.tenant_id).first()
    agency = db.query(CollectionAgency).filter(CollectionAgency.id == collector.agency_id).first()
    team = db.query(CollectionTeam).filter(CollectionTeam.id == collector.team_id).first()
    
    # 更新最后登录时间
    collector.last_login_at = datetime.now()
    db.commit()
    
    # 生成token
    access_token_expires = timedelta(minutes=settings.ACCESS_TOKEN_EXPIRE_MINUTES)
    token = create_access_token(
        subject=f"collector_{collector.id}",
        expires_delta=access_token_expires
    )
    
    # 构建用户信息
    user_info = {
        'id': collector.id,
        'collectorId': collector.login_id,
        'collectorCode': collector.collector_code,
        'collectorName': collector.collector_name,
        'tenantId': str(collector.tenant_id),
        'tenantName': tenant.tenant_name if tenant else '',
        'agencyId': collector.agency_id,
        'agencyName': agency.agency_name if agency else '',
        'teamId': collector.team_id,
        'teamName': team.team_name if team else '',
        'mobile': collector.mobile or '',
        'email': collector.email or '',
        'collectorLevel': collector.collector_level or '',
        'status': collector.status,
        'currentCaseCount': collector.current_case_count,
        'maxCaseCount': collector.max_case_count,
        'role': 'collector',  # 默认角色为催员
        'permissions': [
            'case:view',
            'case:call',
            'message:send'
        ]
    }
    
    return {
        'code': 200,
        'message': '登录成功',
        'data': {
            'token': token,
            'user': user_info
        }
    }


@router.post("/logout")
def im_logout():
    """IM端登出"""
    return {
        'code': 200,
        'message': '登出成功'
    }


@router.get("/user-info")
def get_im_user_info():
    """获取当前IM端用户信息"""
    # TODO: 从token中解析用户信息
    return {
        'code': 200,
        'message': '获取成功',
        'data': {}
    }


@router.post("/refresh-token")
def refresh_token():
    """刷新token"""
    # TODO: 实现token刷新逻辑
    return {
        'code': 200,
        'message': '刷新成功',
        'data': {}
    }


@router.get("/check-session")
def check_session():
    """检查会话状态"""
    # TODO: 实现会话检查逻辑
    return {
        'code': 200,
        'message': '会话有效',
        'data': {}
    }

