from sqlalchemy import Column, BigInteger, String, Boolean, DateTime, Integer
from sqlalchemy.sql import func
from sqlalchemy.orm import relationship
from app.core.database import Base


class Tenant(Base):
    """甲方配置表"""
    __tablename__ = "tenants"

    id = Column(BigInteger, primary_key=True, autoincrement=True)
    tenant_code = Column(String(100), unique=True, nullable=False, comment='甲方编码')
    tenant_name = Column(String(200), nullable=False, comment='甲方名称')
    tenant_name_en = Column(String(200), comment='甲方名称（英文）')
    country_code = Column(String(10), comment='国家代码')
    timezone = Column(Integer, default=0, comment='时区偏移量（UTC偏移，范围-12到+14）')
    currency_code = Column(String(10), default='USD', comment='货币代码')
    is_active = Column(Boolean, default=True, comment='是否启用')
    created_at = Column(DateTime, server_default=func.now())
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now())

    # 关系
    field_configs = relationship("TenantFieldConfig", back_populates="tenant")
    custom_fields = relationship("CustomField", back_populates="tenant")
    cases = relationship("Case", back_populates="tenant")
    filter_configs = relationship("FieldFilterConfig", back_populates="tenant")
    agencies = relationship("CollectionAgency", back_populates="tenant")
    team_groups = relationship("TeamGroup", back_populates="tenant")
    teams = relationship("CollectionTeam", back_populates="tenant")
    collectors = relationship("Collector", back_populates="tenant")
    queues = relationship("CaseQueue", back_populates="tenant")
    channel_suppliers = relationship("ChannelSupplier", back_populates="tenant")
    # 新增关系
    performance_stats = relationship("CollectorPerformanceStat", back_populates="tenant")
    custom_dimension_stats = relationship("CustomDimensionStat", back_populates="tenant")

