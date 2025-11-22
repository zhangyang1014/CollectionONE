"""Infinity配置管理API"""
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from typing import Optional
import httpx
import time

from app.core.database import get_db
from app.models.infinity_call_config import InfinityCallConfig
from app.models.tenant import Tenant
from app.schemas.infinity import (
    InfinityCallConfigCreate,
    InfinityCallConfigUpdate,
    InfinityCallConfigResponse,
    TestConnectionRequest,
    TestConnectionResponse
)

router = APIRouter(prefix="/infinity/configs", tags=["Infinity配置管理"])


@router.post("", response_model=InfinityCallConfigResponse, summary="创建Infinity配置")
def create_infinity_config(
    config: InfinityCallConfigCreate,
    db: Session = Depends(get_db)
):
    """
    为指定甲方创建Infinity外呼配置
    
    - **tenant_id**: 甲方ID（必须唯一）
    - **api_url**: Infinity API地址
    - **access_token**: API访问令牌
    - **app_id**: 应用ID（必填）
    - **caller_number_range_start**: 号段起始（可选）
    - **caller_number_range_end**: 号段结束（可选）
    - **callback_url**: 回调地址（可选）
    - **max_concurrent_calls**: 最大并发呼叫数
    """
    # 验证甲方是否存在
    tenant = db.query(Tenant).filter(Tenant.id == config.tenant_id).first()
    if not tenant:
        raise HTTPException(status_code=404, detail=f"甲方 ID {config.tenant_id} 不存在")
    
    # 检查该甲方是否已有配置
    existing_config = db.query(InfinityCallConfig).filter(
        InfinityCallConfig.tenant_id == config.tenant_id
    ).first()
    if existing_config:
        raise HTTPException(
            status_code=400,
            detail=f"甲方 {config.tenant_id} 已存在 Infinity 配置，请使用更新接口"
        )
    
    # 创建配置
    db_config = InfinityCallConfig(**config.model_dump())
    db.add(db_config)
    db.commit()
    db.refresh(db_config)
    
    return db_config


@router.get("/{tenant_id}", response_model=InfinityCallConfigResponse, summary="获取甲方的Infinity配置")
def get_infinity_config(
    tenant_id: int,
    db: Session = Depends(get_db)
):
    """
    获取指定甲方的Infinity外呼配置
    """
    config = db.query(InfinityCallConfig).filter(
        InfinityCallConfig.tenant_id == tenant_id
    ).first()
    
    if not config:
        raise HTTPException(
            status_code=404,
            detail=f"甲方 {tenant_id} 没有 Infinity 配置"
        )
    
    return config


@router.get("/id/{config_id}", response_model=InfinityCallConfigResponse, summary="根据配置ID获取")
def get_infinity_config_by_id(
    config_id: int,
    db: Session = Depends(get_db)
):
    """
    根据配置ID获取Infinity配置
    """
    config = db.query(InfinityCallConfig).filter(
        InfinityCallConfig.id == config_id
    ).first()
    
    if not config:
        raise HTTPException(status_code=404, detail=f"配置 ID {config_id} 不存在")
    
    return config


@router.put("/{config_id}", response_model=InfinityCallConfigResponse, summary="更新Infinity配置")
def update_infinity_config(
    config_id: int,
    config_update: InfinityCallConfigUpdate,
    db: Session = Depends(get_db)
):
    """
    更新Infinity外呼配置
    """
    db_config = db.query(InfinityCallConfig).filter(
        InfinityCallConfig.id == config_id
    ).first()
    
    if not db_config:
        raise HTTPException(status_code=404, detail=f"配置 ID {config_id} 不存在")
    
    # 更新字段（只更新提供的字段）
    update_data = config_update.model_dump(exclude_unset=True)
    for key, value in update_data.items():
        setattr(db_config, key, value)
    
    db.commit()
    db.refresh(db_config)
    
    return db_config


@router.delete("/{config_id}", summary="删除Infinity配置")
def delete_infinity_config(
    config_id: int,
    db: Session = Depends(get_db)
):
    """
    删除Infinity外呼配置
    
    注意：删除配置会级联删除相关的分机池数据
    """
    config = db.query(InfinityCallConfig).filter(
        InfinityCallConfig.id == config_id
    ).first()
    
    if not config:
        raise HTTPException(status_code=404, detail=f"配置 ID {config_id} 不存在")
    
    db.delete(config)
    db.commit()
    
    return {"message": "配置删除成功", "config_id": config_id}


@router.post("/test-connection", response_model=TestConnectionResponse, summary="测试Infinity连接")
def test_infinity_connection(
    request: TestConnectionRequest,
    db: Session = Depends(get_db)
):
    """
    测试与Infinity API的连接
    
    - 验证API地址是否可访问
    - 验证访问令牌是否有效
    """
    try:
        start_time = time.time()
        
        # 构建测试请求（根据Infinity文档，可以调用一个简单的查询接口）
        # 这里假设有一个获取分机列表的接口
        test_url = f"{request.api_url.rstrip('/')}"
        
        # 尝试发送请求
        with httpx.Client() as client:
            response = client.post(
                test_url,
                data={
                    'service': 'App.Sip_Extension.GetList',  # 假设的测试接口
                    'token': request.access_token
                },
                timeout=10.0
            )
        
        end_time = time.time()
        response_time_ms = int((end_time - start_time) * 1000)
        
        # 检查响应
        if response.status_code == 200:
            result = response.json()
            
            # 根据Infinity的返回格式判断（ret=200表示成功）
            if result.get('ret') == 200:
                return TestConnectionResponse(
                    success=True,
                    message="连接成功！Infinity API 工作正常",
                    response_time_ms=response_time_ms
                )
            else:
                return TestConnectionResponse(
                    success=False,
                    message=f"连接失败：{result.get('msg', '未知错误')}",
                    response_time_ms=response_time_ms
                )
        else:
            return TestConnectionResponse(
                success=False,
                message=f"HTTP错误：{response.status_code}",
                response_time_ms=response_time_ms
            )
    
    except httpx.TimeoutException:
        return TestConnectionResponse(
            success=False,
            message="连接超时：无法在10秒内连接到 Infinity API"
        )
    except httpx.ConnectError:
        return TestConnectionResponse(
            success=False,
            message="连接错误：无法连接到 Infinity API，请检查网络和URL"
        )
    except Exception as e:
        return TestConnectionResponse(
            success=False,
            message=f"测试失败：{str(e)}"
        )


@router.post("/{config_id}/toggle", response_model=InfinityCallConfigResponse, summary="启用/禁用配置")
def toggle_infinity_config(
    config_id: int,
    is_active: bool,
    db: Session = Depends(get_db)
):
    """
    启用或禁用Infinity配置
    """
    config = db.query(InfinityCallConfig).filter(
        InfinityCallConfig.id == config_id
    ).first()
    
    if not config:
        raise HTTPException(status_code=404, detail=f"配置 ID {config_id} 不存在")
    
    config.is_active = is_active
    db.commit()
    db.refresh(config)
    
    status_text = "启用" if is_active else "禁用"
    return config

