"""通知配置相关 Schemas"""
from pydantic import BaseModel, Field
from typing import Optional, List, Dict, Any
from datetime import datetime


class NotificationConfigBase(BaseModel):
    """通知配置基础Schema"""
    tenant_id: Optional[int] = Field(None, description="甲方ID（NULL表示全局配置）")
    notification_type: str = Field(..., description="通知类型：unreplied/nudge/case_update/performance/timeout")
    is_enabled: bool = Field(True, description="是否启用")
    config_data: Dict[str, Any] = Field(..., description="配置数据（JSON格式）")


class NotificationConfigCreate(NotificationConfigBase):
    """创建通知配置"""
    pass


class NotificationConfigUpdate(BaseModel):
    """更新通知配置"""
    is_enabled: Optional[bool] = None
    config_data: Optional[Dict[str, Any]] = None


class NotificationConfigResponse(NotificationConfigBase):
    """通知配置响应"""
    id: int
    created_at: datetime
    updated_at: datetime

    class Config:
        from_attributes = True


# ===== 公共通知 Schemas =====
class PublicNotificationBase(BaseModel):
    """公共通知基础Schema"""
    tenant_id: Optional[int] = Field(None, description="甲方ID（NULL表示全局通知）")
    agency_id: Optional[int] = Field(None, description="机构ID（NULL表示甲方级别通知）")
    title: str = Field(..., max_length=200, description="通知标题")
    h5_content: str = Field(..., description="H5内容（可以是URL或HTML）")
    h5_content_type: str = Field('url', description="内容类型：url/html")
    carousel_interval_seconds: int = Field(30, ge=1, description="轮播间隔（秒）")
    is_forced_read: bool = Field(False, description="是否强制阅读")
    is_enabled: bool = Field(True, description="是否启用")
    effective_start_time: Optional[datetime] = Field(None, description="生效开始时间")
    effective_end_time: Optional[datetime] = Field(None, description="生效结束时间")
    notify_roles: Optional[List[str]] = Field(None, description="通知对象角色列表")
    sort_order: int = Field(0, description="排序顺序")


class PublicNotificationCreate(PublicNotificationBase):
    """创建公共通知"""
    pass


class PublicNotificationUpdate(BaseModel):
    """更新公共通知"""
    title: Optional[str] = Field(None, max_length=200)
    h5_content: Optional[str] = None
    h5_content_type: Optional[str] = None
    carousel_interval_seconds: Optional[int] = Field(None, ge=1)
    is_forced_read: Optional[bool] = None
    is_enabled: Optional[bool] = None
    effective_start_time: Optional[datetime] = None
    effective_end_time: Optional[datetime] = None
    notify_roles: Optional[List[str]] = None
    sort_order: Optional[int] = None


class PublicNotificationResponse(PublicNotificationBase):
    """公共通知响应"""
    id: int
    created_at: datetime
    updated_at: datetime
    created_by: Optional[int] = None

    class Config:
        from_attributes = True


