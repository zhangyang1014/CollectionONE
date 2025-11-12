"""案件分配历史模型"""
from sqlalchemy import Column, BigInteger, String, DateTime, ForeignKey, Text
from sqlalchemy.orm import relationship
from sqlalchemy.sql import func
from app.core.database import Base


class CaseAssignmentHistory(Base):
    """案件分配历史表"""
    __tablename__ = "case_assignment_history"

    id = Column(BigInteger, primary_key=True, index=True, autoincrement=True)
    case_id = Column(BigInteger, ForeignKey("cases.id"), nullable=False, index=True, comment="案件ID")
    assignment_type = Column(String(50), nullable=False, comment="分配类型：to_agency/to_team/to_collector/transfer")
    from_agency_id = Column(BigInteger, ForeignKey("collection_agencies.id"), comment="原机构ID")
    to_agency_id = Column(BigInteger, ForeignKey("collection_agencies.id"), index=True, comment="目标机构ID")
    from_team_id = Column(BigInteger, ForeignKey("collection_teams.id"), comment="原小组ID")
    to_team_id = Column(BigInteger, ForeignKey("collection_teams.id"), index=True, comment="目标小组ID")
    from_collector_id = Column(BigInteger, ForeignKey("collectors.id"), comment="原催员ID")
    to_collector_id = Column(BigInteger, ForeignKey("collectors.id"), index=True, comment="目标催员ID")
    reason = Column(String(200), comment="分配/流转原因")
    assigned_by = Column(BigInteger, comment="分配人ID")
    assigned_by_name = Column(String(100), comment="分配人姓名")
    assigned_at = Column(DateTime, server_default=func.now(), index=True, comment="分配时间")
    remarks = Column(Text, comment="备注")

    # 关系
    case = relationship("Case", back_populates="assignment_history")
    from_agency = relationship("CollectionAgency", foreign_keys=[from_agency_id])
    to_agency = relationship("CollectionAgency", foreign_keys=[to_agency_id])
    from_team = relationship("CollectionTeam", foreign_keys=[from_team_id])
    to_team = relationship("CollectionTeam", foreign_keys=[to_team_id])
    from_collector = relationship("Collector", foreign_keys=[from_collector_id])
    to_collector = relationship("Collector", foreign_keys=[to_collector_id])

    class Config:
        orm_mode = True

