"""Infinity外呼配置模型"""
from sqlalchemy import Column, BigInteger, String, Integer, Boolean, DateTime, ForeignKey, JSON, Text
from sqlalchemy.orm import relationship
from sqlalchemy.sql import func
from app.core.database import Base


class InfinityCallConfig(Base):
    """Infinity外呼配置表"""
    __tablename__ = "infinity_call_configs"

    id = Column(Integer, primary_key=True, index=True, autoincrement=True)
    tenant_id = Column(BigInteger, ForeignKey("tenants.id", ondelete="CASCADE"), nullable=False, unique=True, index=True, comment="所属甲方ID")
    supplier_id = Column(BigInteger, ForeignKey("channel_suppliers.id", ondelete="SET NULL"), index=True, comment="关联的渠道供应商ID")
    
    # API配置
    api_url = Column(String(500), nullable=False, comment="Infinity API地址")
    access_token = Column(String(500), nullable=False, comment="API访问令牌")
    app_id = Column(String(100), nullable=False, comment="应用ID（必填）")
    
    # 号码配置
    caller_number_range_start = Column(String(50), comment="号段起始")
    caller_number_range_end = Column(String(50), comment="号段结束")
    
    # 回调配置
    callback_url = Column(String(500), comment="通话记录回调地址")
    recording_callback_url = Column(String(500), comment="录音回调地址")
    
    # 限制配置
    max_concurrent_calls = Column(Integer, default=100, comment="最大并发呼叫数")
    call_timeout_seconds = Column(Integer, default=60, comment="呼叫超时时间（秒）")
    
    # 状态
    is_active = Column(Boolean, default=True, index=True, comment="是否启用")
    
    # 时间戳
    created_at = Column(DateTime, server_default=func.now(), comment="创建时间")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), comment="更新时间")
    created_by = Column(BigInteger, comment="创建人ID")

    # 关系
    tenant = relationship("Tenant", foreign_keys=[tenant_id])
    supplier = relationship("ChannelSupplier", foreign_keys=[supplier_id])
    extension_pool = relationship("InfinityExtensionPool", back_populates="config", cascade="all, delete-orphan")

    class Config:
        orm_mode = True

