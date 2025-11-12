"""渠道供应商模型"""
from sqlalchemy import Column, Integer, String, Boolean, DateTime, ForeignKey, Enum as SQLEnum
from sqlalchemy.orm import relationship
from sqlalchemy.sql import func
from app.core.database import Base
import enum


class ChannelTypeEnum(str, enum.Enum):
    """渠道类型枚举"""
    SMS = "sms"
    RCS = "rcs"
    WHATSAPP = "whatsapp"
    CALL = "call"


class ChannelSupplier(Base):
    """渠道供应商表"""
    __tablename__ = "channel_suppliers"

    id = Column(Integer, primary_key=True, index=True, autoincrement=True)
    tenant_id = Column(Integer, ForeignKey("tenants.id"), nullable=False, index=True, comment="所属甲方ID")
    channel_type = Column(SQLEnum(ChannelTypeEnum), nullable=False, index=True, comment="渠道类型：sms/rcs/whatsapp/call")
    supplier_name = Column(String(200), nullable=False, comment="供应商名字")
    api_url = Column(String(500), nullable=False, comment="供应商接口地址")
    api_key = Column(String(500), nullable=False, comment="供应商API Key")
    secret_key = Column(String(500), nullable=False, comment="SECRET_KEY")
    sort_order = Column(Integer, default=0, nullable=False, comment="排序号")
    is_active = Column(Boolean, default=True, nullable=False, index=True, comment="是否启用")
    remark = Column(String(1000), comment="备注")
    created_at = Column(DateTime, server_default=func.now(), comment="创建时间")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), comment="更新时间")

    # 关系
    tenant = relationship("Tenant", back_populates="channel_suppliers")

    class Config:
        orm_mode = True

