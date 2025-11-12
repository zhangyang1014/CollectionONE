"""PTP管理API接口"""
from fastapi import APIRouter, Depends, Query, HTTPException
from sqlalchemy.orm import Session
from sqlalchemy import and_, func
from typing import List, Optional
from datetime import datetime, date
from decimal import Decimal

from app.core.database import get_db
from app.models import PTPRecord, Case, Collector
from app.schemas.dashboard import (
    PTPRecordCreate,
    PTPRecordUpdate,
    PTPRecordResponse,
    PTPStatsResponse,
)

router = APIRouter(prefix="/api/v1/ptp", tags=["PTP管理"])


@router.post("/", response_model=PTPRecordResponse)
def create_ptp_record(
    ptp: PTPRecordCreate,
    db: Session = Depends(get_db)
):
    """创建PTP承诺"""
    # 验证案件和催员是否存在
    case = db.query(Case).filter(Case.id == ptp.case_id).first()
    if not case:
        raise HTTPException(status_code=404, detail="案件不存在")
    
    collector = db.query(Collector).filter(Collector.id == ptp.collector_id).first()
    if not collector:
        raise HTTPException(status_code=404, detail="催员不存在")
    
    # 创建PTP记录
    db_ptp = PTPRecord(**ptp.model_dump())
    db_ptp.status = "pending"  # 默认状态为待履约
    
    db.add(db_ptp)
    db.commit()
    db.refresh(db_ptp)
    
    return db_ptp


@router.get("/", response_model=List[PTPRecordResponse])
def get_ptp_records(
    skip: int = 0,
    limit: int = 100,
    case_id: Optional[int] = Query(None, description="案件ID"),
    collector_id: Optional[int] = Query(None, description="催员ID"),
    status: Optional[str] = Query(None, description="PTP状态"),
    start_date: Optional[date] = Query(None, description="承诺日期开始"),
    end_date: Optional[date] = Query(None, description="承诺日期结束"),
    db: Session = Depends(get_db)
):
    """查询PTP列表"""
    query = db.query(PTPRecord)
    
    if case_id:
        query = query.filter(PTPRecord.case_id == case_id)
    
    if collector_id:
        query = query.filter(PTPRecord.collector_id == collector_id)
    
    if status:
        query = query.filter(PTPRecord.status == status)
    
    if start_date:
        query = query.filter(PTPRecord.ptp_date >= start_date)
    
    if end_date:
        query = query.filter(PTPRecord.ptp_date <= end_date)
    
    # 按承诺日期倒序排列
    query = query.order_by(PTPRecord.ptp_date.desc())
    
    ptps = query.offset(skip).limit(limit).all()
    return ptps


@router.get("/{id}", response_model=PTPRecordResponse)
def get_ptp_record(id: int, db: Session = Depends(get_db)):
    """获取PTP记录详情"""
    ptp = db.query(PTPRecord).filter(PTPRecord.id == id).first()
    if not ptp:
        raise HTTPException(status_code=404, detail="PTP记录不存在")
    return ptp


@router.put("/{id}/status", response_model=PTPRecordResponse)
def update_ptp_status(
    id: int,
    ptp_update: PTPRecordUpdate,
    db: Session = Depends(get_db)
):
    """更新PTP状态（履约/违约）"""
    db_ptp = db.query(PTPRecord).filter(PTPRecord.id == id).first()
    if not db_ptp:
        raise HTTPException(status_code=404, detail="PTP记录不存在")
    
    # 更新字段
    update_data = ptp_update.model_dump(exclude_unset=True)
    for key, value in update_data.items():
        setattr(db_ptp, key, value)
    
    # 如果状态为已履约，记录履约时间
    if ptp_update.status == "fulfilled" and not db_ptp.fulfilled_at:
        db_ptp.fulfilled_at = datetime.now()
    
    # 计算履约率
    if ptp_update.actual_payment_amount and db_ptp.ptp_amount:
        db_ptp.fulfillment_rate = (ptp_update.actual_payment_amount / db_ptp.ptp_amount) * 100
    
    db.commit()
    db.refresh(db_ptp)
    
    return db_ptp


@router.get("/stats/summary", response_model=PTPStatsResponse)
def get_ptp_stats(
    collector_id: Optional[int] = Query(None, description="催员ID"),
    case_id: Optional[int] = Query(None, description="案件ID"),
    start_date: Optional[date] = Query(None, description="开始日期"),
    end_date: Optional[date] = Query(None, description="结束日期"),
    db: Session = Depends(get_db)
):
    """PTP统计数据（履约率等）"""
    query = db.query(PTPRecord)
    
    if collector_id:
        query = query.filter(PTPRecord.collector_id == collector_id)
    
    if case_id:
        query = query.filter(PTPRecord.case_id == case_id)
    
    if start_date:
        query = query.filter(PTPRecord.ptp_date >= start_date)
    
    if end_date:
        query = query.filter(PTPRecord.ptp_date <= end_date)
    
    # 获取所有PTP记录
    ptps = query.all()
    
    if not ptps:
        return PTPStatsResponse(
            total_ptp_count=0,
            fulfilled_count=0,
            broken_count=0,
            pending_count=0,
            cancelled_count=0,
            total_ptp_amount=Decimal('0'),
            fulfilled_amount=Decimal('0'),
            fulfillment_rate=Decimal('0')
        )
    
    # 统计各状态数量
    fulfilled_count = sum(1 for p in ptps if p.status == "fulfilled")
    broken_count = sum(1 for p in ptps if p.status == "broken")
    pending_count = sum(1 for p in ptps if p.status == "pending")
    cancelled_count = sum(1 for p in ptps if p.status == "cancelled")
    
    # 统计金额
    total_ptp_amount = sum(p.ptp_amount for p in ptps)
    fulfilled_amount = sum(p.actual_payment_amount or Decimal('0') for p in ptps if p.status == "fulfilled")
    
    # 计算履约率
    fulfillment_rate = (fulfilled_amount / total_ptp_amount * 100) if total_ptp_amount > 0 else Decimal('0')
    
    return PTPStatsResponse(
        total_ptp_count=len(ptps),
        fulfilled_count=fulfilled_count,
        broken_count=broken_count,
        pending_count=pending_count,
        cancelled_count=cancelled_count,
        total_ptp_amount=total_ptp_amount,
        fulfilled_amount=fulfilled_amount,
        fulfillment_rate=fulfillment_rate
    )


@router.delete("/{id}")
def delete_ptp_record(id: int, db: Session = Depends(get_db)):
    """删除PTP记录"""
    db_ptp = db.query(PTPRecord).filter(PTPRecord.id == id).first()
    if not db_ptp:
        raise HTTPException(status_code=404, detail="PTP记录不存在")
    
    db.delete(db_ptp)
    db.commit()
    
    return {"message": "PTP记录已删除", "id": id}

