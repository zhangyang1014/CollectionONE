"""通知配置模型"""
from sqlalchemy import Column, BigInteger, String, Boolean, DateTime, JSON
from sqlalchemy.sql import func
from app.core.database import Base


class NotificationConfig(Base):
    """通知配置表"""
    __tablename__ = "notification_configs"

    id = Column(BigInteger, primary_key=True, index=True, autoincrement=True)
    tenant_id = Column(BigInteger, nullable=True, index=True, comment="甲方ID（NULL表示全局配置）")
    notification_type = Column(String(50), nullable=False, index=True, comment="通知类型：unreplied/nudge/case_update/performance/timeout")
    is_enabled = Column(Boolean, default=True, comment="是否启用")
    config_data = Column(JSON, nullable=False, comment="配置数据（JSON格式）")
    created_at = Column(DateTime, server_default=func.now(), comment="创建时间")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), comment="更新时间")

    class Config:
        orm_mode = True

