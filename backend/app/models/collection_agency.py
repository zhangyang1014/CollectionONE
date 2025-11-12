"""催收机构模型"""
from sqlalchemy import Column, BigInteger, String, Integer, Boolean, DateTime, ForeignKey, Text
from sqlalchemy.orm import relationship
from sqlalchemy.sql import func
from app.core.database import Base


class CollectionAgency(Base):
    """催收机构表"""
    __tablename__ = "collection_agencies"

    id = Column(Integer, primary_key=True, index=True, autoincrement=True)
    tenant_id = Column(BigInteger, ForeignKey("tenants.id"), nullable=False, index=True, comment="所属甲方ID")
    agency_code = Column(String(100), nullable=False, comment="机构编码")
    agency_name = Column(String(200), nullable=False, comment="机构名称")
    agency_name_en = Column(String(200), comment="机构名称（英文）")
    contact_person = Column(String(100), comment="联系人")
    contact_phone = Column(String(50), comment="联系电话")
    contact_email = Column(String(100), comment="联系邮箱")
    address = Column(Text, comment="机构地址")
    description = Column(Text, comment="机构描述")
    timezone = Column(Integer, comment="时区偏移量（UTC偏移，范围-12到+14）")
    agency_type = Column(String(20), default='real', comment="机构类型：real=真实机构，virtual=虚拟机构")
    sort_order = Column(Integer, default=0, comment="排序顺序")
    is_active = Column(Boolean, default=True, index=True, comment="是否启用")
    created_at = Column(DateTime, server_default=func.now(), comment="创建时间")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), comment="更新时间")

    # 关系
    tenant = relationship("Tenant", back_populates="agencies")
    teams = relationship("CollectionTeam", back_populates="agency", cascade="all, delete-orphan")
    collectors = relationship("Collector", back_populates="agency")
    cases = relationship("Case", back_populates="agency")
    working_hours = relationship("AgencyWorkingHours", back_populates="agency", cascade="all, delete-orphan")
    # 新增关系
    performance_stats = relationship("CollectorPerformanceStat", back_populates="agency")

    class Config:
        orm_mode = True

