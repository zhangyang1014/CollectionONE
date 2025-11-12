from sqlalchemy import Column, BigInteger, Boolean, DateTime, ForeignKey, Integer, Enum as SQLEnum
from sqlalchemy.sql import func
from sqlalchemy.orm import relationship
from app.core.database import Base
import enum


class FieldTypeEnum(str, enum.Enum):
    STANDARD = "standard"
    CUSTOM = "custom"


class TenantFieldConfig(Base):
    """甲方字段启用配置表"""
    __tablename__ = "tenant_field_configs"

    id = Column(Integer, primary_key=True, autoincrement=True)
    tenant_id = Column(BigInteger, ForeignKey('tenants.id'), nullable=False, comment='甲方ID')
    field_id = Column(BigInteger, nullable=False, comment='字段ID（标准字段或自定义字段）')
    field_type = Column(SQLEnum(FieldTypeEnum), nullable=False, comment='字段类型')
    is_enabled = Column(Boolean, default=True, comment='是否启用')
    is_required = Column(Boolean, default=False, comment='是否必填')
    is_readonly = Column(Boolean, default=False, comment='是否只读')
    is_visible = Column(Boolean, default=True, comment='是否可见')
    sort_order = Column(Integer, default=0, comment='排序顺序')
    created_at = Column(DateTime, server_default=func.now())
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now())

    # 关系
    tenant = relationship("Tenant", back_populates="field_configs")

