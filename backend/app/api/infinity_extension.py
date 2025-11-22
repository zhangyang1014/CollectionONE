"""Infinity分机池管理API"""
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from typing import List

from app.core.database import get_db
from app.models.infinity_extension_pool import InfinityExtensionPool, ExtensionStatusEnum
from app.models.infinity_call_config import InfinityCallConfig
from app.models.tenant import Tenant
from app.schemas.infinity import (
    ExtensionPoolCreate,
    ExtensionPoolBatchImport,
    ExtensionPoolUpdate,
    ExtensionPoolResponse,
    ExtensionPoolStatistics
)
from app.services.extension_allocator import ExtensionAllocator

router = APIRouter(prefix="/infinity/extensions", tags=["Infinity分机池管理"])


@router.post("/batch-import", summary="批量导入分机号")
def batch_import_extensions(
    batch_data: ExtensionPoolBatchImport,
    db: Session = Depends(get_db)
):
    """
    批量导入分机号到分机池
    
    - **tenant_id**: 甲方ID
    - **config_id**: Infinity配置ID
    - **extension_numbers**: 分机号列表
    """
    # 验证甲方是否存在
    tenant = db.query(Tenant).filter(Tenant.id == batch_data.tenant_id).first()
    if not tenant:
        raise HTTPException(status_code=404, detail=f"甲方 ID {batch_data.tenant_id} 不存在")
    
    # 验证配置是否存在
    config = db.query(InfinityCallConfig).filter(
        InfinityCallConfig.id == batch_data.config_id,
        InfinityCallConfig.tenant_id == batch_data.tenant_id
    ).first()
    if not config:
        raise HTTPException(
            status_code=404,
            detail=f"配置 ID {batch_data.config_id} 不存在或不属于甲方 {batch_data.tenant_id}"
        )
    
    # 检查哪些分机号已存在
    existing_extensions = db.query(InfinityExtensionPool).filter(
        InfinityExtensionPool.tenant_id == batch_data.tenant_id,
        InfinityExtensionPool.infinity_extension_number.in_(batch_data.extension_numbers)
    ).all()
    
    existing_numbers = {ext.infinity_extension_number for ext in existing_extensions}
    new_numbers = [num for num in batch_data.extension_numbers if num not in existing_numbers]
    
    # 批量创建新分机
    created_extensions = []
    for ext_number in new_numbers:
        extension = InfinityExtensionPool(
            tenant_id=batch_data.tenant_id,
            config_id=batch_data.config_id,
            infinity_extension_number=ext_number,
            status=ExtensionStatusEnum.AVAILABLE
        )
        db.add(extension)
        created_extensions.append(ext_number)
    
    db.commit()
    
    return {
        "message": "批量导入完成",
        "total_submitted": len(batch_data.extension_numbers),
        "created_count": len(created_extensions),
        "skipped_count": len(existing_numbers),
        "created_extensions": created_extensions,
        "skipped_extensions": list(existing_numbers)
    }


@router.get("/{tenant_id}", response_model=List[ExtensionPoolResponse], summary="查询分机池")
def get_extensions(
    tenant_id: int,
    config_id: int = None,
    status: ExtensionStatusEnum = None,
    db: Session = Depends(get_db)
):
    """
    查询甲方的分机池
    
    - **tenant_id**: 甲方ID
    - **config_id**: 配置ID（可选）
    - **status**: 状态筛选（可选）
    """
    query = db.query(InfinityExtensionPool).filter(
        InfinityExtensionPool.tenant_id == tenant_id
    )
    
    if config_id:
        query = query.filter(InfinityExtensionPool.config_id == config_id)
    
    if status:
        query = query.filter(InfinityExtensionPool.status == status)
    
    extensions = query.order_by(InfinityExtensionPool.infinity_extension_number.asc()).all()
    
    return extensions


@router.get("/statistics/{tenant_id}", response_model=ExtensionPoolStatistics, summary="分机使用统计")
def get_extension_statistics(
    tenant_id: int,
    config_id: int = None,
    db: Session = Depends(get_db)
):
    """
    获取分机池使用统计
    
    - **tenant_id**: 甲方ID
    - **config_id**: 配置ID（可选）
    """
    allocator = ExtensionAllocator(db)
    stats = allocator.get_extension_statistics(tenant_id, config_id)
    
    return ExtensionPoolStatistics(**stats)


@router.put("/{extension_id}", response_model=ExtensionPoolResponse, summary="更新分机")
def update_extension(
    extension_id: int,
    update_data: ExtensionPoolUpdate,
    db: Session = Depends(get_db)
):
    """
    更新分机信息（如状态、分机号）
    """
    extension = db.query(InfinityExtensionPool).filter(
        InfinityExtensionPool.id == extension_id
    ).first()
    
    if not extension:
        raise HTTPException(status_code=404, detail=f"分机 ID {extension_id} 不存在")
    
    # 更新字段
    update_dict = update_data.model_dump(exclude_unset=True)
    
    # 如果要修改分机号，检查是否重复
    if 'infinity_extension_number' in update_dict:
        new_number = update_dict['infinity_extension_number']
        existing = db.query(InfinityExtensionPool).filter(
            InfinityExtensionPool.tenant_id == extension.tenant_id,
            InfinityExtensionPool.infinity_extension_number == new_number,
            InfinityExtensionPool.id != extension_id
        ).first()
        if existing:
            raise HTTPException(
                status_code=400,
                detail=f"分机号 {new_number} 已存在"
            )
    
    for key, value in update_dict.items():
        setattr(extension, key, value)
    
    db.commit()
    db.refresh(extension)
    
    return extension


@router.post("/{extension_id}/release", summary="手动释放分机")
def release_extension(
    extension_id: int,
    db: Session = Depends(get_db)
):
    """
    手动释放分机（用于异常情况处理）
    """
    extension = db.query(InfinityExtensionPool).filter(
        InfinityExtensionPool.id == extension_id
    ).first()
    
    if not extension:
        raise HTTPException(status_code=404, detail=f"分机 ID {extension_id} 不存在")
    
    if extension.status != ExtensionStatusEnum.IN_USE:
        raise HTTPException(
            status_code=400,
            detail=f"分机 {extension.infinity_extension_number} 当前状态为 {extension.status.value}，无需释放"
        )
    
    # 释放分机
    allocator = ExtensionAllocator(db)
    success = allocator.release_extension(
        tenant_id=extension.tenant_id,
        extension_number=extension.infinity_extension_number
    )
    
    if success:
        return {
            "message": "分机已成功释放",
            "extension_id": extension_id,
            "extension_number": extension.infinity_extension_number
        }
    else:
        raise HTTPException(status_code=500, detail="释放分机失败")


@router.delete("/{extension_id}", summary="删除分机")
def delete_extension(
    extension_id: int,
    db: Session = Depends(get_db)
):
    """
    删除分机
    
    注意：只能删除状态为 available 或 offline 的分机
    """
    extension = db.query(InfinityExtensionPool).filter(
        InfinityExtensionPool.id == extension_id
    ).first()
    
    if not extension:
        raise HTTPException(status_code=404, detail=f"分机 ID {extension_id} 不存在")
    
    if extension.status == ExtensionStatusEnum.IN_USE:
        raise HTTPException(
            status_code=400,
            detail=f"分机 {extension.infinity_extension_number} 正在使用中，无法删除"
        )
    
    extension_number = extension.infinity_extension_number
    db.delete(extension)
    db.commit()
    
    return {
        "message": "分机删除成功",
        "extension_id": extension_id,
        "extension_number": extension_number
    }


@router.post("/batch-delete", summary="批量删除分机")
def batch_delete_extensions(
    extension_ids: List[int],
    db: Session = Depends(get_db)
):
    """
    批量删除分机
    
    - **extension_ids**: 分机ID列表
    """
    extensions = db.query(InfinityExtensionPool).filter(
        InfinityExtensionPool.id.in_(extension_ids)
    ).all()
    
    if not extensions:
        raise HTTPException(status_code=404, detail="未找到任何指定的分机")
    
    # 检查是否有正在使用的分机
    in_use_extensions = [ext for ext in extensions if ext.status == ExtensionStatusEnum.IN_USE]
    if in_use_extensions:
        in_use_numbers = [ext.infinity_extension_number for ext in in_use_extensions]
        raise HTTPException(
            status_code=400,
            detail=f"以下分机正在使用中，无法删除：{', '.join(in_use_numbers)}"
        )
    
    # 执行删除
    deleted_count = 0
    for extension in extensions:
        db.delete(extension)
        deleted_count += 1
    
    db.commit()
    
    return {
        "message": "批量删除完成",
        "deleted_count": deleted_count,
        "total_requested": len(extension_ids)
    }


@router.post("/force-release-collector/{collector_id}", summary="强制释放催员占用的分机")
def force_release_collector_extensions(
    collector_id: int,
    db: Session = Depends(get_db)
):
    """
    强制释放某个催员占用的所有分机（用于异常情况处理）
    
    - **collector_id**: 催员ID
    """
    allocator = ExtensionAllocator(db)
    released_count = allocator.force_release_collector_extensions(collector_id)
    
    return {
        "message": f"已释放催员 {collector_id} 占用的分机",
        "released_count": released_count
    }

