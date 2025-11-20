"""公共通知模型"""
from sqlalchemy import Column, Integer, BigInteger, String, Boolean, DateTime, Text
from sqlalchemy.sql import func
from app.core.database import Base


class PublicNotification(Base):
    """公共通知表"""
    __tablename__ = "public_notifications"

    id = Column(BigInteger, primary_key=True, index=True, autoincrement=True)
    tenant_id = Column(Integer, nullable=True, index=True, comment="甲方ID（NULL表示全局通知）")
    agency_id = Column(Integer, nullable=True, index=True, comment="机构ID（NULL表示甲方级别通知）")
    title = Column(String(200), nullable=False, comment="通知标题")
    content = Column(Text, nullable=False, comment="通知正文内容")
    h5_content = Column(Text, nullable=True, comment="H5链接地址（可选）")
    carousel_interval_seconds = Column(Integer, default=30, comment="轮播间隔（秒）")
    is_forced_read = Column(Boolean, default=False, comment="是否强制阅读")
    is_enabled = Column(Boolean, default=True, comment="是否启用")
    
    # 非强制阅读时的配置
    repeat_interval_minutes = Column(Integer, nullable=True, comment="重复提醒时间间隔（分钟）")
    max_remind_count = Column(Integer, nullable=True, comment="最大提醒次数")
    notify_time_start = Column(String(5), nullable=True, comment="通知时间范围开始（HH:MM）")
    notify_time_end = Column(String(5), nullable=True, comment="通知时间范围结束（HH:MM）")
    
    effective_start_time = Column(DateTime, nullable=True, comment="生效开始时间")
    effective_end_time = Column(DateTime, nullable=True, comment="生效结束时间")
    notify_roles = Column(Text, nullable=True, comment="通知对象角色列表（JSON字符串）")
    sort_order = Column(Integer, default=0, comment="排序顺序")
    created_at = Column(DateTime, server_default=func.now(), comment="创建时间")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), comment="更新时间")
    created_by = Column(Integer, nullable=True, comment="创建人ID")

    class Config:
        orm_mode = True

