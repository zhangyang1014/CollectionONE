"""质检记录模型"""
from sqlalchemy import Column, BigInteger, Integer, DateTime, ForeignKey, Text, JSON, Enum as SQLEnum
from sqlalchemy.sql import func
from sqlalchemy.orm import relationship
from app.core.database import Base
import enum


class InspectionTypeEnum(str, enum.Enum):
    """质检类型枚举"""
    MANUAL = "manual"  # 人工质检
    AI = "ai"  # AI自动质检


class QualityInspectionRecord(Base):
    """质检记录表"""
    __tablename__ = "quality_inspection_records"

    id = Column(BigInteger, primary_key=True, autoincrement=True)
    case_id = Column(BigInteger, ForeignKey('cases.id'), nullable=False, index=True, comment='案件ID')
    collector_id = Column(BigInteger, ForeignKey('collectors.id'), nullable=False, index=True, comment='催员ID')
    communication_id = Column(BigInteger, ForeignKey('communication_records.id'), index=True, comment='关联的通信记录ID')
    
    # 质检人信息
    inspector_id = Column(BigInteger, ForeignKey('collectors.id'), comment='质检员ID（AI质检为null）')
    inspection_type = Column(SQLEnum(InspectionTypeEnum), nullable=False, index=True, comment='质检类型')
    
    # 质检结果
    quality_score = Column(Integer, comment='质检得分（0-100）')
    script_compliance_rate = Column(Integer, comment='脚本命中率（0-100）')
    
    # 违规和合规项（JSON格式）
    violations = Column(JSON, comment='违规条目JSON，格式：[{"type": "high_risk/general", "item": "违规内容", "description": "说明"}]')
    compliant_items = Column(JSON, comment='合规项JSON，格式：[{"item": "合规内容", "description": "说明"}]')
    
    # 反馈
    feedback = Column(Text, comment='质检反馈意见')
    
    # 时间戳
    inspected_at = Column(DateTime, nullable=False, index=True, comment='质检时间')
    created_at = Column(DateTime, server_default=func.now(), comment='创建时间')
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), comment='更新时间')

    # 关系
    case = relationship("Case", back_populates="quality_inspections")
    collector = relationship("Collector", foreign_keys=[collector_id], back_populates="quality_inspections")
    inspector = relationship("Collector", foreign_keys=[inspector_id], back_populates="conducted_inspections")
    communication = relationship("CommunicationRecord", back_populates="quality_inspections")

    class Config:
        orm_mode = True

