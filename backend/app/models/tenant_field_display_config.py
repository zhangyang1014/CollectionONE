"""甲方字段展示配置模型"""
from sqlalchemy import Column, Integer, BigInteger, String, Boolean, DateTime, ForeignKey, Text, JSON
from sqlalchemy.sql import func
from app.core.database import Base


class TenantFieldDisplayConfig(Base):
    """甲方字段展示配置表 - 用于配置不同场景下字段的展示方式"""
    __tablename__ = "tenant_field_display_configs"

    id = Column(Integer, primary_key=True, index=True, autoincrement=True)
    tenant_id = Column(Integer, ForeignKey("tenants.id"), nullable=False, index=True, comment="所属甲方ID")
    
    # 配置场景
    scene_type = Column(String(50), nullable=False, index=True, comment="场景类型：admin_case_list/collector_case_list/collector_case_detail")
    scene_name = Column(String(100), nullable=False, comment="场景名称")
    
    # 字段配置
    field_key = Column(String(100), nullable=False, index=True, comment="字段标识")
    field_name = Column(String(200), nullable=False, comment="字段名称")
    field_data_type = Column(String(50), nullable=True, comment="字段数据类型：String/Integer/Boolean/Enum等")
    field_source = Column(String(20), nullable=True, comment="字段来源：standard/extended/custom")
    
    # 展示配置
    sort_order = Column(Integer, default=0, nullable=False, comment="排序顺序")
    display_width = Column(Integer, default=0, nullable=False, comment="显示宽度（像素），0表示自动")
    
    # 颜色配置
    color_type = Column(String(20), default='normal', nullable=False, comment="颜色类型：normal/red/yellow/green")
    color_rule = Column(JSON, nullable=True, comment="颜色规则（条件表达式）")
    
    # 隐藏规则（仅催员端）
    hide_rule = Column(JSON, nullable=True, comment="隐藏规则：基于案件所属队列配置")
    hide_for_queues = Column(JSON, nullable=True, comment="对哪些队列隐藏（队列ID数组）")
    hide_for_agencies = Column(JSON, nullable=True, comment="对哪些机构隐藏（机构ID数组）")
    hide_for_teams = Column(JSON, nullable=True, comment="对哪些小组隐藏（小组ID数组）")
    
    # 其他配置
    format_rule = Column(JSON, nullable=True, comment="格式化规则")
    is_searchable = Column(Boolean, default=False, nullable=False, comment="是否可搜索（针对文本字段）")
    is_filterable = Column(Boolean, default=False, nullable=False, comment="是否可筛选（针对枚举字段）")
    is_range_searchable = Column(Boolean, default=False, nullable=False, comment="是否支持范围检索（针对数字和时间字段）")
    
    # 元数据
    created_at = Column(DateTime, server_default=func.now(), comment="创建时间")
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now(), comment="更新时间")
    created_by = Column(String(100), comment="创建人")
    updated_by = Column(String(100), comment="更新人")

    class Config:
        orm_mode = True

