"""组织架构相关 Schemas"""
from pydantic import BaseModel, Field
from typing import Optional, List
from datetime import datetime, date
from decimal import Decimal


# ===== 催收机构 Schemas =====
class CollectionAgencyBase(BaseModel):
    """催收机构基础Schema"""
    tenant_id: int = Field(..., description="所属甲方ID")
    agency_code: str = Field(..., max_length=100, description="机构编码")
    agency_name: str = Field(..., max_length=200, description="机构名称")
    agency_name_en: Optional[str] = Field(None, max_length=200, description="机构名称（英文）")
    contact_person: Optional[str] = Field(None, max_length=100, description="联系人")
    contact_phone: Optional[str] = Field(None, max_length=50, description="联系电话")
    contact_email: Optional[str] = Field(None, max_length=100, description="联系邮箱")
    address: Optional[str] = Field(None, description="机构地址")
    description: Optional[str] = Field(None, description="机构描述")
    timezone: Optional[int] = Field(None, ge=-12, le=14, description="时区偏移量（UTC偏移，范围-12到+14）")
    agency_type: str = Field('real', description="机构类型：real=真实机构，virtual=虚拟机构")
    sort_order: int = Field(0, description="排序顺序")
    is_active: bool = Field(True, description="是否启用")


class CollectionAgencyCreate(CollectionAgencyBase):
    """创建催收机构"""
    pass


class CollectionAgencyUpdate(BaseModel):
    """更新催收机构"""
    agency_name: Optional[str] = Field(None, max_length=200)
    agency_name_en: Optional[str] = Field(None, max_length=200)
    contact_person: Optional[str] = Field(None, max_length=100)
    contact_phone: Optional[str] = Field(None, max_length=50)
    contact_email: Optional[str] = Field(None, max_length=100)
    address: Optional[str] = None
    description: Optional[str] = None
    timezone: Optional[int] = Field(None, ge=-12, le=14)
    agency_type: Optional[str] = Field(None, description="机构类型：real=真实机构，virtual=虚拟机构")
    sort_order: Optional[int] = None
    is_active: Optional[bool] = None


class CollectionAgency(CollectionAgencyBase):
    """催收机构响应"""
    id: int
    team_count: int = Field(0, description="小组数量")
    collector_count: int = Field(0, description="催员数量")
    case_count: int = Field(0, description="案件数量")
    created_at: datetime
    updated_at: datetime

    class Config:
        orm_mode = True


# ===== 小组群 Schemas =====
class TeamGroupBase(BaseModel):
    """小组群基础Schema"""
    tenant_id: int = Field(..., description="所属甲方ID")
    agency_id: int = Field(..., description="所属催收机构ID")
    group_code: str = Field(..., max_length=100, description="小组群编码")
    group_name: str = Field(..., max_length=200, description="小组群名称")
    group_name_en: Optional[str] = Field(None, max_length=200, description="小组群名称（英文）")
    description: Optional[str] = Field(None, description="小组群描述")
    sort_order: int = Field(0, description="排序顺序")
    is_active: bool = Field(True, description="是否启用")


class TeamGroupCreate(BaseModel):
    """创建小组群"""
    tenant_id: int = Field(..., description="所属甲方ID")
    agency_id: int = Field(..., description="所属催收机构ID")
    group_code: str = Field(..., max_length=100, description="小组群编码")
    group_name: str = Field(..., max_length=200, description="小组群名称")
    group_name_en: Optional[str] = Field(None, max_length=200, description="小组群名称（英文）")
    description: Optional[str] = Field(None, description="小组群描述")
    sort_order: int = Field(0, description="排序顺序")
    is_active: bool = Field(True, description="是否启用")
    # SPV管理员账号信息
    spv_account_name: str = Field(..., max_length=100, description="SPV账号名称")
    spv_login_id: str = Field(..., max_length=100, description="SPV登录ID")
    spv_email: str = Field(..., max_length=100, description="SPV邮箱")
    spv_password: str = Field(..., max_length=50, description="SPV初始密码")
    spv_mobile: Optional[str] = Field(None, max_length=50, description="SPV手机号码")
    spv_remark: Optional[str] = Field(None, description="SPV备注")


class TeamGroupUpdate(BaseModel):
    """更新小组群"""
    group_name: Optional[str] = Field(None, max_length=200)
    group_name_en: Optional[str] = Field(None, max_length=200)
    description: Optional[str] = None
    sort_order: Optional[int] = None
    is_active: Optional[bool] = None


class TeamGroup(TeamGroupBase):
    """小组群响应"""
    id: int
    agency_name: Optional[str] = Field(None, description="机构名称")
    spv_account_name: Optional[str] = Field(None, description="小组群长SPV姓名")
    spv_login_id: Optional[str] = Field(None, description="SPV登录ID")
    team_count: int = Field(0, description="小组数量")
    collector_count: int = Field(0, description="催员数量")
    created_at: datetime
    updated_at: datetime

    class Config:
        orm_mode = True


# ===== 催收小组 Schemas =====
class CollectionTeamBase(BaseModel):
    """催收小组基础Schema"""
    agency_id: int = Field(..., description="所属催收机构ID")
    team_group_id: int = Field(..., description="所属小组群ID（必选）")
    queue_id: int = Field(..., description="关联的催收队列ID（必选）")
    team_code: str = Field(..., max_length=100, description="小组编码")
    team_name: str = Field(..., max_length=200, description="小组名称")
    team_name_en: Optional[str] = Field(None, max_length=200, description="小组名称（英文）")
    team_leader_id: Optional[int] = Field(None, description="组长ID")
    team_type: Optional[str] = Field(None, max_length=50, description="小组类型")
    description: Optional[str] = Field(None, description="小组描述")
    max_case_count: int = Field(0, description="最大案件数量")
    sort_order: int = Field(0, description="排序顺序")
    is_active: bool = Field(True, description="是否启用")


class CollectionTeamCreate(CollectionTeamBase):
    """创建催收小组"""
    pass


class CollectionTeamUpdate(BaseModel):
    """更新催收小组"""
    team_group_id: Optional[int] = Field(None, description="所属小组群ID")
    queue_id: Optional[int] = Field(None, description="关联的催收队列ID")
    team_name: Optional[str] = Field(None, max_length=200)
    team_name_en: Optional[str] = Field(None, max_length=200)
    team_leader_id: Optional[int] = None
    team_type: Optional[str] = Field(None, max_length=50)
    description: Optional[str] = None
    max_case_count: Optional[int] = None
    sort_order: Optional[int] = None
    is_active: Optional[bool] = None


class CollectionTeam(CollectionTeamBase):
    """催收小组响应"""
    id: int
    tenant_id: int
    tenant_name: Optional[str] = Field(None, description="甲方名称")
    agency_name: Optional[str] = Field(None, description="机构名称")
    team_group_name: Optional[str] = Field(None, description="小组群名称")
    queue_name: Optional[str] = Field(None, description="催收队列名称")
    team_leader_name: Optional[str] = Field(None, description="组长姓名")
    collector_count: int = Field(0, description="催员数量")
    case_count: int = Field(0, description="案件数量")
    created_at: datetime
    updated_at: datetime

    class Config:
        orm_mode = True


# ===== 催员 Schemas =====
class CollectorBase(BaseModel):
    """催员基础Schema"""
    team_id: int = Field(..., description="所属小组ID")
    user_id: int = Field(..., description="关联用户ID")
    collector_code: str = Field(..., max_length=100, description="催员编码")
    collector_name: str = Field(..., max_length=100, description="催员姓名")
    mobile_number: Optional[str] = Field(None, max_length=50, description="手机号码")
    email: Optional[str] = Field(None, max_length=100, description="邮箱")
    employee_no: Optional[str] = Field(None, max_length=50, description="工号")
    collector_level: Optional[str] = Field(None, max_length=50, description="催员等级")
    max_case_count: int = Field(100, description="最大案件数量")
    specialties: Optional[List[str]] = Field(None, description="擅长领域")
    hire_date: Optional[date] = Field(None, description="入职日期")
    is_active: bool = Field(True, description="是否启用")


class CollectorCreate(CollectorBase):
    """创建催员"""
    pass


class CollectorUpdate(BaseModel):
    """更新催员"""
    team_id: Optional[int] = None
    collector_name: Optional[str] = Field(None, max_length=100)
    mobile_number: Optional[str] = Field(None, max_length=50)
    email: Optional[str] = Field(None, max_length=100)
    employee_no: Optional[str] = Field(None, max_length=50)
    collector_level: Optional[str] = Field(None, max_length=50)
    max_case_count: Optional[int] = None
    specialties: Optional[List[str]] = None
    status: Optional[str] = Field(None, max_length=50)
    is_active: Optional[bool] = None


class CollectorStatistics(BaseModel):
    """催员统计数据"""
    today_assigned: int = Field(0, description="今日分配案件数")
    week_assigned: int = Field(0, description="本周分配案件数")
    month_settled: int = Field(0, description="本月结清案件数")
    month_amount_collected: Decimal = Field(0, description="本月回款金额")
    month_contact_count: int = Field(0, description="本月接触次数")
    case_status_distribution: dict = Field({}, description="案件状态分布")


class Collector(CollectorBase):
    """催员响应"""
    id: int
    agency_name: Optional[str] = Field(None, description="机构名称")
    team_name: Optional[str] = Field(None, description="小组名称")
    current_case_count: int = Field(0, description="当前案件数量")
    performance_score: Optional[Decimal] = Field(None, description="绩效评分")
    status: str = Field("active", description="状态")
    created_at: datetime
    updated_at: datetime

    class Config:
        orm_mode = True


class CollectorDetail(Collector):
    """催员详情（包含统计）"""
    statistics: Optional[CollectorStatistics] = None


# ===== 案件分配 Schemas =====
class CaseAssignmentBase(BaseModel):
    """案件分配基础Schema"""
    case_ids: List[int] = Field(..., description="案件ID列表")
    reason: Optional[str] = Field(None, max_length=200, description="分配原因")
    remarks: Optional[str] = Field(None, description="备注")


class AssignToAgency(CaseAssignmentBase):
    """分配案件到机构"""
    agency_id: int = Field(..., description="目标机构ID")


class AssignToTeam(CaseAssignmentBase):
    """分配案件到小组"""
    team_id: int = Field(..., description="目标小组ID")


class AssignToCollector(CaseAssignmentBase):
    """分配案件到催员"""
    collector_id: int = Field(..., description="目标催员ID")


class AutoAssignRequest(CaseAssignmentBase):
    """自动分配请求"""
    assignment_rule: str = Field("balanced", description="分配规则：balanced/random/skill_match")
    target_level: str = Field("collector", description="分配层级：agency/team/collector")
    agency_id: Optional[int] = Field(None, description="指定机构（可选）")
    team_id: Optional[int] = Field(None, description="指定小组（可选）")


class TransferCaseRequest(BaseModel):
    """转移案件请求"""
    case_ids: List[int] = Field(..., description="案件ID列表")
    from_collector_id: int = Field(..., description="原催员ID")
    to_collector_id: int = Field(..., description="目标催员ID")
    reason: str = Field(..., max_length=200, description="转移原因")
    remarks: Optional[str] = Field(None, description="备注")


class ReclaimCaseRequest(BaseModel):
    """回收案件请求"""
    case_ids: List[int] = Field(..., description="案件ID列表")
    reason: str = Field(..., max_length=200, description="回收原因")
    target_level: str = Field("team", description="回收到的层级：team/agency")


class AssignmentResult(BaseModel):
    """分配结果"""
    case_id: int
    assigned_to_collector_id: Optional[int] = None
    assigned_to_collector_name: Optional[str] = None
    assigned_to_team_id: Optional[int] = None
    assigned_to_team_name: Optional[str] = None
    assigned_to_agency_id: Optional[int] = None
    assigned_to_agency_name: Optional[str] = None


class AssignmentResponse(BaseModel):
    """分配响应"""
    assigned_count: int = Field(..., description="成功分配数量")
    assignments: List[AssignmentResult] = Field(..., description="分配详情")


# ===== 机构作息时间 Schemas =====
class TimeSlot(BaseModel):
    """时间段"""
    start: str = Field(..., description="开始时间（格式：HH:MM，24小时制）")
    end: str = Field(..., description="结束时间（格式：HH:MM，24小时制）")


class AgencyWorkingHoursBase(BaseModel):
    """机构作息时间基础Schema"""
    agency_id: int = Field(..., description="机构ID")
    day_of_week: int = Field(..., ge=0, le=6, description="星期几（0=周一，6=周日）")
    time_slots: List[TimeSlot] = Field(..., max_length=5, description="时间段列表（最多5个）")


class AgencyWorkingHoursCreate(AgencyWorkingHoursBase):
    """创建机构作息时间"""
    pass


class AgencyWorkingHoursUpdate(BaseModel):
    """更新机构作息时间"""
    time_slots: List[TimeSlot] = Field(..., max_length=5, description="时间段列表（最多5个）")


class AgencyWorkingHoursResponse(AgencyWorkingHoursBase):
    """机构作息时间响应"""
    id: int
    created_at: datetime
    updated_at: datetime

    class Config:
        from_attributes = True


class AgencyWorkingHoursBatchUpdate(BaseModel):
    """批量更新机构作息时间"""
    working_hours: List[AgencyWorkingHoursCreate] = Field(..., description="作息时间列表（7天）")


# ===== 案件分配历史 Schemas =====
class CaseAssignmentHistory(BaseModel):
    """案件分配历史"""
    id: int
    case_id: int
    assignment_type: str = Field(..., description="分配类型")
    from_agency_name: Optional[str] = None
    to_agency_name: Optional[str] = None
    from_team_name: Optional[str] = None
    to_team_name: Optional[str] = None
    from_collector_name: Optional[str] = None
    to_collector_name: Optional[str] = None
    reason: Optional[str] = None
    assigned_by_name: Optional[str] = None
    assigned_at: datetime
    remarks: Optional[str] = None

    class Config:
        orm_mode = True

