"""通信记录API接口"""
from fastapi import APIRouter, Depends, Query, HTTPException
from sqlalchemy.orm import Session
from sqlalchemy import and_, func
from typing import List, Optional
from datetime import datetime, date

from app.core.database import get_db
from app.models import CommunicationRecord, Case, Collector
from app.schemas.dashboard import (
    CommunicationRecordCreate,
    CommunicationRecordUpdate,
    CommunicationRecordResponse,
    TTFCStatsResponse,
)

router = APIRouter(prefix="/api/v1/communications", tags=["通信记录"])


@router.post("/", response_model=CommunicationRecordResponse)
def create_communication_record(
    communication: CommunicationRecordCreate,
    db: Session = Depends(get_db)
):
    """创建通信记录（催员触达时调用）"""
    # 验证案件和催员是否存在
    case = db.query(Case).filter(Case.id == communication.case_id).first()
    if not case:
        raise HTTPException(status_code=404, detail="案件不存在")
    
    collector = db.query(Collector).filter(Collector.id == communication.collector_id).first()
    if not collector:
        raise HTTPException(status_code=404, detail="催员不存在")
    
    # 如果是首次触达，计算TTFC
    if communication.ttfc_seconds is None:
        # 查找该案件该催员的首次通信记录
        first_comm = db.query(CommunicationRecord).filter(
            and_(
                CommunicationRecord.case_id == communication.case_id,
                CommunicationRecord.collector_id == communication.collector_id,
            )
        ).first()
        
        # 如果是首次通信，计算TTFC
        if not first_comm and case.assigned_at:
            ttfc_seconds = int((communication.contacted_at - case.assigned_at).total_seconds())
            communication.ttfc_seconds = ttfc_seconds if ttfc_seconds > 0 else 0
    
    # 创建通信记录
    db_communication = CommunicationRecord(**communication.model_dump())
    db.add(db_communication)
    
    # 更新案件的最后联系时间
    case.last_contact_at = communication.contacted_at
    
    db.commit()
    db.refresh(db_communication)
    
    return db_communication


@router.get("/", response_model=List[CommunicationRecordResponse])
def get_communication_records(
    skip: int = 0,
    limit: int = 100,
    case_id: Optional[int] = Query(None, description="案件ID"),
    collector_id: Optional[int] = Query(None, description="催员ID"),
    channel: Optional[str] = Query(None, description="通信渠道"),
    start_date: Optional[date] = Query(None, description="开始日期"),
    end_date: Optional[date] = Query(None, description="结束日期"),
    db: Session = Depends(get_db)
):
    """查询通信记录列表（支持按案件、催员、时间范围筛选）"""
    query = db.query(CommunicationRecord)
    
    if case_id:
        query = query.filter(CommunicationRecord.case_id == case_id)
    
    if collector_id:
        query = query.filter(CommunicationRecord.collector_id == collector_id)
    
    if channel:
        query = query.filter(CommunicationRecord.channel == channel)
    
    if start_date:
        query = query.filter(CommunicationRecord.contacted_at >= start_date)
    
    if end_date:
        query = query.filter(CommunicationRecord.contacted_at < end_date)
    
    # 按时间倒序排列
    query = query.order_by(CommunicationRecord.contacted_at.desc())
    
    communications = query.offset(skip).limit(limit).all()
    return communications


@router.get("/{id}", response_model=CommunicationRecordResponse)
def get_communication_record(id: int, db: Session = Depends(get_db)):
    """获取通信记录详情"""
    communication = db.query(CommunicationRecord).filter(CommunicationRecord.id == id).first()
    if not communication:
        raise HTTPException(status_code=404, detail="通信记录不存在")
    return communication


@router.put("/{id}", response_model=CommunicationRecordResponse)
def update_communication_record(
    id: int,
    communication_update: CommunicationRecordUpdate,
    db: Session = Depends(get_db)
):
    """更新通信记录"""
    db_communication = db.query(CommunicationRecord).filter(CommunicationRecord.id == id).first()
    if not db_communication:
        raise HTTPException(status_code=404, detail="通信记录不存在")
    
    # 更新字段
    update_data = communication_update.model_dump(exclude_unset=True)
    for key, value in update_data.items():
        setattr(db_communication, key, value)
    
    db.commit()
    db.refresh(db_communication)
    
    return db_communication


@router.get("/case/{case_id}", response_model=List[CommunicationRecordResponse])
def get_case_communications(
    case_id: int,
    channel: Optional[str] = Query(None, description="通信渠道筛选"),
    db: Session = Depends(get_db)
):
    """获取案件的所有通信记录"""
    query = db.query(CommunicationRecord).filter(CommunicationRecord.case_id == case_id)
    
    if channel:
        query = query.filter(CommunicationRecord.channel == channel)
    
    # 按时间倒序排列
    query = query.order_by(CommunicationRecord.contacted_at.desc())
    
    communications = query.all()
    return communications


@router.get("/stats/ttfc", response_model=TTFCStatsResponse)
def get_ttfc_stats(
    collector_id: int = Query(..., description="催员ID"),
    start_date: Optional[date] = Query(None, description="开始日期"),
    end_date: Optional[date] = Query(None, description="结束日期"),
    db: Session = Depends(get_db)
):
    """获取TTFC统计数据"""
    query = db.query(CommunicationRecord.ttfc_seconds).filter(
        and_(
            CommunicationRecord.collector_id == collector_id,
            CommunicationRecord.ttfc_seconds.isnot(None)
        )
    )
    
    if start_date:
        query = query.filter(CommunicationRecord.contacted_at >= start_date)
    
    if end_date:
        query = query.filter(CommunicationRecord.contacted_at < end_date)
    
    # 获取所有TTFC值
    ttfc_values = [row[0] for row in query.all()]
    
    if not ttfc_values:
        return TTFCStatsResponse(
            median=0,
            mean=0.0,
            distribution={},
            percentile_25=0,
            percentile_75=0,
            percentile_90=0
        )
    
    ttfc_values.sort()
    n = len(ttfc_values)
    
    # 计算统计指标
    median = ttfc_values[n // 2] if n % 2 == 1 else (ttfc_values[n // 2 - 1] + ttfc_values[n // 2]) // 2
    mean = sum(ttfc_values) / n
    percentile_25 = ttfc_values[int(n * 0.25)]
    percentile_75 = ttfc_values[int(n * 0.75)]
    percentile_90 = ttfc_values[int(n * 0.90)]
    
    # 分箱分布（0-1h, 1-3h, 3-6h, 6-12h, 12-24h, >24h）
    distribution = {
        "0-1h": sum(1 for v in ttfc_values if v < 3600),
        "1-3h": sum(1 for v in ttfc_values if 3600 <= v < 10800),
        "3-6h": sum(1 for v in ttfc_values if 10800 <= v < 21600),
        "6-12h": sum(1 for v in ttfc_values if 21600 <= v < 43200),
        "12-24h": sum(1 for v in ttfc_values if 43200 <= v < 86400),
        ">24h": sum(1 for v in ttfc_values if v >= 86400),
    }
    
    return TTFCStatsResponse(
        median=median,
        mean=mean,
        distribution=distribution,
        percentile_25=percentile_25,
        percentile_75=percentile_75,
        percentile_90=percentile_90
    )

