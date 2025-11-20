from sqlalchemy import Column, BigInteger, String, Integer, Boolean, DateTime, ForeignKey
from sqlalchemy.sql import func
from sqlalchemy.orm import relationship
from app.core.database import Base


class FieldGroup(Base):
    """字段分组表"""
    __tablename__ = "field_groups"

    id = Column(BigInteger, primary_key=True, autoincrement=True)
    group_key = Column(String(100), unique=True, nullable=False, comment='分组标识')
    group_name = Column(String(200), nullable=False, comment='分组名称（中文）')
    group_name_en = Column(String(200), comment='分组名称（英文）')
    parent_id = Column(BigInteger, ForeignKey('field_groups.id'), comment='父分组ID')
    sort_order = Column(Integer, default=0, comment='排序顺序')
    is_active = Column(Boolean, default=True, comment='是否启用')
    created_at = Column(DateTime, server_default=func.now())
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now())

    # 关系
    parent = relationship("FieldGroup", remote_side=[id], backref="children")
    standard_fields = relationship("StandardField", back_populates="field_group")
    custom_fields = relationship("CustomField", back_populates="field_group")

