"""自定义维度分析API接口"""
from fastapi import APIRouter, Depends, Query, HTTPException
from sqlalchemy.orm import Session
from sqlalchemy import and_
from typing import List, Optional
from datetime import date

from app.core.database import get_db
from app.models import CustomDimensionStat, CustomField, CaseCustomFieldValue
from app.schemas.dashboard import (
    CustomDimensionAnalysisResponse,
    CustomDimensionStatResponse,
)

router = APIRouter(prefix="/api/v1/analytics/custom-dimensions", tags=["自定义维度分析"])


@router.get("/fields")
def get_analyzable_fields(
    tenant_id: int = Query(..., description="甲方ID"),
    db: Session = Depends(get_db)
):
    """获取可用于分析的自定义字段列表"""
    # 查询该甲方的所有枚举类型自定义字段（最适合用于维度分析）
    fields = db.query(CustomField).filter(
        and_(
            CustomField.tenant_id == tenant_id,
            CustomField.field_type == "Enum",
            CustomField.is_active == True,
            CustomField.is_deleted == False
        )
    ).all()
    
    fields_data = []
    for field in fields:
        # 获取该字段的所有唯一值
        unique_values_query = db.query(CaseCustomFieldValue.field_value).filter(
            CaseCustomFieldValue.field_id == field.id
        ).distinct()
        
        unique_values = [v[0] for v in unique_values_query.all() if v[0]]
        
        fields_data.append({
            "id": field.id,
            "field_key": field.field_key,
            "field_name": field.field_name,
            "field_type": field.field_type,
            "enum_options": field.enum_options,
            "unique_values": unique_values[:20],  # 限制返回数量
            "value_count": len(unique_values)
        })
    
    return {"fields": fields_data}


@router.get("/stats", response_model=CustomDimensionAnalysisResponse)
def get_custom_dimension_stats(
    tenant_id: int = Query(..., description="甲方ID"),
    custom_field_id: int = Query(..., description="自定义字段ID"),
    collector_id: Optional[int] = Query(None, description="催员ID（可选）"),
    start_date: date = Query(..., description="开始日期"),
    end_date: date = Query(..., description="结束日期"),
    period: str = Query("daily", description="统计周期"),
    db: Session = Depends(get_db)
):
    """获取基于自定义字段的统计数据"""
    # 验证自定义字段是否存在
    field = db.query(CustomField).filter(CustomField.id == custom_field_id).first()
    if not field:
        raise HTTPException(status_code=404, detail="自定义字段不存在")
    
    # 查询统计数据
    query = db.query(CustomDimensionStat).filter(
        and_(
            CustomDimensionStat.tenant_id == tenant_id,
            CustomDimensionStat.custom_field_id == custom_field_id,
            CustomDimensionStat.stat_date >= start_date,
            CustomDimensionStat.stat_date <= end_date,
            CustomDimensionStat.stat_period == period
        )
    )
    
    if collector_id:
        query = query.filter(CustomDimensionStat.collector_id == collector_id)
    
    stats = query.all()
    
    # 按维度值分组统计
    dimension_values = list(set(s.dimension_value for s in stats))
    
    # 构建图表数据
    chart_data = {
        "dimensions": dimension_values,
        "metrics": {
            "case_collection_rate": {},
            "amount_collection_rate": {},
            "ptp_fulfillment_rate": {},
        }
    }
    
    for dim_value in dimension_values:
        dim_stats = [s for s in stats if s.dimension_value == dim_value]
        if dim_stats:
            avg_case_rate = sum(float(s.case_collection_rate or 0) for s in dim_stats) / len(dim_stats)
            avg_amount_rate = sum(float(s.amount_collection_rate or 0) for s in dim_stats) / len(dim_stats)
            avg_ptp_rate = sum(float(s.ptp_fulfillment_rate or 0) for s in dim_stats) / len(dim_stats)
            
            chart_data["metrics"]["case_collection_rate"][dim_value] = round(avg_case_rate, 2)
            chart_data["metrics"]["amount_collection_rate"][dim_value] = round(avg_amount_rate, 2)
            chart_data["metrics"]["ptp_fulfillment_rate"][dim_value] = round(avg_ptp_rate, 2)
    
    return CustomDimensionAnalysisResponse(
        custom_field_info={
            "id": field.id,
            "field_key": field.field_key,
            "field_name": field.field_name,
            "field_type": field.field_type
        },
        dimension_stats=stats,
        chart_data=chart_data
    )


@router.get("/chart")
def get_chart_data(
    tenant_id: int = Query(..., description="甲方ID"),
    custom_field_id: int = Query(..., description="自定义字段ID"),
    metric: str = Query("case_collection_rate", description="指标：case_collection_rate/amount_collection_rate/ptp_fulfillment_rate"),
    chart_type: str = Query("bar", description="图表类型：bar/radar/pie/scatter"),
    start_date: date = Query(..., description="开始日期"),
    end_date: date = Query(..., description="结束日期"),
    db: Session = Depends(get_db)
):
    """获取可视化图表数据（支持多种图表类型）"""
    # 查询统计数据
    stats = db.query(CustomDimensionStat).filter(
        and_(
            CustomDimensionStat.tenant_id == tenant_id,
            CustomDimensionStat.custom_field_id == custom_field_id,
            CustomDimensionStat.stat_date >= start_date,
            CustomDimensionStat.stat_date <= end_date
        )
    ).all()
    
    if not stats:
        return {"chart_type": chart_type, "data": []}
    
    # 按维度值分组
    dimension_data = {}
    for s in stats:
        if s.dimension_value not in dimension_data:
            dimension_data[s.dimension_value] = []
        dimension_data[s.dimension_value].append(getattr(s, metric) or 0)
    
    # 计算每个维度的平均值
    dimension_averages = {
        dim: sum(float(v) for v in values) / len(values)
        for dim, values in dimension_data.items()
    }
    
    # 根据图表类型格式化数据
    if chart_type == "bar":
        chart_data = {
            "xAxis": list(dimension_averages.keys()),
            "yAxis": list(dimension_averages.values())
        }
    elif chart_type == "pie":
        chart_data = {
            "data": [{"name": k, "value": v} for k, v in dimension_averages.items()]
        }
    elif chart_type == "radar":
        chart_data = {
            "indicator": [{"name": k, "max": 100} for k in dimension_averages.keys()],
            "value": list(dimension_averages.values())
        }
    else:
        chart_data = {"data": dimension_averages}
    
    return {
        "chart_type": chart_type,
        "metric": metric,
        "data": chart_data
    }

