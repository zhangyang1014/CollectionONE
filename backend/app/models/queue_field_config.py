"""队列字段配置模型"""
from sqlalchemy import Column, BigInteger, Enum, Integer, Boolean, DateTime, ForeignKey
from sqlalchemy.orm import relationship
from sqlalchemy.sql import func
from app.core.database import Base
import enum


class FieldTypeEnum(enum.Enum):
    """字段类型枚举"""
    standard = "standard"
    custom = "custom"


class QueueFieldConfig(Base):
    """队列字段配置表"""
    __tablename__ = "queue_field_configs"

    id = Column(BigInteger, primary_key=True, index=True, autoincrement=True)
    queue_id = Column(BigInteger, ForeignKey("case_queues.id"), nullable=False, index=True, comment="队列ID")
    field_id = Column(BigInteger, nullable=False, index=True, comment="字段ID")
    field_type = Column(Enum(FieldTypeEnum), nullable=False, comment="字段类型")
    is_visible = Column(Boolean, default=True, comment="是否可见")
    is_required = Column(Boolean, default=None, nullable=True, comment="是否必填（NULL表示使用字段默认设置）")
    is_readonly = Column(Boolean, default=False, comment="是否只读")
    is_editable = Column(Boolean, default=True, comment="是否可编辑")
    sort_order = Column(Integer, default=0, comment="字段排序")
    created_at = Column(DateTime, server_default=func.now(), comment="创建时间")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), comment="更新时间")

    # 关系
    queue = relationship("CaseQueue", back_populates="field_configs")

    class Config:
        orm_mode = True

