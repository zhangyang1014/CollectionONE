"""数据看板相关的Pydantic Schemas"""
from pydantic import BaseModel, Field
from typing import Optional, List, Dict, Any
from datetime import datetime, date
from decimal import Decimal


# ============ 案件联系人 Schemas ============

class CaseContactBase(BaseModel):
    """案件联系人基础Schema"""
    case_id: int
    contact_name: str
    phone_number: str
    relation: str
    is_primary: bool = False
    available_channels: Optional[List[str]] = None
    remark: Optional[str] = None


class CaseContactCreate(CaseContactBase):
    """创建案件联系人Schema"""
    pass


class CaseContactUpdate(BaseModel):
    """更新案件联系人Schema"""
    contact_name: Optional[str] = None
    phone_number: Optional[str] = None
    relation: Optional[str] = None
    is_primary: Optional[bool] = None
    available_channels: Optional[List[str]] = None
    remark: Optional[str] = None


class CaseContactResponse(CaseContactBase):
    """案件联系人响应Schema"""
    id: int
    created_at: datetime
    updated_at: datetime

    class Config:
        from_attributes = True


# ============ 通信记录 Schemas ============

class CommunicationRecordBase(BaseModel):
    """通信记录基础Schema"""
    case_id: int
    collector_id: int
    contact_person_id: Optional[int] = None
    channel: str  # phone/whatsapp/sms/rcs
    direction: str  # inbound/outbound
    call_duration: Optional[int] = None
    is_connected: Optional[bool] = None
    call_record_url: Optional[str] = None
    is_replied: Optional[bool] = None
    message_content: Optional[str] = None
    contact_result: str  # connected/not_connected/replied/...
    ttfc_seconds: Optional[int] = None
    remark: Optional[str] = None
    contacted_at: datetime


class CommunicationRecordCreate(CommunicationRecordBase):
    """创建通信记录Schema"""
    pass


class CommunicationRecordUpdate(BaseModel):
    """更新通信记录Schema"""
    contact_result: Optional[str] = None
    call_duration: Optional[int] = None
    is_replied: Optional[bool] = None
    remark: Optional[str] = None


class CommunicationRecordResponse(CommunicationRecordBase):
    """通信记录响应Schema"""
    id: int
    created_at: datetime
    updated_at: datetime

    class Config:
        from_attributes = True


# ============ PTP记录 Schemas ============

class PTPRecordBase(BaseModel):
    """PTP记录基础Schema"""
    case_id: int
    collector_id: int
    communication_id: Optional[int] = None
    ptp_amount: Decimal
    ptp_date: date
    remark: Optional[str] = None


class PTPRecordCreate(PTPRecordBase):
    """创建PTP记录Schema"""
    pass


class PTPRecordUpdate(BaseModel):
    """更新PTP记录Schema"""
    status: Optional[str] = None  # pending/fulfilled/broken/cancelled
    actual_payment_amount: Optional[Decimal] = None
    actual_payment_date: Optional[date] = None
    fulfillment_rate: Optional[Decimal] = None
    remark: Optional[str] = None


class PTPRecordResponse(PTPRecordBase):
    """PTP记录响应Schema"""
    id: int
    status: str
    actual_payment_amount: Optional[Decimal] = None
    actual_payment_date: Optional[date] = None
    fulfillment_rate: Optional[Decimal] = None
    created_at: datetime
    updated_at: datetime
    fulfilled_at: Optional[datetime] = None

    class Config:
        from_attributes = True


# ============ 质检记录 Schemas ============

class QualityInspectionRecordBase(BaseModel):
    """质检记录基础Schema"""
    case_id: int
    collector_id: int
    communication_id: Optional[int] = None
    inspector_id: Optional[int] = None
    inspection_type: str  # manual/ai
    quality_score: Optional[int] = None
    script_compliance_rate: Optional[int] = None
    violations: Optional[List[Dict[str, Any]]] = None
    compliant_items: Optional[List[Dict[str, Any]]] = None
    feedback: Optional[str] = None
    inspected_at: datetime


class QualityInspectionRecordCreate(QualityInspectionRecordBase):
    """创建质检记录Schema"""
    pass


class QualityInspectionRecordUpdate(BaseModel):
    """更新质检记录Schema"""
    quality_score: Optional[int] = None
    script_compliance_rate: Optional[int] = None
    violations: Optional[List[Dict[str, Any]]] = None
    compliant_items: Optional[List[Dict[str, Any]]] = None
    feedback: Optional[str] = None


class QualityInspectionRecordResponse(QualityInspectionRecordBase):
    """质检记录响应Schema"""
    id: int
    created_at: datetime
    updated_at: datetime

    class Config:
        from_attributes = True


# ============ 催员绩效统计 Schemas ============

class CollectorPerformanceStatResponse(BaseModel):
    """催员绩效统计响应Schema"""
    id: int
    collector_id: int
    tenant_id: int
    agency_id: int
    team_id: int
    queue_ids: Optional[List[int]] = None
    stat_date: date
    stat_period: str  # daily/weekly/monthly
    
    # 业绩总览
    assigned_cases: int
    collected_cases: int
    case_collection_rate: Optional[Decimal] = None
    assigned_amount: Decimal
    collected_amount: Decimal
    amount_collection_rate: Optional[Decimal] = None
    
    # PTP指标
    ptp_count: int
    ptp_amount: Decimal
    ptp_fulfilled_count: int
    ptp_fulfillment_rate: Optional[Decimal] = None
    
    # 沟通覆盖
    self_contact_rate: Optional[Decimal] = None
    total_contact_rate: Optional[Decimal] = None
    
    # 电话指标
    self_call_rate: Optional[Decimal] = None
    total_call_rate: Optional[Decimal] = None
    self_connected_rate: Optional[Decimal] = None
    
    # WhatsApp指标
    self_wa_contact_rate: Optional[Decimal] = None
    total_wa_contact_rate: Optional[Decimal] = None
    self_wa_reply_rate: Optional[Decimal] = None
    total_wa_reply_rate: Optional[Decimal] = None
    
    # 首次触达时长
    ttfc_median: Optional[int] = None
    ttfc_distribution: Optional[Dict[str, int]] = None
    
    # 质检合规
    self_number_view_rate: Optional[Decimal] = None
    quality_avg_score: Optional[Decimal] = None
    script_avg_compliance_rate: Optional[Decimal] = None
    violation_count: int
    
    created_at: datetime
    updated_at: datetime

    class Config:
        from_attributes = True


# ============ 自定义维度统计 Schemas ============

class CustomDimensionStatResponse(BaseModel):
    """自定义维度统计响应Schema"""
    id: int
    collector_id: int
    tenant_id: int
    stat_date: date
    stat_period: str
    custom_field_id: int
    custom_field_key: str
    dimension_value: str
    
    # 业绩总览（与CollectorPerformanceStat相同）
    assigned_cases: int
    collected_cases: int
    case_collection_rate: Optional[Decimal] = None
    assigned_amount: Decimal
    collected_amount: Decimal
    amount_collection_rate: Optional[Decimal] = None
    
    # PTP指标
    ptp_count: int
    ptp_amount: Decimal
    ptp_fulfilled_count: int
    ptp_fulfillment_rate: Optional[Decimal] = None
    
    # 沟通覆盖
    self_contact_rate: Optional[Decimal] = None
    total_contact_rate: Optional[Decimal] = None
    
    # 电话指标
    self_call_rate: Optional[Decimal] = None
    total_call_rate: Optional[Decimal] = None
    self_connected_rate: Optional[Decimal] = None
    
    # WhatsApp指标
    self_wa_contact_rate: Optional[Decimal] = None
    total_wa_contact_rate: Optional[Decimal] = None
    self_wa_reply_rate: Optional[Decimal] = None
    total_wa_reply_rate: Optional[Decimal] = None
    
    # 首次触达时长
    ttfc_median: Optional[int] = None
    ttfc_distribution: Optional[Dict[str, int]] = None
    
    # 质检合规
    self_number_view_rate: Optional[Decimal] = None
    quality_avg_score: Optional[Decimal] = None
    script_avg_compliance_rate: Optional[Decimal] = None
    violation_count: int
    
    created_at: datetime
    updated_at: datetime

    class Config:
        from_attributes = True


# ============ 看板API请求/响应 Schemas ============

class PerformanceDashboardRequest(BaseModel):
    """绩效看板请求Schema"""
    collector_id: int
    start_date: date
    end_date: date
    period: str = "daily"  # daily/weekly/monthly
    queue_ids: Optional[List[int]] = None


class PerformanceDashboardResponse(BaseModel):
    """绩效看板响应Schema"""
    collector_info: Dict[str, Any]
    performance_stats: CollectorPerformanceStatResponse
    trend_data: Optional[List[CollectorPerformanceStatResponse]] = None
    comparison_data: Optional[Dict[str, Any]] = None
    ranking_data: Optional[Dict[str, Any]] = None
    alerts: List[Dict[str, Any]] = []


class CustomDimensionAnalysisRequest(BaseModel):
    """自定义维度分析请求Schema"""
    tenant_id: int
    custom_field_id: int
    collector_id: Optional[int] = None
    start_date: date
    end_date: date
    period: str = "daily"


class CustomDimensionAnalysisResponse(BaseModel):
    """自定义维度分析响应Schema"""
    custom_field_info: Dict[str, Any]
    dimension_stats: List[CustomDimensionStatResponse]
    chart_data: Dict[str, Any]


class AlertResponse(BaseModel):
    """预警响应Schema"""
    alert_type: str  # low_contact_rate/low_ptp_fulfillment/ttfc_exceeded/quality_issue
    severity: str  # high/medium/low
    title: str
    description: str
    value: Optional[Any] = None
    threshold: Optional[Any] = None
    recommendation: Optional[str] = None


# ============ TTFC统计 Schemas ============

class TTFCStatsResponse(BaseModel):
    """TTFC统计响应Schema"""
    median: int  # 中位数（秒）
    mean: float  # 平均值（秒）
    distribution: Dict[str, int]  # 分箱分布
    percentile_25: int
    percentile_75: int
    percentile_90: int


# ============ PTP统计 Schemas ============

class PTPStatsResponse(BaseModel):
    """PTP统计响应Schema"""
    total_ptp_count: int
    fulfilled_count: int
    broken_count: int
    pending_count: int
    cancelled_count: int
    total_ptp_amount: Decimal
    fulfilled_amount: Decimal
    fulfillment_rate: Decimal

