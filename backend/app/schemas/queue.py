"""队列管理相关 Schemas"""
from pydantic import BaseModel, Field
from typing import Optional, List
from datetime import datetime


# ===== 案件队列 Schemas =====
class CaseQueueBase(BaseModel):
    """案件队列基础Schema"""
    tenant_id: int = Field(..., description="所属甲方ID")
    queue_code: str = Field(..., max_length=100, description="队列编码")
    queue_name: str = Field(..., max_length=200, description="队列名称")
    queue_name_en: Optional[str] = Field(None, max_length=200, description="队列名称（英文）")
    queue_description: Optional[str] = Field(None, description="队列描述")
    overdue_days_min: Optional[int] = Field(None, description="逾期天数最小值")
    overdue_days_max: Optional[int] = Field(None, description="逾期天数最大值")
    sort_order: int = Field(0, description="排序顺序")
    is_active: bool = Field(True, description="是否启用")


class CaseQueueCreate(CaseQueueBase):
    """创建案件队列"""
    pass


class CaseQueueUpdate(BaseModel):
    """更新案件队列"""
    queue_name: Optional[str] = Field(None, max_length=200)
    queue_name_en: Optional[str] = Field(None, max_length=200)
    queue_description: Optional[str] = None
    overdue_days_min: Optional[int] = None
    overdue_days_max: Optional[int] = None
    sort_order: Optional[int] = None
    is_active: Optional[bool] = None


class CaseQueue(CaseQueueBase):
    """案件队列响应"""
    id: int
    case_count: int = Field(0, description="案件数量")
    configured_field_count: int = Field(0, description="已配置字段数")
    created_at: datetime
    updated_at: datetime

    class Config:
        orm_mode = True


# ===== 队列字段配置 Schemas =====
class QueueFieldConfigBase(BaseModel):
    """队列字段配置基础Schema"""
    field_id: int = Field(..., description="字段ID")
    field_type: str = Field(..., description="字段类型：standard/custom")
    is_visible: bool = Field(True, description="是否可见")
    is_required: Optional[bool] = Field(None, description="是否必填（NULL表示使用默认）")
    is_readonly: bool = Field(False, description="是否只读")
    is_editable: bool = Field(True, description="是否可编辑")
    sort_order: int = Field(0, description="字段排序")


class QueueFieldConfigCreate(QueueFieldConfigBase):
    """创建队列字段配置"""
    queue_id: int = Field(..., description="队列ID")


class QueueFieldConfigUpdate(BaseModel):
    """更新队列字段配置"""
    is_visible: Optional[bool] = None
    is_required: Optional[bool] = None
    is_readonly: Optional[bool] = None
    is_editable: Optional[bool] = None
    sort_order: Optional[int] = None


class QueueFieldConfig(QueueFieldConfigBase):
    """队列字段配置响应"""
    id: int
    queue_id: int
    field_key: Optional[str] = Field(None, description="字段标识")
    field_name: Optional[str] = Field(None, description="字段名称")
    field_data_type: Optional[str] = Field(None, description="字段数据类型")
    field_source: str = Field("standard", description="字段来源")
    created_at: datetime
    updated_at: datetime

    class Config:
        orm_mode = True


class BatchUpdateQueueFieldConfig(BaseModel):
    """批量更新队列字段配置"""
    fields: List[QueueFieldConfigBase] = Field(..., description="字段配置列表")


class CopyQueueFieldConfigRequest(BaseModel):
    """复制队列字段配置请求"""
    source_queue_id: int = Field(..., description="源队列ID")
    copy_mode: str = Field("merge", description="复制模式：merge/replace")


# ===== 获取案件字段配置（运行时） Schemas =====
class CaseFieldConfig(BaseModel):
    """案件字段配置（运行时）"""
    field_id: int
    field_key: str
    field_name: str
    field_type: str
    is_visible: bool
    is_required: Optional[bool]
    is_readonly: bool
    is_editable: bool
    sort_order: int
    value: Optional[str] = None


class FieldGroup(BaseModel):
    """字段分组"""
    group_id: int
    group_name: str
    fields: List[CaseFieldConfig]


class CaseFieldConfigResponse(BaseModel):
    """获取案件字段配置响应"""
    case_id: str
    queue_id: Optional[int]
    queue_name: Optional[str]
    fields_by_group: List[FieldGroup]

