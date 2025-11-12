"""管理后台认证API"""
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from pydantic import BaseModel
from app.core.database import get_db
from app.core.security import verify_password, create_access_token
from app.core.config import settings
from datetime import timedelta

router = APIRouter(prefix="/admin/auth", tags=["管理后台认证"])


class LoginRequest(BaseModel):
    """登录请求"""
    loginId: str
    password: str


class LoginResponse(BaseModel):
    """登录响应"""
    code: int = 200
    message: str = "登录成功"
    data: dict


@router.post("/login", response_model=LoginResponse)
def admin_login(login_data: LoginRequest, db: Session = Depends(get_db)):
    """管理后台登录"""
    login_id = login_data.loginId
    password = login_data.password
    
    # TODO: 这里应该从数据库或环境变量验证SuperAdmin和TenantAdmin
    # 临时实现：支持superadmin和tenantadmin登录
    if login_id.lower() == 'superadmin' and password == '123456':
        # SuperAdmin登录
        user_info = {
            'id': 1,
            'loginId': 'superadmin',
            'username': 'superadmin',
            'role': 'SuperAdmin',
            'email': 'admin@cco.com',
            'name': '超级管理员'
        }
    elif login_id.lower() == 'tenantadmin' and password == 'admin123':
        # TenantAdmin登录（示例）
        user_info = {
            'id': 2,
            'loginId': 'tenantadmin',
            'username': 'tenantadmin',
            'role': 'TenantAdmin',
            'email': 'tenant@cco.com',
            'name': '甲方管理员'
        }
    else:
        raise HTTPException(status_code=401, detail="登录ID或密码错误")
    
    # 生成token
    access_token_expires = timedelta(minutes=settings.ACCESS_TOKEN_EXPIRE_MINUTES)
    token = create_access_token(
        subject=user_info['loginId'],
        expires_delta=access_token_expires
    )
    
    return {
        'code': 200,
        'message': '登录成功',
        'data': {
            'token': token,
            'user': user_info
        }
    }


@router.post("/logout")
def admin_logout():
    """管理后台登出"""
    return {
        'code': 200,
        'message': '登出成功'
    }


@router.get("/me")
def get_current_user():
    """获取当前用户信息"""
    # TODO: 从token中解析用户信息
    return {
        'code': 200,
        'message': '获取成功',
        'data': {}
    }

