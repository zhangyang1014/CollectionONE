# -*- coding: utf-8 -*-
"""
还款相关的Schema定义
"""
from typing import Optional, Any, Dict
from datetime import datetime
from decimal import Decimal
from pydantic import BaseModel, Field, validator


# ==================== 还款渠道配置相关 ====================

class PaymentChannelBase(BaseModel):
    """还款渠道基础模型"""
    channel_name: str = Field(..., description="支付名称")
    channel_icon: Optional[str] = Field(None, description="图标URL")
    channel_type: str = Field(..., description="支付类型：VA/H5/QR")
    service_provider: Optional[str] = Field(None, description="服务公司")
    description: Optional[str] = Field(None, description="渠道描述")
    api_url: str = Field(..., description="API地址")
    api_method: str = Field("POST", description="请求方法")
    auth_type: str = Field(..., description="认证方式：API_KEY/BEARER/BASIC")
    auth_config: Optional[Dict[str, Any]] = Field(None, description="认证配置")
    request_params: Optional[Dict[str, Any]] = Field(None, description="接口入参模板")
    is_enabled: bool = Field(True, description="是否启用")
    sort_order: int = Field(0, description="排序权重")


class PaymentChannelCreate(PaymentChannelBase):
    """创建还款渠道"""
    party_id: int = Field(..., description="甲方ID")


class PaymentChannelUpdate(BaseModel):
    """更新还款渠道"""
    channel_name: Optional[str] = None
    channel_icon: Optional[str] = None
    channel_type: Optional[str] = None
    service_provider: Optional[str] = None
    description: Optional[str] = None
    api_url: Optional[str] = None
    api_method: Optional[str] = None
    auth_type: Optional[str] = None
    auth_config: Optional[Dict[str, Any]] = None
    request_params: Optional[Dict[str, Any]] = None
    is_enabled: Optional[bool] = None
    sort_order: Optional[int] = None


class PaymentChannelResponse(PaymentChannelBase):
    """还款渠道响应"""
    id: int
    party_id: int
    created_by: Optional[int] = None
    updated_by: Optional[int] = None
    created_at: datetime
    updated_at: datetime

    class Config:
        from_attributes = True


class PaymentChannelSimple(BaseModel):
    """简化的还款渠道信息（用于前端选择）"""
    id: int
    channel_name: str
    channel_icon: Optional[str] = None
    channel_type: str
    service_provider: Optional[str] = None
    sort_order: int

    class Config:
        from_attributes = True


class PaymentChannelSortRequest(BaseModel):
    """渠道排序请求"""
    party_id: int = Field(..., description="甲方ID")
    channel_ids: list[int] = Field(..., description="渠道ID列表，按新的排序顺序")


# ==================== 还款码相关 ====================

class PaymentCodeRequest(BaseModel):
    """请求还款码"""
    case_id: int = Field(..., description="案件ID")
    loan_id: int = Field(..., description="借款ID")
    channel_id: int = Field(..., description="渠道ID")
    installment_number: Optional[int] = Field(None, description="期数")
    amount: Decimal = Field(..., description="还款金额", gt=0)

    @validator('amount')
    def validate_amount(cls, v):
        """验证金额格式"""
        if v <= 0:
            raise ValueError('还款金额必须大于0')
        # 限制小数位数为2位
        if v.as_tuple().exponent < -2:
            raise ValueError('还款金额最多保留2位小数')
        return v


class PaymentCodeResponse(BaseModel):
    """还款码响应"""
    code_no: str = Field(..., description="还款码编号")
    payment_type: str = Field(..., description="支付类型")
    payment_code: Optional[str] = Field(None, description="支付码内容")
    qr_image_url: Optional[str] = Field(None, description="二维码图片URL")
    channel_name: str = Field(..., description="渠道名称")
    channel_icon: Optional[str] = Field(None, description="渠道图标")
    amount: Decimal = Field(..., description="还款金额")
    currency: str = Field(..., description="币种")
    expired_at: Optional[datetime] = Field(None, description="过期时间")
    created_at: datetime = Field(..., description="创建时间")

    class Config:
        from_attributes = True


class PaymentCodeListItem(BaseModel):
    """还款码列表项"""
    id: int
    code_no: str
    channel_name: str
    channel_icon: Optional[str] = None
    payment_type: str
    installment_number: Optional[int] = None
    amount: Decimal
    currency: str
    status: str
    created_at: datetime
    expired_at: Optional[datetime] = None
    paid_at: Optional[datetime] = None
    remaining_seconds: Optional[int] = Field(None, description="剩余秒数（待支付状态）")

    class Config:
        from_attributes = True


class PaymentCodeDetail(BaseModel):
    """还款码详情"""
    code_no: str
    party_id: int
    channel_id: int
    channel_name: str
    channel_icon: Optional[str] = None
    service_provider: Optional[str] = None
    payment_type: str
    payment_code: Optional[str] = None
    qr_image_url: Optional[str] = None
    case_id: int
    case_no: Optional[str] = None
    loan_id: int
    loan_no: Optional[str] = None
    customer_name: Optional[str] = None
    installment_number: Optional[int] = None
    amount: Decimal
    currency: str
    status: str
    created_at: datetime
    expired_at: Optional[datetime] = None
    paid_at: Optional[datetime] = None

    class Config:
        from_attributes = True


class InstallmentInfo(BaseModel):
    """期数信息"""
    number: int = Field(..., description="期数")
    status: str = Field(..., description="状态：PAID/OVERDUE/PENDING")
    due_date: str = Field(..., description="应还日期")
    overdue_days: Optional[int] = Field(None, description="逾期天数")
    principal: Decimal = Field(..., description="本金")
    interest: Decimal = Field(..., description="利息")
    penalty: Decimal = Field(0, description="罚息")
    fee: Decimal = Field(0, description="费用")
    total: Decimal = Field(..., description="合计")


class InstallmentListResponse(BaseModel):
    """期数列表响应"""
    total_installments: int = Field(..., description="总期数")
    current_overdue: Optional[int] = Field(None, description="当前逾期期数")
    installments: list[InstallmentInfo] = Field(..., description="期数列表")


# ==================== Webhook相关 ====================

class PaymentWebhookRequest(BaseModel):
    """支付Webhook请求"""
    third_party_order_id: str = Field(..., description="第三方订单ID")
    status: str = Field(..., description="支付状态")
    paid_at: Optional[str] = Field(None, description="支付时间")
    raw_data: Optional[Dict[str, Any]] = Field(None, description="原始数据")


# ==================== 通用响应 ====================

class PaginatedResponse(BaseModel):
    """分页响应"""
    total: int = Field(..., description="总数")
    page: int = Field(..., description="当前页")
    page_size: int = Field(..., description="每页数量")
    items: list = Field(..., description="数据列表", alias="list")
    
    class Config:
        populate_by_name = True  # 允许使用alias

