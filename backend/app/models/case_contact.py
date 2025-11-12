"""案件联系人模型"""
from sqlalchemy import Column, BigInteger, String, Boolean, DateTime, ForeignKey, JSON
from sqlalchemy.sql import func
from sqlalchemy.orm import relationship
from app.core.database import Base


class CaseContact(Base):
    """案件联系人表"""
    __tablename__ = "case_contacts"

    id = Column(BigInteger, primary_key=True, autoincrement=True)
    case_id = Column(BigInteger, ForeignKey('cases.id'), nullable=False, index=True, comment='案件ID')
    
    # 联系人信息
    contact_name = Column(String(100), nullable=False, comment='联系人姓名')
    phone_number = Column(String(50), nullable=False, comment='联系电话')
    relation = Column(String(50), nullable=False, comment='关系（本人/配偶/朋友/同事/家人等）')
    is_primary = Column(Boolean, default=False, index=True, comment='是否本人')
    
    # 可用渠道（JSON数组格式）
    available_channels = Column(JSON, comment='可用通信渠道，格式：["whatsapp", "sms", "call"]')
    
    # 备注
    remark = Column(String(500), comment='备注')
    
    # 时间戳
    created_at = Column(DateTime, server_default=func.now(), comment='创建时间')
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), comment='更新时间')

    # 关系
    case = relationship("Case", back_populates="contacts")
    communication_records = relationship("CommunicationRecord", back_populates="contact_person")

    class Config:
        orm_mode = True

