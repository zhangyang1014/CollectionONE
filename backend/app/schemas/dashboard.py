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


# ============ 空闲催员监控 Schemas ============

class TimeSlot(BaseModel):
    """时间段Schema"""
    start: str = Field(..., description="开始时间，格式：HH:MM")
    end: str = Field(..., description="结束时间，格式：HH:MM")


class IdleMonitorConfigBase(BaseModel):
    """空闲监控配置基础Schema"""
    tenant_id: int
    config_name: str = Field(..., max_length=100, description="配置名称")
    work_time_slots: List[TimeSlot] = Field(..., description="上班时间段列表")
    idle_threshold_minutes: int = Field(..., ge=5, le=120, description="空闲阈值（分钟），范围：5-120")
    monitored_actions: List[str] = Field(..., min_items=1, description="监控行为列表，至少选择一项")
    exclude_holidays: bool = Field(True, description="是否排除节假日")


class IdleMonitorConfigCreate(IdleMonitorConfigBase):
    """创建空闲监控配置Schema"""
    created_by: Optional[str] = None


class IdleMonitorConfigUpdate(BaseModel):
    """更新空闲监控配置Schema"""
    config_name: Optional[str] = None
    work_time_slots: Optional[List[TimeSlot]] = None
    idle_threshold_minutes: Optional[int] = Field(None, ge=5, le=120)
    monitored_actions: Optional[List[str]] = Field(None, min_items=1)
    exclude_holidays: Optional[bool] = None


class IdleMonitorConfigResponse(IdleMonitorConfigBase):
    """空闲监控配置响应Schema"""
    id: int
    is_active: bool
    created_by: Optional[str]
    created_at: datetime
    updated_at: datetime

    class Config:
        from_attributes = True


class IdlePeriod(BaseModel):
    """空闲时段Schema"""
    start: str = Field(..., description="开始时间")
    end: str = Field(..., description="结束时间")
    duration: int = Field(..., description="时长（分钟）")


class ActionInfo(BaseModel):
    """行为信息Schema"""
    type: str = Field(..., description="行为类型")
    time: str = Field(..., description="行为时间")
    details: Optional[str] = Field(None, description="行为详情")


class ManagedCases(BaseModel):
    """管理案件信息Schema"""
    total: int = Field(0, description="总案件数")
    collected: int = Field(0, description="已还案件数")
    collection_rate: Decimal = Field(0, description="催回率")


class ManagedAmount(BaseModel):
    """管理金额信息Schema"""
    total: Decimal = Field(0, description="总金额")
    collected: Decimal = Field(0, description="已还金额")
    collection_rate: Decimal = Field(0, description="回款率")


class IdleMonitorSummary(BaseModel):
    """空闲监控总览数据Schema"""
    total_idle_collectors: int = Field(0, description="空闲催员总数")
    total_idle_count: int = Field(0, description="空闲总次数")
    total_idle_minutes: int = Field(0, description="空闲总时长（分钟）")
    total_idle_hours: Decimal = Field(0, description="空闲总时长（小时）")
    avg_idle_minutes: Decimal = Field(0, description="平均空闲时长（分钟）")
    comparison: Optional[Dict[str, Decimal]] = Field(None, description="环比数据")


class IdleMonitorDetailItem(BaseModel):
    """空闲监控详情列表项Schema"""
    collector_id: int
    collector_name: str
    collector_code: str
    agency_id: int
    agency_name: str
    team_id: int
    team_name: str
    stat_date: str
    idle_count: int
    total_idle_minutes: int
    longest_idle_minutes: int
    avg_idle_minutes: Decimal
    idle_rate: Decimal
    managed_cases: ManagedCases
    managed_amount: ManagedAmount
    idle_periods: List[IdlePeriod]


class IdleMonitorDetailsResponse(BaseModel):
    """空闲监控详情列表响应Schema"""
    total: int
    page: int
    page_size: int
    items: List[IdleMonitorDetailItem]


class CollectorInfo(BaseModel):
    """催员信息Schema"""
    id: int
    name: str
    code: str
    agency_name: str
    team_name: str


class IdleSummary(BaseModel):
    """空闲统计Schema"""
    idle_count: int
    total_idle_minutes: int
    avg_idle_minutes: Decimal
    longest_idle_minutes: int


class CaseSummary(BaseModel):
    """案件统计Schema"""
    total_cases: int
    collected_cases: int
    collection_rate: Decimal
    total_amount: Decimal
    collected_amount: Decimal
    amount_collection_rate: Decimal


class IdleDetail(BaseModel):
    """空闲详细信息Schema"""
    start_time: str
    end_time: str
    duration_minutes: int
    before_action: Optional[ActionInfo]
    after_action: Optional[ActionInfo]


class CollectorIdleDetailResponse(BaseModel):
    """催员空闲详细信息响应Schema"""
    collector_info: CollectorInfo
    stat_date: str
    idle_summary: IdleSummary
    case_summary: CaseSummary
    idle_details: List[IdleDetail]


class IdleTrendResponse(BaseModel):
    """空闲趋势响应Schema"""
    metric: str
    dates: List[str]
    values: List[int]


class ConfigHistoryItem(BaseModel):
    """配置历史项Schema"""
    id: int
    config_name: str
    created_by: Optional[str]
    created_at: datetime
    is_active: bool

    class Config:
        from_attributes = True


class ConfigHistoryResponse(BaseModel):
    """配置历史响应Schema"""
    total: int
    page: int
    page_size: int
    items: List[ConfigHistoryItem]

