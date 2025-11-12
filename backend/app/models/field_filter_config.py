from sqlalchemy import Column, BigInteger, String, Boolean, DateTime, ForeignKey, JSON, Enum as SQLEnum
from sqlalchemy.sql import func
from sqlalchemy.orm import relationship
from app.core.database import Base
from app.models.tenant_field_config import FieldTypeEnum


class FieldFilterConfig(Base):
    """字段筛选配置表"""
    __tablename__ = "field_filter_configs"

    id = Column(BigInteger, primary_key=True, autoincrement=True)
    tenant_id = Column(BigInteger, ForeignKey('tenants.id'), nullable=False, comment='甲方ID')
    field_id = Column(BigInteger, nullable=False, comment='字段ID')
    field_type = Column(SQLEnum(FieldTypeEnum), nullable=False, comment='字段类型')
    filter_type = Column(String(50), nullable=False, comment='筛选类型：text/number/enum/date')
    filter_config = Column(JSON, comment='筛选配置（JSON格式）')
    is_enabled = Column(Boolean, default=True, comment='是否启用筛选')
    created_at = Column(DateTime, server_default=func.now())
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now())

    # 关系
    tenant = relationship("Tenant", back_populates="filter_configs")

