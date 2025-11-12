"""预警API接口"""
from fastapi import APIRouter, Depends, Query
from sqlalchemy.orm import Session
from sqlalchemy import and_
from typing import List
from datetime import date, datetime, timedelta

from app.core.database import get_db
from app.models import CollectorPerformanceStat, QualityInspectionRecord
from app.schemas.dashboard import AlertResponse

router = APIRouter(prefix="/api/v1/alerts", tags=["预警管理"])


# 预警阈值配置
THRESHOLDS = {
    "contact_rate_low": 60,  # 接通率低于60%
    "reply_rate_low": 40,  # 回复率低于40%
    "ptp_fulfillment_low": 50,  # PTP履约率低于50%
    "ttfc_exceeded": 86400,  # TTFC超过24小时（秒）
    "quality_score_low": 70,  # 质检得分低于70分
}


@router.get("/collector/{collector_id}", response_model=List[AlertResponse])
def get_collector_alerts(
    collector_id: int,
    start_date: date = Query(None, description="开始日期"),
    end_date: date = Query(None, description="结束日期"),
    db: Session = Depends(get_db)
):
    """获取催员的预警信息"""
    if not start_date:
        start_date = date.today() - timedelta(days=7)
    if not end_date:
        end_date = date.today()
    
    alerts = []
    
    # 查询绩效统计数据
    stats = db.query(CollectorPerformanceStat).filter(
        and_(
            CollectorPerformanceStat.collector_id == collector_id,
            CollectorPerformanceStat.stat_date >= start_date,
            CollectorPerformanceStat.stat_date <= end_date
        )
    ).all()
    
    if stats:
        latest_stat = stats[-1]
        
        # 检查接通率
        if latest_stat.self_connected_rate and float(latest_stat.self_connected_rate) < THRESHOLDS["contact_rate_low"]:
            alerts.append(AlertResponse(
                alert_type="low_contact_rate",
                severity="high",
                title="接通率过低",
                description=f"本人电话接通率为 {latest_stat.self_connected_rate}%，低于{THRESHOLDS['contact_rate_low']}%阈值",
                value=float(latest_stat.self_connected_rate),
                threshold=THRESHOLDS["contact_rate_low"],
                recommendation="建议：1) 优化拨打时段 2) 提高拨打频率 3) 尝试其他联系方式"
            ))
        
        # 检查WhatsApp回复率
        if latest_stat.self_wa_reply_rate and float(latest_stat.self_wa_reply_rate) < THRESHOLDS["reply_rate_low"]:
            alerts.append(AlertResponse(
                alert_type="low_reply_rate",
                severity="medium",
                title="WhatsApp回复率过低",
                description=f"WhatsApp回复率为 {latest_stat.self_wa_reply_rate}%，低于{THRESHOLDS['reply_rate_low']}%阈值",
                value=float(latest_stat.self_wa_reply_rate),
                threshold=THRESHOLDS["reply_rate_low"],
                recommendation="建议：1) 优化消息内容 2) 选择合适的发送时间 3) 使用模板消息"
            ))
        
        # 检查PTP履约率
        if latest_stat.ptp_fulfillment_rate and float(latest_stat.ptp_fulfillment_rate) < THRESHOLDS["ptp_fulfillment_low"]:
            alerts.append(AlertResponse(
                alert_type="low_ptp_fulfillment",
                severity="high",
                title="PTP履约率过低",
                description=f"PTP履约率为 {latest_stat.ptp_fulfillment_rate}%，低于{THRESHOLDS['ptp_fulfillment_low']}%阈值",
                value=float(latest_stat.ptp_fulfillment_rate),
                threshold=THRESHOLDS["ptp_fulfillment_low"],
                recommendation="建议：1) 加强承诺前的还款能力评估 2) 在承诺日期前提醒客户 3) 提供多种还款方式"
            ))
        
        # 检查TTFC
        if latest_stat.ttfc_median and latest_stat.ttfc_median > THRESHOLDS["ttfc_exceeded"]:
            hours = latest_stat.ttfc_median / 3600
            alerts.append(AlertResponse(
                alert_type="ttfc_exceeded",
                severity="medium" if hours < 48 else "high",
                title="首次触达时长超标",
                description=f"TTFC中位数为 {hours:.1f}小时，超过24小时SLA",
                value=latest_stat.ttfc_median,
                threshold=THRESHOLDS["ttfc_exceeded"],
                recommendation="建议：1) 收到案件后立即外呼 2) 使用自动拨号工具 3) 优先处理新分配案件"
            ))
        
        # 检查质检得分
        if latest_stat.quality_avg_score and float(latest_stat.quality_avg_score) < THRESHOLDS["quality_score_low"]:
            alerts.append(AlertResponse(
                alert_type="quality_score_low",
                severity="high",
                title="质检得分过低",
                description=f"平均质检得分为 {latest_stat.quality_avg_score}分，低于{THRESHOLDS['quality_score_low']}分阈值",
                value=float(latest_stat.quality_avg_score),
                threshold=THRESHOLDS["quality_score_low"],
                recommendation="建议：1) 熟悉催收话术脚本 2) 参加合规培训 3) 复听优秀录音学习"
            ))
    
    # 查询质检高危项
    recent_inspections = db.query(QualityInspectionRecord).filter(
        and_(
            QualityInspectionRecord.collector_id == collector_id,
            QualityInspectionRecord.inspected_at >= start_date
        )
    ).all()
    
    for inspection in recent_inspections:
        if inspection.violations:
            high_risk_items = [v for v in inspection.violations if v.get('type') == 'high_risk']
            if high_risk_items:
                for item in high_risk_items:
                    alerts.append(AlertResponse(
                        alert_type="quality_violation",
                        severity="high",
                        title=f"质检高危项：{item.get('item', '未知')}",
                        description=item.get('description', ''),
                        value=None,
                        threshold=None,
                        recommendation="请立即整改，避免再次出现类似违规"
                    ))
    
    return alerts


@router.get("/team/{team_id}")
def get_team_alerts(
    team_id: int,
    severity: str = Query(None, description="预警级别筛选：high/medium/low"),
    db: Session = Depends(get_db)
):
    """获取小组的汇总预警信息"""
    # TODO: 实现小组级别的预警汇总
    return {"message": "小组预警功能开发中"}


@router.get("/agency/{agency_id}")
def get_agency_alerts(
    agency_id: int,
    severity: str = Query(None, description="预警级别筛选：high/medium/low"),
    db: Session = Depends(get_db)
):
    """获取机构的汇总预警信息"""
    # TODO: 实现机构级别的预警汇总
    return {"message": "机构预警功能开发中"}

