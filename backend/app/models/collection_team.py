"""催收小组模型"""
from sqlalchemy import Column, BigInteger, String, Integer, Boolean, DateTime, ForeignKey, Text
from sqlalchemy.orm import relationship
from sqlalchemy.sql import func
from app.core.database import Base


class CollectionTeam(Base):
    """催收小组表"""
    __tablename__ = "collection_teams"

    id = Column(Integer, primary_key=True, index=True, autoincrement=True)
    tenant_id = Column(BigInteger, ForeignKey("tenants.id"), nullable=False, index=True, comment="所属甲方ID")
    agency_id = Column(BigInteger, ForeignKey("collection_agencies.id"), nullable=False, index=True, comment="所属催收机构ID")
    team_code = Column(String(100), nullable=False, comment="小组编码")
    team_name = Column(String(200), nullable=False, comment="小组名称")
    team_name_en = Column(String(200), comment="小组名称（英文）")
    team_leader_id = Column(BigInteger, ForeignKey("collectors.id"), comment="组长ID（催员ID）")
    team_type = Column(String(50), comment="小组类型（如：电催组、外访组、法务组等）")
    description = Column(Text, comment="小组描述")
    max_case_count = Column(Integer, default=0, comment="最大案件数量（0表示不限制）")
    sort_order = Column(Integer, default=0, comment="排序顺序")
    is_active = Column(Boolean, default=True, index=True, comment="是否启用")
    created_at = Column(DateTime, server_default=func.now(), comment="创建时间")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), comment="更新时间")

    # 关系
    tenant = relationship("Tenant", back_populates="teams")
    agency = relationship("CollectionAgency", back_populates="teams")
    collectors = relationship("Collector", back_populates="team", foreign_keys="Collector.team_id")
    team_leader = relationship("Collector", foreign_keys=[team_leader_id], post_update=True)
    cases = relationship("Case", back_populates="team")
    # 新增关系
    performance_stats = relationship("CollectorPerformanceStat", back_populates="team")

    class Config:
        orm_mode = True

