"""催员绩效统计模型"""
from sqlalchemy import Column, BigInteger, Integer, Numeric, Date, DateTime, ForeignKey, JSON, Enum as SQLEnum
from sqlalchemy.sql import func
from sqlalchemy.orm import relationship
from app.core.database import Base
import enum


class StatPeriodEnum(str, enum.Enum):
    """统计周期枚举"""
    DAILY = "daily"  # 日统计
    WEEKLY = "weekly"  # 周统计
    MONTHLY = "monthly"  # 月统计


class CollectorPerformanceStat(Base):
    """催员绩效统计表"""
    __tablename__ = "collector_performance_stats"

    id = Column(BigInteger, primary_key=True, autoincrement=True)
    collector_id = Column(BigInteger, ForeignKey('collectors.id'), nullable=False, index=True, comment='催员ID')
    tenant_id = Column(BigInteger, ForeignKey('tenants.id'), nullable=False, index=True, comment='甲方ID')
    agency_id = Column(BigInteger, ForeignKey('collection_agencies.id'), nullable=False, index=True, comment='机构ID')
    team_id = Column(BigInteger, ForeignKey('collection_teams.id'), nullable=False, index=True, comment='小组ID')
    queue_ids = Column(JSON, comment='队列ID数组（JSON格式）')
    
    # 统计周期
    stat_date = Column(Date, nullable=False, index=True, comment='统计日期')
    stat_period = Column(SQLEnum(StatPeriodEnum), nullable=False, index=True, comment='统计周期')
    
    # 业绩总览
    assigned_cases = Column(Integer, default=0, comment='应催案件数')
    collected_cases = Column(Integer, default=0, comment='收回案件数')
    case_collection_rate = Column(Numeric(5, 2), comment='案件催回率（%）')
    assigned_amount = Column(Numeric(15, 2), default=0, comment='应催金额')
    collected_amount = Column(Numeric(15, 2), default=0, comment='收回金额')
    amount_collection_rate = Column(Numeric(5, 2), comment='金额催回率（%）')
    
    # PTP指标
    ptp_count = Column(Integer, default=0, comment='PTP数量')
    ptp_amount = Column(Numeric(15, 2), default=0, comment='PTP金额')
    ptp_fulfilled_count = Column(Integer, default=0, comment='PTP履约数量')
    ptp_fulfillment_rate = Column(Numeric(5, 2), comment='PTP履约率（%）')
    
    # 沟通覆盖
    self_contact_rate = Column(Numeric(5, 2), comment='本人沟通率（%）')
    total_contact_rate = Column(Numeric(5, 2), comment='总联系人沟通率（%）')
    
    # 电话指标
    self_call_rate = Column(Numeric(5, 2), comment='本人电话拨打率（%）')
    total_call_rate = Column(Numeric(5, 2), comment='总联系人电话拨打率（%）')
    self_connected_rate = Column(Numeric(5, 2), comment='本人电话接通率（%）')
    
    # WhatsApp指标
    self_wa_contact_rate = Column(Numeric(5, 2), comment='本人WhatsApp联系率（%）')
    total_wa_contact_rate = Column(Numeric(5, 2), comment='总联系人WhatsApp联系率（%）')
    self_wa_reply_rate = Column(Numeric(5, 2), comment='本人WhatsApp回复率（%）')
    total_wa_reply_rate = Column(Numeric(5, 2), comment='总联系人WhatsApp回复率（%）')
    
    # 首次触达时长（TTFC）
    ttfc_median = Column(Integer, comment='TTFC中位数（秒）')
    ttfc_distribution = Column(JSON, comment='TTFC分箱分布JSON，格式：{"0-1h": 10, "1-3h": 15, "3-6h": 8, ...}')
    
    # 质检合规
    self_number_view_rate = Column(Numeric(5, 2), comment='本人号码查看率（%）')
    quality_avg_score = Column(Numeric(5, 2), comment='平均质检得分')
    script_avg_compliance_rate = Column(Numeric(5, 2), comment='平均脚本命中率（%）')
    violation_count = Column(Integer, default=0, comment='违规条目数')
    
    # 时间戳
    created_at = Column(DateTime, server_default=func.now(), comment='创建时间')
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), comment='更新时间')

    # 关系
    collector = relationship("Collector", back_populates="performance_stats")
    tenant = relationship("Tenant", back_populates="performance_stats")
    agency = relationship("CollectionAgency", back_populates="performance_stats")
    team = relationship("CollectionTeam", back_populates="performance_stats")

    class Config:
        orm_mode = True

