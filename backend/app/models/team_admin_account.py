"""小组管理员账号模型"""
from sqlalchemy import Column, Integer, BigInteger, String, Boolean, DateTime, ForeignKey, Text
from sqlalchemy.orm import relationship
from sqlalchemy.sql import func
from app.core.database import Base


class TeamAdminAccount(Base):
    """小组管理员账号表"""
    __tablename__ = "team_admin_accounts"

    id = Column(BigInteger, primary_key=True, index=True, autoincrement=True)
    tenant_id = Column(BigInteger, ForeignKey("tenants.id"), nullable=False, index=True, comment="所属甲方ID")
    agency_id = Column(BigInteger, ForeignKey("collection_agencies.id"), nullable=False, index=True, comment="所属机构ID")
    team_group_id = Column(BigInteger, ForeignKey("team_groups.id"), index=True, comment="所属小组群ID")
    team_id = Column(BigInteger, ForeignKey("collection_teams.id"), index=True, comment="所属小组ID")
    account_code = Column(String(100), nullable=False, unique=True, index=True, comment="账号编码")
    account_name = Column(String(100), nullable=False, comment="账号名称")
    login_id = Column(String(100), unique=True, nullable=False, index=True, comment="登录ID")
    password_hash = Column(String(255), nullable=False, comment="密码哈希")
    role = Column(String(50), nullable=False, index=True, comment="角色：spv/team_leader/quality_inspector/statistician")
    mobile = Column(String(50), comment="手机号码")
    email = Column(String(100), comment="邮箱")
    remark = Column(Text, comment="备注")
    is_active = Column(Boolean, default=True, index=True, comment="是否启用")
    created_at = Column(DateTime, server_default=func.now(), comment="创建时间")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), comment="更新时间")

    # 关系
    tenant = relationship("Tenant", backref="team_admin_accounts")
    agency = relationship("CollectionAgency", backref="team_admin_accounts")
    team_group = relationship("TeamGroup", foreign_keys=[team_group_id], back_populates="spv_accounts")
    team = relationship("CollectionTeam", backref="team_admin_accounts")

    class Config:
        orm_mode = True

