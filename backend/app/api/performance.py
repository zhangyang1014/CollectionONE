"""催员绩效API接口"""
from fastapi import APIRouter, Depends, Query, HTTPException
from sqlalchemy.orm import Session
from sqlalchemy import and_, func
from typing import List, Optional
from datetime import datetime, date, timedelta
from decimal import Decimal

from app.core.database import get_db
from app.models import (
    CollectorPerformanceStat,
    Collector,
    Case,
    CommunicationRecord,
    PTPRecord,
    QualityInspectionRecord,
)
from app.schemas.dashboard import (
    CollectorPerformanceStatResponse,
    PerformanceDashboardResponse,
)

router = APIRouter(prefix="/api/v1/performance", tags=["催员绩效"])


@router.get("/collector/{collector_id}", response_model=PerformanceDashboardResponse)
def get_collector_performance(
    collector_id: int,
    start_date: date = Query(..., description="开始日期"),
    end_date: date = Query(..., description="结束日期"),
    period: str = Query("daily", description="统计周期：daily/weekly/monthly"),
    queue_ids: Optional[str] = Query(None, description="队列ID列表，逗号分隔"),
    db: Session = Depends(get_db)
):
    """获取催员个人绩效看板数据"""
    # 验证催员是否存在
    collector = db.query(Collector).filter(Collector.id == collector_id).first()
    if not collector:
        raise HTTPException(status_code=404, detail="催员不存在")
    
    # 解析队列ID
    queue_id_list = None
    if queue_ids:
        try:
            queue_id_list = [int(q) for q in queue_ids.split(',')]
        except ValueError:
            raise HTTPException(status_code=400, detail="队列ID格式错误")
    
    # 查询绩效统计数据
    query = db.query(CollectorPerformanceStat).filter(
        and_(
            CollectorPerformanceStat.collector_id == collector_id,
            CollectorPerformanceStat.stat_date >= start_date,
            CollectorPerformanceStat.stat_date <= end_date,
            CollectorPerformanceStat.stat_period == period
        )
    )
    
    stats = query.order_by(CollectorPerformanceStat.stat_date).all()
    
    # 如果数据库中没有统计数据，则实时计算
    if not stats:
        # 实时计算绩效数据
        performance_stat = _calculate_realtime_performance(
            db, collector_id, start_date, end_date, queue_id_list
        )
    else:
        # 使用最新的统计数据
        performance_stat = stats[-1]
    
    # 构建响应
    collector_info = {
        "id": collector.id,
        "name": collector.collector_name,
        "code": collector.collector_code,
        "agency_id": collector.agency_id,
        "team_id": collector.team_id,
        "level": collector.collector_level,
    }
    
    # 获取预警数据
    try:
        from app.models import QualityInspectionRecord
        from datetime import timedelta
        
        # 查询预警数据
        recent_inspections = db.query(QualityInspectionRecord).filter(
            and_(
                QualityInspectionRecord.collector_id == collector_id,
                QualityInspectionRecord.inspected_at >= start_date
            )
        ).all()
        
        alerts_data = []
        # 检查质检高危项
        for inspection in recent_inspections:
            if inspection.violations:
                high_risk_items = [v for v in inspection.violations if v.get('type') == 'high_risk']
                if high_risk_items:
                    for item in high_risk_items:
                        alerts_data.append({
                            "alert_type": "quality_violation",
                            "severity": "high",
                            "title": f"质检高危项：{item.get('item', '未知')}",
                            "description": item.get('description', ''),
                            "value": None,
                            "threshold": None,
                            "recommendation": "请立即整改，避免再次出现类似违规"
                        })
    except Exception as e:
        print(f"获取预警数据失败: {e}")
        alerts_data = []
    
    return PerformanceDashboardResponse(
        collector_info=collector_info,
        performance_stats=performance_stat,
        trend_data=stats if len(stats) > 1 else None,
        comparison_data=None,  # TODO: 实现同比/环比数据
        ranking_data=None,  # TODO: 实现排名数据
        alerts=alerts_data
    )


@router.get("/collector/{collector_id}/trend")
def get_collector_trend(
    collector_id: int,
    days: Optional[int] = Query(None, description="趋势天数：7或30"),
    start_date: Optional[date] = Query(None, description="开始日期"),
    end_date: Optional[date] = Query(None, description="结束日期"),
    db: Session = Depends(get_db)
):
    """获取趋势数据（7日/30日）"""
    # 如果提供了日期范围，使用日期范围；否则使用days参数
    if start_date and end_date:
        pass  # 使用提供的日期范围
    elif days:
        end_date = date.today()
        start_date = end_date - timedelta(days=days)
    else:
        # 默认7天
        end_date = date.today()
        start_date = end_date - timedelta(days=7)
    
    stats = db.query(CollectorPerformanceStat).filter(
        and_(
            CollectorPerformanceStat.collector_id == collector_id,
            CollectorPerformanceStat.stat_date >= start_date,
            CollectorPerformanceStat.stat_date <= end_date,
            CollectorPerformanceStat.stat_period == "daily"
        )
    ).order_by(CollectorPerformanceStat.stat_date).all()
    
    # 格式化趋势数据
    trend_data = {
        "dates": [str(s.stat_date) for s in stats],
        "case_collection_rate": [float(s.case_collection_rate or 0) for s in stats],
        "amount_collection_rate": [float(s.amount_collection_rate or 0) for s in stats],
        "ptp_fulfillment_rate": [float(s.ptp_fulfillment_rate or 0) for s in stats],
        "quality_avg_score": [float(s.quality_avg_score or 0) for s in stats],
        # 沟通率相关数据
        "self_call_rate": [float(s.self_call_rate or 0) for s in stats],
        "self_wa_contact_rate": [float(s.self_wa_contact_rate or 0) for s in stats],
        "self_contact_rate": [float(s.self_contact_rate or 0) for s in stats],
        "total_call_rate": [float(s.total_call_rate or 0) for s in stats],
        "total_wa_contact_rate": [float(s.total_wa_contact_rate or 0) for s in stats],
        "total_contact_rate": [float(s.total_contact_rate or 0) for s in stats],
    }
    
    return trend_data


@router.get("/collector/{collector_id}/comparison")
def get_collector_comparison(
    collector_id: int,
    current_start: date = Query(..., description="当前期开始日期"),
    current_end: date = Query(..., description="当前期结束日期"),
    comparison_type: str = Query("previous", description="对比类型：previous(环比)/year_over_year(同比)"),
    db: Session = Depends(get_db)
):
    """获取同比/环比数据"""
    # 计算对比期的日期范围
    days_diff = (current_end - current_start).days + 1
    
    if comparison_type == "previous":
        # 环比：前一个周期
        comparison_end = current_start - timedelta(days=1)
        comparison_start = comparison_end - timedelta(days=days_diff - 1)
    elif comparison_type == "year_over_year":
        # 同比：去年同期
        comparison_start = current_start.replace(year=current_start.year - 1)
        comparison_end = current_end.replace(year=current_end.year - 1)
    else:
        raise HTTPException(status_code=400, detail="无效的对比类型")
    
    # 查询当前期数据
    current_stats = db.query(CollectorPerformanceStat).filter(
        and_(
            CollectorPerformanceStat.collector_id == collector_id,
            CollectorPerformanceStat.stat_date >= current_start,
            CollectorPerformanceStat.stat_date <= current_end
        )
    ).all()
    
    # 查询对比期数据
    comparison_stats = db.query(CollectorPerformanceStat).filter(
        and_(
            CollectorPerformanceStat.collector_id == collector_id,
            CollectorPerformanceStat.stat_date >= comparison_start,
            CollectorPerformanceStat.stat_date <= comparison_end
        )
    ).all()
    
    # 计算平均值
    def calculate_avg(stats, field):
        values = [getattr(s, field) for s in stats if getattr(s, field) is not None]
        return sum(values) / len(values) if values else 0
    
    current_avg = {
        "case_collection_rate": calculate_avg(current_stats, "case_collection_rate"),
        "amount_collection_rate": calculate_avg(current_stats, "amount_collection_rate"),
        "ptp_fulfillment_rate": calculate_avg(current_stats, "ptp_fulfillment_rate"),
        "self_contact_rate": calculate_avg(current_stats, "self_contact_rate"),
        "quality_avg_score": calculate_avg(current_stats, "quality_avg_score"),
    }
    
    comparison_avg = {
        "case_collection_rate": calculate_avg(comparison_stats, "case_collection_rate"),
        "amount_collection_rate": calculate_avg(comparison_stats, "amount_collection_rate"),
        "ptp_fulfillment_rate": calculate_avg(comparison_stats, "ptp_fulfillment_rate"),
        "self_contact_rate": calculate_avg(comparison_stats, "self_contact_rate"),
        "quality_avg_score": calculate_avg(comparison_stats, "quality_avg_score"),
    }
    
    # 计算变化率
    changes = {}
    for key in current_avg.keys():
        if comparison_avg[key] > 0:
            change_rate = ((current_avg[key] - comparison_avg[key]) / comparison_avg[key]) * 100
        else:
            change_rate = 0
        changes[key] = round(change_rate, 2)
    
    return {
        "current_period": {
            "start_date": str(current_start),
            "end_date": str(current_end),
            "metrics": current_avg
        },
        "comparison_period": {
            "start_date": str(comparison_start),
            "end_date": str(comparison_end),
            "metrics": comparison_avg
        },
        "changes": changes
    }


@router.get("/collector/{collector_id}/ranking")
def get_collector_ranking(
    collector_id: int,
    start_date: date = Query(..., description="开始日期"),
    end_date: date = Query(..., description="结束日期"),
    scope: str = Query("team", description="排名范围：team(小组)/agency(机构)"),
    db: Session = Depends(get_db)
):
    """获取小组排名"""
    # 获取催员信息
    collector = db.query(Collector).filter(Collector.id == collector_id).first()
    if not collector:
        raise HTTPException(status_code=404, detail="催员不存在")
    
    # 根据范围确定对比的催员列表
    if scope == "team":
        collectors_query = db.query(Collector).filter(Collector.team_id == collector.team_id)
    elif scope == "agency":
        collectors_query = db.query(Collector).filter(Collector.agency_id == collector.agency_id)
    else:
        raise HTTPException(status_code=400, detail="无效的排名范围")
    
    all_collectors = collectors_query.all()
    
    # 获取所有催员的绩效数据
    rankings = []
    for c in all_collectors:
        stats = db.query(CollectorPerformanceStat).filter(
            and_(
                CollectorPerformanceStat.collector_id == c.id,
                CollectorPerformanceStat.stat_date >= start_date,
                CollectorPerformanceStat.stat_date <= end_date
            )
        ).all()
        
        if stats:
            avg_collection_rate = sum(s.case_collection_rate or 0 for s in stats) / len(stats)
            avg_amount_rate = sum(s.amount_collection_rate or 0 for s in stats) / len(stats)
            total_collected_amount = sum(s.collected_amount or 0 for s in stats)
            
            rankings.append({
                "collector_id": c.id,
                "collector_name": c.collector_name,
                "avg_case_collection_rate": float(avg_collection_rate),
                "avg_amount_collection_rate": float(avg_amount_rate),
                "total_collected_amount": float(total_collected_amount),
            })
    
    # 按案件催回率排序
    rankings.sort(key=lambda x: x["avg_case_collection_rate"], reverse=True)
    
    # 添加排名
    for i, r in enumerate(rankings, 1):
        r["rank"] = i
        if r["collector_id"] == collector_id:
            r["is_current"] = True
    
    return {
        "scope": scope,
        "total_collectors": len(rankings),
        "rankings": rankings
    }


@router.get("/collector/{collector_id}/cases")
def get_collector_cases(
    collector_id: int,
    skip: int = 0,
    limit: int = 100,
    start_date: Optional[date] = Query(None, description="分配开始日期"),
    end_date: Optional[date] = Query(None, description="分配结束日期"),
    case_status: Optional[str] = Query(None, description="案件状态"),
    db: Session = Depends(get_db)
):
    """获取案件明细（支持钻取）"""
    query = db.query(Case).filter(Case.collector_id == collector_id)
    
    if start_date:
        query = query.filter(Case.assigned_at >= start_date)
    
    if end_date:
        query = query.filter(Case.assigned_at < end_date)
    
    if case_status:
        query = query.filter(Case.case_status == case_status)
    
    cases = query.order_by(Case.assigned_at.desc()).offset(skip).limit(limit).all()
    
    # 格式化案件数据
    cases_data = []
    for case in cases:
        # 获取该案件的通信次数
        comm_count = db.query(func.count(CommunicationRecord.id)).filter(
            CommunicationRecord.case_id == case.id
        ).scalar()
        
        # 获取最近一次通信记录
        last_comm = db.query(CommunicationRecord).filter(
            CommunicationRecord.case_id == case.id
        ).order_by(CommunicationRecord.contacted_at.desc()).first()
        
        # 获取PTP状态
        ptp = db.query(PTPRecord).filter(
            and_(
                PTPRecord.case_id == case.id,
                PTPRecord.status.in_(["pending", "fulfilled"])
            )
        ).order_by(PTPRecord.ptp_date.desc()).first()
        
        cases_data.append({
            "case_id": case.id,
            "case_code": case.case_code,
            "user_name": case.user_name,
            "dpd": case.overdue_days,
            "outstanding_amount": float(case.outstanding_amount or 0),
            "case_status": case.case_status,
            "assigned_at": case.assigned_at,
            "communication_count": comm_count,
            "last_contact_result": last_comm.contact_result if last_comm else None,
            "ptp_status": ptp.status if ptp else None,
            "ptp_date": ptp.ptp_date if ptp else None,
        })
    
    return {
        "total": query.count(),
        "cases": cases_data
    }


@router.get("/collector/{collector_id}/communications")
def get_collector_communications(
    collector_id: int,
    skip: int = 0,
    limit: int = 100,
    start_date: Optional[date] = Query(None, description="开始日期"),
    end_date: Optional[date] = Query(None, description="结束日期"),
    channel: Optional[str] = Query(None, description="通信渠道"),
    db: Session = Depends(get_db)
):
    """获取互动明细"""
    query = db.query(CommunicationRecord).filter(
        CommunicationRecord.collector_id == collector_id
    )
    
    if start_date:
        query = query.filter(CommunicationRecord.contacted_at >= start_date)
    
    if end_date:
        query = query.filter(CommunicationRecord.contacted_at < end_date)
    
    if channel:
        query = query.filter(CommunicationRecord.channel == channel)
    
    communications = query.order_by(CommunicationRecord.contacted_at.desc()).offset(skip).limit(limit).all()
    
    # 格式化通信数据
    comms_data = []
    for comm in communications:
        case = db.query(Case).filter(Case.id == comm.case_id).first()
        
        comms_data.append({
            "id": comm.id,
            "case_code": case.case_code if case else None,
            "contacted_at": comm.contacted_at,
            "channel": comm.channel,
            "direction": comm.direction,
            "contact_result": comm.contact_result,
            "call_duration": comm.call_duration,
            "is_connected": comm.is_connected,
            "is_replied": comm.is_replied,
        })
    
    return {
        "total": query.count(),
        "communications": comms_data
    }


def _calculate_realtime_performance(
    db: Session,
    collector_id: int,
    start_date: date,
    end_date: date,
    queue_ids: Optional[List[int]] = None
) -> CollectorPerformanceStatResponse:
    """实时计算绩效数据（当数据库中没有预计算的统计数据时）"""
    # 查询该催员在指定期间的案件
    cases_query = db.query(Case).filter(
        and_(
            Case.collector_id == collector_id,
            Case.assigned_at >= start_date,
            Case.assigned_at <= end_date
        )
    )
    
    if queue_ids:
        cases_query = cases_query.filter(Case.queue_id.in_(queue_ids))
    
    cases = cases_query.all()
    
    # 计算业绩指标
    assigned_cases = len(cases)
    collected_cases = sum(1 for c in cases if c.case_status in ["normal_settlement", "extension_settlement"])
    case_collection_rate = (collected_cases / assigned_cases * 100) if assigned_cases > 0 else 0
    
    assigned_amount = sum(c.outstanding_amount or 0 for c in cases)
    collected_amount = sum(c.repaid_amount or 0 for c in cases)
    amount_collection_rate = (collected_amount / assigned_amount * 100) if assigned_amount > 0 else 0
    
    # 构建实时统计数据
    return CollectorPerformanceStatResponse(
        id=0,  # 实时数据没有ID
        collector_id=collector_id,
        tenant_id=cases[0].tenant_id if cases else 0,
        agency_id=cases[0].agency_id if cases else 0,
        team_id=cases[0].team_id if cases else 0,
        queue_ids=queue_ids,
        stat_date=end_date,
        stat_period="realtime",
        assigned_cases=assigned_cases,
        collected_cases=collected_cases,
        case_collection_rate=Decimal(str(case_collection_rate)),
        assigned_amount=Decimal(str(assigned_amount)),
        collected_amount=Decimal(str(collected_amount)),
        amount_collection_rate=Decimal(str(amount_collection_rate)),
        ptp_count=0,  # TODO: 计算PTP数据
        ptp_amount=Decimal('0'),
        ptp_fulfilled_count=0,
        ptp_fulfillment_rate=None,
        self_contact_rate=None,  # TODO: 计算沟通率
        total_contact_rate=None,
        self_call_rate=None,
        total_call_rate=None,
        self_connected_rate=None,
        self_wa_contact_rate=None,
        total_wa_contact_rate=None,
        self_wa_reply_rate=None,
        total_wa_reply_rate=None,
        ttfc_median=None,
        ttfc_distribution=None,
        self_number_view_rate=None,
        quality_avg_score=None,
        script_avg_compliance_rate=None,
        violation_count=0,
        created_at=datetime.now(),
        updated_at=datetime.now()
    )

