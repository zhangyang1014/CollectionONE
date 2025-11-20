from sqlalchemy import Column, BigInteger, String, Boolean, DateTime, ForeignKey, Integer, Text, JSON
from sqlalchemy.sql import func
from sqlalchemy.orm import relationship
from app.core.database import Base


class StandardField(Base):
    """标准字段定义表"""
    __tablename__ = "standard_fields"

    id = Column(BigInteger, primary_key=True, autoincrement=True)
    field_key = Column(String(100), unique=True, nullable=False, comment='字段唯一标识')
    field_name = Column(String(200), nullable=False, comment='字段名称（中文）')
    field_name_en = Column(String(200), comment='字段名称（英文）')
    field_type = Column(String(50), nullable=False, comment='字段类型')
    field_group_id = Column(BigInteger, ForeignKey('field_groups.id'), nullable=False, comment='所属分组ID')
    is_required = Column(Boolean, default=False, comment='是否必填')
    is_extended = Column(Boolean, default=False, comment='是否为拓展字段')
    description = Column(Text, comment='字段说明')
    example_value = Column(Text, comment='示例值')
    validation_rules = Column(JSON, comment='验证规则（JSON格式）')
    enum_options = Column(JSON, comment='枚举选项（如果是Enum类型）')
    sort_order = Column(Integer, default=0, comment='排序顺序')
    is_active = Column(Boolean, default=True, comment='是否启用')
    is_deleted = Column(Boolean, default=False, comment='软删除标记')
    deleted_at = Column(DateTime, comment='删除时间')
    created_at = Column(DateTime, server_default=func.now())
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now())

    # 关系
    field_group = relationship("FieldGroup", back_populates="standard_fields")

