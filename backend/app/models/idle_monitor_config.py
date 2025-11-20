"""
空闲监控配置模型
"""
from sqlalchemy import Column, BigInteger, String, Integer, Boolean, DateTime, JSON, Text
from sqlalchemy.sql import func
from app.core.database import Base


class IdleMonitorConfig(Base):
    """空闲监控配置表"""
    
    __tablename__ = "idle_monitor_configs"
    
    # 主键
    id = Column(BigInteger, primary_key=True, index=True, comment="主键ID")
    
    # 基本信息
    tenant_id = Column(BigInteger, nullable=False, index=True, comment="甲方ID")
    config_name = Column(String(100), nullable=False, comment="配置名称")
    
    # 配置内容
    work_time_slots = Column(JSON, nullable=False, comment="上班时间段，格式: [{'start':'09:00','end':'12:00'}]")
    idle_threshold_minutes = Column(Integer, nullable=False, default=30, comment="空闲阈值（分钟）")
    monitored_actions = Column(JSON, nullable=False, comment="监控行为列表，格式: ['call','whatsapp','sms']")
    exclude_holidays = Column(Boolean, nullable=False, default=True, comment="是否排除节假日")
    
    # 状态
    is_active = Column(Boolean, nullable=False, default=True, comment="是否启用")
    
    # 审计字段
    created_by = Column(String(100), nullable=True, comment="创建人")
    created_at = Column(DateTime(timezone=True), nullable=False, server_default=func.now(), comment="创建时间")
    updated_at = Column(DateTime(timezone=True), nullable=False, server_default=func.now(), onupdate=func.now(), comment="更新时间")
    
    def __repr__(self):
        return f"<IdleMonitorConfig(id={self.id}, tenant_id={self.tenant_id}, config_name={self.config_name})>"

