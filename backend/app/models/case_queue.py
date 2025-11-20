"""案件队列模型"""
from sqlalchemy import Column, BigInteger, String, Integer, Boolean, DateTime, ForeignKey, Text
from sqlalchemy.orm import relationship
from sqlalchemy.sql import func
from app.core.database import Base


class CaseQueue(Base):
    """案件队列表"""
    __tablename__ = "case_queues"

    id = Column(BigInteger, primary_key=True, index=True, autoincrement=True)
    tenant_id = Column(BigInteger, ForeignKey("tenants.id"), nullable=False, index=True, comment="所属甲方ID")
    queue_code = Column(String(100), nullable=False, comment="队列编码（如：M1, M2, M3+, LEGAL）")
    queue_name = Column(String(200), nullable=False, comment="队列名称")
    queue_name_en = Column(String(200), comment="队列名称（英文）")
    queue_description = Column(Text, comment="队列描述")
    overdue_days_start = Column(Integer, comment="逾期天数起始值（null表示负无穷）")
    overdue_days_end = Column(Integer, comment="逾期天数结束值（null表示正无穷）")
    sort_order = Column(Integer, default=0, index=True, comment="排序顺序")
    is_active = Column(Boolean, default=True, comment="是否启用")
    created_at = Column(DateTime, server_default=func.now(), comment="创建时间")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), comment="更新时间")

    # 关系
    tenant = relationship("Tenant", back_populates="queues")
    field_configs = relationship("QueueFieldConfig", back_populates="queue", cascade="all, delete-orphan")
    cases = relationship("Case", back_populates="queue")

    class Config:
        orm_mode = True

