from sqlalchemy import Column, BigInteger, String, Boolean, DateTime, ForeignKey, Integer, Text, JSON
from sqlalchemy.sql import func
from sqlalchemy.orm import relationship
from app.core.database import Base


class CustomField(Base):
    """自定义字段定义表"""
    __tablename__ = "custom_fields"

    id = Column(BigInteger, primary_key=True, autoincrement=True)
    tenant_id = Column(BigInteger, ForeignKey('tenants.id'), nullable=False, comment='所属甲方ID')
    field_key = Column(String(100), nullable=False, comment='字段唯一标识')
    field_name = Column(String(200), nullable=False, comment='字段名称')
    field_name_en = Column(String(200), comment='字段名称（英文）')
    field_type = Column(String(50), nullable=False, comment='字段类型')
    field_group_id = Column(BigInteger, ForeignKey('field_groups.id'), nullable=False, comment='所属分组ID')
    is_required = Column(Boolean, default=False, comment='是否必填')
    description = Column(Text, comment='字段说明')
    example_value = Column(Text, comment='示例值')
    validation_rules = Column(JSON, comment='验证规则')
    enum_options = Column(JSON, comment='枚举选项（如果是Enum类型）')
    sort_order = Column(Integer, default=0, comment='排序顺序')
    is_active = Column(Boolean, default=True, comment='是否启用')
    is_deleted = Column(Boolean, default=False, comment='软删除标记')
    deleted_at = Column(DateTime, comment='删除时间')
    created_at = Column(DateTime, server_default=func.now())
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now())

    # 关系
    tenant = relationship("Tenant", back_populates="custom_fields")
    field_group = relationship("FieldGroup", back_populates="custom_fields")
    # 新增关系
    dimension_stats = relationship("CustomDimensionStat", back_populates="custom_field")

