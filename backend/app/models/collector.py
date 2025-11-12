"""催员模型"""
from sqlalchemy import Column, BigInteger, String, Integer, Boolean, DateTime, ForeignKey, Numeric, Date, JSON
from sqlalchemy.orm import relationship
from sqlalchemy.sql import func
from app.core.database import Base


class Collector(Base):
    """催员表"""
    __tablename__ = "collectors"

    id = Column(Integer, primary_key=True, index=True, autoincrement=True)
    tenant_id = Column(BigInteger, ForeignKey("tenants.id"), nullable=False, index=True, comment="所属甲方ID")
    agency_id = Column(BigInteger, ForeignKey("collection_agencies.id"), nullable=False, index=True, comment="所属机构ID")
    team_id = Column(BigInteger, ForeignKey("collection_teams.id"), nullable=False, index=True, comment="所属小组ID")
    collector_code = Column(String(100), nullable=False, index=True, comment="催员编码")
    collector_name = Column(String(100), nullable=False, comment="催员姓名")
    login_id = Column(String(100), unique=True, nullable=False, comment="登录ID")
    password_hash = Column(String(255), nullable=False, comment="密码哈希")
    mobile = Column(String(50), comment="手机号码")
    email = Column(String(100), comment="邮箱")
    employee_no = Column(String(50), comment="工号")
    collector_level = Column(String(50), comment="催员等级（初级/中级/高级/资深）")
    max_case_count = Column(Integer, default=100, comment="最大案件数量")
    current_case_count = Column(Integer, default=0, comment="当前案件数量")
    specialties = Column(JSON, comment="擅长领域（JSON数组，如：['高额案件','法务处理']）")
    performance_score = Column(Numeric(5, 2), comment="绩效评分")
    status = Column(String(50), default="active", index=True, comment="状态：active/休假/离职等")
    hire_date = Column(Date, comment="入职日期")
    is_active = Column(Boolean, default=True, index=True, comment="是否启用")
    last_login_at = Column(DateTime, comment="最后登录时间")
    created_at = Column(DateTime, server_default=func.now(), comment="创建时间")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), comment="更新时间")

    # 关系
    tenant = relationship("Tenant", back_populates="collectors")
    agency = relationship("CollectionAgency", back_populates="collectors")
    team = relationship("CollectionTeam", back_populates="collectors", foreign_keys=[team_id])
    cases = relationship("Case", back_populates="collector")
    assignment_history = relationship("CaseAssignmentHistory", foreign_keys="CaseAssignmentHistory.to_collector_id")
    # 新增关系
    communication_records = relationship("CommunicationRecord", back_populates="collector")
    ptp_records = relationship("PTPRecord", back_populates="collector")
    quality_inspections = relationship("QualityInspectionRecord", foreign_keys="QualityInspectionRecord.collector_id", back_populates="collector")
    conducted_inspections = relationship("QualityInspectionRecord", foreign_keys="QualityInspectionRecord.inspector_id", back_populates="inspector")
    performance_stats = relationship("CollectorPerformanceStat", back_populates="collector")
    custom_dimension_stats = relationship("CustomDimensionStat", back_populates="collector")

    class Config:
        orm_mode = True

