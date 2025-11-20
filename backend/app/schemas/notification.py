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
    content: str = Field(..., description="通知正文内容")
    h5_content: Optional[str] = Field(None, description="H5链接地址（可选）")
    carousel_interval_seconds: int = Field(30, ge=1, description="轮播间隔（秒）")
    is_forced_read: bool = Field(False, description="是否强制阅读")
    is_enabled: bool = Field(True, description="是否启用")
    
    # 非强制阅读时的配置
    repeat_interval_minutes: Optional[int] = Field(None, ge=1, description="重复提醒时间间隔（分钟）")
    max_remind_count: Optional[int] = Field(None, ge=1, description="最大提醒次数")
    notify_time_start: Optional[str] = Field(None, description="通知时间范围开始（HH:MM）")
    notify_time_end: Optional[str] = Field(None, description="通知时间范围结束（HH:MM）")
    
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
    content: Optional[str] = None
    h5_content: Optional[str] = None
    carousel_interval_seconds: Optional[int] = Field(None, ge=1)
    is_forced_read: Optional[bool] = None
    is_enabled: Optional[bool] = None
    
    # 非强制阅读时的配置
    repeat_interval_minutes: Optional[int] = Field(None, ge=1)
    max_remind_count: Optional[int] = Field(None, ge=1)
    notify_time_start: Optional[str] = None
    notify_time_end: Optional[str] = None
    
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


# ===== 通知模板 Schemas =====
class NotificationTemplateBase(BaseModel):
    """通知模板基础Schema"""
    tenant_id: Optional[str] = Field(None, description="甲方ID（NULL表示全局模板）")
    template_id: str = Field(..., max_length=100, description="模板ID（唯一标识）")
    template_name: str = Field(..., max_length=200, description="模板名称")
    template_type: str = Field(..., max_length=50, description="模板类型")
    description: Optional[str] = Field(None, description="模板描述")
    content_template: str = Field(..., description="通知正文模板，支持变量")
    jump_url_template: Optional[str] = Field(None, description="跳转URL模板")
    
    # 发送对象配置
    target_type: str = Field("agency", description="发送对象类型：agency/team/collector")
    target_agencies: Optional[List[str]] = Field(None, description="目标机构ID列表")
    target_teams: Optional[List[str]] = Field(None, description="目标小组ID列表")
    target_collectors: Optional[List[str]] = Field(None, description="目标催员ID列表")
    
    # 阅读机制配置
    is_forced_read: bool = Field(False, description="是否强制阅读")
    repeat_interval_minutes: Optional[int] = Field(None, ge=1, description="重复提醒间隔（分钟）")
    max_remind_count: Optional[int] = Field(None, ge=1, description="最大提醒次数")
    notify_time_start: Optional[str] = Field(None, description="通知时间范围开始（HH:MM）")
    notify_time_end: Optional[str] = Field(None, description="通知时间范围结束（HH:MM）")
    
    # 优先级和展示
    priority: int = Field(2, ge=1, le=3, description="优先级：1=最高 2=中等 3=最低")
    display_duration_seconds: int = Field(5, ge=0, description="展示时长（秒），0表示不自动关闭")
    
    # 启用状态
    is_enabled: bool = Field(True, description="是否启用")
    
    # 可用变量
    available_variables: Optional[List[str]] = Field(None, description="可用变量列表")


class NotificationTemplateCreate(NotificationTemplateBase):
    """创建通知模板"""
    pass


class NotificationTemplateUpdate(BaseModel):
    """更新通知模板"""
    template_name: Optional[str] = Field(None, max_length=200)
    template_type: Optional[str] = Field(None, max_length=50)
    description: Optional[str] = None
    content_template: Optional[str] = None
    jump_url_template: Optional[str] = None
    
    target_type: Optional[str] = None
    target_agencies: Optional[List[str]] = None
    target_teams: Optional[List[str]] = None
    target_collectors: Optional[List[str]] = None
    
    is_forced_read: Optional[bool] = None
    repeat_interval_minutes: Optional[int] = Field(None, ge=1)
    max_remind_count: Optional[int] = Field(None, ge=1)
    notify_time_start: Optional[str] = None
    notify_time_end: Optional[str] = None
    
    priority: Optional[int] = Field(None, ge=1, le=3)
    display_duration_seconds: Optional[int] = Field(None, ge=0)
    is_enabled: Optional[bool] = None
    available_variables: Optional[List[str]] = None


class NotificationTemplateResponse(NotificationTemplateBase):
    """通知模板响应"""
    id: int
    total_sent: int
    total_read: int
    created_at: datetime
    updated_at: datetime
    created_by: Optional[int] = None

    class Config:
        from_attributes = True


