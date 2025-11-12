"""机构作息时间模型"""
from sqlalchemy import Column, BigInteger, Integer, DateTime, ForeignKey, JSON
from sqlalchemy.orm import relationship
from sqlalchemy.sql import func
from app.core.database import Base


class AgencyWorkingHours(Base):
    """机构作息时间表"""
    __tablename__ = "agency_working_hours"

    id = Column(BigInteger, primary_key=True, index=True, autoincrement=True)
    agency_id = Column(BigInteger, ForeignKey("collection_agencies.id"), nullable=False, index=True, comment="机构ID")
    day_of_week = Column(Integer, nullable=False, comment="星期几（0=周一，6=周日）")
    time_slots = Column(JSON, nullable=False, comment="时间段列表，格式：[{\"start\": \"09:00\", \"end\": \"12:00\"}, ...]")
    created_at = Column(DateTime, server_default=func.now(), comment="创建时间")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), comment="更新时间")

    # 关系
    agency = relationship("CollectionAgency", back_populates="working_hours")

    class Config:
        orm_mode = True

