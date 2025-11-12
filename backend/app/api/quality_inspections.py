"""质检API接口"""
from fastapi import APIRouter, Depends, Query, HTTPException
from sqlalchemy.orm import Session
from sqlalchemy import and_, func
from typing import List, Optional
from datetime import datetime, date

from app.core.database import get_db
from app.models import QualityInspectionRecord, Case, Collector
from app.schemas.dashboard import (
    QualityInspectionRecordCreate,
    QualityInspectionRecordUpdate,
    QualityInspectionRecordResponse,
)

router = APIRouter(prefix="/api/v1/quality-inspections", tags=["质检管理"])


@router.post("/", response_model=QualityInspectionRecordResponse)
def create_quality_inspection(
    inspection: QualityInspectionRecordCreate,
    db: Session = Depends(get_db)
):
    """创建质检记录"""
    # 验证案件和催员是否存在
    case = db.query(Case).filter(Case.id == inspection.case_id).first()
    if not case:
        raise HTTPException(status_code=404, detail="案件不存在")
    
    collector = db.query(Collector).filter(Collector.id == inspection.collector_id).first()
    if not collector:
        raise HTTPException(status_code=404, detail="催员不存在")
    
    # 如果是人工质检，验证质检员是否存在
    if inspection.inspection_type == "manual" and inspection.inspector_id:
        inspector = db.query(Collector).filter(Collector.id == inspection.inspector_id).first()
        if not inspector:
            raise HTTPException(status_code=404, detail="质检员不存在")
    
    # 创建质检记录
    db_inspection = QualityInspectionRecord(**inspection.model_dump())
    db.add(db_inspection)
    db.commit()
    db.refresh(db_inspection)
    
    return db_inspection


@router.get("/", response_model=List[QualityInspectionRecordResponse])
def get_quality_inspections(
    skip: int = 0,
    limit: int = 100,
    case_id: Optional[int] = Query(None, description="案件ID"),
    collector_id: Optional[int] = Query(None, description="催员ID"),
    inspector_id: Optional[int] = Query(None, description="质检员ID"),
    inspection_type: Optional[str] = Query(None, description="质检类型"),
    start_date: Optional[date] = Query(None, description="开始日期"),
    end_date: Optional[date] = Query(None, description="结束日期"),
    db: Session = Depends(get_db)
):
    """查询质检记录"""
    query = db.query(QualityInspectionRecord)
    
    if case_id:
        query = query.filter(QualityInspectionRecord.case_id == case_id)
    
    if collector_id:
        query = query.filter(QualityInspectionRecord.collector_id == collector_id)
    
    if inspector_id:
        query = query.filter(QualityInspectionRecord.inspector_id == inspector_id)
    
    if inspection_type:
        query = query.filter(QualityInspectionRecord.inspection_type == inspection_type)
    
    if start_date:
        query = query.filter(QualityInspectionRecord.inspected_at >= start_date)
    
    if end_date:
        query = query.filter(QualityInspectionRecord.inspected_at < end_date)
    
    # 按质检时间倒序排列
    query = query.order_by(QualityInspectionRecord.inspected_at.desc())
    
    inspections = query.offset(skip).limit(limit).all()
    return inspections


@router.get("/{id}", response_model=QualityInspectionRecordResponse)
def get_quality_inspection(id: int, db: Session = Depends(get_db)):
    """获取质检记录详情"""
    inspection = db.query(QualityInspectionRecord).filter(QualityInspectionRecord.id == id).first()
    if not inspection:
        raise HTTPException(status_code=404, detail="质检记录不存在")
    return inspection


@router.put("/{id}", response_model=QualityInspectionRecordResponse)
def update_quality_inspection(
    id: int,
    inspection_update: QualityInspectionRecordUpdate,
    db: Session = Depends(get_db)
):
    """更新质检记录"""
    db_inspection = db.query(QualityInspectionRecord).filter(QualityInspectionRecord.id == id).first()
    if not db_inspection:
        raise HTTPException(status_code=404, detail="质检记录不存在")
    
    # 更新字段
    update_data = inspection_update.model_dump(exclude_unset=True)
    for key, value in update_data.items():
        setattr(db_inspection, key, value)
    
    db.commit()
    db.refresh(db_inspection)
    
    return db_inspection


@router.get("/collector/{collector_id}", response_model=List[QualityInspectionRecordResponse])
def get_collector_inspections(
    collector_id: int,
    skip: int = 0,
    limit: int = 50,
    inspection_type: Optional[str] = Query(None, description="质检类型"),
    start_date: Optional[date] = Query(None, description="开始日期"),
    end_date: Optional[date] = Query(None, description="结束日期"),
    db: Session = Depends(get_db)
):
    """获取催员的质检历史"""
    query = db.query(QualityInspectionRecord).filter(
        QualityInspectionRecord.collector_id == collector_id
    )
    
    if inspection_type:
        query = query.filter(QualityInspectionRecord.inspection_type == inspection_type)
    
    if start_date:
        query = query.filter(QualityInspectionRecord.inspected_at >= start_date)
    
    if end_date:
        query = query.filter(QualityInspectionRecord.inspected_at < end_date)
    
    # 按质检时间倒序排列
    query = query.order_by(QualityInspectionRecord.inspected_at.desc())
    
    inspections = query.offset(skip).limit(limit).all()
    return inspections


@router.get("/stats/summary")
def get_quality_stats(
    collector_id: int = Query(..., description="催员ID"),
    start_date: Optional[date] = Query(None, description="开始日期"),
    end_date: Optional[date] = Query(None, description="结束日期"),
    db: Session = Depends(get_db)
):
    """质检统计数据"""
    query = db.query(QualityInspectionRecord).filter(
        QualityInspectionRecord.collector_id == collector_id
    )
    
    if start_date:
        query = query.filter(QualityInspectionRecord.inspected_at >= start_date)
    
    if end_date:
        query = query.filter(QualityInspectionRecord.inspected_at < end_date)
    
    inspections = query.all()
    
    if not inspections:
        return {
            "total_inspections": 0,
            "avg_quality_score": 0,
            "avg_script_compliance_rate": 0,
            "total_violations": 0,
            "high_risk_violations": 0,
            "manual_inspections": 0,
            "ai_inspections": 0,
        }
    
    # 计算统计指标
    total_inspections = len(inspections)
    quality_scores = [i.quality_score for i in inspections if i.quality_score is not None]
    script_rates = [i.script_compliance_rate for i in inspections if i.script_compliance_rate is not None]
    
    avg_quality_score = sum(quality_scores) / len(quality_scores) if quality_scores else 0
    avg_script_compliance_rate = sum(script_rates) / len(script_rates) if script_rates else 0
    
    # 统计违规项
    total_violations = 0
    high_risk_violations = 0
    for i in inspections:
        if i.violations:
            total_violations += len(i.violations)
            high_risk_violations += sum(1 for v in i.violations if v.get('type') == 'high_risk')
    
    # 统计质检类型
    manual_inspections = sum(1 for i in inspections if i.inspection_type == 'manual')
    ai_inspections = sum(1 for i in inspections if i.inspection_type == 'ai')
    
    return {
        "total_inspections": total_inspections,
        "avg_quality_score": round(avg_quality_score, 2),
        "avg_script_compliance_rate": round(avg_script_compliance_rate, 2),
        "total_violations": total_violations,
        "high_risk_violations": high_risk_violations,
        "manual_inspections": manual_inspections,
        "ai_inspections": ai_inspections,
    }


@router.delete("/{id}")
def delete_quality_inspection(id: int, db: Session = Depends(get_db)):
    """删除质检记录"""
    db_inspection = db.query(QualityInspectionRecord).filter(QualityInspectionRecord.id == id).first()
    if not db_inspection:
        raise HTTPException(status_code=404, detail="质检记录不存在")
    
    db.delete(db_inspection)
    db.commit()
    
    return {"message": "质检记录已删除", "id": id}

