"""公共通知模型"""
from sqlalchemy import Column, Integer, String, Boolean, DateTime, Text
from sqlalchemy.sql import func
from app.core.database import Base


class PublicNotification(Base):
    """公共通知表"""
    __tablename__ = "public_notifications"

    id = Column(Integer, primary_key=True, index=True, autoincrement=True)
    tenant_id = Column(Integer, nullable=True, index=True, comment="甲方ID（NULL表示全局通知）")
    agency_id = Column(Integer, nullable=True, index=True, comment="机构ID（NULL表示甲方级别通知）")
    title = Column(String(200), nullable=False, comment="通知标题")
    h5_content = Column(Text, nullable=False, comment="H5内容（可以是URL或HTML）")
    h5_content_type = Column(String(20), default='url', comment="内容类型：url/html")
    carousel_interval_seconds = Column(Integer, default=30, comment="轮播间隔（秒）")
    is_forced_read = Column(Boolean, default=False, comment="是否强制阅读")
    is_enabled = Column(Boolean, default=True, comment="是否启用")
    effective_start_time = Column(DateTime, nullable=True, comment="生效开始时间")
    effective_end_time = Column(DateTime, nullable=True, comment="生效结束时间")
    notify_roles = Column(Text, nullable=True, comment="通知对象角色列表（JSON字符串）")
    sort_order = Column(Integer, default=0, comment="排序顺序")
    created_at = Column(DateTime, server_default=func.now(), comment="创建时间")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), comment="更新时间")
    created_by = Column(Integer, nullable=True, comment="创建人ID")

    class Config:
        orm_mode = True

