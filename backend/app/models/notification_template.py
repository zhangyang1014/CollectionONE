"""通知模板模型"""
from sqlalchemy import Column, Integer, BigInteger, String, Boolean, DateTime, Text, JSON
from sqlalchemy.sql import func
from app.core.database import Base


class NotificationTemplate(Base):
    """通知模板表 - 用于接收甲方核心系统推送的通知"""
    __tablename__ = "notification_templates"

    id = Column(BigInteger, primary_key=True, index=True, autoincrement=True)
    tenant_id = Column(Integer, nullable=True, index=True, comment="甲方ID（NULL表示全局模板）")
    
    # 模板基本信息
    template_id = Column(String(100), nullable=False, unique=True, index=True, comment="模板ID（唯一标识）")
    template_name = Column(String(200), nullable=False, comment="模板名称")
    template_type = Column(String(50), nullable=False, index=True, comment="模板类型：case_tag_change/case_payment/user_app_visit/user_payment_page_visit等")
    description = Column(Text, nullable=True, comment="模板描述")
    
    # 模板内容
    content_template = Column(Text, nullable=False, comment="通知正文模板，支持变量如：{case_id}、{amount}、{tag_name}等")
    
    # 跳转配置
    jump_url_template = Column(Text, nullable=True, comment="点击后跳转的URL模板，支持变量")
    
    # 发送对象配置
    target_type = Column(String(20), nullable=False, default="agency", comment="发送对象类型：agency/team/collector")
    target_agencies = Column(JSON, nullable=True, comment="目标机构ID列表（JSON数组）")
    target_teams = Column(JSON, nullable=True, comment="目标小组ID列表（JSON数组）")
    target_collectors = Column(JSON, nullable=True, comment="目标催员ID列表（JSON数组）")
    
    # 阅读机制配置
    is_forced_read = Column(Boolean, default=False, comment="是否强制阅读")
    repeat_interval_minutes = Column(Integer, nullable=True, comment="非强制阅读时的重复提醒间隔（分钟）")
    max_remind_count = Column(Integer, nullable=True, comment="非强制阅读时的最大提醒次数")
    notify_time_start = Column(String(5), nullable=True, comment="通知时间范围开始（HH:MM）")
    notify_time_end = Column(String(5), nullable=True, comment="通知时间范围结束（HH:MM）")
    
    # 优先级和展示
    priority = Column(String(20), default="medium", comment="优先级：high/medium/low")
    display_duration_seconds = Column(Integer, default=5, comment="展示时长（秒）")
    
    # 启用状态
    is_enabled = Column(Boolean, default=True, comment="是否启用")
    
    # 变量说明（用于前端展示可用变量）
    available_variables = Column(JSON, nullable=True, comment="可用变量列表及说明（JSON）")
    
    # 统计信息
    total_sent = Column(Integer, default=0, comment="累计发送次数")
    total_read = Column(Integer, default=0, comment="累计阅读次数")
    
    # 时间戳
    created_at = Column(DateTime, server_default=func.now(), comment="创建时间")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), comment="更新时间")
    created_by = Column(Integer, nullable=True, comment="创建人ID")

    class Config:
        orm_mode = True

