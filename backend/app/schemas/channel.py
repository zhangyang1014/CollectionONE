"""渠道供应商相关Schema"""
from pydantic import BaseModel, Field
from typing import Optional, List
from datetime import datetime
from app.models.channel_supplier import ChannelTypeEnum


class ChannelSupplierBase(BaseModel):
    """渠道供应商基础Schema"""
    tenant_id: int = Field(..., description="所属甲方ID")
    channel_type: ChannelTypeEnum = Field(..., description="渠道类型")
    supplier_name: str = Field(..., max_length=200, description="供应商名字")
    api_url: str = Field(..., max_length=500, description="供应商接口地址")
    api_key: str = Field(..., max_length=500, description="供应商API Key")
    secret_key: str = Field(..., max_length=500, description="SECRET_KEY")
    sort_order: int = Field(0, description="排序号")
    is_active: bool = Field(True, description="是否启用")
    remark: Optional[str] = Field(None, max_length=1000, description="备注")


class ChannelSupplierCreate(ChannelSupplierBase):
    """创建渠道供应商"""
    pass


class ChannelSupplierUpdate(BaseModel):
    """更新渠道供应商"""
    supplier_name: Optional[str] = Field(None, max_length=200)
    api_url: Optional[str] = Field(None, max_length=500)
    api_key: Optional[str] = Field(None, max_length=500)
    secret_key: Optional[str] = Field(None, max_length=500)
    sort_order: Optional[int] = None
    is_active: Optional[bool] = None
    remark: Optional[str] = Field(None, max_length=1000)


class ChannelSupplierResponse(ChannelSupplierBase):
    """渠道供应商响应"""
    id: int
    created_at: datetime
    updated_at: datetime

    class Config:
        from_attributes = True


class SupplierOrderUpdate(BaseModel):
    """供应商排序更新"""
    supplier_id: int = Field(..., description="供应商ID")
    sort_order: int = Field(..., description="新的排序号")


class SupplierOrderBatchUpdate(BaseModel):
    """批量更新供应商排序"""
    orders: List[SupplierOrderUpdate] = Field(..., description="排序列表")

