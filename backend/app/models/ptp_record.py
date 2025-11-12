"""PTP（承诺还款）记录模型"""
from sqlalchemy import Column, BigInteger, Numeric, Date, DateTime, ForeignKey, Text, Enum as SQLEnum
from sqlalchemy.sql import func
from sqlalchemy.orm import relationship
from app.core.database import Base
import enum


class PTPStatusEnum(str, enum.Enum):
    """PTP状态枚举"""
    PENDING = "pending"  # 待履约（未到承诺日期）
    FULFILLED = "fulfilled"  # 已履约
    BROKEN = "broken"  # 已违约（超过承诺日期未还款）
    CANCELLED = "cancelled"  # 已取消


class PTPRecord(Base):
    """PTP记录表"""
    __tablename__ = "ptp_records"

    id = Column(BigInteger, primary_key=True, autoincrement=True)
    case_id = Column(BigInteger, ForeignKey('cases.id'), nullable=False, index=True, comment='案件ID')
    collector_id = Column(BigInteger, ForeignKey('collectors.id'), nullable=False, index=True, comment='催员ID')
    communication_id = Column(BigInteger, ForeignKey('communication_records.id'), index=True, comment='关联的通信记录ID')
    
    # PTP信息
    ptp_amount = Column(Numeric(15, 2), nullable=False, comment='承诺还款金额')
    ptp_date = Column(Date, nullable=False, index=True, comment='承诺还款日期')
    status = Column(SQLEnum(PTPStatusEnum), nullable=False, default=PTPStatusEnum.PENDING, index=True, comment='PTP状态')
    
    # 履约信息
    actual_payment_amount = Column(Numeric(15, 2), comment='实际还款金额')
    actual_payment_date = Column(Date, comment='实际还款日期')
    fulfillment_rate = Column(Numeric(5, 2), comment='履约率（实际金额/承诺金额 * 100）')
    
    # 备注
    remark = Column(Text, comment='备注')
    
    # 时间戳
    created_at = Column(DateTime, server_default=func.now(), comment='创建时间')
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), comment='更新时间')
    fulfilled_at = Column(DateTime, comment='履约时间')

    # 关系
    case = relationship("Case", back_populates="ptp_records")
    collector = relationship("Collector", back_populates="ptp_records")
    communication = relationship("CommunicationRecord", back_populates="ptp_records")

    class Config:
        orm_mode = True

