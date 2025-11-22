"""Infinity外呼系统相关Schema"""
from pydantic import BaseModel, Field, validator
from typing import Optional, List
from datetime import datetime
from app.models.infinity_extension_pool import ExtensionStatusEnum


# ==================== Infinity 配置相关 Schema ====================

class InfinityCallConfigBase(BaseModel):
    """Infinity外呼配置基础Schema"""
    tenant_id: int = Field(..., description="所属甲方ID")
    supplier_id: Optional[int] = Field(None, description="关联的渠道供应商ID")
    api_url: str = Field(..., max_length=500, description="Infinity API地址")
    access_token: str = Field(..., max_length=500, description="API访问令牌")
    app_id: str = Field(..., max_length=100, description="应用ID（必填）")
    caller_number_range_start: Optional[str] = Field(None, max_length=50, description="号段起始")
    caller_number_range_end: Optional[str] = Field(None, max_length=50, description="号段结束")
    callback_url: Optional[str] = Field(None, max_length=500, description="通话记录回调地址")
    recording_callback_url: Optional[str] = Field(None, max_length=500, description="录音回调地址")
    max_concurrent_calls: int = Field(100, ge=1, le=1000, description="最大并发呼叫数")
    call_timeout_seconds: int = Field(60, ge=10, le=300, description="呼叫超时时间（秒）")
    is_active: bool = Field(True, description="是否启用")


class InfinityCallConfigCreate(InfinityCallConfigBase):
    """创建Infinity外呼配置"""
    pass


class InfinityCallConfigUpdate(BaseModel):
    """更新Infinity外呼配置"""
    supplier_id: Optional[int] = None
    api_url: Optional[str] = Field(None, max_length=500)
    access_token: Optional[str] = Field(None, max_length=500)
    app_id: Optional[str] = Field(None, max_length=100, description="应用ID")
    caller_number_range_start: Optional[str] = Field(None, max_length=50, description="号段起始")
    caller_number_range_end: Optional[str] = Field(None, max_length=50, description="号段结束")
    callback_url: Optional[str] = Field(None, max_length=500)
    recording_callback_url: Optional[str] = Field(None, max_length=500)
    max_concurrent_calls: Optional[int] = Field(None, ge=1, le=1000)
    call_timeout_seconds: Optional[int] = Field(None, ge=10, le=300)
    is_active: Optional[bool] = None


class InfinityCallConfigResponse(InfinityCallConfigBase):
    """Infinity外呼配置响应"""
    id: int
    created_at: datetime
    updated_at: datetime
    created_by: Optional[int]

    class Config:
        from_attributes = True


# ==================== 分机池相关 Schema ====================

class ExtensionPoolBase(BaseModel):
    """分机池基础Schema"""
    tenant_id: int = Field(..., description="所属甲方ID")
    config_id: int = Field(..., description="关联的 Infinity 配置ID")
    infinity_extension_number: str = Field(..., max_length=50, description="分机号")
    status: ExtensionStatusEnum = Field(ExtensionStatusEnum.AVAILABLE, description="状态")


class ExtensionPoolCreate(ExtensionPoolBase):
    """创建分机"""
    pass


class ExtensionPoolBatchImport(BaseModel):
    """批量导入分机"""
    tenant_id: int = Field(..., description="所属甲方ID")
    config_id: int = Field(..., description="关联的 Infinity 配置ID")
    extension_numbers: List[str] = Field(..., min_items=1, description="分机号列表")

    @validator('extension_numbers')
    def validate_extensions(cls, v):
        """验证分机号格式"""
        if not v:
            raise ValueError("分机号列表不能为空")
        # 去重
        unique_extensions = list(set(v))
        if len(unique_extensions) != len(v):
            raise ValueError("分机号列表中存在重复")
        return unique_extensions


class ExtensionPoolUpdate(BaseModel):
    """更新分机"""
    status: Optional[ExtensionStatusEnum] = None
    infinity_extension_number: Optional[str] = Field(None, max_length=50)


class ExtensionPoolResponse(ExtensionPoolBase):
    """分机响应"""
    id: int
    current_collector_id: Optional[int]
    assigned_at: Optional[datetime]
    released_at: Optional[datetime]
    last_used_at: Optional[datetime]
    created_at: datetime
    updated_at: datetime

    class Config:
        from_attributes = True


class ExtensionPoolStatistics(BaseModel):
    """分机池统计信息"""
    tenant_id: int
    config_id: int
    total_extensions: int = Field(..., description="总分机数")
    available_count: int = Field(..., description="空闲数量")
    in_use_count: int = Field(..., description="使用中数量")
    offline_count: int = Field(..., description="离线数量")
    usage_rate: float = Field(..., description="使用率（百分比）")


# ==================== 外呼相关 Schema ====================

class MakeCallRequest(BaseModel):
    """发起外呼请求"""
    case_id: int = Field(..., description="案件ID")
    collector_id: int = Field(..., description="催员ID")
    contact_number: str = Field(..., max_length=50, description="客户号码")
    caller_number: Optional[str] = Field(None, max_length=50, description="主叫显示号码（可选，留空则使用默认）")
    custom_params: Optional[dict] = Field(None, description="自定义参数（如 userid、memberid）")


class MakeCallResponse(BaseModel):
    """发起外呼响应"""
    success: bool = Field(..., description="是否成功")
    call_id: Optional[int] = Field(None, description="通信记录ID")
    call_uuid: Optional[str] = Field(None, description="Infinity返回的通话UUID")
    extension_number: Optional[str] = Field(None, description="使用的分机号")
    message: str = Field(..., description="响应消息")


class CallRecordCallback(BaseModel):
    """Infinity回调 - 通话记录"""
    call_uuid: str = Field(..., description="通话唯一标识")
    call_duration: Optional[int] = Field(None, description="通话时长（秒）")
    is_connected: bool = Field(..., description="是否接通")
    call_record_url: Optional[str] = Field(None, description="录音链接")
    contact_result: Optional[str] = Field(None, description="联系结果")
    remark: Optional[str] = Field(None, description="备注")
    custom_params: Optional[dict] = Field(None, description="自定义参数")


class TestConnectionRequest(BaseModel):
    """测试连接请求"""
    api_url: str = Field(..., description="API地址")
    access_token: str = Field(..., description="访问令牌")


class TestConnectionResponse(BaseModel):
    """测试连接响应"""
    success: bool
    message: str
    response_time_ms: Optional[int] = None

