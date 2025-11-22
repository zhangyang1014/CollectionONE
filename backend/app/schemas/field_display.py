"""甲方字段展示配置相关Schema"""
from typing import Optional, List, Dict, Any
from pydantic import BaseModel, Field
from datetime import datetime


class ColorRule(BaseModel):
    """颜色规则"""
    condition: str = Field(..., description="条件表达式，如：value > 1000")
    color: str = Field(..., description="颜色：red/yellow/green/normal")


class HideRule(BaseModel):
    """隐藏规则"""
    rule_type: str = Field(..., description="规则类型：queue/agency/team")
    target_ids: List[str] = Field(default=[], description="目标ID列表")


class FormatRule(BaseModel):
    """格式化规则"""
    format_type: str = Field(..., description="格式类型：date/number/currency/percent/custom")
    format_pattern: Optional[str] = Field(None, description="格式模式")
    prefix: Optional[str] = Field(None, description="前缀")
    suffix: Optional[str] = Field(None, description="后缀")


class FieldDisplayConfigBase(BaseModel):
    """字段展示配置基础Schema"""
    tenant_id: int
    scene_type: str = Field(..., description="场景类型：admin_case_list/collector_case_list/collector_case_detail")
    scene_name: str = Field(..., description="场景名称")
    field_key: str = Field(..., description="字段标识")
    field_name: str = Field(..., description="字段名称")
    field_data_type: Optional[str] = Field(None, description="字段数据类型：String/Integer/Boolean/Enum等")
    field_source: Optional[str] = Field(None, description="字段来源：standard/extended/custom")
    sort_order: int = Field(default=0, description="排序顺序")
    display_width: int = Field(default=0, description="显示宽度（像素），0表示自动")
    color_type: str = Field(default='normal', description="颜色类型：normal/red/yellow/green")
    color_rule: Optional[List[ColorRule]] = Field(None, description="颜色规则")
    hide_rule: Optional[List[HideRule]] = Field(None, description="隐藏规则")
    hide_for_queues: Optional[List[str]] = Field(None, description="对哪些队列隐藏")
    hide_for_agencies: Optional[List[str]] = Field(None, description="对哪些机构隐藏")
    hide_for_teams: Optional[List[str]] = Field(None, description="对哪些小组隐藏")
    format_rule: Optional[FormatRule] = Field(None, description="格式化规则")
    is_searchable: bool = Field(default=False, description="是否可搜索（针对文本字段）")
    is_filterable: bool = Field(default=False, description="是否可筛选（针对枚举字段）")
    is_range_searchable: bool = Field(default=False, description="是否支持范围检索（针对数字和时间字段）")


class FieldDisplayConfigCreate(FieldDisplayConfigBase):
    """创建字段展示配置"""
    created_by: Optional[str] = None


class FieldDisplayConfigUpdate(BaseModel):
    """更新字段展示配置"""
    scene_name: Optional[str] = None
    field_name: Optional[str] = None
    field_data_type: Optional[str] = None
    field_source: Optional[str] = None
    sort_order: Optional[int] = None
    display_width: Optional[int] = None
    color_type: Optional[str] = None
    color_rule: Optional[List[ColorRule]] = None
    hide_rule: Optional[List[HideRule]] = None
    hide_for_queues: Optional[List[str]] = None
    hide_for_agencies: Optional[List[str]] = None
    hide_for_teams: Optional[List[str]] = None
    format_rule: Optional[FormatRule] = None
    is_searchable: Optional[bool] = None
    is_filterable: Optional[bool] = None
    is_range_searchable: Optional[bool] = None
    updated_by: Optional[str] = None


class FieldDisplayConfigResponse(FieldDisplayConfigBase):
    """字段展示配置响应"""
    id: int
    created_at: datetime
    updated_at: datetime
    created_by: Optional[str] = None
    updated_by: Optional[str] = None

    class Config:
        from_attributes = True


class FieldDisplayConfigBatchUpdate(BaseModel):
    """批量更新字段展示配置"""
    configs: List[FieldDisplayConfigUpdate]


class SceneType(BaseModel):
    """场景类型"""
    key: str = Field(..., description="场景键值")
    name: str = Field(..., description="场景名称")
    description: str = Field(..., description="场景描述")


class FieldDisplayConfigQuery(BaseModel):
    """字段展示配置查询参数"""
    tenant_id: Optional[int] = None
    scene_type: Optional[str] = None
    field_key: Optional[str] = None
    is_enabled: Optional[bool] = None


class AvailableFieldOption(BaseModel):
    """可用字段选项（用于添加字段配置时选择）"""
    field_key: str = Field(..., description="字段标识")
    field_name: str = Field(..., description="字段名称")
    field_type: str = Field(..., description="字段数据类型")
    field_source: str = Field(..., description="字段来源：standard/extended/custom")
    field_group_name: Optional[str] = Field(None, description="字段分组名称")
    is_extended: bool = Field(default=False, description="是否为扩展字段")
    is_required: bool = Field(default=False, description="是否必填")
    enum_options: Optional[List[str]] = Field(None, description="枚举选项（如果是枚举类型）")
    description: Optional[str] = Field(None, description="字段描述")

