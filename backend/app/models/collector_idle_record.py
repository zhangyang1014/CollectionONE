"""
催员空闲记录模型
"""
from sqlalchemy import Column, BigInteger, String, Integer, Date, DateTime, JSON, Text, Numeric
from sqlalchemy.sql import func
from app.core.database import Base


class CollectorIdleRecord(Base):
    """催员空闲记录表"""
    
    __tablename__ = "collector_idle_records"
    
    # 主键
    id = Column(BigInteger, primary_key=True, index=True, comment="主键ID")
    
    # 基本信息
    tenant_id = Column(BigInteger, nullable=False, index=True, comment="甲方ID")
    collector_id = Column(BigInteger, nullable=False, index=True, comment="催员ID")
    agency_id = Column(BigInteger, nullable=False, index=True, comment="机构ID")
    team_id = Column(BigInteger, nullable=False, index=True, comment="小组ID")
    
    # 空闲信息
    idle_date = Column(Date, nullable=False, index=True, comment="空闲日期")
    idle_start_time = Column(DateTime(timezone=True), nullable=False, comment="空闲开始时间")
    idle_end_time = Column(DateTime(timezone=True), nullable=False, comment="空闲结束时间")
    idle_duration_minutes = Column(Integer, nullable=False, comment="空闲时长（分钟）")
    
    # 上下文行为
    before_action = Column(JSON, nullable=True, comment="空闲前的行为，格式: {'type':'call','time':'...','details':'...'}")
    after_action = Column(JSON, nullable=True, comment="空闲后的行为，格式: {'type':'login','time':'...','details':'...'}")
    
    # 配置引用
    config_id = Column(BigInteger, nullable=False, comment="应用的配置ID")
    
    # 审计字段
    created_at = Column(DateTime(timezone=True), nullable=False, server_default=func.now(), comment="创建时间")
    
    def __repr__(self):
        return f"<CollectorIdleRecord(id={self.id}, collector_id={self.collector_id}, idle_date={self.idle_date}, duration={self.idle_duration_minutes})>"


class CollectorIdleStats(Base):
    """催员空闲统计表"""
    
    __tablename__ = "collector_idle_stats"
    
    # 主键
    id = Column(BigInteger, primary_key=True, index=True, comment="主键ID")
    
    # 基本信息
    tenant_id = Column(BigInteger, nullable=False, index=True, comment="甲方ID")
    collector_id = Column(BigInteger, nullable=False, index=True, comment="催员ID")
    agency_id = Column(BigInteger, nullable=False, index=True, comment="机构ID")
    team_id = Column(BigInteger, nullable=False, index=True, comment="小组ID")
    
    # 统计信息
    stat_date = Column(Date, nullable=False, index=True, comment="统计日期")
    
    # 空闲统计
    idle_count = Column(Integer, nullable=False, default=0, comment="空闲次数")
    total_idle_minutes = Column(Integer, nullable=False, default=0, comment="总空闲时长（分钟）")
    longest_idle_minutes = Column(Integer, nullable=False, default=0, comment="最长单次空闲（分钟）")
    avg_idle_minutes = Column(Numeric(10, 2), nullable=False, default=0, comment="平均空闲时长（分钟）")
    
    # 工作统计
    work_minutes = Column(Integer, nullable=False, default=0, comment="工作时长（分钟）")
    idle_rate = Column(Numeric(5, 4), nullable=False, default=0, comment="空闲率")
    
    # 案件管理情况
    managed_cases_total = Column(Integer, nullable=False, default=0, comment="管理案件总数")
    managed_cases_collected = Column(Integer, nullable=False, default=0, comment="已还案件数")
    managed_amount_total = Column(Numeric(15, 2), nullable=False, default=0, comment="管理金额总计")
    managed_amount_collected = Column(Numeric(15, 2), nullable=False, default=0, comment="已还金额")
    
    # 审计字段
    created_at = Column(DateTime(timezone=True), nullable=False, server_default=func.now(), comment="创建时间")
    updated_at = Column(DateTime(timezone=True), nullable=False, server_default=func.now(), onupdate=func.now(), comment="更新时间")
    
    def __repr__(self):
        return f"<CollectorIdleStats(id={self.id}, collector_id={self.collector_id}, stat_date={self.stat_date}, idle_count={self.idle_count})>"

