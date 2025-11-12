from sqlalchemy import Column, BigInteger, String, DateTime, ForeignKey, JSON
from sqlalchemy.sql import func
from sqlalchemy.orm import relationship
from app.core.database import Base


class FieldDependency(Base):
    """字段联动规则表"""
    __tablename__ = "field_dependencies"

    id = Column(BigInteger, primary_key=True, autoincrement=True)
    source_field_id = Column(BigInteger, ForeignKey('standard_fields.id'), nullable=False, comment='源字段ID（触发字段）')
    target_field_id = Column(BigInteger, ForeignKey('standard_fields.id'), nullable=False, comment='目标字段ID（被联动字段）')
    dependency_type = Column(String(50), nullable=False, comment='联动类型：show/hide/options_change')
    dependency_rule = Column(JSON, nullable=False, comment='联动规则（JSON格式）')
    created_at = Column(DateTime, server_default=func.now())
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now())

    # 关系
    source_field = relationship(
        "StandardField",
        foreign_keys=[source_field_id],
        back_populates="source_dependencies"
    )
    target_field = relationship(
        "StandardField",
        foreign_keys=[target_field_id],
        back_populates="target_dependencies"
    )

