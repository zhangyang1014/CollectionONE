"""渠道供应商管理API"""
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from typing import List
from app.core.database import get_db
from app.models.channel_supplier import ChannelSupplier, ChannelTypeEnum
from app.models.tenant import Tenant
from app.schemas.channel import (
    ChannelSupplierCreate,
    ChannelSupplierUpdate,
    ChannelSupplierResponse,
    SupplierOrderBatchUpdate
)

router = APIRouter(prefix="/channel-suppliers", tags=["渠道供应商管理"])


@router.get("/tenants/{tenant_id}/channels/{channel_type}/suppliers", response_model=List[ChannelSupplierResponse])
def get_channel_suppliers(
    tenant_id: int,
    channel_type: ChannelTypeEnum,
    db: Session = Depends(get_db)
):
    """获取指定甲方和渠道类型的供应商列表"""
    # 验证甲方是否存在
    tenant = db.query(Tenant).filter(Tenant.id == tenant_id).first()
    if not tenant:
        raise HTTPException(status_code=404, detail="甲方不存在")
    
    suppliers = db.query(ChannelSupplier).filter(
        ChannelSupplier.tenant_id == tenant_id,
        ChannelSupplier.channel_type == channel_type
    ).order_by(ChannelSupplier.sort_order.asc(), ChannelSupplier.id.asc()).all()
    
    return suppliers


@router.post("/tenants/{tenant_id}/channels/{channel_type}/suppliers", response_model=ChannelSupplierResponse)
def create_channel_supplier(
    tenant_id: int,
    channel_type: ChannelTypeEnum,
    supplier: ChannelSupplierCreate,
    db: Session = Depends(get_db)
):
    """创建渠道供应商"""
    # 验证甲方是否存在
    tenant = db.query(Tenant).filter(Tenant.id == tenant_id).first()
    if not tenant:
        raise HTTPException(status_code=404, detail="甲方不存在")
    
    # 验证渠道类型是否匹配
    if supplier.channel_type != channel_type:
        raise HTTPException(status_code=400, detail="渠道类型不匹配")
    
    if supplier.tenant_id != tenant_id:
        raise HTTPException(status_code=400, detail="甲方ID不匹配")
    
    # 获取当前最大排序号
    max_sort_order = db.query(ChannelSupplier).filter(
        ChannelSupplier.tenant_id == tenant_id,
        ChannelSupplier.channel_type == channel_type
    ).order_by(ChannelSupplier.sort_order.desc()).first()
    
    if max_sort_order:
        supplier.sort_order = max_sort_order.sort_order + 1
    else:
        supplier.sort_order = 0
    
    db_supplier = ChannelSupplier(**supplier.model_dump())
    db.add(db_supplier)
    db.commit()
    db.refresh(db_supplier)
    
    return db_supplier


@router.get("/{supplier_id}", response_model=ChannelSupplierResponse)
def get_channel_supplier(
    supplier_id: int,
    db: Session = Depends(get_db)
):
    """获取单个渠道供应商"""
    supplier = db.query(ChannelSupplier).filter(ChannelSupplier.id == supplier_id).first()
    if not supplier:
        raise HTTPException(status_code=404, detail="供应商不存在")
    return supplier


@router.put("/{supplier_id}", response_model=ChannelSupplierResponse)
def update_channel_supplier(
    supplier_id: int,
    supplier_update: ChannelSupplierUpdate,
    db: Session = Depends(get_db)
):
    """更新渠道供应商"""
    supplier = db.query(ChannelSupplier).filter(ChannelSupplier.id == supplier_id).first()
    if not supplier:
        raise HTTPException(status_code=404, detail="供应商不存在")
    
    # 更新字段
    update_data = supplier_update.model_dump(exclude_unset=True)
    for key, value in update_data.items():
        setattr(supplier, key, value)
    
    db.commit()
    db.refresh(supplier)
    
    return supplier


@router.delete("/{supplier_id}")
def delete_channel_supplier(
    supplier_id: int,
    db: Session = Depends(get_db)
):
    """删除渠道供应商"""
    supplier = db.query(ChannelSupplier).filter(ChannelSupplier.id == supplier_id).first()
    if not supplier:
        raise HTTPException(status_code=404, detail="供应商不存在")
    
    tenant_id = supplier.tenant_id
    channel_type = supplier.channel_type
    deleted_sort_order = supplier.sort_order
    
    db.delete(supplier)
    db.commit()
    
    # 更新其他供应商的排序号
    remaining_suppliers = db.query(ChannelSupplier).filter(
        ChannelSupplier.tenant_id == tenant_id,
        ChannelSupplier.channel_type == channel_type,
        ChannelSupplier.sort_order > deleted_sort_order
    ).all()
    
    for remaining in remaining_suppliers:
        remaining.sort_order -= 1
    
    db.commit()
    
    return {"message": "供应商删除成功"}


@router.put("/tenants/{tenant_id}/channels/{channel_type}/suppliers/order")
def update_supplier_order(
    tenant_id: int,
    channel_type: ChannelTypeEnum,
    order_update: SupplierOrderBatchUpdate,
    db: Session = Depends(get_db)
):
    """批量更新供应商排序"""
    # 验证甲方是否存在
    tenant = db.query(Tenant).filter(Tenant.id == tenant_id).first()
    if not tenant:
        raise HTTPException(status_code=404, detail="甲方不存在")
    
    # 验证所有供应商都属于该甲方和渠道类型
    supplier_ids = [order.supplier_id for order in order_update.orders]
    suppliers = db.query(ChannelSupplier).filter(
        ChannelSupplier.id.in_(supplier_ids),
        ChannelSupplier.tenant_id == tenant_id,
        ChannelSupplier.channel_type == channel_type
    ).all()
    
    if len(suppliers) != len(supplier_ids):
        raise HTTPException(status_code=400, detail="部分供应商不存在或不属于该甲方/渠道类型")
    
    # 创建排序映射
    order_map = {order.supplier_id: order.sort_order for order in order_update.orders}
    
    # 更新排序
    for supplier in suppliers:
        supplier.sort_order = order_map[supplier.id]
    
    db.commit()
    
    return {"message": "排序更新成功"}

