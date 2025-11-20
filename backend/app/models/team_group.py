"""小组群模型"""
from sqlalchemy import Column, BigInteger, String, Integer, Boolean, DateTime, ForeignKey, Text
from sqlalchemy.orm import relationship
from sqlalchemy.sql import func
from app.core.database import Base


class TeamGroup(Base):
    """小组群表 - 介于机构和小组之间的管理层级"""
    __tablename__ = "team_groups"

    id = Column(BigInteger, primary_key=True, index=True, autoincrement=True)
    tenant_id = Column(BigInteger, ForeignKey("tenants.id"), nullable=False, index=True, comment="所属甲方ID")
    agency_id = Column(BigInteger, ForeignKey("collection_agencies.id"), nullable=False, index=True, comment="所属催收机构ID")
    group_code = Column(String(100), nullable=False, comment="小组群编码")
    group_name = Column(String(200), nullable=False, comment="小组群名称")
    group_name_en = Column(String(200), comment="小组群名称（英文）")
    spv_id = Column(BigInteger, ForeignKey("collectors.id"), comment="小组群长SPV ID（催员ID）")
    description = Column(Text, comment="小组群描述")
    sort_order = Column(Integer, default=0, comment="排序顺序")
    is_active = Column(Boolean, default=True, index=True, comment="是否启用")
    created_at = Column(DateTime, server_default=func.now(), comment="创建时间")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), comment="更新时间")

    # 关系
    tenant = relationship("Tenant", back_populates="team_groups")
    agency = relationship("CollectionAgency", back_populates="team_groups")
    teams = relationship("CollectionTeam", back_populates="team_group", foreign_keys="CollectionTeam.team_group_id")
    spv = relationship("Collector", foreign_keys=[spv_id], post_update=True)
    spv_accounts = relationship("TeamAdminAccount", foreign_keys="TeamAdminAccount.team_group_id", back_populates="team_group")

    class Config:
        orm_mode = True

