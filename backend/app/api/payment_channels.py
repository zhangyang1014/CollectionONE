# -*- coding: utf-8 -*-
"""
还款渠道配置管理API（管理控台）
"""
from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.orm import Session
from sqlalchemy import and_
from typing import Optional, List
import json

from app.core.database import get_db
from app.core.response import success_response, error_response
from app.core.security import encrypt_data, decrypt_data
from app.models.payment_channel import PaymentChannel
from app.schemas.payment import (
    PaymentChannelCreate,
    PaymentChannelUpdate,
    PaymentChannelResponse,
    PaymentChannelSimple,
    PaymentChannelSortRequest
)

router = APIRouter(prefix="/api/admin/payment-channels", tags=["还款渠道配置"])


@router.get("", summary="获取渠道列表")
async def get_payment_channels(
    party_id: int = Query(..., description="甲方ID"),
    is_enabled: Optional[bool] = Query(None, description="是否启用"),
    page: int = Query(1, ge=1, description="页码"),
    page_size: int = Query(20, ge=1, le=100, description="每页数量"),
    db: Session = Depends(get_db)
):
    """
    获取还款渠道配置列表
    
    - **party_id**: 甲方ID（必填）
    - **is_enabled**: 是否启用（选填）
    - **page**: 页码
    - **page_size**: 每页数量
    """
    try:
        # 构建查询条件
        query = db.query(PaymentChannel).filter(PaymentChannel.party_id == party_id)
        
        if is_enabled is not None:
            query = query.filter(PaymentChannel.is_enabled == is_enabled)
        
        # 按排序权重和创建时间排序
        query = query.order_by(PaymentChannel.sort_order, PaymentChannel.created_at)
        
        # 计算总数
        total = query.count()
        
        # 分页
        offset = (page - 1) * page_size
        channels = query.offset(offset).limit(page_size).all()
        
        # 解密敏感字段（仅在需要时）
        result_list = []
        for channel in channels:
            channel_dict = {
                "id": channel.id,
                "channel_name": channel.channel_name,
                "channel_icon": channel.channel_icon,
                "channel_type": channel.channel_type,
                "service_provider": channel.service_provider,
                "description": channel.description,
                "api_url": channel.api_url,
                "api_method": channel.api_method,
                "auth_type": channel.auth_type,
                "is_enabled": channel.is_enabled,
                "sort_order": channel.sort_order,
                "created_at": channel.created_at,
                "updated_at": channel.updated_at
            }
            result_list.append(channel_dict)
        
        return success_response(data={
            "total": total,
            "page": page,
            "page_size": page_size,
            "list": result_list
        })
    
    except Exception as e:
        return error_response(message=f"获取渠道列表失败: {str(e)}")


@router.get("/{channel_id}", summary="获取渠道详情")
async def get_payment_channel(
    channel_id: int,
    db: Session = Depends(get_db)
):
    """获取单个还款渠道详情"""
    try:
        channel = db.query(PaymentChannel).filter(PaymentChannel.id == channel_id).first()
        
        if not channel:
            return error_response(message="渠道不存在", code=404)
        
        # 解密认证配置（如果需要展示）
        channel_dict = {
            "id": channel.id,
            "party_id": channel.party_id,
            "channel_name": channel.channel_name,
            "channel_icon": channel.channel_icon,
            "channel_type": channel.channel_type,
            "service_provider": channel.service_provider,
            "description": channel.description,
            "api_url": channel.api_url,
            "api_method": channel.api_method,
            "auth_type": channel.auth_type,
            "auth_config": channel.auth_config,  # 可能需要解密
            "request_params": channel.request_params,
            "is_enabled": channel.is_enabled,
            "sort_order": channel.sort_order,
            "created_by": channel.created_by,
            "updated_by": channel.updated_by,
            "created_at": channel.created_at,
            "updated_at": channel.updated_at
        }
        
        return success_response(data=channel_dict)
    
    except Exception as e:
        return error_response(message=f"获取渠道详情失败: {str(e)}")


@router.post("", summary="创建渠道")
async def create_payment_channel(
    channel_data: PaymentChannelCreate,
    db: Session = Depends(get_db)
):
    """
    创建新的还款渠道配置
    
    - 会对auth_config进行加密存储
    """
    try:
        # 创建新渠道
        new_channel = PaymentChannel(
            party_id=channel_data.party_id,
            channel_name=channel_data.channel_name,
            channel_icon=channel_data.channel_icon,
            channel_type=channel_data.channel_type,
            service_provider=channel_data.service_provider,
            description=channel_data.description,
            api_url=channel_data.api_url,
            api_method=channel_data.api_method,
            auth_type=channel_data.auth_type,
            auth_config=channel_data.auth_config,  # TODO: 加密存储
            request_params=channel_data.request_params,
            is_enabled=channel_data.is_enabled,
            sort_order=channel_data.sort_order
        )
        
        db.add(new_channel)
        db.commit()
        db.refresh(new_channel)
        
        return success_response(
            data={"id": new_channel.id},
            message="创建成功"
        )
    
    except Exception as e:
        db.rollback()
        return error_response(message=f"创建渠道失败: {str(e)}")


@router.put("/{channel_id}", summary="更新渠道")
async def update_payment_channel(
    channel_id: int,
    channel_data: PaymentChannelUpdate,
    db: Session = Depends(get_db)
):
    """更新还款渠道配置"""
    try:
        channel = db.query(PaymentChannel).filter(PaymentChannel.id == channel_id).first()
        
        if not channel:
            return error_response(message="渠道不存在", code=404)
        
        # 更新字段
        update_data = channel_data.dict(exclude_unset=True)
        for key, value in update_data.items():
            if key == 'auth_config' and value is not None:
                # TODO: 加密存储
                setattr(channel, key, value)
            else:
                setattr(channel, key, value)
        
        db.commit()
        db.refresh(channel)
        
        return success_response(message="更新成功")
    
    except Exception as e:
        db.rollback()
        return error_response(message=f"更新渠道失败: {str(e)}")


@router.delete("/{channel_id}", summary="删除渠道")
async def delete_payment_channel(
    channel_id: int,
    db: Session = Depends(get_db)
):
    """删除还款渠道配置"""
    try:
        channel = db.query(PaymentChannel).filter(PaymentChannel.id == channel_id).first()
        
        if not channel:
            return error_response(message="渠道不存在", code=404)
        
        # TODO: 检查是否有关联的还款码记录
        
        db.delete(channel)
        db.commit()
        
        return success_response(message="删除成功")
    
    except Exception as e:
        db.rollback()
        return error_response(message=f"删除渠道失败: {str(e)}")


@router.post("/{channel_id}/toggle", summary="切换启用状态")
async def toggle_payment_channel(
    channel_id: int,
    db: Session = Depends(get_db)
):
    """启用或禁用还款渠道"""
    try:
        channel = db.query(PaymentChannel).filter(PaymentChannel.id == channel_id).first()
        
        if not channel:
            return error_response(message="渠道不存在", code=404)
        
        # 切换状态
        channel.is_enabled = not channel.is_enabled
        db.commit()
        
        status_text = "启用" if channel.is_enabled else "禁用"
        return success_response(message=f"{status_text}成功")
    
    except Exception as e:
        db.rollback()
        return error_response(message=f"切换状态失败: {str(e)}")


@router.post("/sort", summary="批量更新排序")
async def sort_payment_channels(
    sort_data: PaymentChannelSortRequest,
    db: Session = Depends(get_db)
):
    """
    批量更新渠道排序
    
    - 按照channel_ids的顺序更新sort_order
    """
    try:
        # 验证所有渠道都属于该甲方
        channels = db.query(PaymentChannel).filter(
            and_(
                PaymentChannel.id.in_(sort_data.channel_ids),
                PaymentChannel.party_id == sort_data.party_id
            )
        ).all()
        
        if len(channels) != len(sort_data.channel_ids):
            return error_response(message="部分渠道不存在或不属于该甲方")
        
        # 更新排序
        for index, channel_id in enumerate(sort_data.channel_ids):
            db.query(PaymentChannel).filter(
                PaymentChannel.id == channel_id
            ).update({"sort_order": index})
        
        db.commit()
        
        return success_response(message="排序更新成功")
    
    except Exception as e:
        db.rollback()
        return error_response(message=f"更新排序失败: {str(e)}")

