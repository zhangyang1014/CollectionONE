from sqlalchemy import Column, BigInteger, String, DateTime, ForeignKey, Integer, Numeric
from sqlalchemy.sql import func
from sqlalchemy.orm import relationship
from app.core.database import Base


class Case(Base):
    """案件主表"""
    __tablename__ = "cases"

    id = Column(BigInteger, primary_key=True, autoincrement=True)
    case_code = Column(String(100), unique=True, nullable=False, index=True, comment='案件唯一标识')
    tenant_id = Column(BigInteger, ForeignKey('tenants.id'), nullable=False, index=True, comment='所属甲方ID')
    agency_id = Column(BigInteger, ForeignKey('collection_agencies.id'), index=True, comment='所属催收机构ID')
    team_id = Column(BigInteger, ForeignKey('collection_teams.id'), index=True, comment='所属催收小组ID')
    collector_id = Column(BigInteger, ForeignKey('collectors.id'), index=True, comment='分配催员ID')
    queue_id = Column(BigInteger, ForeignKey('case_queues.id'), index=True, comment='所属队列ID')
    user_id = Column(String(100), index=True, comment='用户编号')
    user_name = Column(String(100), comment='用户姓名')
    mobile = Column(String(50), comment='手机号')
    case_status = Column(String(50), index=True, comment='案件状态：pending_repayment/partial_repayment/normal_settlement/extension_settlement')
    overdue_days = Column(Integer, index=True, comment='逾期天数（用于自动分配队列）')
    loan_amount = Column(Numeric(15, 2), comment='贷款金额')
    repaid_amount = Column(Numeric(15, 2), default=0, comment='已还款金额')
    outstanding_amount = Column(Numeric(15, 2), comment='逾期金额')
    due_date = Column(DateTime, index=True, comment='到期日期')
    settlement_date = Column(DateTime, comment='结清日期')
    assigned_at = Column(DateTime, index=True, comment='分配时间')
    last_contact_at = Column(DateTime, comment='最后联系时间')
    next_follow_up_at = Column(DateTime, comment='下次跟进时间')
    created_at = Column(DateTime, server_default=func.now())
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now())

    # 关系
    tenant = relationship("Tenant", back_populates="cases")
    agency = relationship("CollectionAgency", back_populates="cases")
    team = relationship("CollectionTeam", back_populates="cases")
    collector = relationship("Collector", back_populates="cases")
    queue = relationship("CaseQueue", back_populates="cases")
    standard_field_values = relationship("CaseStandardFieldValue", back_populates="case", cascade="all, delete-orphan")
    custom_field_values = relationship("CaseCustomFieldValue", back_populates="case", cascade="all, delete-orphan")
    assignment_history = relationship("CaseAssignmentHistory", back_populates="case", cascade="all, delete-orphan")
    # 新增关系
    communication_records = relationship("CommunicationRecord", back_populates="case", cascade="all, delete-orphan")
    ptp_records = relationship("PTPRecord", back_populates="case", cascade="all, delete-orphan")
    quality_inspections = relationship("QualityInspectionRecord", back_populates="case", cascade="all, delete-orphan")
    contacts = relationship("CaseContact", back_populates="case", cascade="all, delete-orphan")


class CaseStandardFieldValue(Base):
    """标准字段值表"""
    __tablename__ = "case_standard_field_values"

    id = Column(BigInteger, primary_key=True, autoincrement=True)
    case_id = Column(BigInteger, ForeignKey('cases.id'), nullable=False, comment='案件ID')
    field_id = Column(BigInteger, ForeignKey('standard_fields.id'), nullable=False, comment='字段ID')
    field_value = Column(String(2000), comment='字段值（JSON格式存储复杂类型）')
    created_at = Column(DateTime, server_default=func.now())
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now())

    # 关系
    case = relationship("Case", back_populates="standard_field_values")


class CaseCustomFieldValue(Base):
    """自定义字段值表"""
    __tablename__ = "case_custom_field_values"

    id = Column(BigInteger, primary_key=True, autoincrement=True)
    case_id = Column(BigInteger, ForeignKey('cases.id'), nullable=False, comment='案件ID')
    field_id = Column(BigInteger, ForeignKey('custom_fields.id'), nullable=False, comment='自定义字段ID')
    field_value = Column(String(2000), comment='字段值（JSON格式存储复杂类型）')
    created_at = Column(DateTime, server_default=func.now())
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now())

    # 关系
    case = relationship("Case", back_populates="custom_field_values")

