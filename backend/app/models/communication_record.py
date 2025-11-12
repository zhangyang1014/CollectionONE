"""通信记录模型"""
from sqlalchemy import Column, BigInteger, String, Integer, DateTime, ForeignKey, Text, Boolean, Enum as SQLEnum
from sqlalchemy.sql import func
from sqlalchemy.orm import relationship
from app.core.database import Base
import enum


class ChannelEnum(str, enum.Enum):
    """通信渠道枚举"""
    PHONE = "phone"
    WHATSAPP = "whatsapp"
    SMS = "sms"
    RCS = "rcs"


class DirectionEnum(str, enum.Enum):
    """通信方向枚举"""
    INBOUND = "inbound"  # 呼入/收到消息
    OUTBOUND = "outbound"  # 呼出/发送消息


class ContactResultEnum(str, enum.Enum):
    """联系结果枚举"""
    CONNECTED = "connected"  # 接通
    NOT_CONNECTED = "not_connected"  # 未接通
    BUSY = "busy"  # 忙线
    NO_ANSWER = "no_answer"  # 无应答
    REPLIED = "replied"  # 已回复（消息类）
    NOT_REPLIED = "not_replied"  # 未回复（消息类）
    PROMISE_TO_PAY = "promise_to_pay"  # 承诺还款
    REFUSED = "refused"  # 拒绝
    INVALID_NUMBER = "invalid_number"  # 无效号码


class CommunicationRecord(Base):
    """通信记录表"""
    __tablename__ = "communication_records"

    id = Column(BigInteger, primary_key=True, autoincrement=True)
    case_id = Column(BigInteger, ForeignKey('cases.id'), nullable=False, index=True, comment='案件ID')
    collector_id = Column(BigInteger, ForeignKey('collectors.id'), nullable=False, index=True, comment='催员ID')
    contact_person_id = Column(BigInteger, ForeignKey('case_contacts.id'), index=True, comment='联系人ID（本人或其他联系人）')
    
    # 渠道信息
    channel = Column(SQLEnum(ChannelEnum), nullable=False, index=True, comment='通信渠道')
    direction = Column(SQLEnum(DirectionEnum), nullable=False, comment='通信方向')
    
    # 电话专属字段
    call_duration = Column(Integer, comment='通话时长（秒）')
    is_connected = Column(Boolean, comment='是否接通')
    call_record_url = Column(String(500), comment='录音链接')
    
    # 消息专属字段（WhatsApp/SMS/RCS）
    is_replied = Column(Boolean, comment='是否回复（消息类通信）')
    message_content = Column(Text, comment='消息内容')
    
    # 结果信息
    contact_result = Column(SQLEnum(ContactResultEnum), index=True, comment='联系结果')
    ttfc_seconds = Column(Integer, comment='首次触达时长（秒，从案件分配到首次有效触达）')
    remark = Column(Text, comment='备注')
    
    # 时间戳
    contacted_at = Column(DateTime, nullable=False, index=True, comment='触达时间')
    created_at = Column(DateTime, server_default=func.now(), comment='创建时间')
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), comment='更新时间')

    # 关系
    case = relationship("Case", back_populates="communication_records")
    collector = relationship("Collector", back_populates="communication_records")
    contact_person = relationship("CaseContact", back_populates="communication_records")
    ptp_records = relationship("PTPRecord", back_populates="communication")
    quality_inspections = relationship("QualityInspectionRecord", back_populates="communication")

    class Config:
        orm_mode = True

