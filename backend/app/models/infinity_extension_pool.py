"""Infinity分机池模型"""
from sqlalchemy import Column, Integer, BigInteger, String, DateTime, ForeignKey, Enum as SQLEnum
from sqlalchemy.orm import relationship
from sqlalchemy.sql import func
from app.core.database import Base
import enum


class ExtensionStatusEnum(str, enum.Enum):
    """分机状态枚举"""
    AVAILABLE = "available"  # 空闲可用
    IN_USE = "in_use"        # 使用中
    OFFLINE = "offline"      # 离线


class InfinityExtensionPool(Base):
    """Infinity分机池表"""
    __tablename__ = "infinity_extension_pool"

    id = Column(Integer, primary_key=True, index=True, autoincrement=True)
    tenant_id = Column(BigInteger, ForeignKey("tenants.id", ondelete="CASCADE"), nullable=False, index=True, comment="所属甲方ID")
    config_id = Column(BigInteger, ForeignKey("infinity_call_configs.id", ondelete="CASCADE"), nullable=False, index=True, comment="关联的 Infinity 配置ID")
    
    # 分机信息
    infinity_extension_number = Column(String(50), nullable=False, comment="分机号（如 '8001'）")
    
    # 状态信息
    status = Column(SQLEnum(ExtensionStatusEnum), nullable=False, default=ExtensionStatusEnum.AVAILABLE, index=True, comment="状态：available/in_use/offline")
    current_collector_id = Column(BigInteger, ForeignKey("collectors.id", ondelete="SET NULL"), index=True, comment="当前使用的催员ID（空闲时为NULL）")
    
    # 时间戳
    assigned_at = Column(DateTime, comment="分配时间")
    released_at = Column(DateTime, comment="释放时间")
    last_used_at = Column(DateTime, comment="最后使用时间")
    created_at = Column(DateTime, server_default=func.now(), comment="创建时间")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), comment="更新时间")

    # 关系
    tenant = relationship("Tenant", foreign_keys=[tenant_id])
    config = relationship("InfinityCallConfig", back_populates="extension_pool")
    collector = relationship("Collector", foreign_keys=[current_collector_id])

    class Config:
        orm_mode = True

    # 添加联合唯一约束索引的说明（在模型层面）
    __table_args__ = (
        # 由于 SQLAlchemy 的限制，唯一约束在迁移SQL中定义
    )

